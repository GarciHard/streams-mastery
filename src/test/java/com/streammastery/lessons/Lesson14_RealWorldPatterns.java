package com.streammastery.lessons;

import com.streammastery.model.Employee;
import com.streammastery.model.Product;
import com.streammastery.model.Transaction;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;
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
 *   <li><b>mapMulti Validation:</b> Zero-allocation error collection replacing heavy flatMap stream allocations.</li>
 *   <li><b>Sliding Windows & Batching (Java 24/25 Gatherers vs Java 17 Indexing):</b>
 *       In Java 17, batching or sliding windows required clumsy {@code IntStream.rangeClosed} index math.
 *       In Java 24/25, {@code Gatherers.windowFixed()} and {@code Gatherers.windowSliding()} provide native declarative stream operators!</li>
 *   <li><b>Pattern Matching & Record Deconstruction:</b> Seamlessly unpacking domain records in stream pipelines.</li>
 * </ul>
 *
 * <h2>2. When NOT to Use Streams (MAANG Engineering Maturity):</h2>
 * <ol>
 *   <li><b>When an imperative loop is 10x clearer:</b> E.g. complex nested breaking logic or state machines.</li>
 *   <li><b>When primitive micro-optimizations matter:</b> In low-latency game engines or high-frequency trading (HFT),
 *       a raw {@code for(int i=0; i<n; i++)} loop avoiding lambda allocations and iterator overhead is preferred.</li>
 *   <li><b>When mutating shared external state:</b> If you find yourself doing {@code stream.forEach(list::add)}, stop.
 *       Use {@code stream.toList()} or proper collectors instead.</li>
 * </ol>
 */
public class Lesson14_RealWorldPatterns {

    sealed interface Event permits LoginEvent, PurchaseEvent {}
    record LoginEvent(String userId, String ipAddress) implements Event {}
    record PurchaseEvent(String userId, double amount) implements Event {}

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
     * SAMPLE 2: Comprehensive Validation Pipeline with mapMulti (Zero Allocation)
     * What this demonstrates:
     * - Using mapMulti to emit multiple validation errors without creating intermediate Stream objects.
     */
    @Test
    @DisplayName("02. Stream Data Validation: Zero-Allocation mapMulti Validation")
    void demonstrateValidationPattern() {
        record ValidationError(int employeeId, String reason) {}

        List<Employee> employees = DataFactory.getEmployees();

        // Modern Java idiom: mapMulti pushes violations directly downstream
        List<ValidationError> violations = employees.stream()
                .<ValidationError>mapMulti((e, downstream) -> {
                    if (e.getSalary() < 100_000.0) {
                        downstream.accept(new ValidationError(e.getId(), "Below minimum salary threshold"));
                    }
                    if (!e.getEmail().contains("@")) {
                        downstream.accept(new ValidationError(e.getId(), "Malformed email format"));
                    }
                })
                .toList();

        // In clean DataFactory, all pass:
        assertThat(violations).isEmpty();
    }

    /**
     * SAMPLE 3: Legacy Sliding Window (Java 17 Indexing)
     * What this demonstrates:
     * - The old Java 17 index-based technique using IntStream.rangeClosed.
     */
    @Test
    @DisplayName("03. Legacy Sliding Window: Index Math with IntStream.rangeClosed()")
    void demonstrateLegacySlidingWindow() {
        List<Double> data = List.of(10.0, 20.0, 30.0, 40.0, 50.0);
        int windowSize = 3;

        // Windows: [10,20,30] -> avg 20; [20,30,40] -> avg 30; [30,40,50] -> avg 40
        List<Double> movingAverages = IntStream.rangeClosed(0, data.size() - windowSize)
                .mapToDouble(i -> data.subList(i, i + windowSize).stream().mapToDouble(d -> d).average().orElse(0.0))
                .boxed()
                .toList();

        assertThat(movingAverages).containsExactly(20.0, 30.0, 40.0);
    }

    /**
     * SAMPLE 4: Modern Java 24/25 Sliding Window with Gatherers.windowSliding()
     * What this demonstrates:
     * - Native, declarative sliding window using JEP 485 Stream Gatherers.
     * - Zero index arithmetic, perfectly clean stream pipeline!
     */
    @Test
    @DisplayName("04. Modern Sliding Window: Native Gatherers.windowSliding() (Java 24/25)")
    void demonstrateModernSlidingWindow() {
        List<Double> data = List.of(10.0, 20.0, 30.0, 40.0, 50.0);

        // Native sliding window of size 3:
        List<Double> movingAverages = data.stream()
                .gather(Gatherers.windowSliding(3))
                .map(window -> window.stream().mapToDouble(Double::doubleValue).average().orElse(0.0))
                .toList();

        assertThat(movingAverages).containsExactly(20.0, 30.0, 40.0);
    }

    /**
     * SAMPLE 5: Record Pattern Matching in Stream Pipelines
     * What this demonstrates:
     * - Deconstructing records directly within stream operations using Java 21+ pattern matching.
     */
    @Test
    @DisplayName("05. Pattern Matching & Record Deconstruction in Streams (Java 21+)")
    void demonstrateRecordPatternMatching() {
        List<Event> eventStream = List.of(
                new LoginEvent("u1", "192.168.1.1"),
                new PurchaseEvent("u1", 250.0),
                new LoginEvent("u2", "10.0.0.1"),
                new PurchaseEvent("u2", 1500.0)
        );

        // Extract total spend from PurchaseEvents using record patterns in switch expression:
        double totalPurchaseSpend = eventStream.stream()
                .mapToDouble(event -> switch (event) {
                    case PurchaseEvent(_, double amount) -> amount;
                    case LoginEvent _ -> 0.0;
                })
                .sum();

        assertThat(totalPurchaseSpend).isEqualTo(1750.0);
    }
}
