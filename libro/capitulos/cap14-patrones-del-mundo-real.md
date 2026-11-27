# Capítulo 14: Patrones de Producción del Mundo Real

> *"En un sistema real de comercio electrónico, banca o analítica, un Stream rara vez es un ejercicio aislado de 3 líneas: es un pipeline ETL, un motor de validación defensivo, un tablero ejecutivo inmutable o un limitador de tasa en memoria."*

---

## 14.1 Del laboratorio a producción: Los 7 arquetipos industriales

En los Capítulos 1 al 13 aprendiste todas las piezas del lenguaje y del motor de Streams. En este capítulo ensamblaremos esas piezas en los **7 patrones arquitectónicos** que aparecen constantemente en microservicios Java 25 de producción:

1. **Consultas tipo Repositorio y Búsqueda Multi-Palabra Clave**
2. **Pipelines de Validación Defensiva y Manejo Seguro de `null` / Claves Ausentes**
3. **Transformaciones Inmutables de Estado (Promociones y Reglas de Negocio)**
4. **ETL Financiero y Tableros Ejecutivos con `record` y `Collectors.teeing()`**
5. **Procesamiento por Lotes (*Batching*) y Ventanas Deslizantes (*Sliding Windows*)**
6. **Limitadores de Tasa (*Rate Limiters*) Basados en Ventana Deslizante de Logs**
7. **Reportes Corporativos, Directorios y Motores Analíticos en Memoria**

---

## 14.2 Arquetipo 1: Consultas tipo Repositorio y Búsqueda Multi-Keyword

Cuando consultas colecciones en memoria (cachés, resultados de una consulta previa o catálogos), encapsula los filtros con legibilidad SQL:

```java
// Consulta: Ingenieros Senior en San Francisco, ordenados por salario descendente
List<Employee> seniorSfEngineers = employees.stream()
    .filter(e -> "Engineering".equals(e.getDepartment()))
    .filter(e -> "San Francisco".equals(e.getCity()))
    .filter(e -> e.getTitle() != null && e.getTitle().contains("Senior"))
    .sorted(Comparator.comparingDouble(Employee::getSalary).reversed())
    .toList();
```

### Búsqueda de productos por múltiples palabras clave (insensible a mayúsculas)
Para encontrar cualquier producto cuyo nombre contenga **al menos una** de las palabras clave (`"pro"`, `"air"`, `"max"`), combina `.filter()` con un `.anyMatch()` interno:

```java
List<String> keywords = List.of("pro", "air", "max");

List<Product> matches = products.stream()
    .filter(p -> {
        String lowerName = p.getName().toLowerCase(Locale.ROOT);
        return keywords.stream().anyMatch(kw -> lowerName.contains(kw.toLowerCase(Locale.ROOT)));
    })
    .toList();
```

> **Observación de Ingeniería de Software (`Locale.ROOT`)**
> En producción, siempre pasa `Locale.ROOT` a `toLowerCase(Locale.ROOT)` cuando compares identificadores técnicos o texto independiente del idioma. En ciertas configuraciones regionales (como el turco), `"I".toLowerCase()` no produce `"i"` ASCII, causando bugs sutiles en filtros de texto.

---

## 14.3 Arquetipo 2: Validación defensiva y extracción segura de mapas

Los datos que llegan de APIs externas, CSVs o colas Kafka siempre pueden traer campos `null`, cadenas en blanco o divisas no soportadas.

### Pipeline de auditoría de transacciones inválidas
En lugar de lanzar una excepción en el primer error y abortar todo el lote, recolecta **todas** las transacciones que violan alguna regla de negocio para reportarlas juntas:

```java
Set<String> allowedCurrencies = Set.of("USD", "EUR");

List<Transaction> invalidTransactions = transactions.stream()
    .filter(tx -> tx.getAmount() <= 0.0 || !allowedCurrencies.contains(tx.getCurrency()))
    .toList();
```

### Extracción segura desde un `Map` con valor por defecto
Cuando traduces una lista de claves de configuración contra un mapa de variables de entorno:

```java
Map<String, String> env = Map.of("ENV", "PROD", "PORT", "8080");
List<String> keys = List.of("ENV", "DEBUG", "PORT");

List<String> resolved = keys.stream()
    .map(key -> env.getOrDefault(key, "DEFAULT"))
    .toList(); // ["PROD", "DEFAULT", "8080"]
```

---

## 14.4 Arquetipo 3: Transformación inmutable de estado (sin mutar la lista original)

**Regla de oro funcional**: nunca llames a `e.setSalary(...)` dentro de un `.map()` o `.peek()` sobre un objeto compartido. Si necesitas promover a todos los `"Junior Engineer"` a `"Software Engineer"` con un aumento del 15%, **crea una nueva instancia** para los promovidos y conserva intactos a los demás:

```java
List<Employee> updatedStaff = employees.stream()
    .map(e -> "Junior Engineer".equals(e.getTitle())
        ? new Employee(
            e.getId(), e.getFirstName(), e.getLastName(), e.getEmail(),
            e.getDepartment(), "Software Engineer", e.getAge(),
            e.getSalary() * 1.15, e.getCity(), e.getGender(),
            e.getYearsOfExperience(), e.getProgrammingLanguage())
        : e)
    .toList();
```

De este modo, la lista original sigue representando una instantánea histórica intacta (*before*) y `updatedStaff` representa el nuevo estado (*after*), lo que hace que las pruebas unitarias y la concurrencia sean triviales.

---

## 14.5 Arquetipo 4: Tableros ejecutivos con `record`, `teeing()` y ETL financiero

