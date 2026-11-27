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
