package org.hypergraph_hash.testHash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.HyperEdge;
import org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hypergraph_hash.operations.BitOperations.leftRotation;
import static org.hypergraph_hash.operations.BitOperations.rightRotation;

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
  void formulaTest() {
    int[] frequency = new int[256];
    int k = 3;

    for (int i = 0; i < 128; i++) {
      for (int adjacentVertex = 0; adjacentVertex < 128; adjacentVertex++) {
        int input = ((i * k) ^ adjacentVertex ^ leftRotation(i * k, k) ^ rightRotation(adjacentVertex, k - 1)) & 0xFF;

        frequency[input]++;
      }
    }

    for (int i = 0; i < 256; i++) {
      assertThat(frequency[i]).isPositive();
      System.out.println(i + " : " + frequency[i]);
    }
  }
}
