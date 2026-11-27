# Capítulo 7: Recolección de Resultados — Enciclopedia de `collect()` y `Collectors` (Java 25 LTS)

> *"Si `reduce()` es una prensa que compacta datos en un valor inmutable, `collect()` es una fábrica de ensamblaje que llena contenedores mutables a velocidad lineal $O(N)$ y con total seguridad entre hilos."*

En este capítulo cada operación terminal de recolección tiene su propia **ficha técnica completa**: firma oficial de Java 25, explicación teórica desde cero, mecánica interna, escenarios reales de uso, trampas comunes y ejemplos progresivos.

---

## 7.1 `Stream.collect(supplier, accumulator, combiner)`: Reducción Mutable Desde Cero

### Firma oficial
```java
<R> R collect(Supplier<R> supplier,
              BiConsumer<R, ? super T> accumulator,
              BiConsumer<R, R> combiner)
```

### ¿Qué hace y por qué existe frente a `reduce()`?
Construir una lista creando copias con `reduce()` cuesta $1 + 2 + \dots + N = O(N^2)$ copias en memoria. **`collect()`** resuelve esto con **reducción mutable aislada por hilo** en tiempo lineal $O(N)$:
1. **`supplier`**: crea un contenedor mutable vacío nuevo (en paralelo, uno privado para cada hilo).
2. **`accumulator`**: inserta cada elemento `T` en el contenedor local en $O(1)$ amortizado.
3. **`combiner`**: en paralelo, fusiona dos contenedores parciales al terminar.

```text
Hilo 1: supplier() -> ArrayList local 1 -> add(A), add(B)  ─┐
                                                            ├── combiner: local1.addAll(local2) -> O(N)
Hilo 2: supplier() -> ArrayList local 2 -> add(C), add(D)  ─┘
```

### Escenario real: `StringJoiner` y `Map` que acepta valores `null`
```java
// Ejemplo 1: Unir ciudades únicas en un StringJoiner con prefijo y sufijo
StringJoiner joiner = employees.stream()
        .map(Employee::getCity).distinct().sorted()
        .collect(
                () -> new StringJoiner("; ", "[", "]"),
                StringJoiner::add,
                StringJoiner::merge
        );

// Ejemplo 2: Recolectar un HashMap cuando los valores pueden ser null (Collectors.toMap lanzaría NPE!)
Map<Integer, String> idToNullableCity = employees.stream()
        .collect(
                HashMap::new,
                (map, e) -> map.put(e.getId(), e.getCity()),
                HashMap::putAll
        );
```

---

## 7.2 Recolectar a Listas y Conjuntos: Ficha Técnica de Cada Método

### 1. `Stream.toList()` (Java 16+) vs. `Collectors.toList()` vs. `Collectors.toUnmodifiableList()`
* **`List<T> toList()`** *(método directo de `Stream`, Java 16+)*:
  * **Qué hace:** Recolecta en una lista **inmodificable** que **sí acepta `null`**. Es más rápida y compacta que `Collectors.toUnmodifiableList()` porque envuelve el arreglo interno directamente sin copias extra.
  * **Cuándo usarla:** Es el **estándar por defecto** en Java 25 siempre que no necesites mutar la lista resultante.
  * **Trampa:** Llamar `.add()`, `.remove()` o `.sort()` sobre el resultado lanza `UnsupportedOperationException`.
* **`Collectors.toList()`** *(Java 8)*:
  * **Qué hace:** Actualmente devuelve `ArrayList`, pero la especificación oficial **no garantiza** mutabilidad ni tipo concreto. Úsala solo si código legado lo exige.
* **`Collectors.toUnmodifiableList()`** *(Java 10+)*:
  * **Qué hace:** Devuelve una lista inmodificable que **rechaza elementos `null`** lanzando `NullPointerException` (*fail-fast*). Úsala cuando quieres prohibir explícitamente nulos en la salida.

### 2. `Collectors.toSet()` vs. `Collectors.toUnmodifiableSet()`
* **`Collectors.toSet()`**: Recolecta en un `HashSet` eliminando duplicados en $O(1)$ promedio (sin garantía de orden).
* **`Collectors.toUnmodifiableSet()`** *(Java 10+)*: Conjunto inmodificable que rechaza `null` (lanza NPE) y prohíbe mutaciones.

