package org.umair;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.commons.io.FilenameUtils;
import org.umair.models.GraphStatistics;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.CompleteLinkageStrategy;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.visualization.DendrogramPanel;
import com.github.habernal.confusionmatrix.ConfusionMatrix;

public class ClusterTest {
	
	public static Map<String, Double> fileWiseThresholds = new HashMap<String, Double> ();
	
	static {
		fileWiseThresholds.put("results/ser/JiangConrath-1.2331.ser", 0.02);
		fileWiseThresholds.put("results/ser/LeacockChodorow-1.4978.ser", 0.13);
		fileWiseThresholds.put("results/ser/Lin-0.5.ser", 0.03);
		fileWiseThresholds.put("results/ser/LeacockChodorow-92.0.ser", 0.0);
		fileWiseThresholds.put("results/ser/Resnik-7.0.ser", 0.02);
	}

	public static void main(String[] args) throws Exception {

		System.out.println("Testing reading of ser files");
		File groundTruthFolder = new File("results/ser");
		File[] listOfCluster = groundTruthFolder.listFiles();
		for (int i = 0; i < listOfCluster.length; i++) {
			evaluateClusters("results/"+groundTruthFolder.getName() + "/" + listOfCluster[i].getName());
			//System.out.println("results/"+groundTruthFolder.getName() + "/" + listOfCluster[i].getName());
		}
	}
	
	public static void evaluateClusters(String fileName) throws Exception{
		System.out.println("Evaluating: " + fileName);
		GraphStatistics graphStatistics = deserialzeAddressJDK7(fileName);

		String[] namesArr = new String[graphStatistics.getName().size()];
		for (int m = 0; m < graphStatistics.getName().size(); m++) {
			namesArr[m] = graphStatistics.getName().get(m);
		}
		if(getThresholdFromFile(fileName) > 0) {
			draw(namesArr, graphStatistics.getSimilarityMatrix(), getThresholdFromFile(fileName));
		}
	}
	
	
	public static double getThresholdFromFile(String fileName) {
		return fileWiseThresholds.get(fileName);
	}

