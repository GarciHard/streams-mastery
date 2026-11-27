# Capítulo 15: Desafíos de Entrevista MAANG (*The Interview Gauntlet*)

> *"En una entrevista de Meta, Google o Amazon, resolver un problema con 40 líneas de bucles `for` anidados y banderas mutables pasa la prueba mínima. Resolverlo con un pipeline declarativo limpio, analizando su complejidad Big-O y sabiendo cuándo un Stream es mejor (y cuándo un algoritmo imperativo gana en complejidad), te posiciona en nivel Senior/Staff."*

---

## 15.1 Mentalidad MAANG: Cómo evaluar un problema antes de escribir el Stream

Antes de teclear una sola línea en una entrevista técnica, hazte estas 3 preguntas en 30 segundos:

1. **¿Cuál es la estructura de salida y la clave canónica?**
   * *Frecuencias / Top-K* → `groupingBy(identity(), counting())` → `.entrySet().stream()` → `.sorted(...)` → `.limit(k)`.
   * *Anagramas* → clave canónica = caracteres ordenados (`Arrays.sort(chars)`).
   * *Co-ocurrencias / Pares únicos* → índices $i < j$ con `IntStream.range` + `flatMap`.
2. **¿Preserva el orden de encuentro?**
   * Para *primer carácter no repetido* o *Run-Length Encoding* por bloques, usa `LinkedHashMap::new` (o `Gatherer` en Java 25 para rachas consecutivas reales).
3. **¿Cuál es la complejidad temporal y espacial?**
   * Siempre verbaliza tu Big-O al entrevistador: por ejemplo, `groupingBy` + `sorted` + `limit(k)` es $O(N \log N)$, y menciona que si $K \ll N$ puedes bajarlo a $O(N \log K)$ usando el colector `PriorityQueue` acotado que construiste en el Capítulo 11.

---

## 15.2 Soluciones comentadas de los problemas estrella MAANG

### Problema 1: Top-K Palabras Más Frecuentes (LeetCode 692)
**Reto**: Dadas varias oraciones, tokeniza en minúsculas ignorando puntuación y devuelve las $K$ palabras más frecuentes ordenadas por frecuencia descendente y, en empate, alfabéticamente ascendente.

```java
List<String> top3 = sentences.stream()
    .flatMap(s -> Arrays.stream(s.toLowerCase(Locale.ROOT).split("[^a-z]+")))
    .filter(w -> !w.isBlank())
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
        .thenComparing(Map.Entry.comparingByKey()))
    .limit(3)
    .map(Map.Entry::getKey)
    .toList();
```
* **Complejidad**: Tiempo $O(W + U \log U)$ donde $W$ es total de palabras y $U$ palabras únicas; Espacio $O(U)$.

### Problema 2: Agrupar Anagramas (LeetCode 49) y Anagrama Válido (LeetCode 242)
Dos palabras son anagramas si y solo si tienen la misma firma de caracteres ordenados:

```java
Function<String, String> canonical = s -> {
    char[] arr = s.toCharArray();
    Arrays.sort(arr);
    return new String(arr);
};

Collection<List<String>> anagramGroups = words.stream()
    .collect(Collectors.groupingBy(canonical))
    .values();

// LeetCode 242 (Valid Anagram):
boolean isAnagram = canonical.apply(s).equals(canonical.apply(t));
```
* **Complejidad**: Tiempo $O(N \cdot L \log L)$ donde $L$ es la longitud máxima de palabra; Espacio $O(N \cdot L)$.

### Problema 3: Primer Carácter No Repetido y *Run-Length Encoding* (RLE)

#### Primer carácter único en `"swiss"` → `'w'`
Usamos `LinkedHashMap::new` para conservar el orden exacto de aparición:

```java
Optional<Character> firstUnique = input.chars()
    .mapToObj(c -> (char) c)
    .collect(Collectors.groupingBy(
        Function.identity(),
        LinkedHashMap::new,
        Collectors.counting()
    ))
    .entrySet().stream()
    .filter(e -> e.getValue() == 1L)
    .map(Map.Entry::getKey)
    .findFirst(); // Optional['w']
```

#### *Run-Length Encoding* (`"aaabbc"` → `"a3b2c1"`) — ¡Cuidado con la trampa de entrevistas!
Si un carácter solo aparece en un bloque contiguo (como `"aaabbc"`), `LinkedHashMap` funciona. **Pero si la entrada es `"aaabbcaaa"`**, `LinkedHashMap` fusionaría las dos rachas de `'a'` en `"a6b2c1"`, lo cual es incorrecto para RLE verdadero (`"a3b2c1a3"`). En **Java 25 LTS**, un `Gatherer` personalizado (Capítulo 16) resuelve RLE verdadero en una sola pasada $O(N)$ y $O(1)$ de espacio auxiliar.

### Problema 4: Álgebra de Matrices — Transposición (LeetCode 867) y Rotación 90° Horaria

Para una matriz de $R \times C$:
* **Transpuesta**: `transposed[c][r] = matrix[r][c]`.
* **Rotación 90° en sentido horario** (matriz cuadrada $N \times N$): el elemento en `(r, c)` de la matriz rotada proviene de `matrix[N - 1 - c][r]`:

```java
int n = matrix.length;
int[][] rotated = IntStream.range(0, n)
    .mapToObj(r -> IntStream.range(0, n)
        .map(c -> matrix[n - 1 - c][r])
        .toArray())
    .toArray(int[][]::new);
```
* **Complejidad**: Tiempo $O(N^2)$, Espacio $O(N^2)$ (inmutable, sin mutar la matriz original).

