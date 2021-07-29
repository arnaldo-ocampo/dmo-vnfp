package py.edu.fiuni.dmop;


import py.edu.fiuni.dmop.service.MaOEAService;

public class SolutionsAnalyzer {

    public static void main(String[] args) throws Exception {
 
        MaOEAService maOEAService = new MaOEAService();
        maOEAService.maoeaMetrics();
    }

}
