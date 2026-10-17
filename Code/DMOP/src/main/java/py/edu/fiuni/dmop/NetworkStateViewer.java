package py.edu.fiuni.dmop;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.dto.NetworkHealth;
import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.service.MCDMService;
import py.edu.fiuni.dmop.service.TrafficService;
import py.edu.fiuni.dmop.util.Configurations;
import py.edu.fiuni.dmop.util.Constants;
import py.edu.fiuni.dmop.util.NetworkConditionEnum;

/**
 * Muestra para un frente que solucion elige cada metodo, como queda la red
 * con cada una y cual queda mejor.
 *
 * Uso: NetworkStateViewer [algoritmo] [ronda] [ventana] [perfil]
 */
public class NetworkStateViewer {

    private static final Logger logger = Logger.getLogger(NetworkStateViewer.class);

    private static final String[] METHODS = {"topsis", "saw", "ahp", "promethee"};

    public static void main(String[] args) {

        String algorithm = args.length > 0 ? args[0] : "DNSGAII-A";
        int round = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        int windows = args.length > 2 ? Integer.parseInt(args[2]) : 0;
        String profile = args.length > 3 ? args[3] : "equilibrado";

        try {
            Configurations.loadProperties();
            DataService.loadData();

            List<Traffic> traffics = new TrafficService().readTraffics(String.format("Traffic_%d.txt", windows));
            NetworkConditionEnum condition = traffics.size() > Constants.NORMAL_UPPER_LIMIT
                    ? NetworkConditionEnum.Overloaded : NetworkConditionEnum.Normal;

            // Mismo frente que usa MCDMExperiment: objetivos recalculados y salud de cada solucion
            MCDMExperiment.EvaluatedFront evaluated = MCDMExperiment.evaluateFront(algorithm, round, windows, traffics, condition);
            NondominatedPopulation front = evaluated.front;

            // Cada metodo elige y se toma como queda la red con su solucion
            Map<String, Integer> chosen = new LinkedHashMap<>();
            Map<String, NetworkHealth> healths = new LinkedHashMap<>();
            for (String method : METHODS) {
                Solution solution = chooseQuietly(front, condition, method, profile);
                chosen.put(method, indexOf(front, solution));
                healths.put(method, evaluated.healthOf.get(solution));
            }

            System.out.println();
            System.out.printf("%s, ronda %d, ventana %d: %d tráficos, %d soluciones, %s%n",
                    algorithm, round, windows, traffics.size(), front.size(),
                    profile.equals("equilibrado") ? MCDMExperiment.label(profile) : "prioridad " + profile);
            System.out.println();

            printRow("", healths, m -> m.toUpperCase(), null);
            printRow("solución", healths, m -> "#" + chosen.get(m), null);
            printRow("servidores en uso", healths, null, h -> h.getActiveServers() + "/" + h.getTotalServers());
            printRow("saturados (>80%)", healths, null, h -> String.valueOf(h.getSaturatedServers()));
            printRow("CPU máxima", healths, null, h -> String.format("%.0f%%", 100 * h.getMaxCpuUsage()));
            printRow("delay promedio", healths, null, h -> String.format("%.1f ms", h.getAvgDelay()));
            printRow("rechazados", healths, null, h -> String.valueOf(h.getTotalTraffics() - h.getAcceptedTraffics()));
            printRow("calidad de servicio", healths, null, h -> String.format("%.3f", h.getQos()));
            printRow("puntaje", healths, null, h -> String.format("%.3f", h.getScore()));

            printRanking(healths);
            System.out.println();

        } catch (Exception ex) {
            logger.fatal("Error al mostrar el estado de la red", ex);
        }
    }

    // MCDMService imprime la solucion elegida, se silencia la salida mientras elige
    private static Solution chooseQuietly(NondominatedPopulation front, NetworkConditionEnum condition, String method,
            String profile) throws Exception {
        if (!MCDMExperiment.PROFILES.containsKey(profile)) {
            throw new IllegalArgumentException("Perfil desconocido: " + profile + ". Opciones: " + MCDMExperiment.PROFILES.keySet());
        }
        PrintStream out = System.out;
        try {
            System.setOut(new PrintStream(OutputStream.nullOutputStream()));
            return new MCDMService().calculateOptimalSolution(front, condition, method, MCDMExperiment.PROFILES.get(profile));
        } finally {
            System.setOut(out);
        }
    }

    // Una fila de la tabla: el texto sale del nombre del metodo o de sus metricas
    private static void printRow(String label, Map<String, NetworkHealth> healths,
            Function<String, String> byMethod, Function<NetworkHealth, String> byHealth) {
        System.out.printf("  %-20s", label);
        healths.forEach((m, h) -> System.out.printf("%11s", byMethod != null ? byMethod.apply(m) : byHealth.apply(h)));
        System.out.println();
    }

    // Ranking por puntaje; los empates (mismos 3 decimales) comparten puesto
    private static void printRanking(Map<String, NetworkHealth> healths) {
        List<String> order = healths.keySet().stream()
                .sorted((a, b) -> Double.compare(round3(healths.get(b).getScore()), round3(healths.get(a).getScore())))
                .collect(Collectors.toList());
        double first = round3(healths.get(order.get(0)).getScore());

        System.out.println();
        System.out.println("Ranking");
        for (String m : order) {
            double score = round3(healths.get(m).getScore());
            long place = 1 + healths.values().stream().filter(h -> round3(h.getScore()) > score).count();
            String diff = score == first ? "" : String.format("  (%.3f)", score - first);
            System.out.printf("  %d. %-11s %.3f%s%n", place, m.toUpperCase(), score, diff);
        }
    }

    private static int indexOf(NondominatedPopulation front, Solution solution) {
        for (int i = 0; i < front.size(); i++) {
            if (front.get(i) == solution) {
                return i;
            }
        }
        return -1;
    }

    private static double round3(double value) {
        return Math.round(value * 1000) / 1000.0;
    }
}
