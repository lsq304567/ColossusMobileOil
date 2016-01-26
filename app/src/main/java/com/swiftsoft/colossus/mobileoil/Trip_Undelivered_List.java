package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.swiftsoft.colossus.mobileoil.database.adapter.UndeliveredAdapter;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Trip_Undelivered_List extends MyFlipperView
{
	private Trip trip;
	private UndeliveredAdapter adapter;
	
	private MyInfoView1Line infoview;
	private ListView lvOrders;

	public Trip_Undelivered_List(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Undelivered_List(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_List: init");

			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_undelivered_list, this, true);

			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_undelivered_list_infoview);

            lvOrders = (ListView)this.findViewById(R.id.trip_undelivered_list_orders);

			lvOrders.setOnItemClickListener(lvOnClick);

            Button btnBack = (Button) this.findViewById(R.id.trip_undelivered_list_back);
            Button btnDelivered = (Button) this.findViewById(R.id.trip_undelivered_list_delivered);
            Button btnNext = (Button) this.findViewById(R.id.trip_undelivered_list_next);

			btnBack.setOnClickListener(onClickListener);
			btnDelivered.setOnClickListener(onClickListener);
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
            CrashReporter.leaveBreadcrumb("Trip_Undelivered_List: resumeView");

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
            CrashReporter.leaveBreadcrumb("Trip_Undelivered_List: pauseView");

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
            CrashReporter.leaveBreadcrumb("Trip_Undelivered_List: updateUI");

            // Refresh data.
			adapter = new UndeliveredAdapter(trip, Active.trip.GetUndelivered());
			
			// Bind to listview.
			lvOrders.setAdapter(adapter);
	
			// Update UI.
			infoview.setDefaultTv1("Trip " + Active.trip.No);
			infoview.setDefaultTv2(adapter.getCount() + " undelivered");
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private final OnItemClickListener lvOnClick = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> a, View v, int position, long id)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_List: lvOnClick");

				trip.OrderId = id;

				startOrder(id);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

    private final OnClickListener onClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            try
            {
                switch (view.getId())
                {
                    case R.id.trip_undelivered_list_back:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_List: onClick - Back");

                        // Switch views.
                        trip.selectView(Trip.ViewTransportDoc, -1);

                        break;

                    case R.id.trip_undelivered_list_delivered:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_List: onClick - Delivered");

                        // Show delivered dialog.
                        trip.showDelivered();

                        break;

                    case R.id.trip_undelivered_list_next:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_List: onClick - Next");

                        // Check if all delivered.
                        if (Active.trip.GetUndelivered().size() > 0)
                        {
                            trip.OrderId = adapter.getItemId(0);
                            startOrder(adapter.getItemId(0));
                        }
                        else
                        {
                            // All deliveries done; switch to stock at end view.
                            trip.selectView(Trip.ViewStockEnd, +1);
                        }

                        break;
                }
            }
            catch (Exception e)
            {
                CrashReporter.logHandledException(e);
            }
        }
    };

	private void startOrder(long id)
	{
        CrashReporter.leaveBreadcrumb("Trip_Undelivered_List: startOrder");

        // Start next order.
		Active.order = dbTripOrder.load(dbTripOrder.class, id);

		if (Active.order != null)
		{
			// Mark the Active.order as Delivering.
			trip.orderStarted();
			
			// Switch to order summary view.
			trip.selectView(Trip.ViewUndeliveredSummary, +1);
		}
	}
}
