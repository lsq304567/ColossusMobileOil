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

public class UndeliveredAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;
	private ArrayList<dbTripOrder> mItems;

	public UndeliveredAdapter(Context context, Collection<? extends dbTripOrder> items)
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
				convertView = mInflater.inflate(R.layout.trip_undelivered_list_row, null);
	
				holder = new ViewHolder();
				holder.tv1 = (TextView) convertView.findViewById(R.id.trip_undelivered_row1);
				holder.tv2 = (TextView) convertView.findViewById(R.id.trip_undelivered_row2);
				holder.tv3 = (TextView) convertView.findViewById(R.id.trip_undelivered_row3);
				holder.tv4 = (TextView) convertView.findViewById(R.id.trip_undelivered_row4);
	
				convertView.setTag(holder);
			} else
			{
				holder = (ViewHolder) convertView.getTag();
			}
	
			// Find reference to order.
			dbTripOrder order = mItems.get(position);
	
			// Build line 4.
			String line4 = "";
			if (order.RequiredBy.length() > 0)
			{
				line4 = "Required by " + order.RequiredBy;
				
				if (order.Notes.length() > 0)
					line4 += "\n";
			}
			line4 += order.Notes.replace("\n", ", ");
	
			// Show/hide line 4.
			if (line4.length() > 0)
				holder.tv4.setVisibility(View.VISIBLE);
			else
				holder.tv4.setVisibility(View.GONE);
	
			// Set all text values.
			holder.tv1.setText("#" + String.valueOf(order.DeliveryOrder) + " - " + order.InvoiceNo + "  " + order.DeliveryName);
			holder.tv2.setText(order.DeliveryAddress.replace("\n", ", ") + " " + order.DeliveryPostcode);
			holder.tv3.setText(order.getProductsOrdered(" and "));
			holder.tv4.setText(line4);
	
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
		TextView tv4;
	}
}
