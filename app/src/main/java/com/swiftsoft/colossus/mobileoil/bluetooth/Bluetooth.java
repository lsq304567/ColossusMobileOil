package com.swiftsoft.colossus.mobileoil.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.lang.reflect.Method;

public class Bluetooth
{
	// Debugging
	static final String TAG = "Bluetooth";
	static final boolean D = true;

	// Must be initialised from UI thread.
	public static BluetoothAdapter bluetooth;
	
	public static void enable() throws InterruptedException
	{
		if (!bluetooth.isEnabled())
		{
			// Enable Bluetooth.
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
	}
	
	public static BluetoothSocket Connect(String deviceAddress) throws Exception
	{
		BluetoothSocket socket = null;
		
		if (bluetooth.isEnabled())
		{
			if (D)
				Log.d(TAG, "Connecting to " + deviceAddress);
			
			BluetoothDevice device = bluetooth.getRemoteDevice(deviceAddress);
			
			// This method used reflection, but proves more reliable 
			// than the createRfcommSocketToServiceRecord public API!
			Method m = device.getClass().getMethod("createRfcommSocket", new Class[] { Integer.TYPE });         
			socket = (BluetoothSocket) m.invoke(device, new Object[] { Integer.valueOf(1) });
	
			// Public API method.
			//BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			
			// Try to connect.
			socket.connect();

			if (D)
				Log.d(TAG, "Connected to " + deviceAddress);

			// Wait a moment.
			Thread.sleep(200);
		}
		
		return socket;
	}
	
	public static void disable() throws InterruptedException
	{
		if (bluetooth.isEnabled())
		{
			// Disable Bluetooth.
			bluetooth.disable();
		
			// Wait for up to 5 seconds for Bluetooth to disable.
			for (int i=0; i < 10; i++)
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
