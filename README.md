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


## Testing

As many tests as possible will be included; however, with actually going to maven central mocks must be used, the final
test will of course be can the project publish itself.

Since this project is a gradle publishing plugin and library it will use itself to publish release to maven central.


WARNING: if you start playing around to help, use `./gradlew build -x jar` to start with. To bootstrap the use of the plugin
the project is set to apply the plugin itself if the jar exists. Depending on any issues one is having this may prevent
even a `./gradlew clean` if it's not evaluating correctly. Manually delete `plugin/build/libs/plugin.jar` if that happens.


# Usage

A publishing repository with the name "mavenCentralApi" is required.

```groovy

publishing {
    repositories {
        maven {
            name = "mavenCentralApi"
            url = "https://central.sonatype.com"
            def user = project.findProperty("centralApiUsername")
            def passwd = project.findProperty("centralApiPassword")
            credentials {
                username = user
                password = passwd
            }
        }
    }
}
```

use of the signing extension is also required as that is a Maven Central requirement.

There are two properties to control operations

* -PwaitForPublish=true will cause the plugin to wait for the publication to be in the `PUBLISHED` state before moving on.
* -PwaitForPublish=false (default) will wait until the publication is in the `PUBLISHING` state adn then move on.

* -PautomaticPublish=true will cause the plugin to tell the api to automatically publish if all validation checks pass.
* -PautomaticPublish=false (default) will cause the plugin to tell the api *not* to automatically publish. You must go into
  the Maven Central interface and manually publish.
