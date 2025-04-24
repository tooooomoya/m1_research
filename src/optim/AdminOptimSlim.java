package optim;

import agent.Agent;
import constants.Const;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import network.*;

public class AdminOptimSlim {
    private double lambda;

    public AdminOptimSlim(double lambda) {
        this.lambda = lambda;
    }

    public double[][] AdminFeedback(Agent[] agentSet, Network network, int agentNum) {
        Random rand = new Random();
        double[][] W = network.getAdjacencyMatrix();
        int n = agentNum;

        for (int i = 0; i < n; i++) {
            // BCに基づいていいねする人を決める
            List<Integer> likeCandidates = new ArrayList<>();
            double bc = agentSet[i].getBc();
            double opinion = agentSet[i].getOpinion();
            for (int j = 0; j < n; j++) {
                if(W[i][j] > 0.5 || W[i][j] == 0.0){
                    continue;
                }
                if (Math.abs(opinion - agentSet[j].getOpinion()) < bc && i != j) {
                    likeCandidates.add(j);
                }
            }
            if (!likeCandidates.isEmpty()) {
                int likeId = likeCandidates.get(rand.nextInt(likeCandidates.size()));
                W[i][likeId] += Const.FEEDBACK_INCREASE_WEIGHT;

                double rowSum = 0.0;
                for (int j = 0; j < n; j++) {
                    //if (j != likeId) {
                        rowSum += W[i][j];
                    //}
                }
                if (rowSum > 0.0) {
                    for (int j = 0; j < n; j++) {
                        //if (j != likeId) {
                            W[i][j] /= rowSum;
                        //}
                    }
                }
            }
        }

        return W;
    }

}
