# Capítulo 8: Agrupación Multinivel, Particionamiento y Enciclopedia de Collectors *Downstream*

> *"Si sabes escribir `GROUP BY`, `HAVING` y tablas pivote en SQL, `Collectors.groupingBy()` y `Collectors.partitioningBy()` te permiten hacer exactamente lo mismo en memoria en una sola pasada $O(N)$ con total seguridad de tipos —siempre y cuando entiendas qué hace exactamente cada collector downstream por debajo."*

En este capítulo **no daremos nada por sentado ni dejaremos ningún método como un simple nombre en una lista**. Cada una de las herramientas de agrupación, particionamiento y recolección *downstream* de `java.util.stream.Collectors` tiene su propia **ficha técnica completa**: firma oficial de Java 25, explicación teórica desde cero, por qué existe (frente a la alternativa ingenua), diagrama de flujo de datos, escenarios reales de producción, trampas comunes y ejemplos progresivos.

---

## 8.1 `Collectors.groupingBy(...)`: El Motor `GROUP BY` en Memoria

### Firma oficial (3 sobrecargas en Java 25)
```java
// Sobrecarga 1: Agrupación básica -> Map<K, List<T>> (HashMap)
public static <T, K> Collector<T, ?, Map<K, List<T>>>
    groupingBy(Function<? super T, ? extends K> classifier)

// Sobrecarga 2: Agrupación con collector downstream -> Map<K, D> (HashMap)
public static <T, K, A, D> Collector<T, ?, Map<K, D>>
    groupingBy(Function<? super T, ? extends K> classifier,
               Collector<? super T, A, D> downstream)

// Sobrecarga 3: Agrupación con fábrica de Map específica (TreeMap, LinkedHashMap) + downstream
public static <T, K, D, A, M extends Map<K, D>> Collector<T, ?, M>
    groupingBy(Function<? super T, ? extends K> classifier,
               Supplier<M> mapFactory,
               Collector<? super T, A, D> downstream)
```

### ¿Qué hace y cómo funciona por debajo?
`groupingBy` recorre el Stream una sola vez ($O(N)$). Por cada elemento `t`:
1. Aplica `classifier.apply(t)` para calcular la **clave `k`** de su cubeta (*bucket*).
2. Busca `k` en el mapa interno. Si `k` no existe todavía, invoca `downstream.supplier().get()` para crear un acumulador vacío para esa clave.
3. Entrega `t` al acumulador de la clave `k` llamando a `downstream.accumulator().accept(acc, t)`.
4. Al finalizar el Stream, aplica `downstream.finisher()` a cada cubeta.

```text
Stream<Employee> ──▶ classifier(e) = e.getDepartment()
                         │
                         ├── "Engineering" ──▶ [acumulador downstream #1] ──▶ finisher ──▶ Valor D1
                         ├── "Design"      ──▶ [acumulador downstream #2] ──▶ finisher ──▶ Valor D2
                         └── "Product"     ──▶ [acumulador downstream #3] ──▶ finisher ──▶ Valor D3
```

### Puente Imperativo vs. Stream
```java
// ── ANTES (Imperativo) ──
Map<String, List<Employee>> byCityOld = new HashMap<>();
for (Employee e : employees) {
    byCityOld.computeIfAbsent(e.getCity(), _ -> new ArrayList<>()).add(e);
}

// ── DESPUÉS (Java 25 Stream) ──
Map<String, List<Employee>> byCity = employees.stream()
        .collect(Collectors.groupingBy(Employee::getCity));
```

> **Error Común de Programación (Clave `null` en `groupingBy`):**
> `classifier.apply(t)` **jamás puede devolver `null`**. Si un objeto tiene `city == null`, `groupingBy` lanza `NullPointerException: element cannot be mapped to a null key`. Normaliza siempre campos opcionales: `e -> Objects.requireNonNullElse(e.getCity(), "UNKNOWN")`.

---

## 8.2 `Collectors.mapping(...)`: Transformar Elementos *Después* de Clasificar

### Firma oficial
```java
public static <T, U, A, R> Collector<T, ?, R> mapping(
    Function<? super T, ? extends U> mapper,
    Collector<? super U, A, R> downstream)
```

### ¿Por qué existe `Collectors.mapping()` si ya existe `Stream.map()`?
Esta es la pregunta clave que separa a quien memoriza sintaxis de quien entiende la arquitectura de Streams.

Imagina que quieres obtener un **`Map<String, List<String>>`** donde la clave es el **departamento** y el valor es la lista de **nombres completos** de sus empleados (`"Engineering" -> ["Alice Johnson", "Bob Smith"]`).

