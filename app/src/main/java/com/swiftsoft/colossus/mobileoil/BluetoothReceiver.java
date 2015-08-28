package com.swiftsoft.colossus.mobileoil;

import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//
// During Bluetooth pairing the user is prompted for a pin.
// This class automatically passed the default 1234 pin.
//
public class BluetoothReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			// Find Bluetooth device which requires pairing.
			BluetoothDevice device = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");

			// Pass default 1234 pin.
			Method m = device.getClass().getMethod("setPin", byte[].class);
			byte[] pin = "1234".getBytes("UTF-8");
			m.invoke(device, pin);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
}
