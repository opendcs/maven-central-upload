package org.opendcs.maven.central.uploader;

import java.io.File;

import org.opendcs.maven.central.api.PublishingApi;
import org.opendcs.maven.central.invoker.ApiClient;
import org.opendcs.maven.central.invoker.ApiException;

public class Uploader {

    private final ApiClient client;


    public Uploader(String url)
    {
        client = new ApiClient();
        client.updateBaseUri(url);
    }

    public static void main(String[] argv)
    {
        Uploader uploader = new Uploader(argv[0]);
        uploader.publish();
        System.out.println(uploader.toString());
    }

    private void publish() 
    {
        PublishingApi publishing = new PublishingApi(client);
        String id;
        try 
        {
            id = publishing.apiV1PublisherUploadPost("test", "AUTOMATIC", new File("test.zip"));
            System.out.println(id);
        }
        catch (ApiException ex) 
        {
            ex.printStackTrace();
        }
        
    }    
}
