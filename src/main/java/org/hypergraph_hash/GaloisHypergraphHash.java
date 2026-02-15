package org.hypergraph_hash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform;
import org.hypergraph_hash.tables.Irreducible;
import org.hypergraph_hash.operations.GaloisFieldOperations;

import static org.hypergraph_hash.operations.BitOperations.*;
import static org.hypergraph_hash.operations.GaloisFieldOperations.GF8_SIZE;
import static org.hypergraph_hash.tables.SBox.getSBox;

public class GaloisHypergraphHash extends MerkleDamgardConstruction {
  private final int gf8Irreducible;

  private final int edgeDimension; // k

  public GaloisHypergraphHash(HomogenousHypergraph key, int hashLength) {
    super(new GaloisHypergraphTransform(key), hashLength);

    edgeDimension = key.getEdgeDimension();
    gf8Irreducible = Irreducible.getGF8((edgeDimension + 16) % 30);
  }

  @Override
  protected byte[] compressionFunction(byte[] inputBlock, byte[] prevHash) {
    byte[] res = blockTransform.encryption(inputBlock);

    for (int i = 0; i < prevHash.length; i++) {
      int a = getSBox(edgeDimension + 1, res[i] & 0xFF);
      int b = getSBox(edgeDimension + 2, prevHash[i] & 0xFF);

      if (a == 0) {
        a = zeroReplacement(i, b, prevHash.length);
      }

      if (b == 0) {
        b = zeroReplacement(i, a, prevHash.length);
      }

      res[i] = (byte) GaloisFieldOperations.mult(a, b, gf8Irreducible, GF8_SIZE);
    }

    return res;
  }

  @Override
  protected byte[] finalisationFunction(byte[] input) {
    byte[] res = new byte[hashLength];

    int rounds = Math.ceilDiv(input.length, hashLength);

    for (int i = 0; i < rounds; i++) {
      xorIncompleteInPlace(res, 0, input, hashLength * i, hashLength);
    }

    return res;
  }

  @Override
  public String getAlgorithmName() {
    return "GaloisHypergraphHash";
  }

  private int zeroReplacement(int i, int other, int len) {
    int shift = (i ^ other ^ len) & 7;
    int rotated = leftRotation(other, shift);
    int input = ((i + 1) * len) ^ rotated ^ i;
    int replacement = getSBox(edgeDimension + 1, input & 0xFF);

    if (replacement == 0) {
      replacement = getSBox(edgeDimension + 1,
              (rightRotation(input ^ 0x5A, shift ^ i)) & 0xFF) | 1;
    }

    return replacement;
  }
}
