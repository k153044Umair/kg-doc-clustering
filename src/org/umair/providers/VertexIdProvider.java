package org.umair.providers;

import org.jgrapht.ext.ComponentNameProvider;
import org.umair.models.WordFrequency;

public class VertexIdProvider implements ComponentNameProvider<WordFrequency> {

	@Override
	public String getName(WordFrequency component) {
		return component.getWord();
	}

}

