package org.opendcs.maven.central.uploader;

import org.opendcs.maven.central.api.PublishingApi;
import org.opendcs.maven.central.invoker.ApiClient;
import org.opendcs.maven.central.invoker.ApiException;

/**
 * Wrap a deployment with helper methods to monitor status.
 */
public class Upload
{
    private final String deploymentId;
    private final ApiClient client;
    private DeploymentStatus currentStatus = null;

    /**
     * 
     * @param deploymentId The id of this deployment
     * @param client ApiClient to retrieve additional information.
     */
    public Upload(String deploymentId, ApiClient client)
    {
        this.deploymentId = deploymentId;
        this.client = client;
    }

    /**
     * Update the current status object.
     * @throws ApiException any issues with the Http Connection to the API.
     */
    private void updateStatus() throws ApiException
    {
        var api = new PublishingApi(client);
        var response = api.apiV1PublisherStatusPostWithHttpInfo(deploymentId);
        if (response.getStatusCode() == 200)
        {
            var deploymentStatus = response.getData();
            this.currentStatus = DeploymentStatus.fromApi(deploymentStatus);
        }
        else
        {
            throw new ApiException("Unable to get deployment status. Error code: " + response.getStatusCode());
        }
        
    }

    /**
     * 
     * @return The current state of this deployment.
     * @throws ApiException any issues with the Http Connection to the API.
     */
    public DeploymentState state() throws ApiException
    {
        updateStatus();
        return currentStatus.deploymentState();
    }

    /**
     * Publish the deployment.
     * @throws ApiException any issues with the Http Connection to the API.
     */
    public void publish() throws ApiException
    {
       updateStatus();

        if (currentStatus.deploymentState() == DeploymentState.PUBLISHED ||
           currentStatus.deploymentState() == DeploymentState.PUBLISHING)
        {
            /* Do nothing already happened or happening. */
        }
        else if (currentStatus.deploymentState() == DeploymentState.VALIDATED)
        {
            var api = new PublishingApi(client);
            var response = api.apiV1PublisherDeploymentDeploymentIdPostWithHttpInfo(deploymentId);
            if (response.getStatusCode() != 204)
            {
                throw new ApiException("Publish request failed. Error Code: " + response.getStatusCode());
            }
        }
        else
        {
            throw new IllegalStateException("Deployment not in valid state to publish. Current state is: "+ currentStatus.deploymentState());
        }
    }
}
