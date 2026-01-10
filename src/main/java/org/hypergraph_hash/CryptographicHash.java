package org.hypergraph_hash;

public interface CryptographicHash {
  byte[] hash(byte[] input);

  int getHashLength();

  String getAlgorithmName();
}
