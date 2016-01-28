package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Trip_Transport_Doc extends MyFlipperView
{
	private Trip trip;

	private MyInfoView1Line infoview;
	private Button btnNext;
	
	public Trip_Transport_Doc(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Transport_Doc(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Transport_Doc: init");

			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_transport_doc, this, true);
			
			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_transport_doc_infoview);
			Button btnPrint = (Button) this.findViewById(R.id.trip_transport_doc_print);
			Button btnChange = (Button) this.findViewById(R.id.trip_transport_doc_change);
			Button btnBack = (Button) this.findViewById(R.id.trip_transport_doc_back);
			btnNext = (Button)this.findViewById(R.id.trip_transport_doc_next);
			
			btnPrint.setOnClickListener(onPrint);
			btnChange.setOnClickListener(onChange);
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
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Transport_Doc: resumeView");

			// Resume updating.
			infoview.resume();
			
			// Disable 'Next' button.
			btnNext.setEnabled(false);
			
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
			CrashReporter.leaveBreadcrumb("Trip_Transport_Doc: pauseView");

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
			CrashReporter.leaveBreadcrumb("Trip_Transport_Doc: updateUI");

			// Set trip no.
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
				CrashReporter.leaveBreadcrumb("Trip_Transport_Doc: onPrint");

				// Print the transport document.
				Printing.transportDocument(trip);
				
				// Enable 'Next' button.
				btnNext.setEnabled(true);
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
				CrashReporter.leaveBreadcrumb("Trip_Transport_Doc: onChange");

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
				CrashReporter.leaveBreadcrumb("Trip_Transport_Doc: onBack");

				// Switch views.
				trip.selectView(Trip.ViewStockStart, -1);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	private final OnClickListener onNext = new OnClickListener()
	{		
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Transport_Doc: onNext");

				// Switch views.
				trip.selectView(Trip.ViewUndeliveredList, +1);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}
