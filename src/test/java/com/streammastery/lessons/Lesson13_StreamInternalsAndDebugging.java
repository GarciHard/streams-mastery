package com.streammastery.lessons;

import com.streammastery.model.Employee;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * LESSON 13: STREAM INTERNALS, LOOP FUSION & DEBUGGING ARCHITECTURE
 * ====================================================================================================
 *
 * <h2>1. Theoretical Foundation: How Streams Execute Under the Hood</h2>
 * <p>
 * Junior engineers mistakenly envision a stream as a sequence of discrete passes over data:
 * first the whole collection is filtered into a temporary list, then mapped into another list, then sorted, then collected.
 * <b>This is completely wrong!</b>
 * </p>
 * <p>
 * <b>Depth-First Loop Fusion:</b>
 * The Java Stream engine compiles all stateless intermediate operations ({@code filter}, {@code map}, {@code peek})
 * into a single unified pass. Element #1 passes through {@code filter} -> {@code map} -> {@code sink}.
 * Only then is Element #2 processed. This eliminates intermediate heap allocations and allows the CPU
 * to keep data in L1/L2 registers.
 * </p>
 *
 * <h2>2. Pipeline Stage Representation (ReferencePipeline):</h2>
 * <p>
 * Each intermediate operation instantiates an internal {@code ReferencePipeline$StatelessOp} or {@code StatefulOp}.
 * These stages form a linked list pointing backwards to the source {@link java.util.Spliterator}.
 * When a terminal operation is called, the pipeline walks backward to build a chain of {@code Sink} consumers,
 * then pulls elements from the source.
 * </p>
 *
 * <h2>3. peek() Rules of Engagement (Production Standard):</h2>
 * <ul>
 *   <li><b>RULE 1: {@code peek()} is for DIAGNOSTICS ONLY:</b> Never mutate external state or execute business logic inside {@code peek()}.</li>
 *   <li><b>RULE 2: JIT Compiler Elision:</b> In certain JDK versions or operations (like {@code count()}), the compiler
 *       can optimize away operations entirely if it determines the elements are not needed, silently SKIPPING {@code peek()}!</li>
 * </ul>
 */
public class Lesson13_StreamInternalsAndDebugging {

    /**
     * SAMPLE 1: Proof of Lazy Evaluation & Short-Circuiting
     * What this demonstrates:
     * - Intermediate operations do zero work until a terminal operation is invoked.
     * - limit() stops pipeline traversal immediately once threshold is met.
     */
    @Test
    @DisplayName("01. Proof of Lazy Evaluation and Short-Circuiting via peek()")
    void demonstrateLazyEvaluationAndShortCircuit() {
        AtomicInteger filterEvaluations = new AtomicInteger(0);
        AtomicInteger mapEvaluations = new AtomicInteger(0);

        List<String> result = Stream.of("one", "two", "three", "four", "five", "six")
                .filter(s -> {
                    filterEvaluations.incrementAndGet();
                    return s.length() > 3; // "three", "four", "five"
                })
                .peek(s -> mapEvaluations.incrementAndGet())
                .limit(2) // Short-circuits as soon as 2 elements pass!
                .toList();

        assertThat(result).containsExactly("three", "four");
        // "one" (filter false), "two" (filter false), "three" (pass + peek), "four" (pass + peek) -> exactly 4 tests!
        // "five" and "six" are NEVER even evaluated!
        assertThat(filterEvaluations.get()).isEqualTo(4);
        assertThat(mapEvaluations.get()).isEqualTo(2);
    }

    /**
     * SAMPLE 2: Proof of Loop Fusion: Depth-First Execution Order
     * What this demonstrates:
     * - Elements execute vertically (filter:A -> map:A), NOT horizontally (filter:A -> filter:B -> map:A).
     */
    @Test
    @DisplayName("02. Proof of Loop Fusion: Depth-First Element Traversal")
    void demonstrateDepthFirstExecution() {
        List<String> trace = new ArrayList<>();

        List<String> output = Stream.of("A", "B")
                .filter(s -> {
                    trace.add("filter:" + s);
                    return true;
                })
                .map(s -> {
                    trace.add("map:" + s);
                    return s + "!";
                })
                .toList();

        // If breadth-first: filter:A, filter:B, map:A, map:B
        // Because of Depth-First: filter:A, map:A, filter:B, map:B
        assertThat(trace).containsExactly("filter:A", "map:A", "filter:B", "map:B");
        assertThat(output).containsExactly("A!", "B!");
    }

    /**
     * SAMPLE 3: Stream Lifecycle & Resource Management via onClose()
     * What this demonstrates:
     * - Registering cleanup hooks using onClose() in try-with-resources.
     */
    @Test
    @DisplayName("03. Resource Management: Stream.onClose() in try-with-resources")
    void demonstrateOnClose() {
        AtomicInteger closedCount = new AtomicInteger(0);

        try (Stream<String> managedStream = Stream.of("db_conn_1", "db_conn_2")
                .onClose(closedCount::incrementAndGet)
                .onClose(closedCount::incrementAndGet)) {

            List<String> list = managedStream.map(String::toUpperCase).toList();
            assertThat(list).hasSize(2);
        } // onClose handlers executed here automatically when leaving try block!

        assertThat(closedCount.get()).isEqualTo(2);
    }

    /**
     * SAMPLE 4: Spliterator Characteristics Inspection
     * What this demonstrates:
     * - Examining metadata flags: SIZED, ORDERED, DISTINCT, SORTED.
     * - Observing how intermediate operations drop flags (e.g. filter drops SIZED).
     */
    @Test
    @DisplayName("04. Spliterator Characteristics: Flag Propagation")
    void demonstrateSpliteratorCharacteristics() {
        List<String> list = List.of("A", "B", "C");

        Spliterator<String> listSpliterator = list.stream().spliterator();
        assertThat(listSpliterator.hasCharacteristics(Spliterator.SIZED)).isTrue();
        assertThat(listSpliterator.hasCharacteristics(Spliterator.ORDERED)).isTrue();

        // After filter, exact size is unknown, so SIZED characteristic is dropped:
        Spliterator<String> filteredSpliterator = list.stream().filter(s -> true).spliterator();
        assertThat(filteredSpliterator.hasCharacteristics(Spliterator.SIZED)).isFalse();
    }
}
