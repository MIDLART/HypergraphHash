package org.hypergraph_hash.operations;

import java.util.List;

public class ArrayOperations {
  public static byte[] listToArray(List<byte[]> list) {
    if (list == null || list.isEmpty()) return new byte[0];
    if (list.size() == 1) return list.getFirst();

    int blockSize = list.getFirst().length;
    int lastBlockSize = list.getLast().length;

    int size = (list.size() - 1) * blockSize + lastBlockSize;

    byte[] array = new byte[size];

    int i = 0;
    for (; i < list.size() - 1; i++) {
      System.arraycopy(
              list.get(i), 0,
              array, i * blockSize,
              blockSize);
    }

    if (lastBlockSize != 0) {
      System.arraycopy(
              list.getLast(), 0,
              array, i * blockSize,
              lastBlockSize);
    }

    return array;
  }
}
