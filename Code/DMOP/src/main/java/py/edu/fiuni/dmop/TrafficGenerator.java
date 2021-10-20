package py.edu.fiuni.dmop;

import java.util.List;
import org.apache.log4j.Logger;
import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.service.TrafficService;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.util.Configurations;

public class TrafficGenerator {
    
    private static Logger logger = Logger.getLogger(TrafficGenerator.class);
    
    public static void main(String[] args) {
        try {            
            Configurations.loadProperties();
            DataService.loadData();            
            
            
            // Generate random traffic and save them into traffic file
            TrafficService trafficService = new TrafficService();
            
            
            List<Traffic> traffics= trafficService.generateRandomTraffic(Configurations.numberOfTraffics, DataService.nodesMap, DataService.vnfs);
            trafficService.writeTraffics(traffics);
            
            
            /*
            int[] windowsTrafficsNumber = { 10, 80, 24, 93, 55, 12, 33, 99, 64, 22 };
            
            //int[] windowsTrafficsNumber = { 10, 80, 24, 93, 55, 12, 33, 99, 64, 22 };            
            //int[] windowsTrafficsNumber = { 10, 80, 24, 93, 55, 12, 33, 99, 64, 22 };
            
            List<List<Traffic>> allTraffics = trafficService.generateWindowsTraffics(windowsTrafficsNumber, DataService.nodesMap, DataService.vnfs);
            trafficService.writeAllTraffics(allTraffics);
            
            
            //List<List<Traffic>> readTraffics = trafficService.readAllTraffics(windowsTrafficsNumber.length);
            */
            
            
        } catch (Exception e) {
            logger.error("Error Generating Traffic", e);
        }
    }
}
