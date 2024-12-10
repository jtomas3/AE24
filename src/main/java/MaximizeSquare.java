import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.algorithm.Algorithm;
import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.util.comparator.DominanceComparator;

public class MaximizeSquare extends AbstractIntegerProblem {
    private static final long serialVersionUID = 1L;

	public MaximizeSquare(int lowerBound, int upperBound) {
        setNumberOfVariables(1);
        setNumberOfObjectives(1);
        setName("MaximizeSquare");

        List<Integer> lowerLimit = new ArrayList<>(1);
        List<Integer> upperLimit = new ArrayList<>(1);

        lowerLimit.add(lowerBound);
        upperLimit.add(upperBound);

        setVariableBounds(lowerLimit, upperLimit);
    }

    @Override
    public void evaluate(IntegerSolution solution) {
        int value = solution.getVariables().get(0);
        solution.getObjectives()[0] = -value * value;  // Maximizing the square by minimizing the negative
    }

    public static void main(String[] args) {
        int lowerBound = 0;
        int upperBound = 64;

        MaximizeSquare problem = new MaximizeSquare(lowerBound, upperBound);

        CrossoverOperator<IntegerSolution> crossover = new IntegerSBXCrossover(0.9, 20.0);
        MutationOperator<IntegerSolution> mutation = new IntegerPolynomialMutation(1.0 / problem.getNumberOfVariables(), 20.0);
        SelectionOperator<List<IntegerSolution>, IntegerSolution> selection = new BinaryTournamentSelection<>(new DominanceComparator<>());

        Algorithm<IntegerSolution> algorithm = new GeneticAlgorithmBuilder<>(problem, crossover, mutation)
                .setSelectionOperator(selection)
                .setMaxEvaluations(10000)
                .setPopulationSize(100)
                .build();

        // Ejecutar el algoritmo
        algorithm.run();

        System.out.println("Solution: " + algorithm.getResult().getVariables());
        System.out.println("Objective: " + algorithm.getResult().getObjectives()[0]);
    }
}
