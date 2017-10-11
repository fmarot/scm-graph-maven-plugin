package com.teamtter.maven.graph.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString(of = { "repo" })
@EqualsAndHashCode(of = { "repo" })
public class SCMRepo {

	private String			repo;

	private List<MavenGAV>	gavs			= new ArrayList<>();
	@Getter
	private Set<SCMRepo>	referencedRepo	= new HashSet<>();

	public SCMRepo(String repo) {
		this.repo = repo;
	}

	public void addDependency(MavenGAV gav) {
		gavs.add(gav);
	}

	public boolean matchesUrl(String url) {
		return repo.equals(url);
	}

	public void addDependencyTo(SCMRepo repoTo) {
		referencedRepo.add(repoTo);
	}
}
