package constants;

public class Const {
    // simulation parameter
    public static final int MAX_SIMULATION_STEP = 20000;
    public static final int NUM_OF_USER = 1000;
    public static final int NUM_OF_SEED_USER = (int) (0.003 * NUM_OF_USER);
    public static final int NUM_OF_SNS_USER = NUM_OF_USER;
    public static final int RANDOM_SEED = 5;

    // Admin feedback parameter
    // public static final double FEEDBACK_INCREASE_WEIGHT = 0.1;
    public static final double LIKE_INCREASE_WEIGHT = 0.001;
    public static final double FOLLOW_INCREASE_WEIGHT = 0.1;
    public static final int MAX_RECOMMENDATION_POST_LENGTH = 100;
    public static final int LATEST_POST_LIST_LENGTH = 200;

    // public static final double UNACCEPTABLE_BOUNDED_CONFIDENCE = 0.7;

    // network parameter
    public static final double CONNECTION_PROB_OF_RANDOM_NW = 0.01;
    
    // agent parameter
    public static final double BOUNDED_CONFIDENCE = 1.0;
    public static final double MINIMUM_BC = 0.1;
    public static final double REPOST_PROB = 0.4;
    public static final double POST_COST = 0.1;
    public static final double MU_PRAM = 0.1; // Marginal Utility log func parameter

    // user num = 1000
    // increment mur = 0にしても大丈夫か
    public static final double INITIAL_CNN_SEED_GRAPH_CONNECT_PROB = 0.2;
    public static final double INITIAL_POST_PROB = 0.1;
    public static final double INITIAL_MEDIA_USER_RATE = 0.1;
    public static final double INCREMENT_PP = 0.1;
    public static final double INCREMENT_MUR = 0.0;
    public static final double DECREMENT_PP = 0.0;
    public static final double DECREMENT_MUR = 0.0;
    public static final double MIN_PP = 0.1;
    public static final double MIN_MUR = INITIAL_MEDIA_USER_RATE;
    public static final double DECREMENT_BC_BY_UNFOLLOW = 0.025;
    //public static final double INCREMENT_BC = 0.00001;

    // user num = 5000
    // 実験が進みやすいようにMURは小さくしたい。
    /*public static final double INITIAL_CNN_SEED_GRAPH_CONNECT_PROB = 0.04; // 1人あたり10人
    public static final double INITIAL_POST_PROB = 0.1;
    public static final double INITIAL_MEDIA_USER_RATE = 0.02;
    public static final double INCREMENT_PP = 0.01;
    public static final double INCREMENT_PP_BY_LIKE = 0.002;
    public static final double INCREMENT_MUR = 0.002;
    public static final double DECREMENT_PP = 0.001;
    public static final double DECREMENT_MUR = 0.0001;
    public static final double MIN_PP = 0.01;
    public static final double MIN_MUR = 0.01;
    public static final double DECREMENT_BC_BY_UNFOLLOW = 0.01;
    public static final double INCREMENT_BC = 0.000001;*/

    public static final double COMFORT_RATE = 0.5;
    public static final double INITIAL_TOLERANCE = 0.8; // 0.8は決してunrealisticではない
    public static final double FEED_PREFERENTIALITY_RATE = 0.0;

    // follow parameter
    public static final double INITIAL_FOLLOW_RATE = 1.0;

    // unfollow parameter
    public static final double INITIAL_UNFOLLOW_RATE = 0.1;
    // public static final double MIN_OPINION_DIFF_TO_UNFOLLOW = 0.4;

    // input data parameter
    public static final String EDGES_FILE_PATH = "Twitter/edgesTwitter.txt";
    public static final String OPINION_FILE_PATH = "Twitter/Twitter_opinion.txt";

    public static final String READ_NW_PATH = "results/temp/June6_plain_step_20000.gexf";

    // result data parameter
    public static final String[] RESULT_LIST = { "opinionVar", "postOpinionVar", "follow", "unfollow", "rewire", "opinionAvg",
    "feedPostOpinionMean_0", "feedPostOpinionMean_1", "feedPostOpinionMean_2", "feedPostOpinionMean_3", "feedPostOpinionMean_4", "feedPostOpinionVar_0",
    "feedPostOpinionVar_1", "feedPostOpinionVar_2", "feedPostOpinionVar_3", "feedPostOpinionVar_4"};
    public static final String RESULT_FOLDER_PATH = "results";
    public static final int NUM_OF_BINS_OF_POSTS = 5; // 投稿のデータをopinionごとに集計する際に何分割するか
    public static final int NUM_OF_BINS_OF_OPINION = 5; // Agentが待つopinion classの分割数を指定
    public static final int NUM_OF_BINS_OF_OPINION_FOR_WRITER = 10; // writerがresultsフォルダに記録するデータの分割数を指定
}
