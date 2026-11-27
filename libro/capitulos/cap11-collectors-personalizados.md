# Capítulo 11: Collectors Personalizados (`Collector<T, A, R>`)

> *"Cuando los recolectores estándar de `java.util.stream.Collectors` ya no alcanzan para expresar la estructura de datos o la estadística exacta que necesita tu sistema, diseñar un `Collector<T, A, R>` a la medida te convierte de usuario del API de Streams en arquitecto del ecosistema."*

---

## 11.1 Introducción desde cero: ¿Por qué crear un `Collector` personalizado?

En los Capítulos 7 y 8 dominaste los recolectores incluidos en la clase `Collectors`: `toList()`, `toMap()`, `joining()`, `groupingBy()`, `partitioningBy()`, `summarizingDouble()` y `teeing()`. Con ellos puedes resolver alrededor del 90% de los problemas del día a día.

Pero en entrevistas de nivel **Senior / Staff en empresas MAANG** —y en sistemas reales de analítica, finanzas o procesamiento de eventos— inevitablemente aparece ese 10% restante:

* ¿Cómo calculas la **mediana**, la **varianza** o la **desviación estándar** en una sola pasada de un Stream, de forma reutilizable y segura en paralelo?
* ¿Cómo obtienes el **Top-K** de empleados mejor pagados usando un *min-heap* acotado en $O(N \log K)$ en lugar de ordenar toda la lista en $O(N \log N)$?
* ¿Cómo agrupas elementos en **lotes (*chunks*) de tamaño fijo** o en **cubetas de histograma** con etiquetas personalizadas?
* ¿Cómo **agrupas por departamento y asignas un ranking interno** a cada empleado en un solo paso terminal?

Si no conoces la interfaz `Collector<T, A, R>`, caerías en la tentación de romper el flujo funcional: guardar resultados en listas temporales mutables, usar bucles `for` externos o hacer múltiples recorridos innecesarios. En este capítulo aprenderás desde cero cómo funciona exactamente la maquinaria interna de `collect()` y cómo construir tus propios recolectores con rigor de ingeniería.

---

## 11.2 Anatomía de `Collector<T, A, R>`: Los 3 parámetros genéricos

Toda la magia de la reducción mutable vive en la interfaz [`java.util.stream.Collector<T, A, R>`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/stream/Collector.html). Antes de ver sus métodos, entendamos qué significa cada una de sus tres letras genéricas:

| Parámetro | Significado | Analogía de fábrica | Ejemplo (`Collectors.joining()`) |
| :--- | :--- | :--- | :--- |
| **`T`** | **Tipo de entrada**: cada elemento que llega del Stream. | Materia prima que entra por la banda transportadora. | `CharSequence` (por ejemplo, `String`) |
| **`A`** | **Tipo del acumulador**: el contenedor mutable interno donde se van guardando los resultados parciales. | Cajas temporales de trabajo en cada estación de la fábrica. | `StringBuilder` (o `StringJoiner`) |
| **`R`** | **Tipo de resultado final**: el valor que recibe el llamador al terminar `collect()`. | Producto terminado, sellado y listo para envío. | `String` |

> **Observación de Ingeniería de Software (Ocultamiento del acumulador `A`)**
> El acumulador `A` es un detalle interno de implementación. Por eso, en las firmas públicas de los métodos de fábrica casi siempre verás un comodín `?`, por ejemplo: `Collector<Employee, ?, Double>`. Quien usa tu colector solo necesita saber qué entra (`Employee`) y qué sale (`Double`), nunca qué estructura mutable usaste por dentro.

---

## 11.3 Los 5 pilares de todo `Collector`

Un `Collector<T, A, R>` no ejecuta el bucle por sí mismo; es una **receta de 5 ingredientes** que le entregas al motor de Streams para que ejecute el ciclo de reducción tanto en modo secuencial como en modo paralelo:

```text
           [1] supplier()              [2] accumulator()
Stream<T> ───────────────▶  A (caja 1) ◀── acc(A, t1), acc(A, t2)
                            A (caja 2) ◀── acc(A, t3), acc(A, t4)
                                │
                                ▼  [3] combiner() (solo en paralelo)
                            A (caja fusionada = comb(caja1, caja2))
                                │
                                ▼  [4] finisher() (omitido si IDENTITY_FINISH)
                            R (resultado final)
```

Analicemos cada uno de los 5 métodos con precisión absoluta:

