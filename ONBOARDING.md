# 🚀 Java Streams Mastery: The Definitive Onboarding Guide

> **Target Platform:** Java 25 LTS  
> **Curriculum Scope:** 16 Comprehensive Lessons (83 Passing Reference Tests) & 320 Strictly Graded Exercises (20 per lesson)  
> **Engineering Standard:** Big Tech / MAANG L6+ Staff Engineer Bar

---

## 🧭 Executive Summary & Learning Philosophy

The Java Stream API is the single most important functional programming abstraction on the JVM. Yet, over 80% of backend engineers write non-idiomatic, slow, or unsafe stream pipelines that thrash the Garbage Collector, cause accidental quadratic complexity, or break under multi-threaded execution.

This curriculum was engineered to take you from basic lambda syntax to deep stream internals, custom collectors, high-throughput `mapMulti`, Java 21 Sequenced Collections, and the groundbreaking **Stream Gatherers (JEP 485, Java 24/25)**.

---

## 🧠 The 4 Foundational Mental Models

To master Java Streams, you must discard procedural `for` loop thinking and adopt these 4 declarative mental models:

```
+-----------------------------------------------------------------------------------------+
|                                1. THE CONVEYOR BELT (STREAM)                            |
|                                                                                         |
|  [SOURCE] --------> [FILTER (Gate)] --------> [GATHERER (Window/Batch)] ----> [TERMINAL]|
|  (Collection/Array)   (Stateless)              (Intermediate State)            (Reduce) |
|                                                                                         |
|  * Elements pulled ON DEMAND (Lazy Evaluation). Nothing runs until Terminal op fires!   |
|  * Loop Fusion: Elements flow vertically element-by-element, not horizontally in arrays.|
+-----------------------------------------------------------------------------------------+
```

### 1. The Conveyor Belt (Streams)
A Stream is **NOT** a data structure. It does not store elements in memory. It is a one-directional, on-demand conveyor belt:
- **Lazy Evaluation:** Intermediate operations (`filter`, `map`, `gather`) register computation recipes. **Zero CPU cycles are spent** until a terminal operation (`toList()`, `sum()`, `collect()`) pulls elements from the end.
- **Loop Fusion:** Elements are not processed array-by-array. Instead, element 1 traverses `filter -> map -> gather` before element 2 is even inspected.
- **Single-Use Contract:** Once a terminal operation completes, the pipeline is closed and consumed. Reusing a stream throws `IllegalStateException`.

### 2. The Transformation Trinity: `map` vs `flatMap` vs `mapMulti`
```
+-----------------+--------------------------+-----------------------+-----------------------------+
| Operation       | Mathematical Duality     | Emission Cardinality  | Allocation / Memory Cost    |
+-----------------+--------------------------+-----------------------+-----------------------------+
| map()           | Functor (T -> R)         | Exactly 1 : 1         | Zero stream overhead        |
| flatMap()       | Monad (T -> Stream<R>)   | 1 : 0..N              | High (allocates Stream obj) |
| mapMulti()      | Push-Consumer (Imperative)| 1 : 0..N             | Zero Stream allocation      |
+-----------------+--------------------------+-----------------------+-----------------------------+
```
- **Use `map`** when transforming an entity to a DTO or extracting a field.
- **Use `flatMap`** when flattening multi-level collections (`Order -> List<OrderItem>`) or unwrapping nested Optionals.
- **Use `mapMulti` (Java 16/21+)** in high-throughput hot paths when emitting 0, 1, or few elements per upstream item. It invokes `downstream.accept(val)` directly without allocating sub-stream objects!

### 3. The Mutable Bucket (`Collector`)
Terminal operations reduce streams into a final summary or collection.
- `reduce()` performs **immutable reduction**: repeatedly creates new objects (e.g. `acc + element`). Catastrophic for memory if collecting lists or maps.
- `collect()` performs **mutable reduction**: folds stream elements into a single pre-allocated mutable container (`ArrayList`, `HashMap`) via the 5 components of `Collector<T, A, R>`:
  1. `supplier()`: allocates accumulator `A`
  2. `accumulator()`: adds item `T` into `A`
  3. `combiner()`: merges two `A` containers in parallel execution
  4. `finisher()`: transforms `A` into final result `R`
  5. `characteristics()`: metadata flags (`IDENTITY_FINISH`, `UNORDERED`, `CONCURRENT`)

### 4. The Intermediate Pipeline Transformer (`Gatherer` - Java 24/25)
Prior to Java 24, intermediate operations were closed: you could not write your own intermediate operators. **JEP 485 (Stream Gatherers)** makes intermediate operations fully extensible!
- `stream.gather(Gatherer<T, A, R>)` transforms an upstream stream of `T` into a downstream stream of `R` while maintaining intermediate state `A`.
- **Built-ins:** `windowFixed(N)`, `windowSliding(N)`, `fold()`, `scan()`, `mapConcurrent()`.
- **Custom Gatherer:** Defined via `initializer`, `integrator`, `combiner`, `finisher`.

