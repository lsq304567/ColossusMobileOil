package com.swiftsoft.colossus.mobileoil;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ViewFlipper;

import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;

public class Trip_Delivered extends Dialog
{
	private Trip_Delivered_Order tripDeliveredOrder;
	
	private ViewFlipper viewFlipper;
	
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
			Trip trip = (Trip) context;
			
			// Create views
			Trip_Delivered_List tripDeliveredList = new Trip_Delivered_List(trip, this);
			tripDeliveredOrder = new Trip_Delivered_Order(trip, this);
			
			// Add views to flipper.
			viewFlipper = (ViewFlipper)findViewById(R.id.trip_delivered_flipper);
			viewFlipper.addView(tripDeliveredList);
			viewFlipper.addView(tripDeliveredOrder);
			
			// Select list view.
			tripDeliveredList.updateUI();
			viewFlipper.setDisplayedChild(0);
			
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
	   		viewFlipper.setInAnimation(AnimationHelper.inFromLeftAnimation());
	   		viewFlipper.setOutAnimation(AnimationHelper.outToRightAnimation());
	   		
			viewFlipper.setDisplayedChild(0);
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
			viewFlipper.setInAnimation(AnimationHelper.inFromRightAnimation());
			viewFlipper.setOutAnimation(AnimationHelper.outToLeftAnimation());
	    	
	    	// Change view.
			tripDeliveredOrder.setOrder(order);
			tripDeliveredOrder.updateUI();
			viewFlipper.setDisplayedChild(1);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
}
