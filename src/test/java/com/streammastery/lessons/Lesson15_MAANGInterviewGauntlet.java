package com.streammastery.lessons;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * LESSON 15: THE MAANG INTERVIEW GAUNTLET (SOLVED LEETCODE MEDIUM/HARD SUITE)
 * ====================================================================================================
 *
 * <h2>MAANG Senior / Staff Interview Expectations:</h2>
 * In technical rounds at Meta, Apple, Amazon, Netflix, and Google, solving a problem imperatively
 * with 40 lines of nested while-loops, index pointers, and mutable booleans signals junior-level coding.
 * Implementing clean, declarative stream pipelines demonstrates:
 * <ol>
 *   <li>Mastery of modern Java language primitives.</li>
 *   <li>Fluency with category theory, combinatorics, and functional idioms.</li>
 *   <li>Defensive, immutability-first engineering that prevents concurrency bugs by design.</li>
 * </ol>
 *
 * <h3>Below are 5 Fully Solved, High-Frequency MAANG LeetCode Problems:</h3>
 */
public class Lesson15_MAANGInterviewGauntlet {

    /**
     * PROBLEM 1: LeetCode 692: Top K Frequent Words
     * Complexity: Time $O(N \log K)$ or $O(N \log N)$, Space $O(N)$
     * Key Idea: Grouping with counting -> stream entrySet -> custom sort (frequency DESC, word ASC) -> limit(k).
     */
    @Test
    @DisplayName("01. Solved: LeetCode 692 — Top K Frequent Words")
    void solveTopKFrequentWords() {
        List<String> words = List.of("i", "love", "leetcode", "i", "love", "coding", "i", "coding");
        int k = 2;

        List<String> topK = words.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Comparator
                        .<Map.Entry<String, Long>, Long>comparing(Map.Entry::getValue, Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey))
                .limit(k)
                .map(Map.Entry::getKey)
                .toList();

        // "i" has frequency 3; "coding" and "love" have frequency 2 ("coding" < "love" alphabetically)
        assertThat(topK).containsExactly("i", "coding");
    }

    /**
     * PROBLEM 2: LeetCode 49: Group Anagrams
     * Complexity: Time $O(N \times K \log K)$ where K is max word length, Space $O(N \times K)$
     * Key Idea: Canonical sorting function maps every word to its sorted character signature.
     */
    @Test
    @DisplayName("02. Solved: LeetCode 49 — Group Anagrams")
    void solveGroupAnagrams() {
        List<String> strs = List.of("eat", "tea", "tan", "ate", "nat", "bat");

        Function<String, String> canonicalKey = s -> {
            char[] arr = s.toCharArray();
            Arrays.sort(arr);
            return new String(arr);
        };

        Map<String, List<String>> grouped = strs.stream()
                .collect(Collectors.groupingBy(canonicalKey));

        assertThat(grouped.values()).hasSize(3);
        assertThat(grouped.get("aet")).containsExactlyInAnyOrder("eat", "tea", "ate");
        assertThat(grouped.get("ant")).containsExactlyInAnyOrder("tan", "nat");
        assertThat(grouped.get("abt")).containsExactlyInAnyOrder("bat");
    }

    /**
     * PROBLEM 3: LeetCode 867: Matrix Transpose
     * Complexity: Time $O(R \times C)$, Space $O(R \times C)$
     * Key Idea: Stream columns as outer dimension, stream rows as inner dimension.
     */
    @Test
    @DisplayName("03. Solved: LeetCode 867 — Matrix Transpose")
    void solveMatrixTranspose() {
        int[][] matrix = {
                {1, 2, 3},
                {4, 5, 6}
        };

        int rows = matrix.length;
        int cols = matrix[0].length;

        int[][] transposed = IntStream.range(0, cols)
                .mapToObj(c -> IntStream.range(0, rows).map(r -> matrix[r][c]).toArray())
                .toArray(int[][]::new);

        assertThat(transposed).isDeepEqualTo(new int[][]{
                {1, 4},
                {2, 5},
                {3, 6}
        });
    }

    /**
     * PROBLEM 4: LeetCode 347: Top K Frequent Elements
     * Complexity: Time $O(N \log N)$, Space $O(N)$
     * Key Idea: Boxed IntStream -> frequency map -> entry sort -> mapToInt -> toArray.
     */
    @Test
    @DisplayName("04. Solved: LeetCode 347 — Top K Frequent Elements")
    void solveTopKFrequentElements() {
        int[] nums = {1, 1, 1, 2, 2, 3};
        int k = 2;

        int[] topK = Arrays.stream(nums)
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(k)
                .mapToInt(Map.Entry::getKey)
                .toArray();

        assertThat(topK).containsExactly(1, 2);
    }

    /**
     * PROBLEM 5: Run-Length Encoding (Compression Algorithm)
     * Key Idea: Grouping adjacent characters into count signatures.
     */
    @Test
    @DisplayName("05. Solved: Run-Length Encoding: 'aaabbc' -> 'a3b2c1'")
    void solveRunLengthEncoding() {
        String input = "aaabbc";

        // Count frequencies preserving encounter order using LinkedHashMap:
        String encoded = input.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> "" + e.getKey() + e.getValue())
                .collect(Collectors.joining());

        assertThat(encoded).isEqualTo("a3b2c1");
    }
}
