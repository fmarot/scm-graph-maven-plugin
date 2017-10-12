package com.teamtter.maven.graph.builder;

import java.io.File;
import java.util.Set;

import com.teamtter.maven.graph.data.GraphModel;

public interface GraphBuilder {

	public void generateImage(GraphModel model, Set<String> acceptedUrlFilters, Set<String> strippedSubstrings, File outputFile);

}
