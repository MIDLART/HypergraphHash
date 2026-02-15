package org.hypergraph_hash.test_operations;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hypergraph_hash.operations.GaloisFieldOperations.*;

class OperationsTest {
  @Test
  void getGF8IrreducibleTest() {
    var irreducible = getIrreducible(8);

    List<String> bin = new ArrayList<>();
    List<String> hex = new ArrayList<>();

    for (var element : irreducible) {
      bin.add(Integer.toBinaryString(element));
      hex.add(Integer.toHexString(element).toUpperCase());
    }

    System.out.println(bin);
    System.out.println(hex);

    assertThat(irreducible).hasSize(30);
  }

  @Test
  void getGF8PrimitiveTest() {
    var primitive = getGF8Primitive();

    List<String> bin = new ArrayList<>();
    List<String> hex = new ArrayList<>();

    for (var element : primitive) {
      bin.add(Integer.toBinaryString(element));
      hex.add(Integer.toHexString(element).toUpperCase());
    }

    System.out.println(bin);
    System.out.println(hex);

    assertThat(primitive).hasSize(16);
  }
}
