package com.swiftsoft.colossus.mobileoil;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.database.adapter.DebugMessageAdapter;
import com.swiftsoft.colossus.mobileoil.database.adapter.MessageOutAdapter;
import com.swiftsoft.colossus.mobileoil.database.model.dbMessageOut;
import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;
import com.swiftsoft.colossus.mobileoil.service.ColossusIntentService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class InfoView_MobileDataDialog extends Dialog
{
	private myReceiver receiver;
    private TelephonyManager Tel;
    private MyPhoneStateListener MyListener;
	private long listCount = -1;
    private long listDateTime = -1;
    private List<String> debugMessages;
    
    private Activity activity;
	private Dialog dialog;
	private TextView tvPhone;
	private TextView tvData;
	private TextView tvQueue;
	private ListView lv1;
	private ListView lv2;
	
	public InfoView_MobileDataDialog(Context context)
	{
		super(context);
		
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("InfoView_MobileDataDialog: constructor");
	
			// Store references.
			activity = (Activity)context;
			dialog = this;
			
			// Find URL of customer's WebService.
			String colossusURL = dbSetting.FindByKey("ColossusURL").StringValue;
			
			// Setup view.
			requestWindowFeature(Window.FEATURE_NO_TITLE); 
			setContentView(R.layout.infoview_mobiledatadialog);
			
			// Set dialog width & height to 'Fill parent'.
	        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	        
	        // Find controls.
			TextView tvURL = (TextView) this.findViewById(R.id.infoview_mobiledatadialog_url);
	        tvPhone = (TextView)this.findViewById(R.id.infoview_mobiledatadialog_phone);
	        tvData = (TextView)this.findViewById(R.id.infoview_mobiledatadialog_data);
	        tvQueue = (TextView)this.findViewById(R.id.infoview_mobiledatadialog_queue);
	        lv1 = (ListView)this.findViewById(R.id.infoview_mobiledatadialog_lv1);
	        lv2 = (ListView)this.findViewById(R.id.infoview_mobiledatadialog_lv2);
	        
	        tvURL.setText("URL: " + colossusURL);
	        
	        // Attach event handler.
	        Button btnClose = (Button)this.findViewById(R.id.infoview_mobiledatadialog_close);
	        btnClose.setOnClickListener(onClose);

	        // Listen for colossus broadcasts.
	        debugMessages = new ArrayList<String>();
			receiver = new myReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(ColossusIntentService.BroadcastCommsDebug);
			activity.registerReceiver(receiver, filter);
	        
	        // Listen for phone state events.
	        Tel = (TelephonyManager) Active.activity.getSystemService(Context.TELEPHONY_SERVICE);
	        MyListener = new MyPhoneStateListener();
	        Tel.listen(MyListener, PhoneStateListener.LISTEN_SERVICE_STATE | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private final View.OnClickListener onClose = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("InfoView_MobileDataDialog: onClick");

				// Stop listening to colossus broadcasts.
				activity.unregisterReceiver(receiver);

				// Stop listening to phone state events.
				Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);

				// Close dialog.
				dialog.dismiss();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	public void updateUI()
	{
		try
		{
			// Get content for list 1
			List<dbMessageOut> messages = dbMessageOut.GetAll();

			// Hash all DateTimes. 
			long dateTime = 0;

			for (dbMessageOut message : messages)
            {
                dateTime += message.DateTime;
            }

			if (messages.size() != listCount || dateTime != listDateTime)
			{
				// Refresh data.
				MessageOutAdapter adapter1 = new MessageOutAdapter(activity, messages);
				
				// Bind to listview.
				lv1.setAdapter(adapter1);
				
				// Store totals.
				listCount = messages.size();
				listDateTime = dateTime;
			}
			
			// Get content for list 2.
			DebugMessageAdapter adapter2 = new DebugMessageAdapter(activity, debugMessages);
			lv2.setAdapter(adapter2);
			
			// Update queue length.
			if (ColossusIntentService.getQueueSize() >= 0)
            {
                tvQueue.setText(String.format("%d", ColossusIntentService.getQueueSize()));
            }
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}		
	}
	
	class myReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("MyInfoView: onReceive");
				
				if (intent.getAction().equals(ColossusIntentService.BroadcastCommsDebug))
				{
					// Add new debug message.
					Bundle bundle = intent.getExtras();

					if (bundle != null)
					{
						DateFormat df = new SimpleDateFormat("HH:mm:ss");

                        debugMessages.add(0, df.format(Utils.getCurrentTime()) + " " + bundle.getString("Message"));
					}
					
					// Update the UI.
					updateUI();
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	}

	private class MyPhoneStateListener extends PhoneStateListener {

	    @Override
	    public void onServiceStateChanged(ServiceState serviceState)
	    {
	    	super.onServiceStateChanged(serviceState);

			tvPhone.setText(serviceState.getState() == ServiceState.STATE_IN_SERVICE ? "Yes" : "No");
	    }
	    
	    @Override
	    public void onDataConnectionStateChanged(int state, int networkType)
	    {
	    	super.onDataConnectionStateChanged(state, networkType);

			tvData.setText(state == TelephonyManager.DATA_CONNECTED ? "Yes" : "No");
	    }
	}

}

