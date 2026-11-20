package com.streammastery.exercises;

import com.streammastery.model.Employee;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * EXERCISE 01: LAMBDA FOUNDATIONS & FUNCTIONAL INTERFACES (20 EXERCISES)
 * ====================================================================================================
 *
 * Welcome to Lesson 01 Exercises.
 * Every exercise has an incremental difficulty rating from [★☆☆☆☆] (Beginner) to [★★★★★] (MAANG Staff).
 *
 * NOTE: Exercises 01 through 12 contain your verified solutions.
 * Exercises 13 through 20 have been added to complete your MAANG-level mastery of functional programming!
 */
public class Exercise01_LambdaFoundations {

    @Test
    @DisplayName("01 [★☆☆☆☆] Write a Predicate to filter high earners")
    void exercise01_predicateBasic() {
        // Problem: Define a Predicate that tests if an employee earns strictly more than $200,000.
        // TODO: Replace null with your lambda or method reference
        Predicate<Employee> isHighEarner = employee -> employee.getSalary() > 200_000;

        Employee bob = DataFactory.getEmployees().get(1);   // Bob earns $240,000
        Employee carol = DataFactory.getEmployees().get(2); // Carol earns $120,000
        assertThat(isHighEarner).isNotNull();
        assertThat(isHighEarner.test(bob)).isTrue();
        assertThat(isHighEarner.test(carol)).isFalse();
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Write a Function to format employee badges")
    void exercise02_functionBasic() {
        // Problem: Create a function that transforms an Employee into "[ID] FullName (Dept)"
        // Example: "[1] Alice Johnson (Engineering)"
        // TODO: Replace null with your lambda
        Function<Employee, String> badgeFormatter = employee ->
                // Slightly better to concatenate using the character rather than String-format.
                "[" + employee.getId() + "] " +  employee.getFullName() + " (" + employee.getDepartment() + ")";
//                String.format("[%d] %s (%s)", employee.getId(), employee.getFullName(), employee.getDepartment());

        Employee alice = DataFactory.getEmployees().get(0);
        assertThat(badgeFormatter).isNotNull();
        assertThat(badgeFormatter.apply(alice)).isEqualTo("[1] Alice Johnson (Engineering)");
    }

    @Test
    @DisplayName("03 [★☆☆☆☆] Use Consumer to accumulate department headcount")
    void exercise03_consumerBasic() {
        // Problem: Create a Consumer that increments the count in headcountMap for the employee's department.
        Map<String, Integer> headcountMap = new HashMap<>();
        // TODO: Replace null with a Consumer<Employee> that updates headcountMap
        Consumer<Employee> counter = dept ->
                // Instead of use put and then getOrDefault, merge or compute would be a better approach.
                headcountMap.merge(dept.getDepartment(), 1, Integer::sum);
//                headcountMap.put(dept.getDepartment(), headcountMap.getOrDefault(dept.getDepartment(), 0) + 1);



        assertThat(counter).isNotNull();
        DataFactory.getEmployees().forEach(counter);
        assertThat(headcountMap.get("Engineering")).isGreaterThan(5);
        assertThat(headcountMap.get("Marketing")).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("04 [★★☆☆☆] Use Supplier to produce fallback default employee")
    void exercise04_supplierBasic() {
        // Problem: Create a Supplier that produces an Employee with id=-1, name="Guest", "User",
        // email="guest@system.local", department="Guest", title="Visitor", age=0, salary=0.0,
        // city="Unknown", gender="N/A", yearsOfExperience=0, programmingLanguage="None"
        // TODO: Replace null with your Supplier
        Supplier<Employee> guestSupplier = () -> new Employee(
                -1, "Guest", "User", "guest@system.local", "Guest",
                "Visitor", 0, 0.0, "Unknown", "N/A", 0, "None"
        );

        assertThat(guestSupplier).isNotNull();
        Employee guest = guestSupplier.get();
        assertThat(guest.getId()).isEqualTo(-1);
        assertThat(guest.getFirstName()).isEqualTo("Guest");
        assertThat(guest.getSalary()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("05 [★★☆☆☆] Method Reference: Static method")
    void exercise05_staticMethodRef() {
        // Problem: Use a method reference to Double's static method to parse a string representation of salary.
        // TODO: Replace null with Double::parseDouble
        Function<String, Double> parser = Double::parseDouble;

        assertThat(parser).isNotNull();
        assertThat(parser.apply("195000.50")).isEqualTo(195_000.50);
    }

    @Test
    @DisplayName("06 [★★☆☆☆] Method Reference: Unbound instance method")
    void exercise06_unboundInstanceMethodRef() {
        // Problem: Use an unbound instance method reference to extract programmingLanguage from Employee
        // TODO: Replace null with Employee::getProgrammingLanguage
        Function<Employee, String> languageExtractor = Employee::getProgrammingLanguage;

        Employee alice = DataFactory.getEmployees().get(0);
        assertThat(languageExtractor).isNotNull();
        assertThat(languageExtractor.apply(alice)).isEqualTo("Java");
    }

    @Test
    @DisplayName("07 [★★★☆☆] Predicate Composition: Complex screening")
    void exercise07_predicateComposition() {
        // Problem: An employee is eligible for the Staff+ committee if:
        // (Department is "Engineering" AND YearsOfExperience >= 10) OR (Salary >= 300_000.0)
        Predicate<Employee> isEng = emp -> "Engineering".equalsIgnoreCase(emp.getDepartment());
        Predicate<Employee> isTenured = emp -> emp.getYearsOfExperience() >= 10;
        Predicate<Employee> isExecPay = emp -> emp.getSalary() >= 300_000.0;

        // TODO: Compose the three predicates using .and() and .or()
        Predicate<Employee> isEligible = isEng.and(isTenured).or(isExecPay);

        Employee alice = DataFactory.getEmployees().get(0); // Eng, 10 yrs, 185k -> TRUE
        Employee carol = DataFactory.getEmployees().get(2); // Eng, 2 yrs, 120k -> FALSE
        Employee eve = DataFactory.getEmployees().get(4);   // Eng, 20 yrs, 320k -> TRUE

        assertThat(isEligible).isNotNull();
        assertThat(isEligible.test(alice)).isTrue();
        assertThat(isEligible.test(carol)).isFalse();
        assertThat(isEligible.test(eve)).isTrue();
    }

    @Test
    @DisplayName("08 [★★★☆☆] Function Composition: Data pipeline")
    void exercise08_functionAndThen() {
        // Problem: Build a function that takes an Employee:
        // 1. Extracts email
        // 2. Extracts domain part (substring after '@')
        // 3. Converts domain to uppercase
        // Compose using andThen().
        Function<Employee, String> emailStep = Employee::getEmail;
        Function<String, String> domainStep = email -> email.substring(email.indexOf('@') + 1);
        Function<String, String> upperStep = String::toUpperCase;

        // TODO: Compose emailStep, domainStep, upperStep using andThen
//        Function<Employee, String> employeeDomainUpper = domainStep.compose(emailStep).andThen(upperStep);
        Function<Employee, String> employeeDomainUpper = emailStep.andThen(domainStep).andThen(upperStep);

        Employee alice = DataFactory.getEmployees().get(0); // alice@google.com
        assertThat(employeeDomainUpper).isNotNull();
        assertThat(employeeDomainUpper.apply(alice)).isEqualTo("GOOGLE.COM");
    }

    @Test
    @DisplayName("09 [★★★★☆] Compose with compose(): Pre-processing")
    void exercise09_functionCompose() {
        // Problem: compose() executes g before f: f.compose(g) => f(g(x))
        // Given f: Double -> String formatting salary as "$185,000"
        // Given g: Employee -> Double extracting salary
        Function<Double, String> currencyFormatter = s -> String.format("$%,.0f", s);
        Function<Employee, Double> salaryExtractor = Employee::getSalary;

        // TODO: Use currencyFormatter.compose(...) to create a pipeline
        Function<Employee, String> formattedSalary = currencyFormatter.compose(salaryExtractor);

        Employee alice = DataFactory.getEmployees().get(0);
        assertThat(formattedSalary).isNotNull();
        assertThat(formattedSalary.apply(alice)).isEqualTo("$185,000");
    }

    @Test
    @DisplayName("10 [★★★★☆] MAANG Multi-tier Comparator Chain")
    void exercise10_comparatorChaining() {
        // Problem: Build a Comparator that sorts Employees by:
        // 1. Department ASCENDING
        // 2. Salary DESCENDING
        // 3. Age ASCENDING
        // 4. LastName ASCENDING
        // TODO: Build the comparator using Comparator.comparing(...).thenComparing(...)
        Comparator<Employee> multiTierComparator = Comparator
                .comparing(Employee::getDepartment)
                .thenComparing(Comparator.comparingDouble(Employee::getSalary).reversed())
                .thenComparingInt(Employee::getAge)
                .thenComparing(Employee::getLastName);

        assertThat(multiTierComparator).isNotNull();
        List<Employee> list = new ArrayList<>(DataFactory.getEmployees());
        list.sort(multiTierComparator);

        // Verification: within Engineering, highest salary comes first
        Employee firstEng = list.stream().filter(e -> "Engineering".equals(e.getDepartment())).findFirst().orElseThrow();
        assertThat(firstEng.getSalary()).isGreaterThanOrEqualTo(300_000.0);
    }

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);

        default <W> TriFunction<T, U, V, W> andThen(Function<? super R, ? extends W> after) {
            Objects.requireNonNull(after);
            return (T t, U u, V v) -> after.apply(apply(t, u, v));
        }
    }

    @Test
    @DisplayName("11 [★★★★★] Custom Generic Functional Interface: TriFunction")
    void exercise11_customFunctionalInterface() {
        // Problem: Implement a TriFunction that takes (String firstName, String lastName, Double salary)
        // and creates a concise summary: "LASTNAME, FirstName earns $XXX,XXX.XX"
        // Example: "JOHNSON, Alice earns $185,000.00"
        // TODO: Replace null with your TriFunction implementation
        TriFunction<String, String, Double, String> payrollSummarizer = (firstName,lastName,salary) ->
                String.format("%s, %s earns $%,.2f", lastName.toUpperCase(), firstName, salary);

        assertThat(payrollSummarizer).isNotNull();
        String summary = payrollSummarizer.apply("Alice", "Johnson", 185000.0);
        assertThat(summary).isEqualTo("JOHNSON, Alice earns $185,000.00");

        // Verify andThen works
        TriFunction<String, String, Double, Integer> lengthFunc = payrollSummarizer.andThen(String::length);
        assertThat(lengthFunc.apply("Alice", "Johnson", 185000.0)).isEqualTo(summary.length());
    }

    @Test
    @DisplayName("12 [★★★★★] Dynamic Predicate Builder with Closure")
    void exercise12_dynamicPredicateBuilder() {
        // Problem: Write a higher-order function (method or Function) that takes a Map<String, String> of
        // criteria (e.g. "department" -> "Engineering", "city" -> "Seattle") and returns a single composed
        // Predicate<Employee> checking all matching criteria dynamically!
        Function<Map<String, String>, Predicate<Employee>> predicateBuilder = criteria -> {
            // TODO: Iterate over criteria entries, building a composed Predicate that matches all.
            // If criteria is empty, match everything (emp -> true).

            Predicate<Employee> combined = employee -> true;

            for ( Map.Entry<String, String> entry : criteria.entrySet() ) {

                String key = entry.getKey();
                String value = entry.getValue();

                combined = switch (key) {
                    case "department" -> combined.and(emp -> emp.getDepartment().equalsIgnoreCase(value));
                    case "city" -> combined.and(emp -> emp.getCity().equalsIgnoreCase(value));
                    default -> combined;
                };
            }

            return combined;
        };

        assertThat(predicateBuilder).isNotNull();

        Map<String, String> query = Map.of("department", "Engineering", "city", "Seattle");
        Predicate<Employee> queryPredicate = predicateBuilder.apply(query);
        assertThat(queryPredicate).isNotNull();

        long matchCount = DataFactory.getEmployees().stream().filter(queryPredicate).count();
        assertThat(matchCount).isEqualTo(4); // Carol, Henry, Mia, Ryan in Seattle Eng
    }

    // ================================================================================================
    // NEW EXPANDED EXERCISES 13 - 20 (EXTENDING YOUR ZERO-TO-MAANG PATHWAY)
    // ================================================================================================

    @Test
    @DisplayName("13 [★★☆☆☆] BinaryOperator: Find the higher-earning employee between two")
    void exercise13_binaryOperatorMax() {
        // Problem: Create a BinaryOperator<Employee> that takes two employees and returns the one
        // with the strictly higher salary (if equal, return the first one).
        // TODO: Replace null with your BinaryOperator lambda or method

        // 1st attempt - Without using Comparators & BinaryOperations
        BinaryOperator<Employee> higherEarner = (emp1, emp2) ->
                emp1.getSalary().compareTo(emp2.getSalary()) >= 0 ? emp1 : emp2;

        // 2nd attempt. Cleaner solution. But we ain't see Comparators yet
//        BinaryOperator<Employee> higherEarner =
//                BinaryOperator.maxBy(Comparator.comparing(Employee::getSalary));

        Employee alice = DataFactory.getEmployees().get(0); // $185,000
        Employee bob = DataFactory.getEmployees().get(1);   // $240,000

        assertThat(higherEarner).isNotNull();
        assertThat(higherEarner.apply(alice, bob)).isSameAs(bob);
        assertThat(higherEarner.apply(bob, alice)).isSameAs(bob);
    }

    @Test
    @DisplayName("14 [★★☆☆☆] BiPredicate: Check if employee language matches a tech requirement")
    void exercise14_biPredicateLanguageMatch() {
        // Problem: Define a BiPredicate<Employee, String> that evaluates to true if the employee's
        // programming language equals (case-insensitive) the specified target language string.
        // TODO: Replace null with your BiPredicate
        BiPredicate<Employee, String> knowsLanguage = (employee, language) ->
                employee.getProgrammingLanguage().equalsIgnoreCase(language);

        Employee alice = DataFactory.getEmployees().get(0); // Java
        assertThat(knowsLanguage).isNotNull();
        assertThat(knowsLanguage.test(alice, "java")).isTrue();
        assertThat(knowsLanguage.test(alice, "python")).isFalse();
    }

    @Test
    @DisplayName("15 [★★★☆☆] Bound Method Reference: Set Membership ($O(1)$ Filter Predicate)")
    void exercise15_boundMethodRefMembership() {
        // Senior Architectural Context:
        // Bound instance method references (instance::method) capture 'instance' as the receiver (this).
        // In high-throughput stream pipelines at MAANG, pre-computing an immutable Set of allowed keys
        // and using 'set::contains' as a Predicate<T> is the canonical O(1) membership filter.
        //
        // Note: As you correctly noticed, 'prefix::startsWith' failed because it inverted the receiver:
        // it checked prefix.startsWith(candidate) instead of candidate.startsWith(prefix)!
        // In contrast, 'set::contains' captures 'set' as the receiver, taking candidate as the argument.
        Set<String> techHubs = Set.of("San Francisco", "Seattle", "New York");

        // TODO: Replace null with a bound method reference to techHubs::contains
        Predicate<String> isTechHub = techHubs::contains;

        assertThat(isTechHub).isNotNull();
        assertThat(isTechHub.test("Seattle")).isTrue();
        assertThat(isTechHub.test("San Francisco")).isTrue();
        assertThat(isTechHub.test("Miami")).isFalse();
    }

    @Test
    @DisplayName("16 [★★★☆☆] Predicate.not() with custom validator")
    void exercise16_predicateNotFilter() {
        // Problem: Given a predicate that checks if an employee is a Junior Engineer,
        // use Predicate.not() to derive a nonJunior predicate.
        Predicate<Employee> isJunior = emp -> emp.getTitle().contains("Junior");

        // TODO: Use Predicate.not(isJunior)
        Predicate<Employee> isNotJunior = Predicate.not(isJunior);

        Employee alice = DataFactory.getEmployees().get(0); // Senior Engineer
        Employee carol = DataFactory.getEmployees().get(2); // Junior Engineer

        assertThat(isNotJunior).isNotNull();
        assertThat(isNotJunior.test(alice)).isTrue();
        assertThat(isNotJunior.test(carol)).isFalse();
    }

    @Test
    @DisplayName("17 [★★★★☆] UnaryOperator Memoization/Caching Simulator")
    void exercise17_unaryOperatorTransformation() {
        // Problem: Create a UnaryOperator<String> that normalizes an email address:
        // 1. Trims whitespace
        // 2. Converts to lowercase
        // 3. Replaces multiple consecutive spaces with a single space (if any)
        // TODO: Replace null with your UnaryOperator implementation
        final Pattern WHITESPACE = Pattern.compile("\\s+");

        UnaryOperator<String> emailNormalizer = email ->
                WHITESPACE.matcher(email.trim()).replaceAll(" ").toLowerCase(Locale.ROOT);
//                email.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);

        assertThat(emailNormalizer).isNotNull();
        assertThat(emailNormalizer.apply("  Alice.Johnson@GOOGLE.COM  ")).isEqualTo("alice.johnson@google.com");
    }

    @Test
    @DisplayName("18 [★★★★☆] BiFunction Pipeline: Department Budget Calculator")
    void exercise18_biFunctionBudget() {
        // Problem: Implement a BiFunction<List<Employee>, String, Double> that takes the master employee
        // list and a department name, and calculates the total payroll for that department.
        // Return 0.0 if no employees belong to that department.
        // TODO: Implement the BiFunction
        BiFunction<List<Employee>, String, Double> departmentBudgetCalculator =
                (employeesList, department) -> {
                    Double totalPayroll = 0.0;

                    Predicate<Employee> belongsToDepartment = employee -> employee.getDepartment().equalsIgnoreCase(department);
                    Function<Employee, Double> employeeSalary = Employee::getSalary;
                    BinaryOperator<Double> salaryAccumulator = Double::sum;

                    for ( Employee emp : employeesList ) {
                        if ( belongsToDepartment.test(emp) ) {
                            totalPayroll = salaryAccumulator.apply( totalPayroll, employeeSalary.apply(emp) );
                        }
                    }

                    return totalPayroll;
                };

        assertThat(departmentBudgetCalculator).isNotNull();
        Double engBudget = departmentBudgetCalculator.apply(DataFactory.getEmployees(), "Engineering");
        Double unknownBudget = departmentBudgetCalculator.apply(DataFactory.getEmployees(), "NonExistentDept");

        assertThat(engBudget).isGreaterThan(4_000_000.0);
        assertThat(unknownBudget).isEqualTo(0.0);
    }

    @Test
    @DisplayName("19 [★★★★★] Currying in Java: Convert (A, B) -> C into A -> (B -> C)")
    void exercise19_curriedFunction() {
        // Problem: Currying decomposes a function of multiple arguments into a chain of single-argument functions.
        // Implement a curried Function<String, Function<Double, String>>:
        // First call takes prefix (e.g. "USD: ")
        // Second call takes salary (e.g. 185000.0)
        // Returns "USD: $185,000.00"
        // TODO: Implement curried function
        Function<String, Function<Double, String>> curriedFormatter = null;

        assertThat(curriedFormatter).isNotNull();
        Function<Double, String> usdFormatter = curriedFormatter.apply("USD: ");
        assertThat(usdFormatter).isNotNull();
        assertThat(usdFormatter.apply(185000.0)).isEqualTo("USD: $185,000.00");
    }

    @Test
    @DisplayName("20 [★★★★★] Declarative Rule Engine: Combining Predicate and Consumer")
    void exercise20_declarativeRuleEngine() {
        // Problem: Build a lightweight Business Rule record:
        // record Rule(Predicate<Employee> condition, Consumer<Employee> action) {}
        // Given an employee, if the condition passes, the action is executed!
        record Rule(Predicate<Employee> condition, Consumer<Employee> action) {}

        List<String> bonusNotifications = new ArrayList<>();

        // Create 2 rules:
        // Rule 1: If salary > 300_000, add "[EXECUTIVE BONUS] FullName" to bonusNotifications
        // Rule 2: If yearsOfExperience >= 20, add "[VETERAN RECOGNITION] FullName" to bonusNotifications
        // TODO: Build the list of rules
        List<Rule> rules = null;

        assertThat(rules).isNotNull();
        Employee eve = DataFactory.getEmployees().get(4); // Eve: 320k, 20 yrs exp -> triggers BOTH rules!
        Employee carol = DataFactory.getEmployees().get(2); // Carol: 120k, 2 yrs exp -> triggers NEITHER!

        Consumer<Employee> engine = emp -> rules.forEach(rule -> {
            if (rule.condition().test(emp)) {
                rule.action().accept(emp);
            }
        });

        engine.accept(eve);
        engine.accept(carol);

        assertThat(bonusNotifications).containsExactly(
                "[EXECUTIVE BONUS] Eve Davis",
                "[VETERAN RECOGNITION] Eve Davis"
        );
    }
}
