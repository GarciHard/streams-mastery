package com.streammastery.util;

import com.streammastery.model.*;
import com.streammastery.model.Transaction.Type;

import java.time.LocalDate;
import java.util.*;

/**
 * Factory class providing rich test data for all stream exercises.
 * Uses in-memory data to avoid external dependencies in exercises.
 */
public class DataFactory {

    // ═══════════════════════════════════════════════════════════════
    //  EMPLOYEES
    // ═══════════════════════════════════════════════════════════════

    public static List<Employee> getEmployees() {
        return List.of(
            new Employee(1, "Alice", "Johnson", "alice@google.com", "Engineering", "Senior Engineer", 32, 185000.0, "San Francisco", "Female", 10, "Java"),
            new Employee(2, "Bob", "Smith", "bob@meta.com", "Engineering", "Staff Engineer", 38, 240000.0, "Menlo Park", "Male", 15, "Python"),
            new Employee(3, "Carol", "Williams", "carol@amazon.com", "Engineering", "Junior Engineer", 24, 120000.0, "Seattle", "Female", 2, "Java"),
            new Employee(4, "David", "Brown", "david@apple.com", "Design", "Senior Designer", 35, 165000.0, "Cupertino", "Male", 12, "Swift"),
            new Employee(5, "Eve", "Davis", "eve@netflix.com", "Engineering", "Principal Engineer", 42, 320000.0, "Los Gatos", "Female", 20, "Java"),
            new Employee(6, "Frank", "Miller", "frank@google.com", "Marketing", "Marketing Manager", 29, 130000.0, "New York", "Male", 6, "Python"),
            new Employee(7, "Grace", "Wilson", "grace@meta.com", "Engineering", "Senior Engineer", 31, 195000.0, "Menlo Park", "Female", 9, "Go"),
            new Employee(8, "Henry", "Moore", "henry@amazon.com", "Engineering", "Junior Engineer", 23, 115000.0, "Seattle", "Male", 1, "Java"),
            new Employee(9, "Ivy", "Taylor", "ivy@apple.com", "Engineering", "Staff Engineer", 36, 250000.0, "Cupertino", "Female", 14, "Swift"),
            new Employee(10, "Jack", "Anderson", "jack@netflix.com", "Data Science", "Data Scientist", 28, 160000.0, "Los Gatos", "Male", 5, "Python"),
            new Employee(11, "Karen", "Thomas", "karen@google.com", "Engineering", "Senior Engineer", 33, 190000.0, "San Francisco", "Female", 11, "Go"),
            new Employee(12, "Leo", "Jackson", "leo@meta.com", "Product", "Product Manager", 30, 170000.0, "Menlo Park", "Male", 7, "JavaScript"),
            new Employee(13, "Mia", "White", "mia@amazon.com", "Engineering", "Staff Engineer", 37, 235000.0, "Seattle", "Female", 15, "Java"),
            new Employee(14, "Noah", "Harris", "noah@apple.com", "Engineering", "Senior Engineer", 34, 200000.0, "Cupertino", "Male", 12, "C++"),
            new Employee(15, "Olivia", "Martin", "olivia@netflix.com", "Design", "Junior Designer", 25, 110000.0, "Los Gatos", "Female", 3, "JavaScript"),
            new Employee(16, "Peter", "Garcia", "peter@google.com", "Engineering", "Junior Engineer", 22, 125000.0, "San Francisco", "Male", 1, "Python"),
            new Employee(17, "Quinn", "Martinez", "quinn@meta.com", "Data Science", "Senior Data Scientist", 35, 210000.0, "Menlo Park", "Female", 13, "Python"),
            new Employee(18, "Ryan", "Robinson", "ryan@amazon.com", "Engineering", "Principal Engineer", 44, 310000.0, "Seattle", "Male", 22, "Java"),
            new Employee(19, "Sophia", "Clark", "sophia@apple.com", "Marketing", "Marketing Director", 40, 220000.0, "Cupertino", "Female", 18, "JavaScript"),
            new Employee(20, "Thomas", "Rodriguez", "thomas@netflix.com", "Engineering", "Senior Engineer", 30, 188000.0, "Los Gatos", "Male", 8, "Go"),
            new Employee(21, "Uma", "Lewis", "uma@google.com", "Engineering", "Staff Engineer", 39, 260000.0, "San Francisco", "Female", 17, "Java"),
            new Employee(22, "Victor", "Lee", "victor@meta.com", "Engineering", "Junior Engineer", 23, 118000.0, "New York", "Male", 1, "Python"),
            new Employee(23, "Wendy", "Walker", "wendy@amazon.com", "Design", "Senior Designer", 32, 158000.0, "Seattle", "Female", 10, "JavaScript"),
            new Employee(24, "Xavier", "Hall", "xavier@apple.com", "Engineering", "Senior Engineer", 36, 205000.0, "Cupertino", "Male", 14, "C++"),
            new Employee(25, "Yara", "Allen", "yara@netflix.com", "Data Science", "Data Scientist", 27, 155000.0, "Los Gatos", "Female", 4, "Python"),
            new Employee(26, "Zack", "Young", "zack@google.com", "Engineering", "Senior Engineer", 34, 198000.0, "New York", "Male", 12, "Java"),
            new Employee(27, "Amy", "King", "amy@meta.com", "Engineering", "Principal Engineer", 45, 340000.0, "Menlo Park", "Female", 23, "C++"),
            new Employee(28, "Brian", "Wright", "brian@amazon.com", "Product", "Senior Product Manager", 33, 185000.0, "Seattle", "Male", 11, "Java"),
            new Employee(29, "Clara", "Lopez", "clara@apple.com", "Engineering", "Junior Engineer", 24, 122000.0, "Cupertino", "Female", 2, "Swift"),
            new Employee(30, "Derek", "Hill", "derek@netflix.com", "Engineering", "Staff Engineer", 38, 245000.0, "Los Gatos", "Male", 16, "Go")
        );
    }

