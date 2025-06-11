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

def plot_variances(df, output_path="./results/figures/variances_over_time.png"):
    steps = df['step']
    opinion_var = smooth_series(df['opinionVar'], window=10)
    post_opinion_var = smooth_series(df['postOpinionVar'], window=10)

    plt.figure(figsize=(10, 6))
    plt.plot(steps, opinion_var, label='Latent public opinion Variance', color='green')
    plt.plot(steps, post_opinion_var, label='Public opinion Variance inferred from social media posts', color='orange')
    plt.xlabel("Step")
    plt.ylabel("Variance")
    plt.title("latent opinion vs post opinion Variance over Time")
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(output_path)
    print(f"✅ 分散グラフを保存しました: {output_path}")


def main():
    folder_path = "./results/metrics"
    max_step = 20000  # ← ここでステップ数を指定

    df = read_metrics_up_to_step(folder_path, max_step)
    plot_variances(df)  # ← 追加


if __name__ == "__main__":
    main()
