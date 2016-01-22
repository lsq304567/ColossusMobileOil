package com.swiftsoft.colossus.mobileoil.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.swiftsoft.colossus.mobileoil.Active;
import com.swiftsoft.colossus.mobileoil.CrashReporter;

import org.json.JSONObject;

//
// Track the location of the device
// and send to Colossus WebService.
//
public class GpsService extends Service
{
	private long lastGPS;
	private Location lastLocation;
	private LocationManager locationManager;
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();

		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("GpsService: onCreate");

			// Initialise last location.
			lastLocation = new Location("");
			
			// Request GPS updates, every 60 seconds.
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListener);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("GpsService: onDestroy");
			
			// Unregister LocationListener.
			locationManager.removeUpdates(locationListener);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return START_STICKY;
	}
	
	private void updateDatabase(Location location)
	{
		try
		{
            CrashReporter.leaveBreadcrumb("GpsService: updateDatabase");

			// Get the latitude and longitude of the location
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();

			// Create new location object.
			Location newLocation = new Location("");
			newLocation.setLatitude(latitude);
			newLocation.setLongitude(longitude);

            // Get the current UTC time from the location
            long time = location.getTime();

            // Calculate the time elapsed since the last reported location
            long timeElapsed = time - lastGPS;

            // Report location at least every 5 minutes,
			// and at most every 1 minute,
			// if travelled more than 20 metres.
			if ((timeElapsed >= 300000) || (timeElapsed >= 60000 && newLocation.distanceTo(lastLocation) >= 20.0f))
			{
                CrashReporter.leaveBreadcrumb("GpsService: updateDatabase - Reporting position");

				// Store new location.
				lastGPS = time;
				lastLocation.setLatitude(latitude);
				lastLocation.setLongitude(longitude);

                // If there is an Active vehicle send the GPS message
				if (Active.vehicle != null)
				{
                    // Get the speed from the location in miles-per-hour
                    int speed = (int) (location.getSpeed() * 2.2369362920544);

                    // Create content.
					JSONObject json = new JSONObject();

					json.put("VehicleID", Active.vehicle.ColossusID);
					json.put("Latitude", latitude);
					json.put("Longitude", longitude);
					json.put("Speed", speed);
					json.put("Accuracy", location.getAccuracy());
					json.put("DateTime", "/Date(" + time + ")/");
					
					// Call ColossusIntentService.
					sendGpsMessage("GPS", json.toString());
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private void sendGpsMessage(String type, String content)
	{
        CrashReporter.leaveBreadcrumb("GpsService: sendGpsMessage");

		Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);

		i.putExtra("Type", type);
		i.putExtra("Content", content);

		startService(i);
	}

	private final LocationListener locationListener = new LocationListener()
	{
		@Override
		public void onLocationChanged(Location location)
		{
            CrashReporter.leaveBreadcrumb("GpsService: onLocatinChanged");

			if (location != null)
			{
				updateDatabase(location);
			}
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			try
			{
                CrashReporter.leaveBreadcrumb("GpsService: onProviderDisabled");

				// Call ColossusIntentService.
				sendGpsMessage("GPS_Off", "");
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}

		@Override
		public void onProviderEnabled(String provider)
		{
			try
			{
                CrashReporter.leaveBreadcrumb("GpsService: onProviderEnabled");

				// Call ColossusIntentService.
				sendGpsMessage("GPS_On", "");
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
            CrashReporter.leaveBreadcrumb(String.format("GpsService: onStatusChanged - Status -> %d", status));
		}
	};
}