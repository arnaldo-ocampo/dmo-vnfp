package py.edu.fiuni.dmop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import py.edu.fiuni.dmop.util.Configurations;
import py.edu.fiuni.dmop.util.Utility;

/**
 * Lee el CSV de MCDMExperiment y muestra por consola los promedios, las
 * victorias de cada metodo y el ranking.
 *
 * Uso: ResumenResultados [archivo.csv]
 */
public class ResumenResultados {

    private static final Logger logger = Logger.getLogger(ResumenResultados.class);

    private static final String[] METHODS = {"topsis", "saw", "ahp", "promethee"};

    public static void main(String[] args) {
        try {
            Configurations.loadProperties();
            String network = new File(Configurations.networkPackage).getName();
            String path = args.length > 0 ? args[0]
                    : Utility.buildFilePath(Configurations.solutionsFolder + "/experimento_mcdm_" + network + ".csv");

            // frente -> ("perfil|metodo" -> puntaje)
            Map<String, Map<String, Double>> fronts = new LinkedHashMap<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
                List<String> header = Arrays.asList(reader.readLine().split(","));
                int alg = header.indexOf("algoritmo"), round = header.indexOf("ronda"), win = header.indexOf("ventana");
                int prof = header.indexOf("perfil"), met = header.indexOf("metodo"), score = header.indexOf("puntaje");
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] c = line.split(",");
                    String key = c[alg] + "_r" + c[round] + "_w" + c[win];
                    fronts.computeIfAbsent(key, k -> new HashMap<>()).put(c[prof] + "|" + c[met], Double.parseDouble(c[score]));
                }
            }
            List<Map<String, Double>> rows = new ArrayList<>(fronts.values());
            List<String> profiles = new ArrayList<>(MCDMExperiment.PROFILES.keySet());

            System.out.println();
            System.out.printf("Puntaje: promedio entre %d frentes%n%n", rows.size());

            // Promedios
            System.out.printf("  %-18s", "Prioridad/Método");
            for (String m : METHODS) {
                System.out.printf("%11s", m.toUpperCase());
            }
            System.out.println();
            String bestCombo = null;
            for (String p : profiles) {
                System.out.printf("  %-18s", MCDMExperiment.label(p));
                for (String m : METHODS) {
                    double mean = mean(rows, p + "|" + m);
                    System.out.printf("%11.3f", mean);
                    if (bestCombo == null || mean > mean(rows, bestCombo)) {
                        bestCombo = p + "|" + m;
                    }
                }
                System.out.println();
            }
            System.out.printf("  Al azar %.3f, peor solución del frente %.3f%n",
                    mean(rows, "-|aleatorio"), mean(rows, "-|peor_posible"));

            // Victorias con la prioridad de la mejor combinacion
            String bestProfile = bestCombo.split("\\|")[0];
            Map<String, Integer> wins = new LinkedHashMap<>();
            for (String m : METHODS) {
                wins.put(m, 0);
            }
            int ties = 0;
            for (Map<String, Double> r : rows) {
                double top = Arrays.stream(METHODS).mapToDouble(m -> shown(r.get(bestProfile + "|" + m))).max().orElse(0);
                List<String> winners = Arrays.stream(METHODS)
                        .filter(m -> shown(r.get(bestProfile + "|" + m)) == top).collect(Collectors.toList());
                if (winners.size() == METHODS.length) {
                    ties++;
                } else {
                    winners.forEach(m -> wins.merge(m, 1, Integer::sum));
                }
            }
            System.out.printf("%n  Veces que deja la mejor red (prioridad %s, %d frentes sin empate):%n   ", bestProfile, rows.size() - ties);
            wins.forEach((m, w) -> System.out.printf(" %s %d ", m.toUpperCase(), w));
            System.out.println();

            // Ranking de los metodos con pesos iguales; los empates comparten puesto
            String equal = profiles.get(0);
            List<String> order = Arrays.stream(METHODS)
                    .sorted((a, b) -> Double.compare(shown(mean(rows, equal + "|" + b)), shown(mean(rows, equal + "|" + a))))
                    .collect(Collectors.toList());
            double first = shown(mean(rows, equal + "|" + order.get(0)));

            System.out.println();
            System.out.println("Ranking");
            for (String m : order) {
                double value = shown(mean(rows, equal + "|" + m));
                long place = 1 + Arrays.stream(METHODS).filter(o -> shown(mean(rows, equal + "|" + o)) > value).count();
                String diff = value == first ? "" : String.format(Locale.US, "  (%.3f)", value - first);
                System.out.printf(Locale.US, "  %d. %-11s %.3f%s%n", place, m.toUpperCase(), value, diff);
            }
            System.out.printf(Locale.US, "%n  Mejor prioridad: %s (%s %.3f)%n%n",
                    bestProfile, bestCombo.split("\\|")[1].toUpperCase(), mean(rows, bestCombo));

        } catch (Exception ex) {
            logger.fatal("Error al leer los resultados", ex);
        }
    }

    private static double mean(List<Map<String, Double>> rows, String key) {
        return rows.stream().mapToDouble(r -> r.get(key)).average().orElse(Double.NaN);
    }

    // Valor con los 3 decimales que se muestran, para que los empates visibles sean empates
    private static double shown(double value) {
        return Double.parseDouble(String.format(Locale.US, "%.3f", value));
    }
}
