import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.List;

public class RegadoRunner {
	public void runAlgorithm(Regado problem) {
		// Configuración de los operadores
		CrossoverOperator<IntegerSolution> crossover = new IntegerSBXCrossover(0.95, 20.0);
		MutationOperator<IntegerSolution> mutation = new IntegerPolynomialMutation(0.15, 8.0);
		SelectionOperator<List<IntegerSolution>, IntegerSolution> selection = new BinaryTournamentSelection<>(
				new RankingAndCrowdingDistanceComparator<>());

		// Creación del algoritmo NSGA-II
		CustomNSGAII<IntegerSolution> algorithm = new CustomNSGAII<>(problem, 1000000, 1000, 1000, 1000, crossover,
				mutation, selection, new SequentialSolutionListEvaluator<>());

		System.out.println("Comenzando ejecución del algoritmo...");
		// Ejecutar el algoritmo
		algorithm.run();

		// Exportar datos de evolución
		algorithm.exportEvolutionDataToCSV("evolutionData.csv");
		algorithm.exportAvgObjectivesToCSV("objectiveAverages.csv");

		// Obtención de la solución
		List<IntegerSolution> population = algorithm.getResult();
		IntegerSolution bestSolution = population.get(0);
		for (int i = 1; i < population.size(); i++) {
			if (population.get(i).getObjective(0) < bestSolution.getObjective(0)) {
				bestSolution = population.get(i);
			}
		}

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
				int index = i * n + j;
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
}
