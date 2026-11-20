package com.streammastery.lessons;

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
 * LESSON 12: PARALLEL STREAMS, FORK/JOIN POOL & CONCURRENCY PITFALLS
 * ====================================================================================================
 *
 * <h2>1. Theoretical Foundation: The Fork/Join Framework & Work-Stealing</h2>
 * <p>
 * Parallel streams divide the stream into sub-streams using {@link java.util.Spliterator#trySplit()}.
 * These sub-tasks are submitted to the JVM-wide {@link java.util.concurrent.ForkJoinPool#commonPool()}.
 * Threads in the pool employ a <b>Work-Stealing Algorithm</b>: when an idle worker thread finishes its own
 * deque of subtasks, it steals tasks from the tail of busy worker threads' deques, ensuring near-optimal
 * CPU core utilization.
 * </p>
 *
 * <h2>2. The NQ Performance Model (When to Parallelize):</h2>
 * <p>
 * Brian Goetz (Java Language Architect) formalized the NQ rule:
 * $$N \times Q > 10,000$$
 * <ul>
 *   <li>$N$: Number of data elements.</li>
 *   <li>$Q$: Computational cost per element.</li>
 * </ul>
 * If $N$ is small (e.g. 50 items) or $Q$ is trivial (e.g. adding numbers), thread synchronization,
 * context-switching, and task splitting overhead will make a parallel stream <b>SLOWER</b> than a sequential stream!
 * </p>
 *
 * <h2>3. Source Splittability Ranking:</h2>
 * <ul>
 *   <li><b>EXCELLENT ($O(1)$ split):</b> {@code ArrayList}, raw arrays, {@code IntStream.range()}.</li>
 *   <li><b>FAIR ($O(\log N)$ split):</b> {@code HashSet}, {@code TreeSet}.</li>
 *   <li><b>TERRIBLE ($O(N)$ split):</b> {@code LinkedList}, {@code Stream.iterate()}.
 *       To split a linked list in half, the thread must traverse half the list sequentially!</li>
 * </ul>
 *
 * <h2>4. The Common Pool Disaster in Microservices:</h2>
 * <p>
 * {@code parallelStream()} shares the JVM-wide {@code ForkJoinPool.commonPool()} (default threads = CPU cores - 1).
 * If a microservice executes blocking I/O (HTTP calls, DB queries) inside a parallel stream,
 * it exhausts the pool threads, starving ALL OTHER parallel streams in the entire JVM!
 * </p>
 */
public class Lesson12_ParallelStreams {

    /**
     * SAMPLE 1: The Race Condition Bug: Shared Mutable State
     * What this demonstrates:
     * - Mutating an unsynchronized ArrayList from parallel threads causes data corruption.
     * - Safe aggregation via collect() or concurrent collectors.
     */
    @Test
    @DisplayName("01. Concurrency Hazard: Shared Mutable State Bug Demo")
    void demonstrateConcurrencyHazard() {
        // DANGEROUS ANTI-PATTERN: Mutating ArrayList from parallel stream
        List<Integer> unsafeList = new ArrayList<>();
        IntStream.range(0, 1000).parallel().forEach(unsafeList::add);

        // Due to unsynchronized race conditions on ArrayList.elementData, size is often < 1000!
        System.out.println("Unsafe parallel size (corrupted): " + unsafeList.size());

        // SAFE IDIOM: collect() performs thread-safe merging of partial results automatically:
        List<Integer> safeList = IntStream.range(0, 1000).parallel().boxed().toList();
        assertThat(safeList).hasSize(1000);
    }

    /**
     * SAMPLE 2: forEach() vs forEachOrdered() in Parallel Streams
     * What this demonstrates:
     * - forEach in parallel trades order preservation for maximum concurrent throughput.
     * - forEachOrdered enforces strict encounter order at a synchronization penalty.
     */
    @Test
    @DisplayName("02. Order Preservation: forEach() vs forEachOrdered()")
    void demonstrateOrderPreservation() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);

        // forEach in parallel: non-deterministic execution order
        List<Integer> concurrentOrder = Collections.synchronizedList(new ArrayList<>());
        numbers.parallelStream().forEach(concurrentOrder::add);

        // forEachOrdered in parallel: strictly enforces encounter order
        List<Integer> strictOrder = Collections.synchronizedList(new ArrayList<>());
        numbers.parallelStream().forEachOrdered(strictOrder::add);

        assertThat(strictOrder).containsExactly(1, 2, 3, 4, 5, 6, 7, 8);
    }

    /**
     * SAMPLE 3: Custom ForkJoinPool Isolation
     * What this demonstrates:
     * - Isolating parallel tasks into a private thread pool so commonPool is never starved.
     */
    @Test
    @DisplayName("03. Custom ForkJoinPool Isolation")
    void demonstrateCustomForkJoinPool() throws Exception {
        ForkJoinPool customPool = new ForkJoinPool(4);

        try {
            long sum = customPool.submit(() ->
                    IntStream.rangeClosed(1, 100_000).parallel().mapToLong(i -> (long) i).sum()
            ).get();

            assertThat(sum).isEqualTo(5000050000L);
        } finally {
            customPool.shutdown();
        }
    }
}
