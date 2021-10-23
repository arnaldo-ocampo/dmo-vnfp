package py.edu.fiuni.dmop.util;

public class Constants {

    public static final String ZERO = "0";

    public static final String separatorData = "::";

    public static final String separatorVnf = "-";

    // Solution fileName format is::   solution_{ALGORITHM}_r{RoundNumber}_w{WindowsNumber}.dat
    public static final String SOLUTION_FILENAME_TEMPLATE = "solution_%s_r%d_w%d.dat";

    public static final int NUMBER_OF_WINDOWS = 12;
    public static final int LOWER_LIMIT = 10;
    public static final int UPPER_LIMIT = 130;
    public static final int NORMAL_UPPER_LIMIT = 75;
    public static final boolean RANDOMIZE_TRAFFICS = false;

    public static final int MAX_ROUNDS = 10;
    public static final int MAX_WINDOWS = 10;

    //public static final int POPULATION_SIZE = 100;
    //public static final int MAX_EVALUATIONS = 1000;
    // The number of networks request to be used by every windows
    public static int[] WINDOWS_TRAFFICS_COUNT = {30, 82, 44, 109, 77, 56, 33, 89, 125, 41, 73, 90};

    // TODO:: Order the actual traffics list instead of use this hardcoded version
    public static int[] WINDOWS_TRAFFICS_COUNT_SORTED = {0, 6, 9, 2, 5, 4, 1, 7, 3, 8};

}
