package org.hypergraph_hash.data;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.HyperEdge;

public class Key {
  private Key() {}

  public static final HomogenousHypergraph KEY32 = HomogenousHypergraph.ofEdges(
          HyperEdge.of(0, 1, 7),
          HyperEdge.of(1, 8, 5),
          HyperEdge.of(2, 10, 3),
          HyperEdge.of(3, 7, 31),
          HyperEdge.of(4, 5, 6),
          HyperEdge.of(5, 25, 1),
          HyperEdge.of(6, 2, 9),
          HyperEdge.of(7, 4, 2),
          HyperEdge.of(8, 22, 1),
          HyperEdge.of(9, 6, 8),
          HyperEdge.of(10, 7, 9),
          HyperEdge.of(11, 0, 15),
          HyperEdge.of(12, 13, 2),
          HyperEdge.of(13, 11, 14),
          HyperEdge.of(14, 22, 3),
          HyperEdge.of(15, 5, 9),
          HyperEdge.of(16, 10, 22),
          HyperEdge.of(17, 12, 19),
          HyperEdge.of(18, 3, 25),
          HyperEdge.of(19, 16, 28),
          HyperEdge.of(20, 7, 31),
          HyperEdge.of(21, 9, 14),
          HyperEdge.of(22, 18, 27),
          HyperEdge.of(23, 4, 30),
          HyperEdge.of(24, 11, 26),
          HyperEdge.of(25, 8, 21),
          HyperEdge.of(26, 15, 29),
          HyperEdge.of(27, 6, 20),
          HyperEdge.of(28, 13, 23),
          HyperEdge.of(29, 17, 24),
          HyperEdge.of(30, 0, 31),
          HyperEdge.of(31, 19, 25)
  );
}