### Tablero ejecutivo inmutable en una arquitectura limpia
Los `record` de Java (introducidos en Java 16 y estándar en Java 25) son el complemento perfecto para proyectar métricas de negocio:

```java
record Dashboard(long totalEmployees, double avgSalary, String topEarnerName, double topSalary) {}

DoubleSummaryStatistics stats = employees.stream()
    .mapToDouble(Employee::getSalary)
    .summaryStatistics();

Employee topEarner = employees.stream()
    .max(Comparator.comparingDouble(Employee::getSalary))
    .orElseThrow();

Dashboard dashboard = new Dashboard(
    stats.getCount(),
    stats.getAverage(),
    topEarner.getFullName(),
    topEarner.getSalary()
);
```

### Dos métricas simultáneas en una sola pasada con `Collectors.teeing()`
Cuando quieres el mínimo y el máximo salario en una sola iteración del Stream:

```java
Map.Entry<Double, Double> extremes = employees.stream()
    .collect(Collectors.teeing(
        Collectors.minBy(Comparator.comparingDouble(Employee::getSalary)),
        Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary)),
        (minOpt, maxOpt) -> Map.entry(
            minOpt.map(Employee::getSalary).orElse(0.0),
            maxOpt.map(Employee::getSalary).orElse(0.0)
        )
    ));
```

### Pipeline ETL: Balance neto mensual por cuenta bancaria
Para transformar transacciones crudas (`CREDIT` suma, `DEBIT` resta) en un mapa anidado `Map<AccountId, Map<Mes, BalanceNeto>>`:

```java
Map<String, Map<Integer, Double>> accountMonthlyNet = transactions.stream()
    .collect(Collectors.groupingBy(
        Transaction::getAccountId,
        Collectors.groupingBy(
            tx -> tx.getDate().getMonthValue(),
            Collectors.summingDouble(tx ->
                tx.getType() == Transaction.Type.CREDIT ? tx.getAmount() : -tx.getAmount()
            )
        )
    ));
```

---

## 14.6 Arquetipo 5: *Batching*, Ventanas Deslizantes y *Rate Limiting*

### Comparativa clave: Java 8–23 (índices con `IntStream`) vs. Java 25 (`Gatherers`)

Antes de Java 25, dividir una lista en lotes de tamaño fijo o calcular promedios móviles obligaba a usar aritmética de índices sobre una `List` con acceso aleatorio (`subList`):

```java
// ── ENFOQUE CLÁSICO (Java 8–23): requiere una List en memoria con índices ──
int batchSize = 7;
int totalBatches = (int) Math.ceil((double) all.size() / batchSize);
List<List<Employee>> batchesClassic = IntStream.range(0, totalBatches)
    .mapToObj(i -> all.subList(i * batchSize, Math.min((i + 1) * batchSize, all.size())))
    .toList();

// Ventana deslizante clásica (promedio móvil de 3 valores)
int window = 3;
List<Double> movingAvgClassic = IntStream.rangeClosed(0, values.size() - window)
    .mapToObj(i -> values.subList(i, i + window).stream()
        .mapToDouble(Double::doubleValue).average().orElse(0.0))
    .toList();

// ── ENFOQUE MODERNO (Java 25 LTS — JEP 485): funciona en CUALQUIER Stream ──
List<List<Employee>> batchesJava25 = all.stream()
    .gather(Gatherers.windowFixed(7))
    .toList();

List<Double> movingAvgJava25 = values.stream()
    .gather(Gatherers.windowSliding(3))
    .map(w -> w.stream().mapToDouble(Double::doubleValue).average().orElse(0.0))
    .toList();
```

### *Rate Limiter* basado en ventana deslizante de marcas de tiempo
Para contar cuántos eventos ocurrieron en los últimos `windowSeconds` segundos (por ejemplo, en el intervalo `[currentTime - windowSeconds, currentTime]`):

```java
long recentEvents = eventTimestamps.stream()
    .filter(ts -> ts >= currentTime - windowSeconds && ts <= currentTime)
    .count();
```

> **Observación de Ingeniería de Software (Límites inclusivos vs. exclusivos en ventanas temporales):**
> Documenta siempre si tu ventana de *rate limiting* es cerrada `[t - W, t]` (`>=`) o semiabierta `(t - W, t]` (`>`). Un error de un segundo en el borde inferior altera las métricas de tráfico en hora pico.

---

## 14.7 Arquetipo 6: Reportes formateados y directorios corporativos

Generar un reporte multi-línea ordenado alfabéticamente por departamento:

```java
String report = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()))
    .entrySet().stream()
    .sorted(Map.Entry.comparingByKey())
    .map(e -> "Department: " + e.getKey() + ", Count: " + e.getValue())
    .collect(Collectors.joining("\n"));
```

---

## 14.8 Retos de Dominio y Autoevaluación (Capítulo 14)

1. **Nivel Inicial:** Implementa un buscador multi-palabra clave insensible a mayúsculas con `Locale.ROOT` y una extracción segura de configuración desde un `Map` usando `getOrDefault`.
2. **Nivel Intermedio:** Construye una transformación inmutable que promueva a todos los `"Junior Engineer"` a `"Software Engineer"` con un aumento del 15% sin modificar la colección original.
3. **Nivel Avanzado:** Implementa un pipeline ETL financiero que agrupe transacciones en `Map<String, Map<Integer, Double>>` (Cuenta → Mes → Balance neto) y un tablero ejecutivo inmutable con `record Dashboard(...)`.
4. **Nivel Staff MAANG:** Refactoriza un procesador por lotes y un promedio móvil basados en `IntStream.rangeClosed` + `subList` hacia `Gatherers.windowFixed` y `Gatherers.windowSliding` de Java 25 LTS.
