import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.solution.AbstractSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

public class CustomDefaultIntegerSolution extends AbstractSolution<Integer> implements IntegerSolution {
    protected List<Pair<Integer, Integer>> bounds;
    private static int amountOfGreedySolutions;

    // Constructor que permite inicializar con greedySolution
    public CustomDefaultIntegerSolution(List<Pair<Integer, Integer>> bounds, int numberOfObjectives, int numberOfConstraints, int[][] greedySolution) {
        super(bounds.size(), numberOfObjectives);
        this.bounds = bounds;
        CustomDefaultIntegerSolution.amountOfGreedySolutions += 1;

        for (int i = 0; i < bounds.size(); ++i) {
            int greedyValue = extractGreedyValue(greedySolution, i);
            // Probabilidad de usar un valor aleatorio en lugar de greedySolution
            int mutationProbability = (int) (JMetalRandom.getInstance().nextDouble() * 100);
            System.out.println(mutationProbability);
            if ((greedyValue != -1 || mutationProbability > 1) && amountOfGreedySolutions < 2) {
                // Usar el valor de greedySolution si está definido
                this.setVariable(i, greedyValue);
            } else {
                // Inicializar con un valor aleatorio dentro de los límites
                this.setVariable(i, JMetalRandom.getInstance().nextInt(
                        (Integer) bounds.get(i).getLeft(),
                        (Integer) bounds.get(i).getRight()
                ));
            }
        }
    }

    // Constructor sin greedySolution (aleatorio por defecto)
    public CustomDefaultIntegerSolution(List<Pair<Integer, Integer>> bounds, int numberOfObjectives, int[][] greedySolution) {
        this(bounds, numberOfObjectives, 0, greedySolution);
    }

    // Constructor de copia
    public CustomDefaultIntegerSolution(CustomDefaultIntegerSolution solution) {
        super(solution.getNumberOfVariables(), solution.getNumberOfObjectives());

        int i;
        for (i = 0; i < solution.getNumberOfVariables(); ++i) {
            this.setVariable(i, solution.getVariable(i));
        }

        for (i = 0; i < solution.getNumberOfObjectives(); ++i) {
            this.setObjective(i, solution.getObjective(i));
        }

        this.bounds = solution.bounds;
        this.attributes = new HashMap<>(solution.attributes);
    }

    // Método para obtener el valor de la solución greedy si existe
    private int extractGreedyValue(int[][] greedySolution, int index) {
        if (greedySolution == null) {
            return -1; // Indica que no hay un valor definido
        }

        int n = (int) Math.sqrt(greedySolution.length * greedySolution[0].length / 2);
        int row = index / n;
        int col = index % n;

        if (row < greedySolution.length && col < greedySolution[0].length) {
            return greedySolution[row][col];
        }

        return -1; // Indica que no hay un valor definido
    }

    public Integer getLowerBound(int index) {
        return bounds.get(index).getLeft();
    }

    public Integer getUpperBound(int index) {
        return bounds.get(index).getRight();
    }

    public CustomDefaultIntegerSolution copy() {
        return new CustomDefaultIntegerSolution(this);
    }
}
