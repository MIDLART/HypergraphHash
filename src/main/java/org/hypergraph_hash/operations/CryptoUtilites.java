package org.hypergraph_hash.operations;

import java.security.SecureRandom;

public class CryptoUtilites {
  public static byte[] genIV(int blockSize) {
    byte[] IV = new byte[blockSize];
    new SecureRandom().nextBytes(IV);

    return IV;
  }
}