### 1. `Supplier<A> supplier()` — Crear una caja vacía nueva
Devuelve una función sin argumentos que instancia un acumulador vacío.
* **Regla crítica**: Debe crear una instancia **nueva e independiente** en cada llamada. En un Stream paralelo, cada hilo invoca `supplier()` para trabajar en su propia caja sin competir con otros hilos.
* Ejemplo: `ArrayList::new`, `StringBuilder::new`, `() -> new long[1]`.

### 2. `BiConsumer<A, T> accumulator()` — Meter un elemento en la caja
Recibe el acumulador `A` y un elemento `T` del Stream, y muta `A` para incorporar `T`.
* No devuelve nada (`void`), porque opera por mutación controlada sobre el acumulador privado del hilo.
* Ejemplo: `List::add`, `StringBuilder::append`, `(box, _) -> box[0]++`.

### 3. `BinaryOperator<A> combiner()` — Fusionar dos cajas parciales
Recibe dos acumuladores `a1` y `a2` (producidos por dos hilos distintos en un Stream paralelo) y devuelve un único acumulador que contiene la información de ambos.
* **Regla matemática (Asociatividad)**: `combiner(a1, combiner(a2, a3))` debe producir un resultado equivalente a `combiner(combiner(a1, a2), a3)`.
* Puedes mutar `a1` agregándole el contenido de `a2` y retornar `a1` (ahorrando memoria).
* **Nota**: En un Stream secuencial, `combiner()` no suele ejecutarse, **pero debes implementarlo correctamente siempre** para que tu colector no explote cuando alguien escriba `.parallelStream().collect(tuColector)`.

### 4. `Function<A, R> finisher()` — Sellar y entregar el resultado
Transforma el acumulador interno `A` en el resultado público `R` al finalizar toda la acumulación y combinación.
* Por ejemplo: convertir un `StringBuilder` a `String` (`StringBuilder::toString`), envolver una `List` con `List.copyOf(list)`, o calcular `sum / count` a partir de un arreglo `double[2]`.
* Si `A` ya es del mismo tipo que `R` y no requiere transformación, el finisher es simplemente `Function.identity()`.

### 5. `Set<Characteristics> characteristics()` — Pistas de optimización para la JVM
Devuelve un conjunto de banderas del `enum Collector.Characteristics` que le indican al motor de Streams qué atajos puede tomar:

| Característica | Qué le promete tu colector al motor de Streams | Peligro si mientes al declararla |
| :--- | :--- | :--- |
| **`IDENTITY_FINISH`** | El `finisher` es la función identidad (`A` ya es `R`); el motor puede omitir llamar a `finisher()` y hacer un *cast* directo `(R) accumulator`. | `ClassCastException` en tiempo de ejecución si `A` y `R` son tipos incompatibles, o pérdida de lógica si tu `finisher()` ordenaba o congelaba la colección. |
| **`UNORDERED`** | El resultado no depende del orden de encuentro de los elementos (como un `Set`, un conteo o una suma). | Resultados inconsistentes si el llamador esperaba preservar el orden de inserción. |
| **`CONCURRENT`** | El acumulador `A` es *thread-safe* (ej. `ConcurrentHashMap`) y múltiples hilos pueden llamar a `accumulator()` **simultáneamente sobre la misma instancia de `A`** sin necesidad de `combiner()`. | **Catástrofe de concurrencia** (*race conditions*, elementos perdidos, `ArrayIndexOutOfBoundsException`) si lo declaras sobre `ArrayList`, `HashMap` o `StringBuilder`. Además, solo surte efecto si también es `UNORDERED` o el Stream fuente no tiene orden. |

> **Error Común de Programación (`IDENTITY_FINISH` implícito en `Collector.of` de 4 argumentos)**
> `Collector.of(supplier, accumulator, combiner, characteristics...)` —la sobrecarga **sin** `finisher`— agrega **automáticamente** `Characteristics.IDENTITY_FINISH`. Si necesitas un paso final (como `List.copyOf`, ordenar o convertir a `String`), usa siempre la sobrecarga de **5 argumentos** que incluye `finisher` y **no** agregues `IDENTITY_FINISH`.

---

## 11.4 Formas de construir un `Collector` en Java 25

Tienes dos formas oficiales de crear un colector:

1. **Fábrica estática `Collector.of(...)`** (recomendada para el 95% de los casos por su concisión):
   * **Sobrecarga 1 (`A == R`)**:
     ```java
     Collector.of(supplier, accumulator, combiner, characteristics...)
     ```
   * **Sobrecarga 2 (`A != R` o con transformación final)**:
     ```java
     Collector.of(supplier, accumulator, combiner, finisher, characteristics...)
     ```
