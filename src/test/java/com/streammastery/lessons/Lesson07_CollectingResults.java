package com.streammastery.lessons;

import com.streammastery.model.Employee;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ====================================================================================================
 * LESSON 07: COLLECTING RESULTS, COLLECTORS CATALOG & IMMUTABILITY
 * ====================================================================================================
 *
 * <h2>1. Mutable Reduction (collect vs reduce)</h2>
 * <p>
 * While {@code reduce()} combines elements into an immutable value by repeatedly returning new objects,
 * {@code collect()} performs a <b>Mutable Reduction</b>: it folds elements into a single mutable container
 * (like an {@code ArrayList}, {@code HashSet}, or {@code HashMap}).
 * This avoids the astronomical garbage allocation of creating new collections on every single step.
 * </p>
 *
 * <h2>2. The List Immutability Spectrum (MAANG Architecture Rule):</h2>
 * <ul>
 *   <li><b>{@code Stream.toList()} (Java 16+):</b> Returns an unmodifiable list. Null elements ARE allowed.
 *       Compact internal implementation (often backed by a compact array). Recommended default!</li>
 *   <li><b>{@code Collectors.toList()}:</b> Returns a mutable {@code java.util.ArrayList}.
 *       No immutability guarantee. Use only if downstream code needs to modify the list.</li>
 *   <li><b>{@code Collectors.toUnmodifiableList()} (Java 10+):</b> Rejects null elements (throws NPE).
 *       Guaranteed unmodifiable.</li>
 * </ul>
 *
 * <h2>3. Collectors.toMap Collision Handling:</h2>
 * <p>
 * {@code Collectors.toMap(keyMapper, valueMapper)} throws {@link IllegalStateException} if duplicate keys exist!
 * In production systems, <b>ALWAYS</b> specify the merge function:
 * {@code Collectors.toMap(keyMapper, valueMapper, (existing, replacement) -> ...)}
 * </p>
 */
public class Lesson07_CollectingResults {

    /**
     * SAMPLE 1: toList() vs Collectors.toList() vs toUnmodifiableList()
     * What this demonstrates:
     * - The subtle differences in mutability, null tolerance, and memory efficiency.
     */
    @Test
    @DisplayName("01. Mutability Differences: toList() vs Collectors.toList()")
    void demonstrateListCollectors() {
        List<Employee> employees = DataFactory.getEmployees();

        // 1. Collectors.toList(): Mutable ArrayList
        List<String> modifiable = employees.stream().map(Employee::getFirstName).collect(Collectors.toList());
        modifiable.add("NewRecruit");
        assertThat(modifiable).contains("NewRecruit");

        // 2. Stream.toList() (Java 16+): Unmodifiable!
        List<String> unmodifiable = employees.stream().map(Employee::getFirstName).toList();
        assertThatThrownBy(() -> unmodifiable.add("Illegal"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * SAMPLE 2: toMap() with Collision Merge Function
     * What this demonstrates:
     * - Resolving duplicate keys when building maps from streams.
     * - Using custom Map supplier (TreeMap for sorted keys).
     */
    @Test
    @DisplayName("02. Collectors.toMap with Collision Merge Function & Map Supplier")
    void demonstrateToMapWithCollisionHandling() {
        List<Employee> employees = DataFactory.getEmployees();

        // Map Department -> Highest Paid Employee in that department
        Map<String, Employee> topEarnerPerDept = employees.stream()
                .collect(Collectors.toMap(
                        Employee::getDepartment,
                        emp -> emp,
                        (existing, replacement) -> replacement.getSalary() > existing.getSalary() ? replacement : existing,
                        TreeMap::new // Sorted map by department name
                ));

        assertThat(topEarnerPerDept).containsKey("Engineering");
        assertThat(topEarnerPerDept.get("Engineering").getSalary()).isEqualTo(340000.0); // Amy King
    }

    /**
     * SAMPLE 3: Joining Strings with Delimiters, Prefixes, and Suffixes
     * What this demonstrates:
     * - Formatting stream outputs into CSV or formatted brackets.
     */
    @Test
    @DisplayName("03. Joining Strings with Prefix and Suffix")
    void demonstrateJoining() {
        List<String> languages = List.of("Java", "Go", "Rust");

        String formatted = languages.stream()
                .collect(Collectors.joining(", ", "[Tech: ", "]"));

        assertThat(formatted).isEqualTo("[Tech: Java, Go, Rust]");
    }

    /**
     * SAMPLE 4: Post-Processing with collectingAndThen()
     * What this demonstrates:
     * - Applying a finishing function immediately after collection.
     * - Building unmodifiable sets or computing proportions.
     */
    @Test
    @DisplayName("04. collectingAndThen: Post-Processing Accumulated Data")
    void demonstrateCollectingAndThen() {
        List<Employee> employees = DataFactory.getEmployees();

        // Collect distinct cities to set, then freeze in Collections.unmodifiableSet
        Set<String> unmodifiableCities = employees.stream()
                .map(Employee::getCity)
                .collect(Collectors.collectingAndThen(
                        Collectors.toSet(),
                        Collections::unmodifiableSet
                ));

        assertThat(unmodifiableCities).contains("San Francisco", "Seattle");
        assertThatThrownBy(() -> unmodifiableCities.add("Paris"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
