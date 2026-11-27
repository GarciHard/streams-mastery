# Capítulo 2: Creación, Anatomía de Streams, `BaseStream`, `Spliterator` y Fuentes Completas del JDK 25

> *"Un Stream no es una caja donde guardas datos; es una línea de ensamblaje que transforma datos bajo demanda conforme pasan por ella."*

Si vienes del mundo de los bucles `for` y las colecciones clásicas (`ArrayList`, `HashSet`, `HashMap`), este capítulo construirá desde cero el modelo mental exacto de qué es un `Stream`, su jerarquía completa de tipos en el JDK, cómo funciona una prueba de escritorio paso a paso de su evaluación perezosa, y **absolutamente todas las formas de crear un Stream en Java 25 LTS** —incluyendo archivos (`Files.lines`, `list`, `walk`, `find`), expresiones regulares (`Matcher.results()`), Unicode real (`codePoints()`), generadores aleatorios (`SplittableRandom`), `BitSet`, `StackWalker`, `StreamSupport` y las **Colecciones Secuenciadas** de Java 21–25.

---

## 2.1 ¿Qué Es (y Qué NO Es) un `Stream<T>`?

Antes de escribir código, aclaremos la confusión número uno de todo principiante:

| Dimensión | Colección (`List`, `Set`, `Map`) | `Stream<T>` (`java.util.stream`) |
| :--- | :--- | :--- |
| **Propósito principal** | **Almacenar** y organizar datos en memoria. | **Procesar y transformar** datos de forma declarativa. |
| **Evaluación** | **Ansiosa (*eager*):** todos los elementos ya existen en memoria. | **Perezosa (*lazy*):** los elementos se calculan uno por uno solo cuando los pide una operación terminal. |
| **Tamaño** | Siempre **finito** (limitado por la RAM). | Puede ser **finito o infinito** (ej. secuencia de Fibonacci, flujos de sensores). |
| **Reutilización** | Puedes recorrer un `ArrayList` mil veces con `for`. | **Consumo único (*single-use*):** se recorre una sola vez; volver a usarlo lanza `IllegalStateException`. |
| **Iteración** | **Externa:** tú controlas el bucle `for` / `Iterator`. | **Interna:** la librería controla el recorrido (habilitando *loop fusion* y paralelismo automático). |
| **Mutación** | Puedes agregar, borrar o reemplazar elementos. | **No modifica la fuente original**; produce un nuevo resultado. |

### Jerarquía Oficial de Interfaces de Streams en el JDK 25

Todo Stream en Java hereda de `AutoCloseable` y de `BaseStream<T, S>` (por eso todos los Streams soportan `try-with-resources`, `.parallel()`, `.sequential()`, `.unordered()`, `.onClose()` y `.spliterator()`):

```text
                       java.lang.AutoCloseable
                                 ▲
                                 │
              java.util.stream.BaseStream<T, S extends BaseStream<T, S>>
                 (iterator, spliterator, isParallel, sequential,
                  parallel, unordered, onClose, close)
               ┌────────────┬────────────┬────────────┐
               │            │            │            │
         Stream<T>      IntStream    LongStream  DoubleStream
        (objetos)       (int 32b)    (long 64b)  (double 64b)
```

### Las Tres Partes Obligatorias de Todo Pipeline y Prueba de Escritorio

```text
  [ 1. FUENTE ]           [ 2. OPERACIONES INTERMEDIAS (0 o más) ]        [ 3. OPERACIÓN TERMINAL (Exactamente 1) ]
  Lista / Arreglo    -->  .filter(...) -> .map(...) -> .gather(...)  -->  .toList() / .count() / .sum() / .findFirst()
  (Crea el Stream)        (Devuelven otro Stream; son PEREZOSAS)          (Dispara la ejecución y CIERRA el Stream)
```

