# Capítulo 3: Filtrado, Rebanado (*Slicing*), Deduplicación y Coincidencia (*Matching*)

> *"En todo motor de datos —ya sea PostgreSQL, Apache Spark o Java Streams— la regla de rendimiento número uno es idéntica: descarta los datos irrelevantes lo más pronto posible."*

En este capítulo cada operación de selección, recorte, deduplicación y búsqueda cuenta con su **ficha técnica completa**: firma oficial de Java 25, mecánica interna en la JVM, diagrama o prueba de escritorio paso a paso, escenarios reales de producción, trampas comunes y ejemplos progresivos.

---

## 3.1 `Stream.filter(Predicate)`: Selección Sin Estado

### Firma oficial
```java
Stream<T> filter(Predicate<? super T> predicate)
```

### ¿Qué hace y cómo funciona por debajo?
Evalúa `predicate.test(elemento)` sobre cada elemento que fluye por la banda transportadora:
* Si retorna `true`, el elemento pasa a la siguiente etapa.
* Si retorna `false`, se descarta inmediatamente y **nunca** llega a operaciones posteriores (`map`, `sorted`, `distinct`).
* Es **intermedia**, **perezosa** y **sin estado** ($O(1)$ de memoria). **Elimina la bandera `SIZED`** del `Spliterator` porque ya no se conoce cuántos elementos sobrevivirán.

```java
// Puente Imperativo -> Stream:
List<Employee> topEarners = employees.stream()
        .filter(Objects::nonNull)               // Protección contra null
        .filter(e -> e.getSalary() > 250_000.0) // Regla de negocio
        .toList();
```

### Escenarios reales de uso
1. **Principio *Predicate Push-Down*:** Coloca tus `.filter()` siempre **antes** de `.sorted()`, `.distinct()` o `.map()` costosos, y pon primero el filtro más barato y más selectivo.
2. **Filtros dinámicos en APIs REST:** Componer predicados opcionales partiendo de la identidad neutra `_ -> true`:
   ```java
   record FilterCriteria(String department, Double minSalary, String language) {}

   public List<Employee> search(List<Employee> employees, FilterCriteria c) {
       Predicate<Employee> pred = _ -> true;
       if (c.department() != null) pred = pred.and(e -> c.department().equalsIgnoreCase(e.getDepartment()));
       if (c.minSalary() != null)  pred = pred.and(e -> e.getSalary() >= c.minSalary());
       if (c.language() != null)   pred = pred.and(e -> c.language().equalsIgnoreCase(e.getProgrammingLanguage()));
       return employees.stream().filter(Objects::nonNull).filter(pred).toList();
   }
   ```

---

## 3.2 `Stream.skip(long n)` y `Stream.limit(long maxSize)`: Paginación y Recorte

### Firmas oficiales
```java
Stream<T> skip(long n)       // Descarta los primeros n elementos (lanza IllegalArgumentException si n < 0)
Stream<T> limit(long maxSize) // Cortocircuito: detiene el pipeline tras emitir maxSize elementos
```

### ¿Cómo funcionan por debajo?
* **`skip(n)`** lleva un contador interno; descarta los primeros `n` elementos y deja pasar todos los demás. Si el Stream tiene $\le n$ elementos, retorna un Stream vacío (jamás lanza `IndexOutOfBoundsException`).
* **`limit(maxSize)`** es una operación **con estado acotado y de cortocircuito**: en cuanto emite `maxSize` elementos, activa `cancellationRequested()` en el `Sink` y detiene inmediatamente el upstream (incluso sobre Streams infinitos).

### Escenario real: Paginación (Fórmula universal)
```java
int pageIndex = 2; // Página 3 (base 0)
int pageSize  = 8;

List<Product> page3 = products.stream()
        .skip((long) pageIndex * pageSize) // Salta 16 elementos
        .limit(pageSize)                   // Emite 8 y CORTA el flujo
        .toList();
```

> **Error Común de Programación (Invertir `limit` y `skip`):**
> `.limit(8).skip(16)` siempre produce `[]`, porque `limit(8)` solo deja pasar 8 elementos y `skip(16)` los descarta todos. En paginación, **`skip` va siempre ANTES de `limit`**.

---

## 3.3 `Stream.takeWhile(Predicate)` y `Stream.dropWhile(Predicate)` (Java 9+)

### Firmas oficiales
```java
default Stream<T> takeWhile(Predicate<? super T> predicate)
default Stream<T> dropWhile(Predicate<? super T> predicate)
```

### ¿Por qué existen si ya existe `filter()`? (Prueba de escritorio)
`filter()` siempre evalúa **el 100%** de los elementos. En secuencias ya ordenadas (logs por timestamp, precios ordenados, edades ordenadas), `takeWhile` y `dropWhile` operan exclusivamente sobre el **prefijo inicial**:

* **`takeWhile(p)`**: emite mientras `p` sea `true`; en el **primer `false`**, hace **cortocircuito inmediato** y cierra el Stream.
* **`dropWhile(p)`**: descarta mientras `p` sea `true`; en el **primer `false`**, abre la compuerta y emite ese elemento y **todos los restantes sin volver a evaluar `p`**.

