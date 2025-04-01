package org.opendcs.maven.central.uploader;

import java.util.ArrayList;
import java.util.Map;

public record DeploymentStatus(String deploymentId, String deploymentName, DeploymentState deploymentState,
                               ArrayList<String> packageUrls, Map<String,String> errors)
{
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
