package org.hypergraph_hash.hypergraph.transform.hash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.transform.HypergraphTransform;
import org.hypergraph_hash.operations.GaloisFieldOperations;

import java.util.function.IntUnaryOperator;

public class GaloisHypergraphTransform extends HypergraphTransform {
  public static final int GF8_SIZE = 256;
  public static final int GF8_IRREDUCIBLE = 0x11B;

//  public GaloisHypergraphTransform(HomogenousHypergraph key, int smallBlockSize) {
//    super(key, smallBlockSize);
//  }

  /// GF8
  public GaloisHypergraphTransform(HomogenousHypergraph key) {
    super(key, 1);
  }


  //TODO для GF8 можно обойтись без массива. Можно расширить и для других с помощью int/long
  @Override
  protected byte[] transform(byte[] text, IntUnaryOperator vertexSelector) {
    for (int i = 0; i < hypergraphAdjacencyLists.length; i++) {
      int vertex = vertexSelector.applyAsInt(i);
      int val = 1;

      for (int adjacentVertex : hypergraphAdjacencyLists[vertex]) {
        int smallBlock = Byte.toUnsignedInt(text[adjacentVertex]);

        if (smallBlock == 0) {
          smallBlock = ((i + adjacentVertex) * adjacentVertex + 1) & 0xFF;
        }

        val = GaloisFieldOperations.mult(val, smallBlock, GF8_IRREDUCIBLE, GF8_SIZE);
      }

      text[vertex] = (byte) GaloisFieldOperations.mult(val, Byte.toUnsignedInt(text[vertex]), GF8_IRREDUCIBLE, GF8_SIZE);
      for (int adjacentVertex : hypergraphAdjacencyLists[vertex]) {
        text[adjacentVertex] = (byte) GaloisFieldOperations.mult(val, Byte.toUnsignedInt(text[adjacentVertex]), GF8_IRREDUCIBLE, GF8_SIZE);
      }
    }

    return text;
  }
}
