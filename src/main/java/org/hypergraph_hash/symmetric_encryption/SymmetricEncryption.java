package org.hypergraph_hash.symmetric_encryption;

public interface SymmetricEncryption {
  byte[] encryption(byte[] text);

  byte[] decryption(byte[] text);

  int getBlockSize();
}
