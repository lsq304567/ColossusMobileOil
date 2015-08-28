package com.swiftsoft.colossus.mobileoil.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class MyEditText extends EditText
{
	public MyEditText(Context context)
	{
		super(context);
		init();
	}
	
	public MyEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public MyEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init()
	{
		if (!isInEditMode())
		{
			this.setOnTouchListener(new OnTouchListener() {
	
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					EditText et = (EditText)v;
					
					// Prevent Android keyboard from appearing.
					et.requestFocus();
					et.setSelection(et.getText().length(), et.getText().length());
					return true;
				}});
			
			this.setOnLongClickListener(new OnLongClickListener() {
	
				@Override
				public boolean onLongClick(View v)
				{
					// Disable long click - to prevent 'select word', 'select all', 'input method' dialog.
					return true;
				}});
		}
	}
}
