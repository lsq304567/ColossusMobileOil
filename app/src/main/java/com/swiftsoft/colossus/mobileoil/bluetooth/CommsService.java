package com.swiftsoft.colossus.mobileoil.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class CommsService
{
	// Debugging
	private static final String TAG = "CommsService";
	private static final boolean D = true;

	// Message types.
	public static final int MESSAGE_STATE = 0;
	public static final int MESSAGE_READ = 1;
	public static final int MESSAGE_WRITE = 2;
	public static final int MESSAGE_INFO = 3;

	// Connection state types.
	public static final int STATE_DISCONNECTED = 0;
	public static final int STATE_CONNECTING = 1;
	public static final int STATE_CONNECTED = 2;
	
	private final String msgNotEnabled    = "Bluetooth is not enabled.";
	private final String msgTurningOn     = "Turning Bluetooth on";
	private final String msgConnecting    = "Connecting to printer";
	private final String msgCantConnect   = "Unable to connect to printer.\nPlease ensure it is switched on.";
	private final String msgTurningOff    = "Turning Bluetooth off";
	
	private final String deviceAddress;
	private final Handler handler;
	private final BluetoothAdapter bluetooth;
	
	private volatile String error;

	private int state;
	private boolean autoReconnect;
	private boolean lastDisableBluetoothIfOn;
	private ConnectThread connectThread;
	private ConnectedThread connectedThread;
	
	public CommsService(String deviceAddress, Handler handler)
	{
		this.deviceAddress = deviceAddress;		
		this.handler = handler;
		
		bluetooth = BluetoothAdapter.getDefaultAdapter();
		
		// Initialise state.
		setState(STATE_DISCONNECTED);
	}
	
	public synchronized int getState()
	{
		return state;
	}
	
	public synchronized boolean isConnected()
	{
		return (state == STATE_CONNECTED);
	}

	public boolean isError()
	{
		return (error != null);
	}

	public String getError()
	{
		return error;
	}

	void publishProgress(String message)
	{
		handler.obtainMessage(MESSAGE_INFO, -1, -1, message).sendToTarget();
	}

	void publishError(String message)
	{
		error = message;
	}

	private synchronized void setState(int state)
	{
		this.state = state;
		
		// Notify user of state change.
		handler.obtainMessage(MESSAGE_STATE, state, -1).sendToTarget();
		
		// Auto reconnection if disabled.
		if (state == STATE_DISCONNECTED && autoReconnect)
		{
			connect(lastDisableBluetoothIfOn, 5000);
		}
	}
	
	// Public methods.
	public synchronized void Connect(boolean disableBluetoothIfOn)
	{
		connect(disableBluetoothIfOn, 0);
	}
	
	private synchronized void connect(boolean disableBluetoothIfOn, long delay)
	{
		// Clear previous error.
		error = null;
		
		// Store for reconnect.
		lastDisableBluetoothIfOn = disableBluetoothIfOn;
		
		// Start ConnectThread.
		connectThread = new ConnectThread(deviceAddress, disableBluetoothIfOn, delay);
		connectThread.start();
	}
	
	private synchronized void Connecting()
	{
		setState(STATE_CONNECTING);
	}
	
	private synchronized void Connected(BluetoothSocket socket)
	{
		setState(STATE_CONNECTED);
		
		// Start ConnectedThread.
		connectedThread = new ConnectedThread(socket);
		connectedThread.start();
	}
	
	private synchronized void ConnectionLost()
	{
		setState(STATE_DISCONNECTED);
	}
		
	public void Send(String data)
	{
		if (state == STATE_CONNECTED)
		{
			{
				try
				{
					byte[] buffer = data.getBytes("ISO-8859-1");
					connectedThread.write(buffer);
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public void Disconnect(boolean turnOffBluetooth)
	{
		try
		{
			state = STATE_DISCONNECTED;

			// Close socket.
			if (connectedThread != null)
			{
				connectedThread.close();
				connectedThread = null;
			}
			
			// Disable Bluetooth?
			if (turnOffBluetooth)
			{
				if (bluetooth.isEnabled())
				{
//					// Update progress.
//					publishProgress(msgTurningOff);
//
//					// Disable Bluetooth.
//					bluetooth.disable();
//
//					// Wait for up to 5 seconds for Bluetooth to disable.
//					for (int i=0; i < 10; i++)
//					{
//						Thread.sleep(500);
//
//						if (bluetooth.getState() == BluetoothAdapter.STATE_OFF)
//							break;
//					}
				}
			}
		}
		catch (Exception e)
		{
			// Log exception
			if (D)
			{
				Log.d(TAG, "Exception in Disconnect", e);
			}
		}
	}
	
	//
	// Connect Thread
	//
	class ConnectThread extends Thread
	{
		boolean disableBluetoothIfOn;
		long delay;
		BluetoothDevice device;
		BluetoothSocket socket;
		
		public ConnectThread(String deviceAddress, boolean disableBluetoothIfOn, long delay)
		{
			this.disableBluetoothIfOn = disableBluetoothIfOn;
			this.delay = delay;
			
			device = bluetooth.getRemoteDevice(deviceAddress);
		}
		
		public void run()
		{
			try
			{
				Thread.sleep(delay);
				Connecting();
				
				//
				// Step 1: Disable Bluetooth is already enabled, to improve reliability.
				//
//				if (disableBluetoothIfOn && bluetooth.isEnabled())
//				{
//					// Update progress.
//					publishProgress(msgTurningOff);
//
//					// Disable Bluetooth.
//					bluetooth.disable();
//
//					// Wait for up to 5 seconds for Bluetooth to disable.
//					for (int i=0; i < 10; i++)
//					{
//						Thread.sleep(500);
//
//						if (bluetooth.getState() == BluetoothAdapter.STATE_OFF)
//							break;
//					}
//
//					Thread.sleep(1000);
//				}
				
				//
				// Step 2: Check Bluetooth is enabled.
				//
				if (!bluetooth.isEnabled())
				{
					// Update progress.
					publishProgress(msgTurningOn);
					
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
	
				// Still not enabled?
				if (!bluetooth.isEnabled())
				{
					publishError(msgNotEnabled);
					return;
				}				
				
				//
				// Step 3: Connect to device.
				//
				publishProgress(msgConnecting);
				
				// This method used reflection, but proves more reliable 
				// than the createRfcommSocketToServiceRecord public API!
				Method m = device.getClass().getMethod("createInsecureRfcommSocket", new Class[] { Integer.TYPE });
				socket = (BluetoothSocket) m.invoke(device, new Object[] { Integer.valueOf(1) });

				// Public API method.
				//socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				
				try
				{
					socket.connect();
					Thread.sleep(200);
				}
				catch (Exception e)
				{
					ConnectionLost();
					publishError(msgCantConnect);
					return;
				}
				
				// Start Connected thread.
				Connected(socket);
			}
			catch (Exception e1)
			{
				ConnectionLost();

				// Log exception
				if (D)
				{
					Log.d(TAG, "Exception", e1);
				}
				
				try
				{
					// Try closing the socket.
					if (socket != null)
					{
						socket.close();
					}
				}
				catch (IOException e2)
				{
					// Log exception
					if (D)
					{
						Log.d(TAG, "Exception", e2);
					}
				}
			}
		}
	}
	
	//
	// Connected Thread
	//	
	class ConnectedThread extends Thread
	{
		BluetoothSocket socket;
		InputStream inStream;
		OutputStream outStream;
		
		public ConnectedThread(BluetoothSocket socket)
		{
			try
			{
				this.socket = socket;
				
				inStream = socket.getInputStream();
				outStream = socket.getOutputStream();
			}
			catch (IOException e)
			{
				if (D)
				{
					Log.d(TAG, "Exception in ConnectedThread constructor", e);
				}
			}
		}
		
		public void run()
		{
			int bytes;
			byte[] buffer;
			
			while (true)
			{
				try
				{
					// Read from the InputStream
					buffer = new byte[1024];
					bytes = inStream.read(buffer, 0, 1024);
					
					handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
				}
				catch (IOException e)
				{
					if (D)
					{
						Log.d(TAG, "Exception in ConnectedThread", e);
					}
					
					ConnectionLost();
					break;
				}
			}
		}
		
		public void write(byte[] buffer)
		{
			try
			{
				outStream.write(buffer);
				outStream.flush();
				
				handler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
			}
			catch (IOException e)
			{
				if (D)
				{
					Log.d(TAG, "Exception in ConnectedThread write", e);
				}
			}
		}
		
		public void close()
		{
			try
			{
				// Try closing the socket.
				if (socket != null)
				{
					socket.close();
				}
			}
			catch (IOException e)
			{
				// Log exception
				if (D)
				{
					Log.d(TAG, "Exception in ConnectedThread close", e);
				}
			}
		}
	}
}
