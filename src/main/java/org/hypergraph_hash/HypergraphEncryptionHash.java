package org.hypergraph_hash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.transform.HypergraphEncryption;

public class HypergraphEncryptionHash extends MerkleDamgardConstruction{
  public HypergraphEncryptionHash(HomogenousHypergraph key, int smallBlockSize) {
    super(new HypergraphEncryption(key, smallBlockSize));
  }
}
