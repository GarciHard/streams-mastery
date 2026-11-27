# Capítulo 4: Mapeo y Transformación — Enciclopedia de `map`, `flatMap` y `mapMulti` (Java 25 LTS)

> *"Si `filter` decide qué elementos sobreviven en la banda transportadora, `map`, `flatMap` y `mapMulti` deciden en qué se convierten."*

En este capítulo cada método de transformación cuenta con su **ficha técnica completa**: firma oficial JDK 25, teoría y mecánica interna, diagrama de flujo o prueba de escritorio, escenarios reales de producción, trampas comunes y ejemplos progresivos.

---

## 4.1 Comparativa Arquitectónica: La Trinidad de Transformación

```text
1. map(T -> R)                 [E1, E2, E3]  --->  [DTO1, DTO2, DTO3]     (Siempre 1 a 1)
2. flatMap(T -> Stream<R>)     [O1, O2, O3]  --->  [Item1, Item2, Item3]  (1 a 0..N, pull, crea sub-Streams)
3. mapMulti((T, Consumer<R>))  [O1, O2, O3]  --->  [Item1, Item2, Item3]  (1 a 0..N, push, CERO sub-Streams)
```

---

## 4.2 `Stream.map(Function)`: Transformación 1 a 1

### Firma oficial
```java
<R> Stream<R> map(Function<? super T, ? extends R> mapper)
```

### ¿Qué hace y cómo funciona por debajo?
Aplica `mapper.apply(t)` a cada elemento `t` produciendo exactamente **1 elemento de salida `R`** (cardinalidad `1 : 1`).
* Es **intermedia**, **perezosa** y **sin estado** ($O(1)$ memoria).
* **Conserva** las banderas `SIZED` y `ORDERED`, pero **limpia** `DISTINCT` y `SORTED` (dos objetos distintos pueden mapearse al mismo valor).

### Escenarios reales de uso
1. Proyectar entidades de base de datos a DTOs de API (`Employee::fromEmployee`).
2. Extraer un campo (`Employee::getFullName`, `Order::getTotalPrice`).
3. Formatear reportes o registros de auditoría (siempre usando `Locale.US` cuando el formato numérico debe ser independiente del idioma del sistema operativo).

```java
List<EmployeeDTO> dtos = employees.stream()
        .filter(e -> "Engineering".equals(e.getDepartment()))
        .map(EmployeeDTO::fromEmployee)
        .toList();

List<String> ledger = transactions.stream()
        .map(tx -> String.format(Locale.US, "[%d] %s $%,.2f - %s",
                tx.getId(), tx.getType(), tx.getAmount(), tx.getCategory()))
        .toList();
```

---

## 4.3 `mapToInt`, `mapToLong`, `mapToDouble`: Cruce Sin *Boxing* a Streams Primitivos

### Firmas oficiales
```java
IntStream    mapToInt(ToIntFunction<? super T> mapper)
LongStream   mapToLong(ToLongFunction<? super T> mapper)
DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper)
```

### ¿Por qué existen?
Transforman un `Stream<T>` de objetos en un stream primitivo especializado (`IntStream`, `LongStream`, `DoubleStream`), habilitando terminales numéricos directos (`sum()`, `average()`, `summaryStatistics()`) sin crear millones de objetos `Integer`/`Long`/`Double` en el *heap*.

```java
double avgRating    = products.stream().mapToDouble(Product::getRating).average().orElse(0.0);
double totalPayroll = employees.stream().mapToDouble(Employee::getSalary).sum();
int    totalAges    = employees.stream().mapToInt(Employee::getAge).sum();
long   totalIds     = employees.stream().mapToLong(Employee::getId).sum();
```

---

## 4.4 `Stream.flatMap` y `flatMapToInt` / `flatMapToLong` / `flatMapToDouble`: Aplanamiento 1 a 0..N

### Firmas oficiales
```java
<R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper)
IntStream     flatMapToInt(Function<? super T, ? extends IntStream> mapper)
LongStream    flatMapToLong(Function<? super T, ? extends LongStream> mapper)
DoubleStream  flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper)
```

### ¿Qué hace y por qué existe? (Prueba de escritorio)
Cuando cada elemento contiene una colección (`Order` → `List<OrderItem>`), `map` produce `Stream<List<OrderItem>>` (cajas dentro de cajas). **`flatMap`** hace dos pasos atómicos:
1. Convierte cada `T` en un `Stream<R>` (de 0, 1 o $N$ elementos).
2. **Aplana** todos los sub-streams en un único `Stream<R>` continuo (y si `mapper` devuelve `null`, lo trata como un stream vacío).

| Entrada `Order` | `map(Order::getItems)` produce | `flatMap(o -> o.getItems().stream())` emite |
| :---: | :--- | :--- |
| `O1([MacBook, AirPods])` | `List[MacBook, AirPods]` | `MacBook`, `AirPods` |
| `O2([])` | `List[]` | *(0 elementos)* |
| `O3([Kindle])` | `List[Kindle]` | `Kindle` |
| **Salida final** | `List<List<OrderItem>>` | **`List<OrderItem>`: `[MacBook, AirPods, Kindle]`** |

