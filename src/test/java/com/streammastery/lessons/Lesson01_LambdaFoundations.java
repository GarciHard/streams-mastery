package com.streammastery.lessons;

import com.streammastery.model.Employee;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * LESSON 01: LAMBDA FOUNDATIONS & FUNCTIONAL PROGRAMMING IN JAVA
 * ====================================================================================================
 *
 * <h2>1. Theoretical Foundation: Why Functional Programming in Java?</h2>
 * <p>
 * Prior to Java 8 (2014), passing behavior required verbose Anonymous Inner Classes.
 * Every anonymous class compiled into a separate {@code .class} file on disk, allocated a new object
 * instance on the heap, and required explicit boilerplate.
 * </p>
 * <p>
 * <b>Lambdas (JSR 335) changed everything:</b>
 * Under the hood, lambdas do NOT compile into anonymous inner classes. Instead, the Java compiler emits
 * the {@code invokedynamic} bytecode instruction (introduced in Java 7). At runtime, {@code LambdaMetafactory}
 * dynamically generates a call site that links the lambda to a private synthetic method using method handles.
 * This yields near-zero object allocation overhead and allows JVM JIT optimizations (inlining, escape analysis).
 * </p>
 *
 * <h2>2. The Single Abstract Method (SAM) Contract & @FunctionalInterface</h2>
 * <p>
 * An interface is a <i>Functional Interface</i> if and only if it declares <b>exactly one abstract method</b>.
 * It may contain any number of {@code default} methods, {@code static} methods, or public methods overriding
 * methods of {@link java.lang.Object} (like {@code equals()}).
 * The {@code @FunctionalInterface} annotation is optional but enforces compiler validation.
 * </p>
 *
 * <h2>3. The Standard java.util.function Catalog:</h2>
 * <ul>
 *   <li><b>{@code Predicate<T>}:</b> {@code T -> boolean} — Condition testing, filtering, matching.</li>
 *   <li><b>{@code Function<T, R>}:</b> {@code T -> R} — Transformation, projection, extraction.</li>
 *   <li><b>{@code BiFunction<T, U, R>}:</b> {@code (T, U) -> R} — Combining two arguments into a result.</li>
 *   <li><b>{@code Consumer<T>}:</b> {@code T -> void} — Side-effects, logging, mutation, event dispatching.</li>
 *   <li><b>{@code Supplier<T>}:</b> {@code () -> T} — Lazy factory, deferred computation, caching fallback.</li>
 *   <li><b>{@code UnaryOperator<T>}:</b> {@code T -> T} — Specialization of {@code Function<T, T>}.</li>
 *   <li><b>{@code BinaryOperator<T>}:</b> {@code (T, T) -> T} — Specialization of {@code BiFunction<T, T, T>}, used for reductions.</li>
 * </ul>
 *
 * <h2>4. Method References: The 4 Syntax Flavors</h2>
 * <ul>
 *   <li><b>Static:</b> {@code Math::abs} equivalent to {@code x -> Math.abs(x)}</li>
 *   <li><b>Bound Instance:</b> {@code str::length} equivalent to {@code () -> str.length()} (instance fixed at declaration)</li>
 *   <li><b>Unbound Instance:</b> {@code String::toUpperCase} equivalent to {@code (s) -> s.toUpperCase()} (first argument is the target)</li>
 *   <li><b>Constructor:</b> {@code ArrayList::new} equivalent to {@code () -> new ArrayList<>()}</li>
 * </ul>
 */
public class Lesson01_LambdaFoundations {

