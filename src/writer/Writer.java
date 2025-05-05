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
    private int[] opinionBins = new int[Const.NUM_OF_BINS_OF_OPINION];
    private double opinionBinWidth;

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
        for (int i = 0; i < opinionBins.length; i++) {
            opinionBins[i] = 0;
        }
        for (Agent agent : agentSet) {
            this.opinionBins[agent.getOpinionClass()] += 1;
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
                        sb.append(this.opinionVar);
                        break;
                    case "postOpinionVar":
                        sb.append(this.postOpinionVar);
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

}