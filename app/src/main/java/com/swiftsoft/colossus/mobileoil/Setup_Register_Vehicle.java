package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicle;
import com.swiftsoft.colossus.mobileoil.service.ColossusIntentService;
import com.swiftsoft.colossus.mobileoil.view.MyEditText;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Setup_Register_Vehicle extends MyFlipperView
{
	private Setup setup;
	private LayoutInflater inflater;

	private MyInfoView1Line infoview;
	private TextView tvMessage;
	private MyEditText etVehicleNo;
	private TextView tvVehicleReg;
	private TextView tvError;
	private Button btnNext;
	
	private dbVehicle vehicle = null;
	
	public Setup_Register_Vehicle(Context context)
	{
		super(context);
		init(context);
	}

	public Setup_Register_Vehicle(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup_Register_Vehicle: init");
			
			// Store reference to Startup activity.
			setup = (Setup)context;
	
			// Inflate layout.
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.setup_register_vehicle, this, true);
		
			infoview = (MyInfoView1Line)this.findViewById(R.id.setup_register_vehicle_infoview);
			tvMessage = (TextView)this.findViewById(R.id.setup_register_vehicle_message);
			etVehicleNo = (MyEditText)this.findViewById(R.id.setup_register_vehicle_no);
			tvVehicleReg = (TextView)this.findViewById(R.id.setup_register_vehicle_reg);
			tvError = (TextView)this.findViewById(R.id.setup_register_vehicle_error);
			btnNext = (Button)this.findViewById(R.id.setup_register_vehicle_next);
			
			etVehicleNo.addTextChangedListener(twVehicleNo);
			btnNext.setOnClickListener(onNext);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	@Override
	public boolean resumeView() 
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup_Register_Vehicle: resumeView");
			
			// Resume updating.
			infoview.resume();
			
			// Setup UI.
			infoview.setDefaultTv1("App Setup");
			infoview.setDefaultTv2("Vehicle registration");
			etVehicleNo.requestFocus();
			tvVehicleReg.setText("");
			tvError.setVisibility(View.INVISIBLE);
			btnNext.setEnabled(false);
			
			return true;
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
			return false;
		}
	}
	
	@Override
	public void pauseView()
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup_Register_Vehicle: pauseView");

			// Pause updating.
			infoview.pause();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	OnClickListener onNext = new OnClickListener()
	{		
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Setup_Register_Vehicle: onClick");
				
				// Check if registering vehicle.
				boolean register = etVehicleNo.isEnabled();
				
				// Reset UI.
				tvError.setVisibility(View.INVISIBLE);
				etVehicleNo.setEnabled(false);
				btnNext.setEnabled(false);
				
				if (register)
				{			
					// Register vehicle.
					setup.registerVehicle(vehicle.ColossusID);
				}
				else
				{
					// Check if Setup is now complete.
					if (!setup.isSetupComplete())
					{
						// Setup vehicle line product.
						setup.selectView(Setup.ViewVehicleLine, +1);
					}
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

    TextWatcher twVehicleNo = new TextWatcher()
	{
		@Override
		public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3)
		{
		}
		
		@Override
		public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3)
		{
		}
		
		@Override
		public void afterTextChanged(Editable paramEditable)
		{
			validateVehicle();
		}
	};

	private void validateVehicle()
	{
		try
		{
			CrashReporter.leaveBreadcrumb("Setup_Register_Vehicle : validateVehicle - Starting");

			// Get user input.
			String vehicleNoText = etVehicleNo.getText().toString();

			CrashReporter.leaveBreadcrumb("Setup_Register_Vehicle : validateVehicle - vehicleID : " + vehicleNoText);
	
			// Assume data is invalid.
			vehicle = null;
			btnNext.setEnabled(false);
			tvVehicleReg.setText("");
	
			if (vehicleNoText.length() > 0)
			{
				// Query database.
				vehicle = dbVehicle.FindByNo(Utils.Convert2Int(vehicleNoText));

				if (vehicle != null)
				{
					CrashReporter.leaveBreadcrumb("Setup_Register_Vehicle : validateVehicle - vehicle.Reg : " + vehicle.Reg);

					btnNext.setEnabled(true);
					tvVehicleReg.setText(vehicle.Reg);
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	public void onBroadcast(Intent intent)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup_Register_Vehicle: onBroadcast");
			
			// Extract data.
			Bundle bundle = intent.getExtras();
			int vehicleID = bundle.getInt("VehicleID");

			// Record the vehicle ID
			CrashReporter.leaveBreadcrumb("Setup_Register_Vehicle: onBroadcast - Vehicle Id : " + vehicleID);
			
			if (vehicle.ColossusID == vehicleID)
			{
				if (intent.getAction().equals(ColossusIntentService.BroadcastRegisterVehicleOK))
				{
					CrashReporter.leaveBreadcrumb("Setup_Register_Vehicle: onBroadcast - Action : BroadcastRegisterVehicleOK, vehicle.No : " + vehicle.No);

					dbSetting vehicleRecord = new dbSetting();

					vehicleRecord.Key = "VehicleRegistered";
					vehicleRecord.StringValue = "true";
					vehicleRecord.IntValue = vehicle.No;

					vehicleRecord.save();
		
					// User may now proceed.
					tvMessage.setText("Vehicle now registered");
					etVehicleNo.setEnabled(false);
					btnNext.setEnabled(true);
				}
		
				if (intent.getAction().equals(ColossusIntentService.BroadcastRegisterVehicleNOK))
				{
					CrashReporter.leaveBreadcrumb("Setup_Register_Vehicle: onBroadcast - Action : BroadcastRegisterVehicleNOK");

					// Show error message.
					tvError.setText(bundle.getString("Error"));
					tvError.setVisibility(View.VISIBLE);
		
					// Allow another attempt.
					etVehicleNo.setEnabled(true);
					btnNext.setEnabled(true);
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}	
}
