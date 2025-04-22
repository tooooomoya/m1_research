package agent;

import constants.Const;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import network.Network;

public class Agent {
    private int id;
    private double opinion;
    private double tolerance;
    private double bc; // Bounded Confidence
    private int[] screen;
    private double intrinsicOpinion;
    private final int NUM_OF_AGENTS = Const.NUM_OF_SNS_USER;
    private static final Random rand = new Random();
    private final double followProbThres = Const.FOLLOW_RATE;
    private final double unfollowProbThres = Const.UNFOLLOW_RATE;
    private double postDrive; // 投稿意欲を表現するパラメータ [0,1]
    private int toPost; // ある時刻において何件の投稿をするか
    private int numOfPosts = rand.nextInt(100); // 毎時何件の投稿を閲覧できるか
    private final double rewireProbThres = Const.REWIRE_RATE;
    private int opinionClass;

    // constructor
    public Agent(int agentID) {
        this.id = agentID;
        // this.tolerance = rand.nextDouble(); // 0〜1 の乱数
        this.tolerance = 0.4;
        this.intrinsicOpinion = Math.max(-1.0, Math.min(1.0, rand.nextGaussian() * 0.5));
        this.opinion = this.intrinsicOpinion;
        this.screen = new int[NUM_OF_AGENTS]; // 全ユーザの中で、どのユーザの投稿を何件閲覧するかについての配列(隣接行列の行成分)
        this.bc = Const.BOUNDED_CONFIDENCE; // 動的にしてもよい。
        this.postDrive = 0.0;
        // this.numOfPosts = rand.nextInt(100);
        setOpinionClass();
    }

    // getter methods

    public double getOpinion() {
        return this.opinion;
    }

    public double getTolerance() {
        return this.tolerance;
    }

    public int[] getScreen() {
        return this.screen;
    }

    public int getNumOfPosts() {
        return this.numOfPosts;
    }

    public int getToPost() {
        return this.toPost;
    }

    public int getOpinionClass(){
        return this.opinionClass;
    }

    // setter methods

    public void setOpinion(double value) {
        this.opinion = value;
    }

    public void setTolerance(double value) {
        this.tolerance = value;
    }

    public void setIntrinsicOpinion(double value) {
        this.intrinsicOpinion = value;
    }

    public void setNumOfPosts(int value) {
        this.numOfPosts = value;
    }

    public void setToPost(int value) {
        this.toPost = value;
    }

    public void setOpinionClass(){
        double shiftedOpinion = this.opinion + 1; // [-1,1] → [0,2]
        double opinionBinWidth = 2.0 / Const.NUM_OF_BINS_OF_OPINION;
        this.opinionClass = (int) Math.min(shiftedOpinion / opinionBinWidth, Const.NUM_OF_BINS_OF_OPINION - 1);
    }

    // other methods

    public void updateScreen(double[][] adjacencyMatrix) {
        for (int i = 0; i < NUM_OF_AGENTS; i++) {
            this.screen[i] = (int) Math.round(this.numOfPosts * adjacencyMatrix[this.id][i]);
        }
    }

    public void updateOpinion(Agent[] allAgents, Network Network) {
        double temp = 0.0;
        double[] weight = Network.getAdjacencyMatrix()[this.id].clone();

        for (int i = 0; i < NUM_OF_AGENTS; i++) {
            if (allAgents[i].getToPost() > 0) { // その人が投稿した場合だけ、投稿を見て影響を受ける。そもそも投稿しないなら影響０
                temp += weight[i] * allAgents[i].getOpinion();
            }
        }

        // 意見の加重平均（スクリーンに誰もいない場合は intrinsicOpinion のみ）
        if (temp == 0.0) {
            this.opinion = this.tolerance * this.intrinsicOpinion + (1 - this.tolerance) * this.opinion;
            //System.out.println("user " + this.id + " has not read any posts"); // 誰もフォローしてないか、それとも誰も投稿してないか
        } else {
            this.opinion = this.tolerance * this.intrinsicOpinion + (1 - this.tolerance) * temp;
            // System.out.println("\nupdated opinion: " + this.opinion);
        }

        if (this.opinion < -1) {
            this.opinion = -1;
        } else if (this.opinion > 1) {
            this.opinion = 1;
        }

        setOpinionClass();

        // System.out.println("clipped updated opinion" + this.opinion);
    }

    public int[] follow(List<Integer> followList, Agent[] agents) { // 候補の中から誰をフォローするかを返すだけ
        int[] result = new int[2];

        // フォローする確率をチェック
        if (followProbThres <= rand.nextDouble()) {
            result[0] = -1;
            return result;
        }

        List<Integer> candidates = new ArrayList<>();
        double myOpinion = this.opinion;

        for (int id : followList) {
            double diff = Math.abs(myOpinion - agents[id].getOpinion());
            if (diff <= this.bc) {
                candidates.add(id);
            }
        }

        if (!candidates.isEmpty()) {
            int temp = 0;
            while (true) {
                result[0] = candidates.get(rand.nextInt(candidates.size()));
                if (this.screen[result[0]] == 0) {
                    // フォロー人数が増えたので、他の誰かをアンフォローして数を維持
                    List<Integer> followingNow = new ArrayList<>();
                    for (int i = 0; i < this.screen.length; i++) {
                        if (i != result[0] && this.screen[i] > 0) {
                            followingNow.add(i);
                        }
                    }

                    if (!followingNow.isEmpty()) {
                        int reducedfollowId = followingNow.get(rand.nextInt(followingNow.size()));
                        this.screen[reducedfollowId] -= 1;
                        result[1] = reducedfollowId; // 外したIDを記録
                    } else {
                        result[1] = -1; // 外す相手がいない場合
                    }

                    this.screen[result[0]] = 1; // 新しくフォロー

                    break;
                } else if (temp > 100) {
                    result[0] = -1;
                    result[1] = -1;
                    return result; // 全員フォロー済みっぽいので中断
                }
                temp++;
            }
        }

        return result;
    }

