package org.hypergraph_hash.utilities;

import java.security.SecureRandom;

public class CryptoUtilities {
  private CryptoUtilities() {}

  public static byte[] genIV(int blockSize) {
    byte[] iv = new byte[blockSize];
    new SecureRandom().nextBytes(iv);

    return iv;
  }
}
