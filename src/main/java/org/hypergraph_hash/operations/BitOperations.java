package org.hypergraph_hash.operations;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BitOperations {
  private BitOperations() {}

  public static byte bitAt(int bitIndex, byte value) {
    return (byte) ((Byte.toUnsignedInt(value) >>> (7 - bitIndex % 8)) & 1);
  }

  public static byte bitAt(int bitIndex, final byte[] value) {
    return (byte) ((Byte.toUnsignedInt(value[bitIndex / 8]) >>> (7 - bitIndex % 8)) & 1);
  }

  public static byte zeroMaskLow(int bitCount) {
    return (byte) (-(1 << bitCount));
  }

  public static byte zeroMaskHigh(int bitCount) {
    return (byte) ((1 << (8 - bitCount)) - 1);
  }

  public static byte[] concatByteArrays(byte[] arr1, byte[] arr2) {
    byte[] res = new byte[arr1.length + arr2.length];

    System.arraycopy(arr1, 0, res, 0, arr1.length);
    System.arraycopy(arr2, 0, res, arr1.length, arr2.length);

    return res;
  }

  public static byte[] xor(byte[] arr1, byte[] arr2) {
    int len = arr1.length;
    byte[] res = new byte[len];

    for (int i = 0; i < len; i++) {
      res[i] = (byte) (arr1[i] ^ arr2[i]);
    }

    return res;
  }

  public static void xorInPlace(byte[] out, int outFrom, byte[] in, int inFrom, int count) {
    for (int i = 0; i < count; i++) {
      out[outFrom + i] ^= in[inFrom + i];
    }
  }

  public static void xorIncompleteInPlace(byte[] out, int outFrom, byte[] in, int inFrom, int count) {
    count = Math.min(count, in.length - inFrom);

    for (int i = 0; i < count; i++) {
      out[outFrom + i] ^= in[inFrom + i];
    }
  }

  public static long zeroMask(int bitCount) {
    if (bitCount == 64) {
      return -1;
    }

    return (1L << bitCount) - 1;
  }

  public static long leftRotation(long x, long y, int size) {
    y %= size;

    long res = x << y | (x >>> (size - y));
    if (size == 64) {
      return res;
    }

    return res & zeroMask(size);
  }

  public static int leftRotation(int x, int y) {
    return (int) leftRotation(x, y, 32);
  }

  public static long rightRotation(long x, long y, int size) {
    y %= size;

    long res = x >>> y | (x << (size - y));
    if (size == 64) {
      return res;
    }

    return res & zeroMask(size);
  }

  public static int rightRotation(int x, int y) {
    return (int) rightRotation(x, y, 32);
  }

  public static long getBits(long x, int left, int right) {
    int leftShift = 63 - left;

    return x << (leftShift) >>> (right + leftShift);
  }

  public static byte[] bitChanging(byte[] arr, int i) {
    byte[] res = new byte[arr.length];
    System.arraycopy(arr, 0, res, 0, arr.length);

    byte mask = (byte) (1 << i % 8);
    res[arr.length - 1 - i / 8] ^= mask;

    return res;
  }

  public static int bitCount(byte[] arr) {
    int count = 0;

    for (byte b : arr) {
      count += Integer.bitCount(b & 0xFF);
    }

    return count;
  }

  public static int differentBitsCount(byte[] arr1, byte[] arr2) {
    return bitCount(xor(arr1, arr2));
  }

  public static String bytesToBinary(byte[] bytes) {
    return IntStream.range(0, bytes.length)
            .mapToObj(i -> String.format("%8s",
                            Integer.toBinaryString(bytes[i] & 0xFF))
                    .replace(' ', '0'))
            .collect(Collectors.joining(" "));
  }
}
