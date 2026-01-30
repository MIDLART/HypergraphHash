package org.hypergraph_hash.hypergraph.transform.hash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.transform.HypergraphTransform;
import org.hypergraph_hash.tables.SBox;
import org.hypergraph_hash.operations.GaloisFieldOperations;

import java.util.function.IntUnaryOperator;

import static org.hypergraph_hash.operations.BitOperations.*;

public class GaloisHypergraphTransform extends HypergraphTransform {
  public static final int GF8_SIZE = 256;
  public static final int GF8_IRREDUCIBLE = 0x11B;

  private final int edgeDimension; // k
  private final int verticesCount; // n

//  public GaloisHypergraphTransform(HomogenousHypergraph key, int smallBlockSize) {
//    super(key, smallBlockSize);
//  }

  /// GF8
  public GaloisHypergraphTransform(HomogenousHypergraph key) {
    super(key, 1);

    edgeDimension = key.getEdgeDimension();
    verticesCount = key.getVerticesCount();
  }

  @Override
  public byte[] encryption(byte[] text) {
    return transform(
            validateAndTransform(text, IntUnaryOperator.identity()),
            i -> verticesCount - 1 - i
    );
  }

  //TODO для GF8 можно обойтись без массива. Можно расширить и для других с помощью int/long
  @Override
  protected byte[] transform(byte[] text, IntUnaryOperator vertexSelector) {
    for (int vertex = 0; vertex < hypergraphAdjacencyLists.length; vertex++) {
      int curVertexBlockIndex = vertexSelector.applyAsInt(vertex);

      int val = text[curVertexBlockIndex] & 0xFF;
      if (val == 0) {
        val = zeroReplacement(vertex, vertex ^ edgeDimension);
      }

      int smallBlock;

      for (int adjacentVertex : hypergraphAdjacencyLists[vertex]) {
        int blockIndex = vertexSelector.applyAsInt(adjacentVertex);
        smallBlock = SBox.getAES((text[blockIndex]) & 0xFF);

        if (smallBlock == 0) {
          smallBlock = zeroReplacement(vertex, adjacentVertex);
        }

        val = GaloisFieldOperations.mult(val, smallBlock, GF8_IRREDUCIBLE, GF8_SIZE);
      }

      smallBlock = text[curVertexBlockIndex] & 0xFF;
      if (smallBlock == 0) {
        smallBlock = zeroReplacement(vertex + edgeDimension, vertex);
      }

      text[curVertexBlockIndex] = (byte) GaloisFieldOperations.mult(val, smallBlock, GF8_IRREDUCIBLE, GF8_SIZE);

      for (int adjacentVertex : hypergraphAdjacencyLists[vertex]) {
        int blockIndex = vertexSelector.applyAsInt(adjacentVertex);
        smallBlock = text[blockIndex] & 0xFF;

        if (smallBlock == 0) {
          smallBlock = zeroReplacement(vertex, adjacentVertex);
        }

        text[blockIndex] = (byte) GaloisFieldOperations.mult(val, smallBlock, GF8_IRREDUCIBLE, GF8_SIZE);
      }
    }

    return text;
  }

  private int zeroReplacement(int i, int j) {
    i++;
    j++;

    int input = (i * edgeDimension) ^ j
            ^ leftRotation(i * edgeDimension, edgeDimension)
            ^ rightRotation(j, edgeDimension - 1);

    int replacement = SBox.getAES(input & 0xFF);

    if (replacement == 0) {
      replacement = SBox.getAES((
              rightRotation(input, i * j * edgeDimension + 1) ^ leftRotation(input, i + j + 1)) & 0xFF);
    }

    if (replacement == 0) {
      replacement = ((i * 10 + j + 1) & 0xFF) | 1;
    }

    return replacement;
  }
}
