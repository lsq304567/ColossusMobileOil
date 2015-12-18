package com.swiftsoft.colossus.mobileoil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.swiftsoft.colossus.mobileoil.database.adapter.TripAdapter;
import com.swiftsoft.colossus.mobileoil.database.model.dbTrip;
import com.swiftsoft.colossus.mobileoil.service.ColossusIntentService;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView3Line;

import org.json.JSONObject;

public class Trips extends Activity
{
	private myReceiver receiver;
	private TripAdapter adapter;

	private MyInfoView3Line infoview;
	private ListView lvTrips;
	private Button btnNext;
	
	class myReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trips: onReceive");
				
				if (intent.getAction().equals(ColossusIntentService.BroadcastTripsChanged))
				{
					Toast.makeText(getApplicationContext(), "Trip list updated", Toast.LENGTH_LONG).show();
					
					refreshData();
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trips: onCreate");

			// Setup view.
			setContentView(R.layout.trips);
		
			// Find UI controls.
			infoview = (MyInfoView3Line) findViewById(R.id.trips_infoview);
			lvTrips = (ListView) findViewById(R.id.trips_listview);
			lvTrips.setOnItemClickListener(lvOnClick);
			btnNext = (Button) findViewById(R.id.trips_next);
			
			infoview.setDefaultTv1("Trip List");
			infoview.setDefaultTv2("Vehicle " + Active.vehicle.No + " - " + Active.vehicle.Reg);
			infoview.setDefaultTv3("Driver " + Active.driver.No + " - " + Active.driver.Name);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
    // Prevent Back button.
    @Override
    public void onBackPressed() 
    {
    }

    @Override
    protected void onResume()
    {
    	super.onResume();
    
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trips: onResume");
			
			// Store reference to this activity.
			Active.activity = this;
			
			// Resume updating.
			infoview.resume();
			
			// Create IntentFilter for broadcast we are interested in.
			IntentFilter filter = new IntentFilter();
			filter.addAction(ColossusIntentService.BroadcastTripsChanged);
	
			// Create the BroadcastReceiver.
			receiver = new myReceiver();
			
			// Register BroadcastReceiver.
			registerReceiver(receiver, filter);
	
			// Refresh the list of trips.
			refreshData();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
    
    @Override
    protected void onPause()
    {
    	super.onPause();

    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trips: onPause");
			
			// Pause updating.
			infoview.pause();
			
			// Unregister BroadcastReceiver.
			unregisterReceiver(receiver);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
    
	// ---------------------- List --------------------------
	
	private final OnItemClickListener lvOnClick = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> a, View v, int position, long id)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trips: lvOnClick");
				
				// This has been removed, as NFO did not want the driver selecting orders at random.
				// If you need to add this back, uncomment the next line.
	
				// startTrip(id);
				
				// Show message.
				AlertDialog.Builder builder = new AlertDialog.Builder(Trips.this);
				builder.setTitle("Trip list");
				builder.setMessage("To start next trip just tap the 'Next' button");
				builder.setPositiveButton("OK", null);
				
				AlertDialog alert = builder.create();
				alert.show();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	private void refreshData()
	{
		try
		{
			// Refresh data.
			adapter = new TripAdapter(this, dbTrip.GetUndeliveredTrips(Active.vehicle, Active.driver));
			
			// Bind to listview.
			lvTrips.setAdapter(adapter);
			
			// Show/Hide next button.
			btnNext.setVisibility(adapter.getCount() > 0 ? View.VISIBLE : View.INVISIBLE);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	private void startTrip(long id)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trips: startTrip");
			
			// Find trip by ID.
			Active.trip = dbTrip.load(dbTrip.class, id);

			if (Active.trip != null)
			{
				// Create content.
				JSONObject json = new JSONObject();
				json.put("TripID", Active.trip.ColossusID);
		
				// Call ColossusIntentService.
				Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);
				i.putExtra("Type", "Trip_Started");
				i.putExtra("Content", json.toString());
				startService(i);
		
				// Start the trip activity.
				Intent intent = new Intent(this, Trip.class);
				startActivity(intent);

				// Update database.
				Active.trip.start();
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
    public void onBackClicked(View v)
    {
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trips: onBackClicked");

			CrashReporter.leaveBreadcrumb("Trips: onBackClicked - " + ((Button)v).getText().toString());

			// Show message.
			AlertDialog.Builder builder = new AlertDialog.Builder(Trips.this);
			builder.setTitle("Trip list");
			builder.setMessage("End shift.\r\nAre you sure?");
			
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					endShift();
				}
			});
			
			builder.setNegativeButton("No", null);
			
			AlertDialog alert = builder.create();
			alert.show();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }

    private void endShift()
    {
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trips: endShift");
			
			// Call ColossusIntentService.
			Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);
			i.putExtra("Type", "Shift_End");
			i.putExtra("Content", "");
			startService(i);
	
			// Close this activity.
	    	finish();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
    
    public void onSettingsClicked(View v)
    {
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trips: onSettingsClicked");

			CrashReporter.leaveBreadcrumb("Trips: onSettingsClicked - " + ((Button)v).getText().toString());

			// Show settings activity.
			Intent i = new Intent(this, Settings.class);
			startActivity(i);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
    
    public void onNextClicked(View v)
    {
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trips: onNextClicked");

            CrashReporter.leaveBreadcrumb("Trips: onNextClicked - " + ((Button)v).getText().toString());

            // Start next trip.
			startTrip(adapter.getItemId(0));
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
}
