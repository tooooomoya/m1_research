package analysis;

import agent.*;
import constants.Const;

import java.util.ArrayList;
import java.util.List;

public class Analysis {
    private List<Post> postCash;
    private int n;
    private double postOpinionVar;

    // constructor
    public Analysis() {
        this.n = Const.NUM_OF_USER;
        this.postCash = new ArrayList<>();
        this.postOpinionVar = -1;
    }

    public void clearPostCash() {
        postCash.clear();
    }

    public void setPostCash(Post post) {
        postCash.add(post.copyPost());
    }

    // Agent配列から分散を計算
    public double computeVarianceOpinion(Agent[] agentSet) {
        if (n == 0) {
            return -1;
        }

        // 平均を計算
        double sum = 0.0;
        for (Agent agent : agentSet) {
            sum += agent.getOpinion();
        }
        double mean = sum / n;

        // 分散を計算
        double squaredDiffSum = 0.0;
        for (Agent agent : agentSet) {
            double diff = agent.getOpinion() - mean;
            squaredDiffSum += diff * diff;
        }
        return squaredDiffSum / n;
    }

    // Agent配列から意見の平均を計算して返す
    public double computeMeanOpinion(Agent[] agentSet) {
        if (n == 0 || agentSet == null || agentSet.length == 0) {
            return -1; // 意味のある平均がない場合は -1 を返す（必要に応じて変更）
        }

        double sum = 0.0;
        for (Agent agent : agentSet) {
            sum += agent.getOpinion();
        }
        return sum / n;
    }

    public double computeFeedVariance(Agent[] agentSet) {
        double temp = 0.0;
        int postNum = 0;
    
        // 平均を求めるために合計を計算
        for (Agent agent : agentSet) {
            for (Post post : agent.getFeed()) {
                temp += post.getPostOpinion();
                postNum++;
            }
        }
    
        if (postNum == 0) {
            return -1; // 投稿が一つもない場合、意味のある分散が定義できない
        }
    
        double avg = temp / postNum;
    
        // 分散を計算
        double squaredDiffSum = 0.0;
        for (Agent agent : agentSet) {
            for (Post post : agent.getFeed()) {
                double diff = post.getPostOpinion() - avg;
                squaredDiffSum += diff * diff;
            }
        }
    
        return squaredDiffSum / postNum;
    }
    

    public void computePostVariance() {
        int size = postCash.size();
        if (size == 0) {
            this.postOpinionVar = -1;
            return;
        }

        // 平均を計算
        double sum = 0.0;
        for (Post post : postCash) {
            sum += post.getPostOpinion();
        }
        double mean = sum / size;

        // 分散を計算
        double squaredDiffSum = 0.0;
        for (Post post : postCash) {
            double diff = post.getPostOpinion() - mean;
            squaredDiffSum += diff * diff;
        }
        this.postOpinionVar = squaredDiffSum / size;
    }

    public double getPostOpinionVar() {
        return postOpinionVar;
    }
}