---

## 🗺️ Complete Java Streams API & Feature Catalog (Java 8 to 25)

Every single feature of Java Streams is categorized below with its asymptotic complexity and typical Big Tech interview angle:

| Category | API / Method | Introduced | Time Complexity | Space Complexity | Big Tech / MAANG Interview Angle |
|---|---|:---:|:---:|:---:|---|
| **Source** | `Collection.stream()`, `parallelStream()` | Java 8 | $O(1)$ | $O(1)$ | Default collection entry point. Unordered sets allow split optimizations. |
| **Source** | `Arrays.stream(array, from, to)` | Java 8 | $O(1)$ | $O(1)$ | Zero-copy array slicing directly into primitive streams. |
| **Source** | `Stream.of(T...)`, `Stream.ofNullable(T)` | Java 9 | $O(1)$ | $O(1)$ | `ofNullable` eliminates null-checks before pipeline initiation. |
| **Source** | `Stream.iterate(seed, hasNext, next)` | Java 9 | $O(N)$ | $O(1)$ | 3-arg bounded iterator replacing traditional `for(int i=0; i<N; i++)`. |
| **Source** | `SequencedCollection.reversed().stream()` | **Java 21** | $O(1)$ startup | $O(1)$ | **Reverses ordered stream without sorting!** Eliminates $O(N \log N)$ sort overhead. |
| **Intermediate** | `filter(Predicate<T>)` | Java 8 | $O(N)$ | $O(1)$ | Stateless push-down filtering. Filter early to minimize downstream work. |
| **Intermediate** | `map(Function<T, R>)` | Java 8 | $O(N)$ | $O(1)$ | Stateless 1-to-1 projection. Entity-to-DTO conversion. |
| **Intermediate** | `flatMap(Function<T, Stream<R>>)` | Java 8 | $O(N \times M)$ | $O(\text{substream})$ | Flattens nested streams. Avoid deep `concat` recursion; use `flatMap(identity)`. |
| **Intermediate** | `mapMulti(BiConsumer<T, Consumer<R>>)` | **Java 16/21+** | $O(N \times M)$ | $O(1)$ | **Zero-allocation push-based expansion.** Up to 4x faster than `flatMap`. |
| **Intermediate** | `distinct()` | Java 8 | $O(N)$ | $O(N)$ buffer | Stateful operation. Requires hash-set buffer. Push down filters before `distinct`. |
| **Intermediate** | `sorted(Comparator<T>)` | Java 8 | $O(N \log N)$ | $O(N)$ buffer | Stateful barrier. Buffers **entire** stream before emitting first element! |
| **Intermediate** | `takeWhile(Predicate<T>)` | Java 9 | $O(K)$ | $O(1)$ | Short-circuiting slice on ordered streams. Stops immediately on first false. |
| **Intermediate** | `dropWhile(Predicate<T>)` | Java 9 | $O(N)$ | $O(1)$ | Discards initial matching prefix, then emits remaining stream. |
| **Intermediate** | `gather(Gatherer<T, A, R>)` | **Java 24/25** | Variable | $O(\text{state})$ | **Universal intermediate transformation.** Batching, sliding windows, prefix scans. |
| **Gatherer** | `Gatherers.windowFixed(int N)` | **Java 24/25** | $O(N)$ | $O(\text{batch})$ | Partitions stream into fixed batches of size $N$ for bulk DB writes. |
| **Gatherer** | `Gatherers.windowSliding(int N)` | **Java 24/25** | $O(N)$ | $O(\text{window})$ | Rolling moving window over timeseries or adjacent comparisons. |
| **Gatherer** | `Gatherers.scan(Supplier, BiFunction)` | **Java 24/25** | $O(N)$ | $O(1)$ | Cumulative prefix scanner (running total, running max). |
| **Gatherer** | `Gatherers.fold(Supplier, BiFunction)` | **Java 24/25** | $O(N)$ | $O(1)$ | Intermediate reduction emitting a 1-element stream for further chaining. |
| **Gatherer** | `Gatherers.mapConcurrent(concurrency, fn)` | **Java 24/25** | $O(N / C)$ | $O(C)$ | Bounded concurrency with **Virtual Threads** preserving encounter order. |
| **Terminal** | `toList()` | Java 16+ | $O(N)$ | $O(N)$ | Compact unmodifiable list. Standard default terminal operation. |
| **Terminal** | `collect(Collector)` | Java 8 | $O(N)$ | $O(N)$ | Mutable reduction into collections or maps. |
| **Terminal** | `reduce(identity, accumulator)` | Java 8 | $O(N)$ | $O(1)$ | Immutable reduction. Identity element must strictly satisfy algebraic monoid laws. |
| **Terminal** | `anyMatch()`, `allMatch()`, `noneMatch()` | Java 8 | $O(K)$ | $O(1)$ | Short-circuiting booleans. Returns instantly once condition is proven. |
| **Terminal** | `findFirst()`, `findAny()` | Java 8 | $O(1)$ | $O(1)$ | Short-circuiting element lookup. `findAny()` enables parallel optimizations. |
| **Collector** | `Collectors.groupingBy(...)` | Java 8 | $O(N)$ | $O(N)$ | Multi-level classification. Supports downstream mapping, counting, filtering. |
| **Collector** | `Collectors.toMap(k, v, merge, mapFactory)`| Java 8 | $O(N)$ | $O(N)$ | Key collision resolution. Use Java 22 `_` when ignoring duplicate values. |
| **Collector** | `Collectors.teeing(c1, c2, merger)` | Java 12 | $O(N)$ | $O(1)$ | Dual aggregation in a single pass (e.g. min + max simultaneously). |
| **Collector** | `toCollection(LinkedHashSet::new)` | **Java 21** | $O(N)$ | $O(N)$ | Preserves encounter order in a `SequencedSet`, enabling `getFirst()`/`getLast()`. |

