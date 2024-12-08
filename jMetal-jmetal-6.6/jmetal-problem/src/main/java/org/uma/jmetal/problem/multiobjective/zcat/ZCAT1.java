package org.uma.jmetal.problem.multiobjective.zcat;

import java.util.Collections;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.uma.jmetal.problem.doubleproblem.DoubleProblem;
import org.uma.jmetal.problem.multiobjective.zcat.ffunction.F1;
import org.uma.jmetal.problem.multiobjective.zcat.gfunction.G0;
import org.uma.jmetal.problem.multiobjective.zcat.gfunction.G4;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

/**
 * Problem ZCAT1, defined in: "Challenging test problems for multi-and many-objective optimization",
 * DOI: https://doi.org/10.1016/j.swevo.2023.101350
 */
public class ZCAT1 extends ZCAT {

  protected Function<double[], double[]> fFunction;
  protected Function<double[], double[]> gFunction;

  public ZCAT1(int numberOfObjectives, int numberOfVariables) {
    this(numberOfObjectives, numberOfVariables, false, 1, false, false);
  }

  public ZCAT1() {
    this(3, 30, true, 1, false, false);
  }

  protected int paretoSetDimension ;

  public ZCAT1(int numberOfObjectives,
      int numberOfVariables,
      boolean complicatedParetoSet,
      int level,
      boolean bias, boolean imbalance) {
    super(numberOfObjectives, numberOfVariables, complicatedParetoSet, level, bias, imbalance);
    name("ZCAT1");

    paretoSetDimension = numberOfObjectives - 1;

    fFunction = new F1(numberOfObjectives);
    gFunction = complicatedParetoSet ? new G4(numberOfVariables, paretoSetDimension)
        : new G0(numberOfVariables, paretoSetDimension);
  }


  @Override
  public DoubleSolution evaluate(DoubleSolution solution) {
    double[] normalizedVariables = zcatGetY(solution.variables());
    double[] alpha = zcatGetAlpha(normalizedVariables, numberOfObjectives(), fFunction);
    double[] beta = zcatGetBeta(normalizedVariables, numberOfVariables(), paretoSetDimension, bias, imbalance, level, gFunction) ;

    double[] f = zcatMopDefinition(alpha, beta, numberOfObjectives) ;
    IntStream.range(0, numberOfObjectives).forEach(i -> solution.objectives()[i] = f[i]);

    return solution;
  }

  public static void main(String[] args) {
    DoubleProblem problem = new ZCAT1();

    DoubleSolution solution = problem.createSolution();
    Collections.fill(solution.variables(), 0.45);

    problem.evaluate(solution) ;
    System.out.println(solution) ;
  }
}

