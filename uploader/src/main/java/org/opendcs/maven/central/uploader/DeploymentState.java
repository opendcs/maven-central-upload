package org.opendcs.maven.central.uploader;

public enum DeploymentState
{
    PENDING,    
    VALIDATING,
    VALIDATED,
    PUBLISHING,
    PUBLISHED,
    FAILED    
}
