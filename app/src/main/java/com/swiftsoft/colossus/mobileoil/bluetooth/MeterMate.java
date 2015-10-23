package com.swiftsoft.colossus.mobileoil.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
//import com.swiftsoft.colossus.mobileoil.R;

public class MeterMate
{
	// Debugging
	static final String TAG = "MeterMate";
	static final boolean D = true;
	
	// Comms states.
	public static final int COMMS_CONNECTING = 0;
	public static final int COMMS_CONNECTED = 1;
	public static final int COMMS_DISCONNECTED = 2;
	
	// Message IDs.
	public static final int MESSAGE_COMMS_STATUS_CHANGED = 0;
	public static final int MESSAGE_PUMPING_STATUS_CHANGED = 1;

	// Flag to indicate MeterMate is in use - i.e. prevent two MeterMate UIs connecting.
	static boolean inUse = false;
	
	// Flag to indicate MeterMate comms is running.
	static boolean running = false;
	
	// UI Handler.
	static Handler handler;
	static Context context;

	// Bluetooth comms.
	static int STX = 0x02;
	static int ETX = 0x03;
	static int commsStatus = COMMS_DISCONNECTED;
	static String deviceAddress;
	static BluetoothSocket socket;
	static InputStream inStream;
	static OutputStream outStream;

	// Meter.
	static Boolean inDeliveryMode;
	static Boolean inPumpingMode;
	static Integer presetLitres;
	static Integer realtimeLitres;
	static Double temperature;
	
	// Ticket data.
	static boolean readTicket = false;
	static Integer ticketNo;
	static String ticketProductDesc;
	static String ticketStartTime;
	static String ticketFinishTime;
	static double ticketStartTotaliser;
	static double ticketEndTotaliser;
	static double ticketGrossVolume;
	static double ticketNetVolume;
	static double ticketTemperature;
	static boolean ticketAt15Degrees;
	
	// Demo simulator.
	static boolean demoMode = false;
	static boolean demoPumping = false;

    // Private member holding Bluetooth messages sent/received
	static ArrayList<BluetoothMessage> btMessages;

	static boolean logBluetoothData = false;
	
	// Public comms status

	public static boolean getLogBluetoothData()
	{
		return logBluetoothData;
	}

	public static void setLogBluetoothData(boolean logData)
	{
		logBluetoothData = logData;
	}

    public static ArrayList<BluetoothMessage> getMessages()
    {
        return btMessages;
    }
	
	public static synchronized int getCommsStatus()
	{
		return commsStatus;
	}
	
	static synchronized void setCommsStatus(int value)
	{
		if (commsStatus != value)
		{
			if (D)
			{
				Log.d(TAG, "Comms status now " + Integer.toString(value));
			}
			
			commsStatus = value;
			handler.obtainMessage(MESSAGE_COMMS_STATUS_CHANGED).sendToTarget();
		}
	}

	// Public meter status
	
	public static synchronized String getInDeliveryMode()
	{
		return inDeliveryMode == null ? "" : inDeliveryMode ? "Yes" : "No";
	}
	
	static synchronized void setInDeliveryMode(boolean value)
	{
		if (inDeliveryMode == null || inDeliveryMode != value)
		{
			if (D)
			{
				Log.d(TAG, "InDeliveryMode now " + Boolean.toString(value));
			}
			
			inDeliveryMode = value;			
			handler.obtainMessage(MESSAGE_PUMPING_STATUS_CHANGED).sendToTarget();
		}
	}

	public static synchronized String getInPumpingMode()
	{
		return inPumpingMode == null ? "" : inPumpingMode ? "Yes" : "No";
	}
	
	static synchronized void setInPumpingMode(boolean value)
	{
		if (inPumpingMode == null || inPumpingMode != value)
		{
			if (D)
			{
				Log.d(TAG, "InPumpingMode now " + Boolean.toString(value));
			}
			
			inPumpingMode = value;
			handler.obtainMessage(MESSAGE_PUMPING_STATUS_CHANGED).sendToTarget();
		}
	}

	public static synchronized String getPresetLitres()
	{
		return presetLitres == null ? "" : presetLitres.toString();
	}
	
