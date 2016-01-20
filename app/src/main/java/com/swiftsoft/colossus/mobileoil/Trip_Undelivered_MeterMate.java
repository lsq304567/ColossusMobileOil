package com.swiftsoft.colossus.mobileoil;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.bluetooth.MeterMate;
import com.swiftsoft.colossus.mobileoil.database.DbUtils;
import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Trip_Undelivered_MeterMate extends MyFlipperView
{
	private Trip trip;

    private MyInfoView1Line infoview;
	private TextView tvStatus;
	private TextView tvReadingTicket;
	private TextView tvDeliveryMode;
	private TextView tvProductFlowing;
	private TextView tvProduct;
	private TextView tvPresetLitres;
	private TextView tvDeliveredLitres;
	private TextView tvTemperature;
	private LinearLayout llDemoMode;
	private Button btnStart;
	private Button btnStop;
	private Button btnBack;
	private Button btnNext;

	public Trip_MeterMate_Callbacks callbacks;
	private String previousViewName;
	private int presetLitres;
	
	public Trip_Undelivered_MeterMate(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Undelivered_MeterMate(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_MeterMate: init");
			
			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_undelivered_metermate, this, true);
			
			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_undelivered_metermate_infoview);
	    	tvStatus = (TextView)this.findViewById(R.id.trip_undelivered_metermate_comms_status);
	    	tvReadingTicket = (TextView)this.findViewById(R.id.trip_undelivered_metermate_reading_ticket);
	    	tvDeliveryMode = (TextView)this.findViewById(R.id.trip_undelivered_metermate_delivery_mode);
	    	tvProductFlowing = (TextView)this.findViewById(R.id.trip_undelivered_metermate_product_flowing);
	    	tvProduct = (TextView)this.findViewById(R.id.trip_undelivered_metermate_product);
	    	tvPresetLitres = (TextView)this.findViewById(R.id.trip_undelivered_metermate_preset_litres);
	    	tvDeliveredLitres = (TextView)this.findViewById(R.id.trip_undelivered_metermate_delivered_litres);
	    	tvTemperature = (TextView)this.findViewById(R.id.trip_undelivered_metermate_temperature);
	
	    	llDemoMode = (LinearLayout)this.findViewById(R.id.trip_undelivered_metermate_demo_mode);
			llDemoMode.setVisibility(View.GONE);
	    	btnStart = (Button)this.findViewById(R.id.trip_undelivered_metermate_start);
	    	btnStart.setOnClickListener(onStart);
	    	btnStop = (Button)this.findViewById(R.id.trip_undelivered_metermate_stop);
	    	btnStop.setOnClickListener(onStop);
	    	
			btnBack = (Button)this.findViewById(R.id.trip_undelivered_metermate_back);
			btnBack.setOnClickListener(onBack);
			btnNext = (Button)this.findViewById(R.id.trip_undelivered_metermate_next);
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
			// Resume updating.
			infoview.resume();
			
	    	// Find MeterMate Bluetooth address.
	    	dbSetting metermate = dbSetting.FindByKey("MeterMateAddress");
	
	    	if (metermate == null || metermate.StringValue == null)
	    	{
				// Show error.
				AlertDialog.Builder builder = new AlertDialog.Builder(trip);

				builder.setTitle("MeterMate not configured");
				builder.setMessage("You must select your MeterMate in the Settings section before proceeding.");
				builder.setPositiveButton("OK", null);
				builder.show();
				
				return false;
	    	}
	
	    	// Check if in demo mode.
	    	if (metermate.StringValue.equals("00:00:00:00:00"))
			{
	    		// Show Demo simulator UI.
	    		llDemoMode.setVisibility(View.VISIBLE);
	    		
	    		// Update demo buttons.
	    		btnStart.setEnabled(false);
	    		btnStop.setEnabled(false);
			}
	    	
			// Enable connection to MeterMate.
			if (!MeterMate.startup(appHandler, getContext(), metermate.StringValue))
			{
				// Show error.
				AlertDialog.Builder builder = new AlertDialog.Builder(trip);

				builder.setTitle("MeterMate in use");
				builder.setMessage("Please try again.");
				builder.setPositiveButton("OK", null);
				builder.show();
				
				return false;
			}
	    	
			// Hide 'Reading Ticket' text.
			tvReadingTicket.setVisibility(View.INVISIBLE);
			
			// Setup button states.
			btnBack.setEnabled(true);
			btnNext.setEnabled(false);
			
			// Get litres to preset.
			presetLitres = callbacks.getLitres();
			
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
			// Pause updating.
			infoview.pause();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	@Override
	public void setPreviousView(String name) 
	{
		// Store previous view.
		previousViewName = name;
	}
	
	@Override
	public void updateUI() 
	{
		try
		{
			// Update the UI.
			infoview.setDefaultTv1("MeterMate");

			// Line.
			infoview.setDefaultTv2(DbUtils.getInfoviewLineProduct(Active.vehicle.getHosereelProduct()));

			// Product.
			tvProduct.setText(callbacks.getProduct().Desc);
			
			// Update UI.
			updateCommsStatus();
			updatePumpingStatus();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private void updateCommsStatus()
	{
		try
		{
            switch (MeterMate.getCommsStatus())
            {
                case MeterMate.COMMS_CONNECTED:

                    tvStatus.setText("Connected");

                    // Update demo buttons.
                    btnStart.setEnabled(true);
                    btnStop.setEnabled(false);

                    // Now preset the meter.
                    if (presetLitres != 0)
                    {
                        MeterMate.setPreset(presetLitres);

                        // Reset to zero again.
                        presetLitres = 0;
                    }

                    break;

                case MeterMate.COMMS_CONNECTING:

                    tvStatus.setText("Connecting");

                    break;

                case MeterMate.COMMS_DISCONNECTED:

                    tvStatus.setText("Disconnected");

                    // Update demo buttons.
                    btnStart.setEnabled(false);
                    btnStop.setEnabled(false);

                    break;
            }
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	private void updatePumpingStatus()
	{
		try
		{
			tvDeliveryMode.setText(MeterMate.getInDeliveryMode());
			tvProductFlowing.setText(MeterMate.getInPumpingMode());
			
			if (MeterMate.getInPumpingMode().equals("Yes"))
			{
				// Disable Back button, as product is now flowing.
				btnBack.setEnabled(false);
				
				// Disable Start button too.
				btnStart.setEnabled(false);
				btnStop.setEnabled(true);
			}
			
			if (MeterMate.getInDeliveryMode().equals("No") &&
					MeterMate.getInPumpingMode().equals("No") &&
					!btnBack.isEnabled() &&
					!btnNext.isEnabled())
			{
				// If not in delivery mode, and no product flowing, then we could be finished.
				// If Back is disabled, then product was flowing.
				// If Next is disabled, then we have not executed this section before.
	
				if (tvReadingTicket.getVisibility() == View.INVISIBLE)
				{
					// Show 'Reading Ticket' text.
					tvReadingTicket.setVisibility(View.VISIBLE);
					
					// Read last ticket.
					MeterMate.readTicket();
				}
							
				if (MeterMate.hasTicket())
				{
					// Process ticket.
					callbacks.onTicketComplete();
					
					// Hide 'Reading Ticket' text.
					tvReadingTicket.setVisibility(View.INVISIBLE);
	
					// Enable Next button.
					btnNext.setEnabled(true);
					
					// Shutdown MeterMate connection.
					MeterMate.shutdown();
				}
			}
	
			// Update UI.
			tvPresetLitres.setText(MeterMate.getPresetLitres());

            tvDeliveredLitres.setText(btnBack.isEnabled() ? "" : MeterMate.getRealtimeLitres());

			tvTemperature.setText(MeterMate.getTemperature());
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private final Handler appHandler = new Handler()
	{
		@Override 
		public void handleMessage(Message msg) 
		{
			try
			{
				switch (msg.what)
				{
					case MeterMate.MESSAGE_COMMS_STATUS_CHANGED:
						updateCommsStatus();
						break;
						
					case MeterMate.MESSAGE_PUMPING_STATUS_CHANGED:
						updatePumpingStatus();
						break;
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	private final OnClickListener onStart = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_MeterMate: onStart");

				// Start demo meter.
				MeterMate.demoStart();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	private final OnClickListener onStop = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_MeterMate: onStop");

				// Stop demo meter.
				MeterMate.demoStop();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
	
	private final OnClickListener onBack = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_MeterMate: onBack");

				// Shutdown MeterMate.
				MeterMate.shutdown();
				
				// Switch to previous view.
				trip.selectView(previousViewName, -1);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	private final OnClickListener onNext = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_MeterMate: onNext");

				// Handle 'Next' clicked.
				callbacks.onNextClicked();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}
