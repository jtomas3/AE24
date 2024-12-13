import java.util.Map;

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
		Regado problema = new Regado(n, informacionSuelos, informacionCultivos, cultivosCampo, suelosCampo, alpha,
				beta, costoTipo1, costoTipo2, costoTipo3, riegoPorMinuto);

		// Crear y ejecutar el algoritmo
		RegadoRunner runner = new RegadoRunner();
		runner.runAlgorithm(problema);
	}
}
