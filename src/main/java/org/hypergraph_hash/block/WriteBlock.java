package org.hypergraph_hash.block;

public interface WriteBlock {
  void put(int index, byte[] block);
}
