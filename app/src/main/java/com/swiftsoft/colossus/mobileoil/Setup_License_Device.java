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

import com.swiftsoft.colossus.mobileoil.view.MyEditText;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Setup_License_Device extends MyFlipperView
{
	private Setup setup;
	private LayoutInflater inflater;

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
        tvMessage.setText(state.getString("License.Device.Message.Text"));
        tvMessage.setEnabled(state.getBoolean("License.Device.Message.Enabled"));
        tvMessage.setVisibility(state.getInt("License.Device.Message.Visibility"));

        tvSerialNo.setText(state.getString("License.Device.SerialNo.Text"));
        tvSerialNo.setEnabled(state.getBoolean("License.Device.SerialNo.Enabled"));
        tvSerialNo.setVisibility(state.getInt("License.Device.SerialNo.Visibility"));

        tvPin.setText(state.getString("License.Device.Pin.Text"));
        tvPin.setEnabled(state.getBoolean("License.Device.Pin.Enabled"));
        tvPin.setVisibility(state.getInt("License.Device.Pin.Visibility"));

        tvError.setText(state.getString("License.Device.Error.Text"));
        tvError.setEnabled(state.getBoolean("License.Device.Error.Enabled"));
        tvError.setVisibility(state.getInt("License.Device.Error.Visibility"));

        etPin.setText(state.getString("License.Device.Pin.Edit,Text"));
        etPin.setEnabled(state.getBoolean("License.Device.Pin.Edit.Enabled"));
        etPin.setVisibility(state.getInt("License.Device.Pin.Edit.Visibility"));

        btnRetry.setText(state.getString("License.Device.Retry.Text"));
        btnRetry.setEnabled(state.getBoolean("License.Device.Retry.Enabled"));
        btnRetry.setVisibility(state.getInt("License.Device.Retry.Visibility"));

        btnNext.setText(state.getString("License.Device.Next.Text"));
        btnNext.setEnabled(state.getBoolean("License.Device.Next.Enabled"));
        btnNext.setVisibility(state.getInt("License.Device.Next.Visibility"));

        infoview.setDefaultTv1(state.getString("License.Device.Info.TV1"));
        infoview.setDefaultTv2(state.getString("License.Device.Info.TV2"));
    }

    public void saveState(Bundle savedState) {
        // Save Message TextView
        savedState.putString("License.Device.Message.Text", tvMessage.getText().toString());
        savedState.putBoolean("License.Device.Message.Enabled", tvMessage.isEnabled());
        savedState.putInt("License.Device.Message.Visibility", tvMessage.getVisibility());

        // Save Serial No TextView
        savedState.putString("License.Device.SerialNo.Text", tvSerialNo.getText().toString());
        savedState.putBoolean("License.Device.SerialNo.Enabled", tvSerialNo.isEnabled());
        savedState.putInt("License.Device.SerialNo.Visibility", tvSerialNo.getVisibility());

        // Save Pin TextView
        savedState.putString("License.Device.Pin.Text", tvPin.getText().toString());
        savedState.putBoolean("License.Device.Pin.Enabled", tvPin.isEnabled());
        savedState.putInt("License.Device.Pin.Visibility", tvPin.getVisibility());

        // Save Error TextView
        savedState.putString("License.Device.Error.Text", tvError.getText().toString());
        savedState.putBoolean("License.Device.Error.Enabled", tvError.isEnabled());
        savedState.putInt("License.Device.Error.Visibility", tvError.getVisibility());

        // Save Pin EditText
        savedState.putString("License.Device.Pin.Edit.Text", etPin.getText().toString());
        savedState.putBoolean("License.Device.Pin.Edit.Enabled", etPin.isEnabled());
        savedState.putInt("License.Device.Pin.Edit.Visibility", etPin.getVisibility());

        // Save Retry Button
        savedState.putString("License.Device.Retry.Text", btnRetry.getText().toString());
        savedState.putBoolean("License.Device.Retry.Enabled", btnRetry.isEnabled());
        savedState.putInt("License.Device.Retry.Visibility", btnRetry.getVisibility());

        // Save Next Button
        savedState.putString("License.Device.Next.Text", btnNext.getText().toString());
        savedState.putBoolean("License.Device.Next.Enabled", btnNext.isEnabled());
        savedState.putInt("License.Device.Next.Visibility", btnNext.getVisibility());

        // Save the InfoView state
        savedState.putString("License.Device.Info.TV1", infoview.getDefaultTv1());
        savedState.putString("License.Device.Info.TV2", infoview.getDefaultTv2());
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
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    TextWatcher twPin = new TextWatcher()
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

	OnClickListener onRetry = new OnClickListener()
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

	OnClickListener onNext = new OnClickListener()
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
				
				if (etPin.getVisibility() == View.VISIBLE &&
					etPin.isEnabled())
				{
					// Get company PIN.
					int pin = 0;

					try
					{
						pin = Integer.parseInt(etPin.getText().toString());
					}
					catch (Exception e)
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
				tvMessage.setText("Device now licensed");
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
			tvMessage.setText("Checking for existing license");
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
