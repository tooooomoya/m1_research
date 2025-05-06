package optim;

import agent.*;
import constants.Const;
import java.util.Arrays;

public class AdminOptim {
    private int n;
    private double[][] W; // Adminは隣接行列Wを操作する
    private int[] followerNumArray; // ユーザ i のフォロワー数(影響力とする)を記録

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

        setFollowerNumArray(this.W);
    }

    // あるユーザのfeed配列(閲覧投稿数の上限)を決定する関数
    public int[] AdminFeedbackPast(int userId, Agent[] agentSet) {
        // iにとってfeed[j]はjの投稿を閲覧できる上限
        // これをW行列から算出する
        int[] feed = new int[n];
        int maxPostNum = agentSet[userId].getNumOfPosts(); // ユーザが一度の閲覧で消費できる投稿数の上限
        int temp = 0;

        for (int i = 0; i < n; i++) {
            feed[i] = (int) Math.round(this.W[userId][i] * maxPostNum);
            temp += feed[i];
        }
        // System.out.println("max post num : " + temp);

        return feed;
    }

    public Post[] AdminFeedback(int userId, Agent[] agentSet) {
        int maxPostNum = Const.MAX_READABLE_POSTS_NUM; // このユーザーが閲覧できる最大投稿数
        PostCash postCash = agentSet[userId].getPostCash(); // このユーザーが使うPostCashオブジェクト
        Post[] allPosts = postCash.getAllPosts(); // 投稿の配列（仮にキュー的な構造になっている）

        // 投稿元ユーザーのfollower数に基づいて投稿を並び替えるためのComparator
        Arrays.sort(allPosts, (a, b) -> {
            int idA = a.getPostUserId();
            int idB = b.getPostUserId();
            double alpha = 10.0; // W の影響を強めたいなら α > 1
            //double scoreA = followerNumArray[idA] * Math.pow(this.W[userId][idA], alpha);
            //double scoreB = followerNumArray[idB] * Math.pow(this.W[userId][idB], alpha);
            double scoreA = Math.pow(this.W[userId][idA], alpha);
            double scoreB = Math.pow(this.W[userId][idB], alpha);
            return Double.compare(scoreB, scoreA);
        });

        // フィードとして投稿を最大maxPostNum個まで抽出
        int num = Math.min(maxPostNum, allPosts.length);
        Post[] feedQueue = new Post[num];
        for (int i = 0; i < num; i++) {
            feedQueue[i] = allPosts[i];
        }

        return feedQueue;
    }

}
