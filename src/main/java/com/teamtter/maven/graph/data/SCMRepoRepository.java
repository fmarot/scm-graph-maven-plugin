package com.teamtter.maven.graph.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SCMRepoRepository {
	private List<SCMRepo>						repos	= new ArrayList<>();
	private MavenProject						mainMavenProject;
	@Getter
	private DirectedGraph<SCMRepo, DefaultEdge>	graph	= new DefaultDirectedGraph<>(DefaultEdge.class);

	public SCMRepoRepository(MavenProject mainMavenProject) {
		this.mainMavenProject = mainMavenProject;

		SCMRepo repo = initOrGetRepo(mainMavenProject);

		graph.addVertex(repo);
	}

	private SCMRepo initOrGetRepo(MavenProject mavenProject) {
		String scmUrl = computeScm(mavenProject);
		MavenGAV mavenGav = computeMavenGav(mavenProject);

		SCMRepo repo = findExistingSCMRepoByScmUrl(scmUrl).orElseGet(() -> {
			SCMRepo newRepo = new SCMRepo(scmUrl);
			repos.add(newRepo);
			graph.addVertex(newRepo);
			return newRepo;
		});
		repo.addDependency(mavenGav);
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
			log.warn("{}:{}:{} has no scm defined in its MavenProject !",
					mavenProject.getGroupId(),
					mavenProject.getArtifactId(),
					mavenProject.getVersion());
			scmUrl = mavenProject.getArtifactId();
		} else {
			scmUrl = scm.getDeveloperConnection();
			if (scmUrl == null) {
				scmUrl = scm.getConnection();
			}
			if (scmUrl == null) {
				scmUrl = scm.getUrl();
			}
		}
		log.info("{} scm: {}", mavenProject.getArtifactId(), scmUrl);

		return scmUrl;
	}

	public void addDependency(MavenProject from, MavenProject to) {
		SCMRepo repoFrom = initOrGetRepo(from);
		SCMRepo repoTo = initOrGetRepo(to);
		graph.addEdge(repoFrom, repoTo);
	}
}
