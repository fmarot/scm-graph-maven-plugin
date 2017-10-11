package com.teamtter.maven.graph;

import java.io.File;
import java.io.IOException;
import java.util.Set;

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

import com.google.common.base.Strings;
import com.teamtter.maven.graph.data.GraphModel;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;;

/** Generates a picture/graph of the dependencies considering only their original scm repository */
@Slf4j
@Mojo(name = "generate", // the goal
		defaultPhase = LifecyclePhase.VALIDATE,	//
		requiresProject = true,					//
		threadSafe = false,						//
		requiresDependencyCollection = ResolutionScope.TEST,
		requiresDependencyResolution = ResolutionScope.TEST)
public class GenerateGraphMojo extends AbstractMojo {

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
		GraphModel model = new GraphModel(mavenProject);
		fillInGraphModelRecurse(mavenProject, model, 0);

		GraphBuilder graphBuilder = new JGraphTGraphBuilder(model);
		graphBuilder.generateImage(new File("out.png"));
	}

	private void fillInGraphModelRecurse(MavenProject proj, GraphModel repo, int depth) throws ProjectBuildingException, IOException {
		ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
		log.info(Strings.repeat(" ", depth) + "Analyzing dependencies of {}", proj.getArtifactId());

		Set<Artifact> dependencies = proj.getArtifacts();
		log.info(Strings.repeat(" ", depth) + "--> {} to be analyzed", dependencies.size());
		for (Artifact dependency : dependencies) {
			buildingRequest.setProject(null);
			try {
				ProjectBuildingResult projectBuildResult = projectBuilder.build(dependency, buildingRequest);
				MavenProject dependencyProject = projectBuildResult.getProject();
				repo.addDependency(proj, dependencyProject);
				int newDepth = depth + 1;
				fillInGraphModelRecurse(dependencyProject, repo, newDepth);
			} catch (ProjectBuildingException e) {
				log.error("Impossible to construct Project from dependency: {}", dependency.getArtifactId(), e);
			}
		}
	}

}
