package org.umair;

import java.util.ArrayList;
import java.util.HashMap;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.umair.models.WordFrequency;

public class Application {
	

	public static void main(String[] args) {
		
		//Setup: Part 0
		
		try {
			ArrayList<String> fileNames = DocumentService.getFileNamesFromDocumentRepo(Constants.DATA_FOLDER, 0);
			
			HashMap<String, String> documentContentMap = DocumentService.readDocumentContent(fileNames, Constants.DATA_FOLDER);
			
			HashMap<String, ArrayList<String>> documentSentencesListMap = DocumentService.getSentencesFromDocumentContentMap(documentContentMap);
			
			HashMap<String, ArrayList<WordFrequency>> documentWordFrequencies = DocumentService.getDocumentWordFrequencies(documentSentencesListMap);
			
			//DocumentService.printWordFrequencyMap(documentWordFrequencies);
			
			HashMap<String, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> documentWiseGraphs = DocumentService.createGraphForDocuments(documentWordFrequencies, documentSentencesListMap);
			
			DocumentService.exportCreatedGraphs(documentWiseGraphs);
			
			documentContentMap = null;
			documentSentencesListMap = null;
			
			DocumentService.createSimilarityMatrix(documentWordFrequencies, documentWiseGraphs);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Part A
		// readDocumentAndGetSentences();
		
		// readSentencesAndGetWordsWithFrequncy(Senteces);
		
		// traverseWordsAndSentencesAndCreateGraph();
	}
}
