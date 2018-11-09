package com.yevhenpanko.optimizer;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.*;

@Mojo(name = "optimize", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class Optimizer extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Component
    private ProjectBuilder projectBuilder;

    public void execute() throws MojoExecutionException {
        getLog().info("--- Optimizing dependencies ---");

        final ProjectBuildingRequest sessionProjectBuildingRequest = session.getProjectBuildingRequest();
        final ProjectBuildingRequest projectBuildingRequest = new DefaultProjectBuildingRequest(
                sessionProjectBuildingRequest
        );
        projectBuildingRequest.setResolveDependencies(true);

        try {
            for (Artifact artifact : project.getArtifacts()) {
                projectBuildingRequest.setProject(null);
                final MavenProject mavenProject = projectBuilder.build(artifact, projectBuildingRequest).getProject();

                getLog().info(formatArtifactInfo(artifact));
            }
        } catch (ProjectBuildingException e) {
            throw new MojoExecutionException("Error while building project", e);
        }
    }

    private String formatArtifactInfo(Artifact artifact){
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
    }
}
