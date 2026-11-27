# Capítulo 10: `flatMap` Avanzado, Combinatoria, Joins Relacionales, Árboles Recursivos y Zipping Perezoso

> *"En teoría de categorías, `flatMap` es la operación monádica `bind`: el ladrillo universal que permite expresar productos cartesianos, joins relacionales (Inner, Left Outer y Group Join), índices invertidos de motores de búsqueda, recorridos de árboles/grafos y fusión de Streams sin un solo bucle anidado."*

En el Capítulo 4 aprendiste las bases de `flatMap`. Aquí llevaremos la composición de Streams a nivel **Senior / Staff MAANG**, cubriendo todos los patrones que separan a un desarrollador promedio de un ingeniero capaz de modelar motores de consulta y grafos en memoria.

---

## 10.1 Zipping de Secuencias: Listas ($O(N)$) y Zipping Perezoso de Streams Arbitrarios

### 1. Zipping de dos `List` con `IntStream.range()`
Cuando tus dos fuentes ya son `List` de acceso aleatorio (`ArrayList`, `List.of`), emparejarlas por índice es limpio, $O(N)$ y sin estado externo:

```java
List<String> keys    = List.of("One", "Two", "Three");
List<Integer> values = List.of(1, 2, 3);

// Zip a lista formateada
List<String> ranked = IntStream.range(0, Math.min(keys.size(), values.size()))
        .mapToObj(i -> keys.get(i) + " = " + values.get(i))
        .toList();

// Zip directo a Map<String, Integer>
Map<String, Integer> zippedMap = IntStream.range(0, Math.min(keys.size(), values.size()))
        .boxed()
        .collect(Collectors.toMap(keys::get, values::get));
```

### 2. Zipping Perezoso Real de Dos `Stream` Arbitrarios (Incluso Infinitos) con `Spliterators`
¿Qué pasa si tus fuentes **no son `List`** (por ejemplo, dos `Files.lines()` o un `Stream.iterate` infinito emparejado con un flujo de eventos)? No puedes usar `.get(i)`. La solución profesional en Java 25 usa **`Iterator` + `Spliterators.spliteratorUnknownSize` + `onClose()`** para cerrar ambos streams padres correctamente:

```java
public static <A, B, R> Stream<R> zipLazy(
        Stream<? extends A> first,
        Stream<? extends B> second,
        BiFunction<? super A, ? super B, ? extends R> zipper) {

    Iterator<? extends A> itA = first.iterator();
    Iterator<? extends B> itB = second.iterator();

    Iterator<R> combined = new Iterator<>() {
        @Override public boolean hasNext() { return itA.hasNext() && itB.hasNext(); }
        @Override public R next() { return zipper.apply(itA.next(), itB.next()); }
    };

    Spliterator<R> spliterator = Spliterators.spliteratorUnknownSize(combined, Spliterator.ORDERED);
    return StreamSupport.stream(spliterator, false)
            .onClose(first::close)
            .onClose(second::close);
}

// Uso: numerar un stream infinito sin materializar en memoria
try (Stream<String> numbered = zipLazy(
        Stream.iterate(1, n -> n + 1),
        Stream.of("Alpha", "Beta", "Gamma"),
        (idx, name) -> "#" + idx + ": " + name)) {
    List<String> result = numbered.toList(); // ["#1: Alpha", "#2: Beta", "#3: Gamma"]
}
```

---

## 10.2 Productos Cartesianos, Combinaciones y Co-ocurrencia ($i < j$)

### 1. Producto Cartesiano de 2 Vías ($A \times B$) con exclusión (`a != b`)
```java
List<String> langs = List.of("Java", "Go", "Rust");
List<String> pairs = langs.stream()
        .flatMap(l1 -> langs.stream()
                .filter(l2 -> !l1.equals(l2))
                .map(l2 -> l1 + "-" + l2))
        .toList(); // 3 * 2 = 6 pares ordenados
```

