package com.streammastery.exercises;

import com.streammastery.model.Employee;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collector;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * EXERCISE 11: CUSTOM COLLECTORS (20 EXERCISES)
 * ====================================================================================================
 */
public class Exercise11_CustomCollectors {

    @Test
    @DisplayName("01 [★☆☆☆☆] Build a custom comma-separated string collector")
    void exercise01_customCsvCollector() {
        // TODO: Collector.of(StringBuilder::new, (sb, s) -> { if (sb.length() > 0) sb.append(", "); sb.append(s); }, StringBuilder::append, StringBuilder::toString)
        Collector<String, ?, String> csvCollector = null;

        assertThat(csvCollector).isNotNull();
        String result = List.of("A", "B", "C").stream().collect(csvCollector);
        assertThat(result).isEqualTo("A, B, C");
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Custom counting collector using long[1] accumulator")
    void exercise02_customCounting() {
        // TODO: Collector.of(() -> new long[1], (arr, item) -> arr[0]++, (a1, a2) -> { a1[0] += a2[0]; return a1; }, arr -> arr[0])
        Collector<Object, ?, Long> countingCollector = null;

        assertThat(countingCollector).isNotNull();
        Long count = DataFactory.getEmployees().stream().collect(countingCollector);
        assertThat(count).isEqualTo(30L);
    }

    @Test
    @DisplayName("03 [★★☆☆☆] Custom Max Collector using Optional container")
    void exercise03_customMaxBy() {
        // TODO: Build collector returning Optional<Employee> of highest paid
        Collector<Employee, ?, Optional<Employee>> maxCollector = null;

        assertThat(maxCollector).isNotNull();
        Optional<Employee> max = DataFactory.getEmployees().stream().collect(maxCollector);
        assertThat(max).isPresent();
        assertThat(max.get().getSalary()).isEqualTo(340000.0);
    }

    @Test
    @DisplayName("04 [★★☆☆☆] Custom toMap collector")
    void exercise04_customToMap() {
        // TODO: Build custom toMap collector mapping Employee -> (id -> fullName)
        Collector<Employee, ?, Map<Integer, String>> toMapCollector = null;

        assertThat(toMapCollector).isNotNull();
        Map<Integer, String> map = DataFactory.getEmployees().stream().collect(toMapCollector);
        assertThat(map.get(1)).isEqualTo("Alice Johnson");
    }

    @Test
    @DisplayName("05 [★★★☆☆] Custom Median Collector for DoubleStream/Double values")
    void exercise05_medianCollector() {
        // Collects doubles into an ArrayList, sorts in finisher, returns median value
        // TODO: Build the median collector
        Collector<Double, ?, Double> medianCollector = null;

        assertThat(medianCollector).isNotNull();
        List<Double> sample = List.of(10.0, 20.0, 30.0, 40.0, 50.0);
        Double median = sample.stream().collect(medianCollector);
        assertThat(median).isEqualTo(30.0);
    }

    @Test
    @DisplayName("06 [★★★☆☆] Custom Unmodifiable Set Collector")
    void exercise06_unmodifiableSetCollector() {
        // TODO: Build collector accumulating into HashSet, finisher wraps in Collections.unmodifiableSet
        Collector<String, ?, Set<String>> unmodifiableSetCollector = null;

        assertThat(unmodifiableSetCollector).isNotNull();
        Set<String> set = List.of("A", "B", "C").stream().collect(unmodifiableSetCollector);
        assertThat(set).containsExactlyInAnyOrder("A", "B", "C");
    }

    @Test
    @DisplayName("07 [★★★☆☆] Custom Partition Collector based on Predicate")
    void exercise07_customPartitionCollector() {
        // TODO: Build custom partition collector for even/odd integers
        Collector<Integer, ?, Map<Boolean, List<Integer>>> partitionCollector = null;

        assertThat(partitionCollector).isNotNull();
        Map<Boolean, List<Integer>> result = List.of(1, 2, 3, 4, 5, 6).stream().collect(partitionCollector);
        assertThat(result.get(true)).containsExactly(2, 4, 6);
        assertThat(result.get(false)).containsExactly(1, 3, 5);
    }

    @Test
    @DisplayName("08 [★★★★☆] Custom Top-N Salary Collector")
    void exercise08_topNSalaryCollector() {
        // Return List<Employee> of Top 5 highest earners
        // TODO: Build top-N collector
        Collector<Employee, ?, List<Employee>> top5Collector = null;

        assertThat(top5Collector).isNotNull();
        List<Employee> top5 = DataFactory.getEmployees().stream().collect(top5Collector);
        assertThat(top5).hasSize(5);
        assertThat(top5.get(0).getSalary()).isEqualTo(340000.0);
    }

    @Test
    @DisplayName("09 [★★★★☆] Custom Frequency Map Collector")
    void exercise09_customFrequencyMap() {
        // Maps element -> frequency count
        // TODO: Build frequency collector
        Collector<String, ?, Map<String, Long>> freqCollector = null;

        assertThat(freqCollector).isNotNull();
        Map<String, Long> freqs = List.of("a", "b", "a", "c", "b", "a").stream().collect(freqCollector);
        assertThat(freqs.get("a")).isEqualTo(3L);
    }

    @Test
    @DisplayName("10 [★★★★☆] Running Average Collector without primitive stream")
    void exercise10_runningAverageCollector() {
        // Accumulator: double[2] where [0] is sum and [1] is count
        // Finisher: sum / count
        // TODO: Build running average collector for Double
        Collector<Double, ?, Double> avgCollector = null;

        assertThat(avgCollector).isNotNull();
        Double avg = List.of(100.0, 200.0, 300.0).stream().collect(avgCollector);
        assertThat(avg).isEqualTo(200.0);
    }

    @Test
    @DisplayName("11 [★★★★★] Custom Chunking Collector: Partition stream into fixed-size batches of N")
    void exercise11_chunkingCollector() {
        // Collect stream of 30 employees into List<List<Employee>> where each inner list has size 7 (last has 2)
        // TODO: Build chunking collector with batchSize = 7
        Collector<Employee, ?, List<List<Employee>>> batchCollector = null;

        assertThat(batchCollector).isNotNull();
        List<List<Employee>> batches = DataFactory.getEmployees().stream().collect(batchCollector);
        assertThat(batches).hasSize(5); // 7, 7, 7, 7, 2
        assertThat(batches.get(0)).hasSize(7);
        assertThat(batches.get(4)).hasSize(2);
    }

    @Test
    @DisplayName("12 [★★★★★] Parallel-Safe Sorted Merge Collector")
    void exercise12_parallelSortedMergeCollector() {
        // Collector that accumulates integers and returns a sorted List, maintaining correct sort in parallel
        // TODO: Implement collector
        Collector<Integer, ?, List<Integer>> parallelSortedCollector = null;

        assertThat(parallelSortedCollector).isNotNull();
        List<Integer> sorted = List.of(5, 1, 9, 3, 7).parallelStream().collect(parallelSortedCollector);
        assertThat(sorted).containsExactly(1, 3, 5, 7, 9);
    }

    @Test
    @DisplayName("13 [★☆☆☆☆] Custom Sum Collector for Integers")
    void exercise13_customSumCollector() {
        // TODO: Build collector summing integers using int[1] accumulator
        Collector<Integer, ?, Integer> sumCollector = null;

        assertThat(sumCollector).isNotNull();
        int sum = List.of(10, 20, 30).stream().collect(sumCollector);
        assertThat(sum).isEqualTo(60);
    }

    @Test
    @DisplayName("14 [★★☆☆☆] Custom Min Element Collector")
    void exercise14_customMinCollector() {
        // TODO: Build collector returning Optional<Integer> of minimum
        Collector<Integer, ?, Optional<Integer>> minCollector = null;

        assertThat(minCollector).isNotNull();
        Optional<Integer> min = List.of(45, 12, 89, 3).stream().collect(minCollector);
        assertThat(min).contains(3);
    }

    @Test
    @DisplayName("15 [★★☆☆☆] Custom String Joining Collector with Prefix and Suffix")
    void exercise15_customJoiningPrefixSuffix() {
        // TODO: Collector that joins strings with delimiter ", ", prefix "[", suffix "]"
        Collector<String, ?, String> bracketCollector = null;

        assertThat(bracketCollector).isNotNull();
        String res = List.of("X", "Y", "Z").stream().collect(bracketCollector);
        assertThat(res).isEqualTo("[X, Y, Z]");
    }

    @Test
    @DisplayName("16 [★★★☆☆] Custom Deduplicating Collector preserving encounter order")
    void exercise16_orderedDeduplicatingCollector() {
        // TODO: Collector accumulating into LinkedHashSet, finisher returning List
        Collector<String, ?, List<String>> orderPreservingDedup = null;

        assertThat(orderPreservingDedup).isNotNull();
        List<String> dedup = List.of("B", "A", "B", "C", "A").stream().collect(orderPreservingDedup);
        assertThat(dedup).containsExactly("B", "A", "C");
    }

    @Test
    @DisplayName("17 [★★★☆☆] Custom Variance and Standard Deviation Collector")
    void exercise17_stdDevCollector() {
        // Collector computing standard deviation of double values
        // TODO: Implement collector
        Collector<Double, ?, Double> stdDevCollector = null;

        assertThat(stdDevCollector).isNotNull();
        List<Double> vals = List.of(10.0, 10.0, 10.0);
        Double stdDev = vals.stream().collect(stdDevCollector);
        assertThat(stdDev).isEqualTo(0.0);
    }

    @Test
    @DisplayName("18 [★★★★☆] Custom Histogram Range Collector")
    void exercise18_histogramCollector() {
        // Bucket numbers into: "0-49", "50-99", "100+"
        // TODO: Build histogram collector
        Collector<Integer, ?, Map<String, Long>> histCollector = null;

        assertThat(histCollector).isNotNull();
        Map<String, Long> hist = List.of(10, 25, 55, 75, 120, 150, 200).stream().collect(histCollector);
        assertThat(hist.get("0-49")).isEqualTo(2L);
        assertThat(hist.get("50-99")).isEqualTo(2L);
        assertThat(hist.get("100+")).isEqualTo(3L);
    }

    @Test
    @DisplayName("19 [★★★★☆] Custom Group-and-Rank Collector: Rank employees within department by salary")
    void exercise19_groupAndRankCollector() {
        record RankedEmp(String name, double salary, int rank) {}

        // TODO: Collector producing Map<String, List<RankedEmp>>
        Collector<Employee, ?, Map<String, List<RankedEmp>>> groupRankCollector = null;

        assertThat(groupRankCollector).isNotNull();
        Map<String, List<RankedEmp>> ranked = DataFactory.getEmployees().stream().collect(groupRankCollector);
        assertThat(ranked.get("Design").get(0).rank()).isEqualTo(1);
    }

    @Test
    @DisplayName("20 [★★★★★] Custom Teeing Collector implementation from scratch")
    void exercise20_customTeeingCollector() {
        // Implement teeing(c1, c2, merger) using Collector.of()
        // c1 computes min, c2 computes max, merger produces Map.entry(min, max)
        // TODO: Build custom teeing collector
        Collector<Integer, ?, Map.Entry<Integer, Integer>> customTeeing = null;

        assertThat(customTeeing).isNotNull();
        Map.Entry<Integer, Integer> minMax = List.of(5, 2, 9, 1, 7).stream().collect(customTeeing);
        assertThat(minMax.getKey()).isEqualTo(1);
        assertThat(minMax.getValue()).isEqualTo(9);
    }
}
