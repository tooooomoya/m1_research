package gephi;

import agent.Agent;
import network.Network;
import org.gephi.graph.api.*;
import org.gephi.project.api.ProjectController;
import org.gephi.io.exporter.api.ExportController;
import org.openide.util.Lookup;

import java.io.File;
import java.util.List;
import java.util.Map;

public class GraphVisualize {

    private double lambda;
    private GraphModel graphModel;
    private Graph graph;
    private ExportController exportController;

    // constructor
    public GraphVisualize(double lambda, Agent[] agents, Network network) {
        this.lambda = lambda;

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        graph = graphModel.getDirectedGraph();
        exportController = Lookup.getDefault().lookup(ExportController.class);

        initializeGraph(agents, network);
    }

    private void initializeGraph(Agent[] agents, Network network) {
        int nodeCount = agents.length;
        double[][] W = network.getAdjacencyMatrix();

        Column opinionColumn = graphModel.getNodeTable().getColumn("opinion");
        if (opinionColumn == null) {
            graphModel.getNodeTable().addColumn("opinion", Double.class);
        }

        Column communityColumn = graphModel.getNodeTable().getColumn("community");
        if (communityColumn == null) {
            graphModel.getNodeTable().addColumn("community", Integer.class);
        }

        Column opinionClass  = graphModel.getNodeTable().getColumn("opinionClass");
        if(opinionClass == null){
            graphModel.getNodeTable().addColumn("opinionClass", Integer.class);
        }

        Column boundedConfidence = graphModel.getNodeTable().getColumn("boundedConfidence");
        if(boundedConfidence == null){
            graphModel.getNodeTable().addColumn("boundedConfidence", Double.class);
        }

        Column postProb = graphModel.getNodeTable().getColumn("postProb");
        if(postProb == null){
            graphModel.getNodeTable().addColumn("postProb", Double.class);
        }

        for (int i = 0; i < nodeCount; i++) {
            Node node = graphModel.factory().newNode(String.valueOf(i));
            node.setLabel("Node " + i);
            node.setAttribute("opinion", agents[i].getOpinion());
            node.setAttribute("community", -1);
            node.setAttribute("opinionClass", agents[i].getOpinionClass());
            node.setAttribute("boundedConfidence", agents[i].getBc());
            node.setAttribute("postProb", agents[i].getPostProb());
            graph.addNode(node);
        }

        for (int i = 0; i < W.length; i++) {
            for (int j = 0; j < W[i].length; j++) {
                if (W[i][j] > 0) {
                    Edge edge = graphModel.factory().newEdge(
                            graph.getNode(String.valueOf(i)),
                            graph.getNode(String.valueOf(j)),
                            true
                    );
                    edge.setWeight(W[i][j]);
                    graph.addEdge(edge);
                }
            }
        }
    }

    public void assignCommunities(Map<Integer, List<Integer>> communityGroups) {
        Column communityColumn = graphModel.getNodeTable().getColumn("community");
        if (communityColumn == null) {
            graphModel.getNodeTable().addColumn("community", Integer.class);
        }

        for (Map.Entry<Integer, List<Integer>> entry : communityGroups.entrySet()) {
            int communityId = entry.getKey();
            List<Integer> nodes = entry.getValue();

            for (int nodeId : nodes) {
                Node node = graph.getNode(String.valueOf(nodeId));
                if (node != null) {
                    node.setAttribute("community", communityId);
                }
            }
        }
    }

    public void updateGraph(Agent[] agents, Network network) {
        double[][] newW = network.getAdjacencyMatrix();

        for (int i = 0; i < agents.length; i++) {
            Node node = graph.getNode(String.valueOf(i));
            if (node != null) {
                node.setAttribute("opinion", agents[i].getOpinion());
                node.setAttribute("opinionClass", agents[i].getOpinionClass());
                node.setAttribute("boundedConfidence", agents[i].getBc());
                node.setAttribute("postProb", agents[i].getPostProb());
            }
        }

        for (int i = 0; i < newW.length; i++) {
            for (int j = 0; j < newW[i].length; j++) {
                Node source = graph.getNode(String.valueOf(i));
                Node target = graph.getNode(String.valueOf(j));
                Edge edge = graph.getEdge(source, target);

                if (newW[i][j] == 0.0) {
                    if (edge != null) {
                        graph.removeEdge(edge);
                    }
                } else {
                    if (edge != null) {
                        edge.setWeight(newW[i][j]);
                    } else {
                        edge = graphModel.factory().newEdge(source, target, true);
                        edge.setWeight(newW[i][j]);
                        graph.addEdge(edge);
                    }
                }
            }
        }
    }

    public void exportGraph(int step, String folderPath) {
        try {
            String lambdaFolder = folderPath + "/GEXF/lambda_" + lambda;
            File lambdaDir = new File(lambdaFolder);
            if (!lambdaDir.exists()) {
                lambdaDir.mkdirs();
            }

            String fileName = lambdaFolder + "/step_" + step + ".gexf";
            File file = new File(fileName);

            exportController.exportFile(file);
            //System.out.println("Graph exported to " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
