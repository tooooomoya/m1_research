package constants;

public class Const {
    // simulation parameter
    public static final int MAX_SIMULATION_STEP = 10000;
    public static final int NUM_OF_USER = 200;
    public static final int NUM_OF_SNS_USER = NUM_OF_USER;
    public static final int RANDOM_SEED = 5;

    // Admin feedback parameter
    // public static final double FEEDBACK_INCREASE_WEIGHT = 0.1;
    public static final double LIKE_INCREASE_WEIGHT = 0.001;
    public static final double FOLLOW_INCREASE_WEIGHT = 0.1;

    // public static final double UNACCEPTABLE_BOUNDED_CONFIDENCE = 0.7;

    // network parameter
    public static final double CONNECTION_PROB_OF_RANDOM_NW = 0.2;

    // follow parameter
    public static final double INITIAL_FOLLOW_RATE = 0.5;

    // unfollow parameter
    public static final double INITIAL_UNFOLLOW_RATE = 0.1;
    //public static final double MIN_OPINION_DIFF_TO_UNFOLLOW = 0.4;

    // agent parameter
    public static final double INITIAL_POST_PROB = 0.1;
    public static final double BOUNDED_CONFIDENCE = 1.5;
    public static final double MINIMUM_BC = 0.1;
    public static final double INITIAL_MEDIA_USER_RATE = 0.1;
    public static final double COMFORT_RATE = 0.5;
    public static final double INITIAL_TOLERANCE = 0.8; // 0.8は決してunrealisticではない

    // input data parameter
    public static final String EDGES_FILE_PATH = "Twitter/edgesTwitter.txt";
    public static final String OPINION_FILE_PATH = "Twitter/Twitter_opinion.txt";


    // result data parameter
    public static final String[] RESULT_LIST = {"opinionVar", "postOpinionVar","follow", "unfollow", "rewire"};
    public static final String RESULT_FOLDER_PATH = "results";
    public static final int NUM_OF_BINS_OF_POSTS = 5; // 投稿のデータをopinionごとに集計する際に何分割するか
    public static final int NUM_OF_BINS_OF_OPINION = 5; // 描画するときのopinonを何分割するか
}
