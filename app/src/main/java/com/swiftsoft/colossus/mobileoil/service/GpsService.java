package com.swiftsoft.colossus.mobileoil.service;

import org.json.JSONObject;

import com.swiftsoft.colossus.mobileoil.Active;
import com.swiftsoft.colossus.mobileoil.CrashReporter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

//
// Track the location of the device
// and send to Colossus WebService.
//
public class GpsService extends Service
{
	long lastGPS;
	Location lastLocation;
	LocationManager locationManager;
	
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
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			long time = location.getTime();
			int speed = (int) (location.getSpeed() * 2.2369362920544);	// Convert to MPH.
			float accuracy = location.getAccuracy();

			// Create new location object.
			Location newLocation = new Location("");
			newLocation.setLatitude(latitude);
			newLocation.setLongitude(longitude);

			// Report location at least every 5 minutes, 
			// and at most every 1 minute, if travelled more than 20 metres.
			if ((time - lastGPS >= 300000) || (time - lastGPS >= 60000 &&  newLocation.distanceTo(lastLocation) >= 20))
			{
				// Store new location.
				lastGPS = location.getTime();
				lastLocation.setLatitude(latitude);
				lastLocation.setLongitude(longitude);
				
				if (Active.vehicle != null)
				{
					// Create content.
					JSONObject json = new JSONObject();
					json.put("VehicleID", Active.vehicle.ColossusID);
					json.put("Latitude", latitude);
					json.put("Longitude", longitude);
					json.put("Speed", speed);
					json.put("Accuracy", accuracy);
					json.put("DateTime", "/Date(" + time + ")/");
					
					// Call ColossusIntentService.
					addIntent("GPS", json.toString());
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private void addIntent(String type, String content)
	{
		Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);

		i.putExtra("Type", type);
		i.putExtra("Content", content);

		startService(i);
	}

	LocationListener locationListener = new LocationListener()
	{
		@Override
		public void onLocationChanged(Location location)
		{
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
				// Call ColossusIntentService.
				addIntent("GPS_Off", "");
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
				// Call ColossusIntentService.
				addIntent("GPS_On", "");
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
//			Log.w("GPS Status is now " + status);
		}
	};
}