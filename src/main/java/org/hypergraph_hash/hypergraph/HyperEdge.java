package org.hypergraph_hash.hypergraph;

import org.hypergraph_hash.operations.Combinatorics;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.hypergraph_hash.operations.ArrayOperations.arrayIndexOf;

public class HyperEdge {
  private final int[] vertices;

  public HyperEdge(int... vertices) {
    this.vertices = Arrays.copyOf(vertices, vertices.length);
    Arrays.sort(this.vertices);

    for (int i = 1; i < this.vertices.length; i++) {
      if (this.vertices[i - 1] == this.vertices[i]) {
        throw new IllegalArgumentException("Duplicate vertices in edge");
      }
    }
  }

  public static HyperEdge of(int... vertices) {
    return new HyperEdge(vertices);
  }


  public static HyperEdge getByIndex(int bitIndex, int verticesCount, int edgeDimension) {
    return new HyperEdge(Combinatorics
            .getCombinationByOrdinal(verticesCount, edgeDimension, bitIndex));
  }

  public int getIndex(int verticesCount) {
    return Combinatorics.getCombinationOrdinal(verticesCount, vertices).intValueExact();
  }


  public int getVertex(int index) {
    return vertices[index];
  }

  public int dimension() {
    return vertices.length;
  }

  public int maxVertex() {
    return vertices[vertices.length - 1];
  }

  public boolean contains(int vertex) {
    return arrayIndexOf(vertices, vertex) != -1;
  }


  public IntStream stream() {
    return Arrays.stream(vertices);
  }
}
