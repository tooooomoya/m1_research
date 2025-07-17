import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import os

def analyze_and_plot_clustering_vs_indegree(step, output_path):
    # ファイルパスの組み立て
    degree_path = f"results/degrees/degree_result_{step}.csv"
    clustering_path = f"results/clusterings/clustering_result_{step}.csv"

    # CSV読み込み
    df_degree = pd.read_csv(degree_path)
    df_clust = pd.read_csv(clustering_path)

    # agentIdで結合
    df = pd.merge(df_degree, df_clust, on="agentId")

    # in-degree > 0 に限定
    df = df[df["inDegree"] > 0]

    # 平均クラスタ係数
    avg_clust = df["clusteringCoefficient"].mean()
    print("=== クラスタ係数分布 ===")
    print(f"平均クラスタ係数: {avg_clust:.4f}")

    # ▶ 散布図（個々のノード）
    plt.figure(figsize=(6, 4))
    plt.scatter(df["inDegree"], df["clusteringCoefficient"], color='black', alpha=0.5, label="Individual nodes")

    # ▶ inDegree ごとの平均クラスタ係数（赤線）
    grouped = df.groupby("inDegree")["clusteringCoefficient"].mean()
    plt.plot(grouped.index, grouped.values, color='red', linewidth=2, label="Average by in-degree")

    plt.xscale('log')
    plt.xlabel(r'$k^{(\mathrm{in})}$', fontsize=14)
    plt.ylabel('Clustering Coefficient', fontsize=14)
    plt.grid(True, which='both', ls=':')
    plt.legend(fontsize=12)
    plt.tight_layout()
    plt.savefig(output_path)
    print(f"✅ 図を保存しました: {output_path}")

    # 統計結果を返す
    return {
        'step': step,
        'average_clustering': avg_clust,
        'in_degree_unique': grouped.index.tolist(),
        'avg_clustering_by_in_degree': grouped.values.tolist()
    }

def main():
    analyze_and_plot_clustering_vs_indegree(10000, "results/figures/clusteringCoefficinet_dist.png")
 
if __name__ == "__main__":
    main()
