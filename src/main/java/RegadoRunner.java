import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RegadoRunner {
	public void runAlgorithmOnce(Regado problem, double maxObjective0, int maxObjective1, int populationSize, int matingPoolSize, int offspringSize, int regionCrossoverSize) {
		List<IntegerSolution> bestSolutions = runAlgorithm(problem, maxObjective0, maxObjective1, populationSize, matingPoolSize, offspringSize, regionCrossoverSize);

		int counter = 0;
		for (IntegerSolution solution : bestSolutions) {
			counter++;
			int n = problem.n;
			//System.out.println(" ");
			// Imprimir la solución como matriz
			//System.out.println("---------------------------------*");
			//System.out.println("Solución: " + counter);
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					int index = i * n + j;
					//System.out.print(
					//		solution.getVariable(index) + " (" + solution.getVariable(index + (n * n)) + " min.) ");
				}
				//System.out.println();
			}

			double[][] riegoTotal = problem.calcularRiegoTotalCampo(solution);

			// Imprimir el agua optima y real en cada parcela
			//System.out.println("Agua óptima y real:");
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					double aguaOptima = problem.informacionSuelos.get(problem.suelosCampo[i][j]).get("h_campo")
							- problem.informacionSuelos.get(problem.suelosCampo[i][j]).get("h_marchitez")
							+ problem.informacionCultivos.get(problem.cultivosCampo[i][j]).get("agua_requerida");
					//System.out.print(aguaOptima + " (" + riegoTotal[i][j] + ") ");
				}
				//System.out.println();
			}

			// Calcular diferencia hídrica de cada parcela
			double[][] desviacionHidrica = new double[n][n];
			// Imprimir campo
			//System.out.println("Balance de riego:");
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					int index = i * n + j;
					desviacionHidrica[i][j] = problem.calcularDesviacionHidricaRelativaParcela(index, riegoTotal);
					//System.out.print(desviacionHidrica[i][j] + " ");
				}
				//System.out.println();
			}

			//System.out.println("Objective 0 (Diferencia hídrica total): " + solution.getObjective(0));
			//System.out.println("Objective 1 (Costo): " + solution.getObjective(1));
			exportarIrrigacionCSV(riegoTotal, "riegoTotal_" + counter + ".csv");
			exportarAspersoresCSV(solution, "aspersores_" + counter + ".csv");
		}
	}

	public void runMultipleExecutions(Regado problem, int numExecutions, double maxObjective0, int maxObjective1, int populationSize, int matingPoolSize, int offspringSize, int regionCrossoverSize) {
		List<List<IntegerSolution>> bestSolutions = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<Callable<List<IntegerSolution>>> tasks = new ArrayList<>();

		for (int i = 0; i < numExecutions; i++) {
			// Crear una tarea para cada ejecución
			tasks.add(() -> runAlgorithm(problem, maxObjective0, maxObjective1, populationSize, matingPoolSize, offspringSize, regionCrossoverSize));
		}

		try {
			System.out.println("Iniciando ejecuciones concurrentes...");
			// Ejecutar todas las tareas y obtener los resultados
			List<Future<List<IntegerSolution>>> results = executorService.invokeAll(tasks);

			// Procesar los resultados de cada ejecución
			for (Future<List<IntegerSolution>> future : results) {
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

		List<IntegerSolution> bestForObj0 = new ArrayList<>();
		List<IntegerSolution> weighted1 = new ArrayList<>();
		List<IntegerSolution> weighted2 = new ArrayList<>();

		for (List<IntegerSolution> solutions : bestSolutions) {
			bestForObj0.add(solutions.get(0));
			weighted1.add(solutions.get(1));
			weighted2.add(solutions.get(2));
		}

		exportBestSolutionsToCSV(bestForObj0, "bestForObj0.csv");
		exportBestSolutionsToCSV(weighted1, "weighted1.csv");
		exportBestSolutionsToCSV(weighted2, "weighted2.csv");
	}

	private List<IntegerSolution> runAlgorithm(Regado problem, double maxObjective0, int maxObjective1, int populationSize, int matingPoolSize, int offspringSize, int regionCrossoverSize) {
		// Configuración de los operadores
		CrossoverOperator<IntegerSolution> crossover = new CustomRegionalIntegerSBXCrossover(0.5, regionCrossoverSize); // Segundo parametro es la dimension de los cuadrados para el cruce
		MutationOperator<IntegerSolution> mutation = new CustomIntegerMutation(0.06, 8.0);
		SelectionOperator<List<IntegerSolution>, IntegerSolution> selection = new BinaryTournamentSelection<>(
				new RankingAndCrowdingDistanceComparator<>());

		// Creación del algoritmo NSGA-II
		CustomNSGAII<IntegerSolution> algorithm = new CustomNSGAII<>(problem, 1000000, populationSize, matingPoolSize, offspringSize, crossover, mutation,
				selection, new SequentialSolutionListEvaluator<>());

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

		// Calcular la media y desviacion estandar de las soluciones para cada objetivo
		double avgObjective0 = 0;
		double avgObjective1 = 0;
		double stdDevObjective0 = 0;
		double stdDevObjective1 = 0;

		for (IntegerSolution solution : population) {
			avgObjective0 += solution.getObjective(0);
			avgObjective1 += solution.getObjective(1);
		}

		avgObjective0 /= population.size();
		avgObjective1 /= population.size();

		for (IntegerSolution solution : population) {
			stdDevObjective0 += Math.pow(solution.getObjective(0) - avgObjective0, 2);
			stdDevObjective1 += Math.pow(solution.getObjective(1) - avgObjective1, 2);
		}

		stdDevObjective0 = Math.sqrt(stdDevObjective0 / population.size());
		stdDevObjective1 = Math.sqrt(stdDevObjective1 / population.size());

		System.out.println("Best solution Objective 0");
		System.out.println("Objective 0: " + bestSolution.getObjective(0));
		System.out.println("Objective 1: " + bestSolution.getObjective(1));

		List<IntegerSolution> prioritizedSolutions = new ArrayList<>();
		for (IntegerSolution solution : population) {
			// Normalizar los objetivos usando la media y desviación estándar, con valor absoluto
			double normalizedObjective0 = Math.abs((solution.getObjective(0) - (avgObjective0 / 10)) / stdDevObjective0);
			double normalizedObjective1 = Math.abs((solution.getObjective(1) - avgObjective1) / stdDevObjective1);
			// Fitness ponderado
			double weightedScore = 0.9 * normalizedObjective0 + 0.1 * normalizedObjective1; // Normalizar
			solution.setAttribute("WeightedScore", weightedScore);
			prioritizedSolutions.add(solution);
		}

		// Ordena las soluciones por el score ponderado
		prioritizedSolutions.sort(Comparator.comparing(s -> (double) s.getAttribute("WeightedScore")));

		// Selecciona las dos mejores soluciones según el score ponderado
		IntegerSolution bestForObjective0More = prioritizedSolutions.get(0);
		IntegerSolution secondBestForObjective0More = prioritizedSolutions.get(1);

		// Arreglo con las soluciones
		List<IntegerSolution> bestSolutions = new ArrayList<>();
		bestSolutions.add(bestForObjective0More);
		bestSolutions.add(secondBestForObjective0More);
		bestSolutions.add(bestSolution);
		int counter = 0;
		for (IntegerSolution solution : bestSolutions) {
				counter++;
				int n = problem.n;
				System.out.println(" ");
				// Imprimir la solución como matriz
				System.out.println("---------------------------------*");
				System.out.println("Solución: "+ counter);
				// for (int i = 0; i < n; i++) {
				// 	for (int j = 0; j < n; j++) {
				// 		int index = i * n + j;
				// 		System.out.print(
				// 				solution.getVariable(index) + " (" + solution.getVariable(index + (n * n)) + " min.) ");
				// 	}
				// 	System.out.println();
				// }

				double[][] riegoTotal = problem.calcularRiegoTotalCampo(solution);

				// Imprimir el agua optima y real en cada parcela
				// System.out.println("Agua óptima y real:");
				// for (int i = 0; i < n; i++) {
				// 	for (int j = 0; j < n; j++) {
				// 		double aguaOptima = problem.informacionSuelos.get(problem.suelosCampo[i][j]).get("h_campo")
				// 				- problem.informacionSuelos.get(problem.suelosCampo[i][j]).get("h_marchitez")
				// 				+ problem.informacionCultivos.get(problem.cultivosCampo[i][j]).get("agua_requerida");
				// 		System.out.print(aguaOptima + " (" + riegoTotal[i][j] + ") ");
				// 	}
				// 	System.out.println();
				// }

				// Calcular diferencia hídrica de cada parcela
				double[][] desviacionHidrica = new double[n][n];
				double desviacionHidricaReal = 0;
				// Imprimir campo
				// System.out.println("Balance de riego:");
				 for (int i = 0; i < n; i++) {
				 	for (int j = 0; j < n; j++) {
				 		int index = i * n + j;
				 		desviacionHidrica[i][j] = problem.calcularDesviacionHidricaRelativaParcela(index, riegoTotal);
				 		desviacionHidricaReal += Math.abs(desviacionHidrica[i][j]);
						
				 		System.out.print(desviacionHidrica[i][j] + " ");
				 	}
				 	System.out.println();
				 }

				System.out.println("Objective 0 (Diferencia hídrica total): " + solution.getObjective(0));
				System.out.println("Objective 1 (Costo): " + solution.getObjective(1));
				System.out.println("Desviación hídrica real: " + desviacionHidricaReal);
				System.out.println("Cantidad de aspersores: " + contarAspersores(solution));
				exportarIrrigacionCSV(riegoTotal, "riegoTotal_"+counter+".csv");
				exportarAspersoresCSV(solution, "aspersores_"+counter+".csv");
			}

		return bestSolutions;
	}

	private static int contarAspersores(IntegerSolution solution) {
		int n = (int) Math.sqrt(solution.getNumberOfVariables() / 2);
		int aspersores = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int index = i * n + j;
				if (solution.getVariable(index) != 0) {
					aspersores++;
				}
			}
		}
		return aspersores;
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

	private static void exportarIrrigacionCSV(double[][] riegoTotal, String fileName) {
		try {
			FileWriter writer = new FileWriter(fileName);
			for (int i = 0; i < riegoTotal.length; i++) {
				for (int j = 0; j < riegoTotal[0].length; j++) {
					writer.write(riegoTotal[i][j] + ",");
				}
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void exportarAspersoresCSV(IntegerSolution solution, String fileName) {
		try {
			FileWriter writer = new FileWriter(fileName);
			int n = (int) Math.sqrt(solution.getNumberOfVariables() / 2);
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					int index = i * n + j;
					writer.write(solution.getVariable(index) + ",");
				}
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
