package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.swiftsoft.colossus.mobileoil.database.adapter.DeliveredAdapter;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;

public class Trip_Delivered_List extends MyFlipperView
{
	private Trip trip;
	private Trip_Delivered tripDelivered;
	private LayoutInflater inflater;
	private DeliveredAdapter adapter;
	
	private TextView tvTripNo;
	private TextView tvCounter;
	private ListView lvOrders;
	private Button btnClose;
	
	public Trip_Delivered_List(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Delivered_List(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	public Trip_Delivered_List(Context context, Trip_Delivered tripDelivered)
	{
		super(context);
		init(context);
		
		// Store reference to Trip_Delivered dialog.
		this.tripDelivered = tripDelivered;
	}

	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Delivered_List: init");

			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_delivered_list, this, true);
			
			tvTripNo = (TextView)this.findViewById(R.id.trip_delivered_list_trip_no);
			tvCounter = (TextView)this.findViewById(R.id.trip_delivered_list_counter);
			lvOrders = (ListView)this.findViewById(R.id.trip_delivered_list_orders);
			btnClose = (Button)this.findViewById(R.id.trip_delivered_list_close);
			
			lvOrders.setOnItemClickListener(lvOnClick);
			btnClose.setOnClickListener(onClose);
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
			// Refresh data.
			adapter = new DeliveredAdapter(trip, Active.trip.GetDelivered());

			// Bind to listview.
			lvOrders.setAdapter(adapter);

			// Update UI.
			tvTripNo.setText("Trip " + Active.trip.No);
			tvCounter.setText(adapter.getCount() + " delivered");
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	OnItemClickListener lvOnClick = new OnItemClickListener() 
	{
		@Override
		public void onItemClick(AdapterView<?> a, View v, int position, long id)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Delivered_List: lvOnClick");
				
				// Find Order by ID.
				dbTripOrder order = dbTripOrder.load(dbTripOrder.class, id);
				
				// Show order.
				tripDelivered.showOrder(order);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	OnClickListener onClose = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Delivered_List: onClose");
				
				// Close dialog.
				trip.hideDelivered();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}
