package network;

import agent.Agent;
import java.util.Random;

public class RandomNetwork extends Network {
    private double connectionProbability; // エッジを張る確率

    // Constructor
    public RandomNetwork(int size, double connectionProbability) {
        super(size);
        this.connectionProbability = connectionProbability;
    }

    @Override
    public void makeNetwork(Agent[] agentSet) {
        Random rand = new Random(0);
        int size = getSize();
        int[][] tempMatrix = new int[size][size];

        // ユーザ i がユーザ j をフォローするかを確率で決定
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j && rand.nextDouble() < connectionProbability) {
                    int posts = rand.nextInt(5) + 1; // 閲覧する投稿数
                    setEdge(i, j, posts);
                    tempMatrix[i][j] += posts;
                }
            }
        }

        // 正規化
        for (int i = 0; i < size; i++) {
            int rowSum = 0;
            for (int j = 0; j < size; j++) {
                rowSum += tempMatrix[i][j];
            }
            //agentSet[i].setNumOfPosts(rowSum);
            if (rowSum > 0) {
                for (int j = 0; j < size; j++) {
                    if (tempMatrix[i][j] > 0) {
                        double value = (double) tempMatrix[i][j] / rowSum;
                        setEdge(i, j, value);
                    }
                }
            }
        }

    }
}
