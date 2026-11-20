package com.streammastery.exercises;

import com.streammastery.model.*;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ====================================================================================================
 * EXERCISE 10: ADVANCED FLATMAP & STREAM COMPOSITION (20 EXERCISES)
 * ====================================================================================================
 */
public class Exercise10_AdvancedFlatMapAndComposition {

    @Test
    @DisplayName("01 [★☆☆☆☆] Flatten List<List<String>> into a single list of names")
    void exercise01_flattenTeams() {
        List<List<String>> teams = DataFactory.getTeams();
        // TODO: flatMap to all member names
        List<String> members = null;

        assertThat(members).isNotNull();
        assertThat(members).hasSize(15);
    }

    @Test
    @DisplayName("02 [★☆☆☆☆] Concatenate two product lists and count total")
    void exercise02_concatProducts() {
        List<Product> listA = DataFactory.getProducts().subList(0, 5);
        List<Product> listB = DataFactory.getProducts().subList(5, 10);
        // TODO: Stream.concat(listA.stream(), listB.stream()).count()
        long count = 0;

        assertThat(count).isEqualTo(10);
    }

    @Test
    @DisplayName("03 [★★☆☆☆] Extract all unique product names across all orders")
    void exercise03_uniqueProductNamesOrders() {
        // TODO: FlatMap orders to items, map productName, distinct, sorted
        List<String> productNames = null;

        assertThat(productNames).isNotNull();
        assertThat(productNames).contains("MacBook Pro 16\"", "AirPods Pro 2", "USB-C Cable");
    }

    @Test
    @DisplayName("04 [★★☆☆☆] Flatten department skills map into a single sorted list of unique skills")
    void exercise04_flattenSkills() {
        Map<String, List<String>> skills = DataFactory.getDepartmentSkills();
        // TODO: Flatten skills.values() to unique sorted list
        List<String> distinctSkills = null;

        assertThat(distinctSkills).isNotNull();
        assertThat(distinctSkills).contains("Algorithms", "Java", "Python", "SQL");
    }

    @Test
    @DisplayName("05 [★★☆☆☆] Zip employees with rank numbers: '#1: Name', '#2: Name'")
    void exercise05_zipRanks() {
        List<Employee> top3 = DataFactory.getEmployees().subList(0, 3);
        // TODO: IntStream.range(0, 3).mapToObj(i -> "#" + (i + 1) + ": " + top3.get(i).getFullName()).toList()
        List<String> ranked = null;

        assertThat(ranked).isNotNull();
        assertThat(ranked).containsExactly("#1: Alice Johnson", "#2: Bob Smith", "#3: Carol Williams");
    }

    @Test
    @DisplayName("06 [★★★☆☆] Cartesian Cross Product: Generate all unique pairs of languages (lang1, lang2) where lang1 != lang2")
    void exercise06_languagePairs() {
        List<String> langs = List.of("Java", "Go", "Rust");
        // TODO: Generate pairs formatted as "lang1-lang2" where lang1 is not equal to lang2
        List<String> pairs = null;

        assertThat(pairs).isNotNull();
        assertThat(pairs).containsExactly("Java-Go", "Java-Rust", "Go-Java", "Go-Rust", "Rust-Java", "Rust-Go");
    }

    @Test
    @DisplayName("07 [★★★☆☆] Category frequency in orders: Map<Category, Count of items purchased>")
    void exercise07_categoryPurchaseFrequency() {
        // TODO: flatMap orders to items, group by item.getCategory(), count
        Map<String, Long> categoryFreq = null;

        assertThat(categoryFreq).isNotNull();
        assertThat(categoryFreq.get("Electronics")).isGreaterThanOrEqualTo(5L);
    }

