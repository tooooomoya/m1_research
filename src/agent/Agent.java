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
    private double useProb = Const.INITIAL_MEDIA_USER_RATE;
    private double followRate;
    private boolean traitor = false;
    private int timeStep;
    private boolean[] followList = new boolean[NUM_OF_AGENTS];
    private boolean[] unfollowList = new boolean[NUM_OF_AGENTS];
    private Set<Integer> alreadyAddedPostIds = new HashSet<>();
    private int followerNum;
    private boolean used;
    private int recievedLikeCount;

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
        this.timeStep = 0;
        this.recievedLikeCount = 0;
        setNumOfPosts(10); // 10件はないと0.1をかけても残らない

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

    public double getuseProb() {
        return this.useProb;
    }

    public double getFollowRate() {
        return this.followRate;
    }

    public int getFollwerNum() {
        return this.followerNum;
    }

    public PostCash getPostCash() {
        return this.postCash;
    }

    public boolean[] getFollowList() {
        return this.followList;
    }

    public boolean[] getUnfollowList() {
        return this.unfollowList;
    }

    public boolean getTraitor() {
        return this.traitor;
    }

    // setter methods

    public void setOpinion(double value) {
        this.opinion = value;
        setOpinionClass();
    }

    public void setPostProb(double value) {
        this.postProb = value;
    }

    public void setuseProb(double value) {
        this.useProb = value;
    }

    public void setBoundedConfidence(double value) {
        this.bc = value;
        if (this.bc > Const.BOUNDED_CONFIDENCE) {
            // this.bc = Const.BOUNDED_CONFIDENCE;
        }
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

    public void setFollowList(double[][] W) {
        for (int i = 0; i < W.length; i++) {
            if (W[this.id][i] > 0.0) {
                this.followList[i] = true;
            }
        }
    }

    public void setFollowerNum(double[][] W) {
        this.followerNum = 0;
        for (int i = 0; i < NUM_OF_AGENTS; i++) {
            if (W[i][this.id] > 0.0) {
                this.followerNum++;
            }
        }
    }

    public void setTraitor() {
        this.traitor = true;
    }

    public void addToPostCash(Post post) {
        if (!this.alreadyAddedPostIds.contains(post.getPostId()) && post.getPostUserId() != this.id
                && !this.unfollowList[post.getPostUserId()]) {
            this.postCash.addPost(post);
        }
    }

    public void setUsed(){
        this.used = true;
    }

    public void resetUsed(){
        this.used = false;
    }

    // other methods
    public void receiveLike() {
        this.recievedLikeCount++;
    }

    public void resetPostCash() {
        this.postCash.reset();
        this.toPost = 0;
    }

    public void addPostToFeed(Post post) {
        if (!this.alreadyAddedPostIds.contains(post.getPostId()) || !this.unfollowList[post.getPostUserId()]) {
            this.feed.add(post);
        }
    }

    public void resetFeed() {
        this.feed.clear();
    }

    public void updatePostProb(){
        // post prob is set based on the marginal utility theory
        double increment = Const.MU_PRAM * Math.log(this.recievedLikeCount * this.recievedLikeCount + 1);
        if(increment > 0.5){
            increment = 0.5;
        }
        this.postProb += increment;
        if (this.recievedLikeCount > 0 && (this.id == 93 || this.id == 32 || this.id == 8 )) {
             System.out.println("id : " + this.id + ", follower : " + this.followerNum + ", like " + this.recievedLikeCount);
        }
        if (this.postProb > 1.0) {
            this.postProb = 1.0;
        }
        if (this.useProb > 1.0) {
            this.useProb = 1.0;
        }
        this.recievedLikeCount = 0;
    }

    public void updateMyself() {
        double temp = 0.0;

        int postNum = 0;
        int comfortPostNum = 0;
        // feedに表示される投稿は全て閲覧する
        for (Post post : this.feed) {
            temp += post.getPostOpinion();
            if (this.id % 100 == 0) {
                //System.out.println("read post opinion " + post.getPostOpinion());
            }
            postNum++;
            if (Math.abs(post.getPostOpinion() - this.opinion) < Const.MINIMUM_BC) {
            // if (Math.abs(post.getPostOpinion() - this.opinion) < 0.2) {
                comfortPostNum++;
            }

            ///
            if (Math.abs(post.getPostOpinion() - this.opinion) > this.bc) {
                this.bc -= Const.DECREMENT_BC_BY_UNFOLLOW;
                }
            ///
            
        }

        if (postNum == 0)
            return;

        double comfortPostRate = (double) comfortPostNum / postNum;

        if(this.id == rand.nextInt(NUM_OF_AGENTS)){
            System.out.println("id: " + this.id + ", opinion: " + this.opinion +  ", compostnum : " + comfortPostNum + ", comfort : " + comfortPostRate);
        }

        if (comfortPostRate > Const.COMFORT_RATE && this.feed.size() > 2) {
            this.postProb += Const.INCREMENT_PP * decayFunc(this.timeStep);
            this.useProb += Const.INCREMENT_MUR * decayFunc(this.timeStep);
        } else {
            this.postProb -= Const.DECREMENT_PP * decayFunc(this.timeStep);
            this.useProb -= Const.DECREMENT_MUR * decayFunc(this.timeStep);
            // this.bc += Const.INCREMENT_BC * decayFunc(this.timeStep);
        }

        this.opinion = this.tolerance * this.intrinsicOpinion + (1 - this.tolerance) * (temp / postNum);

        // exp 3-3 : infulencerの買収
        ///
        
          /*if ((this.id == 34 || this.id == 21) && this.timeStep > 5000 ) { 
            this.opinion += 0.0001;
          } else {
            this.opinion = this.tolerance * this.intrinsicOpinion + (1 - this.tolerance) * (temp / postNum);
          }*/
         
        ///    

        // 実験 3-1 malicious bot
        ///
        /*if (this.traitor) {
            this.opinion -= 0.0001;
            this.useProb = 1.0;
            this.postProb = 1.0;
        } else {
            this.opinion = this.tolerance * this.intrinsicOpinion + (1 - this.tolerance) * (temp / postNum);
        }*/
        ///

        // 実験 3-1 : あるステップから１方向に意見が傾く奴らが出てくる
        /*
         * if(this.traitor && this.timeStep > 5000){
         * this.opinion += 0.1;
         * this.useProb = 1.0;
         * this.postProb = 1.0;
         * }
         */
        //

        // 実験 2-5 deplarization bot

        /*
         * if (this.traitor && this.timeStep > 0) {
         * if (this.opinion > 0.5) {
         * this.opinion -= 0.0001;
         * this.useProb = 1.0;
         * this.postProb = 1.0;
         * } else if (this.opinion < -0.5) {
         * this.opinion += 0.0001;
         * this.useProb = 1.0;
         * this.postProb = 1.0;
         * }
         * }
         */

        // exp 3-2 : distract
        /*
         * if(this.traitor && this.timeStep > 5000){
         * this.opinion = 1.0;
         * this.useProb = 1.0;
         * this.postProb = 1.0;
         * }
         */
        //

        // exp 2-2 : widen bc

        /*
         * if(rand.nextDouble() < 0.05 && this.bc < Const.BOUNDED_CONFIDENCE){
         * this.bc += 0.02;
         * }
         */

        //

        // 実験 2-3 インフルエンサーのBCを最大値にセット
        /*
         * if(this.id < Const.NUM_OF_SEED_USER && Math.abs(this.intrinsicOpinion) >
         * 0.8){
         * this.bc = Const.BOUNDED_CONFIDENCE;
         * }
         */

        // 実験 2-3-2 インフルエンサーに許容的になっていただく
        /*
         * int followerNum = 0;
         * for (int i = 0; i < NUM_OF_AGENTS; i++) {
         * if(this.followList[i]){
         * followerNum++;
         * }
         * }
         * if(followerNum > (int) 0.01 * Const.NUM_OF_SNS_USER){
         * this.bc += 0.01;
         * }
         */

        if (this.opinion < -1) {
            this.opinion = -1;
        } else if (this.opinion > 1) {
            this.opinion = 1;
        }
        if (this.postProb > 1.0) {
            this.postProb = 1.0;
        } else if (this.postProb < Const.MIN_PP) {
            this.postProb = Const.MIN_PP;
        }
        if (this.useProb > 1.0) {
            this.useProb = 1.0;
        } else if (this.useProb < Const.MIN_MUR) {
            this.useProb = Const.MIN_MUR;
        }
        if(this.bc < Const.MINIMUM_BC){
            this.bc = Const.MINIMUM_BC;
        }


        /// 
        // exp : 人気インフルエンサーがトピックに参加してくる
        /*if(this.id == 2){
            if(this.timeStep < 2000){
                this.postProb = 0.0;
            }else{
                this.opinion = 0.4;
            }
        }*/
        ///

        setOpinionClass();
    }

    public Post like() {
        List<Post> candidates = new ArrayList<>();
        if (this.feed.size() <= 0) {
            return null;
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
            return likedPost;
        } else {
            return null;
        }
    }

    public List<Post> repost(){
        List<Post> candidates = new ArrayList<>();
        List<Post> repostedPostList = new ArrayList<>();
        if(this.feed.isEmpty()){
            return Collections.emptyList();
        }

        // 条件に合う投稿をすべてリストアップ
        for (Post post : this.feed) {
            if (Math.abs(post.getPostOpinion() - this.opinion) < this.bc) {
                candidates.add(post);
            }
        }

        if (!candidates.isEmpty()) {
            for(Post post : candidates){
                if(rand.nextDouble() < Const.REPOST_PROB){
                    post.receiveLike();
                    repostedPostList.add(post);
                }
            }
        } else {
            return Collections.emptyList();
        }

        return repostedPostList;
    }

    public int follow() {
        List<Integer> candidates = new ArrayList<>();

        for (Post post : this.feed) {
            if (Math.abs(post.getPostOpinion() - this.opinion) < this.bc && !this.followList[post.getPostUserId()]
                    && !this.unfollowList[post.getPostUserId()]) {
                candidates.add(post.getPostUserId());
            }
        }

        if (!candidates.isEmpty()) {
            int followId = candidates.get(rand.nextInt(candidates.size()));
            this.followList[followId] = true;
            return followId;
        } else {
            return -1;
        }
    }

    // 閲覧した投稿の中でBC以上の意見の差があったらunfollowする
    public int unfollow() {
        int followeeNum = 0;
        for (int i = 0; i < NUM_OF_AGENTS; i++) {
            if (this.followList[i]) {
                followeeNum++;
            }
        }
        if (this.feed.size() <= 0.0 || followeeNum <= 2) {
            return -1;
        }

        List<Integer> dislikeUser = new ArrayList<>();
        Collections.shuffle(this.feed);
        for (Post post : this.feed) {
            if (Math.abs(post.getPostOpinion() - this.opinion) > this.bc && this.followList[post.getPostUserId()]) {
                this.unfollowList[post.getPostUserId()] = true;
                this.followList[post.getPostUserId()] = false;
                this.bc -= Const.DECREMENT_BC_BY_UNFOLLOW * decayFunc(this.timeStep);

                if (this.bc < Const.MINIMUM_BC) {
                    this.bc = Const.MINIMUM_BC;
                }
                return post.getPostUserId();
            }
            if (Math.abs(post.getPostOpinion() - this.opinion) > this.bc && !this.followList[post.getPostUserId()]) {
                dislikeUser.add(post.getPostUserId());
            }
        }
        if (dislikeUser.size() > 0) {
            // followしていないが、気にくわない投稿があれば1つを選んでその人をブロック
            this.unfollowList[dislikeUser.get(rand.nextInt(dislikeUser.size()))] = true;
            this.bc -= Const.DECREMENT_BC_BY_UNFOLLOW;
            if (this.bc < Const.MINIMUM_BC) {
                this.bc = Const.MINIMUM_BC;
            }
            // System.out.println("do not follow but dislike it");
        }
        return -1;
    }

    public Post makePost(int step) {
        // 極端な投稿を自重するようにmoderate
        Post post;
        if (this.opinion > 0.7 && rand.nextDouble() < 0.0) {
            post = new Post(this.id, this.opinion - 0.2, step);
        } else if (this.opinion < -0.7 && rand.nextDouble() < 0.0) {
            post = new Post(this.id, this.opinion + 0.2, step);
        } else {
            post = new Post(this.id, this.opinion, step);
        }
        this.toPost = 1;

        this.postProb -= Const.POST_COST;
        if(this.postProb < Const.MIN_PP){
            this.postProb = Const.MIN_PP;
        }

        return post;
    }

    public double decayFunc(double time) {
        double lambda = 0.0001;
        // return Math.exp(-lambda * time);
        return 1;
    }

}
