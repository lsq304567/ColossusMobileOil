package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.utilities.ControlSaver;
import com.swiftsoft.colossus.mobileoil.view.MyEditText;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Setup_License_Device extends MyFlipperView
{
	private Setup setup;

	private MyInfoView1Line infoview;
	private TextView tvMessage;
	private TextView tvSerialNo;
	private TextView tvPin;
	private MyEditText etPin;
	private TextView tvError;
	private Button btnRetry;
	private Button btnNext;
	
	public Setup_License_Device(Context context)
	{
		super(context);
		init(context);
	}

	public Setup_License_Device(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

    @SuppressWarnings("ResourceType")
    public void restoreState(Bundle state)
    {
		ControlSaver.restore(tvMessage, "License.Device.Message", state);
		ControlSaver.restore(tvSerialNo, "License.Device.SerialNo", state);
		ControlSaver.restore(tvPin, "License.Device.Pin", state);
		ControlSaver.restore(tvError, "License.Device.Error", state);
		ControlSaver.restore(etPin, "License.Device.Pin.Edit", state);
		ControlSaver.restore(btnRetry, "License.Device.Retry", state);
		ControlSaver.restore(btnNext, "License.Device.Next", state);
		ControlSaver.restore(infoview, "License.Device.Info", state);
    }

    public void saveState(Bundle savedState)
    {
        ControlSaver.save(tvMessage, "License.Device.Message", savedState);
        ControlSaver.save(tvSerialNo, "License.Device.SerialNo", savedState);
        ControlSaver.save(tvPin, "License.Device.Pin", savedState);
        ControlSaver.save(tvError, "License.Device.Error", savedState);
        ControlSaver.save(etPin, "License.Device.Pin.Edit", savedState);
        ControlSaver.save(btnRetry, "License.Device.Retry", savedState);
        ControlSaver.save(btnNext, "License.Device.Next", savedState);
        ControlSaver.save(infoview, "License.Device.Info", savedState);
    }

	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup_License_Device: init");

			// Store reference to Startup activity.
			setup = (Setup)context;
	
			// Inflate layout.
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.setup_license_device, this, true);
			
			infoview = (MyInfoView1Line)this.findViewById(R.id.setup_license_device_infoview);
			tvMessage = (TextView)this.findViewById(R.id.setup_license_device_message);
			tvSerialNo = (TextView)this.findViewById(R.id.setup_license_device_serial_no);
			tvPin = (TextView)this.findViewById(R.id.setup_license_device_pin_tv);
			etPin = (MyEditText)this.findViewById(R.id.setup_license_device_pin_et);
			tvError = (TextView)this.findViewById(R.id.setup_license_device_error);
			btnRetry = (Button)this.findViewById(R.id.setup_license_device_retry);
			btnNext = (Button)this.findViewById(R.id.setup_license_device_next);
			
			etPin.addTextChangedListener(twPin);
			btnRetry.setOnClickListener(onRetry);
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
			CrashReporter.leaveBreadcrumb("Setup_License_Device: resumeView");
			
			// Resume updating.
			infoview.resume();
			
			// First try relicensing device.
			relicense();
			
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
			CrashReporter.leaveBreadcrumb("Setup_License_Device: pauseView");
			
			// Pause updating.
			infoview.pause();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	@Override
	public void updateUI() 
	{
		try
		{
			tvSerialNo.setText(Utils.getSerialNo(setup));
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

    private final TextWatcher twPin = new TextWatcher()
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
			try
			{
				if (etPin.getText().length() > 0)
					btnNext.setEnabled(true);
				else
					btnNext.setEnabled(false);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	private final OnClickListener onRetry = new OnClickListener()
	{		
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Setup_License_Device: onRetry");
				
				// Disable Retry button.
				btnRetry.setEnabled(false);
				
				// Try relicensing again.
				relicense();
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
				CrashReporter.leaveBreadcrumb("Setup_License_Device: onNext");
				
				// Reset UI.
				tvError.setVisibility(View.INVISIBLE);
				btnNext.setEnabled(false);
				
				if (etPin.getVisibility() == View.VISIBLE && etPin.isEnabled())
				{
					// Get company PIN.
					int pin = 0;

					try
					{
						pin = Integer.parseInt(etPin.getText().toString());
					}
					catch (Exception ignored)
					{
					}
	
					// Try to license device.
					setup.licenseDevice(pin);
				}
				else
				{
					if (setup.isDeviceLicensed())
					{
						setup.selectView(Setup.ViewRegisterDevice, +1);
					}
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	public void onLicenseMessage(Message msg)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup_License_Device: onLicenseMessage");
			
			// Extract data.
			Bundle bundle = msg.getData();
			String type = bundle.getString("Type");
			String error = bundle.getString("Error");
			int result = bundle.getInt("Result");
			
			if (setup.isDeviceLicensed())
			{
				// User may now proceed.
				tvMessage.setText(R.string.setup_license_device_now_licensed);
				etPin.setEnabled(false);
				btnNext.setEnabled(true);
				return;
			}
			
			if (type.equals("Relicense"))
			{
				if (result == -2)
				{
					// Device is not registered on licensing server.
					// Request company PIN.
					tvMessage.setText("A new license is required.\r\nPlease enter your company PIN");
					tvPin.setVisibility(View.VISIBLE);
					etPin.setVisibility(View.VISIBLE);
					return;
				}
				
				// Relicense failed - allow retry.
				btnRetry.setVisibility(View.VISIBLE);
				btnRetry.setEnabled(true);
			}
			
			// Show error message.
			tvError.setText(error);
			tvError.setVisibility(View.VISIBLE);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	private void relicense()
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup_License_Device: relicense");
			
			// Setup UI.
			infoview.setDefaultTv1("App Setup");
			infoview.setDefaultTv2("Device licensing");
			tvMessage.setText(R.string.setup_license_device_now_checking_for_license);
			tvPin.setVisibility(View.INVISIBLE);
			etPin.setVisibility(View.INVISIBLE);
			tvError.setVisibility(View.INVISIBLE);
			btnRetry.setEnabled(false);
			btnNext.setEnabled(false);
			
			// Call licensing server.
			setup.relicenseDevice();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
}
