package org.umair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordLemmatizer {
	protected StanfordCoreNLP pipeline;

	public StanfordLemmatizer() {
		// Create StanfordCoreNLP object properties, with POS tagging
		// (required for lemmatization), and lemmatization
		Properties props;
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");

		// StanfordCoreNLP loads a lot of models, so you probably
		// only want to do this once per execution
		this.pipeline = new StanfordCoreNLP(props);
	}

	public List<String> lemmatize(String documentText) {
		List<String> lemmas = new LinkedList<String>();

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(documentText);

		// run all Annotators on this text
		this.pipeline.annotate(document);

		// Iterate over all of the sentences found
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			// Iterate over all tokens in a sentence
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// Retrieve and add the lemma for each word into the list of
				// lemmas
				lemmas.add(token.get(LemmaAnnotation.class));
			}
		}

		return lemmas;
	}
	
	public static void main(String[] args) throws IOException {
		StanfordLemmatizer stanfordLemmatizer = new StanfordLemmatizer();
		/*ArrayList<String> fileNames = DocumentService.getFileNamesFromDocumentRepo(Constants.DATA_FOLDER, 0);
		
		HashMap<String, String> documentContentMap = DocumentService.readDocumentContent(fileNames, Constants.DATA_FOLDER);
		
		Set<Entry<String, String>> entrySet = documentContentMap.entrySet();
		for(Entry<String, String> entry : entrySet) {
			System.out.println("Printing lemmatized words of " + entry.getKey());
			List<String> lemmatize = stanfordLemmatizer.lemmatize(entry.getValue());
			for(String word : lemmatize) {
				System.out.println(word);
			}
		}*/
		
		List<String> lemmatize = stanfordLemmatizer.lemmatize("Two quick brown foxes feed over the lazyness dogs");
		for(String word : lemmatize) {
			System.out.println(word);
		}
	}
}
