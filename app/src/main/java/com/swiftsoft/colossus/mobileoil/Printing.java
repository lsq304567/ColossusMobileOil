package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;

import com.swiftsoft.colossus.mobileoil.bluetooth.PrintingService;
import com.swiftsoft.colossus.mobileoil.database.model.dbDriver;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrderLine;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripStock;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicle;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleStock;
import com.swiftsoft.colossus.mobileoil.printingsystem.BitmapPrinter;
import com.swiftsoft.colossus.mobileoil.printingsystem.Printer;
import com.swiftsoft.colossus.mobileoil.printingsystem.Printer.Size;
import com.swiftsoft.colossus.mobileoil.service.ColossusIntentService;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class Printing
{
	private static final int SINGLE_COLUMN_X = 40;
	private static final int SINGLE_COLUMN_WIDTH = 760;

	private static final int LEFT_COLUMN_X = 40;
	private static final int RIGHT_COLUMN_X = 440;

	private static final int LEFT_COLUMN_WIDTH = 320;
	private static final int RIGHT_COLUMN_WIDTH = 320;

	private static final int TX_DATE_X = 40;
	private static final int TX_DATE_WIDTH = 80;

	private static final int TX_LINE_X = 120;
	private static final int TX_LINE_WIDTH = 680;

	public static void testPage(Context context)
	{
		try
		{
			CrashReporter.leaveBreadcrumb("Printing : testPage - Starting");

			int finalPosition = 0;

			Printer printer = new BitmapPrinter(context);

			CrashReporter.leaveBreadcrumb("Printing : testPage - Printing Title");

			// Print the 'TEST PAGE' title to the label
			finalPosition = printer.addTextCentre(Size.Large, 200, finalPosition, 400, "TEST PAGE");

			CrashReporter.leaveBreadcrumb("Printing : testPage - Printing Footer");

			// Print footer at bottom of label
			printFooter(printer, finalPosition);

			// Add the constructed bitmap, convert to PCX and add to queue
			byte[] ticketImage = printer.addBitmap();

			// Submit to the printer
			sendToPrinter(context, printer);

			// Save the Test Image
			saveLabelImage(context, "TestLabel", ticketImage);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private static int printTitle(Printer printer, int yPosition, String title)
	{
		int finalPosition = yPosition;

		finalPosition = printer.addTextCentre(Size.Large, 0, finalPosition, 800, title);

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

	private static int printDateAndTripNumber(Printer printer, int yPosition)
	{
		int finalPosition = yPosition;

		// Print the Date & Trip No. headers
		printer.addTextLeft(Size.Large, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Date Printed:");
		finalPosition = printer.addTextLeft(Size.Large, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, "Trip No:");

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

		DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");

		long date = new Date().getTime();

		// Print the actual date & trip number values
		printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, df.format(date));
		finalPosition = printer.addTextLeft(Size.Normal, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, Integer.toString(Active.trip.No));

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

		return finalPosition;
	}

	private static int printVehicleAndDriver(Printer printer, int yPosition)
	{
		int finalPosition = yPosition;

		// Print the Vehicle & Driver headers
		printer.addTextLeft(Size.Large, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Vehicle:");
		finalPosition = printer.addTextLeft(Size.Large, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, "Driver:");

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

		// Get the Vehicle & Driver details
		dbVehicle vehicle = dbVehicle.FindByNo(Active.trip.Vehicle.No);
		dbDriver driver = dbDriver.FindByNo(Active.trip.Driver.No);

		// Print the Vehicle & Driver details
		printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, String.format("%d - %s", vehicle.No, vehicle.Reg));
		finalPosition = printer.addTextLeft(Size.Normal, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, String.format("%d - %s", driver.No, driver.Name));

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

		return finalPosition;
	}

	private static int printConsignorConsignee(Printer printer, int yPosition)
	{
		dbSetting consignorName = dbSetting.FindByKey("ConsignorName");
		dbSetting consignorAdd1 = dbSetting.FindByKey("ConsignorAdd1");
		dbSetting consignorAdd2 = dbSetting.FindByKey("ConsignorAdd2");
		dbSetting consignorAdd3 = dbSetting.FindByKey("ConsignorAdd3");

		int finalPosition = yPosition;

		// Print the Consignor & Consignee headers
		printer.addTextLeft(Size.Large, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Consignor:");
		finalPosition = printer.addTextLeft(Size.Large, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, "Consignee:");

		int titlePosition = finalPosition + 10;

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
		finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, consignorName.StringValue);
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
		finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, consignorAdd1.StringValue);
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
		finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, consignorAdd2.StringValue);
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
		finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, consignorAdd3.StringValue);

		printer.addTextLeft(Size.Normal, RIGHT_COLUMN_X, titlePosition, RIGHT_COLUMN_WIDTH, "Various Customers");

		printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

		return finalPosition;
	}

	private static int printStockOnboard(Printer printer, int yPostion)
	{
		int finalPosition = yPostion;

		// Print the 'Stock Onboard' title
		finalPosition = printTitle(printer, finalPosition, "Stock Onboard");

		printer.addTextLeft(Size.Large, 80, finalPosition, 220, "Product");

		// Get the Vehicle details
		dbVehicle vehicle = dbVehicle.FindByNo(Active.trip.Vehicle.No);

		if (vehicle.StockByCompartment)
		{
			printer.addTextLeft(Size.Large, 300, finalPosition, 250, "Comp.");
		}

		finalPosition = printer.addTextLeft(Size.Large, 550, finalPosition, 250, "On board");

		// Get the product that is in the hosereel - if any
		dbProduct lineProduct = Active.vehicle.getHosereelProduct();

		List<dbVehicleStock> stockList;

		if (vehicle.StockByCompartment)
		{
			stockList = dbVehicleStock.GetStockByCompartment(vehicle);
		}
		else
		{
			stockList = dbVehicleStock.GetStockByProduct(vehicle);

			if (lineProduct != null)
			{
				// Subtract line stock.
				for (dbVehicleStock vehicleStock : stockList)
				{
					if (vehicleStock.Product.getId().equals(lineProduct.getId()))
					{
						vehicleStock.CurrentStock -= Active.vehicle.getHosereelCapacity();
					}
				}
			}
		}

		// Print the Vehicle Stock
		for (dbVehicleStock vehicleStock : stockList)
		{
			if (vehicleStock != null)
			{
				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);

				printer.addTextLeft(Size.Large, 80, finalPosition, 220, vehicleStock.Product.Desc);

				if (vehicleStock.Compartment == 0)
				{
					printer.addTextLeft(Size.Large, 300, finalPosition, 250, "Line");
				}

				if (vehicleStock.Compartment > 0)
				{
					printer.addTextLeft(Size.Large, 300, finalPosition, 250, Integer.toString(vehicleStock.Compartment));
				}

				finalPosition = printer.addTextLeft(Size.Large, 550, finalPosition, 250, Integer.toString(vehicleStock.CurrentStock));
			}
		}

		if (!vehicle.StockByCompartment)
		{
			if (lineProduct != null)
			{
				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

				finalPosition = printTitle(printer, finalPosition, "Line stock");

				printer.addTextLeft(Size.Large, 80, finalPosition, 470, lineProduct.Desc);

				finalPosition = printer.addTextLeft(Size.Large, 550, finalPosition, 250, Integer.toString(Active.vehicle.getHosereelCapacity()));
			}
		}

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

		return finalPosition;
	}

	private static int printUndeliveredOrders(Printer printer, int yPosition)
	{
		// Retrieve list of undelivered orders
		List<dbTripOrder> orders = Active.trip.GetUndelivered();

		int finalPosition = yPosition;

		// Determine if there were any undelivered orders and
		// take appropriate action.
		if (orders.size() == 0)
		{
			finalPosition = printTitle(printer, finalPosition, "No undelivered orders");
		}
		else
		{
			// Print Undelivered Orders header
			finalPosition = printTitle(printer, finalPosition, "Undelivered orders");

			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

			// Process all the Undelivered Orders
			finalPosition = printAllUndeliveredOrders(printer, finalPosition, orders);
		}

		return finalPosition;
	}

	private static String getCustomerDetails(dbTripOrder order)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("#");
		builder.append(order.DeliveryOrder);
		builder.append(" - ");
		builder.append(order.InvoiceNo);
		builder.append("  ");
		builder.append(order.DeliveryName);

		return builder.toString();
	}

	private static int printAllUndeliveredOrders(Printer printer, int yPosition, List<dbTripOrder> orders)
	{
		int finalPosition = yPosition;

		// Process the output of each undelivered order
		for (dbTripOrder order : orders)
		{
			// Print theCustomer Details
			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);
			finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, getCustomerDetails(order));

            // Print the delivery address
			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
			finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, order.DeliveryAddress.replace("\n", ", "));

			// Print description of each of the undelivered products
			for (String productOrdered : order.getProductsOrdered("\n").split("\n"))
			{
				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
				finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, productOrdered);
			}

            // Print Required by details if present
            if (order.RequiredBy.length() > 0)
            {
                // Add small spacer befor "Required by" line
                finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

                // Now output the Required by details
                finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, "Required by : " + order.RequiredBy);
            }

            // Print delivery instructions if available
            if (order.Notes.length() > 0)
            {
                // Add small space before the delivery instructions
                finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

                String deliveryInstructions = order.Notes.replace("\n", ", ");

                // Print the delivery instructions
                finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, deliveryInstructions);
            }
		}

		return finalPosition;
	}

	private static int printFooter(Printer printer, int yPosition)
	{
		int finalPosition = yPosition;

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.XLarge);

		finalPosition = printer.addLine(finalPosition);

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);

		finalPosition = printer.addTextCentre(Size.Normal, 0, finalPosition, 800, "End of report");

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

	public static void transportDocument(Context context)
	{
		try
		{
			CrashReporter.leaveBreadcrumb("Printing : transportDocument - Starting");

			int finalPosition = 0;

			Printer printer = new BitmapPrinter(context);

			// Output the 'TRANSPORT DOCUMENT' title for the label
			finalPosition = printTitle(printer, finalPosition, "TRANSPORT DOCUMENT");

			CrashReporter.leaveBreadcrumb("Printing : transportDocument - Printing Date & Trip");

			// Print the Date & Trip
			finalPosition = printDateAndTripNumber(printer, finalPosition);

			CrashReporter.leaveBreadcrumb("Printing : transportDocument - Printing Vehcle &Driver Details");

			// Print the Vehicle & Driver details
			finalPosition = printVehicleAndDriver(printer, finalPosition);

			CrashReporter.leaveBreadcrumb("Printing : transportDocument - Printing Consignor & Consinee");

			// Printer Consignor & Consignee details
			finalPosition = printConsignorConsignee(printer, finalPosition);

			CrashReporter.leaveBreadcrumb("Printing : transportDocument - Printing Stock Onboard");

			// Print the Stock Onboard title
			finalPosition = printStockOnboard(printer, finalPosition);

			CrashReporter.leaveBreadcrumb("Printing : transportDocument - Printing Undelivered Orders");

			// Print Undelivered Orders
			finalPosition = printUndeliveredOrders(printer, finalPosition);

			// Print the footer @ bottom of label
			printFooter(printer, finalPosition);

			byte[] ticketImage = printer.addBitmap();

			// Send to printer.
			sendToPrinter(context, printer);

			// Save the Transport Image
			saveLabelImage(context, "TransportLabel", ticketImage);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private static void sendToPrinter(Context context, Printer printer) throws Exception
	{
		PrintingService service = new PrintingService(context, "Printing");

		service.print(printer.getPrinterData());
	}

	public static void tripReport(Context context)
	{
		try
		{
			// Print using PCX
			printTripToBitmap(context);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private static int printOpeningStock(Printer printer, int yPosition)
	{
		int finalPosition = yPosition;

		// Print the 'Opening Stock' Title
		finalPosition = printTitle(printer, finalPosition, "Opening Stock");

		printer.addTextLeft(Size.Large, 80, finalPosition, 220, "Product");

		dbVehicle vehicle = dbVehicle.FindByNo(Active.trip.Vehicle.No);

		if (vehicle.StockByCompartment)
		{
			printer.addTextLeft(Size.Large, 300, finalPosition, 250, "Comp.");
		}

		finalPosition = printer.addTextLeft(Size.Large, 550, finalPosition, 250, "On board");

		// Get the product that is in the hosereel - if any
		dbProduct lineProduct = Active.vehicle.getHosereelProduct();

		List<dbVehicleStock> stockList;

		if (vehicle.StockByCompartment)
		{
			stockList = dbVehicleStock.GetStockByCompartment(vehicle);
		}
		else
		{
			stockList = dbVehicleStock.GetStockByProduct(vehicle);
		}

		for (dbVehicleStock vehicleStock : stockList)
		{
			if (vehicleStock !=  null)
			{
				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);

				printer.addTextLeft(Size.Large, 80, finalPosition, 220, vehicleStock.Product.Desc);

				if (vehicleStock.Compartment == 0)
				{
					printer.addTextLeft(Size.Large, 300, finalPosition, 250, "Line");
				}

				if (vehicleStock.Compartment > 0)
				{
					printer.addTextLeft(Size.Large, 300, finalPosition, 250, Integer.toString(vehicleStock.Compartment));
				}

				finalPosition = printer.addTextLeft(Size.Large, 550, finalPosition, 250, Integer.toString(vehicleStock.OpeningStock));
			}
		}

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

	private static int printStockTransactions(Printer printer, int yPosition)
	{
		int finalPosition = yPosition;

		// Get the List of Stock Transaction objects
		List<dbTripStock> stockTrans = Active.trip.GetStockTrans();

		if (stockTrans.size() == 0)
		{
			finalPosition = printTitle(printer, finalPosition, "No Transactions");
		}
		else
		{
			DateFormat df1 = new SimpleDateFormat("dd-MMM-yyyy");
			DateFormat df2 = new SimpleDateFormat("HH:mm");

			long date = new Date().getTime();

			String lastDate = df1.format(date);

			String lastGroupBy = "";
			String lastInvoiceNo = "";

			// Add title.
			finalPosition = printTitle(printer, finalPosition, "Transactions");

			// Print each of the Stock Transactions
			for (dbTripStock stockTran : stockTrans)
			{
				// Group by InvoiceNo (if available), otherwise Type.
				String groupBy = stockTran.InvoiceNo;

				if (groupBy.length() == 0)
				{
					groupBy = stockTran.Type;
				}

				if (!groupBy.equals(lastGroupBy))
				{
					lastGroupBy = groupBy;

					// Add spacer when type changes.
					finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);
				}

				if (!df1.format(stockTran.Date).equals(lastDate))
				{
					lastDate = df1.format(stockTran.Date);

					// Print date, if it has changed.
					finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, lastDate);
					finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
				}

				if (!stockTran.InvoiceNo.equals(lastInvoiceNo))
				{
					lastInvoiceNo = stockTran.InvoiceNo;

					if (stockTran.InvoiceNo.length() > 0)
					{
						// Invoice no
						String line1text = "Invoice " + stockTran.InvoiceNo;

						// and Customer code.
						if (stockTran.CustomerCode != null && stockTran.CustomerCode.length() > 0)
						{
							line1text += "  Customer code " + stockTran.CustomerCode;
						}

						// Print line 1
						printer.addTextLeft(Size.Normal, TX_DATE_X, finalPosition, TX_DATE_WIDTH, df2.format(stockTran.Date));
						finalPosition = printer.addTextLeft(Size.Normal, TX_LINE_X, finalPosition, TX_LINE_WIDTH, line1text);
						finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
					}
				}

				// Print line 2 - Description
				printer.addTextLeft(Size.Normal, TX_DATE_X, finalPosition, TX_DATE_WIDTH, df2.format(stockTran.Date));
				finalPosition = printer.addTextLeft(Size.Normal, TX_LINE_X, finalPosition, TX_LINE_WIDTH, stockTran.Description.trim());
				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

				// Print lines 3+ - Notes
				String[] line3s = stockTran.Notes.split("\n");

				for (String line3 : line3s)
				{
					if (line3.length() > 0)
					{
						finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, line3);
                        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
					}
				}
			}
		}

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

	private static int printClosingStock(Printer printer, int yPosition)
	{
		int finalPosition = yPosition;

		finalPosition = printTitle(printer, finalPosition, "Closing Stock");

		// Save the position of the title for future use
		int titlePosition = finalPosition;

		printer.addTextLeft(Size.Large, 80, titlePosition, 220, "Product");

		dbVehicle vehicle = dbVehicle.FindByNo(Active.trip.Vehicle.No);

		if (vehicle.StockByCompartment)
		{
			printer.addTextLeft(Size.Large, 300, titlePosition, 250, "Comp.");
		}

		finalPosition = printer.addTextLeft(Size.Large, 550, titlePosition, 250, "On board");

		List<dbVehicleStock> stockList;

		if (vehicle.StockByCompartment)
		{
			stockList = dbVehicleStock.GetStockByCompartment(vehicle);
		}
		else
		{
			stockList = dbVehicleStock.GetStockByProduct(vehicle);
		}

		for (dbVehicleStock vehicleStock : stockList)
		{
			if (vehicleStock != null)
			{
				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);

				printer.addTextLeft(Size.Large, 80, finalPosition, 220, vehicleStock.Product.Desc);

				if (vehicleStock.Compartment == 0)
				{
					printer.addTextLeft(Size.Large, 300, finalPosition, 250, "Line");
				}

				if (vehicleStock.Compartment > 0)
				{
					printer.addTextLeft(Size.Large, 300, finalPosition, 250, Integer.toString(vehicleStock.Compartment));
				}

				finalPosition = printer.addTextLeft(Size.Large, 550, finalPosition, 250, Integer.toString(vehicleStock.CurrentStock));
			}
		}

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

	private static int printCashReport(Printer printer, int yPosition)
	{
		DecimalFormat decf2 = new DecimalFormat("#,##0.00");

		int finalPosition = yPosition;

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.XLarge);

		// Print the 'CASH REPORT' title
		finalPosition = printTitle(printer, finalPosition, "CASH REPORT");

		// Print the Date & Trip number
		finalPosition = printDateAndTripNumber(printer, finalPosition);

		// Vehicle & Driver.
		finalPosition = printVehicleAndDriver(printer, finalPosition);

		int payments = 0;

		List<dbTripStock> stockTrans = Active.trip.GetStockTrans();

		for (dbTripStock stockTran : stockTrans)
		{
			if (stockTran.Type.equals("Payment"))
			{
				payments++;
			}
		}

		if (payments == 0)
		{
			finalPosition = printer.addTextCentre(Size.Large, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, "No Transactions");
		}
		else
		{
			double cash = 0;
			double cheques = 0;
			double vouchers = 0;

			// Print titles.
			printer.addTextLeft(Size.Normal, 50, finalPosition, 200, "Invoice no");
			printer.addTextLeft(Size.Normal, 250, finalPosition, 200, "Customer");
			printer.addTextLeft(Size.Normal, 450, finalPosition, 100, "Type");
			finalPosition = printer.addTextRight(Size.Normal, 550, finalPosition, 150, "Amount");

			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);

			for (dbTripStock stockTran : stockTrans)
			{
				if (stockTran.Type.equals("Payment"))
				{
					String[] plines = (stockTran.Description + "\n" + stockTran.Notes).split("\n");

					for (String pline : plines)
					{
						int idx = pline.indexOf(":") + 1;
						String strValue = pline.substring(idx).replace(",", "");

						String type = "";
						double amount = 0;

						if (pline.startsWith("Cash payment:"))
						{
							type = "Cash";
							amount = Double.parseDouble(strValue);
							cash += amount;
						}

						if (pline.startsWith("Cheque payment:"))
						{
							type = "Cheque";
							amount = Double.parseDouble(strValue);
							cheques += amount;
						}

						if (pline.startsWith("Voucher payment:"))
						{
							type = "Voucher";
							amount = Double.parseDouble(strValue);
							vouchers += amount;
						}

						if (type.length() > 0)
						{
							// Add small spacer to give some space between lines
							finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

							printer.addTextLeft(Size.Normal, 50, finalPosition, 200, stockTran.InvoiceNo);
							printer.addTextLeft(Size.Normal, 250, finalPosition, 200, stockTran.CustomerCode);
							printer.addTextLeft(Size.Normal, 450, finalPosition, 100, type);
							finalPosition = printer.addTextRight(Size.Normal, 550, finalPosition, 150, decf2.format(amount));
						}
					}
				}
			}

			// Add title.
			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
			finalPosition = printTitle(printer, finalPosition, "Summary");

			if (cash != 0)
			{
				printer.addTextLeft(Size.Large, 200, finalPosition, 180, "Cash");
				finalPosition = printer.addTextRight(Size.Large, 400, finalPosition, 200, decf2.format(cash));

                // Add small spacer
                finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
			}

			if (cheques != 0)
			{
				printer.addTextLeft(Size.Large, 200, finalPosition, 180, "Cheques");
				finalPosition = printer.addTextRight(Size.Large, 400, finalPosition, 200, decf2.format(cheques));

                // Add small spacer
                finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
			}

			if (vouchers != 0)
			{
				printer.addTextLeft(Size.Large, 200, finalPosition, 180, "Vouchers");
				finalPosition = printer.addTextRight(Size.Large, 400, finalPosition, 200, decf2.format(vouchers));

                // Add small spacer
                finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
			}

			// Print total.
			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
			finalPosition = printer.addTextRight(Size.Large, 400, finalPosition, 200, "========");
			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
			finalPosition = printer.addTextRight(Size.Large, 400, finalPosition, 200, decf2.format(cash + cheques + vouchers));
		}

		return finalPosition;
	}

	private static void printTripToBitmap(Context context) throws Exception
	{
		CrashReporter.leaveBreadcrumb("Printing : printTripToBitmap - Starting");

		Printer printer = new BitmapPrinter(context);

		int finalPosition = 0;

		// Print the Trip Report title
		finalPosition = printTitle(printer, finalPosition, "TRIP REPORT");

		CrashReporter.leaveBreadcrumb("Printing : printTripToBitmap - Printing Date & Trip No.");

		// Print the Date & Trip
		finalPosition = printDateAndTripNumber(printer, finalPosition);

		CrashReporter.leaveBreadcrumb("Printing : printTripToBitmap - Printing Vehicle & Driver Details");

		// Print Vehicle & Driver details
		finalPosition = printVehicleAndDriver(printer, finalPosition);

		CrashReporter.leaveBreadcrumb("Printing : printTripToBitmap - Printing Opening Stock");

		// Print the Opening Stock Section
		finalPosition = printOpeningStock(printer, finalPosition);

		CrashReporter.leaveBreadcrumb("Printing : printTripToBitmap - Printing Stock Transactions");

		// Print Stock Transactions
		finalPosition = printStockTransactions(printer, finalPosition);

		CrashReporter.leaveBreadcrumb("Printing : printTripToBitmap - Printing Closing Stock");

		// Print Closing Stock Section
		finalPosition = printClosingStock(printer, finalPosition);

		// Print Separator before Cash Report
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);
		finalPosition = printer.addLine(finalPosition);

		CrashReporter.leaveBreadcrumb("Printing : printTripToBitmap - Printing Cash Report");

		// Print the Cash Report
		finalPosition = printCashReport(printer, finalPosition);

		// Print end of report
		printFooter(printer, finalPosition);

		byte[] ticketImage = printer.addBitmap();

		// Actually print the report
		sendToPrinter(context, printer);

		// Save the Trip Image
		saveLabelImage(context, "TripLabel", ticketImage);
	}

	public static void ticket(Context context, dbTripOrder order)
	{
		try
		{
			printBitmapTicket(context, order);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private static void printBitmapTicket(Context context, dbTripOrder order) throws Exception
	{
		CrashReporter.leaveBreadcrumb("Printing : printBitmapTicket - Starting");

		DateFormat df1 = new SimpleDateFormat("dd-MMM-yyyy");

        DecimalFormat format2dp = new DecimalFormat("#,##0.00");
        DecimalFormat format4dp = new DecimalFormat("#,##0.0000");

		int finalPosition = 0;

		// Create Bitmap printer
		Printer printer = new BitmapPrinter(context);

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

		CrashReporter.leaveBreadcrumb("Printing : printBitmapTicket - Printing Logo");

		// Print logo at top of label
		finalPosition = printer.addLogo(order.BrandID, finalPosition);

		// Print the date
		finalPosition = printer.addTextLeft(Size.Large, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, "Date:");

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

		// Print the actual delivery date if present
		if (order.DeliveryDate == 0)
		{
			finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, df1.format(new Date()));
		}
		else
		{
			finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, df1.format(order.DeliveryDate));
		}

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

		CrashReporter.leaveBreadcrumb("Printing : printBitmapTicket - Printing Account & Invoice Nos.");

		// Print the Account & Invoice numbers
		printer.addTextLeft(Size.Large, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Account no:");
		finalPosition = printer.addTextLeft(Size.Large, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, "Invoice no:");

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

		printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, order.CustomerCode);
		finalPosition = printer.addTextLeft(Size.Normal, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, order.InvoiceNo);

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

		CrashReporter.leaveBreadcrumb("Printing : printBitmapTicket - Printing Invoice & Deliver To Addresses");

		// Print the Invoice To & Deliver To Addresses
		finalPosition = printAddresses(printer, finalPosition, order);

        // Print Terms
        finalPosition = printTerms(printer, finalPosition, order.Terms);

        // Print the Order Lines
		finalPosition = printOrderLines(printer, finalPosition, order);

		CrashReporter.leaveBreadcrumb("Printing - printBitmapTicket - Printing Ticket Amounts");

		// Print VAT
        Hashtable<Double, dbTripOrder.VatRow> vatValues = order.getDeliveredVatValues();
        Enumeration e = vatValues.keys();

        while (e.hasMoreElements())
        {
            double key = (Double)e.nextElement();

            dbTripOrder.VatRow row = vatValues.get(key);

            String vatTitle = "VAT @ " + format2dp.format(row.vatPerc) + " %";
            finalPosition = printTitleAndAmount(printer, finalPosition, vatTitle, row.nettValue * row.vatPerc / 100.0);
        }

        // Print account balance
        finalPosition = printTitleAndAmount(printer, finalPosition, "A/c balance", order.getCodAccBalance());

		// Print total
		finalPosition = printTitleAndAmount(printer, finalPosition, "Total", order.getCreditTotal());

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

        // Print amount paid at office
        finalPosition = printTitleAndAmount(printer, finalPosition, "Paid office", order.getPrepaidAmount());

        // Print amount paid to driver
        finalPosition = printTitleAndAmount(printer, finalPosition, "Paid driver", order.getPaidDriver());

		// Print the discount
		finalPosition = printTitleAndAmount(printer, finalPosition, "Discount", order.Discount);

		// Print any payments outstanding
		finalPosition = printTitleAndAmount(printer, finalPosition, "Outstanding", order.getOutstanding());

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

        CrashReporter.leaveBreadcrumb("Printing - printBitmapTicket - Printing Meter Data");

        // Print the Meter Tickets
        finalPosition = printMeterData(printer, finalPosition, order);

        // Get the surcharge amount
		double surcharge = order.getDeliveredSurchargeValue();

        // Get the amount still to pay
		double outstanding = order.getOutstanding();

        // Print the surcharge/discount message if necessary
		if (surcharge != 0 && outstanding > 0)
		{
            finalPosition = printSurchargeMessage(printer, finalPosition, order);
		}

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

		finalPosition = printer.addLine(finalPosition);

		// Print the customer signature
		if (order.CustomerSignature)
		{
			CrashReporter.leaveBreadcrumb("Printing : printBitmapTicket - Printing Customer Signature");

			// Add the signature
			finalPosition = printer.addSignature("Customer signature", order.CustomerSignatureName, finalPosition, order.CustomerSignatureImage, order.CustomerSignatureDateTime);
		}

		// Print the driver signature
		if (order.DriverSignature)
		{
			CrashReporter.leaveBreadcrumb("Printing : printBitmapTicket - Printing Driver Signature");

			// Add the signature
			finalPosition = printer.addSignature("Driver signature", order.DriverSignatureName, finalPosition, order.DriverSignatureImage, order.DriverSignatureDateTime);
		}

		CrashReporter.leaveBreadcrumb("Printing : printBitmapTicket - Printing Customs Statement");

		// Customs statement.
		finalPosition = printCustomsStatement(printer, finalPosition);

		// Add footer.
		finalPosition = printFooter(printer, finalPosition);

		printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

		// Convert to a bitmap
		byte[] ticketImage = printer.addBitmap();

		// Send to the printer
		sendToPrinter(context, printer);

		// Save the Ticket Image
		saveLabelImage(context, "TicketLabel", ticketImage);
	}

    private static double getOrderVat(dbTripOrder order)
    {
        dbTripOrderLine orderLine = order.GetTripOrderLines().get(0);

        double nettValue = orderLine.getDeliveredNettValue();
        double vatRate = getVatPercentage(orderLine);

        return nettValue * vatRate / 100.0;
    }

    private static double getTotalValue(dbTripOrder order)
    {
        dbTripOrderLine orderLine = order.GetTripOrderLines().get(0);

        double nettValue = orderLine.getDeliveredNettValue();
        double netValueVat = nettValue * getVatPercentage(orderLine) / 100;

        return nettValue + netValueVat;
    }

    private static int printSurchargeMessage(Printer printer, int yPosition, dbTripOrder order)
    {
        DateFormat formatDate = new SimpleDateFormat("dd-MMM-yyyy");

        DecimalFormat format2dp = new DecimalFormat("#,##0.00");
        DecimalFormat format4dp = new DecimalFormat("#,##0.0000");

        int finalPosition = yPosition;

        CrashReporter.leaveBreadcrumb("Printing: printSurchargeMessage");

        // Get all the order lines
        List<dbTripOrderLine> orderLines = order.GetTripOrderLines();

        // Get the surcharge amount (in Pence per Litre)
        double surcharge = orderLines.get(0).Surcharge;

        double totalSurchargeAmount = 0.0;

        double surchargeAmountVat = 0.0;

        for (dbTripOrderLine line : orderLines)
        {
            double surchargeAmount = surcharge / 100.0 * line.OrderedQty;

            totalSurchargeAmount += surchargeAmount;

            surchargeAmountVat += surchargeAmount * (line.OrderedQty > line.VatPerc2Above ? line.VatPerc2 : line.VatPerc1) / 100.0;
        }

        // Print line commencing "Deduct x ppl ..."
        StringBuilder line = new StringBuilder();

        line.append("Deduct ");
        line.append(format4dp.format(surcharge));
        line.append(" ppl = £");
        line.append(format2dp.format(totalSurchargeAmount + surchargeAmountVat));
        line.append(" (inc. £");
        line.append(format2dp.format(surchargeAmountVat));
        line.append(" of VAT)");

        // Print space before the rest of the message
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

        finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, 720, line.toString());

        // Print line commencing "from £xx.xx  if paid by ..."
        line = new StringBuilder();

        line.append("from £");
        line.append(format2dp.format(order.getCreditTotal()));
        line.append(" if paid by ");
        line.append(formatDate.format(order.DueDate));

        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

        finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, 720, line.toString());

        // Print line commencing "No credit note will ..."
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

        finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, 720, "No credit note will be issued.");

        // Print line commencing "Only reclaim the VAT ..."
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

        finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, 720, "Only reclaim the VAT actually paid.");

        line = new StringBuilder();

        double nettValue = 0.0;
        double nettValueVat = 0.0;

        for (dbTripOrderLine orderLine : orderLines)
        {
            nettValue += orderLine.getDeliveredNettValue();
            nettValueVat += order.getDeliveredNettValue() * (orderLine.OrderedQty > orderLine.VatPerc2Above ? orderLine.VatPerc2 : orderLine.VatPerc1) / 100.0;
        }

        line.append("£");
        line.append(format2dp.format(nettValue));
        line.append(" + VAT £");
        line.append(format2dp.format(nettValueVat));
        line.append(" = £");
        line.append(format2dp.format(nettValue + nettValueVat));

        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

        finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, 720, line.toString());

        line = new StringBuilder();

        line.append("if paid by ");
        line.append(formatDate.format(order.DueDate));

        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

        finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, 720, line.toString());

        return finalPosition;
    }

    private static int printTerms(Printer printer, int yPosition, String terms)
    {
        CrashReporter.leaveBreadcrumb("Printing: printTerms");

        int finalPosition = yPosition;

        finalPosition = printer.addTextLeft(Size.Large, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, "Terms:");
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, terms);

        return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
    }

	private static int printCustomsStatement(Printer printer, int yPosition)
	{
		int finalPosition = yPosition;

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.XLarge);
		finalPosition = printer.addTextCentre(Size.Normal, 0, finalPosition, 800, "Marked Gas oil and Kerosene");
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
		finalPosition = printer.addTextCentre(Size.Normal, 0, finalPosition, 800, "should not be used in a road vehicle");

		return finalPosition;
	}

	private static int printTitleAndAmount(Printer printer, int yPosition, String title, double amount)
	{
		int finalPosition = yPosition;

		if (amount != 0)
		{
			DecimalFormat decf2 = new DecimalFormat("#,##0.00");

			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);
			printer.addTextRight(Size.Large, 300, finalPosition, 250, title);
			finalPosition = printer.addTextRight(Size.Large, 590, finalPosition, 170, decf2.format(amount));
		}

		return finalPosition;
	}

	private static int printAddresses(Printer printer, int yPosition, dbTripOrder order)
	{
		int finalPosition = yPosition;

		// Print the Invoice & Delivered To
		printer.addTextLeft(Size.Large, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Invoice to:");
		finalPosition = printer.addTextLeft(Size.Large, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, "Delivered to:");

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

		printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, order.CustomerName);
		finalPosition = printer.addTextLeft(Size.Normal, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, order.DeliveryName);

		// Print the customer address
		int customerAddressPosition = printAddress(printer, LEFT_COLUMN_X, finalPosition, order.CustomerAddress.split("\n"));

		// Print the delivery address
		int deliveryAddressPosition = printAddress(printer, RIGHT_COLUMN_X, finalPosition, order.DeliveryAddress.split("\n"));

		finalPosition = customerAddressPosition > deliveryAddressPosition ? customerAddressPosition : deliveryAddressPosition;

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

	private static int printAddress(Printer printer, int xPosition,  int yPosition, String[] addresses)
	{
		int finalPosition = yPosition;

		for (String address : addresses)
		{
			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

			finalPosition = printer.addTextLeft(Size.Normal, xPosition, finalPosition, LEFT_COLUMN_WIDTH, address);
		}

		return finalPosition;
	}

	private static int printOrderLines(Printer printer, int yPosition, dbTripOrder order)
	{
        CrashReporter.leaveBreadcrumb("Printing: printOrderLines");

        int finalPosition = yPosition;

        DecimalFormat format2dp = new DecimalFormat("#,##0.00");
		DecimalFormat format3dp = new DecimalFormat("#,##0.000");

		// Product, Litres, PPL, Value, VAT
		printer.addTextLeft(Size.Large, 40, finalPosition, 150, "Product");
		printer.addTextRight(Size.Large, 210, finalPosition, 80, "Litres");
		printer.addTextRight(Size.Large, 310, finalPosition, 140, "PPL");
		printer.addTextRight(Size.Large, 470, finalPosition, 130, "Value");
        finalPosition = printer.addTextRight(Size.Large, 620, finalPosition, 140, "%VAT");

		finalPosition = printer.addLine(finalPosition + 10);

		// Find order lines.
		List<dbTripOrderLine> lines = order.GetTripOrderLines();

		// Process each Order Line
		for (dbTripOrderLine line : lines)
		{
			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

            // Print the Product Description
			printer.addTextLeft(Size.Large, 40, finalPosition, 150, line.Product.Desc);

            // Print the Delivered Quantity in litres
			printer.addTextRight(Size.Large, 210, finalPosition, 80, Integer.toString(line.DeliveredQty));

            // Get the Delivered price include surcharge (in PPL).
            double deliveredPrice = line.getDeliveredPrice();

			if (deliveredPrice != 0)
			{
                // Output the price in ppl to 3 decimal places
				printer.addTextRight(Size.Large, 310, finalPosition, 140, format3dp.format(deliveredPrice * line.Ratio));
			}

            // Get the value of the delivered product (in pounds)
            double deliveredValue = line.getDeliveredNettValue() + line.getDeliveredSurchargeValue();

            if (deliveredValue != 0)
			{
                // Output the value in pounds to 2 dp
				 printer.addTextRight(Size.Large, 470, finalPosition, 130, format2dp.format(deliveredValue));
			}

            // Get the VAT percentage
            double vatPercentage = getVatPercentage(line);

            finalPosition = printer.addTextRight(Size.Large, 620, finalPosition, 140, format2dp.format(vatPercentage));
		}

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

    private static double getVatPercentage(dbTripOrderLine line)
    {
        if (line.VatPerc2Above < 1.0e6)
        {
            if (line.DeliveredQty < line.VatPerc2Above)
            {
                return line.VatPerc1;
            }
            else
            {
                return line.VatPerc2;
            }
        }
        else
        {
            return line.VatPerc1;
        }
    }

    private static int printMeterData(Printer printer, int yPosition, dbTripOrder order)
    {
        DecimalFormat decf0 = new DecimalFormat("#,##0");
        DecimalFormat decf1 = new DecimalFormat("#,##0.0");

        int finalPosition = yPosition;

        // Find order lines.
        List<dbTripOrderLine> lines = order.GetTripOrderLines();

        // Process each Order Line
        for (dbTripOrderLine line : lines)
        {
            if (line.ticketNo != null)
			{
				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

				printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Ticket number");
				finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, line.ticketNo);

				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

				printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Product Desc");
				finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, line.ticketProductDesc);

				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

				printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Start");
				finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, line.ticketStartTime);

				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

				printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Finish");
				finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, line.ticketFinishTime);

				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

				printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Totalizer start");
				finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, decf0.format(line.ticketStartTotaliser));

				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

				printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Totalizer end");
				finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, decf0.format(line.ticketEndTotaliser));

				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

				if (line.ticketAt15Degrees)
				{
					printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Volume delivered @ 15.0 C");
					finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, decf0.format(line.ticketNetVolume));
				}
				else
				{
					printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Volume delivered @ " + decf1.format(line.ticketTemperature) + " C");
					finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, decf0.format(line.ticketGrossVolume));
				}
			}
        }

        return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
    }

    /**
	 * Method to compress an array of bytes using the GZIP compression
	 * algorithm.
	 * @param input A byte array that is to be compressed.
	 * @return The compressed byte array.
	 * @throws Exception
	 */
	private static byte[] compressBytes(byte[] input) throws Exception
	{
		CrashReporter.leaveBreadcrumb("Printing : compressBytes - Starting");

		// Create ByteArrayOutputStream to hold the compressed data
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		// Create a GZIPOutputStream to perform the compression
		GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(os));

		try
		{
			CrashReporter.leaveBreadcrumb("Printing : compressBytes - Compressing Data");

			// Write the input data to the compressor
			zos.write(input);

			// Make sure that all buffers are flushed
			zos.flush();
		}
		finally
		{
			// Close the compressor
			zos.close();
		}

		CrashReporter.leaveBreadcrumb("Printing : compressBytes - Returning byte array");

		// Return the data as a byte array
		return os.toByteArray();
	}

	/**
	 * Stores the PCX image of the ticket in thw SQLite DB.
	 * @param context The passed Context.
	 * @param labelType String identifying the type of Label being stored
	 * @param content Byte array of the PCX label.
	 * @throws Exception
	 */
	private static void saveLabelImage(Context context, String labelType, byte[] content) throws Exception
	{
		CrashReporter.leaveBreadcrumb("Printing : saveLabelImage - Starting");

		Intent i = new Intent(context, ColossusIntentService.class);

		// Compress the PCX content and encode as Base64
		String base64Content = Base64.encodeToString(compressBytes(content), Base64.DEFAULT);

		CrashReporter.leaveBreadcrumb("Printing : saveLabelImage - Label Type : " + labelType);

		JSONObject json = new JSONObject();

		long now = new Date().getTime();

		json.put("DateTime", "/Date(" + now + ")/");
		json.put("Image", base64Content);

        if (labelType == "TransportLabel")
        {
            json.put("TripID", Active.trip.ColossusID);
        }
        else if (labelType == "TripLabel")
        {
            json.put("TripID", Active.trip.ColossusID);
        }
        else if (labelType == "TicketLabel")
        {
            json.put("TripID", Active.trip.ColossusID);
            json.put("OrderID", Active.order.ColossusID);
        }

		i.putExtra("Type", labelType);
		i.putExtra("Content", json.toString());

		CrashReporter.leaveBreadcrumb("Printing : saveLabelImage - Saving Label to DB");

		context.startService(i);
	}
}
