package com.swiftsoft.colossus.mobileoil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.database.model.dbDriver;
import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicle;
import com.swiftsoft.colossus.mobileoil.service.ColossusIntentService;
import com.swiftsoft.colossus.mobileoil.utilities.ControlSaver;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

import org.json.JSONObject;

public class Logon extends Activity
{
	MyInfoView1Line infoview;
	EditText vehicleNo;
	TextView vehicleDesc;
	EditText driverNo;
	TextView driverDesc;
	EditText driverPIN;
	TextView driverPINMessage;
	Button logon;

	boolean isVehicleValid;
	boolean isDriverValid;
	boolean isDriverPINValid;

	int orientation;

    boolean orientationChanged = false;

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		ControlSaver.save(infoview, "Logon.Info", outState);
		ControlSaver.save(vehicleNo, "Logon.VehicleNo", outState);
		ControlSaver.save(vehicleDesc, "Logon.VehicleDescription", outState);
		ControlSaver.save(driverNo, "Logon.DriverNo", outState);
		ControlSaver.save(driverDesc, "Logon.DriverDescription", outState);
		ControlSaver.save(driverPIN, "Logon.DriverPin", outState);
		ControlSaver.save(driverPINMessage, "Logon.DriverPin.Message", outState);
		ControlSaver.save(logon, "Logon.Logon", outState);