2. **Implementar la interfaz `Collector<T, A, R>`** en una clase o `record` dedicado: útil cuando el colector necesita estado de configuración (como un tamaño de lote `chunkSize` o un `Comparator`), documentación Javadoc propia o múltiples métodos auxiliares.

---

## 11.5 Recetario progresivo: De cero a patrones MAANG

A continuación construiremos, paso a paso, los patrones de colectores personalizados más importantes en producción y entrevistas técnicas, explicando el porqué de cada decisión de diseño.

### Patrón 1: Colector CSV y delimitadores personalizados (`StringBuilder`)

Supón que quieres unir los nombres de los empleados con `", "` o formatearlos con prefijo y sufijo `[Ana, Luis, Sofía]` sin usar `Collectors.joining()`:

```java
// Colector CSV personalizado con StringBuilder
Collector<String, StringBuilder, String> csvCollector = Collector.of(
    StringBuilder::new,
    (sb, s) -> {
        if (!sb.isEmpty()) sb.append(",");
        sb.append(s);
    },
    (sb1, sb2) -> {
        if (sb1.isEmpty()) return sb2;
        if (sb2.isEmpty()) return sb1;
        return sb1.append(",").append(sb2);
    },
    StringBuilder::toString
);

// Variante con prefijo "[" y sufijo "]"
Collector<String, StringJoiner, String> bracketCollector = Collector.of(
    () -> new StringJoiner(", ", "[", "]"),
    StringJoiner::add,
    StringJoiner::merge,
    StringJoiner::toString
);
```

Fíjate en el detalle fino del `combiner` con `StringBuilder`: si el hilo izquierdo procesó una partición vacía (`sb1.isEmpty()`), debemos devolver `sb2` directamente para no inyectar una coma huérfana al principio (`",Ana,Luis"`). `StringJoiner.merge()` maneja este caso borde automáticamente.

### Patrón 2: Cajas mutables de primitivos (`long[1]`, `int[1]`, `double[2]`)

En el Capítulo 1 aprendiste que una lambda no puede reasignar variables locales (`effectively final`). En un colector personalizado, los arreglos de tamaño fijo son la técnica estándar de alto rendimiento en la JVM para evitar el *auto-boxing* de `Long` o `Double`:

```java
// Conteo personalizado con long[1] (sin boxing por cada elemento)
Collector<Object, long[], Long> customCounting = Collector.of(
    () -> new long[1],
    (box, _) -> box[0]++,                  // Java 22-25: variable sin nombre '_' (JEP 456)
    (b1, b2) -> { b1[0] += b2[0]; return b1; },
    box -> box[0],
    Collector.Characteristics.UNORDERED
);

// Promedio acumulado (running average) con double[2]: [0] = suma, [1] = conteo
Collector<Double, double[], Double> runningAverage = Collector.of(
    () -> new double[2],
    (acc, val) -> { acc[0] += val; acc[1] += 1; },
    (a, b) -> { a[0] += b[0]; a[1] += b[1]; return a; },
    acc -> acc[1] == 0 ? 0.0 : acc[0] / acc[1],
    Collector.Characteristics.UNORDERED
);
```

> **Tip de Rendimiento JVM / MAANG**
> Usar `double[2]` en lugar de crear un objeto `Double` por cada elemento elimina millones de asignaciones efímeras en el *Eden Space* del recolector de basura. Además, observa cómo el `combiner` simplemente suma las sumas parciales y los conteos parciales: **nunca promedies dos promedios** (`(avg1 + avg2) / 2` es matemáticamente incorrecto si las particiones tienen tamaños distintos).

### Patrón 3: Colector de Máximo/Mínimo seguro ante Streams vacíos (`Optional<T>`)

Un Stream vacío no tiene elemento máximo. Para manejarlo con elegancia, el acumulador guarda una caja de un elemento `T[]` (o una clase interna `Box<T>`) y el `finisher` lo envuelve en `Optional.ofNullable(...)`:

```java
public static <T> Collector<T, ?, Optional<T>> customMaxBy(Comparator<? super T> cmp) {
    class Box {
        T value;
        boolean present;
        void accept(T candidate) {
            if (!present || cmp.compare(candidate, value) > 0) {
                value = candidate;
                present = true;
            }
        }
        Box combine(Box other) {
            if (other.present) accept(other.value);
            return this;
        }
    }
    return Collector.of(
        Box::new,
        Box::accept,
        Box::combine,
        box -> box.present ? Optional.ofNullable(box.value) : Optional.empty(),
        Collector.Characteristics.UNORDERED
    );
}
```

