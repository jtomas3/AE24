import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RegadoRunner {
	public void runAlgorithmOnce(Regado problem) {
		IntegerSolution bestSolution = runAlgorithm(problem);

		int n = problem.n;

		// Imprimir la solución como matriz
		System.out.println("Solución:");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int index = i * n + j;
				System.out.print(
						bestSolution.getVariable(index) + " (" + bestSolution.getVariable(index + (n * n)) + " min.) ");
			}
			System.out.println();
		}

		double[][] riegoTotal = problem.calcularRiegoTotalCampo(bestSolution);

		// Imprimir el agua optima y real en cada parcela
		System.out.println("Agua óptima y real:");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				double aguaOptima = problem.informacionSuelos.get(problem.suelosCampo[i][j]).get("h_campo")
						- problem.informacionSuelos.get(problem.suelosCampo[i][j]).get("h_marchitez")
						+ problem.informacionCultivos.get(problem.cultivosCampo[i][j]).get("agua_requerida");
				System.out.print(aguaOptima + " (" + riegoTotal[i][j] + ") ");
			}
			System.out.println();
		}

		// Calcular diferencia hídrica de cada parcela
		double[][] desviacionHidrica = new double[n][n];
		// Imprimir campo
		System.out.println("Balance de riego:");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int index = i * n + j;
				desviacionHidrica[i][j] = problem.calcularDesviacionHidricaRelativaParcela(bestSolution, index,
						riegoTotal);
				System.out.print(desviacionHidrica[i][j] + " ");
			}
			System.out.println();
		}

		System.out.println("Objective 1 (Diferencia hídrica total): " + bestSolution.getObjective(0));
		System.out.println("Objective 2 (Costo): " + bestSolution.getObjective(1));
	}

	public void runMultipleExecutions(Regado problem, int numExecutions) {
		List<IntegerSolution> bestSolutions = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(numExecutions);
		List<Callable<IntegerSolution>> tasks = new ArrayList<>();

		for (int i = 0; i < numExecutions; i++) {
			// Crear una tarea para cada ejecución
			tasks.add(() -> runAlgorithm(problem));
		}

		try {
			System.out.println("Iniciando ejecuciones concurrentes...");
			// Ejecutar todas las tareas y obtener los resultados
			List<Future<IntegerSolution>> results = executorService.invokeAll(tasks);

			// Procesar los resultados de cada ejecución
			for (Future<IntegerSolution> future : results) {
				try {
					bestSolutions.add(future.get()); // Obtener la solución de cada ejecución
				} catch (Exception e) {
					System.err.println("Error durante la ejecución: " + e.getMessage());
				}
			}
		} catch (InterruptedException e) {
			System.err.println("La ejecución fue interrumpida: " + e.getMessage());
		} finally {
			executorService.shutdown(); // Cerrar el pool de hilos
		}

		exportBestSolutionsToCSV(bestSolutions, "solutionsFromMultipleRuns.csv");
		System.out.println("Datos de las mejores soluciones exportados a solutionsFromMultipleRuns.csv");
	}

	private IntegerSolution runAlgorithm(Regado problem) {
		// Configuración de los operadores
		CrossoverOperator<IntegerSolution> crossover = new IntegerSBXCrossover(0.5, 20.0);
		MutationOperator<IntegerSolution> mutation = new IntegerPolynomialMutation(0.02, 8.0);
		SelectionOperator<List<IntegerSolution>, IntegerSolution> selection = new BinaryTournamentSelection<>(
				new RankingAndCrowdingDistanceComparator<>());

		// Creación del algoritmo NSGA-II
		CustomNSGAII<IntegerSolution> algorithm = new CustomNSGAII<>(problem, 2000000, 35, 30, 30, crossover,
				mutation, selection, new SequentialSolutionListEvaluator<>());

		System.out.println("Comenzando ejecución del algoritmo...");
		// Ejecutar el algoritmo
		algorithm.run();

		// Exportar datos de evolución
		algorithm.exportEvolutionDataToCSV("evolutionData.csv");
		algorithm.exportAvgAndBestObjectivesToCSV("avgAndBestEvolution.csv");

		// Obtención de la solución
		List<IntegerSolution> population = algorithm.getResult();

		IntegerSolution bestSolution = population.get(0);

		for (int i = 1; i < population.size(); i++) {
			if (population.get(i).getObjective(0) < bestSolution.getObjective(0)) {
				bestSolution = population.get(i);
			}
		}

		// Encuentra soluciones que prioricen el objetivo 0 más que el objetivo 1
		// List<IntegerSolution> prioritizedSolutions = new ArrayList<>();
		// for (IntegerSolution solution : population) {
		// 	// Calcula un score ponderado (puedes ajustar los pesos según necesites)
		// 	double weightedScore = 0.7 * solution.getObjective(0) + 0.3 * solution.getObjective(1); // Normalizar
		// 	solution.setAttribute("WeightedScore", weightedScore);
		// 	prioritizedSolutions.add(solution);
		// }

		// // Ordena las soluciones por el score ponderado
		// prioritizedSolutions.sort(Comparator.comparing(s -> (double) s.getAttribute("WeightedScore")));

		// // Selecciona las dos mejores soluciones según el score ponderado
		// IntegerSolution bestForObjective0More = prioritizedSolutions.get(0);
		// IntegerSolution secondBestForObjective0More = prioritizedSolutions.get(1);

		return bestSolution;
	}

	private static void exportBestSolutionsToCSV(List<IntegerSolution> bestSolutions, String fileName) {
		try {
			FileWriter writer = new FileWriter(fileName);
			writer.write("Objective 1,Objective 2\n");
			for (IntegerSolution solution : bestSolutions) {
				writer.write(solution.getObjective(0) + "," + solution.getObjective(1) + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