    /**
     * SAMPLE 1: The Core Functional Interfaces
     * What this demonstrates:
     * - How Predicate, Function, Consumer, Supplier, and BinaryOperator behave in isolation.
     * - Line-by-line explanation of inputs, outputs, and side-effects.
     */
    @Test
    @DisplayName("01. Standard Functional Interfaces: Predicate, Function, Consumer, Supplier, BinaryOperator")
    void demonstrateStandardFunctionalInterfaces() {
        Employee alice = DataFactory.getEmployees().get(0); // Alice, 10 yrs exp, Engineering, $185k
        Employee carol = DataFactory.getEmployees().get(2); // Carol, 2 yrs exp, Engineering, $120k

        // 1. Predicate<T>: takes T, evaluates boolean condition
        // Used in stream filter(), allMatch(), anyMatch(), takeWhile()
        Predicate<Employee> isTenured = emp -> emp.getYearsOfExperience() >= 10;
        assertThat(isTenured.test(alice)).as("Alice has 10 years experience").isTrue();
        assertThat(isTenured.test(carol)).as("Carol has 2 years experience").isFalse();

        // 2. Function<T, R>: takes T, returns R
        // Used in stream map(), groupingBy(), flatMap()
        Function<Employee, String> emailExtractor = Employee::getEmail;
        assertThat(emailExtractor.apply(alice)).isEqualTo("alice@google.com");

        // 3. Consumer<T>: takes T, produces side-effect (void return)
        // Used in stream forEach(), peek()
        List<String> auditLog = new ArrayList<>();
        Consumer<Employee> logger = emp -> auditLog.add("Audit: Employee #" + emp.getId() + " evaluated");
        logger.accept(alice);
        assertThat(auditLog).containsExactly("Audit: Employee #1 evaluated");

        // 4. Supplier<T>: takes nothing (), returns T lazily
        // Used in Optional.orElseGet(), Stream.generate(), Collectors.toCollection()
        Supplier<Employee> fallbackSupplier = () -> new Employee(
                999, "Fallback", "Worker", "fallback@corp.com",
                "Operations", "Associate", 22, 75000.0, "Remote", "N/A", 0, "None"
        );
        Employee fallback = fallbackSupplier.get();
        assertThat(fallback.getId()).isEqualTo(999);

        // 5. BinaryOperator<T>: takes (T, T), returns T of same type
        // Used in stream reduce(), map merging
        BinaryOperator<Double> salaryAccumulator = Double::sum;
        assertThat(salaryAccumulator.apply(120_000.0, 185_000.0)).isEqualTo(305_000.0);
    }

    /**
     * SAMPLE 2: The 4 Method Reference Variants
     * What this demonstrates:
     * - Converting lambda expressions to ultra-clean method references.
     * - The subtle difference between bound and unbound instance method references.
     */
    @Test
    @DisplayName("02. The 4 Distinct Types of Method References")
    void demonstrateMethodReferences() {
        Employee alice = DataFactory.getEmployees().get(0);

        // Variant 1: Static method reference -> Class::staticMethod
        // Lambda equivalent: str -> Double.parseDouble(str)
        Function<String, Double> parseSalary = Double::parseDouble;
        assertThat(parseSalary.apply("185000.0")).isEqualTo(185000.0);

        // Variant 2: Bound instance method reference -> instance::instanceMethod
        // The target instance 'alice' is locked at the moment of reference creation!
        // Lambda equivalent: () -> alice.getFullName()
        Supplier<String> aliceNameGetter = alice::getFullName;
        assertThat(aliceNameGetter.get()).isEqualTo("Alice Johnson");

        // Variant 3: Unbound instance method reference -> Class::instanceMethod
        // The first argument passed to the function BECOMES the target instance!
        // Lambda equivalent: (emp) -> emp.getDepartment()
        Function<Employee, String> deptExtractor = Employee::getDepartment;
        assertThat(deptExtractor.apply(alice)).isEqualTo("Engineering");

        // Variant 4: Constructor reference -> Class::new
        // Lambda equivalent: () -> new ArrayList<String>()
        Supplier<List<String>> listFactory = ArrayList::new;
        List<String> freshlyCreatedList = listFactory.get();
        assertThat(freshlyCreatedList).isEmpty();
    }

    /**
     * SAMPLE 3: Predicate Composition
     * What this demonstrates:
     * - Building rich boolean queries using .and(), .or(), .negate(), and Predicate.not().
     * - Short-circuiting behavior within composed predicates.
     */
    @Test
    @DisplayName("03. Predicate Algebra: and(), or(), negate(), Predicate.not()")
    void demonstratePredicateComposition() {
        Predicate<Employee> inEngineering = emp -> "Engineering".equalsIgnoreCase(emp.getDepartment());
        Predicate<Employee> highSalary = emp -> emp.getSalary() >= 200_000.0;
        Predicate<Employee> inSanFrancisco = emp -> "San Francisco".equalsIgnoreCase(emp.getCity());

        // Composed: (Engineering AND highSalary) OR inSanFrancisco
        Predicate<Employee> priorityCandidate = inEngineering.and(highSalary).or(inSanFrancisco);

        Employee alice = DataFactory.getEmployees().get(0); // Eng, 185k, SF -> Passes via inSanFrancisco!
        Employee bob = DataFactory.getEmployees().get(1);   // Eng, 240k, Menlo Park -> Passes via Eng AND highSalary!
        Employee frank = DataFactory.getEmployees().get(5); // Marketing, 130k, New York -> Fails!

        assertThat(priorityCandidate.test(alice)).isTrue();
        assertThat(priorityCandidate.test(bob)).isTrue();
        assertThat(priorityCandidate.test(frank)).isFalse();

        // Negation: !inEngineering
        Predicate<Employee> notInEng = inEngineering.negate();
        assertThat(notInEng.test(frank)).isTrue();

        // Java 11 Predicate.not(methodRef): cleaner than lambda negation
        Predicate<String> isBlank = String::isBlank;
        Predicate<String> isNotBlank = Predicate.not(isBlank);
        assertThat(isNotBlank.test("MAANG")).isTrue();
    }

