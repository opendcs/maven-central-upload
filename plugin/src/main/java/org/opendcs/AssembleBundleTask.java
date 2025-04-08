package org.opendcs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Zip;

public class AssembleBundleTask extends DefaultTask
{


    @TaskAction
    public void assemble()
    {
        
    }


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