* **¿Qué pasa si intentas usar `.map(Employee::getFullName)` ANTES de `groupingBy`?**
  ¡Se rompe! Una vez que haces `.map(Employee::getFullName)`, tu `Stream<Employee>` se convierte en `Stream<String>`. **Acabas de destruir el objeto `Employee` y ya no puedes preguntar `Employee::getDepartment` en el `groupingBy`!**
* **Cómo lo resuelve `Collectors.mapping(mapper, downstream)`:**
  Mantiene el objeto `Employee` intacto para que `groupingBy` lea `Employee::getDepartment`, y **solo dentro de cada cubeta** aplica `mapper` (`Employee::getFullName`) justo antes de pasar el resultado al collector interno `downstream` (`toList()`, `toSet()`, `joining(", ")`).

```text
Employee("Alice", dept="Eng")
    │
    ├──▶ groupingBy lee dept = "Eng" (clave del mapa)
    │
    └──▶ Collectors.mapping(Employee::getFullName, Collectors.toList())
              │
              ├── 1. Aplica mapper: Employee -> "Alice Johnson" (String)
              └── 2. Entrega "Alice Johnson" al downstream: List.add("Alice Johnson")
```

### Escenarios reales de uso
1. **Directorios corporativos:** `Map<Departamento, Set<Email>>` o `Map<Ciudad, List<Nombre>>`.
2. **Reportes concatenados por grupo:** `Map<Lenguaje, String>` con nombres separados por `", "` usando `mapping(..., joining(", "))`.
3. **Ahorro de memoria:** Nunca almacena `List<Employee>` completos en el mapa para luego re-mapearlos; proyecta al vuelo en una sola pasada $O(N)$.

### Ejemplos progresivos
```java
// Ejemplo 1 (Básico): Departamento -> Lista de nombres completos
Map<String, List<String>> namesByDept = employees.stream()
        .collect(Collectors.groupingBy(
                Employee::getDepartment,
                Collectors.mapping(Employee::getFullName, Collectors.toList())
        ));

// Ejemplo 2 (Intermedio): Lenguaje de programación -> Nombres unidos en un solo String
Map<String, String> rosterByLanguage = employees.stream()
        .collect(Collectors.groupingBy(
                Employee::getProgrammingLanguage,
                Collectors.mapping(Employee::getFirstName, Collectors.joining(", "))
        ));

// Ejemplo 3 (Avanzado — TreeMap ordenado + Set inmodificable de emails por departamento):
TreeMap<String, Set<String>> emailDirectory = employees.stream()
        .collect(Collectors.groupingBy(
                Employee::getDepartment,
                TreeMap::new,
                Collectors.mapping(Employee::getEmail, Collectors.toUnmodifiableSet())
        ));
```

---

## 8.3 `Collectors.flatMapping(...)` (Java 9+): Aplanar Subcolecciones *Dentro* de Cada Grupo

### Firma oficial
```java
public static <T, U, A, R> Collector<T, ?, R> flatMapping(
    Function<? super T, ? extends Stream<? extends U>> mapper,
    Collector<? super U, A, R> downstream)
```

### ¿Por qué existe y cómo funciona?
Es el hermano monádico de `mapping()`. Mientras `mapping()` transforma **1 elemento en 1 valor**, `flatMapping()` transforma **1 elemento en un `Stream` de 0, 1 o muchos valores** y los alimenta uno por uno al collector `downstream` dentro de la misma cubeta.

Piensa en `Order` (que tiene `customerName` y `List<OrderItem> items`). Si hicieras `.flatMap(o -> o.getItems().stream())` **antes** de `groupingBy`, perderías `Order::getCustomerName` porque `OrderItem` no sabe quién fue el cliente. Con `Collectors.flatMapping(...)`:
1. `groupingBy` agrupa por `Order::getCustomerName`.
2. Dentro de la cubeta de cada cliente, `flatMapping` abre `order.getItems().stream()` y empuja cada `OrderItem` al `downstream` (que puede ser un `Set`, un `counting()`, o ¡hasta otro `groupingBy` para crear una matriz pivote!).

```text
Order(#1, client="Ana", items=[Laptop, Mouse])
    │
    ├──▶ groupingBy clave = "Ana"
    └──▶ flatMapping(o -> o.getItems().stream(), downstream)
              ├── empuja Laptop ──▶ downstream
              └── empuja Mouse  ──▶ downstream
```

