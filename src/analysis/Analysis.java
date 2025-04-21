package analysis;

import agent.Agent;

public class Analysis {
    private double opinionVar;

    // constructor
    public Analysis() {
        this.opinionVar = -1;
    }

    // Agent配列から分散を計算
    public void computeVariance(Agent[] agentSet) {
        int n = agentSet.length;
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

    // 分散を取得
    public double getOpinionVar() {
        return opinionVar;
    }
}