package org.opendcs.maven.central;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpTemplate.template;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.MediaType;
import org.mockserver.model.HttpTemplate.TemplateType;
import org.opendcs.maven.central.uploader.DeploymentState;
import org.opendcs.maven.central.uploader.DeploymentStatus;
import org.opendcs.maven.central.uploader.Upload;
import org.opendcs.maven.central.uploader.Uploader;

@ExtendWith(MockServerExtension.class)
public class UploadTest
{
    
    @Test
    void test_successful_update(MockServerClient client) throws Exception
    {
        client.reset();
        
        client.when(
            request().withMethod("POST")
                     .withPath("/api/v1/publisher/upload")
             ) 
             .respond(response().withBody("test").withContentType(MediaType.TEXT_PLAIN))
             ;
        client.when(
            request().withMethod("POST")
                     .withPath("/api/v1/publisher/status")
                     .withQueryStringParameter("id", "test")
            )   
            .respond(response().withBody(
                """
                {
                    "deploymentId": "test", 
                    "deploymentName":"test",
                    "deploymentState":"PUBLISHED",
                    "purls": [],
                    "errors": {}
                }
                """, MediaType.APPLICATION_JSON
            ))
        ;
        final String url = "http://localhost:" + client.getPort();
        Uploader uploader = new Uploader(url);
        File tmp = new File("test-file.txt"); // for testing we just need a file to verify API response behavior.
        final var result = uploader.publish(tmp, true);
        assertFalse(result.isFailure(), () -> "Initial upload request reported failure." + result.getFailure());
        Upload status = result.getSuccess();
        assertEquals(DeploymentState.PUBLISHED, status.state(), "test file was not reported as uploaded.");
    }


    @Test
    void test_user_managed(MockServerClient client) throws Exception
    {
        client.when(
            request().withMethod("POST")
            .withPath("/api/v1/publisher/upload")
            ) 
            .respond(response().withBody("test-user-managed").withContentType(MediaType.TEXT_PLAIN))
        ;
        final var statusTemplate = """
                {
                    "deploymentId": "%1$s", 
                    "deploymentName":"%1$s",
                    "deploymentState": "%2$s",
                    "purls": [],
                    "errors": {}
                }
                """;
        client.when(
            request().withMethod("POST")
            .withPath("/api/v1/publisher/status")
            .withQueryStringParameter("id", "test-user-managed")
        )//.respond(template(TemplateType.MUSTACHE,))
        .respond(response().withBody(String.format(statusTemplate, "test-user-managed", DeploymentState.PENDING), MediaType.APPLICATION_JSON))
        ;
        final String url = "http://localhost:" + client.getPort();
        Uploader uploader = new Uploader(url);
        File tmp = new File("test-file.txt"); // for testing we just need a file to verify API response behavior.
        final var result = uploader.publish(tmp, false);
        assertFalse(result.isFailure(), () -> "Initial upload request reported failure." + result.getFailure());
        var status = result.getSuccess();
        assertEquals(DeploymentState.PENDING, status.state(), "Deployment not in expected state.");
    }
}