### Escenarios reales y ejemplos progresivos
```java
// Ejemplo 1: Cliente -> Conjunto único de nombres de productos que ha comprado
Map<String, Set<String>> productsPerCustomer = orders.stream()
        .collect(Collectors.groupingBy(
                Order::getCustomerName,
                Collectors.flatMapping(
                        o -> o.getItems().stream().map(OrderItem::getProductName),
                        Collectors.toSet()
                )
        ));

// Ejemplo 2 (Nivel Staff — Matriz Pivote 2D): Cliente -> (Categoría -> Gasto total)
Map<String, Map<String, Double>> customerCategorySpend = orders.stream()
        .collect(Collectors.groupingBy(
                Order::getCustomerName,
                Collectors.flatMapping(
                        o -> o.getItems().stream(),
                        Collectors.groupingBy(
                                OrderItem::getCategory,
                                Collectors.summingDouble(OrderItem::getSubtotal)
                        )
                )
        ));
```
*(Nota técnica: si `mapper` devuelve `null` para un elemento, `flatMapping` lo trata con seguridad como un stream vacío y no lanza NPE).*

---

## 8.4 `Collectors.filtering(...)` (Java 9+): Filtrar *Dentro* de Cada Cubeta Sin Perder Claves

### Firma oficial
```java
public static <T, A, R> Collector<T, ?, R> filtering(
    Predicate<? super T> predicate,
    Collector<? super T, A, R> downstream)
```

### ¿Por qué existe si ya existe `Stream.filter()`? (Prueba de escritorio)
Supón que tienes `Alice (Engineering, $250k)`, `Bob (Engineering, $150k)` y `Clara (Design, $140k)`, y quieres contar cuántos empleados por departamento ganan `> $200k`:

| Elemento | Enfoque A: `.filter(sal > 200k)` ANTES de `groupingBy` | Enfoque B: `Collectors.filtering(sal > 200k, counting())` DENTRO |
| :--- | :--- | :--- |
| `Alice (Eng, 250k)` | Pasa `filter` → crea clave `"Eng": 1L` | Crea clave `"Eng"` → pasa `filtering` → `"Eng": 1L` |
| `Bob (Eng, 150k)` | Descartado antes de `groupingBy` | Clave `"Eng"` ya existe → descartado → `"Eng": 1L` |
| `Clara (Design, 140k)` | **Descartada antes de `groupingBy` (la clave jamás nace)** | **Crea clave `"Design"` con valor inicial `0L`** → descartada |
| **Mapa final** | `{"Engineering": 1L}` (**`"Design"` desapareció**) | `{"Engineering": 1L, "Design": 0L}` (**preserva `"Design"` con `0L`**) |

### Escenarios reales de uso
* **Tableros y gráficas de barras:** Cuando un departamento tiene `0` ventas o `0` incidentes críticos, necesitas que aparezca en el JSON con `0` (o `[]`) y no que desaparezca de la gráfica.
* **Composición con `mapping`:** Puedes anidar `filtering(pred, mapping(fn, toList()))` dentro de `groupingBy`.

```java
Map<String, List<String>> highEarnerNamesByDept = employees.stream()
        .collect(Collectors.groupingBy(
                Employee::getDepartment,
                Collectors.filtering(
                        e -> e.getSalary() > 200_000.0,
                        Collectors.mapping(Employee::getFullName, Collectors.toList())
                )
        ));
// "Design" aparecerá con lista vacía [] en lugar de desaparecer del mapa!
```

---

## 8.5 Collectors Numéricos y Estadísticos *Downstream* (`counting`, `summing*`, `averaging*`, `summarizing*`)

En lugar de guardar listas enteras en cada cubeta (`O(N)` memoria) para luego sumarlas, estos recolectores mantienen únicamente contadores o acumuladores escalares en **$O(1)$ de memoria por grupo**:

### 1. `Collectors.counting()`
* **Firma:** `Collector<T, ?, Long> counting()`
* **Para qué sirve:** Cuenta cuántos elementos caen en cada grupo (equivale a `COUNT(*)` en SQL). Devuelve `0L` si el grupo recibió 0 elementos tras un `filtering`.
* **Cuándo NO usarlo:** En un stream directo sin `groupingBy`, usa `stream.count()` (que es $O(1)$ si el stream es `SIZED`), nunca `stream.collect(Collectors.counting())`.

