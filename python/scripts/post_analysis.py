import pandas as pd
import matplotlib.pyplot as plt
import os

def read_and_accumulate_bins(data_dir, max_index=99):
    bins = ['bin_0', 'bin_1', 'bin_2', 'bin_3', 'bin_4']
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
                value = int(row[bin_name])  # 明示的に整数化
                current_cumulative[bin_name] += value
                cumulative_counts[bin_name].append(current_cumulative[bin_name])
            
            x.append(i)
            print(f"[{i:02d}] 読み込み完了: {filename}")
        except Exception as e:
            print(f"[{i:02d}] エラー発生: {e}")

    if x:
        print("✅ すべてのファイルの処理が完了しました。")
    else:
        print("⚠️ ファイルが読み込めなかったか、データが空です。")

    return x, cumulative_counts

def plot_cumulative_bins(x, cumulative_counts):
    if not x:
        print("❌ プロットできるデータがありません。")
        return

    plt.figure(figsize=(10, 6))
    for bin_name, values in cumulative_counts.items():
        plt.plot(x, values, label=bin_name)
    plt.xlabel("File Index")
    plt.ylabel("Cumulative Posts")
    plt.title("Cumulative Post Counts by Bin")
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    # 保存して表示
    output_path = "cumulative_bins.png"
    plt.savefig(output_path)
    print(f"✅ プロット画像を保存しました: {output_path}")
    plt.show()

def main():
    data_dir = "./results/posts"  # ← ディレクトリパスを修正
    x, cumulative_counts = read_and_accumulate_bins(data_dir)
    plot_cumulative_bins(x, cumulative_counts)

if __name__ == "__main__":
    main()
