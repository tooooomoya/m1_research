import pandas as pd
import matplotlib.pyplot as plt
import os
import numpy as np

def read_metrics_series(data_dir, target_prefix, metric_prefix, num_classes=5, max_index=20000):
    class_series = {i: [] for i in range(num_classes)}
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
                key = f"{metric_prefix}_{c}"
                class_series[c].append(float(row[key]))
        except Exception as e:
            print(f"[{i:04d}] エラー: {e}")

    return steps, class_series

def smooth_and_get_band(series_dict, window_size=10):
    smoothed_means = {}
    smoothed_stds = {}
    for c, values in series_dict.items():
        means = []
        stds = []
        for i in range(0, len(values), window_size):
            window = values[i:i+window_size]
            if window:
                means.append(np.mean(window))
                stds.append(np.std(window))
        smoothed_means[c] = means
        smoothed_stds[c] = stds
    return smoothed_means, smoothed_stds

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

    plt.xlabel("Step")
    plt.ylabel(ylabel)
    plt.title(title)
    plt.grid(True)
    plt.legend()
    plt.tight_layout()
    plt.savefig(output_path)
    print(f"✅ プロット保存: {output_path}")

def main():
    data_dir = "./results/metrics"
    prefix = "results"
    window_size = 10
    num_classes = 5

    metric_prefix = "feedPostOpinionMean"
    ylabel = "Feed Post Opinion Mean"
    title = "Feed Post Opinion Mean per Class (Smoothed, ±1σ)"
    filename = "feed_post_mean_with_std.png"

    x, series = read_metrics_series(data_dir, prefix, metric_prefix, num_classes)
    if not x:
        print(f"⚠️ データがありません: {metric_prefix}")
        return

    means, stds = smooth_and_get_band(series, window_size)
    os.makedirs("./results/figures", exist_ok=True)
    output_path = os.path.join("./results/figures", filename)
    plot_smoothed_band(x, means, stds, title, ylabel, output_path, window_size)

if __name__ == "__main__":
    main()
