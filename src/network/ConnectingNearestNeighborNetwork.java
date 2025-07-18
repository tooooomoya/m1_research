package network;

import agent.Agent;
import java.util.*;
import rand.randomGenerator;

public class ConnectingNearestNeighborNetwork extends Network {
    private double p; // potential edge を実エッジに変換する確率
    private Random rand = randomGenerator.rand;
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
        System.out.println("start making network");

        double r = 0.01;
        
        int currentSize = 3;
        setEdge(0, 1, 1);
        setEdge(0, 2, 1);
        setEdge(1, 0, 1);
        setEdge(2, 0, 1);
        
        while (currentSize < getSize()) {
            if (rand.nextDouble() < 1 - this.p) {
                // 新しいノードを追加
                int newNode = currentSize++;
                int v = rand.nextInt(newNode);
                setEdge(newNode, v, 1);

                for (int neighbor = 0; neighbor < newNode; neighbor++) {
                    if (adjacencyMatrix[v][neighbor] > 0 && neighbor != newNode) {
                        potentialEdges.add(new Edge(newNode, neighbor));
                    }
                }
            } else {
                if (rand.nextDouble() < 1 - r) {
                    // potential edge を実エッジに変換
                    if (!potentialEdges.isEmpty()) {
                        List<Edge> list = new ArrayList<>(potentialEdges);
                        // from, to でソートして順序を固定
                        list.sort(Comparator.comparingInt((Edge e) -> e.from).thenComparingInt(e -> e.to));
                        Edge edge = list.get(rand.nextInt(list.size()));
                        setEdge(edge.from, edge.to, 1);
                        potentialEdges.remove(edge);
                    }
                } else {
                    // ランダムリンク追加
                    int a = rand.nextInt(currentSize);
                    int b;
                    do {
                        b = rand.nextInt(currentSize);
                    } while (a == b || adjacencyMatrix[a][b] > 0);

                    setEdge(a, b, 1);
                }
            }
        }

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
