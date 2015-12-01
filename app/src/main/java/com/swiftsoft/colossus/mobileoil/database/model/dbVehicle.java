package com.swiftsoft.colossus.mobileoil.database.model;

import android.graphics.Color;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.swiftsoft.colossus.mobileoil.Active;
import com.swiftsoft.colossus.mobileoil.CrashReporter;
import com.swiftsoft.colossus.mobileoil.Utils;
import com.swiftsoft.colossus.mobileoil.bluetooth.MeterMate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.List;

@Table(name = "Vehicle")
public class dbVehicle extends Model
{
	@Column(name = "ColossusID")
	public int ColossusID;
	
	@Column(name = "No")
	public int No;
	
	@Column(name = "Reg")
	public String Reg;

	// 0 = None, 1 = EMR3, 2 = a.n.other
	@Column(name = "MeterType")
	public int MeterType;

	// If true, keep track of stock by compartment.
	@Column(name = "StockByCompartment")
	public boolean StockByCompartment;
	
	// Compartment 0 - optional wet line hosereel
	@Column(name = "C0_Capacity")
	public int C0_Capacity;
	
	// Compartment 1
	@Column(name = "C1_Capacity")
	public int C1_Capacity;
	
	// Compartment 2
	@Column(name = "C2_Capacity")
	public int C2_Capacity;
	
	// Compartment 3
	@Column(name = "C3_Capacity")
	public int C3_Capacity;

	// Compartment 4
	@Column(name = "C4_Capacity")
	public int C4_Capacity;
	
	// Compartment 5
	@Column(name = "C5_Capacity")
	public int C5_Capacity;
	
	// Compartment 6
	@Column(name = "C6_Capacity")
	public int C6_Capacity;
	
	// Compartment 7
	@Column(name = "C7_Capacity")
	public int C7_Capacity;
	
	// Compartment 8
	@Column(name = "C8_Capacity")
	public int C8_Capacity;
	
	// Compartment 9
	@Column(name = "C9_Capacity")
	public int C9_Capacity;

	//
	// Static methods
	//

	public static void DeleteAll()
	{
		new Delete().from(dbVehicle.class).execute();
	}

	public static List<dbVehicle> GetAll()
	{
		return new Select().from(dbVehicle.class).execute();
	}

	public static dbVehicle FindByNo(int No)
	{
		return new Select().from(dbVehicle.class).where("No=?", No).executeSingle();
	}
	
	public static dbVehicle FindByColossusID(int ColossusID)
	{
		return new Select().from(dbVehicle.class).where("ColossusID=?", ColossusID).executeSingle();
	}

	//
	// Non-static methods.
	//
	
	// Find all checklists for this vehicle.
	public List<dbVehicleChecklist> GetVehicleChecklists() 
	{
		return getMany(dbVehicleChecklist.class, "Vehicle");
	}

	// 
	// Physical compartments.
	// (including compartment 0 - optional wet line hosereel)
	//
	
	public boolean getHasHosereel()
	{
		return C0_Capacity > 0;
	}
	
	public int getHosereelCapacity()
	{
		initCompartmentArrays();

		return getHasHosereel() ? getCompartmentCapacity(0) : 0;
	}
	
	public dbProduct getHosereelProduct()
	{
		initCompartmentArrays();

		return getHasHosereel() ? getCompartmentProduct(0) : null;
	}

	public int getCompartmentStartIdx()
	{
		initCompartmentArrays();

		return getHasHosereel() ? 1 : 0;
	}
	
	public int getCompartmentEndIdx()
	{
		initCompartmentArrays();
		
		return noCompartments;
	}
	
	public int getCompartmentCount()
	{
		initCompartmentArrays();
		
		return (getCompartmentEndIdx() - getCompartmentStartIdx());
	}

	public int getCompartmentNo(int compartmentIdx)
	{
		initCompartmentArrays();

		return compartmentIdx >= noCompartments ? -1 : compartmentNumber[compartmentIdx];
	}
	
