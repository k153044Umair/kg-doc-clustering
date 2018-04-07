package org.umair;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.umair.models.GraphStatistics;
import org.umair.models.WordFrequency;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.CompleteLinkageStrategy;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.SingleLinkageStrategy;
import com.apporiented.algorithm.clustering.visualization.DendrogramPanel;
import com.clust4j.algo.HierarchicalAgglomerative;
import com.clust4j.algo.HierarchicalAgglomerative.Linkage;
import com.clust4j.algo.HierarchicalAgglomerativeParameters;
import com.github.habernal.confusionmatrix.ConfusionMatrix;

import edu.cmu.lti.ws4j.RelatednessCalculator;

public class GraphStatisticCalculator implements Runnable {
	
	ArrayList<DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> graphs = null;
	ArrayList<String> names;
	double minSimilarity;
	RelatednessCalculator rc;
	
	public GraphStatisticCalculator(ArrayList<DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> graphs,
			ArrayList<String> names, double minSimilarity, RelatednessCalculator rc) {
		this.graphs = graphs;
		this.names = names;
		this.minSimilarity = minSimilarity;
		this.rc = rc;
	}

	@Override
	public void run() {
		//System.out.println("Calculating similarity in: " + Thread.currentThread().getName());
		
		int MAX_SIZE = graphs.size();
		double[][] similarityMatrix = new double[MAX_SIZE][MAX_SIZE];
		int i=0;
		int j=0;
		for(i = 0; i < graphs.size(); i++) {
			for(j = i; j < graphs.size(); j++) {
				/*if(i==0) {
					similarityMatrix[i][j] = Double.valueOf(names.get(j));
					continue;
				}*/
				if(i == j) {
					similarityMatrix[i][j] = 0;
					continue;
				}
				//System.out.println("Calculating similarity for: " + names.get(i) + " - " + names.get(j));
				int commonNodesCount = 0;
				int commonEdgesCount = 0;
				//Traverse vertex set of graph that has less number of vertices
				List<WordFrequency> vertexSetOfSmallerGraph = null;
				List<WordFrequency> vertexSetOfLargerGraph = null;
				if(this.graphs.get(i).vertexSet().size() > this.graphs.get(j).vertexSet().size()) {
					vertexSetOfSmallerGraph = new ArrayList<WordFrequency>(this.graphs.get(j).vertexSet());
					vertexSetOfLargerGraph = new ArrayList<WordFrequency>(this.graphs.get(i).vertexSet());
				} else {
					vertexSetOfSmallerGraph = new ArrayList<WordFrequency>(this.graphs.get(i).vertexSet());
					vertexSetOfLargerGraph =  new ArrayList<WordFrequency>(this.graphs.get(j).vertexSet());
				}
				
				for(int k = 0; k < vertexSetOfSmallerGraph.size(); k++) {
					String word1 = vertexSetOfSmallerGraph.get(k).getWord();
					String word2 = vertexSetOfLargerGraph.get(k).getWord();
					double similarity = rc.calcRelatednessOfWords(word1, word2);
					if(similarity > minSimilarity) {
						commonNodesCount++;
					}
				}
				
				//Traverse edge set of graph that has less number of vertices
				List<DefaultWeightedEdge> edgeSetOfSmallerGraph = null;
				List<DefaultWeightedEdge> edgeSetOfLargerGraph = null;
				DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge> smallerGraph = null;
				DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge> largerGraph = null;
				if(this.graphs.get(i).edgeSet().size() > this.graphs.get(j).edgeSet().size()) {
					edgeSetOfSmallerGraph = new ArrayList<DefaultWeightedEdge>(this.graphs.get(j).edgeSet());
					edgeSetOfLargerGraph = new ArrayList<DefaultWeightedEdge>(this.graphs.get(i).edgeSet());
					smallerGraph = this.graphs.get(j);
					largerGraph = this.graphs.get(i);
				} else {
					edgeSetOfSmallerGraph = new ArrayList<DefaultWeightedEdge>(this.graphs.get(i).edgeSet());
					edgeSetOfLargerGraph =  new ArrayList<DefaultWeightedEdge>(this.graphs.get(j).edgeSet());
					smallerGraph = this.graphs.get(i);
					largerGraph = this.graphs.get(j);
				}
				
				for (int l = 0; l < edgeSetOfSmallerGraph.size(); l++) {
					DefaultWeightedEdge edgeSmall = edgeSetOfSmallerGraph.get(l);
					DefaultWeightedEdge edgeLarge = edgeSetOfLargerGraph.get(l);
					String sourceWordSmall = smallerGraph.getEdgeSource(edgeSmall).getWord();
		            String targetWordSmall = smallerGraph.getEdgeTarget(edgeSmall).getWord();
		            String sourceWordLarge = largerGraph.getEdgeSource(edgeLarge).getWord();
		            String targetWordLarge = largerGraph.getEdgeTarget(edgeLarge).getWord();
		            double similarityForSource = rc.calcRelatednessOfWords(sourceWordSmall, sourceWordLarge);
					//System.out.println("Calculating similarity for: " + targetWordSmall +  " : " + targetWordLarge);
					double similarityForTarget = rc.calcRelatednessOfWords(targetWordSmall, targetWordLarge);
					if(similarityForSource > minSimilarity && similarityForTarget > minSimilarity) {
						commonEdgesCount++;
					}
		            
				}
				
				double similarity = (0.5 * ((double)commonNodesCount/vertexSetOfLargerGraph.size())) + (0.5 * ((double)commonEdgesCount/edgeSetOfLargerGraph.size()));
				System.out.println("similarity: " + similarity);
				similarityMatrix[i][j] = similarity;
				similarityMatrix[j][i] = similarity;
			}
		}
		
		String[] namesArr = new String[names.size()];
		for (int m = 0; m < names.size(); m++) {
			namesArr[m]= names.get(m);
		}
		
		try {
			draw(namesArr, similarityMatrix);
		} catch (Exception e) {
			e.printStackTrace();
		}
		GraphStatisticsService.exportCalculatedGraphValues(rc.getClass().getSimpleName() + "-" + minSimilarity, new GraphStatistics(names, similarityMatrix));
		
	}
	
	
	private static void draw(String[] names, double[][] distances) throws Exception {
        JFrame frame = new JFrame();
        frame.setSize(1024, 768);
        frame.setLocation(400, 300);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel content = new JPanel();
        DendrogramPanel dp = new DendrogramPanel();

        frame.setContentPane(content);
        content.setBackground(Color.red);
        content.setLayout(new BorderLayout());
        content.add(dp, BorderLayout.CENTER);
        dp.setBackground(Color.WHITE);
        dp.setLineColor(Color.BLACK);
        dp.setScaleValueDecimals(0);
        dp.setScaleValueInterval(1);
        dp.setShowDistances(false);
        
        /*ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
        Cluster cluster = alg.performClustering(distances, names,
                new CompleteLinkageStrategy());
        cluster.toConsole(5);*/
        
        /*final Array2DRowRealMatrix mat = new Array2DRowRealMatrix(distances);
        HierarchicalAgglomerative a = new HierarchicalAgglomerativeParameters(5).setLinkage().fitNewModel(mat);
        final int[] results = a.getLabels();
        for (int i = 0; i < results.length; i++) {
			System.out.println(results[i]);
		}*/
        
        final Array2DRowRealMatrix mat = new Array2DRowRealMatrix(distances);
        for(Linkage linkage: HierarchicalAgglomerative.Linkage.values()) {
        	System.out.println("Linkage: " + linkage.name());
        	HierarchicalAgglomerative a = new HierarchicalAgglomerativeParameters(5).setLinkage(linkage).fitNewModel(mat);
            final int[] results = a.getLabels();
            System.out.println(Arrays.toString(results));
            System.out.println("Document Wise results");
            Map<String, ArrayList<String>> resultsMap = new HashMap<String, ArrayList<String>>();
            for (int i = 0; i < results.length; i++) {
				//System.out.print(names[i]+":"+results[i]+",");
				int clusterNumber = results[i]+1;
				String documentId = names[i];
				String clusterId = "C"+clusterNumber;
				ArrayList<String> list = resultsMap.get(clusterId);
				if(list == null) {
					list = new ArrayList<String>();
				}
				list.add(documentId);
				resultsMap.put(clusterId, list);
			}
            //Group Clusters by cluster number
            
            ClusterTest.processConfusionMatrixAndPrintResults(resultsMap);
            
            System.out.println("Silhoutte Score: " + a.silhouetteScore());
            System.out.println("------------------------");
		}
        /*dp.setModel(cluster);
        frame.setVisible(true);*/
       /* ConfusionMatrix confusionMatrix = new ConfusionMatrix();
        
        ConfusionMatrix cm = new ConfusionMatrix();

        cm.increaseValue("neg", "neg", 25);
        cm.increaseValue("neg", "neu", 5);
        cm.increaseValue("neg", "pos", 2);
        cm.increaseValue("neu", "neg", 3);
        cm.increaseValue("neu", "neu", 32);
        cm.increaseValue("neu", "pos", 4);
        cm.increaseValue("pos", "neg", 1);
        cm.increaseValue("pos", "pos", 15);

        System.out.println(cm);
        System.out.println(cm.printLabelPrecRecFm());
        System.out.println(cm.getPrecisionForLabels());
        System.out.println(cm.getRecallForLabels());
        System.out.println(cm.printNiceResults());*/
    }

}