	public static GraphStatistics deserialzeAddressJDK7(String filename) {

		GraphStatistics graphStatistics = null;

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {

			graphStatistics = (GraphStatistics) ois.readObject();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return graphStatistics;

	}

	private static void draw(String[] names, double[][] distances, double threshold) throws Exception {
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

		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		Cluster cluster = alg.performClustering(distances, names, new CompleteLinkageStrategy());
		//cluster.toConsole(5);
		List<String> numberOfClusters = numberOfClusters(5, cluster);
		numberOfClusters.size();
		List<Cluster> flatClusters = alg.performFlatClustering(distances, names, new CompleteLinkageStrategy(),threshold);
		//System.out.println("Cluster Size:" + flatClusters.size());
		
		Map<String, List<String>> obtainedClusters = new TreeMap<>();
		int i = 1;
		for(Cluster cluster2: flatClusters) {
			obtainedClusters.put("C"+i, getLeafNames(cluster2, new ArrayList<String>()));
			//System.out.println(getLeafNames(cluster2, new ArrayList<String>()));
			i++;
		}
		
		Map<String, ArrayList<String>> groundTruthClusters = getGroundTruthClusters();
		//System.out.println(groundTruthClusters.size());
		
		ConfusionMatrix confusionMatrix = new ConfusionMatrix();
		for(int k = 1; k < 6; k++) {
			String obtainedClusterKey = "C"+k;
			for(int j=1; j < 6; j++) {
				String groundTruthKey="C"+j;
				List<String> intersection = intersection(obtainedClusters.get(obtainedClusterKey), groundTruthClusters.get(groundTruthKey));
				//System.out.println(intersection.size());
				confusionMatrix.increaseValue(obtainedClusterKey, groundTruthKey, intersection.size());
			}
		}
		
		 System.out.println(confusionMatrix); 
		 System.out.println(confusionMatrix.printLabelPrecRecFm());
		 System.out.println(confusionMatrix.getPrecisionForLabels());
		 System.out.println(confusionMatrix.getRecallForLabels());
		 System.out.println(confusionMatrix.printNiceResults());
		 System.out.println("Accuracy: " + confusionMatrix.getAccuracy());
		 System.out.println("Precision: " + confusionMatrix.getAvgPrecision());
		 System.out.println("Recall" + confusionMatrix.getAvgRecall());
		
		/*
		 * dp.setModel(cluster); frame.setVisible(true);
		 */
		/*
		 * ConfusionMatrix confusionMatrix = new ConfusionMatrix();
		 * 
		 * ConfusionMatrix cm = new ConfusionMatrix();
		 
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
	
	
	public static ArrayList<String> getLeafNames(Cluster node, ArrayList<String> leafName)
    {
        if (node.isLeaf()) leafName.add(node.getName());
        for (Cluster child : node.getChildren())
        {
            getLeafNames(child, leafName);
        }
        return leafName;
    }
	
	public static Map<String, ArrayList<String>> getGroundTruthClusters() {
		Map<String, ArrayList<String>> groundTruthClusters = new TreeMap<String, ArrayList<String>>();
		File groundTruthFolder = new File(Constants.DATA_FOLDER+ "GT");
		File[] listOfCluster = groundTruthFolder.listFiles();
		for (int i = 0; i < listOfCluster.length; i++) {
			groundTruthClusters.put(listOfCluster[i].getName(), getFileNamesFromFolder(groundTruthFolder.getParent()+"/"+groundTruthFolder.getName() + "/" + listOfCluster[i].getName()));
		}
		return groundTruthClusters;
	}
	
	public static ArrayList<String> getFileNamesFromFolder(String folderName) {
		ArrayList<String> files = new ArrayList<String>();
    	
    	File folder = new File(folderName);
    	File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	        String name = listOfFiles[i].getName();
	        if(!name.equalsIgnoreCase("stopword")) {
	        	files.add(FilenameUtils.removeExtension(name));
	        }
	      }
	    }
	    
	    return files;
	}
	
	public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }
	

	public static List<String> numberOfClusters(int n, Cluster cluster) {

		if (n <= 0) {
			return null;
		}

		List<Cluster> ret = cluster.getChildren();
		while (ret.size() > n) {
			Cluster lowest = null;
			Iterator<Cluster> iterator = ret.iterator();
			while (iterator.hasNext()) {
				Cluster node = iterator.next();
				if (ret.contains(node.getParent().getChildren().get(0))
						&& ret.contains(node.getParent().getChildren().get(1))) {
					if (lowest == null) {
						lowest = node.getParent();
					} else if (node.getParent().getDistanceValue() < lowest.getDistanceValue()) {
						lowest = node.getParent();
					}
				}
				ret.remove(lowest.getChildren().get(0));
				ret.remove(lowest.getChildren().get(1));
				ret.add(lowest);
			}
		}

		return getNames(ret);

	}

	private static List<String> getNames(List<Cluster> ret) {
		List<String> names = new ArrayList<String>();
		for (Cluster cluster : ret) {
			names.add(cluster.getName());
		}
		return names;
	}
	
	public static void processConfusionMatrixAndPrintResults(Map<String, ArrayList<String>> obtainedClusters, PrintWriter out) {
		Map<String, ArrayList<String>> groundTruthClusters = getGroundTruthClusters();
		//System.out.println(groundTruthClusters.size());
		File groundTruthFolder = new File(Constants.DATA_FOLDER+ "GT");
		File[] listOfCluster = groundTruthFolder.listFiles();
		ConfusionMatrix confusionMatrix = new ConfusionMatrix();
		for(int k = 0; k < obtainedClusters.size(); k++) {
			String obtainedClusterKey = "C"+(k+1);
			for(int j=0; j < listOfCluster.length; j++) {
				String groundTruthKey=listOfCluster[j].getName();
				String groundTruthKeyForCM="C"+(j+1);
				System.out.println("Considering " + groundTruthKey + " as " + groundTruthKeyForCM);
				List<String> intersection = intersection(obtainedClusters.get(obtainedClusterKey), groundTruthClusters.get(groundTruthKey));
				//System.out.println(intersection.size());
				confusionMatrix.increaseValue(obtainedClusterKey, groundTruthKeyForCM, intersection.size());
			}
		}
		
		 System.out.println(confusionMatrix); 
		 out.println(confusionMatrix); 
		 System.out.println(confusionMatrix.printLabelPrecRecFm());
		 out.println(confusionMatrix.printLabelPrecRecFm());
		 System.out.println(confusionMatrix.getPrecisionForLabels());
		 out.println(confusionMatrix.getPrecisionForLabels());
		 System.out.println(confusionMatrix.getRecallForLabels());
		 out.println(confusionMatrix.getRecallForLabels());
		 System.out.println(confusionMatrix.printNiceResults());
		 out.println(confusionMatrix.printNiceResults());
		 System.out.println("Accuracy: " + confusionMatrix.getAccuracy());
		 out.println("Accuracy: " + confusionMatrix.getAccuracy());
		 System.out.println("Precision: " + confusionMatrix.getAvgPrecision());
		 out.println("Precision: " + confusionMatrix.getAvgPrecision());
		 System.out.println("Recall: " + confusionMatrix.getAvgRecall());
		 out.println("Recall: " + confusionMatrix.getAvgRecall());
	}

}
