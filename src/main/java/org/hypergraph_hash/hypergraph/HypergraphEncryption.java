package org.hypergraph_hash.hypergraph;

import org.hypergraph_hash.SymmetricEncryption;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

import static org.hypergraph_hash.operations.BitOperations.xorInPlace;
import static org.hypergraph_hash.utilities.Validation.*;

public class HypergraphEncryption implements SymmetricEncryption {
  private final int blockSize;
  private final int smallBlockSize; //TODO Bit

  private final int[][] hypergraphAdjacencyLists;

  public HypergraphEncryption(HomogenousHypergraph key, int smallBlockSize) {
    validatePositive(smallBlockSize, "smallBlockSize");

    this.smallBlockSize = smallBlockSize;
    this.blockSize = smallBlockSize * key.getVerticesCount();

    this.hypergraphAdjacencyLists = new int[key.getVerticesCount() - key.getEdgeDimension() + 1][];
    for (int i = 0; i < hypergraphAdjacencyLists.length; i++) {
      int vertex = i;

      hypergraphAdjacencyLists[i] = key.getVerticesAdjacentTo(vertex)
              .filter(incidentVertex -> incidentVertex > vertex)
              .toArray();
    }
  }

  @Override
  public byte[] encryption(byte[] text) {
    return transform(text, IntUnaryOperator.identity());
  }

  @Override
  public byte[] decryption(byte[] text) {
    return transform(text, i -> hypergraphAdjacencyLists.length - 1 - i);
  }

  @Override
  public int getBlockSize() {
    return blockSize;
  }


  private byte[] transform(byte[] text, IntUnaryOperator vertexSelector) {
    validateNotNull(text, "Message for encryption");
    validateEquals(text.length, blockSize, "text.length", "blockSize");

    text = Arrays.copyOf(text, text.length);

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
