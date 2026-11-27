# Capítulo 18: Solucionario Maestro Comentado y Caso de Estudio Capstone (Java 25 LTS)

> *"La teoría te da el mapa; resolver problemas reales y comparar tu solución contra una implementación de nivel Staff te da el instinto."*

Este capítulo cumple dos propósitos diseñados para consolidar tu maestría definitiva:
1. **Solucionario Maestro Comentado (Capítulos 1 al 17):** Soluciones completas, idiomáticas en Java 25 LTS y explicadas paso a paso para los retos clave de cada capítulo del libro.
2. **Caso de Estudio Capstone de Producción:** Un motor completo de **Ingesta, Detección de Fraude en Tiempo Real y Conciliación Financiera** escrito de principio a fin en Java 25 LTS integrando `Files.lines()`, `sealed interface Result<T, E>`, `Gatherers.windowSliding()`, `Gatherers.mapConcurrent()` sobre Virtual Threads, `ScopedValue`, `Collectors.teeing()` y `BigDecimal`.

---

## 18.1 Solucionario Maestro Comentado (Capítulos 1 a 17)

### Capítulo 1 — Currying y Motor de Reglas Declarativo
```java
// Función currificada reutilizable por divisa
Function<String, Function<Double, String>> currencyFormatter =
        prefix -> amount -> prefix + String.format(Locale.US, "$%,.2f", amount);

Function<Double, String> mxn = currencyFormatter.apply("MXN ");
Function<Double, String> usd = currencyFormatter.apply("USD ");

// Motor de reglas inmutable
record Rule<T>(Predicate<T> when, Consumer<T> then) {}
List<Rule<Employee>> rules = List.of(
        new Rule<>(e -> e.getSalary() > 300_000.0, e -> System.out.println("Bono: " + e.getFullName())),
        new Rule<>(e -> e.getYearsOfExperience() >= 15, e -> System.out.println("Veterano: " + e.getFullName()))
);
employees.forEach(e -> rules.stream().filter(r -> r.when().test(e)).forEach(r -> r.then().accept(e)));
```

### Capítulo 2 — Collatz con `Stream.iterate` (3 argumentos) y `Matcher.results()`
```java
// 1. Secuencia de Collatz completa desde 27 hasta 1 (0L actúa como centinela de parada)
List<Long> collatz27 = Stream.iterate(
        27L,
        n -> n > 0L,
        n -> (n == 1L) ? 0L : (n % 2L == 0L ? n / 2L : 3L * n + 1L)
).toList();

// 2. Extraer fechas YYYY-MM-DD con Matcher.results() (Java 9+)
Pattern isoDate = Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b");
List<LocalDate> dates = isoDate.matcher(rawText).results()
        .map(m -> LocalDate.parse(m.group(1)))
        .toList();
```

### Capítulo 3 — Paginación segura, Primos y SLO con `Gatherers.windowSliding(3)`
```java
// 1. Paginación genérica segura
static <T> List<T> paginate(List<T> items, int pageIndex, int pageSize) {
    if (pageIndex < 0 || pageSize <= 0) return List.of();
    return items.stream().skip((long) pageIndex * pageSize).limit(pageSize).toList();
}

// 2. Primos menores a 10,000 sin boxing
IntPredicate isPrime = n -> n >= 2 &&
        IntStream.rangeClosed(2, (int) Math.sqrt(n)).noneMatch(d -> n % d == 0);
int[] primes = IntStream.range(2, 10_000).filter(isPrime).toArray();

// 3. Violación de SLO: 3 lecturas consecutivas > 100 ms
boolean sloViolated = latencies.stream()
        .gather(Gatherers.windowSliding(3))
        .anyMatch(w -> w.stream().allMatch(ms -> ms > 100));
```

### Capítulo 4 — Parseo *Zero-Allocation* con `mapMultiToInt`
```java
int positiveSum = rawLines.stream()
        .mapMultiToInt((line, out) -> {
            for (String token : line.split(";")) {
                try {
                    int n = Integer.parseInt(token.trim());
                    if (n > 0) out.accept(n);
                } catch (NumberFormatException _) { /* descarta corruptos sin asignar Optional */ }
            }
        })
        .sum();
```