	public int getCompartmentCapacity(int compartmentIdx)
	{
		initCompartmentArrays();

		return compartmentIdx >= noCompartments ? 0 : compartmentCapacity[compartmentIdx];
	}
	
	public int getCompartmentOnboard(int compartmentIdx)
	{
		initCompartmentArrays();

		return compartmentIdx >= noCompartments ? 0 : compartmentOnboard[compartmentIdx];
	}

	public dbProduct getCompartmentProduct(int compartmentIdx)
	{
		initCompartmentArrays();

		return compartmentIdx >= noCompartments ? null : compartmentProduct[compartmentIdx];
	}

	public int getCompartmentColour(int compartmentIdx)
	{
		initCompartmentArrays();

		if (compartmentIdx >= noCompartments || compartmentProduct[compartmentIdx] == null)
		{
			return Color.BLACK;
		}

		return compartmentProduct[compartmentIdx].Colour;
	}
	
	public void updateCompartmentOnboard(int compartmentIdx, int stockAdjustment)
	{	
		initCompartmentArrays();
		
		if (compartmentIdx >= noCompartments)
		{
			return;
		}
		
		// Set array value.
		compartmentOnboard[compartmentIdx] += stockAdjustment;
		
		// Update database.
		dbVehicleStock vs = dbVehicleStock.FindOrCreateByVehicleCompartment(this, compartmentNumber[compartmentIdx]);
		vs.CurrentStock += stockAdjustment;
		vs.save();
	}

	public void updateCompartmentProduct(int compartmentIdx, dbProduct product)
	{
		initCompartmentArrays();
		
		if (compartmentIdx >= noCompartments)
		{
			return;
		}
		
		// Set array value.
		compartmentProduct[compartmentIdx] = product;
		
		// Update database.
		dbVehicleStock vs = dbVehicleStock.FindOrCreateByVehicleCompartment(this, compartmentNumber[compartmentIdx]);
		vs.Product = product;
		vs.save();
	}
	
	public void validateCompartment(int compartmentIdx)
	{
		initCompartmentArrays();
		
		if (compartmentIdx >= noCompartments)
		{
			return;
		}

		// If stock now zero, clear product.
		if (compartmentOnboard[compartmentIdx] == 0)
		{
			updateCompartmentProduct(compartmentIdx, null);
		}
	}

	// ----- Non-compartment stock -----
	
	private void updateVehicleStock(dbProduct product, int stockAdjustment)
	{
		// Update database.
		dbVehicleStock vs = dbVehicleStock.FindOrCreateByVehicleProduct(this, product);
		vs.CurrentStock += stockAdjustment;
		vs.save();
	}
	
	private void updateLineStock(dbProduct fromProduct, dbProduct toProduct, int litres) throws Exception
	{
		// lc to gas oil from kero
		
		if (toProduct == null)
			throw new Exception("updateLineStock: invalid parameters");
		
		// Reduce toProduct stock. e.g. gas oil
		updateVehicleStock(toProduct, 0 - litres);
		
		// Increase fromProduct stock. e.g. kero
		updateVehicleStock(fromProduct, litres);

		// Find existing line stock. e.g. kero
		dbVehicleStock lineVs = dbVehicleStock.FindOrCreateByVehicleCompartment(this, 0);

		// Find stock for fromProduct. e.g. kero
		dbVehicleStock fromVs = dbVehicleStock.FindOrCreateByVehicleProduct(this, fromProduct);
		fromVs.OpeningStock += lineVs.OpeningStock;
		fromVs.save();
		
		// Change line to toProduct.
		updateCompartmentProduct(0, toProduct);
		
		// and matching VehicleStock row.
		lineVs.OpeningStock = 0;
		lineVs.Product = toProduct;
		lineVs.save();
	}
	
	//
	// ***** Stock *****
	// 

	//
	// ----- Load -----
	//