        // Save the  orientation
        outState.putInt("Logon.Orientation", orientation);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);

		ControlSaver.restore(infoview, "Logon.Info", savedInstanceState);
		ControlSaver.restore(vehicleNo, "Logon.VehicleNo", savedInstanceState);
		ControlSaver.restore(vehicleDesc, "Logon.VehicleDescription", savedInstanceState);
		ControlSaver.restore(driverNo, "Logon.DriverNo", savedInstanceState);
		ControlSaver.restore(driverDesc, "Logon.DriverDescription", savedInstanceState);
		ControlSaver.restore(driverPIN, "Logon.DriverPin", savedInstanceState);
		ControlSaver.restore(driverPINMessage, "Logon.DriverPin.Message", savedInstanceState);
		ControlSaver.restore(logon, "Logon.Logon", savedInstanceState);

        if (orientation != savedInstanceState.getInt("Logon.Orientation"))
        {
            orientationChanged = true;
        }
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Logon: onCreate");

            orientation = getResources().getConfiguration().orientation;

			// Setup view.
			setContentView(R.layout.logon);

			// Find main controls.
			infoview = (MyInfoView1Line) findViewById(R.id.logon1_infoview);
			vehicleNo = (EditText) findViewById(R.id.logon1_vehicle_no);
			vehicleDesc = (TextView) findViewById(R.id.logon1_vehicle_desc);
			driverNo = (EditText) findViewById(R.id.logon1_driver_no);
			driverDesc = (TextView) findViewById(R.id.logon1_driver_desc);
			driverPIN = (EditText) findViewById(R.id.logon1_driver_pin);
			driverPINMessage = (TextView) findViewById(R.id.logon1_driver_pin_message);
			logon = (Button) findViewById(R.id.logon1_button);
		
			// Initialise infoview.
			infoview.setDefaultTv1(getResources().getString(R.string.app_name));
			infoview.setDefaultTv2(getResources().getString(R.string.version));

			// Add validation.
			vehicleNo.addTextChangedListener(twVehicle);
			driverNo.addTextChangedListener(twDriver);
			driverPIN.addTextChangedListener(twDriver);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	// Prevent the Back button from exiting the application.
	@Override
	public void onBackPressed()
	{
		return;
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Logon: onResume");
	
			// Update activity.
			Active.activity = this;
			
			// Resume updating.
			infoview.resume();
			
			init();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Logon: onPause");
			
			// Pause updating.
			infoview.pause();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private void init()
	{
		// Update active vehicle & driver.
		Active.vehicle = null;
		Active.driver = null;
		
		// Find vehicle registation.
		dbSetting registered = dbSetting.FindByKey("VehicleRegistered");
		
		// Initialise UI.
		vehicleNo.setText("" + registered.IntValue);
		vehicleNo.setEnabled(false);
		vehicleDesc.setText("");

        if (!orientationChanged)
        {
            driverNo.setText("");
            driverNo.requestFocus();
            driverDesc.setText("");
            driverPIN.setText("");
            driverPINMessage.setText("");
        }

        orientationChanged = false;

		logon.setEnabled(false);

		// Update UI.
		validateVehicle();
	}
	
	// React to changes to vehicle number.
	TextWatcher twVehicle = new TextWatcher()
	{
		@Override
		public void afterTextChanged(Editable s)
		{
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			validateVehicle();
		}
	};

	private void validateVehicle()
	{
		String vehicleNoText;
		
		try
		{
			// Get user input.
			vehicleNoText = vehicleNo.getText().toString();
	
			// Assume data is invalid.
			isVehicleValid = false;
			vehicleDesc.setText("");
	
			if (vehicleNoText.length() > 0)
			{
				// Query database.
				dbVehicle vehicle = dbVehicle.FindByNo(Utils.Convert2Int(vehicleNoText));

				if (vehicle != null)
				{
					isVehicleValid = true;
					vehicleDesc.setText(vehicle.Reg);
				}
			}
	
			if (vehicleNoText.length() > 0 && !isVehicleValid)
			{
				vehicleDesc.setText("Vehicle no is invalid");
			}
	
			updateLogonState();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	TextWatcher twDriver = new TextWatcher()
	{
		@Override
		public void afterTextChanged(Editable s)
		{
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			validateDriver();
		}
	};

	private void validateDriver()
	{
		String driverNoText;
		String driverPINText;

		try
		{
			// Get user input.
			driverNoText = driverNo.getText().toString();
			driverPINText = driverPIN.getText().toString();

			// Assume data is invalid.
			isDriverValid = false;
			isDriverPINValid = false;
			driverDesc.setText("");
			driverPINMessage.setText("");

			if (driverNoText.length() > 0)
			{
				// Query database.
				dbDriver driver = dbDriver.FindByNo(Utils.Convert2Int(driverNoText));

				if (driver != null)
				{
					isDriverValid = true;
					driverDesc.setText(driver.Name);

					// Check PIN.
					if (driver.PIN == Utils.Convert2Int(driverPINText))
					{
						isDriverPINValid = true;
						driverPINMessage.setText("Driver PIN correct");
					}
				}
			}

			if (driverNoText.length() > 0 && !isDriverValid)
			{
				driverDesc.setText("Driver no is invalid");
			}

			if (isDriverValid)
			{
				if (driverPINText.length() > 0 && !isDriverPINValid)
				{
					driverPINMessage.setText("Driver PIN incorrect");
				}
			}

			// Secret code to generate demo data.
			if (Utils.Convert2Int(driverNoText) == -159 && Utils.Convert2Int(driverPINText) == -951)
			{
				isVehicleValid = true;
				isDriverValid = true;
				isDriverPINValid = true;
			}
			
			updateLogonState();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	private void updateLogonState()
	{
		logon.setEnabled(isVehicleValid && isDriverValid && isDriverPINValid ? true : false);
	}

	public void onLogonClicked(View button)
	{
		String vehicleNoText;
		String driverNoText;
		String driverPINText;

		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Logon: onLogonClicked");

			// Get user input.
			vehicleNoText = vehicleNo.getText().toString();
			driverNoText = driverNo.getText().toString();
			driverPINText = driverPIN.getText().toString();

			// Secret code to generate demo data.
			if (Utils.Convert2Int(driverNoText) == -159 && Utils.Convert2Int(driverPINText) == -951)
			{
				DemoData.Create();
				init();
				return;
			}

			// Find vehicle and driver.
			Active.vehicle = dbVehicle.FindByNo(Utils.Convert2Int(vehicleNoText));
			Active.driver = dbDriver.FindByNo(Utils.Convert2Int(driverNoText));
	
			// Create content.
			JSONObject json = new JSONObject();
			json.put("VehicleID", Active.vehicle.ColossusID);
			json.put("DriverID", Active.driver.ColossusID);
	
			// Call ColossusIntentService.
			Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);
			i.putExtra("Type", "Shift_Start");
			i.putExtra("Content", json.toString());
			startService(i);
			
			// Start the Checklist activity.
			Intent intent = new Intent(this, Checklist.class);
			startActivity(intent);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
}