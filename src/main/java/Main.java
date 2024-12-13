import java.util.Map;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		// Configuración de parámetros del problema
		int n = 10;
		double alpha = 0.7;
		double beta = 0.3;
		int costoTipo1 = 10;
		int costoTipo2 = 13;
		int costoTipo3 = 15;
		int riegoPorMinuto = 10;
		Map<String, Map<String, Double>> informacionSuelos = GeneracionDatos.obtenerInformacionSuelos();
		Map<String, Map<String, Double>> informacionCultivos = GeneracionDatos.obtenerInformacionCultivos();
		String[][] cultivosCampo = GeneracionDatos.obtenerCultivosCampo(n);
		String[][] suelosCampo = GeneracionDatos.obtenerSuelosCampo(n);

		// Crear una instancia del problema
		Regado problema = new Regado(n, informacionSuelos, informacionCultivos, cultivosCampo, suelosCampo, alpha, beta,
				costoTipo1, costoTipo2, costoTipo3, riegoPorMinuto);

		RegadoRunner runner = new RegadoRunner();

		Scanner scanner = new Scanner(System.in);
		System.out.println("Seleccione una opción:");
		System.out.println("1. Ejecutar el algoritmo una vez");
		System.out.println("2. Ejecutar el algoritmo múltiples veces");
		int opcion = scanner.nextInt();

		switch (opcion) {
		case 1:
			System.out.println("Ejecutando el algoritmo una vez...");
			runner.runAlgorithmOnce(problema);
			break;

		case 2:
			System.out.println("Ingrese el número de ejecuciones:");
			int numEjecuciones = scanner.nextInt();
			runner.runMultipleExecutions(problema, numEjecuciones);
			break;

		case 3:
			// Instanciar el algoritmo Greedy para comparar
			GreedyRegado greedy = new GreedyRegado(n, informacionSuelos, informacionCultivos, cultivosCampo, suelosCampo,
					alpha, beta, costoTipo1, costoTipo2, costoTipo3, riegoPorMinuto);

			greedy.ejecutar();
			break;

		default:
			System.out.println("Opción inválida. Saliendo del programa.");
			break;
		}

		scanner.close();
	}
}
