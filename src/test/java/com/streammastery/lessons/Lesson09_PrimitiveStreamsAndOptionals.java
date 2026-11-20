package com.streammastery.lessons;

import com.streammastery.model.Employee;
import com.streammastery.util.DataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ====================================================================================================
 * LESSON 09: PRIMITIVE STREAMS & OPTIONAL DEEP-DIVE
 * ====================================================================================================
 *
 * <h2>1. Primitive Specializations (IntStream, LongStream, DoubleStream)</h2>
 * <p>
 * In Java, an {@code Integer} object occupies 16 to 24 bytes in heap memory (object header + int field + padding),
 * whereas a primitive {@code int} is a raw 4-byte scalar stored directly in CPU registers or contiguous cache lines.
 * When processing millions of telemetry events, financial ticks, or metrics:
 * <ul>
 *   <li><b>Boxing Overhead:</b> Allocating millions of wrapper objects causes catastrophic GC thrashing.</li>
 *   <li><b>Cache Misses:</b> An array of {@code Integer} is an array of memory pointers scattered across RAM (pointer chasing),
 *       destroying L1/L2/L3 CPU cache spatial locality. Primitive arrays (e.g. {@code int[]}) are stored contiguously.</li>
 *   <li><b>Zero-Allocation Math:</b> {@code IntStream}, {@code LongStream}, and {@code DoubleStream} provide
 *       unboxed {@code sum()}, {@code min()}, {@code max()}, {@code average()}, and {@code summaryStatistics()}.</li>
 * </ul>
 * </p>
 *
 * <h2>2. The MAANG Optional Rules of Engagement:</h2>
 * <ul>
 *   <li><b>RULE 1: Never call {@code Optional.get()} without checking:</b> Calling `.get()` directly is simply
 *       a NullPointerException waiting to happen at 3 AM. Use {@code orElse()}, {@code orElseGet()}, or {@code orElseThrow()}.</li>
 *   <li><b>RULE 2: orElse() vs orElseGet() (The Classic Latency Bug):</b>
 *     <ul>
 *       <li>{@code opt.orElse(computeFallback())}: The argument is evaluated <b>EAGERLY</b> every time,
 *           even if the Optional already contains a valid value!</li>
 *       <li>{@code opt.orElseGet(() -> computeFallback())}: Evaluated <b>LAZILY</b> via a Supplier only when the Optional is empty.</li>
 *     </ul>
 *   </li>
 *   <li><b>RULE 3: Do NOT use Optional for class fields or method parameters:</b>
 *       {@code Optional} does not implement {@link java.io.Serializable} and adds an unnecessary heap wrapper.
 *       It is exclusively designed as a return type for methods that might return nothing.</li>
 *   <li><b>RULE 4: Stream of Optionals:</b> In Java 9+, use {@code stream.flatMap(Optional::stream)}
 *       to filter out {@code Optional.empty()} cleanly.</li>
 * </ul>
 */
public class Lesson09_PrimitiveStreamsAndOptionals {

    /**
     * SAMPLE 1: Primitive Performance: IntStream vs Boxed Stream
     * What this demonstrates:
     * - Creating unboxed ranges and computing sum without object allocations.
     * - Converting primitive streams back to Object streams using mapToObj() or boxed().
     */
    @Test
    @DisplayName("01. Primitive Performance: IntStream, LongStream, DoubleStream")
    void demonstratePrimitiveStreams() {
        // IntStream.rangeClosed avoids 1,000 Integer object allocations
        int sum = IntStream.rangeClosed(1, 1000).sum();
        assertThat(sum).isEqualTo(500500);

        // LongStream for large timestamps or high-cardinality IDs
        long factorial10 = LongStream.rangeClosed(1, 10).reduce(1L, (a, b) -> a * b);
        assertThat(factorial10).isEqualTo(3628800L);

        // mapToObj(): Transform primitive to custom Object without auto-boxing
        List<String> hexCodes = IntStream.range(10, 16)
                .mapToObj(Integer::toHexString)
                .toList();
        assertThat(hexCodes).containsExactly("a", "b", "c", "d", "e", "f");
    }

    /**
     * SAMPLE 2: The Latency Trap: orElse() vs orElseGet()
     * What this demonstrates:
     * - Why passing a method call into orElse() causes silent CPU burn.
     * - How orElseGet() preserves lazy evaluation semantics.
     */
    @Test
    @DisplayName("02. Critical Trap: Eager orElse() vs Lazy orElseGet()")
    void demonstrateOrElseVsOrElseGet() {
        List<String> auditLog = new ArrayList<>();
        Optional<String> cachedConfig = Optional.of("FastCachedValue");

        // orElse: auditLog is modified EVEN THOUGH cachedConfig is present! (Eager evaluation)
        String r1 = cachedConfig.orElse(fetchFromRemoteDatabase(auditLog));
        assertThat(auditLog).hasSize(1); // Unnecessary expensive call occurred!

        // orElseGet: Supplier is NOT invoked because cachedConfig is present! (Lazy evaluation)
        String r2 = cachedConfig.orElseGet(() -> fetchFromRemoteDatabase(auditLog));
        assertThat(auditLog).hasSize(1); // No new call!

        assertThat(r1).isEqualTo("FastCachedValue");
        assertThat(r2).isEqualTo("FastCachedValue");
    }

    private String fetchFromRemoteDatabase(List<String> log) {
        log.add("DB_NETWORK_CALL");
        return "RemoteValue";
    }

    /**
     * SAMPLE 3: Optional Monadic Chaining (map, filter, flatMap)
     * What this demonstrates:
     * - Treating Optional as a container with 0 or 1 element.
     * - Safe property navigation without deep nested null checks.
     */
    @Test
    @DisplayName("03. Optional Chaining: map(), filter(), flatMap()")
    void demonstrateOptionalChaining() {
        List<Employee> employees = DataFactory.getEmployees();

        // Safe pipeline: findFirst -> map to email -> filter domain -> orElse
        String domain = employees.stream()
                .filter(e -> "Data Science".equals(e.getDepartment()))
                .findFirst()
                .map(Employee::getEmail)
                .filter(email -> email.endsWith("@netflix.com"))
                .map(email -> email.substring(email.indexOf('@') + 1))
                .orElse("unknown.org");

        assertThat(domain).isEqualTo("netflix.com");
    }

    /**
     * SAMPLE 4: Java 9 Optional.stream() and Optional.or()
     * What this demonstrates:
     * - Flattening a stream of Optionals via flatMap(Optional::stream).
     * - Fallback chain of Optionals via .or().
     */
    @Test
    @DisplayName("04. Java 9 Features: Optional.stream() & Optional.or()")
    void demonstrateJava9OptionalFeatures() {
        List<Optional<String>> optionalList = List.of(
                Optional.of("KeyA"),
                Optional.empty(),
                Optional.of("KeyB"),
                Optional.empty()
        );

        // flatMap with Optional::stream filters out all empty Optionals:
        List<String> presentKeys = optionalList.stream()
                .flatMap(Optional::stream)
                .toList();
        assertThat(presentKeys).containsExactly("KeyA", "KeyB");

        // Optional.or(): Fallback across multiple Optional sources:
        Optional<String> l1Cache = Optional.empty();
        Optional<String> l2Cache = Optional.empty();
        Optional<String> database = Optional.of("ValueFromDB");

        Optional<String> resolved = l1Cache.or(() -> l2Cache).or(() -> database);
        assertThat(resolved).contains("ValueFromDB");
    }
}
