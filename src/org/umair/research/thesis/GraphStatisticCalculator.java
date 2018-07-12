package org.umair.research.thesis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.umair.research.thesis.models.GraphStatistics;
import org.umair.research.thesis.models.WordFrequency;

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
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.habernal.confusionmatrix.ConfusionMatrix;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;

public class GraphStatisticCalculator implements Runnable {

	ArrayList<DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> graphs = null;
	ArrayList<String> names;
	double minSimilarity;
	RelatednessCalculator rc;
	public static Map<String, Integer> commonNodesCountMap;
	public static Map<String, Integer> commonEdgesCountMap;
	public static Map<String, Double> graphWiseSimilarity;

	public GraphStatisticCalculator(ArrayList<DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> graphs,
			ArrayList<String> names, double minSimilarity, RelatednessCalculator rc) {
		this.graphs = graphs;
		this.names = names;
		this.minSimilarity = minSimilarity;
		this.rc = rc;
	}

	@Override
	public void run() {
		System.out.println("Calculating similarity in: " + Thread.currentThread().getName());
		commonNodesCountMap = new HashMap<String, Integer>();
		commonEdgesCountMap = new HashMap<String, Integer>();
		graphWiseSimilarity = new HashMap<String, Double>();
		long startTime = System.currentTimeMillis();
		int MAX_SIZE = graphs.size();
		double[][] similarityMatrix = new double[MAX_SIZE][MAX_SIZE];
		int i = 0;
		int j = 0;
		ILexicalDatabase db = new NictWordNet();
		RelatednessCalculator[] rcs = { new LeacockChodorow(db), new Lesk(db), new WuPalmer(db), new Resnik(db),
				new JiangConrath(db), new Lin(db), new Path(db) };

		ExecutorService executor = Executors.newFixedThreadPool(100);

		for (i = 0; i < graphs.size(); i++) {
			for (j = i; j < graphs.size(); j++) {
				/*
				 * if(i==0) { similarityMatrix[i][j] =
				 * Double.valueOf(names.get(j)); continue; }
				 */
				if (i == j) {
					similarityMatrix[i][j] = 1;
					continue;
				}
				System.out.println("Calculating similarity for: " + names.get(i) + " - " + names.get(j));
				int commonNodesCount = 0;
				int commonEdgesCount = 0;
				// Traverse vertex set of graph that has less number of vertices
				List<WordFrequency> vertexSetOfSmallerGraph = null;
				List<WordFrequency> vertexSetOfLargerGraph = null;
				if (this.graphs.get(i).vertexSet().size() > this.graphs.get(j).vertexSet().size()) {
					vertexSetOfSmallerGraph = new ArrayList<WordFrequency>(this.graphs.get(j).vertexSet());
					vertexSetOfLargerGraph = new ArrayList<WordFrequency>(this.graphs.get(i).vertexSet());
				} else {
					vertexSetOfSmallerGraph = new ArrayList<WordFrequency>(this.graphs.get(i).vertexSet());
					vertexSetOfLargerGraph = new ArrayList<WordFrequency>(this.graphs.get(j).vertexSet());
				}
				
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

				int ithIndex = i;
				int jthIndex = j;
				List<WordFrequency> tempVertexSetOfSmallerGraph = vertexSetOfSmallerGraph;
				List<WordFrequency> tempVertexSetOfLargerGraph = vertexSetOfLargerGraph;
				Runnable graphSimilarityCalculator = new GraphSimilarityCalculator(names.get(ithIndex),
						names.get(jthIndex), rcs, tempVertexSetOfSmallerGraph, tempVertexSetOfLargerGraph,
						minSimilarity, edgeSetOfSmallerGraph, edgeSetOfLargerGraph,smallerGraph, largerGraph);
				executor.execute(graphSimilarityCalculator);

				double similarity = (0.9 * ((double) commonNodesCount / vertexSetOfLargerGraph.size()));// +
																										// (0.5
																										// *
																										// ((double)commonEdgesCount/edgeSetOfLargerGraph.size()));

				similarityMatrix[i][j] = similarity;
				similarityMatrix[j][i] = similarity;
			}
		}

		executor.shutdown();
		// Wait until all threads are finish
		while (!executor.isTerminated()) {

		}
		System.out.println("\nFinished all threads");

		ObjectMapper mapper = new ObjectMapper();

		// write JSON to a file
		try {
			mapper.writeValue(new File("d:\\graphWiseSimilarity-" + new Date().getTime() + "-"
					+ Constants.DATA_FOLDER.split("/")[1] + ".json"), graphWiseSimilarity);
			mapper.writeValue(new File("d:\\commonNodesCountMap" + new Date().getTime() + "-"
					+ Constants.DATA_FOLDER.split("/")[1] + ".json"), commonNodesCountMap);
		} catch (JsonGenerationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String[] namesArr = new String[names.size()];
		for (int m = 0; m < names.size(); m++) {
			namesArr[m] = names.get(m);
		}

		try {
			draw(namesArr, similarityMatrix, rc.getClass().getSimpleName() + "-" + minSimilarity);
		} catch (Exception e) {
			e.printStackTrace();
		}
		GraphStatisticsService.exportCalculatedGraphValues(rc.getClass().getSimpleName() + "-" + minSimilarity,
				new GraphStatistics(names, similarityMatrix));

		long endTime = System.currentTimeMillis();
		NumberFormat formatter = new DecimalFormat("#0.00000");
		System.out.print("Execution time is " + formatter.format((endTime - startTime) / 1000d) + " seconds");
	}

	private static void draw(String[] names, double[][] distances, String fileName) throws Exception {
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

		/*
		 * ClusteringAlgorithm alg = new DefaultClusteringAlgorithm(); Cluster
		 * cluster = alg.performClustering(distances, names, new
		 * CompleteLinkageStrategy()); cluster.toConsole(5);
		 */

		/*
		 * final Array2DRowRealMatrix mat = new Array2DRowRealMatrix(distances);
		 * HierarchicalAgglomerative a = new
		 * HierarchicalAgglomerativeParameters(5).setLinkage().fitNewModel(mat);
		 * final int[] results = a.getLabels(); for (int i = 0; i <
		 * results.length; i++) { System.out.println(results[i]); }
		 */

		PrintWriter out;
		try {
			System.out.println("Creating Text file for: " + fileName);
			new File("results/cm/").mkdirs();
			out = new PrintWriter(new FileWriter("results/cm/" + fileName + ".txt"));
			out.println("-----------------------------------");
			final Array2DRowRealMatrix mat = new Array2DRowRealMatrix(distances);
			for (Linkage linkage : HierarchicalAgglomerative.Linkage.values()) {
				System.out.println("Linkage: " + linkage.name());
				out.println("Linkage: " + linkage.name());
				HierarchicalAgglomerative a = new HierarchicalAgglomerativeParameters(
						ClusterTest.getGroundTruthClusters().size()).setLinkage(linkage).fitNewModel(mat);
				final int[] results = a.getLabels();
				System.out.println(Arrays.toString(results));
				out.println(Arrays.toString(results));
				// System.out.println("Document Wise results");
				Map<String, ArrayList<String>> resultsMap = new HashMap<String, ArrayList<String>>();
				for (int i = 0; i < results.length; i++) {
					// System.out.print(names[i]+":"+results[i]+",");
					int clusterNumber = results[i] + 1;
					String documentId = names[i];
					String clusterId = "C" + clusterNumber;
					ArrayList<String> list = resultsMap.get(clusterId);
					if (list == null) {
						list = new ArrayList<String>();
					}
					list.add(documentId);
					resultsMap.put(clusterId, list);
				}
				// Group Clusters by cluster number

				ClusterTest.processConfusionMatrixAndPrintResults(resultsMap, out);

				System.out.println("Silhoutte Score: " + a.silhouetteScore());
				out.println("Silhoutte Score: " + a.silhouetteScore());
				System.out.println("------------------------");
				out.println("------------------------");
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * dp.setModel(cluster); frame.setVisible(true);
		 */
		/*
		 * ConfusionMatrix confusionMatrix = new ConfusionMatrix();
		 * 
		 * ConfusionMatrix cm = new ConfusionMatrix();
		 * 
		 * cm.increaseValue("neg", "neg", 25); cm.increaseValue("neg", "neu",
		 * 5); cm.increaseValue("neg", "pos", 2); cm.increaseValue("neu", "neg",
		 * 3); cm.increaseValue("neu", "neu", 32); cm.increaseValue("neu",
		 * "pos", 4); cm.increaseValue("pos", "neg", 1); cm.increaseValue("pos",
		 * "pos", 15);
		 * 
		 * System.out.println(cm); System.out.println(cm.printLabelPrecRecFm());
		 * System.out.println(cm.getPrecisionForLabels());
		 * System.out.println(cm.getRecallForLabels());
		 * System.out.println(cm.printNiceResults());
		 */
	}

}
