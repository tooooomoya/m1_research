package analysis;

import agent.*;
import constants.Const;

import java.util.*;

public class Analysis {
    private List<Post> postCash;
    private int n;
    private double postOpinionVar;
    private List<List<Post>> feedList;
    private double[] feedMeanArray;
    private double[] feedVarArray;
    private Map<Integer, List<Post>> feedMap = new HashMap<>();


    // constructor
    public Analysis() {
        this.n = Const.NUM_OF_USER;
        this.postCash = new ArrayList<>();
        this.postOpinionVar = -1;
        this.feedList = new ArrayList<>();
        this.feedMeanArray = new double[Const.NUM_OF_BINS_OF_OPINION];
        this.feedVarArray = new double[Const.NUM_OF_BINS_OF_OPINION];
    }

    public double[] getFeedMeanArray(){
        return this.feedMeanArray;
    }

    public double[] getFeedVarArray(){
        return this.feedVarArray;
    }

    public void clearPostCash() {
        postCash.clear();
    }

    public void clearFeedList() {
        this.feedList.clear();
    }

    public void setPostCash(Post post) {
        postCash.add(post.copyPost());
    }

    public void setFeedList(List<Post> feed) {
        this.feedList.add(new ArrayList<>(feed));
    }

    public void setFeedMap(Agent agent){
        this.feedMap.put(agent.getId(), new ArrayList<>(agent.getFeed()));
    }

    public void resetFeedMap() {
        feedMap.clear();
    }
    

    // Agent配列から分散を計算
    public double computeVarianceOpinion(Agent[] agentSet) {
        int num = 0;
        if (n == 0) {
            return -1;
        }

        // 平均を計算
        double sum = 0.0;
        for (Agent agent : agentSet) {
            if (!agent.getTraitor()) {
                sum += agent.getOpinion();
                num++;
            }
        }
        double mean = sum / num;

        // 分散を計算
        double squaredDiffSum = 0.0;
        for (Agent agent : agentSet) {
            if (!agent.getTraitor()) {
                double diff = agent.getOpinion() - mean;
                squaredDiffSum += diff * diff;
            }
        }
        return squaredDiffSum / num;
    }

    // Agent配列から意見の平均を計算して返す
    public double computeMeanOpinion(Agent[] agentSet) {
        int num = 0;
        if (n == 0 || agentSet == null || agentSet.length == 0) {
            return -1; // 意味のある平均がない場合は -1 を返す（必要に応じて変更）
        }

        double sum = 0.0;
        for (Agent agent : agentSet) {
            if (!agent.getTraitor()) {
                sum += agent.getOpinion();
                num++;
            }
        }
        return sum / num;
    }

    public double computeFeedVariance() {
        double temp = 0.0;
        int postNum = 0;

        // 平均を求めるために合計を計算
        for (List<Post> feed : this.feedList) {
            for (Post post : feed) {
                temp += post.getPostOpinion();
                postNum++;
            }
        }

        if (postNum == 0) {
            System.out.println("no post was read by users in this step.");
            return -1; // 投稿が一つもない場合、意味のある分散が定義できない
        }

        double avg = temp / postNum;

        // 分散を計算
        double squaredDiffSum = 0.0;
        for (List<Post> feed : this.feedList) {
            for (Post post : feed) {
                double diff = post.getPostOpinion() - avg;
                squaredDiffSum += diff * diff;
            }
        }

        return squaredDiffSum / postNum;
    }

    public void computeFeedMetrics(Agent[] agentSet) {
        Arrays.fill(this.feedMeanArray, 0.0);
        Arrays.fill(this.feedVarArray, 0.0);

        double[] classVarianceSum = new double[Const.NUM_OF_BINS_OF_OPINION];
        int[] agentCount = new int[Const.NUM_OF_BINS_OF_OPINION];
        
        for (Map.Entry<Integer, List<Post>> entry : feedMap.entrySet()) {
            Integer userId = entry.getKey();
            List<Post> feed = entry.getValue();
            Agent agent = agentSet[userId];
            int classId = agent.getOpinionClass();
        
            if (feed.isEmpty()) continue;
            
        
            // 個人平均
            double sum = 0.0;
            for (Post post : feed) {
                sum += post.getPostOpinion();
            }
            double mean = sum / feed.size();
            this.feedMeanArray[classId] += mean;
        
            // 個人分散
            double var = 0.0;
            for (Post post : feed) {
                double diff = post.getPostOpinion() - mean;
                var += diff * diff;
            }
            var /= feed.size(); // or (feed.size() - 1) for unbiased
        
            // クラスごとに集計
            classVarianceSum[classId] += var;
            agentCount[classId]++;
        }
        
        // 最終的な平均分散
        for (int i = 0; i < Const.NUM_OF_BINS_OF_OPINION; i++) {
            if (agentCount[i] != 0) {
                this.feedVarArray[i] = classVarianceSum[i] / agentCount[i];
                this.feedMeanArray[i] = this.feedMeanArray[i] / agentCount[i];
            }
        }
        
    }

    public void computePostVariance() {
        int size = postCash.size();
        if (size == 0) {
            this.postOpinionVar = -1;
            return;
        }

        // 平均を計算
        double sum = 0.0;
        for (Post post : postCash) {
            sum += post.getPostOpinion();
        }
        double mean = sum / size;

        // 分散を計算
        double squaredDiffSum = 0.0;
        for (Post post : postCash) {
            double diff = post.getPostOpinion() - mean;
            squaredDiffSum += diff * diff;
        }
        this.postOpinionVar = squaredDiffSum / size;
    }

    public double getPostOpinionVar() {
        return postOpinionVar;
    }

    public double[] computeClusteringCoefficients(double[][] adj) {
        double[] clustering = new double[n];

        for (int i = 0; i < n; i++) {
            // 隣接ノード（in + out）を集める（自身は含めない）
            Set<Integer> neighbors = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if (adj[i][j] > 0.0) neighbors.add(j); // out-neighbor
                if (adj[j][i] > 0.0) neighbors.add(j); // in-neighbor
            }
            neighbors.remove(i); // 自分自身を除く

            int k_total = neighbors.size();
            if (k_total < 2) {
                clustering[i] = 0.0;
                continue;
            }

            // 隣接ノード間の辺を数える（三角形を構成する候補）
            int linkCount = 0;
            for (int u : neighbors) {
                for (int v : neighbors) {
                    if (u != v && adj[u][v] > 0.0) {
                        linkCount++;
                    }
                }
            }

            // 有向グラフでは最大で k_total * (k_total - 1) 通りのリンクが存在可能
            clustering[i] = (double) linkCount / (k_total * (k_total - 1));
        }

        return clustering;
    }
}