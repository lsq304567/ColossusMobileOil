package com.swiftsoft.colossus.mobileoil.database.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.CrashReporter;
import com.swiftsoft.colossus.mobileoil.R;
import com.swiftsoft.colossus.mobileoil.database.model.dbMessageOut;

public class MessageOutAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;
	private ArrayList<dbMessageOut> mItems;

	public MessageOutAdapter(Context context, Collection<? extends dbMessageOut> items)
	{
		mInflater = LayoutInflater.from(context);

		mItems = new ArrayList<dbMessageOut>();
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
			DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			ViewHolder holder;
	
			if (convertView == null)
			{
				convertView = mInflater.inflate(R.layout.infoview_mobiledatadialog_row1, null);
	
				holder = new ViewHolder();
				holder.tv1 = (TextView) convertView.findViewById(R.id.infoview_mobiledatadialog_row1a);
				holder.tv2 = (TextView) convertView.findViewById(R.id.infoview_mobiledatadialog_row1b);
	
				convertView.setTag(holder);
			} else
			{
				holder = (ViewHolder) convertView.getTag();
			}
	
			dbMessageOut message = mItems.get(position);
			holder.tv1.setText("#" + (position + 1) + " " + df.format(message.DateTime));
			holder.tv2.setText("Device: " + message.DeviceNo + "  " + message.Type);
	
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
	}
}
