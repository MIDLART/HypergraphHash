package org.hypergraph_hash.symmetric_encryption.modes;

import lombok.Getter;
import org.hypergraph_hash.symmetric_encryption.enums.PackingMode;

import java.security.SecureRandom;
import java.util.Arrays;

public class Packing {
  private final int size;
  @Getter
  private final PackingMode mode;

  public Packing(int blockSize, PackingMode mode) {
    size = blockSize;
    this.mode = mode;
  }

  public byte[] fill(byte[] input) {
    return switch (mode) {
      case ZEROS -> zerosFill(input);
      case ANSIX923 -> ansiX923Fill(input);
      case PKCS7 -> pkcs7Fill(input);
      case ISO10126 -> iso10126Fill(input);
      default -> input;
    };
  }

  public byte[] unpack(byte[] input) {
    return switch (mode) {
      case ZEROS -> zerosUnpack(input);
      case ANSIX923 -> ansiX923Unpack(input);
      case PKCS7 -> pkcs7Unpack(input);
      case ISO10126 -> iso10126Unpack(input);
      default -> input;
    };
  }

  /// Fill

  private byte[] zerosFill(byte[] input) {
    byte[] res = new byte[size];

    System.arraycopy(input, 0, res, 0, input.length);

    return res;
  }

  private byte[] ansiX923Fill(byte[] input) {
    byte[] res = new byte[size];
    int inputLen = input.length;

    System.arraycopy(input, 0, res, 0, inputLen);

    res[size - 1] = (byte) (size - inputLen);

    return res;
  }

  private byte[] pkcs7Fill(byte[] input) {
    byte[] res = new byte[size];
    int inputLen = input.length;

    System.arraycopy(input, 0, res, 0, inputLen);

    byte N = (byte) (size - inputLen);
    for (int i = inputLen; i < size; ++i) {
      res[i] = N;
    }

    return res;
  }

  private byte[] iso10126Fill(byte[] input) {
    byte[] res = new byte[size];
    int inputLen = input.length;

    System.arraycopy(input, 0, res, 0, inputLen);

    SecureRandom secureRandom = new SecureRandom();
    for (int i = inputLen; i < size - 1; ++i) {
      res[i] = (byte) (secureRandom.nextInt(256));
    }
    res[size - 1] = (byte) (size - inputLen);

    return res;
  }

  /// Unpack

  private byte[] zerosUnpack(byte[] input) {
    int i = size - 1;

    while (i >= 0 && input[i] == 0) {
      --i;
    }

    return Arrays.copyOfRange(input, 0, i + 1);
  }

  private byte[] ansiX923Unpack(byte[] input) {
    return lastUnpack(input);
  }

  private byte[] pkcs7Unpack(byte[] input) {
    return lastUnpack(input);
  }

  private byte[] iso10126Unpack(byte[] input) {
    return lastUnpack(input);
  }

  private byte[] lastUnpack(byte[] input) {
//    if (input[size - 1] < 0 || input[size - 1] > size) {
//      return input;
//    }

    int last = size - 1;
    return Arrays.copyOfRange(input, 0, size - input[last]);
  }
}
