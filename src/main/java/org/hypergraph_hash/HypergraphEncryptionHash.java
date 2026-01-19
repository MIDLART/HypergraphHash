package org.hypergraph_hash;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.transform.HypergraphEncryption;

public class HypergraphEncryptionHash extends MerkleDamgardConstruction{
  public HypergraphEncryptionHash(HomogenousHypergraph key, int smallBlockSize, int hashLength) {
    super(new HypergraphEncryption(key, smallBlockSize), hashLength);
  }

  @Override
  protected byte[] compressionFunction(byte[] inputBlock, byte[] prevHash) {
    return new byte[0];
  }

  @Override
  protected byte[] finalisationFunction(byte[] input) {
    return new byte[0];
  }

  @Override
  public String getAlgorithmName() {
    return "HypergraphEncryptionHash";
  }
}
