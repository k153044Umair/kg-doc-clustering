package org.umair;
import java.awt.Container;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;

import weka.classifiers.Evaluation;
import weka.clusterers.HierarchicalClusterer;
import weka.core.Attribute;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.gui.hierarchyvisualizer.HierarchyVisualizer;


public class WekaTest {
	
	static HierarchicalClusterer clusterer;
	static Instances data;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// Instantiate clusterer
		clusterer = new HierarchicalClusterer();
		clusterer.setOptions(new String[] {"-L", "COMPLETE"});
		clusterer.setDebug(true);
		clusterer.setNumClusters(5);
		clusterer.setDistanceFunction(new EuclideanDistance());
		clusterer.setDistanceIsBranchLength(true);
		// Build dataset
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute("A"));
		attributes.add(new Attribute("B"));
		attributes.add(new Attribute("C"));
		data = null;//("Weka test", attributes, 3);
		
		// load CSV
	    CSVLoader loader = new CSVLoader();
	    loader.setSource(new File("results/Excel/Lin-0.5.csv"));
	    data = loader.getDataSet();
		
		/*// Add data
		data.add(new Instance(1.0, new double[] { 1.0, 0.0, 1.0 }));
		data.add(new Instance(1.0, new double[] { 0.5, 0.0, 1.0 }));
		data.add(new Instance(1.0, new double[] { 0.0, 1.0, 0.0 }));
		data.add(new Instance(1.0, new double[] { 0.0, 1.0, 0.3 }));*/
		
		// Cluster network
		clusterer.buildClusterer(data);
		
		// Print normal
		clusterer.setPrintNewick(false);
		System.out.println(clusterer.graph());
		// Print Newick
		clusterer.setPrintNewick(true);
		System.out.println(clusterer.graph());
		
		// Let's try to show this clustered data!
		JFrame mainFrame = new JFrame("Weka Test");
		mainFrame.setSize(1200, 1200);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = mainFrame.getContentPane();
		content.setLayout(new GridLayout(1, 1));
		
		HierarchyVisualizer visualizer = new HierarchyVisualizer(clusterer.graph());
		content.add(visualizer);
		
		mainFrame.setVisible(true);
	}

}