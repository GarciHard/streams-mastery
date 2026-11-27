# Capítulo 12: Streams Paralelos, Concurrencia y Rendimiento en Java 25

> *"Agregar `.parallel()` a un Stream es la forma más fácil de escribir una línea de código en Java... y la forma más fácil de hacer tu programa 10 veces más lento o corromper silenciosamente tus datos si no entiendes cómo funciona el `ForkJoinPool`."*

---

## 12.1 Introducción desde cero: Concurrencia vs. Paralelismo

Antes de escribir una sola línea con `.parallelStream()`, aclaremos dos conceptos que suelen confundirse en entrevistas:

* **Concurrencia** es *estructurar* un sistema para gestionar múltiples tareas en progreso al mismo tiempo (por ejemplo, 10,000 peticiones HTTP esperando respuesta de red en pocos núcleos usando **Virtual Threads** de Java 21–25).
* **Paralelismo** es *ejecutar* físicamente múltiples sub-tareas en el mismo instante en varios núcleos de CPU para dividir un cálculo intensivo en pedazos más pequeños.

Los **Streams paralelos** de Java fueron diseñados específicamente para **paralelismo de datos intensivo en CPU (*CPU-bound*)**, **NO** para operaciones bloqueantes de entrada/salida (*I/O-bound*) como llamadas HTTP o consultas a base de datos.

### ¿Cómo se activa el paralelismo en un Stream?
Existen dos formas equivalentes:
1. Desde una colección: `lista.parallelStream()`
2. Cambiando el modo de un Stream existente: `stream.parallel()` (y puedes revertirlo con `.sequential()`).

> **Error Común de Programación (Última llamada gana)**
> No puedes hacer que la mitad de un pipeline sea paralela y la otra mitad secuencial. Las banderas `.parallel()` y `.sequential()` solo modifican un booleano interno del pipeline completo: **gana la última que se invoque antes de la operación terminal**.

---

## 12.2 Bajo el capó: `ForkJoinPool.commonPool()` y *Work-Stealing*

Cuando ejecutas una operación terminal en un Stream paralelo, Java sigue la estrategia **Divide y Vencerás (*Fork/Join*)**:

1. **División (*Split*)**: El `Spliterator` de la fuente intenta partir los datos por la mitad de forma recursiva hasta que cada sub-lote es suficientemente pequeño.
2. **Despacho (*Fork*)**: Las sub-tareas se envían al pool global compartido **`ForkJoinPool.commonPool()`**, que por defecto tiene `Runtime.getRuntime().availableProcessors() - 1` hilos trabajadores (más el hilo que invocó la operación terminal, sumando exactamente el número de núcleos disponibles).
3. **Robo de Trabajo (*Work-Stealing*)**: Cada hilo trabajador tiene su propia cola doble (*deque*). Si el Hilo 2 termina rápido sus tareas, **roba** una sub-tarea del final de la cola del Hilo 1 que sigue ocupado. Así ningún núcleo queda ocioso.
4. **Fusión (*Join*)**: Los resultados parciales de cada sub-tarea se combinan de vuelta usando el `combiner` de `reduce()` o `collect()`.

```text
                    [ Stream de 1,000,000 elementos ]
                                   │
                       Spliterator.trySplit()
                     ┌─────────────┴─────────────┐
               [ 0..499,999 ]            [ 500,000..999,999 ]
               (Hilo Worker-1)              (Hilo Worker-2)
               Deque: [T1, T2]              Deque: [T3] ──▶ termina rápido
                                                             y ROBA T2 de Worker-1
```

Puedes comprobar los hilos involucrados observando `Thread.currentThread().getName()`: verás nombres como `main` y `ForkJoinPool.commonPool-worker-1`, `worker-2`, etc.

---

## 12.3 ¿Cuándo conviene paralelizar? El modelo $N \times Q$ y la divisibilidad

Paralelizar **no es gratis**: dividir el `Spliterator`, agendar tareas en el `ForkJoinPool`, sincronizar cachés L1/L2 entre núcleos y fusionar resultados tiene un costo fijo (*overhead*).

### Regla empírica de Brian Goetz / Doug Lea ($N \times Q$)
* **$N$**: número de elementos en el Stream.
* **$Q$**: costo computacional por elemento (operaciones aritméticas, cifrado, cálculo geométrico, etc.).
* **Regla de oro**: Como punto de partida empírico, busca que **$N \times Q \ge 10{,}000$** antes de siquiera considerar `.parallel()`. Si $N = 100$ y solo sumas enteros ($Q \approx 1$), el Stream paralelo será significativamente **más lento** que el secuencial. **Mide siempre con JMH** (*Java Microbenchmark Harness*).

