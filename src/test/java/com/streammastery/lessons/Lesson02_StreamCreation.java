package com.streammastery.lessons;

import com.streammastery.model.Employee;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ====================================================================================================
 * LESSON 02: STREAM CREATION, SOURCES & ANATOMY
 * ====================================================================================================
 *
 * <h2>1. What is a Stream? (The Conveyor Belt Mental Model)</h2>
 * <p>
 * A Java Stream is <b>NOT</b> a data structure. It does not store elements in memory.
 * It is an <i>on-demand computational pipeline</i> that pulls data from a source (Collection, Array,
 * I/O channel, Generator function, Sequenced view), pushes it through intermediate operations (filter, map),
 * and terminates with a final result (reduction, collection).
 * </p>
 *
 * <h2>2. Stream Characteristics & The Spliterator Contract:</h2>
 * Every stream source is backed by a {@link java.util.Spliterator} which declares metadata flags:
 * <ul>
 *   <li><b>ORDERED:</b> Encounter order is preserved (e.g. {@code List}). Unordered sources (e.g. {@code HashSet}) allow parallel optimizations.</li>
 *   <li><b>DISTINCT:</b> Elements are guaranteed distinct by {@code equals()} (e.g. {@code Set}).</li>
 *   <li><b>SORTED:</b> Elements are already sorted according to a comparator (e.g. {@code TreeSet}).</li>
 *   <li><b>SIZED:</b> Exact size is known in advance without traversing (e.g. {@code ArrayList}). Intermediate ops like {@code filter()} drop this flag.</li>
 *   <li><b>IMMUTABLE:</b> Source elements cannot be modified during stream execution.</li>
 *   <li><b>CONCURRENT:</b> Source can be safely modified concurrently without throwing {@link ConcurrentModificationException}.</li>
 * </ul>
 *
 * <h2>3. Infinite Streams: generate() vs iterate()</h2>
 * <ul>
 *   <li>{@code Stream.generate(Supplier<T>)}: Unordered, stateless generation. Must be bounded with {@code limit(n)}.</li>
 *   <li>{@code Stream.iterate(T seed, UnaryOperator<T> f)}: Sequential sequence generation: $s, f(s), f(f(s))\dots$</li>
 *   <li>{@code Stream.iterate(T seed, Predicate hasNext, UnaryOperator f)} (Java 9+): Stream equivalent of a traditional for-loop!</li>
 * </ul>
 *
 * <h2>4. Modern Stream Sources (Java 21+ Sequenced Collections):</h2>
 * <p>
 * Java 21 introduced {@link SequencedCollection}, allowing any ordered collection (List, Deque, LinkedHashSet)
 * to be streamed in reverse directly via {@code collection.reversed().stream()} without any intermediate array copies.
 * </p>
 */
public class Lesson02_StreamCreation {

    /**
     * SAMPLE 1: Creating Streams from Collections, Arrays, and Individual Elements
     * What this demonstrates:
     * - The 4 most common stream source entrypoints in day-to-day backend development.
     * - Array slicing directly into a stream without copying elements.
     */
    @Test
    @DisplayName("01. Basic Stream Sources: Collections, Arrays, Values, Builder")
    void demonstrateBasicSources() {
        // 1. From java.util.Collection
        List<Employee> employees = DataFactory.getEmployees();
        Stream<Employee> listStream = employees.stream();
        assertThat(listStream.count()).isEqualTo(30);

        // 2. From Stream.of(...)
        Stream<String> directStream = Stream.of("Google", "Meta", "Amazon", "Apple", "Netflix");
        assertThat(directStream.toList()).hasSize(5);

        // 3. From Array (Full array and sliced range)
        int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23};
        IntStream fullArrayStream = Arrays.stream(primes);
        assertThat(fullArrayStream.sum()).isEqualTo(100);

        // Sliced array stream: index 2 inclusive to 5 exclusive -> elements [5, 7, 11]
        // Zero-copy! Does not allocate an intermediate sub-array.
        IntStream slicedStream = Arrays.stream(primes, 2, 5);
        assertThat(slicedStream.toArray()).containsExactly(5, 7, 11);

