package org.hypergraph_hash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform;
import org.hypergraph_hash.operations.GaloisFieldOperations;

import static org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform.GF8_IRREDUCIBLE;
import static org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform.GF8_SIZE;
import static org.hypergraph_hash.operations.BitOperations.xorIncompleteInPlace;

public class GaloisHypergraphHash extends MerkleDamgardConstruction {
  public GaloisHypergraphHash(HomogenousHypergraph key, int hashLength) {
    super(new GaloisHypergraphTransform(key), hashLength);
  }

  @Override
  protected byte[] compressionFunction(byte[] inputBlock, byte[] prevHash) {
    byte[] res = blockTransform.encryption(inputBlock);

    for (int i = 0; i < prevHash.length; i++) {
      byte m = (byte) GaloisFieldOperations.mult(res[i], prevHash[i], GF8_IRREDUCIBLE, GF8_SIZE);

      if (m == 0) { //TODO
        m = (byte) GaloisFieldOperations.mult((i + 1) * prevHash.length + i, prevHash[i], GF8_IRREDUCIBLE, GF8_SIZE);
      }

      res[i] = m;
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
}
