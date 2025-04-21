package constants;

public class Const {
    public static final int MAX_SIMULATION_STEP = 100;
    public static final double DYNAMIC_RATE_OF_ADMIN = 0.05;

    public static final int NUM_OF_USER = 500;
    public static final int NUM_OF_SNS_USER = 500;

    public static final double CONNECTION_PROB_OF_RANDOM_NW = 0.02;

    // follow parameter
    public static final double FOLLOW_RATE = 0.5;
    public static final double BOUNDED_CONFIDENCE = 0.4;

    // unfollow parameter
    public static final double UNFOLLOW_RATE = 0.1;
    public static final double MIN_OPINION_DIFF_TO_UNFOLLOW = 0.4;

    // rewire parameter
    public static final double REWIRE_RATE = 0.5;

    public static final String EDGES_FILE_PATH = "Twitter/edgesTwitter.txt";
    public static final String OPINION_FILE_PATH = "Twitter/Twitter_opinion.txt";

    public static final String[] RESULT_LIST = {"opinionVar", "follow", "unfollow", "rewire"};
    public static final String RESULT_FOLDER_PATH = "results";
}
