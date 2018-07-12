package org.umair.research.thesis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.umair.research.thesis.models.WordFrequency;

public class Application {
	

	public static void main(String[] args) {
		
		try {
			ArrayList<String> fileNames = DocumentService.getFileNamesFromDocumentRepo(Constants.DATA_FOLDER, 0);
			
			Collections.shuffle(fileNames);
			
			ArrayList<List<String>> instances = new ArrayList<List<String>>();
			instances.add(fileNames.subList(0, 10));
			/*instances.add(fileNames.subList(0, 200));
			instances.add(fileNames.subList(0, 400));*/
			//instances.add(fileNames);
			
			for(List<String> instance : instances) {
				HashMap<String, String> documentContentMap = DocumentService.readDocumentContent(new ArrayList<String>(instance),
						Constants.DATA_FOLDER);

				HashMap<String, ArrayList<String>> documentSentencesListMap = DocumentService
						.getSentencesFromDocumentContentMap(documentContentMap);

				HashMap<String, ArrayList<WordFrequency>> documentWordFrequencies = DocumentService
						.getDocumentWordFrequencies(documentSentencesListMap);
				
				
				//DocumentService.printWordFrequencyMap(documentWordFrequencies);
				
				HashMap<String, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> documentWiseGraphs = DocumentService
						.createGraphForDocuments(documentWordFrequencies, documentSentencesListMap);
				
				DocumentService.exportCreatedGraphs(documentWiseGraphs);
				
				//documentContentMap = null;
				//documentSentencesListMap = null;
				
				DocumentService.createSimilarityMatrix(documentWordFrequencies, documentWiseGraphs);

			}
						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