### 2. Producto Cartesiano de 3 Vías ($A \times B \times C$ — Generador de SKUs)
```java
List<String> skus = List.of("Red", "Blue").stream()
        .flatMap(c -> List.of("S", "M").stream()
                .flatMap(s -> List.of("Men", "Women").stream()
                        .map(g -> c + " " + s + " " + g)))
        .toList(); // 2 * 2 * 2 = 8 combinaciones
```

### 3. Pares Únicos de Co-ocurrencia ($i < j$) sin duplicar `(A, B)` y `(B, A)`
```java
String[] words = "java streams rock".split(" ");
List<String> uniquePairs = IntStream.range(0, words.length)
        .boxed()
        .flatMap(i -> IntStream.range(i + 1, words.length)
                .mapToObj(j -> words[i] + "-" + words[j]))
        .toList(); // ["java-streams", "java-rock", "streams-rock"]
```

---

## 10.3 Joins Relacionales en Memoria en $O(N + M)$: Inner, Left Outer y Group Join

**Anti-patrón clásico ($O(N \times M)$):** Anidar `employees.stream().filter(...)` dentro de `orders.stream().flatMap(...)` recorre toda la lista interna por cada orden. Con 100,000 órdenes y 100,000 empleados son $10^{10}$ operaciones.

**Patrón Hash Join ($O(N + M)$):** Indexa primero la tabla secundaria en un `Map` ($O(M)$) y luego cruza en $O(1)$ por cada registro principal ($O(N)$).

### 1. INNER JOIN (`Orders` ⋈ `Employees`)
Descarta órdenes cuyo cliente no existe en `empByName` gracias a `Stream.ofNullable(...)`:
```java
Map<String, Employee> empByName = employees.stream()
        .collect(Collectors.toMap(Employee::getFullName, Function.identity(), (first, _) -> first));

List<String> innerJoin = orders.stream()
        .flatMap(order -> Stream.ofNullable(empByName.get(order.getCustomerName()))
                .map(emp -> String.format(Locale.US, "%s (%s) placed Order #%d ($%,.2f)",
                        emp.getFullName(), emp.getDepartment(), order.getId(), order.getTotalPrice())))
        .toList();
```

### 2. LEFT OUTER JOIN (`Orders` ⟕ `Employees`)
Conserva **todas** las órdenes, asignando un valor de respaldo (`"EXTERNAL"`) cuando el cliente no es empleado:
```java
record EnrichedOrder(int orderId, String customer, String department, double total) {}

List<EnrichedOrder> leftJoin = orders.stream()
        .map(order -> {
            Employee emp = empByName.get(order.getCustomerName());
            String dept = (emp != null) ? emp.getDepartment() : "EXTERNAL";
            return new EnrichedOrder(order.getId(), order.getCustomerName(), dept, order.getTotalPrice());
        })
        .toList();
```

### 3. GROUP JOIN (1 a Muchos: `Employee` → todas sus `Order`s)
```java
Map<String, List<Order>> ordersByCustomer = orders.stream()
        .collect(Collectors.groupingBy(Order::getCustomerName));

Map<String, Double> spendByEmployee = employees.stream()
        .collect(Collectors.toMap(
                Employee::getFullName,
                emp -> ordersByCustomer.getOrDefault(emp.getFullName(), List.of()).stream()
                        .mapToDouble(Order::getTotalPrice).sum(),
                Double::sum
        ));
```

---

## 10.4 Índice Invertido (Arquitectura de Motores de Búsqueda) — Con Prueba de Escritorio

Un **índice invertido** (el corazón de Lucene, Elasticsearch y Google Search) mapea cada término normalizado al conjunto de IDs de documentos donde aparece:

