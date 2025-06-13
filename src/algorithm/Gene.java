package algorithm;

import java.util.Arrays;
import java.util.SplittableRandom;

public class Gene {

    private int[] bits;
    private static final double MUTATION_RATE = 0.1; // 突然変異確率
    private static SplittableRandom random = new SplittableRandom();

    // コンストラクタ（bitsのコピーを作成）
    public Gene(int[] bits) {
        this.bits = Arrays.copyOf(bits, bits.length);
    }

    // ゲッター
    public int[] getBits() {
        return bits;
    }

    // セッター
    public void setBits(int[] newBits) {
        this.bits = Arrays.copyOf(newBits, newBits.length);
    }

    // 一様交叉（母親か父親のビットをランダムに選ぶ）
    public static Gene uniformCrossover(Gene mother, Gene father) {
        int length = mother.bits.length;
        int[] childBits = new int[length];
        for (int i = 0; i < length; i++) {
            childBits[i] = random.nextBoolean() ? mother.bits[i] : father.bits[i];
        }
        return new Gene(childBits);
    }

    // 突然変異（確率でビットを反転）
    public void mutateGene() {
        for (int i = 0; i < bits.length; i++) {
            if (random.nextDouble() < MUTATION_RATE) {
                bits[i] = 1 - bits[i];  // 0->1 または 1->0
            }
        }
    }

    // クローン
    public Gene clone() {
        return new Gene(this.bits);
    }

    // デバッグ用表示
    @Override
    public String toString() {
        return Arrays.toString(bits);
    }
}
