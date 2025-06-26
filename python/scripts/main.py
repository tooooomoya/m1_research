import subprocess

# 各スクリプトを順に実行
scripts = [
    "post_analysis.py",
    "degree_analysis.py",
    "feed_analysis.py",
    "latent_opinion_analysis.py",
    "opinion_change_gif_maker.py",
    "opinion_variance_analysis.py"
]

for script in scripts:
    print(f"\n🚀 実行中: {script}")
    result = subprocess.run(["python3", "python/scripts/" + script], capture_output=True, text=True)
    
    if result.returncode != 0:
        print(f"❌ エラーが発生しました: {script}")
        print(result.stderr)
        break
    else:
        print(f"✅ 完了: {script}")
        print(result.stdout)
