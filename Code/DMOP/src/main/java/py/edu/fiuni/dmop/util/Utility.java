/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.fiuni.dmop.util;

/**
 *
 * @author Arnaldo
 */
public class Utility {

    public static String buildFilePath(String filename) {
        return System.getProperty("app.home") + filename;
    }

    /**
     * 
     * @param value
     * @param max
     * @param min
     * @return 
     */
    public static double normalizeValue(double value, double max, double min) {
        if (max == min) {
            return 0;
        }
        return (value - min) / (max - min);
    }
}
