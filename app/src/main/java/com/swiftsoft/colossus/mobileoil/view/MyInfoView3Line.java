package com.swiftsoft.colossus.mobileoil.view;

import com.swiftsoft.colossus.mobileoil.R;

import android.content.Context;
import android.util.AttributeSet;

public class MyInfoView3Line extends MyInfoView1Line
{
	public MyInfoView3Line(Context context)
	{
		super(context);
	}
	
	public MyInfoView3Line(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void init(Context context)
	{
		defaultViewResource = R.layout.infoview_default_3_line;
		
		super.init(context);
	}

	public String getDefaultTv3()
	{
		if (defaultViewResource == R.layout.infoview_default_1_line)
		{
			return infoView_default_1_line_tv2.getText().toString();
		}
		else
		{
			return infoView_default_3_line_tv3.getText().toString();
		}
	}
}
