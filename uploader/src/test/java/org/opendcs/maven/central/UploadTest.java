package org.opendcs.maven.central;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.File;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.MediaType;
import org.opendcs.maven.central.uploader.DeploymentState;
import org.opendcs.maven.central.uploader.Upload;
import org.opendcs.maven.central.uploader.Uploader;

@ExtendWith(MockServerExtension.class)
class UploadTest
{
    private static final String TEST_USER = "test-user";
    private static final String TEST_PASSWORD = "test-password";
    private static final String TEST_CREDENTIALS = "Bearer " +
        Base64.getEncoder().encodeToString((TEST_USER+":"+TEST_PASSWORD).getBytes());

    @Test
    void test_successful_update(MockServerClient client) throws Exception
    {
        client.reset();
        
        client.when(
            request().withMethod("POST")
                     .withPath("/api/v1/publisher/upload")
                     .withHeader("Authorization", TEST_CREDENTIALS)
             ) 
             .respond(response().withBody("test").withContentType(MediaType.TEXT_PLAIN))
             ;
        client.when(
            request().withMethod("POST")
                     .withHeader("Authorization", TEST_CREDENTIALS)
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
        Uploader uploader = new Uploader(url, TEST_USER, TEST_PASSWORD);
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
                     .withHeader("Authorization", TEST_CREDENTIALS)
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
                     .withHeader("Authorization", TEST_CREDENTIALS)
                     .withPath("/api/v1/publisher/status")
                     .withQueryStringParameter("id", "test-user-managed")
        )
            .respond(response().withBody(String.format(statusTemplate, "test-user-managed", DeploymentState.PENDING), MediaType.APPLICATION_JSON))
            ;
        final String url = "http://localhost:" + client.getPort();
        Uploader uploader = new Uploader(url, TEST_USER, TEST_PASSWORD);
        File tmp = new File("test-file.txt"); // for testing we just need a file to verify API response behavior.
        final var result = uploader.publish(tmp, false);
        assertFalse(result.isFailure(), () -> "Initial upload request reported failure." + result.getFailure());
        var status = result.getSuccess();
        assertEquals(DeploymentState.PENDING, status.state(), "Deployment not in expected state.");
    }
}
