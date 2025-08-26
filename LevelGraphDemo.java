package Relabeling;
import java.util.Arrays;

public class LevelGraphDemo {
    public static void main(String[] args) {
        // Tworzenie TRUDNIEJSZEGO przykładowego problemu
        LevelGraph graph = new LevelGraph();
        
        // Poziom A: głównie wysokie wartości (źle przypisane!)
        graph.addLevel("A", Arrays.asList(45.0, 47.0, 20.0, 46.0, 48.0));
        
        // Poziom B: mieszanina wartości  
        graph.addLevel("B", Arrays.asList(25.0, 44.0, 30.0, 35.0, 50.0));
        
        // Poziom C: głównie niskie wartości (źle przypisane!)
        graph.addLevel("C", Arrays.asList(22.0, 24.0, 60.0, 23.0, 21.0));
        
        // Połącz wszystkie poziomy
        graph.connectAllLevels();
        
        System.out.println("=== PRZED optymalizacją ===");
        graph.printStatus();
        
        // Sprawdź czy warunek jest spełniony
        boolean conditionMet = graph.checkMedianGrowthCondition(10.0);
        System.out.println("Warunek 10% wzrostu spełniony: " + conditionMet);
        
        if (!conditionMet) {
            // Rozwiąż problem - chcemy 10% wzrostu median między poziomami
            RelabelingSolver solver = new RelabelingSolver(graph, 10.0);
            int result = solver.findMinimalRelabeling(6);
            
            System.out.println("\nWynik: " + (result >= 0 ? result + " przeetykietowań" : "Brak rozwiązania"));
        }
    }
}