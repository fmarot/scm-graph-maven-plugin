package com.teamtter.maven.graph.data;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = { "repo" })
public class SCMRepo {

	private String			repo;

	private List<MavenGAV>	gavs	= new ArrayList<>();

	public SCMRepo(String repo) {
		this.repo = repo;
	}

	public void addDependency(MavenGAV gav) {
		gavs.add(gav);
	}
	
	public boolean matchesUrl(String url) {
		return repo.equals(url);
	}

	@Override
	public String toString() {
		return repo;
	}
}
