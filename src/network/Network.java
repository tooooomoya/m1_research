package network;

import agent.Agent;

public abstract class Network {
    double[][] adjacencyMatrix;
    private int size;

    // Constructor
    public Network(int size) {
        this.size = size;
        this.adjacencyMatrix = new double[size][size];
    }

    // getter methods

    // 隣接行列の取得（コピーを返すことで外部からの変更を防ぐ）
    public double[][] getAdjacencyMatrix() {
        double[][] copy = new double[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(this.adjacencyMatrix[i], 0, copy[i], 0, size);
        }
        return copy;
    }

    public double getEdge(int from, int to) {
        return this.adjacencyMatrix[from][to];
    }

    // protected getter（継承クラスから使えるように）
    protected int getSize() {
        return this.size;
    }

    // setter methods

    // change the num of posts from user j that user i can see
    public void setEdge(int i, int j, double weight) {
        if (i >= 0 && i < size && j >= 0 && j < size) { // 不正検知
            adjacencyMatrix[i][j] = weight; // weightはユーザiがユーザjの投稿を何件閲覧するか
        }
    }

    public void reduceEdge(int i, int j, double weight) {
        if (i >= 0 && i < size && j >= 0 && j < size) { // 不正検知
            adjacencyMatrix[i][j] -= weight; // weightはユーザiがユーザjの投稿を何件閲覧するか
        }
    }

    public void increaseEdge(int i, int j, double weight) {
        if (i >= 0 && i < size && j >= 0 && j < size) { // 不正検知
            adjacencyMatrix[i][j] += weight; // weightはユーザiがユーザjの投稿を何件閲覧するか
        }
    }

    // ネットワーク全体の隣接行列を一括で設定（サイズチェック付き）
    public void setAdjacencyMatrix(double[][] newMatrix) {
        if (newMatrix.length != size || newMatrix[0].length != size) {
            throw new IllegalArgumentException("Adjacency matrix must be of size " + size + "x" + size);
        }
        for (int i = 0; i < size; i++) {
            System.arraycopy(newMatrix[i], 0, this.adjacencyMatrix[i], 0, size);
        }
    }

    // user i unfollow user j (user i temporarily does not see any posts from user
    // j)
    public void removeEdge(int i, int j) {
        if (i >= 0 && i < size && j >= 0 && j < size) {
            adjacencyMatrix[i][j] = 0;
        }
    }

    // あるユーザがフォローしているユーザのリストを返す
    public double[] getNeighbors(int node) {
        if (node >= 0 && node < size) {
            return adjacencyMatrix[node].clone();
        }
        return new double[0];
    }

    // abstract methods

    public abstract void makeNetwork(Agent[] agentSet);

}