---

## 🎓 The 4-Phase Curriculum Syllabus

```
Phase 1: Foundations            Phase 2: Core Processing        Phase 3: Advanced Streams      Phase 4: MAANG Mastery
(Lessons 01 - 04)               (Lessons 05 - 08)               (Lessons 09 - 12)              (Lessons 13 - 16)
┌───────────────────────┐       ┌───────────────────────┐       ┌───────────────────────┐      ┌────────────────────────┐
│ 01. Lambda Foundations│       │ 05. Sorting & Ordering│       │ 09. Primitive Streams │      │ 13. Stream Internals   │
│ 02. Stream Creation   │ ----> │ 06. Reduction & Stats │ ----> │ 10. Advanced FlatMap  │ ---> │ 14. Real-World Patterns│
│ 03. Filtering & Slice │       │ 07. Collecting Results│       │ 11. Custom Collectors │      │ 15. Interview Gauntlet │
│ 04. Mapping & Multi   │       │ 08. Grouping & Part.  │       │ 12. Parallel Streams  │      │ 16. Stream Gatherers   │
└───────────────────────┘       └───────────────────────┘       └───────────────────────┘      └────────────────────────┘
```

### Detailed Lesson Directory
1. **[Lesson01_LambdaFoundations](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson01_LambdaFoundations.java):**  
   Under the hood of Lambdas: `invokedynamic`, SAM contract, catalog (`Predicate`, `Function`, `Consumer`, `Supplier`), method references (static, bound, unbound, constructor), function composition (`andThen`, `compose`).
2. **[Lesson02_StreamCreation](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson02_StreamCreation.java):**  
   Conveyor belt model, Spliterator metadata (`ORDERED`, `DISTINCT`, `SORTED`, `SIZED`), infinite generators, bounded iteration, and **Java 21 `SequencedCollection.reversed().stream()`**.
3. **[Lesson03_FilteringAndMatching](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson03_FilteringAndMatching.java):**  
   Push-down predicate architecture, `takeWhile()`, `dropWhile()`, stateful `distinct()`, short-circuiting terminals (`allMatch`, `anyMatch`, `findFirst`, `findAny`).
4. **[Lesson04_MappingAndTransformation](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson04_MappingAndTransformation.java):**  
   `map` vs `flatMap` vs **`mapMulti` (Java 16/21+)**, primitive mapping (`mapToInt`, `mapMultiToInt`), avoiding Garbage Collector heap churn.
5. **[Lesson05_SortingAndOrdering](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson05_SortingAndOrdering.java):**  
   TimSort in streams, stateful barrier memory cost ($O(N)$), multi-tier `Comparator.comparing()`, null-safety (`nullsFirst`, `nullsLast`), and **$O(1)$ zero-copy reverse streaming with Java 21 Sequenced Collections**.
6. **[Lesson06_ReductionAndAggregation](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson06_ReductionAndAggregation.java):**  
   Monoids, mathematical folding, identity elements, accumulator vs combiner, `IntSummaryStatistics`, BigDecimal financial reduction.
7. **[Lesson07_CollectingResults](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson07_CollectingResults.java):**  
   Mutable reduction, `Stream.toList()` vs `Collectors.toList()`, collision-safe `toMap()` with **Java 22 unnamed variable `_`**, `joining()`, `collectingAndThen()`, and **collecting to `SequencedSet` (LinkedHashSet)**.
