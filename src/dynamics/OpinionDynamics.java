package dynamics;

import agent.*;
import analysis.*;
import constants.Const;
import gephi.GraphVisualize;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.gurobi.gurobi.GRBException;

import network.Network;
import network.RandomNetwork;
import optim.AdminOptim;
import optim.AdminOptimSlim;
import writer.Writer;

public class OpinionDynamics {
    private final int t = Const.MAX_SIMULATION_STEP;
    private final int agentNum = Const.NUM_OF_SNS_USER;
    private final double lambda = Const.DYNAMIC_RATE_OF_ADMIN;
    private Network network;
    private final Agent[] agentSet = new Agent[agentNum];
    private Writer writer;
    private Analysis analyzer;
    private AssertionCheck ASChecker;
    private final String[] resultList = Const.RESULT_LIST;
    private final String folerPath = Const.RESULT_FOLDER_PATH;
    private GraphVisualize gephi;
    private double connectionProbability = Const.CONNECTION_PROB_OF_RANDOM_NW;
    private AdminOptim AdminOptimizer;
    private AdminOptimSlim AdminSlimOptimizer;

    // constructor
    public OpinionDynamics() {
        setAgents();
        setNetwork();
        this.analyzer = new Analysis();
        this.writer = new Writer(folerPath, resultList);
        this.gephi = new GraphVisualize(lambda, agentSet, network);
        this.AdminOptimizer = new AdminOptim(Const.DYNAMIC_RATE_OF_ADMIN, agentNum, false, false);
        this.AdminSlimOptimizer = new AdminOptimSlim(lambda);
    }

    private void setAgents() {
        for (int i = 0; i < agentNum; i++) {
            agentSet[i] = new Agent(i);
        }
    }

    private void setNetwork() {
        ///// you can change the initial network bellow
        this.network = new RandomNetwork(agentNum, connectionProbability);
        /////

        this.network.makeNetwork(agentSet);
    }

    private void errorReport() {
        ASChecker.reportASError();
    }

