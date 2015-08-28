package com.swiftsoft.colossus.mobileoil.service;

import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.swiftsoft.colossus.mobileoil.CrashReporter;
import com.swiftsoft.colossus.mobileoil.R;
import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;
import com.swiftsoft.colossus.mobileoil.rest.RestClient;


public class LicensingIntentService extends IntentService
{
	public LicensingIntentService()
	{
		super("LicensingIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		int result = -1;
		String error;
		String type = null;
		String serialNo;
		int companyPIN;
		Messenger messenger = null;
		
		try
		{
			// Get parameters.
			Bundle bundle = intent.getExtras();
			type = bundle.getString("Type");
			serialNo = bundle.getString("SerialNo");
			companyPIN = bundle.getInt("CompanyPIN");
			messenger = (Messenger)bundle.get("Messenger");
			
			// Create JSON input for WebService.
			JSONObject json = new JSONObject();
			json.put("SerialNo", serialNo);
			json.put("CompanyPIN", companyPIN);
			String body = json.toString();
			
			// Create RESTful client.
			RestClient client = new RestClient(getResources().getString(R.string.licensing_url) + type);
			
			// JSON header.
			client.AddHeader("Content-type", "application/json");
			
			// JSON body.
			client.AddBody(body);
			
			try
			{
				// Call Licensing WebService.
			    client.Execute(RestClient.RequestMethod.POST);
			}
			catch (Exception e1)
			{
			    error = e1.getMessage();
			}

			int rcode = client.getResponseCode();
			
			if (rcode == 200)
			{
				// Success!
				String response = client.getResponse();

				JSONObject output = new JSONObject(response);
				result = output.getInt("Result");
				error = output.getString("Error");
				int deviceNo = output.getInt("DeviceNo");
				String url = output.getString("Url");

				if (result == 0)
				{
					// Save to database.
					dbSetting setting1 = dbSetting.FindByKeyOrCreate("DeviceLicensed");
					setting1.StringValue = "true";
					setting1.save();

					dbSetting setting2 = dbSetting.FindByKeyOrCreate("DeviceNo");
					setting2.IntValue = deviceNo;
					setting2.save();
					
					dbSetting setting3 = dbSetting.FindByKeyOrCreate("ColossusURL");
					setting3.StringValue = url;
					setting3.save();
					
					dbSetting consignorName = dbSetting.FindByKeyOrCreate("ConsignorName");
					consignorName.StringValue = output.getString("ConsignorName");
					consignorName.save();

					dbSetting consignorAdd1 = dbSetting.FindByKeyOrCreate("ConsignorAdd1");
					consignorAdd1.StringValue = output.getString("ConsignorAdd1");
					consignorAdd1.save();

					dbSetting consignorAdd2 = dbSetting.FindByKeyOrCreate("ConsignorAdd2");
					consignorAdd2.StringValue = output.getString("ConsignorAdd2");
					consignorAdd2.save();

					dbSetting consignorAdd3 = dbSetting.FindByKeyOrCreate("ConsignorAdd3");
					consignorAdd3.StringValue = output.getString("ConsignorAdd3");
					consignorAdd3.save();
				}
			}
			else
			{
				// Code 0 most likely means timed out.
				error = rcode == 0 ? "Timed out" : "Error code " + rcode;
			}				
		}
		catch (Exception e2)
		{
			error = e2.getMessage();
		}
		
		// Send message back to UI.
		Bundle data = new Bundle();
		data.putString("Type", type);
		data.putString("Error", error);
		data.putInt("Result", result);

		Message msg = Message.obtain();
		msg.setData(data);
		
		try
		{
			messenger.send(msg);
		}
		catch (RemoteException e3)
		{
			CrashReporter.logHandledException(e3);
		}
	}
}
