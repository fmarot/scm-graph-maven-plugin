package com.teamtter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import lombok.extern.slf4j;


/** Unpacks native dependencies */
@Mojo(name = "copy" /** the goal */
, threadSafe = false /** until proven otherwise, false */
, defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST, requiresProject = true)
@Slf4j
public class MyMojo extends AbstractMojo {

	private static final String ALREADY_UNPACKED_ARTIFACTS_INFO_FILE = "alreadyUnpackedArtifactsInfo.json";

	public static final String NATIVES_PREFIX = "natives-";

	@Parameter(defaultValue = "${project}", readonly = true)
	@Setter
	private MavenProject mavenProject;

	// @Parameter(property = "nativesTargetDir", defaultValue = "${project.build.directory}/natives")
	/**
	 * by default, in case of a multi module project, we will unpack ALL NATIVES to
	 * the same dir, thus saving space and unzip time while allowing all interdependent
	 * projects to benefit from the presence of native libs
	 */
	@Parameter(property = "nativesTargetDir", defaultValue = "${session.executionRootDirectory}/target/natives")
	@Setter
	private File nativesTargetDir;

	@Parameter(property = "separateDirs", defaultValue = "false")
	@Setter
	private boolean separateDirs;

	@Parameter(property = "autoDetectOSNatives", defaultValue = "false")
	@Setter
	private boolean autoDetectOSNatives;

	@Parameter(property = "skip", defaultValue = "false")
	@Setter
	private boolean skip;

	@Parameter
	@Setter
	private List<OsFilter> osFilters = new ArrayList<>();

	@Component
	@Setter
	private IArtifactHandler artifactHandler;

	@Component
	@Setter
	private BuildContext buildContext;



	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		initOsFiltersIfNeeded();
		if (skip) {
			log.info("Skipping execution due to 'skip' == true");
		} else {
			copyNativeDependencies();
		}
	}



}
