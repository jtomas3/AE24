import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.Random;

public class CustomIntegerMutation extends IntegerPolynomialMutation {
    public CustomIntegerMutation(double mutationProbability, double distributionIndex) {
        super(mutationProbability, distributionIndex);
    }

    @Override
    public IntegerSolution execute(IntegerSolution solution) {
        if (null == solution) {
            throw new JMetalException("Null parameter");
        }

        // Realiza la mutación polinomial estándar
        super.execute(solution);

        // Aplica la mutación adicional para mover aspersores si es posible
        if (JMetalRandom.getInstance().nextDouble() < 0.1) {  // Probabilidad adicional de mover un aspersor
            int index = JMetalRandom.getInstance().nextInt(0, solution.getNumberOfVariables() / 2);
            moveSprinklerIfPossible(solution, index);
        }

        return solution;
    }

    private void moveSprinklerIfPossible(IntegerSolution solution, int index) {
        int n = (int) Math.sqrt(solution.getNumberOfVariables() / 2);
        int i = index / n;
        int j = index % n;

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, -1}, {-1, -1}, {1, 1}};
        for (int[] dir : directions) {
            int newI = i + dir[0];
            int newJ = j + dir[1];
            if (newI >= 0 && newI < n && newJ >= 0 && newJ < n) {
                int newIndex = newI * n + newJ;
                if (newIndex < solution.getNumberOfVariables() / 2) { // Asegurarse de que el nuevo índice está dentro de los límites
                    if (solution.getVariable(newIndex) == 0) { // Asumimos que '0' indica ausencia de aspersor
                        // Intercambio de aspersor y su tiempo
                        int tempType = solution.getVariable(index);
                        int tempTime = solution.getVariable(index - 1 + solution.getNumberOfVariables() / 2);

                        solution.setVariable(index, solution.getVariable(newIndex));
                        solution.setVariable(index - 1 + solution.getNumberOfVariables() / 2, solution.getVariable(newIndex + solution.getNumberOfVariables() / 2));

                        solution.setVariable(newIndex, tempType);
                        solution.setVariable(newIndex + solution.getNumberOfVariables() / 2, tempTime);
                        break;
                    }
                }
            }
        }
    }

    private void shuffleArray(int[][] array) {
        Random rand = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            // Intercambio simple
            int[] temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
}
