package dynamics;

import admin.*;
import agent.*;
import analysis.*;
import constants.Const;
import gephi.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
    private double connectionProbability = Const.CONNECTION_PROB_OF_RANDOM_NW;
    private AdminOptim admin;
    private static final Random rand = randomGenerater.rand;

    // constructor
    public OpinionDynamics() {
        setFromInitial();
        // setCustomized();
        this.analyzer = new Analysis();
        this.writer = new Writer(folerPath, resultList);
        this.gephi = new GraphVisualize(0.00, agentSet, network);
        this.admin = new AdminOptim(agentNum, network.getAdjacencyMatrix());
    }

    private void setFromInitial() {
        setNetwork();
        setAgents();
    }

    private void setNetwork() {
        ///// you can change the initial network bellow
        // this.network = new RandomNetwork(agentNum, connectionProbability);
        this.network = new ConnectingNearestNeighborNetwork(agentNum, 0.8);
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

        int followActionNum;
        int unfollowActionNum;
        List<Post> latestPostList = new ArrayList<>();
        int latestListSize = Const.LATEST_POST_LIST_LENGTH;

        // exp : set bot
        ///
        /*for (Agent agent : agentSet) {
            if (rand.nextDouble() < 0.1 && agent.getFollwerNum() < 10) {
                agent.setTraitor();
            }
        }*/
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
            analyzer.clearFeedList();
            writer.clearPostBins();
            writer.setSimulationStep(step);
            double[][] W = admin.getAdjacencyMatrix();
            List<Post> postList = new ArrayList<>();

            for (Agent agent : agentSet) {
                int agentId = agent.getId();
                agent.setFollowerNum(W);
                agent.setTimeStep(step);
                // このstepでSNSを利用するか決定する
                if (rand.nextDouble() > agent.getMediaUseRate()) {
                    continue;
                }

                /// depolarization 実験 2-2
                /// BCを大きくしてもらう
                /*
                 * if(rand.nextDouble() < 0.05){
                 * agent.setBoundedConfidence(agent.getBc() + 0.01);
                 * }
                 */

                // admin sets user's feed
                admin.AdminFeedback(agentId, agentSet, latestPostList);
                analyzer.setFeedList(agent.getFeed());

                int likedId = -1;

                for (int i = 0; i < 5; i++) {
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

            if (step % 1000 == 0) {
                // export gexf
                network.setAdjacencyMatrix(admin.getAdjacencyMatrix());
                gephi.updateGraph(agentSet, network);
                gephi.exportGraph(step, folerPath);
                writer.writeDegrees(W, folerPath);
            }
            // export metrics
            writer.setOpinionVar(analyzer.computeVarianceOpinion(agentSet));
            analyzer.computePostVariance();
            writer.setPostOpinionVar(analyzer.getPostOpinionVar());
            writer.setFollowUnfollowActionNum(followActionNum, unfollowActionNum);
            writer.setOpinionBins(agentSet);
            writer.setFeedVar(analyzer.computeFeedVariance());
            writer.setOpinionAvg(analyzer.computeMeanOpinion(agentSet));
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
