package analysis;

import admin.*;
import agent.Agent;
import network.*;

public class AssertionCheck {
    private Agent[] agentSet;
    private Network network;
    private int n;
    private int[] numOfError;
    private int maxStep;

    public AssertionCheck(Agent[] agentSet, Network network, int agentNum, int maxStep) {
        this.agentSet = agentSet;
        this.network = network;
        this.n = agentNum;
        this.numOfError = new int[maxStep + 1];
        this.maxStep = maxStep;
    }

    public void assertionChecker(Agent[] agentSet, AdminOptim admin, int agentNum, int step) {
        double[][] tempAdjMatrix = admin.getAdjacencyMatrix();
        // w行列の行方向の和は常に1.0
        for (int i = 0; i < n; i++) {
            double temp = 0;
            for(int j = 0; j < n; j++){
                if(i == j){
                    if(tempAdjMatrix[i][j] > 0){
                        System.out.println("AC Error: W(i, i) > 0 in node " + i);
                    }
                }
                temp += tempAdjMatrix[i][j];
                //System.out.println(temp);
            }
            if(Math.abs(temp - 1.0) > 0.1){
                System.out.println("AC Error: sum of the row is not equal to 1 in node " + i + " because sum is " + temp);
            }
        }

        // opinion should be in [-1, 1]
        for(int i = 0 ; i < n ; i++){
            if(agentSet[i].getOpinion() < -1 || agentSet[i].getOpinion() > 1){
                System.out.println("AC Error: opinion is out of the range in user " + i);
                numOfError[step] ++;
            }
        }
    }

    public void reportASError(){
        int sumError = 0;
        for(int i = 0; i < maxStep ; i++){
                sumError += numOfError[i];
        }
        System.out.println("the sum of error reported : " + sumError);
    }
}