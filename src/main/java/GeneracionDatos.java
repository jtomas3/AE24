import java.util.HashMap;
import java.util.Map;

public class GeneracionDatos {
	static Map<String, Map<String, Double>> obtenerInformacionSuelos() {
		Map<String, Map<String, Double>> informacionSuelos = new HashMap<>();

		// Datos reales para suelo Franco
		Map<String, Double> tipoSueloFranco = new HashMap<>();
		tipoSueloFranco.put("h_campo", 27.5); // Media entre 25% y 30%
		tipoSueloFranco.put("h_marchitez", 12.5); // Media entre 10% y 15%
		informacionSuelos.put("Franco", tipoSueloFranco);

		// Datos reales para suelo Arenoso
		Map<String, Double> tipoSueloArenoso = new HashMap<>();
		tipoSueloArenoso.put("h_campo", 12.5); // Media entre 10% y 15%
		tipoSueloArenoso.put("h_marchitez", 4.0); // Media entre 3% y 5%
		informacionSuelos.put("Arenoso", tipoSueloArenoso);

		return informacionSuelos;
	}

	static Map<String, Map<String, Double>> obtenerInformacionCultivos() {
		Map<String, Map<String, Double>> informacionCultivos = new HashMap<>();

		Map<String, Double> tipoCultivoArroz = new HashMap<>();
		tipoCultivoArroz.put("agua_requerida", 125.0);
		tipoCultivoArroz.put("tolerancia_sobre", 1.05);
		tipoCultivoArroz.put("tolerancia_infra", 1.01);
		// tipoCultivoArroz.put("tolerancia_sobre", 1.0);
		// tipoCultivoArroz.put("tolerancia_infra", 1.0);
		informacionCultivos.put("Arroz", tipoCultivoArroz);

		Map<String, Double> tipoCultivoPapa = new HashMap<>();
		tipoCultivoPapa.put("agua_requerida", 150.0);
		tipoCultivoPapa.put("tolerancia_sobre", 1.03);
		tipoCultivoPapa.put("tolerancia_infra", 1.01);
		// tipoCultivoPapa.put("tolerancia_sobre", 1.0);
		// tipoCultivoPapa.put("tolerancia_infra", 1.0);
		informacionCultivos.put("Papa", tipoCultivoPapa);

		Map<String, Double> tipoCultivoTomate = new HashMap<>();
		tipoCultivoTomate.put("agua_requerida", 240.0);
		tipoCultivoTomate.put("tolerancia_sobre", 1.03);
		tipoCultivoTomate.put("tolerancia_infra", 1.01);
		// tipoCultivoTomate.put("tolerancia_sobre", 1.0);
		// tipoCultivoTomate.put("tolerancia_infra", 1.0);
		informacionCultivos.put("Tomate", tipoCultivoTomate);

		return informacionCultivos;
	}

	static String[][] obtenerCultivosCampo(int n) {
		String[][] cultivosCampo = new String[n][n];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i < n / 2) {
					if (j < n / 2) {
						cultivosCampo[i][j] = "Papa";
					} else {
						cultivosCampo[i][j] = "Tomate";
					}
				} else {
						cultivosCampo[i][j] = "Arroz";
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
					suelosCampo[i][j] = "Arenoso";
				} else {
					suelosCampo[i][j] = "Franco";
				}
			}
		}

		return suelosCampo;
	}
}
