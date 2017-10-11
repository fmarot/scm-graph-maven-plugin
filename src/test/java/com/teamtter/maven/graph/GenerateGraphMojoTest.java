package com.teamtter.maven.graph;

import java.io.File;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Multigraph;
import org.junit.Test;

public class GenerateGraphMojoTest {

	public static void main(String[] args) throws Exception {
		new GenerateGraphMojoTest().testGenerateGraphImage();
	}
	
	@Test
	public void testGenerateGraphImage() throws Exception {
		Multigraph<String, DefaultWeightedEdge> graph = new Multigraph<>(DefaultWeightedEdge.class);
	    graph.addVertex("v1");
	    graph.addVertex("v2");
	    graph.addVertex("v3");
	    DefaultWeightedEdge edge1 = graph.addEdge("v1", "v2");
	    graph.setEdgeWeight(edge1, 5);
	 
	    DefaultWeightedEdge edge2 = graph.addEdge("v1", "v2");
	    graph.setEdgeWeight(edge2, 3);
	    
	    DefaultWeightedEdge edge13 = graph.addEdge("v1", "v3");
	    graph.setEdgeWeight(edge13, 3);
	    DefaultWeightedEdge edge23 = graph.addEdge("v2", "v3");
	    graph.setEdgeWeight(edge23, 3);
	    
	    File outputFile = new File("out.png");
	    if (!outputFile.delete()) {
	    	throw new Exception("Impossible to delete outputFile " + outputFile);
	    }
		JGraphTGraphBuilder.generateImageFromGraph(outputFile, graph);
	}

}
