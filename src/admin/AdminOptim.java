package admin;

import agent.*;
import constants.Const;
import java.util.*;

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
        return this.W;
    }

    public void setW(double[][] W) {
        this.W = W.clone();
        setFollowerNumArray(W);
    }

    public void addRecommendPost(Post post) {
        if (recommendPostQueue.size() >= this.maxRecommPostQueueLength) {
            recommendPostQueue.remove(0); // 先頭（古い投稿）を削除
        }
        recommendPostQueue.add(post); // 新しい投稿を追加
    }

    public void setFollowerNumArray(double[][] adjacencyMatrix) {
        Arrays.fill(this.followerNumArray, 0);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (adjacencyMatrix[i][j] > 0.0) {
                    this.followerNumArray[j]++;
                }
            }
        }
    }

    public void updateRecommendPostQueue(List<Post> postList) {
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
    }

    public void updateAdjacencyMatrix(int userId, int likedId, int followedId, int unfollowedId) {
        if (likedId > 0) {
            this.W[userId][likedId] += Const.LIKE_INCREASE_WEIGHT;
            // this.W[userId][likedId] = 1.01 * this.W[userId][likedId];
            // System.out.println("increased weight : " + this.W[userId][likedId]);
        }
        if (followedId > 0) {
            this.W[userId][followedId] = Const.FOLLOW_INCREASE_WEIGHT;
        }
        if (unfollowedId > 0) {
            this.W[userId][unfollowedId] = 0.0;
        }

        // preferenciality algorithm
        // follower数が多い人についてwの値を水増し
        /*
         * List<Integer> followedUsers = new ArrayList<>();
         * for (int j = 0; j < n; j++) {
         * if (this.W[userId][j] > 0.1) {
         * followedUsers.add(j);
         * }
         * 
         * // フォロワー数に基づいて降順にソート
         * followedUsers.sort((a, b) -> Integer.compare(followerNumArray[b],
         * followerNumArray[a]));
         * 
         * // 上位3人に対してWを増加
         * int topK = Math.min(1, followedUsers.size());
         * for (int i = 0; i < topK; i++) {
         * int targetId = followedUsers.get(i);
         * this.W[userId][targetId] = 1.001 * this.W[userId][targetId];
         * }
         * }
         */

        double rowSum = 0.0;
        for (int j = 0; j < n; j++) {
            if (this.W[userId][j] > 0.5) {
                this.W[userId][j] = 0.5;
            }
            rowSum += this.W[userId][j];
        }
        if (rowSum > 0.0) {
            for (int j = 0; j < n; j++) {
                this.W[userId][j] /= rowSum;
            }
        }
        setFollowerNumArray(W);
    }

    // あるユーザのfeed配列(閲覧投稿数の上限)を決定する関数
    public void AdminFeedback(int userId, Agent[] agentSet) {
        // iにとってfeed[j]はjの投稿を閲覧できる上限
        // これをW行列から算出する
        int postNum = agentSet[userId].getNumOfPosts(); // ユーザが一度の閲覧で消費する投稿数の上限
        int friendPostNum = (int) Math.round(postNum * Const.FEED_PREFERENTIALITY_RATE);
        int recommendPostNum = postNum - friendPostNum;
        if (recommendPostQueue.size() < recommendPostNum) {
            friendPostNum = friendPostNum + recommendPostNum;
            recommendPostNum = 0;
        }

        // add posts from user's postCash to user's feed depending on W matrix
        int[] maxPostNumArray = new int[this.n];
        for (int i = 0; i < n; i++) {
            maxPostNumArray[i] = (int) Math.round(this.W[userId][i] * friendPostNum);
        }
        for (Post post : agentSet[userId].getPostCash().getAllPosts()) {
            if (maxPostNumArray[post.getPostUserId()] > 0) {
                agentSet[userId].addPostToFeed(post);
                maxPostNumArray[post.getPostUserId()]--;
            }
        }

        // add recommendation posts to user's feeds
        // いいねが集まってるやつとか、フォロワーが多い人のとか
        int temp = 0;
        if (recommendPostQueue.size() == 0) {
            return;
        }
        for (int i = recommendPostQueue.size() - 1; i >= 0 && temp < recommendPostNum; i--) {
            if(recommendPostQueue.get(i).getPostUserId() == userId){
                continue;
            }
            agentSet[userId].addPostToFeed(recommendPostQueue.get(i));
            temp++;
        }

        if(postNum - agentSet[userId].getFeed().size() > 10){
            System.out.println("size underflow !!");
        }

    }
}
