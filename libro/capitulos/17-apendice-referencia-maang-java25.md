# Apéndice Maestro: Referencia Rápida MAANG para Java 25 LTS

Guía condensada de bolsillo para consultar en tu día a día de ingeniería, entrevistas técnicas o revisiones de código de nivel Staff.

---

## A.1 Catálogo Maestro de Complejidad (Java 8 a Java 25 LTS)

| Categoría | Operación / API | Versión | ¿Stateful? | ¿Cortocircuito? | Tiempo | Espacio |
|---|---|:---:|:---:|:---:|---|---|
| **Fuente** | `Collection.stream()`, `Arrays.stream(a, i, j)` | Java 8 | No | No | $O(1)$ arranque | $O(1)$ |
| **Fuente** | `Stream.ofNullable(x)`, `Stream.iterate(s, p, f)` | Java 9 | No | No | $O(1)$ / $O(N)$ | $O(1)$ |
| **Fuente** | `SequencedCollection.reversed().stream()` | **Java 21** | No | No | **$O(1)$ arranque** | **$O(1)$** |
| **Intermedia** | `filter`, `map`, `mapToInt/Long/Double`, `peek` | Java 8 | No | No | $O(1)$ / elem | $O(1)$ |
| **Intermedia** | `flatMap`, `flatMapToInt/Long/Double` | Java 8 | No | No | $O(M)$ / elem | $O(\text{sub-stream})$ |
| **Intermedia** | `mapMulti`, `mapMultiToInt/Long/Double` | **Java 16** | No | No | $O(M)$ / elem | **$O(1)$** |
| **Intermedia** | `distinct` | Java 8 | **Sí** | No | $O(1)$ prom / elem | $O(U)$ |
| **Intermedia** | `sorted` | Java 8 | **Sí** (Barrera) | No | $O(N \log N)$ | $O(N)$ |
| **Intermedia** | `limit`, `takeWhile` | Java 8 / 9 | Sí / No | **Sí** | $O(K)$ | $O(1)$ sec. |
| **Intermedia** | `skip`, `dropWhile` | Java 8 / 9 | Sí | No | $O(N)$ | $O(1)$ |
| **Gatherer** | `Gatherers.windowFixed(n)` | **Java 24/25** | **Sí** | Hereda | $O(N)$ | $O(n)$ |
| **Gatherer** | `Gatherers.windowSliding(n)` | **Java 24/25** | **Sí** | Hereda | $O(N)$ | $O(n)$ |
| **Gatherer** | `Gatherers.scan(...)`, `Gatherers.fold(...)` | **Java 24/25** | **Sí** | Hereda | $O(N)$ | $O(1)$ |
| **Gatherer** | `Gatherers.mapConcurrent(c, fn)` | **Java 24/25** | **Sí** | **Sí** | $O(N / c)$ I/O | $O(c)$ |
| **Terminal** | `anyMatch`, `allMatch`, `noneMatch`, `findFirst`, `findAny` | Java 8 | No | **Sí** | $O(1)$–$O(N)$ | $O(1)$ |
| **Terminal** | `count` | Java 8 (opt. 9) | No | No | **$O(1)$ si `SIZED`**, si no $O(N)$ | $O(1)$ |
| **Terminal** | `min`, `max`, `sum`, `average`, `reduce` | Java 8 | No | No | $O(N)$ | $O(1)$ |
| **Terminal** | `toList()` (inmodificable, acepta `null`) | **Java 16** | Sí | No | $O(N)$ | $O(N)$ |
| **Terminal** | `collect(Collectors.*)` (`toMap`, `groupingBy`, `teeing`) | Java 8–12 | Sí | No | $O(N)$ | $O(N)$ |

---

## A.2 Matriz de Decisión Arquitectónica Java 25 LTS

