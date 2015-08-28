package com.swiftsoft.colossus.mobileoil.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class MyFlipperView extends LinearLayout
{
	public MyFlipperView(Context context)
	{
		super(context);
	}

	public MyFlipperView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public boolean resumeView()
	{
		return true;
	}
	
	public void pauseView()
	{
	}
	
	public void setPreviousView(String name)
	{
	}
	
	public void updateUI()
	{
	}
}
