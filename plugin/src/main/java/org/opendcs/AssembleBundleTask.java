package org.opendcs;

import java.util.ArrayList;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

public class AssembleBundleTask extends DefaultTask
{


    @TaskAction
    public void assemble()
    {
        var publications = getPublications();
        var outputDir = getProject().getLayout().getBuildDirectory().dir("bundle").get().getAsFile();
        outputDir.mkdirs();
        for (MavenPublication pub: publications)
        {
            pub.getArtifacts().all(artifact ->
            {
                System.out.println("Artifact: " + artifact.getFile().getAbsolutePath());
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
}
