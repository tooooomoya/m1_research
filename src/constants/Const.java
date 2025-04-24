package constants;

public class Const {
    // simulation parameter
    public static final int MAX_SIMULATION_STEP = 1000;
    public static final double DYNAMIC_RATE_OF_ADMIN = 0.01;
    public static final int NUM_OF_USER = 200;
    public static final int NUM_OF_SNS_USER = 200;

    // Admin feedback parameter
    public static final double FEEDBACK_INCREASE_WEIGHT = 0.1;

    public static final double UNACCEPTABLE_BOUNDED_CONFIDENCE = 0.7;

    // network parameter
    public static final double CONNECTION_PROB_OF_RANDOM_NW = 0.1;

    // follow parameter
    public static final double FOLLOW_RATE = 0.5;

    // unfollow parameter
    public static final double UNFOLLOW_RATE = 0.1;
    //public static final double MIN_OPINION_DIFF_TO_UNFOLLOW = 0.4;

    // rewire parameter
    public static final double REWIRE_RATE = 1.0;
    public static final double BOUNDED_CONFIDENCE = 1.2;
    public static final double MINIMUM_BC = 0.1;
    // BC 0.8　は全体分極は起こる
    // Bc 1.0, tol 0.6 

    // input data parameter
    public static final String EDGES_FILE_PATH = "Twitter/edgesTwitter.txt";
    public static final String OPINION_FILE_PATH = "Twitter/Twitter_opinion.txt";


    // result data parameter
    public static final String[] RESULT_LIST = {"opinionVar", "follow", "unfollow", "rewire"};
    public static final String RESULT_FOLDER_PATH = "results";
    public static final int NUM_OF_BINS_OF_POSTS = 5; // 投稿のデータをopinionごとに集計する際に何分割するか
    public static final int NUM_OF_BINS_OF_OPINION = 5; // 描画するときのopinonを何分割するか
}
