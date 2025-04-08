package org.opendcs.maven.central.uploader;

@SuppressWarnings("javadoc")
public enum DeploymentState
{
    PENDING,
    VALIDATING,
    VALIDATED,
    PUBLISHING,
    PUBLISHED,
    FAILED
}
