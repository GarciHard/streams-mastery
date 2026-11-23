package com.streammastery.lessons;

import com.streammastery.model.Employee;
import com.streammastery.model.Transaction;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * LESSON 16: STREAM GATHERERS & MODERN JAVA 21-25 STREAM ARCHITECTURE
 * ====================================================================================================
 *
 * <h2>1. The Missing Architectural Link in Java Streams (Java 8 -> Java 24/25)</h2>
 * <p>
 * From Java 8 through Java 23, the Java Stream API possessed a fundamental asymmetry:
 * <ul>
 *   <li><b>Terminal operations were fully extensible</b> via the {@link java.util.stream.Collector} interface.
 *       Anyone could write a custom collector to aggregate elements into arbitrary data structures.</li>
 *   <li><b>Intermediate operations were completely closed.</b> You were constrained to the built-in operations
 *       provided by the JDK ({@code map}, {@code filter}, {@code flatMap}, {@code sorted}, {@code distinct},
 *       {@code limit}, {@code skip}, {@code takeWhile}, {@code dropWhile}).</li>
 * </ul>
 * If you needed custom intermediate behaviors—such as chunking streams into fixed batches, maintaining a sliding
 * moving window, computing a running prefix scan, or deduplicating consecutive adjacent items—you were forced to:
 * <ol>
 *   <li>Drop out of streams into verbose, error-prone imperative loops;</li>
 *   <li>Implement custom, complex {@link java.util.Spliterator} classes; or</li>
 *   <li>Buffer entire collections in memory using heavy intermediate collections.</li>
 * </ol>
 * </p>
 * <p>
 * <b>JEP 485 (Finalized in Java 24/25): Stream Gatherers</b> solves this completely.
 * Just as {@code stream.collect(Collector)} is the universal terminal reduction,
 * {@code stream.gather(Gatherer)} is the <b>universal intermediate transformation</b>!
 * </p>
 *
 * <h2>2. The 4 Pillars of Gatherer&lt;T, A, R&gt;</h2>
 * <p>
 * A {@code Gatherer<T, A, R>} transforms input elements of type {@code T} into output elements of type {@code R},
 * optionally maintaining internal intermediate state of type {@code A}:
 * <ul>
 *   <li><b>{@code initializer()}: {@code Supplier<A>}</b> — Allocates private, mutable state for tracking data across elements.</li>
 *   <li><b>{@code integrator()}: {@code Integrator<A, T, R>}</b> — The core engine:
 *       {@code boolean integrate(A state, T element, Downstream<? super R> downstream)}.
 *       Pushes 0, 1, or many elements to {@code downstream.push(r)}.
 *       Returns {@code true} to request more elements, or {@code false} to <b>short-circuit</b> upstream!</li>
 *   <li><b>{@code combiner()}: {@code BinaryOperator<A>}</b> — Merges two partial states when executing on parallel streams.</li>
 *   <li><b>{@code finisher()}: {@code BiConsumer<A, Downstream<? super R>>}</b> — Invoked when upstream has no more elements.
 *       Crucial for flushing buffered elements (e.g. trailing items in a batch or emitting final summary values).</li>
 * </ul>
 * </p>
 *
 * <h2>3. Built-in Gatherers Catalog (java.util.stream.Gatherers):</h2>
 * <ul>
 *   <li><b>{@code windowFixed(int windowSize)}:</b> Groups elements into non-overlapping lists of size $N$.</li>
 *   <li><b>{@code windowSliding(int windowSize)}:</b> Groups elements into overlapping sliding windows of size $N$.</li>
 *   <li><b>{@code fold(Supplier<R> initial, BiFunction<R, T, R> folder)}:</b> Stateful many-to-one reduction producing a 1-element intermediate stream.</li>
 *   <li><b>{@code scan(Supplier<R> initial, BiFunction<R, T, R> scanner)}:</b> Running cumulative prefix scanner (running totals).</li>
 *   <li><b>{@code mapConcurrent(int maxConcurrency, Function<T, R> mapper)}:</b> High-throughput bounded virtual-thread concurrent mapping preserving encounter order!</li>
 * </ul>
 */
public class Lesson16_StreamGatherersAndModernFeatures {

    /**
     * SAMPLE 1: Gatherers.windowFixed() for Batching / Chunking
     * What this demonstrates:
     * - Partitioning a stream into fixed chunks of size N.
     * - The quintessential pattern for bulk database writes and batch API dispatch.
     */
    @Test
    @DisplayName("01. Gatherers.windowFixed(): Chunking Streams into Fixed-Size Batches")
    void demonstrateWindowFixed() {
        List<Integer> transactionIds = List.of(101, 102, 103, 104, 105, 106, 107);

        // Batch into chunks of 3:
        List<List<Integer>> batches = transactionIds.stream()
                .gather(Gatherers.windowFixed(3))
                .toList();

        assertThat(batches).containsExactly(
                List.of(101, 102, 103),
                List.of(104, 105, 106),
                List.of(107) // Final partial batch preserved!
        );
    }

