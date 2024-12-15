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
		int costoTipo2 = 1;
		int costoTipo3 = 1;
		int riegoPorMinuto = 25;
		Map<String, Map<String, Double>> informacionSuelos = GeneracionDatos.obtenerInformacionSuelos();
		Map<String, Map<String, Double>> informacionCultivos = GeneracionDatos.obtenerInformacionCultivos();
		String[][] cultivosCampo = GeneracionDatos.obtenerCultivosCampo(n);
		String[][] suelosCampo = GeneracionDatos.obtenerSuelosCampo(n);
		// TODO: Pasarlo como parametro a Regado.java (está duplicado)
		int tiempoMaximo = 30;
		int tamañoPoblacion = 50;

		// Parametros
		CalcularMaximos calculador = new CalcularMaximos(n, informacionSuelos, informacionCultivos, cultivosCampo,
				suelosCampo, alpha, beta, costoTipo1, costoTipo2, costoTipo3, riegoPorMinuto);
		int costoMaximo = calculador.calcularCostoMaximo(n, costoTipo1, costoTipo2, costoTipo3);
		double desbalanceMaximo = calculador.calcularDesbalanceMaximo(n, tiempoMaximo);

		System.out.println("Costo máximo: " + costoMaximo);
		System.out.println("Desbalance máximo: " + desbalanceMaximo);

		List<int[][]> greedySolutions = new ArrayList<>();

		Scanner scanner = new Scanner(System.in);
		System.out.println("Seleccione una opción:");
		System.out.println("1. Algoritmo Genético");
		System.out.println("2. Algoritmo Genetico empezando con solucion Greedy");
		System.out.println("3. Ejecutar Greedy");

		int opcion = scanner.nextInt();
		switch (opcion) {
		case 1:
			System.out.println("Algoritmo Genético fue seleccionado...");
			greedySolutions = null;
			break;

		case 2:
			System.out.println("Algoritmo Genetico empezando con solucion Greedy fue seleccionado...");
			System.out.println("Tamaño de la población: " + tamañoPoblacion);
			// Tomar 4/5 de la población como soluciones Greedy. Castear a int para
			// redondear hacia abajo.
			int cantidadGreedySolutions = (int) Math.floor(tamañoPoblacion * 3 / 5.0);
			System.out.println("Cantidad de soluciones Greedy a generar: " + cantidadGreedySolutions);

			ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			List<Future<int[][]>> futures = new ArrayList<>();

			for (int i = 0; i < cantidadGreedySolutions; i++) {
				final int greedyIndex = i; // Variable final para usar en la lambda
				futures.add(executor.submit(() -> {
					System.out.println("Iniciando Greedy número " + greedyIndex);
					return new GreedyRegado(n, informacionSuelos, informacionCultivos, cultivosCampo, suelosCampo,
							alpha, beta, costoTipo1, costoTipo2, costoTipo3, riegoPorMinuto).ejecutar();
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
			break;

		case 3:
			System.out.println("Ejecutando Greedy...");
			new GreedyRegado(n, informacionSuelos, informacionCultivos, cultivosCampo, suelosCampo, alpha, beta,
					costoTipo1, costoTipo2, costoTipo3, riegoPorMinuto).ejecutar();
			return;

		default:
			System.out.println("Opción inválida. Saliendo del programa.");
			return;
		}

		// Crear una instancia del problema
		Regado problema = new Regado(n, informacionSuelos, informacionCultivos, cultivosCampo, suelosCampo, alpha, beta,
				costoTipo1, costoTipo2, costoTipo3, riegoPorMinuto, greedySolutions);

		RegadoRunner runner = new RegadoRunner();

		System.out.println("Seleccione una opción:");
		System.out.println("1. Ejecutar el algoritmo una vez");
		System.out.println("2. Ejecutar el algoritmo múltiples veces");
		int opcion2 = scanner.nextInt();

		switch (opcion2) {
		case 1:
			System.out.println("Ejecutando el algoritmo una vez...");
			runner.runAlgorithmOnce(problema, desbalanceMaximo, costoMaximo);
			break;

		case 2:
			System.out.println("Ingrese el número de ejecuciones:");
			int numEjecuciones = scanner.nextInt();
			runner.runMultipleExecutions(problema, numEjecuciones, desbalanceMaximo, costoMaximo);
			break;

		default:
			System.out.println("Opción inválida. Saliendo del programa.");
			break;
		}

		scanner.close();
	}
}
