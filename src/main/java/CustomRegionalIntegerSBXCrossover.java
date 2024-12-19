import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import java.util.ArrayList;
import java.util.List;

public class CustomRegionalIntegerSBXCrossover implements CrossoverOperator<IntegerSolution> {
    private double crossoverProbability;
    private int regionSize;
    private RandomGenerator<Double> randomGenerator;

    public CustomRegionalIntegerSBXCrossover(double crossoverProbability, int regionSize) {
        this(crossoverProbability, regionSize, () -> {
            return JMetalRandom.getInstance().nextDouble();
        });
    }

    public CustomRegionalIntegerSBXCrossover(double crossoverProbability, int regionSize, RandomGenerator<Double> randomGenerator) {
        if (crossoverProbability < 0.0) {
            throw new JMetalException("Crossover probability is negative: " + crossoverProbability);
        } else if (regionSize < 0.0) {
            throw new JMetalException("Region size is negative: " + regionSize);
        } else {
            this.crossoverProbability = crossoverProbability;
            this.regionSize = regionSize;
            this.randomGenerator = randomGenerator;
        }
    }

    public double getCrossoverProbability() {
        return this.crossoverProbability;
    }

    public void setCrossoverProbability(double crossoverProbability) {
        this.crossoverProbability = crossoverProbability;
    }

    @Override
    public List<IntegerSolution> execute(List<IntegerSolution> solutions) {
        if (null == solutions) {
            throw new JMetalException("Null parameter");
        } else if (solutions.size() != 2) {
            throw new JMetalException("There must be two parents instead of " + solutions.size());
        }

        return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1));
    }

    private List<IntegerSolution> doCrossover(double probability, IntegerSolution parent1, IntegerSolution parent2) {
        IntegerSolution child1 = (IntegerSolution) parent1.copy();
        IntegerSolution child2 = (IntegerSolution) parent2.copy();
        int n = (int) Math.sqrt(parent1.getNumberOfVariables() / 2);

        if ((Double)this.randomGenerator.getRandomValue() <= probability) {
            int numRegionsPerSide = n / regionSize;
            for (int regionRow = 0; regionRow < numRegionsPerSide; regionRow++) {
                for (int regionCol = 0; regionCol < numRegionsPerSide; regionCol++) {
                    if ((Double)this.randomGenerator.getRandomValue() <= 0.5) {
                        for (int i = 0; i < regionSize; i++) {
                            for (int j = 0; j < regionSize; j++) {
                                int idx = ((regionRow * regionSize + i) * n + (regionCol * regionSize + j));
                                swap(child1, child2, idx);
                            }
                        }
                    }
                }
            }
        }

        List<IntegerSolution> result = new ArrayList<>();
        result.add(child1);
        result.add(child2);
        return result;
    }

    private void swap(IntegerSolution child1, IntegerSolution child2, int index) {
    int n = (int) Math.sqrt(child1.getNumberOfVariables() / 2);

    // Intercambiar tipos de aspersor
    int tempType = child1.getVariable(index);
    child1.setVariable(index, child2.getVariable(index));
    child2.setVariable(index, tempType);

    // Intercambiar tiempos de aspersor, que están ubicados después de todas las variables de tipo de aspersor
    int timeIndex = index + n * n; // Obtener el índice correspondiente al tiempo del aspersor
    int tempTime = child1.getVariable(timeIndex);
    child1.setVariable(timeIndex, child2.getVariable(timeIndex));
    child2.setVariable(timeIndex, tempTime);
}

    @Override
    public int getNumberOfRequiredParents() {
        return 2;
    }

    @Override
    public int getNumberOfGeneratedChildren() {
        return 2;
    }
}
