package org.hypergraph_hash.testHash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.HyperEdge;
import org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

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
    byte[] message = new byte[] {(byte) 0x27, (byte) 0x01, (byte) 0xFF, (byte) 0x11, (byte) 0xAD, (byte) 0x08};

    // EXECUTION
    var hash = new GaloisHypergraphTransform(key);
    byte[] hashed = hash.encryption(message);

    // ASSERTION
    System.out.println(Arrays.toString(hashed));
  }
}