    /**
     * SAMPLE 4: Function Pipeline Composition
     * What this demonstrates:
     * - How andThen() chains left-to-right: f.andThen(g) -> g(f(x))
     * - How compose() chains right-to-left: f.compose(g) -> f(g(x))
     */
    @Test
    @DisplayName("04. Function Pipelines: andThen() vs compose()")
    void demonstrateFunctionComposition() {
        Employee alice = DataFactory.getEmployees().get(0); // alice@google.com

        Function<Employee, String> getEmail = Employee::getEmail;
        Function<String, String> extractDomain = email -> email.substring(email.indexOf('@') + 1);
        Function<String, String> toUpper = String::toUpperCase;

        // Linear left-to-right data pipeline using andThen():
        // Employee -> email -> domain -> uppercase
        Function<Employee, String> pipelineAndThen = getEmail
                .andThen(extractDomain)
                .andThen(toUpper);

        assertThat(pipelineAndThen.apply(alice)).isEqualTo("GOOGLE.COM");

        // Mathematical compose(): f.compose(g) executes g FIRST, then f!
        Function<Double, Double> addBonus = salary -> salary + 15_000.0;
        Function<Double, Double> applyTax = salary -> salary * 0.75;

        // First applyTax, THEN addBonus: (salary * 0.75) + 15000
        Function<Double, Double> netWithBonus = addBonus.compose(applyTax);
        assertThat(netWithBonus.apply(100_000.0)).isEqualTo(90_000.0);
    }

    /**
     * SAMPLE 5: Comparator Composition (MAANG Production Idiom)
     * What this demonstrates:
     * - Type-safe, multi-key sorting using Comparator.comparing() and thenComparing().
     * - Reversing individual sorting keys.
     */
    @Test
    @DisplayName("05. Declarative Multi-Tier Comparators")
    void demonstrateComparatorComposition() {
        // MAANG Code Quality Rule: Never write raw comparator logic with nested if/else or integer subtraction!
        // Compose declarative comparators that describe the domain sorting intention:
        Comparator<Employee> maangComparator = Comparator
                .comparing(Employee::getDepartment)                              // Tier 1: Department ASC
                .thenComparing(Comparator.comparingDouble(Employee::getSalary).reversed()) // Tier 2: Salary DESC
                .thenComparingInt(Employee::getAge)                              // Tier 3: Age ASC
                .thenComparing(Employee::getLastName);                           // Tier 4: LastName ASC

        List<Employee> modifiable = new ArrayList<>(DataFactory.getEmployees());
        modifiable.sort(maangComparator);

        // Verification: First element in Engineering has the highest salary in that dept
        Employee topEng = modifiable.stream()
                .filter(e -> "Engineering".equals(e.getDepartment()))
                .findFirst()
                .orElseThrow();

        assertThat(topEng.getSalary()).isEqualTo(340000.0); // Amy King
    }

    /**
     * SAMPLE 6: Effectively Final Variables & Closures
     * What this demonstrates:
     * - Java lambdas capture variables by VALUE, not by reference.
     * - Any local variable accessed inside a lambda must be effectively final.
     */
    @Test
    @DisplayName("06. Closures and Effectively Final Constraints")
    void demonstrateEffectivelyFinal() {
        double baselineCutoff = 150_000.0; // Effectively final: never reassigned!

        // The lambda captures 'baselineCutoff' safely:
        Predicate<Employee> aboveBaseline = emp -> emp.getSalary() > baselineCutoff;

        long count = DataFactory.getEmployees().stream().filter(aboveBaseline).count();
        assertThat(count).isGreaterThan(10);

        // If you were to do: baselineCutoff = 200_000.0;
        // The compiler would immediately fail with:
        // "Local variable baselineCutoff defined in an enclosing scope must be final or effectively final"
    }
}
