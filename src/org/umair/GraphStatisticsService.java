package org.umair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import org.umair.models.GraphStatistics;

public class GraphStatisticsService {
	
/*	public static void exportCalculatedGraphValues(String fileName, HashMap<String, GraphStatistics> map) {
		PrintWriter out;
		FileOutputStream fos;
		try {
			System.out.println("Creating Text file for: " + fileName);
			out = new PrintWriter(new FileWriter("results/txt/" + fileName + ".txt"));
			for(String key : map.keySet()) {
				out.println(key);
			}
			out.println("-----------------------------------");
			for(GraphStatistics graphStatistics : map.values()) {
				out.println("commonNodesCount: " + graphStatistics.getCommonNodesCount());
				out.println("vertexSetOfLargerGraph: " + graphStatistics.getMaxNodesCount());
				out.println("commonEdgesCount: " + graphStatistics.getCommonEdgesCount());
				out.println("edgeSetOfLargerGraph: " + graphStatistics.getMaxEdgesCount());
				
				
				out.println("Comma Separated: " + graphStatistics.getCommonNodesCount() + ", " + graphStatistics.getCommonEdgesCount() + ", " + graphStatistics.getMaxNodesCount() + ", " + graphStatistics.getMaxEdgesCount());
			}
			out.flush();
			System.out.println("Creating Ser file for: " + fileName);
			 fos = new FileOutputStream("results/ser" + fileName + ".ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(map);
	        oos.close();
	        fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	public static void exportCalculatedGraphValues(String fileName, GraphStatistics graphStatistics) {
		PrintWriter out;
		FileOutputStream fos;
		try {
			System.out.println("Creating Text file for: " + fileName);
			new File("results/txt/").mkdirs();
			out = new PrintWriter(new FileWriter("results/txt/" + fileName + ".txt"));
			String excelFileName = "results/Excel/" + fileName + ".csv";
			new File("results/Excel/").mkdirs();
			CustomCSVWriter.exportDataToExcel(excelFileName, graphStatistics.getSimilarityMatrix());
			out.println("-----------------------------------");
			out.println("Names: ");
			for(String name: graphStatistics.getName()) {
				out.print(name);
				out.print(",");
			}
			out.flush();
			System.out.println("Creating Ser file for: " + fileName);
			 fos = new FileOutputStream("results/ser/" + fileName + ".ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(graphStatistics);
	        oos.close();
	        fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
