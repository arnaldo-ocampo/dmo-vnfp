package py.edu.fiuni.dmop;

import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.service.TrafficService;
import py.edu.fiuni.dmop.util.Configurations;

public class TrafficGenerator {

    public static void main(String[] args) throws Exception {

        Configurations.loadProperties();
        DataService.loadData();
        TrafficService trafficService = new TrafficService();

        trafficService.generateRandomtraffic(DataService.nodesMap, DataService.vnfs);
    }

}
