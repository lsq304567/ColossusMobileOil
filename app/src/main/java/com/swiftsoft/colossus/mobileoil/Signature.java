package com.swiftsoft.colossus.mobileoil;

import com.swiftsoft.colossus.mobileoil.view.MySignature;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Signature extends Activity
{
	public static final int REQUEST_SIGNATURE = 1001;

	private TextView signatureTitle;
	private MySignature signature;
	private String signatureType;
	private String signatureName;
	
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
			signatureTitle = (TextView) findViewById(R.id.signature_title);
			signature = (MySignature) findViewById(R.id.signature);
	
			// Find parameters.
			Intent intent = getIntent();

			signatureType = intent.getExtras().getString("SignatureType");
			signatureName = intent.getExtras().getString("SignatureName");
			
			if (signatureType.equals("Customer"))
			{
				CrashReporter.leaveBreadcrumb("Signature: onCreate - Customer to sign");

				signatureTitle.setText("CUSTOMER - sign to acknowledge goods received");
			}
			else if (signatureType.equals("Driver"))
			{
				CrashReporter.leaveBreadcrumb("Signature: onCreate - Driver to sign");

				signatureTitle.setText("DRIVER - sign to acknowledge receipt of payment");
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
			
			// Past data back to calling activity.
			Intent data = new Intent();

			data.putExtra("SignatureType", signatureType);
			data.putExtra("SignatureName", signatureName);
			data.putExtra("SignatureImage", signature.toByteArray());

			setResult(RESULT_OK, data);

			finish();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
}
