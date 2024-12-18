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

        int index;

        // Aplica la mutación adicional para fusionar aspersores adyacentes, sobre 2 indices
        index = JMetalRandom.getInstance().nextInt(0, (solution.getNumberOfVariables() / 2) - 1);
        mergeAdjacentSprinklers(solution, index);

        // Aplica la mutación adicional para colocar aspersores aislados
        if (JMetalRandom.getInstance().nextDouble() < 0.1) {  // Probabilidad adicional de mover un aspersor
            index = JMetalRandom.getInstance().nextInt(0, (solution.getNumberOfVariables() / 2) - 1);
            placeIsolatedSprinkler(solution, index);
        }

        // Aplica la mutación adicional para mover aspersores si es posible
        if (JMetalRandom.getInstance().nextDouble() < 0.05) {  // Probabilidad adicional de mover un aspersor
            index = JMetalRandom.getInstance().nextInt(0, (solution.getNumberOfVariables() / 2) - 1);
            moveSprinklerIfPossible(solution, index);
        }

        // Aplica la mutación adicional para centralizar aspersores
        if (JMetalRandom.getInstance().nextDouble() < 0.05) { // Probabilidad baja, ajustable según necesidades
            index = JMetalRandom.getInstance().nextInt(0, (solution.getNumberOfVariables() / 2) - 1);
            centralizeSprinkler(solution, index);
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
                        int tempTime = solution.getVariable(index + solution.getNumberOfVariables() / 2);

                        solution.setVariable(index, solution.getVariable(newIndex));
                        solution.setVariable(index + solution.getNumberOfVariables() / 2, solution.getVariable(newIndex + solution.getNumberOfVariables() / 2));

                        solution.setVariable(newIndex, tempType);
                        solution.setVariable(newIndex + solution.getNumberOfVariables() / 2, tempTime);
                        break;
                    }
                }
            }
        }
    }

    private void mergeAdjacentSprinklers(IntegerSolution solution, int index) {
        int n = (int) Math.sqrt(solution.getNumberOfVariables() / 2);
        int i = index / n;
        int j = index % n;

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, -1}, {-1, -1}, {1, 1}};
        shuffleArray(directions);  // Shuffle directions to randomize the merge direction

        // Loop through shuffled directions and attempt to merge
        for (int[] dir : directions) {
            int newI = i + dir[0];
            int newJ = j + dir[1];
            if (newI >= 0 && newI < n && newJ >= 0 && newJ < n) {
                int newIndex = newI * n + newJ;
                if (newIndex < solution.getNumberOfVariables() / 2 && solution.getVariable(newIndex) != 0) { // Check if there is a sprinkler
                    // Mergear aspersores
                    int originalTime = solution.getVariable(index + solution.getNumberOfVariables() / 2);
                    int adjacentTime = solution.getVariable(newIndex + solution.getNumberOfVariables() / 2);

                    // New sprinkler type is 2
                    solution.setVariable(index, 2);
                    
                    // Set new time as the sum of both sprinkler times
                    solution.setVariable(index + solution.getNumberOfVariables() / 2, originalTime + adjacentTime);
                    
                    // Remove the adjacent sprinkler
                    solution.setVariable(newIndex, 0);
                    solution.setVariable(newIndex + solution.getNumberOfVariables() / 2, 0);
                    break;
                }
            }
        }
    }

    private void centralizeSprinkler(IntegerSolution solution, int index) {
        int n = (int) Math.sqrt(solution.getNumberOfVariables() / 2);
        int i = index / n;
        int j = index % n;
        int centralIndex = i * n + j;

        int sumTime = 0;
        int count = 0;

        // Recorrer el bloque 3x3 centrado en (i, j)
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                int ni = i + di;
                int nj = j + dj;
                if (ni >= 0 && ni < n && nj >= 0 && nj < n) {
                    int adjIndex = ni * n + nj;
                    if (adjIndex < solution.getNumberOfVariables() / 2) {
                        if (solution.getVariable(adjIndex) == 0) {
                            continue; // No hay aspersor en esta posición
                        }
                        sumTime += solution.getVariable(adjIndex + solution.getNumberOfVariables() / 2); // Sumar el tiempo de todos los aspersores
                        solution.setVariable(adjIndex, 0); // Sacar aspersor
                        solution.setVariable(adjIndex + solution.getNumberOfVariables() / 2, 0); // Tiempo a 0
                        count++;
                    }
                }
            }
        }

        // Colocar un nuevo aspersor en el centro con el tiempo total acumulado, si había aspersores en la zona
        if (count > 0) {
            solution.setVariable(centralIndex, 2);
            solution.setVariable(centralIndex + solution.getNumberOfVariables() / 2, sumTime);
        }
    }

    private void placeIsolatedSprinkler(IntegerSolution solution, int index) {
        int n = (int) Math.sqrt(solution.getNumberOfVariables() / 2);
        int i = index / n;
        int j = index % n;

        // Si no hay aspersores en los alrededores, se coloca un aspersor tipo 2
        if (solution.getVariable(index) == 0) {
            boolean foundAspersor = false;
            for (int dRow = -1; dRow <= 1; dRow++) {
                for (int dCol = -1; dCol <= 1; dCol++) {
                    if (dRow == 0 && dCol == 0) continue; // Saltar la parcela actual
                    int neighborRow = i + dRow;
                    int neighborCol = j + dCol;
                    if (neighborRow >= 0 && neighborRow < n && neighborCol >= 0 && neighborCol < n) {
                        int neighborIndex = neighborRow * n + neighborCol;
                        if (solution.getVariable(neighborIndex) > 0) {
                            foundAspersor = true;
                            break;
                        }
                    }
                }
                if (foundAspersor) break;
            }

            if (!foundAspersor) {
                // Colocar un aspersor tipo 2 en la parcela actual si no hay en los alrededores
                solution.setVariable(index, 2); // Tipo de aspersor a 2
                solution.setVariable(index + n * n, 15); // Establecer tiempo de riego a 15
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
