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
 * EXERCISE 04: MAPPING & TRANSFORMATION (20 EXERCISES)
 * ====================================================================================================
 */
public class Exercise04_MappingAndTransformation {

    @Test
    @DisplayName("01 [★☆☆☆☆] Map employees to their full names")
    void exercise01_mapFullNames() {
        // TODO: Map each Employee to employee.getFullName(), collect to List<String>
        List<String> fullNames = null;

        assertThat(fullNames).isNotNull();
        assertThat(fullNames).hasSize(30);
        assertThat(fullNames.get(0)).isEqualTo("Alice Johnson");
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Map products to prices")
    void exercise02_mapPrices() {
        // TODO: Map products to Product::getPrice, collect to List<Double>
        List<Double> prices = null;

        assertThat(prices).isNotNull();
        assertThat(prices).hasSize(30);
        assertThat(prices.get(0)).isEqualTo(2499.99);
    }

    @Test
    @DisplayName("03 [★☆☆☆☆] Map Employee to EmployeeDTO")
    void exercise03_mapToDto() {
        // TODO: Transform all employees into EmployeeDTO via EmployeeDTO::fromEmployee
        List<EmployeeDTO> dtos = null;

        assertThat(dtos).isNotNull();
        assertThat(dtos).hasSize(30);
        assertThat(dtos.get(0).getFullName()).isEqualTo("Alice Johnson");
    }

    @Test
    @DisplayName("04 [★★☆☆☆] Calculate average product rating using mapToDouble")
    void exercise04_averageRating() {
        // TODO: Use mapToDouble(Product::getRating).average().orElse(0.0)
        double avgRating = 0.0;

        assertThat(avgRating).isBetween(4.5, 4.8);
    }

    @Test
    @DisplayName("05 [★★☆☆☆] flatMap: Extract all OrderItems from all orders")
    void exercise05_allOrderItems() {
        // TODO: flatMap orders -> order.getItems().stream()
        List<OrderItem> allItems = null;

        assertThat(allItems).isNotNull();
        assertThat(allItems).hasSize(24);
    }

    @Test
    @DisplayName("06 [★★☆☆☆] Formatted strings: 'FirstName LastName (Department) - $Salary'")
    void exercise06_formattedEmployeeString() {
        // Example: "Alice Johnson (Engineering) - $185,000"
        // TODO: Map employees to formatted String
        List<String> formatted = null;

        assertThat(formatted).isNotNull();
        assertThat(formatted.get(0)).isEqualTo("Alice Johnson (Engineering) - $185,000");
    }

    @Test
    @DisplayName("07 [★★★☆☆] Flatten List<List<String>> of teams into distinct, sorted member names")
    void exercise07_flattenTeams() {
        List<List<String>> teams = DataFactory.getTeams();
        // TODO: flatMap teams into individual names, remove duplicates, sort alphabetically
        List<String> allMembers = null;

        assertThat(allMembers).isNotNull();
        assertThat(allMembers).contains("Alice", "Bob", "Carol", "David", "Eve", "Frank");
        assertThat(allMembers).isSorted();
    }

    @Test
    @DisplayName("08 [★★★☆☆] Extract all unique categories purchased in DELIVERED orders")
    void exercise08_deliveredCategories() {
        // Filter orders status == "DELIVERED", flatMap items, map category, distinct, sorted
        // TODO: Complete the pipeline
        List<String> deliveredCategories = null;

        assertThat(deliveredCategories).isNotNull();
        assertThat(deliveredCategories).contains("Books", "Clothing", "Digital", "Electronics", "Home");
    }

    @Test
    @DisplayName("09 [★★★☆☆] Transformation pipeline: Salary with 12% bonus formatted as Currency")
    void exercise09_salaryBonusFormatter() {
        // Step 1: Map employee to salary * 1.12
        // Step 2: Format to "$XXX,XXX.XX" with String.format("$%,.2f", salaryWithBonus)
        // TODO: Build the pipeline
        List<String> bonusPayrolls = null;

        assertThat(bonusPayrolls).isNotNull();
        assertThat(bonusPayrolls.get(0)).isEqualTo("$207,200.00"); // 185,000 * 1.12
    }

    @Test
    @DisplayName("10 [★★★★☆] FlatMap + Filter: Items costing > $200 from SHIPPED orders")
    void exercise10_highValueShippedItems() {
        // Filter orders where status is "SHIPPED", flatMap to items, filter item price > 200.0
        // TODO: Complete pipeline
        List<OrderItem> items = null;

        assertThat(items).isNotNull();
        assertThat(items).allMatch(item -> item.getPrice() > 200.0);
        assertThat(items).extracting(OrderItem::getProductName).contains("Galaxy S24 Ultra", "Nintendo Switch OLED");
    }

    @Test
    @DisplayName("11 [★★★★☆] Multi-level flatMap: Extract all unique skills across all departments")
    void exercise11_allUniqueSkills() {
        Map<String, List<String>> deptSkills = DataFactory.getDepartmentSkills();
        // Extract all skill lists, flatMap to individual skills, distinct, sorted
        // TODO: Complete pipeline
        List<String> allSkills = null;

        assertThat(allSkills).isNotNull();
        assertThat(allSkills).contains("Algorithms", "CSS", "Java", "Python", "SQL", "System Design");
    }

    @Test
    @DisplayName("12 [★★★★★] Aggregate total revenue generated per order using map and sum")
    void exercise12_orderRevenueMap() {
        // Problem: Produce a Map<Integer, Double> mapping orderId -> total order revenue
        // (total revenue = sum of item.price * item.quantity for all items in that order)
        // TODO: Create the map using Collectors.toMap
        Map<Integer, Double> orderRevenue = null;

        assertThat(orderRevenue).isNotNull();
        assertThat(orderRevenue.get(1)).isEqualTo(2499.99 * 1 + 249.99 * 1 + 19.99 * 3);
    }

    @Test
    @DisplayName("13 [★☆☆☆☆] Map products to uppercase brand names")
    void exercise13_mapBrandsUpper() {
        // TODO: Map products to brand in uppercase, distinct, sorted
        List<String> brands = null;

        assertThat(brands).isNotNull();
        assertThat(brands).contains("APPLE", "GOOGLE", "NIKE", "SONY");
    }

    @Test
    @DisplayName("14 [★★☆☆☆] Map order items to line subtotals")
    void exercise14_itemSubtotals() {
        List<Order> orders = DataFactory.getOrders();
        // TODO: flatMap orders to items, map to item.getSubtotal(), sum using mapToDouble
        double grandTotal = 0.0;

        assertThat(grandTotal).isGreaterThan(10000.0);
    }

    @Test
    @DisplayName("15 [★★☆☆☆] Transform Transaction list to formatted ledger entries")
    void exercise15_ledgerEntries() {
        // Format: "[TX-ID] TYPE $AMOUNT - CATEGORY"
        // Example: "[1] CREDIT $5,500.00 - SALARY"
        // TODO: Map transactions from DataFactory.getTransactions()
        List<String> ledger = null;

        assertThat(ledger).isNotNull();
        assertThat(ledger.get(0)).isEqualTo("[1] CREDIT $5,500.00 - SALARY");
    }

    @Test
    @DisplayName("16 [★★★☆☆] flatMapToInt: Sum of digits across a list of integers")
    void exercise16_sumOfDigits() {
        List<Integer> numbers = List.of(123, 456, 789);
        // Split each integer into individual digits and sum them: (1+2+3) + (4+5+6) + (7+8+9) = 45
        // TODO: Use flatMapToInt with String.valueOf(n).chars().map(c -> c - '0')
        int totalSum = 0;

        assertThat(totalSum).isEqualTo(45);
    }

    @Test
    @DisplayName("17 [★★★☆☆] Tokenize all sentences into distinct, sorted lowercase words")
    void exercise17_tokenizeSentences() {
        List<String> sentences = DataFactory.getSentences();
        // TODO: flatMap sentences splitting by "\\s+", convert to lowercase, distinct, sorted
        List<String> vocab = null;

        assertThat(vocab).isNotNull();
        assertThat(vocab).contains("functional", "java", "lambda", "streams");
    }

    @Test
    @DisplayName("18 [★★★★☆] FlatMap with Optional: Safely parse numbers from mixed string list")
    void exercise18_safeNumberParsing() {
        List<String> mixed = List.of("100", "invalid", "250", "abc", "500");

        // Helper lambda: s -> { try { return Optional.of(Integer.parseInt(s)); } catch(Exception e) { return Optional.empty(); } }
        // TODO: flatMap with Optional::stream to keep only valid parsed integers
        List<Integer> parsed = null;

        assertThat(parsed).isNotNull();
        assertThat(parsed).containsExactly(100, 250, 500);
    }

    @Test
    @DisplayName("19 [★★★★☆] Cross-Product: Map products and discount tiers to PromotionalPrice records")
    void exercise19_crossProductPromotions() {
        record Promo(String productName, int discountPercent, double discountedPrice) {}

        List<Product> sample = DataFactory.getProducts().subList(0, 2);
        List<Integer> discounts = List.of(10, 20); // 10% and 20%

        // TODO: flatMap sample products against discounts to produce 4 Promo records
        List<Promo> promos = null;

        assertThat(promos).isNotNull();
        assertThat(promos).hasSize(4);
    }

    @Test
    @DisplayName("20 [★★★★★] Complex Pipeline: Customer Item Frequency Map")
    void exercise20_customerItemFrequency() {
        // Output Map<String, List<String>> mapping CustomerName -> List of distinct product names they ordered
        // TODO: Group orders by customerName, downstream flatMapping over items to distinct product name list
        Map<String, List<String>> customerProducts = null;

        assertThat(customerProducts).isNotNull();
        assertThat(customerProducts.get("Alice Johnson")).contains("MacBook Pro 16\"", "AirPods Pro 2", "USB-C Cable", "Instant Pot Duo", "YETI Tumbler 30oz");
    }
}
