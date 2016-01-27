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

	private MyInfoView1Line infoview;
	private MyStockSummary stockSummary;

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
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_stock_end, this, true);

			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_stock_end_infoview);
			stockSummary = (MyStockSummary)this.findViewById(R.id.trip_stock_end_summary);

			Button btnReturn = (Button) this.findViewById(R.id.trip_stock_end_return);
			Button btnBack = (Button) this.findViewById(R.id.trip_stock_end_back);
			Button btnNext = (Button) this.findViewById(R.id.trip_stock_end_next);
	
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
			CrashReporter.leaveBreadcrumb("Trip_Stock_End: resumeView");

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
			CrashReporter.leaveBreadcrumb("Trip_Stock_End: pauseView");

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
			CrashReporter.leaveBreadcrumb("Trip_Stock_End: updateUI");

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

    private final OnClickListener onClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            try
            {
                switch (view.getId())
                {
                    case R.id.trip_stock_end_return:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Stock_End: onClick - Return");

                        // Switch views.
                        trip.selectView(Trip.ViewStockReturn, +1);

                        break;

                    case R.id.trip_stock_end_back:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Stock_End: onClick - Back");

                        // Switch views.
                        trip.selectView(Trip.ViewUndeliveredList, -1);

                        break;

                    case R.id.trip_stock_end_next:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Stock_End: onClick - Next");

                        // Switch views.
                        trip.selectView(Trip.ViewTripReport, +1);

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
