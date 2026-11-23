package com.streammastery.lessons;

import com.streammastery.model.Employee;
import com.streammastery.model.Product;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * LESSON 05: SORTING, ORDERING & COMPARATOR ARCHITECTURE
 * ====================================================================================================
 *
 * <h2>1. Theoretical Foundation: Stateful Intermediate Operations</h2>
 * <p>
 * {@code sorted()} is fundamentally different from {@code filter()} or {@code map()}.
 * While filter and map are <i>stateless</i> and stream elements one-by-one lazily,
 * {@code sorted()} is a <b>Stateful Intermediate Operation</b>. It CANNOT emit its first output element
 * until it has read and buffered <b>EVERY SINGLE UPSTREAM ELEMENT</b> in memory!
 * </p>
 * <p>
 * <b>Algorithm:</b> Java Streams use Dual-Pivot Quicksort / TimSort under the hood:
 * <ul>
 *   <li>Time Complexity: $O(N \log N)$ average and worst case for TimSort.</li>
 *   <li>Space Complexity: $O(N)$ because the entire stream must be copied into a temporary array.</li>
 *   <li><b>Stability Guarantee:</b> Java's TimSort is <i>stable</i>; elements with equal comparison keys
 *       guarantee preservation of their original encounter order.</li>
 * </ul>
 * </p>
 *
 * <h2>2. Best Practices for MAANG-Grade Pipelines:</h2>
 * <ol>
 *   <li><b>Push Down Filter Before Sort:</b> Always filter out unwanted elements BEFORE invoking {@code sorted()}.
 *       Sorting 1,000,000 items and then filtering is 100x slower than filtering down to 10,000 items and then sorting.</li>
 *   <li><b>Primitive Comparators:</b> Always prefer {@code Comparator.comparingDouble(Employee::getSalary)}
 *       and {@code thenComparingInt(Employee::getAge)} over generic object comparators to eliminate auto-boxing.</li>
 *   <li><b>Null Safety:</b> Protect sorting pipelines against {@code NullPointerException} using
 *       {@code Comparator.nullsFirst()} or {@code Comparator.nullsLast()}.</li>
 *   <li><b>Java 21 Sequenced Collections Optimization:</b> When reversing an already-ordered collection
 *       (e.g. {@code List}, {@code LinkedHashSet}), avoid {@code .sorted(Comparator.reverseOrder())} (which is $O(N \log N)$
 *       and buffers $O(N)$ elements)! Instead, call {@code collection.reversed().stream()} for an $O(1)$ zero-copy view stream!</li>
 * </ol>
 */
public class Lesson05_SortingAndOrdering {

    /**
     * SAMPLE 1: Natural Order and Reverse Sorting
     * What this demonstrates:
     * - Sorting elements that implement java.lang.Comparable (Strings, Numbers).
     * - Inverting sort order with Comparator.reverseOrder().
     */
    @Test
    @DisplayName("01. Natural Order vs Reversed Order Sorting")
    void demonstrateBasicSorting() {
        List<String> languages = new ArrayList<>(DataFactory.getProgrammingLanguages());

        // Natural sort (alphabetical A-Z):
        List<String> sortedAsc = languages.stream().sorted().toList();
        assertThat(sortedAsc).isSorted();

        // Reverse natural sort (Z-A):
        List<String> sortedDesc = languages.stream().sorted(Comparator.reverseOrder()).toList();
        assertThat(sortedDesc.get(0)).isEqualTo("TypeScript");
        assertThat(sortedDesc.get(sortedDesc.size() - 1)).isEqualTo("C#");
    }

    /**
     * SAMPLE 2: Multi-Tier Declarative Comparator Composition
     * What this demonstrates:
     * - Composing primary, secondary, tertiary, and quaternary sorting keys.
     * - Reversing individual keys without affecting others.
     */
    @Test
    @DisplayName("02. Multi-Tier Sorting using Comparator.comparing() & thenComparing()")
    void demonstrateMultiTierSorting() {
        List<Employee> employees = DataFactory.getEmployees();

        // Sort by Department ASC, then Salary DESC, then Age ASC, then LastName ASC
        Comparator<Employee> maangSort = Comparator
                .comparing(Employee::getDepartment)
                .thenComparing(Comparator.comparingDouble(Employee::getSalary).reversed())
                .thenComparingInt(Employee::getAge)
                .thenComparing(Employee::getLastName);

        List<Employee> sortedEmployees = employees.stream()
                .sorted(maangSort)
                .toList();

        assertThat(sortedEmployees).isSortedAccordingTo(maangSort);
        // Verify Engineering highest salary comes first in Engineering block:
        Employee topEng = sortedEmployees.stream()
                .filter(e -> "Engineering".equals(e.getDepartment()))
                .findFirst()
                .orElseThrow();
        assertThat(topEng.getSalary()).isEqualTo(340000.0); // Amy King
    }

