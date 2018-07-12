package org.umair.research.thesis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.umair.research.thesis.models.GraphStatistics;
import org.umair.research.thesis.models.WordFrequency;
import org.umair.research.thesis.providers.VertexIdProvider;
import org.umair.research.thesis.providers.VertexLabelProvider;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.visualization.DendrogramPanel;

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
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class DocumentService {
	private static final int MYTHREADS = 30;
	private static final HashMap<Integer, String> indexWiseDocumentNames = new HashMap<>();
	
	public static String getNameByIndex(int i) {
		return indexWiseDocumentNames.get(i);
	}
	public static ArrayList<String> getFileNamesFromDocumentRepo(String folderName, Integer type) {
		ArrayList<String> files = new ArrayList<String>();
    	
    	File folder = new File(folderName);
    	File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	        //System.out.println("File " + listOfFiles[i].getName());
	        String name = listOfFiles[i].getName();
	        if(!StringUtils.contains(name, "stopword")) {
	        	switch (type) {
				case 1:
					name = name + "-v.dot";
					break;
				case 2:
					name = name + "-e.dot";
					break;
				default:
					break;
				}
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
	
	public static HashMap<String, ArrayList<WordFrequency>> getDocumentWordFrequenciesFromContent(HashMap<String, String> documentContentMap) throws Exception {
		HashMap<String, ArrayList<WordFrequency>> documentWordFrequenciesMap = new HashMap<String, ArrayList<WordFrequency>>();
		DocumentReader documentReader = new DocumentReader(); 
		
		Set<Entry<String, String>> entrySet = documentContentMap.entrySet();
		
		Iterator<Entry<String, String>> iterator = entrySet.iterator();
		
		while (iterator.hasNext()) {
			Map.Entry<java.lang.String, java.lang.String> entry = (Map.Entry<java.lang.String, java.lang.String>) iterator
					.next();
			//documentWordFrequenciesMap.put(entry.getKey(), documentReader.getWordFrequenciesFromContent(entry.getValue()));
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

	public static HashMap<String, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> createGraphForDocuments(
			HashMap<String, ArrayList<WordFrequency>> documentWordFrequencies,
			HashMap<String, ArrayList<String>> documentSentencesListMap) {

		HashMap<String, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> documentWiseGraphs = new HashMap<String, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>>();
		Set<Entry<String, ArrayList<WordFrequency>>> entrySet = documentWordFrequencies.entrySet();
		Iterator<Entry<String, ArrayList<WordFrequency>>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Map.Entry<java.lang.String, java.util.ArrayList<org.umair.research.thesis.models.WordFrequency>> entry = (Map.Entry<java.lang.String, java.util.ArrayList<org.umair.research.thesis.models.WordFrequency>>) iterator
					.next();
			ArrayList<String> documentSentences = documentSentencesListMap.get(entry.getKey());

			DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>(
					DefaultWeightedEdge.class);

			ArrayList<WordFrequency> values = entry.getValue();

			for (int i = 0; i < values.size(); i++) {
				WordFrequency wordFrequency = values.get(i);
				if (!graph.containsVertex(wordFrequency)) {
					graph.addVertex(wordFrequency);
				}
				for (int j = i; j < values.size(); j++) {
					WordFrequency wordFrequency2 = values.get(j);
					if (!wordFrequency.getWord().equals(wordFrequency2.getWord())) {
						ArrayList<Integer> occurances = new ArrayList<Integer>(wordFrequency.getOccurances());
						boolean retainAll = occurances.retainAll(wordFrequency2.getOccurances());
						List<Integer> distinctOccurances = occurances.stream().distinct().collect(Collectors.toList());
						if (distinctOccurances.size() > 0) {
							double commonOccurances = 0;
							for (Integer sentenceNumber : distinctOccurances) {
								String sentence = documentSentences.get(sentenceNumber);
								if (sentence.indexOf(wordFrequency.getWord()) < sentence
										.indexOf(wordFrequency2.getWord())) {
									commonOccurances++;
								}
							}
							if (commonOccurances > 0) {
								if (!graph.containsVertex(wordFrequency2)) {
									graph.addVertex(wordFrequency2); 
								}
								if (!graph.containsEdge(wordFrequency, wordFrequency2)
										&& graph.edgeSet().size() < 500) {
									DefaultWeightedEdge edge = graph.addEdge(wordFrequency, wordFrequency2);
									graph.setEdgeWeight(edge, commonOccurances);
								}
							}
						}
					}
				}
			}
			documentWiseGraphs.put(entry.getKey(), graph);
		}

		return documentWiseGraphs;
	}
	
	public static HashMap<String,GraphStatistics> getGraphStatisicsByRelatedAlgoAndMinSimilarity(ArrayList<DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> graphs, RelatednessCalculator rc, double minSimilarity) {
		System.out.println("Calculating Statistics for " + rc.getClass().getSimpleName() + ", Min Sim: " + minSimilarity);
		
		long start = System.currentTimeMillis();
		
		ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
		
		
		HashMap<String,GraphStatistics> graphStatisicsByRelatedAlgoAndMinSimilarity = new HashMap<String, GraphStatistics>();
		for(int i = 0; i < graphs.size(); i++) {
			for(int j = i; j < graphs.size(); j++) {
				System.out.println("Value of i: " + i);
				System.out.println("Value of j: " + j);
				if(i == j ) {
					//graphStatisicsByRelatedAlgoAndMinSimilarity.put(i + "-" + j, new GraphStatistics(1,1,1,1));
					continue;
				}
				
			}
		}
		
		executor.shutdown();
		// Wait until all threads are finish
		while (!executor.isTerminated()) {
 
		}
		System.out.println("\nFinished all threads");
				/*
				int commonNodesCount = 0;
				int commonEdgesCount = 0;
				//Traverse vertex set of graph that has less number of vertices
				Set<WordFrequency> vertexSetOfSmallerGraph = null;
				Set<WordFrequency> vertexSetOfLargerGraph = null;
				if(graphs.get(i).vertexSet().size() > graphs.get(j).vertexSet().size()) {
					vertexSetOfSmallerGraph = graphs.get(j).vertexSet();
					vertexS`etOfLargerGraph = graphs.get(i).vertexSet();
				} else {
					vertexSetOfSmallerGraph = graphs.get(i).vertexSet();
					vertexSetOfLargerGraph =  graphs.get(j).vertexSet();
				}
				
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
				int smallerGraphIndex = 0;
				int largerGraphIndex = 0;
				Set<DefaultWeightedEdge> edgeSetOfSmallerGraph = null;
				Set<DefaultWeightedEdge> edgeSetOfLargerGraph = null;
				if(graphs.get(i).edgeSet().size() > graphs.get(j).edgeSet().size()) {
					edgeSetOfSmallerGraph = graphs.get(j).edgeSet();
					edgeSetOfLargerGraph = graphs.get(i).edgeSet();
					smallerGraphIndex = j;
					largerGraphIndex = i;
				} else {
					edgeSetOfSmallerGraph = graphs.get(i).edgeSet();
					edgeSetOfLargerGraph =  graphs.get(j).edgeSet();
					smallerGraphIndex = i;
					largerGraphIndex = j;
				}
				
				for (DefaultWeightedEdge edgeSmall : edgeSetOfSmallerGraph) {
					String sourceWordSmall = graphs.get(smallerGraphIndex).getEdgeSource(edgeSmall).getWord();
		            String targetWordSmall = graphs.get(smallerGraphIndex).getEdgeTarget(edgeSmall).getWord();
					for (DefaultWeightedEdge edgeLarge : edgeSetOfLargerGraph) {
						String sourceWordLarge = graphs.get(largerGraphIndex).getEdgeSource(edgeLarge).getWord();
			            String targetWordLarge = graphs.get(largerGraphIndex).getEdgeTarget(edgeLarge).getWord();
			            //System.out.println("Calculating similarity for: " + sourceWordSmall +  " : " + sourceWordLarge);
						double similarityForSource = rc.calcRelatednessOfWords(sourceWordSmall, sourceWordLarge);
						//System.out.println("Calculating similarity for: " + targetWordSmall +  " : " + targetWordLarge);
						double similarityForTarget = rc.calcRelatednessOfWords(targetWordSmall, targetWordLarge);
						if(similarityForSource > minSimilarity && similarityForTarget > minSimilarity) {
							commonEdgesCount++;
						}
					}
				}
				
				graphStatisicsByRelatedAlgoAndMinSimilarity.put(i + "-" + j, new GraphStatistics(commonNodesCount, commonEdgesCount, vertexSetOfLargerGraph.size(), edgeSetOfLargerGraph.size()));
				
			}
		}
		
				 */
		long end = System.currentTimeMillis();
		NumberFormat formatter = new DecimalFormat("#0.00000");
		System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");
		
		return graphStatisicsByRelatedAlgoAndMinSimilarity;
	}
	
	public static void createSimilarityMatrix(HashMap<String, ArrayList<WordFrequency>> documentWordFrequencies,
			HashMap<String, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> documentWiseGraphs) throws IOException {
		
		
		//JiangConrath double[] MIN_SIMILARITY_VALUE = {1.2331};
		//HirstStOnge double[] MIN_SIMILARITY_VALUE = {8}; very slow
		//LeacockChodorow double[] MIN_SIMILARITY_VALUE = {1.4978};
		//Lesk double[] MIN_SIMILARITY_VALUE = {92}; very slow
		//Lin double[] MIN_SIMILARITY_VALUE = {0.5};
		double[] MIN_SIMILARITY_VALUE = {0.5};
		WS4JConfiguration.getInstance().setMFS(false);
		WS4JConfiguration.getInstance().setStem(true);
		WS4JConfiguration.getInstance().setCache(true);
		WS4JConfiguration.getInstance().setLeskNormalize(true);
		//Jiang Conrath = min:0 mid:1.2331 max:2.4663
		
		ILexicalDatabase db = new NictWordNet();
		/*RelatednessCalculator[] rcs = {
			new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db),  new WuPalmer(db), 
			new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db)
		};*/
		
		RelatednessCalculator[] rcs = {
				new WuPalmer(db)
			};
		
		HashMap<String, HashMap<String, GraphStatistics>> rcMinSimWiseGraph = new HashMap<String, HashMap<String, GraphStatistics>>();
		
		Set<String> keySet = documentWiseGraphs.keySet();
		int i = 0;
		for (String string : keySet) {
			indexWiseDocumentNames.put(i, string);
			i++;
		}
		
		ArrayList<DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge>> graphs = new ArrayList<>(documentWiseGraphs.values()) ;
		
		long start = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);	
		
		for(RelatednessCalculator rc : rcs) {
				for(double minSimilarity : MIN_SIMILARITY_VALUE) {
					Runnable runnable = new GraphStatisticCalculator(graphs, new ArrayList<String>(documentWiseGraphs.keySet()), minSimilarity, rc);
					runnable.run();
				}
			}
		executor.shutdown();
		// Wait until all threads are finish
		while (!executor.isTerminated()) {
 
		}
		System.out.println("\nFinished all threads");
		}
	
	
	/*44444
				similarityMatrixDouble[i][j] = similarity;
				similarityMatrixDouble[j][i] = similarity;*/
	
	
		//GraphStatisticsService.exportCalculatedGraphValues();
		
		/*System.out.println(PrettyPrinter.print(similarityMatrixDouble, new Printer<Double>() {
	        @Override
	        public String print(Double obj) {
	            return obj.toString();
	        }
	    }));
		
		String[] names = documentWiseGraphs.keySet().toArray(new String[documentWiseGraphs.keySet().size()]);
		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		showFrame( alg.performClustering(similarityMatrix, names,
                new AverageLinkageStrategy()));*/
		
		
		
		/*HashMap<String, Double[][]> documentWiseSimilarityMatrix = new HashMap<String, Double[][]>();

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
			Map.Entry<java.lang.String, java.util.ArrayList<org.umair.models.WordFrequency>> entry = (Map.Entry<java.lang.String, java.util.ArrayList<org.umair.models.WordFrequency>>) iterator
					.next();
			
			System.out.println("Document Name: " + entry.getKey());
			DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge> graph = documentWiseGraphs.get(entry.getKey());
			
			Set<WordFrequency> vertexSet = graph.vertexSet();
			ArrayList<WordFrequency> words = new ArrayList<WordFrequency>(vertexSet);
			Double[][] similarityMatrix = new Double[words.size()][words.size()];
			DepthFirstIterator<WordFrequency, DefaultWeightedEdge> depthFirstIterator = new DepthFirstIterator<>(graph);
			WordFrequency next = depthFirstIterator.next();
			
			
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
		}*/
		
	
	public static void showFrame(Cluster cluster) {
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
		
		dp.setModel(cluster);
		frame.setVisible(true);
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

	private static void exportVertices(String key, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge> graph,
			FileWriter fileWriter) {
		PrintWriter out = new PrintWriter(fileWriter);
		for (WordFrequency v : graph.vertexSet()) {
            out.println(v.getWord() + " (" + v.getFrequency() + ")");
        }
		out.flush();
	}

	private static void exportEdges(String key, DirectedWeightedMultigraph<WordFrequency, DefaultWeightedEdge> graph,
			FileWriter fileWriter) {
		PrintWriter out = new PrintWriter(fileWriter);
		
		for (DefaultWeightedEdge e : graph.edgeSet()) {
            String source = graph.getEdgeSource(e).getWord();
            String target = graph.getEdgeTarget(e).getWord();

            out.println(source + "->" + target);
        }
		out.flush();
	}
}
