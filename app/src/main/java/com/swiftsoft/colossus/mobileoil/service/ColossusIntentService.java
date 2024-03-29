package com.swiftsoft.colossus.mobileoil.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.activeandroid.ActiveAndroid;
import com.swiftsoft.colossus.mobileoil.Active;
import com.swiftsoft.colossus.mobileoil.CrashReporter;
import com.swiftsoft.colossus.mobileoil.Utils;
import com.swiftsoft.colossus.mobileoil.database.model.dbDriver;
import com.swiftsoft.colossus.mobileoil.database.model.dbMessageIn;
import com.swiftsoft.colossus.mobileoil.database.model.dbMessageOut;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;
import com.swiftsoft.colossus.mobileoil.database.model.dbTrip;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrderLine;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicle;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleChecklist;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleChecklistSection;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleChecklistSectionItem;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleStock;
import com.swiftsoft.colossus.mobileoil.rest.IRestClient;
import com.swiftsoft.colossus.mobileoil.rest.RestClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ColossusIntentService extends IntentService
{
	class ValidationException extends Exception
	{
		private static final long serialVersionUID = 1L;

		public ValidationException(String msg)
		{
			super(msg);
		}
	}
	
	// Broadcasts generated by this class.
	public static final String BroadcastRegisterVehicleOK = "com.swiftsoft.colossus.mobileoil.registervehicleok";
	public static final String BroadcastRegisterVehicleNOK = "com.swiftsoft.colossus.mobileoil.registervehiclenok";
	public static final String BroadcastNewVehicles = "com.swiftsoft.colossus.mobileoil.newvehicles";
	public static final String BroadcastNewDrivers = "com.swiftsoft.colossus.mobileoil.newdrivers";
	public static final String BroadcastNewProducts = "com.swiftsoft.colossus.mobileoil.newproducts";
	public static final String BroadcastNewBrands = "com.swiftsoft.colossus.mobileoil.newbrands";
	public static final String BroadcastTripsChanged = "com.swiftsoft.colossus.mobileoil.tripschanged";
	public static final String BroadcastCommsDebug = "com.swiftsoft.colossus.mobileoil.commsdebug";
	
	public ColossusIntentService()
	{
		super("ColossusIntentService");
	}

	// Return outbound message queue length 
	static int queueSize = -1;
	public static int getQueueSize()
	{
		return queueSize;
	}

	private void sendDebugMessage(String message)
	{
		Intent intent = new Intent(BroadcastCommsDebug);

		if (message != null)
		{
			intent.putExtra("Message", message);
		}

		sendBroadcast(intent);
	}
	
	@Override
	protected void onHandleIntent(Intent intent)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("ColossusIntentService: onHandleIntent");

			// Update debug UI.
			sendDebugMessage("Session started");

			// Find Device.
			dbSetting device = dbSetting.FindByKey("DeviceNo");

			if (device == null)
			{
				// Update debug UI.
				sendDebugMessage("device is null");

				return;
			}
			
			// Find DeviceNo.
			int DeviceNo = device.IntValue;
	
			// Get parameters.
			Bundle bundle = intent.getExtras();
			
			if (bundle != null)
			{
				// Get parameters.
				String Type = bundle.getString("Type");
				String Content = bundle.getString("Content");
				
				// Write to database.
				dbMessageOut messageOut = new dbMessageOut();

				messageOut.DeviceNo = DeviceNo;
				messageOut.DateTime = Utils.getCurrentTime();
				messageOut.Type = Type;
				messageOut.Content = Content;
				messageOut.Guid = UUID.randomUUID().toString();

				messageOut.save();
			}
	
			// Check if any messages to go out.
			List<dbMessageOut> messagesOut = dbMessageOut.GetAll();
			
			// Update static QueueSize.
			queueSize = messagesOut.size();
			
			// Update debug UI.
			sendDebugMessage(null);

			// Check if any outgoing messages.
			if (messagesOut.size() == 0)
			{
				// No outgoing messages.
				exchangeMessages(DeviceNo, null);
			}
			else
			{
				// Send all queued messages.
				for (int i = 0; i < messagesOut.size(); i++)
				{
					dbMessageOut mout = messagesOut.get(i);

					// Create JSON input for WebService.
					JSONObject json = new JSONObject();
					json.put("DateTime", "/Date(" + mout.DateTime + ")/");
					json.put("Type", mout.Type);
					json.put("Content", mout.Content);
					json.put("Guid", mout.Guid);

					// Send/Receive messages.
					if (!exchangeMessages(DeviceNo, json))
					{
						// Update debug UI.
						sendDebugMessage("Send failed");

						break;
					}

					// Delete outbound message, if successfully sent.
					dbMessageOut.delete(dbMessageOut.class, mout.getId());
					
					// Update static QueueSize.
					--queueSize;
					
					// Update debug UI.
					sendDebugMessage("Send ok");
				}
			}
			
			// Process incoming messages.
			List<dbMessageIn> messagesIn = dbMessageIn.GetAll();

			for (int j = 0; j < messagesIn.size(); j++)
			{
				dbMessageIn min = messagesIn.get(j);

				// Process inbound message.
				processMessage(min);
			
				// Delete inbound message.
				dbMessageIn.delete(dbMessageIn.class, min.getId());
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private boolean exchangeMessages(int deviceNo, JSONObject bodyIn)
	{
		try
		{		
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("ColossusIntentService: ExchangeMessages");
			
			// Find URL of customer's WebService.
			String colossusURL = dbSetting.FindByKey("ColossusURL").StringValue;
			
			// Create RESTful client.
			IRestClient client = new RestClient(colossusURL + "/MessageExchange");
			
			// JSON header.
			client.addHeader("Content-type", "application/json");
			
			// JSON body.
			JSONObject jsonIn = new JSONObject();
			jsonIn.put("DeviceNo", deviceNo);

			if (bodyIn != null)
			{
				jsonIn.put("Body", bodyIn);
			}

			client.addBody(jsonIn.toString());

			try
			{
				// Call Mobile WebService.
			    client.execute(RestClient.RequestMethod.POST);
			} 
			catch (Exception e1)
			{
				// Update debug UI.
				sendDebugMessage(e1.getMessage());

				CrashReporter.logHandledException(e1);
			}
	
			int rcode = client.getResponseCode();

			if (rcode == 200)
			{
				// Store incoming messages.
				String response = client.getResponse();
				
				if (response.length() > 0)
				{
					// Parse response.
					JSONObject jsonOut = new JSONObject(response);

					if (jsonOut.getBoolean("MoreMessages"))
					{
						// Queue another call to this service to get next message.
						Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);
						startService(i);
					}
					
					if (!jsonOut.getString("Body").equals("null"))
					{
						JSONObject jsonBody = new JSONObject(jsonOut.getString("Body"));
	
						// Store in Database.
				        dbMessageIn min = new dbMessageIn();
				        
				        // Check if message with guid already exists.
				        
				        min.MessageID = jsonBody.getInt("MessageID");
				        min.DateTime = getTime(jsonBody.getString("DateTime"));
				        min.Type = jsonBody.getString("Type");
				        min.Content = jsonBody.getString("Content");
				        min.Guid = jsonBody.getString("Guid");
				        min.save();
				        
				        // Confirm message received.
						IRestClient client2 = new RestClient(colossusURL + "/MessageReceived");
						
						// JSON header.
						client2.addHeader("Content-type", "application/json");
						
						// JSON body.
						client2.addBody(Integer.toString(min.MessageID));

						try
						{
							// Call Mobile WebService.
						    client2.execute(RestClient.RequestMethod.POST);
						} 
						catch (Exception e2)
						{
							CrashReporter.logHandledException(e2);
						}
				
						int rcode2 = client2.getResponseCode();

						if (rcode2 != 200)
						{
							//CmLog.writeln("ColossusIntentService.ExchangeMessages: MessageReceived returned code " + rcode2);
							return false;
						}
					}
				}
				
				// Message sent successfully.
				return true;
			}
		}
		catch (Exception e3)
		{
			// Update debug UI.
			sendDebugMessage(e3.getMessage());

			CrashReporter.logHandledException(e3);
		}
		
		return false;
	}
	
	private boolean processMessage(dbMessageIn messageIn)
	{
		try
		{
			if (messageIn.Type.equals("Vehicles"))
			{
				processVehicles(messageIn.Content);
			}
			
			if (messageIn.Type.equals("Drivers"))
			{
				processDrivers(messageIn.Content);
			}
			
			if (messageIn.Type.equals("Products"))
			{
				processProducts(messageIn.Content);
			}
			
			if (messageIn.Type.equals("Brands"))
			{
				processBrands(messageIn.Content);
			}
			
			if (messageIn.Type.equals("Register_Vehicle_OK"))
			{
				processRegisterVehicleOK(messageIn.Content);
			}
			
			if (messageIn.Type.equals("Register_Vehicle_NOK"))
			{
				processRegisterVehicleNOK(messageIn.Content);
			}
			
			if (messageIn.Type.equals("Trip_Add"))
			{
				processTripAdd(messageIn.Content);
			}

			if (messageIn.Type.equals("Trip_Remove"))
			{
				processTripRemove(messageIn.Content);
			}
			
			if (messageIn.Type.equals("Trip_Reverse"))
			{
				processTripReverse(messageIn.Content);
			}
			
			if (messageIn.Type.equals("Order_Add"))
			{
				processOrderAdd(messageIn.Content);
			}
			
			if (messageIn.Type.equals("Order_Remove"))
			{
				processOrderRemove(messageIn.Content);
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);

			return false;
		}
		
		return true;
	}

	private void processVehicles(String content)
	{
		try
		{
			JSONArray jsonArray = new JSONArray(content);
			
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject json = jsonArray.getJSONObject(i);
				
				int colossusID = json.getInt("Id");
				
				// Try to find existing Vehicle.
				dbVehicle vehicle = dbVehicle.FindByColossusID(colossusID);
				if (vehicle == null)
				{
					// Create new vehicle.
					vehicle = new dbVehicle();
				}
				
				vehicle.ColossusID = colossusID;
				vehicle.No = json.getInt("No");
				vehicle.Reg = json.getString("RegNo");
				vehicle.StockByCompartment = json.getBoolean("StockByCompartment");
				vehicle.C0_Capacity = json.getInt("C0");
				vehicle.C1_Capacity = json.getInt("C1");
				vehicle.C2_Capacity = json.getInt("C2");
				vehicle.C3_Capacity = json.getInt("C3");
				vehicle.C4_Capacity = json.getInt("C4");
				vehicle.C5_Capacity = json.getInt("C5");
				vehicle.C6_Capacity = json.getInt("C6");
				vehicle.C7_Capacity = json.getInt("C7");
				vehicle.C8_Capacity = json.getInt("C8");
				vehicle.C9_Capacity = json.getInt("C9");
				vehicle.save();
				
				dbVehicleChecklist v1cl = new dbVehicleChecklist();
				v1cl.Vehicle = vehicle;
				v1cl.Version = 1;
				v1cl.save();
				
				dbVehicleChecklistSection v1cls1 = new dbVehicleChecklistSection();
				v1cls1.VehicleChecklist = v1cl;
				v1cls1.Title = "Documents";
				v1cls1.save();
				
				dbVehicleChecklistSectionItem v1cls1i1 = new dbVehicleChecklistSectionItem();
				v1cls1i1.VehicleChecklistSection = v1cls1;
				v1cls1i1.Title = "Vehicle insurance";
				v1cls1i1.save();
				
				dbVehicleChecklistSectionItem v1cls1i2 = new dbVehicleChecklistSectionItem();
				v1cls1i2.VehicleChecklistSection = v1cls1;
				v1cls1i2.Title = "Vehicle registration card";
				v1cls1i2.save();
				
				dbVehicleChecklistSectionItem v1cls1i3 = new dbVehicleChecklistSectionItem();
				v1cls1i3.VehicleChecklistSection = v1cls1;
				v1cls1i3.Title = "Drivers license";
				v1cls1i3.Summary = "With heavy vehicle permission";
				v1cls1i3.save();
				
				dbVehicleChecklistSection v1cls2 = new dbVehicleChecklistSection();
				v1cls2.VehicleChecklist = v1cl;
				v1cls2.Title = "Safety equipment";
				v1cls2.save();
				
				dbVehicleChecklistSectionItem v1cls2i1 = new dbVehicleChecklistSectionItem();
				v1cls2i1.VehicleChecklistSection = v1cls2;
				v1cls2i1.Title = "Fire extinguisher";
				v1cls2i1.Summary = "Fully charged with valid expiry date";
				v1cls2i1.save();
				
				dbVehicleChecklistSectionItem v1cls2i2 = new dbVehicleChecklistSectionItem();
				v1cls2i2.VehicleChecklistSection = v1cls2;
				v1cls2i2.Title = "First aid kit";
				v1cls2i2.Summary = "Valid expiry dates";
				v1cls2i2.save();
				
				dbVehicleChecklistSectionItem v1cls2i3 = new dbVehicleChecklistSectionItem();
				v1cls2i3.VehicleChecklistSection = v1cls2;
				v1cls2i3.Title = "Personal Protection Equipment";
				v1cls2i3.Summary = "Helmet, safety shoes, safety jacket, safety goggles and hand gloves";
				v1cls2i3.save();
			}
			
			// Broadcast intent to notify UI.
			Intent intent = new Intent(BroadcastNewVehicles);
			sendBroadcast(intent);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private void processDrivers(String content)
	{
		try
		{
			JSONArray jsonArray = new JSONArray(content);
			
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject json = jsonArray.getJSONObject(i);
				
				int colossusID = json.getInt("Id");
				
				// Try to find existing Driver.
				dbDriver driver = dbDriver.FindByColossusID(colossusID);
				if (driver == null)
				{
					// Create new driver.
					driver = new dbDriver();
				}
				
				driver.ColossusID = colossusID;
				driver.No = json.getInt("No");
				driver.Name = json.getString("Name");
				driver.PIN = json.getInt("Pin");
				driver.save();
			}
			
			// Broadcast intent to notify UI.
			Intent intent = new Intent(BroadcastNewDrivers);
			sendBroadcast(intent);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private void processProducts(String content)
	{
		try
		{
			JSONArray jsonArray = new JSONArray(content);
			
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject json = jsonArray.getJSONObject(i);
				
				int colossusID = json.getInt("Id");
				
				// Try to find existing Product.
				dbProduct product = dbProduct.FindByColossusID(colossusID);
				if (product == null)
				{
					// Create new product.
					product = new dbProduct();
				}
				
				product.ColossusID = colossusID;
				product.Code = json.getString("Code");
				product.Desc = json.getString("Description");
				product.Colour = json.getInt("Colour");
				product.MobileOil = json.getInt("MobileOil");
				product.save();
			}

			// Broadcast intent to notify UI.
			Intent intent = new Intent(BroadcastNewProducts);
			sendBroadcast(intent);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private void processBrands(String content)
	{
		try
		{
			// Delete existing Brands.
			List<dbSetting> settings = dbSetting.GetAllBrandLogos();
			for (dbSetting setting : settings)
				setting.delete();
			
			JSONArray jsonArray = new JSONArray(content);
			
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject json = jsonArray.getJSONObject(i);
				
				int colossusID = json.getInt("Id");

				// Convert logo to byte array.
				JSONArray logo = json.getJSONArray("Logo");
				byte[] logoData = new byte[logo.length()];
				for (int j = 0; j < logo.length(); j++)
					logoData[j] = (byte) logo.getInt(j);
				
				// Create/Update setting.
				String key = "Brand Logo:" + colossusID;
				dbSetting setting = dbSetting.FindByKeyOrCreate(key);
				setting.Key = key;
				setting.IntValue = 0;				// means not sent to printer.
				setting.BinaryValue = logoData;
				setting.save();
			}
			
			// Broadcast intent to notify UI.
			Intent intent = new Intent(BroadcastNewBrands);
			sendBroadcast(intent);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	private void processRegisterVehicleOK(String content)
	{
		int vehicleID = -1;
		
		try
		{
			JSONObject json = new JSONObject(content);

			// Find vehicle.
			vehicleID = json.getInt("VehicleID");
			dbVehicle vehicle = dbVehicle.FindByColossusID(vehicleID);

			// Process stock onboard.
			JSONArray stockList = json.getJSONArray("Stock");
			for (int i = 0; i < stockList.length(); i++)
			{
				JSONObject stock = stockList.getJSONObject(i);
				int productID = stock.getInt("ProductID");
				int compartment = stock.getInt("Compartment");
				int quantity = stock.getInt("Quantity");
				
				// Find product.
				dbProduct product = dbProduct.FindByColossusID(productID);
				
				if (compartment == -1)
				{
					// Stock by product.
					dbVehicleStock vs = dbVehicleStock.FindOrCreateByVehicleProduct(vehicle, product);
					vs.CurrentStock = quantity;
					vs.save();
				}
				else
				{
					// Stock by compartment.
					dbVehicleStock vs = dbVehicleStock.FindOrCreateByVehicleCompartment(vehicle, compartment);
					vs.Product = product;
					vs.CurrentStock = quantity;
					vs.save();
				}				
			}

			// Broadcast intent to notify UI.
			Intent intent = new Intent(BroadcastRegisterVehicleOK);
			intent.putExtra("VehicleID", vehicleID);
			sendBroadcast(intent);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
			
			// Broadcast intent to notify UI of error.
			Intent intent = new Intent(BroadcastRegisterVehicleNOK);
			intent.putExtra("VehicleID", vehicleID);
			intent.putExtra("Error", e.getMessage());
			sendBroadcast(intent);
		}
	}
	
	private void processRegisterVehicleNOK(String content)
	{
		try
		{
			JSONObject json = new JSONObject(content);

			// Broadcast intent to notify UI of error.
			Intent intent = new Intent(BroadcastRegisterVehicleNOK);
			intent.putExtra("VehicleID", json.getInt("VehicleID"));
			intent.putExtra("Error", json.getString("Error"));
			sendBroadcast(intent);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	private void processTripAdd(String content)
	{
		int tripID = -1;
		JSONArray orderIDs = new JSONArray();
		String resultType = "Trip_Add_NOK";
		String resultMessage = "";
		
		try
		{
			// Begin transaction.
			ActiveAndroid.beginTransaction();

			// De-serialise content.
			JSONObject json = new JSONObject(content);

			// Find Trip ID.
			tripID = json.getInt("Id");
			
			// Check Trip does not already exist.
			dbTrip trip = dbTrip.FindByColossusID(tripID);
			if (trip != null)
			{
				String msg = String.format("Trip ID %d already exists", tripID);
				throw new ValidationException(msg);
			}
			
			// Create new Trip.
			trip = new dbTrip();
			trip.Delivering = false;
			trip.Delivered = false;
			trip.ColossusID = tripID;
			trip.No = json.getInt("No");
			trip.Date = getTime(json.getString("Date"));
			trip.Vehicle = dbVehicle.FindByColossusID(json.getInt("VehicleID"));
			trip.Driver = dbDriver.FindByColossusID(json.getInt("DriverID"));
			trip.LoadingNotes = json.getString("LoadingNotes");
			trip.save();
		
			JSONArray orders = new JSONArray(json.getString("Orders"));
			for (int i = 0; i < orders.length(); i++)
			{
				JSONObject order = orders.getJSONObject(i);
				
				// Find Order ID.
				int orderID = order.getInt("Id");
				
				// Check Order does not already exist.
				dbTripOrder tripOrder = dbTripOrder.FindByColossusID(orderID);
				if (tripOrder != null)
				{
					String msg = String.format("Order ID %d already exists", orderID);
					throw new ValidationException(msg);
				}
				
				// Create new TripOrder.
				tripOrder = new dbTripOrder();
				tripOrder.Trip = trip;
				tripOrder.Delivering = false;
				tripOrder.Delivered = false;
				tripOrder.ColossusID = orderID;
				tripOrder.DeliveryOrder = order.getInt("DeliveryOrder");
				tripOrder.InvoiceNo = order.getString("InvoiceNo");
				tripOrder.BrandID = order.getInt("BrandID");
				tripOrder.CustomerCode = order.getString("CustomerCode");
				tripOrder.CustomerName = order.getString("CustomerName");
				tripOrder.CustomerAddress = order.getString("CustomerAddress");
				tripOrder.CustomerPostcode = order.getString("CustomerPostCode");
				tripOrder.CustomerType = order.getString("CustomerType");
				tripOrder.OrderNumber = order.getString("OrderNumber").trim();
                tripOrder.HidePrices = order.getBoolean("HidePrices");
				tripOrder.DeliveryName = order.getString("DeliveryName");
				tripOrder.DeliveryAddress = order.getString("DeliveryAddress");
				tripOrder.DeliveryPostcode = order.getString("DeliveryPostCode");
				tripOrder.PhoneNos = order.getString("PhoneNos");
				tripOrder.RequiredBy = order.getString("RequiredBy");
				tripOrder.Terms = order.getString("Terms");
				tripOrder.DueDate = getTime(order.getString("DueDate"));
				tripOrder.Notes = order.getString("Notes");
				tripOrder.setPrepaidAmount(new BigDecimal(order.getDouble("PrepaidAmount")));
				tripOrder.CodPoint = order.getInt("CodPoint");
				tripOrder.CodType = order.getInt("CodType");
				tripOrder.setCodAmount(new BigDecimal(order.getDouble("CodAmount")));
				tripOrder.save();
				
				JSONArray lines = new JSONArray(order.getString("Lines"));

				for (int j = 0; j < lines.length(); j++)
				{
					JSONObject line = lines.getJSONObject(j);
					
					// Find OrderLine ID.
					int orderLineID = line.getInt("Id");
					
					// Check OrderLine does not already exist.
					dbTripOrderLine tripOrderLine = dbTripOrderLine.FindByColossusID(orderLineID);
					if (tripOrderLine != null)
					{
						String msg = String.format("OrderLine ID %d already exists", orderLineID);
						throw new ValidationException(msg);
					}
					
					// Find product.
					int productID = line.getInt("ProductId");
					dbProduct product = dbProduct.FindByColossusID(productID);

					if (product == null)
					{
						String msg = String.format("OrderLine ID %d invalid product ID %d", orderLineID, productID);
						throw new ValidationException(msg);
					}

					// Create new TripOrderLine.
					tripOrderLine = new dbTripOrderLine();
					tripOrderLine.TripOrder = tripOrder;
					tripOrderLine.Delivered = false;
					tripOrderLine.ColossusID = orderLineID;
					tripOrderLine.Product = product;
					tripOrderLine.OrderedQty = line.getInt("OrderedQuantity");
					tripOrderLine.setOrderedPrice(new BigDecimal(line.getDouble("Price")));
					tripOrderLine.setDeliveredPrice(BigDecimal.ZERO);
					tripOrderLine.setSurcharge(new BigDecimal(line.getDouble("Surcharge")));
					tripOrderLine.SurchargePerUOM = line.getBoolean("SurchargePerUOM");
					tripOrderLine.setRatio(new BigDecimal(line.getDouble("Ratio")));
					tripOrderLine.setVatPerc1(new BigDecimal(line.getDouble("VatPerc1")));
					tripOrderLine.setVatPerc2(new BigDecimal(line.getDouble("VatPerc2")));
					tripOrderLine.VatPerc2Above = line.getInt("VatPerc2Above");
					tripOrderLine.save();
				}
				
				// Success - add ID to list of orderIDs for confirmation message.
				orderIDs.put(tripOrder.ColossusID);
			}
			
			// Mark transaction as successful.
			ActiveAndroid.setTransactionSuccessful();

			resultType = "Trip_Add_OK";
		}
		catch (ValidationException e)
		{
			resultMessage = e.getMessage();
		}
		catch (Exception e)
		{
			resultMessage = "Exception " + e.getMessage();

			CrashReporter.logHandledException(e);
		}
		finally
		{
			// End the transaction.
			ActiveAndroid.endTransaction();
		}
		
		try
		{
			// Broadcast intent to notify UI.
			Intent intent = new Intent(BroadcastTripsChanged);
			sendBroadcast(intent);
			
			// Create content.
			JSONObject json2 = new JSONObject();
			json2.put("TripID", tripID);
			json2.put("OrderIDs", orderIDs);
			json2.put("Message", resultMessage);

			// Call ColossusIntentService.
			Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);
			i.putExtra("Type", resultType);
			i.putExtra("Content", json2.toString());
			startService(i);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private void processTripRemove(String content)
	{
		int tripID = -1;
		JSONArray orderIDs = new JSONArray();
		String resultType = "Trip_Remove_NOK";
		String resultMessage = "";

		try
		{
			// Begin transaction.
			ActiveAndroid.beginTransaction();

			// De-serialise content.
			JSONObject json = new JSONObject(content);

			// Find Trip ID.
			tripID = json.getInt("Id");
			
			// Try to find existing Trip.
			dbTrip trip = dbTrip.FindByColossusID(tripID);
			if (trip == null)
			{
				String msg = String.format("Trip ID %d does not exist", tripID);
				throw new ValidationException(msg);
			}

			// Check if trip is delivering.
			if (trip.Delivering)
			{
				String msg = String.format("Trip ID %d is delivering", tripID);
				throw new ValidationException(msg);
			}
			
			// Check if trip is already delivered.
			if (trip.Delivered)
			{
				String msg = String.format("Trip ID %d is delivered", tripID);
				throw new ValidationException(msg);
			}

			// Check if trip is the active trip. (The trip.Delivering check should also be true)
			if (Active.trip != null)
			{
				if (Active.trip.ColossusID == trip.ColossusID)
				{
					String msg = String.format("Trip ID %d is currently active", tripID);
					throw new ValidationException(msg);
				}
			}
			
			// Delete the trip, orders & orderlines.
			for (dbTripOrder order : trip.GetOrders())
			{				
				long orderID = order.getId();
				
				// Check if order is delivering.
				if (order.Delivering)
				{
					String msg = String.format("Trip ID %d Order ID %d is delivering", tripID, orderID);
					throw new ValidationException(msg);
				}
				
				// Check if order is already delivered.
				if (order.Delivered)
				{
					String msg = String.format("Trip ID %d Order ID %d is delivered", tripID, orderID);
					throw new ValidationException(msg);
				}
				
				for (dbTripOrderLine line : order.GetTripOrderLines())
				{
					// Delete order line.
					line.delete();
				}
				
				// Delete order.
				order.delete();

				// Success - add ID to list of orderIDs for confirmation message.
				orderIDs.put(order.ColossusID);
			}
			
			// Delete trip.
			trip.delete();

			// Mark transaction as successful.
			ActiveAndroid.setTransactionSuccessful();
			
			resultType = "Trip_Remove_OK";
		}
		catch (ValidationException e)
		{
			resultMessage = e.getMessage();
		}
		catch (Exception e)
		{
			resultMessage = "Exception " + e.getMessage();

			CrashReporter.logHandledException(e);
		}
		finally
		{
			// End the transaction.
			ActiveAndroid.endTransaction();
		}
		
		try
		{
			// Broadcast intent to notify UI.
			Intent intent = new Intent(BroadcastTripsChanged);
			sendBroadcast(intent);
			
			// Create content.
			JSONObject json2 = new JSONObject();
			json2.put("TripID", tripID);
			json2.put("OrderIDs", orderIDs);
			json2.put("Message", resultMessage);

			// Call ColossusIntentService.
			Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);
			i.putExtra("Type", resultType);
			i.putExtra("Content", json2.toString());
			startService(i);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private void processTripReverse(String content)
	{
		int tripID = -1;
		String resultType = "Trip_Reverse_NOK";
		String resultMessage = "";

		try
		{
			// Begin transaction.
			ActiveAndroid.beginTransaction();

			// De-serialise content.
			JSONObject json = new JSONObject(content);

			// Find Trip ID.
			tripID = json.getInt("Id");
			
			// Try to find existing Trip.
			dbTrip trip = dbTrip.FindByColossusID(tripID);
			if (trip == null)
			{
				String msg = String.format("Trip ID %d does not exist", tripID);
				throw new ValidationException(msg);
			}

			// Check if trip is delivering.
			if (trip.Delivering)
			{
				String msg = String.format("Trip ID %d is delivering", tripID);
				throw new ValidationException(msg);
			}
			
			trip.reverseDelivered();

			// Mark transaction as successful.
			ActiveAndroid.setTransactionSuccessful();
			
			resultType = "Trip_Reverse_OK";
		}
		catch (ValidationException e)
		{
			resultMessage = e.getMessage();
		}
		catch (Exception e)
		{
			resultMessage = "Exception " + e.getMessage();

			CrashReporter.logHandledException(e);
		}
		finally
		{
			// End the transaction.
			ActiveAndroid.endTransaction();
		}
		
		try
		{
			// Broadcast intent to notify UI.
			Intent intent = new Intent(BroadcastTripsChanged);
			sendBroadcast(intent);
			
			// Create content.
			JSONObject json2 = new JSONObject();
			json2.put("TripID", tripID);
			json2.put("Message", resultMessage);

			// Call ColossusIntentService.
			Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);
			i.putExtra("Type", resultType);
			i.putExtra("Content", json2.toString());
			startService(i);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private void processOrderAdd(String content)
	{
		int tripID = -1;
		int operatorID = -1;
		JSONArray orderIDs = new JSONArray();
		String resultType = "Order_Add_NOK";
		String resultMessage = "";
		
		try
		{
			// Begin transaction.
			ActiveAndroid.beginTransaction();

			// De-serialise content.
			JSONObject json = new JSONObject(content);

			// Find parameters.
			tripID = json.getInt("Id");
			operatorID = json.getInt("OperatorID");
			
			// Try to find existing Trip.
			dbTrip trip = dbTrip.FindByColossusID(tripID);
			if (trip == null)
			{
				String msg = String.format("Trip ID %d does not exist", tripID);
				throw new ValidationException(msg);
			}
			
			JSONArray orders = new JSONArray(json.getString("Orders"));
			for (int i = 0; i < orders.length(); i++)
			{
				JSONObject order = orders.getJSONObject(i);
				
				// Find Order ID.
				int orderID = order.getInt("Id");
				
				// Check Order does not already exist.
				dbTripOrder tripOrder = dbTripOrder.FindByColossusID(orderID);
				if (tripOrder != null)
				{
					String msg = String.format("Order ID %d already exists", orderID);
					throw new ValidationException(msg);
				}
				
				// Create new TripOrder.
				tripOrder = new dbTripOrder();
				tripOrder.Trip = trip;
				tripOrder.Delivering = false;
				tripOrder.Delivered = false;
				tripOrder.ColossusID = orderID;
				tripOrder.DeliveryOrder = order.getInt("DeliveryOrder");
				tripOrder.InvoiceNo = order.getString("InvoiceNo");
				tripOrder.BrandID = order.getInt("BrandID");
				tripOrder.CustomerCode = order.getString("CustomerCode");
				tripOrder.CustomerName = order.getString("CustomerName");
				tripOrder.CustomerAddress = order.getString("CustomerAddress");
				tripOrder.CustomerPostcode = order.getString("CustomerPostCode");
				tripOrder.DeliveryName = order.getString("DeliveryName");
				tripOrder.DeliveryAddress = order.getString("DeliveryAddress");
				tripOrder.DeliveryPostcode = order.getString("DeliveryPostCode");
				tripOrder.PhoneNos = order.getString("PhoneNos");
				tripOrder.RequiredBy = order.getString("RequiredBy");
				tripOrder.Terms = order.getString("Terms");
				tripOrder.DueDate = getTime(order.getString("DueDate"));
				tripOrder.Notes = order.getString("Notes");
				tripOrder.setPrepaidAmount(new BigDecimal(order.getDouble("PrepaidAmount")));
				tripOrder.CodPoint = order.getInt("CodPoint");
				tripOrder.CodType = order.getInt("CodType");
				tripOrder.setCodAmount(new BigDecimal(order.getDouble("CodAmount")));
				tripOrder.save();
				
				JSONArray lines = new JSONArray(order.getString("Lines"));
				for (int j = 0; j < lines.length(); j++)
				{
					JSONObject line = lines.getJSONObject(j);
					
					// Find OrderLine ID.
					int orderLineID = line.getInt("Id");
					
					// Check OrderLine does not already exist.
					dbTripOrderLine tripOrderLine = dbTripOrderLine.FindByColossusID(orderLineID);
					if (tripOrderLine != null)
					{
						String msg = String.format("OrderLine ID %d already exists", orderLineID);
						throw new ValidationException(msg);
					}
					
					// Find product.
					int productID = line.getInt("ProductId");
					dbProduct product = dbProduct.FindByColossusID(productID);

					if (product == null)
					{
						String msg = String.format("OrderLine ID %d invalid product ID %d", orderLineID, productID);
						throw new ValidationException(msg);
					}

					// Create new TripOrderLine.
					tripOrderLine = new dbTripOrderLine();
					tripOrderLine.TripOrder = tripOrder;
					tripOrderLine.ColossusID = orderLineID;
					tripOrderLine.Product = product;
					tripOrderLine.OrderedQty = line.getInt("OrderedQuantity");
					tripOrderLine.setOrderedPrice(new BigDecimal(line.getDouble("Price")));
					tripOrderLine.setDeliveredPrice(BigDecimal.ZERO);
					tripOrderLine.setSurcharge(new BigDecimal(line.getDouble("Surcharge")));
					tripOrderLine.SurchargePerUOM = line.getBoolean("SurchargePerUOM");
					tripOrderLine.setRatio(new BigDecimal(line.getDouble("Ratio")));
					tripOrderLine.setVatPerc1(new BigDecimal(line.getDouble("VatPerc1")));
					tripOrderLine.setVatPerc2(new BigDecimal(line.getDouble("VatPerc2")));
					tripOrderLine.VatPerc2Above = line.getInt("VatPerc2Above");
					tripOrderLine.save();
				}
				
				// Success - add ID to list of orderIDs for confirmation message.
				orderIDs.put(tripOrder.ColossusID);
			}
			
			// Mark transaction as successful.
			ActiveAndroid.setTransactionSuccessful();

			resultType = "Order_Add_OK";
		}
		catch (ValidationException e)
		{
			resultMessage = e.getMessage();
		}
		catch (Exception e)
		{
			resultMessage = "Exception " + e.getMessage();

			CrashReporter.logHandledException(e);
		}
		finally
		{
			// End the transaction.
			ActiveAndroid.endTransaction();
		}
		
		try
		{
			// Broadcast intent to notify UI.
			Intent intent = new Intent(BroadcastTripsChanged);
			sendBroadcast(intent);
			
			// Create content.
			JSONObject json2 = new JSONObject();
			json2.put("TripID", tripID);
			json2.put("OrderIDs", orderIDs);
			json2.put("OperatorID", operatorID);
			json2.put("Message", resultMessage);

			// Call ColossusIntentService.
			Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);
			i.putExtra("Type", resultType);
			i.putExtra("Content", json2.toString());
			startService(i);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private void processOrderRemove(String content)
	{
		int tripID = -1;
		int orderID = -1;
		int operatorID = -1;
		String resultType = "Order_Remove_NOK";
		String resultMessage = "";

		try
		{
			// Begin transaction.
			ActiveAndroid.beginTransaction();

			// De-serialise content.
			JSONObject json = new JSONObject(content);

			// Find parameters.
			tripID = json.getInt("TripID");
			orderID = json.getInt("OrderID");
			operatorID = json.getInt("OperatorID");
			
			// Try to find existing Trip.
			dbTrip trip = dbTrip.FindByColossusID(tripID);
			if (trip == null)
			{
				String msg = String.format("Trip ID %d does not exist", tripID);
				throw new ValidationException(msg);
			}

			// Check if trip is already delivered.
			if (trip.Delivered)
			{
				String msg = String.format("Trip ID %d is delivered", tripID);
				throw new ValidationException(msg);
			}

			// Try to find existing Order.
			dbTripOrder order = dbTripOrder.FindByColossusID(orderID);
			if (order == null)
			{
				String msg = String.format("Order ID %d does not exist", orderID);
				throw new ValidationException(msg);
			}

			// Check if order is delivering.
			if (order.Delivering)
			{
				String msg = String.format("Trip ID %d Order ID %d is delivering", tripID, orderID);
				throw new ValidationException(msg);
			}
			
			// Check if order is already delivered.
			if (order.Delivered)
			{
				String msg = String.format("Trip ID %d Order ID %d is delivered", tripID, orderID);
				throw new ValidationException(msg);
			}
			
			// Delete the orderlines.
			for (dbTripOrderLine line : order.GetTripOrderLines())
			{
				line.delete();
			}
			
			// Delete order.
			order.delete();
			
			// Mark transaction as successful.
			ActiveAndroid.setTransactionSuccessful();

			resultType = "Order_Remove_OK";
		}
		catch (ValidationException e)
		{
			resultMessage = e.getMessage();
		}
		catch (Exception e)
		{
			resultMessage = "Exception " + e.getMessage();

			CrashReporter.logHandledException(e);
		}
		finally
		{
			// End the transaction.
			ActiveAndroid.endTransaction();
		}
		
		try
		{
			// Broadcast intent to notify UI.
			Intent intent = new Intent(BroadcastTripsChanged);
			sendBroadcast(intent);
			
			// Create content.
			JSONObject json2 = new JSONObject();
			json2.put("TripID", tripID);
			json2.put("OrderID", orderID);
			json2.put("OperatorID", operatorID);
			json2.put("Message", resultMessage);

			// Call ColossusIntentService.
			Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);
			i.putExtra("Type", resultType);
			i.putExtra("Content", json2.toString());
			startService(i);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private long getTime(String date)
	{
		String t = date.substring(date.indexOf('(') + 1);
		
	    if (date.contains("+"))
		{
			t = t.substring(0, t.indexOf('+'));
		}
	    else if (date.contains("-"))
		{
			t = t.substring(0, t.indexOf('-'));
		}
	    else
		{
			t = t.substring(0, t.indexOf(')'));
		}
	    
	    return Long.parseLong(t);
	} 
}
