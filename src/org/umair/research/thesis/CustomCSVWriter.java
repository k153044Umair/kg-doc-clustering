package org.umair.research.thesis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVWriter;

public class CustomCSVWriter {
	
	public static void exportDataToExcel(String fileName, double[][] data) throws FileNotFoundException, IOException
    {
        File file = new File(fileName);
        if (!file.isFile())
            file.createNewFile();

        CSVWriter csvWriter = new CSVWriter(new FileWriter(file), '\t');
        int rowCount = data.length;

        for (int i = 0; i < rowCount; i++)
        {
            int columnCount = data[i].length;
            String[] values = new String[columnCount];
            for (int j = 0; j < columnCount; j++)
            {
                values[j] = data[i][j] + "";
            }
            csvWriter.writeNext(values);
        }

        csvWriter.flush();
        csvWriter.close();
    }

}
