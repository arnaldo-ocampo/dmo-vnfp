package py.edu.fiuni.dmop;

import java.io.File;
import java.io.IOException;
import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.analysis.plot.Plot;
import py.edu.fiuni.dmop.util.Utility;

/**
 * 
 * Class used for testing purposes of MOEA Framework classes: 
 *      Problem, Algorithm, Executor, Analyzer, Plot
 * @author Arnaldo Ocampo, Nestor Tapía
 * 
 * Not related to VNF Placement problem
 */
public class ComparingAlgorithms {

    public static void main(String[] args) throws IOException {
        
        String problem = "UF1";
        String[] algorithms = {"NSGAII", "NSGAIII"/*, "GDE3", "eMOEA"*/};

        //setup the experiment
        Executor executor = new Executor()
                .withProblem(problem)
                //.withCheckpointFrequency(1000)
                //.withCheckpointFile(new File(Utility.buildFilePath( "example.state")))
                .withMaxEvaluations(1000);

        Analyzer analyzer = new Analyzer()
                .withSameProblemAs(executor)
                .includeHypervolume()
                .includeAdditiveEpsilonIndicator()
                .includeGenerationalDistance()
                .showStatisticalSignificance();

        //run each algorithm for 50 seeds
        for (String algorithm : algorithms) {
            analyzer.addAll(algorithm, executor.withAlgorithm(algorithm).runSeeds(2));
            //analyzer.add(algorithm, executor.withAlgorithm(algorithm).run());
        }

        //print the results
        analyzer.printAnalysis();

        // Plot the results
        new Plot().add(analyzer).setTitle("un titulo").show();
    }
}
