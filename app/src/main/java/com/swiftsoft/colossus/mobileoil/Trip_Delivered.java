package com.swiftsoft.colossus.mobileoil;

import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ViewFlipper;

public class Trip_Delivered extends Dialog
{
	private Trip trip;
	private Trip_Delivered_List tripDeliveredList;
	private Trip_Delivered_Order tripDeliveredOrder;
	
	private ViewFlipper vf;
	
	public Trip_Delivered(Context context)
	{
		super(context);
		
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Delivered: constructor");
			
			// Setup view.
			requestWindowFeature(Window.FEATURE_NO_TITLE); 
			setContentView(R.layout.trip_delivered);
			
			// Store reference to Trip activity.
			trip = (Trip)context;
			
			// Create views
			tripDeliveredList = new Trip_Delivered_List(trip, this);
			tripDeliveredOrder = new Trip_Delivered_Order(trip, this);
			
			// Add views to flipper.
			vf = (ViewFlipper)findViewById(R.id.trip_delivered_flipper);
			vf.addView(tripDeliveredList);
			vf.addView(tripDeliveredOrder);
			
			// Select list view.
			tripDeliveredList.updateUI();
			vf.setDisplayedChild(0);
			
			// Set dialog width & height to 'Fill parent'.
	        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	public void showList()
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Delivered: showList");
			
	    	// Setup animation.
	   		vf.setInAnimation(AnimationHelper.inFromLeftAnimation());
	   		vf.setOutAnimation(AnimationHelper.outToRightAnimation());
	   		
			vf.setDisplayedChild(0);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	public void showOrder(dbTripOrder order)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Delivered: showOrder");
			
	    	// Setup animation.
			vf.setInAnimation(AnimationHelper.inFromRightAnimation());
			vf.setOutAnimation(AnimationHelper.outToLeftAnimation());
	    	
	    	// Change view.
			tripDeliveredOrder.setOrder(order);
			tripDeliveredOrder.updateUI();
			vf.setDisplayedChild(1);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
}
