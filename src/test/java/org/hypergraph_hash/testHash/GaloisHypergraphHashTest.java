package org.hypergraph_hash.testHash;

import org.hypergraph_hash.GaloisHypergraphHash;
import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.HyperEdge;
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
    var hashAlg = new GaloisHypergraphHash(key);
    var hashAlg2 = new GaloisHypergraphHash(key);

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
    var hashAlg = new GaloisHypergraphHash(key);
    var hash = hashAlg.hash(null);

    // ASSERTION
    assertThat(hash)
            .isNotNull()
            .containsOnly((byte) 0);
  }

  @ParameterizedTest
  @MethodSource("messageProvider")
  void uniformDistributionTest(byte[] message) {
    // SETUP
    var key = HomogenousHypergraph.ofEdges(
            HyperEdge.of(0, 3, 4),
            HyperEdge.of(2, 3, 4),
            HyperEdge.of(1, 2, 3),
            HyperEdge.of(0, 1, 5)
    );

    // EXECUTION
    var hashAlg = new GaloisHypergraphHash(key);
    var hash = hashAlg.hash(message);

    String hashString = bytesToHex(hash);
    hashCountMap.computeIfAbsent(hashString, _ -> new AtomicInteger(0))
            .incrementAndGet();

    totalMessages.incrementAndGet();

    // ASSERTION

  }



  @AfterAll
  static void statistics() {
    var stats = getHashStatistics();

    if (stats.isEmpty()) {
      System.out.println("Нет данных для статистики");
      return;
    }

    System.out.printf("Всего сообщений: %,d%n",
            ((Number) stats.get("totalMessages")).longValue());
    System.out.printf("Уникальных хешей: %,d%n",
            ((Number) stats.get("uniqueHashes")).intValue());
    System.out.printf("Макс. коллизий на хеш: %,d%n",
            ((Number) stats.get("maxCollisions")).intValue());
    System.out.printf("Мин. коллизий на хеш: %,d%n",
            ((Number) stats.get("minCollisions")).intValue());
    System.out.printf("Всего дубликатов: %,d%n",
            ((Number) stats.get("totalDuplicates")).intValue());
    System.out.printf("Коэффициент коллизий: %.6f%%%n",
            ((Number) stats.get("collisionRate")).doubleValue());


//    for (Map.Entry<String, AtomicInteger> entry : hashCountMap.entrySet()) {
//      System.out.println(entry.getKey() + ": " + entry.getValue().get());
//    }

    List<Map.Entry<String, AtomicInteger>> sortedEntries = new ArrayList<>(hashCountMap.entrySet());
    sortedEntries.sort((e1, e2) -> Integer.compare(e2.getValue().get(), e1.getValue().get()));
    for (var entry : sortedEntries) {
      System.out.println(entry.getKey() + ": " + entry.getValue());
    }
  }

  static Map<String, Object> getHashStatistics() {
    Map<String, Object> stats = new HashMap<>();

    if (hashCountMap.isEmpty()) {
      return stats;
    }

    int maxCount = 0;
    int minCount = Integer.MAX_VALUE;
    int totalDuplicates = 0;

    for (AtomicInteger count : hashCountMap.values()) {
      int c = count.get();
      maxCount = Math.max(maxCount, c);
      minCount = Math.min(minCount, c);
      if (c > 1) {
        totalDuplicates += (c - 1);
      }
    }

    stats.put("totalMessages", totalMessages.get());
    stats.put("uniqueHashes", hashCountMap.size());
    stats.put("maxCollisions", maxCount);
    stats.put("minCollisions", minCount);
    stats.put("totalDuplicates", totalDuplicates);
    stats.put("collisionRate", (double) totalDuplicates / totalMessages.get() * 100);

    return stats;
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

  // endregion
}