    // ═══════════════════════════════════════════════════════════════
    //  PRODUCTS
    // ═══════════════════════════════════════════════════════════════

    public static List<Product> getProducts() {
        return List.of(
            new Product(1, "MacBook Pro 16\"", "Electronics", 2499.99, 50, 4.8, "Apple"),
            new Product(2, "iPhone 15 Pro", "Electronics", 1199.99, 200, 4.7, "Apple"),
            new Product(3, "Galaxy S24 Ultra", "Electronics", 1299.99, 150, 4.6, "Samsung"),
            new Product(4, "Pixel 8 Pro", "Electronics", 999.99, 100, 4.5, "Google"),
            new Product(5, "ThinkPad X1 Carbon", "Electronics", 1849.99, 30, 4.4, "Lenovo"),
            new Product(6, "Air Jordan 1 Retro", "Footwear", 180.00, 500, 4.9, "Nike"),
            new Product(7, "Ultra Boost 22", "Footwear", 190.00, 300, 4.6, "Adidas"),
            new Product(8, "Classic Leather", "Footwear", 75.00, 400, 4.2, "Reebok"),
            new Product(9, "Chuck Taylor All Star", "Footwear", 55.00, 600, 4.5, "Converse"),
            new Product(10, "Air Max 90", "Footwear", 130.00, 250, 4.7, "Nike"),
            new Product(11, "Levi's 501 Jeans", "Clothing", 69.50, 800, 4.3, "Levi's"),
            new Product(12, "Patagonia Fleece", "Clothing", 149.00, 200, 4.8, "Patagonia"),
            new Product(13, "North Face Jacket", "Clothing", 199.99, 150, 4.6, "North Face"),
            new Product(14, "Uniqlo Down Jacket", "Clothing", 89.90, 350, 4.4, "Uniqlo"),
            new Product(15, "Hanes T-Shirt Pack", "Clothing", 19.99, 1000, 4.1, "Hanes"),
            new Product(16, "Sony WH-1000XM5", "Electronics", 349.99, 180, 4.8, "Sony"),
            new Product(17, "AirPods Pro 2", "Electronics", 249.99, 400, 4.7, "Apple"),
            new Product(18, "Kindle Paperwhite", "Electronics", 139.99, 250, 4.6, "Amazon"),
            new Product(19, "Nintendo Switch OLED", "Electronics", 349.99, 120, 4.8, "Nintendo"),
            new Product(20, "PS5 Digital Edition", "Electronics", 449.99, 80, 4.9, "Sony"),
            new Product(21, "Instant Pot Duo", "Home", 89.99, 500, 4.7, "Instant Pot"),
            new Product(22, "Dyson V15 Detect", "Home", 749.99, 60, 4.5, "Dyson"),
            new Product(23, "KitchenAid Mixer", "Home", 379.99, 100, 4.8, "KitchenAid"),
            new Product(24, "Roomba i7+", "Home", 599.99, 90, 4.4, "iRobot"),
            new Product(25, "YETI Tumbler 30oz", "Home", 38.00, 700, 4.9, "YETI"),
            new Product(26, "Atomic Habits", "Books", 16.99, 1500, 4.9, "Penguin"),
            new Product(27, "Clean Code", "Books", 39.99, 300, 4.7, "Prentice Hall"),
            new Product(28, "Design Patterns", "Books", 49.99, 200, 4.5, "Addison-Wesley"),
            new Product(29, "The Pragmatic Programmer", "Books", 44.99, 250, 4.8, "Pragmatic"),
            new Product(30, "Effective Java", "Books", 42.99, 280, 4.9, "Addison-Wesley")
        );
    }

