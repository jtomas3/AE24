import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
	public static void main(String[] args) {
		// Configuración de parámetros del problema
		int n = 40;
		double alpha = 0.9;
		double beta = 0.8;
		int costoTipo1 = 1;
		int costoTipo2 = 2;
		int riegoPorMinuto = 10;
		int tiempoMinimo = 4;
		int tiempoMaximo = 30;
		int tamañoPoblacion = 20;
		int matingPoolSize = (int) Math.floor(tamañoPoblacion * 0.8);
		int offspringPopulationSize = (int) Math.floor(tamañoPoblacion * 0.7);
		int regionCrossoverSize = 3;
		double proporcionGreedy = 2 / 5.0;
		double probabilidadCrossover = 0.7;
		double probabilidadMutacion = 0.02;
		Map<String, Map<String, Double>> informacionSuelos = GeneracionDatos.obtenerInformacionSuelos();
		Map<String, Map<String, Double>> informacionCultivos = GeneracionDatos.obtenerInformacionCultivos();
		String[][] cultivosCampo = GeneracionDatos.obtenerCultivosCampo(n);
		String[][] suelosCampo = GeneracionDatos.obtenerSuelosCampo(n);

		List<int[][]> greedySolutions = new ArrayList<>();

		Scanner scanner = new Scanner(System.in);
		System.out.println("Seleccione una opción:");
		System.out.println("1. Algoritmo Genético");
		System.out.println("2. Algoritmo Genetico empezando con solucion Greedy");
		System.out.println("3. Ejecutar Greedy");
		System.out.println("4. Experimentación");

		int opcion = scanner.nextInt();
		switch (opcion) {
		case 1:
			System.out.println("Algoritmo Genético fue seleccionado...");
			greedySolutions = null;
			break;

		case 2:
			System.out.println("Algoritmo Genetico empezando con solucion Greedy fue seleccionado...");
			greedySolutions = generarSolucionesGreedy(n, tamañoPoblacion, proporcionGreedy, informacionSuelos,
					informacionCultivos, cultivosCampo, suelosCampo, alpha, beta, costoTipo1, costoTipo2,
					riegoPorMinuto, tiempoMinimo, tiempoMaximo);
			break;

		case 3:
			System.out.println("Ejecutando Greedy...");
			new GreedyRegado(n, informacionSuelos, informacionCultivos, cultivosCampo, suelosCampo, alpha, beta,
					costoTipo1, costoTipo2, riegoPorMinuto, tiempoMinimo, tiempoMaximo).ejecutar();
			return;

		case 4:
			System.out.println("Experimentación...");
			ejecutarExperimentacion(n, informacionSuelos, informacionCultivos, cultivosCampo, suelosCampo, alpha, beta,
					costoTipo1, costoTipo2, riegoPorMinuto, greedySolutions, tiempoMaximo, tiempoMinimo);

		default:
			System.out.println("Opción inválida. Saliendo del programa.");
			return;
		}

		// Crear una instancia del problema
		Regado problema = new Regado(n, informacionSuelos, informacionCultivos, cultivosCampo, suelosCampo, alpha, beta,
				costoTipo1, costoTipo2, riegoPorMinuto, greedySolutions, tiempoMaximo, tiempoMinimo);

		RegadoRunner runner = new RegadoRunner();

		System.out.println("Seleccione una opción:");
		System.out.println("1. Ejecutar el algoritmo una vez");
		System.out.println("2. Ejecutar el algoritmo múltiples veces");
		int opcion2 = scanner.nextInt();

		switch (opcion2) {
		case 1:
			System.out.println("Ejecutando el algoritmo una vez...");
			runner.runAlgorithmOnce(problema, tamañoPoblacion, matingPoolSize,
					offspringPopulationSize, regionCrossoverSize, probabilidadCrossover, probabilidadMutacion, -1, -1);
			break;

		case 2:
			System.out.println("Ingrese el número de ejecuciones:");
			int numEjecuciones = scanner.nextInt();
			runner.runMultipleExecutions(problema, numEjecuciones, tamañoPoblacion,
					matingPoolSize, offspringPopulationSize, regionCrossoverSize, probabilidadCrossover,
					probabilidadMutacion);
			break;

		default:
			System.out.println("Opción inválida. Saliendo del programa.");
			break;
		}

		scanner.close();
	}

	private static List<int[][]> generarSolucionesGreedy(int n, int tamañoPoblacion, double proporcionGreedy,
			Map<String, Map<String, Double>> informacionSuelos, Map<String, Map<String, Double>> informacionCultivos,
			String[][] cultivosCampo, String[][] suelosCampo, double alpha, double beta, int costoTipo1, int costoTipo2,
			int riegoPorMinuto, int tiempoMinimo, int tiempoMaximo) {
		System.out.println("Tamaño de la población: " + tamañoPoblacion);
		int cantidadGreedySolutions = (int) Math.floor(tamañoPoblacion * proporcionGreedy);
		System.out.println("Cantidad de soluciones Greedy a generar: " + cantidadGreedySolutions);
		List<int[][]> greedySolutions = new ArrayList<>();

		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<Future<int[][]>> futures = new ArrayList<>();

		for (int i = 0; i < cantidadGreedySolutions; i++) {
			final int greedyIndex = i; // Variable final para usar en la lambda
			futures.add(executor.submit(() -> {
				System.out.println("Iniciando Greedy número " + greedyIndex);
				return new GreedyRegado(n, informacionSuelos, informacionCultivos, cultivosCampo, suelosCampo, alpha,
						beta, costoTipo1, costoTipo2, riegoPorMinuto, tiempoMinimo, tiempoMaximo)
						.ejecutar();
			}));
		}

		// Recoger los resultados de las tareas
		for (int i = 0; i < futures.size(); i++) {
			try {
				System.out.println("Obteniendo resultado de Greedy número " + i);
				greedySolutions.add(futures.get(i).get());
			} catch (Exception e) {
				System.err.println("Error al ejecutar Greedy número " + i + ": " + e.getMessage());
			}
		}

		// Cerrar el executor
		executor.shutdown();
		System.out.println("Generación de soluciones Greedy completada.");
		return greedySolutions;
	}

	public static void ejecutarExperimentacion(int n, Map<String, Map<String, Double>> informacionSuelos,
			Map<String, Map<String, Double>> informacionCultivos, String[][] cultivosCampo, String[][] suelosCampo,
			double alpha, double beta, int costoTipo1, int costoTipo2, int riegoPorMinuto,
			List<int[][]> greedySolutions, int tiempoMaximo, int tiempoMinimo) {
		// Combinaciones de valores a experimentar
		int[] poblaciones = { 20, 40, 70 };
		double[] probabilidadesCrossover = { 0.3, 0.5, 0.7 };
		double[] probabilidadesMutacion = { 0.01, 0.02, 0.05 };
		int ejecucionesPorConfiguracion = 30;

		// Crear directorios para guardar los resultados, crear un directorio por
		// configuracion: conf_1... conf_27
		File dir = new File("resultados");
		dir.mkdir();
		for (int i = 0; i < poblaciones.length; i++) {
			for (int j = 0; j < probabilidadesCrossover.length; j++) {
				for (int k = 0; k < probabilidadesMutacion.length; k++) {
					File subdir = new File("resultados/conf_" + (i * poblaciones.length * probabilidadesCrossover.length
							+ j * probabilidadesCrossover.length + k));
					subdir.mkdir();
				}
			}
		}

		int configuracionActual = 0;

		// Ejecutar experimentación (cada experimento consiste en correr primero Greedy
		// y luego el algoritmo genético)
		for (int tamañoPoblacion : poblaciones) {
			for (double probabilidadCrossover : probabilidadesCrossover) {
				for (double probabilidadMutacion : probabilidadesMutacion) {
					for (int i = 0; i < ejecucionesPorConfiguracion; i++) {
						System.out.println("Experimentando con tamaño de población: " + tamañoPoblacion
								+ ", probabilidad de crossover: " + probabilidadCrossover
								+ ", probabilidad de mutación: " + probabilidadMutacion);
						greedySolutions = generarSolucionesGreedy(n, tamañoPoblacion, 0.4, informacionSuelos,
								informacionCultivos, cultivosCampo, suelosCampo, alpha, beta, costoTipo1, costoTipo2,
								riegoPorMinuto, tiempoMinimo, tiempoMaximo);
						Regado problema = new Regado(n, informacionSuelos, informacionCultivos, cultivosCampo,
								suelosCampo, alpha, beta, costoTipo1, costoTipo2, riegoPorMinuto,
								greedySolutions, tiempoMaximo, tiempoMinimo);
						RegadoRunner runner = new RegadoRunner();

						// mating pool size = 80% de la población pero llevado a un numero par si es
						// necesario
						// offspring population size = 70% de la población pero llevado a un numero par
						// si es necesario
						int matingPoolSize = (int) Math.floor(tamañoPoblacion * 0.8);
						if (matingPoolSize % 2 != 0) {
							matingPoolSize++;
						}
						int offspringPopulationSize = (int) Math.floor(tamañoPoblacion * 0.7);
						if (offspringPopulationSize % 2 != 0) {
							offspringPopulationSize++;
						}

						runner.runAlgorithmOnce(problema, tamañoPoblacion, matingPoolSize,
								offspringPopulationSize, 3, probabilidadCrossover, probabilidadMutacion,
								configuracionActual, i);
					}
					configuracionActual++;
				}
			}
		}
	}
}
