package dynamics;

import admin.*;
import agent.*;
import analysis.*;
import constants.Const;
import gephi.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
    private RepostVisualize repostGephi;
    private double connectionProbability = Const.CONNECTION_PROB_OF_RANDOM_NW;
    private AdminOptim admin;
    private static final Random rand = randomGenerater.rand;
    private int[][] repostNetwork;

    // constructor
    public OpinionDynamics() {
        setFromInitial();
        // setCustomized();
        this.analyzer = new Analysis();
        this.writer = new Writer(folerPath, resultList);
        this.gephi = new GraphVisualize(0.00, agentSet, network);
        this.repostGephi = new RepostVisualize(agentSet);
        this.admin = new AdminOptim(agentNum, network.getAdjacencyMatrix());
        this.repostNetwork = new int[agentSet.length][agentSet.length];
    }

    private void setFromInitial() {
        setNetwork();
        setAgents();
    }

    private void setNetwork() {
        ///// you can change the initial network bellow
        //this.network = new RandomNetwork(agentNum, connectionProbability);
        this.network = new ConnectingNearestNeighborNetwork(agentNum, 0.6);
        /////

        this.network.makeNetwork(agentSet);
        System.out.println("finish making network");
    }

    private void setAgents() {
        double[][] tempAdjacencyMatrix = this.network.getAdjacencyMatrix();
        for (int i = 0; i < agentNum; i++) {
            agentSet[i] = new Agent(i);
            agentSet[i].setFollowList(tempAdjacencyMatrix);
            agentSet[i].setFollowerNum(tempAdjacencyMatrix);
            /*if(agentSet[i].getFollwerNum() > 10){
                double newO = 2.0 * rand.nextDouble() -1.0;
                agentSet[i].setIntrinsicOpinion(newO);
                agentSet[i].setOpinion(newO);
            }*/

            /*if(agentSet[i].getId() < 20){
                if(agentSet[i].getId() % 5 == 0){
                    agentSet[i].setIntrinsicOpinion(-0.8);
                    agentSet[i].setOpinion(-0.8);
                }else if(agentSet[i].getId() % 5 == 1){
                    agentSet[i].setIntrinsicOpinion(-0.4);
                    agentSet[i].setOpinion(-0.4);
                }else if(agentSet[i].getId() % 5 == 2){
                    agentSet[i].setIntrinsicOpinion(0.0);
                    agentSet[i].setOpinion(0.0);
                }else if(agentSet[i].getId() % 5 == 3){
                    agentSet[i].setIntrinsicOpinion(0.4);
                    agentSet[i].setOpinion(0.4);
                }else if(agentSet[i].getId() % 5 == 4){
                    agentSet[i].setIntrinsicOpinion(0.8);
                    agentSet[i].setOpinion(0.8);
                }
            }*/
        }
    }

    private void setCustomized() {
        this.network = new ReadNetwork(agentNum, Const.READ_NW_PATH);
        this.network.makeNetwork(agentSet);
        System.out.println("finish making network");

        double[][] tempAdjacencyMatrix = this.network.getAdjacencyMatrix();
        for (int i = 0; i < agentNum; i++) {
            agentSet[i] = new Agent(i);
            agentSet[i].setFollowList(tempAdjacencyMatrix);
        }
        GephiReader.readGraphNodes(agentSet, Const.READ_NW_PATH);
    }

    private void errorReport() {
        ASChecker.reportASError();
    }

    // main part of the experimental dynamics
    public void evolve() {
        this.ASChecker = new AssertionCheck(agentSet, network, agentNum, t);
        // export gexf
        gephi.updateGraph(agentSet, network);
        gephi.exportGraph(0, folerPath);

        // export metrics
        writer.setSimulationStep(0);
        writer.setOpinionVar(analyzer.computeVarianceOpinion(agentSet));
        writer.setOpinionBins(agentSet);
        writer.write();
        writer.writeDegrees(network.getAdjacencyMatrix(), folerPath);

        int followActionNum;
        int unfollowActionNum;
        List<Post> latestPostList = new ArrayList<>();
        int latestListSize = Const.LATEST_POST_LIST_LENGTH;

        // exp : set bot
        ///
        /*
         * for (Agent agent : agentSet) {
         * if (rand.nextDouble() < 0.2 && agent.getFollwerNum() < Const.NUM_OF_USER *
         * 0.1) {
         * agent.setTraitor();
         * }
         * }
         */
        ///

        // exp 3-2 : distract
        /*
         * for(Agent agent : agentSet){
         * if(rand.nextDouble() < 0.001){
         * agent.setTraitor();
         * }
         * }
         */
        //

        for (int step = 1; step <= t; step++) {
            System.out.println("step = " + step);
            followActionNum = 0;
            unfollowActionNum = 0;

            analyzer.clearPostCash();
            // analyzer.clearFeedList();
            analyzer.resetFeedMap();
            writer.clearPostBins();
            writer.setSimulationStep(step);
            double[][] W = admin.getAdjacencyMatrix();
            List<Post> postList = new ArrayList<>();

            for (Agent agent : agentSet) {
                int agentId = agent.getId();
                agent.setFollowerNum(W);
                agent.setTimeStep(step);
                agent.resetUsed();
                // このstepでSNSを利用するか決定する
                if (rand.nextDouble() > agent.getuseProb()) {
                    continue;
                }
                agent.setUsed();

                /// depolarization 実験 2-2
                /// BCを大きくしてもらう
                /*
                 * if(rand.nextDouble() < 0.05){
                 * agent.setBoundedConfidence(agent.getBc() + 0.01);
                 * }
                 */

                // admin sets user's feed
                admin.AdminFeedback(agentId, agentSet, latestPostList);
                analyzer.setFeedMap(agent);
                agent.updatePostProb();

                /*int likedId = -1;

                for (int i = 0; i < 5; i++) {
                    Post likedPost = agent.like();
                    if (likedPost != null) {
                        repostNetwork[agentId][likedPost.getPostUserId()]++;
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
                }*/

                List<Post> repostedPostList = agent.repost();
                for(Post repostedPost : repostedPostList){
                    repostNetwork[agentId][repostedPost.getPostUserId()]++;
                    for (Agent otherAgent : agentSet) {
                        if (W[otherAgent.getId()][agentId] > 0.00) { // follower全員のpostCashに追加
                            otherAgent.addToPostCash(repostedPost);
                        }
                    }
                    agentSet[repostedPost.getPostUserId()].receiveLike();
                }


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

                        // exp 3-2 : influencer likes their follower's post
                        /*
                         * if (Math.abs(post.getPostOpinion()) > 0.6 && rand.nextDouble() < 0.01) {
                         * List<Integer> candidates = new ArrayList<>();
                         * for (int i = 0; i < agentSet.length; i++) {
                         * if (agentSet[i].getFollwerNum() > (int) Const.NUM_OF_SNS_USER * 0.05) {
                         * candidates.add(i);
                         * }
                         * }
                         * if (!candidates.isEmpty()) {
                         * int influencerId = candidates.get(rand.nextInt(candidates.size()));
                         * post.receiveLike();
                         * for (Agent follower : agentSet) {
                         * if (W[follower.getId()][influencerId] > 0.00) { // follower全員のpostCashに追加
                         * follower.addToPostCash(post);
                         * }
                         * }
                         * }
                         * }
                         */

                        /// exp 2-4 : add posts randomly irrespective of follow NW
                        /*
                         * if(W[otherAgent.getId()][agentId] == 0.0 && rand.nextDouble() < 0.001){
                         * otherAgent.addToPostCash(post);
                         * }
                         */
                        ///

                        /*
                         * if(rand.nextDouble() < 0.0001){
                         * int iter = 0;
                         * while (true) {
                         * int userId = rand.nextInt(agentSet.length);
                         * if(W[agent.getId()][userId] > 0.0){
                         * agentSet[userId].addPostToFeed(post);
                         * break;
                         * }
                         * iter++;
                         * if(iter > Const.NUM_OF_USER * 0.1){
                         * break;
                         * }
                         * }
                         * }
                         */

                        /*
                         * for(int i = 0; i < agentSet.length; i++){
                         * if(W[agent.getId()][i] > 0.0 && rand.nextDouble() < 0.01){
                         * int iter = 0;
                         * while(true){
                         * int userId = rand.nextInt(agentSet.length);
                         * if(W[userId][i] > 0.0){
                         * agentSet[userId].addPostToFeed(post);
                         * }
                         * if(iter > 10){
                         * break;
                         * }
                         * iter++;
                         * }
                         * }
                         * }
                         */

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
                admin.updateAdjacencyMatrix(agentId, followedId, unfollowedId);
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

            if (step % 1000 == 0) {
                // export gexf
                network.setAdjacencyMatrix(admin.getAdjacencyMatrix());
                gephi.updateGraph(agentSet, network);
                gephi.exportGraph(step, folerPath);
                repostGephi.updateGraph(agentSet, repostNetwork, step);
                repostGephi.exportGraph(step, folerPath);
                for (int[] repostNetwork1 : repostNetwork) {
                    Arrays.fill(repostNetwork1, 0);
                }
                writer.writeDegrees(W, folerPath);
            }
            // export metrics
            writer.setOpinionVar(analyzer.computeVarianceOpinion(agentSet));
            analyzer.computePostVariance();
            writer.setPostOpinionVar(analyzer.getPostOpinionVar());
            writer.setFollowUnfollowActionNum(followActionNum, unfollowActionNum);
            writer.setOpinionBins(agentSet);
            // writer.setFeedVar(analyzer.computeFeedVariance());
            writer.setOpinionAvg(analyzer.computeMeanOpinion(agentSet));
            analyzer.computeFeedMetrics(agentSet);
            writer.setFeedMeanArray(analyzer.getFeedMeanArray());
            writer.setFeedVarArray(analyzer.getFeedVarArray());
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