    /**
     * SAMPLE 3: Null-Safe Sorting Strategies
     * What this demonstrates:
     * - Handling nullable fields or null elements without throwing NullPointerException.
     * - Positioning nulls at the beginning (nullsFirst) or end (nullsLast).
     */
    @Test
    @DisplayName("03. Null-Safe Sorting with Comparator.nullsFirst() and nullsLast()")
    void demonstrateNullSafeSorting() {
        List<String> namesWithNulls = Arrays.asList("Google", null, "Meta", "Amazon", null, "Apple");

        // nullsLast ensures all nulls are pushed to the tail of the stream:
        List<String> sortedNullsLast = namesWithNulls.stream()
                .sorted(Comparator.nullsLast(Comparator.naturalOrder()))
                .toList();

        assertThat(sortedNullsLast.get(0)).isEqualTo("Amazon");
        assertThat(sortedNullsLast.get(1)).isEqualTo("Apple");
        assertThat(sortedNullsLast.get(4)).isNull();
        assertThat(sortedNullsLast.get(5)).isNull();
    }

    /**
     * SAMPLE 4: Top-K Pattern: sorted() + limit()
     * What this demonstrates:
     * - Finding the top K elements by sorting in reverse order and taking a limit.
     * - Understanding the time complexity tradeoff ($O(N \log N)$ vs Heap $O(N \log K)$).
     */
    @Test
    @DisplayName("04. Top-K Pattern: Finding Extreme Elements with sorted().limit(K)")
    void demonstrateTopK() {
        List<Product> products = DataFactory.getProducts();

        // Top 3 most expensive products
        List<Product> top3Expensive = products.stream()
                .sorted(Comparator.comparingDouble(Product::getPrice).reversed())
                .limit(3)
                .toList();

        assertThat(top3Expensive).hasSize(3);
        assertThat(top3Expensive.get(0).getName()).isEqualTo("MacBook Pro 16\"");
        assertThat(top3Expensive.get(0).getPrice()).isEqualTo(2499.99);
    }

    /**
     * SAMPLE 5: Case-Insensitive String Ordering
     * What this demonstrates:
     * - Using String.CASE_INSENSITIVE_ORDER to avoid ASCII case discrepancies.
     */
    @Test
    @DisplayName("05. Case-Insensitive Alphabetical Sorting")
    void demonstrateCaseInsensitiveSorting() {
        List<String> mixedCase = List.of("apple", "Banana", "cherry", "Date");

        List<String> caseInsensitiveSorted = mixedCase.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        assertThat(caseInsensitiveSorted).containsExactly("apple", "Banana", "cherry", "Date");
    }

    /**
     * SAMPLE 6: Java 21 Sequenced Collections - O(1) Zero-Copy Reverse Stream
     * What this demonstrates:
     * - Contrast between:
     *     - Legacy Java 17: .sorted(Comparator.reverseOrder()) -> O(N log N) time, O(N) memory allocation.
     *     - Modern Java 21+: collection.reversed().stream() -> O(1) time view, O(1) memory!
     */
    @Test
    @DisplayName("06. Java 21 Sequenced Collections: O(1) Zero-Copy Reverse Streaming")
    void demonstrateReversedStream() {
        List<String> orderedMilestones = List.of("Ideation", "Architecture", "Implementation", "Production");

        // Java 21: reversed() creates a reverse-order view in O(1) time without copying.
        // Streaming that view begins immediate reverse emission with ZERO sort buffering:
        List<String> backwards = orderedMilestones.reversed().stream().toList();

        assertThat(backwards).containsExactly("Production", "Implementation", "Architecture", "Ideation");
        assertThat(backwards.get(0)).isEqualTo("Production");
        assertThat(backwards.get(backwards.size() - 1)).isEqualTo("Ideation");
    }
}
