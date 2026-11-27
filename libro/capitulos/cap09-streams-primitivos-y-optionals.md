# Capítulo 9: Streams Primitivos (`IntStream`, `LongStream`, `DoubleStream`), Memoria JVM ([JEP 519](https://openjdk.org/jeps/519)) y Enciclopedia Completa de `Optional`

> *"Si tu pipeline envuelve primitivos en objetos dentro de un bucle caliente, estás quemando memoria, rompiendo la caché L1 de tu CPU y saturando el Garbage Collector. Y si devuelves `null` en lugar de `Optional`, estás sembrando una bomba de tiempo para las 3 de la mañana."*

En este capítulo cada método de los Streams primitivos y **absolutamente cada método de la mónada `Optional<T>`** tiene su propia **ficha técnica completa**: firma oficial de Java 25, explicación teórica desde cero, diagrama de memoria o flujo, escenarios reales de producción, trampas comunes y ejemplos progresivos.

---

## 9.1 Física de la Memoria JVM: Por Qué Existen los Streams Primitivos

Un `int` ocupa **4 bytes** contiguos en memoria. Un objeto `java.lang.Integer` en un `Stream<Integer>` vive en el *heap* con cabecera de objeto + relleno de alineación + puntero (*pointer chasing*):

```text
1) int[] (IntStream) — Contiguo en memoria:
   [ 10 | 20 | 30 | 40 | 50 | 60 | ... ]  -> 4 bytes por entero.
   1 línea de caché L1 (64 bytes) carga 16 enteros en UNA sola lectura de RAM!

2) Integer[] (Stream<Integer>) — Indirección + Cache Misses:
   [ ptr1 | ptr2 | ptr3 | ptr4 ]  -> Cada puntero salta a una dirección distinta en RAM
      │      │      │      │
      ▼      ▼      ▼      ▼
   +------------------+  Layout clásico: 12B cabecera + 4B int = 16 bytes por Integer.
   | Cabecera (8/12B) |  Java 25 JEP 519 (-XX:+UseCompactObjectHeaders): cabecera baja a 8B
   | int value (4B)   |  (~22% menos heap, 8% menos CPU y 15% menos GCs en SPECjbb2015).
   +------------------+
```

