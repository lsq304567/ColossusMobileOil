// Version history
// ===============
//
// 1.2  :  1/05/12 - Signature storing and printing
// 1.21 :  7/05/12 - GPS Tracking
// 1.31 :  4/09/12 - MeterMate
// 1.32 : 15/09/12 - Licensing link to WebService
// 1.33 : 18/09/12 - New services for GPS, Timer & Colossus link
// 1.34 : 21/09/12 - GPS improvements
// 1.35 : 24/09/12 - Demo version for Galway!
// 1.40 : 18/10/12 - Colossus link improvements
// 1.50 : 07/11/12 - Replaced Depot activity with Trips activity
//                 - Moved stock actions to 'driving' section
// 1.55 : 21/01/13 - Improved delivery process and MeterMate interface
// 1.60 : 19/02/13 - Support for brands i.e. logo downloading to Zebra printers
// 1.70 : 04/03/13 - Demo version for NFO - Cancelled due to Matthew breaking arm
// 1.75 : 07/03/13 - Demo version for NFO - new StockByCompartment or not!
// 1.77 : 12/03/13 - Demo version for NFO - tried to fix stock by product
// 1.82 : 14/05/13 - Demo version for NFO - complete redesign of delivery process
// 
// Renumbered for prelaunch
// ========================
//
// 0.99a   : 24/05/13 - Demo version for NFO
// 0.99b   : 24/05/13 - Now sends back delivery details for each order
//                    - Added Delivered list
// 0.99c   : 31/05/13 - Demo MeterMate simulator - allows operation without a MeterMate.
// 0.99c.1 : 01/06/13 - Demo Printer mode - just skips printouts.
//                    - Passes back more order details, including signature images.
// 0.99c.2 : 04/06/13 - HockeyApp.net added
// 0.99d   : 14/06/13 - New RegisterDevice & RegisterVehicle messages.
//                    - Replaced Startup activities with Setup activities.
//                    - Support for live stock updates
// 0.99e   : 01/07/13 - Crash reporting now using Crittercism!
// 0.99f   : 19/07/13 - Demo version for NFO
//         : 23/07/13 - Fixed signature not printing correctly, when 'Try again' clicked.
//         : 24/07/13 - Logos are now marked as downloaded, only if the ticket prints successfully.
//                    - Firmware bug with RW420 RxD printers - if LOGOx.PCX is missing, signatures are not printed.
// 0.99g   : 24/07/13 - Improved notification on ColossusIntentService
// 0.99h   : 26/07/13 - Started TimerService in Setup, which will auto-retry message exchange.
//                    - Refreshed order list on broadcast
// 0.99i   : 29/07/13 - Fixed issue printing trip report if > 999 of cash, cheque or voucher received.
// 0.99j   : 12/09/13 - Now displays Colossus URL in setting screen.
//                    - Database changed to support product MobileOil field.
//                    - Amended Setup_Vehicle_Line to only allow Metered products.
//				      - Amended Stock Load/Return to only allow Metered & Non-Metered products.
// 0.99k   : 13/09/13 - Fixed re-print bug (at end of order), was throwing an exception, but not causing any issues.
//                    - Fixed device licensing exception, was throwing an exception, but not causing any issues.
//                    - Support for non-metered products and non-deliverable products.
// 0.99l   : 14/09/13 - Now updates TripOrder.Discount
// 0.99m   : 16/09/13 - Fixed bug in MeterMate demo simulator if quantity ordered was 0, it would not start.
//                    - OrderAdd now validates product exists on device.
//                    - COD before delivery was not calculating discount correctly.
//
//         :  7/10/13 - **** FIRST LIVE TRIP (LCC) ****
//
// 0.99n   : 14/10/13 - Added Serial No to settings activity.
//                    - Price now prints up to 4 dec places.
//                    - Customer code now printed on ticket.
// 0.99o   : 01/11/13 - Changed Transport document to not include line stock, in stock onboard figures.
//                    - Price now prints up to 4 dec places - revised method; now using line.Ratio
//                    - Finish trip now deleted any vehicle stock which has a zero quantity.
//                    - Now shows 'Loading notes' on start of trip page.
//			 11/11/13 - Now prints customer code on trip report.
//                    - Added 'Cash report' to 'Trip report'
//           13/11/13 - Credit cards now disables the payment button & shows as 'paid office'
//                    - Ticket now shows due date and discount, if unpaid.
// 0.99p     14/11/13 - Fixed bug where DueDate was not set for OrderAdd messages.
//                      Customer code and phone nos added to order summary screen.
//                      Delivery address shown at top of order summary screen, if different from customer address.
//			 23/11/13 - Demo data generator (Driver -159 Pin -951)
// 0.99q     30/06/14 - The following activities now support the rotating InfoView - Setup, Logon, Checklist, Trips, Trip
//                    - InfoView mobile data dialog
//                    - Upgraded to Crittercism 4.5.1
//              09/14 - Testing with NFO
// 0.99r	 15/09/14 - Removed MeterMate firmware upgrade code
// 0.99s     18/10/14 - Replaced Crittercism with Crashlytics
//                    - Added 'Hose' or 'Bulk' delivery methods
//                    - Added 'Line changed?' after 'Hose' deliveries
// 0.99t     07/11/14 - Added option to correct line product before delivery
//                    - Added option to amend line price, if delivered qty +/- 50 from ordered qty.
//                    - Changed Trip Report to show 'line changed during delivery' & 'line product corrected'
//                    - Changed Ticket to not price zero prices and zero totals
// 0.99t2    10/11/14 - minor bug fix
// 0.99t3    20/11/14 - fixed exception when correcting line product prior to delivery
// 0.99t4    22/11/14 - added more breadcrumbs for debugging why line goes to null
//

