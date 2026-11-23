package com.streammastery.exercises;

import com.streammastery.model.Employee;
import com.streammastery.model.Order;
import com.streammastery.model.OrderItem;
import com.streammastery.model.Product;
import com.streammastery.model.Transaction;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

/**
 * ====================================================================================================
 * EXERCISE 16: STREAM GATHERERS & MODERN JAVA 21-25 STREAM ARCHITECTURE (20 EXERCISES)
 * ====================================================================================================
 *
 * Welcome to Lesson 16 Exercises.
 * Every exercise has an incremental difficulty rating from [★☆☆☆☆] (Beginner) to [★★★★★] (MAANG Staff).
 *
 * Concepts tested:
 * - Built-in Gatherers: windowFixed, windowSliding, fold, scan, mapConcurrent
 * - Custom Gatherers: state encapsulation, Integrator.of vs ofGreedy, short-circuiting, finisher
 * - Gatherer composition with andThen()
 * - Java 21 Sequenced Collections (collection.reversed().stream())
 * - High-throughput mapMulti and mapMultiToInt
 * - Modern pattern matching and record deconstruction inside stream pipelines
 */
public class Exercise16_StreamGatherersAndModernFeatures {

    public sealed interface StreamEvent permits Transfer, Alert {}
    public record Transfer(String id, double amount) implements StreamEvent {}
    public record Alert(String reason) implements StreamEvent {}

    @Test
    @DisplayName("01 [★☆☆☆☆] windowFixed: Partition employee IDs into batches of 5")
    void exercise01_windowFixedBasic() {
        // Problem: Partition the 30 employees from DataFactory into fixed batches of 5.
        // Return a List of batches, where each batch is a List<Integer> of employee IDs.
        // TODO: Replace null with your stream pipeline using Gatherers.windowFixed(5)
        List<List<Integer>> batches = null;

        assertThat(batches).isNotNull();
        assertThat(batches).hasSize(6); // 30 / 5 = 6 batches
        assertThat(batches.get(0)).containsExactly(1, 2, 3, 4, 5);
        assertThat(batches.get(5)).containsExactly(26, 27, 28, 29, 30);
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] windowSliding: Pairwise adjacent differences")
    void exercise02_windowSlidingPairs() {
        List<Double> stockPrices = List.of(100.0, 105.0, 103.0, 110.0, 108.0);

        // Problem: Using Gatherers.windowSliding(2), calculate the price difference (p2 - p1) between adjacent days.
        // Return List<Double> of differences: [105-100, 103-105, 110-103, 108-110]
        // TODO: Replace null with your pipeline
        List<Double> differences = null;

        assertThat(differences).isNotNull();
        assertThat(differences).containsExactly(5.0, -2.0, 7.0, -2.0);
    }

    @Test
    @DisplayName("03 [★☆☆☆☆] scan: Cumulative payroll running total")
    void exercise03_scanRunningTotal() {
        List<Double> bonuses = List.of(5000.0, 3000.0, 7000.0, 2000.0);

        // Problem: Using Gatherers.scan(), compute running cumulative sum of bonuses starting from 0.0.
        // TODO: Replace null with your pipeline
        List<Double> runningSum = null;

        assertThat(runningSum).isNotNull();
        assertThat(runningSum).containsExactly(5000.0, 8000.0, 15000.0, 17000.0);
    }

    @Test
    @DisplayName("04 [★★☆☆☆] fold: Intermediate reduction of department names")
    void exercise04_foldDepartmentString() {
        List<String> depts = List.of("Engineering", "Marketing", "Finance", "HR");

        // Problem: Using Gatherers.fold(), reduce the department list into a single comma-separated String
        // with "DEPTS: " prefix, then use map to convert to uppercase.
        // Result should be a List<String> with exactly 1 element: "DEPTS: ENGINEERING, MARKETING, FINANCE, HR"
        // TODO: Replace null with your pipeline
        List<String> result = null;

        assertThat(result).isNotNull();
        assertThat(result).containsExactly("DEPTS: ENGINEERING, MARKETING, FINANCE, HR");
    }

    @Test
    @DisplayName("05 [★★☆☆☆] mapConcurrent: Bounded concurrent uppercase transformation")
    void exercise05_mapConcurrentUppercase() {
        List<String> services = List.of("auth", "billing", "notification", "inventory");

        // Problem: Use Gatherers.mapConcurrent(2, String::toUpperCase) to transform service names concurrently
        // while preserving encounter order.
        // TODO: Replace null with your pipeline
        List<String> upper = null;

        assertThat(upper).isNotNull();
        assertThat(upper).containsExactly("AUTH", "BILLING", "NOTIFICATION", "INVENTORY");
    }

