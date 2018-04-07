package org.umair.providers;

import org.jgrapht.ext.ComponentNameProvider;
import org.jgrapht.graph.DefaultWeightedEdge;

public class EdgeLabelProvider implements ComponentNameProvider<DefaultWeightedEdge> {

	@Override
	public String getName(DefaultWeightedEdge component) {
		return component.toString();
	}

}

