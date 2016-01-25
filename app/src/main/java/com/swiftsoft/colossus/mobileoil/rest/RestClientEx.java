package com.swiftsoft.colossus.mobileoil.rest;

import android.content.ContentValues;

import org.apache.http.NameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

public class RestClientEx implements IRestClient
{
    private String url;
    private String body;

    private String response;
    private String errorMessage;
    private int responseCode;

    private ContentValues parameters;
    private ContentValues headers;

    public RestClientEx(String url)
    {
        // Store the URL to be used to call the web service
        this.url = url;

        // Instatiate the parameters & headers
        parameters = new ContentValues();
        headers = new ContentValues();
    }

    @Override
    public String getResponse()
    {
        // Return the response
        return this.response;
    }

    @Override
    public String getErrorMessage()
    {
        // Return the error message
        return this.errorMessage;
    }

    @Override
    public int getResponseCode()
    {
        // Return the response code
        return this.responseCode;
    }

    @Override
    public void addParameter(String name, String value)
    {
        // Add new name/value parameter pair
        parameters.put(name, value);
    }

    @Override
    public void addHeader(String name, String value)
    {
        // Add new name/value header pair
        headers.put(name, value);
    }

    @Override
    public void addBody(String value)
    {
        // Set the body of the request
        this.body = value;
    }

    @Override
    public void execute(RequestMethod method) throws Exception
    {
        switch (method)
        {
            case GET:
            {
                // Combine parameters for the query string
                String combinedParameters = combineParameters(parameters);

                URL u =  new URL(url + combinedParameters);

                executeGet(u);

                break;
            }

            case POST:
            {
                URL u = new URL(url);

                executePost(u);

                break;
            }
        }
    }

    @Override
    public ArrayList<NameValuePair> getHeaders()
    {
        return null;
    }

    @Override
    public ArrayList<NameValuePair> getParameters()
    {
        return null;
    }

    private void executeGet(URL url)
    {
        try
        {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            connection.setRequestMethod("GET");

            for (Map.Entry<String, Object> header : headers.valueSet())
            {
                connection.setRequestProperty(header.getKey(), (String)header.getValue());
            }

            this.responseCode = connection.getResponseCode();
            this.errorMessage = connection.getResponseMessage();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder builder = new StringBuilder();

            String output;

            while ((output = reader.readLine()) != null)
            {
                builder.append(output);
            }

            this.response = builder.toString();

            connection.disconnect();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void executePost(URL url)
    {
        try
        {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            for (Map.Entry<String, Object> header : headers.valueSet())
            {
                connection.setRequestProperty(header.getKey(), (String)header.getValue());
            }

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(this.body.getBytes());
            outputStream.flush();

            this.responseCode = connection.getResponseCode();
            this.errorMessage = connection.getResponseMessage();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder builder = new StringBuilder();

            String output;

            while ((output = reader.readLine()) != null)
            {
                builder.append(output);
            }

            this.response = builder.toString();

            connection.disconnect();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static String combineParameters(ContentValues parameters) throws UnsupportedEncodingException
    {
        // Concatenate all parameters
        StringBuilder combinedParameters = new StringBuilder("");

        // Can only do this if there are some parameters
        if (parameters.size() > 0)
        {
            // Add question mark to begin the query string
            combinedParameters.append("?");

            int parameterIndex = 0;

            // Loop through all the defined parameters
            for (Map.Entry<String, Object> parameter : parameters.valueSet())
            {
                parameterIndex++;

                if (parameterIndex > 1)
                {
                    combinedParameters.append("&");
                }

                combinedParameters.append(parameter.getKey());
                combinedParameters.append(("="));
                combinedParameters.append(URLEncoder.encode((String) parameter.getValue(), "UTF-8"));
            }
        }

        return combinedParameters.toString();
    }
}
