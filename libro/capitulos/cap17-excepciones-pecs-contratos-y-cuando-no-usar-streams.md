# Capítulo 17: Excepciones Verificadas, Genéricos PECS, Contratos Formales y Cuándo NO Usar Streams

> *"Dominar una herramienta no consiste en usarla para todo; consiste en entender sus límites en el sistema de tipos, sus contratos formales y exactamente cuándo un bucle `for` tradicional es la decisión de ingeniería superior."* — Filosofía de diseño de Brian Goetz

Este capítulo cierra los cuatro muros técnicos con los que choca todo ingeniero al llevar Java Streams a sistemas críticos en producción:
1. Cómo dominar las ***Checked Exceptions*** (`IOException`, `SQLException`, `URISyntaxException`) sin código espagueti ni pérdida silenciosa de errores.
2. Cómo leer y diseñar firmas genéricas con **PECS (`? super T`, `? extends R`)** y resolver los 4 errores más famosos de inferencia de tipos (*Target Typing*) en `javac`.
3. Cómo respetar los **contratos formales del JDK** (No-Interferencia, Comportamiento Sin Estado y Asociatividad) para evitar bugs imposibles de reproducir.
4. La guía pragmática definitiva de **cuándo NO usar Streams** y cómo elegir entre `for`, `Stream`, `parallelStream()` y `Gatherers`.

---

## 17.1 El Muro de las *Checked Exceptions* en Streams (y las 4 Soluciones de Producción)

El primer gran choque al usar Streams con APIs clásicas de Java ocurre al intentar leer archivos, consultar JDBC o parsear URIs dentro de `map()`:

```java
// ❌ NO COMPILA: Files.readString(Path) declara 'throws IOException' (checked exception),
// pero Function<Path, String>.apply(Path) NO declara ninguna checked exception!
List<String> contents = paths.stream()
        .map(Files::readString) // Error: unhandled exception: java.io.IOException
        .toList();
```

### ¿Por qué el equipo del JDK NO hizo que `Function.apply()` lanzara `Exception`?
Porque si `Function<T, R>` declarara `throws Exception`, **absolutamente cualquier** `.map(x -> x + 1)` en todo el ecosistema Java te obligaría a envolver cada pipeline en `try { ... } catch (Exception e) { ... }`, destruyendo la composición funcional y la legibilidad.

### Estrategia 1: Envolver en `UncheckedIOException` (*Fail-Fast*)
Úsala cuando un error de lectura debe abortar todo el lote de inmediato:
```java
List<String> contents = paths.stream()
        .map(path -> {
            try {
                return Files.readString(path);
            } catch (IOException e) {
                throw new UncheckedIOException("Error leyendo " + path, e);
            }
        })
        .toList();
```

### Estrategia 2: Interfaz Funcional `ThrowingFunction<T, R, E>` reutilizable
Encapsula el `try/catch` una sola vez para toda tu base de código:
```java
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;

    static <T, R, E extends Exception> Function<T, R> unchecked(ThrowingFunction<T, R, E> fn) {
        Objects.requireNonNull(fn);
        return t -> {
            try {
                return fn.apply(t);
            } catch (Exception e) {
                if (e instanceof IOException ioe) throw new UncheckedIOException(ioe);
                if (e instanceof RuntimeException re) throw re;
                throw new RuntimeException(e);
            }
        };
    }
}

// Uso limpio en cualquier pipeline:
List<String> contents = paths.stream()
        .map(ThrowingFunction.unchecked(Files::readString))
        .toList();
```

### Estrategia 3 (Estándar MAANG Java 25): Mónada `sealed interface Result<T, E>` + Pattern Matching
En sistemas ETL o microservicios reales, **un archivo corrupto o un registro malformado NO debe tumbar el procesamiento de los otros 9,999 registros**. En Java 21–25 modelamos el resultado como un tipo algebraico sellado con métodos `map`, `flatMap` y `isSuccess`:

