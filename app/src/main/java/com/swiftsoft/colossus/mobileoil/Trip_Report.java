package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Trip_Report extends MyFlipperView
{
	private Trip trip;

	private MyInfoView1Line infoview;
	private Button btnFinish;
	
	public Trip_Report(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Report(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Report: init");

			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_report, this, true);

			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_report_infoview);
			Button btnPrint = (Button) this.findViewById(R.id.trip_report_print);
			Button btnChange = (Button) this.findViewById(R.id.trip_report_change);
			Button btnBack = (Button) this.findViewById(R.id.trip_report_back);
			btnFinish = (Button)this.findViewById(R.id.trip_report_finish);
			
			btnPrint.setOnClickListener(onPrint);
			btnChange.setOnClickListener(onChange);
			btnBack.setOnClickListener(onBack);
			btnFinish.setOnClickListener(onFinish);
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
			CrashReporter.leaveBreadcrumb("Trip_Report: resumeView");

			// Resume updating.
			infoview.resume();
			
			// Disable 'Finish' button.
			btnFinish.setEnabled(false);
			
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
			CrashReporter.leaveBreadcrumb("Trip_Report: pauseView");

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
			CrashReporter.leaveBreadcrumb("Trip_Report: updateUI");

			// Update trip no.
			infoview.setDefaultTv1("Trip " + Active.trip.No);
			infoview.setDefaultTv2("");
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private final OnClickListener onPrint = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Report: onPrint");
				
				// Print trip report.
				Printing.tripReport(trip);
			
				// Enable 'Finish' button.
				btnFinish.setEnabled(true);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	private final OnClickListener onChange = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Report: onChange");
				
				// Show Settings activity.
				trip.changeSettings();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	private final OnClickListener onBack = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Report: onBack");
				
				// Show Stock End view.
				trip.selectView(Trip.ViewStockEnd, -1);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	private final OnClickListener onFinish = new OnClickListener()
	{		
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Report: onFinish");
				
				// Trip now complete.
				trip.tripDelivered();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}