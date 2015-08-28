package com.swiftsoft.colossus.mobileoil.view;

import com.swiftsoft.colossus.mobileoil.Active;
import com.swiftsoft.colossus.mobileoil.AnimationHelper;
import com.swiftsoft.colossus.mobileoil.CrashReporter;
import com.swiftsoft.colossus.mobileoil.InfoView_MobileDataDialog;
import com.swiftsoft.colossus.mobileoil.R;
import com.swiftsoft.colossus.mobileoil.service.ColossusIntentService;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class MyInfoView1Line extends ViewFlipper
{
	Thread thread;
	LayoutInflater inflater;
	int currentView = 0;
	
	protected int defaultViewResource = R.layout.infoview_default_1_line;
	
	// Default view (1 line).
	View infoView_default_1_line;
	TextView infoView_default_1_line_tv1;
	TextView infoView_default_1_line_tv2;
	
	// Default view (3 line)
	View infoView_default_3_line;
	TextView infoView_default_3_line_tv1;
	TextView infoView_default_3_line_tv2;
	TextView infoView_default_3_line_tv3;
	
	// Mobile data unavailable view.
	View infoView_mobiledata;
	LinearLayout infoView_mobiledata_ll;
	TextView infoView_mobiledata_tv1;
	TextView infoView_mobiledata_tv2;
	
	View infoView_unread_message;
	
	public MyInfoView1Line(Context context)
	{
		super(context);
		init(context);
	}
	
	public MyInfoView1Line(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	protected void init(Context context)
	{
		try
		{
			if (this.isInEditMode())
			{
				// Due to bugs in the ADT not allow views to inflate correctly from resources,
				// this is a dummy black LinearLayout to act as a placeholder. 
				LinearLayout dummy = new LinearLayout(context);
				dummy.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				dummy.setBackgroundColor(Color.BLACK);	
				dummy.setOrientation(LinearLayout.VERTICAL);
				this.addView(dummy);
			}
			else
			{
				inflater = LayoutInflater.from(context);
	
				if (defaultViewResource == R.layout.infoview_default_1_line)
				{
					// Inflate default 1-line view.
					infoView_default_1_line = inflater.inflate(R.layout.infoview_default_1_line, this, false);
					infoView_default_1_line_tv1 = (TextView)infoView_default_1_line.findViewById(R.id.infoview_default_1_line_tv1);
					infoView_default_1_line_tv2 = (TextView)infoView_default_1_line.findViewById(R.id.infoview_default_1_line_tv2);
					this.addView(infoView_default_1_line);
				}
				else
				{
					// Inflate default 3-line view.
					infoView_default_3_line = inflater.inflate(R.layout.infoview_default_3_line, null);
					infoView_default_3_line_tv1 = (TextView)infoView_default_3_line.findViewById(R.id.infoview_default_3_line_tv1);
					infoView_default_3_line_tv2 = (TextView)infoView_default_3_line.findViewById(R.id.infoview_default_3_line_tv2);
					infoView_default_3_line_tv3 = (TextView)infoView_default_3_line.findViewById(R.id.infoview_default_3_line_tv3);
					this.addView(infoView_default_3_line);
				}
			
				// Inflate mobile data view.
				infoView_mobiledata = inflater.inflate(R.layout.infoview_mobiledata, null);
				infoView_mobiledata_ll = (LinearLayout)infoView_mobiledata.findViewById(R.id.infoview_mobiledata_ll);
				infoView_mobiledata_tv1 = (TextView)infoView_mobiledata.findViewById(R.id.infoview_mobiledata_tv1);
				infoView_mobiledata_tv2 = (TextView)infoView_mobiledata.findViewById(R.id.infoview_mobiledata_tv2);
				infoView_mobiledata_ll.setOnClickListener(showMobileDataDialog);
				infoView_mobiledata_tv1.setOnClickListener(showMobileDataDialog);
				infoView_mobiledata_tv2.setOnClickListener(showMobileDataDialog);
				this.addView(infoView_mobiledata);
				
				// Inflate unread message.
				infoView_unread_message = inflater.inflate(R.layout.infoview_unread_message, null);
				this.addView(infoView_unread_message);
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	public void resume()
	{
		try
		{
			// Reset to default view.
			currentView = 0;
			this.setInAnimation(null);
			this.setOutAnimation(null);
			this.setDisplayedChild(0);
			
			// Start animation thread.
			startThread();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	public void pause()
	{
		try
		{
			// Interrupt the animation thread.
			thread.interrupt();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}			
	
	public void setDefaultTv1(String text)
	{
		if (defaultViewResource == R.layout.infoview_default_1_line)
			infoView_default_1_line_tv1.setText(text);
		else
			infoView_default_3_line_tv1.setText(text);
	}	
	
	public void setDefaultTv2(String text)
	{
		if (defaultViewResource == R.layout.infoview_default_1_line)
			infoView_default_1_line_tv2.setText(text);
		else
			infoView_default_3_line_tv2.setText(text);
	}
	
	public void setDefaultTv3(String text)
	{
		infoView_default_3_line_tv3.setText(text);
	}
	
	OnClickListener showMobileDataDialog = new OnClickListener()
	{		
		@Override
		public void onClick(View v)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("MyInfoView: showMobileDataDialog");

				// Show dialog.
				InfoView_MobileDataDialog dialog = new InfoView_MobileDataDialog(Active.activity);
				dialog.updateUI();
				dialog.show();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
	
	// Private methods
	private void showNextInfoView()
	{
		int oldView = currentView;
		int newView = currentView;
		
		// Find next InfoView to show.
		while (true)
		{
			if (newView++ == this.getChildCount())
			{
				newView = 0;
				break;
			}
			
			if (newView == 1)
			{
				// Show if queue is greater than 0
				if (ColossusIntentService.getQueueSize() > 0)
					break;
			}
		}
		
		// Switch to new view.
		if (oldView != newView)
		{
			currentView = newView;
			
			// Setup animation.
			this.setInAnimation(AnimationHelper.inFromUpAnimation(500));
			this.setOutAnimation(AnimationHelper.outToDownAnimation(500));
			this.setDisplayedChild(currentView);
		}
	}

	private void startThread()
	{
		thread = new Thread() {
		
			@Override public void run() {
				
				while (true)
				{
					try 
					{
			            Thread.sleep(15000);
			        } 
					catch (InterruptedException e) {
						return;
			        }
	
					MyInfoView1Line.this.post(new Runnable() {
		
						@Override
						public void run()
						{
							MyInfoView1Line.this.showNextInfoView();					
						}});
				}
				
			}
		};
		
		thread.setName("MyInfoView");
		thread.start();
	}
}
