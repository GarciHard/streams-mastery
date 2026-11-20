package com.streammastery.lessons;

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
 * LESSON 04: MAPPING, TRANSFORMATION & FLATMAP
 * ====================================================================================================
 *
 * <h2>1. map(T -> R) vs flatMap(T -> Stream&lt;R&gt;) (The Functor vs Monad Duality)</h2>
 * <ul>
 *   <li><b>{@code map}:</b> One-to-One transformation. For every input element $T$, exactly ONE output element $R$ is emitted.
 *       Use for: Entity-to-DTO conversion, field extraction, type casting, mathematical scaling.</li>
 *   <li><b>{@code flatMap}:</b> One-to-Many (or One-to-Zero) transformation. For each input $T$, a {@code Stream<R>} is created,
 *       and all resulting streams are flattened into a single contiguous output stream!
 *       Use for: Unpacking nested collections, unwrapping Optionals, cross-joins, tokenizing sentences into words.</li>
 * </ul>
 *
 * <h2>2. Primitive Specializations (Zero-Allocation Rule):</h2>
 * <p>
 * Always use {@code mapToInt()}, {@code mapToLong()}, and {@code mapToDouble()} when mapping to numbers.
 * Calling {@code map(Employee::getSalary).reduce(0.0, Double::sum)} allocates thousands of {@code java.lang.Double} wrapper objects,
 * stressing the JVM Garbage Collector. Primitive streams perform accumulation in unboxed CPU registers.
 * </p>
 */
public class Lesson04_MappingAndTransformation {

    /**
     * SAMPLE 1: Entity-to-DTO Projection with map()
     * What this demonstrates:
     * - The classic Microservice / API pattern: mapping domain entities to public DTOs.
     * - Decoupling internal domain models from presentation layers.
     */
    @Test
    @DisplayName("01. Entity-to-DTO Transformation with map()")
    void demonstrateMapEntityToDto() {
        List<Employee> employees = DataFactory.getEmployees();

        // Map Employee -> EmployeeDTO
        List<EmployeeDTO> dtos = employees.stream()
                .filter(e -> "Engineering".equals(e.getDepartment()))
                .map(EmployeeDTO::fromEmployee)
                .toList();

        assertThat(dtos).hasSize(20);
        assertThat(dtos.get(0).getFullName()).isEqualTo("Alice Johnson");
        assertThat(dtos.get(0).getDepartment()).isEqualTo("Engineering");
    }

    /**
     * SAMPLE 2: Primitive Mapping (mapToDouble, mapToInt)
     * What this demonstrates:
     * - Eliminating boxing overhead when computing aggregations.
     */
    @Test
    @DisplayName("02. Primitive Mapping: Avoiding Garbage Collector Pressure")
    void demonstratePrimitiveMapping() {
        List<Employee> employees = DataFactory.getEmployees();

        // mapToDouble extracts primitive double directly:
        double totalPayroll = employees.stream()
                .mapToDouble(Employee::getSalary)
                .sum();

        double averagePayroll = employees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);

        assertThat(totalPayroll).isGreaterThan(5_000_000.0);
        assertThat(averagePayroll).isBetween(180_000.0, 220_000.0);
    }

    /**
     * SAMPLE 3: flatMap for Nested Collections
     * What this demonstrates:
     * - Order contains List<OrderItem> items.
     * - Flattening Order::getItems into a single flat stream of OrderItems.
     */
    @Test
    @DisplayName("03. Unpacking Hierarchical Collections with flatMap()")
    void demonstrateFlatMapOrdersToItems() {
        List<Order> orders = DataFactory.getOrders();

        // Each Order contains multiple OrderItems.
        // flatMap flattens Stream<List<OrderItem>> into Stream<OrderItem>:
        List<OrderItem> allItems = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .toList();

        assertThat(allItems.size()).isGreaterThan(orders.size());

        // Extract distinct product names purchased across all orders:
        List<String> distinctProducts = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .map(OrderItem::getProductName)
                .distinct()
                .sorted()
                .toList();

        assertThat(distinctProducts).contains("MacBook Pro 16\"", "AirPods Pro 2", "Instant Pot Duo");
    }

    /**
     * SAMPLE 4: flatMap with Optional::stream (Java 9 Monadic Flattening)
     * What this demonstrates:
     * - Optional.stream() returns a Stream containing 0 or 1 element.
     * - flatMap automatically eliminates Optional.empty() without any null checks!
     */
    @Test
    @DisplayName("04. Safe Optional Unwrapping via flatMap(Optional::stream)")
    void demonstrateFlatMapWithOptional() {
        List<Optional<String>> rawConfigTokens = List.of(
                Optional.of("bearer-token-123"),
                Optional.empty(),
                Optional.of("bearer-token-456"),
                Optional.empty()
        );

        // Java 9 idiom: flatMap(Optional::stream) filters out all Optional.empty()
        List<String> validTokens = rawConfigTokens.stream()
                .flatMap(Optional::stream)
                .toList();

        assertThat(validTokens).containsExactly("bearer-token-123", "bearer-token-456");
    }

    /**
     * SAMPLE 5: Multi-Level Transformation Chains
     * What this demonstrates:
     * - Chaining multiple map steps for progressive data refinement.
     */
    @Test
    @DisplayName("05. Multi-Stage Transformation Pipelines")
    void demonstrateTransformationChains() {
        List<Employee> employees = DataFactory.getEmployees();

        // Pipeline: Employee -> Salary -> With 10% raise -> Formatted Currency String
        List<String> formattedRaises = employees.stream()
                .filter(e -> "Engineering".equals(e.getDepartment()))
                .map(Employee::getSalary)
                .map(salary -> salary * 1.10)
                .map(raised -> String.format("$%,.2f", raised))
                .limit(3)
                .toList();

        assertThat(formattedRaises.get(0)).isEqualTo("$203,500.00"); // Alice: 185k * 1.10
    }
}
