package com.streammastery.lessons;

import com.streammastery.model.Employee;
import com.streammastery.model.Product;
import com.streammastery.model.Transaction;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * LESSON 06: REDUCTION & AGGREGATION (REDUCE, STATS & BIGDECIMAL)
 * ====================================================================================================
 *
 * <h2>1. Theoretical Foundation: The Monoid and Reduction Model</h2>
 * <p>
 * A <i>Reduction</i> combines an entire stream of values into a single summary result through repeated
 * application of a binary accumulator function.
 * Mathematically, parallel reduction requires the operation to form a <b>Monoid</b>:
 * <ul>
 *   <li><b>Associativity:</b> $(a \odot b) \odot c = a \odot (b \odot c)$. Crucial for splitting across threads!
 *       Operations like addition and multiplication are associative. Subtraction and division are NOT.</li>
 *   <li><b>Identity Element:</b> An element $e$ such that $e \odot a = a \odot e = a$.
 *       (0 for addition, 1 for multiplication, {@code BigDecimal.ZERO} for monetary sums).</li>
 * </ul>
 * </p>
 *
 * <h2>2. The 3 reduce() Signatures:</h2>
 * <ol>
 *   <li><b>{@code reduce(BinaryOperator<T> accumulator)}:</b> Returns {@code Optional<T>}.
 *       No identity is provided; if the stream is empty, {@code Optional.empty()} is returned safely.</li>
 *   <li><b>{@code reduce(T identity, BinaryOperator<T> accumulator)}:</b> Returns {@code T}.
 *       The identity value is returned if the stream is empty.</li>
 *   <li><b>{@code reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner)}:</b>
 *       Used when the accumulator return type $U$ differs from the stream element type $T$.
 *       The combiner is required for merging partial results across worker threads in parallel streams.</li>
 * </ol>
 *
 * <h2>3. Monetary Precision Warning (MAANG Banking / Fintech Rule):</h2>
 * <p>
 * NEVER use {@code double} or {@code float} to sum monetary amounts (salaries, ledger transactions).
 * Binary floating-point representation cannot accurately represent decimal fractions like 0.1,
 * leading to critical rounding discrepancies! Always use {@link java.math.BigDecimal}.
 * </p>
 */
public class Lesson06_ReductionAndAggregation {

    /**
     * SAMPLE 1: The 3 Signatures of reduce()
     * What this demonstrates:
     * - Practical comparison of 1-arg, 2-arg, and 3-arg reductions.
     */
    @Test
    @DisplayName("01. The Three reduce() Signatures in Action")
    void demonstrateReduceSignatures() {
        List<Integer> numbers = List.of(10, 20, 30, 40);

        // 1-arg: Returns Optional<Integer> (No identity specified)
        Optional<Integer> optSum = numbers.stream().reduce(Integer::sum);
        assertThat(optSum).contains(100);

        // 2-arg: Returns Integer directly (Identity 0 specified)
        int sumWithId = numbers.stream().reduce(0, Integer::sum);
        assertThat(sumWithId).isEqualTo(100);

        // 3-arg: Transform Employee to total salary directly without intermediate List or boxing!
        List<Employee> employees = DataFactory.getEmployees();
        Double totalSalary = employees.stream().reduce(
                0.0,
                (acc, emp) -> acc + emp.getSalary(),
                Double::sum // Combiner for parallel merging
        );
        assertThat(totalSalary).isGreaterThan(5_000_000.0);
    }

    /**
     * SAMPLE 2: Comprehensive Statistics via summaryStatistics()
     * What this demonstrates:
     * - Computing count, sum, min, max, and average in a single $O(N)$ pass.
     */
    @Test
    @DisplayName("02. DoubleSummaryStatistics: Single-Pass Aggregations")
    void demonstrateSummaryStatistics() {
        List<Product> products = DataFactory.getProducts();

        DoubleSummaryStatistics stats = products.stream()
                .mapToDouble(Product::getPrice)
                .summaryStatistics();

        assertThat(stats.getCount()).isEqualTo(30);
        assertThat(stats.getMin()).isEqualTo(16.99);
        assertThat(stats.getMax()).isEqualTo(2499.99);
        assertThat(stats.getSum()).isGreaterThan(10000.0);
        assertThat(stats.getAverage()).isBetween(300.0, 500.0);
    }

    /**
     * SAMPLE 3: Production Safe Financial Mathematics with BigDecimal
     * What this demonstrates:
     * - Precision-safe monetary summation avoiding IEEE-754 floating-point drift.
     */
    @Test
    @DisplayName("03. Safe Financial Math: BigDecimal Reduction")
    void demonstrateBigDecimalReduction() {
        List<Transaction> transactions = DataFactory.getTransactions();

        // Safe summation of currency amounts using BigDecimal
        BigDecimal totalUsdCredits = transactions.stream()
                .filter(t -> "USD".equals(t.getCurrency()) && t.getType() == Transaction.Type.CREDIT)
                .map(t -> BigDecimal.valueOf(t.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(totalUsdCredits).isGreaterThan(BigDecimal.valueOf(20_000.0));
    }

    /**
     * SAMPLE 4: Custom Reductions: Finding Longest String
     * What this demonstrates:
     * - Using binary operator to reduce a stream of strings to the longest element.
     */
    @Test
    @DisplayName("04. Custom Reduction: Longest String Extractor")
    void demonstrateCustomReduction() {
        List<String> frameworks = List.of("Spring", "Quarkus", "Micronaut", "Vert.x");

        Optional<String> longest = frameworks.stream()
                .reduce((s1, s2) -> s1.length() >= s2.length() ? s1 : s2);

        assertThat(longest).contains("Micronaut");
    }
}
