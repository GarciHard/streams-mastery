# Parte I: Fundamentos de la Programación Funcional en Java 25

# Capítulo 1: Fundamentos de Expresiones Lambda, Interfaces Funcionales y Referencias a Métodos

> *"Antes de construir tuberías de datos de alto rendimiento con Streams, debes dominar el lenguaje de las funciones como ciudadanos de primera clase en Java."*

Si nunca antes has trabajado con programación funcional ni con la API de Streams de Java, **este capítulo empieza desde cero absoluto**. No vamos a dar nada por sentado: construiremos cada concepto ladrillo por ladrillo —desde por qué existían las clases anónimas antes de Java 8, pasando por cada regla sintáctica de una expresión lambda, hasta el desglose exhaustivo con ejemplos paso a paso de **todas** las familias de interfaces funcionales de `java.util.function`, las referencias a métodos, las variables anónimas `_` de Java 22–25 y los patrones de composición de nivel MAANG Staff.

---

## 1.1 El Problema Histórico: Pasar Comportamiento Como Argumento

Imagina que tienes una lista de empleados (`List<Employee>`) y tu jefe te pide un método para filtrar empleados de Ingeniería. Escribes un bucle `for` con `if (e.getDepartment().equals("Engineering"))`. Al día siguiente te piden filtrar por salario mayor a $200,000. Al tercer día, por ciudad `"Seattle"` y experiencia mayor a 5 años. Si escribes un método distinto para cada combinación, tu código explota en duplicación.

La solución orientada a objetos clásica consiste en **parametrizar el comportamiento**: recibir una estrategia (una interfaz) que decida si cada elemento cumple la condición:

```java
// 1. Definimos un contrato que representa "una prueba sobre un Employee"
public interface EmployeeCondition {
    boolean test(Employee e);
}

// 2. Un único método reutilizable que recibe el comportamiento como parámetro
public List<Employee> filterEmployees(List<Employee> employees, EmployeeCondition condition) {
    List<Employee> result = new ArrayList<>();
    for (Employee e : employees) {
        if (condition.test(e)) {
            result.add(e);
        }
    }
    return result;
}
```

### Cómo se hacía antes de Java 8: Clases Anónimas Internas
Antes de Java 8 (2014), cada vez que querías pasar una condición distinta debías escribir una **clase anónima interna** cargada de ceremonia (*boilerplate*):

```java
// Estilo pre-Java 8: 6 líneas verbosas solo para expresar una comparación simple
List<Employee> highEarners = filterEmployees(employees, new EmployeeCondition() {
    @Override
    public boolean test(Employee e) {
        return e.getSalary() > 200_000.0;
    }
});
```

Además de ser incómodo de leer, cada clase anónima generaba un archivo `.class` físico extra en disco (`MiClase$1.class`), cargaba una clase adicional en *Metaspace* y reservaba un objeto nuevo en el *heap* cada vez que se ejecutaba esa línea.

### La Revolución de las Expresiones Lambda (Java 8 a Java 25 LTS)
Con una expresión lambda, eliminamos toda la ceremonia y conservamos únicamente **los parámetros y la lógica real**:

```java
// Estilo moderno Java 8–25: 1 línea limpia, declarativa y eficiente
List<Employee> highEarners = filterEmployees(employees, e -> e.getSalary() > 200_000.0);
```

---

## 1.2 Anatomía Completa de la Sintaxis de una Expresión Lambda

Una expresión lambda es una **función anónima** (sin nombre propio) que puedes guardar en una variable, pasar como argumento a un método o devolver como resultado de otro método.

Su estructura tiene exactamente **tres partes**:

```
   (parámetros)      ->      cuerpo
   ────────────      ──      ──────────────────────────────
   Lo que recibe   Flecha    Lo que calcula o ejecuta
```

### Todas las Variantes Sintácticas Válidas (y Cuándo Usar Cada Una)

