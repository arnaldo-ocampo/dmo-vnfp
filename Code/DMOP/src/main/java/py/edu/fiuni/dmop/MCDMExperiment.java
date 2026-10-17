package py.edu.fiuni.dmop;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Permutation;

import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.dto.NetworkHealth;
import py.edu.fiuni.dmop.dto.ResultGraphMap;
import py.edu.fiuni.dmop.dto.TrafficSolutionMap;
import py.edu.fiuni.dmop.problem.SceneObjectiveFunctions;
import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.service.MCDMService;
import py.edu.fiuni.dmop.service.NetworkStateService;
import py.edu.fiuni.dmop.service.ObjectiveFunctionService;
import py.edu.fiuni.dmop.service.TrafficService;
import py.edu.fiuni.dmop.service.VnfService;
import py.edu.fiuni.dmop.util.Configurations;
import py.edu.fiuni.dmop.util.Constants;
import py.edu.fiuni.dmop.util.NetworkConditionEnum;
import py.edu.fiuni.dmop.util.ObjectiveFunctionEnum;
import py.edu.fiuni.dmop.util.Utility;

/**
 * Para cada frente guardado: cada metodo elige una solucion, se implanta y se
 * guardan las metricas de la red en un CSV.
 *
 * Uso: MCDMExperiment [algoritmos separados por coma] [rondas] [ventanas]
 */
public class MCDMExperiment {

    private static final Logger logger = Logger.getLogger(MCDMExperiment.class);

    private static final String[] METHODS = {"topsis", "saw", "ahp", "promethee"};
    private static final String RANDOM = "aleatorio";
    private static final String BEST = "mejor_posible";
    private static final String WORST = "peor_posible";

    private static final int MAX_OBJECTIVES = 6;

    //Para las referencias (no usan pesos)
    private static final String NO_PROFILE = "-";

    //Pesos de cada perfil, los objetivos que no aparecen valen 1
    static final Map<String, Map<ObjectiveFunctionEnum, Double>> PROFILES = new LinkedHashMap<>();

    static {
        PROFILES.put("equilibrado", Map.of());
        PROFILES.put("latencia", Map.of(ObjectiveFunctionEnum.DELAY, 4.0));
        PROFILES.put("enlaces", Map.of(ObjectiveFunctionEnum.LINK_MAX_USE, 4.0));
        PROFILES.put("pocas_vnfs", Map.of(ObjectiveFunctionEnum.VNF_INSTANCES, 4.0));
        PROFILES.put("costo", Map.of(ObjectiveFunctionEnum.ENERGY_COST, 4.0, ObjectiveFunctionEnum.RESOURCES_COST, 4.0));
    }

    //Nombre del perfil para mostrar por consola
    static String label(String profile) {
        return profile.equals("equilibrado") ? "pesos iguales" : profile;
    }

    public static void main(String[] args) {

        String[] algorithms = args.length > 0 ? args[0].split(",") : new String[]{"DNSGAII-A", "DNSGAII-B"};
        int rounds = args.length > 1 ? Integer.parseInt(args[1]) : Constants.MAX_ROUNDS;
        int windowsCount = args.length > 2 ? Integer.parseInt(args[2]) : Constants.MAX_WINDOWS;

        try {
            Configurations.loadProperties();
            DataService.loadData();

            String network = new File(Configurations.networkPackage).getName();
            String outputFile = Utility.buildFilePath(Configurations.solutionsFolder + "/experimento_mcdm_" + network + ".csv");

            List<Row> rows = new ArrayList<>();
            long start = System.currentTimeMillis();

            try (PrintWriter writer = new PrintWriter(outputFile, "UTF-8")) {
                writer.println(Row.header());

                for (String algorithm : algorithms) {
                    for (int windows = 0; windows < windowsCount; windows++) {
                        List<Traffic> traffics = new TrafficService().readTraffics(String.format("Traffic_%d.txt", windows));
                        NetworkConditionEnum condition = traffics.size() > Constants.NORMAL_UPPER_LIMIT
                                ? NetworkConditionEnum.Overloaded : NetworkConditionEnum.Normal;

                        for (int round = 1; round <= rounds; round++) {
                            List<Row> frontRows = runFront(network, algorithm, round, windows, traffics, condition);
                            frontRows.forEach(r -> writer.println(r.toCsv()));
                            writer.flush();
                            rows.addAll(frontRows);
                        }
                        System.out.printf("%s ventana %d lista (%s)%n", algorithm, windows,
                                Utility.getTime(System.currentTimeMillis() - start));
                    }
                }
            }

            printSummary(rows);
            System.out.println("Resultados guardados en: " + outputFile);

        } catch (Exception ex) {
            logger.fatal("Error en el experimento", ex);
        }
    }

