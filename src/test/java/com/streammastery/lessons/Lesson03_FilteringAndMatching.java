package com.streammastery.lessons;

import com.streammastery.model.Employee;
import com.streammastery.model.Product;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * LESSON 03: FILTERING, SLICING & MATCHING IN DEPTH
 * ====================================================================================================
 *
 * <h2>1. High-Performance Filtering (The Push-Down Predicate Model)</h2>
 * <p>
 * In distributed databases and stream engines (Spark, Flink, Java Streams), the most critical rule
 * of performance optimization is: <b>Filter early, filter aggressively</b>.
 * Every element eliminated at step 1 does not consume CPU cycles or memory in subsequent steps (like sorting or grouping).
 * </p>
 *
 * <h2>2. Intermediate Operations Anatomy:</h2>
 * <ul>
 *   <li><b>{@code filter(Predicate<T>)}:</b> Stateless intermediate op. Evaluates each element independently.</li>
 *   <li><b>{@code distinct()}:</b> Stateful intermediate op! Relies on {@code hashCode()} and {@code equals()}.
 *       Maintains an internal {@code HashSet} of seen elements. Memory consumption is $O(U)$ where $U$ is distinct count.</li>
 *   <li><b>{@code limit(long maxSize)}:</b> Stateful short-circuiting op. Once $maxSize$ elements pass, traversal halts.</li>
 *   <li><b>{@code skip(long n)}:</b> Stateful op. Discards the first $n$ elements, then emits remaining elements.</li>
 *   <li><b>{@code takeWhile(Predicate<T>)} (Java 9+):</b> Takes elements as long as predicate returns true.
 *       CRITICAL: Stops at the FIRST false on ordered streams!</li>
 *   <li><b>{@code dropWhile(Predicate<T>)} (Java 9+):</b> Skips elements as long as predicate returns true.
 *       Emits EVERYTHING after the first false!</li>
 * </ul>
 *
 * <h2>3. Terminal Matching Operations:</h2>
 * <ul>
 *   <li>{@code anyMatch(Predicate)}: Short-circuits on FIRST true. Returns false if stream is empty.</li>
 *   <li>{@code allMatch(Predicate)}: Short-circuits on FIRST false. Returns TRUE on empty stream (Vacuous Truth)!</li>
 *   <li>{@code noneMatch(Predicate)}: Short-circuits on FIRST true. Returns TRUE on empty stream.</li>
 *   <li>{@code findFirst()}: Deterministic. Honors encounter order.</li>
 *   <li>{@code findAny()}: Non-deterministic in parallel streams. Maximum concurrency throughput!</li>
 * </ul>
 */
public class Lesson03_FilteringAndMatching {

    /**
     * SAMPLE 1: Multi-Criteria Filtering and Deduplication
     * What this demonstrates:
     * - Chaining multiple filter() operations vs a single combined predicate.
     * - Using distinct() with objects that implement equals/hashCode.
     */
    @Test
    @DisplayName("01. Multi-Stage Filtering and distinct() Deduplication")
    void demonstrateFilterAndDistinct() {
        List<Employee> employees = DataFactory.getEmployees();

        // Chaining multiple distinct filter stages keeps each predicate focused and readable:
        List<Employee> seniorJavaDevsInBay = employees.stream()
                .filter(e -> "Engineering".equals(e.getDepartment()))
                .filter(e -> "Java".equals(e.getProgrammingLanguage()))
                .filter(e -> e.getYearsOfExperience() >= 10)
                .toList();

        assertThat(seniorJavaDevsInBay).allMatch(e -> e.getYearsOfExperience() >= 10);
        assertThat(seniorJavaDevsInBay).extracting(Employee::getProgrammingLanguage).containsOnly("Java");

        // distinct(): Preserves encounter order on ordered streams, removes duplicate cities
        List<String> distinctCities = employees.stream()
                .map(Employee::getCity)
                .distinct()
                .sorted()
                .toList();

        assertThat(distinctCities).doesNotHaveDuplicates();
        assertThat(distinctCities).contains("San Francisco", "Seattle", "New York", "Cupertino", "Menlo Park", "Los Gatos");
    }

