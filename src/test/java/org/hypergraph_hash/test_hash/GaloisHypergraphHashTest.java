package org.hypergraph_hash.test_hash;

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
import static org.hypergraph_hash.operations.BitOperations.bitChanging;
import static org.hypergraph_hash.operations.BitOperations.differentBitsCount;

class GaloisHypergraphHashTest {
  private static final Random random = new Random();

  private static final boolean DETAILED_PRINT = false;

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

  @Test
  void avalancheEffectTest() {
    // SETUP
    var key = HomogenousHypergraph.ofEdges(
            HyperEdge.of(0, 1, 7),
            HyperEdge.of(1, 8, 5),
            HyperEdge.of(2, 10, 3),
            HyperEdge.of(3, 7, 31),
            HyperEdge.of(4, 5, 6),
            HyperEdge.of(5, 25, 1),
            HyperEdge.of(6, 2, 9),
            HyperEdge.of(7, 4, 2),
            HyperEdge.of(8, 22, 1),
            HyperEdge.of(9, 6, 8),
            HyperEdge.of(10, 7, 9),
            HyperEdge.of(11, 0, 15),
            HyperEdge.of(12, 13, 2),
            HyperEdge.of(13, 11, 14),
            HyperEdge.of(14, 22, 3),
            HyperEdge.of(15, 5, 9),
            HyperEdge.of(16, 10, 22),
            HyperEdge.of(17, 12, 19),
            HyperEdge.of(18, 3, 25),
            HyperEdge.of(19, 16, 28),
            HyperEdge.of(20, 7, 31),
            HyperEdge.of(21, 9, 14),
            HyperEdge.of(22, 18, 27),
            HyperEdge.of(23, 4, 30),
            HyperEdge.of(24, 11, 26),
            HyperEdge.of(25, 8, 21),
            HyperEdge.of(26, 15, 29),
            HyperEdge.of(27, 6, 20),
            HyperEdge.of(28, 13, 23),
            HyperEdge.of(29, 17, 24),
            HyperEdge.of(30, 0, 31),
            HyperEdge.of(31, 19, 25)
    );

    byte[] message = {
            (byte) 0x57, (byte) 0x30, (byte) 0x4A, (byte) 0xF5, (byte) 0x44, (byte) 0xA9, (byte) 0x00, (byte) 0xFF,
            (byte) 0xC4, (byte) 0x01, (byte) 0x43, (byte) 0xAA, (byte) 0x1B, (byte) 0xB9, (byte) 0xFC, (byte) 0x5D,
            (byte) 0xEE, (byte) 0xC5, (byte) 0xBC, (byte) 0x7D, (byte) 0x43, (byte) 0x6B, (byte) 0x4B, (byte) 0xD6,
            (byte) 0x17, (byte) 0xF3, (byte) 0x67, (byte) 0xF3, (byte) 0xF7, (byte) 0x11, (byte) 0x53, (byte) 0x7B,
    };

    // EXECUTION
    var hashAlg = new GaloisHypergraphHash(key, 32);
    var hash = hashAlg.hash(message);

    double hashPercentSum = 0;
    int outlierCount = 0;
    int criticalOutlierCount = 0;

    for (int i = 0; i < message.length * 8; i++) {
      byte[] bitChanged = bitChanging(message, i);

      int differentBits = differentBitsCount(hash, hashAlg.hash(bitChanged));
      double percent = (double) differentBits / (hash.length * 8) * 100;
      if (percent < 40) {
        outlierCount++;
        if (percent < 35) criticalOutlierCount++;

        detailPrint("!!!{" + i + "}");
      }
      detailPrintln("hash: " + (int) percent + "%");

      hashPercentSum += percent;
    }

    // ASSERTION
    double averagePercent = hashPercentSum / (message.length * 8);
    System.out.println("Average percent: " + averagePercent);
    System.out.println("Outlier count(<40%): " + outlierCount);
    System.out.println("Critical outlier count(<35%): " + criticalOutlierCount);

    assertThat(averagePercent).isGreaterThan(49);
    assertThat(criticalOutlierCount).isZero();
    assertThat(outlierCount).isLessThan(message.length * 8 / 100);
  }



  @AfterAll
  static void statistics() {
    if (hashCountMap.isEmpty()) {
      System.out.println("Нет данных для статистики");
      return;
    }

    var stats = new  Statistics(totalMessages.get(), HASH_LENGTH, hashCountMap);

    stats.print();

    if (DETAILED_PRINT) {
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
      String hex = Integer.toHexString(b & 0xFF);

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

  private static void detailPrint(String message) {
    if (DETAILED_PRINT) {
      System.out.print(message);
    }
  }

  private static void detailPrintln(String message) {
    if (DETAILED_PRINT) {
      System.out.println(message);
    }
  }

  // endregion
}
