import java.util.Map;

public class Main {
	public static void main(String[] args) {
		// Configuración de parámetros del problema
		int n = 10;
		double alpha = 0.7;
		double beta = 0.3;
		Map<String, Map<String, Double>> informacionSuelos = GeneracionDatos.obtenerInformacionSuelos();
		Map<String, Map<String, Double>> informacionCultivos = GeneracionDatos.obtenerInformacionCultivos();
		String[][] cultivosCampo = GeneracionDatos.obtenerCultivosCampo();
		String[][] suelosCampo = GeneracionDatos.obtenerSuelosCampo();

		// Crear una instancia del problema
		Regado problema = new Regado(n, informacionSuelos, informacionCultivos, cultivosCampo, suelosCampo, alpha,
				beta);

		// Crear y ejecutar el algoritmo
		RegadoRunner runner = new RegadoRunner();
		runner.runAlgorithm(problema);
	}
}
