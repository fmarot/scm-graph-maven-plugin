package com.teamtter.maven.graph.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class MavenGAV {
	private String	groupId;
	private String	artifactId;
	private String	version;
}
