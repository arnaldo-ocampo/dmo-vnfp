package py.edu.fiuni.dmop.service;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import py.edu.fiuni.dmop.dto.NFVdto.*;
import py.edu.fiuni.dmop.util.Configurations;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Permutation;
import py.edu.fiuni.dmop.dto.RoundBestSolution;
import py.edu.fiuni.dmop.dto.RoundData;
import py.edu.fiuni.dmop.problem.SceneObjectiveFunctions;
import py.edu.fiuni.dmop.util.Constants;
import py.edu.fiuni.dmop.util.NetworkConditionEnum;
import py.edu.fiuni.dmop.util.ObjectiveFunctionEnum;
import py.edu.fiuni.dmop.util.Utility;

public class SolutionService {

    private static Logger logger = Logger.getLogger(SolutionService.class);

    public void printSolutionsList(List<NondominatedPopulation> results) {
        int seed = 1;
        for (NondominatedPopulation roundSolutions : results) {
            logger.info("Round ###: " + seed++);
            int contSol = 1;
            for (Solution s : roundSolutions) {
                //String fitness = DoubleStream.of(s.getObjectives()).map(val  -> String.valueOf(val))
                logger.info(String.format("Solution #%3d Fitness::: %s Variable:: %s", contSol++, Arrays.toString(s.getObjectives()), Arrays.toString(((Permutation) s.getVariable(0)).toArray())));
            }
        }
        /*
            int index = 0;
            for (Solution sol : result) {
                System.out.print("Solution #" + index++ + " = ");
                for (int objIndex = 0; objIndex < sol.getNumberOfObjectives(); objIndex++) {
                    System.out.printf("%s=%6f ,", sceneObjectives.get(objIndex).getPropertyName(), sol.getObjective(objIndex));
                }

                // Shows the permutation used in the current solution.
                System.out.println("Variable: " + Arrays.toString(((Permutation) sol.getVariable(0)).toArray()));
            }
         */
    }

    /**
     *
     * @param result
     * @param fileName
     * @throws IOException
     */
    public void writeSolutions(NondominatedPopulation result, String fileName) throws IOException {
        PopulationIO.write(new File(Utility.buildFilePath(Configurations.solutionsFolder + "/" + fileName)), result);
        logger.info("Solutions Saved to" + fileName + " with " + result.size() + " solutions!");

        //String.format("%s%d.txt", filenamePreffix, windows++)
    }

    /**
     *
     * @param solutionFileName
     * @return
     * @throws IOException
     */
    public NondominatedPopulation readSolutions(String solutionFileName) throws IOException {

        Population result = PopulationIO.read(new File(Utility.buildFilePath(Configurations.solutionsFolder + "/" + solutionFileName)));
        logger.info("Solutions Read from file " + solutionFileName + " with " + result.size() + " solutions!");

        return new NondominatedPopulation(result);
    }

