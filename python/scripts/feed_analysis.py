import pandas as pd
import matplotlib.pyplot as plt
import os
import numpy as np

def read_metrics_mean_var_series(data_dir, target_prefix, metric_prefix, var_prefix, num_classes=5, max_index=10000):
    class_means = {i: [] for i in range(num_classes)}
    class_stds = {i: [] for i in range(num_classes)}
    steps = []

    for i in range(max_index + 1):
        filename = f"{target_prefix}_{i}.csv"
        filepath = os.path.join(data_dir, filename)
        if not os.path.isfile(filepath):
            continue
        try:
            df = pd.read_csv(filepath)
            row = df.iloc[0]
            steps.append(int(row["step"]))
            for c in range(num_classes):
                mean_key = f"{metric_prefix}_{c}"
                var_key = f"{var_prefix}_{c}"
                mean_val = float(row[mean_key])
                var_val = float(row[var_key])
                class_means[c].append(mean_val)
                class_stds[c].append(np.sqrt(var_val))  # 標準偏差に変換
        except Exception as e:
            print(f"[{i:04d}] エラー: {e}")

    return steps, class_means, class_stds

def plot_smoothed_band(x, mean_dict, std_dict, title, ylabel, output_path, window_size=10):
    smoothed_x = [x[i] for i in range(0, len(x), window_size)]

    plt.figure(figsize=(10, 6))
    colors = ["blue", "dodgerblue", "green", "orange", "red"]
    for c in mean_dict:
        mean = mean_dict[c]
        std = std_dict[c]
        color = colors[c % len(colors)]
        plt.plot(smoothed_x, mean, label=f"Class {c}", color=color)
        plt.fill_between(smoothed_x,
                         np.array(mean) - np.array(std),
                         np.array(mean) + np.array(std),
                         color=color, alpha=0.2)

    plt.xlabel("Step", fontsize=14)
    plt.ylabel(ylabel, fontsize=14)
    #plt.title(title)
    plt.grid(True)
    plt.legend(fontsize=14)
    plt.tight_layout()
    plt.savefig(output_path)
    print(f"✅ プロット保存: {output_path}")

def smooth(series_dict, window_size=10):
    smoothed = {}
    for c, values in series_dict.items():
        smoothed[c] = [
            np.mean(values[i:i+window_size])
            for i in range(0, len(values), window_size)
        ]
    return smoothed

def main():
    data_dir = "./results/metrics"
    prefix = "result"
    window_size = 10
    num_classes = 5

    metric_prefix = "feedPostOpinionMean"
    var_prefix = "feedPostOpinionVar"
    ylabel = "Feed Post Opinion Mean"
    title = "Feed Post Opinion Mean per Class (Smoothed, ±1σ)"
    filename = "feed_post_mean_with_std.png"

    x, mean_series, std_series = read_metrics_mean_var_series(
        data_dir, prefix, metric_prefix, var_prefix, num_classes
    )

    if not x:
        print(f"⚠️ データがありません: {metric_prefix}")
        return

    smoothed_means = smooth(mean_series, window_size)
    smoothed_stds = smooth(std_series, window_size)

    os.makedirs("./results/figures", exist_ok=True)
    output_path = os.path.join("./results/figures", filename)
    plot_smoothed_band(x, smoothed_means, smoothed_stds, title, ylabel, output_path, window_size)

if __name__ == "__main__":
    main()
