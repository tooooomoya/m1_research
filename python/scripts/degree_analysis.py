import pandas as pd
import matplotlib.pyplot as plt
from collections import Counter
import powerlaw
import numpy as np

def plot_log_log_degree_distribution(degrees, title, ax):
    print("start")
    degree_counts = Counter(degrees)
    deg, freq = zip(*[(d, f) for d, f in degree_counts.items() if d > 0])
    log_deg = np.log10(deg)
    log_freq = np.log10(freq)
    ax.scatter(log_deg, log_freq)
    ax.set_xlabel("log10(Degree)")
    ax.set_ylabel("log10(Frequency)")
    ax.set_title(title)
    ax.grid(True)

def test_power_law_fit(degrees, label):
    print(f"\n--- {label} のスケールフリー性の検定 ---")
    results = powerlaw.Fit(degrees, discrete=True)
    alpha = results.power_law.alpha
    xmin = results.power_law.xmin
    print(f"推定されたスケールフリー分布のパラメータ: alpha = {alpha:.3f}, xmin = {xmin}")

    R, p = results.distribution_compare('power_law', 'lognormal')
    print(f"power_law vs lognormal: R = {R:.3f}, p = {p:.3f}")
    if p < 0.05:
        print("→ 対数正規分布の方が有意に適合している可能性があります。")
    else:
        print("→ スケールフリー分布が妥当なモデルである可能性があります。")

    return results

def plot_hist_log_y(degrees, title, ax):
    """度数分布を縦軸だけ log スケールでプロットするヒストグラム"""
    ax.hist(degrees, bins=50, edgecolor='black')
    ax.set_yscale('log')  # 縦軸だけ log
    ax.set_xlabel("Degree")
    ax.set_ylabel("Frequency (log scale)")
    ax.set_title(title)
    ax.grid(True)

def filter_and_analyze(degrees, min_degree, label, ax=None):
    # フィルタ処理
    filtered_degrees = degrees[degrees >= min_degree]
    print(f"\n--- {label}（{min_degree}以上） のスケールフリー性の検定 ---")
    print(f"対象ノード数: {len(filtered_degrees)}")

    # 検定
    results = powerlaw.Fit(filtered_degrees, discrete=True)
    alpha = results.power_law.alpha
    xmin = results.power_law.xmin
    print(f"推定されたスケールフリー分布のパラメータ: alpha = {alpha:.3f}, xmin = {xmin}")

    R, p = results.distribution_compare('power_law', 'lognormal')
    print(f"power_law vs lognormal: R = {R:.3f}, p = {p:.3f}")
    if p < 0.05:
        print("→ 対数正規分布の方が有意に適合している可能性があります。")
    else:
        print("→ スケールフリー分布が妥当なモデルである可能性があります。")

    # プロット（オプション）
    if ax:
        degree_counts = Counter(filtered_degrees)
        deg, freq = zip(*sorted(degree_counts.items()))
        ax.plot(deg, freq, marker='o', linestyle='None')
        ax.set_xscale('linear')
        ax.set_yscale('log')
        ax.set_xlabel("Degree")
        ax.set_ylabel("log(Frequency)")
        ax.set_title(f"{label} (Degree ≥ {min_degree})")
        ax.grid(True)

    return results

def plot_binned_degree_distribution(degrees, bin_width, title, ax):
    max_deg = degrees.max()
    bins = np.arange(0, max_deg + bin_width, bin_width)
    hist, bin_edges = np.histogram(degrees, bins=bins)
    
    # 中心点をx軸としてプロット
    bin_centers = 0.5 * (bin_edges[1:] + bin_edges[:-1])
    ax.plot(bin_centers, hist, marker='o', linestyle='-', color='purple')
    
    ax.set_yscale('log')
    ax.set_xlabel(f"Degree (binned, width={bin_width})")
    ax.set_ylabel("log(Frequency)")
    ax.set_title(title)
    ax.grid(True)



def main():
    # CSVファイルの読み込み
    file_path = 'results/degrees/degree_result_10000.csv'
    df = pd.read_csv(file_path)

    # in-degree と out-degree のデータ
    in_degrees = df['inDegree']
    out_degrees = df['outDegree']

    # グラフ描画
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))
    plot_log_log_degree_distribution(in_degrees, "Log-Log Plot of In-Degree", ax1)
    plot_log_log_degree_distribution(out_degrees, "Log-Log Plot of Out-Degree", ax2)
    #plot_hist_log_y(in_degrees, "Histogram of In-Degree (Y-axis Log Scale)", ax3)
    plt.tight_layout()
    plt.savefig('./results/figures/loglog_plot.png')
    
    

    # スケールフリー性の検定
    in_fit = test_power_law_fit(in_degrees, "In-degree")
    out_fit = test_power_law_fit(out_degrees, "Out-degree")

    # オプション：分布フィットの視覚化
    in_fit.plot_ccdf(color='b', label='In-degree empirical')
    in_fit.power_law.plot_ccdf(color='b', linestyle='--', label='In-degree power-law fit')
    out_fit.plot_ccdf(color='r', label='Out-degree empirical')
    out_fit.power_law.plot_ccdf(color='r', linestyle='--', label='Out-degree power-law fit')
    plt.legend()
    plt.title("CCDF and power-law distribution fitness")
    #plt.savefig('./results/figures/powerflow.png')

    min_degree = 10  # 例：10以上のみ使う

    fig, (ax3, ax4) = plt.subplots(1, 2, figsize=(12, 5))
    in_filtered_fit = filter_and_analyze(in_degrees, min_degree, "In-degree", ax3)
    out_filtered_fit = filter_and_analyze(out_degrees, min_degree, "Out-degree", ax4)
    plt.tight_layout()
    #plt.savefig('./results/figures/filtered_degree_logY_plot.png')

    # フィルタ後の CCDF & power law fit の可視化
    in_filtered_fit.plot_ccdf(color='b', label='In-degree empirical')
    in_filtered_fit.power_law.plot_ccdf(color='b', linestyle='--', label='In-degree fit')
    out_filtered_fit.plot_ccdf(color='r', label='Out-degree empirical')
    out_filtered_fit.power_law.plot_ccdf(color='r', linestyle='--', label='Out-degree fit')
    plt.legend()
    plt.title(f"CCDF with Degree ≥ {min_degree}")
    #plt.savefig('./results/figures/filtered_powerflow.png')
    
    # 度数のビン表示（in-degreeのみ例）
    fig, ax = plt.subplots(figsize=(6, 4))
    plot_binned_degree_distribution(in_degrees, bin_width=50, title="In-degree Distribution (Binned, Log-Y)", ax=ax)
    plt.tight_layout()
    plt.savefig("./results/figures/in_degree_binned_logY.png")


if __name__ == "__main__":
    main()