### 3. `Collectors.toCollection(Supplier<C> collectionFactory)`
* **Firma:** `<T, C extends Collection<T>> Collector<T, ?, C> toCollection(Supplier<C> collectionFactory)`
* **Para qué sirve:** Te permite elegir la **implementación exacta** de colección que necesitas:
  * `ArrayList::new`: cuando **sí necesitas** una lista garantizadamente mutable.
  * `LinkedHashSet::new`: deduplica en $O(1)$ **preservando el orden de encuentro** y te da un **`SequencedSet`** ([JEP 431](https://openjdk.org/jeps/431)) con `getFirst()`, `getLast()` y `reversed()`.
  * `TreeSet::new`: deduplica y ordena automáticamente en $O(N \log N)$.
  * `ArrayDeque::new` / `LinkedList::new`: cuando necesitas operaciones de cola o pila.

```java
// SequencedSet preservando orden de aparición (Java 21–25):
SequencedSet<String> orderedDepts = employees.stream()
        .map(Employee::getDepartment)
        .collect(Collectors.toCollection(LinkedHashSet::new));
String first = orderedDepts.getFirst();
String last  = orderedDepts.getLast();
```

---

## 7.3 Recolectar a Mapas: `toMap()`, `toUnmodifiableMap()` y `toConcurrentMap()`

### 1. `Collectors.toMap(...)` (Las 3 sobrecargas)
```java
// Sobrecarga 1 (2 args): SOLO para claves 100% únicas
toMap(Function<T, K> keyMapper, Function<T, V> valueMapper)

// Sobrecarga 2 (3 args): con función de resolución de colisiones (mergeFunction)
toMap(Function<T, K> keyMapper, Function<T, V> valueMapper, BinaryOperator<V> mergeFunction)

// Sobrecarga 3 (4 args): con fábrica de Map específica (TreeMap, LinkedHashMap)
toMap(Function<T, K> keyMapper, Function<T, V> valueMapper, BinaryOperator<V> mergeFunction, Supplier<M> mapFactory)
```

* **Cómo funciona por debajo:** Por cada elemento extrae `(k, v)` e invoca `map.merge(k, v, mergeFunction)`.
* **Trampas críticas:**
  1. En la sobrecarga de 2 argumentos, cualquier clave duplicada lanza **`IllegalStateException: Duplicate key`**.
  2. Si `valueMapper` retorna **`null`**, `Map.merge` lanza **`NullPointerException`** (incluso en un `HashMap`). Usa `collect(HashMap::new, ...)` de la sección 7.1 si los valores pueden ser `null`.

```java
// 1. Clave única -> Function.identity() guarda el objeto completo
Map<Integer, Employee> byId = employees.stream()
        .collect(Collectors.toMap(Employee::getId, Function.identity()));

// 2. Resolución de colisiones conservando el primero con variable anónima '_' (JEP 456)
Map<String, String> firstByCity = employees.stream()
        .collect(Collectors.toMap(Employee::getCity, Employee::getFullName, (existing, _) -> existing));

// 3. TreeMap ordenado alfabéticamente con el mejor pagado por departamento
SequencedMap<String, Employee> topByDept = employees.stream()
        .collect(Collectors.toMap(
                Employee::getDepartment,
                Function.identity(),
                BinaryOperator.maxBy(Comparator.comparingDouble(Employee::getSalary)),
                TreeMap::new
        ));
```

### 2. `Collectors.toUnmodifiableMap(...)` (Java 10+)
* **Para qué sirve:** Crea un mapa estrictamente inmodificable (2 o 3 argumentos). Rechaza claves y valores `null` (*fail-fast*). Ideal para tablas de configuración, catálogos en caché y constantes de dominio.

### 3. `Collectors.toConcurrentMap(...)`
* **Para qué sirve:** Igual que `toMap`, pero declara `CONCURRENT | UNORDERED` y escribe directamente en un `ConcurrentHashMap`. En `parallelStream()` desordenado evita el costo de crear y fusionar múltiples `HashMap` locales.

---

## 7.4 `Collectors.joining(...)`: Concatenación de Texto en $O(N)$

### Firmas oficiales (3 sobrecargas)
```java
Collector<CharSequence, ?, String> joining()
Collector<CharSequence, ?, String> joining(CharSequence delimiter)
Collector<CharSequence, ?, String> joining(CharSequence delimiter, CharSequence prefix, CharSequence suffix)
```

* **Por qué existe frente a `reduce((a, b) -> a + "," + b)`:** Concatenar `String` con `+` dentro de `reduce()` crea un objeto `String` nuevo en cada paso ($O(N^2)$ en memoria). `Collectors.joining()` usa internamente un **`StringBuilder`** (o **`StringJoiner`**), logrando tiempo lineal **$O(N)$** y manejando automáticamente delimitadores sin comas sobrantes al inicio o al final.
* **Escenarios reales:** Generar líneas CSV, cláusulas SQL `IN ('A', 'B', 'C')`, encabezados JSON o logs legibles.

```java
String csv  = List.of("Java", "Go", "Rust").stream().collect(Collectors.joining(", "));
String json = List.of("A", "B", "C").stream().collect(Collectors.joining("\", \"", "[\"", "\"]"));
// json -> ["A", "B", "C"]
```

---

## 7.5 `Collectors.collectingAndThen(...)`: Post-Procesamiento al Finalizar

### Firma oficial
```java
public static <T, A, R, RR> Collector<T, A, RR> collectingAndThen(
    Collector<T, A, R> downstream,
    Function<R, RR> finisher)
```

### ¿Qué hace y cuándo usarlo?
Envuelve cualquier collector `downstream` y ejecuta una función final `finisher` **una sola vez** sobre el resultado ya recolectado `R` para transformarlo en `RR`:

```text
Stream<T> ──▶ downstream Collector ──▶ resultado R ──▶ finisher(R) ──▶ resultado final RR
```

### Escenarios reales de uso
1. **Desempacar un `Optional`** devuelto por `maxBy`/`minBy` dentro de `groupingBy`.
2. **Sellar inmutabilidad** sobre colecciones personalizadas (`Collections::unmodifiableList`, `List::copyOf`).
3. **Formatear un valor agregado** (ej. convertir un `Double` promedio a cadena `"$185,000.00"`).
4. **Ordenar y recortar Top-K** dentro de cada cubeta de un `groupingBy`.

```java
String avgBanner = employees.stream()
        .collect(Collectors.collectingAndThen(
                Collectors.averagingDouble(Employee::getSalary),
                avg -> String.format(Locale.US, "Promedio salarial: $%,.2f", avg)
        ));
```

---

## 7.6 `Collectors.teeing(...)` (Java 12+): Dos Recolectores en Una Sola Pasada

### Firma oficial
```java
public static <T, R1, R2, R> Collector<T, ?, R> teeing(
    Collector<? super T, ?, R1> downstream1,
    Collector<? super T, ?, R2> downstream2,
    BiFunction<? super R1, ? super R2, R> merger)
```

### ¿Qué hace y cómo funciona por debajo?
Envía cada elemento del Stream simultáneamente a **dos collectors independientes** (`downstream1` y `downstream2`) en una **única pasada $O(N)$** —sin tener que abrir el Stream dos veces— y al terminar combina ambos resultados `R1` y `R2` mediante `merger`.

```text
                  ┌──▶ downstream1 (minBy salario) ──▶ R1 (minOpt) ─┐
Stream<Employee>──┤                                                 ├──▶ merger(R1, R2) -> SalarySpread
                  └──▶ downstream2 (maxBy salario) ──▶ R2 (maxOpt) ─┘
```

### Escenarios reales de uso
* Calcular `min` y `max` (y su diferencia *spread*) en una pasada.
* Calcular promedio exacto en `BigDecimal` combinando `reducing(BigDecimal.ZERO, BigDecimal::add)` + `counting()`.
* Separar conteo total y conteo de errores en un solo `record` de métricas.

```java
record SalarySpread(double min, double max, double spread) {}

SalarySpread report = employees.stream()
        .collect(Collectors.teeing(
                Collectors.minBy(Comparator.comparingDouble(Employee::getSalary)),
                Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary)),
                (minOpt, maxOpt) -> {
                    double min = minOpt.map(Employee::getSalary).orElse(0.0);
                    double max = maxOpt.map(Employee::getSalary).orElse(0.0);
                    return new SalarySpread(min, max, max - min);
                }
        ));
```

---

## 7.7 Retos de Dominio y Autoevaluación (Capítulo 7)

1. **Nivel Inicial:** Recolecta nombres de departamentos únicos en un `SequencedSet<String>` (`LinkedHashSet::new`) preservando el orden de aparición y consulta `.getFirst()` y `.getLast()`.
2. **Nivel Intermedio:** Construye un `TreeMap<String, Employee>` que almacene al empleado mejor pagado de cada departamento resolviendo colisiones con `BinaryOperator.maxBy`.
3. **Nivel Avanzado:** Recolecta un `Map<Integer, String>` desde `Employee` donde el valor (`city`) puede ser `null` sin lanzar `NullPointerException`, y explica por qué `Collectors.toMap` fallaría en ese caso.
4. **Nivel Staff MAANG:** Usa `Collectors.teeing()` para calcular simultáneamente el salario mínimo, el máximo y el *spread* en una sola pasada $O(N)$ del Stream.
