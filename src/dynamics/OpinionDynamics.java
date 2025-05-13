package dynamics;

import admin.*;
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
import network.*;
import rand.randomGenerater;
import writer.Writer;

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
        setNetwork();
        setAgents();
        this.analyzer = new Analysis();
        this.writer = new Writer(folerPath, resultList);
        this.gephi = new GraphVisualize(0.00, agentSet, network);
        this.admin = new AdminOptim(agentNum, network.getAdjacencyMatrix());
    }

    private void setAgents() {
        double[][] tempAdjacencyMatrix = this.network.getAdjacencyMatrix();
        for (int i = 0; i < agentNum; i++) {
            agentSet[i] = new Agent(i);
            agentSet[i].setFollowList(tempAdjacencyMatrix);
        }
    }

    private void setNetwork() {
        ///// you can change the initial network bellow
        //this.network = new RandomNetwork(agentNum, connectionProbability);
        this.network = new ConnectingNearestNeighborNetwork(agentNum, 0.5);
        /////

        this.network.makeNetwork(agentSet);
        System.out.println("finish making network");
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
        List<Post> latestPostList = new ArrayList<>();
        int latestListSize = Const.LATEST_POST_LIST_LENGTH;

        for (int step = 1; step <= t; step++) {
            System.out.println("step = " + step);
            followActionNum = 0;
            unfollowActionNum = 0;

            analyzer.clearPostCash();
            writer.clearPostBins();
            writer.setSimulationStep(step);
            double[][] W = admin.getAdjacencyMatrix();
            List<Post> postList = new ArrayList<>();

            for (Agent agent : agentSet) {
                int agentId = agent.getId();
                agent.setTimeStep(step);
                // このstepでSNSを利用するか決定する
                if (rand.nextDouble() > agent.getMediaUseRate()) {
                    continue;
                }

                admin.AdminFeedback(agentId, agentSet, latestPostList);
                if (agent.getId() % 100 == 0) {
                    // System.out.println("post cash length is " + agent.getPostCash().getSize());
                    // System.out.println("feed length is " + agent.getFeed().size());
                }

                
                int likedId = -1;

                for(int i = 0 ; i < 3; i++){
                    Post likedPost = agent.like();
                    if (likedPost != null) {
                        for (Agent otherAgent : agentSet) {
                            if (W[otherAgent.getId()][agentId] > 0.00) { // follower全員のpostCashに追加
                                otherAgent.addToPostCash(likedPost);
                            }
                        }
                        likedId = likedPost.getPostUserId();
                    }
                    if (likedId >= 0) {
                        agentSet[likedId].receiveLike();
                    }
                }
                // int likedId = -1;

                /////// follow
                int followedId = agent.follow();
                // int followedId = -1;

                /////// unfollow
                int unfollowedId = agent.unfollow();

                /////// post
                if (rand.nextDouble() < agent.getPostProb()) {
                    Post post = agent.makePost(step);
                    for (Agent otherAgent : agentSet) {
                        if (W[otherAgent.getId()][agentId] > 0.00) { // follower全員のpostCashに追加
                            otherAgent.addToPostCash(post);
                        }
                    }
                    writer.setPostBins(post);
                    analyzer.setPostCash(post);
                    postList.add(post);
                    if (latestPostList.size() > latestListSize - 1) {
                        latestPostList.remove(0);
                    }
                    latestPostList.add(post);
                }

                agent.updateMyself();
                admin.updateAdjacencyMatrix(agentId, likedId, followedId, unfollowedId);
                agent.resetPostCash();
                agent.resetFeed();
                ASChecker.assertionChecker(agentSet, admin, agentNum, step);
                if (followedId >= 0) {
                    followActionNum++;
                }
                if (unfollowedId >= 0) {
                    unfollowActionNum++;
                }
            }
            // adminがrecommend post を決める
            // admin.updateRecommendPostQueue(postList);

            if (step % 100 == 0) {
                // export gexf
                network.setAdjacencyMatrix(admin.getAdjacencyMatrix());
                gephi.updateGraph(agentSet, network);
                gephi.exportGraph(step, folerPath);
            }
            // export metrics
            analyzer.computeVariance(agentSet);
            writer.setOpinionVar(analyzer.getOpinionVar());
            analyzer.computePostVariance();
            writer.setPostOpinionVar(analyzer.getPostOpinionVar());
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