    @Test
    @DisplayName("06 [★★☆☆☆] windowSliding: 3-period moving average of product prices")
    void exercise06_movingAveragePrices() {
        List<Product> products = DataFactory.getProducts().subList(0, 5);

        // Problem: Compute 3-period moving average of product prices using Gatherers.windowSliding(3).
        // Each element in the result should be the average price of the 3 products in the window.
        // TODO: Replace null with your pipeline
        List<Double> movingAverages = null;

        assertThat(movingAverages).isNotNull();
        assertThat(movingAverages).hasSize(3); // 5 elements, window 3 -> 3 windows
        double firstAvg = (products.get(0).getPrice() + products.get(1).getPrice() + products.get(2).getPrice()) / 3.0;
        assertThat(movingAverages.get(0)).isCloseTo(firstAvg, offset(0.001));
    }

    @Test
    @DisplayName("07 [★★☆☆☆] windowFixed: Handle leftover items in trailing batch")
    void exercise07_windowFixedLeftover() {
        List<Integer> ids = List.of(1, 2, 3, 4, 5, 6, 7);

        // Problem: Batch numbers into chunks of 3. The last chunk should contain leftover [7].
        // Compute the sum of each batch!
        // Output should be List<Integer>: [1+2+3, 4+5+6, 7] -> [6, 15, 7]
        // TODO: Replace null with your pipeline
        List<Integer> batchSums = null;

        assertThat(batchSums).isNotNull();
        assertThat(batchSums).containsExactly(6, 15, 7);
    }

    @Test
    @DisplayName("08 [★★★☆☆] scan: Running maximum salary seen so far")
    void exercise08_scanRunningMax() {
        List<Double> salaries = List.of(90_000.0, 120_000.0, 110_000.0, 150_000.0, 140_000.0, 200_000.0);

        // Problem: Using Gatherers.scan(), track the all-time peak salary encountered up to that point.
        // Expected: [90000.0, 120000.0, 120000.0, 150000.0, 150000.0, 200000.0]
        // TODO: Replace null with your pipeline
        List<Double> runningMax = null;

        assertThat(runningMax).isNotNull();
        assertThat(runningMax).containsExactly(90_000.0, 120_000.0, 120_000.0, 150_000.0, 150_000.0, 200_000.0);
    }

    @Test
    @DisplayName("09 [★★★☆☆] Custom Gatherer: Deduplicate consecutive duplicate elements")
    void exercise09_customGathererDedupConsecutive() {
        List<String> rawStream = List.of("A", "A", "B", "C", "C", "C", "A", "A", "D");

        // Problem: Implement a custom Gatherer via Gatherer.ofSequential() that removes consecutive duplicate elements.
        // Non-consecutive duplicates should be preserved (e.g. the second "A" group should still emit "A").
        // Expected output: ["A", "B", "C", "A", "D"]
        // TODO: Implement dedupConsecutive gatherer
        Gatherer<String, ?, String> dedupConsecutive = null;

        assertThat(dedupConsecutive).isNotNull();
        List<String> result = rawStream.stream().gather(dedupConsecutive).toList();
        assertThat(result).containsExactly("A", "B", "C", "A", "D");
    }

    @Test
    @DisplayName("10 [★★★☆☆] Custom Gatherer: distinctBy(Function<T, K> keyExtractor)")
    void exercise10_customGathererDistinctBy() {
        List<Employee> employees = DataFactory.getEmployees();

        // Problem: Implement a custom Gatherer distinctBy that maintains a Set of seen keys
        // and only passes through the FIRST element encountered for each distinct key.
        // Extract distinct employees by department.
        // TODO: Implement distinctBy Department gatherer
        Gatherer<Employee, ?, Employee> distinctByDept = null;

        assertThat(distinctByDept).isNotNull();
        List<Employee> firstPerDept = employees.stream().gather(distinctByDept).toList();
        List<String> deptNames = firstPerDept.stream().map(Employee::getDepartment).toList();
        assertThat(deptNames).doesNotHaveDuplicates();
        assertThat(deptNames).contains("Engineering", "Marketing");
    }

    @Test
    @DisplayName("11 [★★★☆☆] Custom Gatherer with Finisher: Take last N elements (Sliding Tail)")
    void exercise11_customGathererTailN() {
        List<Integer> numbers = List.of(10, 20, 30, 40, 50, 60, 70);
        int N = 3;

        // Problem: Create a custom Gatherer that keeps a circular buffer/Deque of size N
        // and emits all N buffered elements ONLY when upstream finishes (in the finisher!).
        // Expected: [50, 60, 70]
        // TODO: Implement takeLastN gatherer
        Gatherer<Integer, ?, Integer> takeLastN = null;

        assertThat(takeLastN).isNotNull();
        List<Integer> tail = numbers.stream().gather(takeLastN).toList();
        assertThat(tail).containsExactly(50, 60, 70);
    }

