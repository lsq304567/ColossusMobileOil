package com.swiftsoft.colossus.mobileoil;

import com.swiftsoft.colossus.mobileoil.rest.IRestClient;
import com.swiftsoft.colossus.mobileoil.rest.RestClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestClient.class)
public class RestClientTests
{
    @Test
    public void constructor_only_headers_empty()
    {
        IRestClient restClient = new RestClient("dummy_url");

        assertNotNull("There is no header object", restClient.getHeaders());
        assertTrue("Headers not ArrayList", restClient.getHeaders() instanceof ArrayList);
        assertEquals("Headers not empty", 0, restClient.getHeaders().size());
    }

    @Test
    public void constructor_only_parameters_empty()
    {
        IRestClient restClient = new RestClient("dummy_url");

        assertNotNull("There is no parameters object", restClient.getParameters());
        assertTrue("Parameters not ArrayList", restClient.getParameters() instanceof ArrayList);
        assertEquals("Parameters not empty", 0, restClient.getParameters().size());
    }

    @Test
    public void addition_of_parameters() throws Exception
    {
        // Create IRestClient for testing ...
        IRestClient restClient = new RestClient("dummy_url");

        // No parameters expected
        assertEquals("No parameters expected", 0, restClient.getParameters().size());

        BasicNameValuePair pair;

        pair = Mockito.mock(BasicNameValuePair.class);

        // Add a single parameter
        PowerMockito.whenNew(BasicNameValuePair.class).withArguments("P1", "V1").thenReturn(pair);

        Mockito.when(pair.getName()).thenReturn("P1");
        Mockito.when(pair.getValue()).thenReturn("V1");

        restClient.addParameter("P1", "V1");

        assertEquals("One parameter expected", 1, restClient.getParameters().size());
        assertEquals("Incorrect parameter name", "V1", restClient.getParameters().get(0).getValue());
        assertEquals("Incorrect parameter value", "P1", restClient.getParameters().get(0).getName());

        // Add another parameter
        pair = Mockito.mock(BasicNameValuePair.class);

        PowerMockito.whenNew(BasicNameValuePair.class).withArguments("P2", "V2").thenReturn(pair);

        Mockito.when(pair.getName()).thenReturn("P2");
        Mockito.when(pair.getValue()).thenReturn("V2");

        restClient.addParameter("P2", "V2");

        assertEquals("Two parameters expected", 2, restClient.getParameters().size());
        assertEquals("Incorrect parameter name", "P1", restClient.getParameters().get(0).getName());
        assertEquals("Incorrect parameter value", "V1", restClient.getParameters().get(0).getValue());
        assertEquals("Incorrect parameter name", "P2", restClient.getParameters().get(1).getName());
        assertEquals("Incorrect parameter value", "V2", restClient.getParameters().get(1).getValue());
    }


    @Test
    public void addition_of_headers() throws Exception
    {
        // Create IRestClient for testing ...
        IRestClient restClient = new RestClient("dummy_url");

        // No headers expected
        assertEquals("No headers expected", 0, restClient.getHeaders().size());

        BasicNameValuePair pair;

        pair = Mockito.mock(BasicNameValuePair.class);

        // Add a single header
        PowerMockito.whenNew(BasicNameValuePair.class).withArguments("P1", "V1").thenReturn(pair);

        Mockito.when(pair.getName()).thenReturn("P1");
        Mockito.when(pair.getValue()).thenReturn("V1");

        restClient.addHeader("P1", "V1");

        assertEquals("One header expected", 1, restClient.getHeaders().size());
        assertEquals("Incorrect header name", "V1", restClient.getHeaders().get(0).getValue());
        assertEquals("Incorrect header value", "P1", restClient.getHeaders().get(0).getName());

        // Add another header
        pair = Mockito.mock(BasicNameValuePair.class);

        PowerMockito.whenNew(BasicNameValuePair.class).withArguments("P2", "V2").thenReturn(pair);

        Mockito.when(pair.getName()).thenReturn("P2");
        Mockito.when(pair.getValue()).thenReturn("V2");

        restClient.addHeader("P2", "V2");

        assertEquals("Two headers expected", 2, restClient.getHeaders().size());
        assertEquals("Incorrect header name", "P1", restClient.getHeaders().get(0).getName());
        assertEquals("Incorrect header value", "V1", restClient.getHeaders().get(0).getValue());
        assertEquals("Incorrect header name", "P2", restClient.getHeaders().get(1).getName());
        assertEquals("Incorrect header value", "V2", restClient.getHeaders().get(1).getValue());
    }

    @Test
    public void addition_of_body()
    {
        IRestClient restClient = new RestClient("dummy_url");

        // Add body string
        restClient.addBody("test_body");

        assertEquals("Unexpected body value", "test_body", restClient.getBody());
    }

    @Test
    public void execute_get_success_null() throws Exception
    {
        IRestClient restClient = new RestClient("dummy_url");

        HttpResponse response = PowerMockito.mock(HttpResponse.class);

        StatusLine statusLine = PowerMockito.mock(StatusLine.class);

        PowerMockito.when(statusLine.getStatusCode()).thenReturn(200);
        PowerMockito.when(statusLine.getReasonPhrase()).thenReturn("");

        PowerMockito.when(response.getStatusLine()).thenReturn(statusLine);
        PowerMockito.when(response.getEntity()).thenReturn(null);

        DefaultHttpClient client = PowerMockito.mock(DefaultHttpClient.class);

        PowerMockito.whenNew(DefaultHttpClient.class).withNoArguments().thenReturn(client);

        PowerMockito.when(client.execute(Mockito.any(HttpUriRequest.class))).thenReturn(response);

        restClient.execute(IRestClient.RequestMethod.GET);

        assertEquals("Unexpected response code", 200, restClient.getResponseCode());
    }

    @Test
    public void execute_get_success() throws Exception
    {
        IRestClient restClient = new RestClient("dummy_url");

        HttpResponse response = PowerMockito.mock(HttpResponse.class);

        StatusLine statusLine = PowerMockito.mock(StatusLine.class);

        PowerMockito.when(statusLine.getStatusCode()).thenReturn(200);
        PowerMockito.when(statusLine.getReasonPhrase()).thenReturn("");

        PowerMockito.when(response.getStatusLine()).thenReturn(statusLine);

        HttpEntity entity = PowerMockito.mock(HttpEntity.class);

        InputStream inputStream = new ByteArrayInputStream("Mary had a little lamb".getBytes());

        PowerMockito.when(entity.getContent()).thenReturn(inputStream);

        PowerMockito.when(response.getEntity()).thenReturn(entity);

        DefaultHttpClient client = PowerMockito.mock(DefaultHttpClient.class);

        PowerMockito.whenNew(DefaultHttpClient.class).withNoArguments().thenReturn(client);

        PowerMockito.when(client.execute(Mockito.any(HttpUriRequest.class))).thenReturn(response);

        restClient.execute(IRestClient.RequestMethod.GET);

        assertEquals("Unexpected response code", 200, restClient.getResponseCode());
        assertEquals("Unexpected error message", "", restClient.getErrorMessage());
        assertEquals("Unexpected response", "Mary had a little lamb\n", restClient.getResponse());
    }
}
