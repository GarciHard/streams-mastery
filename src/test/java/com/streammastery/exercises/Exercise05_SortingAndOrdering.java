package com.streammastery.exercises;

import com.streammastery.model.Employee;
import com.streammastery.model.Product;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * EXERCISE 05: SORTING & ORDERING (20 EXERCISES)
 * ====================================================================================================
 */
public class Exercise05_SortingAndOrdering {

    @Test
    @DisplayName("01 [★☆☆☆☆] Sort employees alphabetically by first name")
    void exercise01_sortFirstName() {
        // TODO: Sort employees by firstName ascending
        List<Employee> sorted = null;

        assertThat(sorted).isNotNull();
        assertThat(sorted.get(0).getFirstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Sort products by price ascending")
    void exercise02_sortPriceAsc() {
        // TODO: Sort products by price ascending
        List<Product> sorted = null;

        assertThat(sorted).isNotNull();
        assertThat(sorted.get(0).getPrice()).isEqualTo(16.99); // Atomic Habits
    }

    @Test
    @DisplayName("03 [★☆☆☆☆] Sort employees by age descending")
    void exercise03_sortAgeDesc() {
        // TODO: Sort employees by age descending
        List<Employee> sorted = null;

        assertThat(sorted).isNotNull();
        assertThat(sorted.get(0).getAge()).isEqualTo(45); // Amy King
    }

    @Test
    @DisplayName("04 [★★☆☆☆] Sort employees by department ASC, then salary DESC")
    void exercise04_deptThenSalary() {
        // TODO: Comparator.comparing(Employee::getDepartment).thenComparing(Comparator.comparingDouble(Employee::getSalary).reversed())
        List<Employee> sorted = null;

        assertThat(sorted).isNotNull();
        Employee first = sorted.get(0);
        assertThat(first.getDepartment()).isEqualTo("Data Science");
        assertThat(first.getSalary()).isEqualTo(210000.0); // Quinn
    }

    @Test
    @DisplayName("05 [★★☆☆☆] Find top 5 highest-paid employees")
    void exercise05_top5Salaries() {
        // TODO: Sort by salary desc, limit 5
        List<Employee> top5 = null;

        assertThat(top5).isNotNull();
        assertThat(top5).hasSize(5);
        assertThat(top5.get(0).getSalary()).isEqualTo(340000.0); // Amy King
    }

    @Test
    @DisplayName("06 [★★☆☆☆] Sort products by rating DESC, then price ASC")
    void exercise06_ratingDescPriceAsc() {
        // TODO: Build comparator for rating DESC then price ASC
        List<Product> sorted = null;

        assertThat(sorted).isNotNull();
        assertThat(sorted.get(0).getRating()).isEqualTo(4.9);
        assertThat(sorted.get(0).getPrice()).isLessThanOrEqualTo(sorted.get(1).getPrice());
    }

    @Test
    @DisplayName("07 [★★★☆☆] Null-safe sort on a list containing null values")
    void exercise07_nullSafeSorting() {
        List<String> rawNames = Arrays.asList("Zack", null, "Alice", "Bob", null);
        // TODO: Sort alphabetically with nulls placed first
        List<String> sortedNullsFirst = null;

        assertThat(sortedNullsFirst).isNotNull();
        assertThat(sortedNullsFirst.get(0)).isNull();
        assertThat(sortedNullsFirst.get(1)).isNull();
        assertThat(sortedNullsFirst.get(2)).isEqualTo("Alice");
    }

    @Test
    @DisplayName("08 [★★★☆☆] Sort employees by years of experience, breaking ties with lastName")
    void exercise08_expThenName() {
        // TODO: Sort by years of experience asc, then lastName asc
        List<Employee> sorted = null;

        assertThat(sorted).isNotNull();
        Employee first1Yr = sorted.get(0);
        assertThat(first1Yr.getYearsOfExperience()).isEqualTo(1);
        assertThat(first1Yr.getLastName()).isEqualTo("Garcia"); // Garcia < Moore < Lee
    }

    @Test
    @DisplayName("09 [★★★☆☆] Find top 3 cheapest Footwear products")
    void exercise09_cheapestFootwear() {
        // TODO: Filter category == "Footwear", sort by price asc, limit 3
        List<Product> cheapFootwear = null;

        assertThat(cheapFootwear).isNotNull();
        assertThat(cheapFootwear).hasSize(3);
        assertThat(cheapFootwear.get(0).getName()).isEqualTo("Chuck Taylor All Star");
    }

    @Test
    @DisplayName("10 [★★★★☆] Custom String sort: By length ascending, then case-insensitive alphabetically")
    void exercise10_stringLengthThenAlpha() {
        List<String> words = List.of("banana", "apple", "fig", "cherry", "date", "Apricot");
        // TODO: Build comparator: length asc, then String.CASE_INSENSITIVE_ORDER
        List<String> sorted = null;

        assertThat(sorted).isNotNull();
        assertThat(sorted).containsExactly("fig", "date", "apple", "Banana", "cherry", "Apricot");
    }

    @Test
    @DisplayName("11 [★★★★☆] Custom priority ordering for Departments")
    void exercise11_departmentPrioritySort() {
        Map<String, Integer> deptRank = Map.of(
                "Engineering", 1,
                "Data Science", 2,
                "Product", 3,
                "Design", 4,
                "Marketing", 5
        );

        // TODO: Sort employees based on their department's rank in deptRank, breaking ties by salary desc
        List<Employee> prioritySorted = null;

        assertThat(prioritySorted).isNotNull();
        assertThat(prioritySorted.get(0).getDepartment()).isEqualTo("Engineering");
        assertThat(prioritySorted.get(prioritySorted.size() - 1).getDepartment()).isEqualTo("Marketing");
    }

    @Test
    @DisplayName("12 [★★★★★] Top 2 highest-rated products per category")
    void exercise12_top2PerCategory() {
        // Group products by category, within each category pick top 2 by rating desc (break tie by price asc)
        // Collect into Map<String, List<Product>>
        // TODO: Build the grouping pipeline
        Map<String, List<Product>> top2Map = null;

        assertThat(top2Map).isNotNull();
        assertThat(top2Map.get("Books")).hasSize(2);
        assertThat(top2Map.get("Electronics")).hasSize(2);
        assertThat(top2Map.get("Books").get(0).getRating()).isEqualTo(4.9);
    }

    @Test
    @DisplayName("13 [★☆☆☆☆] Sort programming languages by name length ascending")
    void exercise13_sortLanguagesByLength() {
        List<String> languages = DataFactory.getProgrammingLanguages();
        // TODO: Sort by String::length ascending, breaking ties naturally
        List<String> sortedByLength = null;

        assertThat(sortedByLength).isNotNull();
        assertThat(sortedByLength.get(0)).isEqualTo("C#");
        assertThat(sortedByLength.get(1)).isEqualTo("Go");
    }

    @Test
    @DisplayName("14 [★★☆☆☆] Sort employees by salary descending without boxing")
    void exercise14_sortSalaryUnboxed() {
        // TODO: Sort using Comparator.comparingDouble(Employee::getSalary).reversed()
        List<Employee> sorted = null;

        assertThat(sorted).isNotNull();
        assertThat(sorted.get(0).getSalary()).isEqualTo(340000.0); // Amy
        assertThat(sorted.get(sorted.size() - 1).getSalary()).isEqualTo(110000.0); // Olivia
    }

    @Test
    @DisplayName("15 [★★☆☆☆] Find top 3 oldest employees")
    void exercise15_top3Oldest() {
        // TODO: Sort by age descending, limit 3
        List<Employee> oldest = null;

        assertThat(oldest).isNotNull();
        assertThat(oldest).hasSize(3);
        assertThat(oldest.get(0).getAge()).isEqualTo(45); // Amy
        assertThat(oldest.get(1).getAge()).isEqualTo(44); // Ryan
        assertThat(oldest.get(2).getAge()).isEqualTo(42); // Eve
    }

    @Test
    @DisplayName("16 [★★★☆☆] Null-safe sort on Employee city (nulls last)")
    void exercise16_nullSafeEmployeeCity() {
        Employee e1 = new Employee(101, "Test1", "User", "t1@test.com", "Eng", "Dev", 25, 100000.0, null, "M", 2, "Java");
        Employee e2 = new Employee(102, "Test2", "User", "t2@test.com", "Eng", "Dev", 26, 110000.0, "Austin", "M", 3, "Java");
        Employee e3 = new Employee(103, "Test3", "User", "t3@test.com", "Eng", "Dev", 27, 120000.0, "Boston", "M", 4, "Java");
        List<Employee> list = List.of(e1, e2, e3);

        // TODO: Sort list by city using Comparator.comparing(Employee::getCity, Comparator.nullsLast(Comparator.naturalOrder()))
        List<Employee> sorted = null;

        assertThat(sorted).isNotNull();
        assertThat(sorted.get(0).getCity()).isEqualTo("Austin");
        assertThat(sorted.get(2).getCity()).isNull();
    }

    @Test
    @DisplayName("17 [★★★☆☆] Stable sort proof: Verify equal keys preserve original encounter order")
    void exercise17_stableSortVerification() {
        // 3 employees with identical years of experience (1 year): Henry Moore, Peter Garcia, Victor Lee
        List<Employee> oneYearDevs = DataFactory.getEmployees().stream()
                .filter(e -> e.getYearsOfExperience() == 1)
                .toList(); // Henry (id 8), Peter (id 16), Victor (id 22)

        // TODO: Sort only by yearsOfExperience (all equal to 1). TimSort must preserve exact encounter order!
        List<Employee> stableSorted = null;

        assertThat(stableSorted).isNotNull();
        assertThat(stableSorted.get(0).getId()).isEqualTo(8);
        assertThat(stableSorted.get(1).getId()).isEqualTo(16);
        assertThat(stableSorted.get(2).getId()).isEqualTo(22);
    }

    @Test
    @DisplayName("18 [★★★★☆] Custom multi-attribute sort: Product Stock/Price ratio descending")
    void exercise18_stockPriceRatioSort() {
        // Stock/Price ratio measures units available per dollar: p.getStock() / p.getPrice()
        // TODO: Sort products by stock/price ratio descending, limit 3
        List<Product> bestValueProducts = null;

        assertThat(bestValueProducts).isNotNull();
        assertThat(bestValueProducts).hasSize(3);
        assertThat(bestValueProducts.get(0).getName()).isEqualTo("Hanes T-Shirt Pack"); // 1000 / 19.99 = ~50
    }

    @Test
    @DisplayName("19 [★★★★☆] Sort strings by vowel count descending, break ties by length ascending")
    void exercise19_vowelCountSort() {
        List<String> words = List.of("education", "sky", "tree", "sequoia", "rhythm");

        // Helper: count vowels (a, e, i, o, u)
        // TODO: Sort words by vowel count desc, then length asc
        List<String> sortedByVowels = null;

        assertThat(sortedByVowels).isNotNull();
        assertThat(sortedByVowels.get(0)).isEqualTo("sequoia"); // 5 vowels
        assertThat(sortedByVowels.get(1)).isEqualTo("education"); // 5 vowels, length 9 > 7
        assertThat(sortedByVowels.get(sortedByVowels.size() - 1)).isEqualTo("sky"); // 0 vowels
    }

    @Test
    @DisplayName("20 [★★★★★] Sort Orders by total order price descending with tie-break on item count")
    void exercise20_orderValueSort() {
        // TODO: Sort DataFactory.getOrders() by order.getTotalPrice() desc, then by order.getItems().size() desc
        List<com.streammastery.model.Order> sortedOrders = null;

        assertThat(sortedOrders).isNotNull();
        assertThat(sortedOrders.get(0).getId()).isEqualTo(1); // Alice order with MacBook: ~$2800
        assertThat(sortedOrders.get(0).getTotalPrice()).isGreaterThan(2700.0);
    }
}
