import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Regado extends AbstractIntegerProblem {
	// Conteo de evaluaciones
	private int cantidadEvaluaciones = 0;

	// Tablero que representa el tipo de cultivo en cada parcela del campo
	String[][] cultivosCampo;

	// Tablero que representa el tipo de suelo en cada parcela del campo
	String[][] suelosCampo;

	// Dimensiones del campo (nxn)
	int n;

	// Constantes
	private final int COSTO_TIPO_1 = 1;
	private final int COSTO_TIPO_2 = 3;
	private final int COSTO_TIPO_3 = 5;

	// Parametro X que controla cuanto riegan los aspersores por minuto
	// - Tipo 1: solo riega una cantidad X por minuto en la
	// parcela donde es posicionado.
	// - Tipo 2: riega X por minuto en la parcela actual, y
	// X · alpha por minuto en las parcelas adyacentes.
	// - Tipo 3: Como el Tipo 2, pero riega a su vez una
	// cantidad X · beta de agua por minuto en parcelas una
	// unidad más distante.
	private final int x = 10;

	// Parametros alpha, beta de los aspersores (distancia 1 y 2 respectivamente)
	private final double alpha;
	private final double beta;

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
			if (i < getNumberOfVariables() / 2) {
				// Tipos de aspersores
				lowerLimit.add(0);
				upperLimit.add(3);
			} else {
				// Tiempos de riego en minuto
				lowerLimit.add(0);
				upperLimit.add(60);
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
		if (cantidadEvaluaciones % 10000 == 0) {
			System.out.println("Evaluación numero " + cantidadEvaluaciones + " - Costo: " + solution.getObjective(1)
					+ " - Desviación: " + solution.getObjective(0));
		}

		int costoTotal = 0;
		double totalDiferenciaHidrica = 0.0;
		double[][] riegoTotal = calcularRiegoTotalCampo(solution);

		// Cálculo de costo y diferencia hidrica. Solo vemos los aspersores, los tiempos
		// son consecuencia
		for (int i = 0; i < solution.getNumberOfVariables() / 2; i++) {
			int tipoAspersor = solution.getVariable(i);

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

		desviacionTotal += Math.pow(aguaReal - aguaOptima, toleranciaSobre);

		return desviacionTotal;
	}
}
