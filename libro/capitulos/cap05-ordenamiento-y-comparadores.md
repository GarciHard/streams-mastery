# Capítulo 5: Ordenamiento, Comparadores, Estabilidad y Recorrido Inverso $O(1)$ (Java 25 LTS)

> *"Ordenar parece una línea inofensiva (`.sorted()`), pero es una de las operaciones más caras en CPU y memoria de toda la API de Streams. Saber cuándo usarla —y cuándo evitarla— separa a un junior de un ingeniero Staff."*

En este capítulo cada operación de ordenamiento y cada fábrica de `Comparator` tiene su **ficha técnica completa**: firma oficial de Java 25, mecánica interna (TimSort, estabilidad, barrera *stateful*), escenarios reales, trampas de `null` y de `.reversed()`, y la optimización $O(1)$ de `SequencedCollection.reversed().stream()`.

---

## 5.1 Fundamentos Desde Cero: `Comparable<T>` vs. `Comparator<T>`

* **`Comparable<T>` (Orden natural intrínseco):** Una clase define su orden predeterminado implementando `int compareTo(T other)` (como `String`, `Integer`, `Double`, `BigDecimal`, `LocalDate`). Las entidades de dominio como `Employee`, `Product` u `Order` normalmente **no** implementan `Comparable`.
* **`Comparator<T>` (Orden externo flexible):** `@FunctionalInterface` con método SAM `int compare(T a, T b)`: devuelve `< 0` si `a` va antes que `b`, `0` si empatan, `> 0` si `a` va después.

---

## 5.2 `Stream.sorted()` y `Stream.sorted(Comparator)`: La Barrera Con Estado

### Firmas oficiales
```java
Stream<T> sorted()                                 // Exige que T implemente Comparable<T> (si no -> ClassCastException)
Stream<T> sorted(Comparator<? super T> comparator) // Usa el orden definido por comparator
```

### ¿Cómo funciona por debajo?
`sorted()` es una **barrera *stateful* total**: no puede emitir ni un solo elemento hasta haber consumido y copiado **el 100%** de los datos en un búfer temporal `Object[]` en el *heap*:

```text
Fuente ──▶ filter() ──▶ [ BARRERA sorted(): copia N elementos a Object[] ] ──▶ TimSort O(N log N) ──▶ salida
                        (Memoria: O(N) | Nunca usar en Streams infinitos sin limit() antes!)
```

* **En `Stream<T>` (objetos):** Usa **TimSort** ($O(N \log N)$ peor caso, $O(N)$ si ya viene casi ordenado) y **garantiza estabilidad** (elementos empatados conservan su orden relativo original).
* **En `IntStream`/`LongStream`/`DoubleStream`:** Usa **Dual-Pivot Quicksort** (no necesita estabilidad porque dos enteros iguales son indistinguibles).
* **Optimización `SORTED`:** Si la fuente ya tiene bandera `Spliterator.SORTED` (como `TreeSet`) y llamas a `.sorted()` sin argumentos, el motor la omite en $O(1)$.

---

## 5.3 Enciclopedia de Fábricas y Combinadores de `Comparator<T>`

### 1. `Comparator.naturalOrder()` y `Comparator.reverseOrder()`
* **Para qué sirven:** Orden ascendente o descendente natural para tipos que implementan `Comparable` (`String`, `Integer`, `LocalDate`).
* **Ejemplo:** `names.stream().sorted(Comparator.reverseOrder()).toList()`.

### 2. `Comparator.comparing(...)` y Especializaciones Primitivas (`comparingInt`, `comparingLong`, `comparingDouble`)
* **Firmas:**
  * `comparing(Function<? super T, ? extends U> keyExtractor)` (con sobrecarga opcional `Comparator<? super U> keyComparator`)
  * `comparingInt(ToIntFunction<? super T> keyExtractor)` — **cero *boxing***
  * `comparingLong(ToLongFunction<? super T> keyExtractor)` — **cero *boxing***
  * `comparingDouble(ToDoubleFunction<? super T> keyExtractor)` — **cero *boxing***
* **Por qué usar `comparingInt/Long/Double`:** Evitan envolver cada número en `Integer`/`Double` durante las $O(N \log N)$ comparaciones de TimSort.
* **Trampa fatal:** Jamás escribas `(a, b) -> a.getAge() - b.getAge()`; con números grandes provoca *integer overflow* y corrompe TimSort. Usa siempre `Comparator.comparingInt(...)`.