> **Tip de Rendimiento JVM / MAANG:** Incluso con [JEP 519](https://openjdk.org/jeps/519) activado en Java 25, `Stream<Integer>` sigue costando **4× a 5× más memoria e indirección** que `IntStream`. Usa siempre `IntStream`, `LongStream` o `DoubleStream` para todo cómputo numérico.

---

## 9.2 Fichas Técnicas de Operaciones Clave en `IntStream`, `LongStream` y `DoubleStream`

### 1. `range(startInclusive, endExclusive)` y `rangeClosed(startInclusive, endInclusive)`
* **Firmas:** `IntStream.range(int, int)`, `IntStream.rangeClosed(int, int)` (y sus equivalentes en `LongStream`; no existen en `DoubleStream`).
* **Para qué sirven:** Generan secuencias ordenadas con bandera `SIZED | SUBSIZED` y división paralela en $O(1)$. Usa `range(0, list.size())` para índices y `rangeClosed(1, n)` para fórmulas matemáticas.

### 2. `asLongStream()` y `asDoubleStream()`
* **Firmas:** `LongStream IntStream.asLongStream()`, `DoubleStream IntStream.asDoubleStream()`, `DoubleStream LongStream.asDoubleStream()`.
* **Para qué sirven:** Promocionan numéricamente un stream primitivo a otro de mayor rango/precisión sin *boxing*.

### 3. `mapToObj(IntFunction<U>)` vs. `boxed()`
* **`mapToObj(IntFunction<U>)`**: Convierte cada `int` directamente al objeto destino `U` **sin pasar por `Integer`** (ej. `IntStream.range(1, 6).mapToObj(i -> "Item #" + i)`).
* **`boxed()`**: Convierte `IntStream` → `Stream<Integer>` (o `Long`/`Double`). Úsalo **solo al final** cuando necesites recolectar con `toList()` o `Collectors.groupingBy()`.

### 4. `sum()`, `average()`, `min()`, `max()`, `summaryStatistics()` y `toArray()`
* `sum()`: retorna `int`/`long`/`double` (`0` si está vacío; usa Suma de Kahan en `DoubleStream`).
* `average()`: retorna `OptionalDouble` (vacío si no hay elementos, evitando `0.0 / 0.0 = NaN`).
* `summaryStatistics()`: retorna `IntSummaryStatistics` / `LongSummaryStatistics` / `DoubleSummaryStatistics` con `count`, `sum`, `min`, `max` y `average` en **1 sola pasada $O(N)$**.
* `toArray()`: materializa directamente un arreglo primitivo `int[]` / `long[]` / `double[]`.

```java
// Ejemplo: Fila N del Triángulo de Pascal (cero boxing)
int n = 4;
IntUnaryOperator fact = x -> IntStream.rangeClosed(1, x).reduce(1, (a, b) -> a * b);
int[] pascalRow4 = IntStream.rangeClosed(0, n)
        .map(k -> fact.applyAsInt(n) / (fact.applyAsInt(k) * fact.applyAsInt(n - k)))
        .toArray(); // [1, 4, 6, 4, 1]
```

---

## 9.3 Enciclopedia Completa de `Optional<T>` (Cada Método Explicado a Detalle)

`Optional<T>` es un contenedor inmutable que guarda **0 o 1 valor no nulo**. Su propósito es hacer explícita la posible ausencia de un valor en la firma de un método de retorno, eliminando los `NullPointerException` silenciosos.

### 1. Fábricas de Creación: `Optional.of`, `Optional.ofNullable`, `Optional.empty`
| Método | Firma | Comportamiento si recibe `null` | Cuándo usarlo |
| :--- | :--- | :--- | :--- |
| **`Optional.of(T value)`** | `static <T> Optional<T> of(T)` | **Lanza `NullPointerException` de inmediato** (*fail-fast*). | Cuando el valor **no debe** ser `null` (si lo fuera, es un bug que quieres detectar ya). |
| **`Optional.ofNullable(T value)`** | `static <T> Optional<T> ofNullable(T)` | Devuelve **`Optional.empty()`** con seguridad. | Cuando envuelves resultados de APIs externas, mapas (`map.get(k)`) o campos opcionales. |
| **`Optional.empty()`** | `static <T> Optional<T> empty()` | N/A | Para retornar ausencia explícita desde tus métodos en vez de `return null;`. |

### 2. Inspección: `isPresent()` e `isEmpty()` (Java 11+)
* **`boolean isPresent()`**: `true` si contiene valor.
* **`boolean isEmpty()`** *(Java 11+)*: `true` si está vacío (mucho más legible que `!opt.isPresent()`).
* **Nota de estilo:** Evita el anti-patrón `if (opt.isPresent()) return opt.get(); else return defaultVal;`. Usa siempre los métodos declarativos de abajo.

### 3. Desempaque Seguro: `orElse`, `orElseGet`, `orElseThrow`

#### A) `T orElse(T other)` vs. `T orElseGet(Supplier<? extends T> supplier)`
* **`orElse(other)`** es **ansioso (*eager*)**: Java evalúa la expresión `other` **siempre**, incluso cuando el `Optional` **ya tiene un valor presente**. Úsalo **únicamente** para constantes o variables ya calculadas (`0`, `""`, `"UNKNOWN"`).
* **`orElseGet(supplier)`** es **perezoso (*lazy*)**: solo invoca `supplier.get()` cuando el `Optional` está **vacío**. Es **obligatorio** para llamadas a base de datos, red, `new Object()` o cálculos costosos.

```java
Optional<String> cached = Optional.of("HIT");
String bad  = cached.orElse(queryRemoteDatabase());       // ❌ ¡Llama a la BD aunque había HIT!
String good = cached.orElseGet(this::queryRemoteDatabase); // ✅ Cero llamadas a la BD!
```

#### B) `T orElseThrow()` (Java 10+) y `T orElseThrow(Supplier<? extends X> exceptionSupplier)`
* **`orElseThrow()`** sin argumentos reemplaza al obsoleto `.get()`, dejando explícita la intención de lanzar `NoSuchElementException` si está vacío.
* **`orElseThrow(Supplier)`** lanza una excepción de dominio personalizada (ej. `() -> new OrderNotFoundException(id)`).

### 4. Transformación y Filtrado Monádico: `map`, `flatMap`, `filter`

```text
Optional[user] ──▶ flatMap(User::company) ──▶ flatMap(Company::name) ──▶ filter(n -> !n.isBlank()) ──▶ orElse("N/A")
   (Si en CUALQUIER paso se vuelve empty, el resto se salta sin lanzar NullPointerException!)
```

* **`<U> Optional<U> map(Function<? super T, ? extends U> mapper)`**:
  * Si hay valor, aplica `mapper` y envuelve el resultado en `Optional.ofNullable(...)` (si `mapper` devuelve `null`, produce `Optional.empty()`). Si ya estaba vacío, no hace nada.
* **`<U> Optional<U> flatMap(Function<? super T, ? extends Optional<? extends U>> mapper)`**:
  * Igual que `map`, pero para funciones que **ya devuelven un `Optional`**, evitando anidar `Optional<Optional<U>>`.
  * **Escenario real:** Navegación profunda segura o matemáticas sin excepciones (`safeDivide(100, 4).flatMap(safeSqrt)`).
* **`Optional<T> filter(Predicate<? super T> predicate)`**:
  * Conserva el valor solo si cumple `predicate`; si no lo cumple (o ya estaba vacío), retorna `Optional.empty()`.

```java
record Company(Optional<String> name) {}
record User(Optional<Company> company) {}

String companyName = Optional.ofNullable(user)
        .flatMap(User::company)
        .flatMap(Company::name)
        .filter(n -> !n.isBlank())
        .map(String::toUpperCase)
        .orElse("NOCOMPANY");
```

### 5. Respaldo Encadenado: `Optional<T> or(Supplier<? extends Optional<? extends T>> supplier)` (Java 9+)
* **¿Por qué existe frente a `orElseGet`?** `orElseGet` desempaca a un valor `T`, terminando la cadena. **`or()`** devuelve otro **`Optional<T>`**, permitiendo encadenar múltiples niveles de respaldo de forma 100% perezosa:
  ```java
  Optional<String> config = l1Cache
          .or(() -> l2Cache)        // Solo se consulta si l1Cache está vacío
          .or(() -> databaseLookup); // Solo se consulta si l1 y l2 están vacíos
  ```

### 6. Efectos Secundarios: `ifPresent(Consumer)` e `ifPresentOrElse(Consumer, Runnable)` (Java 9+)
* **`void ifPresent(Consumer<? super T> action)`**: Ejecuta `action` solo si el valor existe.
* **`void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction)`**: Ejecuta `action` si existe, o `emptyAction` si está vacío (ideal para métricas o logging sin `if/else`).

### 7. Puente con Streams: `Stream<T> stream()` (Java 9+)
* **Qué hace:** Convierte un `Optional<T>` en un `Stream<T>` de **0 elementos** (si está vacío) o **1 elemento** (si está presente).
* **Escenario estrella:** Limpiar un `Stream<Optional<T>>` en una sola línea:
  ```java
  List<Employee> validEmployees = optionalEmployees.stream()
          .flatMap(Optional::stream) // Descarta los vacíos y desempaca los presentes!
          .toList();
  ```

### 8. Variantes Primitivas: `OptionalInt`, `OptionalLong`, `OptionalDouble`
Devueltas por `IntStream.min()`, `LongStream.max()`, `DoubleStream.average()`, etc. Usan `getAsInt()` / `getAsLong()` / `getAsDouble()`, `orElse(...)`, `orElseGet(IntSupplier)` y `stream()` (devuelve `IntStream`/`LongStream`/`DoubleStream` sin *boxing*).

---

## 9.4 Retos de Dominio y Autoevaluación (Capítulo 9)

1. **Nivel Inicial:** Genera la fila $N$ del Triángulo de Pascal como `int[]` utilizando únicamente `IntStream` y sin *autoboxing*.
2. **Nivel Intermedio:** Escribe una prueba que demuestre que `Optional.of("ok").orElse(expensiveCall())` ejecuta `expensiveCall()` innecesariamente mientras `orElseGet(this::expensiveCall)` no lo invoca jamás.
3. **Nivel Avanzado:** Encadena tres fuentes (`l1Cache`, `l2Cache`, `dbLookup`) con `Optional.or(...)` y limpia una lista `List<Optional<Employee>>` usando `.flatMap(Optional::stream)`.
4. **Nivel Staff MAANG:** Explica cómo impacta `-XX:+UseCompactObjectHeaders` ([JEP 519](https://openjdk.org/jeps/519)) al layout de memoria de `Stream<Integer>` y por qué `IntStream` sigue ganando en localidad de caché L1.
