package org.umair.models;

import java.util.ArrayList;

import org.umair.WordCounter;

public class WordFrequency {
	
	private String word;
	private int frequency;
	private ArrayList<Integer> occurances;
	private String posTag;
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	public ArrayList<Integer> getOccurances() {
		return occurances;
	}
	public void setOccurances(ArrayList<Integer> occurances) {
		this.occurances = occurances;
	}
	public String getPosTag() {
		return posTag;
	}
	public void setPosTag(String posTag) {
		this.posTag = posTag;
	}

	public static WordFrequency create(String word, WordCounter wordCounter, ArrayList<Integer> occurances, String posTag) {
		WordFrequency wordFrequency = new WordFrequency();
		wordFrequency.setWord(word);
		wordFrequency.setFrequency(wordCounter.getCount());
		wordFrequency.setOccurances(occurances);
		wordFrequency.setPosTag(posTag);
		return wordFrequency;
	}
	
	@Override
	public String toString() {
		return word;
	}
	

}
