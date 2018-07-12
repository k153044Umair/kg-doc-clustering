package org.umair.research.thesis.models;

import java.io.Serializable;
import java.util.ArrayList;

public class GraphStatistics implements Serializable {
	
	private static final long serialVersionUID = 8053354203859372048L;
	ArrayList<String> name;
	double[][] similarityMatrix;
	
	public GraphStatistics(ArrayList<String> name, double[][] similarityMatrix) {
		super();
		this.name = name;
		this.similarityMatrix = similarityMatrix;
	}

	public ArrayList<String> getName() {
		return name;
	}

	public void setName(ArrayList<String> name) {
		this.name = name;
	}

	public double[][] getSimilarityMatrix() {
		return similarityMatrix;
	}

	public void setSimilarityMatrix(double[][] similarityMatrix) {
		this.similarityMatrix = similarityMatrix;
	}
	
}
