package at.bestsolution.efxclipse.tooling.pde.ui.templates.tycho

import at.bestsolution.efxclipse.tooling.rrobot.model.task.Generator
import at.bestsolution.efxclipse.tooling.rrobot.model.task.DynamicFile
import java.util.Map
import java.io.ByteArrayInputStream
import static extension at.bestsolution.efxclipse.tooling.pde.ui.templates.tycho.MavenUtils.*

class FeaturePomGenerator implements Generator<DynamicFile> {
	
	override generate(DynamicFile file, Map<String,Object> data) {
		val relengGroupId = file.variables.findFirst([e| e.key.equals("relengGroupId")]).defaultValue;
		val relengArtifactId = file.variables.findFirst([e| e.key.equals("relengArtifactId")]).defaultValue;
		val relengVersion    = file.variables.findFirst([e| e.key.equals("baseVersion")]).defaultValue.toPomVersion;
		val relengPath       = file.variables.findFirst([e| e.key.equals("relengPath")]).defaultValue;
		
		val groupId = file.variables.findFirst([e| e.key.equals("groupId")]).defaultValue;
		val artifactId	= file.variables.findFirst([e| e.key.equals("artifactId")]).defaultValue;
		val name = file.variables.findFirst([e| e.key.equals("name")]).defaultValue;
		
		val pomData = new PomData(
			name, 
			groupId, 
			artifactId, 
			relengGroupId, 
			relengArtifactId, 
			relengVersion, 
			relengPath
		)
		return new ByteArrayInputStream(generate(pomData).toString.bytes);
	}
	
	def generate(PomData data) '''<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<name>«data.description»</name>
	<groupId>«data.groupId»</groupId>
	<artifactId>«data.artifactId»</artifactId>
	<packaging>eclipse-feature</packaging>

	<parent>
		<groupId>«data.parentGroupId»</groupId>
		<artifactId>«data.parentArtifactId»</artifactId>
		<relativePath>«data.parentPomPath»/pom.xml</relativePath>
		<version>«data.parentVersion»</version>
	</parent>

</project>'''

	
}