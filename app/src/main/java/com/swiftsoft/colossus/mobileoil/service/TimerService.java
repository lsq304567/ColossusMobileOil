package com.swiftsoft.colossus.mobileoil.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.swiftsoft.colossus.mobileoil.CrashReporter;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service
{
	private Timer timer = null;

	@Override
	public IBinder onBind(Intent paramIntent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		CrashReporter.leaveBreadcrumb("TimerService: onCreate");

		super.onCreate();

		try
		{
			// Create timer.
			timer = new Timer();

			int timerInterval = 1;

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
        CrashReporter.leaveBreadcrumb("TimerService.onDestroy");

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
	
	private class timerTask extends TimerTask
	{
		@Override
		public void run()
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("timerTask: run");

				// Call ColossusIntentService.
				startService(new Intent(getApplicationContext(), ColossusIntentService.class));
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	}
}
