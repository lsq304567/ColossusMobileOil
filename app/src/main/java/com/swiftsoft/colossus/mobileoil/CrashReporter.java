package com.swiftsoft.colossus.mobileoil;

import android.content.Context;

import com.crashlytics.android.Crashlytics;

public class CrashReporter
{
	// Initialise the CrashReporter.
	public static void onStart(Context context)
	{
	}
	
	// Leave a breadcrumb.
	public static void leaveBreadcrumb(String breadCrumb)
	{
		try
		{
			Crashlytics.log(breadCrumb);
		}
		catch (Exception e) 
		{
		}
	}
	
	// Log an exception.
	public static void logHandledException(Exception exception)
	{
		try
		{
			Crashlytics.logException(exception);
		}
		catch (Exception e)
		{
		}
	}
}
