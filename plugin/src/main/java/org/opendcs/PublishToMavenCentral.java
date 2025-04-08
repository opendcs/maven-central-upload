package org.opendcs;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.opendcs.maven.central.invoker.ApiException;
import org.opendcs.maven.central.uploader.DeploymentState;
import org.opendcs.maven.central.uploader.Uploader;


public class PublishToMavenCentral extends DefaultTask
{
    @InputFile
    Provider<RegularFile> bundle;
    MavenArtifactRepository remote;

    @TaskAction
    public void publish()
    {
        final var logger = getLogger();

        if (logger.isInfoEnabled())
        {
            logger.info("Using remote repository {} with url {}", remote.getName(), remote.getUrl().toString());
        }

        var credentials = remote.getCredentials();
        Uploader uploader = new Uploader(remote.getUrl().toString(), credentials.getUsername(), credentials.getPassword());
        var result = uploader.publish(bundle.get().getAsFile(), false);
        result.handleError((ex) -> ex.printStackTrace());
        var upload = result.getSuccess();
        try
        {
            if (upload.state() == DeploymentState.VALIDATED)
            {
                logger.info("Deployment ready.");
            }
        }
        catch (ApiException ex)
        {
            throw new GradleException("Unable to check deployment state.", ex);
        }
    }    

    Provider<RegularFile> getBundle()
    {
        return bundle;
    }
}
