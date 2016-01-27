package com.swiftsoft.colossus.mobileoil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.database.DbUtils;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;
import com.swiftsoft.colossus.mobileoil.view.MyStockSummary;

public class Trip_Stock_Start extends MyFlipperView
{
	private Trip trip;

	private MyInfoView1Line infoview;
	private MyStockSummary stockSummary;
	private TextView tvLoadingNote1;
	private TextView tvLoadingNote2;

	public Trip_Stock_Start(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Stock_Start(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Stock_Start: init");
			
			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_stock_start, this, true);
			
			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_stock_start_infoview);
			stockSummary = (MyStockSummary)this.findViewById(R.id.trip_stock_start_summary);

			tvLoadingNote1 = (TextView)this.findViewById(R.id.trip_stock_start_loading_note1);
			tvLoadingNote2 = (TextView)this.findViewById(R.id.trip_stock_start_loading_note2);

			Button btnLoad = (Button) this.findViewById(R.id.trip_stock_start_load);
			Button btnReturn = (Button) this.findViewById(R.id.trip_stock_start_return);
			Button btnBack = (Button) this.findViewById(R.id.trip_stock_start_back);
			Button btnNext = (Button) this.findViewById(R.id.trip_stock_start_next);
	
			btnLoad.setOnClickListener(onClickListener);
			btnReturn.setOnClickListener(onClickListener);
			btnBack.setOnClickListener(onClickListener);
			btnNext.setOnClickListener(onClickListener);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	@Override
	public boolean resumeView()
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Stock_Start: resumeView");

			// Resume updating.
			infoview.resume();
			
			return true;
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
			return false;
		}
	}

	@Override
	public void pauseView()
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Stock_Start: pauseView");

			// Pause updating.
			infoview.pause();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	@Override
	public void updateUI() 
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Stock_Start: updateUI");

			// Trip no.
			infoview.setDefaultTv1("Trip " + Active.trip.No);
			
			if (Active.trip.LoadingNotes == null || Active.trip.LoadingNotes.length() == 0)
			{
				tvLoadingNote1.setVisibility(View.GONE);
				tvLoadingNote2.setVisibility(View.GONE);
			}
			else
			{
				tvLoadingNote2.setText(Active.trip.LoadingNotes);
			}
			
			// Line.
			infoview.setDefaultTv2(DbUtils.getInfoviewLineProduct(Active.vehicle.getHosereelProduct()));

			stockSummary.updateStock();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private final OnClickListener onClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			try
			{
				switch (view.getId())
                {
                    case R.id.trip_stock_start_load:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Stock_Start: onClick - Load");

                        // Switch views.
                        trip.selectView(Trip.ViewStockLoad, +1);

                        break;

                    case R.id.trip_stock_start_return:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Stock_Start: onClick - Return");

                        // Switch views.
                        trip.selectView(Trip.ViewStockReturn, +1);

                        break;

                    case R.id.trip_stock_start_back:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Stock_Start: onClick - Back");

                        // Check if user really wants to end the trip.
                        AlertDialog.Builder builder = new AlertDialog.Builder(trip);
                        builder.setTitle("Trip " + Active.trip.No);
                        builder.setMessage("Do you want to end this trip?");

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                trip.tripStopped();
                            }
                        });

                        builder.setNegativeButton("No", null);

                        AlertDialog alert = builder.create();
                        alert.show();

                        break;

                    case R.id.trip_stock_start_next:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Stock_Start: onClick - Next");

                        // Switch views.
                        trip.selectView(Trip.ViewTransportDoc, +1);

                        break;
                }
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}