    @Test
    @DisplayName("12 [★★★☆☆] Custom Gatherer Short-Circuiting: takeWhileSumAtMost")
    void exercise12_customGathererShortCircuit() {
        List<Integer> values = List.of(10, 20, 15, 30, 50, 5);
        int maxAllowedSum = 50;

        // Problem: Implement a custom Gatherer using Gatherer.Integrator.of(...) that accumulates sum.
        // As long as currentSum + element <= maxAllowedSum, push the element and return true.
        // When it would exceed maxAllowedSum, return FALSE to short-circuit!
        // 10 + 20 + 15 = 45 <= 50. Next is 30 -> 45 + 30 = 75 > 50 -> STOP!
        // Expected: [10, 20, 15]
        // TODO: Implement takeWhileSumAtMost gatherer
        Gatherer<Integer, ?, Integer> takeWhileSumAtMost = null;

        assertThat(takeWhileSumAtMost).isNotNull();
        List<Integer> result = values.stream().gather(takeWhileSumAtMost).toList();
        assertThat(result).containsExactly(10, 20, 15);
    }

    @Test
    @DisplayName("13 [★★★★☆] Gatherer Composition: windowFixed andThen scan")
    void exercise13_gathererComposition() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6);

        // Problem:
        // 1. Chunk into fixed batches of 2: [1,2], [3,4], [5,6]
        // 2. Compose with scan to compute running cumulative sum of batch sums!
        // Batch 1 sum = 3 -> cumulative 3
        // Batch 2 sum = 7 -> cumulative 10
        // Batch 3 sum = 11 -> cumulative 21
        // Expected: [3, 10, 21]
        // TODO: Compose gatherers
        Gatherer<Integer, ?, Integer> composed = null;

        assertThat(composed).isNotNull();
        List<Integer> result = numbers.stream().gather(composed).toList();
        assertThat(result).containsExactly(3, 10, 21);
    }

    @Test
    @DisplayName("14 [★★★★☆] Java 21 reversed().stream() with windowSliding")
    void exercise14_reversedStreamSliding() {
        List<String> steps = List.of("Step1", "Step2", "Step3", "Step4");

        // Problem: Reverse the steps list using Java 21 reversed(), stream it,
        // and using Gatherers.windowSliding(2), create pairwise rollback transitions.
        // Output: [ ["Step4", "Step3"], ["Step3", "Step2"], ["Step2", "Step1"] ]
        // TODO: Replace null with your pipeline
        List<List<String>> rollbackPairs = null;

        assertThat(rollbackPairs).isNotNull();
        assertThat(rollbackPairs).containsExactly(
                List.of("Step4", "Step3"),
                List.of("Step3", "Step2"),
                List.of("Step2", "Step1")
        );
    }

    @Test
    @DisplayName("15 [★★★★☆] mapMulti: Zero-allocation product name flattening")
    void exercise15_mapMultiOrders() {
        List<Order> orders = DataFactory.getOrders();

        // Problem: Use mapMulti to extract all OrderItem product names whose quantity >= 2.
        // Ensure no duplicate product names are in the final sorted list.
        // TODO: Replace null with your stream pipeline using <String>mapMulti
        List<String> multiQuantityProducts = null;

        assertThat(multiQuantityProducts).isNotNull();
        assertThat(multiQuantityProducts).isNotEmpty();
        assertThat(multiQuantityProducts).isSorted();
    }

    @Test
    @DisplayName("16 [★★★★☆] mapMultiToInt: Parse and filter matrix rows directly into sum")
    void exercise16_mapMultiToIntSum() {
        List<String> matrixRows = List.of(
                "10;20;30",
                "invalid;-5;40",
                "50;60"
        );

        // Problem: Use mapMultiToInt to parse semicolon-separated integer strings.
        // If a token is not a valid integer or is <= 0, ignore it.
        // Sum all strictly positive valid integers.
        // Valid positives: 10 + 20 + 30 + 40 + 50 + 60 = 210
        // TODO: Replace 0 with your stream pipeline using mapMultiToInt
        int totalSum = 0;

        assertThat(totalSum).isEqualTo(210);
    }

    @Test
    @DisplayName("17 [★★★★★] Custom Gatherer: Run-Length Encoding (RLE)")
    void exercise17_customGathererRunLengthEncoding() {
        List<String> input = List.of("A", "A", "A", "B", "B", "C", "A", "A");

        record Run(String element, int count) {}

        // Problem: Implement a custom Gatherer that compresses adjacent identical elements
        // into Run records: Run("A", 3), Run("B", 2), Run("C", 1), Run("A", 2).
        // Hint: Maintain current element and count in state. When element changes, push old Run.
        // Don't forget to push the final Run in the finisher!
        // TODO: Implement rleGatherer
        Gatherer<String, ?, Run> rleGatherer = null;

        assertThat(rleGatherer).isNotNull();
        List<Run> runs = input.stream().gather(rleGatherer).toList();
        assertThat(runs).containsExactly(
                new Run("A", 3),
                new Run("B", 2),
                new Run("C", 1),
                new Run("A", 2)
        );
    }

    @Test
    @DisplayName("18 [★★★★★] Custom Greedy Gatherer: Exponential Moving Average (EMA)")
    void exercise18_customGathererEMA() {
        List<Double> values = List.of(100.0, 110.0, 105.0, 120.0);
        double alpha = 0.5; // EMA_t = alpha * value_t + (1 - alpha) * EMA_{t-1}
        // t=0: 100.0
        // t=1: 0.5 * 110.0 + 0.5 * 100.0 = 105.0
        // t=2: 0.5 * 105.0 + 0.5 * 105.0 = 105.0
        // t=3: 0.5 * 120.0 + 0.5 * 105.0 = 112.5

        // Problem: Implement a custom Gatherer that computes the EMA series using Integrator.ofGreedy.
        // Expected: [100.0, 105.0, 105.0, 112.5]
        // TODO: Implement emaGatherer
        Gatherer<Double, ?, Double> emaGatherer = null;

        assertThat(emaGatherer).isNotNull();
        List<Double> ema = values.stream().gather(emaGatherer).toList();
        assertThat(ema).containsExactly(100.0, 105.0, 105.0, 112.5);
    }

    @Test
    @DisplayName("19 [★★★★★] Pattern Matching with Gatherers: High-Value Financial Event Stream")
    void exercise19_patternMatchingWithGatherers() {
        List<StreamEvent> events = List.of(
                new Transfer("T1", 500.0),
                new Alert("System test"),
                new Transfer("T2", 1500.0),
                new Transfer("T3", 2500.0),
                new Alert("Heartbeat"),
                new Transfer("T4", 3000.0)
        );

        // Problem:
        // 1. Filter only Transfer events using pattern matching in filter or mapMulti.
        // 2. Extract their amounts.
        // 3. Using Gatherers.windowFixed(2), chunk amounts into pairs.
        // 4. Compute the sum of each pair.
        // Amounts: [500.0, 1500.0, 2500.0, 3000.0]
        // Pairs: [500, 1500] -> sum 2000.0; [2500, 3000] -> sum 5500.0
        // Expected: [2000.0, 5500.0]
        // TODO: Replace null with your pipeline
        List<Double> pairSums = null;

        assertThat(pairSums).isNotNull();
        assertThat(pairSums).containsExactly(2000.0, 5500.0);
    }

    @Test
    @DisplayName("20 [★★★★★] MAANG Staff Gauntlet: Real-Time Rapid Fraud Detection Pipeline")
    void exercise20_fraudDetectionPipeline() {
        // High-velocity stream of financial transactions:
        List<Transaction> stream = List.of(
                new Transaction(1, "ACC101", Transaction.Type.DEBIT, 50.0, "USD", "FOOD", LocalDate.of(2026, 1, 1), "Store"),
                new Transaction(2, "ACC101", Transaction.Type.DEBIT, 12000.0, "USD", "TRANSFER", LocalDate.of(2026, 1, 1), "Wire"),
                new Transaction(3, "ACC101", Transaction.Type.DEBIT, 15000.0, "USD", "TRANSFER", LocalDate.of(2026, 1, 1), "Wire"),
                new Transaction(4, "ACC101", Transaction.Type.DEBIT, 80.0, "USD", "FOOD", LocalDate.of(2026, 1, 2), "Store"),
                new Transaction(5, "ACC102", Transaction.Type.DEBIT, 500.0, "USD", "SHOPPING", LocalDate.of(2026, 1, 2), "Mall")
        );

        record FraudAlert(int firstTxId, int secondTxId, double totalAmount) {}

        // Problem: Build a real-time sliding window fraud detection pipeline:
        // 1. Using Gatherers.windowSliding(2), inspect consecutive transaction pairs.
        // 2. If both transactions belong to the same accountId AND both have amount >= 10,000.0,
        //    create a FraudAlert with (tx1.id, tx2.id, tx1.amount + tx2.amount).
        // Pair (Tx2, Tx3): same account "ACC101", amounts 12k and 15k >= 10k -> FraudAlert(2, 3, 27000.0)
        // TODO: Replace null with your pipeline
        List<FraudAlert> alerts = null;

        assertThat(alerts).isNotNull();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0)).isEqualTo(new FraudAlert(2, 3, 27000.0));
    }
}
