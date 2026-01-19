package org.hypergraph_hash.testHash;

import org.hypergraph_hash.GaloisHypergraphHash;
import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.HyperEdge;
import org.hypergraph_hash.statistics.Statistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GaloisHypergraphHashTest {
  private static final Random random = new Random();

  private static final int MAX_MESSAGE_LEN = 1000;
  private static final int MESSAGES_COUNT = 100_000;

  private static final int HASH_LENGTH = 1;

  private static final Map<String, AtomicInteger> hashCountMap = new ConcurrentHashMap<>();
  private static final AtomicInteger totalMessages = new AtomicInteger(0);


  @ParameterizedTest
  @MethodSource("messageProvider")
  void stabilityTest(byte[] message) {
    // SETUP
    var key = HomogenousHypergraph.ofEdges(
            HyperEdge.of(0, 3, 4),
            HyperEdge.of(2, 3, 4),
            HyperEdge.of(1, 2, 3),
            HyperEdge.of(0, 1, 5)
    );

    // EXECUTION
    var hashAlg = new GaloisHypergraphHash(key, HASH_LENGTH);
    var hashAlg2 = new GaloisHypergraphHash(key, HASH_LENGTH);

    var hash = hashAlg.hash(message);
    var repeatedHash = hashAlg.hash(message);
    var hash2 = hashAlg2.hash(message);

    // ASSERTION
    assertThat(hash).isEqualTo(repeatedHash).isEqualTo(hash2);
  }

  @Test
  void testNull() {
    // SETUP
    var key = HomogenousHypergraph.ofEdges(
            HyperEdge.of(0, 3, 4),
            HyperEdge.of(2, 3, 4),
            HyperEdge.of(1, 2, 3),
            HyperEdge.of(0, 1, 5)
    );

    // EXECUTION
    var hashAlg = new GaloisHypergraphHash(key, HASH_LENGTH);
    var hash = hashAlg.hash(null);

    // ASSERTION
    assertThat(hash)
            .isNotNull()
            .containsOnly((byte) 0);
  }

  @ParameterizedTest
  @MethodSource("messageProvider")
  void distributionTest(byte[] message) {
    // SETUP
    var key = HomogenousHypergraph.ofEdges(
            HyperEdge.of(0, 3, 4),
            HyperEdge.of(2, 3, 4),
            HyperEdge.of(1, 2, 3),
            HyperEdge.of(0, 1, 5)
    );

    // EXECUTION
    var hashAlg = new GaloisHypergraphHash(key, HASH_LENGTH);
    var hash = hashAlg.hash(message);

    String hashString = bytesToHex(hash);
    hashCountMap.computeIfAbsent(hashString, _ -> new AtomicInteger(0))
            .incrementAndGet();

    totalMessages.incrementAndGet();

    // ASSERTION
    assertThat(hash).isNotEmpty();
  }



  @AfterAll
  static void statistics() {
    if (hashCountMap.isEmpty()) {
      System.out.println("Нет данных для статистики");
      return;
    }

    var stats = new  Statistics(totalMessages.get(), HASH_LENGTH, hashCountMap);

    stats.print();

    if (HASH_LENGTH == 1) {
      printSortedHashCountMap();
    }
  }


  // region Providers

  static Stream<Arguments> messageProvider() {
    return Stream.generate(() -> {
              int length = random.nextInt(MAX_MESSAGE_LEN) + 1;
              byte[] message = new byte[length];

              random.nextBytes(message);

              return message;
            }).map(Arguments::of)
            .limit(MESSAGES_COUNT);
  }

  // endregion


  // region utilities

  private static String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder(2 * bytes.length);

    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);

      if (hex.length() == 1) {
        hexString.append('0');
      }

      hexString.append(hex);
    }

    return hexString.toString();
  }

  private static void printSortedHashCountMap() {
    System.out.println("\n=== HASHES ===\n");

    List<Map.Entry<String, AtomicInteger>> sortedEntries = new ArrayList<>(hashCountMap.entrySet());
    sortedEntries.sort((e1, e2)
                    -> Integer.compare(e2.getValue().get(), e1.getValue().get()));

    for (var entry : sortedEntries) {
      System.out.println(entry.getKey() + ": " + entry.getValue());
    }
  }

  // endregion
}
