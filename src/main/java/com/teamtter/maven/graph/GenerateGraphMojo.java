package com.teamtter.maven.graph;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.jgraph.JGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;

import com.teamtter.maven.graph.data.SCMRepo;
import com.teamtter.maven.graph.data.SCMRepoRepository;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;;

/** Generates a picture/graph of the dependencies considering only their original scm repository */
@Slf4j
@Mojo(name = "generate", // the goal
		defaultPhase = LifecyclePhase.VALIDATE,	//
		requiresProject = true,					//
		threadSafe = false,						//
		requiresDependencyResolution = ResolutionScope.TEST)
public class GenerateGraphMojo extends AbstractMojo implements Contextualizable {

	@Parameter(defaultValue = "${project}", readonly = true)
	@Setter
	private MavenProject	mavenProject;

	@Parameter(property = "skip", defaultValue = "false")
	@Setter
	private boolean			skip;

	@Component
	private ProjectBuilder	projectBuilder;

	/** holds various parameters like location of the remote / local repositories, etc */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession	session;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			log.info("Skipping execution due to 'skip' == true");
		} else {
			try {
				generateGraph();
			} catch (Exception e) {
				throw new MojoExecutionException("Unexpected error", e);
			}
		}
	}

	private void generateGraph() throws ProjectBuildingException, IOException {
		ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());

		SCMRepoRepository repo = new SCMRepoRepository(mavenProject);

		Set<Artifact> dependencies = mavenProject.getArtifacts();
		for (Artifact dependency : dependencies) {
			buildingRequest.setProject(null); // was it changed by previous calls ???
			try {
				ProjectBuildingResult projectBuildResult = projectBuilder.build(dependency, buildingRequest);
				MavenProject dependencyProject = projectBuildResult.getProject();
				repo.addDependency(mavenProject, dependencyProject);
			} catch (ProjectBuildingException e) {
				log.error("Impossible to construct Project from dependency: {}", dependency.getArtifactId(), e);
			}

			// TODO: recursivity For The Win !
		}

		JGraphModelAdapter<SCMRepo, DefaultEdge> graphAdapter = new JGraphModelAdapter<>(repo.getGraph());
		JGraph jGraph = new JGraph(graphAdapter);
		BufferedImage image = jGraph.getImage(Color.WHITE, 5);
		ImageIO.write(image, "PNG", new File("out.png"));
	}

	@Override
	public void contextualize(Context context) throws ContextException {
		log.info("XXXZZZAZERTY");
	}

}
