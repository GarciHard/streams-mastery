package com.streammastery.exercises;

import com.streammastery.model.Employee;
import com.streammastery.model.Product;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * EXERCISE 03: FILTERING, SLICING & MATCHING (20 EXERCISES)
 * ====================================================================================================
 */
public class Exercise03_FilteringAndMatching {

    @Test
    @DisplayName("01 [★☆☆☆☆] Filter employees by salary threshold")
    void exercise01_filterSalary() {
        // Problem: Find all employees earning strictly over $250,000.
        // TODO: Replace null with your stream filter pipeline
        List<Employee> topEarners = null;

        assertThat(topEarners).isNotNull();
        assertThat(topEarners).allMatch(e -> e.getSalary() > 250_000.0);
        assertThat(topEarners).hasSize(4); // Eve (320k), Ryan (310k), Uma (260k), Amy (340k)
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Check if any employee is located in New York")
    void exercise02_anyMatchCity() {
        // TODO: Replace false with your stream anyMatch expression
        boolean anyInNY = false;

        assertThat(anyInNY).isTrue();
    }

    @Test
    @DisplayName("03 [★☆☆☆☆] Find the first employee in Data Science")
    void exercise03_findFirstDataScience() {
        // TODO: Find first employee where department is "Data Science"
        Optional<Employee> firstDataScientist = Optional.empty();

        assertThat(firstDataScientist).isPresent();
        assertThat(firstDataScientist.get().getFirstName()).isEqualTo("Jack");
    }

    @Test
    @DisplayName("04 [★★☆☆☆] Extract distinct programming languages in Engineering")
    void exercise04_distinctLanguages() {
        // TODO: Filter department == "Engineering", extract language, get distinct, sort alphabetically
        List<String> languages = null;

        assertThat(languages).isNotNull();
        assertThat(languages).containsExactly("C++", "Go", "Java", "Python", "Swift");
    }

    @Test
    @DisplayName("05 [★★☆☆☆] Filter Electronics with price < $500 and rating >= 4.7")
    void exercise05_filterElectronics() {
        // TODO: Filter products by category "Electronics", price < 500, rating >= 4.7
        List<Product> topRatedElectronics = null;

        assertThat(topRatedElectronics).isNotNull();
        assertThat(topRatedElectronics).allMatch(p -> "Electronics".equals(p.getCategory()) && p.getPrice() < 500 && p.getRating() >= 4.7);
        assertThat(topRatedElectronics).isNotEmpty();
    }

    @Test
    @DisplayName("06 [★★☆☆☆] takeWhile on price-sorted products")
    void exercise06_takeWhileBudget() {
        List<Product> sortedByPrice = DataFactory.getProducts().stream()
                .sorted(Comparator.comparing(Product::getPrice))
                .toList();

        // TODO: Use takeWhile to grab all products with price <= $50.00
        List<Product> budgetProducts = null;

        assertThat(budgetProducts).isNotNull();
        assertThat(budgetProducts).allMatch(p -> p.getPrice() <= 50.0);
        assertThat(budgetProducts).isNotEmpty();
    }

    @Test
    @DisplayName("07 [★★★☆☆] Validate that all engineering employees have at least 1 year of experience")
    void exercise07_allMatchExperience() {
        // TODO: Replace false with stream allMatch for Engineering department employees
        boolean allExperienced = false;

        assertThat(allExperienced).isTrue();
    }

    @Test
    @DisplayName("08 [★★★☆☆] Pagination: Page 3 of Products (Page size 8)")
    void exercise08_pagination() {
        // Page index 2 (third page, 0-indexed), page size 8
        // TODO: Use skip and limit to extract the 8 products for page 3
        List<Product> page3 = null;

        assertThat(page3).isNotNull();
        assertThat(page3).hasSize(8);
        assertThat(page3.get(0).getId()).isEqualTo(17); // 1-based index 17
    }

    @Test
    @DisplayName("09 [★★★☆☆] dropWhile: Skip employees younger than 30 in age-sorted list")
    void exercise09_dropWhileAge() {
        List<Employee> sortedByAge = DataFactory.getEmployees().stream()
                .sorted(Comparator.comparing(Employee::getAge))
                .toList();

        // TODO: Use dropWhile to skip all employees with age < 30
        List<Employee> age30AndAbove = null;

        assertThat(age30AndAbove).isNotNull();
        assertThat(age30AndAbove).allMatch(e -> e.getAge() >= 30);
    }

    @Test
    @DisplayName("10 [★★★★☆] noneMatch: Verify no junior designer earns over $150,000")
    void exercise10_noneMatchJunior() {
        // Criteria: title contains "Junior Designer" AND salary > 150000.0
        // TODO: Use noneMatch to verify this condition
        boolean noneEarnsExcessive = false;

        assertThat(noneEarnsExcessive).isTrue();
    }

    @Test
    @DisplayName("11 [★★★★☆] Composed Predicate filtering with negation")
    void exercise11_predicateNegation() {
        Predicate<Employee> isSanFrancisco = e -> "San Francisco".equalsIgnoreCase(e.getCity());
        Predicate<Employee> isCupertino = e -> "Cupertino".equalsIgnoreCase(e.getCity());
        Predicate<Employee> isBayArea = isSanFrancisco.or(isCupertino);

        // Problem: Find all Engineering employees NOT in the Bay Area, earning over $200,000
        // TODO: Combine predicates using and() and negate()
        List<Employee> remoteOrNonBayStaff = null;

        assertThat(remoteOrNonBayStaff).isNotNull();
        assertThat(remoteOrNonBayStaff).allMatch(e -> !isBayArea.test(e) && e.getSalary() > 200_000.0 && "Engineering".equals(e.getDepartment()));
    }

    @Test
    @DisplayName("12 [★★★★★] Dynamic Query Builder: Multi-field optional filter")
    void exercise12_dynamicQueryFilter() {
        class FilterCriteria {
            String department;
            Double minSalary;
            String language;
            FilterCriteria(String d, Double s, String l) { this.department = d; this.minSalary = s; this.language = l; }
        }

        FilterCriteria criteria = new FilterCriteria("Engineering", 180_000.0, "Java");

        // TODO: Stream DataFactory.getEmployees() and apply only non-null criteria
        List<Employee> matched = null;

        assertThat(matched).isNotNull();
        assertThat(matched).allMatch(e -> "Engineering".equals(e.getDepartment()) && e.getSalary() >= 180000.0 && "Java".equals(e.getProgrammingLanguage()));
        assertThat(matched).hasSize(4); // Alice, Mia, Ryan, Zack
    }

    @Test
    @DisplayName("13 [★☆☆☆☆] Filter products by stock availability")
    void exercise13_filterInStock() {
        // TODO: Filter products with stock >= 500
        List<Product> highStock = null;

        assertThat(highStock).isNotNull();
        assertThat(highStock).allMatch(p -> p.getStock() >= 500);
        assertThat(highStock).hasSize(7);
    }

    @Test
    @DisplayName("14 [★★☆☆☆] Find any employee programming in Swift")
    void exercise14_findAnySwift() {
        // TODO: Filter programmingLanguage == "Swift", findAny()
        Optional<Employee> swiftDev = Optional.empty();

        assertThat(swiftDev).isPresent();
        assertThat(swiftDev.get().getProgrammingLanguage()).isEqualTo("Swift");
    }

    @Test
    @DisplayName("15 [★★☆☆☆] noneMatch on products with poor ratings")
    void exercise15_noneMatchLowRating() {
        // TODO: Use noneMatch to verify that no product has a rating strictly below 4.0
        boolean noBadProducts = false;

        assertThat(noBadProducts).isTrue();
    }

    @Test
    @DisplayName("16 [★★★☆☆] Distinct product brands in Books category")
    void exercise16_distinctBookPublishers() {
        // TODO: Filter category == "Books", map getBrand, distinct, sorted
        List<String> publishers = null;

        assertThat(publishers).isNotNull();
        assertThat(publishers).containsExactly("Addison-Wesley", "Penguin", "Pragmatic", "Prentice Hall");
    }

    @Test
    @DisplayName("17 [★★★☆☆] takeWhile on ordered timestamps: Slicing business hours")
    void exercise17_takeWhileBusinessHours() {
        List<Integer> hours = List.of(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        // Business hours are strictly before 17 (5 PM).
        // TODO: Use takeWhile to take hours < 17
        List<Integer> workHours = null;

        assertThat(workHours).isNotNull();
        assertThat(workHours).containsExactly(8, 9, 10, 11, 12, 13, 14, 15, 16);
    }

    @Test
    @DisplayName("18 [★★★★☆] Sieve of Eratosthenes filter using Streams")
    void exercise18_primeFilter() {
        List<Integer> candidatePrimes = IntStream.rangeClosed(2, 30).boxed().toList();

        // TODO: Filter candidatePrimes keeping only prime numbers (n is prime if no d in 2..sqrt(n) divides n)
        List<Integer> primes = null;

        assertThat(primes).isNotNull();
        assertThat(primes).containsExactly(2, 3, 5, 7, 11, 13, 17, 19, 23, 29);
    }

    @Test
    @DisplayName("19 [★★★★☆] Filter with custom identity key extractor (Deduplicate by custom property)")
    void exercise19_deduplicateByDepartment() {
        // Problem: Keep only the FIRST employee seen for each department (deduplicate employees by department)
        // Hint: Maintain a Set<String> seen = new HashSet<>() inside a custom predicate helper: seen.add(e.getDepartment())
        Set<String> seenDepts = new HashSet<>();
        // TODO: Filter DataFactory.getEmployees() using seenDepts.add(e.getDepartment())
        List<Employee> firstPerDept = null;

        assertThat(firstPerDept).isNotNull();
        assertThat(firstPerDept).hasSize(5); // Engineering, Design, Marketing, Data Science, Product
    }

    @Test
    @DisplayName("20 [★★★★★] Complex Sliding Window Matcher")
    void exercise20_slidingWindowMatcher() {
        // Problem: Given an array of daily server latency measurements in milliseconds,
        // determine if there exists ANY 3 consecutive readings where EVERY reading was > 100ms (SLO Breach).
        List<Integer> latencies = List.of(45, 60, 120, 140, 110, 80, 95);

        // TODO: Use IntStream.rangeClosed(0, latencies.size() - 3) to test if any 3 consecutive are all > 100
        boolean hasSloBreach = false;

        assertThat(hasSloBreach).isTrue();
    }
}
