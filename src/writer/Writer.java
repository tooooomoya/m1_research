package writer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Writer {
    private int simulationStep;
    private String folderPath;
    private double opinionVar;
    private int followActionNum;
    private int unfollowActionNum;
    private String[] resultList;
    private int rewireActionNum;

    public Writer(String folderPath, String[] resultList) {
        this.simulationStep = -1;
        this.folderPath = folderPath;
        this.opinionVar = -1;
        this.followActionNum = 0;
        this.unfollowActionNum = 0;
        this.rewireActionNum = 0;
        this.resultList = resultList;
    }

    // Setter
    public void setSimulationStep(int step) {
        this.simulationStep = step;
        this.opinionVar = -1;
    }

    public void setOpinionVar(double var) {
        this.opinionVar = var;
    }

    public void setRewireActionNum(int value){
        this.rewireActionNum = value;
    }

    public void setFollowUnfollowActionNum(int followActionNum, int unfollowActionNum){
        this.followActionNum = followActionNum;
        this.unfollowActionNum = unfollowActionNum;
    }

    // 結果を書き出すメソッド

    public void write() {
        String filePath = folderPath + "/metrics/result_" + simulationStep +".csv";

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
                        sb.append(opinionVar);
                        break;
                    case "follow":
                        sb.append(followActionNum);
                        break;
                    case "unfollow":
                        sb.append(unfollowActionNum);
                    case "rewire":
                        sb.append(rewireActionNum);
                    default:
                        sb.append(""); // 未定義の項目は空
                }
            }

            pw.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}