    /**
     * SAMPLE 2: Gatherers.windowSliding() for Moving Windows
     * What this demonstrates:
     * - Sliding windows of size N moving 1 element forward per step.
     * - Eliminates all manual index calculations for moving averages and time-series analysis.
     */
    @Test
    @DisplayName("02. Gatherers.windowSliding(): Rolling Moving Window Calculations")
    void demonstrateWindowSliding() {
        List<Double> stockPrices = List.of(150.0, 152.0, 155.0, 153.0, 158.0);

        // 3-point moving average:
        List<Double> movingAverages = stockPrices.stream()
                .gather(Gatherers.windowSliding(3))
                .map(window -> window.stream().mapToDouble(Double::doubleValue).average().orElse(0.0))
                .toList();

        // Window 1: [150, 152, 155] -> avg 152.333...
        // Window 2: [152, 155, 153] -> avg 153.333...
        // Window 3: [155, 153, 158] -> avg 155.333...
        assertThat(movingAverages).hasSize(3);
        assertThat(movingAverages.get(0)).isCloseTo(152.333, org.assertj.core.data.Offset.offset(0.001));
        assertThat(movingAverages.get(1)).isCloseTo(153.333, org.assertj.core.data.Offset.offset(0.001));
        assertThat(movingAverages.get(2)).isCloseTo(155.333, org.assertj.core.data.Offset.offset(0.001));
    }

    /**
     * SAMPLE 3: Gatherers.scan() for Cumulative Running Totals (Prefix Sums)
     * What this demonstrates:
     * - Emitting intermediate accumulated state for every single stream element.
     * - Calculating running bank balance or running payroll.
     */
    @Test
    @DisplayName("03. Gatherers.scan(): Cumulative Prefix Scan & Running Totals")
    void demonstrateScan() {
        List<Double> deposits = List.of(1000.0, 500.0, -200.0, 750.0);

        // Running balance starting at $0.0:
        List<Double> runningBalance = deposits.stream()
                .gather(Gatherers.scan(() -> 0.0, Double::sum))
                .toList();

        assertThat(runningBalance).containsExactly(1000.0, 1500.0, 1300.0, 2050.0);
    }

    /**
     * SAMPLE 4: Gatherers.fold() vs Terminal reduce()
     * What this demonstrates:
     * - fold() performs an ordered state reduction into an INTERMEDIATE stream (not terminal).
     * - Allows further pipeline operations after reduction!
     */
    @Test
    @DisplayName("04. Gatherers.fold(): Intermediate Stateful Reduction")
    void demonstrateFold() {
        List<String> tokens = List.of("Cloud", "Native", "Microservices");

        // Fold into hyphen-separated string, then continue streaming with map():
        List<String> uppercasedSummary = tokens.stream()
                .gather(Gatherers.fold(() -> "", (acc, s) -> acc.isEmpty() ? s : acc + "-" + s))
                .map(String::toUpperCase)
                .toList();

        assertThat(uppercasedSummary).containsExactly("CLOUD-NATIVE-MICROSERVICES");
    }

    /**
     * SAMPLE 5: Gatherers.mapConcurrent() with Virtual Threads
     * What this demonstrates:
     * - High-throughput concurrent execution with bounded concurrency.
     * - Preserves upstream encounter order while tasks execute concurrently.
     * - Perfect for asynchronous HTTP queries without hijacking the CPU ForkJoinPool.
     */
    @Test
    @DisplayName("05. Gatherers.mapConcurrent(): Bounded Concurrency with Virtual Threads")
    void demonstrateMapConcurrent() {
        List<String> endpoints = List.of("user-service", "order-service", "payment-service", "auth-service");
        AtomicInteger activeThreads = new AtomicInteger(0);

        // Concurrently query mock microservices with max concurrency = 2:
        List<String> responses = endpoints.stream()
                .gather(Gatherers.mapConcurrent(2, service -> {
                    activeThreads.incrementAndGet();
                    try {
                        Thread.sleep(10); // Simulated I/O latency
                        return service + ":OK";
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return service + ":ERROR";
                    }
                }))
                .toList();

        // Encounter order is strictly maintained:
        assertThat(responses).containsExactly(
                "user-service:OK",
                "order-service:OK",
                "payment-service:OK",
                "auth-service:OK"
        );
    }

