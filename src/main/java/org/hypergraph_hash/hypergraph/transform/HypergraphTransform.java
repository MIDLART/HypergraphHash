package org.hypergraph_hash.hypergraph.transform;

import org.hypergraph_hash.SymmetricEncryption;
import org.hypergraph_hash.hypergraph.HomogenousHypergraph;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

import static org.hypergraph_hash.utilities.Validation.*;

public abstract class HypergraphTransform implements SymmetricEncryption {
  protected final int blockSize;
  protected final int smallBlockSize; //TODO Bit

  protected final int[][] hypergraphAdjacencyLists;

  protected HypergraphTransform(HomogenousHypergraph key, int smallBlockSize) {
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
  public final byte[] encryption(byte[] text) {
    return validateAndTransform(text, IntUnaryOperator.identity());
  }

  @Override
  public final byte[] decryption(byte[] text) {
    return validateAndTransform(text, i -> hypergraphAdjacencyLists.length - 1 - i);
  }

  @Override
  public final int getBlockSize() {
    return blockSize;
  }


  protected final byte[] validateAndTransform(byte[] text, IntUnaryOperator vertexSelector) {
    validateNotNull(text, "Message for encryption");
    validateEquals(text.length, blockSize, "text.length", "blockSize");

    text = Arrays.copyOf(text, text.length);

    return transform(text, vertexSelector);
  }

  protected abstract byte[] transform(byte[] text, IntUnaryOperator vertexSelector);
}
