package com.teamtter.maven.graph.builder;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.teamtter.maven.graph.data.SCMRepo;

import lombok.experimental.Delegate;

/** strips some strings from the toString method (to display only meaningfull url parts in the graph).
 * I know, my implem of the decorator pattern using extends + @delegate is really dirty ! :/ */
public class SCMRepoDecorator extends SCMRepo {
	@Delegate
	private SCMRepo			repo;
	private Set<String>	strippedSubstrings;

	public SCMRepoDecorator(SCMRepo repo, Set<String> strippedSubstrings) {
		super(repo.getFilteredRepoUrl());
		this.repo = repo;
		this.strippedSubstrings = strippedSubstrings;
	}

	@Override
	public String toString() {
		String repoName = repo.getFilteredRepoUrl();
		for (String toDelete : strippedSubstrings) {
			repoName = StringUtils.replace(repoName, toDelete, "");
		}
		return repoName;
	}

}
