package com.streammastery.exercises;

import com.streammastery.model.Employee;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ====================================================================================================
 * EXERCISE 09: PRIMITIVE STREAMS & OPTIONALS (20 EXERCISES)
 * ====================================================================================================
 */
public class Exercise09_PrimitiveStreamsAndOptionals {

    @Test
    @DisplayName("01 [★☆☆☆☆] Sum numbers 1 to 100 with IntStream")
    void exercise01_sumIntStream() {
        // TODO: IntStream.rangeClosed(1, 100).sum()
        int sum = 0;

        assertThat(sum).isEqualTo(5050);
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Convert List<Integer> to IntStream and find max")
    void exercise02_listToIntStreamMax() {
        List<Integer> list = List.of(45, 12, 89, 32, 99, 21);
        // TODO: list.stream().mapToInt(Integer::intValue).max().orElse(-1)
        int max = -1;

        assertThat(max).isEqualTo(99);
    }

    @Test
    @DisplayName("03 [★☆☆☆☆] Optional creation and safe fallback with orElse")
    void exercise03_optionalOrElse() {
        String nullableValue = null;
        // TODO: Optional.ofNullable(nullableValue).orElse("MAANG Fallback")
        String result = null;

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("MAANG Fallback");
    }

    @Test
    @DisplayName("04 [★★☆☆☆] Compute Factorial of 10 using LongStream.rangeClosed")
    void exercise04_factorial() {
        // 10! = 1 * 2 * ... * 10 = 3628800L
        // TODO: LongStream.rangeClosed(1, 10).reduce(1L, (a, b) -> a * b)
        long factorial10 = 0L;

        assertThat(factorial10).isEqualTo(3628800L);
    }

    @Test
    @DisplayName("05 [★★☆☆☆] DoubleStream Summary Statistics of Engineering Salaries")
    void exercise05_salaryStats() {
        // TODO: Filter Engineering, mapToDouble salary, get summaryStatistics
        DoubleSummaryStatistics stats = null;

        assertThat(stats).isNotNull();
        assertThat(stats.getCount()).isEqualTo(20);
        assertThat(stats.getAverage()).isGreaterThan(200_000.0);
    }

    @Test
    @DisplayName("06 [★★☆☆☆] Chain Optional.map to safely uppercase first employee name")
    void exercise06_optionalMapChain() {
        // TODO: Find first employee, map to getFirstName, map to toUpperCase, orElse("UNKNOWN")
        String upperName = null;

        assertThat(upperName).isNotNull();
        assertThat(upperName).isEqualTo("ALICE");
    }

    @Test
    @DisplayName("07 [★★★☆☆] Prove orElseGet() lazy behavior")
    void exercise07_proveOrElseGetLazy() {
        List<String> sideEffects = new ArrayList<>();
        Optional<String> value = Optional.of("Available");

        // TODO: Use orElseGet with a lambda that adds "called" to sideEffects and returns "Fallback"
        String result = value.orElseGet(() -> {
            sideEffects.add("called");
            return "Fallback";
        });

        assertThat(result).isEqualTo("Available");
        assertThat(sideEffects).isEmpty(); // MUST BE EMPTY to prove laziness!
    }

    @Test
    @DisplayName("08 [★★★☆☆] IntStream mapToObj: Map numbers 1..5 to word representations")
    void exercise08_mapToObjWords() {
        Map<Integer, String> words = Map.of(1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five");
        // TODO: IntStream.rangeClosed(1, 5).mapToObj(words::get).toList()
        List<String> numberWords = null;

        assertThat(numberWords).isNotNull();
        assertThat(numberWords).containsExactly("One", "Two", "Three", "Four", "Five");
    }

    @Test
    @DisplayName("09 [★★★☆☆] Safe nested property navigation with Optional.flatMap")
    void exercise09_nestedOptionalFlatMap() {
        class Company { Optional<String> name = Optional.of("TechCorp"); }
        class User { Optional<Company> company = Optional.of(new Company()); }

        Optional<User> user = Optional.of(new User());

        // TODO: user.flatMap(u -> u.company).flatMap(c -> c.name).orElse("NoCompany")
        String companyName = null;

        assertThat(companyName).isNotNull();
        assertThat(companyName).isEqualTo("TechCorp");
    }

    @Test
    @DisplayName("10 [★★★★☆] IntStream prime number generator")
    void exercise10_generatePrimes() {
        // Find all prime numbers between 2 and 50 inclusive using IntStream
        // TODO: IntStream.rangeClosed(2, 50).filter(isPrime).boxed().toList()
        List<Integer> primes = null;

        assertThat(primes).isNotNull();
        assertThat(primes).containsExactly(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47);
    }

    @Test
    @DisplayName("11 [★★★★☆] Optional.or() Fallback chain (Java 9+)")
    void exercise11_optionalOrChain() {
        Optional<String> primaryCache = Optional.empty();
        Optional<String> secondaryCache = Optional.empty();
        Optional<String> database = Optional.of("DataFromDB");

        // TODO: primaryCache.or(() -> secondaryCache).or(() -> database).orElse("Default")
        String loaded = null;

        assertThat(loaded).isNotNull();
        assertThat(loaded).isEqualTo("DataFromDB");
    }

    @Test
    @DisplayName("12 [★★★★★] Production Resilient Lookup Chain with Optional")
    void exercise12_productionResilientLookup() {
        // Problem: Lookup employee with ID 999 (does not exist) -> fallback to ID 1 -> get department -> uppercase
        // TODO: Implement lookup pipeline
        String dept = null;

        assertThat(dept).isNotNull();
        assertThat(dept).isEqualTo("ENGINEERING");
    }

    @Test
    @DisplayName("13 [★☆☆☆☆] Calculate average of IntStream with orElse(0.0)")
    void exercise13_intStreamAverage() {
        // TODO: IntStream.of(10, 20, 30, 40).average().orElse(0.0)
        double avg = 0.0;

        assertThat(avg).isEqualTo(25.0);
    }

    @Test
    @DisplayName("14 [★★☆☆☆] Convert IntStream to DoubleStream via asDoubleStream()")
    void exercise14_asDoubleStream() {
        // TODO: IntStream.rangeClosed(1, 5).asDoubleStream().toArray()
        double[] doubles = null;

        assertThat(doubles).isNotNull();
        assertThat(doubles).containsExactly(1.0, 2.0, 3.0, 4.0, 5.0);
    }

    @Test
    @DisplayName("15 [★★☆☆☆] Optional.ifPresentOrElse (Java 9+)")
    void exercise15_ifPresentOrElse() {
        List<String> audit = new ArrayList<>();
        Optional<String> user = Optional.of("Alice");

        // TODO: user.ifPresentOrElse(name -> audit.add("Found: " + name), () -> audit.add("Missing"))

        assertThat(audit).containsExactly("Found: Alice");
    }

    @Test
    @DisplayName("16 [★★★☆☆] Optional.filter(): Validate string length before extraction")
    void exercise16_optionalFilter() {
        Optional<String> password = Optional.of("secret");
        // Check if password has length >= 8, if so keep, otherwise empty
        // TODO: password.filter(p -> p.length() >= 8)
        Optional<String> strongPassword = Optional.of("dummy");

        assertThat(strongPassword).isEmpty();
    }

    @Test
    @DisplayName("17 [★★★☆☆] LongStream summary statistics on byte sizes")
    void exercise17_byteSizeStats() {
        long[] fileSizes = {1024L, 2048L, 4096L, 8192L, 16384L};
        // TODO: Arrays.stream(fileSizes).summaryStatistics()
        LongSummaryStatistics stats = null;

        assertThat(stats).isNotNull();
        assertThat(stats.getSum()).isEqualTo(31744L);
        assertThat(stats.getMax()).isEqualTo(16384L);
    }

    @Test
    @DisplayName("18 [★★★★☆] Generate Pascal's Triangle row using IntStream")
    void exercise18_pascalsRow() {
        // Row 4: [1, 4, 6, 4, 1] using combinations C(4, k) = 4! / (k! * (4-k)!)
        // TODO: Generate row 4 as int[]
        int[] row4 = null;

        assertThat(row4).isNotNull();
        assertThat(row4).containsExactly(1, 4, 6, 4, 1);
    }

    @Test
    @DisplayName("19 [★★★★☆] Flatten stream of Optional employees to List<Employee>")
    void exercise19_flattenOptionalEmployees() {
        List<Optional<Employee>> candidateList = List.of(
                Optional.of(DataFactory.getEmployees().get(0)),
                Optional.empty(),
                Optional.of(DataFactory.getEmployees().get(1)),
                Optional.empty()
        );

        // TODO: Use candidateList.stream().flatMap(Optional::stream).toList()
        List<Employee> validEmployees = null;

        assertThat(validEmployees).isNotNull();
        assertThat(validEmployees).hasSize(2);
        assertThat(validEmployees.get(0).getFirstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("20 [★★★★★] Optional Monadic Pipeline: Safe Division and Square Root")
    void exercise20_monadicComputation() {
        // Given numerator 100.0 and denominator 4.0:
        // Step 1: Divide 100.0 by denominator (if denom == 0, return Optional.empty())
        // Step 2: Compute Math.sqrt of result (if result < 0, return Optional.empty())
        // Result: sqrt(100 / 4) = sqrt(25) = 5.0
        // TODO: Build safe monadic computation
        Optional<Double> result = Optional.empty();

        assertThat(result).contains(5.0);
    }
}
