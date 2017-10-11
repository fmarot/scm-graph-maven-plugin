package com.teamtter.maven.graph;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jgraph.JGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;
import com.teamtter.maven.graph.data.GraphModel;
import com.teamtter.maven.graph.data.SCMRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JGraphTGraphBuilder implements GraphBuilder {

	private GraphModel model;

	public JGraphTGraphBuilder(GraphModel model) {
		this.model = model;
	}

	@Override
	public void generateImage(File outputFile) {
		DirectedGraph<?, ?> graph = createGraphFromModel();
		generateImageFromGraph(outputFile, graph);
	}

	/** public for tests :/ */
	public static void generateImageFromGraph(File outputFile, Graph<?, ?> graph) {
		JGraphModelAdapter<?, ?> graphAdapter = new JGraphModelAdapter<>(graph);
		JGraph jGraph = new JGraph(graphAdapter);
		// jGraph.setBounds(0, 0, 900, 600);
		// jGraph.doLayout();
		// jGraph.repaint();

		// JFrame frame = new JFrame();

		JGraphLayout layout = new JGraphHierarchicalLayout(); // or whatever layouting algorithm
		JGraphFacade facade = new JGraphFacade(jGraph);
		layout.run(facade);
		Map nested = facade.createNestedMap(false, false);
		jGraph.getGraphLayoutCache().edit(nested);
		jGraph.setSize(6000, 9000);
		// JScrollPane sp = new JScrollPane(jGraph);

		// frame.add(sp);
		// frame.setSize(600, 900);
		// frame.pack();
		// frame.setVisible(true);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		BufferedImage image = jGraph.getImage(Color.WHITE, 5);
		try {
			ImageIO.write(image, "PNG", outputFile);
		} catch (IOException e) {
			log.error("Impossible to create image in {}", outputFile, e);
		}
	}

	private DirectedGraph<SCMRepo, DefaultEdge> createGraphFromModel() {
		DirectedGraph<SCMRepo, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		model.getRepos().forEach(repo -> graph.addVertex(repo));
		model.getRepos().forEach(repo -> {
			repo.getReferencedRepo().forEach(referencedRepo -> graph.addEdge(repo, referencedRepo));
		});
		return graph;
	}

}
