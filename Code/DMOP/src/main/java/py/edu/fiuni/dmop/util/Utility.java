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
    
    
    public static String buildFilePath(String filename)
    {
        return System.getProperty("app.home") + filename;
    }
        
    
}
