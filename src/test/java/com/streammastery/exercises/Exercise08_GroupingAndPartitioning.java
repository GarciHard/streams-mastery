package com.streammastery.exercises;

import com.streammastery.model.*;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * EXERCISE 08: GROUPING & PARTITIONING (20 EXERCISES)
 * ====================================================================================================
 */
public class Exercise08_GroupingAndPartitioning {

    @Test
    @DisplayName("01 [★☆☆☆☆] Group employees by city")
    void exercise01_groupByCity() {
        // TODO: Group employees by city
        Map<String, List<Employee>> byCity = null;

        assertThat(byCity).isNotNull();
        assertThat(byCity.get("Seattle")).hasSize(6);
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Partition employees into Senior (age >= 35) and Non-Senior")
    void exercise02_partitionByAge() {
        // TODO: partitioningBy(e -> e.getAge() >= 35)
        Map<Boolean, List<Employee>> partitioned = null;

        assertThat(partitioned).isNotNull();
        assertThat(partitioned.get(true)).allMatch(e -> e.getAge() >= 35);
        assertThat(partitioned.get(false)).allMatch(e -> e.getAge() < 35);
    }

    @Test
    @DisplayName("03 [★★☆☆☆] Group products by category and count each category")
    void exercise03_countPerCategory() {
        // TODO: groupingBy(Product::getCategory, Collectors.counting())
        Map<String, Long> countMap = null;

        assertThat(countMap).isNotNull();
        assertThat(countMap.get("Electronics")).isEqualTo(10L);
        assertThat(countMap.get("Footwear")).isEqualTo(5L);
    }

    @Test
    @DisplayName("04 [★★☆☆☆] Group employees by department and get average years of experience")
    void exercise04_avgExperienceByDept() {
        // TODO: groupingBy(Employee::getDepartment, Collectors.averagingInt(Employee::getYearsOfExperience))
        Map<String, Double> avgExp = null;

        assertThat(avgExp).isNotNull();
        assertThat(avgExp.get("Engineering")).isGreaterThan(10.0);
    }

    @Test
    @DisplayName("05 [★★☆☆☆] Partition products into expensive (> $100) vs affordable, collect names")
    void exercise05_partitionProductNames() {
        // TODO: partitioningBy(p -> p.getPrice() > 100, Collectors.mapping(Product::getName, Collectors.toList()))
        Map<Boolean, List<String>> partitioned = null;

        assertThat(partitioned).isNotNull();
        assertThat(partitioned.get(true)).contains("MacBook Pro 16\"");
        assertThat(partitioned.get(false)).contains("Atomic Habits");
    }

    @Test
    @DisplayName("06 [★★★☆☆] Group employees by city, then find the highest-paid employee in each city")
    void exercise06_highestPaidPerCity() {
        // TODO: Group by city, collect max Employee per city wrapped in Optional
        Map<String, Optional<Employee>> topEarnerPerCity = null;

        assertThat(topEarnerPerCity).isNotNull();
        assertThat(topEarnerPerCity.get("Seattle").orElseThrow().getSalary()).isEqualTo(310000.0); // Ryan
    }

    @Test
    @DisplayName("07 [★★★☆☆] Multi-level grouping: Department -> Programming Language -> List<Employee>")
    void exercise07_deptThenLanguage() {
        // TODO: Nested groupingBy: Department -> ProgrammingLanguage
        Map<String, Map<String, List<Employee>>> nested = null;

        assertThat(nested).isNotNull();
        assertThat(nested.get("Engineering").get("Java")).hasSize(7);
    }

    @Test
    @DisplayName("08 [★★★☆☆] Group orders by status, calculate total revenue per status")
    void exercise08_revenuePerStatus() {
        // TODO: Group orders by status and sum order total prices
        Map<String, Double> revenueByStatus = null;

        assertThat(revenueByStatus).isNotNull();
        assertThat(revenueByStatus.get("DELIVERED")).isGreaterThan(5000.0);
    }

    @Test
    @DisplayName("09 [★★★★☆] Group transactions by category and calculate net balance (Credit minus Debit)")
    void exercise09_netBalancePerCategory() {
        // TODO: Group by category, compute net sum: sum of (CREDIT ? amount : -amount)
        Map<String, Double> netByCategory = null;

        assertThat(netByCategory).isNotNull();
        assertThat(netByCategory.get("SALARY")).isGreaterThan(30000.0);
        assertThat(netByCategory.get("HOUSING")).isLessThan(0.0); // Negative net
    }

    @Test
    @DisplayName("10 [★★★★☆] Group employees by department using TreeMap, downstream sorted list of names")
    void exercise10_treeMapDeptNames() {
        // TODO: Group by dept using TreeMap::new, downstream mapping to full name, collected to List
        TreeMap<String, List<String>> sortedDeptNames = null;

        assertThat(sortedDeptNames).isNotNull();
        assertThat(sortedDeptNames.firstKey()).isEqualTo("Data Science");
        assertThat(sortedDeptNames.get("Data Science")).contains("Jack Anderson", "Quinn Martinez", "Yara Allen");
    }

    @Test
    @DisplayName("11 [★★★★★] Customer Spending Matrix: Customer -> (Product Category -> Total Amount Spent)")
    void exercise11_customerCategorySpending() {
        // TODO: Nested aggregation: Customer -> (Category -> Sum of item subtotal)
        Map<String, Map<String, Double>> spendingMatrix = null;

        assertThat(spendingMatrix).isNotNull();
        assertThat(spendingMatrix.get("Alice Johnson")).containsKey("Electronics");
        assertThat(spendingMatrix.get("Alice Johnson").get("Electronics")).isGreaterThan(2500.0);
    }

    @Test
    @DisplayName("12 [★★★★★] Executive Department Report: Map<String, String>")
    void exercise12_executiveReport() {
        // TODO: Build executive report mapping department to formatted summary
        Map<String, String> reports = null;

        assertThat(reports).isNotNull();
        assertThat(reports.get("Engineering")).contains("Count: 20");
        assertThat(reports.get("Engineering")).contains("Top: Amy King");
    }

    @Test
    @DisplayName("13 [★☆☆☆☆] Group products by brand")
    void exercise13_groupByBrand() {
        // TODO: Group products by brand
        Map<String, List<Product>> byBrand = null;

        assertThat(byBrand).isNotNull();
        assertThat(byBrand.get("Apple")).hasSize(3);
    }

    @Test
    @DisplayName("14 [★★☆☆☆] Partition transactions into CREDIT vs DEBIT")
    void exercise14_partitionTransactions() {
        // TODO: Partition transactions by t.getType() == Transaction.Type.CREDIT
        Map<Boolean, List<Transaction>> partitioned = null;

        assertThat(partitioned).isNotNull();
        assertThat(partitioned.get(true)).allMatch(t -> t.getType() == Transaction.Type.CREDIT);
    }

    @Test
    @DisplayName("15 [★★☆☆☆] Group products by category and calculate max price in each category")
    void exercise15_maxPricePerCategory() {
        // TODO: groupingBy(Product::getCategory, Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparingDouble(Product::getPrice)), opt -> opt.map(Product::getPrice).orElse(0.0)))
        Map<String, Double> maxPrices = null;

        assertThat(maxPrices).isNotNull();
        assertThat(maxPrices.get("Electronics")).isEqualTo(2499.99); // MacBook
        assertThat(maxPrices.get("Books")).isEqualTo(49.99); // Design Patterns
    }

    @Test
    @DisplayName("16 [★★★☆☆] Group employees by programming language and join first names with comma")
    void exercise16_namesByLanguage() {
        // TODO: groupingBy(Employee::getProgrammingLanguage, Collectors.mapping(Employee::getFirstName, Collectors.joining(", ")))
        Map<String, String> namesByLang = null;

        assertThat(namesByLang).isNotNull();
        assertThat(namesByLang.get("Go")).contains("Grace", "Karen", "Thomas", "Derek");
    }

    @Test
    @DisplayName("17 [★★★☆☆] Partition employees by gender, downstream average salary")
    void exercise17_genderAvgSalary() {
        // TODO: partitioningBy(e -> "Female".equals(e.getGender()), Collectors.averagingDouble(Employee::getSalary))
        Map<Boolean, Double> salaryByGender = null;

        assertThat(salaryByGender).isNotNull();
        assertThat(salaryByGender.get(true)).isGreaterThan(180_000.0);
        assertThat(salaryByGender.get(false)).isGreaterThan(180_000.0);
    }

    @Test
    @DisplayName("18 [★★★★☆] Java 9 filtering downstream: Count of senior engineers (> $200k) per department")
    void exercise18_filteringDownstream() {
        // TODO: groupingBy(Employee::getDepartment, Collectors.filtering(e -> e.getSalary() > 200_000.0, Collectors.counting()))
        Map<String, Long> seniorCountByDept = null;

        assertThat(seniorCountByDept).isNotNull();
        assertThat(seniorCountByDept.get("Engineering")).isEqualTo(9L);
        assertThat(seniorCountByDept.get("Design")).isEqualTo(0L); // Key present with count 0!
    }

    @Test
    @DisplayName("19 [★★★★☆] DoubleSummaryStatistics per Department")
    void exercise19_deptSalaryStats() {
        // TODO: groupingBy(Employee::getDepartment, Collectors.summarizingDouble(Employee::getSalary))
        Map<String, DoubleSummaryStatistics> deptStats = null;

        assertThat(deptStats).isNotNull();
        assertThat(deptStats.get("Engineering").getCount()).isEqualTo(20);
        assertThat(deptStats.get("Engineering").getMax()).isEqualTo(340000.0);
    }

    @Test
    @DisplayName("20 [★★★★★] Complex Pivot Table: Account -> (Currency -> Total Credit Amount)")
    void exercise20_accountCurrencyPivot() {
        // From transactions, filter CREDIT transactions, group by accountId -> (currency -> sum of amounts)
        // TODO: Build the pivot table
        Map<String, Map<String, Double>> pivot = null;

        assertThat(pivot).isNotNull();
        assertThat(pivot.get("ACC-001").get("USD")).isGreaterThan(10000.0);
        assertThat(pivot.get("ACC-003").get("EUR")).isGreaterThan(8000.0);
    }
}