### 3. `.thenComparing(...)`, `.thenComparingInt(...)`, `.thenComparingLong(...)`, `.thenComparingDouble(...)`
* **Para qué sirven:** Definen criterios de desempate (2.º, 3.er y 4.º nivel) que solo se evalúan cuando el criterio anterior devolvió `0`.

### 4. `.reversed()`: Inversión y la Trampa de Alcance
* **Qué hace:** Invierte **todo** el comparador acumulado hasta ese punto.
* **Error Común de Programación:**
  * `Comparator.comparing(Employee::getDepartment).thenComparingDouble(Employee::getSalary).reversed()` invierte **tanto el departamento como el salario**.
  * Para invertir **solo** el salario, envuélvelo explícitamente:
    ```java
    Comparator<Employee> staffRank = Comparator
            .comparing(Employee::getDepartment)                                        // 1. Depto ASC
            .thenComparing(Comparator.comparingDouble(Employee::getSalary).reversed()) // 2. Salario DESC
            .thenComparingInt(Employee::getAge)                                        // 3. Edad ASC
            .thenComparing(Employee::getLastName);                                     // 4. Apellido ASC
    ```

### 5. `Comparator.nullsFirst(Comparator)` y `Comparator.nullsLast(Comparator)`
* **Para qué sirven:** Protegen contra `NullPointerException` en **dos niveles distintos**:
  1. **Nivel 1 (Elementos `null` en la colección):** `Comparator.nullsFirst(Comparator.naturalOrder())`
  2. **Nivel 2 (Campo clave `null` dentro de un objeto):** se pasa como **segundo argumento** de `Comparator.comparing(...)`:
     ```java
     List<Employee> byCitySafe = employees.stream()
             .sorted(Comparator.comparing(
                     Employee::getCity,
                     Comparator.nullsLast(Comparator.naturalOrder())))
             .toList();
     ```

### 6. `String.CASE_INSENSITIVE_ORDER` y Prioridades Personalizadas
```java
// Prioridad de negocio personalizada mediante mapa de rangos:
Map<String, Integer> deptRank = Map.of("Engineering", 1, "Data Science", 2, "Product", 3);
List<Employee> byPriority = employees.stream()
        .sorted(Comparator
                .comparingInt((Employee e) -> deptRank.getOrDefault(e.getDepartment(), Integer.MAX_VALUE))
                .thenComparing(Comparator.comparingDouble(Employee::getSalary).reversed()))
        .toList();
```

---

## 5.4 `SequencedCollection.reversed().stream()` ([JEP 431](https://openjdk.org/jeps/431)) y Anti-Patrones MAANG

| Necesidad | Enfoque Costoso | Enfoque Óptimo Java 25 / MAANG |
| :--- | :--- | :--- |
| **Invertir una `List` o `LinkedHashSet`** | `.sorted(Comparator.reverseOrder())` ($O(N \log N)$, $O(N)$ RAM) | **`collection.reversed().stream()`** (**$O(1)$ arranque, 0 copias**) |
| **Obtener el mínimo o máximo (Top-1)** | `.sorted(comp).findFirst()` ($O(N \log N)$, $O(N)$ RAM) | **`.min(comp)` / `.max(comp)`** (**$O(N)$ tiempo, $O(1)$ RAM**) |
| **Filtrar y ordenar** | `.sorted(comp).filter(p)` | **`.filter(p).sorted(comp)`** (*predicate push-down*) |
| **Top-K en millones ($K \ll N$)** | `.sorted(comp).limit(k)` ($O(N \log N)$) | **Custom `Collector` con `PriorityQueue`** ($O(N \log K)$, Cap. 11) |

---

## 5.5 Retos de Dominio y Autoevaluación (Capítulo 5)

1. **Nivel Inicial:** Ordena una lista de `Product` por precio ascendente y, en empate, por rating descendente sin *boxing*.
2. **Nivel Intermedio:** Ordena una lista de `Employee` donde tanto elementos de la lista como el campo `city` pueden ser `null`, colocando los `null` siempre al final sin lanzar `NullPointerException`.
3. **Nivel Avanzado:** Implementa un ordenamiento por prioridad de negocio (`Map<String, Integer>`) y demuestra por qué `.comparing(A).thenComparing(B).reversed()` invierte ambos criterios.
4. **Nivel Staff MAANG:** Compara la complejidad temporal y espacial de `list.stream().sorted(Comparator.reverseOrder())` frente a `list.reversed().stream()` ([JEP 431](https://openjdk.org/jeps/431)) y de `sorted().findFirst()` frente a `min()`.
