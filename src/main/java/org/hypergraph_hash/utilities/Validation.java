package org.hypergraph_hash.utilities;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;

public class Validation {
  private Validation() {}


  public static final String EMPTY_INPUT = "Input is empty";


  public static <T extends Number> void validateNonNegative(T value, String name) {
    boolean valid = switch (value) {
      case BigInteger bigInteger -> bigInteger.signum() >= 0;
      case Number number -> number.longValue() >= 0;
    };

    if (!valid) {
      throw new IllegalArgumentException(name + " must be non-negative. value: " + value);
    }
  }

  public static <T extends Number> void validatePositive(T value, String name) {
    boolean valid = switch (value) {
      case BigInteger bigInteger -> bigInteger.signum() > 0;
      case Number number -> number.longValue() > 0;
    };

    if (!valid) {
      throw new IllegalArgumentException(name + " must be positive. value: " + value);
    }
  }

  public static <T extends Comparable<T>> void validateNonLess(T value, T limit, String name) {
    if (value.compareTo(limit) < 0) {
      throw new IllegalArgumentException("Value of " + name + " must be non-less than " + limit + ". value: " + value);
    }
  }

  public static void validateNotNull(Object value, String name) {
    Objects.requireNonNull(value, name + " must not be null.");
  }

  public static void validateNonEmpty(Collection<?> collection, String name) {
    if (collection.isEmpty()) {
      throw new IllegalArgumentException(name + " must not be empty.");
    }
  }

  public static <T> void validateEquals(T value1, T value2, String name1, String name2) {
    if (!Objects.equals(value1, value2)) {
      throw new IllegalArgumentException(name1 + " must be equal to " + name2 + ". " +
                                         "value1: " + value1 + ", value2: " + value2);
    }
  }

  public static <T extends Number> void validateNotZero(T value, String errorMessage) {
    boolean valid = switch (value) {
      case BigInteger bigInteger -> bigInteger.signum() != 0;
      case Number number -> number.longValue() != 0;
    };

    if (!valid) {
      throw new IllegalArgumentException(errorMessage);
    }
  }
}
