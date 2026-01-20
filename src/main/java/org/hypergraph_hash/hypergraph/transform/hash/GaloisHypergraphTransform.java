package org.hypergraph_hash.hypergraph.transform.hash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.transform.HypergraphTransform;
import org.hypergraph_hash.hypergraph.transform.hash.tables.SBox;
import org.hypergraph_hash.operations.GaloisFieldOperations;

import java.util.function.IntUnaryOperator;

import static org.hypergraph_hash.operations.BitOperations.leftRotation;
import static org.hypergraph_hash.operations.BitOperations.rightRotation;

public class GaloisHypergraphTransform extends HypergraphTransform {
  public static final int GF8_SIZE = 256;
  public static final int GF8_IRREDUCIBLE = 0x11B;

  private final int k;

//  public GaloisHypergraphTransform(HomogenousHypergraph key, int smallBlockSize) {
//    super(key, smallBlockSize);
//  }

  /// GF8
  public GaloisHypergraphTransform(HomogenousHypergraph key) {
    super(key, 1);

    k = key.getEdgeDimension();
  }


  //TODO для GF8 можно обойтись без массива. Можно расширить и для других с помощью int/long
  @Override
  protected byte[] transform(byte[] text, IntUnaryOperator vertexSelector) {
    for (int i = 0; i < hypergraphAdjacencyLists.length; i++) {
      int vertex = vertexSelector.applyAsInt(i);
      int val = 1;
      int smallBlock;

      for (int adjacentVertex : hypergraphAdjacencyLists[vertex]) {
        smallBlock = SBox.getAES(text[adjacentVertex] & 0xFF);

        if (smallBlock == 0) {
          smallBlock = zeroReplacement(i, adjacentVertex);
        }

        val = GaloisFieldOperations.mult(val, smallBlock, GF8_IRREDUCIBLE, GF8_SIZE);
      }

      smallBlock = text[vertex] & 0xFF;
      if (smallBlock == 0) {
        smallBlock = zeroReplacement(i, vertex);
      }

      text[vertex] = (byte) GaloisFieldOperations.mult(val, smallBlock, GF8_IRREDUCIBLE, GF8_SIZE);

      for (int adjacentVertex : hypergraphAdjacencyLists[vertex]) {
        smallBlock = text[adjacentVertex] & 0xFF;

        if (smallBlock == 0) {
          smallBlock = zeroReplacement(i, adjacentVertex);
        }

        text[adjacentVertex] = (byte) GaloisFieldOperations.mult(val, smallBlock, GF8_IRREDUCIBLE, GF8_SIZE);
      }
    }

    return text;
  }

  private int zeroReplacement(int i, int j) {
    int input = (i * k) ^ j ^ leftRotation(i * k, k) ^ rightRotation(j, k - 1);
    int replacement = SBox.getAES(input & 0xFF);

    if (replacement == 0) {
      replacement = SBox.getAES((rightRotation(input, i * j * k + 1) ^ leftRotation(input, i + j + 1)) & 0xFF);
    }

    return replacement;
  }
}
