import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Regado extends AbstractIntegerProblem {
    // Tablero que representa el campo con los cultivos
    private String[][] cultivosCampo;

    // Tablero que representa el campo con los tipo de suelo
    private String[][] suelosCampo;

    // Dimensiones del campo (nxn)
    private int n;

    // Constantes
    private final int COSTO_TIPO_1 = 10;
    private final int COSTO_TIPO_2 = 12;
    private final int COSTO_TIPO_3 = 14;

    // Variable X que controla cuanto riegan los aspersores
    //   - Tipo 1: solo riega una cantidad X por minuto en la
    //      parcela donde es posicionado.
    //   - Tipo 2: riega X por minuto en la parcela actual, y
    //      X · alpha por minuto en las parcelas adyacentes.
    //   - Tipo 3: Como el Tipo 2, pero riega a su vez una
    //      cantidad X · beta de agua por minuto en parcelas una
    //      unidad más distante.
    private final int x = 10;

    // Variables de regado para aspersores: alpha, beta (distancia 1 y 2 respectivamente)
    private final double alpha;
    private final double beta;

    // Mapa con key como tipo de suelo y value como un hash con keys:
    //   - "h_campo": Capacidad de campo
    //   - "h_marchitez": Punto de marchitez
    private final Map<String, Map<String, Double>> informacion_suelos;

    // Mapa con key como tipo de cultivo y value como un hash con keys:
    //  - "agua_requerida": Cantidad de agua requerida en 24 hs
    //  - "tolerancia_sobre": Factor de tolerancia a sobre irrigación
    //  - "tolerancia_infra": Factor de tolerancia a infra irrigación
    private final Map<String, Map<String, Double>> informacion_cultivos;

    public Regado(int n, Map<String, Map<String, Double>> informacion_suelos, Map<String, Map<String, Double>> informacion_cultivos, String[][] cultivosCampo, String[][] sueslosCampo, double alpha, double beta) {
        this.n = n;
        this.informacion_suelos = informacion_suelos;
        this.informacion_cultivos = informacion_cultivos;
        this.cultivosCampo = cultivosCampo;
        this.suelosCampo = sueslosCampo;
        this.alpha = alpha;
        this.beta = beta;

        setNumberOfVariables(n * n);
        setNumberOfObjectives(2);
        setName("Regado");

        List<Integer> lowerLimit = new ArrayList<>(getNumberOfVariables());
        List<Integer> upperLimit = new ArrayList<>(getNumberOfVariables());
        for (int i = 0; i < getNumberOfVariables(); i++) {
            lowerLimit.add(0);
            upperLimit.add(3);
        }

        // Los valores posibles para cada una de las nxn variables (parcelas) son 0 si no hay aspersor,
        // y 1, 2 o 3 si hay un aspersor de tipo 1, 2 o 3 respectivamente.
        setVariableBounds(lowerLimit, upperLimit);
    }

    @Override
    public void evaluate(IntegerSolution solution) {
        int costoTotal = 0;
        double desviacionHidrica = 0.0;
        double[][] riegoTotal = calcularRiegoTotalCampo(solution);

        // Cálculo de costo y desviación
        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            int tipoAspersor = solution.getVariable(i);
            if (tipoAspersor != 0) {
                costoTotal += calcularCosto(tipoAspersor); // Calcula el costo basado en el tipo
                desviacionHidrica += calcularDesviacionHidrica(solution, i, riegoTotal); // Calcula la desviación hídrica
            }
        }

        // Establecer los objetivos
        solution.setObjective(0, costoTotal);
        solution.setObjective(1, desviacionHidrica);
    }

    private int calcularCosto(int tipoAspersor) {
        switch (tipoAspersor) {
            case 1:
                return COSTO_TIPO_1;
            case 2:
                return COSTO_TIPO_2;
            case 3:
                return COSTO_TIPO_3;
            default:
                return 0;
        }
    }

    private double[][] calcularRiegoTotalCampo(IntegerSolution solution) {
        // Matriz de nxn que representa riego de cada parcela
        double[][] riegoTotal = new double[n][n];

        // Recorremos todo el campo, calculando el riego total.
        // Cada vez que encontramos un aspersor, actualizamos el riego de cada una
        // de las parcelas afectadas por el aspersor, teniendo en cuenta que no se salga
        // de los límites del campo.
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                int index = i * n + j; // Convertir coordenadas 2D a índice lineal para acceder a la solución
                int tipoAspersor = solution.getVariable(index);
                if (tipoAspersor != 0) {
                    riegoTotal[i][j] += x; // Riego en la parcela actual
                    if (tipoAspersor == 2 || tipoAspersor == 3) {
                        // Riego en las parcelas adyacentes (a distancia 1)
                        if (i - 1 >= 0) riegoTotal[i - 1][j] += x * alpha;
                        if (i + 1 < n) riegoTotal[i + 1][j] += x * alpha;
                        if (j - 1 >= 0) riegoTotal[i][j - 1] += x * alpha;
                        if (j + 1 < n) riegoTotal[i][j + 1] += x * alpha;
                    }
                    if (tipoAspersor == 3) {
                        // Riego en las parcelas (a distancia 2)
                        if (i - 2 >= 0) riegoTotal[i - 2][j] += x * beta;
                        if (i + 2 < n) riegoTotal[i + 2][j] += x * beta;
                        if (j - 2 >= 0) riegoTotal[i][j - 2] += x * beta;
                        if (j + 2 < n) riegoTotal[i][j + 2] += x * beta;
                        // Diagonales
                        if (i - 1 >= 0 && j - 1 >= 0) riegoTotal[i - 1][j - 1] += x * beta;
                        if (i - 1 >= 0 && j + 1 < n) riegoTotal[i - 1][j + 1] += x * beta;
                        if (i + 1 < n && j - 1 >= 0) riegoTotal[i + 1][j - 1] += x * beta;
                        if (i + 1 < n && j + 1 < n) riegoTotal[i + 1][j + 1] += x * beta;
                    }
                }
            }
        }

        return riegoTotal;
    }

    private double calcularDesviacionHidrica(IntegerSolution solution, int index, double[][] riegoTotal) {
        double desviacionTotal = 0.0;
        int indice_i_parcela = index / n;
        int indice_j_parcela = index % n;

        // Calcular la desviación hídrica para la parcela actual
        String tipoSuelo = suelosCampo[indice_i_parcela][indice_j_parcela];
        String tipoCultivo = cultivosCampo[indice_i_parcela][indice_j_parcela];
        double aguaRequerida = informacion_cultivos.get(tipoCultivo).get("agua_requerida");
        double toleranciaSobre = informacion_cultivos.get(tipoSuelo).get("tolerancia_sobre");
        double toleranciaInfra = informacion_cultivos.get(tipoSuelo).get("tolerancia_infra");
        double capacidadCampo = informacion_suelos.get(tipoSuelo).get("h_campo");
        double puntoMarchitez = informacion_suelos.get(tipoSuelo).get("h_marchitez");
        double aguaReal = riegoTotal[indice_i_parcela][indice_j_parcela];

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

    public static void main(String[] args) {
        int n = 10; // Dimensiones del campo (10x10 por ejemplo)
        Map<String, Map<String, Double>> informacionSuelos = obtenerInformacionSuelos();
        Map<String, Map<String, Double>> informacionCultivos = obtenerInformacionCultivos();
        String[][] cultivosCampo = obtenerCultivosCampo();
        String[][] suelosCampo = obtenerSuelosCampo();
        double alpha = 0.7;
        double beta = 0.3;

        // Creación del problema
        Regado problema = new Regado(n, informacionSuelos, informacionCultivos, cultivosCampo, suelosCampo, alpha, beta);

        // Configuración de los operadores de cruce, mutación y selección
        CrossoverOperator<IntegerSolution> crossover = new IntegerSBXCrossover(0.9, 20.0);
        MutationOperator<IntegerSolution> mutation = new IntegerPolynomialMutation(1.0 / problema.getNumberOfVariables(), 20.0);
        SelectionOperator<List<IntegerSolution>, IntegerSolution> selection = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());

        // Configuración del algoritmo NSGA-II
        Algorithm<List<IntegerSolution>> algorithm = new NSGAIIBuilder<IntegerSolution>(problema, crossover, mutation, 100)
            .setSelectionOperator(selection)
            .setMaxEvaluations(10000)
            .build();

        // Ejecución del algoritmo
        algorithm.run();

        // Obtención de la solución
        List<IntegerSolution> population = algorithm.getResult();
        IntegerSolution bestSolution = population.get(0);
        for (int i = 1; i < population.size(); i++) {
            if (population.get(i).getObjective(0) < bestSolution.getObjective(0)) {
                bestSolution = population.get(i);
            }
        }

        // Imprimir la solución y su evaluación
        System.out.println("Solution: " + bestSolution.getVariables());
        System.out.println("Objective 1 (Costo): " + bestSolution.getObjective(0));
        System.out.println("Objective 2 (Desviación hídrica): " + bestSolution.getObjective(1));
    }

    // Métodos ficticios para obtener información, reemplaza con tus métodos o datos reales
    private static Map<String, Map<String, Double>> obtenerInformacionSuelos() {
        Map<String, Map<String, Double>> informacionSuelos = new HashMap<>();
        // Rellenar con datos reales
        return informacionSuelos;
    }

    private static Map<String, Map<String, Double>> obtenerInformacionCultivos() {
        Map<String, Map<String, Double>> informacionCultivos = new HashMap<>();
        // Rellenar con datos reales
        return informacionCultivos;
    }

    private static String[][] obtenerCultivosCampo() {
        String[][] cultivosCampo = new String[10][10];
        // Rellenar con datos reales
        return cultivosCampo;
    }

    private static String[][] obtenerSuelosCampo() {
        String[][] suelosCampo = new String[10][10];
        // Rellenar con datos reales
        return suelosCampo;
    }
}
