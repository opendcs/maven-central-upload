package org.opendcs;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.api.tasks.options.Option;
import org.opendcs.maven.central.invoker.ApiException;
import org.opendcs.maven.central.uploader.DeploymentState;
import org.opendcs.maven.central.uploader.Uploader;


public abstract class PublishToMavenCentral extends DefaultTask
{
    @InputFile
    final RegularFileProperty bundle = getProject().getObjects().fileProperty();
    MavenArtifactRepository remote;

    private boolean dryRun = false;

    @Option(option="no-upload", description="Perform all steps up-to central upload. But don't actually upload.")
    public void setDryRun(boolean dryRun)
    {
        this.dryRun = dryRun;
    }

    @TaskAction
    public void publish()
    {
        final var logger = getLogger();

        if (logger.isInfoEnabled())
        {
            logger.info("Using remote repository {} with url {}", remote.getName(), remote.getUrl().toString());
            logger.info("From bundle {}", bundle.get().getAsFile().getAbsolutePath());
        }

        var credentials = remote.getCredentials();
        if (!dryRun)
        {
            Uploader uploader = new Uploader(remote.getUrl().toString(), credentials.getUsername(), credentials.getPassword());
            var fileToUpload = bundle.get().getAsFile();
            logger.info("Using {} with size {}",fileToUpload.getAbsolutePath(), fileToUpload.length()/1024.0/1024.0);
            var properties = getProject().getProperties();
            final String automaticPublish = (String)properties.get("automaticPublish");
-           final boolean automatic = Boolean.parseBoolean(automaticPublish);
            final String waitForPublished = (String)properties.get("waitForPublished");
            final var publishedState = Boolean.parseBoolean(waitForPublished)
                                     ? DeploymentState.PUBLISHED : DeploymentState.PUBLISHING;

            var result = uploader.publish(bundle.get().getAsFile(), automatic);

            result.handleError(ex ->
            {
                logger.error("Unable to upload bundle.", ex);
                throw new GradleException("Unable to upload bundle.", ex);
            });
            var upload = result.getSuccess();
            try
            {
                var state = upload.state();
                final var finishState = automatic ? publishedState : DeploymentState.VALIDATED;
                while (state != DeploymentState.FAILED && state != finishState)
                {
                    logger.info("DeploymentStatus... {}",  state);
                    try
                    {
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException ex)
                    {
                        /** no nopthing */
                    }
                    state = upload.state();
                }

                if (state == DeploymentState.FAILED)
                {
                    var status = upload.status();
                    logger.error("Deployment failed. {}", status.errors());
                    this.getState().addFailure(new TaskExecutionException(this, new GradleException("Maven Central Validation failed.")));
                }

                logger.info("Deployment Ready. {}", (automatic ? "Publish Successful" : "Validation Finished. Finish deployment at Maven Central"));

            }
            catch (ApiException ex)
            {
                throw new GradleException("Unable to check deployment state.", ex);
            }
        }
        else
        {
            logger.warn("DryRun. No Upload attempted.");
        }

    }

    Provider<RegularFile> getBundle()
    {
        return bundle;
    }
}
