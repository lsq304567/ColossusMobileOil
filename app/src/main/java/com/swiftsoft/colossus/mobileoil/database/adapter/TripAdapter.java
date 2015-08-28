package com.swiftsoft.colossus.mobileoil.database.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import com.swiftsoft.colossus.mobileoil.CrashReporter;
import com.swiftsoft.colossus.mobileoil.R;
import com.swiftsoft.colossus.mobileoil.database.model.dbTrip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TripAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;
	private ArrayList<dbTrip> mItems;

	public TripAdapter(Context context, Collection<? extends dbTrip> items)
	{
		mInflater = LayoutInflater.from(context);

		mItems = new ArrayList<dbTrip>();
		mItems.addAll(items);
	}

	@Override
	public int getCount()
	{
		return mItems.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return mItems.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		try
		{
			DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
			ViewHolder holder;
	
			if (convertView == null)
			{
				convertView = mInflater.inflate(R.layout.trips_row, null);
	
				holder = new ViewHolder();
				holder.tv1 = (TextView) convertView.findViewById(R.id.trips_row1);
				holder.tv2 = (TextView) convertView.findViewById(R.id.trips_row2);
				holder.tv3 = (TextView) convertView.findViewById(R.id.trips_row3);
	
				convertView.setTag(holder);
			} else
			{
				holder = (ViewHolder) convertView.getTag();
			}
	
			// Find reference to trip.
			dbTrip trip = mItems.get(position);
	
			// Set all text values.
			holder.tv1.setText("Trip " + String.valueOf(trip.No) + "  " + df.format(trip.Date));
			holder.tv2.setText(trip.GetOrders().size() + " orders, " + trip.GetUndelivered().size() + " undelivered");
			holder.tv3.setText("");
			
			return convertView;
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
			return null;
		}
	}

	class ViewHolder
	{
		TextView tv1;
		TextView tv2;
		TextView tv3;
	}
}
