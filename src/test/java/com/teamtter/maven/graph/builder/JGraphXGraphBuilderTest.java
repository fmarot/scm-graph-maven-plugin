package com.teamtter.maven.graph.builder;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.teamtter.maven.graph.data.GraphModel;
import com.teamtter.maven.graph.data.MavenGAV;

public class JGraphXGraphBuilderTest {

	@Test
	public void testGenerateGraph() {
		JGraphXGraphBuilder graphBuilder = new JGraphXGraphBuilder();

		Set<String> acceptedUrlFilters = Sets.newHashSet("olea", "fma");
		Set<String> strippedSubstrings = new HashSet<>();
		GraphModel model = buildGraphModel();
		// WARNING: this test is rather dummy for now...
		File outputFile = new File("./target/testFile.svg");
		if (outputFile.exists()) {
			outputFile.delete();
		}
		
		graphBuilder.generateImage(model, acceptedUrlFilters, strippedSubstrings, outputFile);
		Assert.assertTrue(outputFile.exists());
	}

	private GraphModel buildGraphModel() {
		MavenGAV mainMavenGav = new MavenGAV("com.olea", "artifactId", "1.0.0");
		GraphModel model = new GraphModel("myRepo-olea.git", mainMavenGav);
		String scmFrom = "repo1-olea.git";
		MavenGAV mavenGavFrom = new MavenGAV("com.tests", "artifactIdToto", "1.0.1");
		String scmTo = "repo2-olea.git";
		MavenGAV mavenGavTo = new MavenGAV("com.tests", "artifactIdTiti", "1.0.2");
		model.addDependency(scmFrom, mavenGavFrom, scmTo, mavenGavTo);
		return model;
	}

}
