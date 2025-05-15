package admin;

import agent.*;
import constants.Const;
import java.util.*;
import rand.randomGenerater;

public class AdminOptim {
    private int n;
    private double[][] W; // Adminは隣接行列Wを操作する
    private int[] followerNumArray;
    private List<Post> recommendPostQueue = new ArrayList<>();
    private int maxRecommPostQueueLength = Const.MAX_RECOMMENDATION_POST_LENGTH;

    public AdminOptim(int userNum, double[][] W) {
        this.n = userNum;
        this.W = W;
        this.followerNumArray = new int[n];
    }

    public double[][] getAdjacencyMatrix() {
        return this.W.clone();
    }

    public int[] getFollowerList() {
        return this.followerNumArray.clone();
    }

    public void setW(double[][] W) {
        this.W = W.clone();
        setFollowerNumArray();
    }

    public void addRecommendPost(Post post) {
        if (recommendPostQueue.size() >= this.maxRecommPostQueueLength) {
            recommendPostQueue.remove(0); // 先頭（古い投稿）を削除
        }
        recommendPostQueue.add(post); // 新しい投稿を追加
    }

    public void setFollowerNumArray() {
        Arrays.fill(this.followerNumArray, 0);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (this.W[i][j] > 0.0) {
                    this.followerNumArray[j]++;
                }
            }
        }
    }

    /*public void updateRecommendPostQueue(List<Post> postList) {
        int maxLike = -1;
        List<Post> candidates = new ArrayList<>();

        // 最大の「いいね」数を持つ投稿を集める
        for (Post post : postList) {
            int likes = post.getReceivedLike();
            if (likes > maxLike) {
                maxLike = likes;
                candidates.clear();
                candidates.add(post);
            } else if (likes == maxLike) {
                candidates.add(post);
            }
        }

        // 複数候補がある場合はランダムに1つ選ぶ
        if (!candidates.isEmpty()) {
            Random rand = new Random();
            Post chosen = candidates.get(rand.nextInt(candidates.size()));
            addRecommendPost(chosen);
        }
    }*/

    public void updateAdjacencyMatrix(int userId, int likedId, int followedId, int unfollowedId) {
        if (likedId >= 0) {
            // フォローしていないユーザに対するいいねは無視
            /*
             * if(this.W[userId][likedId] > 0.0){
             * this.W[userId][likedId] += Const.LIKE_INCREASE_WEIGHT;
             * }
             */
            // this.W[userId][likedId] = 1.01 * this.W[userId][likedId];
            // System.out.println("increased weight : " + this.W[userId][likedId]);
        }
        if (followedId >= 0) {
            // this.W[userId][followedId] = Const.FOLLOW_INCREASE_WEIGHT;
            this.W[userId][followedId] = 1.0;
        }
        if (unfollowedId >= 0) {
            this.W[userId][unfollowedId] = 0.0;
        }

        /*
         * double rowSum = 0.0;
         * for (int j = 0; j < n; j++) {
         * if (this.W[userId][j] > 0.5) {
         * this.W[userId][j] = 0.5;
         * }else if(this.W[userId][j] < 0.0){
         * this.W[userId][j] = 0.0;
         * }
         * rowSum += this.W[userId][j];
         * }
         * if (rowSum > 0.0) {
         * for (int j = 0; j < n; j++) {
         * this.W[userId][j] /= rowSum;
         * }
         * }
         * if(userId == 10){
         * for(int i = 0; i < n ; i++){
         * System.out.println(this.W[userId][i]);
         * }
         * }
         */
        setFollowerNumArray();
    }

    public void AdminFeedback(int userId, Agent[] agentSet, List<Post> latestPostList) {
        // iにとってfeed[j]はjの投稿を閲覧できる上限
        // これをW行列から算出する
        int postNum = agentSet[userId].getNumOfPosts(); // ユーザが一度の閲覧で消費する投稿数の上限
        int friendPostNum = (int) Math.round(postNum * (1 - Const.FEED_PREFERENTIALITY_RATE));
        int recommendPostNum = postNum - friendPostNum;
        if (recommendPostQueue.size() < recommendPostNum) {
            friendPostNum = friendPostNum + recommendPostNum;
            recommendPostNum = 0;
        }

        int addedPost = 0;
        List<Post> tempFeed = new ArrayList<>();
        if (agentSet[userId].getId() % 100 == 0) {
            // System.out.println("post cash size : " +
            // agentSet[userId].getPostCash().getSize());
        }
        for (Post post : agentSet[userId].getPostCash().getAllPosts()) {
            // if(this.W[userId][post.getPostUserId()] > 0.0){
            tempFeed.add(post);
            addedPost++;
            // }
        }
        Collections.shuffle(tempFeed, randomGenerater.rand);
        for(Post post : tempFeed){
            agentSet[userId].addPostToFeed(post);
        }
        if(userId % 100 == 0){
            //System.out.println("post cash rate in feed " + (double) addedPost / postNum);
        }

        List<Post> shuffled = new ArrayList<>(latestPostList);
        Collections.shuffle(shuffled, randomGenerater.rand);

        // n件以下しかない場合にも対応
        int numToSelect = Math.min(1, shuffled.size());

        // 先頭からn件を取り出して返す
        for(Post selectedPost : shuffled.subList(0, numToSelect)){
           // agentSet[userId].addPostToFeed(selectedPost);
        }
        
        // いいね数が多い順にソート（降順）
        /*List<Post> sortedList = new ArrayList<>(latestPostList);
        sortedList.sort((p1, p2) -> Integer.compare(p2.getRecievedLike(), p1.getRecievedLike()));

        int recPostNum = 0;
        // 上位 postNum 件を追加（件数を超えないようにする）
        for (Post post : sortedList) {
            if (addedPost >= postNum || recPostNum > 1)
                break;
            agentSet[userId].addPostToFeed(post);
            addedPost++;
            recPostNum++;
        }*/

        // add recommendation posts to user's feeds
        // いいねが集まってるやつとか、フォロワーが多い人のとか
        /*
         * int temp = 0;
         * int recommended = 0;
         * if (recommendPostQueue.isEmpty()) {
         * return;
         * }
         * for (int i = recommendPostQueue.size() - 1; i >= 0 && temp <
         * recommendPostNum; i--) {
         * if(recommendPostQueue.get(i).getPostUserId() == userId){
         * continue;
         * }
         * agentSet[userId].addPostToFeed(recommendPostQueue.get(i));
         * recommended++;
         * temp++;
         * }
         * 
         * if(postNum - agentSet[userId].getFeed().size() > 10){
         * for (int i = recommendPostQueue.size() - recommended - 1; i >= 0 &&
         * agentSet[userId].getFeed().size() + 1 <= postNum; i--) {
         * if(recommendPostQueue.get(i).getPostUserId() == userId){
         * continue;
         * }
         * agentSet[userId].addPostToFeed(recommendPostQueue.get(i));
         * }
         * }
         * if(postNum - agentSet[userId].getFeed().size() > 10){
         * System.out.println("size underflow !!");
         * }
         */
    }
}
