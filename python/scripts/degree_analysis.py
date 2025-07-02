import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import powerlaw

def analyze_and_plot_indegree_fit(in_degrees, output_path):
    # 前処理
    filtered = in_degrees[in_degrees > 0]

    # フィッティング
    fit = powerlaw.Fit(filtered, discrete=True, verbose=False)
    alpha = fit.power_law.alpha
    xmin = fit.power_law.xmin

    # ▶ lognormal との比較
    R, p = fit.distribution_compare('power_law', 'lognormal')

    print("=== Power-law フィット結果 ===")
    print(f"推定された alpha: {alpha:.3f}")
    print(f"xmin: {xmin}")
    print(f"尤度比 R = L_powerlaw - L_lognormal = {R:.3f}")
    print(f"p-value = {p:.3f}")
    if p < 0.05:
        print("✅ 対数正規分布（log-normal）の方が統計的に有意に適合")
    else:
        print("⚠ Power-law でも十分に適合している可能性あり")

    # ▶ 図2A: CCDF + フィット2本重ね
    plt.figure(figsize=(6, 4))
    ax = plt.gca()  # 現在のAxesを取得

    # 実データ（黒点）
    fit.plot_ccdf(ax=ax, color='black', marker='o', linestyle='None', label='Empirical')

    # Power-law フィット（赤線）
    fit.power_law.plot_ccdf(ax=ax, color='red', linestyle='--', linewidth=2, label='Power-law fit')

    # Log-normal フィット（青線）
    fit.lognormal.plot_ccdf(ax=ax, color='blue', linestyle='-', linewidth=2, label='Log-normal fit')

    plt.xscale('log')
    plt.yscale('log')
    plt.xlabel(r'$k^{(\mathrm{in})}$', fontsize=14)
    plt.ylabel(r'$P(k^{(\mathrm{in})} \geq x)$', fontsize=14)
    plt.grid(True, which='both', ls=':')
    plt.legend(fontsize=14)
    plt.tight_layout()
    plt.savefig(output_path)
    print(f"✅ 図を保存しました: {output_path}")

    # 統計結果を返す
    return {
        'alpha': alpha,
        'xmin': xmin,
        'R': R,
        'p_value': p
    }


def main():
    df = pd.read_csv("results/degrees/degree_result_10000.csv")
    in_degrees = df["inDegree"]
    results = analyze_and_plot_indegree_fit(
        in_degrees,
        "./results/figures/indegree_ccdf_fit.png"
    )
 
if __name__ == "__main__":
    main()