### 2. Familia `summing*`: `summingInt`, `summingLong`, `summingDouble`
* **Firmas:**
  * `Collector<T, ?, Integer> summingInt(ToIntFunction<? super T> mapper)`
  * `Collector<T, ?, Long> summingLong(ToLongFunction<? super T> mapper)`
  * `Collector<T, ?, Double> summingDouble(ToDoubleFunction<? super T> mapper)` *(usa **Suma Compensada de Kahan** internamente)*
* **Para qué sirve:** Equivale a `SUM(columna)` en SQL por cada grupo. Retorna `0` / `0L` / `0.0` para grupos vacíos.

### 3. Familia `averaging*`: `averagingInt`, `averagingLong`, `averagingDouble`
* **Firmas:**
  * `Collector<T, ?, Double> averagingInt(ToIntFunction<? super T> mapper)`
  * `Collector<T, ?, Double> averagingLong(ToLongFunction<? super T> mapper)`
  * `Collector<T, ?, Double> averagingDouble(ToDoubleFunction<? super T> mapper)`
* **Para qué sirve:** Equivale a `AVG(columna)` en SQL. Mantiene `[suma, conteo]` por cubeta y divide en el `finisher`. Retorna `0.0` (no `NaN`) si la cubeta quedó vacía.

### 4. Familia `summarizing*`: `summarizingInt`, `summarizingLong`, `summarizingDouble`
* **Firmas:**
  * `Collector<T, ?, IntSummaryStatistics> summarizingInt(ToIntFunction<? super T> mapper)`
  * `Collector<T, ?, LongSummaryStatistics> summarizingLong(ToLongFunction<? super T> mapper)`
  * `Collector<T, ?, DoubleSummaryStatistics> summarizingDouble(ToDoubleFunction<? super T> mapper)`
* **Para qué sirve:** Calcula simultáneamente **`count`, `sum`, `min`, `max` y `average`** por cada grupo en **una sola pasada $O(N)$**.

```java
// Ejemplos completos de las 4 familias numéricas downstream:
Map<String, Long> countByCategory = products.stream()
        .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));

Map<String, Integer> unitsByCategory = orders.stream().flatMap(o -> o.getItems().stream())
        .collect(Collectors.groupingBy(OrderItem::getCategory, Collectors.summingInt(OrderItem::getQuantity)));

Map<String, Long> sumIdsByDept = employees.stream()
        .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.summingLong(Employee::getId)));

Map<String, Double> netBalanceByCategory = transactions.stream()
        .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.summingDouble(tx -> tx.getType() == Transaction.Type.CREDIT ? tx.getAmount() : -tx.getAmount())
        ));

Map<String, Double> avgExpByDept = employees.stream()
        .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.averagingInt(Employee::getYearsOfExperience)));

Map<String, Double> avgIdByDept = employees.stream()
        .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.averagingLong(Employee::getId)));

Map<String, IntSummaryStatistics> ageStatsByDept = employees.stream()
        .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.summarizingInt(Employee::getAge)));

Map<String, LongSummaryStatistics> longStatsByDept = employees.stream()
        .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.summarizingLong(Employee::getId)));

Map<String, DoubleSummaryStatistics> salaryStatsByDept = employees.stream()
        .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.summarizingDouble(Employee::getSalary)));
```

---

## 8.6 `Collectors.maxBy`, `minBy`, `reducing` y `collectingAndThen` Como *Downstreams*

### 1. `Collectors.maxBy(Comparator)` y `Collectors.minBy(Comparator)`
* **Firmas:** `Collector<T, ?, Optional<T>> maxBy(Comparator<? super T> comparator)` (y análogo `minBy`).
* **Por qué devuelven `Optional<T>`:** Porque si un grupo pudiera quedar vacío (por ejemplo, tras `Collectors.filtering`), no existe un máximo real.
* **Cómo desempacar el `Optional` limpiamente:** Envuélvelo con **`Collectors.collectingAndThen(maxBy(...), opt -> opt.map(...).orElse(...))`**.

```java
// Precio máximo por categoría sin Optional usando collectingAndThen:
Map<String, Double> maxPricePerCategory = products.stream()
        .collect(Collectors.groupingBy(
                Product::getCategory,
                Collectors.collectingAndThen(
                        Collectors.maxBy(Comparator.comparingDouble(Product::getPrice)),
                        opt -> opt.map(Product::getPrice).orElse(0.0)
                )
        ));
```

### 2. `Collectors.reducing(...)` (Las 3 sobrecargas dentro de `groupingBy`)
* `reducing(BinaryOperator<T> op)` → `Optional<T>`
* `reducing(T identity, BinaryOperator<T> op)` → `T`
* `reducing(U identity, Function<? super T, ? extends U> mapper, BinaryOperator<U> op)` → `U` (proyecta y reduce al vuelo sin `Optional`)