    // the main part of the experimental dynamics
    public void evolve() {

        /*
         * for (int i = 0; i < agentNum; i++) {
         * agentSet[i].updateScreen(network.getAdjacencyMatrix());
         * }
         */

        this.ASChecker = new AssertionCheck(agentSet, network, agentNum, t);
        // export gexf
        gephi.updateGraph(agentSet, network);
        gephi.exportGraph(0, folerPath);

        // export metrics
        analyzer.computeVariance(agentSet);
        writer.setSimulationStep(0);
        writer.setOpinionVar(analyzer.getOpinionVar());
        writer.setOpinionBins(agentSet);
        writer.write();

        for (int step = 1; step <= t; step++) {
            int followActionNum = 0;
            int unfollowActionNum = 0;
            int rewireActionNum = 0;
            System.out.println("step = " + step);

            writer.setSimulationStep(step);

            //// SNS Admin optimization

            /*
             * try {
             * network.setAdjacencyMatrix(AdminOptimizer.AdminFeedback(agentSet, network,
             * agentNum));
             * } catch (GRBException e) {
             * System.out.println("Gurobi optimization error: " + e.getMessage());
             * e.printStackTrace();
             * }
             */

            network.setAdjacencyMatrix(AdminSlimOptimizer.AdminFeedback(agentSet, network, agentNum));

            /*
             * for (int i = 0; i < agentNum; i++) {
             * agentSet[i].updateScreen(network.getAdjacencyMatrix());
             * }
             */

            ASChecker.assertionChecker(agentSet, network, agentNum, step);
            System.out.println("SNS Admin feedback finished. ");

            //// follow dynamics
            /*
             * double[][] tempAdjacencyMatrix = network.getAdjacencyMatrix();
             * 
             * for (int i = 0; i < agentNum; i++) {
             * List<Integer> followList = new ArrayList<>();
             * 
             * for (int j = 0; j < agentNum; j++) {
             * if (tempAdjacencyMatrix[i][j] > 0) { // iがjをフォローしている（1-hop）
             * for (int k = 0; k < agentNum; k++) {
             * if (tempAdjacencyMatrix[j][k] > 0 && k != i && tempAdjacencyMatrix[i][k] ==
             * 0) {
             * // jがkをフォローしていて、iはkをフォローしていない → iにとっての2-hop先
             * followList.add(k);
             * }
             * }
             * }
             * }
             * 
             * // 重複を削除 (フォローしているユーザがフォローしているユーザは被る可能性がある)
             * followList = new ArrayList<>(new HashSet<>(followList)); //
             * 
             * // follow action
             * int[] followResult = agentSet[i].follow(followList, agentSet);
             * if(followResult[0] != -1){
             * network.setEdge(i, followResult[0], 1);
             * network.reduceEdge(i, followResult[1], 1);
             * followActionNum++;
             * }
             * 
             * }
             * System.out.println("follow dynamics finished. ");
             * ASChecker.assertionChecker(agentSet, network, agentNum, step);
             * 
             * 
             * //// unfollow dynamics
             * for(int i = 0; i < agentNum; i++){
             * double[] unfollowResult = agentSet[i].unfollow(agentSet);
             * if(unfollowResult[0] != -1){
             * network.setEdge(i, (int) unfollowResult[0], 0);
             * network.increaseEdge(i, (int) unfollowResult[1], unfollowResult[2]);
             * unfollowActionNum++;
             * }
             * }
             * System.out.println("unfollow dynamics finished. ");
             * ASChecker.assertionChecker(agentSet, network, agentNum, step);
             */

            double[][] tempAdjacencyMatrix = network.getAdjacencyMatrix();

            //// social rewiring
            double rewireBc = 0.0;
            double rewireOpinionStrength = 0.0;
            for (int i = 0; i < agentNum; i++) {

                // follow List の作成
                List<Integer> followList = new ArrayList<>();

                /*
                 * for (int j = 0; j < agentNum; j++) {
                 * if (tempAdjacencyMatrix[i][j] > 0) { // iがjをフォローしている（1-hop）
                 * for (int k = 0; k < agentNum; k++) {
                 * if (tempAdjacencyMatrix[j][k] > 0 && k != i && tempAdjacencyMatrix[i][k] ==
                 * 0) {
                 * // jがkをフォローしていて、iはkをフォローしていない → iにとっての2-hop先
                 * followList.add(k);
                 * }
                 * }
                 * }
                 * }
                 */

                for (int j = 0; j < agentNum; j++) {
                    if (i != j && tempAdjacencyMatrix[i][j] == 0.0) {
                        followList.add(j);
                    }
                }

                // 重複を削除 (フォローしているユーザがフォローしているユーザは被る可能性がある)
                followList = new ArrayList<>(new HashSet<>(followList));

                // rewire action
                int[] result = agentSet[i].rewire(followList, agentSet);
                if (result[0] != -1 && result[1] != -1) {
                    network.setEdge(i, result[1], tempAdjacencyMatrix[i][result[1]]);
                    network.setEdge(i, result[0], 0);
                    // System.out.println(i + " unfollows " + result[0] + ", follows " + result[1]);
                    // agentSet[i].updateScreen(tempAdjacencyMatrix, agentSet); // just for
                    // consistence
                    rewireBc += agentSet[i].getBc();
                    rewireOpinionStrength += Math.abs(agentSet[i].getOpinion());
                    rewireActionNum++;
                }
            }
            ASChecker.assertionChecker(agentSet, network, agentNum, step);
            System.out.println("avg rewire bc : " + rewireBc / rewireActionNum + ", avg rewire opinion strength : "
                    + rewireOpinionStrength / rewireActionNum);
            System.out.println("social rewiring finished.");

            /// decide whether to post
            for (int i = 0; i < agentNum; i++) {
                Post post = agentSet[i].makePost(agentSet, step);
                if(post == null){
                    continue;
                }
                for(int j = 0 ; j < agentNum; j++){
                    if(tempAdjacencyMatrix[i][j] > 0.0){
                        agentSet[i].setPostCash(post);
                    }
                }
            }

            //// social influence
            // screen[i]はAdminによって決められるiに対する投稿閲覧数上限
            for (int i = 0; i < agentNum; i++) {
                agentSet[i].updateScreen(network.getAdjacencyMatrix(), agentSet);
            }

            Agent[] pastAgentSet = agentSet.clone();
            for (int i = 0; i < agentNum; i++) {
                agentSet[i].updateOpinion(pastAgentSet, network);
            }
            ASChecker.assertionChecker(agentSet, network, agentNum, step);
            System.out.println("social influence finished. ");

            if (step % 10 == 0) {

                // export gexf
                gephi.updateGraph(agentSet, network);
                gephi.exportGraph(step, folerPath);

            }
            // export metrics
            analyzer.computeVariance(agentSet);
            writer.setOpinionVar(analyzer.getOpinionVar());
            writer.setFollowUnfollowActionNum(followActionNum, unfollowActionNum);
            writer.setRewireActionNum(rewireActionNum);
            writer.setPostBins(agentSet);
            writer.setOpinionBins(agentSet);
            writer.write();
        }
    }

    public static void main(String[] args) {
        Instant start = Instant.now();

        OpinionDynamics simulator = new OpinionDynamics();
        simulator.evolve();

        Instant end = Instant.now();

        Duration timeElapsed = Duration.between(start, end);

        // print some major information about the simulation parameter

        simulator.errorReport();

        System.out.println("Start time:     " + start);
        System.out.println("End time:       " + end);
        System.out.println("Elapsed time:   " + timeElapsed.toMillis() + " ms");
    }
}