### Ranking de divisibilidad (*Splitability*) de las fuentes

El paralelismo solo escala si el `Spliterator` puede partir la estructura en pedazos balanceados rápidamente:

| Fuente de datos | Complejidad de `trySplit()` | Calidad para paralelo | Por qué |
| :--- | :--- | :--- | :--- |
| **`ArrayList`**, arreglos `T[]`/`int[]`, `IntStream.range` | **$O(1)$** | **Excelente** | Acceso aleatorio por índice; partir a la mitad es solo calcular `(inicio + fin) / 2`. |
| **`HashSet`**, **`TreeSet`**, **`ConcurrentHashMap`** | **$O(\log N)$** / cubetas | **Buena** | Se pueden dividir por ramas o cubetas, aunque con ligero desbalance ocasional. |
| **`LinkedList`**, **`Stream.iterate`**, **`BufferedReader.lines()`** | **$O(N)$** | **Pésima** | Estructura secuencial enlazada; el motor tiene que recorrer nodos uno por uno para partir lotes, anulando la ganancia. |

---

## 12.4 Los 5 peligros mortales de los Streams paralelos (y cómo corregirlos)

### Peligro 1: Estado mutable compartido (*Race Conditions*)

Nunca modifiques una colección externa no sincronizada (como un `ArrayList` o `HashMap`) desde un `.forEach()` paralelo:

```java
// ❌ INCORRECTO: Race condition catastrófica (elementos perdidos o ArrayIndexOutOfBoundsException)
List<String> unsafe = new ArrayList<>();
employees.parallelStream()
    .map(Employee::getFullName)
    .forEach(unsafe::add);

// ✅ CORRECTO (Opción 1): Usa collect() / toList() para reducción aislada por hilo
List<String> safe = employees.parallelStream()
    .map(Employee::getFullName)
    .toList();

// ✅ CORRECTO (Opción 2): Si requieres una estructura concurrente específica
ConcurrentSkipListSet<String> sortedConcurrent = employees.parallelStream()
    .map(Employee::getFullName)
    .collect(Collectors.toCollection(ConcurrentSkipListSet::new));
```

### Peligro 2: Operaciones no asociativas en `reduce()`

En un Stream paralelo, `reduce(identity, accumulator, combiner)` agrupa las operaciones en árboles arbitrarios. Si la operación no es **asociativa** (como la resta o la división), el resultado paralelo **diverge** del secuencial:

```java
List<Integer> nums = List.of(10, 2, 3, 1);

// Secuencial: (((0 - 10) - 2) - 3) - 1 = -16
int seq = nums.stream().reduce(0, (a, b) -> a - b);

// Paralelo: ((0 - 10) - (0 - 2)) - ((0 - 3) - (0 - 1)) = (-8) - (-2) = -6  (¡NO determinista!)
int par = nums.parallelStream().reduce(0, (a, b) -> a - b);
```

Además, la identidad debe cumplir `op(identity, x) == x`. Si pones `identity = 10` en una suma paralela, cada hilo sumará `10` a su partición y el total quedará inflado.

### Peligro 3: El costo oculto del orden (`findFirst`, `limit`, `forEachOrdered`)

Algunas operaciones obligan al motor paralelo a sincronizar resultados para respetar el **orden de encuentro**:
* **`forEachOrdered()`** imprime en el orden original pero sacrifica paralelismo al final; **`forEach()`** entrega máximo rendimiento sin garantía de orden.
* **`findFirst()`** debe esperar a las particiones anteriores para garantizar el primer elemento; **`findAny()`** retorna en cuanto **cualquier** hilo encuentra una coincidencia.
* **`limit(N)`** sobre un Stream paralelo ordenado es costoso. Si el orden no importa, libera la restricción explícitamente con **`.unordered()`**:

```java
List<Employee> anyThreeEngineers = employees.parallelStream()
    .unordered()
    .filter(e -> "Engineering".equals(e.getDepartment()))
    .limit(3)
    .toList();
```

### Peligro 4: *Auto-boxing* masivo y contención de `Random`

En cálculos paralelos numéricos:
1. Usa siempre Streams primitivos (`IntStream`, `LongStream`, `DoubleStream`) o `Arrays.stream(arr).parallel()` para evitar crear millones de objetos `Long`/`Double`.
2. **Nunca uses `Math.random()` ni una instancia compartida de `java.util.Random`** dentro de un Stream paralelo: internamente comparten una semilla `AtomicLong` con contención CAS brutal entre núcleos. Usa siempre **`ThreadLocalRandom.current()`**.

#### Ejemplo estrella: Aproximación paralela de $\pi$ por Monte Carlo

