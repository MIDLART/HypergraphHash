package org.hypergraph_hash.testing_utils;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hypergraph_hash.utilities.Validation.validatePositive;

@Getter
public class Statistics {
  private final int totalMessages;
  private final int uniqueHashes;
  private final int hashSpace;

  private int maxCollisions;
  private int minCollisions;
  private final List<String> mostFrequentHashes = new ArrayList<>();
  private final List<String> mostRareHashes = new ArrayList<>();

  private double average;
  private int median;
  private double downDeviation;
  private double upDeviation;

  private double standardDeviation;
  private double cv;
  private double shannonEntropy;
  private double idealEntropy;
  private double giniIndex;
  private double chiSquared;
  private double kolmogorovSmirnov;

  public Statistics(int totalMessages, int hashLength, Map<String, AtomicInteger> hashCountMap) {
    validatePositive(totalMessages, "totalMessages");
    validatePositive(hashCountMap.size(), "hashCountMap.size");
    this.totalMessages = totalMessages;
    uniqueHashes = hashCountMap.size();
    hashSpace = (int) Math.pow(2, 8 * hashLength);

    var sortedEntries = getSortedEntries(hashCountMap);

    getHashStatistics(sortedEntries);
    getAverage();

    calculateMetrics(sortedEntries);
  }

  /// Print

  public void print() {
    System.out.println("\n=== СТАТИСТИКА ===\n");

    System.out.println("Всего сообщений: " + totalMessages);
    System.out.println("Уникальных хешей: " + uniqueHashes + "/" + hashSpace);
    System.out.println();
    System.out.println("Макс. одинаковых хешей: " + maxCollisions);
    System.out.println("Мин. одинаковых хешей: " + minCollisions);
    System.out.println("Самый частый хеш: " + mostFrequentHashes);
    System.out.println("Самый редкий хеш: " + mostRareHashes);
    System.out.println();
    System.out.println("В среднем сообщений на хеш: " + average);
    System.out.println("Медиана: " + median);
    System.out.println();
    System.out.println("Макс. больше среднего на: " + Math.round(upDeviation * 100) + "%");
    System.out.println("Мин. меньше среднего на: " + Math.round(downDeviation * 100) + "%");
    System.out.println();
    System.out.printf(Locale.ROOT, "Коэффициент вариации: %.4f%n", cv);
    System.out.printf(Locale.ROOT, "Энтропия Шеннона: %.4f%n", shannonEntropy);
    System.out.printf(Locale.ROOT, "Идеальная энтропия: %.1f%n", idealEntropy);
    System.out.printf(Locale.ROOT, "Индекс Джини: %.4f%n", giniIndex);
    System.out.printf(Locale.ROOT, "Критерий хи-квадрат: %.1f%n", chiSquared);
    System.out.printf(Locale.ROOT, "Тест Колмогорова-Смирнова: %.4f%n", kolmogorovSmirnov);
    System.out.println();
  }

  /// Get statistics

  private void getHashStatistics(List<Map.Entry<String, AtomicInteger>> sortedEntries) {
    int maxLength = Math.min(sortedEntries.size(), 5);

    for (int i = 0; i < maxLength; i++) {
      mostRareHashes.add(sortedEntries.get(i).getKey());

      if (i < sortedEntries.size() - 1
          && sortedEntries.get(i).getValue().get() != sortedEntries.get(i + 1).getValue().get()) {
        break;
      }

      if (i == maxLength - 1 && i != sortedEntries.size() - 2) {
        mostRareHashes.add("etc...");
      }
    }

    maxLength = Math.max(sortedEntries.size() - 5, 0);

    for (int i = sortedEntries.size() - 1; i >= maxLength; i--) {
      mostFrequentHashes.add(sortedEntries.get(i).getKey());

      if (i > 0 && sortedEntries.get(i).getValue().get() != sortedEntries.get(i - 1).getValue().get()) {
        break;
      }

      if (i == maxLength && i != 0) {
        mostFrequentHashes.add("etc...");
      }
    }

    minCollisions = sortedEntries.getFirst().getValue().get();
    maxCollisions = sortedEntries.getLast().getValue().get();
    median = sortedEntries.get(sortedEntries.size() / 2).getValue().get();
  }

  private List<Map.Entry<String, AtomicInteger>> getSortedEntries(Map<String, AtomicInteger> hashCountMap) {
    List<Map.Entry<String, AtomicInteger>> sortedEntries = new ArrayList<>(hashCountMap.entrySet());
    sortedEntries.sort(Comparator.comparingInt(e -> e.getValue().get()));

    return sortedEntries;
  }

  private void getAverage() {
    average = (double) totalMessages / uniqueHashes;

    downDeviation = (average - minCollisions) / average;
    upDeviation = (maxCollisions - average) / average;
  }

  /// Metrics

  private void calculateStandardDeviation(List<Integer> hashCountValues) {
    double variance = 0;

    for (int count : hashCountValues) {
      variance += Math.pow(count - average, 2);
    }
    variance = variance / uniqueHashes;

    standardDeviation = Math.sqrt(variance);
  }

  private void calculateCV(List<Integer> hashCountValues) {
    calculateStandardDeviation(hashCountValues);

    cv = standardDeviation / average;
  }

  private void calculateShannonEntropy(List<Integer> hashCountValues) {
    double entropy = 0;

    for (int count : hashCountValues) {
      double probability = (double) count / totalMessages;
      entropy -= probability * (Math.log(probability) / Math.log(2));
    }

    shannonEntropy = entropy;
    idealEntropy = Math.log(uniqueHashes) / Math.log(2);
  }

  private void calculateGiniIndex(List<Integer> sortedValues) {
    int n = uniqueHashes;
    double weightedSum = 0;

    for (int i = 0; i < n; i++) {
      weightedSum += (i + 1) * sortedValues.get(i);
    }

    giniIndex = (2 * weightedSum / (n * totalMessages)) - (n + 1.0) / n;
  }

  private void calculateChiSquared(List<Integer> hashCountValues) {
    double chi2 = 0;
    for (int count : hashCountValues) {
      double diff = count - average;
      chi2 += Math.pow(diff, 2) / average;
    }

    chiSquared = chi2;
  }

  private void calculateKolmogorovSmirnov(List<Integer> sortedValues) {
    double maxDiff = 0;
    double cumulative = 0;

    for (int i = 0; i < uniqueHashes; i++) {
      cumulative += sortedValues.get(i);

      double empiricalCDF = cumulative / totalMessages;
      double uniformCDF = (i + 1.0) / uniqueHashes;

      double diff = Math.abs(empiricalCDF - uniformCDF);
      maxDiff = Math.max(maxDiff, diff);
    }

    kolmogorovSmirnov = maxDiff;
  }

  private void calculateMetrics(List<Map.Entry<String, AtomicInteger>> sortedEntries) {
    var sortedValues = sortedEntries.stream().map(Map.Entry::getValue).map(AtomicInteger::get).toList();

    calculateCV(sortedValues);
    calculateShannonEntropy(sortedValues);
    calculateGiniIndex(sortedValues);
    calculateChiSquared(sortedValues);
    calculateKolmogorovSmirnov(sortedValues);
  }
}
