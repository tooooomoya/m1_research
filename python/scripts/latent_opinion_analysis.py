import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import os
import glob

def smooth_series(series, window=10):
    return series.rolling(window=window, min_periods=1, center=True).mean()

def read_metrics_up_to_step(folder_path, max_step):
    all_files = sorted(glob.glob(os.path.join(folder_path, "result_*.csv")))
    dfs = []

    for file in all_files:
        try:
            df = pd.read_csv(file)
            if 'step' in df.columns:
                dfs.append(df[df['step'] <= max_step])
        except Exception as e:
            print(f"⚠️ エラー: {file} - {e}")

    if not dfs:
        raise ValueError("⚠️ 有効なデータが読み込めませんでした。")

    combined_df = pd.concat(dfs).sort_values(by='step').reset_index(drop=True)
    return combined_df

def plot_opinion_with_scaled_std(df, output_path="./results/figures/opinion_avg_with_scaled_std_band.png"):
    steps = df['step']
    opinion_avg = df['opinionAvg']
    std_dev = np.sqrt(df['opinionVar']) * 0.01  # スケーリング

    smoothed_avg = smooth_series(opinion_avg, window=10)
    smoothed_std = smooth_series(std_dev, window=10)

    upper_band = smoothed_avg + smoothed_std
    lower_band = smoothed_avg - smoothed_std

    plt.figure(figsize=(10, 6))
    plt.plot(steps, smoothed_avg, label='smoothed opinion Average', color='blue')
    plt.fill_between(steps, lower_band, upper_band, color='lightblue', alpha=0.4, label='±0.01×Std Dev')
    plt.xlabel("Step")
    plt.ylabel("latent opinion Average")
    plt.title("latent opinion Average with scaled std dev band")
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(output_path)
    print(f"✅ グラフを保存しました: {output_path}")

def main():
    folder_path = "./results/metrics"
    max_step = 20000  # ← ここでステップ数を指定

    df = read_metrics_up_to_step(folder_path, max_step)
    plot_opinion_with_scaled_std(df)

if __name__ == "__main__":
    main()
