package org.hypergraph_hash.utilites;

import java.security.SecureRandom;

public class CryptoUtilites {
  private CryptoUtilites() {}

  public static byte[] genIV(int blockSize) {
    byte[] IV = new byte[blockSize];
    new SecureRandom().nextBytes(IV);

    return IV;
  }
}
