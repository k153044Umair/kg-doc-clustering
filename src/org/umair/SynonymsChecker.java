package org.umair;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

public class SynonymsChecker {
	public static void main(String[] args) throws Exception {
		// construct the URL to the Wordnet dictionary directory
		// construct the dictionary object and open it
		File file = new File("/home/umair/FAST/Thesis/libraries/WordNet-3.0/dict");
		IDictionary dict = new Dictionary ( file ) ;
		dict . open () ;
		/*// look up first sense of the word " dog "
		IIndexWord idxWord = dict.getIndexWord ( " dog " , POS.NOUN ) ;
		IWordID wordID = idxWord . getWordIDs () . get (0) ;
		IWord word = dict . getWord ( wordID ) ;
		System . out . println ( " Id = " + wordID ) ;
		System . out . println ( " Lemma = " + word . getLemma () ) ;
		System . out . println ( " Gloss = " + word . getSynset () . getGloss () ) ;*/
		
		Set<String> lexicon = new HashSet<>();

		for (POS p : POS.values()) {
		    IIndexWord idxWord = dict.getIndexWord("chase", p);
		    if (idxWord != null) {
		        System.out.println("\t : " + idxWord.getWordIDs().size());
		        IWordID wordID = idxWord.getWordIDs().get(0);
		        IWord word = dict.getWord(wordID);
		        ISynset synset = word.getSynset();
		        System.out.print(synset.getWords().size());
		        for (IWord w : synset.getWords()) {
		            lexicon.add(w.getLemma());
		        }

		    }
		}

		for (String s : lexicon) {
		    System.out.println("wordnet lexicon : " + s);
		}
	}
	

}