package com.swiftsoft.colossus.mobileoil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.widget.ViewFlipper;

import com.crashlytics.android.Crashlytics;
import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicle;
import com.swiftsoft.colossus.mobileoil.service.ColossusIntentService;
import com.swiftsoft.colossus.mobileoil.service.GpsService;
import com.swiftsoft.colossus.mobileoil.service.LicensingIntentService;
import com.swiftsoft.colossus.mobileoil.service.TimerService;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;

import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

public class Setup extends Activity
{
	private static final String ViewLicenseDevice   = "Setup.LicenseDevice";
	public static final String ViewRegisterDevice  = "Setup.RegisterDevice";
	public static final String ViewRegisterVehicle = "Setup.RegisterVehicle";
	public static final String ViewVehicleLine     = "Setup.VehicleLine";

	private Setup_License_Device setupLicenseDevice;
    private Setup_Register_Device setupRegisterDevice;
    private Setup_Register_Vehicle setupRegisterVehicle;
    private Setup_Vehicle_Line setupVehicleLine;

	private ViewFlipper vf;
	private String currentViewName;
	private MyFlipperView currentView;
	private RegisterDeviceReceiver registerDeviceReceiver;
	private RegisterVehicleReceiver registerVehicleReceiver;

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
        setupLicenseDevice.saveState(outState);
        setupRegisterDevice.saveState(outState);
        setupRegisterVehicle.saveState(outState);
        setupVehicleLine.saveState(outState);

