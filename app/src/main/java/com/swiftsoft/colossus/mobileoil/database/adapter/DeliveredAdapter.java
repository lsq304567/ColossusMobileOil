package com.swiftsoft.colossus.mobileoil.database.adapter;

import java.util.ArrayList;
import java.util.Collection;

import com.swiftsoft.colossus.mobileoil.CrashReporter;
import com.swiftsoft.colossus.mobileoil.R;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DeliveredAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;
	private ArrayList<dbTripOrder> mItems;

	public DeliveredAdapter(Context context, Collection<? extends dbTripOrder> items)
	{
		mInflater = LayoutInflater.from(context);

		mItems = new ArrayList<dbTripOrder>();
		mItems.addAll(items);
	}

	public void remove(int position)
	{
		mItems.remove(position);
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
			ViewHolder holder;
	
			if (convertView == null)
			{
				convertView = mInflater.inflate(R.layout.trip_delivered_list_row, null);
	
				holder = new ViewHolder();
				holder.tv1 = (TextView) convertView.findViewById(R.id.trip_delivered_list_row1);
				holder.tv2 = (TextView) convertView.findViewById(R.id.trip_delivered_list_row2);
				holder.tv3 = (TextView) convertView.findViewById(R.id.trip_delivered_list_row3);
	
				convertView.setTag(holder);
			} else
			{
				holder = (ViewHolder) convertView.getTag();
			}
	
			dbTripOrder order = mItems.get(position);
			holder.tv1.setText("#" + String.valueOf(order.DeliveryNo) + " - " + order.InvoiceNo + "  " + order.DeliveryName);
			holder.tv2.setText(order.DeliveryAddress.replace("\n", ", "));
			holder.tv3.setText(order.getProductsDelivered()); 
	
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
