package com.swiftsoft.colossus.mobileoil.database.adapter;

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

public class DebugMessageAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;
	private ArrayList<String> mItems;

	public DebugMessageAdapter(Context context, Collection<? extends String> items)
	{
		mInflater = LayoutInflater.from(context);

		mItems = new ArrayList<String>();
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
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		try
		{
			ViewHolder holder;
	
			if (convertView == null)
			{
				convertView = mInflater.inflate(R.layout.infoview_mobiledatadialog_row2, null);
	
				holder = new ViewHolder();
				holder.tv1 = (TextView) convertView.findViewById(R.id.infoview_mobiledatadialog_row2a);
	
				convertView.setTag(holder);
			} else
			{
				holder = (ViewHolder) convertView.getTag();
			}

			String message = mItems.get(position);
			holder.tv1.setText(message);
	
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
	}
}
