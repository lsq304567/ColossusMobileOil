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
}