```java
public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    record Success<T, E>(T value) implements Result<T, E> {}
    record Failure<T, E>(E error) implements Result<T, E> {}

    static <T> Result<T, Exception> of(Callable<T> action) {
        try {
            return new Success<>(action.call());
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    default boolean isSuccess() {
        return this instanceof Success<T, E>;
    }

    default <R> Result<R, E> map(Function<? super T, ? extends R> mapper) {
        return switch (this) {
            case Success(var val) -> new Success<>(mapper.apply(val));
            case Failure(var err) -> new Failure<>(err);
        };
    }
}

// Caso de Estudio: procesar 10,000 archivos en UNA sola pasada separando éxitos y errores
Map<Boolean, List<Result<String, Exception>>> partitioned = paths.stream()
        .map(p -> Result.of(() -> Files.readString(p)))
        .collect(Collectors.partitioningBy(Result::isSuccess));

List<String> loadedDocs = partitioned.get(true).stream()
        .map(r -> ((Result.Success<String, Exception>) r).value())
        .toList();

List<Exception> errors = partitioned.get(false).stream()
        .map(r -> ((Result.Failure<String, Exception>) r).error())
        .toList();
```

### Estrategia 4: `mapMulti` con *Dead-Letter Queue* *thread-safe*
Si deseas emitir únicamente los éxitos al downstream y enviar cada fallo a una cola de errores sin crear objetos `Result`:
```java
// En paralelo usa ConcurrentLinkedQueue; en secuencial basta ArrayList
Queue<String> deadLetterErrors = new ConcurrentLinkedQueue<>();
List<String> loaded = paths.stream()
        .<String>mapMulti((path, downstream) -> {
            try {
                downstream.accept(Files.readString(path));
            } catch (IOException e) {
                deadLetterErrors.add(path + ": " + e.getMessage());
            }
        })
        .toList();
```

---

## 17.2 Genéricos, Comodines PECS (`? super T`, `? extends R`) y *Target Typing*

Cuando lees la especificación oficial de `Stream<T>`, encuentras firmas como:
```java
<R> Stream<R> map(Function<? super T, ? extends R> mapper)
```
Si eres principiante, `? super T` y `? extends R` parecen ruido críptico. En realidad siguen una regla sencilla inmortalizada por Joshua Bloch (*Effective Java*): **PECS — *Producer Extends, Consumer Super***.

```text
          Jerarquía de ejemplo:   Object  ◀──  Number  ◀──  Integer

  Stream<Integer>  ──▶  mapper: Function<? super Integer, ? extends Number>
                                            ▲                 ▲
               CONSUME un Integer ──────────┘                 └────────── PRODUCE un Number
               Acepta: Function<Object, ...>                              Acepta: devuelve Integer,
                       Function<Number, ...>                                      Double o Number
                       Function<Integer, ...>
```

* **`? super T` (Contravarianza en entrada):** Si el Stream entrega `Integer`, una función `Function<Object, String>` (`Object::toString`) sabe consumir cualquier `Object`, por lo tanto puede consumir `Integer` con total seguridad. Sin `? super T`, `Stream<Integer>.map(Object::toString)` no compilaría si la función estuviera tipada como `Function<Object, String>`.
* **`? extends R` (Covarianza en salida):** Si necesitas obtener un `Stream<Number>`, una función que produzca `Double` o `Integer` (subtipos de `Number`) es completamente válida.

### Los 4 Errores Más Famosos de Inferencia de Tipos en `javac` (y su Solución Exacta)

| Código que NO compila | Por qué falla el *Target Typing* de `javac` | Solución idiomática |
| :--- | :--- | :--- |
| `Stream.builder().add("A").build()` | Sin tipo objetivo inmediato, infiere `Stream<Object>`. | **`Stream.<String>builder()`** (*type witness*) |
| `orders.stream().mapMulti((o, c) -> c.accept(o.getId())).distinct().toList()` | `c` se infiere como `Consumer<Object>` al encadenar etapas posteriores. | **`orders.stream().<Integer>mapMulti(...)`** |
| `Comparator.comparingDouble(p -> p.getStock() / p.getPrice()).reversed()` | `.reversed()` corta la inferencia hacia atrás; `p` se infiere como `Object`. | **`Comparator.comparingDouble((Product p) -> ...).reversed()`** |
| `Map.Entry.comparingByValue().reversed()` | El comparador genérico pierde `K, V` al encadenar `.reversed()`. | **`Map.Entry.<String, Long>comparingByValue().reversed()`** |

