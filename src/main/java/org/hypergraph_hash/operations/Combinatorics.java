package org.hypergraph_hash.operations;

import java.math.BigInteger;

import static org.hypergraph_hash.utilities.Validation.validateNonNegative;

public class Combinatorics {
  private Combinatorics() {}

  public static BigInteger factorial(int x) {
    BigInteger res = BigInteger.ONE;

    for (int i = 2; i <= x; i++) {
      res = res.multiply(BigInteger.valueOf(i));
    }

    return res;
  }

  public static BigInteger combinationCount(int n, int k) {
    validateNonNegative(k, "parameter k for combination count");
    validateNonNegative(k, "parameter n for combination count");

    if (k == 1) {
      return BigInteger.valueOf(n);
    }
    if (k == n) {
      return BigInteger.ONE;
    }
    if (k > n) {
      return BigInteger.ZERO;
    }

    BigInteger res = BigInteger.ONE;
    for (int i = Math.max(k, n - k) + 1; i <= n; i++) {
      res = res.multiply(BigInteger.valueOf(i));
    }

    return res.divide(factorial(Math.min(k, n - k)));
  }


  public static BigInteger getCombinationOrdinal(int n, int[] combination) {
    int k = combination.length;

    BigInteger combinationCount = combinationCount(n, k);
    for (int i = 0; i < k; ++i) {
      combinationCount = combinationCount.subtract(combinationCount(n - combination[i] - 1, k - i));
    }

    return combinationCount.subtract(BigInteger.ONE);
  }

  public static int[] getCombinationByOrdinal(int n, int k, long ordinal) {
    return getCombinationByOrdinal(n, k, BigInteger.valueOf(ordinal));
  }

  public static int[] getCombinationByOrdinal(int n, int k, BigInteger ordinal) {
    if (ordinal.compareTo(BigInteger.ZERO) < 0 || ordinal.compareTo(combinationCount(n, k)) >= 0) {
      return new int[0];
    }

    int[] combination = new int[k];
    int i = 0;
    int next = 0;

    while (k > 0) {
      BigInteger cc = combinationCount(n - 1, k - 1);
      if (ordinal.compareTo(cc) < 0) {
        combination[i++] = next;
        k--;
      } else {
        ordinal = ordinal.subtract(cc);
      }

      n--;
      next++;
    }

    return combination;
  }
}
