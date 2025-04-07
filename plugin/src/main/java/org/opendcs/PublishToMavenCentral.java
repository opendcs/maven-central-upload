package org.opendcs;

import org.gradle.api.publish.internal.PublishOperation;
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven;
import org.gradle.api.tasks.TaskAction;


public class PublishToMavenCentral extends AbstractPublishToMaven
{
    @TaskAction
    public void publish()
    {
        new PublishOperation("test", "mavenCentralApi") {

            @Override
            protected void publish() throws Exception
            {
                System.out.println("Hello");
            }
            
        }.run();
    }    
}
