package com.streammastery.exercises;

import com.streammastery.model.Employee;
import com.streammastery.model.Product;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ====================================================================================================
 * EXERCISE 07: COLLECTING RESULTS (20 EXERCISES)
 * ====================================================================================================
 */
public class Exercise07_CollectingResults {

    @Test
    @DisplayName("01 [★☆☆☆☆] Collect employee names to a List")
    void exercise01_collectToList() {
        // TODO: Map to getFullName, collect to List
        List<String> names = null;

        assertThat(names).isNotNull();
        assertThat(names).hasSize(30);
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Collect unique cities to a Set")
    void exercise02_collectToSet() {
        // TODO: Map to getCity, collect to Set
        Set<String> cities = null;

        assertThat(cities).isNotNull();
        assertThat(cities).contains("San Francisco", "Seattle", "Cupertino", "Menlo Park", "New York", "Los Gatos");
    }

    @Test
    @DisplayName("03 [★☆☆☆☆] Join all product categories separated by ' | '")
    void exercise03_joinCategories() {
        // TODO: Distinct categories, sorted, joined with " | "
        String joined = null;

        assertThat(joined).isNotNull();
        assertThat(joined).isEqualTo("Books | Clothing | Electronics | Footwear | Home");
    }

    @Test
    @DisplayName("04 [★★☆☆☆] Collect department names to a sorted TreeSet")
    void exercise04_collectToTreeSet() {
        // TODO: Collect to TreeSet using Collectors.toCollection(TreeSet::new)
        TreeSet<String> departments = null;

        assertThat(departments).isNotNull();
        assertThat(departments.first()).isEqualTo("Data Science");
        assertThat(departments.last()).isEqualTo("Product");
    }

    @Test
    @DisplayName("05 [★★☆☆☆] Map Employee ID to Employee full name")
    void exercise05_idToNameMap() {
        // TODO: Collectors.toMap(Employee::getId, Employee::getFullName)
        Map<Integer, String> idNameMap = null;

        assertThat(idNameMap).isNotNull();
        assertThat(idNameMap.get(1)).isEqualTo("Alice Johnson");
    }

    @Test
    @DisplayName("06 [★★☆☆☆] Join employee first names formatted with prefix and suffix")
    void exercise06_joiningDelimited() {
        // Format: "<Alice, Bob, Carol>" (first 3 employees)
        // TODO: Stream first 3 employees, map firstName, join with ", ", prefix "<", suffix ">"
        String result = null;

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("<Alice, Bob, Carol>");
    }

    @Test
    @DisplayName("07 [★★★☆☆] toMap with Collision: Department -> Maximum Salary in that dept")
    void exercise07_deptToMaxSalary() {
        // TODO: toMap(Employee::getDepartment, Employee::getSalary, Math::max)
        Map<String, Double> deptMaxSalary = null;

        assertThat(deptMaxSalary).isNotNull();
        assertThat(deptMaxSalary.get("Engineering")).isEqualTo(340000.0);
        assertThat(deptMaxSalary.get("Marketing")).isEqualTo(220000.0);
    }

    @Test
    @DisplayName("08 [★★★☆☆] collectingAndThen: Count total items and return formatted string")
    void exercise08_collectingAndThenCount() {
        // TODO: Collectors.collectingAndThen(Collectors.counting(), count -> "Total Products: " + count)
        String summary = null;

        assertThat(summary).isNotNull();
        assertThat(summary).isEqualTo("Total Products: 30");
    }

    @Test
    @DisplayName("09 [★★★☆☆] Verify Stream.toList() immutability")
    void exercise09_verifyListImmutability() {
        List<String> immutable = DataFactory.getEmployees().stream().map(Employee::getFirstName).toList();

        // TODO: Complete assertion ensuring UnsupportedOperationException on add
        assertThatThrownBy(() -> {
            // YOUR CODE HERE
        }).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("10 [★★★★☆] toMap: Department -> Total Payroll (Sum of salaries)")
    void exercise10_deptTotalPayroll() {
        // TODO: toMap(Employee::getDepartment, Employee::getSalary, Double::sum)
        Map<String, Double> payroll = null;

        assertThat(payroll).isNotNull();
        assertThat(payroll.get("Engineering")).isGreaterThan(4_000_000.0);
    }

    @Test
    @DisplayName("11 [★★★★☆] 3-argument collect: Build a custom StringJoiner")
    void exercise11_threeArgCollect() {
        // TODO: Use collect(Supplier, accumulator, combiner) to join employee cities into a StringJoiner
        StringJoiner joiner = DataFactory.getEmployees().stream()
                .map(Employee::getCity)
                .distinct()
                .sorted()
                .collect(null, null, null);

        assertThat(joiner).isNotNull();
        assertThat(joiner.toString()).contains("Cupertino; Los Gatos; Menlo Park");
    }

    @Test
    @DisplayName("12 [★★★★★] Complex toMap: Map product brand to list of product names sorted by price")
    void exercise12_brandToSortedProducts() {
        // TODO: Collect products into Map<String, List<String>> mapping Brand -> Sorted List of Product Names
        Map<String, List<String>> brandMap = null;

        assertThat(brandMap).isNotNull();
        assertThat(brandMap.get("Apple")).contains("AirPods Pro 2", "iPhone 15 Pro", "MacBook Pro 16\"");
    }

    @Test
    @DisplayName("13 [★☆☆☆☆] Collect to unmodifiable Set")
    void exercise13_toUnmodifiableSet() {
        // TODO: Collect employee programming languages using Collectors.toUnmodifiableSet()
        Set<String> unmodifiableLanguages = null;

        assertThat(unmodifiableLanguages).isNotNull();
        assertThat(unmodifiableLanguages).contains("Java", "Python", "Go");
    }

    @Test
    @DisplayName("14 [★★☆☆☆] Collect to LinkedList")
    void exercise14_collectToLinkedList() {
        // TODO: Collect employee first names to a LinkedList using Collectors.toCollection(LinkedList::new)
        LinkedList<String> list = null;

        assertThat(list).isNotNull();
        assertThat(list.peekFirst()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("15 [★★☆☆☆] toMap with key collision: Keep earliest employee ID for each department")
    void exercise15_deptToFirstId() {
        // TODO: toMap(Employee::getDepartment, Employee::getId, (id1, id2) -> Math.min(id1, id2))
        Map<String, Integer> earliestDeptMember = null;

        assertThat(earliestDeptMember).isNotNull();
        assertThat(earliestDeptMember.get("Engineering")).isEqualTo(1); // Alice
        assertThat(earliestDeptMember.get("Design")).isEqualTo(4);      // David
    }

    @Test
    @DisplayName("16 [★★★☆☆] summarizingDouble as standalone collector on salaries")
    void exercise16_summarizingCollector() {
        // TODO: Collect employees using Collectors.summarizingDouble(Employee::getSalary)
        DoubleSummaryStatistics stats = null;

        assertThat(stats).isNotNull();
        assertThat(stats.getCount()).isEqualTo(30);
        assertThat(stats.getMax()).isEqualTo(340000.0);
    }

    @Test
    @DisplayName("17 [★★★☆☆] filtering collector (Java 9+): Collect high-earning names into Set")
    void exercise17_filteringCollector() {
        // TODO: Collectors.filtering(e -> e.getSalary() > 250_000.0, Collectors.mapping(Employee::getFullName, Collectors.toSet()))
        Set<String> highEarners = DataFactory.getEmployees().stream()
                .collect(null);

        assertThat(highEarners).isNotNull();
        assertThat(highEarners).contains("Eve Davis", "Ryan Robinson", "Uma Lewis", "Amy King");
    }

    @Test
    @DisplayName("18 [★★★★☆] collectingAndThen: Calculate average salary and format as string")
    void exercise18_collectingAndThenAvg() {
        // TODO: Collectors.collectingAndThen(Collectors.averagingDouble(Employee::getSalary), avg -> String.format("Avg: $%,.2f", avg))
        String formattedAvg = null;

        assertThat(formattedAvg).isNotNull();
        assertThat(formattedAvg).startsWith("Avg: $");
    }

    @Test
    @DisplayName("19 [★★★★☆] Map employee full name to Employee object with Function.identity()")
    void exercise19_nameToEmployeeMap() {
        // TODO: Collectors.toMap(Employee::getFullName, java.util.function.Function.identity())
        Map<String, Employee> employeeIndex = null;

        assertThat(employeeIndex).isNotNull();
        assertThat(employeeIndex.get("Alice Johnson").getDepartment()).isEqualTo("Engineering");
    }

    @Test
    @DisplayName("20 [★★★★★] Complex toConcurrentMap for thread-safe caching")
    void exercise20_concurrentMap() {
        // TODO: Collect employees to ConcurrentMap<Integer, String> mapping ID -> Email
        java.util.concurrent.ConcurrentMap<Integer, String> cache = null;

        assertThat(cache).isNotNull();
        assertThat(cache.get(1)).isEqualTo("alice@google.com");
    }
}