	// If stockbycompartment is false.
	public void recordLoad(dbProduct product, int litres)
	{
		// Add product to stock.
		updateVehicleStock(product, litres);
		
		// Write stock transaction.
		dbTripStock lcTripStock = new dbTripStock();
		lcTripStock.Trip = Active.trip;
		lcTripStock.Type = "Load";
		lcTripStock.Date = Utils.getCurrentTime();
		lcTripStock.InvoiceNo = "";
		lcTripStock.CustomerCode = "";
		lcTripStock.TicketNo = 0;	// Unmetered by truck.
		lcTripStock.Description = "Loaded " + litres + " of " + product.Desc;
		lcTripStock.Notes = "";
		lcTripStock.save();
	}

	// If stockbycompartment is false.
	public void recordReturn(dbProduct product, int litres, boolean viaMeterMate)
	{
		long ticketNo = 0;
		
		if (viaMeterMate)
		{
			ticketNo = Long.parseLong(MeterMate.getTicketNo());
		}
		
		// Remove product from stock.
		updateVehicleStock(product, 0 - litres);
		
		// Write stock transaction.
		dbTripStock lcTripStock = new dbTripStock();
		lcTripStock.Trip = Active.trip;
		lcTripStock.Type = "Return";
		lcTripStock.Date = Utils.getCurrentTime();
		lcTripStock.InvoiceNo = "";
		lcTripStock.CustomerCode = "";
		lcTripStock.TicketNo = ticketNo;

		lcTripStock.Description = ticketNo == 0 ? "Returned " + litres + " of " + product.Desc : "Ticket #" + ticketNo + " returned " + litres + " of " + product.Desc;
		
		lcTripStock.Notes = "";
		lcTripStock.save();
	}


	//
	// ----- Line change -----
	//
	
	// If stockbycompartment is false.
	public void recordLineChange(dbProduct toProduct, int litres, String ticketNo) throws Exception
	{
		// Ensure there is always product in the hosereel!		
		dbProduct lineProduct = getHosereelProduct();

		// Change product in line.
		updateLineStock(lineProduct, toProduct, litres);

		// Write stock movement transactions.
		recordLineChangeTran(lineProduct, -1, toProduct, -1, litres, ticketNo);
	}

	private void recordLineChangeTran(dbProduct fromProduct, int fromCompartmentNo, dbProduct toProduct, int toCompartmentNo, int litres, String ticketNo)
	{
		// Write stock transaction.
		dbTripStock lcTripStock = new dbTripStock();
		lcTripStock.Trip = Active.trip;
		lcTripStock.Type = "Line change";
		lcTripStock.Date = Utils.getCurrentTime();
		lcTripStock.InvoiceNo = Active.order.InvoiceNo;
		lcTripStock.CustomerCode = "";

		if (ticketNo.equals("0"))
		{
			// Line change without a ticket, i.e. during delivery.
			lcTripStock.TicketNo = 0;
			lcTripStock.Description = "Line changed during delivery";
			lcTripStock.Notes = "  from " + fromProduct.Desc + " to " + toProduct.Desc;
		}
		else if (ticketNo.equals("C"))
		{
			// Line change correction.
			lcTripStock.TicketNo = 0;
			lcTripStock.Description = "Line product CORRECTED";
			lcTripStock.Notes = "  from " + fromProduct.Desc + " to " + toProduct.Desc;
		}
		else
		{
			lcTripStock.TicketNo = Long.parseLong(ticketNo);
			lcTripStock.Description = "Ticket #" + ticketNo + " line change";
			lcTripStock.Notes = "  from " + fromProduct.Desc + " to " + toProduct.Desc + "\n" + "  using " + litres + " litres.";
		}

		lcTripStock.save();
	}
	
	//
	// ----- Delivery -----
	//
	
	// If stockbycompartment is false.
	public void recordDelivery()
	{
        CrashReporter.leaveBreadcrumb("dbVehicle: recordDelivery");

		// Remove product from stock.
		updateVehicleStock(Active.orderLine.Product, 0 - Active.orderLine.DeliveredQty);

		// Write stock movement transactions.
		recordDeliveryTransaction();
	}

