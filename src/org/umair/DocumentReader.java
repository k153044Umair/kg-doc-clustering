package org.umair;
/* This file is part of DocumentCluster, a program for clustering text
   documents based on similarity. To use, specify the number of clusters
   followed by the documents, which must be located in the data subdirectory.
   Stopwords are eliminated by filtering the document contents against
   stopwords.txt in the same directory. Words are stemmed using the Porter
   Stemming algorithm. k-means clustering based on cosine similarity is used
   for the clustering.

    Copyright (C) 2013   Ezra Erb

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published
    by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    I'd appreciate a note if you find this program useful or make
    updates. Please contact me through LinkedIn or github (my profile also has
    a link to the code depository)
*/
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.umair.models.WordFrequency;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;

/* This file handles translating a text file into a document map. The map
   treats the document as a bag of words, so puncation has no meaning. It
   gets stripped, followed by conversion to lower case. Numbers are also
   dropped. Stop words are then eliminated, followed by stemming and then
   the conversion to a map of word counts for the document. */
public final class DocumentReader
{
    /* File being processed. Class member to ensure it is always closed and
       relesed */
    private BufferedReader _file;
    // Words in input document so common they should be ignored
    private Stopwords _stopwords;
    // Service to convert words to their stems
    private PorterStemmer _stemmer;
    
    // Constuctor. 
    public DocumentReader() throws Exception
    {
        _stopwords = Stopwords.getStopWords();
        _stemmer = PorterStemmer.getStemmer();
    }
    
