# ☕ Java Streams Mastery: Zero to MAANG Hero

> **A brutally comprehensive, production-grade, Big Tech / MAANG-bar curriculum covering every corner of the Java Stream API.**  
> Built from scratch with **16 in-depth lessons** (83 passing reference implementations) and **320 hands-on exercises** (strictly 20 per lesson) with incremental difficulty from Beginner [★☆☆☆☆] to Principal Engineer [★★★★★].  
> Fully upgraded to **Java 25 LTS** with complete coverage of **Stream Gatherers (JEP 485)**, **Sequenced Collections (JEP 431)**, **mapMulti**, and **Record Pattern Matching**.

```
 ╔══════════════════════════════════════════════════════════════════════╗
 ║   "Talk is cheap. Show me the stream pipeline." — Linus T.           ║
 ║   "If your stream boxes primitives in an inner loop, you're fired."  ║
 ║                                             — Dictatorial MAANG Bar  ║
 ╚══════════════════════════════════════════════════════════════════════╝
```

---

## 🚀 Quick Start & Onboarding

> 📖 **New here? Read the [Definitive Onboarding Guide (ONBOARDING.md)](file:///Users/garcihard/Documents/Playground/java-streams-mastery/ONBOARDING.md)** for a deep dive into the 4 Foundational Mental Models, the complete Java 8 to 25 API feature catalog, and the Big Tech Code Review Checklist.

### 1. Verify all reference lessons (83 passing tests)
```bash
mvn test
```

### 2. Solve and run an exercise set
```bash
mvn test -Dtest="Exercise16_StreamGatherersAndModernFeatures"
```

---

## 🗂️ Curriculum Overview

| Metric | Total |
|---|---|
| **Comprehensive Lessons** | **16 Lessons** (83 Reference Implementations + Deep Architectural Notes) |
| **Interactive Exercises** | **320 Exercises** (Strictly 20 per lesson, incrementally graded) |
| **Difficulty Range** | [★☆☆☆☆] Beginner to [★★★★★] MAANG Principal / Staff |
| **Target Java Version** | **Java 25 LTS** (Stream Gatherers, Sequenced Collections, Unnamed Variables, Records) |
| **Test Engine** | JUnit 5 Jupiter + AssertJ Fluent Assertions |

---

## 📚 Syllabus & Lesson Plan

### Part I: Foundations (Lessons 01–04 · 80 Exercises)
| # | Lesson | Exercises | Core Topics & MAANG Interview Angles |
|---|---|:---:|---|
| 01 | [**Lambda Foundations**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson01_LambdaFoundations.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise01_LambdaFoundations.java) | Functional interfaces, `@FunctionalInterface`, `invokedynamic`, closures, effectively final variables, method references (static, bound, unbound, constructor), `Predicate`, `Function`, `Consumer`, `Supplier`, `Comparator`, composition (`andThen`, `compose`). |
| 02 | [**Stream Creation & Anatomy**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson02_StreamCreation.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise02_StreamCreation.java) | `Collection.stream()`, `Arrays.stream()`, `Stream.of()`, `Stream.builder()`, `Stream.generate()`, 3-arg `Stream.iterate()`, `IntStream.range()` vs `rangeClosed()`, infinite streams, `limit()`, `skip()`, `Spliterator` flags, and **Java 21 `SequencedCollection.reversed().stream()`**. |
| 03 | [**Filtering & Matching**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson03_FilteringAndMatching.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise03_FilteringAndMatching.java) | `filter()`, multi-condition predicate composition, `takeWhile()`, `dropWhile()`, stateful `distinct()`, short-circuiting terminals: `allMatch()`, `anyMatch()`, `noneMatch()`, `findFirst()`, `findAny()`, null-filtering idioms. |
| 04 | [**Mapping & Transformation**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson04_MappingAndTransformation.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise04_MappingAndTransformation.java) | 1-to-1 transformation via `map()`, DTO projections, primitive mapping (`mapToInt`, `mapToLong`, `mapToDouble`), 1-to-N transformation via `flatMap()`, and **zero-allocation push-based `mapMulti()` and `mapMultiToInt()`**. |

---

### Part II: Core Operations (Lessons 05–08 · 80 Exercises)
| # | Lesson | Exercises | Core Topics & MAANG Interview Angles |
|---|---|:---:|---|
| 05 | [**Sorting & Ordering**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson05_SortingAndOrdering.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise05_SortingAndOrdering.java) | Natural ordering vs custom comparators, `Comparator.comparing()`, chained sorting with `thenComparing()`, `reversed()`, null-safety via `nullsFirst()` and `nullsLast()`, stateful barrier memory cost ($O(N)$ buffer), and **$O(1)$ zero-copy reverse streaming with Java 21 `reversed().stream()`**. |
| 06 | [**Reduction & Aggregation**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson06_ReductionAndAggregation.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise06_ReductionAndAggregation.java) | Mathematical foundations of fold/reduce: associativity, identity elements, accumulator vs combiner in parallel execution, specialized terminals: `count()`, `sum()`, `min()`, `max()`, `average()`, `IntSummaryStatistics`, BigDecimal financial reduction. |
| 07 | [**Collecting Results**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson07_CollectingResults.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise07_CollectingResults.java) | Mutable reduction with `collect()`, `toList()`, `toSet()`, `toCollection()`, `toMap()` with merge collision resolution using **Java 22 unnamed variable `_`**, `joining()`, `collectingAndThen()`, and **collecting to `SequencedSet` (LinkedHashSet)**. |
| 08 | [**Grouping & Partitioning**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson08_GroupingAndPartitioning.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise08_GroupingAndPartitioning.java) | Single-level and multi-level classification with `groupingBy()`, downstream collectors (`mapping`, `filtering`, `counting`, `averagingDouble`, `maxBy`), binary classification via `partitioningBy()`. |

---

### Part III: Advanced Streams (Lessons 09–12 · 80 Exercises)
| # | Lesson | Exercises | Core Topics & MAANG Interview Angles |
|---|---|:---:|---|
| 09 | [**Primitive Streams & Optionals**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson09_PrimitiveStreamsAndOptionals.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise09_PrimitiveStreamsAndOptionals.java) | Cache locality and GC pressure: `IntStream`, `LongStream`, `DoubleStream` vs `Stream<T>`, `Optional` monad, `orElse()` vs lazy `orElseGet()`, `orElseThrow()`, chaining `map()` / `flatMap()` / `filter()`, stream-of-optionals flattening. |
| 10 | [**Advanced flatMap & Composition**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson10_AdvancedFlatMapAndComposition.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise10_AdvancedFlatMapAndComposition.java) | Nested flatMap, relational inner/outer joins, Cartesian cross-products, stream concatenation (`Stream.concat()`), batch sliding windows, matrix flattening, tokenization pipelines. |
| 11 | [**Custom Collectors**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson11_CustomCollectors.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise11_CustomCollectors.java) | Anatomy of `Collector<T, A, R>`: `supplier`, `accumulator`, `combiner`, `finisher`, `characteristics` (`CONCURRENT`, `UNORDERED`, `IDENTITY_FINISH`). Custom reservoir sampling, Top-K bounded collectors, running median. |
| 12 | [**Parallel Streams & Performance**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson12_ParallelStreams.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise12_ParallelStreams.java) | `ForkJoinPool.commonPool()`, Doug Lea's $N \times Q > 10,000$ heuristic, split costs (`ArrayList` vs `LinkedList` vs `iterate`), thread safety, race conditions, avoiding state corruption, custom fork-join pools, false sharing. |

---

### Part IV: Production Engineering & Gauntlet (Lessons 13–15 · 60 Exercises)
| # | Lesson | Exercises | Core Topics & MAANG Interview Angles |
|---|---|:---:|---|
| 13 | [**Stream Internals & Debugging**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson13_StreamInternalsAndDebugging.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise13_StreamInternalsAndDebugging.java) | Depth-first execution, loop fusion, stream flags (`DISTINCT`, `SORTED`, `ORDERED`, `SIZED`), pipeline stages, custom `Spliterator`, `tryAdvance()` vs `forEachRemaining()`, debugging with `peek()`, resource management with `onClose()`. |
| 14 | [**Real-World Patterns & Idioms**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson14_RealWorldPatterns.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise14_RealWorldPatterns.java) | `Collectors.teeing()`, zero-allocation domain validation with `mapMulti`, **Java 24/25 Gatherers vs Java 17 manual range indexing**, and **Java 21 Record Pattern Matching** in stream switch pipelines. |
| 15 | [**MAANG Interview Gauntlet**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson15_MAANGInterviewGauntlet.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise15_MAANGInterviewGauntlet.java) | 20 hard algorithmic problems solved with idiomatic streams: Top-K Frequent Elements, Group Anagrams, Two Sum, Longest Substring Without Repeating Characters, Trapping Rain Water, Course Schedule, Merge Intervals, and Matrix Rotations. |

---

### Part V: Modern Streams Architecture (Lesson 16 · 20 Exercises)
| # | Lesson | Exercises | Core Topics & MAANG Interview Angles |
|---|---|:---:|---|
| 16 | [**Stream Gatherers & Modern Architecture**](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/lessons/Lesson16_StreamGatherersAndModernFeatures.java) | [20 Exercises](file:///Users/garcihard/Documents/Playground/java-streams-mastery/src/test/java/com/streammastery/exercises/Exercise16_StreamGatherersAndModernFeatures.java) | **JEP 485 (Java 24/25)** Stream Gatherers: `windowFixed()`, `windowSliding()`, `scan()`, `fold()`, `mapConcurrent()` with **Virtual Threads**, custom stateful `Gatherer<T, A, R>`, `Integrator.ofGreedy`, short-circuiting, finisher flushing, and `andThen()` gatherer composition. |

---

## 🏗️ Project Architecture

```
java-streams-mastery/
├── pom.xml                                   ← Upgraded to Java 25 LTS release
├── README.md                                 ← Master syllabus & curriculum index
├── ONBOARDING.md                             ← The definitive onboarding guide & cheat-sheet
└── src/
    ├── main/java/com/streammastery/
    │   ├── model/
    │   │   ├── Employee.java                 ← Rich tech entity (skills, salary, rating, city)
    │   │   ├── EmployeeDTO.java              ← Transformation projections
    │   │   ├── Product.java                  ← E-commerce catalog item
    │   │   ├── Order.java                    ← Multi-item customer orders
    │   │   ├── OrderItem.java                ← Nested order line items
    │   │   └── Transaction.java              ← Financial transactions (amounts, fraud status)
    │   └── util/
    │       └── DataFactory.java              ← 30 employees, 30 products, 12 orders, 30 transactions
    └── test/java/com/streammastery/
        ├── lessons/                          ← 16 Lessons (83 passing reference test implementations)
        │   ├── Lesson01_LambdaFoundations.java
        │   ├── ...
        │   ├── Lesson15_MAANGInterviewGauntlet.java
        │   └── Lesson16_StreamGatherersAndModernFeatures.java
        └── exercises/                        ← 16 Companion Exercise Sets (320 interactive exercises)
            ├── Exercise01_LambdaFoundations.java
            ├── ...
            ├── Exercise15_MAANGInterviewGauntlet.java
            └── Exercise16_StreamGatherersAndModernFeatures.java
```

---

## 📏 Dictatorial MAANG Grading Rubric

When submitting your completed exercises for grading, your code is evaluated against the strictest Big Tech engineering standard:

| Criterion | Weight | Failure Triggers |
|---|:---:|---|
| **Correctness & Output Precision** | **40%** | Any assertion failure, index error, or wrong aggregation. |
| **Idiomatic Stream Architecture** | **25%** | Writing loops inside `forEach`, mutating external collections, failing to use method references when cleaner, improper use of `peek()`. |
| **Zero Unnecessary Boxing** | **15%** | Using `Stream<Integer>` where `IntStream` belongs; calling `.map()` before `.reduce()` instead of `mapToInt().sum()`; thrashing the GC. |
| **Short-Circuiting & Efficiency** | **10%** | Running exhaustive filters before `findFirst()` or `anyMatch()`; sorting an entire stream when only top element is needed; redundant collections. |
| **Thread Safety & Immutability** | **10%** | Non-thread-safe state in parallel streams; side-effects in lambda functions; modifying source collections. |

### Score Thresholds
- 🟢 **90% – 100% (MAANG L6+ Staff/Principal):** Flawless, idiomatic, zero-allocation where possible, optimal algorithmic complexity.
- 🟡 **70% – 89% (MAANG L4/L5 Senior):** Functionally correct, but minor boxing, missed short-circuit opportunities, or clunky pipelines.
- 🔴 **Below 70% (REJECTED):** Non-idiomatic loops, mutation bugs, broken edge cases. Back to the lessons!
