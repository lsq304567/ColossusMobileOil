package com.swiftsoft.colossus.mobileoil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.view.MySignature;

public class Signature extends Activity
{
	public static final int REQUEST_SIGNATURE = 1001;

	private MySignature signature;
	private String signatureType;
	private String signatureName;
	private boolean unattendedDelivery;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Signature: onCreate");
			
			// Setup view.
			setContentView(R.layout.signature);
			
			// Find UI controls.
			TextView signatureTitle = (TextView) findViewById(R.id.signature_title);
			signature = (MySignature) findViewById(R.id.signature);
	
			// Find parameters.
			Intent intent = getIntent();

			signatureType = intent.getExtras().getString("SignatureType");
			signatureName = intent.getExtras().getString("SignatureName");
			unattendedDelivery = intent.getExtras().getBoolean("SignatureUnattended");
			
			if (signatureType.equals("Customer"))
			{
				CrashReporter.leaveBreadcrumb("Signature: onCreate - Customer to sign");

                if (unattendedDelivery)
                {
                    signatureTitle.setText(R.string.signature_title_unattended_delivery);
                }
                else
                {
                    signatureTitle.setText(R.string.signature_title_customer);
                }
			}
			else if (signatureType.equals("Driver"))
			{
				CrashReporter.leaveBreadcrumb("Signature: onCreate - Driver to sign");

				signatureTitle.setText(R.string.signature_title_driver);
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	public void onBackClicked(View button) 
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Signature: onBackClicked");

			CrashReporter.leaveBreadcrumb("Signature: onBackClicked - " + ((Button)button).getText().toString());
			
			// Close activity.
			setResult(RESULT_CANCELED);
			finish();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	public void onTryAgainClicked(View button) 
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Signature: onTryAgainClicked");

            CrashReporter.leaveBreadcrumb("Signature: onTryAgainClicked - " + ((Button)button).getText().toString());

            // Reset the signature.
			signature.ResetSignature();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	public void onFinishClicked(View button) 
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Signature: onFinishClicked");

            CrashReporter.leaveBreadcrumb("Signature: onFinishClicked - " + ((Button)button).getText().toString());

            // Past data back to calling activity.
			Intent data = new Intent();

			data.putExtra("SignatureType", signatureType);
			data.putExtra("SignatureName", signatureName);
			data.putExtra("SignatureImage", signature.toByteArray());
            data.putExtra("SignatureUnattended", unattendedDelivery);

			setResult(RESULT_OK, data);

			finish();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
}
