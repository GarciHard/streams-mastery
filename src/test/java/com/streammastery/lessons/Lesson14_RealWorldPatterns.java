package com.streammastery.lessons;

import com.streammastery.model.Employee;
import com.streammastery.model.Product;
import com.streammastery.model.Transaction;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * LESSON 14: REAL-WORLD PRODUCTION PATTERNS & MAANG IDIOMS
 * ====================================================================================================
 *
 * <h2>1. High-Impact Architectural Patterns</h2>
 * <ul>
 *   <li><b>Collectors.teeing() (Java 12+):</b> Eliminates the need for multiple passes over data.
 *       Feeds elements into two independent downstream collectors concurrently and merges their results!</li>
 *   <li><b>Batching / Chunking:</b> Partitioning continuous streams into chunks of size $K$ for batch database insertions.</li>
 *   <li><b>Domain Validation Aggregation:</b> Collecting all validation errors instead of fail-fast throwing on the first error.</li>
 *   <li><b>Sliding Window Averages:</b> Streaming moving averages over financial or telemetry data series.</li>
 * </ul>
 *
 * <h2>2. When NOT to Use Streams (MAANG Engineering Maturity):</h2>
 * <ol>
 *   <li><b>When an imperative loop is 10x clearer:</b> E.g. complex nested breaking logic or state machines.</li>
 *   <li><b>When primitive micro-optimizations matter:</b> In low-latency game engines or high-frequency trading (HFT),
 *       a raw {@code for(int i=0; i<n; i++)} loop avoiding lambda allocations and iterator overhead is preferred.</li>
 *   <li><b>When mutating shared external state:</b> If you find yourself doing {@code stream.forEach(list::add)}, stop.
 *       Use {@code stream.toList()} instead.</li>
 * </ol>
 */
public class Lesson14_RealWorldPatterns {

    /**
     * SAMPLE 1: Java 12 Collectors.teeing()
     * What this demonstrates:
     * - Computing min and max salary simultaneously in a SINGLE iteration pass.
     */
    @Test
    @DisplayName("01. Collectors.teeing(): Dual Aggregation in a Single Pass (Java 12+)")
    void demonstrateTeeingCollector() {
        List<Product> products = DataFactory.getProducts();

        record PriceRange(double min, double max) {}

        // teeing feeds each product to minBy AND maxBy simultaneously:
        PriceRange range = products.stream().collect(
                Collectors.teeing(
                        Collectors.minBy(Comparator.comparingDouble(Product::getPrice)),
                        Collectors.maxBy(Comparator.comparingDouble(Product::getPrice)),
                        (minOpt, maxOpt) -> new PriceRange(
                                minOpt.map(Product::getPrice).orElse(0.0),
                                maxOpt.map(Product::getPrice).orElse(0.0)
                        )
                )
        );

        assertThat(range.min()).isEqualTo(16.99);
        assertThat(range.max()).isEqualTo(2499.99);
    }

    /**
     * SAMPLE 2: Comprehensive Validation Pipeline
     * What this demonstrates:
     * - Collecting all domain violations instead of throwing on the first error.
     */
    @Test
    @DisplayName("02. Stream Data Validation: Collecting Multiple Domain Violations")
    void demonstrateValidationPattern() {
        record ValidationError(int employeeId, String reason) {}

        List<Employee> employees = DataFactory.getEmployees();

        // Audit employees for compliance violations:
        List<ValidationError> violations = employees.stream()
                .flatMap(e -> {
                    List<ValidationError> errors = new ArrayList<>();
                    if (e.getSalary() < 100_000.0) {
                        errors.add(new ValidationError(e.getId(), "Below minimum salary threshold"));
                    }
                    if (!e.getEmail().contains("@")) {
                        errors.add(new ValidationError(e.getId(), "Malformed email format"));
                    }
                    return errors.stream();
                })
                .toList();

        // In clean DataFactory, all pass:
        assertThat(violations).isEmpty();
    }

    /**
     * SAMPLE 3: Sliding Window Moving Average
     * What this demonstrates:
     * - Implementing a sliding window of size K over ordered data using IntStream.rangeClosed.
     */
    @Test
    @DisplayName("03. Sliding Window Moving Average over Timeseries")
    void demonstrateSlidingWindow() {
        List<Double> data = List.of(10.0, 20.0, 30.0, 40.0, 50.0);
        int windowSize = 3;

        // Windows: [10,20,30] -> avg 20; [20,30,40] -> avg 30; [30,40,50] -> avg 40
        List<Double> movingAverages = IntStream.rangeClosed(0, data.size() - windowSize)
                .mapToDouble(i -> data.subList(i, i + windowSize).stream().mapToDouble(d -> d).average().orElse(0.0))
                .boxed()
                .toList();

        assertThat(movingAverages).containsExactly(20.0, 30.0, 40.0);
    }
}
