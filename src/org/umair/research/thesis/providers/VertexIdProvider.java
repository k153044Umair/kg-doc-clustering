package org.umair.research.thesis.providers;

import org.jgrapht.ext.ComponentNameProvider;
import org.umair.research.thesis.models.WordFrequency;

public class VertexIdProvider implements ComponentNameProvider<WordFrequency> {

	@Override
	public String getName(WordFrequency component) {
		return component.getWord();
	}

}

