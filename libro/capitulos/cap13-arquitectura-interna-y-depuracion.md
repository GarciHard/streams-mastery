# Parte IV: Maestría Interna, Producción y Entrevistas MAANG

# Capítulo 13: Arquitectura Interna de Streams, *Loop Fusion* y Depuración en Java 25

> *"La diferencia entre un programador que usa Streams y un ingeniero Staff que los domina es simple: el primero ve una cadena de métodos; el segundo ve una lista doblemente enlazada de `AbstractPipeline`, banderas de bits de `Spliterator`, una cascada de `Sink` en memoria L1 y el grafo de perfiles en Java Flight Recorder."*

---

## 13.1 Desmitificando cómo se ejecuta un Stream desde cero

Cuando alguien aprende Streams por primera vez, suele imaginar un modelo **horizontal (por pasadas completas)** —también llamado *breadth-first*— como si cada operación creara una lista temporal en memoria:

```text
❌ MODELO MENTAL INCORRECTO (Horizontal / Pasadas separadas):
   [A, B, C] ──filter()──▶ [Lista temp 1] ──map()──▶ [Lista temp 2] ──limit(2)──▶ [Resultado]
```

**Esto es completamente falso.** Si Java funcionara así, procesar un archivo de 10 millones de líneas crearía varias copias gigantescas en el *heap* y agotaría la memoria de inmediato.

### La realidad: *Depth-First Loop Fusion* (Fusión de Bucles en Profundidad)

El motor de Streams de Java compila todas las operaciones intermedias sin estado (`filter`, `map`, `peek`) en **un único recorrido vertical (elemento por elemento)**:

```text
✅ MODELO REAL DE LA JVM (Vertical / Depth-First Loop Fusion):
   Elemento "A" ──▶ filter("A") ──▶ map("A") ──▶ Sink final (toList)
   Elemento "B" ──▶ filter("B") ──▶ map("B") ──▶ Sink final (toList)
   (Si limit(2) ya se cumplió, "C" JAMÁS se toca)
```

Comprobémoslo con una traza real de ejecución:

```java
List<String> trace = new ArrayList<>();

List<String> output = Stream.of("A", "B")
    .filter(s -> { trace.add("filter:" + s); return true; })
    .map(s -> { trace.add("map:" + s); return s + "!"; })
    .toList();

// Traza real: ["filter:A", "map:A", "filter:B", "map:B"]
// ¡NUNCA ["filter:A", "filter:B", "map:A", "map:B"]!
```

**Por qué esto importa en rendimiento**:
1. **Cero colecciones intermedias** en el *heap* para operaciones sin estado.
2. **Localidad de caché L1/L2**: El objeto `"A"` permanece caliente en los registros del CPU mientras atraviesa `filter`, `map` y llega al `Collector`.
3. **Cortocircuito real**: Si encadenas `.filter(...).map(...).limit(2)` sobre 100 millones de elementos, en cuanto pasan 2 elementos válidos la tubería se detiene al instante.

---

## 13.2 Anatomía interna: `ReferencePipeline`, `Spliterator` y la cadena de `Sink`

¿Qué ocurre exactamente en memoria cuando construyes un pipeline?

1. **Fase de construcción (100% perezosa — *Lazy*)**:
   Cada operación intermedia crea un nodo `ReferencePipeline$StatelessOp` (como `filter` o `map`) o `ReferencePipeline$StatefulOp` (como `sorted` o `distinct`). Estos nodos forman una **lista doblemente enlazada** que apunta hacia atrás hasta el `Spliterator` fuente. **Si no llamas a una operación terminal, no se ejecuta absolutamente nada** (ni siquiera un `peek()`).

2. **Fase de ejecución (al invocar la operación terminal)**:
   El motor recorre la lista **de atrás hacia adelante** (desde la operación terminal hasta la fuente) envolviendo cada etapa en una interfaz interna llamada **`Sink<T>`** con 4 métodos:
   * `begin(long size)`: avisa cuántos elementos vienen (`-1` si el tamaño es desconocido). Así `toList()` puede pre-reservar el tamaño exacto de su `ArrayList` sin redimensionar.
   * `accept(T value)`: procesa un elemento y, si pasa, llama a `downstream.accept(...)`.
   * `cancellationRequested()`: devuelve `true` cuando un cortocircuito (`limit`, `findFirst`, `anyMatch`, `noneMatch`) ya terminó y le dice al bucle padre que aborte.
   * `end()`: vacía buffers pendientes (crucial para `sorted()` o `Gatherer.finisher()`).

### Operadores que rompen la pereza: *Stateless* vs *Stateful*

