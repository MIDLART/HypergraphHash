package org.hypergraph_hash.test_hash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.HyperEdge;
import org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hypergraph_hash.operations.BitOperations.*;

class GaloisHypergraphTransformTest {
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
    var hashAlg = new GaloisHypergraphTransform(key);
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

        System.out.println("!!!{" + i + "}");
      }
      System.out.println("hash: " + (int) percent + "%");
    }

    double averagePercent = percentSum / (message.length * 8);
    System.out.println("Average percent: " + averagePercent);
    System.out.println("Outlier count: " + outlierCount);

    assertThat(averagePercent).isGreaterThan(49);
    assertThat(outlierCount).isZero();
  }
}
