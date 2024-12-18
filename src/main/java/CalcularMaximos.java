import java.util.Map;

public class CalcularMaximos {
    // TODO: Usar una clase abstracta para CalcularMaximos y GreedyRegado, hay duplciados
    private int n;
    private double alpha;
    private double beta;
    private int costoTipo1;
    private int costoTipo2;
    private int costoTipo3;
    private int riegoPorMinuto;
    private Map<String, Map<String, Double>> informacionSuelos;
    private Map<String, Map<String, Double>> informacionCultivos;
    private String[][] cultivosCampo;
    private String[][] suelosCampo;
    private int[][] configuracionAspersores;
    private int[][] tiemposRiego;
    private int tiempoMaximo;

    public CalcularMaximos(int n, Map<String, Map<String, Double>> informacionSuelos,
                        Map<String, Map<String, Double>> informacionCultivos, String[][] cultivosCampo,
                        String[][] suelosCampo, double alpha, double beta, int costoTipo1, int costoTipo2,
                        int costoTipo3, int riegoPorMinuto, int tiempoMaximo) {
        this.n = n;
        this.alpha = alpha;
        this.beta = beta;
        this.costoTipo1 = costoTipo1;
        this.costoTipo2 = costoTipo2;
        this.costoTipo3 = costoTipo3;
        this.riegoPorMinuto = riegoPorMinuto;
        this.informacionSuelos = informacionSuelos;
        this.informacionCultivos = informacionCultivos;
        this.cultivosCampo = cultivosCampo;
        this.suelosCampo = suelosCampo;
        this.configuracionAspersores = new int[n][n];
        this.tiemposRiego = new int[n][n];
        this.tiempoMaximo = tiempoMaximo;
    }

    int calcularCostoMaximo() {
        int costoMaximo = 0;

        // Computar el maximo de costoTipo1, costoTipo2 y costoTipo3
        if (costoTipo1 > costoTipo2) {
            if (costoTipo1 > costoTipo3) {
                costoMaximo = costoTipo1;
            } else {
                costoMaximo = costoTipo3;
            }
        } else {
            if (costoTipo2 > costoTipo3) {
                costoMaximo = costoTipo2;
            } else {
                costoMaximo = costoTipo3;
            }
        }

        // Multiplicar por n*n
        costoMaximo *= n * n;

        return costoMaximo;
    }

    double calcularDesbalanceMaximo() {
        // Inicializar configuracionAspersores todos con tipo 3, y tiemposRiego todo en tiempos maximos para el peor caso
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                configuracionAspersores[i][j] = 2;
                tiemposRiego[i][j] = tiempoMaximo;
            }
        }

        // Calcular riego total
        double[][] riegoTotal = calcularRiegoTotalCampo(configuracionAspersores, tiemposRiego);

        // Calcular desviacion hídrica
        double desviacionTotal = calcularDesviacionHidrica(riegoTotal, n, configuracionAspersores, tiemposRiego);

        return desviacionTotal;
    }

    // TODO: Metodos duplicados de GreedyRegado
    private double calcularDesviacionHidrica(double[][] riegoTotal, int n, int[][] configuracionAspersores, int[][] tiemposRiego) {
        double desviacionTotal = 0.0;

        // Calcular la desviación hídrica para cada parcela
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                desviacionTotal += calcularDesviacionHidricaParcela(i, j, configuracionAspersores[i][j], tiemposRiego[i][j], riegoTotal);
            }
        }

        return desviacionTotal;
    }

    // TODO: Sacar parametros no usados
    private double calcularDesviacionHidricaParcela(int i, int j, int tipo, int tiempo, double[][] riegoTotal) {
        double desviacionTotal = 0.0;

        // Calcular la desviación hídrica para la parcela actual
        String tipoSuelo = suelosCampo[i][j];
        String tipoCultivo = cultivosCampo[i][j];
        double aguaRequerida = informacionCultivos.get(tipoCultivo).get("agua_requerida");
        double toleranciaSobre = informacionCultivos.get(tipoCultivo).get("tolerancia_sobre");
        double toleranciaInfra = informacionCultivos.get(tipoCultivo).get("tolerancia_infra");
        double capacidadCampo = informacionSuelos.get(tipoSuelo).get("h_campo");
        double puntoMarchitez = informacionSuelos.get(tipoSuelo).get("h_marchitez");
        double aguaReal = riegoTotal[i][j];

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

    double[][] calcularRiegoTotalCampo(int[][] configuracionAspersores, int[][] tiemposRiego) {
        // Matriz de nxn que representa riego de cada parcela
        double[][] riegoTotal = new double[n][n];

        // Recorremos todo el campo, calculando el riego total.
        // Cada vez que encontramos un aspersor, actualizamos el riego de cada una
        // de las parcelas afectadas por el aspersor, teniendo en cuenta que no se salga
        // de los límites del campo.
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int tipoAspersor = configuracionAspersores[i][j];
                if (tipoAspersor != 0) {
                    int tiempoEncendido = tiemposRiego[i][j];
                    riegoTotal[i][j] += riegoPorMinuto * tiempoEncendido; // Riego en la parcela actual
                    if (tipoAspersor == 1 || tipoAspersor == 2) {
                        // Riego en las parcelas adyacentes (a distancia 1)
                        if (i - 1 >= 0)
                            riegoTotal[i - 1][j] += riegoPorMinuto * alpha * tiempoEncendido;
                        if (i + 1 < n)
                            riegoTotal[i + 1][j] += riegoPorMinuto * alpha * tiempoEncendido;
                        if (j - 1 >= 0)
                            riegoTotal[i][j - 1] += riegoPorMinuto * alpha * tiempoEncendido;
                        if (j + 1 < n)
                            riegoTotal[i][j + 1] += riegoPorMinuto * alpha * tiempoEncendido;
                    }
                    if (tipoAspersor == 2) {
                        // Riego en las parcelas (a distancia 2)
                        if (i - 2 >= 0)
                            riegoTotal[i - 2][j] += riegoPorMinuto * beta * tiempoEncendido;
                        if (i + 2 < n)
                            riegoTotal[i + 2][j] += riegoPorMinuto * beta * tiempoEncendido;
                        if (j - 2 >= 0)
                            riegoTotal[i][j - 2] += riegoPorMinuto * beta * tiempoEncendido;
                        if (j + 2 < n)
                            riegoTotal[i][j + 2] += riegoPorMinuto * beta * tiempoEncendido;
                        // Diagonales
                        if (i - 1 >= 0 && j - 1 >= 0)
                            riegoTotal[i - 1][j - 1] += riegoPorMinuto * beta * tiempoEncendido;
                        if (i - 1 >= 0 && j + 1 < n)
                            riegoTotal[i - 1][j + 1] += riegoPorMinuto * beta * tiempoEncendido;
                        if (i + 1 < n && j - 1 >= 0)
                            riegoTotal[i + 1][j - 1] += riegoPorMinuto * beta * tiempoEncendido;
                        if (i + 1 < n && j + 1 < n)
                            riegoTotal[i + 1][j + 1] += riegoPorMinuto * beta * tiempoEncendido;
                    }
                }
            }
        }

        return riegoTotal;
    }
}
