package com.swiftsoft.colossus.mobileoil.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.CrashReporter;
import com.swiftsoft.colossus.mobileoil.R;

import java.util.ArrayList;

public class Discovery extends Activity
{
	BluetoothAdapter bluetooth;
	String type;
	String oldAddress;
	String newAddress;
	String newName;
	
	TextView tvMessage;
	ListView lv;
	Button btnOK;
	Button btnCancel;
	
	final String msgUnavailable = "Bluetooth is not available.";
	final String msgNotEnabled  = "Bluetooth is not enabled.";
	final String msgChecking    = "Checking Bluetooth";
	final String msgTurningOn   = "Turning Bluetooth on";
	final String msgDiscovery   = "Discovery started";
	final String msgSuccess     = "Success";
	final String msgTurningOff  = "Turning Bluetooth off";
	
	DiscoveredDeviceAdapter adapter;
	ArrayList<DiscoveredDevice> discoveredDevices;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		try
		{
			// Setup view.
			setContentView(R.layout.bluetooth_discovery);
	
			// Retrieve intent data.
			Intent intent = getIntent();
			type = intent.getExtras().getString("type");
			oldAddress = intent.getExtras().getString("oldAddress");
	
			// Setup UI.
			tvMessage = (TextView)findViewById(R.id.bluetooth_discovery_message);
			lv = (ListView)findViewById(R.id.bluetooth_discovery_devices);
			lv.setEnabled(false);
			lv.setOnItemClickListener(new OnItemClickListener() 
			{
				@Override
				public void onItemClick(AdapterView<?> a, View v, int position, long id)
				{
					lv.setItemChecked(position, true);
					
					// Store new address.
					DiscoveredDevice dd = (DiscoveredDevice)lv.getAdapter().getItem(position);
					newAddress = dd.remoteDevice.getAddress();
					newName = dd.remoteDevice.getName();
					
					// Enable OK button.
					btnOK.setEnabled(true);
				}
			}
			);
	
			btnOK = (Button)findViewById(R.id.bluetooth_discovery_ok);
			btnOK.setEnabled(false);
			
			btnCancel = (Button)findViewById(R.id.bluetooth_discovery_cancel);
			btnCancel.setEnabled(false);
	
			// Register for Bluetooth messages.
			registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
			
			// Start discovery process in background thread.
			DiscoveryTask dt = new DiscoveryTask();
			dt.execute();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		try
		{
	        // Make sure we're not doing discovery anymore
	        if (bluetooth != null) {
	            bluetooth.cancelDiscovery();
	        }
	
	        // Unregister broadcast listeners
	        unregisterReceiver(discoveryResult);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	// Prevent the Back button from exiting the application.
	@Override
	public void onBackPressed()
	{
		return;
	}

	public void onOKClicked(View button)
	{
		try
		{
			// Pass data back to calling Activity.
			Intent data = new Intent();
			data.putExtra("type", type);
			data.putExtra("newAddress", newAddress);
			data.putExtra("newName", newName);
			setResult(RESULT_OK, data);
			finish();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	public void onCancelClicked(View button)
	{
		try
		{
			// Finish activity.
			finish();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	class DiscoveryTask extends AsyncTask<Void, String, DiscoveryThreadResult>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			
			// Initialise Bluetooth (must be done in UI thread)
			@SuppressWarnings("unused")
			BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
			
			// Create new empty list.
			discoveredDevices = new ArrayList<DiscoveredDevice>();
		}
		
		@Override
		protected DiscoveryThreadResult doInBackground(Void... params)
		{
			BluetoothAdapter bluetooth = null;
			DiscoveryThreadResult result = new DiscoveryThreadResult();
			
			try
			{
				// Initialise result object.
				result.success = false;
				result.message = "";
				
				//
				// Step 1: Check Bluetooth is available.
				//
				bluetooth = BluetoothAdapter.getDefaultAdapter();

				if (bluetooth == null)
				{
					// Return error.
					result.message = msgUnavailable;
					return result;
				}
				
				//
				// Step 2: Disable Bluetooth is already enabled, to improve reliability.
				//
				if (bluetooth.isEnabled())
				{
					// Update progress.
					this.publishProgress(msgTurningOff);

					// Disable Bluetooth.
					bluetooth.disable();

					// Wait for up to 5 seconds for Bluetooth to disable.
					for (int i=0; i < 10; i++)
					{
						Thread.sleep(500);

						if (bluetooth.getState() == BluetoothAdapter.STATE_OFF)
							break;
					}

					Thread.sleep(1000);
				}

				//
				// Step 3: Check Bluetooth is enabled.
				//
				if (!bluetooth.isEnabled())
				{
					// Update progress.
					this.publishProgress(msgTurningOn);

					// If not, try to enable Bluetooth.
					bluetooth.enable();
					
					// Wait for up to 5 seconds for Bluetooth to enable.
					for (int i = 0; i < 10; i++)
					{
						Thread.sleep(500);
				
						if (bluetooth.getState() == BluetoothAdapter.STATE_ON)
						{
							break;
						}
					}
				}
				
				// Still not enabled?
				if (!bluetooth.isEnabled())
				{
					// Return error.
					result.message = msgNotEnabled;
					return result;
				}

				//
				// Step 4: Start Bluetooth discovery process.
				//
				if (!bluetooth.isDiscovering())
				{
					// Update progress.
					this.publishProgress(msgDiscovery);
					
					bluetooth.startDiscovery();
					
					// Wait until complete.
					while (bluetooth.isDiscovering())
					{
						Thread.sleep(1000);
					}
				}

				// Success.
				result.message = msgSuccess;
			}
			catch (Exception e)
			{
				// Return error.
				result.message = "Exception: " + e.getMessage();
				return result;
			}
			finally
			{
				if (bluetooth != null)
				{
					bluetooth.cancelDiscovery();
				}
				
				//
				// Disable Bluetooth.
				//
				try
				{
					if (bluetooth != null)
					{
						if (bluetooth.isEnabled())
						{
							// Update progress.
							this.publishProgress(msgTurningOff);

							// Disable Bluetooth.
							bluetooth.disable();

							// Wait for up to 5 seconds for Bluetooth to disable.
							for (int i = 0; i < 10; i++)
							{
								Thread.sleep(500);

								if (bluetooth.getState() == BluetoothAdapter.STATE_OFF)
								{
									break;
								}
							}
						}
					}
				}
				catch (Exception e)
				{					
				}
			}

			// Return success!
			result.success = true;
			return result;
		}

		@Override
		protected void onProgressUpdate(String... values)
		{
			super.onProgressUpdate(values);

			// Update UI.
			tvMessage.setText(values[0]);
		}

		@Override
		protected void onPostExecute(DiscoveryThreadResult result)
		{
			super.onPostExecute(result);

			try
			{
				// Enable ListView & Cancel button.
				lv.setEnabled(true);
				btnCancel.setEnabled(true);
				
				// Update UI.
				tvMessage.setText(result.message);
				
				if (result.message.equals(msgSuccess))
					tvMessage.setText("Select a " + type);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	}

	class DiscoveryThreadResult
	{	
		boolean success;
		String message;
	}
	
	BroadcastReceiver discoveryResult = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			try
			{
				DiscoveredDevice dd = new DiscoveredDevice();
				dd.id = (discoveredDevices.size() + 1);
				dd.name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
				dd.remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				if (dd.remoteDevice.getAddress().equals(oldAddress))
				{
					dd.current = true;
				}
				
				discoveredDevices.add(dd);
				
				refreshData();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
	
	void refreshData()
	{
		try
		{
			// Refresh data.
			adapter = new DiscoveredDeviceAdapter(this, discoveredDevices);
			
			// Bind to listview.
			lv.setAdapter(adapter);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	class DiscoveredDevice
	{
		int id;
		boolean current;
		String name;
		BluetoothDevice remoteDevice;
	}
	
	class DiscoveredDeviceAdapter extends BaseAdapter
	{
		private LayoutInflater mInflater;
		private ArrayList<DiscoveredDevice> mItems;

		public DiscoveredDeviceAdapter(Context context, ArrayList<DiscoveredDevice> items)
		{
			mInflater = LayoutInflater.from(context);

			mItems = new ArrayList<DiscoveredDevice>();
			mItems.addAll(items);
		}

		@Override
		public int getCount()
		{
			return mItems.size();
		}

		@Override
		public Object getItem(int position)
		{
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return mItems.get(position).id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder holder;

			try
			{
				if (convertView == null)
				{
					convertView = mInflater.inflate(R.layout.bluetooth_discovery_row, null);
	
					holder = new ViewHolder();
					holder.tv = (TextView) convertView.findViewById(R.id.bluetooth_discovery_row);
	
					convertView.setTag(holder);
				} 
				else
				{
					holder = (ViewHolder) convertView.getTag();
				}
	
				DiscoveredDevice dd = mItems.get(position);
				String text = "#" + dd.id + " - " + dd.name + (dd.current ? " (Current)" : "");
				text += "\n   Address: " + dd.remoteDevice.getAddress();
				holder.tv.setText(text);
				return convertView;
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
				return null;
			}
		}

		class ViewHolder
		{
			TextView tv;
		}
	}
}