#### Prueba de escritorio paso a paso (¿Qué ocurre en cada nanosegundo?)
Supón que ejecutas:
```java
List<String> res = List.of("ana", "luis", "sofia", "pedro").stream()
        .filter(s -> s.length() >= 4)
        .map(String::toUpperCase)
        .limit(2)
        .toList();
```

| Paso | Elemento jalado de la fuente | `filter(len >= 4)` | `map(toUpperCase)` | `limit(2)` (acumulados) | Acción del motor |
| :---: | :---: | :---: | :---: | :---: | :--- |
| 1 | `"ana"` (len 3) | `false` (descartado) | *(no se ejecuta)* | 0 | Pide el siguiente elemento |
| 2 | `"luis"` (len 4) | `true` (pasa) | `"LUIS"` | 1 (`["LUIS"]`) | Pide el siguiente elemento |
| 3 | `"sofia"` (len 5) | `true` (pasa) | `"SOFIA"` | **2** (`["LUIS", "SOFIA"]`) | **¡CORTOCIRCUITO!** Cierra el pipeline |
| 4 | `"pedro"` | *(jamás se lee)* | *(jamás se ejecuta)* | — | Retorna `["LUIS", "SOFIA"]` |

* **Regla 1 (Sin terminal no pasa nada):** Si escribes `employees.stream().filter(...)` sin operación terminal, **la JVM no evalúa el predicado ni una sola vez**.
* **Regla 2 (Consumo único):** Una vez invocada la operación terminal, reutilizar el mismo `Stream` lanza `IllegalStateException: stream has already been operated upon or closed`. Si necesitas reutilizar la receta, envuélvela en un `Supplier<Stream<T>>`:
  ```java
  Supplier<Stream<String>> techSupplier = () -> Stream.of("Java", "Go", "Rust");
  long total = techSupplier.get().count();         // Stream fresco #1
  List<String> list = techSupplier.get().toList(); // Stream fresco #2
  ```

---

## 2.2 Guía Exhaustiva: Todas las Formas de Crear un Stream en Java 25 LTS

Dominar cada fábrica del JDK te evita copias innecesarias en memoria, fugas de archivos abiertos, *NullPointerException* y *boxing* accidental.

### 1. Desde Cualquier `Collection` (`List`, `Set`, `Queue`, `Deque`)
Toda clase que implementa `java.util.Collection<E>` ofrece `.stream()` (secuencial) y `.parallelStream()` (paralelo):
```java
List<String> frameworks = List.of("Spring", "Quarkus", "Micronaut", "Vert.x");
long count = frameworks.stream().count(); // 4
```

### 2. Desde Arreglos (`Arrays.stream`) y Cortes *Zero-Copy*
`Arrays.stream(...)` está sobrecargado para `T[]`, `int[]`, `long[]` y `double[]`. Además acepta un rango semiabierto `[startInclusive, endExclusive)` **sin copiar el sub-arreglo en memoria**:
```java
int[] timestamps = {100, 200, 300, 400, 500, 600, 700};
int totalSum = Arrays.stream(timestamps).sum();            // IntStream (cero boxing)
int[] middleSlice = Arrays.stream(timestamps, 2, 5).toArray(); // [300, 400, 500]
```

> **Error Común de Programación (`Stream.of(int[])`):**
> Nunca pases un arreglo primitivo (`int[]`, `long[]`, `double[]`) a `Stream.of(...)`. Como los genéricos no soportan primitivos, `Stream.of(new int[]{10, 20, 30})` crea un **`Stream<int[]>` de longitud 1** (cuyo único elemento es el arreglo entero), ¡no un stream de 3 números! Usa siempre `Arrays.stream(intArray)` o `IntStream.of(10, 20, 30)`.

### 3. Fábricas Estáticas: `Stream.of`, `Stream.empty`, `Stream.ofNullable`
```java
Stream<String> maang = Stream.of("Meta", "Amazon", "Apple", "Netflix", "Google");
Stream<Employee> noResults = Stream.empty();
Stream<String> safe = Stream.ofNullable(getConfigOrNull()); // 0 elementos si es null, 1 si no lo es
```

