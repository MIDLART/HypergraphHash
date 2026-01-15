package org.hypergraph_hash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform;
import org.hypergraph_hash.operations.GaloisFieldOperations;

import static org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform.GF8_IRREDUCIBLE;
import static org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform.GF8_SIZE;

public class GaloisHypergraphHash extends MerkleDamgardConstruction {
  public GaloisHypergraphHash(HomogenousHypergraph key) {
    super(new GaloisHypergraphTransform(key));
  }

  @Override
  protected byte[] compressionFunction(byte[] inputBlock, byte[] prevHash) {
    byte[] res = blockTransform.encryption(inputBlock);

    for (int i = 0; i < prevHash.length; i++) {
      byte m = (byte) GaloisFieldOperations.mult(res[i], prevHash[i], GF8_IRREDUCIBLE, GF8_SIZE);

      if (m != 0) {
        res[i] = m;
      }
    }

    return res;
  }

  @Override
  protected byte[] finalisationFunction(byte[] input) {
    byte[] res = new byte[1]; //TODO

    for (byte b : input) {
      res[0] = (byte) (res[0] ^ b);
    }

    return res;
  }

  @Override
  public String getAlgorithmName() {
    return "GaloisHypergraphHash";
  }
}
