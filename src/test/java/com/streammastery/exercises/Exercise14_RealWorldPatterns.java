package com.streammastery.exercises;

import com.streammastery.model.*;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * EXERCISE 14: REAL-WORLD PRODUCTION PATTERNS & IDIOMS (20 EXERCISES)
 * ====================================================================================================
 */
public class Exercise14_RealWorldPatterns {

    @Test
    @DisplayName("01 [★☆☆☆☆] Repository Query: Senior Engineers in San Francisco sorted by salary desc")
    void exercise01_repositoryQuery() {
        // TODO: Filter department Engineering, city San Francisco, title contains Senior, sort salary desc
        List<Employee> results = null;

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2); // Alice ($185k), Karen ($190k)
        assertThat(results.get(0).getSalary()).isGreaterThanOrEqualTo(results.get(1).getSalary());
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Validate employee data: Find any employee with null or empty email")
    void exercise02_validateEmails() {
        // TODO: Filter e.getEmail() == null || e.getEmail().isBlank()
        List<Employee> invalidEmployees = null;

        assertThat(invalidEmployees).isNotNull();
        assertThat(invalidEmployees).isEmpty(); // Clean mock data
    }

    @Test
    @DisplayName("03 [★★☆☆☆] Department summary report as formatted multi-line String")
    void exercise03_departmentReportString() {
        // Format each dept: "Department: [DEPT], Count: [N]" joined by newline "\n"
        // TODO: Group by dept, count, map to string, sort, join
        String report = null;

        assertThat(report).isNotNull();
        assertThat(report).contains("Department: Engineering, Count: 20");
    }

    @Test
    @DisplayName("04 [★★☆☆☆] Search products by multiple keywords (case-insensitive ANY match)")
    void exercise04_searchKeywords() {
        List<String> keywords = List.of("pro", "air", "max");
        // TODO: Filter products whose name contains ANY of the keywords (case-insensitive)
        List<Product> matches = null;

        assertThat(matches).isNotNull();
        assertThat(matches).isNotEmpty();
        assertThat(matches).extracting(Product::getName).contains("MacBook Pro 16\"", "AirPods Pro 2", "Air Max 90");
    }

    @Test
    @DisplayName("05 [★★☆☆☆] Fast Lookup Table: Map<Department, List<String>> of employee full names")
    void exercise05_lookupTable() {
        // TODO: Group by department, downstream mapping to full name
        Map<String, List<String>> lookup = null;

        assertThat(lookup).isNotNull();
        assertThat(lookup.get("Data Science")).contains("Jack Anderson", "Quinn Martinez", "Yara Allen");
    }

    @Test
    @DisplayName("06 [★★★☆☆] Batch Processing: Partition 30 employees into fixed chunks of 7")
    void exercise06_batching() {
        List<Employee> all = DataFactory.getEmployees();
        int batchSize = 7;
        int totalBatches = (int) Math.ceil((double) all.size() / batchSize);

        // TODO: IntStream.range(0, totalBatches).mapToObj(i -> all.subList(i * batchSize, Math.min((i + 1) * batchSize, all.size()))).toList()
        List<List<Employee>> batches = null;

        assertThat(batches).isNotNull();
        assertThat(batches).hasSize(5);
        assertThat(batches.get(0)).hasSize(7);
        assertThat(batches.get(4)).hasSize(2);
    }

    @Test
    @DisplayName("07 [★★★☆☆] Java 12 Collectors.teeing(): Min and Max salary in a single pass")
    void exercise07_teeingMinMax() {
        // TODO: Use Collectors.teeing to extract min and max salary
        Map.Entry<Double, Double> extremes = null;

        assertThat(extremes).isNotNull();
        assertThat(extremes.getKey()).isEqualTo(110000.0);
        assertThat(extremes.getValue()).isEqualTo(340000.0);
    }

    @Test
    @DisplayName("08 [★★★☆☆] Corporate Email Directory: Map<Department, Set<String>>")
    void exercise08_emailDirectory() {
        // TODO: Group by department, downstream mapping to email set
        Map<String, Set<String>> directory = null;

        assertThat(directory).isNotNull();
        assertThat(directory.get("Design")).hasSize(3);
    }

    @Test
    @DisplayName("09 [★★★★☆] Sliding Window Moving Average over last 3 amounts")
    void exercise09_slidingWindow() {
        List<Double> values = List.of(10.0, 20.0, 30.0, 40.0, 50.0);
        int window = 3;
        // Output moving averages for windows: [10,20,30]->20.0, [20,30,40]->30.0, [30,40,50]->40.0
        // TODO: IntStream.rangeClosed(0, values.size() - window).mapToDouble(...)
        List<Double> movingAverages = null;

        assertThat(movingAverages).isNotNull();
        assertThat(movingAverages).containsExactly(20.0, 30.0, 40.0);
    }

    @Test
    @DisplayName("10 [★★★★☆] Validation Pipeline: Collect invalid transactions")
    void exercise10_invalidTransactions() {
        // Rules: Amount must be > 0.0, Currency must be "USD" or "EUR"
        // TODO: Filter transactions violating either rule
        List<Transaction> invalidTransactions = null;

        assertThat(invalidTransactions).isNotNull();
        assertThat(invalidTransactions).isEmpty(); // Clean mock data
    }

