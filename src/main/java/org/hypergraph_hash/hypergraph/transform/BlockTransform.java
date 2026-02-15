package org.hypergraph_hash.hypergraph.transform;

public interface BlockTransform {
  byte[] encryption(byte[] text);

  int getBlockSize();
}
