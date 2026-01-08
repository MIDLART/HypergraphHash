package org.hypergraph_hash;

public interface SymmetricEncryption {
  byte[] encryption(byte[] text);

  byte[] decryption(byte[] text);

  int getBlockSize();
}
