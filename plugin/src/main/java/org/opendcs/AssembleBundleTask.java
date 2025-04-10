package org.opendcs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class AssembleBundleTask extends DefaultTask
{

    @OutputDirectory
    final DirectoryProperty outputDirectory = getProject().getObjects()
                                                          .directoryProperty()
                                                          .value(getProject().getLayout().getBuildDirectory().dir("bundle"));

    final Property<MavenPublication> publication = getProject().getObjects().property(MavenPublication.class);

    @TaskAction
    public void assemble()
    {
        for ( var dep : this.getTaskDependencies().getDependencies(this))
        {
            System.out.println("Dep Task: " + dep.getName());
        }
        var outputDir = outputDirectory.get().getAsFile();
        outputDir.mkdirs();
        final var pub = publication.get();  
        final var groupFolder = Path.of(outputDir.getAbsolutePath(), pub.getGroupId().split("\\."));
        final var artifactFolder = new File(groupFolder.toFile(),pub.getArtifactId());
        final var versionFolder = new File(artifactFolder, pub.getVersion());
        versionFolder.mkdirs();
        pub.getArtifacts().all(artifact ->
        {
            try
            {
                System.out.println("Artifact: " + artifact.getFile().getAbsolutePath());
                var newArtifact = new File(versionFolder, artifact.getFile().toPath().getFileName().toString());
                Files.copy(artifact.getFile().toPath(), newArtifact.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException ex)
            {
                throw new GradleException("Unable to copy required file."+ex.getLocalizedMessage(), ex);   
            }
        });

        // get the pom file.
        this.getTaskDependencies().getDependenciesForInternalUse(this).forEach(t -> {
            if (t instanceof GenerateMavenPom pomTask)
            {
                var pomFile = pomTask.getDestination();
                var pomFileCopy = new File(versionFolder, pomFile.getName());
                try
                {
                    Files.copy(pomFile.toPath(), pomFileCopy.toPath());
                }
                catch (IOException ex)
                {
                    throw new GradleException("Unable to copy required file."+ex.getLocalizedMessage(), ex);   
                }
            }
        });
    }


    public DirectoryProperty getOutputDirectory()
    {
        return this.outputDirectory;
    }
}
