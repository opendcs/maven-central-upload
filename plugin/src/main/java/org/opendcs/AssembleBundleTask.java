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
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class AssembleBundleTask extends DefaultTask
{

    @OutputDirectory
    final DirectoryProperty outputDirectory = getProject().getObjects()
                                                          .directoryProperty()
                                                          .value(getProject().getLayout().getBuildDirectory().dir("bundle"));

    @TaskAction
    public void assemble()
    {
        for ( var dep : this.getTaskDependencies().getDependencies(this))
        {
            System.out.println("Dep Task: " + dep.getName());
        }
        var publications = getPublications();
        var outputDir = outputDirectory.get().getAsFile();
        outputDir.mkdirs();
     
        for (MavenPublication pub: publications)
        {
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
        }
    }

    @Internal
    ArrayList<MavenPublication> getPublications()
    {
        var publications = new ArrayList<MavenPublication>();
        for (Project p : getProject().getAllprojects())
        {
            var pubExtension = p.getExtensions().findByType(PublishingExtension.class);
   
            if (pubExtension != null)
            {
                var pubs = pubExtension.getPublications();
                pubs.all(pub ->
                {
                    if (pub instanceof MavenPublication mavenPub)
                    {
                        publications.add(mavenPub);
                    }
                });
            }   
        }
        return publications;
    }


    public DirectoryProperty getOutputDirectory()
    {
        return this.outputDirectory;
    }
}