    // ═══════════════════════════════════════════════════════════════
    //  ORDERS (with nested OrderItems for flatMap exercises)
    // ═══════════════════════════════════════════════════════════════

    public static List<Order> getOrders() {
        return List.of(
            new Order(1, 101, "Alice Johnson", "DELIVERED", LocalDate.of(2024, 1, 15), List.of(
                new OrderItem("MacBook Pro 16\"", "Electronics", 2499.99, 1),
                new OrderItem("AirPods Pro 2", "Electronics", 249.99, 1),
                new OrderItem("USB-C Cable", "Electronics", 19.99, 3)
            )),
            new Order(2, 102, "Bob Smith", "SHIPPED", LocalDate.of(2024, 2, 20), List.of(
                new OrderItem("Air Jordan 1 Retro", "Footwear", 180.00, 1),
                new OrderItem("Nike Socks Pack", "Clothing", 14.99, 2)
            )),
            new Order(3, 103, "Carol Williams", "PENDING", LocalDate.of(2024, 3, 10), List.of(
                new OrderItem("Kindle Paperwhite", "Electronics", 139.99, 1),
                new OrderItem("Atomic Habits", "Books", 16.99, 1),
                new OrderItem("Clean Code", "Books", 39.99, 1),
                new OrderItem("Effective Java", "Books", 42.99, 1)
            )),
            new Order(4, 101, "Alice Johnson", "DELIVERED", LocalDate.of(2024, 1, 28), List.of(
                new OrderItem("Instant Pot Duo", "Home", 89.99, 1),
                new OrderItem("YETI Tumbler 30oz", "Home", 38.00, 2)
            )),
            new Order(5, 104, "David Brown", "CANCELLED", LocalDate.of(2024, 2, 5), List.of(
                new OrderItem("PS5 Digital Edition", "Electronics", 449.99, 1)
            )),
            new Order(6, 105, "Eve Davis", "DELIVERED", LocalDate.of(2024, 3, 22), List.of(
                new OrderItem("Dyson V15 Detect", "Home", 749.99, 1),
                new OrderItem("Roomba i7+", "Home", 599.99, 1)
            )),
            new Order(7, 106, "Frank Miller", "SHIPPED", LocalDate.of(2024, 4, 1), List.of(
                new OrderItem("Galaxy S24 Ultra", "Electronics", 1299.99, 1),
                new OrderItem("Samsung Case", "Electronics", 29.99, 1)
            )),
            new Order(8, 102, "Bob Smith", "DELIVERED", LocalDate.of(2024, 1, 5), List.of(
                new OrderItem("Levi's 501 Jeans", "Clothing", 69.50, 2),
                new OrderItem("Hanes T-Shirt Pack", "Clothing", 19.99, 3)
            )),
            new Order(9, 107, "Grace Wilson", "PENDING", LocalDate.of(2024, 4, 15), List.of(
                new OrderItem("KitchenAid Mixer", "Home", 379.99, 1),
                new OrderItem("Patagonia Fleece", "Clothing", 149.00, 1),
                new OrderItem("North Face Jacket", "Clothing", 199.99, 1)
            )),
            new Order(10, 103, "Carol Williams", "DELIVERED", LocalDate.of(2024, 2, 14), List.of(
                new OrderItem("Sony WH-1000XM5", "Electronics", 349.99, 1),
                new OrderItem("Spotify Gift Card", "Digital", 99.99, 1)
            )),
            new Order(11, 108, "Henry Moore", "SHIPPED", LocalDate.of(2024, 3, 30), List.of(
                new OrderItem("Nintendo Switch OLED", "Electronics", 349.99, 1),
                new OrderItem("Zelda TOTK", "Electronics", 69.99, 1)
            )),
            new Order(12, 104, "David Brown", "DELIVERED", LocalDate.of(2024, 4, 10), List.of(
                new OrderItem("The Pragmatic Programmer", "Books", 44.99, 1),
                new OrderItem("Design Patterns", "Books", 49.99, 1)
            ))
        );
    }

