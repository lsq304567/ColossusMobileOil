package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.swiftsoft.colossus.mobileoil.utilities.ISecureSettings;
import com.swiftsoft.colossus.mobileoil.utilities.SecureSettings;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class Utils
{
    public static ISecureSettings SecureSettings = new SecureSettings();

	// Convert a string to an integer, without throw an exception.
	public static int convert2Int(String text)
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
	public static double roundNearest(double value, int n)
	{
		double power = Math.pow(10, n);
		double roundedValue = Math.round(value * power);
		return roundedValue / power;
	}

	public static BigDecimal roundNearest(BigDecimal value, int n)
	{
        CrashReporter.leaveBreadcrumb("Utils: RoundNearest");

        CrashReporter.leaveBreadcrumb(String.format("Utils: Roundnearest - Rounding %f to %d decimal places", value, n));

		return value.setScale(n, BigDecimal.ROUND_HALF_UP);
	}
	
    /**
     * Truncates double value to n decimal places
     *
     * 1.234 -> 1.23, if n=2
     * 1.235 -> 1.23, if n=2
     * @param value double to be truncated
     * @param n number of decimals in the result
     * @return double truncated to n decimal places.
     */
    public static double truncate(double value, int n)
	{
		double power = Math.pow(10, n);
		double truncatedValue = Math.floor(value * power);
		return truncatedValue / power;
	}

	public static BigDecimal truncate(BigDecimal value, int n)
	{
		return value.setScale(n, RoundingMode.DOWN);
	}

    /**
     * If value is null return nullValue,
     * otherwise return value.
     * @param value Value to be tested.
     * @param nullValue This is returned if value is null.
     * @return value or nullValue
     */
	public static String toStringNoNull(String value, String nullValue)
	{
		return value == null ? nullValue : value;
	}
	
	public static void showKeyboard(EditText et)
	{
		// Open soft keyboard.
		InputMethodManager imm = (InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

		if (imm != null) 
		{
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY); 
			imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
		}
	}
	
    // Returns: Serial no of device i.e. IMEI no.
    public static String getSerialNo(Context context)
    {
    	String serialNumber = "";

    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Utils: getSerialNo");
			
			// Get IMEI
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			serialNumber = tm.getDeviceId();

            // If no serial number was retrieved then attempt
            // to get the Android ID
			if (serialNumber == null)
			{
                serialNumber = SecureSettings.getSerialNumber(context);
			}
    	}
		catch (Exception e) 
		{
			CrashReporter.logHandledException(e);
		}			
    	
		return serialNumber;
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