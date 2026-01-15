package org.hypergraph_hash.utilities;

import java.security.SecureRandom;

public class CryptoUtilities {
  private CryptoUtilities() {}

  private static final byte[] CONSTANTS = {
          (byte) 0x18, (byte) 0xC8, (byte) 0x65, (byte) 0x55, (byte) 0xB1, (byte) 0xB4, (byte) 0x6F, // pi
          (byte) 0xE0, (byte) 0xD9, (byte) 0xDB, (byte) 0x51, (byte) 0xB8, (byte) 0x20, (byte) 0xF1, // e
          (byte) 0x23, (byte) 0xED, (byte) 0x75, (byte) 0x58, (byte) 0x16, (byte) 0x50, (byte) 0x3A  // ф
  };

  public static byte[] getHashIV(int length) {
    byte[] iv = new byte[length];

    for (int i = 0; i < length; i++) {
      iv[i] = CONSTANTS[i % CONSTANTS.length];
    }

    return iv;
  }

  public static byte[] genIV(int blockSize) {
    byte[] iv = new byte[blockSize];
    new SecureRandom().nextBytes(iv);

    return iv;
  }
}