    //Frente guardado con los objetivos recalculados y la salud de la red de cada solucion
    static class EvaluatedFront {

        NondominatedPopulation front;
        Map<Solution, NetworkHealth> healthOf = new IdentityHashMap<>();
        int storedSize, corrected;
    }

    static EvaluatedFront evaluateFront(String algorithm, int round, int windows,
            List<Traffic> traffics, NetworkConditionEnum condition) throws Exception {

        String fileName = String.format(Constants.SOLUTION_FILENAME_TEMPLATE, algorithm, round, windows);
        Population stored = PopulationIO.read(new File(Utility.buildFilePath(Configurations.solutionsFolder + "/" + fileName)));

        List<ObjectiveFunctionEnum> objectives = SceneObjectiveFunctions.SceneMap.get(condition);
        VnfService vnfService = new VnfService();
        ObjectiveFunctionService objectiveService = new ObjectiveFunctionService();
        NetworkStateService stateService = new NetworkStateService();

        //Se implanta cada solucion y se recalculan sus objetivos
        EvaluatedFront result = new EvaluatedFront();
        Population reevaluated = new Population();

        for (Solution original : stored) {
            Permutation permutation = (Permutation) original.getVariable(0);
            ResultGraphMap deployed = vnfService.placementGraph(traffics, permutation);
            TrafficSolutionMap values = objectiveService.solutionTrafficFOs(
                    deployed.getNodesMap(), deployed.getLinksMap(), deployed.getTraffics(), DataService.vnfsShared);

            Solution solution = original.copy();
            boolean changed = false;
            for (int i = 0; i < objectives.size(); i++) {
                double value = values.getObjectiveFunctionValueFor(objectives.get(i));
                if (Math.abs(value - original.getObjective(i)) > 1e-6) {
                    changed = true;
                }
                solution.setObjective(i, value);
            }
            if (changed) {
                result.corrected++;
            }

            result.healthOf.put(solution, stateService.evaluate(deployed));
            reevaluated.add(solution);
        }

        //Frente con los objetivos recalculados
        result.front = new NondominatedPopulation(reevaluated);
        result.storedSize = stored.size();
        return result;
    }

    private static List<Row> runFront(String network, String algorithm, int round, int windows,
            List<Traffic> traffics, NetworkConditionEnum condition) throws Exception {

        EvaluatedFront evaluated = evaluateFront(algorithm, round, windows, traffics, condition);
        NondominatedPopulation front = evaluated.front;
        Map<Solution, NetworkHealth> healthOf = evaluated.healthOf;

        FrontInfo info = new FrontInfo(network, algorithm, round, windows, traffics.size(), condition,
                evaluated.storedSize, evaluated.corrected, front.size());

        //Eleccion de cada metodo con cada perfil
        List<Row> rows = new ArrayList<>();
        for (Map.Entry<String, Map<ObjectiveFunctionEnum, Double>> profile : PROFILES.entrySet()) {
            for (String method : METHODS) {
                Solution chosen = chooseQuietly(front, condition, method, profile.getValue());
                rows.add(new Row(info, profile.getKey(), method, indexOf(front, chosen), chosen, healthOf.get(chosen)));
            }
        }

        //Referencias: al azar, mejor y peor del frente
        Solution random = front.get(new Random(1000L * round + windows).nextInt(front.size()));
        rows.add(new Row(info, NO_PROFILE, RANDOM, indexOf(front, random), random, healthOf.get(random)));

        Comparator<Solution> byScore = Comparator.comparingDouble(s -> healthOf.get(s).getScore());
        Solution best = front.get(0), worst = front.get(0);
        for (Solution s : front) {
            if (byScore.compare(s, best) > 0) {
                best = s;
            }
            if (byScore.compare(s, worst) < 0) {
                worst = s;
            }
        }
        rows.add(new Row(info, NO_PROFILE, BEST, indexOf(front, best), best, healthOf.get(best)));
        rows.add(new Row(info, NO_PROFILE, WORST, indexOf(front, worst), worst, healthOf.get(worst)));

        return rows;
    }