| Doc ID | Texto original | Pares `(término, docId)` emitidos por `flatMap` |
| :---: | :--- | :--- |
| `0` | `"Java Streams rock"` | `("java", 0)`, `("streams", 0)`, `("rock", 0)` |
| `1` | `"Java 25 Gatherers"` | `("java", 1)`, `("25", 1)`, `("gatherers", 1)` |
| **Índice final** | `groupingBy(key, mapping(val, toSet()))` | `{"java": [0, 1], "streams": [0], "rock": [0], "25": [1], "gatherers": [1]}` |

```java
Map<String, Set<Integer>> invertedIndex = IntStream.range(0, sentences.size())
        .boxed()
        .flatMap(idx -> Pattern.compile("[^a-zA-Z0-9]+")
                .splitAsStream(sentences.get(idx).toLowerCase(Locale.ROOT))
                .filter(Predicate.not(String::isBlank))
                .map(word -> Map.entry(word, idx)))
        .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
        ));
```

---

## 10.5 Aplanamiento Recursivo de Árboles (Organigramas / Menús / JSON)

Uno de los patrones más elegantes de entrevista MAANG es aplanar un árbol de profundidad arbitraria (`OrgNode` → subordinados → subordinados...):

```java
record OrgNode(String name, List<OrgNode> reports) {
    // Opción 1: Recursiva con Stream.concat + flatMap (súper expresiva)
    public Stream<OrgNode> flatten() {
        return Stream.concat(
                Stream.of(this),
                reports.stream().flatMap(OrgNode::flatten)
        );
    }

    // Opción 2: Zero-Allocation en Java 16–25 con mapMulti (evita crear sub-streams por cada nodo!)
    public void emitSubtree(Consumer<OrgNode> downstream) {
        downstream.accept(this);
        for (OrgNode child : reports) {
            child.emitSubtree(downstream);
        }
    }
}

// Uso de Opción 2 (cero sub-streams intermedios):
List<String> allOrgNames = Stream.of(ceoNode)
        .<OrgNode>mapMulti(OrgNode::emitSubtree)
        .map(OrgNode::name)
        .toList();
```

---

## 10.6 Concatenación Segura de $K$ Streams, Matrices 2D y Grafos de 2 Saltos

```java
// 1. Concatenar K >= 3 streams en O(1) profundidad de pila (evita StackOverflowError de Stream.concat en bucle)
List<String> allFour = Stream.of(s1, s2, s3, s4).flatMap(Function.identity()).toList();

// 2. Aplanar matriz 2D primitiva sin boxing
int[] flat = Arrays.stream(new int[][]{{1, 2}, {3, 4}, {5, 6}})
        .flatMapToInt(Arrays::stream).toArray();

// 3. Recorrido de grafo de 2 saltos (2-Hop Reachability)
List<String> twoHopNodes = graph.getOrDefault("A", List.of()).stream()
        .flatMap(neighbor -> graph.getOrDefault(neighbor, List.of()).stream())
        .toList();
```

---

## 10.7 Retos de Dominio y Autoevaluación (Capítulo 10)

1. **Nivel Inicial:** Empareja dos listas `keys` y `values` en un `Map` usando `IntStream.range` y genera todos los pares únicos $i < j$ de una lista de palabras sin duplicados simétricos.
2. **Nivel Intermedio:** Implementa un **Hash Join** (`INNER JOIN` y `LEFT OUTER JOIN`) en $O(N + M)$ entre `List<Order>` y `List<Employee>` evitando bucles anidados $O(N \times M)$.
3. **Nivel Avanzado:** Construye un **índice invertido** (`Map<String, Set<Integer>>`) sobre un corpus de documentos y aplana un organigrama recursivo `OrgNode` sin crear sub-streams intermedios usando `mapMulti`.
4. **Nivel Staff MAANG:** Implementa `zipLazy(Stream<A>, Stream<B>, BiFunction<A, B, R>)` con `Spliterators` y `.onClose()` para combinar un `Stream.iterate` infinito con un `Files.lines()` finito sin fugas de recursos.