    /**
     * SAMPLE 2: Production Pagination with skip() and limit()
     * What this demonstrates:
     * - Implementing zero-allocation pagination for REST APIs.
     * - Formula: skip(pageIndex * pageSize).limit(pageSize)
     */
    @Test
    @DisplayName("02. Pagination Pipeline with skip() and limit()")
    void demonstratePagination() {
        List<Product> products = DataFactory.getProducts();

        int pageSize = 5;
        int pageIndex = 1; // 0-based index: Page 1 = items 5 to 9

        List<Product> page1 = products.stream()
                .skip((long) pageIndex * pageSize)
                .limit(pageSize)
                .toList();

        assertThat(page1).hasSize(pageSize);
        assertThat(page1.get(0).getId()).isEqualTo(6);  // Air Jordan 1 Retro
        assertThat(page1.get(4).getId()).isEqualTo(10); // Air Max 90
    }

    /**
     * SAMPLE 3: Java 9 takeWhile() and dropWhile()
     * What this demonstrates:
     * - The behavior of takeWhile and dropWhile on pre-sorted data.
     * - Contrast with standard filter() which would evaluate ALL elements.
     */
    @Test
    @DisplayName("03. Ordered Slicing: takeWhile() and dropWhile() (Java 9+)")
    void demonstrateTakeWhileAndDropWhile() {
        // Given an ascending sorted list of prices:
        List<Double> sortedPrices = List.of(16.99, 19.99, 38.00, 44.99, 55.00, 75.00, 149.00, 999.99);

        // takeWhile: Takes budget items (< $50.00). Aborts immediately at 55.00!
        List<Double> budgetItems = sortedPrices.stream()
                .takeWhile(price -> price < 50.0)
                .toList();
        assertThat(budgetItems).containsExactly(16.99, 19.99, 38.00, 44.99);

        // dropWhile: Drops budget items (< $50.00). Emits from 55.00 onwards to the end!
        List<Double> premiumItems = sortedPrices.stream()
                .dropWhile(price -> price < 50.0)
                .toList();
        assertThat(premiumItems).containsExactly(55.00, 75.00, 149.00, 999.99);
    }

    /**
     * SAMPLE 4: Short-Circuiting Matching (allMatch, anyMatch, noneMatch)
     * What this demonstrates:
     * - Boolean evaluations that terminate as soon as the outcome is known.
     * - Vacuous truth in allMatch on empty streams.
     */
    @Test
    @DisplayName("04. Boolean Matching: allMatch(), anyMatch(), noneMatch()")
    void demonstrateMatchingOperations() {
        List<Employee> employees = DataFactory.getEmployees();

        // allMatch: Short-circuits on first false
        boolean allAdults = employees.stream().allMatch(e -> e.getAge() >= 18);
        assertThat(allAdults).isTrue();

        // anyMatch: Short-circuits on first true
        boolean hasSuperEarner = employees.stream().anyMatch(e -> e.getSalary() > 300_000.0);
        assertThat(hasSuperEarner).isTrue();

        // noneMatch: Short-circuits on first true
        boolean noneEarnsNegative = employees.stream().noneMatch(e -> e.getSalary() < 0.0);
        assertThat(noneEarnsNegative).isTrue();

        // TRAP: Vacuous truth on empty stream!
        // allMatch on an empty stream returns TRUE by definition in mathematical logic!
        boolean emptyAllMatch = Stream.<String>empty().allMatch(s -> s.length() > 100);
        assertThat(emptyAllMatch).as("Empty stream allMatch is vacuously true").isTrue();
    }

    /**
     * SAMPLE 5: findFirst() vs findAny() in Sequential and Parallel Streams
     * What this demonstrates:
     * - findFirst() guarantees the encounter order.
     * - findAny() provides massive throughput advantages in parallel pipelines.
     */
    @Test
    @DisplayName("05. Extraction: findFirst() vs findAny()")
    void demonstrateFindFirstVsFindAny() {
        List<Employee> employees = DataFactory.getEmployees();

        // findFirst on sequential stream: Guaranteed to return the first matching element (Alice)
        Optional<Employee> firstEngineer = employees.stream()
                .filter(e -> "Engineering".equals(e.getDepartment()))
                .findFirst();

        assertThat(firstEngineer).isPresent();
        assertThat(firstEngineer.get().getFirstName()).isEqualTo("Alice");

        // findAny on parallel stream: Any thread finding a match immediately short-circuits
        Optional<Employee> anyEngineer = employees.parallelStream()
                .filter(e -> "Engineering".equals(e.getDepartment()))
                .findAny();

        assertThat(anyEngineer).isPresent();
        assertThat(anyEngineer.get().getDepartment()).isEqualTo("Engineering");
    }
}
