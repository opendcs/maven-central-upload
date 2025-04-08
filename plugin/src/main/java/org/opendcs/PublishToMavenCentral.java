package org.opendcs;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.tasks.TaskAction;


public class PublishToMavenCentral extends DefaultTask
{

    MavenArtifactRepository local;
    MavenArtifactRepository remote;

    @TaskAction
    public void publish()
    {
        final var logger = getLogger();

        if (logger.isInfoEnabled())
        {
            logger.info("Using local repository {} with files at {}", local.getName(), local.getUrl().toString());
            logger.info("Using remote repository {} with url {}", remote.getName(), remote.getUrl().toString());
        }

        var localUrl = local.getUrl();
        local.getArtifactUrls().forEach(url -> logger.info("URL {}",url));
        final var group = (String)this.getProject().getGroup();
        final var bundleBase = String.format("%s%s",localUrl.toString(), group.replaceAll("\\.","/"));
        final var version = (String)this.getProject().getVersion();
        logger.info("Creating bundle from {} with version {}", bundleBase, version);
    }    
}
