import os
import pandas as pd
import matplotlib.pyplot as plt
import imageio

def create_histogram_gif(input_dir='results/opinion', output_gif='histogram.gif', duration=0.5, step_interval=100, y_max=2000):
    """
    指定ディレクトリ内の opinion_result_*.csv をステップ間隔ごとに読み込み、
    ヒストグラムを描いてGIFを作成します。

    Parameters:
    - input_dir: CSVファイルが保存されているディレクトリ
    - output_gif: 保存するGIFファイル名
    - duration: 各フレームの表示時間（秒）
    - step_interval: 何ステップごとにプロットするか（例：100ステップごと）
    """
    files = sorted([
        f for f in os.listdir(input_dir)
        if f.startswith("opinion_result_") and f.endswith(".csv")
    ], key=lambda x: int(x.split('_')[-1].split('.')[0]))  # ステップ順にソート

    images = []
    for file in files:
        step = int(file.split('_')[-1].split('.')[0])

        if step % step_interval != 0:
            continue  # スキップ

        df = pd.read_csv(os.path.join(input_dir, file))
        bins = df.columns[1:]
        values = df.iloc[0, 1:]

        plt.figure()
        plt.bar(bins, values, color='skyblue')
        plt.title(f'Opinion Histogram - Step {step}')
        plt.xlabel('Opinion Bins')
        plt.ylabel('Frequency')
        plt.ylim(0, y_max)
        plt.tight_layout()

        tmp_path = f'temp_hist_{step}.png'
        plt.savefig(tmp_path)
        plt.close()
        images.append(imageio.v2.imread(tmp_path))
        os.remove(tmp_path)

    if images:
        imageio.mimsave(output_gif, images, duration=duration)
        print(f'GIF saved as {output_gif}')
    else:
        print("No images were created. Please check your input files and step interval.")


def save_histogram_for_step(step, input_dir='results/opinion', output_path=None):
    """
    指定ステップの opinion_result_{step}.csv を読み込んで
    ヒストグラム画像を保存します。
    """
    file = os.path.join(input_dir, f'opinion_result_{step}.csv')
    if not os.path.exists(file):
        raise FileNotFoundError(f"No file found for step {step}: {file}")

    df = pd.read_csv(file)
    bins = df.columns[1:]
    values = df.iloc[0, 1:]

    plt.figure()
    plt.bar(bins, values, color='coral')
    plt.title(f'Opinion Histogram - Step {step}')
    plt.xlabel('Opinion Bins')
    plt.ylabel('Frequency')
    plt.tight_layout()

    if output_path is None:
        output_path = f'opinion_histogram_step_{step}.png'

    plt.savefig(output_path)
    plt.close()
    print(f'Histogram for step {step} saved as {output_path}')

def main():
    # ヒストグラムGIFの作成
    create_histogram_gif(step_interval=1000, y_max=2000)

    # ステップ100のヒストグラム画像を保存
    #save_histogram_for_step(10000)


if __name__ == "__main__":
    main()