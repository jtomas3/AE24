import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Regado extends AbstractIntegerProblem {
	// Conteo de evaluaciones
	private int cantidadEvaluaciones;

	// Tablero que representa el campo con los cultivos
	private String[][] cultivosCampo;

	// Tablero que representa el campo con los tipo de suelo
	private String[][] suelosCampo;

	// Dimensiones del campo (nxn)
	private int n;

	// Constantes
	private final int COSTO_TIPO_1 = 4;
	private final int COSTO_TIPO_2 = 5;
	private final int COSTO_TIPO_3 = 6;

	// Variable X que controla cuanto riegan los aspersores por minuto
	// - Tipo 1: solo riega una cantidad X por minuto en la
	// parcela donde es posicionado.
	// - Tipo 2: riega X por minuto en la parcela actual, y
	// X · alpha por minuto en las parcelas adyacentes.
	// - Tipo 3: Como el Tipo 2, pero riega a su vez una
	// cantidad X · beta de agua por minuto en parcelas una
	// unidad más distante.
	private final int x = 10;

	// Variables de regado para aspersores: alpha, beta (distancia 1 y 2
	// respectivamente)
	private final double alpha;
	private final double beta;

	// Mapa con key como tipo de suelo y value como un hash con keys:
	// - "h_campo": Capacidad de campo
	// - "h_marchitez": Punto de marchitez
	private final Map<String, Map<String, Double>> informacionSuelos;

	// Mapa con key como tipo de cultivo y value como un hash con keys:
	// - "agua_requerida": Cantidad de agua requerida en 24 hs
	// - "tolerancia_sobre": Factor de tolerancia a sobre irrigación
	// - "tolerancia_infra": Factor de tolerancia a infra irrigación
	private final Map<String, Map<String, Double>> informacionCultivos;

	public Regado(int n, Map<String, Map<String, Double>> informacionSuelos,
			Map<String, Map<String, Double>> informacionCultivos, String[][] cultivosCampo, String[][] sueslosCampo,
			double alpha, double beta) {
		this.n = n;
		this.informacionSuelos = informacionSuelos;
		this.informacionCultivos = informacionCultivos;
		this.cultivosCampo = cultivosCampo;
		this.suelosCampo = sueslosCampo;
		this.alpha = alpha;
		this.beta = beta;

		setNumberOfVariables(n * n * 2);
		setNumberOfObjectives(2);
		setName("Regado");

		List<Integer> lowerLimit = new ArrayList<>(getNumberOfVariables());
		List<Integer> upperLimit = new ArrayList<>(getNumberOfVariables());
		for (int i = 0; i < getNumberOfVariables(); i++) {
            if (i <= getNumberOfVariables() / 2) {
                // Tipos de aspersores
                lowerLimit.add(0);
                upperLimit.add(3);
            } else {
                // Tiempos de riego en minuto
                lowerLimit.add(0);
                upperLimit.add(10);
            }
		}

		// Los valores posibles para cada una de las nxn variables (parcelas) son 0 si
		// no hay aspersor,
		// y 1, 2 o 3 si hay un aspersor de tipo 1, 2 o 3 respectivamente.
		setVariableBounds(lowerLimit, upperLimit);
	}

	@Override
	public void evaluate(IntegerSolution solution) {
		cantidadEvaluaciones++;
		int costoTotal = 0;
		double totalDiferenciaHidrica = 0.0;
		double[][] riegoTotal = calcularRiegoTotalCampo(solution);

		// Cálculo de costo y desviación. Solo vemos los aspersores, los tiempos son consecuencia
		for (int i = 0; i < solution.getNumberOfVariables() / 2; i++) {
			int tipoAspersor = solution.getVariable(i);

			costoTotal += calcularCosto(tipoAspersor); // Calcula el costo basado en el tipo
			totalDiferenciaHidrica += calcularDesviacionHidricaParcela(solution, i, riegoTotal); // Calcula la desviación hidrica
		}

		if ((cantidadEvaluaciones % 10000) == 0) {
			System.out.println("Evaluación numero " + cantidadEvaluaciones);
			// Imprimir solucion actual en forma matricial
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					int index = i * n + j;
					System.out.print(solution.getVariable(index) + " ");
				}
				System.out.println();
			}
			System.out.println("----");
			System.out.println("Costo total: " + costoTotal);
			System.out.println("Desviación total: " + totalDiferenciaHidrica);
		}

		// Establecer los objetivos
		solution.setObjective(0, totalDiferenciaHidrica);
		solution.setObjective(1, costoTotal);
	}

	private int calcularCosto(int tipoAspersor) {
		switch (tipoAspersor) {
		case 1:
			return COSTO_TIPO_1;
		case 2:
			return COSTO_TIPO_2;
		case 3:
			return COSTO_TIPO_3;
		default:
			return 0;
		}
	}

	private double[][] calcularRiegoTotalCampo(IntegerSolution solution) {
		// Matriz de nxn que representa riego de cada parcela
		double[][] riegoTotal = new double[n][n];

		// Recorremos todo el campo, calculando el riego total.
		// Cada vez que encontramos un aspersor, actualizamos el riego de cada una
		// de las parcelas afectadas por el aspersor, teniendo en cuenta que no se salga
		// de los límites del campo.
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int index = i * n + j; // Convertir coordenadas 2D a índice lineal para acceder a la solución
				int tipoAspersor = solution.getVariable(index);
				if (tipoAspersor != 0) {
                    int tiempoEncendido = solution.getVariable(index + n * n);
					riegoTotal[i][j] += x * tiempoEncendido; // Riego en la parcela actual
					if (tipoAspersor == 2 || tipoAspersor == 3) {
						// Riego en las parcelas adyacentes (a distancia 1)
						if (i - 1 >= 0)
							riegoTotal[i - 1][j] += x * alpha * tiempoEncendido;
						if (i + 1 < n)
							riegoTotal[i + 1][j] += x * alpha * tiempoEncendido;
						if (j - 1 >= 0)
							riegoTotal[i][j - 1] += x * alpha * tiempoEncendido;
						if (j + 1 < n)
							riegoTotal[i][j + 1] += x * alpha * tiempoEncendido;
					}
					if (tipoAspersor == 3) {
						// Riego en las parcelas (a distancia 2)
						if (i - 2 >= 0)
							riegoTotal[i - 2][j] += x * beta * tiempoEncendido;
						if (i + 2 < n)
							riegoTotal[i + 2][j] += x * beta * tiempoEncendido;
						if (j - 2 >= 0)
							riegoTotal[i][j - 2] += x * beta * tiempoEncendido;
						if (j + 2 < n)
							riegoTotal[i][j + 2] += x * beta * tiempoEncendido;
						// Diagonales
						if (i - 1 >= 0 && j - 1 >= 0)
							riegoTotal[i - 1][j - 1] += x * beta * tiempoEncendido;
						if (i - 1 >= 0 && j + 1 < n)
							riegoTotal[i - 1][j + 1] += x * beta * tiempoEncendido;
						if (i + 1 < n && j - 1 >= 0)
							riegoTotal[i + 1][j - 1] += x * beta * tiempoEncendido;
						if (i + 1 < n && j + 1 < n)
							riegoTotal[i + 1][j + 1] += x * beta * tiempoEncendido;
					}
				}
			}
		}

		return riegoTotal;
	}

	private double calcularDesviacionHidricaParcela(IntegerSolution solution, int index, double[][] riegoTotal) {
		double desviacionTotal = 0.0;
		int indice_i_parcela = index / n;
		int indice_j_parcela = index % n;

		// Calcular la desviación hídrica para la parcela actual
		String tipoSuelo = suelosCampo[indice_i_parcela][indice_j_parcela];
		String tipoCultivo = cultivosCampo[indice_i_parcela][indice_j_parcela];
		double aguaRequerida = informacionCultivos.get(tipoCultivo).get("agua_requerida");
		double toleranciaSobre = informacionCultivos.get(tipoCultivo).get("tolerancia_sobre");
		double toleranciaInfra = informacionCultivos.get(tipoCultivo).get("tolerancia_infra");
		double capacidadCampo = informacionSuelos.get(tipoSuelo).get("h_campo");
		double puntoMarchitez = informacionSuelos.get(tipoSuelo).get("h_marchitez");
		double aguaReal = riegoTotal[indice_i_parcela][indice_j_parcela];

		// Calcular cantidad de agua optima
		double aguaOptima = capacidadCampo - puntoMarchitez + aguaRequerida;

		// Calcular desviación hídrica y elevar segun la tolerancia
		if (aguaReal > aguaOptima) {
			desviacionTotal += Math.pow(aguaReal - aguaOptima, toleranciaSobre);
		} else {
			desviacionTotal += Math.pow(aguaOptima - aguaReal, toleranciaInfra);
		}

		return desviacionTotal;
	}

	// Igual que la funcion calcularDesviacionHidricaParcela pero esta solo se usa para imprimir la tabla final, mostrando donde
	// faltó agua y donde sobró (incluyendo signo negativo).
	private double calcularDesviacionHidricaRelativaParcela(IntegerSolution solution, int index, double[][] riegoTotal) {
		double desviacionTotal = 0.0;
		int indice_i_parcela = index / n;
		int indice_j_parcela = index % n;

		// Calcular la desviación hídrica para la parcela actual
		String tipoSuelo = suelosCampo[indice_i_parcela][indice_j_parcela];
		String tipoCultivo = cultivosCampo[indice_i_parcela][indice_j_parcela];
		double aguaRequerida = informacionCultivos.get(tipoCultivo).get("agua_requerida");
		double toleranciaSobre = informacionCultivos.get(tipoCultivo).get("tolerancia_sobre");
		double toleranciaInfra = informacionCultivos.get(tipoCultivo).get("tolerancia_infra");
		double capacidadCampo = informacionSuelos.get(tipoSuelo).get("h_campo");
		double puntoMarchitez = informacionSuelos.get(tipoSuelo).get("h_marchitez");
		double aguaReal = riegoTotal[indice_i_parcela][indice_j_parcela];

		// Calcular cantidad de agua optima
		double aguaOptima = capacidadCampo - puntoMarchitez + aguaRequerida;

		desviacionTotal += Math.pow(aguaReal - aguaOptima, toleranciaSobre);

		return desviacionTotal;
	}

	public static void main(String[] args) {
		int n = 10; // Dimensiones del campo (10x10 por ejemplo)
		Map<String, Map<String, Double>> informacionSuelos = obtenerInformacionSuelos();
		Map<String, Map<String, Double>> informacionCultivos = obtenerInformacionCultivos();
		String[][] cultivosCampo = obtenerCultivosCampo();
		String[][] suelosCampo = obtenerSuelosCampo();
		double alpha = 0.7;
		double beta = 0.3;

		// Creación del problema
		Regado problema = new Regado(n, informacionSuelos, informacionCultivos, cultivosCampo, suelosCampo, alpha,
				beta);

		// Configuración de los operadores de cruce, mutación y selección
		CrossoverOperator<IntegerSolution> crossover = new IntegerSBXCrossover(0.9, 20.0);
		MutationOperator<IntegerSolution> mutation = new IntegerPolynomialMutation(
				10.0 / problema.getNumberOfVariables(), 20.0);
		SelectionOperator<List<IntegerSolution>, IntegerSolution> selection = new BinaryTournamentSelection<>(
				new RankingAndCrowdingDistanceComparator<>());

		// Configuración del algoritmo NSGA-II
		Algorithm<List<IntegerSolution>> algorithm = new NSGAIIBuilder<IntegerSolution>(problema, crossover, mutation,
				2000).setSelectionOperator(selection).setMaxEvaluations(300000).build();

		// Ejecución del algoritmo
		algorithm.run();

		// Obtención de la solución
		List<IntegerSolution> population = algorithm.getResult();
		IntegerSolution bestSolution = population.get(0);
		for (int i = 1; i < population.size(); i++) {
			if (population.get(i).getObjective(0) < bestSolution.getObjective(0)) {
				bestSolution = population.get(i);
			}
		}

		// Imprimir la solución como matriz
		System.out.println("Solución:");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int index = i * n + j;
				System.out.print(bestSolution.getVariable(index) + " (" + bestSolution.getVariable(index + (n*n)) + " min.) ");
			}
			System.out.println();
		}
		
		// Calcular diferencia hídrica de cada parcela
		double[][] riegoTotal = problema.calcularRiegoTotalCampo(bestSolution);
		double[][] desviacionHidrica = new double[n][n];
		// Imprimir campo
        System.out.println("Balance de riego:");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int index = i * n + j;
				desviacionHidrica[i][j] = problema.calcularDesviacionHidricaRelativaParcela(bestSolution, index, riegoTotal);
				System.out.print(desviacionHidrica[i][j] + " ");
			}
			System.out.println();
		}

		System.out.println("Objective 1 (Diferencia hídrica total): " + bestSolution.getObjective(0));
		System.out.println("Objective 2 (Costo): " + bestSolution.getObjective(1));
	}

	// Métodos ficticios para obtener información, reemplaza con tus métodos o datos
	// reales
	private static Map<String, Map<String, Double>> obtenerInformacionSuelos() {
		Map<String, Map<String, Double>> informacionSuelos = new HashMap<>();

		// Datos ficticios
		Map<String, Double> tipoSuelo1 = new HashMap<>();
		tipoSuelo1.put("h_campo", 25.0);
		tipoSuelo1.put("h_marchitez", 12.0);
		informacionSuelos.put("tipo1", tipoSuelo1);
		
		Map<String, Double> tipoSuelo2 = new HashMap<>();
		tipoSuelo2.put("h_campo", 90.0);
		tipoSuelo2.put("h_marchitez", 80.0);
		informacionSuelos.put("tipo2", tipoSuelo2);

		return informacionSuelos;
	}

	private static Map<String, Map<String, Double>> obtenerInformacionCultivos() {
		Map<String, Map<String, Double>> informacionCultivos = new HashMap<>();

		// Datos ficticios
		Map<String, Double> tipoCultivo1 = new HashMap<>();
		tipoCultivo1.put("agua_requerida", 50.0);
		tipoCultivo1.put("tolerancia_sobre", 1.0);
		tipoCultivo1.put("tolerancia_infra", 1.0);
		informacionCultivos.put("cultivo1", tipoCultivo1);

		return informacionCultivos;
	}

	private static String[][] obtenerCultivosCampo() {
		String[][] cultivosCampo = new String[10][10];

		// Datos ficticios
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				cultivosCampo[i][j] = "cultivo1";
			}
		}

		return cultivosCampo;
	}

	private static String[][] obtenerSuelosCampo() {
		String[][] suelosCampo = new String[10][10];

		// Datos ficticios
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				if (j < 5)
					suelosCampo[i][j] = "tipo1";
				else
					suelosCampo[i][j] = "tipo2";
			}
		}

		return suelosCampo;
	}
}