    @Test
    @DisplayName("08 [★★★☆☆] Stream.concat 3 regional lists to find highest paid employee")
    void exercise08_concatRegionsHighestPaid() {
        List<Employee> sf = DataFactory.getEmployees().stream().filter(e -> "San Francisco".equals(e.getCity())).toList();
        List<Employee> seattle = DataFactory.getEmployees().stream().filter(e -> "Seattle".equals(e.getCity())).toList();
        List<Employee> cupertino = DataFactory.getEmployees().stream().filter(e -> "Cupertino".equals(e.getCity())).toList();

        // TODO: Concat all 3 streams, find max by salary
        Optional<Employee> maxEarner = Optional.empty();

        assertThat(maxEarner).isPresent();
        assertThat(maxEarner.get().getSalary()).isEqualTo(310000.0); // Ryan in Seattle
    }

    @Test
    @DisplayName("09 [★★★★☆] Shared skills with Engineering across other departments")
    void exercise09_departmentsSharingSkillsWithEngineering() {
        Map<String, List<String>> deptSkills = DataFactory.getDepartmentSkills();
        Set<String> engSkills = new HashSet<>(deptSkills.get("Engineering"));

        // TODO: Find all department names (other than "Engineering") that share at least one skill with Engineering
        List<String> sharingDepts = null;

        assertThat(sharingDepts).isNotNull();
        assertThat(sharingDepts).contains("Data Science"); // shares Python
    }

    @Test
    @DisplayName("10 [★★★★☆] Inverted Word Index from Sentences: Map<Word, Set<SentenceIndex>>")
    void exercise10_invertedIndex() {
        List<String> sentences = DataFactory.getSentences();
        // TODO: Tokenize words (lowercase, remove punctuation), map to sentence index, group into Map<String, Set<Integer>>
        Map<String, Set<Integer>> index = null;

        assertThat(index).isNotNull();
        assertThat(index.get("streams")).isNotEmpty();
    }

    @Test
    @DisplayName("11 [★★★★★] Join Orders to Employees: Match CustomerName to Employee FullName")
    void exercise11_joinOrdersToEmployees() {
        // Output List<String>: "Employee (Dept) placed Order #ID with total $Amount"
        // TODO: Join orders with DataFactory.getEmployees() on customerName == employee.getFullName()
        List<String> joinedRecords = null;

        assertThat(joinedRecords).isNotNull();
        assertThat(joinedRecords).isNotEmpty();
        assertThat(joinedRecords.get(0)).contains("Alice Johnson (Engineering) placed Order #1");
    }

    @Test
    @DisplayName("12 [★★★★★] Filtered Cartesian Product: Electronics Products x West Coast Cities")
    void exercise12_filteredCartesianJoin() {
        Set<String> westCoastCities = Set.of("San Francisco", "Seattle", "Cupertino", "Menlo Park", "Los Gatos");
        List<Product> electronics = DataFactory.getProducts().stream()
                .filter(p -> "Electronics".equals(p.getCategory()))
                .limit(2)
                .toList();

        // TODO: Generate "Product in City" for each electronic in westCoastCities
        List<String> pairs = null;

        assertThat(pairs).isNotNull();
        assertThat(pairs).hasSize(2 * westCoastCities.size());
    }