Lanzamos $N$ puntos aleatorios $(x, y)$ en el cuadrado unitario $[0, 1] \times [0, 1]$. La proporción de puntos que caen dentro del cuarto de círculo ($x^2 + y^2 \le 1$) aproxima $\pi / 4$:

```java
long samples = 10_000_000L;
long insideCircle = LongStream.range(0, samples)
    .parallel()
    .filter(_ -> {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double x = rnd.nextDouble();
        double y = rnd.nextDouble();
        return (x * x + y * y) <= 1.0;
    })
    .count();

double piEstimate = 4.0 * insideCircle / samples;
```

Este ejemplo tiene un $N \times Q$ enorme, fuente `LongStream.range` con división $O(1)$, cero *boxing*, cero estado compartido y generador aleatorio local por hilo. ¡Escala casi linealmente con los núcleos de tu CPU!

### Peligro 5: Inanición (*Starvation*) de `ForkJoinPool.commonPool()` con I/O

**Regla sagrada de arquitectura**: Como todos los Streams paralelos y `CompletableFuture.supplyAsync()` de tu aplicación comparten el mismo `ForkJoinPool.commonPool()`, si haces llamadas HTTP o de base de datos dentro de un `parallelStream()`, bloquearás los pocos hilos del pool global y congelarás **toda la JVM**.

#### Solución clásica (aislamiento con `ForkJoinPool` dedicado — `AutoCloseable` desde Java 19+)
Para aislar una tarea CPU-bound con un nivel de paralelismo controlado (por ejemplo, exactamente 2 hilos):

```java
try (ForkJoinPool customPool = new ForkJoinPool(2)) {
    double totalSalary = customPool.submit(() ->
        employees.parallelStream()
            .mapToDouble(Employee::getSalary)
            .sum()
    ).get();
}
```

