package com.teamtter.maven.graph;

import java.io.IOException;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Multigraph;
import org.junit.Test;

public class GenerateGraphMojoTest {

	public static void main(String[] args) throws IOException {
		new GenerateGraphMojoTest().testGenerateGraphImage();
	}
	
	@Test
	public void testGenerateGraphImage() throws IOException {
		Multigraph<String, DefaultWeightedEdge> graph = new Multigraph<>(DefaultWeightedEdge.class);
	    graph.addVertex("v1");
	    graph.addVertex("v2");
	    DefaultWeightedEdge edge1 = graph.addEdge("v1", "v2");
	    graph.setEdgeWeight(edge1, 5);
	 
	    DefaultWeightedEdge edge2 = graph.addEdge("v1", "v2");
	    graph.setEdgeWeight(edge2, 3);
	    
	    GenerateGraphMojo.generateGraphImage(graph);
	}

}
