package Relabeling;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Level {
    private String name;
    private List<Double> observations;
    private double median;
    
    public Level(String name, List<Double> observations) {
        this.name = name;
        this.observations = new ArrayList<>(observations);
        updateMedian();
    }
    
    public void updateMedian() {
        if (observations.isEmpty()) {
            this.median = 0.0; // lub Double.NaN, lub rzuć wyjątek
            return;
        }
        
        List<Double> sorted = new ArrayList<>(observations);
        Collections.sort(sorted);
        int size = sorted.size();
        
        if (size % 2 == 0) {
            this.median = (sorted.get(size/2 - 1) + sorted.get(size/2)) / 2.0;
        } else {
            this.median = sorted.get(size/2);
        }
    }
    
    public double moveObservationTo(int index, Level targetLevel) {
        if (index < 0 || index >= observations.size()) {
            throw new IndexOutOfBoundsException("Invalid observation index");
        }
        
        // WAŻNE: Nie pozwól opróżnić poziomu!
        if (observations.size() <= 1) {
            throw new IllegalStateException("Cannot move last observation - level would be empty!");
        }
        
        double value = observations.remove(index);
        targetLevel.observations.add(value);
        
        this.updateMedian();
        targetLevel.updateMedian();
        
        return value;
    }
    
    public Level deepCopy() {
        return new Level(this.name, new ArrayList<>(this.observations));
    }
    
    // Gettery
    public String getName() { return name; }
    public List<Double> getObservations() { return new ArrayList<>(observations); }
    public double getMedian() { return median; }
    public int size() { return observations.size(); }
    
    @Override
    public String toString() {
        return String.format("Level %s: median=%.2f, size=%d", 
                           name, median, observations.size());
    }
}