### 4. `Stream.builder()`: Construcción Imperativa Condicional
```java
List<String> clouds = Stream.<String>builder() // Type witness <String> obligatorio al encadenar
        .add("AWS")
        .add("GCP")
        .add("Azure")
        .build()
        .toList();
```
*(Una vez que invocas `.build()`, volver a llamar `.add()` lanza `IllegalStateException`).*

### 5. Streams Infinitos con `Stream.generate(Supplier<T>)`
Produce un stream **infinito y desordenado** invocando `supplier.get()` bajo demanda. Acótalo siempre con `.limit(n)`, `.takeWhile(...)` o `.findFirst()`:
```java
List<UUID> ids = Stream.generate(UUID::randomUUID).limit(3).toList();
```

### 6. Secuencias Ordenadas con `Stream.iterate()` (Sobrecargas de 2 y 3 Argumentos)
* **2 argumentos (Java 8 — Infinito, requiere `limit` o `takeWhile`):**
  ```java
  // Primeros 10 números de Fibonacci [0, 1, 1, 2, 3, 5, 8, 13, 21, 34]
  List<Integer> fibonacci = Stream.iterate(
                  new int[]{0, 1},
                  pair -> new int[]{pair[1], pair[0] + pair[1]})
          .limit(10)
          .map(pair -> pair[0]) // pair[0] empieza en 0; pair[1] empezaría en 1
          .toList();
  ```
* **3 argumentos (Java 9+ — Finito, equivalente exacto al bucle `for (seed; hasNext; next)`):**
  ```java
  // Potencias de 2 menores a 100 -> [1, 2, 4, 8, 16, 32, 64]
  List<Integer> powers = Stream.iterate(1, n -> n < 100, n -> n * 2).toList();

  // Conjetura de Collatz desde 6 hasta 1 (incluyendo el 1 final usando 0L como centinela):
  List<Long> collatz = Stream.iterate(
          6L,
          n -> n > 0L,
          n -> (n == 1L) ? 0L : (n % 2L == 0L ? n / 2L : 3L * n + 1L)
  ).toList(); // [6, 3, 10, 5, 16, 8, 4, 2, 1]
  ```

### 7. Rangos Numéricos Primitivos: `range()` vs `rangeClosed()`
Disponibles en `IntStream` y `LongStream`:
```java
int sumOfSquares = IntStream.rangeClosed(1, 10).map(n -> n * n).sum();           // 385
int[] bitmasks   = IntStream.rangeClosed(0, 8).map(shift -> 1 << shift).toArray(); // [1..256]
long[] offsets   = LongStream.rangeClosed(0, 4).map(i -> i * 60L).toArray();
```

### 8. Cadenas de Texto: `chars()` vs `codePoints()` (Unicode Real) y `String.lines()`
Un error muy frecuente en aplicaciones internacionales es asumir que un carácter siempre cabe en 1 `char` (16 bits UTF-16). Los emojis y muchos ideogramas ocupan **2 `char`** (un par sustituto):

```java
String rocket = "🚀";
long utf16Units = rocket.chars().count();      // ¡2! (par sustituto UTF-16 roto si lo partes)
long realChars  = rocket.codePoints().count(); // ¡1! (punto de código Unicode real)

// Convertir cada punto de código real de vuelta a String seguro:
List<String> graphemes = "Java🚀25".codePoints()
        .mapToObj(Character::toString) // Java 11+: convierte int codePoint a String completo
        .toList(); // ["J", "a", "v", "a", "🚀", "2", "5"]

// Java 11+: String.lines() divide líneas manejando \n, \r y \r\n de forma perezosa
List<String> nonBlankLines = "uno\r\n\ndos\ntres".lines()
        .filter(Predicate.not(String::isBlank))
        .toList(); // ["uno", "dos", "tres"]
```

