package com.swiftsoft.colossus.mobileoil.bluetooth;

import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PrintingService
{
	// Debugging
	private static final String TAG = "PrintingService";
	private static final boolean D = true;

	private final Context context;
	private final String title;

	private final String msgUnavailable   = "Bluetooth is not available.";
	private final String msgNoPrinter     = "No printer selected.\nSelect 'Change printer' first.";
	private final String msgNotResponding = "Printer not responding.\nPlease ensure it is switched on.";
	private final String msgCheckStatus   = "Checking printer";
	private final String msgHealthBad     = "Printer is not ready.\nPlease check it has paper.";
	private final String msgSendData      = "Sending data to printer";

	private ProgressDialog pd;
	private dbSetting printer;
	private dbSetting logo;
	
	public PrintingService(Context context, String title)
	{
		this.context = context;
		this.title = title;
	}
	
	public void Print(String data)
	{
		// Print data without brand logo.
		internalPrint(data, 0);
	}
	
	void internalPrint(String data, int brandID)
	{
		try
		{
			// Initialise Bluetooth.
			BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

			if (bluetooth == null)
			{
				throw new Exception(msgUnavailable);
			}

			// Find current printer address.
			printer = dbSetting.FindByKey("PrinterAddress");

			if (printer == null || printer.StringValue == null)
			{
				throw new Exception(msgNoPrinter);
			}

			// Find brand logo.
			if (brandID != 0)
			{
				logo = dbSetting.FindByKey("Brand Logo:" + brandID);
			}
			else
			{
				logo = null;
			}

			// Check if Demo printer.
			if (printer.StringValue.equals("00:00:00:00:00"))
			{
				return;
			}
			
			// Create PrintingTask.
			PrintingTask pt = new PrintingTask();
			pt.execute(data);
		}
		catch (Exception e)
		{
			// Show error message.
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(title + " Error");
			builder.setMessage(e.getMessage());
			builder.setPositiveButton("OK", null);
			
			AlertDialog alert = builder.create();
			alert.show();
		}
	}
	
	class PrintingTask extends AsyncTask<String, String, PrintingTaskResult>
	{
		String printerReply;
		CommsService cs;
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();

			// Create the ProgressDialog.
			pd = new ProgressDialog(context);
			pd.setTitle(title);
			pd.setIndeterminate(true);
			pd.setCancelable(false);
			pd.show();
		}
		
		@Override
		protected PrintingTaskResult doInBackground(String... params)
		{
			PrintingTaskResult result = new PrintingTaskResult();
			
			try
			{
				// Initialise result object.
				result.success = false;
				result.message = "";

				// Setup connection to Bluetooth printer.
				cs = new CommsService(printer.StringValue, mHandler);
				cs.Connect(true);
				
				// Wait 20 seconds until connected.
				for (int i = 0; i < 40; i++)
				{
					Thread.sleep(500);
					
					if (cs.isConnected() || cs.isError())
					{
						break;
					}
				}
				
				// Check if error occurred.
				if (cs.isError())
				{
					throw new Exception(cs.getError());
				}

				// Check if now connected.
				if (!cs.isConnected())
				{
					// Return connection timeout error.
					result.message = msgNotResponding;
					return result;
				}

				// Send printer check status command (ESC h - Zebra RW420 printer)
				publishProgress(msgCheckStatus);
				printerReply = "";
				cs.Send("\u001b\u0068\u0000");

				// Wait 5 seconds for reply.
				boolean printerOK = false;

				for (int i = 0; i < 10; i++)
				{
					if (D)
					{
						Log.d(TAG, "Waiting for printer reply");
					}
					
					Thread.sleep(500);

					if (printerReply.length() > 0)
					{
						if (D)
						{
							Log.d(TAG, "Printer reply " + printerReply);
						}
						
						if (printerReply.equals("\u0010"))
						{
							printerOK = true;
							break;
						}
						
						// Printer status error.
						result.message = msgHealthBad;
						return result;
					}
				}
				
				if (!printerOK)
				{
					// Return comms timeout error.
					result.message = msgNotResponding;
					return result;
				}
				
				// Send data.
				publishProgress(msgSendData);
				cs.Send(params[0]);
				Thread.sleep(1000);
				
				// Send printer check status command (ESC h - Zebra RW420 printer)
				printerReply = "";
				cs.Send("\u001b\u0068\u0000");

				// Wait 20 seconds for reply.
				printerOK = false;

				for (int i = 0; i < 38; i++)
				{
					if (D)
					{
						Log.d(TAG, "Waiting for printer reply");
					}
					
					Thread.sleep(500);

					if (printerReply.length() > 0)
					{
						if (D)
						{
							Log.d(TAG, "Printer reply " + printerReply);
						}
						
						if (printerReply.equals("\u0010") ||	// Printer ok 
							printerReply.equals("\u0018"))		// Printer ok - but battery low.
						{
							printerOK = true;
							break;
						}
						
						// Printer status error.
						result.message = msgHealthBad;
						return result;
					}
				}
				
				if (D)
				{
					Log.d("Printing", "Printer " + printerOK);
				}
			}
			catch (Exception e)
			{
				if (D)
				{
					Log.d("Printing", "Exception " + e.getMessage());
				}
				
				result.message = e.getMessage();
				return result;
			}
			finally
			{
				if (D)
				{
					Log.d(TAG, "Disconnecting with result " + result.message);
				}

				// Close connection
				cs.Disconnect(true);				
			}
			
			// Return success!
			result.success = true;
			return result;
		}

		@Override
		protected void onProgressUpdate(String... values)
		{
			super.onProgressUpdate(values);
			
			pd.setMessage(values[0]);
		}
		
		@Override
		protected void onPostExecute(PrintingTaskResult result)
		{
			super.onPostExecute(result);
			
			try
			{
				// Hide ProgressDialog
				if (pd.isShowing())
				{
					pd.dismiss();
				}
				
				if (!result.success)
				{
					// Show error message.
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Printing error");
					builder.setMessage(result.message);
					builder.setPositiveButton("OK", null);
					
					AlertDialog alert = builder.create();
					alert.show();
				}
				
				if (result.success)
				{
					// Mark logo as downloaded.
					if (logo != null)
					{
						logo.IntValue = 1;
						logo.save();
					}
				}
			}
			catch (Exception e) 
			{
				// Exception can occur, if the Activity has been destroyed.
			}
		}
		
		@SuppressLint("HandlerLeak")
		private final Handler mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
					case CommsService.MESSAGE_READ:
						byte[] readBuf = (byte[]) msg.obj;
						printerReply = new String(readBuf, 0, msg.arg1);
						break;
						
					case CommsService.MESSAGE_INFO:
						pd.setMessage((String)msg.obj);
						break;
				}
			}
		};
	}
	
	class PrintingTaskResult
	{
		boolean success;
		String message;
	}
}