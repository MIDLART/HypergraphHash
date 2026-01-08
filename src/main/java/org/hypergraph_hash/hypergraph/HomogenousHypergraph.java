package org.hypergraph_hash.hypergraph;

import lombok.Getter;
import org.hypergraph_hash.operations.Combinatorics;

import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hypergraph_hash.utilities.Validation.*;

@Getter
public class HomogenousHypergraph {
  private final int edgeDimension;
  private final int verticesCount;
  private final int edgeMaxCount;

  private final BitSet edges;

  public HomogenousHypergraph(int edgeDimension, int verticesCount) {
    validateNonLess(edgeDimension, 2, "edgeDimension");
    validateNonNegative(verticesCount, "verticesCount");

    this.edgeDimension = edgeDimension;
    this.verticesCount = verticesCount;

    edgeMaxCount = Combinatorics.combinationCount(verticesCount, edgeDimension).intValueExact();
    edges = new BitSet(edgeMaxCount);
  }

  public static HomogenousHypergraph ofEdges(HyperEdge... edges) {
    return ofEdges(List.of(edges));
  }

  public static HomogenousHypergraph ofEdges(List<HyperEdge> edges) {
    validateNonEmpty(edges, "List edges");

    if (edges.stream().map(HyperEdge::dimension).distinct().count() != 1) {
      throw new IllegalArgumentException("Edges has different dimensions");
    }

    int edgeDimension = edges.getFirst().dimension();
    int verticesCount = edges.stream()
            .map(HyperEdge::maxVertex)
            .max(Integer::compareTo)
            .orElse(0)
            + 1;

    HomogenousHypergraph graph = new HomogenousHypergraph(edgeDimension, verticesCount);
    for (HyperEdge e : edges) {
      graph.addEdge(e);
    }

    return graph;
  }

  public void addEdge(HyperEdge edge) {
    if (edge.dimension() != edgeDimension) {
      throw new IllegalArgumentException("Edge dimension must be " + edgeDimension);
    }
    if (edge.maxVertex() >= verticesCount) {
      throw new IllegalArgumentException("One of vertices is not included in graph");
    }

    edges.set(edge.getIndex(verticesCount));
  }


  public Stream<HyperEdge> getEdgesIncidentTo(int vertex) {
    return edges.stream()
            .mapToObj(edgeIndex -> HyperEdge.getByIndex(edgeIndex, verticesCount, edgeDimension))
            .filter(edge -> edge.contains(vertex));
  }

  public IntStream getVerticesAdjacentTo(int vertex) {
    return getEdgesIncidentTo(vertex)
            .flatMapToInt(HyperEdge::stream)
            .distinct()
            .filter(v -> v != vertex);
  }
}