### 9. Expresiones Regulares: `Pattern.splitAsStream()` y `Matcher.results()` (Java 9+)
* **`Pattern.splitAsStream(CharSequence)`**: divide sin crear un arreglo `String[]` temporal.
* **`Matcher.results()` (Java 9+)**: devuelve un **`Stream<MatchResult>`** con todas las coincidencias y sus **grupos de captura**, eliminando para siempre los bucles `while (matcher.find())`:

```java
// Extraer pares clave=valor de un log estructurado usando Matcher.results()
String log = "user=alice latency_ms=42 region=mx-central status=200";
Pattern kvPattern = Pattern.compile("(\\w+)=([\\w-]+)");

Map<String, String> parsedLog = kvPattern.matcher(log).results()
        .collect(Collectors.toMap(
                m -> m.group(1),
                m -> m.group(2),
                (_, latest) -> latest,
                LinkedHashMap::new
        ));
// {user=alice, latency_ms=42, region=mx-central, status=200}
```

### 10. Archivos y Sistema de Archivos: `java.nio.file.Files` y `BufferedReader.lines()`
Toda la familia de `Files` devuelve Streams **respaldados por descriptores de archivo del sistema operativo**. **Regla absoluta de producción: envuélvelos SIEMPRE en `try-with-resources`** para evitar fugas de *file descriptors* (`Too many open files`):

| Método de `Files` | Qué produce | Caso de uso real |
| :--- | :--- | :--- |
| **`Files.lines(Path, Charset)`** | `Stream<String>` (línea por línea, perezoso) | Procesar logs o CSVs de 50 GB sin cargarlos en RAM. |
| **`Files.list(Path)`** | `Stream<Path>` (solo 1 nivel, no recursivo) | Listar archivos directos de un directorio. |
| **`Files.walk(Path, maxDepth)`** | `Stream<Path>` (recursivo *depth-first*) | Buscar todos los `.java` o calcular peso de un árbol de carpetas. |
| **`Files.find(Path, maxDepth, BiPredicate)`** | `Stream<Path>` filtrando por `BasicFileAttributes` | Encontrar archivos mayores a 10 MB sin doble llamada `stat()` al SO. |

```java
// Ejemplo real: encontrar los 5 archivos .java más grandes en un proyecto (try-with-resources obligatorio)
Path root = Path.of("src");
try (Stream<Path> paths = Files.find(root, 10,
        (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".java"))) {
    List<Path> largestJavaFiles = paths
            .sorted(Comparator.comparingLong((Path p) -> p.toFile().length()).reversed())
            .limit(5)
            .toList();
}
```

### 11. Generadores Aleatorios Primitivos: `Random`, `ThreadLocalRandom` y `SplittableRandom`
En lugar de `Stream.generate(() -> rng.nextInt(100))` (que hace *autoboxing* a `Integer`), el JDK provee Streams primitivos directos:
```java
// 1. Secuencial determinista (reproducible en tests con semilla):
int[] sample = new Random(42).ints(5, 1, 101).toArray(); // 5 enteros en [1, 100]

// 2. Alto rendimiento y paralelizable (sin contención CAS en la semilla):
double[] monteCarlo = new SplittableRandom().doubles(1_000_000, 0.0, 1.0).parallel().toArray();
```

### 12. Otras Fuentes del JDK: `Map`, `BitSet`, `Scanner` y `StackWalker`
```java
// 1. Streaming sobre Map (Map no tiene .stream() directo)
List<String> highStocks = stocks.entrySet().stream()
        .filter(e -> e.getValue() > 180.0).map(Map.Entry::getKey).sorted().toList();

// 2. BitSet.stream() -> IntStream con los índices de los bits encendidos (1)
BitSet flags = new BitSet();
flags.set(2); flags.set(7); flags.set(15);
int[] activeBits = flags.stream().toArray(); // [2, 7, 15]

// 3. Scanner.tokens() (Java 9+): tokenización perezosa de entrada estándar o texto
try (Scanner sc = new Scanner("10 20 30")) {
    int sum = sc.tokens().mapToInt(Integer::parseInt).sum(); // 60
}

// 4. StackWalker.walk(Function<Stream<StackFrame>, T>) (Java 9+): inspeccionar el stack sin Exception
List<String> top3Frames = StackWalker.getInstance()
        .walk(frames -> frames.limit(3).map(StackWalker.StackFrame::getMethodName).toList());
```