### Escenarios reales de uso
```java
// 1. Aplanar Map<String, List<String>> (habilidades por departamento)
List<String> allSkills = departmentSkills.values().stream()
        .flatMap(List::stream).distinct().sorted().toList();

// 2. flatMapToDouble: ingreso total de ítems sin boxing
double grandTotal = orders.stream()
        .flatMapToDouble(o -> o.getItems().stream().mapToDouble(OrderItem::getSubtotal))
        .sum();

// 3. flatMapToInt y flatMapToLong: aplanar directo a primitivos
int digitSum = List.of(123, 456, 789).stream()
        .flatMapToInt(n -> String.valueOf(n).chars().map(c -> c - '0'))
        .sum(); // 45

long totalBatchBytes = List.of(new long[]{1024L, 2048L}, new long[]{4096L}).stream()
        .flatMapToLong(Arrays::stream)
        .sum(); // 7168L

// 4. Parseo seguro con Optional::stream (Java 9–25) + '_' (JEP 456)
List<Integer> validInts = List.of("100", "invalid", "250").stream()
        .map(s -> {
            try { return Optional.of(Integer.parseInt(s)); }
            catch (NumberFormatException _) { return Optional.<Integer>empty(); }
        })
        .flatMap(Optional::stream)
        .toList(); // [100, 250]
```

---

## 4.5 `Stream.mapMulti` y `mapMultiToInt` / `Long` / `Double` (Java 16–25): Expansión *Zero-Allocation*

### Firmas oficiales
```java
default <R> Stream<R> mapMulti(BiConsumer<? super T, ? super Consumer<R>> mapper)
default IntStream     mapMultiToInt(BiConsumer<? super T, ? super IntConsumer> mapper)
default LongStream    mapMultiToLong(BiConsumer<? super T, ? super LongConsumer> mapper)
default DoubleStream  mapMultiToDouble(BiConsumer<? super T, ? super DoubleConsumer> mapper)
```

### ¿Por qué existe frente a `flatMap`?
En `flatMap`, por cada elemento de entrada la JVM instancia un nuevo `Stream` y un `Spliterator` en el *heap*. Si procesas millones de eventos y cada uno produce 0, 1 o pocos elementos, generas millones de objetos basura. **`mapMulti`** invierte el modelo a ***push***: te entrega un `Consumer<R>` (o `IntConsumer`) al que empujas directamente los resultados con `.accept(...)` **sin asignar sub-streams**.

* **Nota de compilación (*Type Witness*):** Si encadenas más operaciones después de `mapMulti`, indica explícitamente el tipo `R`: `stream.<String>mapMulti(...)`.
* **Cuándo preferir `flatMap` sobre `mapMulti`:** Cuando cada entrada produce un sub-stream enorme y tienes un `.limit(k)` después (porque `flatMap` hace cortocircuito perezoso, mientras `mapMulti` termina su bucle interno antes de regresar control).

```java
// Ejemplo 1: Empujar ítems caros sin asignar sub-streams
List<String> expensiveItems = orders.stream()
        .<String>mapMulti((order, downstream) -> {
            for (OrderItem item : order.getItems()) {
                if (item.getSubtotal() > 100.0) downstream.accept(item.getProductName());
            }
        })
        .distinct()
        .toList();

// Ejemplo 2: mapMultiToInt — Parseo defensivo sin Optional ni Stream<Integer> (cero boxing)
int positiveSum = List.of("10;20;30", "invalid;-5;40", "50;60").stream()
        .mapMultiToInt((row, intConsumer) -> {
            for (String token : row.split(";")) {
                try {
                    int value = Integer.parseInt(token.trim());
                    if (value > 0) intConsumer.accept(value);
                } catch (NumberFormatException _) { /* descarta token corrupto */ }
            }
        })
        .sum(); // 210
```

---

## 4.6 Retos de Dominio y Autoevaluación (Capítulo 4)

1. **Nivel Inicial:** Convierte una lista de `Employee` a `EmployeeDTO` filtrando `"Engineering"` y calcula el salario promedio sin *autoboxing* usando `mapToDouble`.
2. **Nivel Intermedio:** Aplana una lista de `Order` entregadas (`"DELIVERED"`) para obtener las categorías únicas de `OrderItem` ordenadas alfabéticamente.
3. **Nivel Avanzado:** Dada una lista de cadenas con números mezclados y texto corrupto (`"10;abc;25;-4;30"`), calcula la suma de enteros positivos usando `mapMultiToInt` con **cero asignaciones de `Optional` y cero `Stream<Integer>`**.
4. **Nivel Staff MAANG:** Genera una matriz de promociones (`record Promo(String product, int discount, double finalPrice)`) y justifica cuándo elegir `flatMap` sobre `mapMulti` si el pipeline termina en `.limit(5)`.