	private void recordDeliveryTransaction()
	{
		long ticketNo = 0;

		// Get ticketNo.
		try
		{
			ticketNo = Long.parseLong(Active.orderLine.ticketNo);
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		
		// Write stock transaction.
		dbTripStock lcTripStock = new dbTripStock();
		lcTripStock.Trip = Active.trip;
		lcTripStock.Type = "Delivery";
		lcTripStock.Date = Utils.getCurrentTime();
		lcTripStock.InvoiceNo = Active.order.InvoiceNo;
		lcTripStock.CustomerCode = Active.order.CustomerCode;
		lcTripStock.CustomerName = Active.order.CustomerName;
		lcTripStock.TicketNo = ticketNo;

		lcTripStock.Description = ticketNo == 0 ? "Delivered " + Active.orderLine.DeliveredQty + " of " + Active.orderLine.Product.Desc : "Ticket #" + Active.orderLine.ticketNo + " delivered " + Active.orderLine.DeliveredQty + " of " + Active.orderLine.Product.Desc;
		
		lcTripStock.Notes = "";
		lcTripStock.save();
	}

    //
    // ----- NonDelivery -----
    //

    public void recordNonDeliveryTransaction(int reasonCode, String customReason)
    {
        // Write Stock Transaction
        dbTripStock tripStock = new dbTripStock();

        tripStock.Trip = Active.trip;
        tripStock.Type = "NonDelivery";
        tripStock.Date = Utils.getCurrentTime();
        tripStock.InvoiceNo = Active.order.InvoiceNo;
        tripStock.CustomerCode = Active.order.CustomerCode;
        tripStock.CustomerName = Active.order.CustomerName;
        tripStock.Description = "Non-delivery of order";

        tripStock.Notes = String.format("%d - %s", reasonCode, getReasonCodeDescription(reasonCode));

        // If it is a custom reason code add the custom reason
        if (reasonCode == 6)
        {
            tripStock.Notes += "\n" + customReason;
        }

        tripStock.save();
    }

    private String getReasonCodeDescription(int reasonCode)
    {
        switch (reasonCode)
        {
            case 0:
                return "Gate locked";
            case 1:
                return "Car(s) blocking entrance";
            case 2:
                return "Tank locked";
            case 3:
                return "Dog in garden";
            case 4:
                return "Requires payment on delivery";
            case 5:
                return "No access";
            case 6:
                return "Other";
            default:
                return "Unknown";
        }
    }
	
	//
	// ----- Stock -----
	//
	
	public String buildStock()
	{
		try
		{
			// Create content.
			JSONArray stock = new JSONArray();

			for (dbVehicleStock vehicleStock : dbVehicleStock.GetAll(this))
			{
				JSONObject stockLine = new JSONObject();
				stockLine.put("ProductID", vehicleStock.Product.ColossusID);
				stockLine.put("Compartment", vehicleStock.Compartment);
				stockLine.put("Quantity", vehicleStock.CurrentStock);
	
				stock.put(stockLine);
			}
	
			JSONObject json = new JSONObject();
			json.put("VehicleID", this.ColossusID);
			json.put("Stock", stock);

			return json.toString();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
		
		return "";
	}
	
	//
	// ----- Payment -----
	//
	
	public void recordPayments()
	{
		String notes = "";
		int noOfPaymentMethods = 0;
		DecimalFormat decf2 = new DecimalFormat("#,##0.00");

		String desc = "Payment total: " + decf2.format(Active.order.getPaidDriver());

		if (Active.order.CashReceived != 0)
		{
			noOfPaymentMethods++;
			notes += "Cash payment: " + decf2.format(Active.order.CashReceived) + "\n";
		}
		
		if (Active.order.ChequeReceived != 0)
		{
			noOfPaymentMethods++;
			notes += "Cheque payment: " + decf2.format(Active.order.ChequeReceived) + "\n";
		}

		if (Active.order.VoucherReceived != 0)
		{
			noOfPaymentMethods++;
			notes += "Voucher payment: " + decf2.format(Active.order.VoucherReceived) + "\n";
		}
		
		if (noOfPaymentMethods > 0)
		{
			if (noOfPaymentMethods == 1)
			{
				desc = notes;
				notes = "";
			}		
			
			// Write stock transaction.
			dbTripStock lcTripStock = new dbTripStock();
			lcTripStock.Trip = Active.trip;
			lcTripStock.Type = "Payment";
			lcTripStock.Date = Utils.getCurrentTime();
			lcTripStock.InvoiceNo = Active.order.InvoiceNo;
			lcTripStock.CustomerCode = Active.order.CustomerCode;
            lcTripStock.CustomerName = Active.order.CustomerName;
			lcTripStock.TicketNo = 0;
			lcTripStock.Description = desc;
			lcTripStock.Notes = notes;
			lcTripStock.save();
		}
	}	
	
	// Helper section to turn compartments into arrays.
	
	private boolean initialised = false;
	private int noCompartments = 0;
	private int[] compartmentNumber;
	private int[] compartmentCapacity;
	private int[] compartmentOnboard;
	private dbProduct[] compartmentProduct;
	
	private void initCompartmentArrays()
	{
		if (initialised)
		{
			return;
		}
		
		initialised = true;
		
		// Calculate number of compartments.
		noCompartments = 0;
		if (C0_Capacity > 0) noCompartments++;
		if (C1_Capacity > 0) noCompartments++;
		if (C2_Capacity > 0) noCompartments++;
		if (C3_Capacity > 0) noCompartments++;
		if (C4_Capacity > 0) noCompartments++;
		if (C5_Capacity > 0) noCompartments++;
		if (C6_Capacity > 0) noCompartments++;
		if (C7_Capacity > 0) noCompartments++;
		if (C8_Capacity > 0) noCompartments++;
		if (C9_Capacity > 0) noCompartments++;
		
		// Allocate arrays.
		compartmentNumber = new int[noCompartments];
		compartmentCapacity = new int[noCompartments];
		compartmentOnboard = new int[noCompartments];
		compartmentProduct = new dbProduct[noCompartments];
		
		int compartmentIdx = 0;
		if (C0_Capacity > 0) setupVehicleStock(compartmentIdx++, 0, C0_Capacity);
		if (C1_Capacity > 0) setupVehicleStock(compartmentIdx++, 1, C1_Capacity);
		if (C2_Capacity > 0) setupVehicleStock(compartmentIdx++, 2, C2_Capacity);
		if (C3_Capacity > 0) setupVehicleStock(compartmentIdx++, 3, C3_Capacity);
		if (C4_Capacity > 0) setupVehicleStock(compartmentIdx++, 4, C4_Capacity);
		if (C5_Capacity > 0) setupVehicleStock(compartmentIdx++, 5, C5_Capacity);
		if (C6_Capacity > 0) setupVehicleStock(compartmentIdx++, 6, C6_Capacity);
		if (C7_Capacity > 0) setupVehicleStock(compartmentIdx++, 7, C7_Capacity);
		if (C8_Capacity > 0) setupVehicleStock(compartmentIdx++, 8, C8_Capacity);
		if (C9_Capacity > 0) setupVehicleStock(compartmentIdx++, 9, C9_Capacity);
	}

	private void setupVehicleStock(int compartmentIdx, int compartmentNo, int capacity)
	{
		dbVehicleStock vs = dbVehicleStock.FindOrCreateByVehicleCompartment(this, compartmentNo);
		
		compartmentNumber[compartmentIdx] = compartmentNo;
		compartmentCapacity[compartmentIdx] = capacity;
		compartmentProduct[compartmentIdx] = vs.Product;
		compartmentOnboard[compartmentIdx] = vs.CurrentStock;
	}
}
