package org.hypergraph_hash.symmetric_encryption.block;

public interface WriteBlock {
  void put(int index, byte[] block);
}
