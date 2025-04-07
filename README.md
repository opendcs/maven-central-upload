# Maven Central Upload plugin

This project is yet-another-library for using the new maven central publishing api.
The goal of the project is to be a generic library for others to use and provide a gradle plugin
that wraps the existing maven-publish plugin to handle everything requirement up until building the bundle zip
and pushing that bundle to maven central.

The Central API OpenAPI Specification was full from below and converted to yaml, along with a few validation corrections
and the final result can be found within the central-api sub project.

https://central.sonatype.com/swagger.json


## Organization (Sub-projects)

### Central API

This contains the code generation portion. The built-in java http client is used to reduce additional dependencies.

### Uploader

This library builds on the Central API generated code to provide a friendly interface to interact with the API.


### Plugin

This is the gradle plugin. The primary focus of the plugin will be for use in OpenDCS projects. However, we try to do
things in standards ways so it should be useful to others and we will gladly except issues and direct code contributions.
