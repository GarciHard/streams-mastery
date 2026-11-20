package com.streammastery.exercises;

import com.streammastery.model.Employee;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * EXERCISE 12: PARALLEL STREAMS & PERFORMANCE (20 EXERCISES)
 * ====================================================================================================
 */
public class Exercise12_ParallelStreams {

    @Test
    @DisplayName("01 [★☆☆☆☆] Convert sequential stream to parallel and sum all salaries")
    void exercise01_parallelSum() {
        // TODO: employees.parallelStream().mapToDouble(Employee::getSalary).sum()
        double total = 0.0;

        assertThat(total).isGreaterThan(5_000_000.0);
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Maintain order in parallel stream with forEachOrdered")
    void exercise02_forEachOrdered() {
        List<Integer> collected = Collections.synchronizedList(new ArrayList<>());
        // TODO: IntStream.rangeClosed(1, 10).parallel().forEachOrdered(collected::add)

        assertThat(collected).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    @DisplayName("03 [★★☆☆☆] Collect distinct thread names executing the parallel stream")
    void exercise03_distinctThreads() {
        // TODO: Stream 1..100 in parallel, map to Thread.currentThread().getName(), collect to Set
        Set<String> threadNames = null;

        assertThat(threadNames).isNotNull();
        assertThat(threadNames.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("04 [★★☆☆☆] Parallel reduce with combiner")
    void exercise04_parallelReduceCombiner() {
        List<String> tokens = List.of("M", "A", "A", "N", "G");
        // TODO: tokens.parallelStream().reduce("", (acc, s) -> acc + s, (s1, s2) -> s1 + s2)
        String combined = null;

        assertThat(combined).isNotNull();
        assertThat(combined).isEqualTo("MAANG");
    }

    @Test
    @DisplayName("05 [★★☆☆☆] findAny vs findFirst on parallel stream")
    void exercise05_findAnyParallel() {
        // TODO: employees.parallelStream().filter(e -> "Engineering".equals(e.getDepartment())).findAny()
        Optional<Employee> anyEngineer = Optional.empty();

        assertThat(anyEngineer).isPresent();
        assertThat(anyEngineer.get().getDepartment()).isEqualTo("Engineering");
    }

    @Test
    @DisplayName("06 [★★★☆☆] Fix a concurrency race bug by switching to collect()")
    void exercise06_fixSharedStateBug() {
        // Buggy code was: List<Integer> list = new ArrayList<>(); IntStream.range(0, 1000).parallel().forEach(list::add);
        // TODO: Replace with parallelStream collect to List
        List<Integer> safeList = null;

        assertThat(safeList).isNotNull();
        assertThat(safeList).hasSize(1000);
    }

    @Test
    @DisplayName("07 [★★★☆☆] unordered() + parallel to optimize limit() search")
    void exercise07_unorderedParallel() {
        // TODO: employees.parallelStream().unordered().filter(e -> e.getSalary() > 200_000.0).limit(3).toList()
        List<Employee> threeHighEarners = null;

        assertThat(threeHighEarners).isNotNull();
        assertThat(threeHighEarners).hasSize(3);
    }

    @Test
    @DisplayName("08 [★★★☆☆] Parallel groupingBy: Verify result matches sequential")
    void exercise08_parallelGrouping() {
        // TODO: employees.parallelStream().collect(java.util.stream.Collectors.groupingByConcurrent(Employee::getDepartment, java.util.stream.Collectors.counting()))
        Map<String, Long> concurrentGroup = null;

        assertThat(concurrentGroup).isNotNull();
        assertThat(concurrentGroup.get("Engineering")).isEqualTo(20L);
    }

    @Test
    @DisplayName("09 [★★★★☆] Parallel-safe word counter with 3-arg collect")
    void exercise09_parallelWordCounter() {
        List<String> words = List.of("apple", "banana", "apple", "cherry", "banana", "date");
        // TODO: words.parallelStream().collect(ConcurrentHashMap::new, (map, w) -> map.merge(w, 1L, Long::sum), (m1, m2) -> m2.forEach((k, v) -> m1.merge(k, v, Long::sum)))
        ConcurrentMap<String, Long> counts = null;

        assertThat(counts).isNotNull();
        assertThat(counts.get("apple")).isEqualTo(2L);
    }

    @Test
    @DisplayName("10 [★★★★☆] Execute parallel stream in custom ForkJoinPool with 2 threads")
    void exercise10_customPool() throws Exception {
        ForkJoinPool customPool = new ForkJoinPool(2);
        try {
            // TODO: customPool.submit(() -> IntStream.rangeClosed(1, 100).parallel().sum()).get()
            int sum = 0;

            assertThat(sum).isEqualTo(5050);
        } finally {
            customPool.shutdown();
        }
    }

    @Test
    @DisplayName("11 [★★★★★] Demonstrate non-associative reduction divergence")
    void exercise11_nonAssociativeReduction() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        int seq = numbers.stream().reduce(0, (a, b) -> a - b);
        int par = numbers.parallelStream().reduce(0, (a, b) -> a - b);

        // Subtraction is non-associative, so sequential and parallel MUST diverge!
        assertThat(seq).isNotEqualTo(par);
    }

    @Test
    @DisplayName("12 [★★★★★] Parallel Stream Map-Reduce with proper combiner")
    void exercise12_parallelMapReduce() {
        // TODO: employees.parallelStream().map(Employee::getFirstName).reduce(0, (acc, name) -> acc + name.length(), Integer::sum)
        int totalChars = 0;

        assertThat(totalChars).isGreaterThan(100);
    }

    @Test
    @DisplayName("13 [★☆☆☆☆] Parallel max salary")
    void exercise13_parallelMaxSalary() {
        // TODO: employees.parallelStream().mapToDouble(Employee::getSalary).max().orElse(0.0)
        double maxSalary = 0.0;

        assertThat(maxSalary).isEqualTo(340000.0);
    }

    @Test
    @DisplayName("14 [★★☆☆☆] Parallel anyMatch on large range")
    void exercise14_parallelAnyMatch() {
        // TODO: IntStream.range(1, 1_000_000).parallel().anyMatch(n -> n == 777_777)
        boolean found = false;

        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("15 [★★☆☆☆] Parallel check if all products have positive price")
    void exercise15_parallelAllMatch() {
        // TODO: DataFactory.getProducts().parallelStream().allMatch(p -> p.getPrice() > 0.0)
        boolean allPositive = false;

        assertThat(allPositive).isTrue();
    }

    @Test
    @DisplayName("16 [★★★☆☆] Parallel sum of squares with LongStream")
    void exercise16_parallelSumOfSquares() {
        // 1^2 + 2^2 + ... + 1000^2
        // TODO: LongStream.rangeClosed(1, 1000).parallel().map(n -> n * n).sum()
        long sumSquares = 0L;

        assertThat(sumSquares).isEqualTo(333833500L);
    }

    @Test
    @DisplayName("17 [★★★☆☆] Parallel Stream to ConcurrentSkipListSet (Sorted thread-safe)")
    void exercise17_concurrentSkipListSet() {
        // TODO: Collect unique programming languages in parallel to ConcurrentSkipListSet
        ConcurrentSkipListSet<String> languages = null;

        assertThat(languages).isNotNull();
        assertThat(languages.first()).isEqualTo("C++");
        assertThat(languages.last()).isEqualTo("Swift");
    }

    @Test
    @DisplayName("18 [★★★★☆] Benchmark simulation: Compare sequential vs parallel array sum")
    void exercise18_benchmarkArraySum() {
        long[] data = new long[100_000];
        Arrays.fill(data, 1L);

        // TODO: Sum data using Arrays.stream(data).parallel().sum()
        long parallelSum = 0L;

        assertThat(parallelSum).isEqualTo(100_000L);
    }

    @Test
    @DisplayName("19 [★★★★☆] Parallel Monte Carlo Pi approximation")
    void exercise19_monteCarloPi() {
        int samples = 100_000;
        // Count samples where x^2 + y^2 <= 1.0 using ThreadLocalRandom
        // TODO: Estimate Pi using parallel stream
        double piEstimate = 0.0;

        assertThat(piEstimate).isBetween(3.0, 3.3);
    }

    @Test
    @DisplayName("20 [★★★★★] Parallel Merge-Sort using Stream Spliterator Split")
    void exercise20_parallelMerge() {
        List<Integer> unsorted = List.of(9, 3, 5, 2, 8, 1, 7, 4, 6);
        // TODO: Sort list using parallelStream().sorted().toList()
        List<Integer> sorted = null;

        assertThat(sorted).isNotNull();
        assertThat(sorted).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
    }
}
