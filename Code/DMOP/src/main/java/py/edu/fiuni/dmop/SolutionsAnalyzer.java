package py.edu.fiuni.dmop;


import py.edu.fiuni.dmop.service.DMOPService;

public class SolutionsAnalyzer {

    public static void main(String[] args) throws Exception {
 
        DMOPService dmopService = new DMOPService();
        dmopService.maoeaMetrics();
    }

}
