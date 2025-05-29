package network;

import agent.Agent;
import java.util.*;
import rand.randomGenerater;
import constants.Const;

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
            if (!(o instanceof Edge)) return false;
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

        // 初期ノード間をランダムに接続
        for (int i = 0; i < seedNodes; i++) {
            for (int j = 0; j < seedNodes; j++) {
                if (i != j && rand.nextDouble() < Const.INITIAL_CNN_SEED_GRAPH_CONNECT_PROB) {
                    int weight = rand.nextInt(5) + 1;
                    setEdge(i, j, weight);
                }
            }
        }

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
                setEdge(newNode, existingNode, rand.nextInt(5) + 1);

                // 既存ノードの隣接ノードからpotential edgeを作成
                for (int neighbor = 0; neighbor < currentSize; neighbor++) {
                    if (this.adjacencyMatrix[existingNode][neighbor] > 0 && neighbor != newNode) {
                        potentialEdges.add(new Edge(newNode, neighbor));
                    }
                }

                currentSize++;
            }
        }

        // 正規化処理
        /*for (int i = 0; i < getSize(); i++) {
            int rowSum = 0;
            for (int j = 0; j < getSize(); j++) {
                rowSum += this.adjacencyMatrix[i][j];
            }
            //agentSet[i].setNumOfPosts(rowSum);
            if (rowSum > 0) {
                for (int j = 0; j < getSize(); j++) {
                    if (this.adjacencyMatrix[i][j] > 0) {
                        double value = (double) this.adjacencyMatrix[i][j] / rowSum;
                        setEdge(i, j, value);
                    }
                }
            }
        }*/
    }
}
