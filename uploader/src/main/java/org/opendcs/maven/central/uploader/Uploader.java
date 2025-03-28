/*
* Copyright 2025 The OpenDCS Consortium or its contributors.
* 
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy
* of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations 
* under the License.
*/
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
