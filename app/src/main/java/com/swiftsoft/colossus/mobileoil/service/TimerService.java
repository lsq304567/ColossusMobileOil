package com.swiftsoft.colossus.mobileoil.service;

import java.util.Timer;
import java.util.TimerTask;

import com.swiftsoft.colossus.mobileoil.CrashReporter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TimerService extends Service
{
	Timer timer = null;
	int timerInterval = 1;
	
	@Override
	public IBinder onBind(Intent paramIntent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		try
		{
			// Create timer.
			timer = new Timer();
			timer.scheduleAtFixedRate(new timerTask(), 0, timerInterval * 60 * 1000);
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
			// Stop timer.
			timer.cancel();
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
	
	class timerTask extends TimerTask
	{
		@Override
		public void run()
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("timerTask: run");

				// Call ColossusIntentService.
				Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);
				startService(i);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	}
}
