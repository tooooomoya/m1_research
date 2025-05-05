package analysis;

import agent.*;
import constants.Const;

import java.util.ArrayList;
import java.util.List;

public class Analysis {
    private double opinionVar;
    private List<Post> postCash;
    private int n;
    private double postOpinionVar;

    // constructor
    public Analysis() {
        this.n = Const.NUM_OF_USER;
        this.opinionVar = -1;
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
    public void computeVariance(Agent[] agentSet) {
        if (n == 0) {
            this.opinionVar = -1;
            return;
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
        this.opinionVar = squaredDiffSum / n;
    }

    public double getOpinionVar() {
        return opinionVar;
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