---

## 17.3 Matriz Definitiva de Decisión: ¿`for`, `Stream`, `parallelStream()` o `Gatherers`?

Los Streams son extraordinarios para consultas declarativas, transformaciones de datos, agregaciones y paralelismo de datos. **Pero NO reemplazan todos los bucles `for`.** Un ingeniero Staff elige la herramienta exacta según el problema:

| Escenario | Herramienta Ganadora | Por qué |
| :--- | :--- | :--- |
| Consultas, filtros, proyecciones DTO, `GROUP BY`, reportes, ETL | **`Stream` secuencial** | Máxima legibilidad, *loop fusion*, inmutabilidad y cero boilerplate. |
| Lotes fijos, ventanas deslizantes, sumas corridas, RLE, EMA | **`Stream.gather(Gatherers.*)`** (Java 25) | Operaciones intermedias con estado, cortocircuito y composición `andThen`. |
| Llamadas HTTP, microservicios, DB con concurrencia acotada | **`Gatherers.mapConcurrent(max, fn)`** | Usa **Virtual Threads** ([JEP 444](https://openjdk.org/jeps/444)/[491](https://openjdk.org/jeps/491)) sin bloquear `ForkJoinPool.commonPool()`. |
| Cómputo CPU puro ($N \times Q \ge 10{,}000$, Monte Carlo, arreglos) | **`parallelStream()` / `LongStream.parallel()`** | Aprovecha núcleos físicos con división $O(1)$ y *work-stealing*. |
| Dos punteros, Programación Dinámica, BFS/DFS con `Queue` y `visited` | **Bucle `for` / `while` imperativo** | Requieren mutación coordinada de múltiples índices y tablas DP; forzar Streams daña claridad y complejidad. |
| Bucles ultra-calientes de nanosegundos (*HFT*, *parsers* de bytes) | **Bucle `for` sobre arreglos primitivos** | Cero asignaciones de `Spliterator`/`Sink` y auto-vectorización SIMD óptima en C2 JIT. |
| Salida temprana con múltiples efectos o `return` directo | **Bucle `for` clásico** | `if (cond) return x;` es inmediato y transparente para cualquier lector. |

> **Observación de Ingeniería de Software (La prueba de las 3 AM):**
> El objetivo de escribir Streams jamás es ganar un concurso de "una sola línea" (*code golf*). El objetivo es que cualquier compañero del equipo pueda leer tu código a las 3 de la mañana durante un incidente en producción y entender **en menos de 10 segundos** qué entra, qué se filtra, qué se transforma y qué sale.

---

## 17.4 Retos de Dominio y Autoevaluación (Capítulo 17)

1. **Nivel Inicial:** Implementa `ThrowingFunction.unchecked(...)` para leer una lista de `Path` con `Files::readString` envolviendo `IOException` en `UncheckedIOException`.
2. **Nivel Intermedio:** Usa la mónada `sealed interface Result<T, E>` para procesar un lote de archivos separando `Success` y `Failure` en una sola pasada con `Collectors.partitioningBy`.
3. **Nivel Avanzado:** Explica con la regla PECS por qué `Stream<Employee>.map(Function<Object, String>)` compila sin errores y corrige los 4 casos clásicos de fallo de inferencia de tipos con *type witnesses*.
4. **Nivel Staff MAANG:** Toma un algoritmo de Programación Dinámica (como *Coin Change*) y un pipeline de agregación de ventas, y redacta un RFC breve justificando por qué el primero debe permanecer imperativo y el segundo declarativo con Java 25 Streams.
