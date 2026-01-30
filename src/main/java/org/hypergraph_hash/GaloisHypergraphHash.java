package org.hypergraph_hash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform;
import org.hypergraph_hash.tables.SBox;
import org.hypergraph_hash.operations.GaloisFieldOperations;

import static org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform.GF8_IRREDUCIBLE;
import static org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform.GF8_SIZE;
import static org.hypergraph_hash.operations.BitOperations.*;

public class GaloisHypergraphHash extends MerkleDamgardConstruction {
  public GaloisHypergraphHash(HomogenousHypergraph key, int hashLength) {
    super(new GaloisHypergraphTransform(key), hashLength);
  }

  @Override
  protected byte[] compressionFunction(byte[] inputBlock, byte[] prevHash) {
    byte[] res = blockTransform.encryption(inputBlock);

    for (int i = 0; i < prevHash.length; i++) {
      int a = res[i];
      int b = prevHash[i];

      if (a == 0) {
        a = zeroReplacement(i, b, prevHash.length);
      }

      if (b == 0) {
        b = zeroReplacement(i, a, prevHash.length);
      }

      res[i] = (byte) GaloisFieldOperations.mult(a, b, GF8_IRREDUCIBLE, GF8_SIZE);
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
    int replacement = SBox.getKuznyechik(input & 0xFF);

    if (replacement == 0) {
      replacement = SBox.getKuznyechik((rightRotation(input ^ 0x5A, shift ^ i)) & 0xFF) | 1;
    }

    return replacement;
  }
}
