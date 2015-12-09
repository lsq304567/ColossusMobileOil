package com.swiftsoft.colossus.mobileoil.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.swiftsoft.colossus.mobileoil.CrashReporter;
import com.swiftsoft.colossus.mobileoil.Utils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
	public String PrepaidAmount;

    public BigDecimal getPrepaidAmount()
    {
        return new BigDecimal(PrepaidAmount);
    }

    public void setPrepaidAmount(BigDecimal value)
    {
        PrepaidAmount = value.toString();
    }

    @Column(name = "CodPoint")
	public int CodPoint;
	
	@Column(name = "CodType")
	public int CodType;
	
	@Column(name = "CodAmount")
	public String CodAmount;

    public BigDecimal getCodAmount()
    {
        return new BigDecimal(CodAmount);
    }

    public void setCodAmount(BigDecimal value)
    {
        CodAmount = value.toString();
    }

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
	public String CashReceived;

    public BigDecimal getCashReceived()
    {
        if (CashReceived == null)
        {
            setCashReceived(BigDecimal.ZERO);
        }

        return new BigDecimal(CashReceived);
    }

    public void setCashReceived(BigDecimal value)
    {
        CashReceived = value.toString();
    }
	
	@Column(name = "ChequeReceived")
	public String ChequeReceived;

    public BigDecimal getChequeReceived()
    {
        if (ChequeReceived == null)
        {
            setChequeReceived(BigDecimal.ZERO);
        }

        return new BigDecimal(ChequeReceived);
    }

    public void setChequeReceived(BigDecimal value)
    {
        ChequeReceived = value.toString();
    }

    @Column(name = "VoucherReceived")
	public String VoucherReceived;

    public BigDecimal getVoucherReceived()
    {
        if (VoucherReceived == null)
        {
            setVoucherReceived(BigDecimal.ZERO);
        }

        return new BigDecimal(VoucherReceived);
    }

    public void setVoucherReceived(BigDecimal value)
    {
        VoucherReceived = value.toString();
    }

    @Column(name = "Discount")
	public String Discount;

    public BigDecimal getDiscount()
    {
        if (Discount == null)
        {
            setDiscount(BigDecimal.ZERO);
        }

        return new BigDecimal(Discount);
    }

    public void setDiscount(BigDecimal value)
    {
        Discount = value.toString();
    }

    @Column(name = "DriverSignature")
	public boolean DriverSignature;

	@Column(name = "DriverSignatureName")
	public String DriverSignatureName;

	@Column(name = "DriverSignatureImage")
	public byte[] DriverSignatureImage;
	
	@Column(name = "DriverSignatureDateTime")
	public long DriverSignatureDateTime;

	@Column(name = "UnattendedSignature")
	public boolean UnattendedSignature;

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
		public BigDecimal vatPercentage = BigDecimal.ZERO;
		public BigDecimal nettValue = BigDecimal.ZERO;
	}

	// Find all order lines.
	public List<dbTripOrderLine> GetTripOrderLines() 
	{
		return getMany(dbTripOrderLine.class, "TripOrder");
	}

	public String getTerms()
	{
		String terms = this.Terms;
		
		if (CodPoint == 1)
		{
			terms += ", after delivery";
		}
		
		if (CodPoint == 2)
		{
			terms += ", before delivery";
		}
		
		return terms;
	}
	
	// 
	// Return COD 'before delivery' value 
	//
	public BigDecimal getCodBeforeDeliveryValue()
	{
		BigDecimal cod = BigDecimal.ZERO;

		// Before delivery.
		if (CodPoint == 2)
		{
			if (CodType == 1)
			{
				cod = getOrderedNettValue().add(getOrderedVatValue());
			}
			
			if (CodType == 2)
			{
				cod = getOrderedNettValue().add(getOrderedVatValue()).add(getCodAmount());
			}
			
			if (CodType == 3)
			{
				cod = getCodAmount();
			}
		}	

		return cod;
	}
	
	// 
	// Return COD 'after delivery' value. 
	//
	public BigDecimal getCodAfterDeliveryValue()
	{
		BigDecimal cod = BigDecimal.ZERO;

		// Before/After delivery.
		if (CodPoint != 0)
		{
			if (CodType == 1)
            {
                cod = getDeliveredNettValue().add(getDeliveredVatValue());
            }
			
			if (CodType == 2)
            {
                cod = getDeliveredNettValue().add(getDeliveredVatValue()).add(getCodAmount());
            }
			
			if (CodType == 3)
            {
                cod = getCodAmount();
            }
		}	

		return cod;
		// After delivery, all customers can pay the cash price if they like.
		// return getDeliveredNettValue() + getDeliveredVatValue();
	}
	
	//
	// Return COD 'Acc balance' value, which is held in CodAmount if CodType is 2.
	//
	public BigDecimal getCodAccBalance()
	{
		if (CodPoint != 0 && CodType == 2)
        {
            return getCodAmount();
        }
		
		return BigDecimal.ZERO;
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
            {
                continue;
            }
			
			if (line.DeliveryDate == 0)
            {
                counter++;
            }
		}
				
		return counter;
	}
	
	//
	// Ordered values.
	//
	
	private BigDecimal getOrderedNettValue()
	{
		BigDecimal value = BigDecimal.ZERO;

        for (dbTripOrderLine line : this.GetTripOrderLines())
        {
			value = value.add(line.getOrderedNettValue());
        }
		
		return value;
	}
	
	public BigDecimal getOrderedSurchargeValue()
	{
		BigDecimal value = BigDecimal.ZERO;

        for (dbTripOrderLine line : this.GetTripOrderLines())
        {
			value = value.add(line.getOrderedSurchargeValue());
        }
		
		return value;
	}
	
	private BigDecimal getOrderedVatValue()
	{
		List<VatRow> vatRows = new ArrayList<VatRow>();
		
		// Summarise all lines by VatPercentage.
		for (dbTripOrderLine line : this.GetTripOrderLines())
		{
			AddToVatTable(vatRows, line.getOrderedVatPerc(), line.getOrderedNettValue());
			
			if (line.getOrderedSurchargeValue().compareTo(BigDecimal.ZERO) != 0)
            {
                AddToVatTable(vatRows, BigDecimal.ZERO, line.getOrderedSurchargeValue());
            }
		}

		// Calculate VAT value.
		BigDecimal vatValue = BigDecimal.ZERO;

        for (int i = 0; i < vatRows.size(); i++)
		{
			VatRow vatRow = vatRows.get(i);

			BigDecimal vat = vatRow.nettValue.multiply(vatRow.vatPercentage).divide(new BigDecimal(100), 10, BigDecimal.ROUND_HALF_UP);

			vatValue = vatValue.add(Utils.RoundNearest(vat, 2));
		}

		return vatValue;
	}

	//
	// Delivered values.
	//

	public boolean getDeliveredQtyVariesFromOrdered()
	{
		for (dbTripOrderLine line : this.GetTripOrderLines())
        {
            if (line.getDeliveredQtyVariesFromOrdered())
            {
                return true;
            }
        }
		
		return false;
	}
	
	public BigDecimal getDeliveredNettValue()
	{
        CrashReporter.leaveBreadcrumb("dbTripOrder: getDeliveredNettValue");

		BigDecimal value = BigDecimal.ZERO;

		for (dbTripOrderLine line : this.GetTripOrderLines())
        {
            value = value.add(line.getDeliveredNettValue());
        }

        CrashReporter.leaveBreadcrumb(String.format("dbTripOrder: getDeliveredNettValue - %f", value));
		
		return value;
	}
	
	public BigDecimal getDeliveredSurchargeValue()
	{
        CrashReporter.leaveBreadcrumb("dbTripOrder: getDeliveredSurchargeValue");

        BigDecimal value = BigDecimal.ZERO;

		for (dbTripOrderLine line : this.GetTripOrderLines())
        {
            value = value.add(line.getDeliveredSurchargeValue());
        }

        CrashReporter.leaveBreadcrumb(String.format("dbTripOrder: getDeliveredSurchargeValue - %f", value));

        return value;
	}

    private static void addRow(Hashtable<BigDecimal, VatRow> table, VatRow row)
    {
        if (!table.containsKey(row.vatPercentage))
        {
            table.put(row.vatPercentage, row);
        }
        else
        {
            VatRow foundRow = table.get(row.vatPercentage);

            foundRow.nettValue = foundRow.nettValue.add(row.nettValue);
        }
    }

	public Hashtable<BigDecimal, VatRow> getDeliveredVatValues()
	{
		Hashtable<BigDecimal, VatRow> vatRows = new Hashtable<BigDecimal, VatRow>();

        for (dbTripOrderLine line : GetTripOrderLines())
        {
            VatRow row = new VatRow();

            row.nettValue = line.getDeliveredNettValue();
            row.vatPercentage = line.getDeliveredVatPerc();

            addRow(vatRows, row);

            if (line.getDeliveredSurchargeValue().compareTo(BigDecimal.ZERO) != 0)
            {
                row = new VatRow();

                row.nettValue = line.getDeliveredSurchargeValue();
                row.vatPercentage = line.getDeliveredVatPerc();

                addRow(vatRows, row);
            }
        }

		return vatRows;
	}
	
	public BigDecimal getDeliveredVatValue()
	{
		List<VatRow> vatRows = new ArrayList<VatRow>();
		
		// Summarise all lines by VatPercentage.
		for (dbTripOrderLine line : this.GetTripOrderLines())
		{
			AddToVatTable(vatRows, line.getDeliveredVatPerc(), line.getDeliveredNettValue());
			
			if (line.getDeliveredSurchargeValue().compareTo(BigDecimal.ZERO) != 0)
			{
				AddToVatTable(vatRows, line.getDeliveredVatPerc(), line.getDeliveredSurchargeValue());
			}
		}

		// Calculate VAT value.
		BigDecimal vatValue = BigDecimal.ZERO;

		for (int i = 0; i < vatRows.size(); i++)
		{
			VatRow vatRow = vatRows.get(i);

			BigDecimal vat = vatRow.nettValue.multiply(vatRow.vatPercentage).divide(new BigDecimal(100), 10, BigDecimal.ROUND_HALF_UP);

            vatValue = vatValue.add(vat);
		}
		
		return Utils.RoundNearest(vatValue, 2);
	}

	//
	// Common
	//
	
	private void AddToVatTable(List<VatRow> vatRows, BigDecimal vatPercentage, BigDecimal nettValue)
	{
		VatRow vatRow = null;
		
		// Search for existing row.
		for (int i = 0; i < vatRows.size(); i++)
		{
			if (vatRows.get(i).vatPercentage.compareTo(vatPercentage) == 0)
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
		vatRow.vatPercentage = vatPercentage;
        vatRow.nettValue = vatRow.nettValue.add(nettValue);
	}
	
	//
	// Return total if charging to account.
	//
	public BigDecimal getCreditTotal()
	{
        BigDecimal deliveredNettValue = getDeliveredNettValue();
        BigDecimal deliveredVatValue = getDeliveredVatValue();
        BigDecimal deliveredSurchargeValue = getDeliveredSurchargeValue();
        BigDecimal codAccBalance = getCodAccBalance();

        return Utils.RoundNearest(deliveredNettValue.add(deliveredVatValue).add(deliveredSurchargeValue).add(codAccBalance), 2);
	}

	//
	// Return total if paying cash to driver.
	//
	public BigDecimal getCashTotal()
	{
        CrashReporter.leaveBreadcrumb("dbTripOrder: getCashTotal");

        BigDecimal deliveredNettValue = getDeliveredNettValue();
        BigDecimal deliveredVatValue = getDeliveredVatValue();
        BigDecimal codAccBalance = getCodAccBalance();

		return Utils.RoundNearest(deliveredNettValue.add(deliveredVatValue).add(codAccBalance), 2);
	}

    public BigDecimal getSurchargeVat()
    {
        BigDecimal surcharge = getDeliveredSurchargeValue();
        BigDecimal surchargeVat = BigDecimal.ZERO;

        if (surcharge.compareTo(BigDecimal.ZERO) == 1)
        {
            dbTripOrderLine orderLine = GetTripOrderLines().get(0);

            BigDecimal vatPercentage = getVatPercentage(orderLine);

            surchargeVat = surcharge.multiply(vatPercentage).divide(new BigDecimal(100), 10, BigDecimal.ROUND_HALF_UP);
        }

        return surchargeVat;
    }

    private static BigDecimal getVatPercentage(dbTripOrderLine line)
    {
        if (line.VatPerc2Above < 1000000)
        {
            if (line.DeliveredQty < line.VatPerc2Above)
            {
                return line.getVatPerc1();
            }
            else
            {
                return line.getVatPerc2();
            }
        }
        else
        {
            return line.getVatPerc1();
        }
    }


    //
	// Return total paid to office.
	//
	public BigDecimal getAmountPrepaid()
	{
		BigDecimal prepaid = getPrepaidAmount();		// Really paid office i.e. cash in advance
		
		if (Terms.equals("Paying by Card"))
        {
            prepaid = prepaid.add(getCashTotal());
        }
		
		return prepaid;
	}
	
	//
	// Return total paid to driver.
	//
	public BigDecimal getPaidDriver()
	{
		return getCashReceived().add(getChequeReceived()).add(getVoucherReceived());
	}

	//
	// Return outstanding amount.
	//
	public BigDecimal getOutstanding()
	{
        CrashReporter.leaveBreadcrumb("dbTripOrder: getOutstanding");

        BigDecimal creditTotal = getCreditTotal();
        BigDecimal prepaidAmount = getAmountPrepaid();
        BigDecimal paidDriver = getPaidDriver();

		return Utils.RoundNearest(creditTotal.subtract(prepaidAmount).subtract(paidDriver).subtract(getDiscount()), 2);
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
            {
                productsOrdered.append(separator);
            }

			if (line.Product == null)
            {
                productsOrdered.append(line.OrderedQty);
                productsOrdered.append(" of unknown product");
            }
			else
			{
                productsOrdered.append(line.OrderedQty);
                productsOrdered.append(line.Product.MobileOil == 1 ? " litres of " : " of ");
                productsOrdered.append(line.Product.Desc);
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
            {
                continue;
            }
			
			if (productsDelivered.length() > 0)
            {
                productsDelivered.append("\n");
            }

            productsDelivered.append("Delivered ");
            productsDelivered.append(line.DeliveredQty);
            productsDelivered.append(line.Product.MobileOil == 1 ? " litres of " : " of ");
            productsDelivered.append(line.Product.Desc);
            productsDelivered.append(" @ ");
            productsDelivered.append(df.format(line.DeliveryDate));
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
        CrashReporter.leaveBreadcrumb("dbTripOrder: calculateDiscount");

        BigDecimal cashTotal = getCashTotal();
        BigDecimal prepaidAmount = getAmountPrepaid();
        BigDecimal paidDriver = getPaidDriver();

		BigDecimal unpaid = cashTotal.subtract(prepaidAmount).subtract(paidDriver);

        CrashReporter.leaveBreadcrumb(String.format("dbTripOrder: calculateDiscount - Unpaid amount : %f", unpaid));

		// Round off pence as discount.
		setDiscount(BigDecimal.ZERO);

        if (unpaid.compareTo(BigDecimal.ONE) < 0)
        {
            BigDecimal deliveredSurchargeValue = getDeliveredSurchargeValue();

            if (unpaid.compareTo(BigDecimal.ZERO) > 0)
            {
                setDiscount(deliveredSurchargeValue.add(unpaid));
            }
            else
            {
                setDiscount(deliveredSurchargeValue);
            }
        }
	}
	
	// Order delivered.
	public void delivered()
	{
		// Record delivery number - this indicates order has been delivered.
		if (DeliveryNo == 0)
        {
            DeliveryNo = Trip.GetDelivered().size() + 1;
        }
		
		// Record when delivery occurred. 
		DeliveryDate = Utils.getCurrentTime();

		Delivering = false;
		Delivered = true;

		save();
	}
}
