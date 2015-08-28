package com.swiftsoft.colossus.mobileoil;

import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;
import com.swiftsoft.colossus.mobileoil.view.MyStockSummary;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Trip_Stock_Start extends MyFlipperView
{
	private Trip trip;
	private LayoutInflater inflater;
	
	private MyInfoView1Line infoview;
	private MyStockSummary stockSummary;
	private TextView tvLoadingNote1;
	private TextView tvLoadingNote2;
	private Button btnLoad;
	private Button btnReturn;
	private Button btnBack;
	private Button btnNext;
	
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
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_stock_start, this, true);
			
			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_stock_start_infoview);
			stockSummary = (MyStockSummary)this.findViewById(R.id.trip_stock_start_summary);
			tvLoadingNote1 = (TextView)this.findViewById(R.id.trip_stock_start_loading_note1);
			tvLoadingNote2 = (TextView)this.findViewById(R.id.trip_stock_start_loading_note2);
			btnLoad = (Button)this.findViewById(R.id.trip_stock_start_load);
			btnReturn = (Button)this.findViewById(R.id.trip_stock_start_return);
			btnBack = (Button)this.findViewById(R.id.trip_stock_start_back);
			btnNext = (Button)this.findViewById(R.id.trip_stock_start_next);
	
			btnLoad.setOnClickListener(onLoad);
			btnReturn.setOnClickListener(onReturn);
			btnBack.setOnClickListener(onBack);
			btnNext.setOnClickListener(onNext);
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
			// Update the UI.
			dbProduct lineProduct = Active.vehicle.getHosereelProduct();
			
			// Trip no.
			infoview.setDefaultTv1("Trip " + Active.trip.No);
			
			if (Active.trip.LoadingNotes == null ||
				Active.trip.LoadingNotes.length() == 0)
			{
				tvLoadingNote1.setVisibility(View.GONE);
				tvLoadingNote2.setVisibility(View.GONE);
			}
			else
			{
				tvLoadingNote2.setText(Active.trip.LoadingNotes);
			}
			
			// Line.
			if (lineProduct == null)
				infoview.setDefaultTv2("Line: None");
			else
				infoview.setDefaultTv2("Line: " + lineProduct.Desc);
	
			stockSummary.UpdateStock();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	OnClickListener onLoad = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Stock_Start: onLoad");

				// Switch views.
				trip.selectView(Trip.ViewStockLoad, +1);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	OnClickListener onReturn = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Stock_Start: onReturn");

				// Switch views.
				trip.selectView(Trip.ViewStockReturn, +1);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	OnClickListener onBack = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Stock_Start: onBack");

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
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	OnClickListener onNext = new OnClickListener()
	{		
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Stock_Start: onNext");

				// Switch views.
				trip.selectView(Trip.ViewTransportDoc, +1);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}