		// Call the super class
		super.onSaveInstanceState(outState);
	}

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        setupLicenseDevice.restoreState(savedInstanceState);
        setupRegisterDevice.restoreState(savedInstanceState);
        setupRegisterVehicle.restoreState(savedInstanceState);
        setupVehicleLine.restoreState(savedInstanceState);
    }

    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());

		try
		{
            // NOTE: This is the starting point of the App.

			// Start CrashReporter.
			CrashReporter.onStart(getApplicationContext());
	
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup: onCreate");

			// Start services.
			startService(new Intent(this, GpsService.class));
			startService(new Intent(this, TimerService.class));

			// Check if setup is already complete.
			if (!isSetupComplete())
            {
                // Setup view.
                setContentView(R.layout.setup);

                // Create views.
                setupLicenseDevice = new Setup_License_Device(this);
                setupRegisterDevice = new Setup_Register_Device(this);
                setupRegisterVehicle = new Setup_Register_Vehicle(this);
                setupVehicleLine = new Setup_Vehicle_Line(this);

                vf = (ViewFlipper) findViewById(R.id.setup_flipper);

                vf.removeAllViews();

                vf.addView(setupLicenseDevice);                    // Index 0
                vf.addView(setupRegisterDevice);                // Index 1
                vf.addView(setupRegisterVehicle);                // Index 2
                vf.addView(setupVehicleLine);                   // Index 3

                // Select the initial view.
                selectInitView();
            }
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
			CrashReporter.leaveBreadcrumb("Setup: onResume");

			// Update Active.activity
			Active.activity = this;

            // Create IntentFilter for broadcasts we are interested in.
            IntentFilter registerDeviceFilter = new IntentFilter();
            registerDeviceFilter.addAction(ColossusIntentService.BroadcastNewVehicles);
            registerDeviceFilter.addAction(ColossusIntentService.BroadcastNewDrivers);
            registerDeviceFilter.addAction(ColossusIntentService.BroadcastNewProducts);
            registerDeviceFilter.addAction(ColossusIntentService.BroadcastNewBrands);

            IntentFilter registerVehicleFilter = new IntentFilter();
            registerVehicleFilter.addAction(ColossusIntentService.BroadcastRegisterVehicleOK);
            registerVehicleFilter.addAction(ColossusIntentService.BroadcastRegisterVehicleNOK);

            // Create the BroadcastReceiver.
            registerDeviceReceiver = new RegisterDeviceReceiver();
            registerVehicleReceiver = new RegisterVehicleReceiver();

            // Register BroadcastReceiver.
            registerReceiver(registerDeviceReceiver, registerDeviceFilter);
            registerReceiver(registerVehicleReceiver, registerVehicleFilter);

            if (!isSetupComplete())
            {
                selectInitView();
            }
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();

		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup: onPause");
	
			// Unregister BroadcastReceivers.
			unregisterReceiver(registerDeviceReceiver);
			unregisterReceiver(registerVehicleReceiver);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	// Change the current view.
    public void selectView(String newName, int direction)
    {
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup: selectView - starting. newName = " + newName + " direction = " + direction);
	
	    	// Setup animation.
			if (direction < 0)
			{
	    		vf.setInAnimation(AnimationHelper.inFromLeftAnimation());
	    		vf.setOutAnimation(AnimationHelper.outToRightAnimation());
			}
			
			if (direction > 0)
			{
	    		vf.setInAnimation(AnimationHelper.inFromRightAnimation());
	    		vf.setOutAnimation(AnimationHelper.outToLeftAnimation());
			}
				
			MyFlipperView newView = null;

			int newIdx = -1;
			
			// Switch to specified view.
			if (newName.equals(ViewLicenseDevice))
			{
				newView = setupLicenseDevice;
				newIdx = 0;
			}
			
			if (newName.equals(ViewRegisterDevice))
			{
				newView = setupRegisterDevice;
				newIdx = 1;
			}
	
			if (newName.equals(ViewRegisterVehicle))
			{
				newView = setupRegisterVehicle;
				newIdx = 2;
			}
			
			if (newName.equals(ViewVehicleLine))
			{
				newView = setupVehicleLine;
				newIdx = 3;
			}
			
			// Switch to new view.
			if (newView != null)
			{
				// Pause currentView.
				if (currentView != null)
				{
					currentView.pauseView();
				}

				// Initialise the view.
				if (newView.resumeView())
				{
					if (direction > 0)
					{
						newView.setPreviousView(currentViewName);
					}
					
					newView.updateUI();
					vf.setDisplayedChild(newIdx);
					
					currentView = newView;
					currentViewName = newName;
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }

	// ------------------------ Setup flags ------------------

    // Check if setup is now complete.    
    public boolean isSetupComplete()
    {
    	try
    	{
			if (isDeviceLicensed() && isDeviceRegistered() && isVehicleRegistered() && isVehicleLineSetup())
			{		
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Setup: isSetupComplete - starting Logon activity");
				
				// Start Logon activity.
				Intent i = new Intent(this, Logon.class);
				startActivity(i);
				finish();
				
				return true;
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
		
		return false;		
    }

    // Check if device is now licensed.
    public boolean isDeviceLicensed()
    {
		dbSetting setting = dbSetting.FindByKey("DeviceLicensed");

		return setting != null;
    }

    // Check if device is now registered.
	private boolean isDeviceRegistered()
    {
		dbSetting setting = dbSetting.FindByKey("DeviceRegistered");

		return setting != null;
    }
    
    // Check if vehicle is now registered.
	private boolean isVehicleRegistered()
    {
		dbSetting setting = dbSetting.FindByKey("VehicleRegistered");

		return setting != null;
    }

    // Check if vehicle has a line product, if applicable.
	private boolean isVehicleLineSetup()
    {
		CrashReporter.leaveBreadcrumb("Setup: isVehicleLineSetup");

		dbSetting setting = dbSetting.FindByKey("VehicleRegistered");

    	if (setting != null)
    	{
            CrashReporter.leaveBreadcrumb("Setup: isVehicleLineSetup - Vehicle is registered");

    		dbVehicle vehicle = dbVehicle.FindByNo(setting.IntValue);

    		if (vehicle != null)
    		{
                CrashReporter.leaveBreadcrumb("Setup: isVehicleLineSetup - Vehicle found");

    			if (!vehicle.getHasHosereel())
				{
                    CrashReporter.leaveBreadcrumb("Setup: isVehicleLineSetup - Vehicle does not have hosereel");

					return true;
				}
    			
    			if (vehicle.getHosereelProduct() != null)
				{
                    CrashReporter.leaveBreadcrumb("Setup: isVehicleLineSetup - No hosereel product");

					return true;
				}
    		}
    	}
    	
    	return false;
    }
    
    // Select correct initial view.
    private void selectInitView()
    {
    	try
    	{
            CrashReporter.leaveBreadcrumb("Setup: selectInitView");

            if (!isDeviceLicensed())
            {
                selectView(Setup.ViewLicenseDevice, 0);
            }
            else if (!isDeviceRegistered())
            {
                selectView(Setup.ViewRegisterDevice, 0);
            }
            else if (!isVehicleRegistered())
            {
                selectView(Setup.ViewRegisterVehicle, 0);
            }
            else if (!isVehicleLineSetup())
            {
                selectView(Setup.ViewVehicleLine, 0);
            }
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
    
    // ----------------- Device licensing -------------------
    
    public void relicenseDevice()
    {
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup: relicenseDevice");
			
			// Call LicensingIntentService.
			Intent i = new Intent(this, LicensingIntentService.class);

			i.putExtra("Type", "Relicense");
			i.putExtra("SerialNo", Utils.getSerialNo(this));
			i.putExtra("Messenger", new Messenger(handler));

			startService(i);
    	}
		catch (Exception e) 
		{
			CrashReporter.logHandledException(e);
		}			
    }
	
	public void licenseDevice(int pin)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup: licenseDevice");
			
			// Call LicensingIntentService.
			Intent i = new Intent(this, LicensingIntentService.class);

			i.putExtra("Type", "License");
			i.putExtra("SerialNo", Utils.getSerialNo(this));
			i.putExtra("CompanyPIN", pin);
			i.putExtra("Messenger", new Messenger(handler));

			startService(i);
		}
		catch (Exception e) 
		{
			CrashReporter.logHandledException(e);
		}			
	}

	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler()
	{    
		@Override    
		public void handleMessage(Message msg) 
		{
			try
			{
				setupLicenseDevice.onLicenseMessage(msg);
			}
			catch (Exception e) 
			{
				CrashReporter.logHandledException(e);
			}			
		}  
	};
	

    // ----------------- Device registration -----------------

    public void registerDevice()
    {
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup: registerDevice");

			// Build 'RegisterDevice' content.
			JSONObject json = new JSONObject();
			json.put("SerialNo", Utils.getSerialNo(this));
			String content = json.toString();
	
			// Call ColossusIntentService.
			Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);

			i.putExtra("Type", "RegisterDevice");
			i.putExtra("Content", content);

			startService(i);
		}
		catch (Exception e) 
		{
			CrashReporter.logHandledException(e);
		}			
    }

	class RegisterDeviceReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			try
			{
				setupRegisterDevice.onBroadcast(intent);
			}
			catch (Exception e) 
			{
				CrashReporter.logHandledException(e);
			}			
		}
	}

    // ----------------- Vehicle registration -----------------
	
    public void registerVehicle(int vehicleID)
    {
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup: registerVehicle");
			
			// Build 'RegisterVehicle' content.
			JSONObject json = new JSONObject();
			json.put("VehicleID", vehicleID);
			String content = json.toString();
	
			// Call ColossusIntentService.
			Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);

			i.putExtra("Type", "RegisterVehicle");
			i.putExtra("Content", content);

			startService(i);
		}
		catch (Exception e) 
		{
			CrashReporter.logHandledException(e);
		}			
    }

	class RegisterVehicleReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			try
			{
				setupRegisterVehicle.onBroadcast(intent);
			}
			catch (Exception e) 
			{
				CrashReporter.logHandledException(e);
			}			
		}
	}
	
    // ------------------ Misc -----------------------
    
	// Update stock on server.
	public void sendVehicleStock(dbVehicle vehicle)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup: sendVehicleStock");

			// Call ColossusIntentService.
			Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);

			i.putExtra("Type", "Stock");
			i.putExtra("Content", vehicle.buildStock());

			startService(i);
		}
		catch (Exception e) 
		{
			CrashReporter.logHandledException(e);
		}			
	}
}