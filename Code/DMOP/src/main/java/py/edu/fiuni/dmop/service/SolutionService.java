package py.edu.fiuni.dmop.service;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import py.edu.fiuni.dmop.dto.NFVdto.*;
import py.edu.fiuni.dmop.util.Configurations;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Population;
import py.edu.fiuni.dmop.util.Utility;

public class SolutionService {

    private static Logger logger = Logger.getLogger(SolutionService.class);

    /**
     *
     * @param traffics
     * @throws Exception
     */
    public void writeSolutions(NondominatedPopulation result, String fileName) throws IOException {
        PopulationIO.write(new File(Utility.buildFilePath(Configurations.solutionsFolder + "/" + fileName)), result);
        System.out.println("Solutions Saved to" + fileName + " with " + result.size() + " solutions!");
        
        //String.format("%s%d.txt", filenamePreffix, windows++)
    }

    public NondominatedPopulation readSolutions(String solutionFileName) throws IOException {
        
        Population result = PopulationIO.read(new File(Utility.buildFilePath(Configurations.solutionsFolder + "/" + solutionFileName)));
        System.out.println("Solutions Read from file " + solutionFileName + " with " + result.size() + " solutions!");
        
        return new NondominatedPopulation(result);
    }
}
