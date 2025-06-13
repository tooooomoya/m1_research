package algorithm;

import agent.Agent;
import java.util.ArrayList;
import java.util.SplittableRandom;

public class MWGA extends GA {

    private double mutationRate;

    public MWGA(SplittableRandom random, int type, double mutationRate) {
        super(random, type);
        this.mutationRate = mutationRate;
    }

    // 複数Agentの次世代を作成する例
    public Agent[] makeNextGeneration(Agent[] currentPopulation) {
        int populationSize = currentPopulation.length;
        Agent[] nextGeneration = new Agent[populationSize];

        for (int i = 0; i < populationSize; i++) {
            // 親2人を選ぶ
            Agent mother = selectParent(currentPopulation);
            Agent father = selectParent(currentPopulation);

            // 子Agentを母のcloneや新規生成で用意（clone推奨）
            Agent child = mother.clone();

            // 4つの遺伝子すべてについて交叉
            for (int geneIndex = 0; geneIndex < 4; geneIndex++) {
                crossoverGene(mother, father, child, geneIndex);
            }

            // 4つの遺伝子すべてについて突然変異
            for (int geneIndex = 0; geneIndex < 4; geneIndex++) {
                mutateGene(child, geneIndex, mutationRate);
            }

            nextGeneration[i] = child;
        }

        return nextGeneration;
    }
}