| Caso | Sintaxis Válida | Ejemplo Concreto | Notas del Compilador |
|---|---|---|---|
| **0 parámetros** | `() -> expresión` | `() -> 42` | Los paréntesis vacíos `()` son **obligatorios**. |
| **1 parámetro (tipo inferido)** | `x -> expresión` | `e -> e.getSalary() > 100_000` | Puedes omitir los paréntesis si hay 1 solo parámetro sin tipo explícito. |
| **1 parámetro (tipo explícito)** | `(Tipo x) -> expresión` | `(Employee e) -> e.getSalary() > 100_000` | Si escribes el tipo, los paréntesis son **obligatorios**. |
| **2 o más parámetros** | `(a, b) -> expresión` | `(a, b) -> a + b` | Los paréntesis son **obligatorios**. |
| **Parámetros con `var` (Java 11+)** | `(var a, var b) -> ...` | `(@NonNull var s) -> s.trim()` | Útil **únicamente** si necesitas poner anotaciones (como `@NonNull`) en parámetros inferidos. No puedes mezclar `var` con parámetros sin `var`. |
| **Parámetro ignorado `_` (Java 22+, [JEP 456](https://openjdk.org/jeps/456))** | `(a, _) -> expresión` | `(existing, _) -> existing` | Indica al compilador y al lector que el segundo parámetro no se usa jamás. |
| **Cuerpo de 1 sola expresión** | `x -> expr` | `s -> s.toUpperCase()` | **Sin llaves `{}` ni `return` ni `;`**. El resultado de la expresión se devuelve automáticamente. |
| **Cuerpo de bloque (varias líneas)** | `x -> { sentencias; return res; }` | `e -> { double bonus = e.getSalary() * 0.1; return bonus; }` | Si abres llaves `{}` y el método retorna un valor, la palabra clave **`return` y el `;` son obligatorios**. |

> **Error Común de Programación (Llaves sin `return`):**
> `Function<String, Integer> bad = s -> { s.length(); };` **NO compila.** En cuanto escribes `{ ... }`, el compilador espera un bloque de sentencias y exige `return s.length();`. Si es una sola expresión, quita las llaves: `s -> s.length()`.

---

## 1.3 Interfaces Funcionales y el Contrato SAM (`@FunctionalInterface`)

¿Cómo sabe Java de qué tipo es la lambda `e -> e.getSalary() > 200_000`?
A través de su **tipo objetivo (*target type*)**: el contexto donde se asigna o pasa la lambda debe ser una **interfaz funcional**.

Una **interfaz funcional** (*Single Abstract Method* o **SAM**) es cualquier interfaz en Java que cumple una única regla: **declara exactamente un método abstracto**.

Además de ese único método abstracto, una interfaz funcional **puede tener**:
1. Cualquier cantidad de métodos `default` (con cuerpo implementado).
2. Cualquier cantidad de métodos `static` (fábricas o utilidades).
3. Métodos `private` auxiliares (desde Java 9).
4. Declaraciones de métodos públicos de `java.lang.Object` (`equals`, `hashCode`, `toString`), que **no cuentan** porque toda clase en Java hereda su implementación de `Object`.

```java
@FunctionalInterface
public interface TriFunction<T, U, V, R> {
    // ÚNICO método abstracto (contrato SAM)
    R apply(T t, U u, V v);

    // Método default permitido: permite encadenar una transformación posterior
    default <W> TriFunction<T, U, V, W> andThen(Function<? super R, ? extends W> after) {
        Objects.requireNonNull(after);
        return (t, u, v) -> after.apply(apply(t, u, v));
    }

    // Método de Object: NO rompe el contrato SAM
    @Override
    boolean equals(Object obj);
}
```

> **Observación de Ingeniería de Software:** Aunque `@FunctionalInterface` es opcional para que una interfaz SAM acepte lambdas, **úsala siempre** en tus interfaces funcionales propias. Actúa como un candado en tiempo de compilación: si otro integrante del equipo agrega un segundo método abstracto por accidente, `javac` marcará error de inmediato.

---

## 1.4 Bajo el Capó de la JVM: `invokedynamic` vs Clases Anónimas

En entrevistas de nivel Senior/Staff es muy común que te pregunten: *"¿Una lambda en Java es lo mismo que una clase anónima?"* La respuesta técnica precisa es **no**:

| Aspecto | Clase Anónima Interna | Expresión Lambda (Java 8–25) |
|---|---|---|
| **Compilación (`javac`)** | Crea un archivo `.class` extra por cada clase anónima (`App$1.class`). | **Cero archivos `.class` extra.** Convierte el cuerpo de la lambda en un método `private` sintético dentro de la misma clase (`lambda$metodo$0`). |
| **Bytecode en ejecución** | `new` + `invokespecial` cada vez que pasa por esa línea. | Instrucción **`invokedynamic`** que llama a `LambdaMetafactory.metafactory(...)` la primera vez (*bootstrap*) y enlaza el sitio de llamada con `MethodHandle`. |
| **Memoria en Heap** | Siempre asigna un nuevo objeto por invocación. | Si la lambda **no captura variables externas** (*stateless*), la JVM puede reutilizar una única instancia *singleton* (**0 asignaciones**). |
| **Palabra clave `this`** | Se refiere a la instancia de la clase anónima. | Se refiere léxicamente a la instancia de la **clase que contiene el método** (la lambda no tiene su propio `this`). |
| **Sombreado de variables** | Permite declarar variables con el mismo nombre que una variable local externa. | **Prohibido**: declarar un parámetro o variable con el mismo nombre que una variable local externa es error de compilación. |

---

## 1.5 Las 4 Familias Raíz de `java.util.function` Explicadas Desde Cero

En lugar de obligarte a inventar interfaces como `EmployeeCondition` o `StringTransformer` en cada proyecto, Java incluye en el paquete **`java.util.function`** un catálogo completo de **43 interfaces funcionales estándar** que alimentan absolutamente a toda la API de Streams.

Todas esas 43 interfaces nacen de **4 familias raíz**. Si entiendes a fondo estas 4 familias, podrás leer y escribir cualquier pipeline de Streams sin titubear:

```
  1. Predicate<T>     :  T       --->  boolean    (¿Cumple la condición? Sí / No)
  2. Function<T, R>   :  T       --->  R          (Transforma una entrada T en una salida R)
  3. Consumer<T>      :  T       --->  void       (Recibe T y ejecuta una acción / efecto)
  4. Supplier<T>      :  ()      --->  T          (No recibe nada; fabrica o provee un valor T)
```

Estudiemos cada una de ellas **con total detalle, su API completa y ejemplos progresivos desde cero**.

---

### Familia 1: `Predicate<T>` — Evaluar Condiciones (`T -> boolean`)

#### ¿Qué es y para qué sirve?
Un `Predicate<T>` representa una **pregunta de verdadero o falso** sobre un objeto de tipo `T`. Piensa en él como un **filtro o guardián de entrada**: recibe un elemento y decide si pasa (`true`) o se descarta (`false`).

- **Dónde aparece en Streams:** `filter()`, `takeWhile()`, `dropWhile()`, `anyMatch()`, `allMatch()`, `noneMatch()`, `Collectors.partitioningBy()`, `Collectors.filtering()`.

#### API Completa de `Predicate<T>`
- **`boolean test(T t)`** *(SAM)*: evalúa la condición sobre `t`.
- **`default Predicate<T> and(Predicate<? super T> other)`**: equivale a `&&` (con cortocircuito: si el primero es `false`, el segundo ni se evalúa).
- **`default Predicate<T> or(Predicate<? super T> other)`**: equivale a `||` (con cortocircuito: si el primero es `true`, el segundo ni se evalúa).
- **`default Predicate<T> negate()`**: equivale a `!` (invierte el resultado).
- **`static <T> Predicate<T> not(Predicate<? super T> target)`** *(Java 11+)*: niega un predicado o referencia a método con sintaxis súper limpia (`Predicate.not(String::isBlank)`).
- **`static <T> Predicate<T> isEqual(Object targetRef)`**: devuelve un predicado seguro contra `null` que comprueba igualdad con `Objects.equals`.

#### Ejemplos Paso a Paso (De Cero a Composición Real)
```java
// Ejemplo 1 (Básico): probar si un número es par
Predicate<Integer> isEven = n -> n % 2 == 0;
System.out.println(isEven.test(10)); // true
System.out.println(isEven.test(7));  // false

// Ejemplo 2 (Con Employee): verificar antigüedad, departamento y salario
Predicate<Employee> isEngineering = e -> "Engineering".equalsIgnoreCase(e.getDepartment());
Predicate<Employee> isTenured     = e -> e.getYearsOfExperience() >= 10;
Predicate<Employee> isExecPay     = e -> e.getSalary() >= 300_000.0;

// Ejemplo 3 (Álgebra de Predicados): combinar con .and(), .or(), .negate()
// Elegible si: (es de Ingeniería Y tiene >= 10 años) O (gana >= $300,000)
Predicate<Employee> isStaffCommitteeEligible = isEngineering.and(isTenured).or(isExecPay);

boolean aliceOk = isStaffCommitteeEligible.test(alice); // Engineering, 10 años, $185k -> true
boolean carolOk = isStaffCommitteeEligible.test(carol); // Engineering, 2 años, $120k  -> false

// Ejemplo 4 (Negación limpia con Predicate.not — Java 11+):
Predicate<Employee> isJunior = e -> e.getTitle().contains("Junior");
Predicate<Employee> isNotJunior = Predicate.not(isJunior);
// También directamente sobre una referencia a método:
Predicate<String> hasText = Predicate.not(String::isBlank);
System.out.println(hasText.test("   "));  // false
System.out.println(hasText.test("Java")); // true

// Ejemplo 5 (Cómo se conecta con Streams):
List<Employee> committee = employees.stream()
        .filter(isStaffCommitteeEligible) // Aquí entra tu Predicate!
        .toList();
```

---

### Familia 2: `Function<T, R>` — Transformar Valores (`T -> R`)

#### ¿Qué es y para qué sirve?
Una `Function<T, R>` representa una **máquina de transformación**: recibe una entrada de tipo `T` y devuelve una salida de tipo `R` (pueden ser tipos distintos, por ejemplo `Employee -> String` o `String -> Integer`, o del mismo tipo).

- **Dónde aparece en Streams:** `map()`, `flatMap()`, `Collectors.groupingBy()`, `Collectors.toMap()`, `Collectors.mapping()`, `Comparator.comparing()`.

#### API Completa de `Function<T, R>`
- **`R apply(T t)`** *(SAM)*: aplica la transformación a `t` y devuelve el resultado `R`.
- **`default <V> Function<T, V> andThen(Function<? super R, ? extends V> after)`**: ejecuta primero `this` y luego pasa el resultado a `after` → $after(this(x))$. Lectura natural **de izquierda a derecha**.
- **`default <V> Function<V, R> compose(Function<? super V, ? extends T> before)`**: ejecuta primero `before` y luego pasa ese resultado a `this` → $this(before(x))$. Orden matemático **de derecha a izquierda**.
- **`static <T> Function<T, T> identity()`**: devuelve una función que retorna exactamente su argumento sin modificarlo (`x -> x`). Muy usada en `Collectors.toMap(Employee::getId, Function.identity())`.

#### Ejemplos Paso a Paso (De Cero a Composición `andThen` vs `compose`)
```java
// Ejemplo 1 (Básico): transformar un String en su longitud (String -> Integer)
Function<String, Integer> textLength = s -> s.length();
System.out.println(textLength.apply("Java 25")); // 7

// Ejemplo 2 (Con Employee): formatear un gafete corporativo (Employee -> String)
Function<Employee, String> badgeFormatter = e ->
        "[" + e.getId() + "] " + e.getFullName() + " (" + e.getDepartment() + ")";
// Para Alice: "[1] Alice Johnson (Engineering)"

// Ejemplo 3 (Encadenamiento con andThen — izquierda a derecha):
// Paso 1: Employee -> email ("alice@google.com")
// Paso 2: email    -> dominio ("google.com")
// Paso 3: dominio  -> mayúsculas ("GOOGLE.COM")
Function<Employee, String> getEmail      = Employee::getEmail;
Function<String, String>   extractDomain = email -> email.substring(email.indexOf('@') + 1);
Function<String, String>   toUpper       = String::toUpperCase;

Function<Employee, String> employeeDomainUpper = getEmail
        .andThen(extractDomain)
        .andThen(toUpper);

String domain = employeeDomainUpper.apply(alice); // "GOOGLE.COM"

// Ejemplo 4 (Pre-procesamiento con compose — derecha a izquierda):
// f.compose(g) significa: primero ejecuta g, y al resultado aplícale f.
Function<Double, String>   formatUsd     = s -> String.format(Locale.US, "$%,.0f", s);
Function<Employee, Double> extractSalary = Employee::getSalary;

// Primero extractSalary(alice) -> 185000.0, luego formatUsd(185000.0) -> "$185,000"
Function<Employee, String> salaryLabel = formatUsd.compose(extractSalary);
System.out.println(salaryLabel.apply(alice)); // "$185,000"
```

> **Error Común de Programación (Confundir `andThen` con `compose`):**
> Traza siempre un valor concreto mentalmente:
> - Con `addBonus.andThen(applyTax)` sobre `$100,000`: primero suma `$15,000` (`$115,000`), luego aplica impuesto `* 0.75` → **`$86,250`**.
> - Con `addBonus.compose(applyTax)` sobre `$100,000`: primero aplica impuesto `* 0.75` (`$75,000`), luego suma `$15,000` → **`$90,000`**.
> ¡Mismas dos funciones, resultados financieros completamente distintos!

---

### Familia 3: `Consumer<T>` — Ejecutar Acciones / Efectos Secundarios (`T -> void`)

#### ¿Qué es y para qué sirve?
Un `Consumer<T>` recibe un elemento de tipo `T` y **no devuelve nada (`void`)**. Su propósito es **consumir** el dato para producir un efecto externo: imprimir en consola, escribir un registro de auditoría (*log*), enviar un mensaje a una cola, o actualizar una métrica.

- **Dónde aparece en Streams:** `forEach()`, `forEachOrdered()`, `peek()`, `Optional.ifPresent()`, `Iterable.forEach()`, `mapMulti()` (el segundo parámetro `Consumer<R>`).

#### API Completa de `Consumer<T>`
- **`void accept(T t)`** *(SAM)*: ejecuta la acción sobre `t`.
- **`default Consumer<T> andThen(Consumer<? super T> after)`**: encadena dos acciones en secuencia sobre el mismo elemento `t` (primero `this.accept(t)`, luego `after.accept(t)`).

#### Ejemplos Paso a Paso
```java
// Ejemplo 1 (Básico): imprimir con formato
Consumer<String> printBanner = msg -> System.out.println(">>> " + msg);
printBanner.accept("Bienvenido a Java 25"); // >>> Bienvenido a Java 25

// Ejemplo 2 (Encadenar dos acciones con andThen):
List<String> auditLog = new ArrayList<>();
Consumer<Employee> logToConsole = e -> System.out.println("Auditando: " + e.getFullName());
Consumer<Employee> saveToAudit  = e -> auditLog.add("Audit #" + e.getId() + ": " + e.getFullName());

Consumer<Employee> fullAudit = logToConsole.andThen(saveToAudit);
fullAudit.accept(alice); // Imprime en consola Y agrega a auditLog

// Ejemplo 3 (Acumulación en un Map fuera de Streams con Map.merge):
Map<String, Integer> headcountByDept = new HashMap<>();
Consumer<Employee> countDept = e -> headcountByDept.merge(e.getDepartment(), 1, Integer::sum);
employees.forEach(countDept);
```

> **Observación de Ingeniería de Software:** `Consumer<T>` es útil en bordes del sistema (logging, envío de eventos, `Iterable.forEach`), pero **dentro de un Stream evita usar `forEach` para llenar colecciones externas** (`stream.forEach(lista::add)`). En Streams, la forma correcta y segura en paralelo es recolectar con `.toList()` o `.collect(...)`.

---

### Familia 4: `Supplier<T>` — Fabricar o Proveer Valores Bajo Demanda (`() -> T`)

#### ¿Qué es y para qué sirve?
Un `Supplier<T>` es lo opuesto a `Consumer<T>`: **no recibe ningún parámetro** y **produce (devuelve) un valor de tipo `T` cada vez que llamas a `.get()`**. Su gran superpoder es la **evaluación perezosa (*lazy evaluation*)**: permite retrasar un cálculo costoso, la creación de un objeto o una consulta hasta el instante exacto en que realmente se necesita (¡o nunca ejecutarlo si no hace falta!).

- **Dónde aparece en Streams:** `Stream.generate()`, `Optional.orElseGet()`, `Collectors.toCollection()`, `Collector.supplier()`, `Gatherer.initializer()`.

#### API Completa de `Supplier<T>`
- **`T get()`** *(SAM)*: produce y devuelve una instancia de `T`.

#### Ejemplos Paso a Paso
```java
// Ejemplo 1 (Básico): proveedor de identificadores únicos o marcas de tiempo
Supplier<UUID> idGenerator = UUID::randomUUID;
UUID id1 = idGenerator.get();
UUID id2 = idGenerator.get(); // Cada llamada genera un UUID nuevo

// Ejemplo 2 (Fábrica de objeto de respaldo / fallback):
Supplier<Employee> guestFactory = () -> new Employee(
        -1, "Guest", "User", "guest@system.local", "Guest",
        "Visitor", 0, 0.0, "Unknown", "N/A", 0, "None"
);
// Hasta aquí NO se ha creado ningún objeto Employee en el heap!
Employee guest = guestFactory.get(); // Aquí se instancia bajo demanda

// Ejemplo 3 (Por qué importa la pereza en producción — logging diferido):
public void logDebug(boolean debugEnabled, Supplier<String> expensiveMessage) {
    if (debugEnabled) {
        System.out.println(expensiveMessage.get()); // Solo se construye el String si debugEnabled == true!
    }
}
// Llamada: si debugEnabled es false, serializar a JSON jamás consume CPU ni memoria:
logDebug(false, () -> "Payload completo: " + serializeHugeObject(order));
```

---

## 1.6 Variantes de Aridad y Operadores Especializados de `java.util.function`

Además de las 4 familias raíz, existen **5 interfaces derivadas** que aparecerán constantemente en pipelines de producción y entrevistas técnicas:

### 1. `UnaryOperator<T>` — Transformación del Mismo Tipo (`T -> T`)
Extiende `Function<T, T>`. Se usa cuando la entrada y la salida tienen el mismo tipo (por ejemplo, normalizar un `String`, incrementar un número o avanzar una semilla en `Stream.iterate`).
```java
final Pattern MULTI_SPACE = Pattern.compile("\\s+");
UnaryOperator<String> normalizeEmail = email ->
        MULTI_SPACE.matcher(email.trim()).replaceAll(" ").toLowerCase(Locale.ROOT);

String clean = normalizeEmail.apply("  Alice.Johnson@GOOGLE.COM  ");
// "alice.johnson@google.com"
```

### 2. `BinaryOperator<T>` — Combinar Dos Valores del Mismo Tipo (`(T, T) -> T`)
Extiende `BiFunction<T, T, T>`. Es la pieza central de `Stream.reduce()`, la función de fusión (*merge*) de `Collectors.toMap()` y el `combiner` de `Collector`/`Gatherer`. Incluye además dos fábricas estáticas muy útiles: **`BinaryOperator.maxBy(Comparator)`** y **`BinaryOperator.minBy(Comparator)`**.
```java
// Ejemplo 1: Suma de salarios
BinaryOperator<Double> addSalaries = Double::sum;
double combined = addSalaries.apply(185_000.0, 240_000.0); // 425_000.0

// Ejemplo 2: Elegir al empleado con mayor salario entre dos (versión lambda y versión maxBy)
BinaryOperator<Employee> higherEarner =
        BinaryOperator.maxBy(Comparator.comparingDouble(Employee::getSalary));
Employee winner = higherEarner.apply(alice, bob); // bob ($240,000)
```

### 3. `BiPredicate<T, U>` — Condición Sobre Dos Entradas (`(T, U) -> boolean`)
Evalúa una relación entre dos argumentos de tipos potencialmente distintos (también tiene `and`, `or` y `negate`).
```java
BiPredicate<Employee, String> knowsLanguage = (emp, lang) ->
        emp.getProgrammingLanguage().equalsIgnoreCase(lang);

boolean knowsJava = knowsLanguage.test(alice, "java"); // true
```

### 4. `BiFunction<T, U, R>` — Combinar Dos Entradas en un Resultado (`(T, U) -> R`)
Recibe dos parámetros `T` y `U` y produce `R` (incluye `andThen`). Es la firma del acumulador de `reduce` de 3 argumentos y de `Gatherers.scan`/`fold`.
```java
BiFunction<List<Employee>, String, Double> deptPayroll = (list, dept) ->
        list.stream()
            .filter(e -> e.getDepartment().equalsIgnoreCase(dept))
            .mapToDouble(Employee::getSalary)
            .sum();

double engPayroll = deptPayroll.apply(employees, "Engineering");
```

### 5. `BiConsumer<T, U>` — Acción Sobre Dos Entradas (`(T, U) -> void`)
Recibe dos valores y devuelve `void` (incluye `andThen`). Se usa en `Map.forEach((k, v) -> ...)`, `Stream.mapMulti((elem, downstream) -> ...)`, `Collector.accumulator()` y `Gatherer.finisher()`.
```java
Map<String, Double> budgets = Map.of("Engineering", 4_500_000.0, "Marketing", 1_200_000.0);
BiConsumer<String, Double> printBudget = (dept, amount) ->
        System.out.printf(Locale.US, "%-12s -> $%,.2f%n", dept, amount);
budgets.forEach(printBudget);
```

---

## 1.7 Enciclopedia Completa de las 43 Interfaces Funcionales de `java.util.function`

Como los genéricos en Java (`Predicate<Integer>`) no aceptan tipos primitivos (`int`, `long`, `double`), usar `Function<Employee, Double>` obliga a envolver cada `double` en un objeto `java.lang.Double` en el *heap* (**autoboxing**). Para eliminar esa sobrecarga por completo, el paquete [`java.util.function`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/function/package-summary.html) define exactamente **43 interfaces funcionales estándar**, organizadas en un árbol completamente simétrico y fácil de memorizar:

### Mapa Visual de las 43 Interfaces de `java.util.function`

```text
                       java.util.function (43 interfaces)
 ┌──────────────────┬──────────────────┬──────────────────┬──────────────────┬──────────────────┐
 │  1. PREDICATES   │   2. FUNCTIONS   │   3. OPERATORS   │   4. CONSUMERS   │   5. SUPPLIERS   │
 │       (5)        │       (17)       │       (8)        │       (8)        │       (5)        │
 ├──────────────────┼──────────────────┼──────────────────┼──────────────────┼──────────────────┤
 │ Predicate<T>     │ Function<T,R>    │ UnaryOperator<T> │ Consumer<T>      │ Supplier<T>      │
 │ BiPredicate<T,U> │ BiFunction<T,U,R>│ BinaryOperator<T>│ BiConsumer<T,U>  │ BooleanSupplier  │
 │ IntPredicate     │ IntFunction<R>   │ IntUnaryOperator │ IntConsumer      │ IntSupplier      │
 │ LongPredicate    │ LongFunction<R>  │ LongUnaryOperator│ LongConsumer     │ LongSupplier     │
 │ DoublePredicate  │ DoubleFunction<R>│ DoubleUnaryOper. │ DoubleConsumer   │ DoubleSupplier   │
 │                  │ ToIntFunction<T> │ IntBinaryOperator│ ObjIntConsumer<T>│                  │
 │                  │ ToLongFunction<T>│ LongBinaryOper.  │ ObjLongConsumer  │                  │
 │                  │ ToDoubleFunc.<T> │ DoubleBinaryOper.│ ObjDoubleConsumer│                  │
 │                  │ ToIntBiFunction  │                  │                  │                  │
 │                  │ ToLongBiFunction │                  │                  │                  │
 │                  │ ToDoubleBiFunc.  │                  │                  │                  │
 │                  │ IntToLongFunc.   │                  │                  │                  │
 │                  │ IntToDoubleFunc. │                  │                  │                  │
 │                  │ LongToIntFunc.   │                  │                  │                  │
 │                  │ LongToDoubleFunc.│                  │                  │                  │
 │                  │ DoubleToIntFunc. │                  │                  │                  │
 │                  │ DoubleToLongFunc.│                  │                  │                  │
 └──────────────────┴──────────────────┴──────────────────┴──────────────────┴──────────────────┘
                 Total exacto: 5 + 17 + 8 + 8 + 5 = 43 interfaces funcionales
```

*(Observa la regla de diseño del JDK: solo existen especializaciones para **`int`, `long`, `double`** —más `BooleanSupplier`— porque `byte`, `short` y `char` se promocionan a `int` y `float` a `double`, evitando una explosión combinatoria de cientos de interfaces).*

---

### Grupo 1: Los 5 Predicados (`... -> boolean`)

| # | Interfaz | Método SAM | Firma | Uso típico en Streams |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `Predicate<T>` | `test(T)` | `T -> boolean` | `Stream.filter`, `anyMatch`, `takeWhile` |
| 2 | `BiPredicate<T, U>` | `test(T, U)` | `(T, U) -> boolean` | `Files.find(path, depth, BiPredicate)` |
| 3 | `IntPredicate` | `test(int)` | `int -> boolean` | `IntStream.filter`, `IntStream.noneMatch` |
| 4 | `LongPredicate` | `test(long)` | `long -> boolean` | `LongStream.filter`, `LongStream.anyMatch` |
| 5 | `DoublePredicate` | `test(double)` | `double -> boolean` | `DoubleStream.filter`, `DoubleStream.allMatch` |

```java
IntPredicate    isEvenInt   = n -> (n & 1) == 0;
LongPredicate   isRecentMs  = ts -> ts > 1_700_000_000_000L;
DoublePredicate isValidRate = r -> r >= 0.0 && r <= 1.0;
```

---

### Grupo 2: Las 17 Funciones (`entrada(s) -> salida`)

| # | Interfaz | Método SAM | Firma | Uso típico en Streams |
| :-: | :--- | :--- | :--- | :--- |
| 6 | `Function<T, R>` | `apply(T)` | `T -> R` | `Stream.map`, `groupingBy`, `toMap` |
| 7 | `BiFunction<T, U, R>` | `apply(T, U)` | `(T, U) -> R` | `reduce` 3-arg, `Gatherers.scan`/`fold`, `Collectors.teeing` |
| 8 | `IntFunction<R>` | `apply(int)` | `int -> R` | `IntStream.mapToObj`, `Stream.toArray(Employee[]::new)` |
| 9 | `LongFunction<R>` | `apply(long)` | `long -> R` | `LongStream.mapToObj(Instant::ofEpochMilli)` |
| 10 | `DoubleFunction<R>` | `apply(double)` | `double -> R` | `DoubleStream.mapToObj(BigDecimal::valueOf)` |
| 11 | `ToIntFunction<T>` | `applyAsInt(T)` | `T -> int` | `Stream.mapToInt`, `Comparator.comparingInt`, `Collectors.summingInt` |
| 12 | `ToLongFunction<T>` | `applyAsLong(T)` | `T -> long` | `Stream.mapToLong`, `Comparator.comparingLong`, `Collectors.summingLong` |
| 13 | `ToDoubleFunction<T>` | `applyAsDouble(T)` | `T -> double` | `Stream.mapToDouble`, `Comparator.comparingDouble`, `Collectors.summingDouble` |
| 14 | `ToIntBiFunction<T, U>` | `applyAsInt(T, U)` | `(T, U) -> int` | Comparaciones o puntajes entre dos objetos sin *boxing* |
| 15 | `ToLongBiFunction<T, U>` | `applyAsLong(T, U)` | `(T, U) -> long` | Diferencias temporales en milisegundos entre dos eventos |
| 16 | `ToDoubleBiFunction<T, U>` | `applyAsDouble(T, U)` | `(T, U) -> double` | Cálculo de comisiones/totales combinando objeto + tasa sin *boxing* |
| 17 | `IntToLongFunction` | `applyAsLong(int)` | `int -> long` | `IntStream.mapToLong(i -> i * 1_000L)` |
| 18 | `IntToDoubleFunction` | `applyAsDouble(int)` | `int -> double` | `IntStream.mapToDouble(cents -> cents / 100.0)` |
| 19 | `LongToIntFunction` | `applyAsInt(long)` | `long -> int` | `LongStream.mapToInt(Math::toIntExact)` |
| 20 | `LongToDoubleFunction` | `applyAsDouble(long)` | `long -> double` | `LongStream.mapToDouble(ms -> ms / 1000.0)` |
| 21 | `DoubleToIntFunction` | `applyAsInt(double)` | `double -> int` | `DoubleStream.mapToInt(d -> (int) Math.floor(d))` |
| 22 | `DoubleToLongFunction` | `applyAsLong(double)` | `double -> long` | `DoubleStream.mapToLong(Math::round)` |

```java
// Ejemplos de funciones primitivo <-> objeto y primitivo <-> primitivo (cero boxing):
ToIntBiFunction<String, String>    combinedLength  = (a, b) -> a.length() + b.length();
ToLongBiFunction<Instant, Instant> elapsedMillis   = (start, end) -> end.toEpochMilli() - start.toEpochMilli();
ToDoubleBiFunction<Product, Double> discountedCost = (p, pct) -> p.getPrice() * (1.0 - pct);

IntToLongFunction    daysToSeconds   = days -> days * 86_400L;
IntToDoubleFunction  centsToDollars  = cents -> cents / 100.0;
LongToIntFunction    safeNarrow      = Math::toIntExact;
LongToDoubleFunction bytesToMegabyte = bytes -> bytes / (1024.0 * 1024.0);
DoubleToIntFunction  floorBucket     = d -> (int) Math.floor(d);
DoubleToLongFunction roundToCents    = amount -> Math.round(amount * 100.0);
```

---

### Grupo 3: Los 8 Operadores (`T -> T` y `(T, T) -> T`)

| # | Interfaz | Método SAM | Firma | Uso típico en Streams |
| :-: | :--- | :--- | :--- | :--- |
| 23 | `UnaryOperator<T>` | `apply(T)` | `T -> T` | `Stream.iterate`, `List.replaceAll` |
| 24 | `BinaryOperator<T>` | `apply(T, T)` | `(T, T) -> T` | `Stream.reduce`, `Collectors.toMap` merge, `Collector.combiner` |
| 25 | `IntUnaryOperator` | `applyAsInt(int)` | `int -> int` | `IntStream.map`, `IntStream.iterate`, `Arrays.setAll` |
| 26 | `LongUnaryOperator` | `applyAsLong(long)` | `long -> long` | `LongStream.map`, `LongStream.iterate` |
| 27 | `DoubleUnaryOperator` | `applyAsDouble(double)` | `double -> double` | `DoubleStream.map`, `DoubleStream.iterate` |
| 28 | `IntBinaryOperator` | `applyAsInt(int, int)` | `(int, int) -> int` | `IntStream.reduce`, `Arrays.parallelPrefix` |
| 29 | `LongBinaryOperator` | `applyAsLong(long, long)` | `(long, long) -> long` | `LongStream.reduce` (ej. factorial en `LongStream`) |
| 30 | `DoubleBinaryOperator` | `applyAsDouble(double, double)` | `(double, double) -> double` | `DoubleStream.reduce`, `Arrays.parallelPrefix` |

```java
IntUnaryOperator     squareInt    = x -> x * x;
LongUnaryOperator    nextPowerOf2 = x -> x << 1;
DoubleUnaryOperator  applyIvaMx   = price -> price * 1.16;

IntBinaryOperator    maxInt       = Integer::max;
LongBinaryOperator   multiplyLong = (a, b) -> a * b;
DoubleBinaryOperator maxDouble    = Double::max;
```

---

### Grupo 4: Los 8 Consumidores (`entrada(s) -> void`)

| # | Interfaz | Método SAM | Firma | Uso típico en Streams |
| :-: | :--- | :--- | :--- | :--- |
| 31 | `Consumer<T>` | `accept(T)` | `T -> void` | `Stream.forEach`, `peek`, `Optional.ifPresent` |
| 32 | `BiConsumer<T, U>` | `accept(T, U)` | `(T, U) -> void` | `Map.forEach`, `Stream.mapMulti`, `Collector.accumulator` |
| 33 | `IntConsumer` | `accept(int)` | `int -> void` | `IntStream.forEach`, `OptionalInt.ifPresent`, `mapMultiToInt` |
| 34 | `LongConsumer` | `accept(long)` | `long -> void` | `LongStream.forEach`, `OptionalLong.ifPresent`, `mapMultiToLong` |
| 35 | `DoubleConsumer` | `accept(double)` | `double -> void` | `DoubleStream.forEach`, `OptionalDouble.ifPresent`, `mapMultiToDouble` |
| 36 | `ObjIntConsumer<T>` | `accept(T, int)` | `(T, int) -> void` | **`IntStream.collect(supplier, ObjIntConsumer, combiner)`** |
| 37 | `ObjLongConsumer<T>` | `accept(T, long)` | `(T, long) -> void` | **`LongStream.collect(supplier, ObjLongConsumer, combiner)`** |
| 38 | `ObjDoubleConsumer<T>` | `accept(T, double)` | `(T, double) -> void` | **`DoubleStream.collect(supplier, ObjDoubleConsumer, combiner)`** |

```java
// Ejemplo clave de ObjIntConsumer / ObjLongConsumer / ObjDoubleConsumer:
// Son exactamente el acumulador del collect() de 3 argumentos en streams primitivos!
ObjIntConsumer<IntSummaryStatistics>       intAcc    = IntSummaryStatistics::accept;
ObjLongConsumer<LongSummaryStatistics>     longAcc   = LongSummaryStatistics::accept;
ObjDoubleConsumer<DoubleSummaryStatistics> doubleAcc = DoubleSummaryStatistics::accept;

// Uso directo en DoubleStream.collect sin boxing:
DoubleSummaryStatistics stats = DoubleStream.of(10.5, 20.0, 30.5)
        .collect(DoubleSummaryStatistics::new, doubleAcc, DoubleSummaryStatistics::combine);
```

---

### Grupo 5: Los 5 Proveedores (`() -> salida`)

| # | Interfaz | Método SAM | Firma | Uso típico en Streams |
| :-: | :--- | :--- | :--- | :--- |
| 39 | `Supplier<T>` | `get()` | `() -> T` | `Stream.generate`, `Optional.orElseGet`, `Collector.supplier` |
| 40 | `BooleanSupplier` | `getAsBoolean()` | `() -> boolean` | Evaluación perezosa de banderas de estado / *feature flags* |
| 41 | `IntSupplier` | `getAsInt()` | `() -> int` | `IntStream.generate`, `OptionalInt.orElseGet` |
| 42 | `LongSupplier` | `getAsLong()` | `() -> long` | `LongStream.generate`, `OptionalLong.orElseGet` |
| 43 | `DoubleSupplier` | `getAsDouble()` | `() -> double` | `DoubleStream.generate`, `OptionalDouble.orElseGet` |

```java
BooleanSupplier isFeatureOn  = () -> "true".equals(System.getenv("ENABLE_VTHREADS"));
IntSupplier     randomDie    = () -> ThreadLocalRandom.current().nextInt(1, 7);
LongSupplier    currentNanos = System::nanoTime;
DoubleSupplier  unitRandom   = () -> ThreadLocalRandom.current().nextDouble();
```

> **Tip de Rendimiento JVM / MAANG:** En la Rúbrica MAANG, el 15% de la calificación evalúa **cero *boxing* innecesario**. Con este mapa completo de las 43 interfaces, cada vez que escribas una función numérica sabrás al instante cuál especialización (`ToDoubleFunction`, `LongBinaryOperator`, `ObjIntConsumer`, `DoubleSupplier`) evita asignar basura en el *heap*.

---

## 1.8 Los Cuatro Tipos de Referencias a Métodos (`::`) — Con Casos Sutiles

Cuando una lambda no hace ningún cálculo adicional y solo delega directamente a un método o constructor existente, una **referencia a método** hace el código más limpio y expresivo. Existen exactamente **4 tipos**:

### 1. Referencia a Método Estático (`Clase::metodoEstatico`)
- **Lambda:** `s -> Double.parseDouble(s)`
- **Referencia:** `Double::parseDouble`
- **Más ejemplos:** `EmployeeDTO::fromEmployee`, `Objects::nonNull`, `Math::abs`, `Integer::sum`.

### 2. Referencia a Método de Instancia Ligada (*Bound*: `instancia::metodo`)
La instancia receptora (`this`) queda **fija (capturada)** en el momento en que declaras la referencia:
- **Lambda:** `() -> alice.getFullName()` → **Referencia:** `alice::getFullName` (`Supplier<String>`).
- **Lambda:** `city -> techHubs.contains(city)` → **Referencia:** `techHubs::contains` (`Predicate<String>` — filtro de membresía $O(1)$ muy usado en producción).

> **Error Común de Programación (`prefix::startsWith` vs `set::contains`):**
> Un error clásico al aprender referencias ligadas es intentar escribir `Predicate<String> startsWithA = "A"::startsWith;` esperando verificar si el candidato empieza con `"A"`. **¡Hace exactamente lo contrario!** Como `"A"` es la instancia ligada (el receptor `this`), `"A"::startsWith` equivale a `candidato -> "A".startsWith(candidato)`, lo cual evalúa si `"A"` comienza con `"Alice"` (`false`)!
> En cambio, `techHubs::contains` sí funciona porque `techHubs.contains(candidato)` tiene el receptor y el parámetro en el orden exacto.

### 3. Referencia a Método de Instancia No Ligada (*Unbound*: `Clase::metodoInstancia`)
Aquí **no hay una instancia fija**; el **primer parámetro** que recibe la lambda se convierte en el receptor `this`, y los parámetros restantes se pasan como argumentos al método:
- **1 parámetro:** `emp -> emp.getDepartment()` → `Employee::getDepartment` (`Function<Employee, String>`).
- **2 parámetros:** `(s1, s2) -> s1.compareToIgnoreCase(s2)` → `String::compareToIgnoreCase` (`Comparator<String>`).

### 4. Referencia a Constructor (`Clase::new`)
- **0 argumentos:** `() -> new ArrayList<>()` → `ArrayList::new` (`Supplier<List<String>>`).
- **1 argumento:** `size -> new Employee[size]` → `Employee[]::new` (`IntFunction<Employee[]>`).

---

## 1.9 Closures, Variables *Effectively Final* y Variables Anónimas `_` (Java 22–25)

### Captura de Variables y *Effectively Final*
Una lambda puede leer variables locales del método que la envuelve **solo si son `final` o *effectively final*** (nunca se reasignan después de inicializarse).

¿Por qué? Porque las variables locales viven en el *stack* del hilo actual y desaparecen al terminar el método, mientras la lambda puede ejecutarse después en otro hilo (`ForkJoinPool` o Hilos Virtuales). Java copia su valor dentro de la lambda (**captura por valor**). Si permitiera reasignar la variable local, habría dos copias desincronizadas y errores de concurrencia silenciosos.

```java
double cutoff = 150_000.0; // Effectively final: nunca se reasigna
Predicate<Employee> aboveCutoff = e -> e.getSalary() > cutoff; // Válido y seguro

// Si agregaras abajo: cutoff = 200_000.0; -> Error de compilación inmediato!
```

### Variables Anónimas `_` en Java 22–25 ([JEP 456](https://openjdk.org/jeps/456))
Desde Java 22 (estándar en Java 25 LTS), usa el guion bajo **`_`** para cualquier parámetro de lambda, variable local o excepción `catch` que la sintaxis exija pero tu código ignore:

```java
// En funciones de fusión de Collectors.toMap:
Map<String, Employee> firstPerCity = employees.stream()
        .collect(Collectors.toMap(Employee::getCity, Function.identity(), (existing, _) -> existing));

// En lambdas que ignoran uno o varios parámetros:
Predicate<Employee> matchAll = _ -> true;
BiConsumer<String, Integer> notifyOnly = (_, _) -> System.out.println("Ping recibido");
```

---

## 1.10 Patrones Avanzados de Nivel Staff: Comparadores, Currying y Motores de Reglas

### 1. Composición Declarativa de `Comparator<T>`
```java
Comparator<Employee> maangOrder = Comparator
        .comparing(Employee::getDepartment)                                        // 1. Depto ASC
        .thenComparing(Comparator.comparingDouble(Employee::getSalary).reversed()) // 2. Salario DESC (sin boxing)
        .thenComparingInt(Employee::getAge)                                        // 3. Edad ASC (sin boxing)
        .thenComparing(Employee::getLastName);                                     // 4. Apellido ASC
```

### 2. Currying (`(A, B) -> C` convertido en `A -> (B -> C)`)
El *currying* descompone una función de múltiples argumentos en una cadena de funciones de un solo argumento, permitiendo **aplicación parcial** (fijar la configuración una vez y reutilizar la función especializada):

```java
// Función currificada: recibe prefijo -> devuelve función que formatea salarios
Function<String, Function<Double, String>> curriedCurrency =
        prefix -> amount -> prefix + String.format(Locale.US, "$%,.2f", amount);

// Aplicación parcial: creamos formateadores especializados reutilizables
Function<Double, String> usdFormatter = curriedCurrency.apply("USD: ");
Function<Double, String> mxnFormatter = curriedCurrency.apply("MXN: ");

System.out.println(usdFormatter.apply(185_000.0)); // "USD: $185,000.00"
System.out.println(mxnFormatter.apply(185_000.0)); // "MXN: $185,000.00"
```

### 3. Motor de Reglas Declarativo (Combinando `Predicate` + `Consumer` + `record`)
En producción, en lugar de `if / else if` gigantes imposibles de mantener, las reglas de negocio se modelan como datos inmutables:

```java
record BusinessRule<T>(Predicate<T> condition, Consumer<T> action) {}

List<String> notifications = new ArrayList<>();
List<BusinessRule<Employee>> rules = List.of(
        new BusinessRule<>(
                e -> e.getSalary() > 300_000.0,
                e -> notifications.add("[BONO EJECUTIVO] " + e.getFullName())),
        new BusinessRule<>(
                e -> e.getYearsOfExperience() >= 20,
                e -> notifications.add("[RECONOCIMIENTO VETERANO] " + e.getFullName()))
);

// Motor ejecutor reutilizable:
Consumer<Employee> engine = emp -> rules.forEach(rule -> {
    if (rule.condition().test(emp)) {
        rule.action().accept(emp);
    }
});
```

---

## 1.11 Retos de Dominio y Autoevaluación (Capítulo 1)

Pon a prueba tu comprensión antes de pasar al Capítulo 2:

1. **Nivel Inicial:** Escribe una `Function<String, String>` que elimine espacios sobrantes, convierta a minúsculas y extraiga el dominio de un correo electrónico encadenando tres funciones simples con `.andThen()`.
2. **Nivel Intermedio:** Construye una `@FunctionalInterface` llamada `TriFunction<A, B, C, R>` con un método `default andThen(...)` y úsala para calcular el precio final de un producto a partir de `(precioBase, porcentajeDescuento, tasaImpuesto)`.
3. **Nivel Avanzado:** Implementa un `Comparator<Employee>` de 4 niveles sin *boxing* (departamento ASC, salario DESC, edad ASC, apellido ASC) y explica por qué `"A"::startsWith` no sirve para probar si una cadena empieza con `"A"`.
4. **Nivel Staff MAANG:** Diseña un motor de reglas declarativo usando `record Rule<T>(Predicate<T> when, Consumer<T> then)` y una función currificada `Function<String, Function<Double, String>>` para formatear montos en múltiples divisas.

---

## Resumen Ejecutivo del Capítulo 1

| Pieza Clave | Firma / Regla | Cuándo la Usarás en Streams |
|---|---|---|
| `Predicate<T>` | `T -> boolean` (`test`, `and`, `or`, `negate`, `not`) | `filter`, `takeWhile`, `dropWhile`, `*Match`, `partitioningBy` |
| `Function<T, R>` | `T -> R` (`apply`, `andThen`, `compose`, `identity`) | `map`, `flatMap`, `groupingBy`, `toMap`, `comparing` |
| `Consumer<T>` | `T -> void` (`accept`, `andThen`) | `forEach`, `peek`, `mapMulti`, `ifPresent` |
| `Supplier<T>` | `() -> T` (`get`) | `generate`, `orElseGet`, `toCollection`, `Gatherer.initializer` |
| `UnaryOperator<T>` / `BinaryOperator<T>` | `T -> T` / `(T, T) -> T` (`maxBy`, `minBy`) | `iterate`, `reduce`, `toMap` merge, `combiner` |
| `BiPredicate` / `BiFunction` / `BiConsumer` | 2 entradas → `boolean` / `R` / `void` | `reduce` 3-arg, `scan`/`fold`, `mapMulti`, `Collector.accumulator` |
| `invokedynamic` + `_` ([JEP 456](https://openjdk.org/jeps/456)) | Lambdas sin `.class` extra; `_` para parámetros ignorados | Código limpio y de bajo overhead en Java 25 LTS |