8. **[Lesson08_GroupingAndPartitioning](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson08_GroupingAndPartitioning.java):**  
   The SQL `GROUP BY` of streams: single-level and multi-level classification, downstream chains (`mapping`, `filtering`, `counting`, `averagingDouble`, `maxBy`), binary partitioning.
9. **[Lesson09_PrimitiveStreamsAndOptionals](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson09_PrimitiveStreamsAndOptionals.java):**  
   Cache locality and CPU registers: `IntStream`, `LongStream`, `DoubleStream`, `Optional` monad, eliminating `null`, flattening stream of optionals.
10. **[Lesson10_AdvancedFlatMapAndComposition](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson10_AdvancedFlatMapAndComposition.java):**  
    Cartesian cross-joins ($O(M \times N)$), coordinate zipping, inverted index construction (search engine architecture), multi-stream flattening without stack overflow.
11. **[Lesson11_CustomCollectors](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson11_CustomCollectors.java):**  
    Anatomy of `Collector<T, A, R>`, building custom statistics collectors, Top-K bounded heaps, reservoir sampling for infinite streams.
12. **[Lesson12_ParallelStreams](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson12_ParallelStreams.java):**  
    `ForkJoinPool.commonPool()`, Doug Lea's $N \times Q > 10,000$ heuristic, split costs (`ArrayList` vs `LinkedList`), race condition pitfalls, avoiding thread starvation.
13. **[Lesson13_StreamInternalsAndDebugging](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson13_StreamInternalsAndDebugging.java):**  
    Depth-first execution, loop fusion, stream flag propagation (`ORDERED`, `SIZED`, `DISTINCT`, `SORTED`), custom `Spliterator`, `peek()` debugging, `onClose()` resource management.
14. **[Lesson14_RealWorldPatterns](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson14_RealWorldPatterns.java):**  
    `Collectors.teeing()`, zero-allocation validation with `mapMulti`, **Java 24/25 Gatherers vs Java 17 manual range slicing**, **Java 21 Record Pattern Matching** in stream switch expressions.
15. **[Lesson15_MAANGInterviewGauntlet](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson15_MAANGInterviewGauntlet.java):**  
    20 LeetCode Medium/Hard algorithmic challenges solved with idiomatic streams (Top-K Frequent, Group Anagrams, Two Sum, Longest Substring Without Repeating, Course Schedule, Merge Intervals, Trapping Rain Water).
16. **[Lesson16_StreamGatherersAndModernFeatures](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson16_StreamGatherersAndModernFeatures.java):**  
    **JEP 485 Stream Gatherers (Java 24/25)**: `windowFixed`, `windowSliding`, `scan`, `fold`, `mapConcurrent` with Virtual Threads, custom sequential and greedy gatherers, short-circuiting, finisher flushing, and `andThen()` gatherer composition.

---

## 🛠️ How to Practice & Solve Exercises

### 1. Verify All Reference Lessons
Run the entire reference lesson suite:
```bash
mvn test
```
All 83 reference implementation tests will compile and pass.

### 2. Solve a Specific Exercise File
1. Open the exercise file: e.g. `src/test/java/com/streammastery/exercises/Exercise16_StreamGatherersAndModernFeatures.java`
2. Study the problem description and find the `// TODO:` line.
3. Replace `null` or placeholder with your stream pipeline.
4. Run the test for that specific exercise:
```bash
mvn test -Dtest="Exercise16_StreamGatherersAndModernFeatures"
```

### 3. Debugging Stream Pipelines
When an exercise assertion fails:
- Insert `.peek(x -> System.out.println("DEBUG: " + x))` between intermediate stages to inspect data as it flows through the conveyor belt.
- Check primitive boxing: use `mapToInt()` / `mapToDouble()` if doing numeric reductions.
- Ensure your lambda does NOT mutate external state (no `list.add()` inside `forEach`).

---

## 🏆 The Big Tech / MAANG Code Review Checklist

Before considering an exercise complete, audit your stream pipeline against this 5-point checklist:
- [ ] **Zero Boxing Overhead:** Are you using `IntStream` / `mapToDouble` / `mapMultiToInt` instead of `Stream<Integer>` / `Stream<Double>`?
- [ ] **Short-Circuiting:** Did you place `filter()` before `sorted()`? Did you use `findFirst()` or `anyMatch()` instead of processing the entire stream?
- [ ] **Push-Down Operations:** Are operations ordered to minimize element count at each subsequent stage?
- [ ] **Modern Idioms:** Did you use `collection.reversed().stream()` instead of `.sorted(Comparator.reverseOrder())` for ordered collections? Did you use `Gatherers.windowSliding()` instead of manual index math?
- [ ] **Immutability & Thread Safety:** Are your lambdas pure functions with zero side-effects?
