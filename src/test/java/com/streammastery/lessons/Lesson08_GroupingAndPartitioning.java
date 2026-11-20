package com.streammastery.lessons;

import com.streammastery.model.Employee;
import com.streammastery.model.Product;
import com.streammastery.model.Transaction;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * LESSON 08: GROUPING, PARTITIONING & DOWNSTREAM COLLECTOR CHAINS
 * ====================================================================================================
 *
 * <h2>1. Theoretical Foundation: The SQL GROUP BY of Java Streams</h2>
 * <p>
 * {@code Collectors.groupingBy()} is the centerpiece of backend business logic in Java.
 * It classifies stream elements by a key extractor function into a {@code Map<K, V>}.
 * By default, {@code groupingBy(classifier)} produces a {@code Map<K, List<T>>}.
 * </p>
 *
 * <h2>2. The Downstream Collector Engine (Composable Aggregation):</h2>
 * <p>
 * The true power of grouping emerges when you attach a <b>Downstream Collector</b>:
 * <pre>
 * groupingBy(classifier, downstreamCollector)
 * </pre>
 * Common downstream collectors:
 * <ul>
 *   <li>{@code counting()}: Counts items per group ($O(1)$ space per bucket).</li>
 *   <li>{@code summingDouble() / averagingDouble()}: Numeric aggregations per group.</li>
 *   <li>{@code maxBy(comparator)}: Extracts the maximum element per group wrapped in Optional.</li>
 *   <li>{@code mapping(mapper, downstream)}: Transforms elements before feeding them to the next collector!</li>
 *   <li>{@code filtering(predicate, downstream)} (Java 9+): Filters elements within buckets without discarding empty keys.</li>
 *   <li>{@code flatMapping(mapper, downstream)} (Java 9+): Unpacks collections per bucket.</li>
 * </ul>
 * </p>
 *
 * <h2>3. partitioningBy (Boolean Specialization):</h2>
 * <p>
 * {@code partitioningBy(Predicate<T>)} splits elements into exactly two buckets: {@code true} and {@code false}.
 * It returns a {@code Map<Boolean, List<T>>}. Both keys are guaranteed to be present in the resulting map,
 * even if one of the buckets is empty!
 * </p>
 */
public class Lesson08_GroupingAndPartitioning {

    /**
     * SAMPLE 1: Single-Level Grouping by Department
     * What this demonstrates:
     * - Basic classification: Department -> List<Employee>
     */
    @Test
    @DisplayName("01. Basic Grouping: Department -> List<Employee>")
    void demonstrateSimpleGrouping() {
        List<Employee> employees = DataFactory.getEmployees();

        Map<String, List<Employee>> byDept = employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment));

        assertThat(byDept).containsKeys("Engineering", "Data Science", "Design", "Marketing", "Product");
        assertThat(byDept.get("Engineering")).hasSize(20);
    }

    /**
     * SAMPLE 2: Downstream Aggregations: Headcount & Average Salary
     * What this demonstrates:
     * - Using counting() and averagingDouble() as downstream collectors.
     */
    @Test
    @DisplayName("02. Downstream Collectors: counting() & averagingDouble()")
    void demonstrateDownstreamAggregations() {
        List<Employee> employees = DataFactory.getEmployees();

        // Department -> Headcount
        Map<String, Long> headcount = employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));
        assertThat(headcount.get("Engineering")).isEqualTo(20L);

        // Department -> Average Salary
        Map<String, Double> avgSalary = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.averagingDouble(Employee::getSalary)
                ));
        assertThat(avgSalary.get("Engineering")).isGreaterThan(200_000.0);
    }

    /**
     * SAMPLE 3: Downstream Mapping: Department -> Set<ProgrammingLanguage>
     * What this demonstrates:
     * - Transforming values inside each bucket using Collectors.mapping().
     */
    @Test
    @DisplayName("03. Downstream Mapping: Department -> Set<String> of Tech Stack")
    void demonstrateDownstreamMapping() {
        List<Employee> employees = DataFactory.getEmployees();

        Map<String, Set<String>> deptLanguages = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.mapping(Employee::getProgrammingLanguage, Collectors.toSet())
                ));

        assertThat(deptLanguages.get("Engineering")).contains("Java", "Python", "Go", "C++", "Swift");
    }

    /**
     * SAMPLE 4: Multi-Level Nested Grouping (Department -> City -> List<Employee>)
     * What this demonstrates:
     * - Nesting groupingBy inside another groupingBy for multi-dimensional aggregation.
     */
    @Test
    @DisplayName("04. Multi-Level Grouping: Department -> (City -> List<Employee>)")
    void demonstrateMultiLevelGrouping() {
        List<Employee> employees = DataFactory.getEmployees();

        Map<String, Map<String, List<Employee>>> multiLevel = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.groupingBy(Employee::getCity)
                ));

        assertThat(multiLevel.get("Engineering")).containsKey("Seattle");
        assertThat(multiLevel.get("Engineering").get("Seattle")).isNotEmpty();
    }

    /**
     * SAMPLE 5: partitioningBy for Binary Classification
     * What this demonstrates:
     * - Splitting into true and false partitions based on salary threshold.
     */
    @Test
    @DisplayName("05. PartitioningBy: True / False Binary Classification")
    void demonstratePartitioning() {
        List<Employee> employees = DataFactory.getEmployees();

        // High earners (> $200k) vs Standard earners
        Map<Boolean, List<Employee>> partitioned = employees.stream()
                .collect(Collectors.partitioningBy(e -> e.getSalary() > 200_000.0));

        assertThat(partitioned.get(true)).isNotEmpty();
        assertThat(partitioned.get(false)).isNotEmpty();
        assertThat(partitioned.get(true)).allMatch(e -> e.getSalary() > 200_000.0);
    }
}