* **Sin estado (*Stateless*)** (`filter`, `map`, `flatMap`, `mapMulti`, `peek`): procesan cada elemento de forma independiente. Mantienen $O(1)$ de memoria y preservan el cortocircuito.
* **Con estado acotado** (`limit`, `skip`, `takeWhile`, `dropWhile`): llevan solo un contador o booleano.
* **Con estado no acotado (*Full Barrier*)** (`sorted`, `distinct`):
  * **`sorted()`** es una **barrera total**: no puede entregar ni siquiera el primer elemento (`limit(1)`) sin haber visto, almacenado en un buffer interno y ordenado **todos** los elementos del Stream.
  * **Consecuencia Big-O**: `nums.stream().sorted().limit(1).findFirst()` cuesta **$O(N \log N)$ en tiempo y $O(N)$ en memoria**, mientras que `nums.stream().min(Comparator.naturalOrder())` cuesta **$O(N)$ en tiempo y $O(1)$ en memoria**.

---

## 13.3 Propagación de banderas (*Characteristics*) en `Spliterator`

Cada `Spliterator` transporta una máscara de bits con metadatos que el motor de Streams propaga (o borra) en cada etapa para activar optimizaciones automáticas:

| Bandera `Spliterator` | Qué significa | Ejemplo de fuente que la tiene | Qué operación la **borra** |
| :--- | :--- | :--- | :--- |
| **`SIZED`** | Se conoce el tamaño exacto de antemano. | `List.of(...)`, arreglos, `IntStream.range` | **`filter()`**, `flatMap()`, `distinct()` (ahora no se sabe cuántos sobrevivirán). |
| **`ORDERED`** | Tiene un orden de encuentro definido. | `List`, `LinkedHashSet`, `TreeSet` | `.unordered()` |
| **`DISTINCT`** | Todos los elementos son únicos. | `Set.of(...)`, `HashSet`, `TreeSet` | `map()` (dos entradas distintas pueden mapear al mismo valor). |
| **`SORTED`** | Ya está ordenado según un `Comparator`. | `TreeSet`, `ConcurrentSkipListSet` | `map()`, `flatMap()` |
| **`IMMUTABLE`** / **`NONNULL`** | La fuente no cambiará ni contiene `null`. | `List.of(...)`, `Set.of(...)` | Varía según la operación. |

> **Tip de Rendimiento JVM / MAANG (Optimización invisible de `count()` desde Java 9+)**
> Si ejecutas `List.of("A", "B", "C").stream().peek(System.out::println).count()`, **`peek()` NO SE EJECUTA** en Java 9–25. ¿Por qué? Porque `List` tiene la bandera `SIZED`, `peek()` no altera el tamaño y `count()` sabe el resultado en **$O(1)$** sin iterar un solo elemento. **Regla de oro de producción**: jamás pongas lógica de negocio ni efectos secundarios dentro de `peek()`; úsalo únicamente para diagnóstico.

### Construyendo un `Spliterator` personalizado con `Spliterators`

Cuando necesitas envolver una fuente de datos que no es una `Collection` (un rango personalizado, un cursor de base de datos o un paginador de API), usas `Spliterators.spliterator(...)` junto con `StreamSupport.stream(spliterator, false)`:

```java
// Ejemplo: Stream personalizado del rango 10..20 usando Iterator + Spliterators
Iterator<Integer> rangeIterator = new Iterator<>() {
    int current = 10;
    @Override public boolean hasNext() { return current <= 20; }
    @Override public Integer next() { return current++; }
};

Spliterator<Integer> spliterator = Spliterators.spliterator(
    rangeIterator,
    11, // tamaño exacto conocido
    Spliterator.ORDERED | Spliterator.SIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE
);

List<Integer> values = StreamSupport.stream(spliterator, false).toList(); // [10..20]
```

---

## 13.4 Ciclo de vida, `onClose()` y transparencia de excepciones

### 1. Gestión de recursos con `onClose()` y `try-with-resources`
Los Streams implementan `AutoCloseable`. Para fuentes ligadas a recursos del sistema operativo (`Files.lines()`, `BufferedReader.lines()`, cursores JDBC), debes envolver siempre el Stream en un bloque `try-with-resources`. Puedes registrar múltiples *hooks* de limpieza con `.onClose(Runnable)`:

```java
List<String> closeOrder = new ArrayList<>();
try (Stream<String> s = Stream.of("res1", "res2")
        .onClose(() -> closeOrder.add("first"))
        .onClose(() -> closeOrder.add("second"))) {
    s.forEach(_ -> {});
}
// Los manejadores onClose() se ejecutan en orden FIFO: ["first", "second"]
// Incluso si el primero lanza una excepción, el segundo se ejecuta y la excepción se agrega como suppressed.
```

### 2. Transparencia de excepciones (*Fail-Fast*)
Si una lambda lanza una excepción no verificada (`ArithmeticException`, `NullPointerException`, `IllegalArgumentException`) durante el recorrido:
* La excepción se propaga **de inmediato** hacia el llamador sin quedar atrapada.
* Gracias a la evaluación en profundidad (*depth-first*), **ningún elemento posterior llega a evaluarse**: en `Stream.of(1, 2, 0, 4, 5).map(n -> 100 / n).toList()`, solo se evalúan `1`, `2` y `0` antes de lanzar `ArithmeticException`; `4` y `5` jamás se tocan.

