package com.teamtter;

import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Scm;
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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;;


/** Unpacks native dependencies */
@Mojo(name = "generate", /** the goal */
	threadSafe = true,
	defaultPhase = LifecyclePhase.COMPILE,
	requiresDependencyResolution = ResolutionScope.TEST,
	requiresProject = true)
@Slf4j
public class GenerateGraphMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true)
	@Setter
	private MavenProject mavenProject;

	@Parameter(property = "skip", defaultValue = "false")
	@Setter
	private boolean skip;

	@Component
    private ProjectBuilder projectBuilder;
	
	/** holds various parameters like location of the remote / local repositories, etc */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

//	@Component
//	@Setter
//	private IArtifactHandler artifactHandler;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			log.info("Skipping execution due to 'skip' == true");
		} else {
			try {
				generateGraph();
			}catch (Exception e) {
				throw new MojoExecutionException("Unexpected error", e);
			}
		}
	}

	private void generateGraph() throws ProjectBuildingException {
		ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());

		// direct dependencies
		Set<Artifact> dependencies = mavenProject.getDependencyArtifacts();

		for (Artifact dependency : dependencies) {
			buildingRequest.setProject(null); // was it changed by previous calls ???
			ProjectBuildingResult projectBuildResult = projectBuilder.build(dependency, buildingRequest);
			MavenProject dependencyProject = projectBuildResult.getProject();

			Scm scm = dependencyProject.getScm();

			log.info("{}", scm);

			// TODO: recursivity For The Win !

		};

	}


}
