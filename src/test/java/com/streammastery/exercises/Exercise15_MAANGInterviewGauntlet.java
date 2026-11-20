package com.streammastery.exercises;

import com.streammastery.model.*;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * EXERCISE 15: THE MAANG INTERVIEW GAUNTLET (20 HARD INTERVIEW PROBLEMS)
 * ====================================================================================================
 *
 * Welcome to the final boss of the curriculum.
 * Every problem here is drawn directly from technical phone screens and on-site coding rounds
 * at Google, Meta, Amazon, Apple, and Netflix.
 *
 * Solve all 20 problems using pure, idiomatic Java Streams. No imperative for-loops allowed!
 */
public class Exercise15_MAANGInterviewGauntlet {

    @Test
    @DisplayName("01 [★★★☆☆] LeetCode 692: Top 3 Most Frequent Words in Sentences")
    void problem01_top3Words() {
        List<String> sentences = List.of(
                "Java streams are fast and declarative",
                "Streams allow functional programming in Java",
                "Declarative streams make Java powerful"
        );
        // Tokenize words (lowercase, ignore punctuation), find top 3 words by count desc, then word asc
        // TODO: Complete the pipeline
        List<String> top3 = null;

        assertThat(top3).isNotNull();
        assertThat(top3).containsExactly("java", "streams", "declarative");
    }

    @Test
    @DisplayName("02 [★★★☆☆] LeetCode 49: Group Anagrams")
    void problem02_groupAnagrams() {
        List<String> words = List.of("listen", "silent", "enlist", "rat", "tar", "art", "evil", "vile", "live");
        // TODO: Group anagrams into Collection<List<String>>
        Collection<List<String>> anagramGroups = null;

        assertThat(anagramGroups).isNotNull();
        assertThat(anagramGroups).hasSize(3);
    }

    @Test
    @DisplayName("03 [★★★☆☆] Two Sum in Stream: Find all pairs of employees whose combined salary == $400,000")
    void problem03_twoSumSalary() {
        List<Employee> employees = DataFactory.getEmployees();
        double targetSum = 400_000.0;

        // Find pairs (e1, e2) where e1.id < e2.id and e1.salary + e2.salary == targetSum
        // TODO: Build pairs
        List<String> pairs = null;

        assertThat(pairs).isNotNull();
        assertThat(pairs).isNotEmpty();
    }

    @Test
    @DisplayName("04 [★★★★☆] Character Frequency Histogram sorted by frequency desc, then char asc")
    void problem04_frequencyHistogram() {
        String text = "mississippi";
        // 'i': 4, 's': 4, 'p': 2, 'm': 1
        // Formatted entries: "i:4", "s:4", "p:2", "m:1"
        // TODO: Complete pipeline
        List<String> histogram = null;

        assertThat(histogram).isNotNull();
        assertThat(histogram).containsExactly("i:4", "s:4", "p:2", "m:1");
    }

    @Test
    @DisplayName("05 [★★★★☆] Run-Length Encoding: 'aaabbc' -> 'a3b2c1'")
    void problem05_runLengthEncoding() {
        String input = "aaabbc";
        // Group consecutive duplicate characters and format as char + count
        // TODO: Implement RLE using streams
        String encoded = null;

        assertThat(encoded).isNotNull();
        assertThat(encoded).isEqualTo("a3b2c1");
    }

