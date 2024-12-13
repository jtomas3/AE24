import java.util.HashMap;
import java.util.Map;

public class GeneracionDatos {
	static Map<String, Map<String, Double>> obtenerInformacionSuelos() {
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

		Map<String, Double> tipoSuelo3 = new HashMap<>();
		tipoSuelo3.put("h_campo", 50.0);
		tipoSuelo3.put("h_marchitez", 30.0);
		informacionSuelos.put("tipo3", tipoSuelo3);

		Map<String, Double> tipoSuelo4 = new HashMap<>();
		tipoSuelo4.put("h_campo", 40.0);
		tipoSuelo4.put("h_marchitez", 20.0);
		informacionSuelos.put("tipo4", tipoSuelo4);

		return informacionSuelos;
	}

	static Map<String, Map<String, Double>> obtenerInformacionCultivos() {
		Map<String, Map<String, Double>> informacionCultivos = new HashMap<>();

		// Datos ficticios
		Map<String, Double> tipoCultivo1 = new HashMap<>();
		tipoCultivo1.put("agua_requerida", 250.0);
		tipoCultivo1.put("tolerancia_sobre", 1.0);
		tipoCultivo1.put("tolerancia_infra", 1.0);
		informacionCultivos.put("cultivo1", tipoCultivo1);

		Map<String, Double> tipoCultivo2 = new HashMap<>();
		tipoCultivo2.put("agua_requerida", 300.0);
		tipoCultivo2.put("tolerancia_sobre", 1.5);
		tipoCultivo2.put("tolerancia_infra", 0.8);
		informacionCultivos.put("cultivo2", tipoCultivo2);

		Map<String, Double> tipoCultivo3 = new HashMap<>();
		tipoCultivo3.put("agua_requerida", 200.0);
		tipoCultivo3.put("tolerancia_sobre", 1.2);
		tipoCultivo3.put("tolerancia_infra", 1.1);
		informacionCultivos.put("cultivo3", tipoCultivo3);

		Map<String, Double> tipoCultivo4 = new HashMap<>();
		tipoCultivo4.put("agua_requerida", 150.0);
		tipoCultivo4.put("tolerancia_sobre", 1.3);
		tipoCultivo4.put("tolerancia_infra", 1.2);
		informacionCultivos.put("cultivo4", tipoCultivo4);

		return informacionCultivos;
	}

	static String[][] obtenerCultivosCampo(int n) {
		String[][] cultivosCampo = new String[n][n];

		// Datos ficticios
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i < n / 2) {
					if (j < n / 2) {
						cultivosCampo[i][j] = "cultivo1";
					} else {
						cultivosCampo[i][j] = "cultivo2";
					}
				} else {
					if (j < n / 2) {
						cultivosCampo[i][j] = "cultivo3";
					} else {
						cultivosCampo[i][j] = "cultivo4";
					}
				}
			}
		}

		return cultivosCampo;
	}

	static String[][] obtenerSuelosCampo(int n) {
		String[][] suelosCampo = new String[n][n];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i < n / 2) {
					if (j < n / 2) {
						suelosCampo[i][j] = "tipo1";
					} else {
						suelosCampo[i][j] = "tipo2";
					}
				} else {
					if (j < n / 2) {
						suelosCampo[i][j] = "tipo3";
					} else {
						suelosCampo[i][j] = "tipo4";
					}
				}
			}
		}

		return suelosCampo;
	}
}
