package optim;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import com.gurobi.gurobi.GRBLinExpr;
import com.gurobi.gurobi.GRBModel;
import com.gurobi.gurobi.GRBQuadExpr;
import com.gurobi.gurobi.GRBVar;

import agent.Agent;
import network.*;

public class AdminOptim {
    private double lambda;
    private double gamma;
    private boolean reducePls;
    private boolean existing;

    public AdminOptim(double lambda, double gamma, boolean reducePls, boolean existing) {
        this.lambda = lambda;
        this.gamma = gamma;
        this.reducePls = reducePls;
        this.existing = existing;
    }

    public double[][] AdminFeedback(Agent[] agentSet, Network network, int agentNum) throws GRBException {

        int n = agentNum;
        System.out.println("the number of n: " + n);

        double[] opinion = new double[n];
        for (int i = 0; i < n; i++) {
            opinion[i] = agentSet[i].getOpinion();
        }
        double[][] W0 = network.getAdjacencyMatrix();

        GRBEnv env = new GRBEnv("minW_integer.log");
        env.set(GRB.IntParam.OutputFlag, 0);
        GRBModel model = new GRBModel(env);

        GRBVar[][] x = new GRBVar[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    x[i][j] = model.addVar(0.0, 1.0, 0.0, GRB.CONTINUOUS, "x_" + i + "_" + j);
                }
            }
        }

        // 目的関数：∑(zi - zj)^2 * x[i][j]
        GRBLinExpr objExpr = new GRBLinExpr();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    double diffSq = (opinion[i] - opinion[j]) * (opinion[i] - opinion[j]);
                    objExpr.addTerm(diffSq, x[i][j]);
                }
            }
        }
        model.setObjective(objExpr, GRB.MINIMIZE);

        // 制約：各ノードの次数 <= degreeLimit（上限設定）
        /*
         * int degreeLimit = 10; // 適宜変更
         * for (int i = 0; i < n; i++) {
         * GRBLinExpr degreeExpr = new GRBLinExpr();
         * for (int j = 0; j < n; j++) {
         * if (i != j) {
         * degreeExpr.addTerm(1.0, x[i][j]);
         * }
         * }
         * model.addConstr(degreeExpr, GRB.LESS_EQUAL, degreeLimit, "deg_limit_" + i);
         * }
         */

        // Add constraints sum_j x[i,j] = di : the degree of each vertex should not
        // change
        // calculate d[i] d[i]にはノードiのinitialの度数が入る
        double[] d = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    d[i] += W0[i][j]; // W0の行ごとの総和を計算
                }
            }
            // System.out.println("d[i]" + d[i]);
        }

        // Add constraint
        for (int i = 0; i < n; i++) {// 各ノードiに対して
            GRBLinExpr expr = new GRBLinExpr();

            for (int j = 0; j < n; j++) {
                if (i != j) {
                    expr.addTerm(1.0, x[i][j]);
                }
            }

            // 制約: sum_j x[i,j] = d[i]
            model.addConstr(expr, GRB.EQUAL, d[i], "c_" + i);
        }

        // Add constraint
        for (int i = 0; i < n; i++) {// 各ノードiに対して

            for (int j = 0; j < n; j++) {
                if (i != j && W0[i][j] == 0.0) {
                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addTerm(1.0, x[i][j]);
                    model.addConstr(expr, GRB.EQUAL, 0.0, "c_" + i + "_" + j);
                }
            }
        }

        if (this.lambda > 0) {
            double normW0Sq = 0.0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j)
                        normW0Sq += W0[i][j] * W0[i][j];
                }
            }

            GRBQuadExpr diffExpr = new GRBQuadExpr();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        if (existing && W0[i][j] > 0) {
                            diffExpr.addTerm(1.0, x[i][j], x[i][j]);
                            diffExpr.addTerm(-2.0 * W0[i][j], x[i][j]);
                            diffExpr.addConstant(W0[i][j] * W0[i][j]);
                        } else if (!existing) {
                            diffExpr.addTerm(1.0, x[i][j], x[i][j]);
                            if (W0[i][j] > 0) {
                                diffExpr.addTerm(-2.0 * W0[i][j], x[i][j]);
                                diffExpr.addConstant(W0[i][j] * W0[i][j]);
                            }
                        }
                    }
                }
            }

            double rhs = lambda * lambda * normW0Sq;
            model.addQConstr(diffExpr, GRB.LESS_EQUAL, rhs, "diff_norm_constraint");
        }

        /*
         * for (int i = 0; i < n; i++) {
         * GRBQuadExpr diffExpr = new GRBQuadExpr();
         * for (int j = 0; j < n; j++) {
         * if (i != j) {
         * diffExpr.addTerm(1.0, x[i][j], x[i][j]);
         * diffExpr.addTerm(-2.0 * W0[i][j], x[i][j]);
         * diffExpr.addConstant(W0[i][j] * W0[i][j]);
         * }
         * }
         * model.addQConstr(diffExpr, GRB.LESS_EQUAL, 0.01, null);
         * }
         */

        // 最適化
        model.optimize();
        if (model.get(GRB.IntAttr.Status) != GRB.Status.OPTIMAL) {
            throw new GRBException("Optimization failed. Status: " + model.get(GRB.IntAttr.Status));
        }

        // 結果取り出し
        double[][] W = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    W[i][j] = x[i][j].get(GRB.DoubleAttr.X);
                    /*if(W[i][j] < 0.01){
                        W[i][j] = 0.0;
                    }*/
                }
            }
        }

        // 各行の和で割って正規化
        for (int i = 0; i < n; i++) {
            double rowSum = 0.0;
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    rowSum += W[i][j];
                }
            }
            if (rowSum > 0) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        W[i][j] /= rowSum;
                    }
                }
            }
        }

        model.dispose();
        env.dispose();

        return W;
    }

}
