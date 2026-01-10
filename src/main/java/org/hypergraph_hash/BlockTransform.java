package org.hypergraph_hash;

public interface BlockTransform {
  byte[] encryption(byte[] text);

  int getBlockSize();
}
