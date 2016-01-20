package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.swiftsoft.colossus.mobileoil.database.DbUtils;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;
import com.swiftsoft.colossus.mobileoil.view.MyStockSummary;

public class Trip_Stock_End extends MyFlipperView
{
	private Trip trip;
	private LayoutInflater inflater;
	
	private MyInfoView1Line infoview;
	private MyStockSummary stockSummary;
	private Button btnReturn;
	private Button btnBack;
	private Button btnNext;
	
	public Trip_Stock_End(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Stock_End(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Stock_End: init");
			
			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_stock_end, this, true);

			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_stock_end_infoview);
			stockSummary = (MyStockSummary)this.findViewById(R.id.trip_stock_end_summary);
			btnReturn = (Button)this.findViewById(R.id.trip_stock_end_return);
			btnBack = (Button)this.findViewById(R.id.trip_stock_end_back);
			btnNext = (Button)this.findViewById(R.id.trip_stock_end_next);
	
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
			// Trip no.
			infoview.setDefaultTv1("Trip " + Active.trip.No);
			
			// Line.
			infoview.setDefaultTv2(DbUtils.getInfoviewLineProduct(Active.vehicle.getHosereelProduct()));

			stockSummary.updateStock();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	OnClickListener onReturn = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Stock_End: onReturn");
				
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
				CrashReporter.leaveBreadcrumb("Trip_Stock_End: onBack");
				
				// Switch views.
				trip.selectView(Trip.ViewUndeliveredList, -1);
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
				CrashReporter.leaveBreadcrumb("Trip_Stock_End: onNext");
				
				// Switch views.
				trip.selectView(Trip.ViewTripReport, +1);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}
