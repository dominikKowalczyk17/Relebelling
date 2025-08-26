package Relabeling;

import java.util.ArrayList;
import java.util.List;

class RelabelingSolver {
    private LevelGraph originalGraph;
    private double targetGrowthPercent;
    private int minSwapsFound;
    private List<String> bestSolution;
    private int totalStatesExplored;
    private int totalPossibleMoves;

    public RelabelingSolver(LevelGraph graph, double targetGrowthPercent) {
        this.originalGraph = graph;
        this.targetGrowthPercent = targetGrowthPercent;
        this.minSwapsFound = Integer.MAX_VALUE;
        this.bestSolution = new ArrayList<>();
        this.totalStatesExplored = 0;
        this.totalPossibleMoves = calculateTotalPossibleMoves(graph);
    }

    private int calculateTotalPossibleMoves(LevelGraph graph) {
        int total = 0;
        for (String levelName : graph.getLevelNames()) {
            Level level = graph.getLevel(levelName);
            if (level.size() > 1) { // Tylko poziomy które można opróżnić
                total += level.size() * (graph.getLevelNames().size() - 1);
            }
        }
        return total;
    }

    public int findMinimalRelabeling(int maxSwaps) {
        System.out.println("Szukam minimalnej liczby przeetykietowań...");
        originalGraph.printStatus();

        System.out.println("Szacowana liczba możliwych ruchów na poziom: " + totalPossibleMoves);
        System.out.println("Maksymalna głębokość przeszukiwania: " + maxSwaps);
        System.out.println("Rozpoczynam przeszukiwanie...\n");

        if (originalGraph.checkMedianGrowthCondition(targetGrowthPercent)) {
            System.out.println("Warunek już spełniony! Potrzeba 0 przeetykietowań.");
            return 0;
        }

        long startTime = System.currentTimeMillis();
        dfs(originalGraph.deepCopy(), 0, maxSwaps, new ArrayList<>());
        long endTime = System.currentTimeMillis();

        System.out.println("\n=== PODSUMOWANIE ===");
        System.out.println("Czas wykonania: " + (endTime - startTime) + " ms");
        System.out.println("Przeszukano stanów: " + totalStatesExplored);

        if (minSwapsFound == Integer.MAX_VALUE) {
            System.out.println("Nie znaleziono rozwiązania w " + maxSwaps + " krokach.");
            return -1;
        } else {
            System.out.println("Minimalna liczba przeetykietowań: " + minSwapsFound);
            System.out.println("Sekwencja ruchów: " + bestSolution);
            return minSwapsFound;
        }
    }

    private void dfs(LevelGraph currentGraph, int swapsUsed, int maxSwaps, List<String> moveHistory) {
        totalStatesExplored++;

        // Pokaż postęp co 1000 stanów
        if (totalStatesExplored % 5000 == 0) {
            showProgress(currentGraph, swapsUsed);
        }

        // Sprawdź warunek końcowy
        if (currentGraph.checkMedianGrowthCondition(targetGrowthPercent)) {
            if (swapsUsed < minSwapsFound) {
                minSwapsFound = swapsUsed;
                bestSolution = new ArrayList<>(moveHistory);
                System.out.println("🎉 Znaleziono rozwiązanie w " + swapsUsed + " krokach!");
                showCurrentMedianChanges(currentGraph);
                System.out.println("Sekwencja: " + bestSolution + "\n");
            }
            return;
        }

        // Obcięcie - jeśli już przekroczyliśmy najlepsze rozwiązanie
        if (swapsUsed >= minSwapsFound || swapsUsed >= maxSwaps) {
            return;
        }

        // Spróbuj wszystkie możliwe przeetykietowania
        for (String fromLevelName : currentGraph.getLevelNames()) {
            Level fromLevel = currentGraph.getLevel(fromLevelName);

            // Pomiń poziomy z tylko 1 obserwacją (nie można opróżnić)
            if (fromLevel.size() <= 1)
                continue;

            for (int obsIndex = 0; obsIndex < fromLevel.size(); obsIndex++) {
                for (String toLevelName : currentGraph.getLevelNames()) {
                    if (fromLevelName.equals(toLevelName))
                        continue;

                    try {
                        // Wykonaj ruch
                        LevelGraph nextGraph = currentGraph.deepCopy();
                        Level nextFromLevel = nextGraph.getLevel(fromLevelName);
                        Level nextToLevel = nextGraph.getLevel(toLevelName);

                        double movedValue = nextFromLevel.moveObservationTo(obsIndex, nextToLevel);

                        String move = String.format("%s[%d](%.1f)->%s",
                                fromLevelName, obsIndex, movedValue, toLevelName);
                        moveHistory.add(move);

                        // Rekurencyjne wywołanie
                        dfs(nextGraph, swapsUsed + 1, maxSwaps, moveHistory);

                        // Cofnij ruch (backtrack)
                        moveHistory.remove(moveHistory.size() - 1);

                    } catch (IllegalStateException e) {
                        // Pomiń ruchy, które opróżniłyby poziom
                        continue;
                    }
                }
            }
        }
    }

