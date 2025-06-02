package writer;

import agent.*;
import constants.Const;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Writer {
    private int simulationStep;
    private String folderPath;
    private double opinionVar;
    private double postOpinionVar;
    private int followActionNum;
    private int unfollowActionNum;
    private String[] resultList;
    private int rewireActionNum;
    private int[] postBins = new int[Const.NUM_OF_BINS_OF_POSTS];
    private double postBinWidth;
    private int[] opinionBins = new int[Const.NUM_OF_BINS_OF_OPINION_FOR_WRITER];
    private double opinionBinWidth;
    private double opinionAvg;
    private double feedVar;

    public Writer(String folderPath, String[] resultList) {
        this.simulationStep = -1;
        this.folderPath = folderPath;
        this.opinionVar = -1;
        this.postOpinionVar = -1;
        this.followActionNum = 0;
        this.unfollowActionNum = 0;
        this.rewireActionNum = 0;
        this.resultList = resultList;
        this.postBinWidth = 2.0 / postBins.length;
        this.opinionBinWidth = 2.0 / opinionBins.length;
        this.opinionAvg = 0.0;
        this.feedVar = -1;
    }

    // Setter
    public void setSimulationStep(int step) {
        this.simulationStep = step;
        this.opinionVar = -1;
    }

    public void setOpinionVar(double var) {
        this.opinionVar = var;
    }

    public void setPostOpinionVar(double var){
        this.postOpinionVar = var;
    }

    public void setRewireActionNum(int value) {
        this.rewireActionNum = value;
    }

    public void setFollowUnfollowActionNum(int followActionNum, int unfollowActionNum) {
        this.followActionNum = followActionNum;
        this.unfollowActionNum = unfollowActionNum;
    }

    public void setOpinionAvg(double value){
        this.opinionAvg = value;
    }

    public void setFeedVar(double value){
        this.feedVar = value;
    }

    public void clearPostBins() {
        for (int i = 0; i < postBins.length; i++) {
            postBins[i] = 0;
        }
    }

    public void setPostBins(Post post) {
        double shiftedOpinion = post.getPostOpinion() + 1;
        int binIndex = (int) Math.min(shiftedOpinion / postBinWidth, postBins.length - 1);
        postBins[binIndex] += 1;
    }

    public void setOpinionBins(Agent[] agentSet) {
        double opinionBinWidth = 2.0 / Const.NUM_OF_BINS_OF_OPINION_FOR_WRITER;
        for (int i = 0; i < opinionBins.length; i++) {
            opinionBins[i] = 0;
        }
        for (Agent agent : agentSet) {
            double shiftedOpinion = agent.getOpinion() + 1; // [-1,1] → [0,2]
            int opinionClass = (int) Math.min(shiftedOpinion / opinionBinWidth, Const.NUM_OF_BINS_OF_OPINION_FOR_WRITER - 1);
            this.opinionBins[opinionClass] += 1;
        }
    }

    // 結果を書き出すメソッド

    public void write() {
        String filePath = folderPath + "/metrics/result_" + simulationStep + ".csv";

        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, false))) { // ← false で上書きモード
            // ヘッダーを書き込む
            StringBuilder header = new StringBuilder();
            header.append("step");
            for (String key : resultList) {
                header.append(",").append(key);
            }
            pw.println(header.toString());

            // 結果の1行を書く
            StringBuilder sb = new StringBuilder();
            sb.append(simulationStep); // ステップ番号

            for (String key : resultList) {
                sb.append(",");
                switch (key) {
                    case "opinionVar":
                        sb.append(String.format("%.4f", this.opinionVar));
                        break;
                    case "postOpinionVar":
                        sb.append(String.format("%.4f", this.postOpinionVar));
                        break;
                    case "follow":
                        sb.append(this.followActionNum);
                        break;
                    case "unfollow":
                        sb.append(this.unfollowActionNum);
                        break;
                    case "rewire":
                        sb.append(this.rewireActionNum);
                        break;
                    case "opinionAvg":
                        sb.append(String.format("%.4f", this.opinionAvg));
                        break;
                    case "feedVar":
                        sb.append(String.format("%.4f", this.feedVar));
                        break;
                    default:
                        sb.append(""); // 未定義の項目は空
                }
            }

            pw.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String postsFilePath = folderPath + "/posts/post_result_" + simulationStep + ".csv";

        try (PrintWriter pw = new PrintWriter(new FileWriter(postsFilePath, false))) { // 上書きモード
            // ヘッダーを書き込む
            StringBuilder header = new StringBuilder();
            header.append("step");

            for (int i = 0; i < postBins.length; i++) {
                header.append(",bin_").append(i);
            }
            header.append(",sumOfPosts");
            pw.println(header.toString());

            // データ行を書き込む
            StringBuilder sb = new StringBuilder();
            sb.append(simulationStep); // ステップ番号
            int sumOfPosts = 0;

            for (int i = 0; i < postBins.length; i++) {
                sb.append(",").append(postBins[i]);
                sumOfPosts += postBins[i];
            }
            sb.append(",").append(sumOfPosts);

            pw.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String opinionFilePath = folderPath + "/opinion/opinion_result_" + simulationStep + ".csv";

        try (PrintWriter pw = new PrintWriter(new FileWriter(opinionFilePath, false))) { // 上書きモード
            // ヘッダーを書き込む
            StringBuilder header = new StringBuilder();
            header.append("step");

            for (int i = 0; i < opinionBins.length; i++) {
                header.append(",bin_").append(i);
            }
            pw.println(header.toString());

            // データ行を書き込む
            StringBuilder sb = new StringBuilder();
            sb.append(simulationStep); // ステップ番号

            for (int i = 0; i < opinionBins.length; i++) {
                sb.append(",").append(opinionBins[i]);
            }

            pw.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeDegrees(double[][] adjacencyMatrix, String outputDirPath) {
        String filePath = outputDirPath + "/degrees/degree_result_" + simulationStep + ".csv";
    
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, false))) {
            // ヘッダー行
            pw.println("agentId,inDegree,outDegree");
    
            int numAgents = adjacencyMatrix.length;
    
            for (int i = 0; i < numAgents; i++) {
                int outDegree = 0;
                int inDegree = 0;
    
                // out-degree: 行を見る
                for (int j = 0; j < numAgents; j++) {
                    outDegree += (adjacencyMatrix[i][j] > 0) ? 1 : 0;
                }
    
                // in-degree: 列を見る
                for (int j = 0; j < numAgents; j++) {
                    inDegree += (adjacencyMatrix[j][i] > 0) ? 1 : 0;
                }
    
                pw.printf("%d,%d,%d%n", i, inDegree, outDegree);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}