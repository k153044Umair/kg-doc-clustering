package org.umair;

import java.util.ArrayList;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.umair.models.GraphStatistics;
import org.umair.models.WordFrequency;

import edu.cmu.lti.ws4j.RelatednessCalculator;

public class GraphStatisticCalculator2 implements Runnable {
	
	ArrayList<DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> graphs = null;
	ArrayList<String> names;
	double minSimilarity;
	RelatednessCalculator rc;
	
	public GraphStatisticCalculator2(ArrayList<DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> graphs,
			ArrayList<String> names, double minSimilarity, RelatednessCalculator rc) {
		this.graphs = graphs;
		this.names = names;
		this.minSimilarity = minSimilarity;
		this.rc = rc;
	}

	@Override
	public void run() {
		System.out.println("Calculating similarity in: " + Thread.currentThread().getName());
		
		double[][] similarityMatrix = new double[45][45];
		for(int i = 0; i < graphs.size(); i++) {
			for(int j = i; j < graphs.size(); j++) {
				if(i == j) {
					similarityMatrix[i][j] = 1;
					continue;
				}
				
				int commonNodesCount = 0;
				int commonEdgesCount = 0;
				//Traverse vertex set of graph that has less number of vertices
				Set<WordFrequency> vertexSetOfSmallerGraph = null;
				Set<WordFrequency> vertexSetOfLargerGraph = null;
				if(this.graphs.get(i).vertexSet().size() > this.graphs.get(j).vertexSet().size()) {
					vertexSetOfSmallerGraph = this.graphs.get(j).vertexSet();
					vertexSetOfLargerGraph = this.graphs.get(i).vertexSet();
				} else {
					vertexSetOfSmallerGraph = this.graphs.get(i).vertexSet();
					vertexSetOfLargerGraph =  this.graphs.get(j).vertexSet();
				}double[] MIN_SIMILARITY_VALUE = {0.2, 0.5, 1.0};
				
				for (WordFrequency wordFrequency : vertexSetOfSmallerGraph) {
					String word1 = wordFrequency.getWord();
					for (WordFrequency wordFrequency2 : vertexSetOfLargerGraph) {
						String word2 = wordFrequency2.getWord();
						//System.out.println("Calculating similarity for: " + word1 +  " : " + word2);
						double similarity = rc.calcRelatednessOfWords(word1, word2);
						if(similarity > minSimilarity) {
							commonNodesCount++;
						}
					}
				}
				
				//Traverse edge set of graph that has less number of vertices
				Set<DefaultWeightedEdge> edgeSetOfSmallerGraph = null;
				Set<DefaultWeightedEdge> edgeSetOfLargerGraph = null;
				DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge> smallerGraph = null;
				DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge> largerGraph = null;
				if(this.graphs.get(i).edgeSet().size() > this.graphs.get(j).edgeSet().size()) {
					edgeSetOfSmallerGraph = this.graphs.get(j).edgeSet();
					edgeSetOfLargerGraph = this.graphs.get(i).edgeSet();
					smallerGraph = this.graphs.get(j);
					largerGraph = this.graphs.get(i);
				} else {
					edgeSetOfSmallerGraph = this.graphs.get(i).edgeSet();
					edgeSetOfLargerGraph =  this.graphs.get(j).edgeSet();
					smallerGraph = this.graphs.get(i);
					largerGraph = this.graphs.get(j);
				}
				
				for (DefaultWeightedEdge edgeSmall : edgeSetOfSmallerGraph) {
					String sourceWordSmall = smallerGraph.getEdgeSource(edgeSmall).getWord();
		            String targetWordSmall = smallerGraph.getEdgeTarget(edgeSmall).getWord();
					for (DefaultWeightedEdge edgeLarge : edgeSetOfLargerGraph) {
						String sourceWordLarge = largerGraph.getEdgeSource(edgeLarge).getWord();
			            String targetWordLarge = largerGraph.getEdgeTarget(edgeLarge).getWord();
			            //System.out.println("Calculating similarity for: " + sourceWordSmall +  " : " + sourceWordLarge);
						double similarityForSource = rc.calcRelatednessOfWords(sourceWordSmall, sourceWordLarge);
						//System.out.println("Calculating similarity for: " + targetWordSmall +  " : " + targetWordLarge);
						double similarityForTarget = rc.calcRelatednessOfWords(targetWordSmall, targetWordLarge);
						if(similarityForSource > minSimilarity && similarityForTarget > minSimilarity) {
							commonEdgesCount++;
						}
					}
				}
				
				double similarity = (0.5 * ((double)commonNodesCount/vertexSetOfLargerGraph.size())) + (0.5 * ((double)commonEdgesCount/edgeSetOfLargerGraph.size()));
				System.out.println("similarity: " + similarity);
				similarityMatrix[i][j] = similarity;
				similarityMatrix[j][i] = similarity;
			}
		}
		
		
		GraphStatisticsService.exportCalculatedGraphValues(rc.getClass().getSimpleName() + "-" + minSimilarity, new GraphStatistics(names, similarityMatrix));
	}

}
