package Relabeling;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class LevelGraph {
    private Map<String, Level> levels;
    private Map<String, List<String>> adjacencyList;
    private List<String> levelOrder; // A, B, C, ... porządek poziomów
    
    public LevelGraph() {
        this.levels = new HashMap<>();
        this.adjacencyList = new HashMap<>();
        this.levelOrder = new ArrayList<>();
    }
    
    public void addLevel(String name, List<Double> observations) {
        Level level = new Level(name, observations);
        levels.put(name, level);
        adjacencyList.put(name, new ArrayList<>());
        levelOrder.add(name);
    }
    
    public void connectLevels(String fromLevel, String toLevel) {
        if (adjacencyList.containsKey(fromLevel)) {
            adjacencyList.get(fromLevel).add(toLevel);
        }
    }
    
    public void connectAllLevels() {
        // Każdy poziom może przekazać obserwacje do każdego innego
        for (String from : levels.keySet()) {
            for (String to : levels.keySet()) {
                if (!from.equals(to)) {
                    connectLevels(from, to);
                }
            }
        }
    }
    
    public boolean checkMedianGrowthCondition(double targetGrowthPercent) {
        for (int i = 0; i < levelOrder.size() - 1; i++) {
            String currentLevel = levelOrder.get(i);
            String nextLevel = levelOrder.get(i + 1);
            
            double currentMedian = levels.get(currentLevel).getMedian();
            double nextMedian = levels.get(nextLevel).getMedian();
            
            double actualGrowth = ((nextMedian - currentMedian) / currentMedian) * 100;
            
            if (actualGrowth < targetGrowthPercent) {
                return false;
            }
        }
        return true;
    }
    
    public LevelGraph deepCopy() {
        LevelGraph copy = new LevelGraph();
        copy.levelOrder = new ArrayList<>(this.levelOrder);
        
        for (String levelName : levels.keySet()) {
            Level originalLevel = levels.get(levelName);
            copy.levels.put(levelName, originalLevel.deepCopy());
            copy.adjacencyList.put(levelName, new ArrayList<>(adjacencyList.get(levelName)));
        }
        
        return copy;
    }
    
    public Level getLevel(String name) {
        return levels.get(name);
    }
    
    public Set<String> getLevelNames() {
        return levels.keySet();
    }
    
    public List<String> getLevelOrder() {
        return new ArrayList<>(levelOrder);
    }
    
    public void printStatus() {
        System.out.println("=== Status poziomów ===");
        for (String levelName : levelOrder) {
            System.out.println(levels.get(levelName));
        }
        System.out.println();
    }
}