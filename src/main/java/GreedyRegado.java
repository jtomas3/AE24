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

    public GreedyRegado(int n, Map<String, Map<String, Double>> informacionSuelos,
                        Map<String, Map<String, Double>> informacionCultivos, String[][] cultivosCampo,
                        String[][] suelosCampo, double alpha, double beta, int costoTipo1, int costoTipo2,
                        int costoTipo3, int riegoPorMinuto) {
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
    }

    public int[][] ejecutar() {

        // Recorrer el campo en orden
        // for (int i = 0; i < n; i++) {
        //     for (int j = 0; j < n; j++) {
        //         seleccionarMejorConfiguracion(i, j);
        //    }
        // }

        // Elegir la cantidad de iteraciones del greedy entre 1 y 3
        int randomNumber = (int) (Math.random() * 3) + 1;
        // Recorrer el campo, con combinaciones i,j random, probando todas solo una vez
        // Arreglo de tamaño n, con los digitos de 1..n en orden aleatorio
        for (int z = 0; z < randomNumber; z++) {
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
      double probabilidad = probabilidadBase * Math.pow(0.75, aspersoresAdyacentes); // Reduce por cada aspersor adyacente

      // Evaluar cada tipo de aspersor con tiempos de riego incrementales
      for (int tipo = 0; tipo <= 2; tipo++) {
          for (int tiempo = 1; tiempo <= 60; tiempo += 2) { // Incrementos de 2
              double costo = calcularCosto(tipo);
              configuracionAspersores[i][j] = tipo;
              tiemposRiego[i][j] = tiempo;
              double[][] riegoTotal = calcularRiegoTotalCampo(configuracionAspersores, tiemposRiego);
              double desviacion = calcularDesviacionHidrica(riegoTotal);

              // Seleccionar la configuración con menor desviación hídrica
              if (desviacion < mejorDesviacion || (desviacion == mejorDesviacion && costo < mejorCosto)) {
                  if (Math.random() < probabilidad) { // Solo actualizar si supera la prueba probabilística
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

    private double calcularCosto(int tipo) {
        // Implementar lógica de costo basada en tipo de aspersor y tiempo de riego
        switch (tipo) {
            case 1:
                return costoTipo1;
            case 2:
                return costoTipo2;
            case 3:
                return costoTipo3;
            default:
                return 0;
        }
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

    void imprimirCostoTotal() {
        double costoTotal = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                costoTotal += calcularCosto(configuracionAspersores[i][j]);
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
