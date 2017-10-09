//package com.teamtter.maven;
//
//import org.junit.Test;
//
////public class GenerateGraphMojoTest {
//
////	@Rule
////	public MojoRule mojoRule = new MojoRule();
//
//	//	@Test
//	//	public void testSomething() throws Exception {
//	//		myMojo.execute();
//	//		File testPomFile = new File("src/test/resources/test1-pom.xml");
//	//		GenerateGraphMojo myMojo = (GenerateGraphMojo) rule.lookupMojo("generate", testPomFile);
//	//		
//	//		
//	//		MavenSession session = rule.newMavenSession(myMojo);
//	//		
//	//		assertNotNull(myMojo);
//	//		myMojo.execute();
//	//	}
//
////	@Test
////	public void aaa() throws Exception {
////		File basedir = new File("src/test/resources/");
////		File pomFile = new File("src/test/resources/pom.xml");
////
////		MavenProject project = mojoRule.readMavenProject(basedir);
////		MavenSession session = mojoRule.newMavenSession(project);
////		MojoExecution execution = mojoRule.newMojoExecution("generate");
////		mojoRule.executeMojo(session, project, execution);
////		
////		GenerateGraphMojo myMojo = (GenerateGraphMojo) mojoRule.lookupMojo("generate", pomFile);
////		
////		Assert.assertNotNull(myMojo);
//		//		    String expected = new File(basedir, "target/generated-sources/twirl").getAbsolutePath();
//		//		    assertThat(project.getCompileSourceRoots()).contains(expected);
////	}
//	
//
////}