---

## 13.5 Depuración profesional y perfilado en Java 25 LTS

1. **Sondas de diagnóstico (*Pipeline Probes*) con `peek()`**:
   Inserta `peek()` antes y después de cada `filter()` con un `Map<String, AtomicInteger>` para medir exactamente en qué etapa se pierde tráfico (*drop-off rate*):
   ```java
   Map<String, AtomicInteger> counters = Map.of(
       "source", new AtomicInteger(), "filtered", new AtomicInteger(), "mapped", new AtomicInteger()
   );
   List<Integer> out = IntStream.rangeClosed(1, 10).boxed()
       .peek(_ -> counters.get("source").incrementAndGet())
       .filter(n -> n % 2 == 0)
       .peek(_ -> counters.get("filtered").incrementAndGet())
       .map(n -> n * 10)
       .peek(_ -> counters.get("mapped").incrementAndGet())
       .toList();
   ```
2. **Stream Trace en IntelliJ IDEA**: Al detenerte en un *breakpoint* sobre un Stream, el botón *Trace Current Stream Chain* visualiza gráficamente qué elemento entra y sale de cada operación.
3. **Java Flight Recorder (JFR) en Java 25 LTS**:
   Java 25 ([JDK 25](https://openjdk.org/projects/jdk/25/)) elevó la observabilidad de producción a nivel quirúrgico con tres JEPs dedicados a JFR:
   * **JFR Cooperative Sampling** ([JEP 518](https://openjdk.org/jeps/518)): muestreo de pilas de ejecución sin sesgo de *safepoint*, crucial para perfilar *hot loops* de Streams sin distorsionar el resultado.
   * **JFR CPU-Time Profiling** ([JEP 509](https://openjdk.org/jeps/509), experimental en Linux): mide tiempo real de CPU por método para distinguir si un Stream es lento por cómputo o por bloqueo.
   * **JFR Method Timing & Tracing** ([JEP 520](https://openjdk.org/jeps/520)): permite rastrear y cronometrar métodos específicos (como tu colector o mapper) directamente desde JFR sin modificar código fuente ni insertar `System.nanoTime()`.

---

## 13.6 Contratos Formales del Motor: No-Interferencia y Comportamiento Sin Estado

La especificación oficial de `java.util.stream` exige dos reglas estrictas para garantizar resultados deterministas:

### 1. No-Interferencia (*Non-Interference*)
Mientras el pipeline se está ejecutando, ninguna lambda debe **modificar la colección fuente** (a menos que sea una fuente concurrente diseñada para ello, como `ConcurrentHashMap`):

```java
// ❌ ROTO: modificar la lista fuente durante el recorrido lanza ConcurrentModificationException
List<String> list = new ArrayList<>(List.of("one", "two", "three"));
list.stream().peek(s -> {
    if ("two".equals(s)) list.add("four"); // ¡Interferencia ilegal!
}).toList();
```

### 2. Comportamiento Sin Estado (*Stateless Behaviors*)
Las lambdas pasadas a `filter`, `map` o `flatMap` no deben depender de ningún estado que pueda cambiar durante la ejecución del pipeline:

```java
// ❌ ROTO en paralelo: el resultado depende del orden arbitrario en que los hilos acceden a 'seen'
Set<Integer> seen = Collections.synchronizedSet(new HashSet<>());
List<Integer> broken = List.of(1, 2, 1, 3, 2).parallelStream()
        .map(x -> seen.add(x) ? x : 0) // Comportamiento CON estado -> no determinista en paralelo!
        .toList();
```
Si necesitas estado intermedio de forma segura, encapsúlalo en un **`Collector`** (Capítulo 11) o un **`Gatherer`** de Java 25 (Capítulo 16).

---

## 13.7 Retos de Dominio y Autoevaluación (Capítulo 13)

1. **Nivel Inicial:** Demuestra con `AtomicInteger` dentro de `peek()` que un Stream sin operación terminal ejecuta `0` iteraciones y que `limit(3)` detiene el recorrido exactamente en el tercer elemento válido.
2. **Nivel Intermedio:** Construye una prueba que demuestre cómo `sorted().limit(1)` evalúa el 100% de la entrada (barrera *stateful*), y comprueba con `.spliterator().hasCharacteristics(...)` cómo `filter()` elimina la bandera `SIZED`.
3. **Nivel Avanzado:** Implementa una sonda de telemetría con `Map<String, AtomicInteger>` para medir tasas de descarte por etapa (*drop-off rate*) y verifica que múltiples `.onClose()` en `try-with-resources` se ejecutan en orden FIFO.
4. **Nivel Staff MAANG:** Explica por qué `List.of(1, 2, 3).stream().peek(System.out::println).count()` no imprime nada en Java 9–25 y cómo perfilar un pipeline en producción con JFR Method Timing & Tracing ([JEP 520](https://openjdk.org/jeps/520)).