### 13. Colecciones Secuenciadas ([JEP 431](https://openjdk.org/jeps/431)), `Stream.concat()` y `StreamSupport`
```java
// 1. Vista inversa en O(1) tiempo y O(1) memoria (Java 21–25, JEP 431):
List<String> reverseStages = List.of("Extract", "Transform", "Load").reversed().stream().toList();

// 2. Concatenar 2 streams (para K >= 3 usa Stream.of(s1, s2, s3).flatMap(Function.identity())):
List<String> combined = Stream.concat(list1.stream(), list2.stream()).toList();

// 3. Puente oficial desde cualquier Iterator/Spliterator personalizado:
Spliterator<Integer> customSpliterator = Spliterators.spliteratorUnknownSize(
        myIterator, Spliterator.ORDERED | Spliterator.NONNULL);
List<Integer> firstFive = StreamSupport.stream(customSpliterator, false).limit(5).toList();
```

---

## 2.3 El Motor Oculto: Características del `Spliterator<T>`

| Bandera | Significado | Ejemplos de Fuente | Optimización que Habilita |
| :--- | :--- | :--- | :--- |
| `ORDERED` | Orden de encuentro definido | `List`, arrays, `IntStream.range` | Garantiza orden en `findFirst`, `limit`, `skip`, `gather` |
| `DISTINCT` | Elementos únicos por `equals` | `HashSet`, `TreeSet`, `LinkedHashSet` | Convierte `.distinct()` posterior en *no-op* |
| `SORTED` | Ya ordenado por comparador/natural | `TreeSet`, `TreeMap.keySet()` | Convierte `.sorted()` natural posterior en *no-op* |
| `SIZED` | Tamaño exacto conocido de antemano | `ArrayList`, arrays, `IntStream.range` | Pre-dimensiona arreglos y resuelve `.count()` en **$O(1)$** |
| `SUBSIZED` | Todos los sub-splits son `SIZED` | `ArrayList`, arrays | División paralela perfecta $O(1)$ |
| `NONNULL` | Garantiza ausencia de `null` | `ConcurrentHashMap.keySet()` | Evita comprobaciones de nulidad innecesarias |
| `IMMUTABLE` | Fuente inmutable durante recorrido | `List.of(...)`, `IntStream.range` | Recorrido seguro sin copias defensivas |
| `CONCURRENT` | Segura ante modificación concurrente | `ConcurrentHashMap` | Sin `ConcurrentModificationException` |

---

## 2.4 Retos de Dominio y Autoevaluación (Capítulo 2)

1. **Nivel Inicial:** Genera los primeros 15 términos de Fibonacci con `Stream.iterate` asegurándote de incluir el `0` inicial (`pair[0]`) y explica por qué reutilizar el mismo `Stream` lanza `IllegalStateException`.
2. **Nivel Intermedio:** Usa `Stream.iterate` de 3 argumentos para generar la secuencia de Collatz completa desde `n = 27` hasta `1` (incluyendo el `1` final), y utiliza `Matcher.results()` para extraer todas las fechas `YYYY-MM-DD` de un archivo de texto.
3. **Nivel Avanzado:** Escribe un bloque `try-with-resources` con `Files.find()` que localice los 10 archivos `.log` más grandes dentro de un directorio sin fugar *file descriptors*.
4. **Nivel Staff MAANG:** Construye un `Spliterator` personalizado con `Spliterators.spliteratorUnknownSize` y `StreamSupport.stream()` sobre un paginador externo, y demuestra con código por qué `"🚀".codePoints().count()` difiere de `"🚀".chars().count()`.
