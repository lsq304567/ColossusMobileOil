package com.swiftsoft.colossus.mobileoil.view;

import com.swiftsoft.colossus.mobileoil.CrashReporter;
import com.swiftsoft.colossus.mobileoil.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.LightingColorFilter;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;

public class MyNumericKeypad extends TableLayout
{
	private Activity activity;
	private Dialog dialog;
	
	public MyNumericKeypad(Context context)
	{
		super(context);
		init();
	}

	public MyNumericKeypad(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	
	private void init()
	{
		try
		{
			LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			li.inflate(R.layout.numeric_keypad, this, true);
	
			if (!isInEditMode())
			{
				Button      keyPad1      = (Button)this.findViewById(R.id.keypad_one);
				Button      keyPad2      = (Button)this.findViewById(R.id.keypad_two);
				Button      keyPad3      = (Button)this.findViewById(R.id.keypad_three);
				Button      keyPad4      = (Button)this.findViewById(R.id.keypad_four);
				Button      keyPad5      = (Button)this.findViewById(R.id.keypad_five);
				Button      keyPad6      = (Button)this.findViewById(R.id.keypad_six);
				Button      keyPad7      = (Button)this.findViewById(R.id.keypad_seven);
				Button      keyPad8      = (Button)this.findViewById(R.id.keypad_eight);
				Button      keyPad9      = (Button)this.findViewById(R.id.keypad_nine);
				Button      keyPad0      = (Button)this.findViewById(R.id.keypad_zero);
				Button      keyPadDot    = (Button)this.findViewById(R.id.keypad_dot);
				Button      keyPadMinus  = (Button)this.findViewById(R.id.keypad_minus);
				ImageButton keyPadDel    = (ImageButton)this.findViewById(R.id.keypad_del);
				Button      keyPadNext   = (Button)this.findViewById(R.id.keypad_next);
				Button      keyPadSpare1 = (Button)this.findViewById(R.id.keypad_spare1);
				Button      keyPadSpare2 = (Button)this.findViewById(R.id.keypad_spare2);
		
				keyPadDel.getBackground().setColorFilter(new LightingColorFilter(0xff7f8F8F, 0x00000000));
				keyPadNext.getBackground().setColorFilter(new LightingColorFilter(0xff7f8F8F, 0x00000000));
				keyPadSpare1.getBackground().setColorFilter(new LightingColorFilter(0xff7f8F8F, 0x00000000));
				keyPadSpare2.getBackground().setColorFilter(new LightingColorFilter(0xff7f8F8F, 0x00000000));
				
				keyPad1.setTag(KeyEvent.KEYCODE_1);
				keyPad2.setTag(KeyEvent.KEYCODE_2);
				keyPad3.setTag(KeyEvent.KEYCODE_3);
				keyPad4.setTag(KeyEvent.KEYCODE_4);
				keyPad5.setTag(KeyEvent.KEYCODE_5);
				keyPad6.setTag(KeyEvent.KEYCODE_6);
				keyPad7.setTag(KeyEvent.KEYCODE_7);
				keyPad8.setTag(KeyEvent.KEYCODE_8);
				keyPad9.setTag(KeyEvent.KEYCODE_9);
				keyPad0.setTag(KeyEvent.KEYCODE_0);
				keyPadDot.setTag(KeyEvent.KEYCODE_PERIOD);
				keyPadMinus.setTag(KeyEvent.KEYCODE_MINUS);
				keyPadDel.setTag(KeyEvent.KEYCODE_DEL);
				keyPadNext.setTag(KeyEvent.KEYCODE_ENTER);
				
				keyPad1.setOnClickListener(onClick);
				keyPad2.setOnClickListener(onClick);
				keyPad3.setOnClickListener(onClick);
				keyPad4.setOnClickListener(onClick);
				keyPad5.setOnClickListener(onClick);
				keyPad6.setOnClickListener(onClick);
				keyPad7.setOnClickListener(onClick);
				keyPad8.setOnClickListener(onClick);
				keyPad9.setOnClickListener(onClick);
				keyPad0.setOnClickListener(onClick);
				keyPadDot.setOnClickListener(onClick);
				keyPadMinus.setOnClickListener(onClick);
				keyPadDel.setOnClickListener(onClick);
				keyPadNext.setOnClickListener(onClick);
		
				Context context = this.getContext();

				if (context.getClass() != ContextThemeWrapper.class)
				{
					activity = (Activity) context;
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	OnClickListener onClick = new OnClickListener() {

		@Override
		public void onClick(View paramView)
		{
			try
			{
				int keyCode = Integer.parseInt(paramView.getTag().toString());
				sendKey(keyCode);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	private void sendKey(int keyCode)
	{
		try
		{
			KeyEvent event1 = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
			KeyEvent event2 = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
	
			if (activity != null)
			{
				activity.dispatchKeyEvent(event1);
				activity.dispatchKeyEvent(event2);
			}
			
			if (dialog != null)
			{
				dialog.dispatchKeyEvent(event1);
				dialog.dispatchKeyEvent(event2);
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	// Hack
	public void setDialog(Dialog d)
	{
		dialog = d;
	}
}
