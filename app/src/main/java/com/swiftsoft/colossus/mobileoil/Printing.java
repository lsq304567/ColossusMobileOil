package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;

import com.swiftsoft.colossus.mobileoil.bluetooth.PrintingService;
import com.swiftsoft.colossus.mobileoil.database.model.dbDriver;
import com.swiftsoft.colossus.mobileoil.database.model.dbEndOfDay;
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
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class Printing
{
	private static final int SINGLE_COLUMN_X = 40;
	private static final int SINGLE_COLUMN_WIDTH = 740;

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
        CrashReporter.leaveBreadcrumb("Printer: printTitle");

		int finalPosition = yPosition;

		finalPosition = printer.addTextCentre(Size.Large, 0, finalPosition, 800, title);

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

	private static int printDateAndTripNumber(Printer printer, int yPosition)
	{
        CrashReporter.leaveBreadcrumb("Printing: printDateAndTripNumber");

		int finalPosition = yPosition;

        CrashReporter.leaveBreadcrumb("Printing: printDateAndTripNumber - Printing date & trip no. headers");

		// Print the Date & Trip No. headers
		printer.addTextLeft(Size.Large, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Date Printed:");
		finalPosition = printer.addTextLeft(Size.Large, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, "Trip No:");

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

        DateFormat formatDate = new SimpleDateFormat("dd-MMM-yyyy");

        CrashReporter.leaveBreadcrumb("Printing: printDateAndTripNumber - Printing date & trip no. details");

        // Print the actual date & trip number values
		printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, formatDate.format(Utils.getCurrentTime()));
		finalPosition = printer.addTextLeft(Size.Normal, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, Integer.toString(Active.trip.No));

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

	private static int printVehicleAndDriver(Printer printer, int yPosition)
	{
        CrashReporter.leaveBreadcrumb("Printing: printVehicleAndDriver");

		int finalPosition = yPosition;

		CrashReporter.leaveBreadcrumb("Printing: printVehicleAndDriver - Printing vehicle & driver headers");

		// Print the Vehicle & Driver headers
		printer.addTextLeft(Size.Large, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Vehicle:");
		finalPosition = printer.addTextLeft(Size.Large, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, "Driver:");

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

		CrashReporter.leaveBreadcrumb("Printing: printVehicleAndDriver - Fetching vehicle & driver details");

		// Get the Vehicle & Driver details
		dbVehicle vehicle = dbVehicle.FindByNo(Active.vehicle.No);
		dbDriver driver = dbDriver.FindByNo(Active.driver.No);

		CrashReporter.leaveBreadcrumb("Printing: printVehicleAndDriver - Printing vehicle & driver details");
		// Print the Vehicle & Driver details
		printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, String.format("%d - %s", vehicle.No, vehicle.Reg));
		finalPosition = printer.addTextLeft(Size.Normal, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, String.format("%d - %s", driver.No, driver.Name));

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

	private static int printConsignorConsignee(Printer printer, int yPosition)
	{
        CrashReporter.leaveBreadcrumb("Printing: printConsignorConsignee");

		dbSetting consignorName = dbSetting.FindByKey("ConsignorName");
		dbSetting consignorAdd1 = dbSetting.FindByKey("ConsignorAdd1");
		dbSetting consignorAdd2 = dbSetting.FindByKey("ConsignorAdd2");
		dbSetting consignorAdd3 = dbSetting.FindByKey("ConsignorAdd3");

		int finalPosition = yPosition;

        CrashReporter.leaveBreadcrumb("Printing: printConsignorConsignee - Printing consignor/consignee headers");

        // Print the Consignor & Consignee headers
		printer.addTextLeft(Size.Large, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Consignor:");
		finalPosition = printer.addTextLeft(Size.Large, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, "Consignee:");

		int titlePosition = finalPosition + 10;

        CrashReporter.leaveBreadcrumb("Printing: printConsignorConsignee - Printing consignor/consignee details");

        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
		finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, consignorName.StringValue);
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
		finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, consignorAdd1.StringValue);
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
		finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, consignorAdd2.StringValue);
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
		finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, consignorAdd3.StringValue);

		printer.addTextLeft(Size.Normal, RIGHT_COLUMN_X, titlePosition, RIGHT_COLUMN_WIDTH, "Various Customers");

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

	private static int printStockOnboard(Printer printer, int yPosition)
	{
        CrashReporter.leaveBreadcrumb("Printing: printStockOnboard");

		int finalPosition = yPosition;

		// Print the 'Stock Onboard' title
		finalPosition = printTitle(printer, finalPosition, "Stock Onboard");

        CrashReporter.leaveBreadcrumb("Printing: printStockOnboard - Printing product header");

		printer.addTextLeft(Size.Large, 80, finalPosition, 220, "Product");

        CrashReporter.leaveBreadcrumb("Printing: printStockOnboard - Fetching vehicle details from DB");

		// Get the Vehicle details
		dbVehicle vehicle = dbVehicle.FindByNo(Active.trip.Vehicle.No);

        CrashReporter.leaveBreadcrumb("Printing: printStockOnboard - Printing On board header");

		finalPosition = printer.addTextLeft(Size.Large, 550, finalPosition, 250, "On board");

        CrashReporter.leaveBreadcrumb("Printing: printStockOnboard - Fetching hosereel product");

		// Get the product that is in the hosereel - if any
		dbProduct lineProduct = Active.vehicle.getHosereelProduct();

		CrashReporter.leaveBreadcrumb("Printing: printStockOnboard - Fetching stock by product");

		List<dbVehicleStock> stockList = dbVehicleStock.GetStockByProduct(vehicle);

        // If there is line stock present then subtract the hosereel
        // capacity from the product stuck of the same type
		if (lineProduct != null)
		{
			CrashReporter.leaveBreadcrumb("Printing: printStockOnboard - Subtracting line stock from current stock");

			// Subtract line stock.
			for (dbVehicleStock vehicleStock : stockList)
			{
				if (vehicleStock.Product.getId().equals(lineProduct.getId()))
				{
					vehicleStock.CurrentStock -= Active.vehicle.getHosereelCapacity();

                    // There can only be one possible line stock, so once found
                    // we can break
					break;
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

        // Now finally print the lines stock - type & volume
        if (lineProduct != null)
        {
            finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

            finalPosition = printTitle(printer, finalPosition, "Line stock");

            printer.addTextLeft(Size.Large, 80, finalPosition, 470, lineProduct.Desc);

            finalPosition = printer.addTextLeft(Size.Large, 550, finalPosition, 250, Integer.toString(Active.vehicle.getHosereelCapacity()));
        }

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

	private static int printUndeliveredOrders(Printer printer, int yPosition)
	{
        CrashReporter.leaveBreadcrumb("Printing: printUndeliveredOrders");

		// Retrieve list of undelivered orders
		List<dbTripOrder> orders = Active.trip.GetUndelivered();

		int finalPosition = yPosition;

		// Determine if there were any undelivered orders and
		// take appropriate action.
		if (orders.size() == 0)
		{
            CrashReporter.leaveBreadcrumb("Printing: printUndeliveredOrders - There are no undelivered orders");

			finalPosition = printTitle(printer, finalPosition, "No undelivered orders");
		}
		else
		{
            CrashReporter.leaveBreadcrumb("Printing: printUndeliveredOrders - Printing 'Undelivered orders' header");

			// Print Undelivered Orders header
			finalPosition = printTitle(printer, finalPosition, "Undelivered orders");

			// Process all the Undelivered Orders
			finalPosition = printAllUndeliveredOrders(printer, finalPosition, orders);
		}

		return finalPosition;
	}

	private static String getCustomerDetails(dbTripOrder order)
	{
        return String.format("#%d - %s  %s", order.DeliveryOrder, order.InvoiceNo, order.DeliveryName);
	}

	private static int printAllUndeliveredOrders(Printer printer, int yPosition, List<dbTripOrder> orders)
	{
        CrashReporter.leaveBreadcrumb(("Printing: printAllUndeliveredOrders"));

		int finalPosition = yPosition;

		// Process the output of each undelivered order
		for (dbTripOrder order : orders)
		{
            CrashReporter.leaveBreadcrumb("Printing: printAllUndeliveredOrders - Printing Customer details");

			// Print the Customer Details
			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);
			finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, getCustomerDetails(order));

            CrashReporter.leaveBreadcrumb("Printing: printAllUndeliveredOrders - Printing delivery address");

            // Print the delivery address
			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
			finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, order.DeliveryAddress.replace("\n", ", "));

			// Print description of each of the undelivered products
			for (String productOrdered : order.getProductsOrdered("\n").split("\n"))
			{
                CrashReporter.leaveBreadcrumb("Printing: printAllUndeliveredOrders - Printing description of the undelivered products");

				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
				finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, productOrdered);
			}

            // Print Required by details if present
            if (order.RequiredBy.length() > 0)
            {
                CrashReporter.leaveBreadcrumb("Printing: printAllUndeliveredOrders - Printing required by details ...");

                // Add small spacer before "Required by" line
                finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

                // Now output the Required by details
                finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, "Required by : " + order.RequiredBy);
            }

            // Print delivery instructions if available
            if (order.Notes.length() > 0)
            {
                CrashReporter.leaveBreadcrumb("Printing: printAllUndeliveredOrders - Printing delivery instructions ...");

                // Add small space before the delivery instructions
                finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

                String deliveryInstructions = order.Notes.replace("\n", ", ");

                // Print the delivery instructions
                finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, deliveryInstructions);
            }
		}

		return finalPosition;
	}

	@SuppressWarnings("SuspiciousNameCombination")
	private static int printFooter(Printer printer, int yPosition)
	{
        CrashReporter.leaveBreadcrumb("Printing: printFooter");

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

			// Print the Date & Trip
			finalPosition = printDateAndTripNumber(printer, finalPosition);

			// Print the Vehicle & Driver details
			finalPosition = printVehicleAndDriver(printer, finalPosition);

			// Printer Consignor & Consignee details
			finalPosition = printConsignorConsignee(printer, finalPosition);

			// Print the Stock Onboard title
			finalPosition = printStockOnboard(printer, finalPosition);

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
        CrashReporter.leaveBreadcrumb("Printing: sendToPrinter");

		PrintingService service = new PrintingService(context, "Printing");

		service.print(printer.getPrinterData());
	}

	public static void endOfDayReport(Context context)
	{
		try
		{
			// Print EOD report to PCX
            printEndOfDayReport(context);
        }
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private static double getStartTotalizer(int tripNumber)
	{
		double totalizer = Double.MAX_VALUE;

		for (dbTripOrder tripOrder :  dbTripOrder.GetAll())
		{
			if (tripOrder.Trip.No == tripNumber)
			{
				for (dbTripOrderLine line : tripOrder.GetTripOrderLines())
				{
					if (line.ticketStartTotaliser < totalizer)
					{
						totalizer = line.ticketStartTotaliser;
					}
				}
			}
		}

		return totalizer;
	}

	private static double getFinishTotalizer(int tripNumber)
	{
		double totalizer = 0.0;

		for (dbTripOrder tripOrder :  dbTripOrder.GetAll())
		{
			if (tripOrder.Trip.No == tripNumber)
			{
				for (dbTripOrderLine line : tripOrder.GetTripOrderLines())
				{
					if (line.ticketEndTotaliser > totalizer)
					{
						totalizer = line.ticketEndTotaliser;
					}
				}
			}
		}

		return totalizer;
	}

	private static void printEndOfDayReport(Context context) throws Exception
    {
        int finalPosition = 0;

        Printer printer = new BitmapPrinter(context);

        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

        finalPosition = printTitle(printer, finalPosition, "End Of Day Report");

		// Vehicle & Driver.
		finalPosition = printVehicleAndDriver(printer, finalPosition);

		int savedPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);

		// Print the date & time
        finalPosition = printer.addTextLeft(Size.Large, LEFT_COLUMN_X, savedPosition, LEFT_COLUMN_WIDTH, "Date Printed:");

        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

        DateFormat formatDate = new SimpleDateFormat("dd-MMM-yyyy");

        // Print the actual date value
        printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, formatDate.format(Utils.getCurrentTime()));

        finalPosition = printer.addTextLeft(Size.Medium, RIGHT_COLUMN_X, savedPosition, RIGHT_COLUMN_WIDTH, "Trip IDs:");

		// get the unique trip ids
		List<Integer> uniqueTripIds = dbEndOfDay.getUniqueTripIds();

        for (int id : uniqueTripIds)
        {
            finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
            finalPosition = printer.addTextLeft(Size.Normal, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, String.format("%d", id));
        }

        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

        /* */

        int xOffset = 200;

        // Get the list of products that are on lorry
        List<dbProduct> products = dbEndOfDay.getUniqueProducts();

        int headerHeight = 0;

        for (dbProduct product : products)
        {
            headerHeight = printer.addTextRight(Size.Normal, xOffset, finalPosition, 130, product.Desc);

            xOffset += 150;
        }

        xOffset = 200;
        finalPosition = headerHeight;

        // Print the original stock value for each product
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        printer.addTextLeft(Size.Normal, 40, finalPosition, 160, "Starting");

        for (dbProduct product : products)
        {
            headerHeight = printer.addTextRight(Size.Normal, xOffset, finalPosition, 130, String.format("%d", dbEndOfDay.getStartingQuantity(product)));

            xOffset += 150;
        }

        xOffset = 200;
        finalPosition = headerHeight;

        // Print the Loaded quantities
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        printer.addTextLeft(Size.Normal, 40, finalPosition, 160, "Loaded");

        for (dbProduct product : products)
        {
            headerHeight = printer.addTextRight(Size.Normal, xOffset, finalPosition, 130, String.format("%d", dbEndOfDay.getLoadedQuantity(product)));

            xOffset += 150;
        }

        xOffset = 200;
        finalPosition = headerHeight;

        // Print the delivered quantities
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        printer.addTextLeft(Size.Normal, 40, finalPosition, 150, "Delivery");

        for (dbProduct product : products)
        {
            headerHeight = printer.addTextRight(Size.Normal, xOffset, finalPosition, 130, String.format("%d", dbEndOfDay.getDeliveredQuantity(product)));

            xOffset += 150;
        }

        xOffset = 200;
        finalPosition = headerHeight;

        // Print the returned quantities
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        printer.addTextLeft(Size.Normal, 40, finalPosition, 150, "Return to Stock");

        for (dbProduct product : products)
        {
            headerHeight = printer.addTextRight(Size.Normal, xOffset, finalPosition, 130, String.format("%d", dbEndOfDay.getReturnedQuantity(product)));

            xOffset += 150;
        }

        xOffset = 200;
        finalPosition = headerHeight;

        // Print the closing stock value for each product
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        printer.addTextLeft(Size.Normal, 40, finalPosition, 160, "Finishing");

        for (dbProduct product : products)
        {
            headerHeight = printer.addTextRight(Size.Normal, xOffset, finalPosition, 130, String.format("%d", dbEndOfDay.getFinishingQuantity(product)));

            xOffset += 150;
        }

        xOffset = 200;
        finalPosition = headerHeight;

        DecimalFormat formatVolume = new DecimalFormat("#,##0");

        double startTotalizer = getStartTotalizer(uniqueTripIds.get(0));
        double endTotaliser = getFinishTotalizer(uniqueTripIds.get(uniqueTripIds.size() - 1));

        // Print the totalizer values
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
        printer.addTextLeft(Size.Normal, 40, finalPosition, 140, "Start Totalizer");
        finalPosition = printer.addTextRight(Size.Normal, 200, finalPosition, 200, formatVolume.format(startTotalizer));
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        printer.addTextLeft(Size.Normal, 40, finalPosition, 140, "End Totalizer");
        finalPosition = printer.addTextRight(Size.Normal, 200, finalPosition, 200, formatVolume.format(endTotaliser));
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        printer.addTextLeft(Size.Normal, 40, finalPosition, 140, "Total Delivered");
        finalPosition = printer.addTextRight(Size.Normal, 200, finalPosition, 200, formatVolume.format(endTotaliser - startTotalizer));

        // Print the payments @ the bottom of the report
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.XLarge);

        if (dbEndOfDay.getNumberOfPayments() == 0)
        {
            finalPosition = printTitle(printer, finalPosition, "No Payments Made");
        }
        else
        {
            finalPosition = printTitle(printer, finalPosition, "Payments");

            DecimalFormat formatMoney = new DecimalFormat("#,##0.00");

            // Get the sum of the cash payments
            BigDecimal cashPayments = dbEndOfDay.getCashPayments();

            // Get the sum of the cheque payments
            BigDecimal chequePayments = dbEndOfDay.getChequePayments();

            // Get the sum of the voucher payments
            BigDecimal voucherPayments = dbEndOfDay.getVoucherPayments();

            if (cashPayments.compareTo(BigDecimal.ZERO) > 0)
            {
                finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);

                printer.addTextLeft(Size.Medium, 200, finalPosition, 200, "Cash");

                finalPosition = printer.addTextRight(Size.Medium, 400, finalPosition, 200, formatMoney.format(cashPayments));
            }

            if (chequePayments.compareTo(BigDecimal.ZERO) > 0)
            {
                finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);

                printer.addTextLeft(Size.Medium, 200, finalPosition, 200, "Cheque");

                finalPosition = printer.addTextRight(Size.Medium, 400, finalPosition, 200, formatMoney.format(chequePayments));
            }

            if (voucherPayments.compareTo(BigDecimal.ZERO) > 0)
            {
                finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);

                printer.addTextLeft(Size.Medium, 200, finalPosition, 200, "Voucher");

                finalPosition = printer.addTextRight(Size.Medium, 400, finalPosition, 200, formatMoney.format(voucherPayments));
            }

            finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);

            finalPosition = printer.addLine(400, finalPosition, 600, finalPosition);

            finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);

            BigDecimal total = cashPayments.add(chequePayments).add(voucherPayments);

            finalPosition = printer.addTextRight(Size.Medium, 400, finalPosition, 200, formatMoney.format(total));
        }

        printFooter(printer, finalPosition);

        byte[] ticketImage = printer.addBitmap();

        // Send to printer.
        sendToPrinter(context, printer);

        // Save the Transport Image
        saveLabelImage(context, "EndOfDayLabel", ticketImage);
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
        CrashReporter.leaveBreadcrumb("Printing: printOpeningStock");

		int finalPosition = yPosition;

		// Print the 'Opening Stock' Title
		finalPosition = printTitle(printer, finalPosition, "Opening Stock");

		printer.addTextLeft(Size.Large, 80, finalPosition, 220, "Product");

		dbVehicle vehicle = dbVehicle.FindByNo(Active.trip.Vehicle.No);

		finalPosition = printer.addTextLeft(Size.Large, 550, finalPosition, 250, "On board");

        // Get the original product in the hosereel (if any)
        dbProduct originalHosereelProduct = Active.trip.OriginalHosereelProduct;

        // Get the product stock
        List<dbVehicleStock> stockList = dbVehicleStock.GetStockByProduct(vehicle);

        // If there was any original hosereel product subtract from
        // the OpeningStock value
        if (originalHosereelProduct != null)
        {
            for (dbVehicleStock vehicleStock : stockList)
            {
                if (vehicleStock.Product.getId().equals(originalHosereelProduct.getId()))
                {
                    CrashReporter.leaveBreadcrumb("Printing: printOpeningStock - Subtracting original hosereel capacity from Opening Stock");

                    vehicleStock.OpeningStock -= Active.vehicle.getHosereelCapacity();

                    break;
                }
            }
        }

        // Now print out the opening stock values for each product
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

        // If there was original line stock print this now
        if (originalHosereelProduct != null)
        {
            finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

            finalPosition = printTitle(printer, finalPosition, "Line Stock");

            printer.addTextLeft(Size.Large, 80, finalPosition, 470, originalHosereelProduct.Desc);

            finalPosition = printer.addTextLeft(Size.Large, 550, finalPosition, 250, Integer.toString(Active.vehicle.getHosereelCapacity()));
        }

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

    private static int printStockMatrix(Printer printer, int yPosition)
    {
        CrashReporter.leaveBreadcrumb("Printing: printStockMatrix");

        int finalPosition = yPosition;
        int xOffset = 200;

        // Get the list of vehicle stocks
        List<dbVehicleStock> vehicleStocks = dbVehicleStock.GetStockByProduct(dbVehicle.FindByNo(Active.trip.Vehicle.No));

        // Get the list of stock transactions
        List<dbTripStock> transactions = Active.trip.GetStockTrans();

        // Get the list of products that are on lorry
        List<dbProduct> products = getUniqueProducts(transactions, vehicleStocks);

        int headerHeight = 0;

        // Print the product headers
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

        for (dbProduct product : products)
        {
            headerHeight = printer.addTextRight(Size.Normal, xOffset, finalPosition, 130, product.Desc);

            xOffset += 150;
        }

        xOffset = 200;
        finalPosition = headerHeight;

        // Print the original stock value for each product
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        printer.addTextLeft(Size.Normal, 40, finalPosition, 160, "Starting");

        for (dbProduct product : products)
        {
            headerHeight = printer.addTextRight(Size.Normal, xOffset, finalPosition, 130, String.format("%d", getStartingVolume(vehicleStocks, product)));

            xOffset += 150;
        }

        xOffset = 200;
        finalPosition = headerHeight;

        // Print the Loaded quantities
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        printer.addTextLeft(Size.Normal, 40, finalPosition, 160, "Loaded");

        for (dbProduct product : products)
        {
            headerHeight = printer.addTextRight(Size.Normal, xOffset, finalPosition, 130, String.format("%d", getLoadedVolume(transactions, product)));

            xOffset += 150;
        }

        xOffset = 200;
        finalPosition = headerHeight;

        // Print the delivered quantities
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        printer.addTextLeft(Size.Normal, 40, finalPosition, 150, "Delivery");

        for (dbProduct product : products)
        {
            headerHeight = printer.addTextRight(Size.Normal, xOffset, finalPosition, 130, String.format("%d", getDeliveredVolume(transactions, product)));

            xOffset += 150;
        }

        xOffset = 200;
        finalPosition = headerHeight;

        // Print the returned quantities
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        printer.addTextLeft(Size.Normal, 40, finalPosition, 150, "Return to Stock");

        for (dbProduct product : products)
        {
            headerHeight = printer.addTextRight(Size.Normal, xOffset, finalPosition, 130, String.format("%d", getReturnedVolume(transactions, product)));

            xOffset += 150;
        }

        xOffset = 200;
        finalPosition = headerHeight;

        // Print the closing stock value for each product
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        printer.addTextLeft(Size.Normal, 40, finalPosition, 160, "Finishing");

        for (dbProduct product : products)
        {
            headerHeight = printer.addTextRight(Size.Normal, xOffset, finalPosition, 130, String.format("%d", getFinishingVolume(vehicleStocks, product)));

            xOffset += 150;
        }

        xOffset = 200;
        finalPosition = headerHeight;

        return finalPosition;
    }

	private static int getStartingVolume(List<dbVehicleStock> stockList, dbProduct product)
	{
		CrashReporter.leaveBreadcrumb("Printing: getStartingVolume");

		int quantity = 0;

		// Go through all stock until we find a matching product
		// and return the quantity
		for (dbVehicleStock stock : stockList)
		{
			if (stock != null && stock.Product.ColossusID == product.ColossusID)
			{
				quantity = stock.OpeningStock;

				CrashReporter.leaveBreadcrumb(String.format("Printing: getStartingVolume - Product [%s] : %d litres", product.Desc, quantity));

				break;
			}
		}

		return quantity;
	}

	private static int getFinishingVolume(List<dbVehicleStock> stockList, dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("Printing: getStartingVolume");

        int quantity = 0;

        // Go through all stock until we find a matching product
        // and return the quantity
        for (dbVehicleStock stock : stockList)
        {
            if (stock != null && stock.Product.ColossusID == product.ColossusID)
            {
                quantity = stock.CurrentStock;

                CrashReporter.leaveBreadcrumb(String.format("Printing: getFinishingVolume - Product [%s] : %d litres", product.Desc, quantity));

                break;
            }
        }

        return quantity;
    }

    private static int getLoadedVolume(List<dbTripStock> stockTransactions, dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("Printing: getLoadedVolume");

        int quantity = 0;

        // Loop through all 'Load' transactions calculating the total for the product
        for (dbTripStock stockTransaction : stockTransactions)
        {
            if (stockTransaction.Type.equals("Load"))
            {
                if (stockTransaction.Product.ColossusID == product.ColossusID)
                {
                    quantity += stockTransaction.Quantity;
                }
            }
        }

        return quantity;
    }

    private static int getDeliveredVolume(List<dbTripStock> stockTransactions, dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("Printing: getDelivereVolume");

        int quantity = 0;

        // Loop through all 'Delivery' transactions calculating the total for the product
        for (dbTripStock stockTransaction : stockTransactions)
        {
            if (stockTransaction.Type.equals("Delivery"))
            {
                if (stockTransaction.Product.ColossusID == product.ColossusID)
                {
                    quantity += stockTransaction.Quantity;
                }
            }
        }

        return quantity;
    }

    private static int getReturnedVolume(List<dbTripStock> stockTransactions, dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("Printing: getReturnedVolume");

        int quantity = 0;

        // Loop through all 'Return' transactions calculating the total for the product
        for (dbTripStock stockTransaction : stockTransactions)
        {
            if (stockTransaction.Type.equals("Return"))
            {
                if (stockTransaction.Product.ColossusID == product.ColossusID)
                {
                    quantity += stockTransaction.Quantity;
                }
            }
        }

        return quantity;
    }

    private static List<dbProduct> getUniqueProducts(List<dbTripStock> transactions, List<dbVehicleStock> stocks)
    {
        CrashReporter.leaveBreadcrumb("Printing: getUniqueProducts");

        // Create object to hold list of unique product in stock & transactions
        // to be returned
        List<dbProduct> products =  new ArrayList<dbProduct>();

        for (dbTripStock transaction : transactions)
        {
            if (transaction.Type.equals("Load") || transaction.Type.equals("Return") || transaction.Type.equals("Delivery"))
            {
                if (!products.contains(transaction.Product))
                {
                    CrashReporter.leaveBreadcrumb(String.format("Printing: getUniqueProducts - Adding Product %s", transaction.Product.Desc));

                    products.add(transaction.Product);
                }
            }
        }

        for (dbVehicleStock stock : stocks)
        {
            if (!products.contains(stock.Product))
            {
                CrashReporter.leaveBreadcrumb(String.format("Printing: getUniqueProducts - Adding Product %s", stock.Product.Desc));

                products.add(stock.Product);
            }
        }

        return products;
    }

    private static int printStockTransactions(Printer printer, int yPosition)
	{
        CrashReporter.leaveBreadcrumb("Printing: printStockTransactions");

		int finalPosition = yPosition;

		// Get the List of Stock Transaction objects
		List<dbTripStock> stockTransactions = Active.trip.GetStockTrans();

		if (stockTransactions.size() == 0)
		{
            CrashReporter.leaveBreadcrumb("Printing: printStockTransactions - No Transactions present");

			finalPosition = printTitle(printer, finalPosition, "No Transactions");
		}
		else
		{
            CrashReporter.leaveBreadcrumb("Printing: printStockTransactions - Printing " + stockTransactions.size() + " transactions ...");

			DateFormat formatDate = new SimpleDateFormat("dd-MMM-yyyy");
			DateFormat formatTime = new SimpleDateFormat("HH:mm");

			long date = Utils.getCurrentTime();

			String lastDate = formatDate.format(date);

			String lastGroupBy = "";
			String lastInvoiceNo = "";

			// Add title.
			finalPosition = printTitle(printer, finalPosition, "Transactions");

			// Print each of the Stock Transactions
			for (dbTripStock stockTransaction : stockTransactions)
			{
				// Group by InvoiceNo (if available), otherwise Type.
				String groupBy = stockTransaction.InvoiceNo;

				if (groupBy.length() == 0)
				{
					groupBy = stockTransaction.Type;
				}

                // If the group by has changed print a spacer
				if (!groupBy.equals(lastGroupBy))
				{
                    // Save the group by
					lastGroupBy = groupBy;

					// Add spacer when type changes.
					finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);
				}

                // Output the date if it is different from that stored in last date
				if (!formatDate.format(stockTransaction.Date).equals(lastDate))
				{
					lastDate = formatDate.format(stockTransaction.Date);

					// Print date, if it has changed.
					finalPosition = printer.addTextLeft(Size.Normal, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, lastDate);
					finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
				}

				if (!stockTransaction.InvoiceNo.equals(lastInvoiceNo))
				{
					lastInvoiceNo = stockTransaction.InvoiceNo;

                    // If there is an Invoice No present then print out the
                    // first line of the transaction
					if (stockTransaction.InvoiceNo.length() > 0)
					{
                        // Print the time of the transaction
                        printer.addTextLeft(Size.Normal, TX_DATE_X, finalPosition, TX_DATE_WIDTH, formatTime.format(stockTransaction.Date));

                        StringBuilder builder = new StringBuilder();

                        // Add the invoice number
                        builder.append("Invoice ");
                        builder.append(stockTransaction.InvoiceNo);

                        // If there is a Customer Name add it
                        if (stockTransaction.CustomerName != null && stockTransaction.CustomerName.length() > 0)
                        {
                            builder.append(" ");
                            builder.append(stockTransaction.CustomerName);
                        }

                        // If there is a customer code add it
                        if (stockTransaction.CustomerCode != null && stockTransaction.CustomerCode.length() > 0)
                        {
                            builder.append(" (");
                            builder.append(stockTransaction.CustomerCode);
                            builder.append(")");
                        }

						// Print the invoice and customer details
						finalPosition = printer.addTextLeft(Size.Normal, TX_LINE_X, finalPosition, TX_LINE_WIDTH, builder.toString());

                        // Print small spacer beneath
						finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
					}
				}

				// Print line 2 - Description
				printer.addTextLeft(Size.Normal, TX_DATE_X, finalPosition, TX_DATE_WIDTH, formatTime.format(stockTransaction.Date));
				finalPosition = printer.addTextLeft(Size.Normal, TX_LINE_X, finalPosition, TX_LINE_WIDTH, stockTransaction.Description.trim());
				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

				// Print lines 3+ - Notes
				for (String note : stockTransaction.Notes.split("\n"))
				{
					if (note.length() > 0)
					{
						finalPosition = printer.addTextLeft(Size.Normal, TX_LINE_X, finalPosition, TX_LINE_WIDTH, note.trim());
                        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
					}
				}
			}
		}

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

	private static int printClosingStock(Printer printer, int yPosition)
	{
        CrashReporter.leaveBreadcrumb("Printing: printClosingStock");

		int finalPosition = yPosition;

		finalPosition = printTitle(printer, finalPosition, "Closing Stock");

		// Save the position of the title for future use
		int titlePosition = finalPosition;

		printer.addTextLeft(Size.Large, 80, titlePosition, 220, "Product");

		dbVehicle vehicle = dbVehicle.FindByNo(Active.trip.Vehicle.No);

		finalPosition = printer.addTextLeft(Size.Large, 550, titlePosition, 250, "On board");

        // Get the original product in the hosereel (if any)
        dbProduct hosereelProduct = Active.vehicle.getHosereelProduct();

        List<dbVehicleStock> stockList = dbVehicleStock.GetStockByProduct(vehicle);

        // If there was any original hosereel product subtract from
        // the OpeningStock value
        if (hosereelProduct != null)
        {
            for (dbVehicleStock vehicleStock : stockList)
            {
                if (vehicleStock.Product.getId().equals(hosereelProduct.getId()))
                {
                    CrashReporter.leaveBreadcrumb("Printing: printOpeningStock - Subtracting hosereel capacity from Current Stock");

                    vehicleStock.CurrentStock -= Active.vehicle.getHosereelCapacity();

                    break;
                }
            }
        }

        // Now print out the current stock values for each product
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

        // If there is current line stock print this now
        if (hosereelProduct != null)
        {
            finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

            finalPosition = printTitle(printer, finalPosition, "Line Stock");

            printer.addTextLeft(Size.Large, 80, finalPosition, 470, hosereelProduct.Desc);

            finalPosition = printer.addTextLeft(Size.Large, 550, finalPosition, 250, Integer.toString(Active.vehicle.getHosereelCapacity()));
        }

        return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

	@SuppressWarnings("SuspiciousNameCombination")
	private static int printCashReport(Printer printer, int yPosition)
	{
        CrashReporter.leaveBreadcrumb("Printing: printCashReport");

		DecimalFormat formatMoney = new DecimalFormat("#,##0.00");

		int finalPosition = yPosition;

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.XLarge);

		// Print the 'CASH REPORT' title
		finalPosition = printTitle(printer, finalPosition, "CASH REPORT");

		// Print the Date & Trip number
		finalPosition = printDateAndTripNumber(printer, finalPosition);

		// Vehicle & Driver.
		finalPosition = printVehicleAndDriver(printer, finalPosition);

		int payments = 0;

        // Get the stock transactions for the trip
		List<dbTripStock> stockTransactions = Active.trip.GetStockTrans();

        // Calculate the number of payment transactions
		for (dbTripStock stockTransaction : stockTransactions)
		{
			if (stockTransaction.Type.equals("Payment"))
			{
				payments++;
			}
		}

		if (payments == 0)
		{
            // There were no payment transactions
			finalPosition = printer.addTextCentre(Size.Large, SINGLE_COLUMN_X, finalPosition, SINGLE_COLUMN_WIDTH, "No Transactions");
		}
		else
		{
			// Print titles.
			printer.addTextLeft(Size.Normal, 50, finalPosition, 150, "Invoice no");
			printer.addTextLeft(Size.Normal, 150, finalPosition, 300, "Customer");
			printer.addTextLeft(Size.Normal, 450, finalPosition, 100, "Type");
			finalPosition = printer.addTextRight(Size.Normal, 550, finalPosition, 150, "Amount");

			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);

            double cash = 0;
            double cheques = 0;
            double vouchers = 0;

            // Loop through each transaction looking for payments
            for (dbTripStock stockTransaction : stockTransactions)
			{
                // If it is a payment transaction then print it and
                // also keep a running total of cash, cheques  or vouchers
				if (stockTransaction.Type.equals("Payment"))
				{
                    // Get all payment information in the transaction
					String[] paymentLines = (stockTransaction.Description + "\n" + stockTransaction.Notes).split("\n");

					for (String paymentLine : paymentLines)
					{
						int idx = paymentLine.indexOf(":") + 1;

                        // Remove all commas from the value field so that parsing does not fail
						String strValue = paymentLine.substring(idx).replace(",", "");

						String type = "";

                        // Stores the amount of this payment type for the transaction
						double amount = 0;

                        // Test for cash payment
						if (paymentLine.startsWith("Cash payment:"))
						{
							type = "Cash";
							amount = Double.parseDouble(strValue);
							cash += amount;
						}

                        // Test for cheque payment
						if (paymentLine.startsWith("Cheque payment:"))
						{
							type = "Cheque";
							amount = Double.parseDouble(strValue);
							cheques += amount;
						}

                        // Test for voucher payment
						if (paymentLine.startsWith("Voucher payment:"))
						{
							type = "Voucher";
							amount = Double.parseDouble(strValue);
							vouchers += amount;
						}

						if (type.length() > 0)
						{
							// Add small spacer to give some space between lines
							finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

                            // Print the invoice number of the payment
							printer.addTextLeft(Size.Normal, 50, finalPosition, 150, stockTransaction.InvoiceNo);

                            // Print the customer making the payment
                            String customerDetail = String.format("%s (%s)", stockTransaction.CustomerName, stockTransaction.CustomerCode);
							printer.addTextLeft(Size.Normal, 150, finalPosition, 300, customerDetail);

                            // Prin the type of the payment (cash/cheque/voucher)
							printer.addTextLeft(Size.Normal, 450, finalPosition, 100, type);

                            // Print the amount of the payment
                            finalPosition = printer.addTextRight(Size.Normal, 550, finalPosition, 150, formatMoney.format(amount));
						}
					}
				}
			}

			// Add title.
			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
			finalPosition = printTitle(printer, finalPosition, "Summary");

            // If there was some payments made with cash then output amount
            finalPosition = printPaymentAmount(printer, finalPosition, "Cash", cash);

            // If there was some payments made with cheques then output amount
            finalPosition = printPaymentAmount(printer, finalPosition, "Cheques", cheques);

            // If there was some payments made with vouchers then output amount
            finalPosition = printPaymentAmount(printer, finalPosition, "Vouchers", vouchers);

			// Print total.
			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
			finalPosition = printer.addTextRight(Size.Large, 400, finalPosition, 200, "========");
			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
			finalPosition = printer.addTextRight(Size.Large, 400, finalPosition, 200, formatMoney.format(cash + cheques + vouchers));
		}

		return finalPosition;
	}

    private static int printPaymentAmount(Printer printer, int yPosition, String type, double amount)
    {
        CrashReporter.leaveBreadcrumb("Printing: printPaymentAmount");

        int finalPosition = yPosition;

        DecimalFormat formatMoney = new DecimalFormat("#,##0.00");

        if (amount != 0)
        {
            printer.addTextLeft(Size.Large, 200, finalPosition, 180, type);
            finalPosition = printer.addTextRight(Size.Large, 400, finalPosition, 200, formatMoney.format(amount));

            // Add small spacer
            finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
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

		// Print the Date & Trip
		finalPosition = printDateAndTripNumber(printer, finalPosition);

		// Print Vehicle & Driver details
		finalPosition = printVehicleAndDriver(printer, finalPosition);

		// Print the Opening Stock Section
		finalPosition = printOpeningStock(printer, finalPosition);

		// Print Stock Transactions
		finalPosition = printStockTransactions(printer, finalPosition);

		// Print Closing Stock Section
		finalPosition = printClosingStock(printer, finalPosition);

        // Print the matrix of product loaded/Returned/Delivered
        finalPosition = printStockMatrix(printer, finalPosition);

		// Print Separator before Cash Report
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);
		finalPosition = printer.addLine(finalPosition);

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
		finalPosition = printer.addTextLeft(Size.Large, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, "Delivery/Invoice No:");

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

		printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, order.CustomerCode);
		finalPosition = printer.addTextLeft(Size.Normal, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, order.InvoiceNo);

		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

		// Print the Invoice To & Deliver To Addresses
		finalPosition = printAddresses(printer, finalPosition, order);

        // Print Terms & CustomerType
        printTerms(printer, finalPosition, order.Terms);
        finalPosition = printCustomerType(printer, finalPosition, order.CustomerType);

		// Print the customer order number
		finalPosition = printOrderNumber(printer, finalPosition, order.OrderNumber);

        // Print the Vehicle & Driver
        finalPosition = printVehicleAndDriver(printer, finalPosition);

        // Print the Vehicle Registration

        // Print the Order Lines
		finalPosition = printOrderLines(printer, finalPosition, order);

        if (!order.HidePrices)
        {
            CrashReporter.leaveBreadcrumb("Printing - printBitmapTicket - Printing Ticket Amounts");

            // Print VAT
            Hashtable<BigDecimal, dbTripOrder.VatRow> vatValues = order.getDeliveredVatValues();
            Enumeration e = vatValues.keys();

            while (e.hasMoreElements())
            {
                BigDecimal key = (BigDecimal) e.nextElement();

                dbTripOrder.VatRow row = vatValues.get(key);

                String vatTitle = "VAT @ " + format2dp.format(row.vatPercentage) + " %";
                finalPosition = printTitleAndAmount(printer, finalPosition, vatTitle, row.nettValue.multiply(row.vatPercentage).divide(new BigDecimal(100)));
            }

            // Print account balance
            finalPosition = printTitleAndAmount(printer, finalPosition, "A/c balance", order.getCodAccBalance());

            // Print total
            finalPosition = printTitleAndAmount(printer, finalPosition, "Total", order.getCreditTotal());

            finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

            // Print amount paid at office
            finalPosition = printTitleAndAmount(printer, finalPosition, "Paid office", order.getAmountPrepaid());

            // Print the discount
            finalPosition = printTitleAndAmount(printer, finalPosition, "Discount", order.getDiscount());

            // Print amount paid to driver
            finalPosition = printTitleAndAmount(printer, finalPosition, "Payment Received", order.getPaidDriver());

            // Print any payments outstanding
            finalPosition = printTitleAndAmount(printer, finalPosition, "Outstanding", order.getOutstanding());

            finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
        }

        if (!order.HidePrices)
        {
            // Get the surcharge amount
            BigDecimal surcharge = order.getDeliveredSurchargeValue();

            // Get the amount still to pay
            BigDecimal outstanding = order.getOutstanding();

            // Print the surcharge/discount message if necessary
            if (surcharge.compareTo(BigDecimal.ZERO) != 0 && outstanding.compareTo(BigDecimal.ZERO) > 0)
            {
                finalPosition = printSurchargeMessage(printer, finalPosition, order);
            }

            finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
        }

        // Print the Meter Tickets
        finalPosition = printMeterData(printer, finalPosition, order);

        finalPosition = printer.addLine(finalPosition);

		// Print the customer signature
		if (order.CustomerSignature)
		{
            if (order.UnattendedSignature)
            {
                // Add the signature
                finalPosition = printer.addSignature("Unattended delivery signature", order.CustomerSignatureName, finalPosition, order.CustomerSignatureImage, order.CustomerSignatureDateTime);
            }
            else
            {
                // Add the signature
                finalPosition = printer.addSignature("Customer signature", order.CustomerSignatureName, finalPosition, order.CustomerSignatureImage, order.CustomerSignatureDateTime);
            }
		}

		// Print the driver signature
		if (order.DriverSignature)
		{
			// Add the signature
			finalPosition = printer.addSignature("Driver signature", order.DriverSignatureName, finalPosition, order.DriverSignatureImage, order.DriverSignatureDateTime);
		}

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

    private static int printSurchargeMessage(Printer printer, int yPosition, dbTripOrder order)
    {
        CrashReporter.leaveBreadcrumb("Printing: printSurchargeMessage");

        DateFormat formatDate = new SimpleDateFormat("dd-MMM-yyyy");

        int finalPosition = yPosition;

        CrashReporter.leaveBreadcrumb("Printing: printSurchargeMessage");

        // Get all the order lines
        List<dbTripOrderLine> orderLines = order.GetTripOrderLines();

        // Get the surcharge amount (in Pence per Litre)
        BigDecimal surcharge = orderLines.get(0).getSurcharge();

        BigDecimal totalSurchargeAmount = BigDecimal.ZERO;

        BigDecimal surchargeAmountVat = BigDecimal.ZERO;

        for (dbTripOrderLine line : orderLines)
        {
            BigDecimal surchargeAmount = surcharge.divide(new BigDecimal(100), 10, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(line.DeliveredQty));

            totalSurchargeAmount = totalSurchargeAmount.add(surchargeAmount);

            if (line.DeliveredQty > line.VatPerc2Above)
            {
                surchargeAmountVat = surchargeAmountVat.add(surchargeAmount.multiply(line.getVatPerc2().divide(new BigDecimal(100), 10, BigDecimal.ROUND_HALF_UP)));
            }
            else
            {
                surchargeAmountVat = surchargeAmountVat.add(surchargeAmount.multiply(line.getVatPerc1().divide(new BigDecimal(100), 10, BigDecimal.ROUND_HALF_UP)));
            }
        }

        DecimalFormat formatMoney = new DecimalFormat("#,##0.00");
        DecimalFormat formatPpl = new DecimalFormat("#,##0.00");

        // Print line commencing "Deduct x ppl ..."
        StringBuilder line = new StringBuilder();

        line.append("Deduct ");
        line.append(formatPpl.format(surcharge));
        line.append(" ppl = £");
        line.append(formatMoney.format(totalSurchargeAmount.add(surchargeAmountVat)));
        line.append(" (inc. £");
        line.append(formatMoney.format(surchargeAmountVat));
        line.append(" of VAT)");

        // Print space before the rest of the message
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

        finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, 720, line.toString());

        // Print line commencing "from £xx.xx  if paid by ..."
        line = new StringBuilder();

        line.append("from £");
        line.append(formatMoney.format(order.getCreditTotal()));
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

        BigDecimal nettValue = BigDecimal.ZERO;
        BigDecimal nettValueVat = BigDecimal.ZERO;

        for (dbTripOrderLine orderLine : orderLines)
        {
            nettValue =  nettValue.add(orderLine.getDeliveredNettValue());

            if (orderLine.OrderedQty > orderLine.VatPerc2Above)
            {
                nettValueVat = nettValueVat.add(order.getDeliveredNettValue().multiply(orderLine.getVatPerc2()).divide(new BigDecimal(100), 10, BigDecimal.ROUND_HALF_UP));
            }
            else
            {
                nettValueVat = nettValueVat.add(order.getDeliveredNettValue().multiply(orderLine.getVatPerc1()).divide(new BigDecimal(100), 10, BigDecimal.ROUND_HALF_UP));
            }
        }

        line.append("£");
        line.append(formatMoney.format(nettValue));
        line.append(" + VAT £");
        line.append(formatMoney.format(nettValueVat));
        line.append(" = £");
        line.append(formatMoney.format(nettValue.add(nettValueVat)));

        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);

        finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, 720, line.toString());

        line = new StringBuilder();

        line.append("if paid by ");
        line.append(formatDate.format(order.DueDate));

        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

        finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, 720, line.toString());

        return finalPosition;
    }

    private static int printOrderNumber(Printer printer, int yPosition, String orderNumber)
    {
        CrashReporter.leaveBreadcrumb("Printing: printOrderNumber");

        int finalPosition = yPosition;

        finalPosition = printer.addTextLeft(Size.Large, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Order No:");
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

        if (orderNumber != null && orderNumber.length() > 0)
        {
            finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, orderNumber);
        }
        else
        {
            finalPosition = printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "n/a");
        }

        return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
    }

    private static int printCustomerType(Printer printer, int yPosition, String customerType)
    {
        CrashReporter.leaveBreadcrumb("Printing: printCustomerType");

        int finalPosition = yPosition;

        finalPosition = printer.addTextLeft(Size.Large, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, "Customer Type:");
        finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
        finalPosition = printer.addTextLeft(Size.Normal, RIGHT_COLUMN_X, finalPosition, RIGHT_COLUMN_WIDTH, customerType);

        return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
    }

    @SuppressWarnings("UnusedReturnValue")
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
        CrashReporter.leaveBreadcrumb("Printing: printCustomsStatement");

		int finalPosition = yPosition;

		//noinspection SuspiciousNameCombination
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.XLarge);
		finalPosition = printer.addTextCentre(Size.Normal, 0, finalPosition, 800, "Marked Gas oil and Kerosene");
		finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);
		finalPosition = printer.addTextCentre(Size.Normal, 0, finalPosition, 800, "should not be used in a road vehicle");

		return finalPosition;
	}

	private static int printTitleAndAmount(Printer printer, int yPosition, String title, BigDecimal amount)
	{
        CrashReporter.leaveBreadcrumb("Printing: printTitleAndAmount");

		int finalPosition = yPosition;

		if (amount.compareTo(BigDecimal.ZERO) != 0)
		{
			DecimalFormat formatMoney = new DecimalFormat("#,##0.00");

			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Normal);
			printer.addTextRight(Size.Large, 300, finalPosition, 250, title);
			finalPosition = printer.addTextRight(Size.Large, 590, finalPosition, 170, formatMoney.format(amount));
		}

		return finalPosition;
	}

	private static int printAddresses(Printer printer, int yPosition, dbTripOrder order)
	{
        CrashReporter.leaveBreadcrumb("Printing: printAddresses");

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
        CrashReporter.leaveBreadcrumb("Printing: printAddress");

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
		if (!order.HidePrices)
		{
			printer.addTextRight(Size.Large, 40, finalPosition, 110, "Ordered");
			printer.addTextLeft(Size.Large, 170, finalPosition, 150, "Product");
			printer.addTextRight(Size.Large, 340, finalPosition, 140, "Delivered");
			printer.addTextRight(Size.Large, 500, finalPosition, 110, "PPL");
			finalPosition = printer.addTextRight(Size.Large, 630, finalPosition, 130, "Value");
		}
		else
		{
			printer.addTextRight(Size.Large, 40, finalPosition, 110, "Ordered");
			printer.addTextLeft(Size.Large, 170, finalPosition, 150, "Product");
			finalPosition = printer.addTextRight(Size.Large, 340, finalPosition, 140, "Delivered");
		}

		finalPosition = printer.addLine(finalPosition + 10);

		// Find order lines.
		List<dbTripOrderLine> lines = order.GetTripOrderLines();

		// Process each Order Line
		for (dbTripOrderLine line : lines)
		{
			finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

            // Print the volume ordered
            printer.addTextRight(Size.Large, 40, finalPosition, 110, Integer.toString(line.OrderedQty));

            // Print the Product Description
			printer.addTextLeft(Size.Large, 170, finalPosition, 150, line.Product.Desc);

            // Print the Delivered Quantity in litres
			printer.addTextRight(Size.Large, 340, finalPosition, 140, Integer.toString(line.DeliveredQty));

            if (!order.HidePrices)
            {
                // Get the Delivered price include surcharge (in PPL).
                BigDecimal deliveredPrice = line.getPriceDelivered();

                if (deliveredPrice.compareTo(BigDecimal.ZERO) != 0)
                {
                    // Output the price in ppl to 3 decimal places
                    printer.addTextRight(Size.Large, 500, finalPosition, 110, format3dp.format(deliveredPrice.multiply(line.getRatio())));
                }

                // Get the value of the delivered product (in pounds)
                BigDecimal deliveredValue = line.getDeliveredNettValue().add(line.getDeliveredSurchargeValue());

                if (deliveredValue.compareTo(BigDecimal.ZERO) != 0)
                {
                    // Output the value in pounds to 2 dp
                    printer.addTextRight(Size.Large, 630, finalPosition, 130, format2dp.format(deliveredValue));
                }
            }
		}

		return printer.addSpacer(finalPosition, Printer.SpacerHeight.Large);
	}

    private static int printMeterData(Printer printer, int yPosition, dbTripOrder order) throws Exception
    {
        CrashReporter.leaveBreadcrumb("Printing: printMeterData");

        SimpleDateFormat formatDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        DecimalFormat formatVolume = new DecimalFormat("#,##0");

        int finalPosition = yPosition;

        // Process each Order Line
        for (dbTripOrderLine line : order.GetTripOrderLines())
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

				try
				{
					Date startTime = formatDate.parse(line.ticketStartTime);

					printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Start");
					finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, formatDate.format(startTime));
				}
				catch (ParseException e)
				{
					printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Start");
					finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, line.ticketStartTime);
				}

				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

                try
                {
                    Date finishTime = formatDate.parse(line.ticketFinishTime);

                    printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Finish");
                    finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, formatDate.format(finishTime));
                }
                catch (ParseException e)
                {
                    printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Finish");
                    finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, line.ticketFinishTime);
                }

				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

                printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Totalizer start");
				finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, formatVolume.format(line.ticketStartTotaliser));

				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

				printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Totalizer end");
				finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, formatVolume.format(line.ticketEndTotaliser));

				finalPosition = printer.addSpacer(finalPosition, Printer.SpacerHeight.Small);

				if (line.ticketAt15Degrees)
				{
					printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Volume delivered @ 15.0 C");
					finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, formatVolume.format(line.ticketNetVolume));
				}
				else
				{
                    DecimalFormat formatTemperature = new DecimalFormat("#,##0.0");

                    printer.addTextLeft(Size.Normal, LEFT_COLUMN_X, finalPosition, LEFT_COLUMN_WIDTH, "Volume delivered @ " + formatTemperature.format(line.ticketTemperature) + " C");
					finalPosition = printer.addTextRight(Size.Normal, RIGHT_COLUMN_X, finalPosition, 250, formatVolume.format(line.ticketGrossVolume));
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
		CrashReporter.leaveBreadcrumb("Printing : compressBytes");

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
		CrashReporter.leaveBreadcrumb("Printing : saveLabelImage");

		Intent i = new Intent(context, ColossusIntentService.class);

		// Compress the PCX content and encode as Base64
		String base64Content = Base64.encodeToString(compressBytes(content), Base64.DEFAULT);

		CrashReporter.leaveBreadcrumb("Printing : saveLabelImage - Label Type : " + labelType);

		JSONObject json = new JSONObject();

		long now = Utils.getCurrentTime();

		json.put("DateTime", "/Date(" + now + ")/");
		json.put("Image", base64Content);

        if (labelType.equals("TransportLabel"))
        {
            json.put("TripID", Active.trip.ColossusID);
        }
        else if (labelType.equals("TripLabel"))
        {
            json.put("TripID", Active.trip.ColossusID);
        }
        else if (labelType.equals("TicketLabel"))
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
