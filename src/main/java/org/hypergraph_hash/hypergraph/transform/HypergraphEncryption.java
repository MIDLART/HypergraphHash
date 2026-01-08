package org.hypergraph_hash.hypergraph.transform;

import org.hypergraph_hash.SymmetricEncryption;
import org.hypergraph_hash.hypergraph.HomogenousHypergraph;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

import static org.hypergraph_hash.operations.BitOperations.xorInPlace;

public class HypergraphEncryption extends HypergraphTransform implements SymmetricEncryption {
  public HypergraphEncryption(HomogenousHypergraph key, int smallBlockSize) {
    super(key, smallBlockSize);
  }

  @Override
  protected byte[] transform(byte[] text, IntUnaryOperator vertexSelector) {
    byte[] tmp = new byte[smallBlockSize];

    for (int i = 0; i < hypergraphAdjacencyLists.length; i++) {
      Arrays.fill(tmp, (byte) 0);

      int vertex = vertexSelector.applyAsInt(i);

      for (int adjacentVertex : hypergraphAdjacencyLists[vertex]) {
        xorInPlace(tmp, 0, text, smallBlockSize * adjacentVertex, smallBlockSize);
      }
      if ((hypergraphAdjacencyLists[vertex].length & 1) == 1) {
        xorInPlace(tmp, 0, text, smallBlockSize * vertex, smallBlockSize);
      }

      xorInPlace(text, smallBlockSize * vertex, tmp, 0, smallBlockSize);
      for (int adjacentVertex : hypergraphAdjacencyLists[vertex]) {
        xorInPlace(text, smallBlockSize * adjacentVertex, tmp, 0, smallBlockSize);
      }
    }

    return text;
  }
}
