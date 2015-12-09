package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class Utils {

	// Convert a string to an integer, without throw an exception.
	public static int Convert2Int(String text)
	{
		try
		{
			return Integer.parseInt(text);
		}
		catch (NumberFormatException nfe)
		{
			return 0;
		}
	}
	
	// Round value to n decimal places.
	// 1.234 = 1.23, if n=2
	// 1.235 = 1.24, if n=2
	public static double RoundNearest(double value, int n)
	{
		double power = Math.pow(10, n);
		double roundedValue = Math.round(value * power);
		return roundedValue / power;
	}

	public static BigDecimal RoundNearest(BigDecimal value, int n)
	{
		return value.setScale(n, BigDecimal.ROUND_HALF_UP);
	}
	
	// Truncate value to n decimal places.
	// 1.234 = 1.23, if n=2
	// 1.235 = 1.23, if n=2
	public static double Truncate(double value, int n)
	{
		double power = Math.pow(10, n);
		double truncatedValue = Math.floor(value * power);
		return truncatedValue / power;
	}

	public static BigDecimal Truncate(BigDecimal value, int n)
	{
		return value.setScale(n, RoundingMode.DOWN);
	}

	// If value is null returns nullValue string, otherwise returns value.
	public static String ToStringNoNull(String value, String nullValue)
	{
		return value == null ? nullValue : value;
	}
	
	public static void ShowKeyboard(EditText et)
	{
		// Open soft keyboard.
		InputMethodManager imm = (InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

		if (imm != null) 
		{
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY); 
			imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
		}
	}
	
	public static void HideKeyboard(View v)
	{
		InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
    // Returns: Serial no of device i.e. IMEI no.
    public static String getSerialNo(Context context)
    {
    	String serialNo = "";

    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Utils: getSerialNo");
			
			// Get IMEI
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			serialNo = tm.getDeviceId();

			if (serialNo == null)
			{
				// Use serial number of device, which may not be unique!
				serialNo = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			}
    	}
		catch (Exception e) 
		{
			CrashReporter.logHandledException(e);
		}			
    	
		return serialNo;
    }

    /**
     * Get the currnt time in milliseconds as a long
     * @return The time in millseconds
     */
	public static long getCurrentTime()
	{
		return new Date().getTime();
	}
}