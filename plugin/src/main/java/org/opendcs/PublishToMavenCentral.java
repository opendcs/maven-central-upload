package org.opendcs;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;


public class PublishToMavenCentral extends DefaultTask
{
    @TaskAction
    public void publish()
    {
        getLogger().debug("Would publish");
    }    
}
