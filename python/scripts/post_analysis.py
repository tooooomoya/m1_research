import pandas as pd
import matplotlib.pyplot as plt
import os

def read_and_accumulate_bins(data_dir, bins, max_index=10000):
    cumulative_counts = {bin_name: [] for bin_name in bins}
    current_cumulative = {bin_name: 0 for bin_name in bins}
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

            for bin_name in bins:
                value = int(row[bin_name])
                current_cumulative[bin_name] += value
                cumulative_counts[bin_name].append(current_cumulative[bin_name])
            
            x.append(i)
        except Exception as e:
            print(f"[{i:02d}] エラー発生: {e}")

    if x:
        print("✅ すべてのファイルの処理が完了しました。")
    else:
        print("⚠️ ファイルが読み込めなかったか、データが空です。")

    return x, cumulative_counts

def plot_cumulative_bins(x, cumulative_counts, bin_labels=None):
    if not x:
        print("❌ プロットできるデータがありません。")
        return

    plt.figure(figsize=(10, 6))

    colors = ["blue", "dodgerblue", "green", "orange", "red"]

    for idx, (bin_name, values) in enumerate(cumulative_counts.items()):
        label = bin_labels[idx] if bin_labels and idx < len(bin_labels) else bin_name
        color = colors[idx % len(colors)]  # インデックス超えても安全
        plt.plot(x, values, label=label, color=color)

    plt.xlabel("Step")
    plt.ylabel("Cumulative Posts")
    plt.title("Cumulative Post Counts by Bin")
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    output_path = "cumulative_bins.png"
    plt.savefig(output_path)
    print(f"✅ プロット画像を保存しました: {output_path}")


def main():
    data_dir = "./results/posts"
    bins = ["bin_0", "bin_1", "bin_2", "bin_3", "bin_4"]  # ← 実際のCSVカラム名
    bin_labels = ["-1.0 ~ -0.6", "-0.6 ~ -0.2", "-0.2 ~ 0.2", "0.2 ~ 0.6", "0.6 ~ 1.0"]  # ← 描画用の凡例名

    x, cumulative_counts = read_and_accumulate_bins(data_dir, bins)
    plot_cumulative_bins(x, cumulative_counts, bin_labels)

if __name__ == "__main__":
    main()
