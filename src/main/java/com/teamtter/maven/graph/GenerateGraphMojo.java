package com.teamtter.maven.graph;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.teamtter.maven.graph.data.GraphModel;

import lombok.Setter;;

/** Generates a picture/graph of the dependencies considering only their original scm repository */
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

	@Setter
	@Component
	private GraphBuilder			graphBuilder;

	/** Holds various parameters like location of the remote / local repositories, etc... */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession			session;

	/** Contains the full list of projects in the reactor. */
	@Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
	private List<MavenProject>		reactorProjects;

	/** Only the scm containing one of those token will be displayed in the graph. */
	@Parameter
	@Setter
	private Set<String>				acceptedUrlFilters	= new HashSet<>();

	/** Name of the file to be created in ${project.build.directory} */
	@Parameter
	@Setter
	private String					pngFileName			= "scmDependencyGraph.png";

	/** Strings that will be stripped from the repo name in the graph, making it clearer */
	@Parameter
	@Setter
	private Set<String>				strippedSubstrings	= new HashSet<>();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("Skipping execution due to 'skip' == true");
		} else {
			try {
				generateGraph();
			} catch (MojoExecutionException e) {
				throw e;
			} catch (Exception e) {
				throw new MojoExecutionException("Unexpected error", e);
			}
		}
	}

	private void generateGraph() throws ProjectBuildingException, IOException, MojoExecutionException, DependencyGraphBuilderException {

		final ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
		buildingRequest.setProject(mavenProject);
		DependencyNode rootNode = dependencyGraphBuilder.buildDependencyGraph(buildingRequest,
				null/*new ScopeArtifactFilter(Artifact.SCOPE_TEST)*/,
				reactorProjects);

		GraphModel model = new GraphModel(mavenProject);

		DependencyNodeVisitor visitor = new DependencyNodeVisitor() {
			private Map<DependencyNode, MavenProject> nodesToProjectCache = new HashMap<>();

			@Override
			public boolean visit(DependencyNode node) {
				if (node.getParent() != null) {
					try {
						MavenProject mavenProjectFrom = buildMavenProjectFromNode(buildingRequest, node.getParent());
						MavenProject mavenProjectTo = buildMavenProjectFromNode(buildingRequest, node);

						model.addDependency(mavenProjectFrom, mavenProjectTo);
					} catch (ProjectBuildingException e) {
						getLog().error("Impossible to construct Project from dependency: " + node.getArtifact().getArtifactId(), e);
					}
				}
				return true;
			}

			private MavenProject buildMavenProjectFromNode(final ProjectBuildingRequest buildingRequest, DependencyNode node) throws ProjectBuildingException {
				MavenProject mavenProject;
				MavenProject existingMavenProject = nodesToProjectCache.get(node);
				if (existingMavenProject == null) {
					buildingRequest.setProject(null);
					ProjectBuildingResult projectBuildResultFrom = projectBuilder.build(node.getArtifact(), buildingRequest);
					mavenProject = projectBuildResultFrom.getProject();
					nodesToProjectCache.put(node, mavenProject);	// populate cache
				} else {
					mavenProject = existingMavenProject;
				}
				return mavenProject;
			}

			@Override
			public boolean endVisit(DependencyNode node) {
				return true;
			}
		};
		rootNode.accept(visitor);	// will fill-in the 'model'

		String buildDirectory = mavenProject.getBuild().getDirectory();
		File outputPngFile = new File(buildDirectory, pngFileName);
		File parentDir = outputPngFile.getParentFile();
		if (parentDir.exists() && !parentDir.isDirectory()) {
			throw new MojoExecutionException("directory " + buildDirectory + " is not a directory !");
		} else if (!parentDir.exists()) {
			parentDir.mkdirs();
		}
		graphBuilder.generateImage(model, acceptedUrlFilters, strippedSubstrings, outputPngFile);
	}

}
