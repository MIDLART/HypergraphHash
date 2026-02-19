package org.hypergraph_hash.test_hash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.HyperEdge;
import org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hypergraph_hash.data.Key.KEY32;
import static org.hypergraph_hash.operations.BitOperations.*;
import static org.hypergraph_hash.testing_utils.Print.detailPrint;
import static org.hypergraph_hash.testing_utils.Print.detailPrintln;

class GaloisHypergraphTransformTest {
  private static final Random random = new Random();

  private static final boolean DETAILED_PRINT = false;

  private static final int MESSAGE_LEN = 32;
  private static final int MESSAGES_COUNT = 1000;

  @Test
  void test() {
    // SETUP
    var key = HomogenousHypergraph.ofEdges(
            HyperEdge.of(0, 3, 4),
            HyperEdge.of(2, 3, 4),
            HyperEdge.of(1, 2, 3),
            HyperEdge.of(0, 1, 5)
    );
    byte[] message = new byte[] {(byte) 0x27, (byte) 0x00, (byte) 0xFF, (byte) 0x11, (byte) 0xAD, (byte) 0x08};

    // EXECUTION
    var hash = new GaloisHypergraphTransform(key);
    byte[] hashed = hash.encryption(message);

    // ASSERTION
    System.out.println(Arrays.toString(hashed));
    assertThat(hashed).isNotNull();
  }

  @Test
  void zeroTest() {
    // SETUP
    var key = HomogenousHypergraph.ofEdges(
            HyperEdge.of(0, 3, 4),
            HyperEdge.of(2, 3, 4),
            HyperEdge.of(1, 2, 3),
            HyperEdge.of(0, 1, 5)
    );
    var key2 = HomogenousHypergraph.ofEdges(
            HyperEdge.of(0, 1, 5),
            HyperEdge.of(2, 3, 5),
            HyperEdge.of(4, 0, 5),
            HyperEdge.of(2, 3, 5)
    );
    byte[] message = new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    // EXECUTION
    var hash = new GaloisHypergraphTransform(key);
    var hash2 = new GaloisHypergraphTransform(key2);
    byte[] hashed = hash.encryption(message);
    byte[] hashed2 = hash2.encryption(message);

    // ASSERTION
    System.out.println(Arrays.toString(hashed));
    System.out.println(Arrays.toString(hashed2));

    assertThat(hashed).isNotEqualTo(hashed2);
  }

  @Test
  void avalancheEffectTest() {
    // SETUP
    byte[] message = {
            (byte) 0x57, (byte) 0x30, (byte) 0x4A, (byte) 0xF5, (byte) 0x44, (byte) 0xA9, (byte) 0x00, (byte) 0xFF,
            (byte) 0xC4, (byte) 0x01, (byte) 0x43, (byte) 0xAA, (byte) 0x1B, (byte) 0xB9, (byte) 0xFC, (byte) 0x5D,
            (byte) 0xEE, (byte) 0xC5, (byte) 0xBC, (byte) 0x7D, (byte) 0x43, (byte) 0x6B, (byte) 0x4B, (byte) 0xD6,
            (byte) 0x17, (byte) 0xF3, (byte) 0x67, (byte) 0xF3, (byte) 0xF7, (byte) 0x11, (byte) 0x53, (byte) 0x7B,
    };

    // EXECUTION
    var hashAlg = new GaloisHypergraphTransform(KEY32);
    var hash = hashAlg.encryption(message);

    double percentSum = 0;
    int outlierCount = 0;

    // ASSERTION
    for (int i = 0; i < message.length * 8; i++) {
      byte[] bitChanged = bitChanging(message, i);

      int differentBits = differentBitsCount(hash, hashAlg.encryption(bitChanged));
      double percent = (double) differentBits / (message.length * 8) * 100;
      percentSum += percent;

      if (percent < 40) {
        outlierCount++;

        detailPrint("!!!{" + i + "}", DETAILED_PRINT);
      }
      detailPrintln("hash: " + (int) percent + "%", DETAILED_PRINT);
    }

    double averagePercent = percentSum / (message.length * 8);
    System.out.println("Average percent: " + averagePercent);
    System.out.println("Outlier count: " + outlierCount);

    assertThat(averagePercent).isGreaterThan(49);
    assertThat(outlierCount).isZero();
  }

  @ParameterizedTest
  @MethodSource("messageProvider")
  void bitIndependenceTest(byte[] message) {
    // EXECUTION
    var hashAlg = new GaloisHypergraphTransform(KEY32);
    var hash = hashAlg.encryption(message);

    int bitLen = message.length * 8;

    int[][] jointCount = new int[bitLen][bitLen];
    int[] changedCount = new int[bitLen];

    for (int i = 0; i < bitLen; i++) {
      byte[] bitChanged = bitChanging(message, i);
      byte[] differentBits = xor(hash, hashAlg.encryption(bitChanged));

      for (int j = 0; j < bitLen; j++) {
        if (bitAt(j, differentBits) != 0) {
          changedCount[j]++;

          for (int k = j + 1; k < bitLen; k++) {
            if (bitAt(k, differentBits) != 0) {
              jointCount[j][k]++;
            }
          }
        }
      }
    }

    double bic = getBIC(bitLen, changedCount, jointCount);
    detailPrintln(String.valueOf(bic), DETAILED_PRINT);

    // ASSERTION

    assertThat(bic).isLessThan(0.4);
  }


  // region Providers

  static Stream<Arguments> messageProvider() {
    return Stream.generate(() -> {
              byte[] message = new byte[MESSAGE_LEN];
              random.nextBytes(message);

              return message;
            }).map(Arguments::of)
            .limit(MESSAGES_COUNT);
  }

  // endregion

  // region utilities

  private static double getBIC(int bitLen, int[] changedCount, int[][] jointCount) {
    double bic = 0;

    for (int j = 0; j < bitLen; j++) {
      double pj = (double) changedCount[j] / bitLen;

      for (int k = j + 1; k < bitLen; k++) {
        double pk = (double) changedCount[k] / bitLen;
        double pjk = (double) jointCount[j][k] / bitLen;

        double numerator = pjk - pj * pk;
        double denominator = Math.sqrt(pj * (1 - pj) * pk * (1 - pk));

        if (denominator > 1e-9) {
          double corr = Math.abs(numerator / denominator);
          bic = Math.max(bic, corr);
        }
      }
    }
    return bic;
  }

  // endregion
}
