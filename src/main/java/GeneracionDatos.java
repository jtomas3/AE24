import java.util.HashMap;
import java.util.Map;

public class GeneracionDatos {
    static Map<String, Map<String, Double>> obtenerInformacionSuelos() {
        Map<String, Map<String, Double>> informacionSuelos = new HashMap<>();

        // Datos de suelos reales
        Map<String, Double> sueloArcilloso = new HashMap<>();
        sueloArcilloso.put("h_campo", 200.0);
        sueloArcilloso.put("h_marchitez", 100.0);
        informacionSuelos.put("Arcilloso", sueloArcilloso);

        Map<String, Double> sueloArenoso = new HashMap<>();
        sueloArenoso.put("h_campo", 100.0);
        sueloArenoso.put("h_marchitez", 50.0);
        informacionSuelos.put("Arenoso", sueloArenoso);

        Map<String, Double> sueloFranco = new HashMap<>();
        sueloFranco.put("h_campo", 150.0);
        sueloFranco.put("h_marchitez", 75.0);
        informacionSuelos.put("Franco", sueloFranco);

        Map<String, Double> sueloLimoso = new HashMap<>();
        sueloLimoso.put("h_campo", 180.0);
        sueloLimoso.put("h_marchitez", 90.0);
        informacionSuelos.put("Limoso", sueloLimoso);

        return informacionSuelos;
    }

    static Map<String, Map<String, Double>> obtenerInformacionCultivos() {
        Map<String, Map<String, Double>> informacionCultivos = new HashMap<>();

        // Datos de cultivos reales
        Map<String, Double> trigo = new HashMap<>();
        trigo.put("agua_requerida", 450.0);
        trigo.put("tolerancia_sobre", 1.2);
        trigo.put("tolerancia_infra", 0.8);
        informacionCultivos.put("Trigo", trigo);

        Map<String, Double> maiz = new HashMap<>();
        maiz.put("agua_requerida", 600.0);
        maiz.put("tolerancia_sobre", 1.1);
        maiz.put("tolerancia_infra", 0.9);
        informacionCultivos.put("Maíz", maiz);

        Map<String, Double> arroz = new HashMap<>();
        arroz.put("agua_requerida", 1200.0);
        arroz.put("tolerancia_sobre", 1.0);
        arroz.put("tolerancia_infra", 0.7);
        informacionCultivos.put("Arroz", arroz);

        Map<String, Double> soja = new HashMap<>();
        soja.put("agua_requerida", 500.0);
        soja.put("tolerancia_sobre", 1.1);
        soja.put("tolerancia_infra", 0.9);
        informacionCultivos.put("Soja", soja);

        return informacionCultivos;
    }

    static String[][] obtenerCultivosCampo(int n) {
        String[][] cultivosCampo = new String[n][n];

        // Asignar cultivos reales al campo
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i < n / 2) {
                    if (j < n / 2) {
                        cultivosCampo[i][j] = "Trigo";
                    } else {
                        cultivosCampo[i][j] = "Maíz";
                    }
                } else {
                    if (j < n / 2) {
                        cultivosCampo[i][j] = "Arroz";
                    } else {
                        cultivosCampo[i][j] = "Soja";
                    }
                }
            }
        }

        return cultivosCampo;
    }

    static String[][] obtenerSuelosCampo(int n) {
        String[][] suelosCampo = new String[n][n];

        // Asignar suelos reales al campo
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i < n / 2) {
                    if (j < n / 2) {
                        suelosCampo[i][j] = "Arcilloso";
                    } else {
                        suelosCampo[i][j] = "Arenoso";
                    }
                } else {
                    if (j < n / 2) {
                        suelosCampo[i][j] = "Franco";
                    } else {
                        suelosCampo[i][j] = "Limoso";
                    }
                }
            }
        }

        return suelosCampo;
    }
}
