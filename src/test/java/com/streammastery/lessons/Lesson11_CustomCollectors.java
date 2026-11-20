package com.streammastery.lessons;

import com.streammastery.model.Employee;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collector;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * LESSON 11: CUSTOM COLLECTORS & THE COLLECTOR INTERFACE (STAFF MAANG LEVEL)
 * ====================================================================================================
 *
 * <h2>1. Theoretical Foundation: The 5 Pillars of Collector&lt;T, A, R&gt;</h2>
 * <p>
 * When standard collectors ({@code toList}, {@code groupingBy}) cannot fulfill complex data aggregation needs,
 * senior engineers implement custom collectors using {@link java.util.stream.Collector#of}.
 * </p>
 * <p>
 * The type parameters:
 * <ul>
 *   <li><b>{@code T}:</b> Type of input elements in the stream.</li>
 *   <li><b>{@code A}:</b> Type of mutable accumulator container (intermediate state).</li>
 *   <li><b>{@code R}:</b> Type of final result returned to the caller.</li>
 * </ul>
 * </p>
 *
 * <h2>2. The 5 Core Methods:</h2>
 * <ol>
 *   <li><b>{@code supplier()}:</b> {@code () -> A} — Creates a new, empty mutable accumulator.</li>
 *   <li><b>{@code accumulator()}:</b> {@code (A, T) -> void} — Folds an input element into the accumulator.</li>
 *   <li><b>{@code combiner()}:</b> {@code (A, A) -> A} — Merges two partial accumulators into one (essential for parallel streams!).</li>
 *   <li><b>{@code finisher()}:</b> {@code A -> R} — Transforms the accumulator into the final result (e.g. wrapping in unmodifiable).</li>
 *   <li><b>{@code characteristics()}:</b> Set of flags optimizing execution:
 *     <ul>
 *       <li>{@code IDENTITY_FINISH}: The container {@code A} can be directly cast to {@code R} without invoking {@code finisher()}.</li>
 *       <li>{@code UNORDERED}: The collector does not preserve or depend on encounter order.</li>
 *       <li>{@code CONCURRENT}: The accumulator can be invoked concurrently across threads on the SAME container (requires thread-safe accumulator!).</li>
 *     </ul>
 *   </li>
 * </ol>
 */
public class Lesson11_CustomCollectors {

    /**
     * SAMPLE 1: Word Frequency Collector using Collector.of()
     * What this demonstrates:
     * - Building an unmodifiable frequency map directly from stream.
     */
    @Test
    @DisplayName("01. Custom Collector: Word Frequency Counter using Collector.of()")
    void demonstrateFrequencyCollector() {
        List<String> words = List.of("apple", "banana", "apple", "cherry", "banana", "apple");

        Collector<String, Map<String, Integer>, Map<String, Integer>> frequencyCollector = Collector.of(
                HashMap::new,                                                         // supplier
                (Map<String, Integer> map, String word) -> map.merge(word, 1, Integer::sum), // accumulator
                (Map<String, Integer> map1, Map<String, Integer> map2) -> {          // combiner
                    map2.forEach((k, v) -> map1.merge(k, v, Integer::sum));
                    return map1;
                },
                Collections::unmodifiableMap                                          // finisher
        );

        Map<String, Integer> freqs = words.stream().collect(frequencyCollector);
        assertThat(freqs.get("apple")).isEqualTo(3);
        assertThat(freqs.get("banana")).isEqualTo(2);
    }

    /**
     * SAMPLE 2: Top-N Elements Collector with Bounded PriorityQueue
     * What this demonstrates:
     * - Maintaining a bounded min-heap of size N during streaming ($O(N \log K)$ time, $O(K)$ space).
     * - Far more memory-efficient than sorting the whole stream when data size is millions!
     */
    @Test
    @DisplayName("02. Custom Collector: Top-N Elements Bounded PriorityQueue")
    void demonstrateTopNCollector() {
        List<Employee> employees = DataFactory.getEmployees();

        // Collector that maintains only the Top 3 highest earners
        int topN = 3;
        Collector<Employee, PriorityQueue<Employee>, List<Employee>> topNEarnersCollector = Collector.of(
                () -> new PriorityQueue<>(Comparator.comparingDouble(Employee::getSalary)), // Min-heap
                (pq, emp) -> {
                    pq.offer(emp);
                    if (pq.size() > topN) {
                        pq.poll(); // Evict the lowest salary in the top-N window
                    }
                },
                (pq1, pq2) -> {
                    while (!pq2.isEmpty()) {
                        pq1.offer(pq2.poll());
                        if (pq1.size() > topN) pq1.poll();
                    }
                    return pq1;
                },
                pq -> {
                    List<Employee> result = new ArrayList<>(pq);
                    result.sort(Comparator.comparingDouble(Employee::getSalary).reversed());
                    return Collections.unmodifiableList(result);
                }
        );

        List<Employee> top3 = employees.stream().collect(topNEarnersCollector);
        assertThat(top3).hasSize(3);
        assertThat(top3.get(0).getSalary()).isEqualTo(340000.0); // Amy King
    }

    /**
     * SAMPLE 3: Custom Running Statistics Collector (Mean & Standard Deviation)
     * What this demonstrates:
     * - Accumulating multiple metrics simultaneously without multiple passes.
     */
    @Test
    @DisplayName("03. Custom Stats Collector: Average Calculation Container")
    void demonstrateRunningStatsCollector() {
        class AverageAccumulator {
            double sum = 0.0;
            long count = 0;
            void add(double val) { sum += val; count++; }
            AverageAccumulator merge(AverageAccumulator other) {
                sum += other.sum;
                count += other.count;
                return this;
            }
            double average() { return count == 0 ? 0.0 : sum / count; }
        }

        Collector<Double, AverageAccumulator, Double> avgCollector = Collector.of(
                AverageAccumulator::new,
                AverageAccumulator::add,
                AverageAccumulator::merge,
                AverageAccumulator::average
        );

        Double avg = List.of(100.0, 200.0, 300.0).stream().collect(avgCollector);
        assertThat(avg).isEqualTo(200.0);
    }
}