### Capítulo 5 — Ordenamiento Null-Safe en Dos Niveles + `reversed().stream()`
```java
Comparator<Employee> safeByCityDescSalary = Comparator.nullsLast(
        Comparator.comparing(Employee::getCity, Comparator.nullsLast(Comparator.naturalOrder()))
                  .thenComparing(Comparator.comparingDouble(Employee::getSalary).reversed())
);
List<Employee> sorted = nullableEmployees.stream().sorted(safeByCityDescSalary).toList();

// Recorrido inverso O(1) sobre colección ya ordenada (JEP 431)
List<Employee> reversed = sorted.reversed().stream().toList();
```

### Capítulo 6 — Promedio exacto `BigDecimal` con `RoundingMode.HALF_EVEN`
```java
BigDecimal exactAvg = transactions.stream()
        .map(tx -> BigDecimal.valueOf(tx.getAmount()))
        .collect(Collectors.teeing(
                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add),
                Collectors.counting(),
                (sum, count) -> count == 0
                        ? BigDecimal.ZERO
                        : sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_EVEN)
        ));
```

### Capítulo 7 — Mapa Seguro ante Valores `null` + `SalarySpread` en Una Pasada
```java
// 1. Permite valores null (evita el NPE interno de Map.merge usado por Collectors.toMap)
Map<Integer, String> idToNullableCity = employees.stream()
        .collect(HashMap::new, (m, e) -> m.put(e.getId(), e.getCity()), HashMap::putAll);

// 2. Min, Max y Spread en 1 pasada
record Spread(double min, double max, double diff) {}
Spread s = employees.stream().collect(Collectors.teeing(
        Collectors.minBy(Comparator.comparingDouble(Employee::getSalary)),
        Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary)),
        (lo, hi) -> {
            double min = lo.map(Employee::getSalary).orElse(0.0);
            double max = hi.map(Employee::getSalary).orElse(0.0);
            return new Spread(min, max, max - min);
        }
));
```

### Capítulo 8 — `Collectors.filtering` + `HAVING` + Matriz Pivote 2D
```java
// Preserva departamentos con 0 coincidencias:
Map<String, Long> highEarners = employees.stream()
        .collect(Collectors.groupingBy(
                Employee::getDepartment,
                Collectors.filtering(e -> e.getSalary() > 200_000.0, Collectors.counting())
        ));

// Departamentos con nómina total > $1,000,000 (SQL HAVING):
List<String> millionDollarDepts = employees.stream()
        .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.summingDouble(Employee::getSalary)))
        .entrySet().stream()
        .filter(e -> e.getValue() > 1_000_000.0)
        .map(Map.Entry::getKey)
        .toList();
```

### Capítulo 9 — Fila $N$ del Triángulo de Pascal y Cadena `Optional.or`
```java
static int[] pascalRow(int n) {
    IntUnaryOperator fact = x -> IntStream.rangeClosed(1, x).reduce(1, (a, b) -> a * b);
    return IntStream.rangeClosed(0, n)
            .map(k -> fact.applyAsInt(n) / (fact.applyAsInt(k) * fact.applyAsInt(n - k)))
            .toArray();
}

Optional<String> config = l1Cache.or(() -> l2Cache).or(() -> dbLookup);
```

### Capítulo 10 — `zipLazy` con `Spliterators` y Hash Join ($O(N + M)$)
```java
// Hash Join O(N + M) con LEFT OUTER JOIN (conserva órdenes externas)
Map<String, Employee> idx = employees.stream()
        .collect(Collectors.toMap(Employee::getFullName, Function.identity(), (a, _) -> a));

record OrderView(int id, String customer, String dept, double total) {}
List<OrderView> leftJoin = orders.stream()
        .map(o -> {
            Employee emp = idx.get(o.getCustomerName());
            return new OrderView(o.getId(), o.getCustomerName(),
                    emp != null ? emp.getDepartment() : "EXTERNAL", o.getTotalPrice());
        })
        .toList();
```

### Capítulo 11 — Colector `topK` en $O(N \log K)$ con `PriorityQueue`
```java
static <T> Collector<T, ?, List<T>> topK(int k, Comparator<? super T> cmp) {
    return Collector.of(
            () -> new PriorityQueue<T>(k + 1, cmp),
            (pq, elem) -> { pq.offer(elem); if (pq.size() > k) pq.poll(); },
            (a, b) -> { b.forEach(e -> { a.offer(e); if (a.size() > k) a.poll(); }); return a; },
            pq -> {
                List<T> out = new ArrayList<>(pq);
                out.sort(cmp.reversed());
                return List.copyOf(out);
            },
            Collector.Characteristics.UNORDERED
    );
}
```