```java
// Suma exacta en BigDecimal por departamento en una sola pasada:
Map<String, BigDecimal> exactPayrollByDept = employees.stream()
        .collect(Collectors.groupingBy(
                Employee::getDepartment,
                Collectors.reducing(
                        BigDecimal.ZERO,
                        e -> BigDecimal.valueOf(e.getSalary()),
                        BigDecimal::add
                )
        ));
```

### 3. Cláusula SQL `HAVING` sobre el resultado de `groupingBy`
Cuando necesitas filtrar grupos completos (por ejemplo, departamentos cuya nómina total supera `$1,000,000`), abre un segundo stream sobre `.entrySet().stream()`:

```java
List<String> highBudgetDepts = employees.stream()
        .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.summingDouble(Employee::getSalary)))
        .entrySet().stream()
        .filter(entry -> entry.getValue() > 1_000_000.0) // Equivalente a HAVING SUM(salary) > 1000000
        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
        .map(Map.Entry::getKey)
        .toList();
```

---

## 8.7 `Collectors.partitioningBy(...)` y `Collectors.groupingByConcurrent(...)`

### 1. `Collectors.partitioningBy(Predicate, [downstream])`
* **Firmas:**
  * `Collector<T, ?, Map<Boolean, List<T>>> partitioningBy(Predicate<? super T> predicate)`
  * `Collector<T, ?, Map<Boolean, D>> partitioningBy(Predicate<? super T> predicate, Collector<? super T, A, D> downstream)`
* **Por qué preferirlo sobre `groupingBy(e -> condicionBooleana)`:**
  1. **Garantía de 2 claves:** El mapa resultante **siempre** contiene tanto `Boolean.TRUE` como `Boolean.FALSE` (incluso si el stream viene vacío o todos los elementos caen en `true`), evitando `NullPointerException` al consultar `map.get(false)`.
  2. **Menor costo:** Internamente usa un objeto `Partition<T>` de solo 2 campos en lugar de calcular *hashes* en un `HashMap`.

```java
Map<Boolean, Double> avgSalaryByFemale = employees.stream()
        .collect(Collectors.partitioningBy(
                e -> "Female".equals(e.getGender()),
                Collectors.averagingDouble(Employee::getSalary)
        ));
```

### 2. `Collectors.groupingByConcurrent(...)`
* **Firmas:** Análogas a las 3 sobrecargas de `groupingBy`, devolviendo `ConcurrentMap<K, D>`.
* **Cómo funciona y cuándo usarlo:** En un `parallelStream()` con millones de elementos, `groupingBy` crea un `HashMap` por hilo y luego fusiona todos los mapas en el `combiner` (lo cual puede ser muy costoso si hay miles de claves). `groupingByConcurrent` declara las banderas `CONCURRENT | UNORDERED`, permitiendo que todos los hilos escriban directamente en un único `ConcurrentHashMap` compartido.
* **Requisito:** Solo se activa el modo concurrente si el stream es paralelo y **desordenado** (`.unordered()`).

```java
ConcurrentMap<String, Long> countByDeptParallel = employees.parallelStream()
        .unordered()
        .collect(Collectors.groupingByConcurrent(Employee::getDepartment, Collectors.counting()));
```

---

## 8.8 Retos de Dominio y Autoevaluación (Capítulo 8)

1. **Nivel Inicial:** Agrupa una lista de `Product` por categoría obteniendo el conteo por cubeta y el precio promedio sin almacenar listas intermedias.
2. **Nivel Intermedio:** Usa `Collectors.mapping` para construir un `TreeMap<String, Set<String>>` de correos por departamento y explica por qué no podías usar `.map(Employee::getEmail)` antes de `groupingBy`.
3. **Nivel Avanzado:** Demuestra la diferencia entre `.filter(sal > 200k)` antes de `groupingBy` y `Collectors.filtering(sal > 200k, counting())` dentro de `groupingBy` cuando un departamento tiene `0` coincidencias, y añade un filtro `HAVING` para conservar solo departamentos con nómina `> $1M`.
4. **Nivel Staff MAANG:** Construye una matriz pivote 2D `Map<String, Map<String, Double>>` (Cliente → Categoría → Gasto Total) en una sola pasada encadenando `groupingBy` + `flatMapping` + `groupingBy` + `summingDouble`, y calcula la nómina exacta por departamento en `BigDecimal` usando `Collectors.reducing`.
