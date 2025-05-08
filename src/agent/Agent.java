package agent;

import constants.Const;
import java.util.*;
import rand.randomGenerater;

public class Agent {
    private int id;
    private double opinion;
    private double tolerance;
    private double bc; // Bounded Confidence
    private double intrinsicOpinion;
    private final int NUM_OF_AGENTS = Const.NUM_OF_SNS_USER;
    private static final Random rand = randomGenerater.rand;
    private int toPost; // ある時刻において何件の投稿をするか
    private int numOfPosts; // 一度に何件の投稿を閲覧するか
    private int opinionClass;
    private PostCash postCash;
    private double postProb;
    private List<Post> feed = new ArrayList<>(); // ユーザjの投稿を何件閲覧できるか(タイムラインのモデル)、Adminによって操作される
    private double mediaUseRate = Const.INITIAL_MEDIA_USER_RATE;
    private double followRate;
    private double unfollowRate;
    private boolean traitor = false;
    private int timeStep;
    private boolean[] followList = new boolean[NUM_OF_AGENTS];
    private Set<Integer> alreadyAddedPostIds = new HashSet<>();

    // constructor
    public Agent(int agentID) {
        this.id = agentID;
        // this.tolerance = rand.nextDouble(); // 0〜1 の乱数
        this.tolerance = Const.INITIAL_TOLERANCE;
        this.intrinsicOpinion = Math.max(-1.0, Math.min(1.0, rand.nextGaussian() * 0.6));
        // this.intrinsicOpinion = rand.nextDouble() * 2.0 - 1;
        this.opinion = this.intrinsicOpinion;
        this.bc = Const.BOUNDED_CONFIDENCE; // 動的
        // this.numOfPosts = rand.nextInt(80) + 20;
        // this.numOfPosts = 10;
        setOpinionClass();
        this.postProb = Const.INITIAL_POST_PROB;
        this.followRate = Const.INITIAL_FOLLOW_RATE;
        this.unfollowRate = Const.INITIAL_UNFOLLOW_RATE;
        this.timeStep = 0;
        setNumOfPosts(rand.nextInt(10) + 10);
        /*
         * if(0.1 > rand.nextDouble()){
         * this.traitor = true;
         * this.intrinsicOpinion = 0.0;
         * }
         */
    }

    // getter methods

    public int getId() {
        return this.id;
    }

    public double getOpinion() {
        return this.opinion;
    }

    public double getIntrinsicOpinion() {
        return this.intrinsicOpinion;
    }

    public double getTolerance() {
        return this.tolerance;
    }

    public int getNumOfPosts() {
        return this.numOfPosts;
    }

    public int getToPost() {
        return this.toPost;
    }

    public int getOpinionClass() {
        return this.opinionClass;
    }

    public double getBc() {
        return this.bc;
    }

    public double getPostProb() {
        return this.postProb;
    }

    public List<Post> getFeed() {
        return this.feed;
    }

    public double getMediaUseRate() {
        return this.mediaUseRate;
    }

    public double getFollowRate() {
        return this.followRate;
    }

    public double getUnfollowRate() {
        return this.unfollowRate;
    }

    public PostCash getPostCash() {
        return this.postCash;
    }

    // setter methods

    public void setOpinion(double value) {
        this.opinion = value;
    }

    public void setTimeStep(int time) {
        this.timeStep = time;
    }

    public void setTolerance(double value) {
        this.tolerance = value;
    }

    public void setIntrinsicOpinion(double value) {
        this.intrinsicOpinion = value;
    }

    public void setNumOfPosts(int value) {
        this.numOfPosts = value;
        setPostCash(this.numOfPosts);
    }

    public void setPostCash(int value) {
        this.postCash = new PostCash(value);
    }

    public void setToPost(int value) {
        this.toPost = value;
    }

    public void setOpinionClass() {
        double shiftedOpinion = this.opinion + 1; // [-1,1] → [0,2]
        double opinionBinWidth = 2.0 / Const.NUM_OF_BINS_OF_OPINION;
        this.opinionClass = (int) Math.min(shiftedOpinion / opinionBinWidth, Const.NUM_OF_BINS_OF_OPINION - 1);
    }

    public void addToPostCash(Post post) {
        if (!this.alreadyAddedPostIds.contains(post.getPostId())) {
            this.postCash.addPost(post);
        }
    }

    // other methods
    public void resetPostCash() {
        this.postCash.reset();
        this.toPost = 0;
    }

    public void addPostToFeed(Post post) {
        if (!this.alreadyAddedPostIds.contains(post.getPostId())) {
            this.feed.add(post);
        }
    }

    public void resetFeed() {
        this.feed.clear();
    }

