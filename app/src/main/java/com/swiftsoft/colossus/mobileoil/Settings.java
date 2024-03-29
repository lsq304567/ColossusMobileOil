package com.swiftsoft.colossus.mobileoil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.swiftsoft.colossus.mobileoil.bluetooth.Discovery;
import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;

public class Settings extends Activity
{
	private static final int REQUEST_DISCOVERY = 2001;

	private int printerDemoCounter;
	private TextView tvPrinterName;
	private TextView tvPrinterAddress;
	private Button btnPrinterTest;
	private int metermateDemoCounter;
	private TextView tvMeterMateName;
	private TextView tvMeterMateAddress;
	private TextView tvURL;
	private EditText etURL;
	private TextView tvSerialNo;
	
	private dbSetting printerName;
	private dbSetting printerAddress;
	private String printerOldNameStringValue;
	private String printerOldAddressStringValue;
	
	private dbSetting metermateName;
	private dbSetting metermateAddress;
	private String metermateOldNameStringValue;
	private String metermateOldAddressStringValue;
	
	private dbSetting colossusURL;
	private String colossusURLValue;

    private dbSetting logBluetoothData;
    private Button btnLogData;

    private final String DATA_LOGGED = "Logging";
    private final String DATA_NOT_LOGGED = "Not Logging";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Settings: onCreate");

			// Setup view.
			setContentView(R.layout.settings);
	
			// Find UI controls.
			tvPrinterName = (TextView) this.findViewById(R.id.settings_printer_name);
			tvPrinterName.setOnClickListener(onPrinterClicked);
			tvPrinterAddress = (TextView) this.findViewById(R.id.settings_printer_address);
			tvPrinterAddress.setOnClickListener(onPrinterClicked);
			btnPrinterTest = (Button) this.findViewById(R.id.settings_printer_test);
		
			tvMeterMateName = (TextView) this.findViewById(R.id.settings_metermate_name);
			tvMeterMateName.setOnClickListener(onMeterMateClicked);
			tvMeterMateAddress = (TextView) this.findViewById(R.id.settings_metermate_address);
			tvMeterMateAddress.setOnClickListener(onMeterMateClicked);
	
			tvURL = (TextView) this.findViewById(R.id.settings_url_tv);
			etURL = (EditText) this.findViewById(R.id.settings_url_et);
			tvSerialNo = (TextView) this.findViewById(R.id.settings_serial_no);

            btnLogData = (Button)this.findViewById(R.id.settings_log_data);
			
			// Find/Create settings in database.
			printerName = dbSetting.FindByKeyOrCreate("PrinterName");
			printerOldNameStringValue = printerName.StringValue;
			
			printerAddress = dbSetting.FindByKeyOrCreate("PrinterAddress");
			printerOldAddressStringValue = printerAddress.StringValue;
			
			metermateName = dbSetting.FindByKeyOrCreate("MeterMateName");
			metermateOldNameStringValue = metermateName.StringValue;
			
			metermateAddress = dbSetting.FindByKeyOrCreate("MeterMateAddress");
			metermateOldAddressStringValue = metermateAddress.StringValue;

            logBluetoothData = dbSetting.FindByKeyOrCreate("LogBluetoothData");

            btnLogData.setText(logBluetoothData.IntValue == 0 ? DATA_NOT_LOGGED : DATA_LOGGED);

			colossusURL = dbSetting.FindByKey("ColossusURL");
			colossusURLValue = colossusURL.StringValue;
			
			tvURL.setVisibility(View.GONE);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
    // Prevent Back button.
    @Override
    public void onBackPressed()
	{
    }

