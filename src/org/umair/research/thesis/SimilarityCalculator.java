package org.umair.research.thesis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class SimilarityCalculator {
	
	public static void main(String[] args) {
		
		String resultsFolder = "results/graph";
		
		
		try {
			//Read nodes files first
			ArrayList<String> fileNames = DocumentService.getFileNamesFromDocumentRepo(Constants.DATA_FOLDER, 1);
			HashMap<String, String> documentContentMap = DocumentService.readDocumentContent(fileNames, resultsFolder);
			Set<Entry<String, String>> entrySet = documentContentMap.entrySet();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
