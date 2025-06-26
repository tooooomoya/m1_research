import pandas as pd
import matplotlib.pyplot as plt
import os

def smooth_ratios(ratios, window_size=10):
    smoothed = {bin_name: [] for bin_name in ratios}
    num_steps = len(next(iter(ratios.values())))

    for i in range(0, num_steps, window_size):
        for bin_name in ratios:
            window = ratios[bin_name][i:i+window_size]
            if window:
                avg = sum(window) / len(window)
                smoothed[bin_name].append(avg)

    return smoothed


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

def plot_ratio_bins(x, ratios, bin_labels=None, window_size=10):
    if not x:
        print("❌ プロットできるデータがありません。")
        return

    # スムージング
    smoothed_ratios = smooth_ratios(ratios, window_size)
    smoothed_x = [x[i] for i in range(0, len(x), window_size)]

    plt.figure(figsize=(10, 6))
    colors = ["blue", "dodgerblue", "green", "orange", "red"]

    for idx, (bin_name, values) in enumerate(smoothed_ratios.items()):
        label = bin_labels[idx] if bin_labels and idx < len(bin_labels) else bin_name
        color = colors[idx % len(colors)]
        plt.plot(smoothed_x, values, label=label, color=color, alpha=0.6)

    plt.xlabel("Step")
    plt.ylabel("Post Ratio (10-step average)")
    plt.title("Smoothed Ratio of Posts in Each Bin (10-step average)")
    plt.ylim(0, 1)
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    output_path = "./results/figures/ratio_bins_smoothed.png"
    plt.savefig(output_path)
    print(f"✅ 平滑化プロットを保存しました: {output_path}")


def main():
    data_dir = "./results/posts"
    bins = ["bin_0", "bin_1", "bin_2", "bin_3", "bin_4"]
    bin_labels = ["-1.0 ~ -0.6", "-0.6 ~ -0.2", "-0.2 ~ 0.2", "0.2 ~ 0.6", "0.6 ~ 1.0"]

    x, ratios = read_and_compute_ratios(data_dir, bins)
    plot_ratio_bins(x, ratios, bin_labels)

if __name__ == "__main__":
    main()