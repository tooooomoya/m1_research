import pandas as pd
import matplotlib.pyplot as plt
import os
import numpy as np

def smooth_ratios_and_sems(ratios, window_size=100):
    smoothed_means = {bin_name: [] for bin_name in ratios}
    smoothed_sems = {bin_name: [] for bin_name in ratios}
    num_steps = len(next(iter(ratios.values())))

    for i in range(0, num_steps, window_size):
        for bin_name in ratios:
            window = ratios[bin_name][i:i+window_size]
            if window:
                mean = np.mean(window)
                sem = np.std(window, ddof=1) / np.sqrt(len(window))  # 標準誤差
                smoothed_means[bin_name].append(mean)
                smoothed_sems[bin_name].append(sem)

    return smoothed_means, smoothed_sems


def read_and_compute_ratios(data_dir, bins, max_index=10000):
    ratios = {bin_name: [] for bin_name in bins}
    x = []

    for i in range(max_index + 1):
        filename = f"post_result_{i}.csv"
        filepath = os.path.join(data_dir, filename)
        if not os.path.isfile(filepath):
            print(f"[{i:02d}] ファイルが見つかりません: {filename}")
            continue

        try:
            df = pd.read_csv(filepath)
            row = df.iloc[0]

            total = sum([int(row[bin_name]) for bin_name in bins])
            if total == 0:
                continue  # ゼロ除算を避けるためスキップ

            for bin_name in bins:
                count = int(row[bin_name])
                ratios[bin_name].append(count / total)

            x.append(i)
        except Exception as e:
            print(f"[{i:02d}] エラー発生: {e}")

    if x:
        print("✅ すべてのファイルの処理が完了しました。")
    else:
        print("⚠️ ファイルが読み込めなかったか、データが空です。")

    return x, ratios

def plot_ratio_bins(x, ratios, bin_labels=None, window_size=100):
    if not x:
        print("❌ プロットできるデータがありません。")
        return

    # スムージング + 標準誤差計算
    smoothed_means, smoothed_sems = smooth_ratios_and_sems(ratios, window_size)
    smoothed_x = [x[i] for i in range(0, len(x), window_size)]

    plt.figure(figsize=(10, 6))
    colors = ["blue", "dodgerblue", "green", "orange", "red"]

    for idx, bin_name in enumerate(smoothed_means):
        mean_values = smoothed_means[bin_name]
        sem_values = smoothed_sems[bin_name]
        label = bin_labels[idx] if bin_labels and idx < len(bin_labels) else bin_name
        color = colors[idx % len(colors)]

        plt.plot(smoothed_x, mean_values, label=label, color=color, alpha=0.8)
        plt.fill_between(smoothed_x,
                         np.array(mean_values) - np.array(sem_values),
                         np.array(mean_values) + np.array(sem_values),
                         color=color, alpha=0.2)

    plt.xlabel("Step", fontsize=14)
    plt.ylabel("Post Ratio (100-step average ±1 SE)", fontsize=14)
    plt.ylim(0, 1)
    plt.legend(fontsize=12)
    plt.grid(True)
    plt.tight_layout()
    output_path = "./results/figures/ratio_bins_smoothed_with_se.png"
    plt.savefig(output_path)
    print(f"✅ 平滑化プロット（±SE帯つき）を保存しました: {output_path}")



def main():
    data_dir = "./results/posts"
    bins = ["bin_0", "bin_1", "bin_2", "bin_3", "bin_4"]
    bin_labels = ["-1.0 ~ -0.6", "-0.6 ~ -0.2", "-0.2 ~ 0.2", "0.2 ~ 0.6", "0.6 ~ 1.0"]

    x, ratios = read_and_compute_ratios(data_dir, bins)
    plot_ratio_bins(x, ratios, bin_labels)

if __name__ == "__main__":
    main()