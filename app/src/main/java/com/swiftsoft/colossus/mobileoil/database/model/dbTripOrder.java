package com.swiftsoft.colossus.mobileoil.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.swiftsoft.colossus.mobileoil.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

@Table(name = "TripOrder")
public class dbTripOrder extends Model
{
	@Column(name = "ColossusID")
	public int ColossusID;

	@Column(name = "Trip")
	public dbTrip Trip;

	// Indicates order deliveries should be made in.
	@Column(name = "DeliveryOrder")
	public int DeliveryOrder;
	
	@Column(name = "InvoiceNo")
	public String InvoiceNo;

	@Column(name = "BrandID")
	public int BrandID;
	
	@Column(name = "CustomerCode")
	public String CustomerCode;
	
	@Column(name = "CustomerName")
	public String CustomerName;

	@Column(name = "CustomerAddress")
	public String CustomerAddress;

	@Column(name = "CustomerPostcode")
	public String CustomerPostcode;
	
	@Column(name = "DeliveryName")
	public String DeliveryName;

	@Column(name = "DeliveryAddress")
	public String DeliveryAddress;

	@Column(name = "DeliveryPostcode")
	public String DeliveryPostcode;

	@Column(name = "DeliveryLatitude")
	public double DeliveryLatitude;
	
	@Column(name = "DeliveryLongitude")
	public double DeliveryLongitude;
	
	@Column(name = "PhoneNos")
	public String PhoneNos;

	@Column(name = "RequiredBy")
	public String RequiredBy;

	@Column(name = "Terms")
	public String Terms;
	
	@Column(name = "DueDate")
	public long DueDate;
	
	@Column(name = "PrepaidAmount")
	public double PrepaidAmount;
	
	@Column(name = "CodPoint")
	public int CodPoint;
	
	@Column(name = "CodType")
	public int CodType;
	
	@Column(name = "CodAmount")
	public double CodAmount;

	@Column(name = "Notes")
	public String Notes;
	
	//
	// Modified by app
	//
	
	@Column(name = "Delivering")
	public boolean Delivering;
	
	@Column(name = "Delivered")
	public boolean Delivered;

	// Indicates order deliveries were made in.
	@Column(name = "DeliveryNo")
	public int DeliveryNo;
	
	@Column(name = "DeliveryDate")
	public long DeliveryDate;
	
	@Column(name = "CustomerSignature")
	public boolean CustomerSignature;

	@Column(name = "CustomerSignatureName")
	public String CustomerSignatureName;

	@Column(name = "CustomerSignatureImage")
	public byte[] CustomerSignatureImage;
	
	@Column(name = "CustomerSignatureDateTime")
	public long CustomerSignatureDateTime;

    @Column(name = "CustomerType")
    public String CustomerType;

	@Column(name = "OrderNumber")
	public String OrderNumber;

    @Column(name = "HidePrices")
    public boolean HidePrices;
	
	@Column(name = "CashReceived")
	public double CashReceived;
	
	@Column(name = "ChequeReceived")
	public double ChequeReceived;
	
	@Column(name = "VoucherReceived")
	public double VoucherReceived;
	
	@Column(name = "Discount")
	public double Discount;

	@Column(name = "DriverSignature")
	public boolean DriverSignature;

	@Column(name = "DriverSignatureName")
	public String DriverSignatureName;

	@Column(name = "DriverSignatureImage")
	public byte[] DriverSignatureImage;
	
	@Column(name = "DriverSignatureDateTime")
	public long DriverSignatureDateTime;

	// Static methods.
	
	public static void DeleteAll()
	{
		new Delete().from(dbTripOrder.class).execute();
	}

	// Find trip by ColossusID.
	public static dbTripOrder FindByColossusID(int ColossusID)
	{
		return new Select().from(dbTripOrder.class).where("ColossusID=?", ColossusID).executeSingle();
	}
	
