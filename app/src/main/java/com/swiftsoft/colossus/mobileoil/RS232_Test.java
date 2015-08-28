package com.swiftsoft.colossus.mobileoil;

import com.swiftsoft.colossus.mobileoil.bluetooth.CommsService;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class RS232_Test extends Activity
{
	// Debugging
	static final String TAG = "RS232Test";
	static final boolean D = true;
	
	// Temporary hardcoded device ID.
//	static final String rs232device = "00:12:6F:21:50:DC";
	static final String rs232device = "00:03:7A:33:21:4D";

	static String buffer = "";
	
	BluetoothAdapter bluetooth;
	CommsService cs;
	
	TextView tvMessage;
	LinearLayout llConnect;
	Button btnConnect;
	LinearLayout llSend;
	EditText etSendData;
	ScrollView svAscii;
	TextView tvAscii;
	ScrollView svHex;
	TextView tvHex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (D)
		{
			Log.d(TAG, "+++ ON CREATE +++");
		}
		
		setContentView(R.layout.rs232_test);

		// Initialise Bluetooth.
		bluetooth = BluetoothAdapter.getDefaultAdapter();

		if (bluetooth == null)
		{
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// Create CommsService.
		cs = new CommsService(rs232device, mHandler);
		
		// Find UI controls.
		tvMessage  = (TextView)findViewById(R.id.rs232_message);
		llConnect  = (LinearLayout)findViewById(R.id.rs232_connect);
		btnConnect = (Button)findViewById(R.id.rs232_connect_button);
		llSend     = (LinearLayout)findViewById(R.id.rs232_send_data);
		etSendData = (EditText)findViewById(R.id.rs232_data);
		svAscii    = (ScrollView)findViewById(R.id.rs232_ascii_scrollview);
		tvAscii    = (TextView)findViewById(R.id.rs232_ascii_textview);
		svHex      = (ScrollView)findViewById(R.id.rs232_hex_scrollview);
		tvHex      = (TextView)findViewById(R.id.rs232_hex_textview);
		
		// Set inital state.
		updateUI();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		if (D)
		{
			Log.d(TAG, "+++ ON START +++");
		}
		
		// Enable Bluetooth is not on.
		if (!bluetooth.isEnabled())
		{
			bluetooth.enable();
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();

		if (D)
		{
			Log.d(TAG, "+++ ON RESUME +++");
		}

		// Updating UI to reflect connection state.
		updateUI();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if (D)
		{
			Log.d(TAG, "+++ ON DESTROY +++");
		}
		
		// Close Bluetooth connection.
		cs.Disconnect(false);
	}
	
    @Override
    public void onBackPressed()
	{
		super.onBackPressed();

		if (D)
		{
			Log.d(TAG, "+++ ON BACK +++");
		}
		
		finish();
    }

	public void onConnectClicked(View button)
	{
		// Disable button to prevent another attempt.
		btnConnect.setEnabled(false);
		
		// Try to connect to RS232 device.
		cs.Connect(false);
	}
	
	// Button onClick handler.
	public void onSendData(View button)
	{
		cs.Send(etSendData.getText().toString() + "\r\n");
	}
	
	// Button onClick handler.
	public void onSendNUL(View button)
	{
		cs.Send("\u0000");
	}

	// Button onClick handler.
	public void onSendETX(View button)
	{
		cs.Send("\u0003");
	}

	byte[] outputBuffer;
	
    private void SendCommandToEMR3(String command)
    {
		char delimiter = 0x7e;
		char destination = 0x01;
		char source = 0xff;
    	
        // EMR3 Format is:
        // delimiter, destination, source, command, checksum, delimiter

        StringBuilder msg = new StringBuilder();
        msg.append(destination);
        msg.append(source);
        msg.append(command);
        
        // Calculate checksum.
		int chk = 0;
		for (char ch : msg.toString().toCharArray())
			chk += ch;

		chk = (0x100 - (chk & 0xff));

		msg.append((char) chk);
		
		// Send command preset to EMR3
		cs.Send(delimiter + msg.toString() + delimiter);
    }
	
	public void onCmd50(View button)
	{
		SendCommandToEMR3("T\u0001");
	}

	public void onCmd51(View button)
	{
//		SendCommandToEMR3("H\u0000");	// Get no of tickets in memory
		SendCommandToEMR3("Sc\u0000\u0040\u001c\u0046");
	}

	public void onCmd53(View button)
	{
		SendCommandToEMR3("Su\u0002");	// press mode
		// SendCommandToEMR3("O\u0001\u0001");
	}
	
	public void onCmd54(View button)
	{
		SendCommandToEMR3("Sp\u0001");	// set product
//		SendCommandToEMR3("O\u0003");
	}

	protected void updateUI()
	{
		if (cs.getState() == CommsService.STATE_CONNECTED)
		{
			tvMessage.setText("Connected");
			llConnect.setVisibility(View.GONE);
			llSend.setVisibility(View.VISIBLE);
			tvAscii.setText("");
			tvHex.setText("");
			etSendData.requestFocus();
		}

		if (cs.getState() == CommsService.STATE_CONNECTING)
		{
			tvMessage.setText("Connecting");
		}
		
		if (cs.getState() == CommsService.STATE_DISCONNECTED)
		{
			tvMessage.setText("Disconnected");
			llConnect.setVisibility(View.VISIBLE);
			llSend.setVisibility(View.GONE);
			btnConnect.setEnabled(true);
		}
	}
	
//	private long unsignedIntToLong(char[] Msg, int startIndex)
//	{
//	    long l = 0;
//	    
//	    l |= Msg[startIndex + 3] & 0xFF;
//	    l <<= 8;
//	    l |= Msg[startIndex + 2] & 0xFF;
//	    l <<= 8;
//	    l |= Msg[startIndex + 1] & 0xFF;
//	    l <<= 8;
//	    l |= Msg[startIndex] & 0xFF;
//	    
//	    return l;
//	}
//
//	private long unsignedShortToInt(char[] Msg, int startIndex)
//	{
//	    long l = 0;
//
//	    l |= Msg[startIndex + 1] & 0xFF;
//	    l <<= 8;
//	    l |= Msg[startIndex] & 0xFF;
//	    
//	    return l;
//	}

	private final Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case CommsService.MESSAGE_STATE:
					//Toast.makeText(getApplicationContext(), "State changed: " + msg.arg1, Toast.LENGTH_LONG).show();
					updateUI();
					break;
					
				case CommsService.MESSAGE_READ:
					String readMessage;
					byte[] readBuf = (byte[]) msg.obj;
					
					// Show HEX version ...
					readMessage = "";
					for (int i = 0; i < msg.arg1; i++)
					{
						readMessage += Integer.toHexString(0xFF & readBuf[i]);
						if ((int)readBuf[i] == 13)  // was 10
							readMessage += "\n";
						else
							readMessage += ",";
					}
					
					tvHex.setText(tvHex.getText().toString() + readMessage);

					// and scroll to bottom.
					svHex.post(new Runnable() {                
						@Override    
						public void run() {           
							svHex.fullScroll(View.FOCUS_DOWN);                  
					}});

					
					// Show ASCII version ...
					readMessage = new String(readBuf, 0, msg.arg1);
					tvAscii.setText(tvAscii.getText().toString() + readMessage);
					
					buffer += readMessage;
					
//					if (buffer.startsWith("\u0006") && buffer.endsWith("\r"))
//					{
//						char[] te550Msg = buffer.toCharArray();
//
//						// Parse buffer.
//						StringBuilder decodedMsg = new StringBuilder();
//						decodedMsg.append("\n");
//						decodedMsg.append("Status          : " + (int)te550Msg[4] + "\n");
//						decodedMsg.append("Errors          : " + unsignedShortToInt(te550Msg, 5) + "\n");
//						decodedMsg.append("Product         : " + (int)te550Msg[11] + "\n");
//						decodedMsg.append("Actual volume   : " + unsignedIntToLong(te550Msg, 12) + "\n");
//						decodedMsg.append("Actual Totalizer: " + unsignedIntToLong(te550Msg, 16) + "\n");
//						decodedMsg.append("Custom volume   : " + unsignedIntToLong(te550Msg, 20) + "\n");
//						decodedMsg.append("Custom Totalizer: " + unsignedIntToLong(te550Msg, 24) + "\n");
//						decodedMsg.append("Preset volume   : " + unsignedIntToLong(te550Msg, 28) + "\n");
//						decodedMsg.append("Temperature     : " + unsignedShortToInt(te550Msg, 32) + "\n");
//						decodedMsg.append("Pressure        : " + unsignedShortToInt(te550Msg, 34) + "\n");
//						decodedMsg.append("Density         : " + unsignedShortToInt(te550Msg, 36) + "\n");
//						decodedMsg.append("Flow rate       : " + unsignedShortToInt(te550Msg, 38) + "\n");
//						decodedMsg.append("Delivery number : " + unsignedIntToLong(te550Msg, 45) + "\n");
//						
//						tvAscii.setText(tvAscii.getText().toString() + decodedMsg.toString());
//						
//						// Clear buffer for next message.
//						buffer = "";
//					}

					// and scroll to bottom.
					svAscii.post(new Runnable() {                
						@Override    
						public void run() {           
							svAscii.fullScroll(View.FOCUS_DOWN);                  
					}});
					
					break;
			}
		}
	};
}
