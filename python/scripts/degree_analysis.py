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


def main():
    # CSVファイルの読み込み
    file_path = 'results/degrees/degree_result_1000.csv'
    df = pd.read_csv(file_path)

    # in-degree と out-degree のデータ
    in_degrees = df['inDegree']
    out_degrees = df['outDegree']

    # グラフ描画
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))
    plot_log_log_degree_distribution(in_degrees, "Log-Log Plot of In-Degree", ax1)
    plot_log_log_degree_distribution(out_degrees, "Log-Log Plot of Out-Degree", ax2)
    plt.tight_layout()
    plt.savefig('loglog_plot.png')
    
    

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
    plt.savefig('powerflow.png')


if __name__ == "__main__":
    main()