	public static List<dbTripOrder> GetAll()
	{
		return new Select().from(dbTripOrder.class).execute();
	}

	// Non-static methods.

	public class VatRow
	{
		public double vatPerc;
		public double nettValue;
	};

	// Find all order lines.
	public List<dbTripOrderLine> GetTripOrderLines() 
	{
		return getMany(dbTripOrderLine.class, "TripOrder");
	}

	public String getTerms()
	{
		String terms = this.Terms;
		
		if (CodPoint == 1)
			terms += ", after delivery";
		
		if (CodPoint == 2)
			terms += ", before delivery";
		
		return terms;
	}
	
	// 
	// Return COD 'before delivery' value 
	//
	public double getCodBeforeDeliveryValue()
	{
		double cod = 0;

		// Before delivery.
		if (CodPoint == 2)
		{
			if (CodType == 1)
				cod = getOrderedNettValue() + getOrderedVatValue();
			
			if (CodType == 2)
				cod = getOrderedNettValue() + getOrderedVatValue() + CodAmount;
			
			if (CodType == 3)
				cod = CodAmount;
		}	

		return cod;
	}
	
	// 
	// Return COD 'after delivery' value. 
	//
	public double getCodAfterDeliveryValue()
	{
		double cod = 0;

		// Before/After delivery.
		if (CodPoint != 0)
		{
			if (CodType == 1)
				cod = getDeliveredNettValue() + getDeliveredVatValue();
			
			if (CodType == 2)
				cod = getDeliveredNettValue() + getDeliveredVatValue() + CodAmount;
			
			if (CodType == 3)
				cod = CodAmount;
		}	

		return cod;
		// After delivery, all customers can pay the cash price if they like.
		// return getDeliveredNettValue() + getDeliveredVatValue();
	}
	
	//
	// Return COD 'Acc balance' value, which is held in CodAmount if CodType is 2.
	//
	public double getCodAccBalance()
	{
		if (CodPoint != 0 && CodType == 2)
			return CodAmount;
		
		return 0;
	}

	//
	// Return count of undelivered OrderLines
	//
	public int getUndeliveredCount()
	{
		int counter = 0;
		for (dbTripOrderLine line : this.GetTripOrderLines())
		{
			// Don't count non-deliverable products e.g. credit card fees.
			if (line.Product.MobileOil == 3)
				continue;
			
			if (line.DeliveryDate == 0)
				counter++;
		}
				
		return counter;
	}
	
	//
	// Ordered values.
	//
	
	public double getOrderedNettValue()
	{
		double value = 0;
		for (dbTripOrderLine line : this.GetTripOrderLines())
			value += line.getOrderedNettValue();
		
		return value;
	}
	
	public double getOrderedSurchargeValue()
	{
		double value = 0;
		for (dbTripOrderLine line : this.GetTripOrderLines())
			value += line.getOrderedSurchargeValue();
		
		return value;
	}
	
	public double getOrderedVatValue()
	{
		List<VatRow> vatRows = new ArrayList<VatRow>();
		
		// Summarise all lines by VatPerc.
		for (dbTripOrderLine line : this.GetTripOrderLines())
		{
			AddToVatTable(vatRows, line.getOrderedVatPerc(), line.getOrderedNettValue());
			
			if (line.getOrderedSurchargeValue() != 0)
				AddToVatTable(vatRows, 0, line.getOrderedSurchargeValue());
		}

		// Calculate VAT value.
		double vatValue = 0;
		for (int i = 0; i < vatRows.size(); i++)
		{
			VatRow vatRow = vatRows.get(i);
			double vat = vatRow.nettValue * vatRow.vatPerc / 100.0;
			vatValue += Utils.RoundNearest(vat, 2);
		}		
		
		return vatValue;
	}

	//
	// Delivered values.
	//

	public boolean getDeliveredQtyVariesFromOrdered()
	{
		for (dbTripOrderLine line : this.GetTripOrderLines())
			if (line.getDeliveredQtyVariesFromOrdered())
				return true;
		
		return false;
	}
	