### Capítulo 12 — Monte Carlo $\pi$ Paralelo sin Contención
```java
long n = 10_000_000L;
long hits = LongStream.range(0, n)
        .parallel()
        .filter(_ -> {
            var rnd = ThreadLocalRandom.current();
            double x = rnd.nextDouble(), y = rnd.nextDouble();
            return x * x + y * y <= 1.0;
        })
        .count();
double pi = 4.0 * hits / n;
```

### Capítulos 13–17 — Sondas, Intervalos Solapados, Gatherer `takeLastN` y `Result<T, E>`
```java
// 1. Gatherer personalizado takeLastN(int n) — vacía el ArrayDeque acotado en finisher
static <T> Gatherer<T, ?, T> takeLastN(int n) {
    return Gatherer.ofSequential(
            () -> new ArrayDeque<T>(n),
            Gatherer.Integrator.ofGreedy((dq, elem, _) -> {
                if (n > 0) {
                    if (dq.size() == n) dq.removeFirst();
                    dq.addLast(elem);
                }
                return true;
            }),
            (dq, downstream) -> {
                while (!dq.isEmpty() && !downstream.isRejecting()) {
                    downstream.push(dq.removeFirst());
                }
            }
    );
}

// 2. Longest Common Prefix usando Stream.reduce()
String lcp = words.stream()
        .reduce((a, b) -> {
            int i = 0, limit = Math.min(a.length(), b.length());
            while (i < limit && a.charAt(i) == b.charAt(i)) i++;
            return a.substring(0, i);
        })
        .orElse("");
```

---

## 18.2 Caso de Estudio Capstone: Motor de Fraude y Conciliación Bancaria en Java 25 LTS

Para cerrar el tratado, ensamblaremos **en un solo sistema cohesivo** las mejores piezas de Java 25 LTS que aprendiste a lo largo de los 17 capítulos:

