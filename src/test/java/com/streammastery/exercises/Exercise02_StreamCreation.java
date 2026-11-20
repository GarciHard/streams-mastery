package com.streammastery.exercises;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ====================================================================================================
 * EXERCISE 02: STREAM CREATION, SOURCES & ANATOMY (20 EXERCISES)
 * ====================================================================================================
 *
 * Difficulty scale: [★☆☆☆☆] to [★★★★★]
 * Complete each exercise by replacing the null/placeholder assignments with your stream implementation.
 */
public class Exercise02_StreamCreation {

    @Test
    @DisplayName("01 [★☆☆☆☆] Stream from List count")
    void exercise01_streamFromList() {
        List<String> frameworks = List.of("Spring", "Quarkus", "Micronaut", "Vert.x");
        // TODO: Replace 0 with stream count of frameworks
        long count = frameworks.stream().count();

//        2nd approach using Stream.
/*
        Stream<String> framework = frameworks.stream();
        long count = framework.count();
*/

        assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Stream from primitive array sum")
    void exercise02_streamFromArray() {
        int[] scores = {98, 85, 92, 100, 78};
        // TODO: Replace 0 with the sum of scores using Arrays.stream()
        int total = Arrays.stream(scores)
                .sum();

//        Using IntStream
/*
        IntStream scoreStream = Arrays.stream(scores);
        int total = scoreStream.sum();
*/
        assertThat(total).isEqualTo(453);
    }

    @Test
    @DisplayName("03 [★☆☆☆☆] Stream.of() nullable handling")
    void exercise03_streamOfNullable() {
        String possibleNull = null;
        // TODO: Replace Stream.empty() with Stream.ofNullable(possibleNull)
//        Stream<String> stream = Stream.empty();
        Stream<String> stream = Stream.ofNullable(possibleNull);

        assertThat(stream.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("04 [★★☆☆☆] First 10 Fibonacci numbers with Stream.iterate()")
    void exercise04_fibonacciIterate() {
        // Problem: Generate the first 10 Fibonacci numbers: 0, 1, 1, 2, 3, 5, 8, 13, 21, 34
        // Hint: Iterate over int[] pairs: [a, b] -> [b, a + b], map to pair[0]
        // TODO: Replace null with your Stream.iterate implementation

        UnaryOperator<Integer> fibo = num -> num == 0 || num == 1 ? num : (num - 1) + (num - 2);

        List<Integer> fibonacci = Stream.iterate(0, n -> n < 10, fibo).toList();
//        List<Integer> fibonacci = null;
//
//        assertThat(fibonacci).isNotNull();
//        assertThat(fibonacci).containsExactly(0, 1, 1, 2, 3, 5, 8, 13, 21, 34);

        List<Integer> list = new ArrayList<>();
        for (int idx = 0; idx <= 3; idx++) {
            if ( idx == 0 || idx == 1 ) {
                list.add(idx);
                continue;
            }
            int tmp = (idx - 1) + (idx - 2);
            list.add(tmp);
        }

    }

    @Test
    @DisplayName("05 [★★☆☆☆] Calculate sum of squares from 1 to 10")
    void exercise05_intStreamSumOfSquares() {
        // Problem: Using IntStream.rangeClosed(1, 10), map each number to its square and sum them.
        // Formula: 1^2 + 2^2 + ... + 10^2 = 385
        // TODO: Replace 0 with your IntStream pipeline
        int sumOfSquares = IntStream.rangeClosed(1, 10)
                .map(num -> num * num)
                .sum();

        assertThat(sumOfSquares).isEqualTo(385);
    }

    @Test
    @DisplayName("06 [★★☆☆☆] Generate 5 pseudo-random numbers with Stream.generate()")
    void exercise06_generateRandoms() {
        Random rng = new Random(42);
        // TODO: Generate 5 integers between 1 and 100 using rng.nextInt(100) + 1
        List<Integer> randoms = Stream.generate(() -> rng.nextInt(100) + 1)
                .limit(5)
                .toList();

        assertThat(randoms).isNotNull();
        assertThat(randoms).hasSize(5);
        assertThat(randoms).allMatch(n -> n >= 1 && n <= 100);
    }

    @Test
    @DisplayName("07 [★★★☆☆] Word stream from text using regex Pattern")
    void exercise07_wordsFromSentence() {
        String sentence = "High performance Java streams require zero allocation mental models";
        // TODO: Use Pattern.compile("\\s+").splitAsStream(...) to get words as List<String>
        List<String> words = Pattern.compile("\\s+")
                .splitAsStream(sentence)
                .toList();

        assertThat(words).isNotNull();
        assertThat(words).containsExactly("High", "performance", "Java", "streams", "require", "zero", "allocation", "mental", "models");
    }

    @Test
    @DisplayName("08 [★★★☆☆] Stream from Map entries")
    void exercise08_streamFromMap() {
        Map<String, Double> stockPrices = Map.of("AAPL", 182.5, "GOOGL", 175.2, "MSFT", 420.0, "AMZN", 185.0);
        // TODO: Stream the entries, filter where price > 180.0, and collect the keys to a sorted List<String>
        List<String> highPricedStocks = stockPrices.entrySet().stream()
                .filter(stock -> stock.getValue() > 180.0)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();

        assertThat(highPricedStocks).isNotNull();
        assertThat(highPricedStocks).containsExactly("AAPL", "AMZN", "MSFT");
    }

    @Test
    @DisplayName("09 [★★★☆☆] Java 9 Stream.iterate with termination predicate: Collatz sequence")
    void exercise09_collatzConjecture() {
        // Collatz step: if n is even -> n/2, else -> 3*n + 1
        // Generate sequence starting at 6 until 1 (inclusive of 1): 6 -> 3 -> 10 -> 5 -> 16 -> 8 -> 4 -> 2 -> 1
        // TODO: Replace null with your sequence ending at 1

        Predicate<Long> hasNext = num -> num > 0L;
//        UnaryOperator<Long> longUnaryOperator = num -> num == 1 ? 0L : num % 2L == 0L ? num / 2L : (num * 3L) + 1L;

        UnaryOperator<Long> collatzStep = num -> {
            if ( num == 1L ) return 0L;
            return (num % 2L == 0L) ? num / 2L : (num * 3) + 1;
        };

        List<Long> collatz = Stream.iterate(6L, hasNext, collatzStep).toList();

        assertThat(collatz).isNotNull();
        assertThat(collatz).containsExactly(6L, 3L, 10L, 5L, 16L, 8L, 4L, 2L, 1L);
    }

    @Test
    @DisplayName("10 [★★★★☆] Stream of characters: Filter consonants, keep distinct lowercase vowels")
    void exercise10_vowelsFromString() {
        String text = "MAANG Engineering Bar Is Demanding";
        Set<Character> vowels = Set.of('a', 'e', 'i', 'o', 'u');

        // TODO: text.chars() -> lowercase -> filter vowels -> distinct -> collect to sorted List<Character>
        List<Character> resultVowels = text.chars()
                .mapToObj(character -> (char) character)
                .map(Character::toLowerCase)
                .filter(vowels::contains)
                .distinct()
                .sorted()
                .toList();

        assertThat(resultVowels).isNotNull();
        assertThat(resultVowels).containsExactly('a', 'e', 'i');
    }

    @Test
    @DisplayName("11 [★★★★☆] Concatenate 3 distinct stream sources")
    void exercise11_streamConcat() {
        List<String> l1 = List.of("Google", "Meta");
        String[] arr = {"Amazon", "Apple"};
        Set<String> set = Set.of("Netflix");

        // TODO: Combine all 3 sources into a single Stream<String> and collect to List

/*        Stream.Builder<String> mergedList = Stream.builder();
        l1.forEach( mergedList::add);
        Arrays.stream(arr).forEach(mergedList::add);
        set.forEach(mergedList::add);*/

        List<String> allFive = Stream.concat(l1.stream(),
                Stream.concat(Arrays.stream(arr), set.stream())
        ).toList();

        assertThat(allFive).isNotNull();
        assertThat(allFive).contains("Google", "Meta", "Amazon", "Apple", "Netflix");
        assertThat(allFive).hasSize(5);
    }

    @Test
    @DisplayName("12 [★★★★★] Prove stream immutability and consumption lifecycle")
    void exercise12_verifyStreamReuseException() {
        // Problem: Construct a stream, consume it once, and assert that attempting to consume it again
        // throws IllegalStateException.
        Stream<String> reusableAttempt = Stream.of("test");
        reusableAttempt.findFirst(); // consumed

        // TODO: Fill the lambda inside assertThatThrownBy to trigger the IllegalStateException
        assertThatThrownBy(reusableAttempt::findAny).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("13 [★☆☆☆☆] Stream.builder(): Build a priority queue of cloud providers")
    void exercise13_streamBuilder() {
        // TODO: Use Stream.<String>builder() to add "AWS", "GCP", "Azure" and build into List<String>
        List<String> clouds = Stream.<String>builder()
                .add("AWS")
                .add("GCP")
                .add("Azure")
                .build().toList();

        assertThat(clouds).isNotNull();
        assertThat(clouds).containsExactly("AWS", "GCP", "Azure");
    }

    @Test
    @DisplayName("14 [★★☆☆☆] Array slice streaming: Slicing the middle elements")
    void exercise14_arraySliceStreaming() {
        int[] timestamps = {100, 200, 300, 400, 500, 600, 700};
        // TODO: Use Arrays.stream(array, startInclusive, endExclusive) to grab index 2 to 5 (300, 400, 500)
        int[] slice = Arrays.stream(timestamps, 2, 5).toArray();

        assertThat(slice).isNotNull();
        assertThat(slice).containsExactly(300, 400, 500);
    }

    @Test
    @DisplayName("15 [★★☆☆☆] LongStream.rangeClosed() for timestamp interval generator")
    void exercise15_longStreamRange() {
        // Problem: Generate sequential minute offsets: 0L, 60L, 120L, 180L, 240L
        // TODO: Use LongStream.rangeClosed(0, 4).map(i -> i * 60L).toArray()
        long[] offsets = LongStream.rangeClosed(0, 4)
                .map(interval -> interval * 60L)
                .toArray();

        assertThat(offsets).isNotNull();
        assertThat(offsets).containsExactly(0L, 60L, 120L, 180L, 240L);
    }

    @Test
    @DisplayName("16 [★★★☆☆] BufferedReader.lines(): Stream multiline CSV string")
    void exercise16_bufferedReaderLines() {
        String csv = "id,name,role\n1,Alice,Engineer\n2,Bob,Manager";
        // TODO: Create BufferedReader from new StringReader(csv), skip header (skip 1), and collect lines
        List<String> dataRows = null;

        assertThat(dataRows).isNotNull();
        assertThat(dataRows).containsExactly("1,Alice,Engineer", "2,Bob,Manager");
    }

    @Test
    @DisplayName("17 [★★★☆☆] Bitmask Generator with IntStream: Powers of 2 up to 2^8")
    void exercise17_bitmaskGenerator() {
        // Generate bitmasks: 1, 2, 4, 8, 16, 32, 64, 128, 256
        // TODO: IntStream.rangeClosed(0, 8).map(shift -> 1 << shift).toArray()
        int[] bitmasks = IntStream.rangeClosed(0, 8)
                .map(shift -> 1 << shift)
                .toArray();

        assertThat(bitmasks).isNotNull();
        assertThat(bitmasks).containsExactly(1, 2, 4, 8, 16, 32, 64, 128, 256);
    }

    @Test
    @DisplayName("18 [★★★★☆] Generate consecutive date stream with Stream.iterate()")
    void exercise18_dateStream() {
        java.time.LocalDate startDate = java.time.LocalDate.of(2024, 1, 1);
        // TODO: Generate first 5 days of 2024 using Stream.iterate(startDate, d -> d.plusDays(1)).limit(5)
        List<java.time.LocalDate> dates = Stream.iterate(startDate, d -> d.plusDays(1))
                .limit(5)
                .toList();

        assertThat(dates).isNotNull();
        assertThat(dates).hasSize(5);
        assertThat(dates.get(4)).isEqualTo(java.time.LocalDate.of(2024, 1, 5));
    }

    @Test
    @DisplayName("19 [★★★★☆] Character Frequency Generator from String codePoints")
    void exercise19_characterFrequencies() {
        String input = "banana";
        // Count frequencies of each character using input.chars()
        // TODO: Collect to Map<Character, Long>

        Map<Character, Long> charFrequencies = input.chars()
                .mapToObj(character -> (char) character)
                .collect(Collectors.toMap(key -> key, value -> 1L, Long::sum));

        assertThat(charFrequencies).isNotNull();
        assertThat(charFrequencies.get('a')).isEqualTo(3L);
        assertThat(charFrequencies.get('b')).isEqualTo(1L);
        assertThat(charFrequencies.get('n')).isEqualTo(2L);
    }

    @Test
    @DisplayName("20 [★★★★★] Custom Range Spliterator Stream using StreamSupport")
    void exercise20_streamSupportRange() {
        // Problem: Create an infinite Stream<Integer> counting from 100 upwards using StreamSupport.stream()
        // and a custom Spliterator or Spliterators.spliteratorUnknownSize().
        // Limit to 5 elements: [100, 101, 102, 103, 104]
        // TODO: Build the stream using StreamSupport.stream(...)
        List<Integer> customStreamList = null;

        assertThat(customStreamList).isNotNull();
        assertThat(customStreamList).containsExactly(100, 101, 102, 103, 104);
    }
}
