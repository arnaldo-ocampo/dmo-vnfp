/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.fiuni.dmop.util;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Arnaldo
 */
public class Utility {
    
    /**
     * 
     * @param millis
     * @return 
     */
    public static String getTime(long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    /**
     * 
     * @param filename
     * @return 
     */
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