### Patrón 4: Colector de Mediana (ordenamiento diferido en el `finisher`)

La mediana es un clásico de entrevistas porque **no se puede calcular incrementalmente** en $O(1)$ de memoria; necesitas todos los valores ordenados. El acumulador recolecta en un `ArrayList<Double>` y el `finisher` ordena y calcula el punto medio:

```java
Collector<Double, List<Double>, Double> medianCollector = Collector.of(
    ArrayList::new,
    List::add,
    (l1, l2) -> { l1.addAll(l2); return l1; },
    list -> {
        if (list.isEmpty()) return 0.0;
        Collections.sort(list);
        int n = list.size();
        return (n % 2 == 1)
            ? list.get(n / 2)
            : (list.get(n / 2 - 1) + list.get(n / 2)) / 2.0;
    },
    Collector.Characteristics.UNORDERED
);
```

### Patrón 5: Varianza y Desviación Estándar en una sola pasada

Para calcular la varianza poblacional $\sigma^2 = \frac{1}{N}\sum x_i^2 - \mu^2$ (o muestral con $N-1$) de forma paralela, podemos almacenar tres acumulados en `double[3]`: `[0] = n`, `[1] = suma`, `[2] = sumaDeCuadrados`:

```java
// Varianza poblacional en una sola pasada (combinable en paralelo)
Collector<Double, double[], Double> populationVariance = Collector.of(
    () -> new double[3], // [count, sum, sumOfSquares]
    (a, x) -> { a[0]++; a[1] += x; a[2] += x * x; },
    (a, b) -> { a[0] += b[0]; a[1] += b[1]; a[2] += b[2]; return a; },
    a -> {
        if (a[0] == 0) return 0.0;
        double mean = a[1] / a[0];
        return Math.max(0.0, (a[2] / a[0]) - (mean * mean));
    },
    Collector.Characteristics.UNORDERED
);
```

> **Nota numérica MAANG**: Para valores gigantescos con varianza muy pequeña (como marcas de tiempo en milisegundos), la fórmula $\frac{\sum x^2}{N} - \mu^2$ puede sufrir cancelación catastrófica en punto flotante. En sistemas científicos o financieros de alta precisión, se utiliza el **algoritmo paralelo de Chan (generalización de Welford)** manteniendo `(n, mean, M2)` y combinando con $\delta = \mu_B - \mu_A$.

### Patrón 6: Top-K eficiente con *Min-Heap* acotado (`PriorityQueue`)

Si tienes 10 millones de empleados y quieres los **5 salarios más altos**, ordenar todo cuesta $O(N \log N)$ en tiempo y $O(N)$ en memoria. Con un `PriorityQueue` acotado a tamaño $K=5$ ordenado de menor a mayor (el menor de los top-5 siempre está en la cima para ser expulsado fácilmente), bajas el costo a **$O(N \log K)$ en tiempo y $O(K)$ en memoria**:

```java
public static <T> Collector<T, ?, List<T>> topK(int k, Comparator<? super T> comparator) {
    return Collector.of(
        () -> new PriorityQueue<T>(k + 1, comparator), // min-heap: el más débil en peek()
        (pq, elem) -> {
            pq.offer(elem);
            if (pq.size() > k) pq.poll(); // expulsa al menor cuando excede K
        },
        (pq1, pq2) -> {
            for (T elem : pq2) {
                pq1.offer(elem);
                if (pq1.size() > k) pq1.poll();
            }
            return pq1;
        },
        pq -> {
            List<T> result = new ArrayList<>(pq);
            result.sort(comparator.reversed()); // entregar de mayor a menor
            return List.copyOf(result);
        },
        Collector.Characteristics.UNORDERED
    );
}
```

### Patrón 7: Agrupación en lotes (*Chunking*) de tamaño fijo

Un problema clásico de procesamiento por lotes: dividir una secuencia de 30 empleados en lotes de tamaño 7 (produciendo 4 lotes de 7 y 1 lote final de 2):

```java
public static <T> Collector<T, ?, List<List<T>>> chunked(int size) {
    if (size <= 0) throw new IllegalArgumentException("El tamaño del lote debe ser > 0");
    return Collector.of(
        ArrayList<List<T>>::new,
        (batches, item) -> {
            if (batches.isEmpty() || batches.getLast().size() == size) { // getLast() de Java 21+ (JEP 431)
                batches.add(new ArrayList<>(size));
            }
            batches.getLast().add(item);
        },
        (left, right) -> {
            // Para mantener lotes exactos en paralelo, re-empaquetamos los elementos de right
            for (List<T> batch : right) {
                for (T item : batch) {
                    if (left.isEmpty() || left.getLast().size() == size) {
                        left.add(new ArrayList<>(size));
                    }
                    left.getLast().add(item);
                }
            }
            return left;
        },
        List::copyOf
    );
}
```