    private void showProgress(LevelGraph currentGraph, int swapsUsed) {
        System.out.printf("Przeszukano: %d stanów, głębokość: %d, najlepsze: %s\n",
                totalStatesExplored, swapsUsed,
                minSwapsFound == Integer.MAX_VALUE ? "brak" : minSwapsFound + " ruchów");

        // Pokaż łączny procent zmian w całym systemie
        showOverallSystemChanges(currentGraph);

        // Pokaż aktualne procenty zmian median
        showCurrentMedianChanges(currentGraph);
        System.out.println();
    }

    private void showOverallSystemChanges(LevelGraph currentGraph) {
        int totalOriginalObservations = 0;
        int totalMovedObservations = 0;

        for (String levelName : currentGraph.getLevelNames()) {
            Level originalLevel = originalGraph.getLevel(levelName);
            Level currentLevel = currentGraph.getLevel(levelName);

            int originalSize = originalLevel.size();
            int currentSize = currentLevel.size();

            totalOriginalObservations += originalSize;
            totalMovedObservations += Math.abs(currentSize - originalSize);
        }

        // Każde przeniesienie liczy się podwójnie (z jednego poziomu, do drugiego)
        // więc dzielimy przez 2 żeby uzyskać rzeczywistą liczbę przeniesionych
        // obserwacji
        int actualMovedObservations = totalMovedObservations / 2;
        double systemChangePercent = (actualMovedObservations * 100.0) / totalOriginalObservations;

        System.out.printf("Zmodyfikowany system: %d/%d obserwacji (%.1f%%)\n",
                actualMovedObservations, totalOriginalObservations, systemChangePercent);
    }

    private void showCurrentMedianChanges(LevelGraph currentGraph) {
        List<String> levels = currentGraph.getLevelOrder();
        System.out.print("Zmiany w poziomach: ");
        for (String levelName : levels) {
            Level originalLevel = originalGraph.getLevel(levelName);
            Level currentLevel = currentGraph.getLevel(levelName);

            int originalSize = originalLevel.size();
            int currentSize = currentLevel.size();
            int changedObservations = Math.abs(currentSize - originalSize);
            double changePercent = (changedObservations * 100.0) / originalSize;

            System.out.printf("%s: %d→%d (%.0f%%) ",
                    levelName, originalSize, currentSize, changePercent);
        }
        System.out.println(); // Przenieś to wyżej

        // Pokaż wzrosty median
        System.out.print("Wzrosty median: ");
        for (int i = 0; i < levels.size() - 1; i++) {
            String currentLevel = levels.get(i);
            String nextLevel = levels.get(i + 1);

            double currentMedian = currentGraph.getLevel(currentLevel).getMedian();
            double nextMedian = currentGraph.getLevel(nextLevel).getMedian();

            double actualGrowth = ((nextMedian - currentMedian) / currentMedian) * 100;
            String status = actualGrowth >= targetGrowthPercent ? "✅" : "❌";

            System.out.printf("%s→%s: %+.1f%% %s ",
                    currentLevel, nextLevel, actualGrowth, status);
        }
    }
}
