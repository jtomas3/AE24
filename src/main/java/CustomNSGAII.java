import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomNSGAII<S extends Solution<?>> extends NSGAII<S> {

	// Lista para almacenar los objetivos en cada generación
	private final List<List<double[]>> evolutionData = new ArrayList<>();

	public CustomNSGAII(Problem<S> problem, int maxEvaluations, int populationSize, int matingPoolSize,
			int offspringPopulationSize, CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
			SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
		super(problem, maxEvaluations, populationSize, matingPoolSize, offspringPopulationSize, crossoverOperator,
				mutationOperator, selectionOperator, evaluator);
	}

	@Override
	protected void updateProgress() {
		super.updateProgress();

		// Registrar los objetivos actuales de la población
		List<double[]> currentGenerationObjectives = new ArrayList<>();
		for (S solution : getPopulation()) {
			double[] objectives = new double[solution.getNumberOfObjectives()];
			for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
				objectives[i] = solution.getObjective(i);
			}
			currentGenerationObjectives.add(objectives);
		}
		evolutionData.add(currentGenerationObjectives);
	}

	public List<List<double[]>> getEvolutionData() {
		return evolutionData;
	}

	public void exportEvolutionDataToCSV(String filePath) {
		try (FileWriter writer = new FileWriter(filePath)) {
			writer.append("Generation,Objective1,Objective2\n"); // Encabezados de las columnas

			for (int generation = 0; generation < evolutionData.size(); generation++) {
				for (double[] objectives : evolutionData.get(generation)) {
					writer.append(String.format("%d,%.4f,%.4f\n", generation, objectives[0], objectives[1]));
				}
			}

			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
