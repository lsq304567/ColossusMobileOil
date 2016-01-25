package com.swiftsoft.colossus.mobileoil;

import com.swiftsoft.colossus.mobileoil.rest.IRestClient;
import com.swiftsoft.colossus.mobileoil.rest.RestClient;

import org.apache.http.NameValuePair;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

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
    public void addition_of_parameters()
    {
        NameValuePair nvp;

        IRestClient restClient = new RestClient("dummy_url");

        assertEquals("No parameters expected", 0, restClient.getParameters().size());

        nvp = Mockito.mock(NameValuePair.class);

        when(nvp.getName()).thenReturn("Parameter1");
        when(nvp.getValue()).thenReturn("Value1");

        restClient.getParameters().add(nvp);

        assertEquals("Only one parameter expected", 1, restClient.getParameters().size());
        assertEquals("Parameter 1 incorrect", "Value1", restClient.getParameters().get(0).getValue());

        nvp = Mockito.mock(NameValuePair.class);

        when(nvp.getName()).thenReturn("Parameter2");
        when(nvp.getValue()).thenReturn("Value2");

        restClient.getParameters().add(nvp);

        assertEquals("Two parameters expected", 2, restClient.getParameters().size());
        assertEquals("Parameter 1 incorrect", "Value1", restClient.getParameters().get(0).getValue());
        assertEquals("Parameter 2 incorrect", "Value2", restClient.getParameters().get(1).getValue());
    }
}