* **Lectura perezosa segura**: `try (Stream<String> lines = Files.lines(path))` (Capítulo 2).
* **Tolerancia a fallos sin abortar el lote**: `sealed interface Result<T, E>` (Capítulo 17).
* **Detección de ráfagas de fraude en tiempo real**: `Gatherers.windowSliding(2)` (Capítulo 16).
* **Enriquecimiento I/O concurrente**: `Gatherers.mapConcurrent(16, fn)` sobre Virtual Threads ([JEP 485](https://openjdk.org/jeps/485) / [JEP 491](https://openjdk.org/jeps/491)) + `ScopedValue` ([JEP 506](https://openjdk.org/jeps/506)) (Capítulo 12).
* **Conciliación exacta en 1 pasada**: `BigDecimal` + `Collectors.teeing()` (Capítulos 6 y 7).

```java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

public class Java25FraudAndLedgerEngine {

    // Contexto inmutable propagado automáticamente a Virtual Threads (JEP 506 - Java 25)
    private static final ScopedValue<String> TRACE_ID = ScopedValue.newInstance();

    public record RawEvent(String accountId, String type, BigDecimal amount, Instant timestamp) {}
    public record FraudAlert(String traceId, String accountId, BigDecimal combinedAmount, String riskScore) {}
    public record BatchReport(long validCount, long errorCount, BigDecimal netBalance, List<FraudAlert> alerts) {}

    public sealed interface Result<T, E> permits Result.Success, Result.Failure {
        record Success<T, E>(T value) implements Result<T, E> {}
        record Failure<T, E>(E error) implements Result<T, E> {}
        static <T> Result<T, String> tryParse(Callable<T> fn) {
            try { return new Success<>(fn.call()); }
            catch (Exception e) { return new Failure<>(e.getMessage()); }
        }
    }

    public static BatchReport processCsvFile(Path csvPath, String traceId) throws Exception {
        // Bind léxico de ScopedValue para todo el pipeline (incluyendo Virtual Threads de mapConcurrent)
        return ScopedValue.where(TRACE_ID, traceId).call(() -> {
            List<Result<RawEvent, String>> parsed;
            try (Stream<String> lines = Files.lines(csvPath)) {
                parsed = lines.skip(1) // saltar encabezado CSV
                        .filter(line -> !line.isBlank())
                        .map(Java25FraudAndLedgerEngine::parseLine)
                        .toList();
            }

            // 1. Separar registros válidos y corruptos en una sola pasada
            Map<Boolean, List<Result<RawEvent, String>>> split = parsed.stream()
                    .collect(Collectors.partitioningBy(r -> r instanceof Result.Success));

            List<RawEvent> validEvents = split.get(true).stream()
                    .map(r -> ((Result.Success<RawEvent, String>) r).value())
                    .toList();
            long errorCount = split.get(false).size();

            // 2. Conciliación financiera exacta con BigDecimal en una sola pasada
            BigDecimal netBalance = validEvents.stream()
                    .map(e -> "CREDIT".equals(e.type()) ? e.amount() : e.amount().negate())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 3. Detección de fraude en tiempo real (2 débitos consecutivos >= $10,000 en la misma cuenta)
            //    + enriquecimiento I/O concurrente con Virtual Threads (max 16 en vuelo).
            //    NOTA CRÍTICA JDK 25: ScopedValue (JEP 506) se hereda automáticamente solo en StructuredTaskScope,
            //    NO en los Virtual Threads no estructurados que crea internamente Gatherers.mapConcurrent.
            //    Por eso capturamos 'currentTrace' y lo re-vinculamos dentro del hilo virtual:
            String currentTrace = TRACE_ID.get();
            BigDecimal threshold = new BigDecimal("10000.00");
            List<FraudAlert> alerts = validEvents.stream()
                    .filter(e -> "DEBIT".equals(e.type()))
                    .gather(Gatherers.windowSliding(2))
                    .filter(w -> w.getFirst().accountId().equals(w.getLast().accountId()))
                    .filter(w -> w.getFirst().amount().compareTo(threshold) >= 0
                              && w.getLast().amount().compareTo(threshold) >= 0)
                    .gather(Gatherers.mapConcurrent(16, window ->
                            ScopedValue.where(TRACE_ID, currentTrace).call(() -> {
                                String account = window.getFirst().accountId();
                                BigDecimal sum = window.getFirst().amount().add(window.getLast().amount());
                                String risk = queryExternalRiskService(account); // lee TRACE_ID.get() en Virtual Thread
                                return new FraudAlert(TRACE_ID.get(), account, sum, risk);
                            })
                    ))
                    .toList();

            return new BatchReport(validEvents.size(), errorCount, netBalance, alerts);
        });
    }

    private static Result<RawEvent, String> parseLine(String line) {
        return Result.tryParse(() -> {
            String[] cols = line.split(",");
            return new RawEvent(
                    cols[0].trim(),
                    cols[1].trim().toUpperCase(Locale.ROOT),
                    new BigDecimal(cols[2].trim()),
                    Instant.parse(cols[3].trim())
            );
        });
    }

    private static String queryExternalRiskService(String accountId) {
        // Simulación de llamada HTTP bloqueante segura en Virtual Thread
        return "HIGH_RISK_" + accountId;
    }
}
```

### Por qué este Caso de Estudio representa el pináculo de Java 25 LTS
* **Cero fugas de descriptores:** `try-with-resources` cierra el archivo automáticamente.
* **Cero pérdida silenciosa de datos:** cada línea malformada queda contabilizada sin abortar el lote.
* **Cero errores de centavos:** aritmética 100% `BigDecimal` con `compareTo`.
* **Cero aritmética frágil de índices:** `Gatherers.windowSliding(2)` + `getFirst()`/`getLast()` ([JEP 431](https://openjdk.org/jeps/431) + [JEP 485](https://openjdk.org/jeps/485)).
* **Cero bloqueo de `ForkJoinPool.commonPool()`:** `Gatherers.mapConcurrent(16, ...)` corre en Virtual Threads y hereda `TRACE_ID` en $O(1)$ vía `ScopedValue` ([JEP 506](https://openjdk.org/jeps/506)).

---

## Epílogo: Tu Camino Como Arquitecto de Java Streams

Llegaste aquí habiendo recorrido cada capa de la pila: desde el bytecode `invokedynamic` y las 4 familias raíz de `java.util.function`, pasando por la física de la caché L1, la suma de Kahan, `Spliterator`, `Collector<T, A, R>`, `ForkJoinPool`, JMH, `Gatherer<T, A, R>`, PECS y `Result<T, E>`, hasta diseñar motores financieros concurrentes en Java 25 LTS.

Recuerda siempre los tres mandamientos de los creadores de Java:
1. **Claridad primero:** escribe pipelines que tu equipo entienda en 10 segundos a las 3 AM.
2. **Mide antes de paralelizar:** respeta el modelo $N \times Q$, la divisibilidad del `Spliterator` y mide siempre con JMH.
3. **Usa la herramienta correcta:** `Stream` para transformar y agregar, `Gatherers` para flujo intermedio con estado o I/O concurrente, y un bucle `for` limpio cuando el algoritmo realmente lo pida.
