package org.hypergraph_hash;

public interface SymmetricEncryption {
  public byte[] encryption(byte[] text);

  public byte[] decryption(byte[] text);

  public int getBlockSize();
}
