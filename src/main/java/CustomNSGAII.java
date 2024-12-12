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
	private List<Double> objetivo1Avg = new ArrayList<>();
	private List<Double> objetivo2Avg = new ArrayList<>();
	private int generation = 0;

	public CustomNSGAII(Problem<S> problem, int maxEvaluations, int populationSize, int matingPoolSize,
			int offspringPopulationSize, CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
			SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
		super(problem, maxEvaluations, populationSize, matingPoolSize, offspringPopulationSize, crossoverOperator,
				mutationOperator, selectionOperator, evaluator);
	}

	@Override
	protected void updateProgress() {
		super.updateProgress();

		// Incrementar el contador de generaciones
		generation++;

		// Registrar los objetivos actuales de la población
		List<double[]> currentGenerationObjectives = new ArrayList<>();

		// Para el promedio de objetivos
		double objetivo1Sum = 0;
		double objetivo2Sum = 0;

		for (S solution : getPopulation()) {
			objetivo1Sum += solution.getObjective(0);
			objetivo2Sum += solution.getObjective(1);

			double[] objectives = new double[solution.getNumberOfObjectives()];
			for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
				objectives[i] = solution.getObjective(i);
			}
			currentGenerationObjectives.add(objectives);
		}
		evolutionData.add(currentGenerationObjectives);

		// Calcular promedio de objetivos y agregar a la lista
		objetivo1Avg.add(objetivo1Sum / getPopulation().size());
		objetivo2Avg.add(objetivo2Sum / getPopulation().size());

		if (generation % 10 == 0) {
			printGenerationInformation();
		}
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

	public void exportAvgObjectivesToCSV(String filePath) {
		try (FileWriter writer = new FileWriter(filePath)) {
			writer.append("Generation,AvgObjective1,AvgObjective2\n"); // Encabezados de las columnas

			for (int generation = 0; generation < objetivo1Avg.size(); generation++) {
				writer.append(String.format("%d,%.4f,%.4f\n", generation, objetivo1Avg.get(generation),
						objetivo2Avg.get(generation)));
			}

			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printGenerationInformation() {
		System.out.println("Generación " + generation + " - Promedio de objetivos (Diferencia Hídrica, Costo): "
				+ objetivo1Avg.get(generation - 1) + ", " + objetivo2Avg.get(generation - 1));
	}
}