    public int[] unfollow(Agent[] agentSet) {
        int[] result = new int[3]; // result[0] = unfollowId, result[1] = increasedId, result[2] = increase
                                   // weight(= reduced weight)
        result[0] = -1;
        result[1] = -1;

        if (unfollowProbThres <= rand.nextDouble()) {
            return result;
        }

        List<Integer> unfollowCandidates = new ArrayList<>();
        for (int i = 0; i < NUM_OF_AGENTS; i++) {
            if (this.screen[i] > 0 && this.id != i) {
                double diff = Math.abs(this.opinion - agentSet[i].getOpinion());
                if (diff >= this.bc) {
                    unfollowCandidates.add(i);
                }
            }
        }

        if (!unfollowCandidates.isEmpty()) {
            // アンフォロー対象
            int unfollowId = unfollowCandidates.get(rand.nextInt(unfollowCandidates.size()));
            result[2] = this.screen[unfollowId]; // unfollowするユーザの投稿を毎時何件閲覧していたか
            this.screen[unfollowId] = 0;
            result[0] = unfollowId;

            // すでにフォローしている人の中から誰か1人選んで screen を +result[2]
            List<Integer> increaseCandidates = new ArrayList<>();
            for (int i = 0; i < NUM_OF_AGENTS; i++) {
                if (i != unfollowId && this.screen[i] > 0 && this.id != i) {
                    increaseCandidates.add(i);
                }
            }

            if (!increaseCandidates.isEmpty()) {
                int increaseId = increaseCandidates.get(rand.nextInt(increaseCandidates.size()));
                this.screen[increaseId] += result[2];
                result[1] = increaseId;
            } else {
                result[0] = -1;
                result[1] = -1;
            }
        }

        return result;
    }

    public int[] rewire(List<Integer> followList, Agent[] agentSet) {
        int[] result = new int[2]; // result[0]が新しくフォローする人、result[1]がフォローを外す人

        if (rewireProbThres <= rand.nextDouble()) {
            result[0] = -1;
            return result;
        }

        // アンフォロー候補：すでにフォローしている人
        List<Integer> unfollowCandidates = new ArrayList<>();
        // フォロー候補：まだフォローしていない人（自分自身以外）
        List<Integer> followCandidates = new ArrayList<>();

        for (int i = 0; i < screen.length; i++) {
            if (i == this.id)
                continue; // 自分自身は対象外

            if (screen[i] > 0) {
                double diff = Math.abs(this.opinion - agentSet[i].getOpinion());
                if (diff >= this.bc) {
                    unfollowCandidates.add(i);
                }
            } else if (screen[i] == 0) {
                for (int id : followList) {
                    double diff = Math.abs(this.opinion - agentSet[id].getOpinion());
                    if (diff <= this.bc) {
                        followCandidates.add(id);
                    }
                }
            }
        }

        // 両方の候補が存在している場合のみリワイヤリングを実行
        if (!unfollowCandidates.isEmpty() && !followCandidates.isEmpty()) {
            int unfollowId = unfollowCandidates.get(rand.nextInt(unfollowCandidates.size()));
            int followId = followCandidates.get(rand.nextInt(followCandidates.size()));

            // フォロー
            screen[followId] = screen[unfollowId];
            // アンフォロー
            screen[unfollowId] = 0;

            result[0] = followId;
            result[1] = unfollowId;
        } else {
            // どちらか一方の候補がいなければ、リワイヤリングできない
            result[0] = -1;
            result[1] = -1;
        }

        return result;
    }

    public int decideToPost(Agent[] agentSet) { // 自身が見ているscreenの状況によって、投稿するかどうかを決定する
        int n = screen.length;

        // たまに周囲の状況とは無関係に投稿する
        if (0.1 >= rand.nextDouble()) {
            this.toPost = 1;
            return this.toPost;
        }

        // 周りに同じ意見の人がどのくらいいるかで決定
        int numOfComfortPost = 0;
        for (int i = 0; i < n; i++) {
            if (this.screen[i] > 0 && Math.abs(agentSet[i].getOpinion() - this.opinion) < 0.2) {
                numOfComfortPost += this.screen[i];
            }
        }
        double comfortRate = (double) numOfComfortPost / this.numOfPosts;
        if (comfortRate > 0.2) {
            this.postDrive += comfortRate * 0.05;
        }
        if (this.postDrive > 0.5) {
            this.toPost = 1;
            this.postDrive = 0;
        } else {
            this.toPost = 0;
        }
        return this.toPost;
    }

}
