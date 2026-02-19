package org.hypergraph_hash.testing_utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Print {
  public static void detailPrint(String message, boolean detailedPrint) {
    if (detailedPrint) {
      System.out.print(message);
    }
  }

  public static void detailPrintln(String message, boolean detailedPrint) {
    if (detailedPrint) {
      System.out.println(message);
    }
  }

  public static void printSortedHashCountMap(Map<String, AtomicInteger> hashCountMap) {
    System.out.println("\n=== HASHES ===\n");

    List<Map.Entry<String, AtomicInteger>> sortedEntries = new ArrayList<>(hashCountMap.entrySet());
    sortedEntries.sort((e1, e2)
            -> Integer.compare(e2.getValue().get(), e1.getValue().get()));

    for (var entry : sortedEntries) {
      System.out.println(entry.getKey() + ": " + entry.getValue());
    }
  }
}