	public double getDeliveredNettValue()
	{
		double value = 0;

		for (dbTripOrderLine line : this.GetTripOrderLines())
        {
            value += line.getDeliveredNettValue();
        }
		
		return value;
	}
	
	public double getDeliveredSurchargeValue()
	{
		double value = 0;

		for (dbTripOrderLine line : this.GetTripOrderLines())
        {
            value += line.getDeliveredSurchargeValue();
        }
		
		return value;
	}

    private static void addRow(Hashtable<Double, VatRow> table, VatRow row)
    {
        if (!table.containsKey(row.vatPerc))
        {
            table.put(row.vatPerc, row);
        }
        else
        {
            VatRow foundRow = table.get(row.vatPerc);

            foundRow.nettValue += row.nettValue;
        }
    }

	public Hashtable<Double, VatRow> getDeliveredVatValues()
	{
		Hashtable<Double, VatRow> vatRows = new Hashtable<Double, VatRow>();

        for (dbTripOrderLine line : GetTripOrderLines())
        {
            VatRow row = new VatRow();

            row.nettValue = line.getDeliveredNettValue();
            row.vatPerc = line.getDeliveredVatPerc();

            addRow(vatRows, row);

            if (line.getDeliveredSurchargeValue() != 0)
            {
                row = new VatRow();

                row.nettValue = line.getDeliveredSurchargeValue();
                row.vatPerc = line.getDeliveredVatPerc();

                addRow(vatRows, row);
            }
        }

		return vatRows;
	}
	
	public double getDeliveredVatValue()
	{
		List<VatRow> vatRows = new ArrayList<VatRow>();
		
		// Summarise all lines by VatPerc.
		for (dbTripOrderLine line : this.GetTripOrderLines())
		{
			AddToVatTable(vatRows, line.getDeliveredVatPerc(), line.getDeliveredNettValue());
			
			if (line.getDeliveredSurchargeValue() != 0)
			{
				AddToVatTable(vatRows, line.getDeliveredVatPerc(), line.getDeliveredSurchargeValue());
			}
		}

		// Calculate VAT value.
		double vatValue = 0;

		for (int i = 0; i < vatRows.size(); i++)
		{
			VatRow vatRow = vatRows.get(i);
			double vat = vatRow.nettValue * vatRow.vatPerc / 100.0;
			vatValue += vat;
		}		
		
		return Utils.RoundNearest(vatValue, 2);
	}

	//
	// Common
	//
	
	private void AddToVatTable(List<VatRow> vatRows, double vatPerc, double nettValue)
	{
		VatRow vatRow = null;
		
		// Search for existing row.
		for (int i = 0; i < vatRows.size(); i++)
		{
			if (vatRows.get(i).vatPerc == vatPerc)
			{
				vatRow = vatRows.get(i);
				break;
			}
		}

		// If not found, add a new row.
		if (vatRow == null)
		{
			vatRow = new VatRow();
			vatRows.add(vatRow);
		}
		
		// Update row values.
		vatRow.vatPerc = vatPerc;
		vatRow.nettValue += nettValue;
	}
	
	//
	// Return total if charging to account.
	//
	public double getCreditTotal()
	{
        double deliveredNettValue = getDeliveredNettValue();
        double deliveredVatValue = getDeliveredVatValue();
        double deliveredSurchargeValue = getDeliveredSurchargeValue();
        double codAccBalance = getCodAccBalance();

        return Utils.RoundNearest(deliveredNettValue + deliveredVatValue + deliveredSurchargeValue + codAccBalance, 2);
	}

	//
	// Return total if paying cash to driver.
	//
	public double getCashTotal()
	{
        double deliveredNettValue = getDeliveredNettValue();
        double deliveredVatValue = getDeliveredVatValue();
        double codAccBalance = getCodAccBalance();

		return Utils.RoundNearest(deliveredNettValue + deliveredVatValue + codAccBalance, 2);
	}

