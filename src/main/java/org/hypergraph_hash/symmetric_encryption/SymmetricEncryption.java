package org.hypergraph_hash.symmetric_encryption;

import org.hypergraph_hash.hypergraph.transform.BlockTransform;

public interface SymmetricEncryption extends BlockTransform {
  byte[] encryption(byte[] text);

  byte[] decryption(byte[] text);

  int getBlockSize();
}
