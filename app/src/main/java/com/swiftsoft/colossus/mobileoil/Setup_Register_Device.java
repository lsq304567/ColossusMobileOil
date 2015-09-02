package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;
import com.swiftsoft.colossus.mobileoil.service.ColossusIntentService;
import com.swiftsoft.colossus.mobileoil.utilities.ControlSaver;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Setup_Register_Device extends MyFlipperView
{
	private Setup setup;
	private LayoutInflater inflater;

	private MyInfoView1Line infoview;
	private TextView tvMessage;
	private CheckBox ckbVehicles;
	private CheckBox ckbDrivers;
	private CheckBox ckbProducts;
	private CheckBox ckbBrands;
	private Button btnNext;
	
	private boolean vehiclesDownloaded;
	private boolean driversDownloaded;
	private boolean productsDownloaded;
	private boolean brandsDownloaded;

	public Setup_Register_Device(Context context)
	{
		super(context);
		init(context);
	}

	public Setup_Register_Device(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public void saveState(Bundle state)
	{
		ControlSaver.save(infoview, "Register.Device.Info", state);
		ControlSaver.save(tvMessage, "Register.Device.Message", state);
		ControlSaver.save(btnNext, "Register.Device.Next", state);
		ControlSaver.save(ckbVehicles, "Register.Device.Vehicles", state);
		ControlSaver.save(ckbDrivers, "Register.Device.Drivers", state);
		ControlSaver.save(ckbProducts, "Register.Device.Products", state);
		ControlSaver.save(ckbBrands, "Register.Device.Brands", state);
    }

	@SuppressWarnings("ResourceType")
    public void restoreState(Bundle state)
	{
        ControlSaver.restore(infoview, "Register.Device.Info", state);
        ControlSaver.restore(tvMessage, "Register.Device.Message", state);
        ControlSaver.restore(btnNext, "Register.Device.Next", state);
        ControlSaver.restore(ckbVehicles, "Register.Device.Vehicles", state);
        ControlSaver.restore(ckbDrivers, "Register.Device.Drivers", state);
        ControlSaver.restore(ckbProducts, "Register.Device.Products", state);
        ControlSaver.restore(ckbBrands, "Register.Device.Brands", state);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup_Register_Device: init");
			
			// Store reference to Startup activity.
			setup = (Setup)context;
	
			// Inflate layout.
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.setup_register_device, this, true);
		
			infoview = (MyInfoView1Line)this.findViewById(R.id.setup_register_device_infoview);
			tvMessage = (TextView)this.findViewById(R.id.setup_register_device_message);
			ckbVehicles = (CheckBox)this.findViewById(R.id.setup_register_device_vehicles);
			ckbDrivers = (CheckBox)this.findViewById(R.id.setup_register_device_drivers);
			ckbProducts = (CheckBox)this.findViewById(R.id.setup_register_device_products);
			ckbBrands = (CheckBox)this.findViewById(R.id.setup_register_device_brands);
			btnNext = (Button)this.findViewById(R.id.setup_register_device_next);
			
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
			CrashReporter.leaveBreadcrumb("Setup_Register_Device: resumeView");
			
			// Resume updating.
			infoview.resume();
			
			// Find Colossus URL.
			String colossusURL = dbSetting.FindByKey("ColossusURL").StringValue;
			
			// Setup UI.
			infoview.setDefaultTv1("App Setup");
			infoview.setDefaultTv2("Device registration");
			tvMessage.setText("Downloading from " + colossusURL);
			ckbVehicles.setVisibility(View.INVISIBLE);
			ckbDrivers.setVisibility(View.INVISIBLE);
			ckbProducts.setVisibility(View.INVISIBLE);
			ckbBrands.setVisibility(View.INVISIBLE);
			btnNext.setEnabled(false);
			
			// Reset flags.
			vehiclesDownloaded = false;
			driversDownloaded = false;
			productsDownloaded = false;
			brandsDownloaded = false;
			
			// Register device with customer's server.
			setup.registerDevice();
			
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
			CrashReporter.leaveBreadcrumb("Setup_Register_Device: pauseView");
			
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
				CrashReporter.leaveBreadcrumb("Setup_Register_Device: onNext");
				
				// Switch to next view.
				setup.selectView(Setup.ViewRegisterVehicle, +1);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	public void onBroadcast(Intent intent)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup_Register_Device: onBroadcast");

			if (intent.getAction().equals(ColossusIntentService.BroadcastNewVehicles))
			{
				vehiclesDownloaded = true;
	
				if (ckbVehicles != null)
					ckbVehicles.setVisibility(View.VISIBLE);
			}
			
			if (intent.getAction().equals(ColossusIntentService.BroadcastNewDrivers))
			{
				driversDownloaded = true;
				
				if (ckbDrivers != null)
					ckbDrivers.setVisibility(View.VISIBLE);
			}
	
			if (intent.getAction().equals(ColossusIntentService.BroadcastNewProducts))
			{
				productsDownloaded = true;
				
				if (ckbProducts != null)
					ckbProducts.setVisibility(View.VISIBLE);
			}
			
			if (intent.getAction().equals(ColossusIntentService.BroadcastNewBrands))
			{
				brandsDownloaded = true;
				
				if (ckbBrands != null)
					ckbBrands.setVisibility(View.VISIBLE);
			}
			
			// If all ticked proceed to login.
			if (vehiclesDownloaded && 
				driversDownloaded && 
				productsDownloaded &&
				brandsDownloaded)
			{
				dbSetting setting1 = new dbSetting();
				setting1.Key = "DeviceRegistered";
				setting1.StringValue = "true";
				setting1.save();
			
				// User may now proceed.
				tvMessage.setText("Device now registered");
				btnNext.setEnabled(true);
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
}
