package network;

import agent.Agent;
import constants.Const;
import java.util.*;
import rand.randomGenerater;

public class ConnectingNearestNeighborNetwork extends Network {
    private double p; // potential edge を実エッジに変換する確率
    private Random rand = randomGenerater.rand;
    private Set<Edge> potentialEdges;

    private static class Edge {
        int from, to;

        Edge(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Edge))
                return false;
            Edge other = (Edge) o;
            return this.from == other.from && this.to == other.to;
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to);
        }
    }

    public ConnectingNearestNeighborNetwork(int size, double p) {
        super(size);
        this.p = p;
        this.potentialEdges = new HashSet<>();
    }

    @Override
    public void makeNetwork(Agent[] agentSet) {
        int seedNodes = Math.min(Const.NUM_OF_SEED_USER, getSize()); // 最初に作るノード数
        int currentSize = seedNodes;
        System.out.println("start making network");

        
          setEdge(0, 1, 1);
          setEdge(1, 0, 1);
          setEdge(0, 2, 1);
          setEdge(2, 1, 1);
         

        /*for (int i = 0; i < seedNodes; i++) {
            for (int j = 0; j < seedNodes; j++) {
                if (i != j && rand.nextDouble() < 0.1) {
                    setEdge(i, j, 1.0);
                }
            }
        }*/

        // seed nodes have to follow at least 1 other node
        for (int i = 0; i < seedNodes; i++) {
            boolean hasFollowee = false;
            for (int j = 0; j < seedNodes; j++) {
                if (this.adjacencyMatrix[i][j] > 0) {
                    hasFollowee = true;
                    break;
                }
            }
            if (!hasFollowee) {
                int randomTarget;
                do {
                    randomTarget = rand.nextInt(seedNodes);
                } while (randomTarget == i); // 自分自身は避ける

                setEdge(i, randomTarget, 1);
            }
        }

        // 初期ノード間をランダムに接続
        /*
         * for (int i = 0; i < seedNodes; i++) {
         * for (int j = 0; j < seedNodes; j++) {
         * if (i != j && rand.nextDouble() < Const.INITIAL_CNN_SEED_GRAPH_CONNECT_PROB)
         * {
         * int weight = rand.nextInt(5) + 1;
         * setEdge(i, j, weight);
         * }
         * }
         * }
         */

        // 本処理: 残りノードを追加していく
        while (currentSize < getSize()) {

            if (rand.nextDouble() < p && !potentialEdges.isEmpty()) {
                // potential edge を実エッジに変換
                List<Edge> list = new ArrayList<>(potentialEdges);
                Edge edge = list.get(rand.nextInt(list.size()));
                setEdge(edge.from, edge.to, rand.nextInt(5) + 1);
                potentialEdges.remove(edge);
            } else {
                // 新しいノードを追加
                int newNode = currentSize;
                int existingNode = rand.nextInt(currentSize);
                // int existingNode = chooseNodeByDegree(newNode);
                setEdge(newNode, existingNode, 1);

                // 既存ノードの隣接ノードからpotential edgeを作成
                for (int neighbor = 0; neighbor < currentSize; neighbor++) {
                    if (this.adjacencyMatrix[existingNode][neighbor] > 0 && neighbor != newNode) {
                        potentialEdges.add(new Edge(newNode, neighbor));
                        // potentialEdges.add(new Edge(neighbor, newNode));
                    }
                }

                currentSize++;
            }
        }

        // 全ノードを確認し、出次数0のノードがいたらランダムに誰かをフォロー
        /*
         * for (int i = 0; i < getSize(); i++) {
         * boolean hasFollowee = false;
         * for (int j = 0; j < getSize(); j++) {
         * if (adjacencyMatrix[i][j] > 0) {
         * hasFollowee = true;
         * break;
         * }
         * }
         * if (!hasFollowee) {
         * int randomTarget;
         * do {
         * randomTarget = rand.nextInt(getSize());
         * } while (randomTarget == i); // 自分自身は避ける
         * 
         * setEdge(i, randomTarget, rand.nextInt(5) + 1);
         * }
         * }
         */

        // exp
        ///
        /*
         * for(int i = 0; i < agentSet.length; i++){
         * if(rand.nextDouble() < 0.5){
         * setEdge(i, 2, 1);
         * }
         * }
         */
        ///
        ///

        // --- Step: 全エッジ収集 ---
        List<Edge> edgeList = new ArrayList<>();
        for (int from = 0; from < getSize(); from++) {
            for (int to = 0; to < getSize(); to++) {
                if (adjacencyMatrix[from][to] > 0) {
                    edgeList.add(new Edge(from, to));
                }
            }
        }

        // --- Step: エッジスワップを指定回数試行 ---
        int rewiringTrials = 1000;
        int swapCount = 0;

        for (int t = 0; t < rewiringTrials; t++) {
            // ランダムに2つの異なるエッジを選択
            if (edgeList.size() < 2)
                break;

            Edge e1 = edgeList.get(rand.nextInt(edgeList.size()));
            Edge e2 = edgeList.get(rand.nextInt(edgeList.size()));
            if (e1.equals(e2))
                continue;

            int a = e1.from, b = e1.to;
            int c = e2.from, d = e2.to;

            // 無効スワップ条件（ループ、重複）を避ける
            if (a == d || c == b || a == c || b == d)
                continue;
            if (adjacencyMatrix[a][d] > 0 || adjacencyMatrix[c][b] > 0)
                continue;

            // スワップ実行：a→b, c→d → a→d, c→b
            double w1 = adjacencyMatrix[a][b];
            double w2 = adjacencyMatrix[c][d];

            // 行列更新
            adjacencyMatrix[a][b] = 0;
            adjacencyMatrix[c][d] = 0;
            adjacencyMatrix[a][d] = w1;
            adjacencyMatrix[c][b] = w2;

            // edgeList更新
            edgeList.remove(e1);
            edgeList.remove(e2);
            edgeList.add(new Edge(a, d));
            edgeList.add(new Edge(c, b));

            swapCount++;
        }
        System.out.println("Edge swap count in rewiring phase: " + swapCount);

    }

    private int chooseNodeByDegree(int maxIndex) {
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < maxIndex; i++) {
            int degree = 0;
            for (int j = 0; j < maxIndex; j++) {
                if (adjacencyMatrix[i][j] > 0 || adjacencyMatrix[j][i] > 0) {
                    degree++;
                }
            }

            // logスケールの重みを整数で近似（たとえば log(6) ≒ 1.79 → 約1回追加）
            int weight = (int) Math.floor(Math.log(degree + 1)); // +1で degree=0 のときも回避
            for (int k = 0; k < weight; k++) {
                candidates.add(i);
            }
        }

        // どのノードも選ばれなかったとき（全部degree=0など）→等確率で選ぶ
        if (candidates.isEmpty()) {
            return rand.nextInt(maxIndex);
        }

        return candidates.get(rand.nextInt(candidates.size()));
    }

}
