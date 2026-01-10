package org.hypergraph_hash.operations;

public class GaloisFieldOperations {
  private GaloisFieldOperations() {}

  public static byte add(byte a, byte b) {
    return (byte) (a ^ b);
  }

  public static byte sub(byte a, byte b) {
    return (byte) (a ^ b);
  }

  public static int mult(int a, int b, int generatorPolynomial, int fieldSize) {
    int res = 0;

    while (b != 0) {
      if ((b & 1) != 0) {
        res ^= a;
      }
      a <<= 1;

      if (a >= fieldSize) {
        a ^= generatorPolynomial;
      }
      b >>>= 1;
    }

    return res;
  }

  public static byte exp3(int a, int generatorPolynomial, int fieldSize) {
    if (a == 0) {
      return 0;
    }

    return (byte) mult(a, mult(a, a, generatorPolynomial, fieldSize), generatorPolynomial, fieldSize);
  }
}
