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
		state.putString("Register.Device.Info.TV1", infoview.getDefaultTv1());
        state.putString("Register.Device.Info.TV2", infoview.getDefaultTv2());

        state.putString("Register.Device.Message.Text", tvMessage.getText().toString());
        state.putBoolean("Register.Device.Message.Enabled", tvMessage.isEnabled());
        state.putInt("Register.Device.Message.Visibility", tvMessage.getVisibility());

        state.putString("Register.Device.Next.Text", btnNext.getText().toString());
        state.putBoolean("Register.Device.Next.Enabled", btnNext.isEnabled());
        state.putInt("Register.Device.Next.Visibility", btnNext.getVisibility());

        state.putString("Register.Device.Vehicles.Text", ckbVehicles.getText().toString());
        state.getBoolean("Register.Device.Vehicles.Enabled", ckbVehicles.isEnabled());
        state.getInt("Register.Device.Vehicles.Visibility", ckbVehicles.getVisibility());
        state.getBoolean("Register.Device.Vehicles.Checked", ckbVehicles.isChecked());

        state.putString("Register.Device.Drivers.Text", ckbDrivers.getText().toString());
        state.getBoolean("Register.Device.Drivers.Enabled", ckbDrivers.isEnabled());
        state.getInt("Register.Device.Drivers.Visibility", ckbDrivers.getVisibility());
        state.getBoolean("Register.Device.Drivers.Checked", ckbDrivers.isChecked());

        state.putString("Register.Device.Products.Text", ckbProducts.getText().toString());
        state.getBoolean("Register.Device.Products.Enabled", ckbProducts.isEnabled());
        state.getInt("Register.Device.Products.Visibility", ckbProducts.getVisibility());
        state.getBoolean("Register.Device.Products.Checked", ckbProducts.isChecked());

        state.putString("Register.Device.Brands.Text", ckbBrands.getText().toString());
        state.getBoolean("Register.Device.Brands.Enabled", ckbBrands.isEnabled());
        state.getInt("Register.Device.Brands.Visibility", ckbBrands.getVisibility());
        state.getBoolean("Register.Device.Brands.Checked", ckbBrands.isChecked());
    }

	@SuppressWarnings("ResourceType")
    public void restoreState(Bundle state)
	{
        infoview.setDefaultTv1(state.getString("Register.Device.Info.TV1"));
        infoview.setDefaultTv2(state.getString("Register.Device.Info.TV2"));

        tvMessage.setText(state.getString("Register.Device.Message.Text"));
        tvMessage.setEnabled(state.getBoolean("Register.Device.Message.Enabled"));
        tvMessage.setVisibility(state.getInt("Register.Device.Message.Visibility"));

        btnNext.setText(state.getString("Register.Device.Next.Text"));
        btnNext.setEnabled(state.getBoolean("Register.Device.Next.Enabled"));
        btnNext.setVisibility(state.getInt("Register.Device.Next.Visibility"));

        ckbVehicles.setText(state.getString("Register.Device.Vehicles.Text"));
        ckbVehicles.setEnabled(state.getBoolean("Register.Device.Vehicles.Enabled"));
        ckbVehicles.setVisibility(state.getInt("Register.Device.Vehicles.Visibility"));
        ckbVehicles.setChecked(state.getBoolean("Register.Device.Vehicles.Checked"));

        ckbDrivers.setText(state.getString("Register.Device.Drivers.Text"));
        ckbDrivers.setEnabled(state.getBoolean("Register.Device.Drivers.Enabled"));
        ckbDrivers.setVisibility(state.getInt("Register.Device.Drivers.Visibility"));
        ckbDrivers.setChecked(state.getBoolean("Register.Device.Drivers.Checked"));

        ckbProducts.setText(state.getString("Register.Device.Products.Text"));
        ckbProducts.setEnabled(state.getBoolean("Register.Device.Products.Enabled"));
        ckbProducts.setVisibility(state.getInt("Register.Device.Products.Visibility"));
        ckbProducts.setChecked(state.getBoolean("Register.Device.Products.Checked"));

        ckbBrands.setText(state.getString("Register.Device.Brands.Text"));
        ckbBrands.setEnabled(state.getBoolean("Register.Device.Brands.Enabled"));
        ckbBrands.setVisibility(state.getInt("Register.Device.Brands.Visibility"));
        ckbBrands.setChecked(state.getBoolean("Register.Device.Brands.Checked"));
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
