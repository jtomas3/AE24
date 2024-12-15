import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Regado extends AbstractIntegerProblem {
	// Tablero que representa el tipo de cultivo en cada parcela del campo
	String[][] cultivosCampo;

	// Tablero que representa el tipo de suelo en cada parcela del campo
	String[][] suelosCampo;

	// Dimensiones del campo (nxn)
	int n;

	// Constantes
	private final int COSTO_TIPO_1;
	private final int COSTO_TIPO_2;
	private final int COSTO_TIPO_3;

	// Parametro X que controla cuanto riegan los aspersores por minuto
	// - Tipo 1: solo riega una cantidad X por minuto en la
	// parcela donde es posicionado.
	// - Tipo 2: riega X por minuto en la parcela actual, y
	// X · alpha por minuto en las parcelas adyacentes.
	// - Tipo 3: Como el Tipo 2, pero riega a su vez una
	// cantidad X · beta de agua por minuto en parcelas una
	// unidad más distante.
	private final int x;

	// Parametros alpha, beta de los aspersores (distancia 1 y 2 respectivamente)
	private final double alpha;
	private final double beta;

	// Soluciones dada por el algoritmo greedy. (Opcional)
	private List<int[][]> greedySolutions;
	private int indiceGreedySolution = 0;

	// Mapa con key como tipo de suelo y value como un hash con keys:
	// - "h_campo": Capacidad de campo
	// - "h_marchitez": Punto de marchitez
	// Ejemplo: { "tipo1": { "h_campo": 25.0, "h_marchitez": 12.0 } }
	final Map<String, Map<String, Double>> informacionSuelos;

	// Mapa con key como tipo de cultivo y value como un hash con keys:
	// - "agua_requerida": Cantidad de agua requerida en 24 hs
	// - "tolerancia_sobre": Factor de tolerancia a sobre irrigación
	// - "tolerancia_infra": Factor de tolerancia a infra irrigación
	// Ejemplo: { "cultivo1": { "agua_requerida": 50.0, "tolerancia_sobre": 1.0,
	// "tolerancia_infra": 1.0 } }
	final Map<String, Map<String, Double>> informacionCultivos;

	public Regado(int n, Map<String, Map<String, Double>> informacionSuelos,
			Map<String, Map<String, Double>> informacionCultivos, String[][] cultivosCampo, String[][] sueslosCampo,
			double alpha, double beta, int costoTipo1, int costoTipo2, int costoTipo3, int riegoPorMinuto, List<int[][]> greedySolutions, int tiempoMaximo) {
		this.n = n;
		this.informacionSuelos = informacionSuelos;
		this.informacionCultivos = informacionCultivos;
		this.cultivosCampo = cultivosCampo;
		this.suelosCampo = sueslosCampo;
		this.alpha = alpha;
		this.beta = beta;
		this.COSTO_TIPO_1 = costoTipo1;
		this.COSTO_TIPO_2 = costoTipo2;
		this.COSTO_TIPO_3 = costoTipo3;
		this.x = riegoPorMinuto;
		this.greedySolutions = greedySolutions;

		setNumberOfVariables(n * n * 2);
		setNumberOfObjectives(2);
		setName("Regado");

		List<Integer> lowerLimit = new ArrayList<>(getNumberOfVariables());
		List<Integer> upperLimit = new ArrayList<>(getNumberOfVariables());
		for (int i = 0; i < getNumberOfVariables(); i++) {
			if (i < getNumberOfVariables() / 2) {
				// Tipos de aspersores
				lowerLimit.add(0);
				upperLimit.add(2);
			} else {
				// Tiempos de riego en minuto
				lowerLimit.add(0);
				upperLimit.add(tiempoMaximo);
			}
		}

		// Los valores posibles para cada una de las nxn variables (parcelas) son 0 si
		// no hay aspersor,
		// y 1, 2 o 3 si hay un aspersor de tipo 1, 2 o 3 respectivamente.
		setVariableBounds(lowerLimit, upperLimit);
	}

	@Override
	public IntegerSolution createSolution() {
		if (greedySolutions != null && indiceGreedySolution < greedySolutions.size()) {
			int[][] greedySolution = greedySolutions.get(indiceGreedySolution);
			indiceGreedySolution++;
			return new CustomDefaultIntegerSolution(this.getVariableBounds(), this.getNumberOfObjectives(), greedySolution);
		}
		return new CustomDefaultIntegerSolution(this.getVariableBounds(), this.getNumberOfObjectives(), null);
	}

	@Override
	public void evaluate(IntegerSolution solution) {
		int costoTotal = 0;
		double totalDiferenciaHidrica = 0.0;
		double[][] riegoTotal = calcularRiegoTotalCampo(solution);

		// Cálculo de costo y diferencia hidrica. Solo vemos los aspersores, los tiempos
		// son consecuencia
		for (int i = 0; i < solution.getNumberOfVariables() / 2; i++) {
			int tipoAspersor = solution.getVariable(i);

			// Si no hay aspersores en los alrededores, se coloca un aspersor tipo 1 con probabilidad 4%
			int random = (int) (Math.random() * 100);
			// Heuristica: Se checkea que no se trate del caso donde no hay aspersores en un 3x3.
			if (tipoAspersor == 0 && random < 6) {
				// Convertir índice lineal a coordenadas 2D
				int row = i / n;
				int col = i % n;
				boolean foundAspersor = false;
				for (int dRow = -1; dRow <= 1; dRow++) {
							for (int dCol = -1; dCol <= 1; dCol++) {
									if (dRow == 0 && dCol == 0) continue; // Saltar la parcela actual
									int neighborRow = row + dRow;
									int neighborCol = col + dCol;
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
					solution.setVariable(i, 2);
					solution.setVariable(i + n * n, 10); // Establecer tiempo de riego a 10 default
					tipoAspersor = 1; // Actualizar el tipo de aspersor para continuar con la evaluación
				}
			}

			costoTotal += calcularCosto(tipoAspersor); // Calcula el costo basado en el tipo
			totalDiferenciaHidrica += calcularDesviacionHidricaParcela(solution, i, riegoTotal); // Calcula la
																									// desviación
																									// hidrica
		}

//		if ((cantidadEvaluaciones % 10000) == 0) {
//			System.out.println("Evaluación numero " + cantidadEvaluaciones);
//			// Imprimir solucion actual en forma matricial
//			for (int i = 0; i < n; i++) {
//				for (int j = 0; j < n; j++) {
//					int index = i * n + j;
//					System.out.print(solution.getVariable(index) + " ");
//				}
//				System.out.println();
//			}
//			System.out.println("----");
//			System.out.println("Costo total: " + costoTotal);
//			System.out.println("Desviación total: " + totalDiferenciaHidrica);
//		}

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

	double[][] calcularRiegoTotalCampo(IntegerSolution solution) {
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
					if (tipoAspersor == 1 || tipoAspersor == 2) {
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
					if (tipoAspersor == 2) {
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

	// Igual que la funcion calcularDesviacionHidricaParcela pero esta solo se usa
	// para imprimir la tabla final, mostrando donde
	// faltó agua y donde sobró (incluyendo signo negativo).
	double calcularDesviacionHidricaRelativaParcela(IntegerSolution solution, int index, double[][] riegoTotal) {
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

		if (aguaReal > aguaOptima) {
			desviacionTotal += Math.pow(aguaReal - aguaOptima, toleranciaSobre);
		} else {
			desviacionTotal -= Math.pow(aguaOptima - aguaReal, toleranciaInfra);
		}

		return desviacionTotal;
	}
}
