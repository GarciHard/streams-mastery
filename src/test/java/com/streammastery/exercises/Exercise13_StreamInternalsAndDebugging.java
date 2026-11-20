package com.streammastery.exercises;

import com.streammastery.model.Employee;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * EXERCISE 13: STREAM INTERNALS, SPLITERATORS & DEBUGGING (20 EXERCISES)
 * ====================================================================================================
 */
public class Exercise13_StreamInternalsAndDebugging {

    @Test
    @DisplayName("01 [★☆☆☆☆] Use peek() to observe filter evaluation count")
    void exercise01_peekFilterCount() {
        AtomicInteger inspectedCount = new AtomicInteger(0);
        // Stream numbers 1..10, peek to increment inspectedCount, filter even, limit 2
        // TODO: Complete the pipeline
        List<Integer> evens = null;

        assertThat(evens).isNotNull();
        assertThat(evens).containsExactly(2, 4);
        assertThat(inspectedCount.get()).isEqualTo(4); // Only inspected 1, 2, 3, 4!
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Demonstrate limit(3) stops pipeline early")
    void exercise02_limitShortCircuit() {
        AtomicInteger processed = new AtomicInteger(0);
        // TODO: Stream 1..100, peek(processed::incrementAndGet), limit(3), toList()
        List<Integer> first3 = null;

        assertThat(first3).isNotNull();
        assertThat(first3).hasSize(3);
        assertThat(processed.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("03 [★★☆☆☆] Prove findFirst() stops evaluating immediately on match")
    void exercise03_findFirstEvaluationCount() {
        AtomicInteger tests = new AtomicInteger(0);
        // Filter DataFactory.getEmployees(): e -> tests.incrementAndGet() > 0 && e.getYearsOfExperience() >= 10
        // findFirst()
        // TODO: Complete pipeline
        Optional<Employee> firstTenured = Optional.empty();

        assertThat(firstTenured).isPresent();
        assertThat(tests.get()).isEqualTo(1); // Alice is index 0 with 10 yrs! Exactly 1 test!
    }

    @Test
    @DisplayName("04 [★★☆☆☆] Log execution trace to prove depth-first order")
    void exercise04_executionTrace() {
        List<String> log = new ArrayList<>();
        // Stream of "X", "Y"
        // peek adding "step1:elem", map elem -> elem.toLowerCase(), peek adding "step2:elem", toList()
        // TODO: Complete pipeline
        List<String> output = null;

        assertThat(output).isNotNull();
        assertThat(log).containsExactly("step1:X", "step2:x", "step1:Y", "step2:y");
    }

    @Test
    @DisplayName("05 [★★☆☆☆] Debug pipeline data loss using peek()")
    void exercise05_debugDataLoss() {
        List<String> beforeFilter = new ArrayList<>();
        List<String> afterFilter = new ArrayList<>();

        List<String> res = List.of("apple", "pie", "banana").stream()
                .peek(beforeFilter::add)
                .filter(s -> s.length() > 4)
                .peek(afterFilter::add)
                .toList();

        assertThat(beforeFilter).hasSize(3);
        assertThat(afterFilter).containsExactly("apple", "banana");
    }

    @Test
    @DisplayName("06 [★★★☆☆] Prove that sorted() breaks streaming laziness")
    void exercise06_sortedBreaksLaziness() {
        AtomicInteger evaluationsBeforeSort = new AtomicInteger(0);
        // Stream 10 items, peek before sorted(), limit(1)
        // Because sorted() must inspect EVERY element to find minimum, all 10 items must be evaluated!
        // TODO: Build stream with peek before sorted() and limit(1)
        Optional<Integer> min = Optional.empty();

        assertThat(min).isPresent();
        assertThat(evaluationsBeforeSort.get()).isEqualTo(10);
    }

    @Test
    @DisplayName("07 [★★★☆☆] Stream Characteristics: SIZED flag check")
    void exercise07_streamCharacteristics() {
        List<String> items = List.of("A", "B", "C");
        Spliterator<String> spliterator = items.stream().spliterator();

        boolean isSized = spliterator.hasCharacteristics(Spliterator.SIZED);
        assertThat(isSized).isTrue();

        Spliterator<String> filteredSpliterator = items.stream().filter(s -> true).spliterator();
        assertThat(filteredSpliterator.hasCharacteristics(Spliterator.SIZED)).isFalse();
    }

    @Test
    @DisplayName("08 [★★★☆☆] Build a pipeline step counter")
    void exercise08_pipelineStepCounter() {
        Map<String, AtomicInteger> stageCounters = Map.of(
                "source", new AtomicInteger(0),
                "filtered", new AtomicInteger(0),
                "mapped", new AtomicInteger(0)
        );

        // TODO: Stream numbers 1..10, tracking throughput at each stage (filter even, map * 10)
        List<Integer> out = null;

        assertThat(out).isNotNull();
        assertThat(stageCounters.get("source").get()).isEqualTo(10);
        assertThat(stageCounters.get("filtered").get()).isEqualTo(5);
        assertThat(stageCounters.get("mapped").get()).isEqualTo(5);
    }

    @Test
    @DisplayName("09 [★★★★☆] Custom Range Spliterator")
    void exercise09_customSpliterator() {
        // TODO: Use java.util.Spliterators.spliterator() to build stream of 10..20
        List<Integer> customStreamValues = null;

        assertThat(customStreamValues).isNotNull();
        assertThat(customStreamValues).hasSize(11);
    }

    @Test
    @DisplayName("10 [★★★★☆] Stream onClose cleanup in try-with-resources")
    void exercise10_onCloseCleanup() {
        AtomicInteger cleanupFlag = new AtomicInteger(0);

        try (Stream<String> stream = Stream.of("res1", "res2").onClose(cleanupFlag::incrementAndGet)) {
            stream.forEach(s -> {});
        }

        assertThat(cleanupFlag.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("11 [★★★★★] Profiling stream wrapper: Measure pipeline execution time")
    void exercise11_profilingWrapper() {
        long[] times = new long[2]; // [0] start, [1] end

        List<Integer> res = Stream.of(1, 2, 3)
                .peek(n -> { if (times[0] == 0) times[0] = System.nanoTime(); })
                .map(n -> n * 2)
                .onClose(() -> times[1] = System.nanoTime())
                .toList();

        assertThat(res).containsExactly(2, 4, 6);
    }

    @Test
    @DisplayName("12 [★★★★★] Compare Big-O: sorted().limit(1) vs min()")
    void exercise12_sortedLimitVsMin() {
        Comparator<Integer> cmp = Comparator.naturalOrder();
        List<Integer> nums = List.of(9, 3, 5, 1, 8);

        Integer fromSorted = nums.stream().sorted(cmp).limit(1).findFirst().orElseThrow();
        Integer fromMin = nums.stream().min(cmp).orElseThrow();

        assertThat(fromSorted).isEqualTo(fromMin).isEqualTo(1);
    }

    @Test
    @DisplayName("13 [★☆☆☆☆] Stream without terminal operation executes nothing")
    void exercise13_lazinessProof() {
        AtomicInteger sideEffect = new AtomicInteger(0);
        // Build stream with peek(n -> sideEffect.incrementAndGet()) BUT DO NOT CALL TERMINAL OPERATION!
        Stream<Integer> unconsumed = Stream.of(1, 2, 3).peek(n -> sideEffect.incrementAndGet());

        // Assert that sideEffect count is still 0:
        assertThat(sideEffect.get()).isEqualTo(0);
        assertThat(unconsumed).isNotNull();
    }

    @Test
    @DisplayName("14 [★★☆☆☆] anyMatch short-circuiting proof")
    void exercise14_anyMatchShortCircuit() {
        AtomicInteger testCount = new AtomicInteger(0);
        // TODO: Stream numbers 1..10, testCount.incrementAndGet() > 0 && n == 3, anyMatch
        boolean matched = false;

        assertThat(matched).isTrue();
        assertThat(testCount.get()).isEqualTo(3); // Aborted at element 3!
    }

    @Test
    @DisplayName("15 [★★☆☆☆] noneMatch short-circuiting proof")
    void exercise15_noneMatchShortCircuit() {
        AtomicInteger testCount = new AtomicInteger(0);
        // TODO: Stream numbers 1..10, testCount.incrementAndGet() > 0 && n == 2, noneMatch
        boolean none = true;

        assertThat(none).isFalse();
        assertThat(testCount.get()).isEqualTo(2); // Aborted at element 2!
    }

    @Test
    @DisplayName("16 [★★★☆☆] Multiple onClose handlers executed in LIFO/FIFO order")
    void exercise16_multipleOnClose() {
        List<String> closeOrder = new ArrayList<>();

        try (Stream<String> s = Stream.of("A")
                .onClose(() -> closeOrder.add("first"))
                .onClose(() -> closeOrder.add("second"))) {
            s.toList();
        }

        assertThat(closeOrder).containsExactly("first", "second");
    }

    @Test
    @DisplayName("17 [★★★☆☆] DISTINCT characteristic check on Set vs List")
    void exercise17_distinctCharacteristic() {
        Spliterator<String> setSpliterator = Set.of("A", "B").stream().spliterator();
        Spliterator<String> listSpliterator = List.of("A", "B").stream().spliterator();

        assertThat(setSpliterator.hasCharacteristics(Spliterator.DISTINCT)).isTrue();
        assertThat(listSpliterator.hasCharacteristics(Spliterator.DISTINCT)).isFalse();
    }

    @Test
    @DisplayName("18 [★★★★☆] Custom Count-Tracking Peek Wrapper")
    void exercise18_countTrackingPeek() {
        class MeteredStream {
            int in = 0;
            int out = 0;
        }
        MeteredStream meter = new MeteredStream();

        List<Integer> evens = List.of(1, 2, 3, 4, 5, 6, 7, 8).stream()
                .peek(n -> meter.in++)
                .filter(n -> n % 2 == 0)
                .peek(n -> meter.out++)
                .limit(2)
                .toList();

        assertThat(evens).containsExactly(2, 4);
        assertThat(meter.in).isEqualTo(4);
        assertThat(meter.out).isEqualTo(2);
    }

    @Test
    @DisplayName("19 [★★★★☆] Diagnostic Pipeline Probe: Identify filtering drop-off rates")
    void exercise19_pipelineDiagnosticProbe() {
        Map<String, Integer> throughput = new HashMap<>();

        List<Employee> selected = DataFactory.getEmployees().stream()
                .peek(e -> throughput.merge("stage1_source", 1, Integer::sum))
                .filter(e -> "Engineering".equals(e.getDepartment()))
                .peek(e -> throughput.merge("stage2_eng", 1, Integer::sum))
                .filter(e -> e.getSalary() > 200_000.0)
                .peek(e -> throughput.merge("stage3_high_salary", 1, Integer::sum))
                .toList();

        assertThat(throughput.get("stage1_source")).isEqualTo(30);
        assertThat(throughput.get("stage2_eng")).isEqualTo(20);
        assertThat(throughput.get("stage3_high_salary")).isEqualTo(9);
    }

    @Test
    @DisplayName("20 [★★★★★] Exception transparency in Stream pipeline")
    void exercise20_exceptionHandlingInStream() {
        // Test that an unchecked exception inside a map operation propagates immediately
        // and aborts further element processing
        AtomicInteger elementsProcessed = new AtomicInteger(0);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            Stream.of(1, 2, 0, 4, 5)
                    .peek(n -> elementsProcessed.incrementAndGet())
                    .map(n -> 100 / n) // Division by zero at index 2!
                    .toList();
        }).isInstanceOf(ArithmeticException.class);

        assertThat(elementsProcessed.get()).isEqualTo(3); // 1, 2, and 0 evaluated before abort!
    }
}
