package com.streammastery.lessons;

import com.streammastery.model.Order;
import com.streammastery.model.OrderItem;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * LESSON 10: ADVANCED FLATMAP, COMBINATORICS & STREAM COMPOSITION
 * ====================================================================================================
 *
 * <h2>1. flatMap as the Monadic Bind Operation</h2>
 * <p>
 * In category theory and functional programming, {@code flatMap} is the <i>monadic bind</i> operation ($M[T] \to (T \to M[R]) \to M[R]$).
 * It enables:
 * <ul>
 *   <li><b>Cartesian Joins (Cross Products):</b> Generating all permutations of pairs across two streams ($O(M \times N)$).</li>
 *   <li><b>Zipping Elements:</b> Pairing elements from two distinct lists by streaming coordinate indices ($[0, \min(size_A, size_B))$).</li>
 *   <li><b>Inverted Index Construction:</b> The cornerstone algorithm behind full-text search engines (ElasticSearch, Google Search).</li>
 * </ul>
 * </p>
 *
 * <h2>2. Stream Concatenation Best Practices:</h2>
 * <p>
 * When combining streams:
 * <ul>
 *   <li>{@code Stream.concat(a, b)}: Safe for combining TWO streams.</li>
 *   <li><b>Deep Concat Anti-Pattern:</b> Chaining {@code Stream.concat(a, Stream.concat(b, Stream.concat(c, d)))} creates
 *       a deeply unbalanced tree of streams, causing {@link StackOverflowError} on traversal!</li>
 *   <li><b>MAANG Solution:</b> For 3 or more streams, use:
 *       {@code Stream.of(s1, s2, s3, s4).flatMap(Function.identity())}. This flattens into an $O(1)$ depth pipeline!</li>
 * </ul>
 * </p>
 */
public class Lesson10_AdvancedFlatMapAndComposition {

    /**
     * SAMPLE 1: Cartesian Product (Cross Join)
     * What this demonstrates:
     * - Generating all combinations of cards (Ranks x Suits).
     * - Nested flatMap pattern.
     */
    @Test
    @DisplayName("01. Cartesian Product (Cross Join) using nested flatMap")
    void demonstrateCartesianJoin() {
        List<String> ranks = List.of("A", "K", "Q");
        List<String> suits = List.of("♠", "♥");

        // Cartesian product: Every rank paired with every suit
        List<String> deck = ranks.stream()
                .flatMap(rank -> suits.stream().map(suit -> rank + suit))
                .toList();

        assertThat(deck).containsExactly("A♠", "A♥", "K♠", "K♥", "Q♠", "Q♥");
    }

    /**
     * SAMPLE 2: Zipping Two Lists with IntStream.range
     * What this demonstrates:
     * - Pairing elements from two lists without an external index variable.
     */
    @Test
    @DisplayName("02. Zipping Two Lists with Coordinate IntStream.range()")
    void demonstrateZipping() {
        List<String> names = List.of("Alice", "Bob", "Carol");
        List<Integer> scores = List.of(95, 88, 92);

        // Zip by index
        List<String> scorecards = IntStream.range(0, Math.min(names.size(), scores.size()))
                .mapToObj(i -> names.get(i) + ": " + scores.get(i))
                .toList();

        assertThat(scorecards).containsExactly("Alice: 95", "Bob: 88", "Carol: 92");
    }

    /**
     * SAMPLE 3: Inverted Search Index Construction
     * What this demonstrates:
     * - Tokenizing documents and grouping word appearances by document index.
     * - The foundational data structure of Elasticsearch and Google Search.
     */
    @Test
    @DisplayName("03. Search Engine Inverted Index Construction from Text")
    void demonstrateInvertedIndex() {
        List<String> docs = List.of(
                "java streams functional",
                "parallel streams concurrency",
                "functional java lambda"
        );

        // Map: word -> Set of document indices where the word appears
        Map<String, Set<Integer>> invertedIndex = IntStream.range(0, docs.size())
                .boxed()
                .flatMap(docIndex -> Arrays.stream(docs.get(docIndex).split("\\s+"))
                        .map(word -> Map.entry(word, docIndex)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                ));

        assertThat(invertedIndex.get("streams")).containsExactlyInAnyOrder(0, 1);
        assertThat(invertedIndex.get("functional")).containsExactlyInAnyOrder(0, 2);
    }

    /**
     * SAMPLE 4: Multi-Stream Concatenation (Avoiding StackOverflow)
     * What this demonstrates:
     * - Combining multiple stream sources using Stream.of(...).flatMap(identity).
     */
    @Test
    @DisplayName("04. Multi-Stream Concatenation without Deep Recursion")
    void demonstrateMultiStreamConcat() {
        Stream<String> s1 = Stream.of("A", "B");
        Stream<String> s2 = Stream.of("C", "D");
        Stream<String> s3 = Stream.of("E", "F");

        // Clean flattening of N streams:
        List<String> combined = Stream.of(s1, s2, s3)
                .flatMap(s -> s)
                .toList();

        assertThat(combined).containsExactly("A", "B", "C", "D", "E", "F");
    }
}
