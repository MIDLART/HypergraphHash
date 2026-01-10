package org.hypergraph_hash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.transform.hash.GaloisHypergraphTransform;

public class GaloisHypergraphHash extends MerkleDamgardConstruction {
  protected GaloisHypergraphHash(HomogenousHypergraph key) {
    super(new GaloisHypergraphTransform(key));
  }

  @Override
  protected byte[] finalisationFunction(byte[] input) {
    return input;
  }

  @Override
  public String getAlgorithmName() {
    return "GaloisHypergraphHash";
  }
}
