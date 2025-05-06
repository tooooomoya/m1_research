package network;

import agent.Agent;
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
        int seedNodes = Math.min(20, getSize()); // 最初に作るノード数
        int currentSize = seedNodes;

        // 初期ノード間をランダムに接続
        for (int i = 0; i < seedNodes; i++) {
            for (int j = 0; j < seedNodes; j++) {
                if (i != j && rand.nextDouble() < 0.2) {
                    int weight = rand.nextInt(5) + 1;
                    setEdge(i, j, weight);
                }
            }
        }

        // 本処理: 残りノードを追加していく
        while (currentSize < getSize()) {
            if (rand.nextDouble() < p && !potentialEdges.isEmpty()) {
                // make potential edge to the real edge with Prob p
                List<Edge> list = new ArrayList<>(potentialEdges);
                Edge edge = list.get(rand.nextInt(list.size()));
                setEdge(edge.from, edge.to, rand.nextInt(5) + 1);
                potentialEdges.remove(edge);
            } else {
                // follow someone (maybe his or her friend)
                int newNode = currentSize;
                int existingNode = rand.nextInt(currentSize);
                setEdge(newNode, existingNode, rand.nextInt(5) + 1);

                // the friend's friend can be a "potential edge" (he or she likely follow their friend's friends)
                for (int neighbor = 0; neighbor < currentSize; neighbor++) {
                    if (this.adjacencyMatrix[existingNode][neighbor] > 0 && neighbor != newNode) {
                        potentialEdges.add(new Edge(newNode, neighbor));
                    }
                }

                currentSize++;
            }
        }

        // 正規化処理
        for (int i = 0; i < getSize(); i++) {
            int rowSum = 0;
            for (int j = 0; j < getSize(); j++) {
                rowSum += this.adjacencyMatrix[i][j];
            }
            if (rowSum > 0) {
                for (int j = 0; j < getSize(); j++) {
                    if (this.adjacencyMatrix[i][j] > 0) {
                        double value = (double) this.adjacencyMatrix[i][j] / rowSum;
                        setEdge(i, j, value);
                    }
                }
            }
        }
    }
}
