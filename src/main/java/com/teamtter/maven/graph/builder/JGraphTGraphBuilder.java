package com.teamtter.maven.graph.builder;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import javax.imageio.ImageIO;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.teamtter.maven.graph.data.GraphModel;
import com.teamtter.maven.graph.data.SCMRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JGraphTGraphBuilder implements GraphBuilder {

	private GraphModel		model;
	private List<String>	acceptedUrlFilters;

	public JGraphTGraphBuilder(GraphModel model, List<String> acceptedUrlFilters) {
		this.acceptedUrlFilters = acceptedUrlFilters;
		this.model = model;
	}

	@Override
	public void generateImage(File outputFile) {
		DirectedGraph<?, ?> graph = createGraphFromModel();
		try {
			generateJGraphXImageFromGraph(outputFile, graph);
		} catch (Exception e) {
			log.error("Error with JGraph...", e);
		}
	}

	/** public for tests :/ */
	public static void generateJGraphXImageFromGraph(File outputFile, Graph<?, ?> graph) {
		JGraphXAdapter<?, ?> graphAdapter = new JGraphXAdapter(graph);
		mxGraphComponent mxc = new mxGraphComponent(graphAdapter);
		
		graphAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_NOLABEL, "1");

		mxGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
		layout.execute(graphAdapter.getDefaultParent());

		BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 1, Color.WHITE, true, null);
		try {
			ImageIO.write(image, "PNG", outputFile);
		} catch (IOException e) {
			log.error("Impossible to create image in {}", outputFile, e);
		}
	}

	private DirectedGraph<SCMRepo, DefaultEdge> createGraphFromModel() {
		DirectedGraph<SCMRepo, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		// @formatter:off
		Predicate<SCMRepo> filterOnAcceptedUrlPredicate = buildFilterOnAcceptedUrlPredicate();
		model.getRepos().stream()
				.filter(filterOnAcceptedUrlPredicate)
				.forEach(repo -> graph.addVertex(repo));
		model.getRepos().stream()
				.filter(filterOnAcceptedUrlPredicate)
				.forEach(repo -> {
					repo.getReferencedRepo().stream()
							.filter(filterOnAcceptedUrlPredicate)
							.forEach(referencedRepo -> graph.addEdge(repo, referencedRepo));
				});
		// @formatter:on
		return graph;
	}

	private Predicate<SCMRepo> buildFilterOnAcceptedUrlPredicate() {
		return new Predicate<SCMRepo>() {

			@Override
			public boolean test(SCMRepo t) {
				String url = t.getFilteredRepoUrl();
				boolean anyMatch = acceptedUrlFilters.stream().anyMatch(filter -> url.contains(filter));
				return anyMatch;
			}
		};
	}

}
