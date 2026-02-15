package org.hypergraph_hash.operations;

import java.util.ArrayList;
import java.util.List;

public class GaloisFieldOperations {
  private GaloisFieldOperations() {}

  public static final int GF8_SIZE = 256;

  public static int add(int a, int b) {
    return a ^ b;
  }

  public static int sub(int a, int b) {
    return a ^ b;
  }

  public static int mult(int a, int b, int irreduciblePolynomial, int fieldSize) {
    int res = 0;

    while (b != 0) {
      if ((b & 1) != 0) {
        res ^= a;
      }
      a <<= 1;

      if (a >= fieldSize) {
        a ^= irreduciblePolynomial;
      }
      b >>>= 1;
    }

    return res;
  }

  public static int pow(int a, int exp, int irreduciblePolynomial, int fieldSize) {
    if (a == 0) {
      return 0;
    }

    int res = 1;

    while (exp > 0) {
      if ((exp & 1) == 1) {
        res = mult(res, a, irreduciblePolynomial, fieldSize);
      }

      a = mult(a, a, irreduciblePolynomial, fieldSize);
      exp >>>= 1;
    }

    return res;
  }

  public static boolean isIrreducible(int polynomial) {
    int degree = Integer.SIZE - Integer.numberOfLeadingZeros(polynomial) - 1;

    for (int i = 2; i < (2 << (int) Math.ceil(degree / 2.0)); i++) {
      if (isPolynomialDivided(polynomial, i)) {
        return false;
      }
    }

    return true;
  }

  public static List<Integer> getIrreducible(int degree) {
    List<Integer> polynomials = new ArrayList<>();

    int min = (1 << degree) + 1;
    int max = (1 << (degree + 1)) - 1;

    for (int polynomial = min; polynomial < max; polynomial++) {
      if (isIrreducible(polynomial)) {
        polynomials.add(polynomial);
      }
    }

    return polynomials;
  }

  public static boolean isGF8Primitive(int polynomial) {
    int x = 2;
    int order = 255;

    int[] primeFactors = {3, 5, 17};

    if (pow(x, 255, polynomial, 256) != 1) {
      return false;
    }

    for (int p : primeFactors) {
      int exp = order / p;

      if (pow(x, exp, polynomial, 256) == 1) {
        return false;
      }
    }

    return true;
  }

  public static List<Integer> getGF8Primitive() {
    List<Integer> polynomials = getIrreducible(8);

    for (int i = polynomials.size() - 1; i >= 0; i--) {
      if (!isGF8Primitive(polynomials.get(i))) {
        polynomials.remove(i);
      }
    }

    return polynomials;
  }

  private static boolean isPolynomialDivided(int dividend, int divisor) {
    if (divisor == 0) {
      throw new ArithmeticException("division by zero");
    }

    int dividendDegree = Integer.SIZE - Integer.numberOfLeadingZeros(dividend) - 1;
    int divisorDegree = Integer.SIZE - Integer.numberOfLeadingZeros(divisor) - 1;

    while (dividendDegree >= divisorDegree) {
      dividend = sub(dividend, divisor << (dividendDegree - divisorDegree));
      dividendDegree = Integer.SIZE - Integer.numberOfLeadingZeros(dividend) - 1;
    }

    return dividend == 0;
  }
}
