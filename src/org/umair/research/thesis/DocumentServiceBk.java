package org.umair.research.thesis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.DOTUtils;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.umair.research.thesis.models.WordFrequency;
import org.umair.research.thesis.providers.VertexIdProvider;
import org.umair.research.thesis.providers.VertexLabelProvider;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.Word;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class DocumentServiceBk {
	
	public static ArrayList<String> getFileNamesFromDocumentRepo(String folderName) {
		ArrayList<String> files = new ArrayList<String>();
    	
    	File folder = new File(folderName);
    	File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	        System.out.println("File " + listOfFiles[i].getName());
	        String name = listOfFiles[i].getName();
	        if(!StringUtils.contains(name, "stopword")) {
	        	files.add(name);
	        }
	      }
	    }
	    
	    return files;
	}
	
	public static HashMap<String, String> readDocumentContent(ArrayList<String> fileNames, String rootFolderName) throws IOException {
		HashMap<String, String> documentsContentMap = new HashMap<String, String>();
		
		BufferedReader _file = null;
		
		if(fileNames != null && fileNames.size() > 0) {
			for (String fileName : fileNames) {
				String fullName = rootFolderName + "/" + fileName;
				_file = new BufferedReader(new FileReader(fullName));
				String buffer = _file.readLine();
				StringBuilder sb = new StringBuilder();
				while (buffer != null) {
					sb.append(buffer).append("\n");
					buffer = _file.readLine();
				}
				documentsContentMap.put(FilenameUtils.removeExtension(fileName), sb.toString());
			}
		}
		
		return documentsContentMap;
	}
	
	
	public static HashMap<String, ArrayList<String>> getSentencesFromDocumentContentMap(HashMap<String, String> documentContentMap) {

		HashMap<String, ArrayList<String>> documentWiseSentencesList = new HashMap<String, ArrayList<String>>();
		
		Set<Entry<String, String>> entrySet = documentContentMap.entrySet();
		
		Iterator<Entry<String, String>> iterator = entrySet.iterator();
		
		while (iterator.hasNext()) {
			Map.Entry<java.lang.String, java.lang.String> entry = (Map.Entry<java.lang.String, java.lang.String>) iterator
					.next();
			documentWiseSentencesList.put(entry.getKey(), getSentencesList(entry.getValue()));
		}
		
		return documentWiseSentencesList;
	}
	
	
	public static ArrayList<String> getSentencesList(String content) {
		ArrayList<String> sentencesList = new ArrayList<String>();

		try {

			// always start with a model, a model is learned from training data
			InputStream is = new FileInputStream("models/en-sent.bin");
			SentenceModel model = new SentenceModel(is);
			SentenceDetectorME sdetector = new SentenceDetectorME(model);

			String[] sentences = sdetector.sentDetect(content);

			sentencesList = new ArrayList((List) Arrays.asList(sentences));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return sentencesList;

	}
	
	public static HashMap<String, ArrayList<WordFrequency>> getDocumentWordFrequencies(HashMap<String, ArrayList<String>> documentSentencesListMap) throws Exception {
		HashMap<String, ArrayList<WordFrequency>> documentWordFrequenciesMap = new HashMap<String, ArrayList<WordFrequency>>();
		DocumentReader documentReader = new DocumentReader(); 
		
		Set<Entry<String,ArrayList<String>>> entrySet = documentSentencesListMap.entrySet();
		
		Iterator<Entry<String, ArrayList<String>>> iterator = entrySet.iterator();
		
		while (iterator.hasNext()) {
			Map.Entry<java.lang.String, java.util.ArrayList<java.lang.String>> entry = (Map.Entry<java.lang.String, java.util.ArrayList<java.lang.String>>) iterator
					.next();
			documentWordFrequenciesMap.put(entry.getKey(), documentReader.getWordFrequencies(entry.getValue()));
		}
		
		return documentWordFrequenciesMap;
	}

	
	public static void printWordFrequencyMap(HashMap<String, ArrayList<WordFrequency>> documentWordFrequencies) {
		Set<Entry<String, ArrayList<WordFrequency>>> entrySet = documentWordFrequencies.entrySet();
		
		Iterator<Entry<String, ArrayList<WordFrequency>>> iterator = entrySet.iterator();
		String processedFolderName = "results/processed";
		new File(processedFolderName).mkdirs();
		String statisticsFolderName = "results/word_statistics";
		new File(statisticsFolderName).mkdirs();
		
		while (iterator.hasNext()) {
			Map.Entry<java.lang.String, java.util.ArrayList<org.umair.research.thesis.models.WordFrequency>> entry = (Map.Entry<java.lang.String, java.util.ArrayList<org.umair.research.thesis.models.WordFrequency>>) iterator
					.next();
			System.out.println("Document Name:" + entry.getKey());
			String processedFilePath = processedFolderName + "/" + entry.getKey() + ".txt";
			String statisticsFilePath = statisticsFolderName + "/" + entry.getKey() + ".txt";
			
			try{
				PrintWriter processedFileWriter = new PrintWriter(new FileWriter(processedFilePath));
				PrintWriter statisticsFileWriter = new PrintWriter(new FileWriter(statisticsFilePath));
				for (WordFrequency wordFrequency : entry.getValue()) {
					System.out.println("Word: " + wordFrequency.getWord());
					System.out.println("Frequency: " + wordFrequency.getFrequency());
					System.out.println("Word Occurances : " + wordFrequency.getOccurances());
					System.out.println("Sentence Numbers: " + wordFrequency.getPosTag());
				    processedFileWriter.println(wordFrequency.getWord());
				    statisticsFileWriter.println("Word: " + wordFrequency.getWord());
				    statisticsFileWriter.println("Frequency: " + wordFrequency.getFrequency());
				    statisticsFileWriter.println("Word Occurances : " + wordFrequency.getOccurances());
				    statisticsFileWriter.println("Sentence Numbers: " + wordFrequency.getPosTag());
				    statisticsFileWriter.println("------------------");
				}
				processedFileWriter.close();
				statisticsFileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static HashMap<String, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> createGraphForDocuments(HashMap<String, ArrayList<WordFrequency>> documentWordFrequencies, HashMap<String, ArrayList<String>> documentSentencesListMap) {
		
		HashMap<String, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> documentWiseGraphs = new HashMap<String, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>>();
		Set<Entry<String, ArrayList<WordFrequency>>> entrySet = documentWordFrequencies.entrySet();
		
		Iterator<Entry<String, ArrayList<WordFrequency>>> iterator = entrySet.iterator();
		
		while (iterator.hasNext()) {
			Map.Entry<java.lang.String, java.util.ArrayList<org.umair.research.thesis.models.WordFrequency>> entry = (Map.Entry<java.lang.String, java.util.ArrayList<org.umair.research.thesis.models.WordFrequency>>) iterator
					.next();
			System.out.println("Document Name:" + entry.getKey());
			ArrayList<String> documentSentences = documentSentencesListMap.get(entry.getKey());
			
			DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>  graph = 
					new DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>
			(DefaultWeightedEdge.class);
			
			ArrayList<WordFrequency> values = entry.getValue();
			
			for (int i = 0; i < values.size(); i++) {
				WordFrequency wordFrequency = values.get(i);
				if(!graph.containsVertex(wordFrequency)) {
					graph.addVertex(wordFrequency); //FIXME: Check if this is correct
				}
				for (int j = i; j < values.size(); j++) {
					WordFrequency wordFrequency2 = values.get(j);
					if(!wordFrequency.getWord().equals(wordFrequency2.getWord())) {
						ArrayList<Integer> occurances = new ArrayList<Integer>(wordFrequency.getOccurances());
						boolean retainAll = occurances.retainAll(wordFrequency2.getOccurances());
						if(retainAll && occurances.size() > 0) {
							double commonOccurances = 0;
							for(Integer sentenceNumber : occurances) {
								String sentence = documentSentences.get(sentenceNumber);
								if(sentence.indexOf(wordFrequency.getWord()) < sentence.indexOf(wordFrequency2.getWord())) {
									commonOccurances++;
								}
							}
							if(commonOccurances > 0) {
								if(!graph.containsVertex(wordFrequency2)) {
									graph.addVertex(wordFrequency2); //FIXME: Check if this is correct
								}
								DefaultWeightedEdge edge = graph.addEdge(wordFrequency, wordFrequency2);
								graph.setEdgeWeight(edge, commonOccurances);
								
							}
						}
					}
				}
			}
			documentWiseGraphs.put(entry.getKey(), graph);
		}
		
		return documentWiseGraphs;
	}

	public static void createSimilarityMatrix(HashMap<String, ArrayList<WordFrequency>> documentWordFrequencies,
			HashMap<String, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> documentWiseGraphs) throws IOException {
		
		HashMap<String, Double[][]> documentWiseSimilarityMatrix = new HashMap<String, Double[][]>();

		Set<Entry<String,ArrayList<WordFrequency>>> entrySet = documentWordFrequencies.entrySet();
		
		Iterator<Entry<String, ArrayList<WordFrequency>>> iterator = entrySet.iterator();
		
		String vers = "3.0";
		String wnhome = System.getenv("WNHOME") + "/dict";
		String icfile = System.getenv("WNHOME") + "/WordNet-InfoContent-" + vers + "/ic-semcor.dat";
		URL url = null;
		try {
			url = new URL("file", null, wnhome);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (url == null)
			return;
		IDictionary dict = new Dictionary(url);
		dict.open();
		ICFinder icfinder = new ICFinder(icfile);
		DepthFinder depthFinder = new DepthFinder(dict, icfile);
		
		while (iterator.hasNext()) {
			Map.Entry<java.lang.String, java.util.ArrayList<org.umair.research.thesis.models.WordFrequency>> entry = (Map.Entry<java.lang.String, java.util.ArrayList<org.umair.research.thesis.models.WordFrequency>>) iterator
					.next();
			
			System.out.println("Document Name: " + entry.getKey());
			DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge> graph = documentWiseGraphs.get(entry.getKey());
			
			Set<WordFrequency> vertexSet = graph.vertexSet();
			ArrayList<WordFrequency> words = new ArrayList<WordFrequency>(vertexSet);
			Double[][] similarityMatrix = new Double[words.size()][words.size()];
			/*DepthFirstIterator<WordFrequency, DefaultWeightedEdge> depthFirstIterator = new DepthFirstIterator<>(graph);
			WordFrequency next = depthFirstIterator.next();
			*/
			
			for(int i = 0; i < words.size(); i++) {
				WordFrequency wordI = words.get(i);
				for(int j = 0; j < words.size(); j++) {
					if(i == j) {
						similarityMatrix[i][j] = 1.0;
						continue;
					} else {
						//Calculating Array
						GraphPath<WordFrequency, DefaultWeightedEdge> shortestPath = DijkstraShortestPath.findPathBetween(graph, words.get(i), words.get(j));
						if(shortestPath != null) {
							double distanceIJ = shortestPath.getWeight();
							double dVi =   depthFinder.getSynsetDepth(words.get(i).getWord(), 1, getConvertedPOSTag(words.get(i).getPosTag()));
							double dVj = depthFinder.getSynsetDepth(words.get(j).getWord(), 1, getConvertedPOSTag(words.get(j).getPosTag()));
							int nViVj = shortestPath.getLength();
							int H = depthFinder.getSynsetMaximumDepth(0, getConvertedPOSTag(words.get(i).getPosTag()));
							
							double rViVj = ((distanceIJ + Constants.BETA) * Constants.BETA * (dVi + dVj))/ (nViVj * 2 * H * Math.max(dVi - dVj, 1));
							
							if(rViVj > Constants.GAMMA) {
								similarityMatrix[i][j] = rViVj;
							} else {
								similarityMatrix[i][j] = 0.0;
							}
						} else {
							similarityMatrix[i][j] = 0.0;
						}
						
					}
				}
			}
			System.out.println(PrettyPrinter.print(similarityMatrix, new Printer<Double>() {
		        @Override
		        public String print(Double obj) {
		            return obj.toString();
		        }
		    }));
			documentWiseSimilarityMatrix.put(entry.getKey(), similarityMatrix);
		}
		
	}

	public static String getConvertedPOSTag(String posTag) {
		if(StringUtils.equals(posTag, "NN") || StringUtils.equals(posTag, "NNP")) {
			return "n";
		} else if(StringUtils.equals(posTag, "VB") || StringUtils.equals(posTag, "VBD") || StringUtils.equals(posTag, "VBZ")) {
			return "v";
		} else {
			return "e";
		}
	}

	public static void exportCreatedGraphs(
			HashMap<String, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> documentWiseGraphs) throws IOException {
		
		Set<Entry<String,DirectedWeightedMultigraph<WordFrequency,DefaultWeightedEdge>>> entrySet = documentWiseGraphs.entrySet();
		
		Iterator<Entry<String, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>>> iterator = entrySet.iterator();
		
		while (iterator.hasNext()) {
			Map.Entry<java.lang.String, org.jgrapht.graph.DirectedWeightedMultigraph<org.umair.research.thesis.models.WordFrequency, org.jgrapht.graph.DefaultWeightedEdge>> entry = (Map.Entry<java.lang.String, org.jgrapht.graph.DirectedWeightedMultigraph<org.umair.research.thesis.models.WordFrequency, org.jgrapht.graph.DefaultWeightedEdge>>) iterator
					.next();
			ComponentAttributeProvider<DefaultWeightedEdge> p4 =
					new ComponentAttributeProvider<DefaultWeightedEdge>() {
				public Map<String, String> getComponentAttributes(DefaultWeightedEdge e) {
					Map<String, String> map =new LinkedHashMap<String, String>();
					map.put("label", String.valueOf((int)entry.getValue().getEdgeWeight(e)));
					return map;
				}
			};
			
			DOTExporter<org.umair.research.thesis.models.WordFrequency, org.jgrapht.graph.DefaultWeightedEdge> exporter = new DOTExporter<org.umair.research.thesis.models.WordFrequency, org.jgrapht.graph.DefaultWeightedEdge>(new VertexIdProvider() , new VertexLabelProvider(),
					null, null, p4);
			String targetDirectory = "results/graph/";
			new File(targetDirectory).mkdirs();
			exporter.exportGraph(entry.getValue(), new FileWriter(targetDirectory + entry.getKey() + "-g.dot"));
			exportVertices(entry.getKey(), entry.getValue(), new FileWriter(targetDirectory + entry.getKey() + "-v.dot"));
			exportEdges(entry.getKey(), entry.getValue(), new FileWriter(targetDirectory + entry.getKey() + "-e.dot"));
		}
	}

	private static void exportEdges(String key, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge> graph,
			FileWriter fileWriter) {
		PrintWriter out = new PrintWriter(fileWriter);
		for (WordFrequency v : graph.vertexSet()) {
            out.println(v.getWord() + " (" + v.getFrequency() + ")");
        }
		out.flush();
	}

	private static void exportVertices(String key, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge> graph,
			FileWriter fileWriter) {
		PrintWriter out = new PrintWriter(fileWriter);
		
		for (DefaultWeightedEdge e : graph.edgeSet()) {
            String source = graph.getEdgeSource(e).getWord();
            String target = graph.getEdgeTarget(e).getWord();

            out.print(source + "->" + target);
        }
		out.flush();
	}
}
