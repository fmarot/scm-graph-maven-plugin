package com.teamtter.maven.graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
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
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;

import com.teamtter.maven.graph.builder.GraphBuilder;
import com.teamtter.maven.graph.builder.JGraphTGraphBuilder;
import com.teamtter.maven.graph.data.GraphModel;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;;

/** Generates a picture/graph of the dependencies considering only their original scm repository */
@Slf4j
@Mojo(name = "generate", // the goal
		defaultPhase = LifecyclePhase.VALIDATE,	//
		requiresProject = true,					//
		threadSafe = false,						//
		requiresDependencyCollection = ResolutionScope.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE)	// ??? TEST ? COMPILE ??? OTHER ??
public class GenerateGraphMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true)
	@Setter
	private MavenProject			mavenProject;

	@Parameter(property = "skip", defaultValue = "false")
	@Setter
	private boolean					skip;

	@Component
	private ProjectBuilder			projectBuilder;

	@Component(hint = "default")
	private DependencyGraphBuilder	dependencyGraphBuilder;

	/** Holds various parameters like location of the remote / local repositories, etc... */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession			session;

	/** Contains the full list of projects in the reactor. */
	@Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
	private List<MavenProject>		reactorProjects;

	/** Only the scm containing one of those token will be displayed in the graph. */
	@Parameter
	@Setter
	private List<String>			acceptedUrlFilters	= new ArrayList<>();

	/** Name of the file to be created in ${project.build.directory} */
	@Parameter
	@Setter
	private String					pngFileName			= "scmDependencyGraph.png";

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

	private void generateGraph() throws ProjectBuildingException, IOException, MojoExecutionException, DependencyGraphBuilderException {

		final ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
		buildingRequest.setProject(mavenProject);
		DependencyNode rootNode = dependencyGraphBuilder.buildDependencyGraph(buildingRequest, new ScopeArtifactFilter(Artifact.SCOPE_TEST), reactorProjects);

		GraphModel model = new GraphModel(mavenProject);

		DependencyNodeVisitor visitor = new DependencyNodeVisitor() {

			@Override
			public boolean visit(DependencyNode node) {
				Artifact dependency = node.getArtifact();
				if (node.getParent() != null) {
					try {
						buildingRequest.setProject(null);
						ProjectBuildingResult projectBuildResultFrom = projectBuilder.build(node.getParent().getArtifact(), buildingRequest);
						MavenProject mavenProjectFrom = projectBuildResultFrom.getProject();

						buildingRequest.setProject(null);
						ProjectBuildingResult projectBuildResultTo = projectBuilder.build(dependency, buildingRequest);
						MavenProject mavenProjectTo = projectBuildResultTo.getProject();

						model.addDependency(mavenProjectFrom, mavenProjectTo);
					} catch (ProjectBuildingException e) {
						log.error("Impossible to construct Project from dependency: {}", dependency.getArtifactId(), e);
					}
				}
				return true;
			}

			@Override
			public boolean endVisit(DependencyNode node) {
				return true;
			}
		};
		rootNode.accept(visitor);	// will fill-in the 'model'

		GraphBuilder graphBuilder = new JGraphTGraphBuilder(model, acceptedUrlFilters);
		String buildDirectory = mavenProject.getBuild().getDirectory();
		graphBuilder.generateImage(new File(buildDirectory, pngFileName));
	}

}