	static synchronized void setPresetLitres(int value)
	{
		if (presetLitres == null || presetLitres != value)
		{
			if (D)
			{
				Log.d(TAG, "PresetLitres now " + Integer.toString(value));
			}

			presetLitres = value;
			handler.obtainMessage(MESSAGE_PUMPING_STATUS_CHANGED).sendToTarget();
		}
	}

	public static synchronized String getRealtimeLitres()
	{
		return realtimeLitres == null ? "" : realtimeLitres.toString();
	}
	
	static synchronized void setRealtimeLitres(int value)
	{
		if (realtimeLitres == null || realtimeLitres != value)
		{
			if (D)
			{
				Log.d(TAG, "RealtimeLitres now " + Integer.toString(value));
			}

			realtimeLitres = value;
			handler.obtainMessage(MESSAGE_PUMPING_STATUS_CHANGED).sendToTarget();
		}
	}

	public static synchronized String getTemperature()
	{
		return temperature == null ? "" : temperature.toString();
	}
	
	static synchronized void setTemperature(double value)
	{
		if (temperature == null || temperature != value)
		{
			if (D)
			{
				Log.d(TAG, "Temperature now " + Double.toString(value));
			}
			
			temperature = value;
			handler.obtainMessage(MESSAGE_PUMPING_STATUS_CHANGED).sendToTarget();
		}
	}

	//
	// Ticket data.
	//
	
	public static boolean hasTicket()
	{
		return ticketNo != null;
	}
	
	public static synchronized String getTicketNo()
	{
		return ticketNo == null ? "" : ticketNo.toString();
	}

	public static synchronized String getTicketProductDesc()
	{
		return ticketNo == null ? "" : ticketProductDesc;
	}
	
	public static synchronized String getTicketStartTime()
	{
		return ticketNo == null ? "" : ticketStartTime;
	}
	
	public static synchronized String getTicketFinishTime()
	{
		return ticketNo == null ? "" : ticketFinishTime;
	}
	
	public static synchronized double getTicketStartTotaliser()
	{
		return ticketNo == null ? 0 : ticketStartTotaliser;
	}
	
	public static synchronized double getTicketEndTotaliser()
	{
		return ticketNo == null ? 0 : ticketEndTotaliser;
	}
	
	public static synchronized double getTicketGrossVolume()
	{
		return ticketNo == null ? 0 : ticketGrossVolume;
	}
	
	public static synchronized double getTicketNetVolume()
	{
		return ticketNo == null ? 0 : ticketNetVolume;
	}

	public static synchronized double getTicketTemperature()
	{
		return ticketNo == null ? 0 : ticketTemperature;
	}

	public static synchronized boolean getTicketAt15Degrees()
	{
		return ticketNo != null && ticketAt15Degrees;
	}
	

	static synchronized void setTicketDetails(
			int newTicketNo,
			String newProductDesc,
			String newStart,
			String newFinish,
			double newTotaliserStart,
			double newTotaliserEnd,
			double newGrossVolume,
			double newVolume,
			double newTemperature,
			boolean newAt15Degrees)
	{
		ticketNo = newTicketNo;
		ticketProductDesc = newProductDesc;
		ticketStartTime = newStart;
		ticketFinishTime = newFinish;
		ticketStartTotaliser = newTotaliserStart;
		ticketEndTotaliser = newTotaliserEnd;
		ticketGrossVolume = newGrossVolume;
		ticketNetVolume = newVolume;
		ticketTemperature = newTemperature;
		ticketAt15Degrees = newAt15Degrees;
		
		handler.obtainMessage(MESSAGE_PUMPING_STATUS_CHANGED).sendToTarget();
	}

	public static synchronized void demoStart()
	{
		// Start pumping!
		setInDeliveryMode(true);
		setInPumpingMode(true);
		setRealtimeLitres(0);
		setTemperature(15);

		demoPumping = true;
	}
	
	public static synchronized void demoStop()
	{
		// Stop pumping.
		setInDeliveryMode(false);
		setInPumpingMode(false);
		
		demoPumping = false;
	}
	
	// Public methods.
	public static void Initialise()
	{
		readTicket = false;
		ticketNo = null;
		ticketProductDesc = "";
		ticketStartTime = "";
		ticketFinishTime = "";
		ticketStartTotaliser = 0;
		ticketEndTotaliser = 0;
		ticketGrossVolume = 0;
		ticketNetVolume = 0;
		ticketTemperature = 0;
		ticketAt15Degrees = false;
	}	
	
