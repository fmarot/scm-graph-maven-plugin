package com.teamtter.maven.graph.builder;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.plexus.component.annotations.Component;
import org.w3c.dom.Document;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.teamtter.maven.graph.data.GraphModel;
import com.teamtter.maven.graph.data.SCMRepo;

import lombok.extern.slf4j.Slf4j;

@Component(role = GraphBuilder.class)
@Slf4j
public class JGraphXGraphBuilder implements GraphBuilder {

	@Override
	public void generateImage(GraphModel model, Set<String> acceptedUrlFilters, Set<String> strippedSubstrings, File outputFile) {
		mxGraph graph = createGraphFromModel(model, acceptedUrlFilters, strippedSubstrings);
		try {
			generateJGraphXImageFromGraph(graph, outputFile);
		} catch (Exception e) {
			log.error("Error with JGraph...", e);
		}
	}

	private void generateJGraphXImageFromGraph(mxGraph graph, File outputFile) {
		mxGraphLayout layout = new mxHierarchicalLayout(graph);
		layout.execute(graph.getDefaultParent());
		createSVGFile(graph, outputFile);
//		createPNGFile(graph, outputFile);
	}

	private void createSVGFile(mxGraph graph, File outputFile) {
		
		Document image = mxCellRenderer.createSvgDocument(graph, null, 1, Color.WHITE, new mxRectangle(0,0,10000,10000));
		
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			Result output = new StreamResult(outputFile);
			Source input = new DOMSource(image);
			transformer.transform(input, output);
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			log.error("", e);
		}
	}

	private void createPNGFile(mxGraph graph, File outputFile) {
		BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, null);
		if (image == null) {
			log.error("No generated image. Maybe you have too strong filters and no scm is left in the graph !");
		} else {
			try {
				ImageIO.write(image, "PNG", outputFile);
			} catch (IOException e) {
				log.error("Impossible to create image in {}", outputFile, e);
			}
		}
	}

	private mxGraph createGraphFromModel(GraphModel model, Set<String> acceptedUrlFilters, Set<String> strippedSubstrings) {
		//		DirectedGraph<SCMRepo, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		List<SCMRepoDecorator> decoratedRepos = model.getRepos().stream()
				.map(repo -> new SCMRepoDecorator(repo, strippedSubstrings))
				.collect(Collectors.toList());
		Map<SCMRepoDecorator, Object> repoToVertex = new HashMap<>();
		Predicate<SCMRepo> filterOnAcceptedUrlPredicate = buildFilterOnAcceptedUrlPredicate(acceptedUrlFilters);

		mxGraph graph = buildGraphWithStyle();

		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			// @formatter:off
			decoratedRepos.stream()
					.filter(filterOnAcceptedUrlPredicate)
					.forEach(repo -> {
						Object vertex = graph.insertVertex(parent, null, repo, 0, 0, 50, 40);
						repoToVertex.put(repo, vertex);
					});
			decoratedRepos.stream()
					.filter(filterOnAcceptedUrlPredicate)
					.forEach(repo -> {
						repo.getReferencedRepo().stream()
								.filter(filterOnAcceptedUrlPredicate)
								.forEach(referencedRepo -> {
									SCMRepoDecorator decoratedRepo = getDecoratedRepoFrom(referencedRepo, decoratedRepos);
									Object referencedVertex = repoToVertex.get(decoratedRepo);
									Object sourceRepo = repoToVertex.get(repo);
									graph.insertEdge(parent, "", null, sourceRepo, referencedVertex);
								});
					});
		// @formatter:on
		} finally {
			repoToVertex.values().forEach(vertex -> graph.updateCellSize(vertex));

			graph.getModel().endUpdate();
		}
		return graph;
	}

	private mxGraph buildGraphWithStyle() {
		mxGraph graph = new mxGraph();

		Map<String, Object> edgeStyle = graph.getStylesheet().getDefaultEdgeStyle();
		edgeStyle.put(mxConstants.STYLE_NOLABEL, "1");

		Map<String, Object> vertexStyle = graph.getStylesheet().getDefaultVertexStyle();
		vertexStyle.put(mxConstants.STYLE_FONTFAMILY, "Verdana");
		vertexStyle.put(mxConstants.STYLE_FONTCOLOR, "Black");
		vertexStyle.put(mxConstants.STYLE_FONTSIZE, 18);

		graph.setAutoSizeCells(true);	// to be defined BEFORE adding vertices in the graph
		graph.setCellsResizable(true);
		return graph;
	}

	private SCMRepoDecorator getDecoratedRepoFrom(SCMRepo referencedRepo, List<SCMRepoDecorator> decoratedRepos) {
		SCMRepoDecorator matchingDecorated = decoratedRepos.stream()
				.filter(repo -> repo.getFilteredRepoUrl().equals(referencedRepo.getFilteredRepoUrl()))
				.findFirst().get();
		return matchingDecorated;
	}

	private Predicate<SCMRepo> buildFilterOnAcceptedUrlPredicate(Set<String> acceptedUrlFilters) {
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
