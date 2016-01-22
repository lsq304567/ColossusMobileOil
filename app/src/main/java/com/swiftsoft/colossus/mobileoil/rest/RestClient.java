package com.swiftsoft.colossus.mobileoil.rest;

import com.swiftsoft.colossus.mobileoil.CrashReporter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;

public class RestClient implements IRestClient
{
    private final ArrayList <NameValuePair> params;
    private final ArrayList <NameValuePair> headers;
    private String body;

    private final String url;

    private int responseCode;
    private String message;
    private String response;

//    public enum RequestMethod
//    {
//     GET,
//     POST
//    }
    
    public String getResponse()
    {
        return response;
    }

    public String getErrorMessage()
    {
        return message;
    }

    public int getResponseCode()
    {
        return responseCode;
    }

    public RestClient(String url)
    {
        this.url = url;
        params = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();
    }

    public void addParameter(String name, String value)
    {
        params.add(new BasicNameValuePair(name, value));
    }

    public void addHeader(String name, String value)
    {
        headers.add(new BasicNameValuePair(name, value));
    }
    
    public void addBody(String value)
    {
    	body = value;
    }

    public void execute(RequestMethod method) throws Exception
    {
        CrashReporter.leaveBreadcrumb("RestClient: execute");
        
        switch(method)
        {
            case GET:
            {
                //add parameters
                String combinedParams = "";

                if(!params.isEmpty())
                {
                    combinedParams += "?";

                    for(NameValuePair p : params)
                    {
                        String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");

                        combinedParams += combinedParams.length() > 1 ? "&" + paramString : paramString;
                    }
                }

                HttpGet request = new HttpGet(url + combinedParams);

                //add headers
                for(NameValuePair header : headers)
                {
                    request.addHeader(header.getName(), header.getValue());
                }

                executeRequest(request);

                break;
            }

            case POST:
            {
                HttpPost request = new HttpPost(url);

                //add headers
                for(NameValuePair header : headers)
                {
                    request.addHeader(header.getName(), header.getValue());
                }

                if (!params.isEmpty())
                {
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                }
                
                if (body != null && body.length() > 0)
                {
                    request.setEntity(new StringEntity(body, HTTP.UTF_8));
                }

                executeRequest(request);

                break;
            }
        }
    }

    private void executeRequest(HttpUriRequest request)
    {
        CrashReporter.leaveBreadcrumb("RestClient: executeRequest");

        HttpClient client = new DefaultHttpClient();

        HttpResponse httpResponse;

        try
        {
            httpResponse = client.execute(request);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();

            HttpEntity entity = httpResponse.getEntity();

            if (entity != null)
            {
                InputStream inputStream = entity.getContent();

                response = convertStreamToString(inputStream);

                // Closing the input stream will trigger connection release
                inputStream.close();
            }

        }
        catch (ClientProtocolException e)
        {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        }
        catch (IOException e)
        {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        }
    }

    private static String convertStreamToString(InputStream is)
    {
        CrashReporter.leaveBreadcrumb("RestClient: convertStreamToString");

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;

        try
        {
            while ((line = reader.readLine()) != null)
            {
                sb.append(line).append("\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
}