| Necesidad en Producción o Entrevista | Solución Estándar Java 25 | Anti-Patrón que Debes Evitar |
|---|---|---|
| Invertir una lista o `LinkedHashSet` | `collection.reversed().stream()` | `.sorted(Comparator.reverseOrder())` |
| Verificar si algún elemento cumple `p` | `.anyMatch(p)` | `.filter(p).count() > 0` |
| Obtener mínimo o máximo | `.min(comp)` / `.max(comp)` | `.sorted(comp).findFirst()` |
| Sumar / promediar números | `.mapToDouble(...).sum()` / `.average()` | `.map(...).reduce(0.0, Double::sum)` |
| Expansión 1-a-0..N en ruta caliente | `.mapMulti(...)` / `.mapMultiToInt(...)` | `.flatMap(x -> ... ? Stream.of(...) : Stream.empty())` |
| Lotes fijos o ventanas móviles | `Gatherers.windowFixed(n)` / `windowSliding(n)` | `IntStream.rangeClosed` + `subList` |
| Totales corridos (*running total*) | `Gatherers.scan(...)` | Variable externa mutable en `.map()` |
| Llamadas HTTP / BD concurrentes | `Gatherers.mapConcurrent(max, fn)` (Virtual Threads) | `.parallelStream().map(this::httpCall)` |
| Trabajo CPU intensivo ($N \times Q > 10{,}000$) | `.parallelStream()` sobre `ArrayList` / `IntStream.range` | `.parallelStream()` sobre `LinkedList` o `Stream.iterate` |
| Ignorar parámetro en lambda / merge de `toMap` | `(existing, _) -> existing` | `(existing, ignored) -> existing` |
| Deduplicar preservando orden y extremos | `.collect(Collectors.toCollection(LinkedHashSet::new))` (`SequencedSet`) | `.distinct().toList()` si necesitas `Set` |
| Top-K sobre millones de registros | Custom `Collector` con `PriorityQueue` ($O(N \log K)$) | `.sorted().limit(k)` ($O(N \log N)$) |
| Fuente de archivos / directorios | `try (var s = Files.lines/walk/find(...))` | Olvidar `try-with-resources` (fuga de *file descriptors*) |
| Extraer grupos de Regex | `pattern.matcher(text).results()` (Java 9+) | Bucle `while (m.find())` manual |
| Caracteres Unicode / emojis | `str.codePoints()` | `str.chars()` (rompe pares sustitutos UTF-16) |
| Suma de `double` con menor error | `DoubleStream.sum()` (Suma de Kahan) | `reduce(0.0, Double::sum)` |
| `Map` con valores `null` permitidos | `collect(HashMap::new, (m,e)->m.put(...), HashMap::putAll)` | `Collectors.toMap(...)` (lanza NPE en valores `null`) |
| *Checked Exceptions* en lotes ETL | `sealed interface Result<T, E>` + `partitioningBy` | `try/catch` que traga excepciones o tumba todo el lote |
| Algoritmos DP / dos punteros / nanosegundos | Bucle `for` imperativo clásico | Forzar un Stream ilegible o lento |
| Valor por defecto costoso en `Optional` | `.orElseGet(this::compute)` | `.orElse(compute())` |

---

## A.3 Índice de Especificaciones Oficiales (OpenJDK JEPs Citados)

- **[JDK 25 Release (LTS, JSR 400)](https://openjdk.org/projects/jdk/25/):** Plataforma base LTS (GA: 16 de septiembre de 2025).
- **[JEP 431: Sequenced Collections](https://openjdk.org/jeps/431):** `SequencedCollection`, `SequencedSet`, `SequencedMap`, `reversed()`, `getFirst()`, `getLast()`.
- **[JEP 444: Virtual Threads](https://openjdk.org/jeps/444)** y **[JEP 491: Synchronize Virtual Threads without Pinning](https://openjdk.org/jeps/491):** Fundamento de concurrencia ligera para `Gatherers.mapConcurrent()`.
- **[JEP 456: Unnamed Variables & Patterns](https://openjdk.org/jeps/456):** Guion bajo `_` en lambdas, patrones de `record` y cláusulas `catch`.
- **[JEP 485: Stream Gatherers](https://openjdk.org/jeps/485):** `Stream.gather`, `Gatherer<T, A, R>`, `Gatherers` (`windowFixed`, `windowSliding`, `fold`, `scan`, `mapConcurrent`).
- **[JEP 506: Scoped Values](https://openjdk.org/jeps/506):** Compartición segura e inmutable de contexto entre hilos y subprocesos virtuales.
- **[JEP 519: Compact Object Headers](https://openjdk.org/jeps/519):** Cabeceras de objeto de 64 bits en HotSpot (`-XX:+UseCompactObjectHeaders`).

---

## A.4 Checklist Pre-Entrega (Estándar L6+ Staff / Principal)

Antes de aprobar o desplegar cualquier pipeline de Streams en producción:
1. **Cero *boxing*:** ¿Usaste `IntStream`, `DoubleStream`, `mapMultiToInt` y `Comparator.comparingInt/Double` en toda aritmética y comparación numérica?
2. **Push-down y cortocircuito:** ¿Están tus `filter()` antes de `sorted()` y `distinct()`? ¿Usaste `anyMatch`/`findFirst`/`takeWhile` en vez de materializar toda la fuente?
3. **Idiomas modernos Java 25:** ¿Usaste `reversed().stream()`, `_`, `Gatherers.*`, `mapMulti` y Record Pattern Matching donde simplifican y aceleran el pipeline?
4. **Pureza y seguridad de hilos:** ¿Están todas tus lambdas libres de efectos secundarios sobre estado externo mutable?
5. **Casos borde blindados:** ¿Validaste colecciones vacías (*vacuous truth*), valores `null`, claves duplicadas en `toMap`, `finisher` en Gatherers con estado y `combiner` en paralelo?