### Problema 5: Secuencia Consecutiva Más Larga (LeetCode 128) en $O(N)$

**Idea brillante de entrevista**: Si metes todos los números en un `Set<Integer>`, un número `x` es el **inicio** de una secuencia consecutiva únicamente si `x - 1` **no está** en el conjunto. Desde cada inicio, contamos cuántos consecutivos existen con `IntStream.iterate(x, set::contains, n -> n + 1).count()`:

```java
Set<Integer> numSet = Arrays.stream(nums).boxed().collect(Collectors.toSet());

int longest = numSet.stream()
    .filter(x -> !numSet.contains(x - 1)) // Solo inicios de racha -> garantiza O(N) total!
    .mapToInt(start -> (int) IntStream.iterate(start, numSet::contains, x -> x + 1).count())
    .max()
    .orElse(0);
```
* **Complejidad**: Tiempo **$O(N)$** amortizado (cada número se visita a lo sumo 2 veces), Espacio $O(N)$.

### Problema 6: *Product of Array Except Self* (LeetCode 238) sin división

Calcula productos prefijos y sufijos (o en versión declarativa directa para arreglos pequeños usando `IntStream.range` excluyendo el índice `i`):

```java
int[] result = IntStream.range(0, nums.length)
    .map(i -> IntStream.range(0, nums.length)
        .filter(j -> j != i)
        .map(j -> nums[j])
        .reduce(1, (a, b) -> a * b))
    .toArray();
```
> **Observación de Ingeniería de Software (Claridad vs. Escala)**
> La versión anterior de `IntStream` anidado es $O(N^2)$ y perfecta para expresar la especificación con máxima claridad. En una entrevista para $N = 10^5$, menciona al entrevistador que el algoritmo óptimo en $O(N)$ tiempo usa un *prefix scan* (en Java 25 puedes hacerlo con `Gatherers.scan`) y un *suffix scan* multiplicados elemento a elemento.

### Problema 7: Fusión de Intervalos Solapados (LeetCode 56)

Ordena los intervalos por `start` y reduce en una lista acumulada comprobando si el intervalo actual se solapa con el último intervalo fusionado (`getLast()` de Java 21+ / [JEP 431](https://openjdk.org/jeps/431)):

```java
record Interval(int start, int end) {}

List<Interval> merged = intervals.stream()
    .sorted(Comparator.comparingInt(Interval::start))
    .collect(
        ArrayList<Interval>::new,
        (acc, curr) -> {
            if (acc.isEmpty() || acc.getLast().end() < curr.start()) {
                acc.add(curr);
            } else {
                Interval last = acc.removeLast();
                acc.add(new Interval(last.start(), Math.max(last.end(), curr.end())));
            }
        },
        (left, right) -> {
            for (Interval iv : right) {
                if (left.isEmpty() || left.getLast().end() < iv.start()) left.add(iv);
                else {
                    Interval last = left.removeLast();
                    left.add(new Interval(last.start(), Math.max(last.end(), iv.end())));
                }
            }
        }
    );
```

### Problema 8: *Market Basket Analysis* (Pares de categorías co-compradas)

Para cada orden, extraemos sus categorías distintas ordenadas alfabéticamente y generamos todos los pares $i < j$ (`"CatA-CatB"`), luego contamos la frecuencia global y tomamos el máximo:

```java
String topPair = orders.stream()
    .flatMap(order -> {
        List<String> cats = order.getItems().stream()
            .map(OrderItem::getCategory)
            .distinct()
            .sorted()
            .toList();
        return IntStream.range(0, cats.size())
            .boxed()
            .flatMap(i -> IntStream.range(i + 1, cats.size())
                .mapToObj(j -> cats.get(i) + "-" + cats.get(j)));
    })
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .max(Map.Entry.comparingByValue())
    .map(Map.Entry::getKey)
    .orElse("");
```

### Problema 9: Validación de Ordenamiento Topológico en Grafos

Dada una secuencia propuesta `proposedSequence` y un grafo dirigido `A -> B`, la secuencia es topológicamente válida si y solo si, para toda arista `A -> B`, el índice de `A` es estrictamente menor que el índice de `B`:

```java
Map<String, Integer> pos = IntStream.range(0, proposedSequence.size())
    .boxed()
    .collect(Collectors.toMap(proposedSequence::get, Function.identity()));

boolean isValid = graph.entrySet().stream()
    .allMatch(e -> e.getValue().stream()
        .allMatch(dep -> pos.get(e.getKey()) < pos.get(dep)));
```

---

## 15.3 Retos de Dominio y Autoevaluación (Capítulo 15)

1. **Nivel Inicial:** Resuelve *Contains Duplicate* (LeetCode 217), *Valid Anagram* (LeetCode 242) y *First Non-Repeating Character* usando Streams idiomáticos con `LinkedHashMap`.
2. **Nivel Intermedio:** Implementa *Top K Frequent Words* (LeetCode 692) con desempate alfabético y *Longest Common Prefix* usando `Stream.reduce()`.
3. **Nivel Avanzado:** Resuelve *Rotate Matrix 90° Clockwise*, *Longest Consecutive Sequence* (LeetCode 128) en $O(N)$ con `IntStream.iterate(start, set::contains, x -> x + 1)` y *Market Basket Analysis* (pares de categorías co-compradas $i < j$).
4. **Nivel Staff MAANG:** Implementa *Merge Overlapping Intervals* (LeetCode 56) usando `getLast()`/`removeLast()` ([JEP 431](https://openjdk.org/jeps/431)) y un validador de ordenamiento topológico sobre un grafo dirigido de dependencias.