> **Conexión con Java 25 (JEP 485)**: En Java 25, cuando quieras dividir en lotes de tamaño fijo **como operación intermedia** sin materializar todo en memoria de golpe, ahora existe la operación estándar `stream.gather(Gatherers.windowFixed(7))` (que dominarás en el Capítulo 16). ¡Conocer ambas técnicas demuestra dominio tanto clásico como moderno!

### Patrón 8: Agrupar y asignar ranking interno (`Group-and-Rank`)

En reportes analíticos (similar a `RANK()` o `ROW_NUMBER() OVER (PARTITION BY dept ORDER BY salary DESC)` en SQL), puedes combinar `groupingBy` o un `Collector` personalizado con un `record` inmutable:

```java
record RankedEmp(String name, double salary, int rank) {}

Collector<Employee, Map<String, List<Employee>>, Map<String, List<RankedEmp>>> groupAndRankByDept =
    Collector.of(
        HashMap::new,
        (map, e) -> map.computeIfAbsent(e.getDepartment(), _ -> new ArrayList<>()).add(e),
        (m1, m2) -> {
            m2.forEach((dept, list) ->
                m1.computeIfAbsent(dept, _ -> new ArrayList<>()).addAll(list));
            return m1;
        },
        map -> {
            Map<String, List<RankedEmp>> ranked = new HashMap<>();
            map.forEach((dept, list) -> {
                list.sort(Comparator.comparingDouble(Employee::getSalary).reversed());
                List<RankedEmp> deptRanks = new ArrayList<>(list.size());
                for (int i = 0; i < list.size(); i++) {
                    Employee e = list.get(i);
                    deptRanks.add(new RankedEmp(e.getFullName(), e.getSalary(), i + 1));
                }
                ranked.put(dept, List.copyOf(deptRanks));
            });
            return Map.copyOf(ranked);
        },
        Collector.Characteristics.UNORDERED
    );
```

### Patrón 9: Construyendo `Collectors.teeing()` desde cero

¿Alguna vez te preguntaste cómo implementa el JDK `Collectors.teeing(c1, c2, merger)`? Es un `Collector` envolvente que mantiene **ambos acumuladores simultáneamente** en una pareja auxiliar:

```java
public static <T, A1, A2, R1, R2, R> Collector<T, ?, R> customTeeing(
        Collector<? super T, A1, R1> c1,
        Collector<? super T, A2, R2> c2,
        BiFunction<? super R1, ? super R2, R> merger) {

    class PairBox {
        A1 left = c1.supplier().get();
        A2 right = c2.supplier().get();

        void add(T t) {
            c1.accumulator().accept(left, t);
            c2.accumulator().accept(right, t);
        }

        PairBox combine(PairBox other) {
            left = c1.combiner().apply(left, other.left);
            right = c2.combiner().apply(right, other.right);
            return this;
        }

        R finish() {
            return merger.apply(c1.finisher().apply(left), c2.finisher().apply(right));
        }
    }

    return Collector.of(PairBox::new, PairBox::add, PairBox::combine, PairBox::finish);
}
```

Con esta sola función puedes calcular `Map.entry(min, max)` en una sola pasada del Stream, replicando por completo el mecanismo interno de `Collectors.teeing()`.

---

## 11.6 Retos de Dominio y Autoevaluación (Capítulo 11)

1. **Nivel Inicial:** Implementa un `Collector<String, StringBuilder, String>` estilo CSV con delimitador configurable y verifica que el `combiner` maneje particiones vacías sin insertar comas huérfanas.
2. **Nivel Intermedio:** Construye un colector de histograma con cubetas (`"0-49"`, `"50-99"`, `"100+"`) garantizando que las tres claves existan en el mapa incluso si alguna recibe `0` elementos.
3. **Nivel Avanzado:** Implementa un colector paralelizable para varianza y desviación estándar en una sola pasada usando `double[3]` (o el algoritmo de Chan/Welford) y compáralo con el colector de mediana.
4. **Nivel Staff MAANG:** Diseña un colector `topK(int k, Comparator<T>)` basado en `PriorityQueue` acotado en $O(N \log K)$ y un colector `groupAndRank` que emita `record RankedEmp(String name, double salary, int rank)`.
