import java.util.Map;

public class GreedyRegado {
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
    private int tiempoMinimo;
    private int tiempoMaximo;

    public GreedyRegado(int n, Map<String, Map<String, Double>> informacionSuelos,
                        Map<String, Map<String, Double>> informacionCultivos, String[][] cultivosCampo,
                        String[][] suelosCampo, double alpha, double beta, int costoTipo1, int costoTipo2,
                        int costoTipo3, int riegoPorMinuto, int tiempoMinimo, int tiempoMaximo) {
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
        this.tiempoMinimo = tiempoMinimo;
        this.tiempoMaximo = tiempoMaximo;
    }

    public int[][] ejecutar() {
        // Elegir la cantidad de iteraciones del greedy entre 1 y 4
        int randomNumber = (int) (Math.random() * 4) + 1;
        // Recorrer el campo, con combinaciones i,j random, probando todas solo una vez
        for (int z = 0; z < randomNumber; z++) {
        	// Arreglo de tamaño n, con los digitos de 1..n en orden aleatorio
            int[] randomOrder = new int[n];
            for (int i = 0; i < n; i++) {
                randomOrder[i] = i;
            }
            for (int i = 0; i < n; i++) {
                int randomIndex = (int) (Math.random() * n);
                int temp = randomOrder[i];
                randomOrder[i] = randomOrder[randomIndex];
                randomOrder[randomIndex] = temp;
            }

            // Lo mismo para j
            int[] randomOrder2 = new int[n];
            for (int i = 0; i < n; i++) {
                randomOrder2[i] = i;
            }
            for (int i = 0; i < n; i++) {
                int randomIndex = (int) (Math.random() * n);
                int temp = randomOrder2[i];
                randomOrder2[i] = randomOrder2[randomIndex];
                randomOrder2[randomIndex] = temp;
            }

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    seleccionarMejorConfiguracion(randomOrder[i], randomOrder2[j]);
                }
            }

            System.out.println("Corrida numero " + z++);
            imprimirCostoTotal();
            imprimirDiferenciaHidrica();
        }

        imprimirConfiguracion();

        // Concatenar configuraciones
        int[][] greedySolution = new int[2*n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                greedySolution[i][j] = configuracionAspersores[i][j];
                greedySolution[i+n][j] = tiemposRiego[i][j];
            }
        }

      return greedySolution;
    }

  private void seleccionarMejorConfiguracion(int i, int j) {
      double mejorDesviacion = Double.MAX_VALUE;
      int mejorTipo = 0;
      int mejorTiempo = 0;
      double mejorCosto = Double.MAX_VALUE;
      int aspersoresAdyacentes = contarAspersoresAdyacentes(i, j);

      // Probabilidad de añadir un aspersor disminuye según el número de aspersores adyacentes
      double probabilidadBase = 1; // Probabilidad base de colocar un aspersor si no hay adyacentes
      double probabilidad = probabilidadBase * Math.pow(0.7, aspersoresAdyacentes); // Reduce por cada aspersor adyacente
      boolean yaTieneAspersor = configuracionAspersores[i][j] > 0;
      // Evaluar cada tipo de aspersor con tiempos de riego incrementales
      for (int tipo = 0; tipo <= 2; tipo++) {
          for (int tiempo = tiempoMinimo; tiempo <= tiempoMaximo; tiempo += 2) { // Incrementos de 2
              double costo = calcularCosto(tipo, tiempo, i, j);
              configuracionAspersores[i][j] = tipo;
              tiemposRiego[i][j] = tiempo;
              double[][] riegoTotal = calcularRiegoTotalCampo(configuracionAspersores, tiemposRiego);
              double desviacion = calcularDesviacionHidrica(riegoTotal);

              // Seleccionar la configuración con menor desviación hídrica
              if (desviacion < mejorDesviacion || (desviacion == mejorDesviacion && costo < mejorCosto)) {
                  if (yaTieneAspersor || (Math.random() < probabilidad)) { // Solo actualizar si supera la prueba probabilística
                      mejorDesviacion = desviacion;
                      mejorTipo = tipo;
                      mejorTiempo = tiempo;
                      mejorCosto = costo;
                  }
              }
          }
      }

      configuracionAspersores[i][j] = mejorTipo;
      tiemposRiego[i][j] = mejorTiempo;
  }

    private int contarAspersoresAdyacentes(int i, int j) {
        int count = 0;
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                if (di == 0 && dj == 0) continue; // Saltar la propia parcela
                int ni = i + di;
                int nj = j + dj;
                if (ni >= 0 && ni < n && nj >= 0 && nj < n) { // Asegurarse de que no se salga de los límites
                    if (configuracionAspersores[ni][nj] > 0) { // Verificar si hay aspersor
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private int calcularCosto(int tipoAspersor, int tiempoEncendido, int i, int j) {
        int costo = 0;
        switch (tipoAspersor) {
            case 1:
                costo = costoTipo1;
                break;
            case 2:
                costo = costoTipo2;
                break;
            case 3:
                costo = costoTipo3;
                break;
        }

        if (tiempoEncendido < 8 && costo != 0) {
            costo += (10 - tiempoEncendido)/2;
        }

        // Penalizar aspersores en bordes del campo
        if (i == 0 || i == n - 1 || j == 0 || j == n - 1 && costo != 0) {
            costo += 3;
        }

        return costo;
    }

    private double calcularDesviacionHidrica(double[][] riegoTotal) {
      double desviacionTotal = 0.0;

      // Calcular la desviación hídrica para cada parcela
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          desviacionTotal += calcularDesviacionHidricaParcela(i, j, configuracionAspersores[i][j], tiemposRiego[i][j], riegoTotal);
        }
      }

      return desviacionTotal;
    }

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

      double proporcionAgua;

      // Calcular desviación hídrica ajustada por la proporción del agua
      if (aguaReal > aguaOptima) {
          proporcionAgua = aguaReal / aguaOptima;
          desviacionTotal += Math.pow((aguaReal - aguaOptima) / aguaOptima, 1/toleranciaSobre) * proporcionAgua;
      } else {
          if (aguaReal == 0) {
              proporcionAgua = 10;
          } else {
              proporcionAgua = aguaOptima / aguaReal;
          }
          desviacionTotal += Math.pow((aguaOptima - aguaReal) / aguaOptima, 1/toleranciaInfra) * proporcionAgua;
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

    void imprimirCostoTotal() {
        double costoTotal = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                costoTotal += calcularCosto(configuracionAspersores[i][j], tiemposRiego[i][j], i, j);
            }
        }
        System.out.println("Costo total: " + costoTotal);
    }

    void imprimirDiferenciaHidrica() {
        double totalDiferenciaHidrica = 0.0;
        double[][] riegoTotal = calcularRiegoTotalCampo(configuracionAspersores, tiemposRiego);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                totalDiferenciaHidrica += calcularDesviacionHidricaParcela(i, j, configuracionAspersores[i][j],
                        tiemposRiego[i][j], riegoTotal);
            }
        }
        System.out.println("Desviación total: " + totalDiferenciaHidrica);
    }

    public void imprimirConfiguracion() {
        System.out.println("Configuración de Aspersores:");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(configuracionAspersores[i][j] + " (" + tiemposRiego[i][j] + " min) ");
            }
            System.out.println();
        }
        imprimirCostoTotal();
        imprimirDiferenciaHidrica();
    }
}
