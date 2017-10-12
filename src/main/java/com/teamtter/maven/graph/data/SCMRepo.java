package com.teamtter.maven.graph.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = { "filteredRepoUrl" })
public class SCMRepo {
	@Getter
	private String			filteredRepoUrl;

	private List<MavenGAV>	gavs			= new ArrayList<>();
	@Getter
	private Set<SCMRepo>	referencedRepo	= new HashSet<>();

	public SCMRepo(String filteredRepoUrl) {
		this.filteredRepoUrl = filteredRepoUrl;
	}

	public void addDependency(MavenGAV gav) {
		gavs.add(gav);
	}

	public boolean matchesUrl(String url) {
		return filteredRepoUrl.equals(url);
	}

	public void addDependencyTo(SCMRepo repoTo) {
		referencedRepo.add(repoTo);
	}
	
	@Override
	public String toString() {
		return getFilteredRepoUrl();
	}
}