| Entrada (`h`) | `filter(h < 17)` | `takeWhile(h < 17)` | `dropWhile(h < 17)` |
| :---: | :--- | :--- | :--- |
| `8, 9, 10, 15` | `true` → emite | `true` → emite | `true` → descarta |
| **`17`** | `false` → descarta | **`false` → ¡STOP!** | **`false` → abre compuerta, emite `17`** |
| `18, 12, 19` | evalúa todos (`12` pasa) | *(jamás se leen)* | *(sin evaluar `p`)* emite `18, 12, 19` |
| **Resultado** | `[8, 9, 10, 15, 12]` | `[8, 9, 10, 15]` | `[17, 18, 12, 19]` |

### Escenario real: Extraer una ventana de rango en datos ordenados
```java
// Empleados entre 30 y 40 años en una lista ya ordenada por edad (sin leer > 40 años):
List<Employee> thirties = sortedByAge.stream()
        .dropWhile(e -> e.getAge() < 30)
        .takeWhile(e -> e.getAge() <= 40)
        .toList();
```

> **Error Común de Programación:** Sobre fuentes sin bandera `ORDERED` (como `HashSet`), `takeWhile`/`dropWhile` devuelven un subconjunto no determinista. Úsalos solo en streams ordenados.

---

## 3.4 `Stream.distinct()`: Deduplicación Con Estado

### Firma oficial
```java
Stream<T> distinct()
```

### ¿Cómo funciona por debajo?
* Es una operación intermedia **con estado** ($O(U)$ memoria donde $U$ es el número de elementos únicos).
* En secuencial, mantiene internamente un **`HashSet<T>`** y compara usando **`equals()` y `hashCode()`**. Si la fuente es `ORDERED`, garantiza preservar la **primera aparición**.
* Si la fuente ya tiene la característica `Spliterator.DISTINCT` (`HashSet`, `TreeSet`), la JVM lo convierte en *no-op*.

### Escenario real: Deduplicar por un atributo específico (`distinctBy`)
Cuando quieres el primer `Employee` completo de cada departamento:
```java
// 100% puro y seguro en paralelo (con variable anónima '_' de Java 22–25):
List<Employee> firstPerDept = new ArrayList<>(employees.stream()
        .collect(Collectors.toMap(
                Employee::getDepartment,
                Function.identity(),
                (first, _) -> first,
                LinkedHashMap::new
        )).values());
// En Java 25 también puedes hacerlo perezosamente con un Gatherer distinctBy (Capítulo 16).
```

---

## 3.5 Operaciones Terminales de Coincidencia y Búsqueda (Cortocircuito)

Todas estas operaciones son **terminales** y **cortocircuitan** en cuanto el resultado queda determinado:

### 1. `boolean anyMatch(Predicate<? super T> predicate)`
* **Qué hace:** Devuelve `true` si **al menos un** elemento cumple el predicado. Se detiene en el primer `true`. En Stream vacío devuelve **`false`**.
* **Escenario real:** Verificar si existe al menos un error crítico en un log, o si algún empleado trabaja en `"New York"`. Reemplaza siempre `filter(p).count() > 0`.

### 2. `boolean allMatch(Predicate<? super T> predicate)`
* **Qué hace:** Devuelve `true` si **todos** los elementos cumplen el predicado. Se detiene en el primer `false`.
* **Trampa (*Vacuous Truth*):** En Stream vacío devuelve **`true`** ($\forall x \in \emptyset: P(x)$). Valida `!list.isEmpty()` si requieres al menos un elemento.

### 3. `boolean noneMatch(Predicate<? super T> predicate)`
* **Qué hace:** Devuelve `true` si **ningún** elemento cumple el predicado (equivale a `!anyMatch(p)`). Se detiene en el primer `true` retornando `false`. En Stream vacío devuelve **`true`**.
* **Escenario real:** Validar ausencia de productos con rating `< 4.0`, o prueba de primalidad:
  ```java
  IntPredicate isPrime = n -> n >= 2 &&
          IntStream.rangeClosed(2, (int) Math.sqrt(n)).noneMatch(d -> n % d == 0);
  ```

### 4. `Optional<T> findFirst()` vs. `Optional<T> findAny()`
* **`findFirst()`**: Devuelve el **primer** elemento en orden de encuentro envuelto en `Optional<T>` (o `Optional.empty()` si no hay coincidencias). En paralelo debe coordinar particiones para respetar el orden.
* **`findAny()`**: Devuelve **cualquier** elemento coincidente tan pronto como cualquier hilo lo encuentra. En `parallelStream()` ofrece el máximo rendimiento cuando el orden no importa.

```java
// Ejemplo: detectar violación de SLO (3 lecturas seguidas > 100 ms) con cortocircuito en Java 25
boolean sloBreach = latencies.stream()
        .gather(Gatherers.windowSliding(3))
        .anyMatch(window -> window.stream().allMatch(ms -> ms > 100));
```

---

## 3.6 Retos de Dominio y Autoevaluación (Capítulo 3)

1. **Nivel Inicial:** Implementa `paginate(List<T> items, int pageIndex, int pageSize)` usando `skip` y `limit`, y explica qué sucede si inviertes su orden.
2. **Nivel Intermedio:** Dada una lista de transacciones ordenadas por fecha, extrae únicamente las de marzo a junio usando `dropWhile` + `takeWhile` sin evaluar el resto del año.
3. **Nivel Avanzado:** Escribe un generador de números primos menores a 10,000 usando `IntStream.rangeClosed` y `noneMatch`, y explica la trampa de la *vacuous truth* en `allMatch` sobre colecciones vacías.
4. **Nivel Staff MAANG:** Deduplica empleados conservando el primero de cada departamento sin mutar estado externo, y detecta violaciones de SLO (3 lecturas > 100 ms) con `Gatherers.windowSliding(3)`.
