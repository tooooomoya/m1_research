package agent;

import constants.Const;
import java.util.List;
import java.util.Random;
import rand.randomGenerater;

public class Agent {
    private int id;
    private double opinion;
    private double tolerance;
    private double bc; // Bounded Confidence
    private double intrinsicOpinion;
    private final int NUM_OF_AGENTS = Const.NUM_OF_SNS_USER;
    private static final Random rand = randomGenerater.rand;
    private final double followProbThres = Const.FOLLOW_RATE;
    private final double unfollowProbThres = Const.UNFOLLOW_RATE;
    private int toPost; // ある時刻において何件の投稿をするか
    private int numOfPosts; // 一度に何件の投稿を閲覧できるか
    private final double rewireProbThres = Const.REWIRE_RATE;
    private int opinionClass;
    private PostCash postCash;
    private double postProb;
    private int[] feed; // ユーザjの投稿を何件閲覧できるか(タイムラインのモデル)、Adminによって操作される

    // constructor
    public Agent(int agentID) {
        this.id = agentID;
        // this.tolerance = rand.nextDouble(); // 0〜1 の乱数
        this.tolerance = 0.8;
        this.intrinsicOpinion = Math.max(-1.0, Math.min(1.0, rand.nextGaussian() * 0.6));
        // this.intrinsicOpinion = rand.nextDouble() * 2.0 - 1;
        this.opinion = this.intrinsicOpinion;
        this.bc = Const.BOUNDED_CONFIDENCE; // 動的
        this.numOfPosts = rand.nextInt(90) + 10;
        setOpinionClass();
        this.postCash = new PostCash(numOfPosts);
        this.postProb = 0.1;
        this.feed = new int[NUM_OF_AGENTS];
    }

    // getter methods

    public int getId(){
        return this.id;
    }

    public double getOpinion() {
        return this.opinion;
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

    public double getPostProb(){
        return this.postProb;
    }

    public int[] getFeed() {
        return this.feed;
    }

    // setter methods

    public void setOpinion(double value) {
        this.opinion = value;
    }

    public void setTolerance(double value) {
        this.tolerance = value;
    }

    public void setIntrinsicOpinion(double value) {
        this.intrinsicOpinion = value;
    }

    public void setNumOfPosts(int value) {
        this.numOfPosts = value;
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
        this.postCash.addPost(post);
    }

    public void setFeed(int[] feed) {
        this.feed = feed.clone();
    }

    // other methods
    public void resetPostCash() {
        this.postCash.reset();
    }

    public void updateMyself() {
        if (rand.nextDouble() > 0.1) {
            return;
        }

        int[] tempFeed = this.feed.clone();
        double temp = 0.0;

        int postNum = 0;
        int comfortPostNum = 0;
        // feedに表示される投稿は全て閲覧する
        for (Post post : postCash.getAllPosts()) {
            if (tempFeed[post.getPostUserId()] > 0) {
                temp += post.getPostOpinion();
                tempFeed[post.getPostUserId()]--;
                postNum++;
                if(Math.abs(post.getPostOpinion() - this.opinion) < Const.MINIMUM_BC){
                    comfortPostNum++;
                }
            }
        }

        if (postNum == 0)
            return;

        double comfortPostRate = (double) comfortPostNum / postNum;
        if(comfortPostRate > 0.8){
            postProb += 0.1;
        }

        this.opinion = this.tolerance * this.intrinsicOpinion + (1 - this.tolerance) * (temp / postNum);

        if (this.opinion < -1) {
            this.opinion = -1;
        } else if (this.opinion > 1) {
            this.opinion = 1;
        }

        setOpinionClass();

        // System.out.println("clipped updated opinion" + this.opinion);
    }

    public int like() {
        int attemps = 0;
        while (attemps < 100) {
            Post likedPost = this.postCash.getAllPosts()[rand.nextInt(postCash.getSize())];
            if (Math.abs(likedPost.getPostOpinion() - this.opinion) < this.bc) {
                int likeId = likedPost.getPostUserId();
                return likeId;
            }
            attemps++;
        }
        return -1;
    }

    public int follow(List<Integer> followList, Agent[] agentSet) {
        int followId;
        int attempts = 0;

        while (attempts < 100) {
            int tempId = followList.get(rand.nextInt(followList.size()));
            if (Math.abs(this.opinion - agentSet[tempId].getOpinion()) < this.bc) {
                followId = tempId;
                return followId;
            }
            attempts++;
        }

        return -1; // 100回試しても条件を満たすエージェントが見つからなかった場合
    }

    // 閲覧した投稿の中でBC以上の意見の差があったらunfollowする
    public int unfollow() {
        int attemps = 0;
        while (attemps < 100) {
            Post unfollowPost = this.postCash.getAllPosts()[rand.nextInt(postCash.getSize())];
            if (Math.abs(unfollowPost.getPostOpinion() - this.opinion) > this.bc) {
                int unfollowId = unfollowPost.getPostUserId();
                this.bc -= 0.05;
                return unfollowId;
            }
            attemps++;
        }
        return -1;
    }

    public Post makePost(int step){
        Post post = new Post(this.id, this.opinion, step);
        return post;
    }

}
