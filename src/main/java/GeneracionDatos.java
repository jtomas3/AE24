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

		return informacionCultivos;
	}

	static String[][] obtenerCultivosCampo() {
		String[][] cultivosCampo = new String[10][10];

		// Datos ficticios
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				cultivosCampo[i][j] = "cultivo1";
			}
		}

		return cultivosCampo;
	}

	static String[][] obtenerSuelosCampo() {
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
