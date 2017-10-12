package com.teamtter.maven.graph.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GraphModel {
	@Getter
	private List<SCMRepo>	repos	= new ArrayList<>();
	private MavenProject	mainMavenProject;

	public GraphModel(MavenProject mainMavenProject) {
		this.mainMavenProject = mainMavenProject;

		SCMRepo repo = initOrGetRepo(mainMavenProject);
		MavenGAV mavenGav = computeMavenGav(mainMavenProject);
		repo.addDependency(mavenGav);
	}
	
	// TODO: the SCMRepo object should encapsulate the full info (all scm content + GAV Info
	// so that we can decide later on how we display it...
	private SCMRepo initOrGetRepo(MavenProject mavenProject) {
		String scmUrl = computeScm(mavenProject);

		SCMRepo repo = findExistingSCMRepoByScmUrl(scmUrl).orElseGet(() -> {
			SCMRepo newRepo = new SCMRepo(scmUrl);
			repos.add(newRepo);
			return newRepo;
		});

		return repo;
	}

	private Optional<SCMRepo> findExistingSCMRepoByScmUrl(String scmUrl) {
		Optional<SCMRepo> matchingRepo = repos.stream()
				.filter(repo -> repo.matchesUrl(scmUrl))
				.findFirst();
		return matchingRepo;
	}

	private MavenGAV computeMavenGav(MavenProject mavenProject) {
		MavenGAV gav = new MavenGAV(mavenProject.getGroupId(),
				mavenProject.getArtifactId(),
				mavenProject.getVersion());
		return gav;
	}

	private String computeScm(MavenProject mavenProject) {
		Scm scm = mavenProject.getScm();
		String scmUrl = null;
		if (scm == null) {
			String gavString = buildGAVString(mavenProject);
			log.warn(gavString + " has no scm defined in its MavenProject !");
			scmUrl = gavString;
		} else {
			scmUrl = scm.getDeveloperConnection();
			if (scmUrl == null) {
				scmUrl = scm.getConnection();
			}
			if (scmUrl == null) {
				scmUrl = scm.getUrl();
			}
			scmUrl = cleanScmUrl(scmUrl);
		}


		log.info("{} scm: {}", mavenProject.getArtifactId(), scmUrl);

		return scmUrl;
	}

	private String cleanScmUrl(String scmUrl) {

		List<String> patternsToRemove = Arrays.asList("git:", "ssh://", "http://", "https://", "scm:", "git@");
		for (String pattern : patternsToRemove) {
			scmUrl = StringUtils.remove(scmUrl, pattern);
		}

		if (scmUrl.contains(".git")) {
			scmUrl = StringUtils.substringBefore(scmUrl, ".git");
		} else {
			String[] parts = scmUrl.split("/");
			if (parts.length >= 2) {
				scmUrl = parts[0] + "/" + parts[1];
			} else {
				// keep original scmUrl as is...
			}
		}
		return scmUrl;
	}

	private static String buildGAVString(MavenProject mavenProject) {
		return mavenProject.getGroupId() + ":" + mavenProject.getArtifactId() + ":" + mavenProject.getVersion();
	}

	public void addDependency(MavenProject from, MavenProject to) {
		SCMRepo repoFrom = initOrGetRepo(from);
		MavenGAV mavenGavFrom = computeMavenGav(from);
		repoFrom.addDependency(mavenGavFrom);

		SCMRepo repoTo = initOrGetRepo(to);
		MavenGAV mavenGavTo = computeMavenGav(to);
		repoFrom.addDependency(mavenGavTo);
		
		if (mavenGavTo.getArtifactId().contains("core-gui")) {
			log.warn("core-gui:: {} --> {}", mavenGavFrom, mavenGavTo);
		}

		repoFrom.addDependencyTo(repoTo);
	}
}
