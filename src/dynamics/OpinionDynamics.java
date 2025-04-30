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
import java.util.Random;
import java.util.Set;

import network.Network;
import network.RandomNetwork;
import optim.*;
import writer.Writer;
import rand.randomGenerater;

public class OpinionDynamics {
    private final int t = Const.MAX_SIMULATION_STEP;
    private final int agentNum = Const.NUM_OF_SNS_USER;
    private Network network;
    private final Agent[] agentSet = new Agent[agentNum];
    private Writer writer;
    private Analysis analyzer;
    private AssertionCheck ASChecker;
    private final String[] resultList = Const.RESULT_LIST;
    private final String folerPath = Const.RESULT_FOLDER_PATH;
    private GraphVisualize gephi;
    private double connectionProbability = Const.CONNECTION_PROB_OF_RANDOM_NW;
    private AdminOptim admin;
    private static final Random rand = randomGenerater.rand;

    // constructor
    public OpinionDynamics() {
        setAgents();
        setNetwork();
        this.analyzer = new Analysis();
        this.writer = new Writer(folerPath, resultList);
        this.gephi = new GraphVisualize(0.00, agentSet, network);
        this.admin = new AdminOptim(agentNum, network.getAdjacencyMatrix());
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

        int followActionNum;
        int unfollowActionNum;

        for (int step = 1; step <= t; step++) {
            System.out.println("step = " + step);
            followActionNum = 0;
            unfollowActionNum = 0;

            writer.clearPostBins();
            writer.setSimulationStep(step);
            double[][] W = admin.getAdjacencyMatrix();

            for (Agent agent : agentSet) {
                int agentId = agent.getId();
                // このstepでSNSを利用するか決定する
                if (rand.nextDouble() > agent.getMediaUseRate()) {
                    continue;
                }

                int likedId = agent.like();
                //int likedId = -1;

                // follow
                /*List<Integer> followList = new ArrayList<>();
                for (int j = 0; j < agentNum; j++) {
                    if (agentId != j && W[agentId][j] == 0.0) {
                        followList.add(j);
                    }
                }
                // 重複を削除 (フォローしているユーザがフォローしているユーザは被る可能性がある)
                followList = new ArrayList<>(new HashSet<>(followList));*/
                List<Integer> followList = new ArrayList<>();
                Set<Integer> candidates = new HashSet<>();

                // 自分のフォロー相手（1次近傍）を取得
                for (int j = 0; j < agentNum; j++) {
                    if (agentId != j && W[agentId][j] > 0.0) {
                        // フォロー中のエージェント j のフォロー相手を候補に追加（2次近傍）
                        for (int k = 0; k < agentNum; k++) {
                            if (k != agentId && W[j][k] > 0.0 && W[agentId][k] == 0.0) {
                                candidates.add(k);
                            }
                        }
                    }
                }

                followList = new ArrayList<>(candidates);

                int followedId = agent.follow(followList, agentSet);
                //int followedId = -1;

                // unfollow
                int unfollowedId = agent.unfollow();

                // post
                if (rand.nextDouble() < agent.getPostProb()) {
                    Post post = agent.makePost(step);
                    for (Agent otherAgent : agentSet) {
                        if (otherAgent.getId() != agentId && W[agentId][otherAgent.getId()] > 0.01) { // follower全員のpostCashに追加
                            otherAgent.addToPostCash(post);
                        }
                    }
                    writer.setPostBins(post);
                }

                agent.updateMyself();
                admin.updateAdjacencyMatrix(agentId, likedId, followedId, unfollowedId);
                agent.setFeed(admin.AdminFeedback(agentId, agentSet));
                agent.resetPostCash();
                ASChecker.assertionChecker(agentSet, network, agentNum, step);
                if (followedId > 0) {
                    followActionNum++;
                }
                if (unfollowedId > 0) {
                    unfollowActionNum++;
                }
            }

            if (step % 100 == 0) {
                // export gexf
                network.setAdjacencyMatrix(admin.getAdjacencyMatrix());
                gephi.updateGraph(agentSet, network);
                gephi.exportGraph(step, folerPath);
            }
            // export metrics
            analyzer.computeVariance(agentSet);
            writer.setOpinionVar(analyzer.getOpinionVar());
            writer.setFollowUnfollowActionNum(followActionNum, unfollowActionNum);
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