    // ═══════════════════════════════════════════════════════════════
    //  TRANSACTIONS (for grouping/partitioning/statistics exercises)
    // ═══════════════════════════════════════════════════════════════

    public static List<Transaction> getTransactions() {
        return List.of(
            new Transaction(1, "ACC-001", Type.CREDIT, 5500.00, "USD", "SALARY", LocalDate.of(2024, 1, 1), "TechCorp Inc"),
            new Transaction(2, "ACC-001", Type.DEBIT, 1200.00, "USD", "HOUSING", LocalDate.of(2024, 1, 2), "Apartment LLC"),
            new Transaction(3, "ACC-001", Type.DEBIT, 85.50, "USD", "FOOD", LocalDate.of(2024, 1, 3), "Whole Foods"),
            new Transaction(4, "ACC-001", Type.DEBIT, 45.00, "USD", "TRANSPORT", LocalDate.of(2024, 1, 3), "Uber"),
            new Transaction(5, "ACC-001", Type.DEBIT, 15.99, "USD", "ENTERTAINMENT", LocalDate.of(2024, 1, 4), "Netflix"),
            new Transaction(6, "ACC-001", Type.DEBIT, 120.00, "USD", "UTILITIES", LocalDate.of(2024, 1, 5), "PG&E"),
            new Transaction(7, "ACC-002", Type.CREDIT, 7200.00, "USD", "SALARY", LocalDate.of(2024, 1, 1), "BigBank Corp"),
            new Transaction(8, "ACC-002", Type.DEBIT, 2100.00, "USD", "HOUSING", LocalDate.of(2024, 1, 2), "Mortgage Co"),
            new Transaction(9, "ACC-002", Type.DEBIT, 250.00, "USD", "FOOD", LocalDate.of(2024, 1, 5), "Costco"),
            new Transaction(10, "ACC-002", Type.CREDIT, 500.00, "USD", "INVESTMENT", LocalDate.of(2024, 1, 6), "Vanguard"),
            new Transaction(11, "ACC-001", Type.DEBIT, 65.00, "USD", "FOOD", LocalDate.of(2024, 1, 7), "Trader Joe's"),
            new Transaction(12, "ACC-001", Type.DEBIT, 200.00, "USD", "ENTERTAINMENT", LocalDate.of(2024, 1, 8), "Concert Tickets"),
            new Transaction(13, "ACC-002", Type.DEBIT, 35.00, "USD", "TRANSPORT", LocalDate.of(2024, 1, 8), "Gas Station"),
            new Transaction(14, "ACC-003", Type.CREDIT, 4800.00, "EUR", "SALARY", LocalDate.of(2024, 1, 1), "EuroTech GmbH"),
            new Transaction(15, "ACC-003", Type.DEBIT, 900.00, "EUR", "HOUSING", LocalDate.of(2024, 1, 2), "Berlin Apt"),
            new Transaction(16, "ACC-003", Type.DEBIT, 150.00, "EUR", "FOOD", LocalDate.of(2024, 1, 4), "Edeka"),
            new Transaction(17, "ACC-003", Type.DEBIT, 50.00, "EUR", "TRANSPORT", LocalDate.of(2024, 1, 5), "BVG Monthly"),
            new Transaction(18, "ACC-001", Type.CREDIT, 5500.00, "USD", "SALARY", LocalDate.of(2024, 2, 1), "TechCorp Inc"),
            new Transaction(19, "ACC-001", Type.DEBIT, 1200.00, "USD", "HOUSING", LocalDate.of(2024, 2, 2), "Apartment LLC"),
            new Transaction(20, "ACC-001", Type.DEBIT, 95.00, "USD", "FOOD", LocalDate.of(2024, 2, 3), "Whole Foods"),
            new Transaction(21, "ACC-002", Type.CREDIT, 7200.00, "USD", "SALARY", LocalDate.of(2024, 2, 1), "BigBank Corp"),
            new Transaction(22, "ACC-002", Type.DEBIT, 2100.00, "USD", "HOUSING", LocalDate.of(2024, 2, 2), "Mortgage Co"),
            new Transaction(23, "ACC-002", Type.DEBIT, 180.00, "USD", "FOOD", LocalDate.of(2024, 2, 4), "Safeway"),
            new Transaction(24, "ACC-003", Type.CREDIT, 4800.00, "EUR", "SALARY", LocalDate.of(2024, 2, 1), "EuroTech GmbH"),
            new Transaction(25, "ACC-003", Type.DEBIT, 900.00, "EUR", "HOUSING", LocalDate.of(2024, 2, 2), "Berlin Apt"),
            new Transaction(26, "ACC-001", Type.CREDIT, 1000.00, "USD", "INVESTMENT", LocalDate.of(2024, 2, 10), "Fidelity"),
            new Transaction(27, "ACC-002", Type.DEBIT, 75.00, "USD", "ENTERTAINMENT", LocalDate.of(2024, 2, 14), "Movie Night"),
            new Transaction(28, "ACC-003", Type.DEBIT, 200.00, "EUR", "ENTERTAINMENT", LocalDate.of(2024, 2, 15), "Berghain"),
            new Transaction(29, "ACC-001", Type.DEBIT, 300.00, "USD", "FOOD", LocalDate.of(2024, 2, 20), "Restaurant Week"),
            new Transaction(30, "ACC-002", Type.CREDIT, 350.00, "USD", "INVESTMENT", LocalDate.of(2024, 2, 25), "Schwab")
        );
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPER DATA for various exercises
    // ═══════════════════════════════════════════════════════════════

    public static List<String> getProgrammingLanguages() {
        return List.of("Java", "Python", "JavaScript", "Go", "C++", "Rust",
                       "TypeScript", "Swift", "Kotlin", "Scala", "Ruby", "PHP",
                       "C#", "Dart", "Haskell", "Elixir", "Clojure", "R");
    }

    public static List<List<String>> getTeams() {
        return List.of(
            List.of("Alice", "Bob", "Carol"),
            List.of("David", "Eve"),
            List.of("Frank", "Grace", "Henry", "Ivy"),
            List.of("Jack"),
            List.of("Karen", "Leo", "Mia", "Noah", "Olivia")
        );
    }

    public static List<String> getSentences() {
        return List.of(
            "Java streams provide a functional approach to processing collections",
            "Lambda expressions are the foundation of functional programming in Java",
            "Method references offer a shorthand for lambda expressions",
            "Collectors provide powerful ways to accumulate stream elements",
            "Parallel streams can improve performance for CPU-intensive operations",
            "The Stream API was introduced in Java 8",
            "Optional helps avoid NullPointerException in stream pipelines",
            "FlatMap is essential for working with nested data structures"
        );
    }

    public static Map<String, List<String>> getDepartmentSkills() {
        return Map.of(
            "Engineering", List.of("Java", "Python", "Go", "C++", "System Design", "Algorithms"),
            "Data Science", List.of("Python", "R", "SQL", "TensorFlow", "Statistics"),
            "Design", List.of("Figma", "Sketch", "CSS", "JavaScript", "User Research"),
            "Product", List.of("Analytics", "SQL", "Communication", "Strategy"),
            "Marketing", List.of("Analytics", "SEO", "Content", "Social Media", "SQL")
        );
    }
}