        // 4. Stream.builder(): Programmatic imperative creation before streaming
        Stream.Builder<String> builder = Stream.builder();
        builder.add("L6 Staff");
        builder.add("L7 Principal");
        builder.add("L8 Distinguished");
        Stream<String> careerStream = builder.build();
        assertThat(careerStream.toList()).containsExactly("L6 Staff", "L7 Principal", "L8 Distinguished");
    }

    /**
     * SAMPLE 2: Infinite Streams with generate() and iterate()
     * What this demonstrates:
     * - How to generate unbounded sequences safely.
     * - Using Java 9 iterate with predicate termination.
     */
    @Test
    @DisplayName("02. Infinite Streams: generate() vs iterate()")
    void demonstrateInfiniteStreams() {
        // Stream.generate(Supplier) - Unbounded, must use limit()
        Stream<UUID> uuidStream = Stream.generate(UUID::randomUUID).limit(3);
        List<UUID> uuids = uuidStream.toList();
        assertThat(uuids).hasSize(3);

        // Stream.iterate(seed, UnaryOperator) - Infinite arithmetic progression
        // Powers of 2: 1, 2, 4, 8, 16, 32
        List<Integer> powersOfTwo = Stream.iterate(1, n -> n * 2)
                .limit(6)
                .toList();
        assertThat(powersOfTwo).containsExactly(1, 2, 4, 8, 16, 32);

        // Java 9 iterate with predicate: for (int i = 1; i < 100; i *= 2)
        List<Integer> powersUnder100 = Stream.iterate(1, n -> n < 100, n -> n * 2).toList();
        assertThat(powersUnder100).containsExactly(1, 2, 4, 8, 16, 32, 64);
    }

    /**
     * SAMPLE 3: Primitive Streams: IntStream, LongStream, DoubleStream
     * What this demonstrates:
     * - Zero-allocation primitive numeric processing.
     * - The difference between range() (exclusive) and rangeClosed() (inclusive).
     */
    @Test
    @DisplayName("03. Primitive Numeric Streams & Specialized Ranges")
    void demonstratePrimitiveNumericStreams() {
        // range(0, 5) -> 0, 1, 2, 3, 4 (exclusive end)
        int[] exclusive = IntStream.range(0, 5).toArray();
        assertThat(exclusive).containsExactly(0, 1, 2, 3, 4);

        // rangeClosed(0, 5) -> 0, 1, 2, 3, 4, 5 (inclusive end)
        int[] inclusive = IntStream.rangeClosed(0, 5).toArray();
        assertThat(inclusive).containsExactly(0, 1, 2, 3, 4, 5);

        // High-performance sum, min, max, average without boxing
        int sum = IntStream.rangeClosed(1, 100).sum(); // n*(n+1)/2 = 5050
        assertThat(sum).isEqualTo(5050);
    }

    /**
     * SAMPLE 4: Streams from Strings, Regex & I/O Readers
     * What this demonstrates:
     * - Streaming character code points from strings.
     * - Pattern.splitAsStream() for regex tokenization.
     * - BufferedReader.lines() for streaming I/O lines lazily.
     */
    @Test
    @DisplayName("04. Text & I/O Streams: chars(), Pattern.splitAsStream(), BufferedReader.lines()")
    void demonstrateStringAndIOStreams() {
        // 1. chars(): Returns IntStream of Unicode code points
        String text = "JAVA";
        List<Character> charList = text.chars()
                .mapToObj(c -> (char) c)
                .toList();
        assertThat(charList).containsExactly('J', 'A', 'V', 'A');

        // 2. Pattern.splitAsStream: Zero-copy regex streaming
        Pattern csvSplitter = Pattern.compile("\\s*,\\s*");
        List<String> tokens = csvSplitter.splitAsStream("Engineering, Data Science, Marketing, Design")
                .map(String::toUpperCase)
                .toList();
        assertThat(tokens).containsExactly("ENGINEERING", "DATA SCIENCE", "MARKETING", "DESIGN");

        // 3. BufferedReader.lines(): Stream file lines lazily without loading whole file to RAM
        String multilineContent = "line1\nline2\nline3";
        BufferedReader reader = new BufferedReader(new StringReader(multilineContent));
        List<String> lines = reader.lines().toList();
        assertThat(lines).containsExactly("line1", "line2", "line3");
    }

    /**
     * SAMPLE 5: Streams from Maps (EntrySet, KeySet, Values)
     * What this demonstrates:
     * - Maps do NOT have a direct .stream() method!
     * - You must stream either entrySet(), keySet(), or values().
     */
    @Test
    @DisplayName("05. Streaming Maps via entrySet(), keySet(), and values()")
    void demonstrateMapStreaming() {
        Map<String, Double> deptBudgets = Map.of(
                "Engineering", 4_500_000.0,
                "Marketing", 1_200_000.0,
                "Design", 800_000.0
        );

        // Stream entries to filter by budget and extract department names
        List<String> largeDepts = deptBudgets.entrySet().stream()
                .filter(entry -> entry.getValue() >= 1_000_000.0)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();

        assertThat(largeDepts).containsExactly("Engineering", "Marketing");
    }

    /**
     * SAMPLE 6: The Single-Use Rule & Stream Lifecycle
     * What this demonstrates:
     * - A stream is consumable. Once a terminal operation is executed, the pipeline closes.
     * - Attempting to reuse a stream throws IllegalStateException.
     */
    @Test
    @DisplayName("06. Stream Lifecycle: Consumed Pipelines Cannot Be Reused")
    void demonstrateSingleUseException() {
        Stream<String> techStream = Stream.of("Go", "Rust", "Java");

        // First terminal operation consumes the stream:
        long count = techStream.count();
        assertThat(count).isEqualTo(3);

        // Attempting a second terminal operation throws:
        assertThatThrownBy(techStream::toList)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stream has already been operated upon or closed");
    }

    /**
     * SAMPLE 7: Sequenced Collections Reversed Stream (Java 21+)
     * What this demonstrates:
     * - Streaming in reverse encounter order without sorting or array allocation.
     */
    @Test
    @DisplayName("07. Modern Stream Sources: SequencedCollection.reversed().stream() (Java 21+)")
    void demonstrateSequencedCollectionStream() {
        List<String> pipelineStages = List.of("Extract", "Transform", "Load");

        // Java 21 reversed stream view
        List<String> reverseOrder = pipelineStages.reversed().stream().toList();

        assertThat(reverseOrder).containsExactly("Load", "Transform", "Extract");
    }
}