    //Process the given document and returns the list of strings in the document
    public ArrayList<String> getSentences(String fileName) throws Exception {
    	
    	ArrayList<String> results = new ArrayList<String>();
    	try {
    		String directory = new String("data");
            String fullName = directory + "/" + fileName;
            _file = new BufferedReader(new FileReader(fullName));
            if (_file == null) {
                // Failed to open. 
                System.out.println("ERROR: File to analyze " + fullName + " not found.");
                throw new IOException("ERROR: File to analyze " + fullName + " not found.");
            }
            String buffer = _file.readLine();
            StringBuilder sb = new StringBuilder();
			while (buffer != null) {
				sb.append(buffer).append("\n");
				buffer = _file.readLine();
			}
			
			// always start with a model, a model is learned from training data
			InputStream is = new FileInputStream("models/en-sent.bin");
			SentenceModel model = new SentenceModel(is);
			SentenceDetectorME sdetector = new SentenceDetectorME(model);

			String[] sentences = sdetector.sentDetect(sb.toString());
			
			results = new ArrayList((List) Arrays.asList(sentences));
            
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return results;
    }
    
    // Processes the given document and returns a word frequency map
    public ArrayList<WordFrequency> getWordFrequencies(List<String> sentences) throws Exception
    {
        HashMap<String, WordCounter> results = new HashMap<String, WordCounter>();
        ArrayList<WordFrequency> wordFrequencyList = new ArrayList<WordFrequency>();
        HashMap<String, ArrayList<Integer>> wordOccurances = new HashMap<String, ArrayList<Integer>>();
        InputStream inputStream = new 
        		FileInputStream("models/en-pos-maxent.bin"); 
        POSModel model = new POSModel(inputStream); 
        
        //Instantiating POSTaggerME class 
        POSTaggerME tagger = new POSTaggerME(model); 
        try {
        	int i = 0;
        	for (String sentence : sentences) {
        		//Loading Parts of speech-maxent model       
        		sentence = sentence.replaceAll("[^A-Za-z ]", " ").trim().replaceAll(" +", " ");
        		
        		
        		// Convert to lower case
        		sentence = sentence.toLowerCase();
        		
        		if (!sentence.isEmpty()) { // Not empty string at this point
        			// Split into words
        			WhitespaceTokenizer whitespaceTokenizer= WhitespaceTokenizer.INSTANCE; 
    		      String[] procWords = whitespaceTokenizer.tokenize(sentence); 
    		      String[] tags = tagger.tag(procWords);
    		      System.out.println(procWords.toString() + ": " + procWords.length);
    		      System.out.println(tags.toString() + ": " + tags.length);
    		      POSSample sample = new POSSample(procWords, tags);
    		      System.out.println(sample.toString());
    		      
    		      Map<String, String> sentencePOSTagMap = new HashMap<String, String>();
    		      if(procWords.length > 0 && tags.length > 0) {
    		    	  for (int j = 0; j < procWords.length; j++) {
    		    		  sentencePOSTagMap.put(procWords[j], tags[j]);
					}
    		      }
    		      
        			int index;
        			for (index = 0; index < procWords.length; index++) {
        				// Exclude words on the stopwords list
        				if (!_stopwords.isStopWord(procWords[index]) && procWords[index].length() > 1) {
        					/* Ignore words consisting of 's'. This is the
                               remainder of a possessive: [word]'s */
        					if (!procWords[index].equals("s")) {
        						// Stem the word from the file
        						//String stem = _stemmer.getStem(procWords[index]);
        						String stem = procWords[index];
        						String posTag = sentencePOSTagMap.get(procWords[index]);
        						// Update count as needed
        						WordCounter count = results.get(stem + "_" + posTag);
        						if (count == null) {
        							count = new WordCounter();
        							results.put(stem + "_" + posTag, count);
        						}
        						count.increment();
        						if(wordOccurances.get(stem) == null) {
        							ArrayList<Integer> ocurrances = new ArrayList<Integer>();
        							wordOccurances.put(stem, ocurrances);
        						}
        						wordOccurances.get(stem).add(i);
        					} // Not a single s
        				} // Not on the list of words to ignore
        			} // For each word to process
        		} // Have words to process
        		i++;
			}
        }
        catch (Exception e) {
            // Clean up file
            if (_file != null)
                _file.close();
            _file = null;
            throw e;
        }
        
        if(results.size() > 0) {
        	Set<Entry<String, WordCounter>> entrySet = results.entrySet();
        	
        	Iterator<Entry<String, WordCounter>> iterator = entrySet.iterator();
        	
        	while (iterator.hasNext()) {
				Map.Entry<java.lang.String, org.umair.WordCounter> entry = (Map.Entry<java.lang.String, org.umair.WordCounter>) iterator
						.next();
				String[] wordAndPOSTag = entry.getKey().split("_");
				String word = wordAndPOSTag[0];
				String posTag = wordAndPOSTag[1];
				if(!StringUtils.equals(DocumentService.getConvertedPOSTag(posTag), "e")) {
					wordFrequencyList.add(WordFrequency.create(word, entry.getValue(),wordOccurances.get(word), posTag));
				}
			}
        }
        return wordFrequencyList;
    }

	// Processes the given document and returns a word frequency map
    public HashMap<String, WordCounter> getWordFrequencies(String fileName) throws Exception
    {
        HashMap<String, WordCounter> results = new HashMap<String, WordCounter>();
        try {
            // Open the data file. 
            /* TRICKY NOTE: Notice the double backslash below. Java uses
               '\' as an escape character. The first is the esacpe
               character needed to insert a litteral '\' in the string! */
            String directory = new String("data");
            String fullName = directory + "/" + fileName;
            _file = new BufferedReader(new FileReader(fullName));
            if (_file == null) {
                // Failed to open. 
                System.out.println("ERROR: File to analyze " + fullName + " not found.");
                throw new IOException("ERROR: File to analyze " + fullName + " not found.");
            }
            String buffer = _file.readLine();
            String hyphenate = null; // Word wrapped from previous line
            boolean haveHyphenate = false; // Word wrapps to the next line
            while (buffer != null) {
                /* A dash not by itself at the end of the line indicates
                   hyphenation
                   NOTE: Deliberately designed so trailing spaces are treated
                   as a sign of formatting problems */
                haveHyphenate = buffer.matches(".*[A-Za-z]-$");

                /* Remove all non-alpha characters and consolidate resulting
                   whitespace */
                buffer = buffer.replaceAll("[^A-Za-z ]", " ").trim().replaceAll(" +", " ");

                // Convert to lower case
                buffer = buffer.toLowerCase();

                if (!buffer.isEmpty()) { // Not empty string at this point
                    // Split into words
                    String [] procWords = buffer.split(" ");
                    if (hyphenate != null)
                        // Merge into FRONT of first word
                        procWords[0] = hyphenate + procWords[0];
                    hyphenate = null;

                    int index;
                    for (index = 0; index < procWords.length; index++) {
                        /* If last word and line is hyphenated, save it for
                           the next line */
                        if (haveHyphenate && (index >= procWords.length - 1)) {
                            hyphenate = procWords[index];
                            haveHyphenate = false;
                        }
                        // Exclude words on the stopwords list
                        else if (!_stopwords.isStopWord(procWords[index])) {
                            /* Ignore words consisting of 's'. This is the
                               remainder of a possessive: [word]'s */
                            if (!procWords[index].equals("s")) {
                                // Stem the word from the file
                                String stem = _stemmer.getStem(procWords[index]);
                                // Update count as needed
                                WordCounter count = results.get(stem);
                                if (count == null) {
                                    count = new WordCounter();
                                    results.put(stem, count);
                                }
                                count.increment();
                            } // Not a single s
                        } // Not on the list of words to ignore
                    } // For each word to process
                } // Have words to process
                buffer = _file.readLine();
            } // While file lines to process

            /* If get to here with a hypenate word set, it was on the last
               line; the rest no longer exists. Log it and ignore it */
            if (hyphenate != null)
                System.out.println("WARNING: Badly formed file " + fullName + ". Hypenated word on last line: " + hyphenate + " ignored");
        }
        catch (Exception e) {
            // Clean up file
            if (_file != null)
                _file.close();
            _file = null;
            throw e;
        }
        return results;
    }

    /** This method ensures the file is always closed before the object dies.
        In general, if the file gets to here, something has gone wrong and
        resources have been held far longer than needed. A warning is issued
        to handle this case */
    public void finailize()
    {
        if (_file != null) {
            System.out.println("WARNING: File not properly closed");
            try {
                _file.close();
            }
            /* Catch and dispose of any IO exception, since the object is going
               away soon anyway. This is normally an anti-pattern, but needed
               in this case */
            catch (IOException e) {}
            _file = null;
        }
    }

    // Test program. Read an input file and print the results
    public static void main(String[] args) throws Exception
    {
        try {
            if (args.length != 1)
                System.out.println("Single file for test must be specified");
            else {
                DocumentReader test = new DocumentReader();
                System.out.println(test.getWordFrequencies(args[0]));
            }
        }
        catch (Exception e) {
            System.out.println("Exception " + e + " caught");
            throw e; // Rethrow so improper temination is obvious
        }
    }
}