    @Test
    @DisplayName("13 [★☆☆☆☆] Flatten 2D integer array into 1D array")
    void exercise13_flatten2dArray() {
        int[][] matrix = {{1, 2}, {3, 4}, {5, 6}};
        // TODO: Arrays.stream(matrix).flatMapToInt(Arrays::stream).toArray()
        int[] flat = null;

        assertThat(flat).isNotNull();
        assertThat(flat).containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    @DisplayName("14 [★★☆☆☆] Concatenate 4 streams without deep recursion")
    void exercise14_concat4Streams() {
        Stream<String> a = Stream.of("A");
        Stream<String> b = Stream.of("B");
        Stream<String> c = Stream.of("C");
        Stream<String> d = Stream.of("D");

        // TODO: Stream.of(a, b, c, d).flatMap(s -> s).toList()
        List<String> allFour = null;

        assertThat(allFour).isNotNull();
        assertThat(allFour).containsExactly("A", "B", "C", "D");
    }

    @Test
    @DisplayName("15 [★★☆☆☆] Unpack nested Map<String, List<String>> entries")
    void exercise15_unpackNestedMap() {
        Map<String, List<String>> map = Map.of(
                "DeptA", List.of("Skill1", "Skill2"),
                "DeptB", List.of("Skill3")
        );
        // TODO: flatMap map.values() to all unique skills
        List<String> allSkills = null;

        assertThat(allSkills).isNotNull();
        assertThat(allSkills).containsExactlyInAnyOrder("Skill1", "Skill2", "Skill3");
    }

    @Test
    @DisplayName("16 [★★★☆☆] Zip two lists into Map<String, Integer>")
    void exercise16_zipToMap() {
        List<String> keys = List.of("One", "Two", "Three");
        List<Integer> values = List.of(1, 2, 3);

        // TODO: IntStream.range(0, 3).boxed().collect(Collectors.toMap(keys::get, values::get))
        Map<String, Integer> map = null;

        assertThat(map).isNotNull();
        assertThat(map.get("Two")).isEqualTo(2);
    }

    @Test
    @DisplayName("17 [★★★☆☆] Word Co-occurrence Pairs in Sentences")
    void exercise17_wordPairs() {
        String sentence = "java streams rock";
        // Words: [java, streams, rock]
        // Ordered pairs (i < j): "java-streams", "java-rock", "streams-rock"
        String[] words = sentence.split(" ");
        // TODO: Generate ordered pairs using nested IntStreams
        List<String> pairs = null;

        assertThat(pairs).isNotNull();
        assertThat(pairs).containsExactly("java-streams", "java-rock", "streams-rock");
    }

    @Test
    @DisplayName("18 [★★★★☆] Cartesian Triples: 3-way Cartesian Join")
    void exercise18_cartesianTriples() {
        List<String> colors = List.of("Red", "Blue");
        List<String> sizes = List.of("S", "M");
        List<String> genders = List.of("Men", "Women");

        // Generate combinations: "Color Size Gender" (2 x 2 x 2 = 8 combinations)
        // TODO: Triply nested flatMap
        List<String> skus = null;

        assertThat(skus).isNotNull();
        assertThat(skus).hasSize(8);
        assertThat(skus).contains("Red S Men", "Blue M Women");
    }

    @Test
    @DisplayName("19 [★★★★☆] Flatten Order item categories to distinct purchase combinations per order")
    void exercise19_orderCategoryCombinations() {
        // For each order, extract distinct categories sorted as a single comma-separated string
        // E.g. Order 1 has items in Electronics -> "Electronics"
        // Order 2 has Footwear and Clothing -> "Clothing, Footwear"
        // TODO: Stream orders, map items to distinct sorted categories joined by ", "
        List<String> categorySignatures = null;

        assertThat(categorySignatures).isNotNull();
        assertThat(categorySignatures).contains("Clothing, Footwear");
    }

    @Test
    @DisplayName("20 [★★★★★] Graph Adjacency List: Flatten All Reachable 2-Hop Nodes")
    void exercise20_twoHopGraphTraversal() {
        Map<String, List<String>> graph = Map.of(
                "A", List.of("B", "C"),
                "B", List.of("D"),
                "C", List.of("E", "F"),
                "D", List.of(),
                "E", List.of(),
                "F", List.of()
        );

        // Find all nodes reachable from "A" in exactly 2 hops:
        // A -> [B, C]; B -> [D]; C -> [E, F] => [D, E, F]
        // TODO: graph.get("A").stream().flatMap(neighbor -> graph.getOrDefault(neighbor, List.of()).stream()).toList()
        List<String> twoHopNodes = null;

        assertThat(twoHopNodes).isNotNull();
        assertThat(twoHopNodes).containsExactlyInAnyOrder("D", "E", "F");
    }
}
