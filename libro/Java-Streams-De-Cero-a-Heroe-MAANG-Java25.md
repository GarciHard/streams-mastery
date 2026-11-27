# Java Streams: De Cero a Héroe MAANG — Edición Java 25 LTS
## Tratado Integral de Programación Funcional, Arquitectura Interna de la JVM y Patrones de Ingeniería de Alto Rendimiento

> **Plataforma objetivo:** [Java 25 LTS (JDK 25, JSR 400)](https://openjdk.org/projects/jdk/25/) — Lanzamiento GA del 16 de septiembre de 2025  
> **Estructura de la obra:** 18 Capítulos Enciclopédicos · Solucionario Maestro y Caso de Estudio Capstone · Apéndice de Referencia MAANG  
> **Estándar técnico:** De Cero Absoluto a Nivel L6+ Staff / Principal Engineer · Idioma: Español (México)

---

## Prólogo: Por Qué Existe Este Libro

La mayoría de los materiales sobre Java Streams se limitan a mostrar tres ejemplos triviales de `filter`, `map` y `collect` y dan por terminado el tema. Sin embargo, cuando pasas de un tutorial básico a un sistema en producción con millones de eventos por segundo —o te sientas en una entrevista técnica de nivel Senior, Staff o Principal en una empresa MAANG— descubres rápidamente que **saber la sintaxis no alcanza**.

Un ingeniero de élite debe responder con absoluta soltura preguntas como:
* ¿Qué diferencia hay a nivel de bytecode (`invokedynamic`) y memoria entre una lambda sin estado y una clase anónima interna?
* ¿Por qué un `Stream<Integer>` puede degradar el rendimiento en un factor de 5× frente a un `IntStream`, y cómo cambia el panorama con **Compact Object Headers ([JEP 519](https://openjdk.org/jeps/519))** en Java 25?
* ¿Por qué `DoubleStream.sum()` utiliza suma compensada de Kahan y es numéricamente más preciso que `reduce(0.0, Double::sum)`?
* ¿Cuándo una operación intermedia como `sorted()` o `distinct()` introduce una barrera con estado de memoria $O(N)$ que destruye la evaluación perezosa?
* ¿Por qué `mapMulti()` puede ser hasta 4× más rápido que `flatMap()` en rutas críticas?
* ¿Cómo elimina `collection.reversed().stream()` (**[JEP 431](https://openjdk.org/jeps/431)**) el costo $O(N \log N)$ de invertir colecciones ordenadas?
* ¿Cómo revolucionan los **Stream Gatherers ([JEP 485](https://openjdk.org/jeps/485))** la arquitectura de Streams en Java 24/25 al permitir operaciones intermedias personalizadas con estado, cortocircuito y concurrencia acotada sobre **Hilos Virtuales ([JEP 444](https://openjdk.org/jeps/444) y [JEP 491](https://openjdk.org/jeps/491))**?
* ¿Cómo manejar *Checked Exceptions* sin romper la elegancia funcional y cuándo un bucle `for` clásico supera a cualquier Stream?

Este tratado está diseñado bajo la filosofía pedagógica de los grandes manuales de referencia (*Deitel & Deitel* y *Effective Java*): **no asume conocimiento previo alguno sobre programación funcional ni Streams**, explica cada pieza desde sus cimientos, incluye pruebas de escritorio paso a paso, anatomía interna de la JVM, ejemplos 100% autocontenidos en Java 25 LTS y retos progresivos al cierre de cada capítulo.

---

## Convenciones Didácticas del Libro

A lo largo de los 18 capítulos encontrarás tres tipos de recuadros clave:

> **Observación de Ingeniería de Software:** Principios de diseño, mantenibilidad, legibilidad y arquitectura limpia en equipos de producción.

> **Tip de Rendimiento JVM / MAANG:** Optimizaciones concretas de memoria, localidad de caché L1/L2, eliminación de *boxing*, cortocircuito y complejidad asintótica.

> **Error Común de Programación:** Trampas reales que provocan bugs silenciosos, `NullPointerException`, `IllegalStateException`, condiciones de carrera o rechazos inmediatos en entrevistas.

---

## Los 4 Modelos Mentales Fundamentales de Java 25 Streams

Antes de escribir una sola línea de código, internaliza estos cuatro modelos mentales:

```text
+---------------------------------------------------------------------------------------------+
|                        EL PIPELINE MODERNO DE JAVA 25 LTS                                   |
|                                                                                             |
|  [ FUENTE ]        [ SIN ESTADO ]     [ GATHERER (JEP 485) ]     [ TERMINAL / COLLECTOR ]   |
|  List / Array  --> filter / map  -->  windowFixed / scan    -->  toList() / collect()       |
|  reversed()        mapMulti           mapConcurrent (VThreads)   reduce() / anyMatch()      |
|                                                                                             |
|  1. Banda transportadora (Lazy): cero ciclos de CPU hasta invocar la operación terminal.    |
|  2. Fusión de bucles (Loop Fusion): flujo vertical elemento por elemento (depth-first).     |
|  3. Trinidad de transformación: map (1:1), flatMap (1:0..N stream), mapMulti (1:0..N push). |
|  4. Extensibilidad total: Collector<T,A,R> (terminal) + Gatherer<T,A,R> (intermedio).       |
+---------------------------------------------------------------------------------------------+
```

---

## Evolución Completa de Java Streams: De Java 8 a Java 25 LTS

| Versión | Hitos Clave en Streams y Lenguaje Funcional | JEP / Especificación |
| :--- | :--- | :--- |
| **Java 8 (LTS)** | Nacimiento de Lambdas (`invokedynamic`), `java.util.function`, `Stream`, `IntStream`/`LongStream`/`DoubleStream`, `Optional`, `Collectors`, `Spliterator`, `ForkJoinPool`. | JSR 335 |
| **Java 9** | `takeWhile`, `dropWhile`, `Stream.ofNullable`, `Stream.iterate` de 3 argumentos, `Optional.stream()`, `Optional.or()`, `Optional.ifPresentOrElse()`, `Collectors.filtering()`, `Collectors.flatMapping()`, `Matcher.results()`, optimización `SIZED` de `count()`. | JEP 213 / Core Libs |
| **Java 10** | `var`, `Collectors.toUnmodifiableList()`, `toUnmodifiableSet()`, `toUnmodifiableMap()`. | JEP 286 |
| **Java 11 (LTS)** | `Predicate.not()`, `String.lines()`. | Core Libs |
| **Java 12** | `Collectors.teeing(c1, c2, merger)` para agregaciones duales en una sola pasada. | Core Libs |
| **Java 16** | `Stream.toList()` (inmodificable, acepta nulos), `Stream.mapMulti()` y variantes primitivas, `record` finalizados. | JEP 395 / Core Libs |
| **Java 17 (LTS)** | `sealed` classes e interfaces (ideales para `Result<T, E>` y jerarquías exhaustivas de eventos). | JEP 409 |
| **Java 21 (LTS)** | **Sequenced Collections** (`reversed().stream()`, `getFirst()`, `getLast()`), **Virtual Threads**, **Record Patterns** y **Pattern Matching para `switch`**. | [JEP 431](https://openjdk.org/jeps/431), [JEP 444](https://openjdk.org/jeps/444) |
| **Java 22** | **Unnamed Variables & Patterns (`_`)**: limpia lambdas con parámetros ignorados como `(existing, _) -> existing` y patrones de `record`. | [JEP 456](https://openjdk.org/jeps/456) |
| **Java 24** | **Stream Gatherers** finalizados (`Stream.gather`, `Gatherer`, `Gatherers`), **Hilos Virtuales sin *pinning*** en bloques `synchronized`. | [JEP 485](https://openjdk.org/jeps/485), [JEP 491](https://openjdk.org/jeps/491) |
| **Java 25 (LTS)** | Consolida todo lo anterior en la nueva base LTS, añade **Scoped Values** finalizados, **Compact Object Headers** (`-XX:+UseCompactObjectHeaders`) y mejoras quirúrgicas de **Java Flight Recorder**. | [JDK 25](https://openjdk.org/projects/jdk/25/), [JEP 506](https://openjdk.org/jeps/506), [JEP 519](https://openjdk.org/jeps/519) |

---

## Modelos de Dominio Autocontenidos del Libro

Para que todos los ejemplos de este tratado sean realistas —simulando sistemas corporativos de recursos humanos, comercio electrónico y banca— utilizaremos seis entidades de dominio estándar. Puedes copiar sus contratos en cualquier proyecto Java 25 o sesión de `jshell` para ejecutar cada ejemplo por tu cuenta:

| Entidad | Campos y Getters Principales | Notas de Contrato |
| :--- | :--- | :--- |
| **`Employee`** | `id`, `firstName`, `lastName`, `email`, `department`, `title`, `age`, `salary`, `city`, `gender`, `yearsOfExperience`, `programmingLanguage` | `getFullName()` retorna `firstName + " " + lastName`. Igualdad por `id`. No implementa `Comparable`. |
| **`EmployeeDTO`** | `id`, `fullName`, `department`, `salary` | Fábrica estática `EmployeeDTO.fromEmployee(Employee e)` para proyecciones limpias. |
| **`Product`** | `id`, `name`, `category`, `price`, `stock`, `rating`, `brand` | Igualdad por `id`. No implementa `Comparable`. |
| **`Order`** | `id`, `customerId`, `customerName`, `status` (`"PENDING"`, `"SHIPPED"`, `"DELIVERED"`, `"CANCELLED"`), `orderDate` (`LocalDate`), `items` (`List<OrderItem>`) | `getTotalPrice()` suma `price * quantity` con `mapToDouble().sum()`. |
| **`OrderItem`** | `productName`, `category`, `price`, `quantity` | `getSubtotal()` retorna `price * quantity`. |
| **`Transaction`** | `id`, `accountId`, `type` (`Transaction.Type.CREDIT` / `DEBIT`), `amount`, `currency`, `category`, `date` (`LocalDate`), `merchant` | Enum interno `Transaction.Type`. Igualdad por `id`. |

---

## Estándar de Revisión de Código MAANG (Rúbrica de Producción)

Cada pipeline de Streams que escribas en entrevistas o en producción debe pasar cinco filtros de calidad:

| Criterio | Peso | Qué Provoca Penalización o Rechazo |
| :--- | ---: | :--- |
| **1. Corrección y casos borde** | 40% | Fallos ante streams vacíos (*vacuous truth*), valores `null`, claves duplicadas o errores de redondeo. |
| **2. Arquitectura idiomática Java 25** | 25% | Bucles imperativos disfrazados en `forEach`, mutación externa, omitir referencias a métodos o idiomas modernos (`reversed().stream()`, `_`, `Gatherers`). |
| **3. Cero *boxing* innecesario** | 15% | Usar `Stream<Integer>`/`Stream<Double>` o `.map().reduce()` donde corresponde `IntStream`, `mapToDouble().sum()` o `mapMultiToInt()`. |
| **4. Cortocircuito y eficiencia** | 10% | `sorted().findFirst()` en vez de `min()`, `filter().count() > 0` en vez de `anyMatch()`, o `sorted()` antes de `filter()`. |
| **5. Seguridad de hilos e inmutabilidad** | 10% | Estado compartido mutable en paralelo, efectos secundarios en lambdas o interferencia sobre la colección fuente. |

---

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

---

# Capítulo 2: Creación, Anatomía de Streams, `BaseStream`, `Spliterator` y Fuentes Completas del JDK 25

> *"Un Stream no es una caja donde guardas datos; es una línea de ensamblaje que transforma datos bajo demanda conforme pasan por ella."*

Si vienes del mundo de los bucles `for` y las colecciones clásicas (`ArrayList`, `HashSet`, `HashMap`), este capítulo construirá desde cero el modelo mental exacto de qué es un `Stream`, su jerarquía completa de tipos en el JDK, cómo funciona una prueba de escritorio paso a paso de su evaluación perezosa, y **absolutamente todas las formas de crear un Stream en Java 25 LTS** —incluyendo archivos (`Files.lines`, `list`, `walk`, `find`), expresiones regulares (`Matcher.results()`), Unicode real (`codePoints()`), generadores aleatorios (`SplittableRandom`), `BitSet`, `StackWalker`, `StreamSupport` y las **Colecciones Secuenciadas** de Java 21–25.

---

## 2.1 ¿Qué Es (y Qué NO Es) un `Stream<T>`?

Antes de escribir código, aclaremos la confusión número uno de todo principiante:

| Dimensión | Colección (`List`, `Set`, `Map`) | `Stream<T>` (`java.util.stream`) |
| :--- | :--- | :--- |
| **Propósito principal** | **Almacenar** y organizar datos en memoria. | **Procesar y transformar** datos de forma declarativa. |
| **Evaluación** | **Ansiosa (*eager*):** todos los elementos ya existen en memoria. | **Perezosa (*lazy*):** los elementos se calculan uno por uno solo cuando los pide una operación terminal. |
| **Tamaño** | Siempre **finito** (limitado por la RAM). | Puede ser **finito o infinito** (ej. secuencia de Fibonacci, flujos de sensores). |
| **Reutilización** | Puedes recorrer un `ArrayList` mil veces con `for`. | **Consumo único (*single-use*):** se recorre una sola vez; volver a usarlo lanza `IllegalStateException`. |
| **Iteración** | **Externa:** tú controlas el bucle `for` / `Iterator`. | **Interna:** la librería controla el recorrido (habilitando *loop fusion* y paralelismo automático). |
| **Mutación** | Puedes agregar, borrar o reemplazar elementos. | **No modifica la fuente original**; produce un nuevo resultado. |

### Jerarquía Oficial de Interfaces de Streams en el JDK 25

Todo Stream en Java hereda de `AutoCloseable` y de `BaseStream<T, S>` (por eso todos los Streams soportan `try-with-resources`, `.parallel()`, `.sequential()`, `.unordered()`, `.onClose()` y `.spliterator()`):

```text
                       java.lang.AutoCloseable
                                 ▲
                                 │
              java.util.stream.BaseStream<T, S extends BaseStream<T, S>>
                 (iterator, spliterator, isParallel, sequential,
                  parallel, unordered, onClose, close)
               ┌────────────┬────────────┬────────────┐
               │            │            │            │
         Stream<T>      IntStream    LongStream  DoubleStream
        (objetos)       (int 32b)    (long 64b)  (double 64b)
```

### Las Tres Partes Obligatorias de Todo Pipeline y Prueba de Escritorio

```text
  [ 1. FUENTE ]           [ 2. OPERACIONES INTERMEDIAS (0 o más) ]        [ 3. OPERACIÓN TERMINAL (Exactamente 1) ]
  Lista / Arreglo    -->  .filter(...) -> .map(...) -> .gather(...)  -->  .toList() / .count() / .sum() / .findFirst()
  (Crea el Stream)        (Devuelven otro Stream; son PEREZOSAS)          (Dispara la ejecución y CIERRA el Stream)
```

#### Prueba de escritorio paso a paso (¿Qué ocurre en cada nanosegundo?)
Supón que ejecutas:
```java
List<String> res = List.of("ana", "luis", "sofia", "pedro").stream()
        .filter(s -> s.length() >= 4)
        .map(String::toUpperCase)
        .limit(2)
        .toList();
```

| Paso | Elemento jalado de la fuente | `filter(len >= 4)` | `map(toUpperCase)` | `limit(2)` (acumulados) | Acción del motor |
| :---: | :---: | :---: | :---: | :---: | :--- |
| 1 | `"ana"` (len 3) | `false` (descartado) | *(no se ejecuta)* | 0 | Pide el siguiente elemento |
| 2 | `"luis"` (len 4) | `true` (pasa) | `"LUIS"` | 1 (`["LUIS"]`) | Pide el siguiente elemento |
| 3 | `"sofia"` (len 5) | `true` (pasa) | `"SOFIA"` | **2** (`["LUIS", "SOFIA"]`) | **¡CORTOCIRCUITO!** Cierra el pipeline |
| 4 | `"pedro"` | *(jamás se lee)* | *(jamás se ejecuta)* | — | Retorna `["LUIS", "SOFIA"]` |

* **Regla 1 (Sin terminal no pasa nada):** Si escribes `employees.stream().filter(...)` sin operación terminal, **la JVM no evalúa el predicado ni una sola vez**.
* **Regla 2 (Consumo único):** Una vez invocada la operación terminal, reutilizar el mismo `Stream` lanza `IllegalStateException: stream has already been operated upon or closed`. Si necesitas reutilizar la receta, envuélvela en un `Supplier<Stream<T>>`:
  ```java
  Supplier<Stream<String>> techSupplier = () -> Stream.of("Java", "Go", "Rust");
  long total = techSupplier.get().count();         // Stream fresco #1
  List<String> list = techSupplier.get().toList(); // Stream fresco #2
  ```

---

## 2.2 Guía Exhaustiva: Todas las Formas de Crear un Stream en Java 25 LTS

Dominar cada fábrica del JDK te evita copias innecesarias en memoria, fugas de archivos abiertos, *NullPointerException* y *boxing* accidental.

### 1. Desde Cualquier `Collection` (`List`, `Set`, `Queue`, `Deque`)
Toda clase que implementa `java.util.Collection<E>` ofrece `.stream()` (secuencial) y `.parallelStream()` (paralelo):
```java
List<String> frameworks = List.of("Spring", "Quarkus", "Micronaut", "Vert.x");
long count = frameworks.stream().count(); // 4
```

### 2. Desde Arreglos (`Arrays.stream`) y Cortes *Zero-Copy*
`Arrays.stream(...)` está sobrecargado para `T[]`, `int[]`, `long[]` y `double[]`. Además acepta un rango semiabierto `[startInclusive, endExclusive)` **sin copiar el sub-arreglo en memoria**:
```java
int[] timestamps = {100, 200, 300, 400, 500, 600, 700};
int totalSum = Arrays.stream(timestamps).sum();            // IntStream (cero boxing)
int[] middleSlice = Arrays.stream(timestamps, 2, 5).toArray(); // [300, 400, 500]
```

> **Error Común de Programación (`Stream.of(int[])`):**
> Nunca pases un arreglo primitivo (`int[]`, `long[]`, `double[]`) a `Stream.of(...)`. Como los genéricos no soportan primitivos, `Stream.of(new int[]{10, 20, 30})` crea un **`Stream<int[]>` de longitud 1** (cuyo único elemento es el arreglo entero), ¡no un stream de 3 números! Usa siempre `Arrays.stream(intArray)` o `IntStream.of(10, 20, 30)`.

### 3. Fábricas Estáticas: `Stream.of`, `Stream.empty`, `Stream.ofNullable`
```java
Stream<String> maang = Stream.of("Meta", "Amazon", "Apple", "Netflix", "Google");
Stream<Employee> noResults = Stream.empty();
Stream<String> safe = Stream.ofNullable(getConfigOrNull()); // 0 elementos si es null, 1 si no lo es
```

### 4. `Stream.builder()`: Construcción Imperativa Condicional
```java
List<String> clouds = Stream.<String>builder() // Type witness <String> obligatorio al encadenar
        .add("AWS")
        .add("GCP")
        .add("Azure")
        .build()
        .toList();
```
*(Una vez que invocas `.build()`, volver a llamar `.add()` lanza `IllegalStateException`).*

### 5. Streams Infinitos con `Stream.generate(Supplier<T>)`
Produce un stream **infinito y desordenado** invocando `supplier.get()` bajo demanda. Acótalo siempre con `.limit(n)`, `.takeWhile(...)` o `.findFirst()`:
```java
List<UUID> ids = Stream.generate(UUID::randomUUID).limit(3).toList();
```

### 6. Secuencias Ordenadas con `Stream.iterate()` (Sobrecargas de 2 y 3 Argumentos)
* **2 argumentos (Java 8 — Infinito, requiere `limit` o `takeWhile`):**
  ```java
  // Primeros 10 números de Fibonacci [0, 1, 1, 2, 3, 5, 8, 13, 21, 34]
  List<Integer> fibonacci = Stream.iterate(
                  new int[]{0, 1},
                  pair -> new int[]{pair[1], pair[0] + pair[1]})
          .limit(10)
          .map(pair -> pair[0]) // pair[0] empieza en 0; pair[1] empezaría en 1
          .toList();
  ```
* **3 argumentos (Java 9+ — Finito, equivalente exacto al bucle `for (seed; hasNext; next)`):**
  ```java
  // Potencias de 2 menores a 100 -> [1, 2, 4, 8, 16, 32, 64]
  List<Integer> powers = Stream.iterate(1, n -> n < 100, n -> n * 2).toList();

  // Conjetura de Collatz desde 6 hasta 1 (incluyendo el 1 final usando 0L como centinela):
  List<Long> collatz = Stream.iterate(
          6L,
          n -> n > 0L,
          n -> (n == 1L) ? 0L : (n % 2L == 0L ? n / 2L : 3L * n + 1L)
  ).toList(); // [6, 3, 10, 5, 16, 8, 4, 2, 1]
  ```

### 7. Rangos Numéricos Primitivos: `range()` vs `rangeClosed()`
Disponibles en `IntStream` y `LongStream`:
```java
int sumOfSquares = IntStream.rangeClosed(1, 10).map(n -> n * n).sum();           // 385
int[] bitmasks   = IntStream.rangeClosed(0, 8).map(shift -> 1 << shift).toArray(); // [1..256]
long[] offsets   = LongStream.rangeClosed(0, 4).map(i -> i * 60L).toArray();
```

### 8. Cadenas de Texto: `chars()` vs `codePoints()` (Unicode Real) y `String.lines()`
Un error muy frecuente en aplicaciones internacionales es asumir que un carácter siempre cabe en 1 `char` (16 bits UTF-16). Los emojis y muchos ideogramas ocupan **2 `char`** (un par sustituto):

```java
String rocket = "🚀";
long utf16Units = rocket.chars().count();      // ¡2! (par sustituto UTF-16 roto si lo partes)
long realChars  = rocket.codePoints().count(); // ¡1! (punto de código Unicode real)

// Convertir cada punto de código real de vuelta a String seguro:
List<String> graphemes = "Java🚀25".codePoints()
        .mapToObj(Character::toString) // Java 11+: convierte int codePoint a String completo
        .toList(); // ["J", "a", "v", "a", "🚀", "2", "5"]

// Java 11+: String.lines() divide líneas manejando \n, \r y \r\n de forma perezosa
List<String> nonBlankLines = "uno\r\n\ndos\ntres".lines()
        .filter(Predicate.not(String::isBlank))
        .toList(); // ["uno", "dos", "tres"]
```

### 9. Expresiones Regulares: `Pattern.splitAsStream()` y `Matcher.results()` (Java 9+)
* **`Pattern.splitAsStream(CharSequence)`**: divide sin crear un arreglo `String[]` temporal.
* **`Matcher.results()` (Java 9+)**: devuelve un **`Stream<MatchResult>`** con todas las coincidencias y sus **grupos de captura**, eliminando para siempre los bucles `while (matcher.find())`:

```java
// Extraer pares clave=valor de un log estructurado usando Matcher.results()
String log = "user=alice latency_ms=42 region=mx-central status=200";
Pattern kvPattern = Pattern.compile("(\\w+)=([\\w-]+)");

Map<String, String> parsedLog = kvPattern.matcher(log).results()
        .collect(Collectors.toMap(
                m -> m.group(1),
                m -> m.group(2),
                (_, latest) -> latest,
                LinkedHashMap::new
        ));
// {user=alice, latency_ms=42, region=mx-central, status=200}
```

### 10. Archivos y Sistema de Archivos: `java.nio.file.Files` y `BufferedReader.lines()`
Toda la familia de `Files` devuelve Streams **respaldados por descriptores de archivo del sistema operativo**. **Regla absoluta de producción: envuélvelos SIEMPRE en `try-with-resources`** para evitar fugas de *file descriptors* (`Too many open files`):

| Método de `Files` | Qué produce | Caso de uso real |
| :--- | :--- | :--- |
| **`Files.lines(Path, Charset)`** | `Stream<String>` (línea por línea, perezoso) | Procesar logs o CSVs de 50 GB sin cargarlos en RAM. |
| **`Files.list(Path)`** | `Stream<Path>` (solo 1 nivel, no recursivo) | Listar archivos directos de un directorio. |
| **`Files.walk(Path, maxDepth)`** | `Stream<Path>` (recursivo *depth-first*) | Buscar todos los `.java` o calcular peso de un árbol de carpetas. |
| **`Files.find(Path, maxDepth, BiPredicate)`** | `Stream<Path>` filtrando por `BasicFileAttributes` | Encontrar archivos mayores a 10 MB sin doble llamada `stat()` al SO. |

```java
// Ejemplo real: encontrar los 5 archivos .java más grandes en un proyecto (try-with-resources obligatorio)
Path root = Path.of("src");
try (Stream<Path> paths = Files.find(root, 10,
        (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".java"))) {
    List<Path> largestJavaFiles = paths
            .sorted(Comparator.comparingLong((Path p) -> p.toFile().length()).reversed())
            .limit(5)
            .toList();
}
```

### 11. Generadores Aleatorios Primitivos: `Random`, `ThreadLocalRandom` y `SplittableRandom`
En lugar de `Stream.generate(() -> rng.nextInt(100))` (que hace *autoboxing* a `Integer`), el JDK provee Streams primitivos directos:
```java
// 1. Secuencial determinista (reproducible en tests con semilla):
int[] sample = new Random(42).ints(5, 1, 101).toArray(); // 5 enteros en [1, 100]

// 2. Alto rendimiento y paralelizable (sin contención CAS en la semilla):
double[] monteCarlo = new SplittableRandom().doubles(1_000_000, 0.0, 1.0).parallel().toArray();
```

### 12. Otras Fuentes del JDK: `Map`, `BitSet`, `Scanner` y `StackWalker`
```java
// 1. Streaming sobre Map (Map no tiene .stream() directo)
List<String> highStocks = stocks.entrySet().stream()
        .filter(e -> e.getValue() > 180.0).map(Map.Entry::getKey).sorted().toList();

// 2. BitSet.stream() -> IntStream con los índices de los bits encendidos (1)
BitSet flags = new BitSet();
flags.set(2); flags.set(7); flags.set(15);
int[] activeBits = flags.stream().toArray(); // [2, 7, 15]

// 3. Scanner.tokens() (Java 9+): tokenización perezosa de entrada estándar o texto
try (Scanner sc = new Scanner("10 20 30")) {
    int sum = sc.tokens().mapToInt(Integer::parseInt).sum(); // 60
}

// 4. StackWalker.walk(Function<Stream<StackFrame>, T>) (Java 9+): inspeccionar el stack sin Exception
List<String> top3Frames = StackWalker.getInstance()
        .walk(frames -> frames.limit(3).map(StackWalker.StackFrame::getMethodName).toList());
```

### 13. Colecciones Secuenciadas ([JEP 431](https://openjdk.org/jeps/431)), `Stream.concat()` y `StreamSupport`
```java
// 1. Vista inversa en O(1) tiempo y O(1) memoria (Java 21–25, JEP 431):
List<String> reverseStages = List.of("Extract", "Transform", "Load").reversed().stream().toList();

// 2. Concatenar 2 streams (para K >= 3 usa Stream.of(s1, s2, s3).flatMap(Function.identity())):
List<String> combined = Stream.concat(list1.stream(), list2.stream()).toList();

// 3. Puente oficial desde cualquier Iterator/Spliterator personalizado:
Spliterator<Integer> customSpliterator = Spliterators.spliteratorUnknownSize(
        myIterator, Spliterator.ORDERED | Spliterator.NONNULL);
List<Integer> firstFive = StreamSupport.stream(customSpliterator, false).limit(5).toList();
```

---

## 2.3 El Motor Oculto: Características del `Spliterator<T>`

| Bandera | Significado | Ejemplos de Fuente | Optimización que Habilita |
| :--- | :--- | :--- | :--- |
| `ORDERED` | Orden de encuentro definido | `List`, arrays, `IntStream.range` | Garantiza orden en `findFirst`, `limit`, `skip`, `gather` |
| `DISTINCT` | Elementos únicos por `equals` | `HashSet`, `TreeSet`, `LinkedHashSet` | Convierte `.distinct()` posterior en *no-op* |
| `SORTED` | Ya ordenado por comparador/natural | `TreeSet`, `TreeMap.keySet()` | Convierte `.sorted()` natural posterior en *no-op* |
| `SIZED` | Tamaño exacto conocido de antemano | `ArrayList`, arrays, `IntStream.range` | Pre-dimensiona arreglos y resuelve `.count()` en **$O(1)$** |
| `SUBSIZED` | Todos los sub-splits son `SIZED` | `ArrayList`, arrays | División paralela perfecta $O(1)$ |
| `NONNULL` | Garantiza ausencia de `null` | `ConcurrentHashMap.keySet()` | Evita comprobaciones de nulidad innecesarias |
| `IMMUTABLE` | Fuente inmutable durante recorrido | `List.of(...)`, `IntStream.range` | Recorrido seguro sin copias defensivas |
| `CONCURRENT` | Segura ante modificación concurrente | `ConcurrentHashMap` | Sin `ConcurrentModificationException` |

---

## 2.4 Retos de Dominio y Autoevaluación (Capítulo 2)

1. **Nivel Inicial:** Genera los primeros 15 términos de Fibonacci con `Stream.iterate` asegurándote de incluir el `0` inicial (`pair[0]`) y explica por qué reutilizar el mismo `Stream` lanza `IllegalStateException`.
2. **Nivel Intermedio:** Usa `Stream.iterate` de 3 argumentos para generar la secuencia de Collatz completa desde `n = 27` hasta `1` (incluyendo el `1` final), y utiliza `Matcher.results()` para extraer todas las fechas `YYYY-MM-DD` de un archivo de texto.
3. **Nivel Avanzado:** Escribe un bloque `try-with-resources` con `Files.find()` que localice los 10 archivos `.log` más grandes dentro de un directorio sin fugar *file descriptors*.
4. **Nivel Staff MAANG:** Construye un `Spliterator` personalizado con `Spliterators.spliteratorUnknownSize` y `StreamSupport.stream()` sobre un paginador externo, y demuestra con código por qué `"🚀".codePoints().count()` difiere de `"🚀".chars().count()`.

---

# Capítulo 3: Filtrado, Rebanado (*Slicing*), Deduplicación y Coincidencia (*Matching*)

> *"En todo motor de datos —ya sea PostgreSQL, Apache Spark o Java Streams— la regla de rendimiento número uno es idéntica: descarta los datos irrelevantes lo más pronto posible."*

En este capítulo cada operación de selección, recorte, deduplicación y búsqueda cuenta con su **ficha técnica completa**: firma oficial de Java 25, mecánica interna en la JVM, diagrama o prueba de escritorio paso a paso, escenarios reales de producción, trampas comunes y ejemplos progresivos.

---

## 3.1 `Stream.filter(Predicate)`: Selección Sin Estado

### Firma oficial
```java
Stream<T> filter(Predicate<? super T> predicate)
```

### ¿Qué hace y cómo funciona por debajo?
Evalúa `predicate.test(elemento)` sobre cada elemento que fluye por la banda transportadora:
* Si retorna `true`, el elemento pasa a la siguiente etapa.
* Si retorna `false`, se descarta inmediatamente y **nunca** llega a operaciones posteriores (`map`, `sorted`, `distinct`).
* Es **intermedia**, **perezosa** y **sin estado** ($O(1)$ de memoria). **Elimina la bandera `SIZED`** del `Spliterator` porque ya no se conoce cuántos elementos sobrevivirán.

```java
// Puente Imperativo -> Stream:
List<Employee> topEarners = employees.stream()
        .filter(Objects::nonNull)               // Protección contra null
        .filter(e -> e.getSalary() > 250_000.0) // Regla de negocio
        .toList();
```

### Escenarios reales de uso
1. **Principio *Predicate Push-Down*:** Coloca tus `.filter()` siempre **antes** de `.sorted()`, `.distinct()` o `.map()` costosos, y pon primero el filtro más barato y más selectivo.
2. **Filtros dinámicos en APIs REST:** Componer predicados opcionales partiendo de la identidad neutra `_ -> true`:
   ```java
   record FilterCriteria(String department, Double minSalary, String language) {}

   public List<Employee> search(List<Employee> employees, FilterCriteria c) {
       Predicate<Employee> pred = _ -> true;
       if (c.department() != null) pred = pred.and(e -> c.department().equalsIgnoreCase(e.getDepartment()));
       if (c.minSalary() != null)  pred = pred.and(e -> e.getSalary() >= c.minSalary());
       if (c.language() != null)   pred = pred.and(e -> c.language().equalsIgnoreCase(e.getProgrammingLanguage()));
       return employees.stream().filter(Objects::nonNull).filter(pred).toList();
   }
   ```

---

## 3.2 `Stream.skip(long n)` y `Stream.limit(long maxSize)`: Paginación y Recorte

### Firmas oficiales
```java
Stream<T> skip(long n)       // Descarta los primeros n elementos (lanza IllegalArgumentException si n < 0)
Stream<T> limit(long maxSize) // Cortocircuito: detiene el pipeline tras emitir maxSize elementos
```

### ¿Cómo funcionan por debajo?
* **`skip(n)`** lleva un contador interno; descarta los primeros `n` elementos y deja pasar todos los demás. Si el Stream tiene $\le n$ elementos, retorna un Stream vacío (jamás lanza `IndexOutOfBoundsException`).
* **`limit(maxSize)`** es una operación **con estado acotado y de cortocircuito**: en cuanto emite `maxSize` elementos, activa `cancellationRequested()` en el `Sink` y detiene inmediatamente el upstream (incluso sobre Streams infinitos).

### Escenario real: Paginación (Fórmula universal)
```java
int pageIndex = 2; // Página 3 (base 0)
int pageSize  = 8;

List<Product> page3 = products.stream()
        .skip((long) pageIndex * pageSize) // Salta 16 elementos
        .limit(pageSize)                   // Emite 8 y CORTA el flujo
        .toList();
```

> **Error Común de Programación (Invertir `limit` y `skip`):**
> `.limit(8).skip(16)` siempre produce `[]`, porque `limit(8)` solo deja pasar 8 elementos y `skip(16)` los descarta todos. En paginación, **`skip` va siempre ANTES de `limit`**.

---

## 3.3 `Stream.takeWhile(Predicate)` y `Stream.dropWhile(Predicate)` (Java 9+)

### Firmas oficiales
```java
default Stream<T> takeWhile(Predicate<? super T> predicate)
default Stream<T> dropWhile(Predicate<? super T> predicate)
```

### ¿Por qué existen si ya existe `filter()`? (Prueba de escritorio)
`filter()` siempre evalúa **el 100%** de los elementos. En secuencias ya ordenadas (logs por timestamp, precios ordenados, edades ordenadas), `takeWhile` y `dropWhile` operan exclusivamente sobre el **prefijo inicial**:

* **`takeWhile(p)`**: emite mientras `p` sea `true`; en el **primer `false`**, hace **cortocircuito inmediato** y cierra el Stream.
* **`dropWhile(p)`**: descarta mientras `p` sea `true`; en el **primer `false`**, abre la compuerta y emite ese elemento y **todos los restantes sin volver a evaluar `p`**.

| Entrada (`h`) | `filter(h < 17)` | `takeWhile(h < 17)` | `dropWhile(h < 17)` |
| :---: | :--- | :--- | :--- |
| `8, 9, 10, 15` | `true` → emite | `true` → emite | `true` → descarta |
| **`17`** | `false` → descarta | **`false` → ¡STOP!** | **`false` → abre compuerta, emite `17`** |
| `18, 12, 19` | evalúa todos (`12` pasa) | *(jamás se leen)* | *(sin evaluar `p`)* emite `18, 12, 19` |
| **Resultado** | `[8, 9, 10, 15, 12]` | `[8, 9, 10, 15]` | `[17, 18, 12, 19]` |

### Escenario real: Extraer una ventana de rango en datos ordenados
```java
// Empleados entre 30 y 40 años en una lista ya ordenada por edad (sin leer > 40 años):
List<Employee> thirties = sortedByAge.stream()
        .dropWhile(e -> e.getAge() < 30)
        .takeWhile(e -> e.getAge() <= 40)
        .toList();
```

> **Error Común de Programación:** Sobre fuentes sin bandera `ORDERED` (como `HashSet`), `takeWhile`/`dropWhile` devuelven un subconjunto no determinista. Úsalos solo en streams ordenados.

---

## 3.4 `Stream.distinct()`: Deduplicación Con Estado

### Firma oficial
```java
Stream<T> distinct()
```

### ¿Cómo funciona por debajo?
* Es una operación intermedia **con estado** ($O(U)$ memoria donde $U$ es el número de elementos únicos).
* En secuencial, mantiene internamente un **`HashSet<T>`** y compara usando **`equals()` y `hashCode()`**. Si la fuente es `ORDERED`, garantiza preservar la **primera aparición**.
* Si la fuente ya tiene la característica `Spliterator.DISTINCT` (`HashSet`, `TreeSet`), la JVM lo convierte en *no-op*.

### Escenario real: Deduplicar por un atributo específico (`distinctBy`)
Cuando quieres el primer `Employee` completo de cada departamento:
```java
// 100% puro y seguro en paralelo (con variable anónima '_' de Java 22–25):
List<Employee> firstPerDept = new ArrayList<>(employees.stream()
        .collect(Collectors.toMap(
                Employee::getDepartment,
                Function.identity(),
                (first, _) -> first,
                LinkedHashMap::new
        )).values());
// En Java 25 también puedes hacerlo perezosamente con un Gatherer distinctBy (Capítulo 16).
```

---

## 3.5 Operaciones Terminales de Coincidencia y Búsqueda (Cortocircuito)

Todas estas operaciones son **terminales** y **cortocircuitan** en cuanto el resultado queda determinado:

### 1. `boolean anyMatch(Predicate<? super T> predicate)`
* **Qué hace:** Devuelve `true` si **al menos un** elemento cumple el predicado. Se detiene en el primer `true`. En Stream vacío devuelve **`false`**.
* **Escenario real:** Verificar si existe al menos un error crítico en un log, o si algún empleado trabaja en `"New York"`. Reemplaza siempre `filter(p).count() > 0`.

### 2. `boolean allMatch(Predicate<? super T> predicate)`
* **Qué hace:** Devuelve `true` si **todos** los elementos cumplen el predicado. Se detiene en el primer `false`.
* **Trampa (*Vacuous Truth*):** En Stream vacío devuelve **`true`** ($\forall x \in \emptyset: P(x)$). Valida `!list.isEmpty()` si requieres al menos un elemento.

### 3. `boolean noneMatch(Predicate<? super T> predicate)`
* **Qué hace:** Devuelve `true` si **ningún** elemento cumple el predicado (equivale a `!anyMatch(p)`). Se detiene en el primer `true` retornando `false`. En Stream vacío devuelve **`true`**.
* **Escenario real:** Validar ausencia de productos con rating `< 4.0`, o prueba de primalidad:
  ```java
  IntPredicate isPrime = n -> n >= 2 &&
          IntStream.rangeClosed(2, (int) Math.sqrt(n)).noneMatch(d -> n % d == 0);
  ```

### 4. `Optional<T> findFirst()` vs. `Optional<T> findAny()`
* **`findFirst()`**: Devuelve el **primer** elemento en orden de encuentro envuelto en `Optional<T>` (o `Optional.empty()` si no hay coincidencias). En paralelo debe coordinar particiones para respetar el orden.
* **`findAny()`**: Devuelve **cualquier** elemento coincidente tan pronto como cualquier hilo lo encuentra. En `parallelStream()` ofrece el máximo rendimiento cuando el orden no importa.

```java
// Ejemplo: detectar violación de SLO (3 lecturas seguidas > 100 ms) con cortocircuito en Java 25
boolean sloBreach = latencies.stream()
        .gather(Gatherers.windowSliding(3))
        .anyMatch(window -> window.stream().allMatch(ms -> ms > 100));
```

---

## 3.6 Retos de Dominio y Autoevaluación (Capítulo 3)

1. **Nivel Inicial:** Implementa `paginate(List<T> items, int pageIndex, int pageSize)` usando `skip` y `limit`, y explica qué sucede si inviertes su orden.
2. **Nivel Intermedio:** Dada una lista de transacciones ordenadas por fecha, extrae únicamente las de marzo a junio usando `dropWhile` + `takeWhile` sin evaluar el resto del año.
3. **Nivel Avanzado:** Escribe un generador de números primos menores a 10,000 usando `IntStream.rangeClosed` y `noneMatch`, y explica la trampa de la *vacuous truth* en `allMatch` sobre colecciones vacías.
4. **Nivel Staff MAANG:** Deduplica empleados conservando el primero de cada departamento sin mutar estado externo, y detecta violaciones de SLO (3 lecturas > 100 ms) con `Gatherers.windowSliding(3)`.

---

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

---

# Capítulo 5: Ordenamiento, Comparadores, Estabilidad y Recorrido Inverso $O(1)$ (Java 25 LTS)

> *"Ordenar parece una línea inofensiva (`.sorted()`), pero es una de las operaciones más caras en CPU y memoria de toda la API de Streams. Saber cuándo usarla —y cuándo evitarla— separa a un junior de un ingeniero Staff."*

En este capítulo cada operación de ordenamiento y cada fábrica de `Comparator` tiene su **ficha técnica completa**: firma oficial de Java 25, mecánica interna (TimSort, estabilidad, barrera *stateful*), escenarios reales, trampas de `null` y de `.reversed()`, y la optimización $O(1)$ de `SequencedCollection.reversed().stream()`.

---

## 5.1 Fundamentos Desde Cero: `Comparable<T>` vs. `Comparator<T>`

* **`Comparable<T>` (Orden natural intrínseco):** Una clase define su orden predeterminado implementando `int compareTo(T other)` (como `String`, `Integer`, `Double`, `BigDecimal`, `LocalDate`). Las entidades de dominio como `Employee`, `Product` u `Order` normalmente **no** implementan `Comparable`.
* **`Comparator<T>` (Orden externo flexible):** `@FunctionalInterface` con método SAM `int compare(T a, T b)`: devuelve `< 0` si `a` va antes que `b`, `0` si empatan, `> 0` si `a` va después.

---

## 5.2 `Stream.sorted()` y `Stream.sorted(Comparator)`: La Barrera Con Estado

### Firmas oficiales
```java
Stream<T> sorted()                                 // Exige que T implemente Comparable<T> (si no -> ClassCastException)
Stream<T> sorted(Comparator<? super T> comparator) // Usa el orden definido por comparator
```

### ¿Cómo funciona por debajo?
`sorted()` es una **barrera *stateful* total**: no puede emitir ni un solo elemento hasta haber consumido y copiado **el 100%** de los datos en un búfer temporal `Object[]` en el *heap*:

```text
Fuente ──▶ filter() ──▶ [ BARRERA sorted(): copia N elementos a Object[] ] ──▶ TimSort O(N log N) ──▶ salida
                        (Memoria: O(N) | Nunca usar en Streams infinitos sin limit() antes!)
```

* **En `Stream<T>` (objetos):** Usa **TimSort** ($O(N \log N)$ peor caso, $O(N)$ si ya viene casi ordenado) y **garantiza estabilidad** (elementos empatados conservan su orden relativo original).
* **En `IntStream`/`LongStream`/`DoubleStream`:** Usa **Dual-Pivot Quicksort** (no necesita estabilidad porque dos enteros iguales son indistinguibles).
* **Optimización `SORTED`:** Si la fuente ya tiene bandera `Spliterator.SORTED` (como `TreeSet`) y llamas a `.sorted()` sin argumentos, el motor la omite en $O(1)$.

---

## 5.3 Enciclopedia de Fábricas y Combinadores de `Comparator<T>`

### 1. `Comparator.naturalOrder()` y `Comparator.reverseOrder()`
* **Para qué sirven:** Orden ascendente o descendente natural para tipos que implementan `Comparable` (`String`, `Integer`, `LocalDate`).
* **Ejemplo:** `names.stream().sorted(Comparator.reverseOrder()).toList()`.

### 2. `Comparator.comparing(...)` y Especializaciones Primitivas (`comparingInt`, `comparingLong`, `comparingDouble`)
* **Firmas:**
  * `comparing(Function<? super T, ? extends U> keyExtractor)` (con sobrecarga opcional `Comparator<? super U> keyComparator`)
  * `comparingInt(ToIntFunction<? super T> keyExtractor)` — **cero *boxing***
  * `comparingLong(ToLongFunction<? super T> keyExtractor)` — **cero *boxing***
  * `comparingDouble(ToDoubleFunction<? super T> keyExtractor)` — **cero *boxing***
* **Por qué usar `comparingInt/Long/Double`:** Evitan envolver cada número en `Integer`/`Double` durante las $O(N \log N)$ comparaciones de TimSort.
* **Trampa fatal:** Jamás escribas `(a, b) -> a.getAge() - b.getAge()`; con números grandes provoca *integer overflow* y corrompe TimSort. Usa siempre `Comparator.comparingInt(...)`.

### 3. `.thenComparing(...)`, `.thenComparingInt(...)`, `.thenComparingLong(...)`, `.thenComparingDouble(...)`
* **Para qué sirven:** Definen criterios de desempate (2.º, 3.er y 4.º nivel) que solo se evalúan cuando el criterio anterior devolvió `0`.

### 4. `.reversed()`: Inversión y la Trampa de Alcance
* **Qué hace:** Invierte **todo** el comparador acumulado hasta ese punto.
* **Error Común de Programación:**
  * `Comparator.comparing(Employee::getDepartment).thenComparingDouble(Employee::getSalary).reversed()` invierte **tanto el departamento como el salario**.
  * Para invertir **solo** el salario, envuélvelo explícitamente:
    ```java
    Comparator<Employee> staffRank = Comparator
            .comparing(Employee::getDepartment)                                        // 1. Depto ASC
            .thenComparing(Comparator.comparingDouble(Employee::getSalary).reversed()) // 2. Salario DESC
            .thenComparingInt(Employee::getAge)                                        // 3. Edad ASC
            .thenComparing(Employee::getLastName);                                     // 4. Apellido ASC
    ```

### 5. `Comparator.nullsFirst(Comparator)` y `Comparator.nullsLast(Comparator)`
* **Para qué sirven:** Protegen contra `NullPointerException` en **dos niveles distintos**:
  1. **Nivel 1 (Elementos `null` en la colección):** `Comparator.nullsFirst(Comparator.naturalOrder())`
  2. **Nivel 2 (Campo clave `null` dentro de un objeto):** se pasa como **segundo argumento** de `Comparator.comparing(...)`:
     ```java
     List<Employee> byCitySafe = employees.stream()
             .sorted(Comparator.comparing(
                     Employee::getCity,
                     Comparator.nullsLast(Comparator.naturalOrder())))
             .toList();
     ```

### 6. `String.CASE_INSENSITIVE_ORDER` y Prioridades Personalizadas
```java
// Prioridad de negocio personalizada mediante mapa de rangos:
Map<String, Integer> deptRank = Map.of("Engineering", 1, "Data Science", 2, "Product", 3);
List<Employee> byPriority = employees.stream()
        .sorted(Comparator
                .comparingInt((Employee e) -> deptRank.getOrDefault(e.getDepartment(), Integer.MAX_VALUE))
                .thenComparing(Comparator.comparingDouble(Employee::getSalary).reversed()))
        .toList();
```

---

## 5.4 `SequencedCollection.reversed().stream()` ([JEP 431](https://openjdk.org/jeps/431)) y Anti-Patrones MAANG

| Necesidad | Enfoque Costoso | Enfoque Óptimo Java 25 / MAANG |
| :--- | :--- | :--- |
| **Invertir una `List` o `LinkedHashSet`** | `.sorted(Comparator.reverseOrder())` ($O(N \log N)$, $O(N)$ RAM) | **`collection.reversed().stream()`** (**$O(1)$ arranque, 0 copias**) |
| **Obtener el mínimo o máximo (Top-1)** | `.sorted(comp).findFirst()` ($O(N \log N)$, $O(N)$ RAM) | **`.min(comp)` / `.max(comp)`** (**$O(N)$ tiempo, $O(1)$ RAM**) |
| **Filtrar y ordenar** | `.sorted(comp).filter(p)` | **`.filter(p).sorted(comp)`** (*predicate push-down*) |
| **Top-K en millones ($K \ll N$)** | `.sorted(comp).limit(k)` ($O(N \log N)$) | **Custom `Collector` con `PriorityQueue`** ($O(N \log K)$, Cap. 11) |

---

## 5.5 Retos de Dominio y Autoevaluación (Capítulo 5)

1. **Nivel Inicial:** Ordena una lista de `Product` por precio ascendente y, en empate, por rating descendente sin *boxing*.
2. **Nivel Intermedio:** Ordena una lista de `Employee` donde tanto elementos de la lista como el campo `city` pueden ser `null`, colocando los `null` siempre al final sin lanzar `NullPointerException`.
3. **Nivel Avanzado:** Implementa un ordenamiento por prioridad de negocio (`Map<String, Integer>`) y demuestra por qué `.comparing(A).thenComparing(B).reversed()` invierte ambos criterios.
4. **Nivel Staff MAANG:** Compara la complejidad temporal y espacial de `list.stream().sorted(Comparator.reverseOrder())` frente a `list.reversed().stream()` ([JEP 431](https://openjdk.org/jeps/431)) y de `sorted().findFirst()` frente a `min()`.

---

# Capítulo 6: Reducción, Álgebra de Monoides, Suma de Kahan, `BigDecimal` y Agregación Estadística

> *"Toda agregación —sumar una nómina, calcular un factorial, encontrar el producto más caro o reconstruir un balance financiero— es una forma de reducción: plegar N elementos en un único resultado."*

En este capítulo estudiarás desde cero absoluto qué significa **reducir** (*fold*) una secuencia de datos. Veremos el puente directo desde un bucle `for`, trazaremos paso a paso una reducción secuencial frente a una paralela, demostraremos las leyes matemáticas que exige la JVM (**monoides**), destaparemos el secreto interno del JDK llamado **Suma Compensada de Kahan** en `DoubleStream.sum()`, dominaremos las finanzas exactas con **`BigDecimal` y `RoundingMode.HALF_EVEN`**, y cerraremos con un caso de estudio completo de conciliación bancaria en una sola pasada.

---

## 6.1 ¿Qué Es una Reducción y Qué Es un Monoide? (Desde Cero)

En programación imperativa, cada vez que quieres resumir una lista de 1,000 elementos en un solo número o cadena, escribes siempre las mismas 4 líneas:

```java
// ── ANTES (Imperativo): tú controlas la variable mutable 'acc' ──
int acc = 0;                 // 1. Valor inicial (identidad)
for (int x : numbers) {
    acc = acc + x;           // 2. Operación binaria que combina el acumulado con el siguiente elemento
}

// ── DESPUÉS (Java 25 Stream): declarativo, inmutable y paralelizable ──
int total = numbers.stream().reduce(0, Integer::sum);
```

### Prueba de escritorio: Reducción secuencial vs. paralela sobre `[10, 20, 30, 40]`

| Paso | Modo Secuencial (`stream()`) | Modo Paralelo (`parallelStream()`) |
| :---: | :--- | :--- |
| 1 | `acc = 0` (identidad) | **Hilo 1** inicia `acc1 = 0`; **Hilo 2** inicia `acc2 = 0` |
| 2 | `acc = 0 + 10 = 10` | **Hilo 1**: `0 + 10 = 10`, luego `10 + 20 = 30` |
| 3 | `acc = 10 + 20 = 30` | **Hilo 2**: `0 + 30 = 30`, luego `30 + 40 = 70` |
| 4 | `acc = 30 + 30 = 60` | **Combiner**: `30 + 70 = 100` |
| 5 | `acc = 60 + 40 = 100` | **Resultado final: `100`** |

```text
Árbol Secuencial:  (((0 + 10) + 20) + 30) + 40  =  100

Árbol Paralelo:    Hilo 1: (0 + 10) + 20 = 30  ─┐
                                                ├── combiner(30, 70) = 100
                   Hilo 2: (0 + 30) + 40 = 70  ─┘
```

Para que **cualquier** forma de agrupar paréntesis y **cualquier** número de hilos devuelvan exactamente el mismo resultado, el par `(identidad, operador)` debe cumplir las dos leyes de un **monoide algebraico**:

### Ley 1: Elemento Identidad ($e$)
Combinar cualquier elemento $x$ con la identidad $e$ **no altera $x$**:
$$op(e, x) = x \quad \text{y} \quad op(x, e) = x$$

| Operación | Identidad Correcta ($e$) | Verificación |
| :--- | :--- | :--- |
| Suma (`+`) | `0`, `0.0`, `BigDecimal.ZERO` | `0 + x = x` |
| Multiplicación (`*`) | `1`, `1.0`, `BigDecimal.ONE` | `1 * x = x` |
| `AND` lógico (`&&`) | `true` | `true && x = x` |
| `OR` lógico (`||`) | `false` | `false || x = x` |
| Concatenación de cadenas | `""` | `"" + s = s` |
| Unión de conjuntos | `Set.of()` (inmutable) | $\emptyset \cup A = A$ |

### Ley 2: Asociatividad Estricta
El orden de los paréntesis no cambia el resultado:
$$op(a, op(b, c)) = op(op(a, b), c)$$
* **Son asociativos:** suma entera, multiplicación, `min`, `max`, `&&`, `||`, concatenación de `String`.
* **NO son asociativos:** resta (`(10 - 5) - 2 = 3`, pero `10 - (5 - 2) = 7`), división (`(16 / 4) / 2 = 2`, pero `16 / (4 / 2) = 8`), promedio directo `(a + b) / 2`.
* **Nota importante:** La *conmutatividad* ($a \circ b = b \circ a$) **no** es obligatoria para `reduce()`. La concatenación de cadenas (`"Hola " + "Mundo" != "Mundo " + "Hola"`) no es conmutativa, pero sí es asociativa, y Java respeta el orden de encuentro al combinar particiones izquierdas y derechas.

> **Error Común de Programación (Identidad falsa en paralelo):**
> Si escribes `List.of(1, 2, 3, 4).parallelStream().reduce(10, Integer::sum)` esperando obtener `10 + 1 + 2 + 3 + 4 = 20`, en paralelo **cada hilo arranca su partición en `10`**, devolviendo `50` (con 4 hilos). La identidad de la suma es siempre `0`; cualquier base fija se suma fuera: `10 + list.parallelStream().mapToInt(Integer::intValue).sum()`.

---

## 6.2 Las Tres Sobrecargas de `Stream.reduce()` Explicadas Paso a Paso

### Sobrecarga 1: `Optional<T> reduce(BinaryOperator<T> accumulator)`
* **Cuándo usarla:** Cuando **no existe un valor identidad natural** (por ejemplo, encontrar el apellido más largo, elegir al empleado mejor pagado o unir elementos sin separador inicial).
* **Por qué devuelve `Optional<T>`:** Si el stream está vacío, no hay resultado posible; devuelve `Optional.empty()` en lugar de `null`.

```java
// Ejemplo 1: Apellido más largo
Optional<String> longestLastName = employees.stream()
        .map(Employee::getLastName)
        .reduce((n1, n2) -> n1.length() >= n2.length() ? n1 : n2);

// Ejemplo 2: Unir palabras con ", " (Nota: para listas grandes usa Collectors.joining(", ") en O(N))
String joined = List.of("Java", "Go", "Rust").stream()
        .reduce((a, b) -> a + ", " + b)
        .orElse(""); // "Java, Go, Rust"
```

### Sobrecarga 2: `T reduce(T identity, BinaryOperator<T> accumulator)`
* **Cuándo usarla:** Cuando existe identidad clara y el tipo de entrada `T` coincide con el tipo de salida `T`. Si el stream está vacío, devuelve `identity`.

```java
int factorial5    = List.of(1, 2, 3, 4, 5).stream().reduce(1, (a, b) -> a * b); // 120
boolean allPassed = List.of(true, true, false).stream().reduce(true, (a, b) -> a && b); // false
```

### Sobrecarga 3: `<U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner)`
* **Cuándo usarla:** Cuando el resultado **`U`** tiene un tipo **distinto** al de los elementos del stream **`T`** (por ejemplo, reducir `Stream<Transaction>` directamente hacia `BigDecimal`).
* **Papel del `combiner`:** En un stream secuencial el `combiner` nunca se llama, **pero en un stream paralelo fusiona los acumuladores parciales de tipo `U`**. Si lo escribes mal, tu código pasará tests secuenciales y fallará en producción en paralelo.

```java
BigDecimal eurNetBalance = transactions.stream()
        .filter(tx -> "EUR".equals(tx.getCurrency()))
        .reduce(
                BigDecimal.ZERO,
                (acc, tx) -> {
                    BigDecimal amt = BigDecimal.valueOf(tx.getAmount());
                    return tx.getType() == Transaction.Type.CREDIT
                            ? acc.add(amt)
                            : acc.subtract(amt);
                },
                BigDecimal::add // Combiner asociativo para ejecución paralela
        );
```

> **Error Común de Programación (Mutar contenedores en `reduce`):**
> Jamás pases un `new ArrayList<>()`, `new HashMap<>()` o `new StringBuilder()` como `identity` de `reduce()`. En paralelo todos los hilos mutarán el mismo objeto generando *race conditions* o duplicaciones. Para contenedores mutables usa siempre **`collect()`** (Capítulo 7).

---

## 6.3 Secreto del JDK: Suma Compensada de Kahan en `DoubleStream.sum()`

En aritmética IEEE-754 de punto flotante (`double` de 64 bits), la mantisa tiene 53 bits de precisión (~15–17 dígitos decimales). Cuando sumas un número muy pequeño a uno muy grande, los bits menos significativos se pierden por redondeo. En consecuencia, **la suma de `double` NO es estrictamente asociativa**:
`(1e16 + 1.0) - 1e16 == 0.0`, mientras que `1e16 + (1.0 - 1e16) == 1.0`.

Por diseño explícito de Brian Goetz y el equipo del JDK, **`DoubleStream.sum()`**, **`DoubleStream.average()`**, **`DoubleSummaryStatistics`** y **`Collectors.summingDouble()`** implementan internamente el **algoritmo de suma compensada de Kahan**: mantienen una variable adicional `c` que captura el error de redondeo perdido en cada suma y lo reinyecta en la siguiente iteración.

```java
double[] tricky = {1e16, 1.0, 1.0, 1.0, -1e16};

// Naive reduce pierde los +1.0 por redondeo IEEE-754 -> 0.0
double naiveSum = Arrays.stream(tricky).reduce(0.0, Double::sum);

// DoubleStream.sum() usa compensación de Kahan -> preserva mayor precisión
double kahanSum = Arrays.stream(tricky).sum();
```

> **Tip de Rendimiento y Precisión JVM / MAANG:** Prefiere siempre `.mapToDouble(...).sum()` sobre `.mapToDouble(...).reduce(0.0, Double::sum)`: es más rápido, más legible y numéricamente más preciso gracias a la suma compensada del JDK.

---

## 6.4 Precisión Financiera Exacta con `BigDecimal`, `MathContext` y `RoundingMode`

En banca, contabilidad o *fintech*, ni siquiera Kahan en `double` basta porque `0.1` es periódico infinito en binario (`0.1 + 0.2 == 0.30000000000000004`).

**Las 4 reglas de oro financieras con `BigDecimal` en Streams:**
1. Construye siempre con **`BigDecimal.valueOf(double)`** o **`new BigDecimal("19.99")`**, **nunca** `new BigDecimal(0.1)` (que hereda el ruido binario `0.10000000000000000555...`).
2. Reduce con identidad **`BigDecimal.ZERO`** y acumulador **`BigDecimal::add`**.
3. Al comparar igualdad numérica usa **`compareTo(...) == 0`**, no `.equals()` (porque `new BigDecimal("10.0").equals(new BigDecimal("10.00"))` devuelve **`false`** por distinta escala).
4. Al dividir (por ejemplo, al calcular promedios), especifica siempre escala y modo de redondeo —el estándar contable es **`RoundingMode.HALF_EVEN`** (*Banker's Rounding*)— para evitar `ArithmeticException: Non-terminating decimal expansion`:

```java
// Promedio exacto de precios con BigDecimal en UNA sola pasada usando Collectors.teeing()
BigDecimal exactAvgPrice = products.stream()
        .map(p -> BigDecimal.valueOf(p.getPrice()))
        .collect(Collectors.teeing(
                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add),
                Collectors.counting(),
                (sum, count) -> count == 0
                        ? BigDecimal.ZERO
                        : sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_EVEN)
        ));
```

---

## 6.5 Terminales Especializados, `SummaryStatistics`, Promedio Ponderado y Caso de Estudio

Antes de escribir un `reduce()` manual, verifica si existe un terminal especializado:

| Terminal | Streams | Retorno | Complejidad / Ventaja |
| :--- | :--- | :--- | :--- |
| `count()` | Todos | `long` | **$O(1)$ en Java 9–25** si mantiene bandera `SIZED`; si no, $O(N)$. |
| `sum()` | Primitivos | primitivo | Cero *boxing*; Kahan summation en `DoubleStream`; `0` en vacío. |
| `average()` | Primitivos | `OptionalDouble` | Retorna `OptionalDouble.empty()` en vacío (evita `0/0 = NaN`). |
| `min()` / `max()` | Todos | `Optional*` | $O(N)$ tiempo, $O(1)$ espacio. |
| `summaryStatistics()` | Primitivos | `*SummaryStatistics` | Calcula **count, sum, min, max y average en 1 sola pasada $O(N)$**. |

### Caso de Estudio Completo: Conciliación Contable Inmutable con `record` + `reduce`
Supón que necesitas resumir un lote de transacciones en un único objeto inmutable `LedgerSummary(long count, BigDecimal credits, BigDecimal debits)` que sea **100% paralelizable**:

```java
record LedgerSummary(long count, BigDecimal credits, BigDecimal debits) {
    static final LedgerSummary ZERO = new LedgerSummary(0, BigDecimal.ZERO, BigDecimal.ZERO);

    LedgerSummary add(Transaction tx) {
        BigDecimal amt = BigDecimal.valueOf(tx.getAmount());
        return tx.getType() == Transaction.Type.CREDIT
                ? new LedgerSummary(count + 1, credits.add(amt), debits)
                : new LedgerSummary(count + 1, credits, debits.add(amt));
    }

    LedgerSummary merge(LedgerSummary other) {
        return new LedgerSummary(
                count + other.count,
                credits.add(other.credits),
                debits.add(other.debits)
        );
    }

    BigDecimal netBalance() {
        return credits.subtract(debits);
    }
}

// Reducción paralela 100% pura (cumple las leyes de monoide: identidad ZERO + merge asociativo):
LedgerSummary summary = transactions.parallelStream()
        .reduce(LedgerSummary.ZERO, LedgerSummary::add, LedgerSummary::merge);
```

Observa cómo este patrón combina inmutabilidad (`record`), precisión financiera (`BigDecimal`), identidad neutra (`ZERO`) y un `combiner` asociativo (`merge`), logrando **cero estado compartido** en paralelo.

> **Conexión con Java 25 ([JEP 485](https://openjdk.org/jeps/485)):** Si necesitas reducir y **seguir encadenando** `.map()` o `.filter()` sin cerrar el stream, usa `stream.gather(Gatherers.fold(supplier, folder))` (Capítulo 16).

---

## 6.6 Retos de Dominio y Autoevaluación (Capítulo 6)

1. **Nivel Inicial:** Calcula el producto factorial de un entero $N$ usando `LongStream.rangeClosed` y `reduce`, y el producto punto de dos vectores `int[]` sin *boxing*.
2. **Nivel Intermedio:** Calcula el balance neto de una lista de `Transaction` en euros usando `BigDecimal` (identidad `BigDecimal.ZERO`, sumando `CREDIT` y restando `DEBIT`) y el promedio exacto con `RoundingMode.HALF_EVEN`.
3. **Nivel Avanzado:** Demuestra experimentalmente un caso donde `reduce(10, Integer::sum)` produce un resultado incorrecto en `parallelStream()` y explica qué ley de los monoides se violó.
4. **Nivel Staff MAANG:** Explica cómo la suma compensada de Kahan en `DoubleStream.sum()` mitiga la pérdida de precisión IEEE-754 frente a `reduce(0.0, Double::sum)`, e implementa un `record LedgerSummary` paralelizable con `reduce` de 3 argumentos.

---

# Capítulo 7: Recolección de Resultados — Enciclopedia de `collect()` y `Collectors` (Java 25 LTS)

> *"Si `reduce()` es una prensa que compacta datos en un valor inmutable, `collect()` es una fábrica de ensamblaje que llena contenedores mutables a velocidad lineal $O(N)$ y con total seguridad entre hilos."*

En este capítulo cada operación terminal de recolección tiene su propia **ficha técnica completa**: firma oficial de Java 25, explicación teórica desde cero, mecánica interna, escenarios reales de uso, trampas comunes y ejemplos progresivos.

---

## 7.1 `Stream.collect(supplier, accumulator, combiner)`: Reducción Mutable Desde Cero

### Firma oficial
```java
<R> R collect(Supplier<R> supplier,
              BiConsumer<R, ? super T> accumulator,
              BiConsumer<R, R> combiner)
```

### ¿Qué hace y por qué existe frente a `reduce()`?
Construir una lista creando copias con `reduce()` cuesta $1 + 2 + \dots + N = O(N^2)$ copias en memoria. **`collect()`** resuelve esto con **reducción mutable aislada por hilo** en tiempo lineal $O(N)$:
1. **`supplier`**: crea un contenedor mutable vacío nuevo (en paralelo, uno privado para cada hilo).
2. **`accumulator`**: inserta cada elemento `T` en el contenedor local en $O(1)$ amortizado.
3. **`combiner`**: en paralelo, fusiona dos contenedores parciales al terminar.

```text
Hilo 1: supplier() -> ArrayList local 1 -> add(A), add(B)  ─┐
                                                            ├── combiner: local1.addAll(local2) -> O(N)
Hilo 2: supplier() -> ArrayList local 2 -> add(C), add(D)  ─┘
```

### Escenario real: `StringJoiner` y `Map` que acepta valores `null`
```java
// Ejemplo 1: Unir ciudades únicas en un StringJoiner con prefijo y sufijo
StringJoiner joiner = employees.stream()
        .map(Employee::getCity).distinct().sorted()
        .collect(
                () -> new StringJoiner("; ", "[", "]"),
                StringJoiner::add,
                StringJoiner::merge
        );

// Ejemplo 2: Recolectar un HashMap cuando los valores pueden ser null (Collectors.toMap lanzaría NPE!)
Map<Integer, String> idToNullableCity = employees.stream()
        .collect(
                HashMap::new,
                (map, e) -> map.put(e.getId(), e.getCity()),
                HashMap::putAll
        );
```

---

## 7.2 Recolectar a Listas y Conjuntos: Ficha Técnica de Cada Método

### 1. `Stream.toList()` (Java 16+) vs. `Collectors.toList()` vs. `Collectors.toUnmodifiableList()`
* **`List<T> toList()`** *(método directo de `Stream`, Java 16+)*:
  * **Qué hace:** Recolecta en una lista **inmodificable** que **sí acepta `null`**. Es más rápida y compacta que `Collectors.toUnmodifiableList()` porque envuelve el arreglo interno directamente sin copias extra.
  * **Cuándo usarla:** Es el **estándar por defecto** en Java 25 siempre que no necesites mutar la lista resultante.
  * **Trampa:** Llamar `.add()`, `.remove()` o `.sort()` sobre el resultado lanza `UnsupportedOperationException`.
* **`Collectors.toList()`** *(Java 8)*:
  * **Qué hace:** Actualmente devuelve `ArrayList`, pero la especificación oficial **no garantiza** mutabilidad ni tipo concreto. Úsala solo si código legado lo exige.
* **`Collectors.toUnmodifiableList()`** *(Java 10+)*:
  * **Qué hace:** Devuelve una lista inmodificable que **rechaza elementos `null`** lanzando `NullPointerException` (*fail-fast*). Úsala cuando quieres prohibir explícitamente nulos en la salida.

### 2. `Collectors.toSet()` vs. `Collectors.toUnmodifiableSet()`
* **`Collectors.toSet()`**: Recolecta en un `HashSet` eliminando duplicados en $O(1)$ promedio (sin garantía de orden).
* **`Collectors.toUnmodifiableSet()`** *(Java 10+)*: Conjunto inmodificable que rechaza `null` (lanza NPE) y prohíbe mutaciones.

### 3. `Collectors.toCollection(Supplier<C> collectionFactory)`
* **Firma:** `<T, C extends Collection<T>> Collector<T, ?, C> toCollection(Supplier<C> collectionFactory)`
* **Para qué sirve:** Te permite elegir la **implementación exacta** de colección que necesitas:
  * `ArrayList::new`: cuando **sí necesitas** una lista garantizadamente mutable.
  * `LinkedHashSet::new`: deduplica en $O(1)$ **preservando el orden de encuentro** y te da un **`SequencedSet`** ([JEP 431](https://openjdk.org/jeps/431)) con `getFirst()`, `getLast()` y `reversed()`.
  * `TreeSet::new`: deduplica y ordena automáticamente en $O(N \log N)$.
  * `ArrayDeque::new` / `LinkedList::new`: cuando necesitas operaciones de cola o pila.

```java
// SequencedSet preservando orden de aparición (Java 21–25):
SequencedSet<String> orderedDepts = employees.stream()
        .map(Employee::getDepartment)
        .collect(Collectors.toCollection(LinkedHashSet::new));
String first = orderedDepts.getFirst();
String last  = orderedDepts.getLast();
```

---

## 7.3 Recolectar a Mapas: `toMap()`, `toUnmodifiableMap()` y `toConcurrentMap()`

### 1. `Collectors.toMap(...)` (Las 3 sobrecargas)
```java
// Sobrecarga 1 (2 args): SOLO para claves 100% únicas
toMap(Function<T, K> keyMapper, Function<T, V> valueMapper)

// Sobrecarga 2 (3 args): con función de resolución de colisiones (mergeFunction)
toMap(Function<T, K> keyMapper, Function<T, V> valueMapper, BinaryOperator<V> mergeFunction)

// Sobrecarga 3 (4 args): con fábrica de Map específica (TreeMap, LinkedHashMap)
toMap(Function<T, K> keyMapper, Function<T, V> valueMapper, BinaryOperator<V> mergeFunction, Supplier<M> mapFactory)
```

* **Cómo funciona por debajo:** Por cada elemento extrae `(k, v)` e invoca `map.merge(k, v, mergeFunction)`.
* **Trampas críticas:**
  1. En la sobrecarga de 2 argumentos, cualquier clave duplicada lanza **`IllegalStateException: Duplicate key`**.
  2. Si `valueMapper` retorna **`null`**, `Map.merge` lanza **`NullPointerException`** (incluso en un `HashMap`). Usa `collect(HashMap::new, ...)` de la sección 7.1 si los valores pueden ser `null`.

```java
// 1. Clave única -> Function.identity() guarda el objeto completo
Map<Integer, Employee> byId = employees.stream()
        .collect(Collectors.toMap(Employee::getId, Function.identity()));

// 2. Resolución de colisiones conservando el primero con variable anónima '_' (JEP 456)
Map<String, String> firstByCity = employees.stream()
        .collect(Collectors.toMap(Employee::getCity, Employee::getFullName, (existing, _) -> existing));

// 3. TreeMap ordenado alfabéticamente con el mejor pagado por departamento
SequencedMap<String, Employee> topByDept = employees.stream()
        .collect(Collectors.toMap(
                Employee::getDepartment,
                Function.identity(),
                BinaryOperator.maxBy(Comparator.comparingDouble(Employee::getSalary)),
                TreeMap::new
        ));
```

### 2. `Collectors.toUnmodifiableMap(...)` (Java 10+)
* **Para qué sirve:** Crea un mapa estrictamente inmodificable (2 o 3 argumentos). Rechaza claves y valores `null` (*fail-fast*). Ideal para tablas de configuración, catálogos en caché y constantes de dominio.

### 3. `Collectors.toConcurrentMap(...)`
* **Para qué sirve:** Igual que `toMap`, pero declara `CONCURRENT | UNORDERED` y escribe directamente en un `ConcurrentHashMap`. En `parallelStream()` desordenado evita el costo de crear y fusionar múltiples `HashMap` locales.

---

## 7.4 `Collectors.joining(...)`: Concatenación de Texto en $O(N)$

### Firmas oficiales (3 sobrecargas)
```java
Collector<CharSequence, ?, String> joining()
Collector<CharSequence, ?, String> joining(CharSequence delimiter)
Collector<CharSequence, ?, String> joining(CharSequence delimiter, CharSequence prefix, CharSequence suffix)
```

* **Por qué existe frente a `reduce((a, b) -> a + "," + b)`:** Concatenar `String` con `+` dentro de `reduce()` crea un objeto `String` nuevo en cada paso ($O(N^2)$ en memoria). `Collectors.joining()` usa internamente un **`StringBuilder`** (o **`StringJoiner`**), logrando tiempo lineal **$O(N)$** y manejando automáticamente delimitadores sin comas sobrantes al inicio o al final.
* **Escenarios reales:** Generar líneas CSV, cláusulas SQL `IN ('A', 'B', 'C')`, encabezados JSON o logs legibles.

```java
String csv  = List.of("Java", "Go", "Rust").stream().collect(Collectors.joining(", "));
String json = List.of("A", "B", "C").stream().collect(Collectors.joining("\", \"", "[\"", "\"]"));
// json -> ["A", "B", "C"]
```

---

## 7.5 `Collectors.collectingAndThen(...)`: Post-Procesamiento al Finalizar

### Firma oficial
```java
public static <T, A, R, RR> Collector<T, A, RR> collectingAndThen(
    Collector<T, A, R> downstream,
    Function<R, RR> finisher)
```

### ¿Qué hace y cuándo usarlo?
Envuelve cualquier collector `downstream` y ejecuta una función final `finisher` **una sola vez** sobre el resultado ya recolectado `R` para transformarlo en `RR`:

```text
Stream<T> ──▶ downstream Collector ──▶ resultado R ──▶ finisher(R) ──▶ resultado final RR
```

### Escenarios reales de uso
1. **Desempacar un `Optional`** devuelto por `maxBy`/`minBy` dentro de `groupingBy`.
2. **Sellar inmutabilidad** sobre colecciones personalizadas (`Collections::unmodifiableList`, `List::copyOf`).
3. **Formatear un valor agregado** (ej. convertir un `Double` promedio a cadena `"$185,000.00"`).
4. **Ordenar y recortar Top-K** dentro de cada cubeta de un `groupingBy`.

```java
String avgBanner = employees.stream()
        .collect(Collectors.collectingAndThen(
                Collectors.averagingDouble(Employee::getSalary),
                avg -> String.format(Locale.US, "Promedio salarial: $%,.2f", avg)
        ));
```

---

## 7.6 `Collectors.teeing(...)` (Java 12+): Dos Recolectores en Una Sola Pasada

### Firma oficial
```java
public static <T, R1, R2, R> Collector<T, ?, R> teeing(
    Collector<? super T, ?, R1> downstream1,
    Collector<? super T, ?, R2> downstream2,
    BiFunction<? super R1, ? super R2, R> merger)
```

### ¿Qué hace y cómo funciona por debajo?
Envía cada elemento del Stream simultáneamente a **dos collectors independientes** (`downstream1` y `downstream2`) en una **única pasada $O(N)$** —sin tener que abrir el Stream dos veces— y al terminar combina ambos resultados `R1` y `R2` mediante `merger`.

```text
                  ┌──▶ downstream1 (minBy salario) ──▶ R1 (minOpt) ─┐
Stream<Employee>──┤                                                 ├──▶ merger(R1, R2) -> SalarySpread
                  └──▶ downstream2 (maxBy salario) ──▶ R2 (maxOpt) ─┘
```

### Escenarios reales de uso
* Calcular `min` y `max` (y su diferencia *spread*) en una pasada.
* Calcular promedio exacto en `BigDecimal` combinando `reducing(BigDecimal.ZERO, BigDecimal::add)` + `counting()`.
* Separar conteo total y conteo de errores en un solo `record` de métricas.

```java
record SalarySpread(double min, double max, double spread) {}

SalarySpread report = employees.stream()
        .collect(Collectors.teeing(
                Collectors.minBy(Comparator.comparingDouble(Employee::getSalary)),
                Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary)),
                (minOpt, maxOpt) -> {
                    double min = minOpt.map(Employee::getSalary).orElse(0.0);
                    double max = maxOpt.map(Employee::getSalary).orElse(0.0);
                    return new SalarySpread(min, max, max - min);
                }
        ));
```

---

## 7.7 Retos de Dominio y Autoevaluación (Capítulo 7)

1. **Nivel Inicial:** Recolecta nombres de departamentos únicos en un `SequencedSet<String>` (`LinkedHashSet::new`) preservando el orden de aparición y consulta `.getFirst()` y `.getLast()`.
2. **Nivel Intermedio:** Construye un `TreeMap<String, Employee>` que almacene al empleado mejor pagado de cada departamento resolviendo colisiones con `BinaryOperator.maxBy`.
3. **Nivel Avanzado:** Recolecta un `Map<Integer, String>` desde `Employee` donde el valor (`city`) puede ser `null` sin lanzar `NullPointerException`, y explica por qué `Collectors.toMap` fallaría en ese caso.
4. **Nivel Staff MAANG:** Usa `Collectors.teeing()` para calcular simultáneamente el salario mínimo, el máximo y el *spread* en una sola pasada $O(N)$ del Stream.

---

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

---

# Capítulo 9: Streams Primitivos (`IntStream`, `LongStream`, `DoubleStream`), Memoria JVM ([JEP 519](https://openjdk.org/jeps/519)) y Enciclopedia Completa de `Optional`

> *"Si tu pipeline envuelve primitivos en objetos dentro de un bucle caliente, estás quemando memoria, rompiendo la caché L1 de tu CPU y saturando el Garbage Collector. Y si devuelves `null` en lugar de `Optional`, estás sembrando una bomba de tiempo para las 3 de la mañana."*

En este capítulo cada método de los Streams primitivos y **absolutamente cada método de la mónada `Optional<T>`** tiene su propia **ficha técnica completa**: firma oficial de Java 25, explicación teórica desde cero, diagrama de memoria o flujo, escenarios reales de producción, trampas comunes y ejemplos progresivos.

---

## 9.1 Física de la Memoria JVM: Por Qué Existen los Streams Primitivos

Un `int` ocupa **4 bytes** contiguos en memoria. Un objeto `java.lang.Integer` en un `Stream<Integer>` vive en el *heap* con cabecera de objeto + relleno de alineación + puntero (*pointer chasing*):

```text
1) int[] (IntStream) — Contiguo en memoria:
   [ 10 | 20 | 30 | 40 | 50 | 60 | ... ]  -> 4 bytes por entero.
   1 línea de caché L1 (64 bytes) carga 16 enteros en UNA sola lectura de RAM!

2) Integer[] (Stream<Integer>) — Indirección + Cache Misses:
   [ ptr1 | ptr2 | ptr3 | ptr4 ]  -> Cada puntero salta a una dirección distinta en RAM
      │      │      │      │
      ▼      ▼      ▼      ▼
   +------------------+  Layout clásico: 12B cabecera + 4B int = 16 bytes por Integer.
   | Cabecera (8/12B) |  Java 25 JEP 519 (-XX:+UseCompactObjectHeaders): cabecera baja a 8B
   | int value (4B)   |  (~22% menos heap, 8% menos CPU y 15% menos GCs en SPECjbb2015).
   +------------------+
```

> **Tip de Rendimiento JVM / MAANG:** Incluso con [JEP 519](https://openjdk.org/jeps/519) activado en Java 25, `Stream<Integer>` sigue costando **4× a 5× más memoria e indirección** que `IntStream`. Usa siempre `IntStream`, `LongStream` o `DoubleStream` para todo cómputo numérico.

---

## 9.2 Fichas Técnicas de Operaciones Clave en `IntStream`, `LongStream` y `DoubleStream`

### 1. `range(startInclusive, endExclusive)` y `rangeClosed(startInclusive, endInclusive)`
* **Firmas:** `IntStream.range(int, int)`, `IntStream.rangeClosed(int, int)` (y sus equivalentes en `LongStream`; no existen en `DoubleStream`).
* **Para qué sirven:** Generan secuencias ordenadas con bandera `SIZED | SUBSIZED` y división paralela en $O(1)$. Usa `range(0, list.size())` para índices y `rangeClosed(1, n)` para fórmulas matemáticas.

### 2. `asLongStream()` y `asDoubleStream()`
* **Firmas:** `LongStream IntStream.asLongStream()`, `DoubleStream IntStream.asDoubleStream()`, `DoubleStream LongStream.asDoubleStream()`.
* **Para qué sirven:** Promocionan numéricamente un stream primitivo a otro de mayor rango/precisión sin *boxing*.

### 3. `mapToObj(IntFunction<U>)` vs. `boxed()`
* **`mapToObj(IntFunction<U>)`**: Convierte cada `int` directamente al objeto destino `U` **sin pasar por `Integer`** (ej. `IntStream.range(1, 6).mapToObj(i -> "Item #" + i)`).
* **`boxed()`**: Convierte `IntStream` → `Stream<Integer>` (o `Long`/`Double`). Úsalo **solo al final** cuando necesites recolectar con `toList()` o `Collectors.groupingBy()`.

### 4. `sum()`, `average()`, `min()`, `max()`, `summaryStatistics()` y `toArray()`
* `sum()`: retorna `int`/`long`/`double` (`0` si está vacío; usa Suma de Kahan en `DoubleStream`).
* `average()`: retorna `OptionalDouble` (vacío si no hay elementos, evitando `0.0 / 0.0 = NaN`).
* `summaryStatistics()`: retorna `IntSummaryStatistics` / `LongSummaryStatistics` / `DoubleSummaryStatistics` con `count`, `sum`, `min`, `max` y `average` en **1 sola pasada $O(N)$**.
* `toArray()`: materializa directamente un arreglo primitivo `int[]` / `long[]` / `double[]`.

```java
// Ejemplo: Fila N del Triángulo de Pascal (cero boxing)
int n = 4;
IntUnaryOperator fact = x -> IntStream.rangeClosed(1, x).reduce(1, (a, b) -> a * b);
int[] pascalRow4 = IntStream.rangeClosed(0, n)
        .map(k -> fact.applyAsInt(n) / (fact.applyAsInt(k) * fact.applyAsInt(n - k)))
        .toArray(); // [1, 4, 6, 4, 1]
```

---

## 9.3 Enciclopedia Completa de `Optional<T>` (Cada Método Explicado a Detalle)

`Optional<T>` es un contenedor inmutable que guarda **0 o 1 valor no nulo**. Su propósito es hacer explícita la posible ausencia de un valor en la firma de un método de retorno, eliminando los `NullPointerException` silenciosos.

### 1. Fábricas de Creación: `Optional.of`, `Optional.ofNullable`, `Optional.empty`
| Método | Firma | Comportamiento si recibe `null` | Cuándo usarlo |
| :--- | :--- | :--- | :--- |
| **`Optional.of(T value)`** | `static <T> Optional<T> of(T)` | **Lanza `NullPointerException` de inmediato** (*fail-fast*). | Cuando el valor **no debe** ser `null` (si lo fuera, es un bug que quieres detectar ya). |
| **`Optional.ofNullable(T value)`** | `static <T> Optional<T> ofNullable(T)` | Devuelve **`Optional.empty()`** con seguridad. | Cuando envuelves resultados de APIs externas, mapas (`map.get(k)`) o campos opcionales. |
| **`Optional.empty()`** | `static <T> Optional<T> empty()` | N/A | Para retornar ausencia explícita desde tus métodos en vez de `return null;`. |

### 2. Inspección: `isPresent()` e `isEmpty()` (Java 11+)
* **`boolean isPresent()`**: `true` si contiene valor.
* **`boolean isEmpty()`** *(Java 11+)*: `true` si está vacío (mucho más legible que `!opt.isPresent()`).
* **Nota de estilo:** Evita el anti-patrón `if (opt.isPresent()) return opt.get(); else return defaultVal;`. Usa siempre los métodos declarativos de abajo.

### 3. Desempaque Seguro: `orElse`, `orElseGet`, `orElseThrow`

#### A) `T orElse(T other)` vs. `T orElseGet(Supplier<? extends T> supplier)`
* **`orElse(other)`** es **ansioso (*eager*)**: Java evalúa la expresión `other` **siempre**, incluso cuando el `Optional` **ya tiene un valor presente**. Úsalo **únicamente** para constantes o variables ya calculadas (`0`, `""`, `"UNKNOWN"`).
* **`orElseGet(supplier)`** es **perezoso (*lazy*)**: solo invoca `supplier.get()` cuando el `Optional` está **vacío**. Es **obligatorio** para llamadas a base de datos, red, `new Object()` o cálculos costosos.

```java
Optional<String> cached = Optional.of("HIT");
String bad  = cached.orElse(queryRemoteDatabase());       // ❌ ¡Llama a la BD aunque había HIT!
String good = cached.orElseGet(this::queryRemoteDatabase); // ✅ Cero llamadas a la BD!
```

#### B) `T orElseThrow()` (Java 10+) y `T orElseThrow(Supplier<? extends X> exceptionSupplier)`
* **`orElseThrow()`** sin argumentos reemplaza al obsoleto `.get()`, dejando explícita la intención de lanzar `NoSuchElementException` si está vacío.
* **`orElseThrow(Supplier)`** lanza una excepción de dominio personalizada (ej. `() -> new OrderNotFoundException(id)`).

### 4. Transformación y Filtrado Monádico: `map`, `flatMap`, `filter`

```text
Optional[user] ──▶ flatMap(User::company) ──▶ flatMap(Company::name) ──▶ filter(n -> !n.isBlank()) ──▶ orElse("N/A")
   (Si en CUALQUIER paso se vuelve empty, el resto se salta sin lanzar NullPointerException!)
```

* **`<U> Optional<U> map(Function<? super T, ? extends U> mapper)`**:
  * Si hay valor, aplica `mapper` y envuelve el resultado en `Optional.ofNullable(...)` (si `mapper` devuelve `null`, produce `Optional.empty()`). Si ya estaba vacío, no hace nada.
* **`<U> Optional<U> flatMap(Function<? super T, ? extends Optional<? extends U>> mapper)`**:
  * Igual que `map`, pero para funciones que **ya devuelven un `Optional`**, evitando anidar `Optional<Optional<U>>`.
  * **Escenario real:** Navegación profunda segura o matemáticas sin excepciones (`safeDivide(100, 4).flatMap(safeSqrt)`).
* **`Optional<T> filter(Predicate<? super T> predicate)`**:
  * Conserva el valor solo si cumple `predicate`; si no lo cumple (o ya estaba vacío), retorna `Optional.empty()`.

```java
record Company(Optional<String> name) {}
record User(Optional<Company> company) {}

String companyName = Optional.ofNullable(user)
        .flatMap(User::company)
        .flatMap(Company::name)
        .filter(n -> !n.isBlank())
        .map(String::toUpperCase)
        .orElse("NOCOMPANY");
```

### 5. Respaldo Encadenado: `Optional<T> or(Supplier<? extends Optional<? extends T>> supplier)` (Java 9+)
* **¿Por qué existe frente a `orElseGet`?** `orElseGet` desempaca a un valor `T`, terminando la cadena. **`or()`** devuelve otro **`Optional<T>`**, permitiendo encadenar múltiples niveles de respaldo de forma 100% perezosa:
  ```java
  Optional<String> config = l1Cache
          .or(() -> l2Cache)        // Solo se consulta si l1Cache está vacío
          .or(() -> databaseLookup); // Solo se consulta si l1 y l2 están vacíos
  ```

### 6. Efectos Secundarios: `ifPresent(Consumer)` e `ifPresentOrElse(Consumer, Runnable)` (Java 9+)
* **`void ifPresent(Consumer<? super T> action)`**: Ejecuta `action` solo si el valor existe.
* **`void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction)`**: Ejecuta `action` si existe, o `emptyAction` si está vacío (ideal para métricas o logging sin `if/else`).

### 7. Puente con Streams: `Stream<T> stream()` (Java 9+)
* **Qué hace:** Convierte un `Optional<T>` en un `Stream<T>` de **0 elementos** (si está vacío) o **1 elemento** (si está presente).
* **Escenario estrella:** Limpiar un `Stream<Optional<T>>` en una sola línea:
  ```java
  List<Employee> validEmployees = optionalEmployees.stream()
          .flatMap(Optional::stream) // Descarta los vacíos y desempaca los presentes!
          .toList();
  ```

### 8. Variantes Primitivas: `OptionalInt`, `OptionalLong`, `OptionalDouble`
Devueltas por `IntStream.min()`, `LongStream.max()`, `DoubleStream.average()`, etc. Usan `getAsInt()` / `getAsLong()` / `getAsDouble()`, `orElse(...)`, `orElseGet(IntSupplier)` y `stream()` (devuelve `IntStream`/`LongStream`/`DoubleStream` sin *boxing*).

---

## 9.4 Retos de Dominio y Autoevaluación (Capítulo 9)

1. **Nivel Inicial:** Genera la fila $N$ del Triángulo de Pascal como `int[]` utilizando únicamente `IntStream` y sin *autoboxing*.
2. **Nivel Intermedio:** Escribe una prueba que demuestre que `Optional.of("ok").orElse(expensiveCall())` ejecuta `expensiveCall()` innecesariamente mientras `orElseGet(this::expensiveCall)` no lo invoca jamás.
3. **Nivel Avanzado:** Encadena tres fuentes (`l1Cache`, `l2Cache`, `dbLookup`) con `Optional.or(...)` y limpia una lista `List<Optional<Employee>>` usando `.flatMap(Optional::stream)`.
4. **Nivel Staff MAANG:** Explica cómo impacta `-XX:+UseCompactObjectHeaders` ([JEP 519](https://openjdk.org/jeps/519)) al layout de memoria de `Stream<Integer>` y por qué `IntStream` sigue ganando en localidad de caché L1.

---

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

---

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

---

# Capítulo 12: Streams Paralelos, Concurrencia y Rendimiento en Java 25

> *"Agregar `.parallel()` a un Stream es la forma más fácil de escribir una línea de código en Java... y la forma más fácil de hacer tu programa 10 veces más lento o corromper silenciosamente tus datos si no entiendes cómo funciona el `ForkJoinPool`."*

---

## 12.1 Introducción desde cero: Concurrencia vs. Paralelismo

Antes de escribir una sola línea con `.parallelStream()`, aclaremos dos conceptos que suelen confundirse en entrevistas:

* **Concurrencia** es *estructurar* un sistema para gestionar múltiples tareas en progreso al mismo tiempo (por ejemplo, 10,000 peticiones HTTP esperando respuesta de red en pocos núcleos usando **Virtual Threads** de Java 21–25).
* **Paralelismo** es *ejecutar* físicamente múltiples sub-tareas en el mismo instante en varios núcleos de CPU para dividir un cálculo intensivo en pedazos más pequeños.

Los **Streams paralelos** de Java fueron diseñados específicamente para **paralelismo de datos intensivo en CPU (*CPU-bound*)**, **NO** para operaciones bloqueantes de entrada/salida (*I/O-bound*) como llamadas HTTP o consultas a base de datos.

### ¿Cómo se activa el paralelismo en un Stream?
Existen dos formas equivalentes:
1. Desde una colección: `lista.parallelStream()`
2. Cambiando el modo de un Stream existente: `stream.parallel()` (y puedes revertirlo con `.sequential()`).

> **Error Común de Programación (Última llamada gana)**
> No puedes hacer que la mitad de un pipeline sea paralela y la otra mitad secuencial. Las banderas `.parallel()` y `.sequential()` solo modifican un booleano interno del pipeline completo: **gana la última que se invoque antes de la operación terminal**.

---

## 12.2 Bajo el capó: `ForkJoinPool.commonPool()` y *Work-Stealing*

Cuando ejecutas una operación terminal en un Stream paralelo, Java sigue la estrategia **Divide y Vencerás (*Fork/Join*)**:

1. **División (*Split*)**: El `Spliterator` de la fuente intenta partir los datos por la mitad de forma recursiva hasta que cada sub-lote es suficientemente pequeño.
2. **Despacho (*Fork*)**: Las sub-tareas se envían al pool global compartido **`ForkJoinPool.commonPool()`**, que por defecto tiene `Runtime.getRuntime().availableProcessors() - 1` hilos trabajadores (más el hilo que invocó la operación terminal, sumando exactamente el número de núcleos disponibles).
3. **Robo de Trabajo (*Work-Stealing*)**: Cada hilo trabajador tiene su propia cola doble (*deque*). Si el Hilo 2 termina rápido sus tareas, **roba** una sub-tarea del final de la cola del Hilo 1 que sigue ocupado. Así ningún núcleo queda ocioso.
4. **Fusión (*Join*)**: Los resultados parciales de cada sub-tarea se combinan de vuelta usando el `combiner` de `reduce()` o `collect()`.

```text
                    [ Stream de 1,000,000 elementos ]
                                   │
                       Spliterator.trySplit()
                     ┌─────────────┴─────────────┐
               [ 0..499,999 ]            [ 500,000..999,999 ]
               (Hilo Worker-1)              (Hilo Worker-2)
               Deque: [T1, T2]              Deque: [T3] ──▶ termina rápido
                                                             y ROBA T2 de Worker-1
```

Puedes comprobar los hilos involucrados observando `Thread.currentThread().getName()`: verás nombres como `main` y `ForkJoinPool.commonPool-worker-1`, `worker-2`, etc.

---

## 12.3 ¿Cuándo conviene paralelizar? El modelo $N \times Q$ y la divisibilidad

Paralelizar **no es gratis**: dividir el `Spliterator`, agendar tareas en el `ForkJoinPool`, sincronizar cachés L1/L2 entre núcleos y fusionar resultados tiene un costo fijo (*overhead*).

### Regla empírica de Brian Goetz / Doug Lea ($N \times Q$)
* **$N$**: número de elementos en el Stream.
* **$Q$**: costo computacional por elemento (operaciones aritméticas, cifrado, cálculo geométrico, etc.).
* **Regla de oro**: Como punto de partida empírico, busca que **$N \times Q \ge 10{,}000$** antes de siquiera considerar `.parallel()`. Si $N = 100$ y solo sumas enteros ($Q \approx 1$), el Stream paralelo será significativamente **más lento** que el secuencial. **Mide siempre con JMH** (*Java Microbenchmark Harness*).

### Ranking de divisibilidad (*Splitability*) de las fuentes

El paralelismo solo escala si el `Spliterator` puede partir la estructura en pedazos balanceados rápidamente:

| Fuente de datos | Complejidad de `trySplit()` | Calidad para paralelo | Por qué |
| :--- | :--- | :--- | :--- |
| **`ArrayList`**, arreglos `T[]`/`int[]`, `IntStream.range` | **$O(1)$** | **Excelente** | Acceso aleatorio por índice; partir a la mitad es solo calcular `(inicio + fin) / 2`. |
| **`HashSet`**, **`TreeSet`**, **`ConcurrentHashMap`** | **$O(\log N)$** / cubetas | **Buena** | Se pueden dividir por ramas o cubetas, aunque con ligero desbalance ocasional. |
| **`LinkedList`**, **`Stream.iterate`**, **`BufferedReader.lines()`** | **$O(N)$** | **Pésima** | Estructura secuencial enlazada; el motor tiene que recorrer nodos uno por uno para partir lotes, anulando la ganancia. |

---

## 12.4 Los 5 peligros mortales de los Streams paralelos (y cómo corregirlos)

### Peligro 1: Estado mutable compartido (*Race Conditions*)

Nunca modifiques una colección externa no sincronizada (como un `ArrayList` o `HashMap`) desde un `.forEach()` paralelo:

```java
// ❌ INCORRECTO: Race condition catastrófica (elementos perdidos o ArrayIndexOutOfBoundsException)
List<String> unsafe = new ArrayList<>();
employees.parallelStream()
    .map(Employee::getFullName)
    .forEach(unsafe::add);

// ✅ CORRECTO (Opción 1): Usa collect() / toList() para reducción aislada por hilo
List<String> safe = employees.parallelStream()
    .map(Employee::getFullName)
    .toList();

// ✅ CORRECTO (Opción 2): Si requieres una estructura concurrente específica
ConcurrentSkipListSet<String> sortedConcurrent = employees.parallelStream()
    .map(Employee::getFullName)
    .collect(Collectors.toCollection(ConcurrentSkipListSet::new));
```

### Peligro 2: Operaciones no asociativas en `reduce()`

En un Stream paralelo, `reduce(identity, accumulator, combiner)` agrupa las operaciones en árboles arbitrarios. Si la operación no es **asociativa** (como la resta o la división), el resultado paralelo **diverge** del secuencial:

```java
List<Integer> nums = List.of(10, 2, 3, 1);

// Secuencial: (((0 - 10) - 2) - 3) - 1 = -16
int seq = nums.stream().reduce(0, (a, b) -> a - b);

// Paralelo: ((0 - 10) - (0 - 2)) - ((0 - 3) - (0 - 1)) = (-8) - (-2) = -6  (¡NO determinista!)
int par = nums.parallelStream().reduce(0, (a, b) -> a - b);
```

Además, la identidad debe cumplir `op(identity, x) == x`. Si pones `identity = 10` en una suma paralela, cada hilo sumará `10` a su partición y el total quedará inflado.

### Peligro 3: El costo oculto del orden (`findFirst`, `limit`, `forEachOrdered`)

Algunas operaciones obligan al motor paralelo a sincronizar resultados para respetar el **orden de encuentro**:
* **`forEachOrdered()`** imprime en el orden original pero sacrifica paralelismo al final; **`forEach()`** entrega máximo rendimiento sin garantía de orden.
* **`findFirst()`** debe esperar a las particiones anteriores para garantizar el primer elemento; **`findAny()`** retorna en cuanto **cualquier** hilo encuentra una coincidencia.
* **`limit(N)`** sobre un Stream paralelo ordenado es costoso. Si el orden no importa, libera la restricción explícitamente con **`.unordered()`**:

```java
List<Employee> anyThreeEngineers = employees.parallelStream()
    .unordered()
    .filter(e -> "Engineering".equals(e.getDepartment()))
    .limit(3)
    .toList();
```

### Peligro 4: *Auto-boxing* masivo y contención de `Random`

En cálculos paralelos numéricos:
1. Usa siempre Streams primitivos (`IntStream`, `LongStream`, `DoubleStream`) o `Arrays.stream(arr).parallel()` para evitar crear millones de objetos `Long`/`Double`.
2. **Nunca uses `Math.random()` ni una instancia compartida de `java.util.Random`** dentro de un Stream paralelo: internamente comparten una semilla `AtomicLong` con contención CAS brutal entre núcleos. Usa siempre **`ThreadLocalRandom.current()`**.

#### Ejemplo estrella: Aproximación paralela de $\pi$ por Monte Carlo

Lanzamos $N$ puntos aleatorios $(x, y)$ en el cuadrado unitario $[0, 1] \times [0, 1]$. La proporción de puntos que caen dentro del cuarto de círculo ($x^2 + y^2 \le 1$) aproxima $\pi / 4$:

```java
long samples = 10_000_000L;
long insideCircle = LongStream.range(0, samples)
    .parallel()
    .filter(_ -> {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double x = rnd.nextDouble();
        double y = rnd.nextDouble();
        return (x * x + y * y) <= 1.0;
    })
    .count();

double piEstimate = 4.0 * insideCircle / samples;
```

Este ejemplo tiene un $N \times Q$ enorme, fuente `LongStream.range` con división $O(1)$, cero *boxing*, cero estado compartido y generador aleatorio local por hilo. ¡Escala casi linealmente con los núcleos de tu CPU!

### Peligro 5: Inanición (*Starvation*) de `ForkJoinPool.commonPool()` con I/O

**Regla sagrada de arquitectura**: Como todos los Streams paralelos y `CompletableFuture.supplyAsync()` de tu aplicación comparten el mismo `ForkJoinPool.commonPool()`, si haces llamadas HTTP o de base de datos dentro de un `parallelStream()`, bloquearás los pocos hilos del pool global y congelarás **toda la JVM**.

#### Solución clásica (aislamiento con `ForkJoinPool` dedicado — `AutoCloseable` desde Java 19+)
Para aislar una tarea CPU-bound con un nivel de paralelismo controlado (por ejemplo, exactamente 2 hilos):

```java
try (ForkJoinPool customPool = new ForkJoinPool(2)) {
    double totalSalary = customPool.submit(() ->
        employees.parallelStream()
            .mapToDouble(Employee::getSalary)
            .sum()
    ).get();
}
```

#### Solución moderna en Java 25 LTS para tareas I/O-bound: Virtual Threads + `Gatherers.mapConcurrent`
En Java 25 **no uses `parallelStream()` para I/O**. Combina:
* **Virtual Threads** ([JEP 444](https://openjdk.org/jeps/444)) mejorados con **Synchronize Virtual Threads without Pinning** ([JEP 491](https://openjdk.org/jeps/491)), que eliminan el *pinning* en bloques `synchronized`.
* **`Gatherers.mapConcurrent(maxConcurrency, mapper)`** ([JEP 485](https://openjdk.org/jeps/485)), que despacha cada elemento en su propio hilo virtual con límite de concurrencia y preservando el orden de encuentro.
* **`ScopedValue`** ([JEP 506](https://openjdk.org/jeps/506), finalizado en Java 25), el reemplazo moderno, inmutable y seguro de `ThreadLocal` para propagar *trace IDs*, usuario autenticado o *tenant* hacia hilos virtuales e hijos sin fugas de memoria.

| Tipo de carga | Herramienta correcta en Java 25 LTS | Por qué |
| :--- | :--- | :--- |
| **CPU-bound** ($N \times Q \ge 10{,}000$, cálculos puros, Monte Carlo, ordenamiento) | `.parallelStream()` / `LongStream.range().parallel()` | Aprovecha los núcleos físicos sin crear hilos de sobra; mínimo *context switching*. |
| **I/O-bound** (HTTP, microservicios, DB, archivos remotos) | `.gather(Gatherers.mapConcurrent(max, fn))` + Virtual Threads | Nunca bloquea el `commonPool`; escala a miles de operaciones concurrentes con memoria mínima. |
| **Propagación de contexto** (Auth, Tracing) | `ScopedValue.where(KEY, val).run(...)` ([JEP 506](https://openjdk.org/jeps/506)) | Inmutable, acotado léxicamente y de costo $O(1)$ al heredar en hilos virtuales. |

---

## 12.5 Recolección concurrente: `groupingByConcurrent` y `collect` de 3 argumentos

Cuando agrupas millones de registros en paralelo, `Collectors.groupingBy()` crea un `HashMap` por hilo y luego fusiona todos los mapas en el `combiner` (lo cual puede ser costoso si hay muchas claves). Si el orden no importa, **`Collectors.groupingByConcurrent()`** usa un único `ConcurrentHashMap` compartido con bandera `CONCURRENT | UNORDERED`:

```java
ConcurrentMap<String, Long> countByDept = employees.parallelStream()
    .unordered()
    .collect(Collectors.groupingByConcurrent(
        Employee::getDepartment,
        Collectors.counting()
    ));
```

También puedes usar el `collect()` de 3 argumentos directamente sobre un `ConcurrentHashMap`:

```java
ConcurrentHashMap<String, Double> sumByDept = employees.parallelStream()
    .collect(
        ConcurrentHashMap::new,
        (map, e) -> map.merge(e.getDepartment(), e.getSalary(), Double::sum),
        (m1, m2) -> m2.forEach((k, v) -> m1.merge(k, v, Double::sum))
    );
```

---

## 12.6 Ingeniería de Rendimiento Real: Ley de Amdahl, *False Sharing*, JMH y `ManagedBlocker`

### 1. La Ley de Amdahl: Por qué 16 núcleos nunca dan 16× de velocidad
Si una fracción $P$ de tu programa es paralelizable y la fracción $(1 - P)$ es inevitablemente secuencial (por ejemplo, leer la fuente, construir el resultado final o sincronizar), la aceleración máxima teórica con $N$ núcleos es:

$$S(N) = \frac{1}{(1 - P) + \frac{P}{N}}$$

Si el 20% de tu pipeline es secuencial ($P = 0.80$), incluso con **infinitos núcleos** jamás superarás un *speedup* de $1 / 0.20 = 5\times$. Por eso eliminar barreras secuenciales (`sorted`, `limit` ordenado, `forEachOrdered`) suele dar más rendimiento que comprar más CPU.

### 2. *False Sharing* en líneas de caché de 64 bytes
Los procesadores modernos leen la RAM en bloques llamados **líneas de caché (*cache lines*) de 64 bytes**. Si dos hilos de un `parallelStream()` actualizan posiciones contiguas de un mismo arreglo (por ejemplo, `counters[0]++` en el Hilo 1 y `counters[1]++` en el Hilo 2), ambos elementos viven en la **misma línea de caché**. Cada escritura invalida la caché L1 del otro núcleo (*cache line bouncing*), haciendo el código paralelo más lento que el secuencial. **Solución:** cada hilo debe acumular en su propia instancia privada devuelta por `supplier()` en `collect()`, nunca en ranuras adyacentes compartidas.

### 3. Cómo medir correctamente con JMH (*Java Microbenchmark Harness*)
Jamás midas Streams paralelos con `System.nanoTime()` dentro de un `main()`: el compilador JIT (C2) no ha calentado el código, puede eliminar el resultado por *Dead Code Elimination* y el GC distorsiona una sola corrida. Usa siempre **JMH**:

```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class StreamParallelBenchmark {

    private long[] data;

    @Setup
    public void setup() {
        data = LongStream.rangeClosed(1, 1_000_000).toArray();
    }

    @Benchmark
    public void sequentialSum(Blackhole bh) {
        bh.consume(Arrays.stream(data).map(x -> x * x).sum());
    }

    @Benchmark
    public void parallelSum(Blackhole bh) {
        bh.consume(Arrays.stream(data).parallel().map(x -> x * x).sum());
    }
}
```

### 4. `ForkJoinPool.ManagedBlocker` vs. Virtual Threads en Java 25
Antes de Java 21, si un bloque dentro de `ForkJoinPool` tenía que bloquearse inevitablemente, la única vía segura para que el pool compensara creando un hilo extra era implementar `ForkJoinPool.ManagedBlocker`. En **Java 25 LTS**, ese patrón quedó relegado a librerías de bajo nivel: para cualquier operación I/O bloqueante, usa **`Gatherers.mapConcurrent(max, fn)` sobre Virtual Threads** ([JEP 444](https://openjdk.org/jeps/444), [JEP 485](https://openjdk.org/jeps/485), [JEP 491](https://openjdk.org/jeps/491)).

---

## 12.7 Retos de Dominio y Autoevaluación (Capítulo 12)

1. **Nivel Inicial:** Demuestra con una prueba unitaria cómo `(a, b) -> a - b` en `reduce()` produce resultados distintos en `.stream()` vs. `.parallelStream()` por violar la asociatividad.
2. **Nivel Intermedio:** Toma un pipeline con *race condition* sobre `ArrayList::add` en `parallelStream().forEach(...)` y corrígelo de dos maneras: con `.toList()` y con `Collectors.groupingByConcurrent()`.
3. **Nivel Avanzado:** Implementa una aproximación paralela de $\pi$ por Monte Carlo con 10 millones de muestras usando `LongStream.range().parallel()` y `ThreadLocalRandom.current()`, y explica por qué `Math.random()` destruiría el escalado.
4. **Nivel Staff MAANG:** Configura un microbenchmark **JMH** comparando `sequential` vs. `parallel` sobre `ArrayList` y `LinkedList`, y justifica con la Ley de Amdahl cuándo usar `Gatherers.mapConcurrent` sobre Virtual Threads en lugar de `parallelStream()`.

---

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

---

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

---

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

---

# Parte V: Arquitectura Moderna de Streams en Java 25 LTS

# Capítulo 16: Stream Gatherers ([JEP 485](https://openjdk.org/jeps/485)) — Enciclopedia de Operaciones Intermedias Personalizadas

> *"Con `Collector` en Java 8 aprendimos a personalizar cómo termina un Stream. Con `Gatherer` en Java 25 LTS, por fin podemos personalizar cómo fluye y se transforma un Stream por dentro."*

Durante más de una década (de Java 8 a Java 23), las operaciones terminales eran extensibles vía `Collector<T, A, R>`, pero las operaciones intermedias formaban un catálogo cerrado y fijo. **[JEP 485: Stream Gatherers](https://openjdk.org/jeps/485)** (diseñado por Viktor Klang, finalizado en JDK 24 y **estándar en Java 25 LTS**) resuelve definitivamente esta asimetría mediante **`Stream.gather(Gatherer<T, A, R>)`**.

En este capítulo cada uno de los **5 Gatherers integrados** de `java.util.stream.Gatherers` y cada fábrica de `Gatherer` cuentan con su **ficha técnica completa**: firma oficial JDK 25, teoría, traza visual paso a paso, escenarios reales de producción, trampas y ejemplos completos.

---

## 16.1 Anatomía Completa de `Gatherer<T, A, R>`

Un `Gatherer<T, A, R>` es una estación intermedia programable compuesta por **4 piezas** (3 opcionales + 1 obligatoria):

```text
  Upstream (T) ──▶ [ 1. initializer(): () -> A ]  (crea estado privado mutable A, opcional)
                          │
                          ▼
                   [ 2. integrator(): (A, T, Downstream<R>) -> boolean ]  (OBLIGATORIO)
                          ├── downstream.push(r)  (emite 0, 1 o N elementos R)
                          ├── retorna true   ──▶ pide el siguiente elemento T
                          └── retorna false  ──▶ ¡CORTOCIRCUITO! cancela upstream
                          │
                   [ 3. combiner(): (A, A) -> A ] (opcional: habilita ejecución paralela)
                          │
                          ▼
                   [ 4. finisher(): (A, Downstream<R>) -> void ] (al llegar al EOF: vacía buffers)
```

* **`Gatherer.Integrator.ofGreedy(...)`**: Promete que el integrador **nunca** devolverá `false` por iniciativa propia (solo se detiene si `downstream.push(...)` retorna `false` por un `.limit()` posterior). Permite a la JVM optimizar el bucle interno.
* **`Gatherer.Integrator.of(...)`**: Úsalo cuando tu propia lógica decide hacer cortocircuito devolviendo `false` (ej. presupuesto excedido).
* **`Downstream.push(R)`** y **`Downstream.isRejecting()`**: `push` envía un elemento hacia abajo; `isRejecting()` permite evitar trabajo inútil en el `finisher` si el downstream ya cerró.

---

## 16.2 Enciclopedia de los 5 Gatherers Integrados (`java.util.stream.Gatherers`)

### 1. `Gatherers.windowFixed(int windowSize)` — Lotes Disjuntos de Tamaño Fijo
* **Firma oficial:** `public static <TR> Gatherer<TR, ?, List<TR>> windowFixed(int windowSize)`
* **¿Qué hace y cómo funciona?** Acumula elementos en un búfer interno hasta reunir exactamente `windowSize` elementos y entonces emite una `List<TR>` inmodificable. Al llegar al final del Stream (`finisher`), si quedó un último lote parcial con $< \text{windowSize}$ elementos, **también lo emite** (nunca pierde datos). Lanza `IllegalArgumentException` si `windowSize <= 0`.
* **Traza visual:** `[1, 2, 3, 4, 5]` con `windowFixed(2)` → `[[1, 2], [3, 4], [5]]`.
* **Escenarios reales:** Inserciones por lotes (*batch inserts*) en JDBC, envíos paginados a APIs externas (ej. máximo 100 IDs por petición HTTP), lectura en bloques desde `Files.lines()` sin cargar el archivo completo en RAM.
```java
List<List<Integer>> batches = List.of(101, 102, 103, 104, 105, 106, 107).stream()
        .gather(Gatherers.windowFixed(3))
        .toList(); // [[101, 102, 103], [104, 105, 106], [107]]
```

### 2. `Gatherers.windowSliding(int windowSize)` — Ventanas Móviles Solapadas
* **Firma oficial:** `public static <TR> Gatherer<TR, ?, List<TR>> windowSliding(int windowSize)`
* **¿Qué hace y cómo funciona?** Mantiene una ventana deslizante que avanza **de 1 en 1**. La primera ventana se emite al reunir `windowSize` elementos; cada elemento posterior desplaza la ventana descartando el más antiguo y agregando el nuevo. Si el Stream total tiene menos de `windowSize` elementos (y al menos 1), emite una única ventana con todos los elementos disponibles.
* **Traza visual:** `[1, 2, 3, 4, 5]` con `windowSliding(3)` → `[[1, 2, 3], [2, 3, 4], [3, 4, 5]]`.
* **Escenarios reales:** Promedios móviles (*moving averages*) en finanzas, cálculo de deltas entre lecturas adyacentes (`w.get(1) - w.get(0)`), detección de violaciones de SLO (3 latencias seguidas > 100 ms) y detección de fraude (2 débitos consecutivos altos en la misma cuenta).
```java
List<Double> movingAvg = List.of(150.0, 152.0, 155.0, 153.0, 158.0).stream()
        .gather(Gatherers.windowSliding(3))
        .map(w -> w.stream().mapToDouble(Double::doubleValue).average().orElse(0.0))
        .toList();
```

### 3. `Gatherers.scan(Supplier<R> initial, BiFunction<? super R, ? super T, ? extends R> scanner)` — Acumulación Corrida (*Prefix Scan*)
* **Firma oficial:** `public static <T, R> Gatherer<T, ?, R> scan(Supplier<R> initial, BiFunction<? super R, ? super T, ? extends R> scanner)`
* **¿Qué hace y cómo funciona?** A diferencia de `reduce()` (que solo entrega el total final), `scan` aplica `state = scanner.apply(state, element)` y **emite el valor acumulado intermedio por cada elemento** (cardinalidad `1 : 1`). Si el Stream viene vacío, emite `0` elementos.
* **Traza visual:** `[1000.0, 500.0, -200.0, 750.0]` con `scan(() -> 0.0, Double::sum)` → `[1000.0, 1500.0, 1300.0, 2050.0]`.
* **Escenarios reales:** Saldo bancario corrido después de cada movimiento (*running balance*), sumas prefijas (*prefix sums*), máximo histórico acumulado (*running max*) y cálculo de *drawdown* en bolsa.
```java
List<Double> runningBalance = List.of(1000.0, 500.0, -200.0, 750.0).stream()
        .gather(Gatherers.scan(() -> 0.0, Double::sum))
        .toList(); // [1000.0, 1500.0, 1300.0, 2050.0]
```

### 4. `Gatherers.fold(Supplier<R> initial, BiFunction<? super R, ? super T, ? extends R> folder)` — Reducción Intermedia
* **Firma oficial:** `public static <T, R> Gatherer<T, ?, R> fold(Supplier<R> initial, BiFunction<? super R, ? super T, ? extends R> folder)`
* **¿Por qué existe si ya existe `Stream.reduce()`?**
  1. `reduce()` es **terminal** (cierra el Stream); **`fold()` es intermedio** (emite el resultado único dentro de un `Stream<R>` para seguir encadenando `.map()`, `.filter()`, otro `.gather()`, etc.).
  2. `fold()` **NO exige asociatividad** porque es secuencial (puedes plegar tipos distintos `T -> R` con cualquier lógica ordenada sin necesitar `combiner`).
  3. **Caso borde:** Si el Stream está vacío, `fold` emite el valor devuelto por `initial.get()` (1 elemento).
```java
List<String> summary = List.of("Cloud", "Native", "Java25").stream()
        .gather(Gatherers.fold(() -> "", (acc, s) -> acc.isEmpty() ? s : acc + "-" + s))
        .map(String::toUpperCase)
        .toList(); // ["CLOUD-NATIVE-JAVA25"]
```

### 5. `Gatherers.mapConcurrent(int maxConcurrency, Function<? super T, ? extends R> mapper)` — Concurrencia I/O en Hilos Virtuales
* **Firma oficial:** `public static <T, R> Gatherer<T, ?, R> mapConcurrent(int maxConcurrency, Function<? super T, ? extends R> mapper)`
* **¿Qué hace y cómo funciona?** Lanza cada invocación de `mapper.apply(t)` en su propio **Hilo Virtual** ([JEP 444](https://openjdk.org/jeps/444) / [JEP 491](https://openjdk.org/jeps/491)) manteniendo a lo sumo `maxConcurrency` tareas en vuelo simultáneamente, y **entrega los resultados estrictamente en el orden original de encuentro**.
* **Superpoder de cancelación:** Si encadenas `.limit(5)` o `.findFirst()`, cancela e interrumpe automáticamente los hilos virtuales restantes en cuanto obtiene los resultados necesarios.
* **Escenarios reales:** Enriquecer registros llamando a APIs HTTP, microservicios o bases de datos sin bloquear jamás `ForkJoinPool.commonPool()`.
* **Trampa importante en JDK 25 (`ScopedValue`):** Como `mapConcurrent` crea hilos virtuales no estructurados (fuera de `StructuredTaskScope`), **`ScopedValue` ([JEP 506](https://openjdk.org/jeps/506)) no se hereda automáticamente**; captura el valor en el hilo padre y re-vincúlalo dentro del lambda con `ScopedValue.where(KEY, val).call(...)` (ver Capítulo 18).
```java
List<String> responses = List.of("users", "orders", "payments", "auth").stream()
        .gather(Gatherers.mapConcurrent(4, svc -> callMicroservice(svc)))
        .toList(); // Siempre en orden exacto: [users, orders, payments, auth]
```

---

## 16.3 Construyendo Gatherers Personalizados: `ofSequential`, `of` (Paralelo) y `andThen`

### 1. `distinctBy(Function<T, K>)` — Deduplicación perezosa por clave (`ofSequential` + `ofGreedy`)
```java
public static <T, K> Gatherer<T, Set<K>, T> distinctBy(Function<? super T, ? extends K> keyExtractor) {
    return Gatherer.ofSequential(
            HashSet::new,
            Gatherer.Integrator.ofGreedy((seenKeys, element, downstream) -> {
                if (seenKeys.add(keyExtractor.apply(element))) {
                    return downstream.push(element);
                }
                return true;
            })
    );
}
```

### 2. *Run-Length Encoding* (RLE) verdadero — Uso obligatorio de `finisher` al EOF
```java
record Run<T>(T value, int count) {}

public static <T> Gatherer<T, ?, Run<T>> runLengthEncode() {
    class RleState { T current; int count = 0; boolean started = false; }
    return Gatherer.ofSequential(
            RleState::new,
            Gatherer.Integrator.ofGreedy((state, elem, downstream) -> {
                if (!state.started) {
                    state.current = elem; state.count = 1; state.started = true;
                    return true;
                }
                if (Objects.equals(state.current, elem)) {
                    state.count++;
                    return true;
                }
                boolean keepGoing = downstream.push(new Run<>(state.current, state.count));
                state.current = elem; state.count = 1;
                return keepGoing;
            }),
            (state, downstream) -> {
                if (state.started && !downstream.isRejecting()) {
                    downstream.push(new Run<>(state.current, state.count));
                }
            }
    );
}
```

### 3. Media Móvil Exponencial (EMA) y Cortocircuito Propio (`Integrator.of`)
```java
// EMA: EMA_0 = x_0, EMA_t = alpha * x_t + (1 - alpha) * EMA_{t-1}
public static Gatherer<Double, ?, Double> exponentialMovingAverage(double alpha) {
    class EmaState { boolean initialized = false; double ema = 0.0; }
    return Gatherer.ofSequential(
            EmaState::new,
            Gatherer.Integrator.ofGreedy((state, value, downstream) -> {
                state.ema = !state.initialized ? value : alpha * value + (1.0 - alpha) * state.ema;
                state.initialized = true;
                return downstream.push(state.ema);
            })
    );
}

// Cortocircuito propio (NO greedy): detiene el upstream al superar el presupuesto
public static Gatherer<Double, ?, Double> takeWhileSumAtMost(double maxBudget) {
    class Tracker { double sum = 0.0; }
    return Gatherer.ofSequential(
            Tracker::new,
            Gatherer.Integrator.of((tracker, expense, downstream) -> {
                if (tracker.sum + expense <= maxBudget) {
                    tracker.sum += expense;
                    return downstream.push(expense);
                }
                return false; // Cortocircuito inmediato
            })
    );
}
```

### 4. Gatherer Paralelizable con `Gatherer.of(...)` y `combiner`
```java
public static <T> Gatherer<T, ?, T> parallelMax(Comparator<? super T> comparator) {
    class Best { T value; boolean present; }
    return Gatherer.of(
            Best::new,
            Gatherer.Integrator.ofGreedy((best, elem, _) -> {
                if (!best.present || comparator.compare(elem, best.value) > 0) {
                    best.value = elem; best.present = true;
                }
                return true;
            }),
            (left, right) -> {
                if (!left.present) return right;
                if (!right.present) return left;
                return comparator.compare(left.value, right.value) >= 0 ? left : right;
            },
            (best, downstream) -> {
                if (best.present && !downstream.isRejecting()) {
                    downstream.push(best.value);
                }
            }
    );
}
```

### 5. Composición nativa `gathererA.andThen(gathererB)` y Sinergias Java 21–25
```java
// Componer Gatherers como bloques LEGO
Gatherer<Integer, ?, List<Integer>> batcher = Gatherers.windowFixed(2);
Gatherer<List<Integer>, ?, Integer> runningBatchSum = Gatherers.scan(
        () -> 0, (acc, batch) -> acc + batch.stream().mapToInt(Integer::intValue).sum());

List<Integer> cumulativeSums = List.of(1, 2, 3, 4, 5, 6).stream()
        .gather(batcher.andThen(runningBatchSum))
        .toList(); // [3, 10, 21]

// Sinergia JEP 431 + JEP 485: pares de rollback en O(1) sin copias
List<List<String>> rollbackPairs = List.of("Step1", "Step2", "Step3", "Step4")
        .reversed().stream()
        .gather(Gatherers.windowSliding(2))
        .toList(); // [[Step4, Step3], [Step3, Step2], [Step2, Step1]]
```

---

## 16.4 Retos de Dominio y Autoevaluación (Capítulo 16)

1. **Nivel Inicial:** Aplica los 5 Gatherers integrados (`windowFixed`, `windowSliding`, `scan`, `fold`, `mapConcurrent`) explicando qué emite cada uno cuando el Stream de entrada está vacío.
2. **Nivel Intermedio:** Construye `takeLastN(int n)` usando un `ArrayDeque` acotado que vacíe los últimos $N$ elementos en el `finisher`, y `takeWhileSumAtMost(double limit)` con `Integrator.of(...)` que cortocircuite sobre un Stream infinito.
3. **Nivel Avanzado:** Implementa *Run-Length Encoding* verdadero (`"aaabbcaaa"` → `[a:3, b:2, c:1, a:3]`) y la *Media Móvil Exponencial (EMA)* en una sola pasada.
4. **Nivel Staff MAANG:** Compón dos Gatherers con `g1.andThen(g2)`, implementa un `Gatherer.of(...)` paralelizable con `combiner`, y explica cómo propagar `ScopedValue` dentro de `Gatherers.mapConcurrent`.

---

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

---

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

---

# Apéndice Maestro: Referencia Rápida MAANG para Java 25 LTS

Guía condensada de bolsillo para consultar en tu día a día de ingeniería, entrevistas técnicas o revisiones de código de nivel Staff.

---

## A.1 Catálogo Maestro de Complejidad (Java 8 a Java 25 LTS)

| Categoría | Operación / API | Versión | ¿Stateful? | ¿Cortocircuito? | Tiempo | Espacio |
|---|---|:---:|:---:|:---:|---|---|
| **Fuente** | `Collection.stream()`, `Arrays.stream(a, i, j)` | Java 8 | No | No | $O(1)$ arranque | $O(1)$ |
| **Fuente** | `Stream.ofNullable(x)`, `Stream.iterate(s, p, f)` | Java 9 | No | No | $O(1)$ / $O(N)$ | $O(1)$ |
| **Fuente** | `SequencedCollection.reversed().stream()` | **Java 21** | No | No | **$O(1)$ arranque** | **$O(1)$** |
| **Intermedia** | `filter`, `map`, `mapToInt/Long/Double`, `peek` | Java 8 | No | No | $O(1)$ / elem | $O(1)$ |
| **Intermedia** | `flatMap`, `flatMapToInt/Long/Double` | Java 8 | No | No | $O(M)$ / elem | $O(\text{sub-stream})$ |
| **Intermedia** | `mapMulti`, `mapMultiToInt/Long/Double` | **Java 16** | No | No | $O(M)$ / elem | **$O(1)$** |
| **Intermedia** | `distinct` | Java 8 | **Sí** | No | $O(1)$ prom / elem | $O(U)$ |
| **Intermedia** | `sorted` | Java 8 | **Sí** (Barrera) | No | $O(N \log N)$ | $O(N)$ |
| **Intermedia** | `limit`, `takeWhile` | Java 8 / 9 | Sí / No | **Sí** | $O(K)$ | $O(1)$ sec. |
| **Intermedia** | `skip`, `dropWhile` | Java 8 / 9 | Sí | No | $O(N)$ | $O(1)$ |
| **Gatherer** | `Gatherers.windowFixed(n)` | **Java 24/25** | **Sí** | Hereda | $O(N)$ | $O(n)$ |
| **Gatherer** | `Gatherers.windowSliding(n)` | **Java 24/25** | **Sí** | Hereda | $O(N)$ | $O(n)$ |
| **Gatherer** | `Gatherers.scan(...)`, `Gatherers.fold(...)` | **Java 24/25** | **Sí** | Hereda | $O(N)$ | $O(1)$ |
| **Gatherer** | `Gatherers.mapConcurrent(c, fn)` | **Java 24/25** | **Sí** | **Sí** | $O(N / c)$ I/O | $O(c)$ |
| **Terminal** | `anyMatch`, `allMatch`, `noneMatch`, `findFirst`, `findAny` | Java 8 | No | **Sí** | $O(1)$–$O(N)$ | $O(1)$ |
| **Terminal** | `count` | Java 8 (opt. 9) | No | No | **$O(1)$ si `SIZED`**, si no $O(N)$ | $O(1)$ |
| **Terminal** | `min`, `max`, `sum`, `average`, `reduce` | Java 8 | No | No | $O(N)$ | $O(1)$ |
| **Terminal** | `toList()` (inmodificable, acepta `null`) | **Java 16** | Sí | No | $O(N)$ | $O(N)$ |
| **Terminal** | `collect(Collectors.*)` (`toMap`, `groupingBy`, `teeing`) | Java 8–12 | Sí | No | $O(N)$ | $O(N)$ |

---

## A.2 Matriz de Decisión Arquitectónica Java 25 LTS

| Necesidad en Producción o Entrevista | Solución Estándar Java 25 | Anti-Patrón que Debes Evitar |
|---|---|---|
| Invertir una lista o `LinkedHashSet` | `collection.reversed().stream()` | `.sorted(Comparator.reverseOrder())` |
| Verificar si algún elemento cumple `p` | `.anyMatch(p)` | `.filter(p).count() > 0` |
| Obtener mínimo o máximo | `.min(comp)` / `.max(comp)` | `.sorted(comp).findFirst()` |
| Sumar / promediar números | `.mapToDouble(...).sum()` / `.average()` | `.map(...).reduce(0.0, Double::sum)` |
| Expansión 1-a-0..N en ruta caliente | `.mapMulti(...)` / `.mapMultiToInt(...)` | `.flatMap(x -> ... ? Stream.of(...) : Stream.empty())` |
| Lotes fijos o ventanas móviles | `Gatherers.windowFixed(n)` / `windowSliding(n)` | `IntStream.rangeClosed` + `subList` |
| Totales corridos (*running total*) | `Gatherers.scan(...)` | Variable externa mutable en `.map()` |
| Llamadas HTTP / BD concurrentes | `Gatherers.mapConcurrent(max, fn)` (Virtual Threads) | `.parallelStream().map(this::httpCall)` |
| Trabajo CPU intensivo ($N \times Q > 10{,}000$) | `.parallelStream()` sobre `ArrayList` / `IntStream.range` | `.parallelStream()` sobre `LinkedList` o `Stream.iterate` |
| Ignorar parámetro en lambda / merge de `toMap` | `(existing, _) -> existing` | `(existing, ignored) -> existing` |
| Deduplicar preservando orden y extremos | `.collect(Collectors.toCollection(LinkedHashSet::new))` (`SequencedSet`) | `.distinct().toList()` si necesitas `Set` |
| Top-K sobre millones de registros | Custom `Collector` con `PriorityQueue` ($O(N \log K)$) | `.sorted().limit(k)` ($O(N \log N)$) |
| Fuente de archivos / directorios | `try (var s = Files.lines/walk/find(...))` | Olvidar `try-with-resources` (fuga de *file descriptors*) |
| Extraer grupos de Regex | `pattern.matcher(text).results()` (Java 9+) | Bucle `while (m.find())` manual |
| Caracteres Unicode / emojis | `str.codePoints()` | `str.chars()` (rompe pares sustitutos UTF-16) |
| Suma de `double` con menor error | `DoubleStream.sum()` (Suma de Kahan) | `reduce(0.0, Double::sum)` |
| `Map` con valores `null` permitidos | `collect(HashMap::new, (m,e)->m.put(...), HashMap::putAll)` | `Collectors.toMap(...)` (lanza NPE en valores `null`) |
| *Checked Exceptions* en lotes ETL | `sealed interface Result<T, E>` + `partitioningBy` | `try/catch` que traga excepciones o tumba todo el lote |
| Algoritmos DP / dos punteros / nanosegundos | Bucle `for` imperativo clásico | Forzar un Stream ilegible o lento |
| Valor por defecto costoso en `Optional` | `.orElseGet(this::compute)` | `.orElse(compute())` |

---

## A.3 Índice de Especificaciones Oficiales (OpenJDK JEPs Citados)

- **[JDK 25 Release (LTS, JSR 400)](https://openjdk.org/projects/jdk/25/):** Plataforma base LTS (GA: 16 de septiembre de 2025).
- **[JEP 431: Sequenced Collections](https://openjdk.org/jeps/431):** `SequencedCollection`, `SequencedSet`, `SequencedMap`, `reversed()`, `getFirst()`, `getLast()`.
- **[JEP 444: Virtual Threads](https://openjdk.org/jeps/444)** y **[JEP 491: Synchronize Virtual Threads without Pinning](https://openjdk.org/jeps/491):** Fundamento de concurrencia ligera para `Gatherers.mapConcurrent()`.
- **[JEP 456: Unnamed Variables & Patterns](https://openjdk.org/jeps/456):** Guion bajo `_` en lambdas, patrones de `record` y cláusulas `catch`.
- **[JEP 485: Stream Gatherers](https://openjdk.org/jeps/485):** `Stream.gather`, `Gatherer<T, A, R>`, `Gatherers` (`windowFixed`, `windowSliding`, `fold`, `scan`, `mapConcurrent`).
- **[JEP 506: Scoped Values](https://openjdk.org/jeps/506):** Compartición segura e inmutable de contexto entre hilos y subprocesos virtuales.
- **[JEP 519: Compact Object Headers](https://openjdk.org/jeps/519):** Cabeceras de objeto de 64 bits en HotSpot (`-XX:+UseCompactObjectHeaders`).

---

## A.4 Checklist Pre-Entrega (Estándar L6+ Staff / Principal)

Antes de aprobar o desplegar cualquier pipeline de Streams en producción:
1. **Cero *boxing*:** ¿Usaste `IntStream`, `DoubleStream`, `mapMultiToInt` y `Comparator.comparingInt/Double` en toda aritmética y comparación numérica?
2. **Push-down y cortocircuito:** ¿Están tus `filter()` antes de `sorted()` y `distinct()`? ¿Usaste `anyMatch`/`findFirst`/`takeWhile` en vez de materializar toda la fuente?
3. **Idiomas modernos Java 25:** ¿Usaste `reversed().stream()`, `_`, `Gatherers.*`, `mapMulti` y Record Pattern Matching donde simplifican y aceleran el pipeline?
4. **Pureza y seguridad de hilos:** ¿Están todas tus lambdas libres de efectos secundarios sobre estado externo mutable?
5. **Casos borde blindados:** ¿Validaste colecciones vacías (*vacuous truth*), valores `null`, claves duplicadas en `toMap`, `finisher` en Gatherers con estado y `combiner` en paralelo?