    public double getSurchargeVat()
    {
        double surcharge = getDeliveredSurchargeValue();
        double surchargeVat = 0.0;

        if (surcharge > 0)
        {
            dbTripOrderLine orderLine = GetTripOrderLines().get(0);

            double vatPercentage = getVatPercentage(orderLine);

            surchargeVat = surcharge * vatPercentage / 100.0;
        }

        return surchargeVat;
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


    //
	// Return total paid to office.
	//
	public double getPrepaidAmount()
	{
		double prepaid = PrepaidAmount;		// Really paid office i.e. cash in advance
		
		if (Terms.equals("Paying by Card"))
			prepaid += getCashTotal();
		
		return prepaid;
	}
	
	//
	// Return total paid to driver.
	//
	public double getPaidDriver()
	{
		return CashReceived + ChequeReceived + VoucherReceived;
	}

	//
	// Return outstanding amount.
	//
	public double getOutstanding()
	{
        double creditTotal = getCreditTotal();
        double prepaidAmount = getPrepaidAmount();
        double paidDriver = getPaidDriver();

		return Utils.RoundNearest(creditTotal - prepaidAmount - paidDriver - Discount, 2);
	}
	
	//
	// Returns list of products ordered.
	//
	public String getProductsOrdered(String separator)
	{
		// Find order lines.
		List<dbTripOrderLine> lines = getMany(dbTripOrderLine.class, "TripOrder");
		
		StringBuilder productsOrdered = new StringBuilder();
		for (dbTripOrderLine line : lines)
		{
			if (productsOrdered.length() > 0)
				productsOrdered.append(separator);

			if (line.Product == null)
				productsOrdered.append(line.OrderedQty + " of unknown product");
			else
			{
				if (line.Product.MobileOil == 1)
					productsOrdered.append(line.OrderedQty + " litres of " + line.Product.Desc);
				else
					productsOrdered.append(line.OrderedQty + " of " + line.Product.Desc);
			}
		}
		
		return productsOrdered.toString();		
	}

	//
	// Returns list of products delivered.
	//
	public String getProductsDelivered()
	{
		DateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

		// Find order lines.
		List<dbTripOrderLine> lines = GetTripOrderLines();
		
		StringBuilder productsDelivered = new StringBuilder();
		for (dbTripOrderLine line : lines)
		{
			if (line.DeliveredQty == 0)
				continue;
			
			if (productsDelivered.length() > 0)
				productsDelivered.append("\n");
			
			if (line.Product.MobileOil == 1)
				productsDelivered.append("Delivered " + line.DeliveredQty + " litres of " + line.Product.Desc + " @ " + df.format(line.DeliveryDate));
			else
				productsDelivered.append("Delivered " + line.DeliveredQty + " of " + line.Product.Desc + " @ " + df.format(line.DeliveryDate));
		}
		
		return productsDelivered.toString();		
	}
	
	//
	// Modify order.
	//
	
	// Start delivering.
	public void start()
	{
		Delivering = true;
		save();
	}

	// Stop delivering.
	public void stop()
	{
		Delivering = false;
		save();
	}

	// Update Discount.
	public void calculateDiscount()
	{
		double unpaid = getCashTotal() - getPrepaidAmount() - getPaidDriver();

		// Round off pence as discount.
		Discount = 0;
		if (unpaid < 1)
			Discount = (double) Math.round((getDeliveredSurchargeValue() + Math.max(0, unpaid)) * 100) / 100;
	}
	
	// Order delivered.
	public void delivered()
	{
		// Record delivery number - this indicates order has been delivered.
		if (DeliveryNo == 0)
			DeliveryNo = Trip.GetDelivered().size() + 1;
		
		// Record when delivery occurred. 
		DeliveryDate = new Date().getTime();

		Delivering = false;
		Delivered = true;
		save();
	}
}
