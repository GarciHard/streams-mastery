package com.streammastery.exercises;

import com.streammastery.model.Employee;
import com.streammastery.model.Order;
import com.streammastery.model.Product;
import com.streammastery.model.Transaction;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * EXERCISE 06: REDUCTION & AGGREGATION (20 EXERCISES)
 * ====================================================================================================
 */
public class Exercise06_ReductionAndAggregation {

    @Test
    @DisplayName("01 [★☆☆☆☆] Sum all employee ages using reduce")
    void exercise01_sumAges() {
        // TODO: mapToInt(Employee::getAge).reduce(0, Integer::sum)
        int totalAge = 0;

        assertThat(totalAge).isGreaterThan(900);
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Find the maximum product price using max()")
    void exercise02_maxPrice() {
        // TODO: Find max Product by price
        Optional<Product> maxProduct = Optional.empty();

        assertThat(maxProduct).isPresent();
        assertThat(maxProduct.get().getPrice()).isEqualTo(2499.99);
    }

    @Test
    @DisplayName("03 [★☆☆☆☆] Count employees in Engineering department")
    void exercise03_countEngineers() {
        // TODO: Count employees with department == "Engineering"
        long count = 0;

        assertThat(count).isEqualTo(20);
    }

    @Test
    @DisplayName("04 [★★☆☆☆] Calculate average salary of all employees using primitive stream")
    void exercise04_averageSalary() {
        // TODO: mapToDouble(Employee::getSalary).average().orElse(0.0)
        double avgSalary = 0.0;

        assertThat(avgSalary).isBetween(190_000.0, 210_000.0);
    }

    @Test
    @DisplayName("05 [★★☆☆☆] Find the employee with the minimum years of experience")
    void exercise05_minExperienceEmployee() {
        // TODO: Find min by yearsOfExperience
        Optional<Employee> junior = Optional.empty();

        assertThat(junior).isPresent();
        assertThat(junior.get().getYearsOfExperience()).isEqualTo(1);
    }

    @Test
    @DisplayName("06 [★★☆☆☆] Reduce a list of strings into a single comma-separated string")
    void exercise06_csvReduce() {
        List<String> langs = List.of("Java", "Go", "Rust", "Python");
        // TODO: Use reduce with binary operator to create "Java, Go, Rust, Python"
        String csv = null;

        assertThat(csv).isNotNull();
        assertThat(csv).isEqualTo("Java, Go, Rust, Python");
    }

    @Test
    @DisplayName("07 [★★★☆☆] Use DoubleSummaryStatistics on employee salaries")
    void exercise07_salaryStatistics() {
        // TODO: Extract summaryStatistics from mapToDouble(Employee::getSalary)
        DoubleSummaryStatistics stats = null;

        assertThat(stats).isNotNull();
        assertThat(stats.getCount()).isEqualTo(30);
        assertThat(stats.getMin()).isEqualTo(110000.0);
        assertThat(stats.getMax()).isEqualTo(340000.0);
    }

    @Test
    @DisplayName("08 [★★★☆☆] Calculate total revenue from DELIVERED orders")
    void exercise08_deliveredOrdersRevenue() {
        // TODO: Filter DELIVERED orders, map to order.getTotalPrice(), sum
        double totalDeliveredRevenue = 0.0;

        assertThat(totalDeliveredRevenue).isGreaterThan(5000.0);
    }

    @Test
    @DisplayName("09 [★★★☆☆] Find the longest employee last name using reduce()")
    void exercise09_longestLastNameReduce() {
        // TODO: Use reduce((name1, name2) -> name1.length() >= name2.length() ? name1 : name2)
        Optional<String> longestLastName = Optional.empty();

        assertThat(longestLastName).isPresent();
        assertThat(longestLastName.get()).isEqualTo("Rodriguez");
    }

    @Test
    @DisplayName("10 [★★★★☆] Production Safe: Sum all USD DEBIT transactions using BigDecimal")
    void exercise10_bigDecimalDebitSum() {
        // Filter: currency == "USD" && type == DEBIT
        // Map: BigDecimal.valueOf(amount)
        // Reduce: BigDecimal.ZERO, BigDecimal::add
        // TODO: Implement BigDecimal reduction
        BigDecimal totalDebit = null;

        assertThat(totalDebit).isNotNull();
        assertThat(totalDebit.doubleValue()).isGreaterThan(7000.0);
    }

    @Test
    @DisplayName("11 [★★★★☆] 3-argument reduce: Accumulate department headcounts into a Map")
    void exercise11_threeArgReduceHeadcount() {
        // TODO: Use 3-argument reduce(new HashMap<String, Integer>(), accumulator, combiner)
        Map<String, Integer> deptCounts = null;

        assertThat(deptCounts).isNotNull();
        assertThat(deptCounts.get("Engineering")).isEqualTo(20);
        assertThat(deptCounts.get("Design")).isEqualTo(3);
    }

    @Test
    @DisplayName("12 [★★★★★] Calculate weighted average salary: weighted by years of experience")
    void exercise12_weightedAverageSalary() {
        // Formula: Sum(salary * experience) / Sum(experience)
        // TODO: Compute both values via stream pipelines and return the weighted average
        double weightedAvg = 0.0;

        assertThat(weightedAvg).isGreaterThan(200_000.0);
    }

    @Test
    @DisplayName("13 [★☆☆☆☆] Count products in Electronics category")
    void exercise13_countElectronics() {
        // TODO: Filter category == "Electronics", count()
        long count = 0;

        assertThat(count).isEqualTo(10);
    }

    @Test
    @DisplayName("14 [★★☆☆☆] Find product with lowest stock")
    void exercise14_minStockProduct() {
        // TODO: Min by Product::getStock
        Optional<Product> minStock = Optional.empty();

        assertThat(minStock).isPresent();
        assertThat(minStock.get().getStock()).isEqualTo(30); // ThinkPad X1 Carbon
    }

    @Test
    @DisplayName("15 [★★☆☆☆] Reduce integers by multiplication (Factorial of 5)")
    void exercise15_multiplyReduce() {
        List<Integer> factors = List.of(1, 2, 3, 4, 5);
        // TODO: reduce(1, (a, b) -> a * b)
        int product = 0;

        assertThat(product).isEqualTo(120);
    }

    @Test
    @DisplayName("16 [★★★☆☆] Reduce Boolean flags: allMatch logic using reduce")
    void exercise16_booleanAndReduce() {
        List<Boolean> checks = List.of(true, true, true, false, true);
        // TODO: reduce(true, (a, b) -> a && b)
        boolean allPassed = true;

        assertThat(allPassed).isFalse();
    }

    @Test
    @DisplayName("17 [★★★☆☆] Find Net Financial Balance across EUR transactions using BigDecimal")
    void exercise17_eurNetBalance() {
        // CREDIT adds, DEBIT subtracts
        // TODO: Filter EUR transactions, map to BigDecimal (credit positive, debit negative), sum
        BigDecimal eurNet = null;

        assertThat(eurNet).isNotNull();
        assertThat(eurNet.doubleValue()).isGreaterThan(6000.0);
    }

    @Test
    @DisplayName("18 [★★★★☆] Custom 3-arg reduce: Concatenate names into a custom StringBuilder")
    void exercise18_stringBuilderReduce() {
        List<String> names = List.of("Alpha", "Beta", "Gamma");
        // TODO: reduce(new StringBuilder(), (sb, s) -> sb.append(s).append(";"), StringBuilder::append)
        StringBuilder result = null;

        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo("Alpha;Beta;Gamma;");
    }

    @Test
    @DisplayName("19 [★★★★☆] IntSummaryStatistics of team sizes")
    void exercise19_teamSizeStats() {
        List<List<String>> teams = DataFactory.getTeams();
        // TODO: mapToInt(List::size).summaryStatistics()
        IntSummaryStatistics stats = null;

        assertThat(stats).isNotNull();
        assertThat(stats.getCount()).isEqualTo(5);
        assertThat(stats.getMin()).isEqualTo(1); // Jack
        assertThat(stats.getMax()).isEqualTo(5); // Karen, Leo, Mia, Noah, Olivia
        assertThat(stats.getSum()).isEqualTo(15);
    }

    @Test
    @DisplayName("20 [★★★★★] Parallel-Safe Matrix Dot Product using reduce")
    void exercise20_dotProduct() {
        int[] vectorA = {1, 2, 3, 4, 5};
        int[] vectorB = {2, 0, 1, 3, -1};
        // Dot product: 1*2 + 2*0 + 3*1 + 4*3 + 5*(-1) = 2 + 0 + 3 + 12 - 5 = 12
        // TODO: Use IntStream.range(0, 5).map(i -> vectorA[i] * vectorB[i]).sum()
        int dotProduct = 0;

        assertThat(dotProduct).isEqualTo(12);
    }
}
