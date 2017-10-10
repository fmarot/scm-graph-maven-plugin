package com.teamtter.maven.graph.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MavenGAV {
	private String	groupId;
	private String	artifactId;
	private String	version;
}
