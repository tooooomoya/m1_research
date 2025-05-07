package admin;

import agent.*;
import constants.Const;
import java.util.*;

public class AdminOptim {
    private int n;
    private double[][] W; // Adminは隣接行列Wを操作する
    private int[] followerNumArray;

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
            //this.W[userId][likedId] = 1.01 * this.W[userId][likedId];
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
        /*List<Integer> followedUsers = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            if (this.W[userId][j] > 0.1) {
                followedUsers.add(j);
            }

            // フォロワー数に基づいて降順にソート
            followedUsers.sort((a, b) -> Integer.compare(followerNumArray[b], followerNumArray[a]));

            // 上位3人に対してWを増加
            int topK = Math.min(1, followedUsers.size());
            for (int i = 0; i < topK; i++) {
                int targetId = followedUsers.get(i);
                this.W[userId][targetId] = 1.001 * this.W[userId][targetId];
            }
        }*/

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
    public int[] AdminFeedback(int userId, Agent[] agentSet) {
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
}
