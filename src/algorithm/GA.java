package algorithm;

import agent.Agent;

import java.util.SplittableRandom;

public abstract class GA {

    protected SplittableRandom random;
    protected int type;

    public GA(SplittableRandom random, int type) {
        this.random = random;
        this.type = type;
    }

    // 親の選択（Roulette選択）
    protected Agent selectParent(Agent[] candidates) {
        return spinRoulette(candidates);
    }

    protected Agent spinRoulette(Agent[] candidates) {
        double sum = 0.0;
        double min = Double.MAX_VALUE;

        for (Agent agent : candidates) {
            min = Math.min(min, agent.getFitness());
        }

        for (Agent agent : candidates) {
            sum += Math.pow(agent.getFitness() - min, 2);
        }

        double prob = random.nextDouble(sum + 1e-5);

        for (Agent agent : candidates) {
            double value = Math.pow(agent.getFitness() - min, 2);
            if (prob < value)
                return agent;
            prob -= value;
        }

        return candidates[candidates.length - 1];
    }

    // --- 交叉メソッド ---
    // 4種類すべてに対応したuniform crossoverを用意
    protected int[] uniformCrossover(int[] motherGene, int[] fatherGene) {
        int length = motherGene.length;
        int[] childGene = new int[length];
        for (int i = 0; i < length; i++) {
            childGene[i] = random.nextBoolean() ? motherGene[i] : fatherGene[i];
        }
        return childGene;
    }

    // crossoverGeneメソッド
    protected void crossoverGene(Agent mother, Agent father, Agent child, int geneIndex) {
        // 母親・父親からビット列を取得
        int[] motherBits = mother.getGene(geneIndex).getBits();
        int[] fatherBits = father.getGene(geneIndex).getBits();

        // uniform crossoverで子供のbit列を生成
        int[] childBits = uniformCrossover(motherBits, fatherBits);

        // 子供の該当Geneを取得し、ビット列をセット
        Gene childGene = child.getGene(geneIndex);
        childGene.setBits(childBits);

        // 子供のgeneArrayにセットし直す（参照の問題がなければ不要かも）
        child.setGene(geneIndex, childGene);
    }

    // --- 突然変異メソッド ---
    // 1ビットずつ突然変異する（0⇄1を反転）
    // mutateGeneメソッド（AgentのGeneを対象）
    protected void mutateGene(Agent agent, int geneIndex, double mutationRate) {
        Gene gene = agent.getGene(geneIndex);
        int[] bits = gene.getBits();

        for (int i = 0; i < bits.length; i++) {
            if (random.nextDouble() < mutationRate) {
                bits[i] = 1 - bits[i]; // bit flip
            }
        }

        gene.setBits(bits);
        agent.setGene(geneIndex, gene);
    }
}