    /**
     *
     * @param algorithms
     * @param decisionService
     */
    public void generateCSVFiles(String[] algorithms, MCDMService decisionService) {

        try {

            Map<String, Map<Integer, List<RoundData>>> algorithmsSolutionsMap = new HashMap<>();

            for (String alg : algorithms) {
                algorithmsSolutionsMap.put(alg, readSolutionsForAlgorithm(alg));
            }

            logger.info("############ Calculte the best solution in every case ############");

            // Create a map to hold the best solution for every round and windows
            // It is added to List of best solutions becuase they are already in order of Round and Windows
            Map<String, List<RoundBestSolution>> algorithmsBestSolutionsMap = new HashMap<>();

            // Key: windows number
            // Value: List of the best-solution per round
            Map<Integer, List<RoundBestSolution>> bestResultsMapByWindows = new HashMap<>();

            for (String alg : algorithmsSolutionsMap.keySet()) {

                // add the collection for the specified algorithm
                algorithmsBestSolutionsMap.put(alg, new ArrayList<>());

                Map<Integer, List<RoundData>> roundsBestByWindows = algorithmsSolutionsMap.get(alg);
                for (int wnd : roundsBestByWindows.keySet()) {

                    if (!bestResultsMapByWindows.containsKey(wnd)) {
                        bestResultsMapByWindows.put(wnd, new ArrayList<>());
                    }

                    List<RoundData> roundDataList = roundsBestByWindows.get(wnd);

                    for (RoundData data : roundDataList) {

                        int trafficsCount = getTrafficsCountForWindows(wnd);
                        NetworkConditionEnum networkCondition = getNetConditionForWindows(wnd);

                        // Decision Making
                        Solution bestSolution = decisionService.calculateOptimalSolution(data.getSolutions(), networkCondition);

                        RoundBestSolution sol = new RoundBestSolution(data.getAlgorithm(), data.getRoundNumber(), wnd, bestSolution, trafficsCount, networkCondition);

                        algorithmsBestSolutionsMap.get(alg).add(sol);

                        bestResultsMapByWindows.get(wnd).add(sol);
                    }
                }
            }

            //#####################################################################
            // Generate first group of csv files
            generateAlgFOFiles(algorithmsBestSolutionsMap);

            //#####################################################################            
            //#####################################################################
            //#####################################################################
            logger.info("############  GENERATING CSV FILES::: Windows#-OF  ####################");

            //List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(NetworkConditionEnum.Normal);
            for (int windows : bestResultsMapByWindows.keySet()) {

                List<RoundBestSolution> currWndBestSolutionsList = bestResultsMapByWindows.get(windows);

                NetworkConditionEnum networkCondition = getNetConditionForWindows(windows);
                List<ObjectiveFunctionEnum> currWindowsObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);

                Map<String, List<double[]>> toFile = new HashMap<>();

                for (int round = 1; round <= Constants.MAX_ROUNDS; round++) {

                    final int roundNumber = round;

                    // conjunto de mejores soluciones del round actual
                    List<RoundBestSolution> roundBestSolutions = currWndBestSolutionsList.stream().filter(rbs -> roundNumber == rbs.getRoundNumber()).toList();

                    for (int index = 0; index < currWindowsObjectives.size(); index++) {
                        double[] values = extractAlgorithmsValuesFor(index, roundBestSolutions, algorithms);

                        String key = String.format("Ventana_#%d_-_%s", windows, currWindowsObjectives.get(index).getName());

                        if (!toFile.containsKey(key)) {
                            toFile.put(key, new ArrayList<>());
                        }
                        toFile.get(key).add(values);

                        System.out.println(" *** " + Arrays.toString(values));
                    }
                }

                for (String key : toFile.keySet()) {
                    List<double[]> values = toFile.get(key);
                    System.out.println("archivo " + key);
                    values.forEach(objs -> System.out.println(Arrays.toString(objs)));

                    createCompareAlgsCSVFile(key, values, algorithms);
                }
            }

            //Permutation bestVar1 = (Permutation) bestSolution1.getVariable(0);
            //System.out.println("Permutation Variable 1: " + Arrays.toString(bestVar1.toArray()) + " Fitness:: " + Arrays.toString(bestSolution1.getObjectives()));
        } catch (Exception ex) {
            logger.fatal("Error", ex);
        }
    }

    // Generates files to be used to create the charts that show 
    // the fitness of a particular objective function in every windows
    // for a particular algorithm
    private void generateAlgFOFiles(Map<String, List<RoundBestSolution>> algorithmsBestSolutionsMap) {

        logger.info("############  GENERATING CSV FILES::: Algorithm-OF  ####################");
        
        String[] windowsOrder = new String[Constants.MAX_WINDOWS];
        for(int i = 0; i < windowsOrder.length; i++){
            int wnd = Constants.WINDOWS_TRAFFICS_COUNT_SORTED[i];
            windowsOrder[i] = "w"+wnd+":"+Constants.WINDOWS_TRAFFICS_COUNT[wnd];
        }
        

        List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(NetworkConditionEnum.Normal);

        for (String alg : algorithmsBestSolutionsMap.keySet()) {
            logger.info("Solutions for Algorithm :::: " + alg);

            List<RoundBestSolution> bestSolutionsList = algorithmsBestSolutionsMap.get(alg);

            Map<String, List<double[]>> toFile = new HashMap<>();
            for (int round = 1; round <= Constants.MAX_ROUNDS; round++) {

                final int roundNumber = round;

                // conjunto de mejores soluciones del round actual
                List<RoundBestSolution> roundBestSolutions = bestSolutionsList.stream().filter(rbs -> roundNumber == rbs.getRoundNumber()).toList();

                // Order the solutions by windows number (asc order)
                //roundBestSolutions.sort((o1, o2) -> o1.getWindows() - o2.getWindows());
                /*
                System.out.println("Best for the Round #" + round);
                for (RoundBestSolution best : roundBestSolutions) {
                    Solution bestSol = best.getSolution();
                    System.out.println(String.format("Windows #%3d  Solution Fitness::: %s", best.getWindows(), Arrays.toString(bestSol.getObjectives())));
                }
                 */
                for (int index = 0; index < sceneObjectives.size(); index++) {
                    double[] values = extractValuesFor(index, roundBestSolutions, Constants.MAX_WINDOWS, Constants.WINDOWS_TRAFFICS_COUNT_SORTED);

                    String key = String.format("%s_-_%s", alg, sceneObjectives.get(index).getName());

                    if (!toFile.containsKey(key)) {
                        toFile.put(key, new ArrayList<>());
                    }
                    toFile.get(key).add(values);

                    System.out.println(" *** " + Arrays.toString(values));
                }
            }

            for (String key : toFile.keySet()) {
                List<double[]> values = toFile.get(key);
                System.out.println("archivo " + key);
                values.forEach(objs -> System.out.println(Arrays.toString(objs)));

                createCSVFile(key, values, windowsOrder);
            }
        }
    }

    /**
     *
     * @param solutionService
     * @param algorithm
     * @return
     * @throws IOException
     */
    private Map<Integer, List<RoundData>> readSolutionsForAlgorithm(String algorithm) throws IOException {

        // key: windows number
        // value: List of best-solution of every round
        Map<Integer, List<RoundData>> resultsMap = new HashMap<>();
        for (int round = 1; round <= Constants.MAX_ROUNDS; round++) {
            for (int windows = 0; windows < Constants.MAX_WINDOWS; windows++) {
                String fileName = String.format(Constants.SOLUTION_FILENAME_TEMPLATE, algorithm, round, windows);
                NondominatedPopulation result = readSolutions(fileName);

                if (!resultsMap.containsKey(windows)) {
                    resultsMap.put(windows, new ArrayList<>());
                }

                resultsMap.get(windows).add(new RoundData(algorithm, round, result));
            }
        }
        return resultsMap;
    }

    private double[] extractValuesFor(int ofIndex, List<RoundBestSolution> solutions, int maxWindows, int[] windowsOrder) {
        double[] values = DoubleStream.generate(() -> Double.MAX_VALUE).limit(maxWindows).toArray();

        int index = 0;
        for (int windowsIndex : windowsOrder) {
            Optional<RoundBestSolution> sol = solutions.stream().filter(s -> s.getWindows() == windowsIndex).findFirst();
            if (sol.isPresent()) {
                double[] objectives = sol.get().getSolution().getObjectives();
                if (ofIndex < objectives.length) {
                    values[index] = objectives[ofIndex];
                }                
            }
            index++;
        }
        return values; //result.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private double[] extractAlgorithmsValuesFor(int ofIndex, List<RoundBestSolution> solutions, String[] algorithms) {

        double[] values = DoubleStream.generate(() -> Double.MAX_VALUE).limit(algorithms.length).toArray();

        int cont = 0;
        for (String algorithm : algorithms) {
            Optional<RoundBestSolution> sol = solutions.stream().filter(s -> s.getAlgorithm().equals(algorithm)).findFirst();
            if (sol.isPresent()) {
                double[] objectives = sol.get().getSolution().getObjectives();
                values[cont] = objectives[ofIndex];
            }
            cont++;
        }

        return values;
    }

    private void createCSVFile(String fileName, List<double[]> rows, String[] windowsOrder) {

        try (PrintWriter writer = new PrintWriter(new File(Utility.buildFilePath(Configurations.solutionsFolder + "/CSVs/" + fileName + ".csv")))) {

            // Get the first row values in order to generate the header correctly
            double[] roundValues = rows.get(0);

            List<String> header = new ArrayList<>();
            for (int i = 0; i < roundValues.length; i++) {
                if (roundValues[i] < Double.MAX_VALUE) {
                    header.add(windowsOrder[i]);
                }
            }
            writer.println(header.stream().collect(Collectors.joining(",")));

            for (double[] row : rows) {
                String valuesAsString = Arrays.stream(row).filter(d -> d < Double.MAX_VALUE).mapToObj(String::valueOf).collect(Collectors.joining(","));
                writer.println(valuesAsString);
            }

        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    private void createCompareAlgsCSVFile(String fileName, List<double[]> rows, String[] algorithms) {

        try (PrintWriter writer = new PrintWriter(new File(Utility.buildFilePath(Configurations.solutionsFolder + "/CSVs/" + fileName + ".csv")))) {

            writer.println(Arrays.stream(algorithms).collect(Collectors.joining(",")));

            for (double[] row : rows) {
                String valuesAsString = Arrays.stream(row).filter(d -> d < Double.MAX_VALUE).mapToObj(String::valueOf).collect(Collectors.joining(","));
                writer.println(valuesAsString);
            }

        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    private int getTrafficsCountForWindows(int winNumber) {
        return Constants.WINDOWS_TRAFFICS_COUNT[winNumber];
    }

    private NetworkConditionEnum getNetConditionForWindows(int winNumber) {

        int winTrafficsCount = Constants.WINDOWS_TRAFFICS_COUNT[winNumber];
        return winTrafficsCount > Constants.NORMAL_UPPER_LIMIT ? NetworkConditionEnum.Overloaded : NetworkConditionEnum.Normal;
    }
}
