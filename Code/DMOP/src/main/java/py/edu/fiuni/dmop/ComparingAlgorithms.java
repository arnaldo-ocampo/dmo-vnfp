/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.fiuni.dmop;

import java.io.IOException;
import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.analysis.plot.Plot;

public class ComparingAlgorithms {

    public static void main(String[] args) throws IOException {
        String problem = "UF1";
        String[] algorithms = {"NSGAII", "GDE3", "eMOEA"};

        //setup the experiment
        Executor executor = new Executor()
                .withProblem(problem)
                .withMaxEvaluations(10000);

        Analyzer analyzer = new Analyzer()
                .withSameProblemAs(executor)
                .includeHypervolume()
                .showStatisticalSignificance();

        //run each algorithm for 50 seeds
        for (String algorithm : algorithms) {
            analyzer.addAll(algorithm,
                    executor.withAlgorithm(algorithm).runSeeds(50));
        }

        //print the results
        analyzer.printAnalysis();

        //plot the results
        new Plot()
                .add(analyzer)
                .show();
    }

}
