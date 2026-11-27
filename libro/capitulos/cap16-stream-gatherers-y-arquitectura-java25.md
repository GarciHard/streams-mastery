# Parte V: Arquitectura Moderna de Streams en Java 25 LTS

# Capítulo 16: Stream Gatherers ([JEP 485](https://openjdk.org/jeps/485)) — Enciclopedia de Operaciones Intermedias Personalizadas

> *"Con `Collector` en Java 8 aprendimos a personalizar cómo termina un Stream. Con `Gatherer` en Java 25 LTS, por fin podemos personalizar cómo fluye y se transforma un Stream por dentro."*

Durante más de una década (de Java 8 a Java 23), las operaciones terminales eran extensibles vía `Collector<T, A, R>`, pero las operaciones intermedias formaban un catálogo cerrado y fijo. **[JEP 485: Stream Gatherers](https://openjdk.org/jeps/485)** (diseñado por Viktor Klang, finalizado en JDK 24 y **estándar en Java 25 LTS**) resuelve definitivamente esta asimetría mediante **`Stream.gather(Gatherer<T, A, R>)`**.

En este capítulo cada uno de los **5 Gatherers integrados** de `java.util.stream.Gatherers` y cada fábrica de `Gatherer` cuentan con su **ficha técnica completa**: firma oficial JDK 25, teoría, traza visual paso a paso, escenarios reales de producción, trampas y ejemplos completos.

---

## 16.1 Anatomía Completa de `Gatherer<T, A, R>`

Un `Gatherer<T, A, R>` es una estación intermedia programable compuesta por **4 piezas** (3 opcionales + 1 obligatoria):

```text
  Upstream (T) ──▶ [ 1. initializer(): () -> A ]  (crea estado privado mutable A, opcional)
                          │
                          ▼
                   [ 2. integrator(): (A, T, Downstream<R>) -> boolean ]  (OBLIGATORIO)
                          ├── downstream.push(r)  (emite 0, 1 o N elementos R)
                          ├── retorna true   ──▶ pide el siguiente elemento T
                          └── retorna false  ──▶ ¡CORTOCIRCUITO! cancela upstream
                          │
                   [ 3. combiner(): (A, A) -> A ] (opcional: habilita ejecución paralela)
                          │
                          ▼
                   [ 4. finisher(): (A, Downstream<R>) -> void ] (al llegar al EOF: vacía buffers)
```

* **`Gatherer.Integrator.ofGreedy(...)`**: Promete que el integrador **nunca** devolverá `false` por iniciativa propia (solo se detiene si `downstream.push(...)` retorna `false` por un `.limit()` posterior). Permite a la JVM optimizar el bucle interno.
* **`Gatherer.Integrator.of(...)`**: Úsalo cuando tu propia lógica decide hacer cortocircuito devolviendo `false` (ej. presupuesto excedido).
* **`Downstream.push(R)`** y **`Downstream.isRejecting()`**: `push` envía un elemento hacia abajo; `isRejecting()` permite evitar trabajo inútil en el `finisher` si el downstream ya cerró.

---

## 16.2 Enciclopedia de los 5 Gatherers Integrados (`java.util.stream.Gatherers`)

### 1. `Gatherers.windowFixed(int windowSize)` — Lotes Disjuntos de Tamaño Fijo
* **Firma oficial:** `public static <TR> Gatherer<TR, ?, List<TR>> windowFixed(int windowSize)`
* **¿Qué hace y cómo funciona?** Acumula elementos en un búfer interno hasta reunir exactamente `windowSize` elementos y entonces emite una `List<TR>` inmodificable. Al llegar al final del Stream (`finisher`), si quedó un último lote parcial con $< \text{windowSize}$ elementos, **también lo emite** (nunca pierde datos). Lanza `IllegalArgumentException` si `windowSize <= 0`.
* **Traza visual:** `[1, 2, 3, 4, 5]` con `windowFixed(2)` → `[[1, 2], [3, 4], [5]]`.
* **Escenarios reales:** Inserciones por lotes (*batch inserts*) en JDBC, envíos paginados a APIs externas (ej. máximo 100 IDs por petición HTTP), lectura en bloques desde `Files.lines()` sin cargar el archivo completo en RAM.
```java
List<List<Integer>> batches = List.of(101, 102, 103, 104, 105, 106, 107).stream()
        .gather(Gatherers.windowFixed(3))
        .toList(); // [[101, 102, 103], [104, 105, 106], [107]]
```

### 2. `Gatherers.windowSliding(int windowSize)` — Ventanas Móviles Solapadas
* **Firma oficial:** `public static <TR> Gatherer<TR, ?, List<TR>> windowSliding(int windowSize)`
* **¿Qué hace y cómo funciona?** Mantiene una ventana deslizante que avanza **de 1 en 1**. La primera ventana se emite al reunir `windowSize` elementos; cada elemento posterior desplaza la ventana descartando el más antiguo y agregando el nuevo. Si el Stream total tiene menos de `windowSize` elementos (y al menos 1), emite una única ventana con todos los elementos disponibles.
* **Traza visual:** `[1, 2, 3, 4, 5]` con `windowSliding(3)` → `[[1, 2, 3], [2, 3, 4], [3, 4, 5]]`.
* **Escenarios reales:** Promedios móviles (*moving averages*) en finanzas, cálculo de deltas entre lecturas adyacentes (`w.get(1) - w.get(0)`), detección de violaciones de SLO (3 latencias seguidas > 100 ms) y detección de fraude (2 débitos consecutivos altos en la misma cuenta).
```java
List<Double> movingAvg = List.of(150.0, 152.0, 155.0, 153.0, 158.0).stream()
        .gather(Gatherers.windowSliding(3))
        .map(w -> w.stream().mapToDouble(Double::doubleValue).average().orElse(0.0))
        .toList();
```

### 3. `Gatherers.scan(Supplier<R> initial, BiFunction<? super R, ? super T, ? extends R> scanner)` — Acumulación Corrida (*Prefix Scan*)
* **Firma oficial:** `public static <T, R> Gatherer<T, ?, R> scan(Supplier<R> initial, BiFunction<? super R, ? super T, ? extends R> scanner)`
* **¿Qué hace y cómo funciona?** A diferencia de `reduce()` (que solo entrega el total final), `scan` aplica `state = scanner.apply(state, element)` y **emite el valor acumulado intermedio por cada elemento** (cardinalidad `1 : 1`). Si el Stream viene vacío, emite `0` elementos.
* **Traza visual:** `[1000.0, 500.0, -200.0, 750.0]` con `scan(() -> 0.0, Double::sum)` → `[1000.0, 1500.0, 1300.0, 2050.0]`.
* **Escenarios reales:** Saldo bancario corrido después de cada movimiento (*running balance*), sumas prefijas (*prefix sums*), máximo histórico acumulado (*running max*) y cálculo de *drawdown* en bolsa.
```java
List<Double> runningBalance = List.of(1000.0, 500.0, -200.0, 750.0).stream()
        .gather(Gatherers.scan(() -> 0.0, Double::sum))
        .toList(); // [1000.0, 1500.0, 1300.0, 2050.0]
```

### 4. `Gatherers.fold(Supplier<R> initial, BiFunction<? super R, ? super T, ? extends R> folder)` — Reducción Intermedia
* **Firma oficial:** `public static <T, R> Gatherer<T, ?, R> fold(Supplier<R> initial, BiFunction<? super R, ? super T, ? extends R> folder)`
* **¿Por qué existe si ya existe `Stream.reduce()`?**
  1. `reduce()` es **terminal** (cierra el Stream); **`fold()` es intermedio** (emite el resultado único dentro de un `Stream<R>` para seguir encadenando `.map()`, `.filter()`, otro `.gather()`, etc.).
  2. `fold()` **NO exige asociatividad** porque es secuencial (puedes plegar tipos distintos `T -> R` con cualquier lógica ordenada sin necesitar `combiner`).
  3. **Caso borde:** Si el Stream está vacío, `fold` emite el valor devuelto por `initial.get()` (1 elemento).
```java
List<String> summary = List.of("Cloud", "Native", "Java25").stream()
        .gather(Gatherers.fold(() -> "", (acc, s) -> acc.isEmpty() ? s : acc + "-" + s))
        .map(String::toUpperCase)
        .toList(); // ["CLOUD-NATIVE-JAVA25"]
```

### 5. `Gatherers.mapConcurrent(int maxConcurrency, Function<? super T, ? extends R> mapper)` — Concurrencia I/O en Hilos Virtuales
* **Firma oficial:** `public static <T, R> Gatherer<T, ?, R> mapConcurrent(int maxConcurrency, Function<? super T, ? extends R> mapper)`
* **¿Qué hace y cómo funciona?** Lanza cada invocación de `mapper.apply(t)` en su propio **Hilo Virtual** ([JEP 444](https://openjdk.org/jeps/444) / [JEP 491](https://openjdk.org/jeps/491)) manteniendo a lo sumo `maxConcurrency` tareas en vuelo simultáneamente, y **entrega los resultados estrictamente en el orden original de encuentro**.
* **Superpoder de cancelación:** Si encadenas `.limit(5)` o `.findFirst()`, cancela e interrumpe automáticamente los hilos virtuales restantes en cuanto obtiene los resultados necesarios.
* **Escenarios reales:** Enriquecer registros llamando a APIs HTTP, microservicios o bases de datos sin bloquear jamás `ForkJoinPool.commonPool()`.
* **Trampa importante en JDK 25 (`ScopedValue`):** Como `mapConcurrent` crea hilos virtuales no estructurados (fuera de `StructuredTaskScope`), **`ScopedValue` ([JEP 506](https://openjdk.org/jeps/506)) no se hereda automáticamente**; captura el valor en el hilo padre y re-vincúlalo dentro del lambda con `ScopedValue.where(KEY, val).call(...)` (ver Capítulo 18).
```java
List<String> responses = List.of("users", "orders", "payments", "auth").stream()
        .gather(Gatherers.mapConcurrent(4, svc -> callMicroservice(svc)))
        .toList(); // Siempre en orden exacto: [users, orders, payments, auth]
```

---

## 16.3 Construyendo Gatherers Personalizados: `ofSequential`, `of` (Paralelo) y `andThen`

### 1. `distinctBy(Function<T, K>)` — Deduplicación perezosa por clave (`ofSequential` + `ofGreedy`)
```java
public static <T, K> Gatherer<T, Set<K>, T> distinctBy(Function<? super T, ? extends K> keyExtractor) {
    return Gatherer.ofSequential(
            HashSet::new,
            Gatherer.Integrator.ofGreedy((seenKeys, element, downstream) -> {
                if (seenKeys.add(keyExtractor.apply(element))) {
                    return downstream.push(element);
                }
                return true;
            })
    );
}
```

### 2. *Run-Length Encoding* (RLE) verdadero — Uso obligatorio de `finisher` al EOF
```java
record Run<T>(T value, int count) {}

public static <T> Gatherer<T, ?, Run<T>> runLengthEncode() {
    class RleState { T current; int count = 0; boolean started = false; }
    return Gatherer.ofSequential(
            RleState::new,
            Gatherer.Integrator.ofGreedy((state, elem, downstream) -> {
                if (!state.started) {
                    state.current = elem; state.count = 1; state.started = true;
                    return true;
                }
                if (Objects.equals(state.current, elem)) {
                    state.count++;
                    return true;
                }
                boolean keepGoing = downstream.push(new Run<>(state.current, state.count));
                state.current = elem; state.count = 1;
                return keepGoing;
            }),
            (state, downstream) -> {
                if (state.started && !downstream.isRejecting()) {
                    downstream.push(new Run<>(state.current, state.count));
                }
            }
    );
}
```

### 3. Media Móvil Exponencial (EMA) y Cortocircuito Propio (`Integrator.of`)
```java
// EMA: EMA_0 = x_0, EMA_t = alpha * x_t + (1 - alpha) * EMA_{t-1}
public static Gatherer<Double, ?, Double> exponentialMovingAverage(double alpha) {
    class EmaState { boolean initialized = false; double ema = 0.0; }
    return Gatherer.ofSequential(
            EmaState::new,
            Gatherer.Integrator.ofGreedy((state, value, downstream) -> {
                state.ema = !state.initialized ? value : alpha * value + (1.0 - alpha) * state.ema;
                state.initialized = true;
                return downstream.push(state.ema);
            })
    );
}

// Cortocircuito propio (NO greedy): detiene el upstream al superar el presupuesto
public static Gatherer<Double, ?, Double> takeWhileSumAtMost(double maxBudget) {
    class Tracker { double sum = 0.0; }
    return Gatherer.ofSequential(
            Tracker::new,
            Gatherer.Integrator.of((tracker, expense, downstream) -> {
                if (tracker.sum + expense <= maxBudget) {
                    tracker.sum += expense;
                    return downstream.push(expense);
                }
                return false; // Cortocircuito inmediato
            })
    );
}
```

### 4. Gatherer Paralelizable con `Gatherer.of(...)` y `combiner`
```java
public static <T> Gatherer<T, ?, T> parallelMax(Comparator<? super T> comparator) {
    class Best { T value; boolean present; }
    return Gatherer.of(
            Best::new,
            Gatherer.Integrator.ofGreedy((best, elem, _) -> {
                if (!best.present || comparator.compare(elem, best.value) > 0) {
                    best.value = elem; best.present = true;
                }
                return true;
            }),
            (left, right) -> {
                if (!left.present) return right;
                if (!right.present) return left;
                return comparator.compare(left.value, right.value) >= 0 ? left : right;
            },
            (best, downstream) -> {
                if (best.present && !downstream.isRejecting()) {
                    downstream.push(best.value);
                }
            }
    );
}
```

### 5. Composición nativa `gathererA.andThen(gathererB)` y Sinergias Java 21–25
```java
// Componer Gatherers como bloques LEGO
Gatherer<Integer, ?, List<Integer>> batcher = Gatherers.windowFixed(2);
Gatherer<List<Integer>, ?, Integer> runningBatchSum = Gatherers.scan(
        () -> 0, (acc, batch) -> acc + batch.stream().mapToInt(Integer::intValue).sum());

List<Integer> cumulativeSums = List.of(1, 2, 3, 4, 5, 6).stream()
        .gather(batcher.andThen(runningBatchSum))
        .toList(); // [3, 10, 21]

// Sinergia JEP 431 + JEP 485: pares de rollback en O(1) sin copias
List<List<String>> rollbackPairs = List.of("Step1", "Step2", "Step3", "Step4")
        .reversed().stream()
        .gather(Gatherers.windowSliding(2))
        .toList(); // [[Step4, Step3], [Step3, Step2], [Step2, Step1]]
```

---

## 16.4 Retos de Dominio y Autoevaluación (Capítulo 16)

1. **Nivel Inicial:** Aplica los 5 Gatherers integrados (`windowFixed`, `windowSliding`, `scan`, `fold`, `mapConcurrent`) explicando qué emite cada uno cuando el Stream de entrada está vacío.
2. **Nivel Intermedio:** Construye `takeLastN(int n)` usando un `ArrayDeque` acotado que vacíe los últimos $N$ elementos en el `finisher`, y `takeWhileSumAtMost(double limit)` con `Integrator.of(...)` que cortocircuite sobre un Stream infinito.
3. **Nivel Avanzado:** Implementa *Run-Length Encoding* verdadero (`"aaabbcaaa"` → `[a:3, b:2, c:1, a:3]`) y la *Media Móvil Exponencial (EMA)* en una sola pasada.
4. **Nivel Staff MAANG:** Compón dos Gatherers con `g1.andThen(g2)`, implementa un `Gatherer.of(...)` paralelizable con `combiner`, y explica cómo propagar `ScopedValue` dentro de `Gatherers.mapConcurrent`.