	public static boolean Startup(Handler myHandler, Context myContext, String myDeviceAddress)
	{
		if (inUse)
		{
			return false;
		}

		// Store parameters.
		handler = myHandler;
		context = myContext;
		deviceAddress = myDeviceAddress;

		// Initialise meter variables.
		inDeliveryMode = null;
		inPumpingMode = null;
		presetLitres = null;
		realtimeLitres = null;
		temperature = null;

		// Initialise ticket variables.
		Initialise();

		// Start comms thread running.
		running = true;

		// MeterMate is now in use.
		inUse = true;
		
		return true;
	}
	
	public static void Shutdown()
	{
		// Stop comms thread running.
		running = false;
	}

	public static void setPreset(int litres)
	{
		if (D)
		{
			Log.d(TAG, "Setting preset to " + litres);
		}

		if (demoMode)
		{
			setPresetLitres(litres);
		}
		else
		{
			sendMessage("Sp," + Integer.toString(litres));
		}
	}
	
	public static void readTicket()
	{
		if (D)
		{
			Log.d(TAG, "Reading ticket");
		}
		
		readTicket = true;
	}
	
	// Background thread.
	static Runnable runnable = new Runnable()
	{
		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					// Determine if in demo simulator mode.
					demoMode = deviceAddress.equals("00:00:00:00:00");
					
					if (!running)
					{
						inUse = false;
					}
					else
					{
						// Set status to Connecting.
						setCommsStatus(COMMS_CONNECTING);

						if (!demoMode)
						{
							// Disable Bluetooth to flush old connections.
							Bluetooth.Disable();
							
							// Ensure Bluetooth is enabled.
							Bluetooth.Enable();

							// Try to connect to MeterMate.
							socket = Bluetooth.Connect(deviceAddress);

							// Get the in & out streams.
							inStream = socket.getInputStream();
							outStream = socket.getOutputStream();
						}
						
						Thread.sleep(250);
						
						// Set status to Connected.
						setCommsStatus(COMMS_CONNECTED);
						
						// Log status change.
						if (D)
							Log.d(TAG, "Comms connected");
						
						if (demoMode)
						{
							while (running)
							{
								if (demoPumping)
								{
									int litres = 0;
									
									// Find current litres delivered.
									try {litres = Integer.parseInt(getRealtimeLitres());}
									catch (Exception e) {}

									// Check if delivery is complete.
									if (presetLitres != null && litres >= presetLitres)
										demoStop();
									else
										setRealtimeLitres(litres + 1);
								}

								if (readTicket)
								{
									readTicket = false;

									// Simulate ticket read.
									setTicketDetails(
											1234, 
											"Oil", 
											"" + 0, 
											"" + realtimeLitres, 
											(double)1000, 
											(double)1000 + realtimeLitres, 
											(double)realtimeLitres,
											(double)realtimeLitres,
											temperature,
											true);
								}
								
								Thread.sleep(40);
							}
						}
						else
						{
                            // Create object for holding sent/received BT messages
                            btMessages =  new ArrayList<BluetoothMessage>();

							// Check MeterMate version is ok.
							sendMessage("Gv");
						
							// Await messages.
							String message = "";
							
							int keepAlive = 0;
							while (running)
							{
								int available;

								if ((available = inStream.available()) > 0)
								{
									// Read from the InputStream
									byte[] byteIn = new byte[available];

									if (inStream.read(byteIn, 0, available) > 0)
									{
										for (int i = 0; i < available; i++)
										{
											// STX - Start of Text
											if (byteIn[i] == STX)
											{
												// Start of message.
												message = "";
												continue;
											}
											
											// ETX - End of Text
											if (byteIn[i] == ETX)
											{
												// End of message.
												processMessage(message);

												continue;
											}
											
											// Add to message.
											message += (char)byteIn[i];
										}
									}
								}
								
								// Send some data to ensure 
								// Bluetooth link is still working.
								if (keepAlive++ > 40)
								{
									keepAlive = 0;

									sendMessage("NOP");
								}
								
								Thread.sleep(250);
							}
	
							// Close the socket.
							socket.close();
						}
						
						// Set status to Disconnected.
						setCommsStatus(COMMS_DISCONNECTED);

						// Log status change.
						if (D)
							Log.d(TAG, "Comms disconnected");

						if (!demoMode)
						{
							// Disable Bluetooth.
							Bluetooth.Disable();
						}
					}
					
					Thread.sleep(1000);
				}
				catch (Exception e)
				{
					// Log exception.
					if (D)
						Log.d(TAG, "Run exception " + e.getMessage());

					// Set status to Disconnected.
					setCommsStatus(COMMS_DISCONNECTED);
					
					// Clear references.
					socket = null;
					inStream = null;
					outStream = null;
				}
			}
		}
	};

	// Send message to MeterMate.
	static synchronized boolean sendMessage(String message)
	{
		try
		{
			if (getCommsStatus() == COMMS_CONNECTED)
			{
				byte[] buffer = message.getBytes("ISO-8859-1");
				
				// Log message.
				outStream.write(STX);
				outStream.write(buffer);
				outStream.write(ETX);
				outStream.flush();

                if (getLogBluetoothData())
                {
                    BluetoothMessage btMessage = new BluetoothMessage(BluetoothMessage.Direction.Outgoing, message, new Date().getTime());

                    btMessages.add(btMessage);
                }

				return true;
			}
		}
		catch (IOException e)
		{
			// Log exception.
			if (D)
			{
				Log.d(TAG, "SendMessage exception " + e.getMessage());
			}

			// Set status to Disconnected.
			setCommsStatus(COMMS_DISCONNECTED);
			
			// Clear references.
			socket = null;
			inStream = null;
			outStream = null;
		}
		
		return false;
	}

	// Process message from MeterMate.
	static void processMessage(String message)
	{
		try
		{
            if (getLogBluetoothData())
            {
                BluetoothMessage btMessage = new BluetoothMessage(BluetoothMessage.Direction.Incoming, message, new Date().getTime());

                btMessages.add(btMessage);
            }

			// Parse message into a JSON array.
			JSONObject json = new JSONObject(message);
			
			String command = json.getString("Command");
			int result = json.getInt("Result");

			if (D)
			{
				Log.d(TAG, "Received command: " + command + " Result: " + result);
			}

			// Get meter status results.
			if (command.equals("Gs") && result == 0)
			{
				setInDeliveryMode(json.getBoolean("InDeliveryMode"));
				setInPumpingMode(json.getBoolean("ProductFlowing"));
				
				// Read last ticket.
				if (readTicket)
				{
					sendMessage("Gtr,0");
				}
			}

			// Get preset litres results.
			if (command.equals("Gpl") && result == 0)
			{
				setPresetLitres(json.getInt("Litres"));
			}

			// Get realtime litres results.
			if (command.equals("Grl") && result == 0)
			{
				setRealtimeLitres(json.getInt("Litres"));
			}
			
			// Get temperature results.
			if (command.equals("Gt") && result == 0)
			{
				setTemperature(json.getDouble("Temp"));
			}
			
			// Get transaction results.
			if (command.equals("Gtr") && result == 0)
			{
				int at15MASK = 2;
				
				// Reset flag.
				readTicket = false;
				
				setTicketDetails(
						json.getInt("TicketNo"),
						json.getString("ProductDesc"),
						json.getString("Start"),
						json.getString("Finish"),
						json.getDouble("totaliserStart"),
						json.getDouble("totaliserEnd"),
						json.getDouble("grossVolume"),
						json.getDouble("volume"),
						json.getDouble("temperature"),
						(json.getInt("flags") & at15MASK) == at15MASK
						);
			}
		}
		catch (Exception e)
		{
			if (D)
			{
				Log.d(TAG, "ProcessMessage exception: " + e.getMessage());
			}
		}
	}

	static
	{
		// Start background thread.
		Thread thread = new Thread(runnable);
		thread.start();

        dbSetting dbLogBluetoothData = dbSetting.FindByKey("LogBluetoothData");

        if (dbLogBluetoothData != null)
        {
            logBluetoothData = dbLogBluetoothData.IntValue == 0 ? false : true;
        }
        else
        {
            logBluetoothData = false;
        }
	}
}