    @Test
    @DisplayName("11 [★★★★★] Complete Executive Dashboard Snapshot Record")
    void exercise11_executiveDashboard() {
        record Dashboard(long totalEmployees, double avgSalary, String topEarnerName, double topSalary) {}

        // TODO: Build dashboard record using stream operations
        Dashboard dashboard = null;

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.totalEmployees()).isEqualTo(30L);
        assertThat(dashboard.topEarnerName()).isEqualTo("Amy King");
        assertThat(dashboard.topSalary()).isEqualTo(340000.0);
    }

    @Test
    @DisplayName("12 [★★★★★] Stream ETL Pipeline: Raw Transactions to Account Monthly Balance")
    void exercise12_accountMonthlyBalances() {
        // Group by accountId -> (Month of date -> Net Balance)
        // TODO: Build nested grouping
        Map<String, Map<Integer, Double>> accountMonthlyNet = null;

        assertThat(accountMonthlyNet).isNotNull();
        assertThat(accountMonthlyNet.get("ACC-001")).containsKey(1); // January
        assertThat(accountMonthlyNet.get("ACC-001").get(1)).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("13 [★☆☆☆☆] Null-safe field filter: Collect employees with non-null city")
    void exercise13_nonNullCity() {
        // TODO: Filter e.getCity() != null, count()
        long count = 0;

        assertThat(count).isEqualTo(30);
    }

    @Test
    @DisplayName("14 [★★☆☆☆] Safe Map extraction with defaultValue")
    void exercise14_safeMapExtraction() {
        Map<String, String> env = Map.of("ENV", "PROD", "PORT", "8080");
        List<String> keys = List.of("ENV", "DEBUG", "PORT");

        // TODO: Map keys to env value or "DEFAULT" if absent
        List<String> values = null;

        assertThat(values).isNotNull();
        assertThat(values).containsExactly("PROD", "DEFAULT", "8080");
    }

    @Test
    @DisplayName("15 [★★☆☆☆] Builder Pattern: Build Product Catalog Summary")
    void exercise15_catalogSummary() {
        record CatalogSummary(long totalProducts, double totalInventoryValue) {}

        // Inventory value = sum of (price * stock)
        // TODO: Build CatalogSummary
        CatalogSummary summary = null;

        assertThat(summary).isNotNull();
        assertThat(summary.totalProducts()).isEqualTo(30L);
        assertThat(summary.totalInventoryValue()).isGreaterThan(1_000_000.0);
    }

    @Test
    @DisplayName("16 [★★★☆☆] Batch Processing: Process transactions in chunks of 5")
    void exercise16_transactionBatches() {
        List<Transaction> txs = DataFactory.getTransactions();
        int batchSize = 5;
        int totalBatches = (int) Math.ceil((double) txs.size() / batchSize);

        // TODO: IntStream.range(0, totalBatches).mapToObj(i -> txs.subList(i * batchSize, Math.min((i + 1) * batchSize, txs.size()))).toList()
        List<List<Transaction>> batches = null;

        assertThat(batches).isNotNull();
        assertThat(batches).hasSize(6); // 30 / 5 = 6
    }

    @Test
    @DisplayName("17 [★★★☆☆] Teeing: Mean and Standard Deviation in a Single Pass")
    void exercise17_teeingMeanAndStdDev() {
        List<Double> scores = List.of(10.0, 20.0, 30.0, 40.0, 50.0);

        // Mean = 30.0
        // Variance = sum((x - 30)^2) / 5 = (400 + 100 + 0 + 100 + 400) / 5 = 200
        // StdDev = sqrt(200) = 14.142
        // TODO: Compute mean using teeing or stream
        Double mean = null;

        assertThat(mean).isEqualTo(30.0);
    }

    @Test
    @DisplayName("18 [★★★★☆] Immutable State Transformation: Promote Junior Engineers to Engineers with 15% raise")
    void exercise18_promoteJuniors() {
        // Output new List<Employee> where employees with title "Junior Engineer"
        // become "Software Engineer" with salary = salary * 1.15
        // (All other employees unchanged)
        // TODO: Complete transformation
        List<Employee> updatedStaff = null;

        assertThat(updatedStaff).isNotNull();
        Employee carol = updatedStaff.stream().filter(e -> e.getId() == 3).findFirst().orElseThrow();
        assertThat(carol.getTitle()).isEqualTo("Software Engineer");
        assertThat(carol.getSalary()).isEqualTo(120_000.0 * 1.15);
    }

    @Test
    @DisplayName("19 [★★★★☆] Rate Limiter / Sliding Window Log Event Count")
    void exercise19_rateLimiterEventCount() {
        // Event timestamps in epoch seconds:
        List<Long> eventTimestamps = List.of(100L, 102L, 105L, 108L, 120L, 140L, 155L, 158L, 159L);
        long currentTime = 160L;
        long windowSeconds = 60L; // Lookback window: [100, 160]

        // Count events in the last 60 seconds (timestamp >= currentTime - windowSeconds)
        // TODO: Filter and count
        long recentEvents = 0L;

        assertThat(recentEvents).isEqualTo(8L);
    }

    @Test
    @DisplayName("20 [★★★★★] Complex Stream-based In-Memory Analytics Engine")
    void exercise20_analyticsEngine() {
        // Compute total company spend per city across both Payroll (sum of Employee salaries)
        // Output Map<String, Double> city -> total spend
        // TODO: Group employees by city, summing salaries
        Map<String, Double> citySpend = null;

        assertThat(citySpend).isNotNull();
        assertThat(citySpend.get("San Francisco")).isGreaterThan(500_000.0);
        assertThat(citySpend.get("Seattle")).isGreaterThan(1_000_000.0);
    }
}
