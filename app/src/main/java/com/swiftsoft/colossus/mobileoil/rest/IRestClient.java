package com.swiftsoft.colossus.mobileoil.rest;

import org.apache.http.NameValuePair;

import java.util.ArrayList;

/**
 * Created by Alan on 25/09/2015.
 */
public interface IRestClient
{
    enum RequestMethod
    {
        GET,
        POST
    }

    String getResponse();

    String getErrorMessage();

    int getResponseCode();

    void addParameter(String name, String value);

    void addHeader(String name, String value);

    void addBody(String value);

    void execute(RequestMethod method) throws Exception;

    ArrayList<NameValuePair> getHeaders();

    ArrayList<NameValuePair> getParameters();

    String getBody();
}
