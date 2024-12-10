import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import java.util.ArrayList;
import java.util.List;

public class NIntegerMin extends AbstractIntegerProblem {
    private int valueN;

    public NIntegerMin(int numberOfVariables, int n, int lowerBound, int upperBound) {
        valueN = n;
        setNumberOfVariables(numberOfVariables);
        setNumberOfObjectives(1);
        setName("NIntegerMin");

        List<Integer> lowerLimit = new ArrayList<>(getNumberOfVariables());
        List<Integer> upperLimit = new ArrayList<>(getNumberOfVariables());
        for (int i = 0; i < getNumberOfVariables(); i++) {
            lowerLimit.add(lowerBound);
            upperLimit.add(upperBound);
        }

        setVariableBounds(lowerLimit, upperLimit);
    }

    @Override
    public void evaluate(IntegerSolution solution) {
        int approximationToN = 0;
        for (int i = 0; i < solution.getVariables().size(); i++) {
            int value = solution.getVariables().get(i);
            approximationToN += Math.abs(valueN - value);
        }
        solution.getObjectives()[0] = approximationToN;
    }

    public static void main(String[] args) {
        int numberOfVariables = 5;
        int n = 50;
        int lowerBound = 0;
        int upperBound = 100;

        NIntegerMin problem = new NIntegerMin(numberOfVariables, n, lowerBound, upperBound);

        IntegerSolution newSolution = new DefaultIntegerSolution(problem.getBounds(), problem.getNumberOfObjectives());

        // Generar valores aleatorios dentro de los límites para cada variable
        for (int i = 0; i < numberOfVariables; i++) {
            int randomValue = lowerBound + (int) (Math.random() * ((upperBound - lowerBound) + 1));
            newSolution.getVariables().set(i, randomValue);
        }

        // Evaluamos la solución
        problem.evaluate(newSolution);

        // Imprimir la solución y su evaluación
        System.out.println("Solution: " + newSolution.getVariables());
        System.out.println("Objective (Approximation to " + n + "): " + newSolution.getObjectives()[0]);
    }
}