    //MCDMService imprime la solucion elegida, se silencia la salida
    private static Solution chooseQuietly(NondominatedPopulation front, NetworkConditionEnum condition, String method,
            Map<ObjectiveFunctionEnum, Double> weights) throws Exception {
        PrintStream out = System.out;
        try {
            System.setOut(new PrintStream(OutputStream.nullOutputStream()));
            return new MCDMService().calculateOptimalSolution(front, condition, method, weights);
        } finally {
            System.setOut(out);
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

    //Resumen en consola
    private static void printSummary(List<Row> rows) {

        System.out.println();
        System.out.println("=====================================================================================");
        System.out.println(" RESUMEN: puntaje de salud promedio de la red que deja cada metodo (0 a 1)");
        System.out.println("=====================================================================================");
        System.out.printf(" %-12s", "Perfil");
        for (String m : METHODS) {
            System.out.printf("%12s", m.toUpperCase());
        }
        System.out.println();
        for (String profile : PROFILES.keySet()) {
            System.out.printf(" %-12s", profile);
            for (String m : METHODS) {
                List<Row> r = rows.stream().filter(x -> x.profile.equals(profile) && x.method.equals(m)).toList();
                System.out.printf("%12.3f", avg(r, null, x -> x.health.getScore()));
            }
            System.out.println();
        }

        System.out.println();
        System.out.println(" Referencias (no dependen de los pesos):");
        for (String ref : new String[]{RANDOM, BEST, WORST}) {
            List<Row> r = rows.stream().filter(x -> x.method.equals(ref)).toList();
            System.out.printf("   %-14s %.3f  (normal %.3f, sobrecarga %.3f)%n", ref,
                    avg(r, null, x -> x.health.getScore()),
                    avg(r, NetworkConditionEnum.Normal, x -> x.health.getScore()),
                    avg(r, NetworkConditionEnum.Overloaded, x -> x.health.getScore()));
        }

        System.out.println();
        System.out.println(" Notas promedio por perfil (promedio de los 4 metodos):");
        System.out.printf("   %-12s %9s %9s %9s %9s %9s %9s%n", "Perfil", "Puntaje", "Satur.", "Balance", "Eficien.", "Enlaces", "QoS");
        for (String profile : PROFILES.keySet()) {
            List<Row> r = rows.stream().filter(x -> x.profile.equals(profile)).toList();
            System.out.printf("   %-12s %9.3f %9.3f %9.3f %9.3f %9.3f %9.3f%n", profile,
                    avg(r, null, x -> x.health.getScore()), avg(r, null, x -> x.health.getSaturation()),
                    avg(r, null, x -> x.health.getBalance()), avg(r, null, x -> x.health.getEfficiency()),
                    avg(r, null, x -> x.health.getLinks()), avg(r, null, x -> x.health.getQos()));
        }

        long fronts = rows.stream().map(r -> r.info.key()).distinct().count();
        long frontsCorrected = rows.stream().filter(r -> r.info.corrected > 0).map(r -> r.info.key()).distinct().count();
        System.out.printf("%n Frentes evaluados: %d. Con objetivos guardados que no coincidian (corregidos): %d%n", fronts, frontsCorrected);
        System.out.println("=====================================================================================");
    }

    private static double avg(List<Row> rows, NetworkConditionEnum condition, java.util.function.ToDoubleFunction<Row> value) {
        return rows.stream().filter(r -> condition == null || r.info.condition == condition)
                .mapToDouble(value).average().orElse(Double.NaN);
    }

    //Fila del CSV
    private static class FrontInfo {

        final String network, algorithm;
        final int round, windows, traffics, storedSize, corrected, frontSize;
        final NetworkConditionEnum condition;

        FrontInfo(String network, String algorithm, int round, int windows, int traffics,
                NetworkConditionEnum condition, int storedSize, int corrected, int frontSize) {
            this.network = network;
            this.algorithm = algorithm;
            this.round = round;
            this.windows = windows;
            this.traffics = traffics;
            this.condition = condition;
            this.storedSize = storedSize;
            this.corrected = corrected;
            this.frontSize = frontSize;
        }

        String key() {
            return algorithm + "_r" + round + "_w" + windows;
        }
    }

    private static class Row {

        final FrontInfo info;
        final String profile;
        final String method;
        final int solutionIndex;
        final Solution solution;
        final NetworkHealth health;

        Row(FrontInfo info, String profile, String method, int solutionIndex, Solution solution, NetworkHealth health) {
            this.info = info;
            this.profile = profile;
            this.method = method;
            this.solutionIndex = solutionIndex;
            this.solution = solution;
            this.health = health;
        }

        static String header() {
            StringBuilder sb = new StringBuilder(
                    "red,algoritmo,ronda,ventana,traficos,condicion,soluciones_guardadas,objetivos_corregidos,tam_frente,perfil,metodo,solucion");
            for (int i = 1; i <= MAX_OBJECTIVES; i++) {
                sb.append(",obj").append(i);
            }
            sb.append(",saturacion,balance,eficiencia,enlaces,calidad_servicio,puntaje");
            sb.append(",servidores_total,servidores_activos,servidores_saturados,uso_max_enlace,traficos_aceptados,traficos_cumplen_sla");
            sb.append(",delay_promedio_ms,cpu_promedio_activos,cpu_max,ram_max");
            return sb.toString();
        }

        String toCsv() {
            StringBuilder sb = new StringBuilder();
            sb.append(info.network).append(',').append(info.algorithm).append(',').append(info.round).append(',')
                    .append(info.windows).append(',').append(info.traffics).append(',').append(info.condition).append(',')
                    .append(info.storedSize).append(',').append(info.corrected).append(',').append(info.frontSize).append(',')
                    .append(profile).append(',').append(method).append(',').append(solutionIndex);
            for (int i = 0; i < MAX_OBJECTIVES; i++) {
                sb.append(',');
                if (i < solution.getNumberOfObjectives()) {
                    sb.append(fmt(solution.getObjective(i)));
                }
            }
            sb.append(',').append(fmt(health.getSaturation())).append(',').append(fmt(health.getBalance()))
                    .append(',').append(fmt(health.getEfficiency())).append(',').append(fmt(health.getLinks()))
                    .append(',').append(fmt(health.getQos())).append(',').append(fmt(health.getScore()))
                    .append(',').append(health.getTotalServers()).append(',').append(health.getActiveServers())
                    .append(',').append(health.getSaturatedServers()).append(',').append(fmt(health.getMaxLinkUsage()))
                    .append(',').append(health.getAcceptedTraffics()).append(',').append(health.getSlaCompliantTraffics())
                    .append(',').append(fmt(health.getAvgDelay())).append(',').append(fmt(health.getAvgCpuUsage()))
                    .append(',').append(fmt(health.getMaxCpuUsage())).append(',').append(fmt(health.getMaxRamUsage()));
            return sb.toString();
        }

        private static String fmt(double value) {
            return String.format(Locale.US, "%.6f", value);
        }
    }
}