    @Override
    protected void onResume() 
    {
    	super.onResume();
    	
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Settings: onResume");
			
	    	// Reset counters.
	    	printerDemoCounter = 0;
	    	metermateDemoCounter = 0;
	    	
			updateUI();
    	}
    	catch (Exception e)
    	{
    		CrashReporter.logHandledException(e);
    	}
    }

    private void updateUI()
    {
    	try
    	{
			// Update UI.
			tvPrinterName.setText(Utils.toStringNoNull(printerName.StringValue, "(none)"));
			tvPrinterAddress.setText(Utils.toStringNoNull(printerAddress.StringValue, ""));

			btnPrinterTest.setEnabled(printerName.StringValue != null);
	
			tvMeterMateName.setText(Utils.toStringNoNull(metermateName.StringValue, "(none)"));
			tvMeterMateAddress.setText(Utils.toStringNoNull(metermateAddress.StringValue, ""));
			
			tvURL.setText(colossusURLValue);
			etURL.setText(colossusURLValue);

			tvSerialNo.setText(Utils.getSerialNo(this));
    	}
    	catch (Exception e)
    	{
    		CrashReporter.logHandledException(e);
    	}
    }
    
    private final OnClickListener onPrinterClicked = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Settings: onPrinterClicked");

				if (printerDemoCounter++ == 5)
				{
					// Reset counter.				
					printerDemoCounter = 0;
	
					// Inform user.
					Toast.makeText(getApplicationContext(), "Demo Printer selected", Toast.LENGTH_LONG).show();
					
					// Switch printer to demo mode.
					printerName.StringValue = "Demo simulator";
					printerName.save();
					
					printerAddress.StringValue = "00:00:00:00:00";
					printerAddress.save();
	
					updateUI();
				}
				
				// Reset other counter.
				metermateDemoCounter = 0;
			}
	    	catch (Exception e)
	    	{
	    		CrashReporter.logHandledException(e);
	    	}
		}
	};
    
    public void onPrinterChange(View v)
    {
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Settings: onPrinterChange");

			CrashReporter.leaveBreadcrumb("Settings: onPrinterChange - " + ((Button)v).getText().toString());
			
	    	// Reset counters.
	    	printerDemoCounter = 0;
	    	metermateDemoCounter = 0;
	    	
	    	// Discover new printer.
			Intent intent = new Intent(getApplicationContext(), Discovery.class);

			intent.putExtra("type", "Printer");
			intent.putExtra("oldAddress", printerAddress.StringValue);

			startActivityForResult(intent, REQUEST_DISCOVERY);
    	}
    	catch (Exception e)
    	{
    		CrashReporter.logHandledException(e);
    	}
    }
    
    public void onPrinterTest(View v)
    {
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Settings: onPrinterTest");

			CrashReporter.leaveBreadcrumb("Settings: onPrinterTest - " + ((Button)v).getText().toString());

			// Reset counters.
	    	printerDemoCounter = 0;
	    	metermateDemoCounter = 0;
	    	
	    	// Print test page.
	    	Printing.testPage(this);
    	}
    	catch (Exception e)
    	{
    		CrashReporter.logHandledException(e);
    	}
    }
    
    private final OnClickListener onMeterMateClicked = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Settings: onMeterMateClicked");
				
				if (metermateDemoCounter++ == 5)
				{
					// Reset counter.				
					metermateDemoCounter = 0;
	
					// Inform user.
					Toast.makeText(getApplicationContext(), "Demo MeterMate selected", Toast.LENGTH_LONG).show();
	
					// Switch metermate to demo mode.
			   		metermateName.StringValue = "Demo simulator";
			   		metermateName.save();
			   		
			   		metermateAddress.StringValue = "00:00:00:00:00";
					metermateAddress.save();
	
					updateUI();
				}
				
				// Reset other counter.
				printerDemoCounter = 0;
	    	}
	    	catch (Exception e)
	    	{
	    		CrashReporter.logHandledException(e);
	    	}
		}
	};

	public void onLogDataClick(View v)
	{
		try
        {
            // Leave Breadcrumb
            CrashReporter.leaveBreadcrumb("Settings: onLogDataClick");

            CrashReporter.leaveBreadcrumb("Settings: onLogDataClick - " + ((Button)v).getText().toString());

            if (btnLogData.getText() == DATA_NOT_LOGGED)
            {
                logBluetoothData.IntValue = 1;
                logBluetoothData.save();

                btnLogData.setText(DATA_LOGGED);
            }
            else
            {
                logBluetoothData.IntValue = 0;
                logBluetoothData.save();

                btnLogData.setText(DATA_NOT_LOGGED);
            }
        }
        catch (Exception e)
        {
            CrashReporter.logHandledException(e);
        }
	}

	public void onMeterMateChange(View v)
    {
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Settings: onMeterMateChange");
			
	    	CrashReporter.leaveBreadcrumb("Settings: onMeterMateChange - " + ((Button)v).getText().toString());

            // Reset counters.
	    	printerDemoCounter = 0;
	    	metermateDemoCounter = 0;
	    	
	    	// Discover new MeterMate.
			Intent intent = new Intent(getApplicationContext(), Discovery.class);

			intent.putExtra("type", "MeterMate");
			intent.putExtra("oldAddress", metermateAddress.StringValue);

			startActivityForResult(intent, REQUEST_DISCOVERY);
    	}
    	catch (Exception e)
    	{
    		CrashReporter.logHandledException(e);
    	}
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Settings: onActivityResult");
			
			if (requestCode == REQUEST_DISCOVERY)
			{
				if (resultCode == RESULT_OK)
				{
					String type = data.getExtras().getString("type");
					String newName = data.getExtras().getString("newName");
					String newAddress = data.getExtras().getString("newAddress");
	
					if (type.equals("Printer"))
					{
						// Test print won't work unless we save this now.
						printerName.StringValue = newName;
						printerName.save();
						
						printerAddress.StringValue = newAddress;
						printerAddress.save();
					}
	
					if (type.equals("MeterMate"))
					{
						metermateName.StringValue = newName;
						metermateName.save();
						
						metermateAddress.StringValue = newAddress;
						metermateAddress.save();
					}
					
					// Refresh UI.
					updateUI();
				}
			}
    	}
    	catch (Exception e)
    	{
    		CrashReporter.logHandledException(e);
    	}
	}
	
	public void onOKClicked(View v)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Settings: onOKClicked");

            CrashReporter.leaveBreadcrumb("Settings: onOKClicked - " + ((Button)v).getText().toString());

            // If printer has changed, then all brands will need to be resent.
	    	if (printerAddress.StringValue != null)
	    	{
	    		if (!printerAddress.StringValue.equals(printerOldAddressStringValue))
		    	{
					for (dbSetting setting : dbSetting.GetAllBrandLogos())
					{
						setting.IntValue = 0;
						setting.save();
					}
		    	}
	    	}
	    	
	    	// Save Colossus URL.
	    	if (etURL.getVisibility() == View.VISIBLE)
	    	{
	    		colossusURL.StringValue = etURL.getText().toString();
	    		colossusURL.save();
	    	}
	    	
			finish();
    	}
    	catch (Exception e)
    	{
    		CrashReporter.logHandledException(e);
    	}
	}

    public void onCancelClicked(View v)
    {
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Settings: onCancelClicked");

            CrashReporter.leaveBreadcrumb("Settings: onCancelClicked - " + ((Button)v).getText().toString());

            // Undo changes to printer.
	    	printerName.StringValue = printerOldNameStringValue;
	    	printerName.save();
	    	
	   		printerAddress.StringValue = printerOldAddressStringValue;
	   		printerAddress.save();
	
	   		// Undo changes to metermate.
	   		metermateName.StringValue = metermateOldNameStringValue;
	   		metermateName.save();
	   		
	   		metermateAddress.StringValue = metermateOldAddressStringValue;
	    	metermateAddress.save();
	    	
	    	finish();
    	}
    	catch (Exception e)
    	{
    		CrashReporter.logHandledException(e);
    	}
    }
}