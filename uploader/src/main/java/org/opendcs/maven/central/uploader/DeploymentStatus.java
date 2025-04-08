package org.opendcs.maven.central.uploader;

import java.util.ArrayList;
import java.util.Map;

/**
 * Current Status of a deployment.
 */
public record DeploymentStatus(String deploymentId, String deploymentName, DeploymentState deploymentState,
                               ArrayList<String> packageUrls, Map<String,String> errors)
{
    /**
     * Given a HashMap covert to this record
     * @param status map of data from the api
     * @return new DeploymentStatus record.
     */
    @SuppressWarnings("unchecked")
    public static DeploymentStatus fromApi(Map<String,Object> status)
    {
        return new DeploymentStatus(
             (String)status.get("deploymenId"), 
             (String)status.get("deploymentName"),
             DeploymentState.valueOf((String)status.get("deploymentState")),
             (ArrayList<String>)status.get("purls"),
             (Map<String, String>)status.get("errors")
            );
    }
}