    public void updateFollowList(double[][] adjacencyMatrix) {
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            for (int j = 0; j < adjacencyMatrix.length; j++) {
                if (adjacencyMatrix[i][j] > 0.0) {
                    this.followList[j] = true;
                } else {
                    this.followList[j] = false;
                }
            }
        }
    }

    public void updateMyself() {
        double temp = 0.0;

        int postNum = 0;
        int comfortPostNum = 0;
        // feedに表示される投稿は全て閲覧する
        for (Post post : this.feed) {
            temp += post.getPostOpinion();
            postNum++;
            if (Math.abs(post.getPostOpinion() - this.opinion) < Const.MINIMUM_BC) {
                comfortPostNum++;
            }
        }

        if (postNum == 0)
            return;

        double comfortPostRate = (double) comfortPostNum / postNum;

        if (comfortPostRate > Const.COMFORT_RATE) {
            this.postProb += 0.01 * decayFunc(this.timeStep);
            this.mediaUseRate += 0.01 * decayFunc(this.timeStep);
        } else {
            //this.postProb -= 0.0001 * decayFunc(this.timeStep);
            //this.mediaUseRate -= 0.0001 * decayFunc(this.timeStep);
        }

        this.opinion = this.tolerance * this.intrinsicOpinion + (1 - this.tolerance) * (temp / postNum);
        // this.opinion = this.tolerance * this.opinion + (1 - this.tolerance) * (temp /
        // postNum);
        if (this.traitor) {
            // this.opinion = (temp / postNum) - 0.5;
        }

        if (this.opinion < -1) {
            this.opinion = -1;
        } else if (this.opinion > 1) {
            this.opinion = 1;
        }
        if (this.postProb > 1.0) {
            this.postProb = 1.0;
        } else if (this.postProb < 0.0) {
            this.postProb = 0.0;
        }
        if (this.mediaUseRate > 1.0) {
            this.mediaUseRate = 1.0;
        } else if (this.mediaUseRate < 0.0) {
            this.mediaUseRate = 0.0;
        }

        setOpinionClass();
    }

    public int like() {
        List<Post> candidates = new ArrayList<>();
        if (this.feed.size() <= 0) {
            return -1;
        }

        // 条件に合う投稿をすべてリストアップ
        for (Post post : this.feed) {
            if (Math.abs(post.getPostOpinion() - this.opinion) < this.bc) {
                candidates.add(post);
            }
        }

        // 条件に合う投稿が存在すればランダムに1つ選ぶ
        if (!candidates.isEmpty()) {
            Post likedPost = candidates.get(rand.nextInt(candidates.size()));
            likedPost.receiveLike();
            return likedPost.getPostUserId();
        } else {
            return -1;
        }
    }

    public int follow(List<Post> latestPostList) {
        if(this.followList.length == 0){
            return rand.nextInt(NUM_OF_AGENTS);
        }
        if (this.followRate < rand.nextDouble()) {
            return -1;
        }

        List<Integer> candidates = new ArrayList<>();
        for (Post post : latestPostList) {
            if (Math.abs(post.getPostOpinion() - this.opinion) < Const.MINIMUM_BC && this.id != post.getPostUserId()
                    && !followList[post.getPostUserId()]) {
                candidates.add(post.getPostUserId());
            }
        }
        if (!candidates.isEmpty()) {
            return candidates.get(rand.nextInt(candidates.size()));
        } else {
            return -1;
        }
    }

    // 閲覧した投稿の中でBC以上の意見の差があったらunfollowする
    public int unfollow() {
        int attemps = 0;
        if (this.feed.size() <= 0.0) {
            return -1;
        }
        while (attemps < 100) {
            Post unfollowPost = this.feed.get(rand.nextInt(this.feed.size()));
            if (Math.abs(unfollowPost.getPostOpinion() - this.opinion) > this.bc) {
                int unfollowId = unfollowPost.getPostUserId();
                this.bc -= 0.05 * decayFunc(this.timeStep);
                if (this.bc < Const.MINIMUM_BC) {
                    this.bc = Const.MINIMUM_BC;
                }
                return unfollowId;
            }
            attemps++;
        }
        return -1;
    }

    public Post makePost(int step) {
        // 極端な投稿を自重するようにmoderate
        Post post;
        if (this.opinion > 0.8 && rand.nextDouble() < 0.0) {
            post = new Post(this.id, this.opinion - 0.1, step);
        } else if (this.opinion < -0.8 && rand.nextDouble() < 0.0) {
            post = new Post(this.id, this.opinion + 0.1, step);
        } else {
            post = new Post(this.id, this.opinion, step);
        }
        this.toPost = 1;
        return post;
    }

    public double decayFunc(double time) {
        double lambda = 0.001;
        return Math.exp(-lambda * time);
        //return 1;
    }

}