    @Test
    @DisplayName("06 [★★★★☆] Merge K Sorted Lists into a Single Sorted List")
    void problem06_mergeKSortedLists() {
        List<List<Integer>> kLists = List.of(
                List.of(1, 4, 7),
                List.of(2, 5, 8),
                List.of(3, 6, 9)
        );
        // TODO: Merge and sort into List<Integer>
        List<Integer> merged = null;

        assertThat(merged).isNotNull();
        assertThat(merged).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Test
    @DisplayName("07 [★★★★☆] Longest Consecutive Sequence Length")
    void problem07_longestConsecutiveSequence() {
        int[] nums = {100, 4, 200, 1, 3, 2};
        // Longest consecutive sequence is [1, 2, 3, 4] -> length 4
        // TODO: Find length of longest sequence using streams
        int longestLength = 0;

        assertThat(longestLength).isEqualTo(4);
    }

    @Test
    @DisplayName("08 [★★★★★] Dependency Graph: Find all tasks with zero pending dependencies")
    void problem08_readyTasks() {
        Map<String, List<String>> taskDependencies = Map.of(
                "Deploy", List.of("Build", "Test"),
                "Test", List.of("Build"),
                "Build", List.of(),
                "Lint", List.of()
        );

        // Ready tasks have empty dependency list
        // TODO: Extract task names with no dependencies
        List<String> readyTasks = null;

        assertThat(readyTasks).isNotNull();
        assertThat(readyTasks).containsExactlyInAnyOrder("Build", "Lint");
    }

    @Test
    @DisplayName("09 [★★★★★] LeetCode 347: Top K Frequent Elements")
    void problem09_topKFrequent() {
        int[] nums = {1, 1, 1, 2, 2, 3};
        int k = 2;
        // Top 2 elements: 1 (count 3) and 2 (count 2)
        // TODO: Return int[] of top k elements
        int[] topK = null;

        assertThat(topK).isNotNull();
        assertThat(topK).containsExactly(1, 2);
    }

    @Test
    @DisplayName("10 [★★★★★] Market Basket Analysis: Top Co-purchased Category Pairs")
    void problem10_coPurchasedCategories() {
        // From DataFactory.getOrders():
        // For each order, extract distinct product categories.
        // If an order has categories [A, B, C], it generates pairs: A-B, A-C, B-C (sorted)
        // Count pair frequency across all orders and return the most frequent pair!
        // TODO: Complete the market basket pipeline
        String topPair = null;

        assertThat(topPair).isNotNull();
        assertThat(topPair).isNotEmpty();
    }

    @Test
    @DisplayName("11 [★★★☆☆] LeetCode 217: Contains Duplicate in Array")
    void problem11_containsDuplicate() {
        int[] nums = {1, 2, 3, 1};
        // Check if array has duplicates using streams
        // TODO: Arrays.stream(nums).distinct().count() != nums.length
        boolean hasDuplicate = false;

        assertThat(hasDuplicate).isTrue();
    }

    @Test
    @DisplayName("12 [★★★☆☆] LeetCode 242: Valid Anagram")
    void problem12_validAnagram() {
        String s = "anagram";
        String t = "nagaram";

        // TODO: Check if s and t have identical sorted character sequences using streams
        boolean isAnagram = false;

        assertThat(isAnagram).isTrue();
    }

    @Test
    @DisplayName("13 [★★★☆☆] Find First Non-Repeating Character in String")
    void problem13_firstNonRepeatingChar() {
        String input = "swiss"; // 'w' is first non-repeating
        // TODO: Stream characters into LinkedHashMap with counting, find first with count == 1
        Optional<Character> firstUnique = Optional.empty();

        assertThat(firstUnique).contains('w');
    }

    @Test
    @DisplayName("14 [★★★★☆] LeetCode 238: Product of Array Except Self")
    void problem14_productExceptSelf() {
        int[] nums = {1, 2, 3, 4};
        // Result for index i is product of all elements except nums[i]: [24, 12, 8, 6]
        // TODO: Calculate using streams
        int[] result = null;

        assertThat(result).isNotNull();
        assertThat(result).containsExactly(24, 12, 8, 6);
    }

    @Test
    @DisplayName("15 [★★★★☆] Rotate Matrix 90 Degrees Clockwise")
    void problem15_rotateMatrix() {
        int[][] matrix = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        // Rotated 90 deg clockwise:
        // { {7, 4, 1}, {8, 5, 2}, {9, 6, 3} }
        // TODO: Rotate using 2D coordinate streaming
        int[][] rotated = null;

        assertThat(rotated).isNotNull();
        assertThat(rotated).isDeepEqualTo(new int[][]{
                {7, 4, 1},
                {8, 5, 2},
                {9, 6, 3}
        });
    }

    @Test
    @DisplayName("16 [★★★★☆] Longest Common Prefix across Strings")
    void problem16_longestCommonPrefix() {
        List<String> words = List.of("flower", "flow", "flight");
        // Longest common prefix is "fl"
        // TODO: Find longest common prefix using streams
        String prefix = null;

        assertThat(prefix).isNotNull();
        assertThat(prefix).isEqualTo("fl");
    }

    @Test
    @DisplayName("17 [★★★★☆] Find All Subarrays of Size K with Sum >= Threshold")
    void problem17_subarraysSumThreshold() {
        int[] arr = {2, 2, 2, 2, 5, 5, 5, 8};
        int k = 3;
        int threshold = 4; // Subarray average >= 4 (sum >= 12)
        // Subarrays of size 3:
        // [2,2,2]->6, [2,2,2]->6, [2,2,5]->9, [2,5,5]->12*, [5,5,5]->15*, [5,5,8]->18*
        // Total valid subarrays: 3
        // TODO: Use IntStream.rangeClosed(0, arr.length - k) to count
        long validCount = 0;

        assertThat(validCount).isEqualTo(3);
    }

    @Test
    @DisplayName("18 [★★★★★] Merge Overlapping Intervals using Stream / Reducer")
    void problem18_mergeIntervals() {
        record Interval(int start, int end) {}
        List<Interval> intervals = List.of(
                new Interval(1, 3),
                new Interval(2, 6),
                new Interval(8, 10),
                new Interval(15, 18)
        );
        // Merged: [1, 6], [8, 10], [15, 18]
        // TODO: Sort intervals by start and merge overlapping intervals
        List<Interval> merged = null;

        assertThat(merged).isNotNull();
        assertThat(merged).containsExactly(
                new Interval(1, 6),
                new Interval(8, 10),
                new Interval(15, 18)
        );
    }

    @Test
    @DisplayName("19 [★★★★★] Topological Sort Validation: Verify Task Execution Sequence")
    void problem19_topologicalSortValidation() {
        Map<String, List<String>> graph = Map.of(
                "Build", List.of("Test"),
                "Test", List.of("Deploy")
        );
        List<String> proposedSequence = List.of("Build", "Test", "Deploy");

        // Verify that for every edge A -> B, A appears before B in proposedSequence
        // TODO: Verify valid topological ordering using streams
        boolean isValidSequence = false;

        assertThat(isValidSequence).isTrue();
    }

    @Test
    @DisplayName("20 [★★★★★] Stream-Based Trie Autocomplete / Prefix Search")
    void problem20_prefixSearchEngine() {
        List<String> dictionary = List.of("apple", "app", "application", "apt", "banana", "bat");
        String prefix = "app";

        // Find all dictionary words starting with prefix, sorted by length ascending, then alphabetically
        // TODO: Complete stream pipeline
        List<String> completions = null;

        assertThat(completions).isNotNull();
        assertThat(completions).containsExactly("app", "apple", "application");
    }
}
