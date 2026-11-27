# Java Streams: De Cero a Héroe MAANG — Edición Java 25 LTS
## Tratado Integral de Programación Funcional, Arquitectura Interna de la JVM y Patrones de Ingeniería de Alto Rendimiento

> **Plataforma objetivo:** [Java 25 LTS (JDK 25, JSR 400)](https://openjdk.org/projects/jdk/25/) — Lanzamiento GA del 16 de septiembre de 2025  
> **Estructura de la obra:** 18 Capítulos Enciclopédicos · Solucionario Maestro y Caso de Estudio Capstone · Apéndice de Referencia MAANG  
> **Estándar técnico:** De Cero Absoluto a Nivel L6+ Staff / Principal Engineer · Idioma: Español (México)

---

## Prólogo: Por Qué Existe Este Libro

La mayoría de los materiales sobre Java Streams se limitan a mostrar tres ejemplos triviales de `filter`, `map` y `collect` y dan por terminado el tema. Sin embargo, cuando pasas de un tutorial básico a un sistema en producción con millones de eventos por segundo —o te sientas en una entrevista técnica de nivel Senior, Staff o Principal en una empresa MAANG— descubres rápidamente que **saber la sintaxis no alcanza**.

Un ingeniero de élite debe responder con absoluta soltura preguntas como:
* ¿Qué diferencia hay a nivel de bytecode (`invokedynamic`) y memoria entre una lambda sin estado y una clase anónima interna?
* ¿Por qué un `Stream<Integer>` puede degradar el rendimiento en un factor de 5× frente a un `IntStream`, y cómo cambia el panorama con **Compact Object Headers ([JEP 519](https://openjdk.org/jeps/519))** en Java 25?
* ¿Por qué `DoubleStream.sum()` utiliza suma compensada de Kahan y es numéricamente más preciso que `reduce(0.0, Double::sum)`?
* ¿Cuándo una operación intermedia como `sorted()` o `distinct()` introduce una barrera con estado de memoria $O(N)$ que destruye la evaluación perezosa?
* ¿Por qué `mapMulti()` puede ser hasta 4× más rápido que `flatMap()` en rutas críticas?
* ¿Cómo elimina `collection.reversed().stream()` (**[JEP 431](https://openjdk.org/jeps/431)**) el costo $O(N \log N)$ de invertir colecciones ordenadas?
* ¿Cómo revolucionan los **Stream Gatherers ([JEP 485](https://openjdk.org/jeps/485))** la arquitectura de Streams en Java 24/25 al permitir operaciones intermedias personalizadas con estado, cortocircuito y concurrencia acotada sobre **Hilos Virtuales ([JEP 444](https://openjdk.org/jeps/444) y [JEP 491](https://openjdk.org/jeps/491))**?
* ¿Cómo manejar *Checked Exceptions* sin romper la elegancia funcional y cuándo un bucle `for` clásico supera a cualquier Stream?

Este tratado está diseñado bajo la filosofía pedagógica de los grandes manuales de referencia (*Deitel & Deitel* y *Effective Java*): **no asume conocimiento previo alguno sobre programación funcional ni Streams**, explica cada pieza desde sus cimientos, incluye pruebas de escritorio paso a paso, anatomía interna de la JVM, ejemplos 100% autocontenidos en Java 25 LTS y retos progresivos al cierre de cada capítulo.

---

## Convenciones Didácticas del Libro

A lo largo de los 18 capítulos encontrarás tres tipos de recuadros clave:

> **Observación de Ingeniería de Software:** Principios de diseño, mantenibilidad, legibilidad y arquitectura limpia en equipos de producción.

> **Tip de Rendimiento JVM / MAANG:** Optimizaciones concretas de memoria, localidad de caché L1/L2, eliminación de *boxing*, cortocircuito y complejidad asintótica.

> **Error Común de Programación:** Trampas reales que provocan bugs silenciosos, `NullPointerException`, `IllegalStateException`, condiciones de carrera o rechazos inmediatos en entrevistas.

---

## Los 4 Modelos Mentales Fundamentales de Java 25 Streams

Antes de escribir una sola línea de código, internaliza estos cuatro modelos mentales:

```text
+---------------------------------------------------------------------------------------------+
|                        EL PIPELINE MODERNO DE JAVA 25 LTS                                   |
|                                                                                             |
|  [ FUENTE ]        [ SIN ESTADO ]     [ GATHERER (JEP 485) ]     [ TERMINAL / COLLECTOR ]   |
|  List / Array  --> filter / map  -->  windowFixed / scan    -->  toList() / collect()       |
|  reversed()        mapMulti           mapConcurrent (VThreads)   reduce() / anyMatch()      |
|                                                                                             |
|  1. Banda transportadora (Lazy): cero ciclos de CPU hasta invocar la operación terminal.    |
|  2. Fusión de bucles (Loop Fusion): flujo vertical elemento por elemento (depth-first).     |
|  3. Trinidad de transformación: map (1:1), flatMap (1:0..N stream), mapMulti (1:0..N push). |
|  4. Extensibilidad total: Collector<T,A,R> (terminal) + Gatherer<T,A,R> (intermedio).       |
+---------------------------------------------------------------------------------------------+
```

---

## Evolución Completa de Java Streams: De Java 8 a Java 25 LTS

| Versión | Hitos Clave en Streams y Lenguaje Funcional | JEP / Especificación |
| :--- | :--- | :--- |
| **Java 8 (LTS)** | Nacimiento de Lambdas (`invokedynamic`), `java.util.function`, `Stream`, `IntStream`/`LongStream`/`DoubleStream`, `Optional`, `Collectors`, `Spliterator`, `ForkJoinPool`. | JSR 335 |
| **Java 9** | `takeWhile`, `dropWhile`, `Stream.ofNullable`, `Stream.iterate` de 3 argumentos, `Optional.stream()`, `Optional.or()`, `Optional.ifPresentOrElse()`, `Collectors.filtering()`, `Collectors.flatMapping()`, `Matcher.results()`, optimización `SIZED` de `count()`. | JEP 213 / Core Libs |
| **Java 10** | `var`, `Collectors.toUnmodifiableList()`, `toUnmodifiableSet()`, `toUnmodifiableMap()`. | JEP 286 |
| **Java 11 (LTS)** | `Predicate.not()`, `String.lines()`. | Core Libs |
| **Java 12** | `Collectors.teeing(c1, c2, merger)` para agregaciones duales en una sola pasada. | Core Libs |
| **Java 16** | `Stream.toList()` (inmodificable, acepta nulos), `Stream.mapMulti()` y variantes primitivas, `record` finalizados. | JEP 395 / Core Libs |
| **Java 17 (LTS)** | `sealed` classes e interfaces (ideales para `Result<T, E>` y jerarquías exhaustivas de eventos). | JEP 409 |
| **Java 21 (LTS)** | **Sequenced Collections** (`reversed().stream()`, `getFirst()`, `getLast()`), **Virtual Threads**, **Record Patterns** y **Pattern Matching para `switch`**. | [JEP 431](https://openjdk.org/jeps/431), [JEP 444](https://openjdk.org/jeps/444) |
| **Java 22** | **Unnamed Variables & Patterns (`_`)**: limpia lambdas con parámetros ignorados como `(existing, _) -> existing` y patrones de `record`. | [JEP 456](https://openjdk.org/jeps/456) |
| **Java 24** | **Stream Gatherers** finalizados (`Stream.gather`, `Gatherer`, `Gatherers`), **Hilos Virtuales sin *pinning*** en bloques `synchronized`. | [JEP 485](https://openjdk.org/jeps/485), [JEP 491](https://openjdk.org/jeps/491) |
| **Java 25 (LTS)** | Consolida todo lo anterior en la nueva base LTS, añade **Scoped Values** finalizados, **Compact Object Headers** (`-XX:+UseCompactObjectHeaders`) y mejoras quirúrgicas de **Java Flight Recorder**. | [JDK 25](https://openjdk.org/projects/jdk/25/), [JEP 506](https://openjdk.org/jeps/506), [JEP 519](https://openjdk.org/jeps/519) |

---

## Modelos de Dominio Autocontenidos del Libro

Para que todos los ejemplos de este tratado sean realistas —simulando sistemas corporativos de recursos humanos, comercio electrónico y banca— utilizaremos seis entidades de dominio estándar. Puedes copiar sus contratos en cualquier proyecto Java 25 o sesión de `jshell` para ejecutar cada ejemplo por tu cuenta:

| Entidad | Campos y Getters Principales | Notas de Contrato |
| :--- | :--- | :--- |
| **`Employee`** | `id`, `firstName`, `lastName`, `email`, `department`, `title`, `age`, `salary`, `city`, `gender`, `yearsOfExperience`, `programmingLanguage` | `getFullName()` retorna `firstName + " " + lastName`. Igualdad por `id`. No implementa `Comparable`. |
| **`EmployeeDTO`** | `id`, `fullName`, `department`, `salary` | Fábrica estática `EmployeeDTO.fromEmployee(Employee e)` para proyecciones limpias. |
| **`Product`** | `id`, `name`, `category`, `price`, `stock`, `rating`, `brand` | Igualdad por `id`. No implementa `Comparable`. |
| **`Order`** | `id`, `customerId`, `customerName`, `status` (`"PENDING"`, `"SHIPPED"`, `"DELIVERED"`, `"CANCELLED"`), `orderDate` (`LocalDate`), `items` (`List<OrderItem>`) | `getTotalPrice()` suma `price * quantity` con `mapToDouble().sum()`. |
| **`OrderItem`** | `productName`, `category`, `price`, `quantity` | `getSubtotal()` retorna `price * quantity`. |
| **`Transaction`** | `id`, `accountId`, `type` (`Transaction.Type.CREDIT` / `DEBIT`), `amount`, `currency`, `category`, `date` (`LocalDate`), `merchant` | Enum interno `Transaction.Type`. Igualdad por `id`. |

---

## Estándar de Revisión de Código MAANG (Rúbrica de Producción)

Cada pipeline de Streams que escribas en entrevistas o en producción debe pasar cinco filtros de calidad:

| Criterio | Peso | Qué Provoca Penalización o Rechazo |
| :--- | ---: | :--- |
| **1. Corrección y casos borde** | 40% | Fallos ante streams vacíos (*vacuous truth*), valores `null`, claves duplicadas o errores de redondeo. |
| **2. Arquitectura idiomática Java 25** | 25% | Bucles imperativos disfrazados en `forEach`, mutación externa, omitir referencias a métodos o idiomas modernos (`reversed().stream()`, `_`, `Gatherers`). |
| **3. Cero *boxing* innecesario** | 15% | Usar `Stream<Integer>`/`Stream<Double>` o `.map().reduce()` donde corresponde `IntStream`, `mapToDouble().sum()` o `mapMultiToInt()`. |
| **4. Cortocircuito y eficiencia** | 10% | `sorted().findFirst()` en vez de `min()`, `filter().count() > 0` en vez de `anyMatch()`, o `sorted()` antes de `filter()`. |
| **5. Seguridad de hilos e inmutabilidad** | 10% | Estado compartido mutable en paralelo, efectos secundarios en lambdas o interferencia sobre la colección fuente. |
