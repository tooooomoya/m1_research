package optim;

import agent.Agent;
import java.util.ArrayList;
import java.util.Random;
import network.*;

public class AdminOptimSlim {
    private double lambda;

    public AdminOptimSlim(double lambda) {
        this.lambda = lambda;
    }

    public double[][] AdminFeedback(Agent[] agentSet, Network network, int agentNum) {
        Random rand = new Random(1);
        double[][] W0 = network.getAdjacencyMatrix();
        int n = agentNum;

        double[] opinionArray = new double[n];
        for (int i = 0; i < n; i++) {
            opinionArray[i] = agentSet[i].getOpinion();
        }

        double[][] W = new double[n][n];
        int[] numFollowee = new int[n];
        for (int i = 0; i < n; i++) {
            W[i] = W0[i].clone();
            for (int j = 0; j < n; j++) {
                if (W[i][j] > 0) {
                    numFollowee[i]++;
                }
            }
        }

        for (int i = 0; i < n; i++) {
            // Admin cannot give feedback to those who follow only one other user as they
            // just watch the person's posts.
            if (numFollowee[i] < 2) {
                continue;
            }

            int closestAgentId = -1;
            double closestOpinionDiff = 1.0;
            for (int j = 0; j < n; j++) {
                // opinionArray に変更
                if (W0[i][j] > 0 && closestOpinionDiff > Math.abs(opinionArray[i] - opinionArray[j])) {
                    closestOpinionDiff = Math.abs(opinionArray[i] - opinionArray[j]);
                    closestAgentId = j;
                }
            }
            if (closestAgentId != -1) {
                // 最も意見が近いユーザに対する投稿閲覧数を増加させる
                W[i][closestAgentId]++;
                // 他に閲覧しているユーザがいれば、どれかからランダムに選んで減らす(閲覧できる投稿数は各自不変)
                ArrayList<Integer> candidates = new ArrayList<>();
                for (int j = 0; j < n; j++) {
                    if (j != closestAgentId && W[i][j] > 0) {
                        candidates.add(j);
                    }
                }

                if (!candidates.isEmpty()) {
                    int toDecrease = candidates.get(rand.nextInt(candidates.size()));
                    W[i][toDecrease]--;
                    if (W[i][toDecrease] == 0) {
                        // System.out.println("detected the event of W[i][j] being 0.");
                    }
                }
            }
        }

        return W;
    }

}