#### Solución moderna en Java 25 LTS para tareas I/O-bound: Virtual Threads + `Gatherers.mapConcurrent`
En Java 25 **no uses `parallelStream()` para I/O**. Combina:
* **Virtual Threads** ([JEP 444](https://openjdk.org/jeps/444)) mejorados con **Synchronize Virtual Threads without Pinning** ([JEP 491](https://openjdk.org/jeps/491)), que eliminan el *pinning* en bloques `synchronized`.
* **`Gatherers.mapConcurrent(maxConcurrency, mapper)`** ([JEP 485](https://openjdk.org/jeps/485)), que despacha cada elemento en su propio hilo virtual con límite de concurrencia y preservando el orden de encuentro.
* **`ScopedValue`** ([JEP 506](https://openjdk.org/jeps/506), finalizado en Java 25), el reemplazo moderno, inmutable y seguro de `ThreadLocal` para propagar *trace IDs*, usuario autenticado o *tenant* hacia hilos virtuales e hijos sin fugas de memoria.

| Tipo de carga | Herramienta correcta en Java 25 LTS | Por qué |
| :--- | :--- | :--- |
| **CPU-bound** ($N \times Q \ge 10{,}000$, cálculos puros, Monte Carlo, ordenamiento) | `.parallelStream()` / `LongStream.range().parallel()` | Aprovecha los núcleos físicos sin crear hilos de sobra; mínimo *context switching*. |
| **I/O-bound** (HTTP, microservicios, DB, archivos remotos) | `.gather(Gatherers.mapConcurrent(max, fn))` + Virtual Threads | Nunca bloquea el `commonPool`; escala a miles de operaciones concurrentes con memoria mínima. |
| **Propagación de contexto** (Auth, Tracing) | `ScopedValue.where(KEY, val).run(...)` ([JEP 506](https://openjdk.org/jeps/506)) | Inmutable, acotado léxicamente y de costo $O(1)$ al heredar en hilos virtuales. |

---

## 12.5 Recolección concurrente: `groupingByConcurrent` y `collect` de 3 argumentos

Cuando agrupas millones de registros en paralelo, `Collectors.groupingBy()` crea un `HashMap` por hilo y luego fusiona todos los mapas en el `combiner` (lo cual puede ser costoso si hay muchas claves). Si el orden no importa, **`Collectors.groupingByConcurrent()`** usa un único `ConcurrentHashMap` compartido con bandera `CONCURRENT | UNORDERED`:

```java
ConcurrentMap<String, Long> countByDept = employees.parallelStream()
    .unordered()
    .collect(Collectors.groupingByConcurrent(
        Employee::getDepartment,
        Collectors.counting()
    ));
```

También puedes usar el `collect()` de 3 argumentos directamente sobre un `ConcurrentHashMap`:

```java
ConcurrentHashMap<String, Double> sumByDept = employees.parallelStream()
    .collect(
        ConcurrentHashMap::new,
        (map, e) -> map.merge(e.getDepartment(), e.getSalary(), Double::sum),
        (m1, m2) -> m2.forEach((k, v) -> m1.merge(k, v, Double::sum))
    );
```

---

## 12.6 Ingeniería de Rendimiento Real: Ley de Amdahl, *False Sharing*, JMH y `ManagedBlocker`

### 1. La Ley de Amdahl: Por qué 16 núcleos nunca dan 16× de velocidad
Si una fracción $P$ de tu programa es paralelizable y la fracción $(1 - P)$ es inevitablemente secuencial (por ejemplo, leer la fuente, construir el resultado final o sincronizar), la aceleración máxima teórica con $N$ núcleos es:

$$S(N) = \frac{1}{(1 - P) + \frac{P}{N}}$$

Si el 20% de tu pipeline es secuencial ($P = 0.80$), incluso con **infinitos núcleos** jamás superarás un *speedup* de $1 / 0.20 = 5\times$. Por eso eliminar barreras secuenciales (`sorted`, `limit` ordenado, `forEachOrdered`) suele dar más rendimiento que comprar más CPU.

### 2. *False Sharing* en líneas de caché de 64 bytes
Los procesadores modernos leen la RAM en bloques llamados **líneas de caché (*cache lines*) de 64 bytes**. Si dos hilos de un `parallelStream()` actualizan posiciones contiguas de un mismo arreglo (por ejemplo, `counters[0]++` en el Hilo 1 y `counters[1]++` en el Hilo 2), ambos elementos viven en la **misma línea de caché**. Cada escritura invalida la caché L1 del otro núcleo (*cache line bouncing*), haciendo el código paralelo más lento que el secuencial. **Solución:** cada hilo debe acumular en su propia instancia privada devuelta por `supplier()` en `collect()`, nunca en ranuras adyacentes compartidas.

### 3. Cómo medir correctamente con JMH (*Java Microbenchmark Harness*)
Jamás midas Streams paralelos con `System.nanoTime()` dentro de un `main()`: el compilador JIT (C2) no ha calentado el código, puede eliminar el resultado por *Dead Code Elimination* y el GC distorsiona una sola corrida. Usa siempre **JMH**:

```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class StreamParallelBenchmark {

    private long[] data;

    @Setup
    public void setup() {
        data = LongStream.rangeClosed(1, 1_000_000).toArray();
    }

    @Benchmark
    public void sequentialSum(Blackhole bh) {
        bh.consume(Arrays.stream(data).map(x -> x * x).sum());
    }

    @Benchmark
    public void parallelSum(Blackhole bh) {
        bh.consume(Arrays.stream(data).parallel().map(x -> x * x).sum());
    }
}
```

### 4. `ForkJoinPool.ManagedBlocker` vs. Virtual Threads en Java 25
Antes de Java 21, si un bloque dentro de `ForkJoinPool` tenía que bloquearse inevitablemente, la única vía segura para que el pool compensara creando un hilo extra era implementar `ForkJoinPool.ManagedBlocker`. En **Java 25 LTS**, ese patrón quedó relegado a librerías de bajo nivel: para cualquier operación I/O bloqueante, usa **`Gatherers.mapConcurrent(max, fn)` sobre Virtual Threads** ([JEP 444](https://openjdk.org/jeps/444), [JEP 485](https://openjdk.org/jeps/485), [JEP 491](https://openjdk.org/jeps/491)).

---

## 12.7 Retos de Dominio y Autoevaluación (Capítulo 12)

1. **Nivel Inicial:** Demuestra con una prueba unitaria cómo `(a, b) -> a - b` en `reduce()` produce resultados distintos en `.stream()` vs. `.parallelStream()` por violar la asociatividad.
2. **Nivel Intermedio:** Toma un pipeline con *race condition* sobre `ArrayList::add` en `parallelStream().forEach(...)` y corrígelo de dos maneras: con `.toList()` y con `Collectors.groupingByConcurrent()`.
3. **Nivel Avanzado:** Implementa una aproximación paralela de $\pi$ por Monte Carlo con 10 millones de muestras usando `LongStream.range().parallel()` y `ThreadLocalRandom.current()`, y explica por qué `Math.random()` destruiría el escalado.
4. **Nivel Staff MAANG:** Configura un microbenchmark **JMH** comparando `sequential` vs. `parallel` sobre `ArrayList` y `LinkedList`, y justifica con la Ley de Amdahl cuándo usar `Gatherers.mapConcurrent` sobre Virtual Threads en lugar de `parallelStream()`.