    /**
     * SAMPLE 6: Custom Gatherer: Deduplicating Consecutive Identical Elements
     * What this demonstrates:
     * - Building a custom Gatherer via Gatherer.ofSequential().
     * - Encapsulating mutable state in the initializer.
     * - Using Integrator.ofGreedy for non-short-circuiting element consumption.
     */
    @Test
    @DisplayName("06. Custom Gatherer: Consecutive Element Deduplication")
    void demonstrateCustomGathererDedupConsecutive() {
        List<String> noisySignals = List.of("PING", "PING", "ACK", "ACK", "ACK", "PING", "TIMEOUT", "TIMEOUT");

        // Custom state container holding previous seen element:
        class State {
            String lastSeen = null;
        }

        Gatherer<String, State, String> dedupConsecutive = Gatherer.ofSequential(
                State::new,
                Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
                    if (!Objects.equals(state.lastSeen, element)) {
                        state.lastSeen = element;
                        return downstream.push(element);
                    }
                    return true;
                })
        );

        List<String> cleanSignals = noisySignals.stream()
                .gather(dedupConsecutive)
                .toList();

        assertThat(cleanSignals).containsExactly("PING", "ACK", "PING", "TIMEOUT");
    }

    /**
     * SAMPLE 7: Custom Gatherer with Finisher: Emitting the Tail Window (Last N Elements)
     * What this demonstrates:
     * - Using the finisher to emit buffered state when upstream reaches EOF.
     */
    @Test
    @DisplayName("07. Custom Gatherer with Finisher: Sliding Tail Window")
    void demonstrateCustomGathererWithFinisher() {
        List<Integer> stream = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        int tailSize = 3;

        Gatherer<Integer, ArrayDeque<Integer>, Integer> takeLastN = Gatherer.ofSequential(
                ArrayDeque::new,
                Gatherer.Integrator.ofGreedy((buffer, element, downstream) -> {
                    buffer.addLast(element);
                    if (buffer.size() > tailSize) {
                        buffer.removeFirst();
                    }
                    return true;
                }),
                (buffer, downstream) -> {
                    // Flush trailing elements at EOF:
                    for (Integer item : buffer) {
                        downstream.push(item);
                    }
                }
        );

        List<Integer> lastThree = stream.stream()
                .gather(takeLastN)
                .toList();

        assertThat(lastThree).containsExactly(8, 9, 10);
    }

    /**
     * SAMPLE 8: Custom Gatherer with Short-Circuiting (Non-Greedy Integrator)
     * What this demonstrates:
     * - Returning false from integrate() to cancel upstream processing early.
     * - Downstream.push() returning false when downstream rejects further elements.
     */
    @Test
    @DisplayName("08. Custom Gatherer: Short-Circuiting on Cumulative Budget Limit")
    void demonstrateCustomGathererShortCircuiting() {
        List<Double> expenses = List.of(200.0, 300.0, 450.0, 100.0, 500.0);
        double maxBudget = 1000.0;

        class BudgetTracker {
            double currentSum = 0.0;
        }

        Gatherer<Double, BudgetTracker, Double> takeUntilBudgetExceeded = Gatherer.ofSequential(
                BudgetTracker::new,
                Gatherer.Integrator.of((tracker, expense, downstream) -> {
                    if (tracker.currentSum + expense <= maxBudget) {
                        tracker.currentSum += expense;
                        return downstream.push(expense);
                    }
                    return false; // Short-circuit! Cease pulling upstream elements
                })
        );

        List<Double> approvedExpenses = expenses.stream()
                .gather(takeUntilBudgetExceeded)
                .toList();

        // 200 + 300 + 450 = 950 <= 1000. Next is 100 which exceeds 1000 -> short circuits!
        assertThat(approvedExpenses).containsExactly(200.0, 300.0, 450.0);
    }

    /**
     * SAMPLE 9: Gatherer Composition with andThen()
     * What this demonstrates:
     * - Composing two independent gatherers into a single unified gatherer step.
     */
    @Test
    @DisplayName("09. Gatherer Composition: Chaining Gatherers with andThen()")
    void demonstrateGathererComposition() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);

        // Stage 1: Group into batches of 4
        Gatherer<Integer, ?, List<Integer>> batcher = Gatherers.windowFixed(4);

        // Stage 2: Calculate sum of each batch using scan
        Gatherer<List<Integer>, ?, Integer> batchSummer = Gatherers.scan(
                () -> 0,
                (_, list) -> list.stream().mapToInt(Integer::intValue).sum()
        );

        // Compose: batcher.andThen(batchSummer)
        List<Integer> batchSums = numbers.stream()
                .gather(batcher.andThen(batchSummer))
                .toList();

        // Batch 1: [1,2,3,4] -> sum 10
        // Batch 2: [5,6,7,8] -> sum 26
        assertThat(batchSums).containsExactly(10, 26);
    }

    /**
     * SAMPLE 10: Combining Gatherers with Java 21 Sequenced Collections
     * What this demonstrates:
     * - Feeding Java 21 reversed views into sliding window gatherers.
     * - Zero buffer allocation for reversed moving windows.
     */
    @Test
    @DisplayName("10. Synergizing Java 21 Sequenced Collections with Java 24/25 Gatherers")
    void demonstrateSequencedCollectionWithGatherers() {
        List<String> eventTrail = List.of("INIT", "AUTH", "FETCH", "RENDER", "FLUSH");

        // Reverse the stream in O(1) time, then take sliding pairs:
        List<List<String>> reverseTransitions = eventTrail.reversed().stream()
                .gather(Gatherers.windowSliding(2))
                .toList();

        assertThat(reverseTransitions).containsExactly(
                List.of("FLUSH", "RENDER"),
                List.of("RENDER", "FETCH"),
                List.of("FETCH", "AUTH"),
                List.of("AUTH", "INIT")
        );
    }
}
