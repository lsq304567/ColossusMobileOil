package com.swiftsoft.colossus.mobileoil.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.swiftsoft.colossus.mobileoil.Utils;

import java.math.BigDecimal;

@Table(name = "TripOrderLine")
public class dbTripOrderLine extends Model
{
	@Column(name = "ColossusID")
	public int ColossusID;

	@Column(name = "TripOrder")
	public dbTripOrder TripOrder;

	@Column(name = "Product")
	public dbProduct Product;

	@Column(name = "OrderedQty")
	public int OrderedQty;
	
	@Column(name = "Price")
	public String OrderedPrice;

    public BigDecimal getOrderedPrice()
    {
        if (OrderedPrice == null)
        {
            setOrderedPrice(BigDecimal.ZERO);
        }

        return new BigDecimal(OrderedPrice);
    }

    public void setOrderedPrice(BigDecimal value)
    {
        OrderedPrice = value.toString();
    }

	@Column(name = "Surcharge")
	public String Surcharge;

    public BigDecimal getSurcharge()
    {
        if (Surcharge == null)
        {
            setSurcharge(BigDecimal.ZERO);
        }

        return new BigDecimal(Surcharge);
    }

    public void setSurcharge(BigDecimal value)
    {
        Surcharge = value.toString();
    }

    // If true multiply Qty by Surcharge and divide by Ratio,
	// otherwise just add Surcharge to NettValue.
	@Column(name = "SurchargePerUOM")
	public boolean SurchargePerUOM;
	
	@Column(name = "Ratio")
	public String Ratio;

    public BigDecimal getRatio()
    {
        if (Ratio == null)
        {
            setRatio(BigDecimal.ZERO);
        }

        return new BigDecimal(Ratio);
    }

    public void setRatio(BigDecimal value)
    {
        Ratio = value.toString();
    }

    @Column(name = "VatPerc1")
	public String VatPerc1;

    public BigDecimal getVatPerc1()
    {
        if (VatPerc1 == null)
        {
            setVatPerc1(BigDecimal.ZERO);
        }

        return new BigDecimal(VatPerc1);
    }

    public void setVatPerc1(BigDecimal value)
    {
        VatPerc1 = value.toString();
    }

    @Column(name = "VatPerc2")
	public String VatPerc2;

    public BigDecimal getVatPerc2()
    {
        if (VatPerc2 == null)
        {
            setVatPerc2(BigDecimal.ZERO);
        }

        return new BigDecimal(VatPerc2);
    }

    public void setVatPerc2(BigDecimal value)
    {
        VatPerc2 = value.toString();
    }

    @Column(name = "VatPerc2Above")
	public int VatPerc2Above;
	
	//
	// Modified by app.
	//
	
	@Column(name = "Delivered")
	public boolean Delivered;

	@Column(name = "DeliveredPrice")
	public String DeliveredPrice;

    public BigDecimal getDeliveredPrice()
    {
        if (DeliveredPrice == null)
        {
            setDeliveredPrice(BigDecimal.ZERO);
        }

        return new BigDecimal(DeliveredPrice);
    }

    public void setDeliveredPrice(BigDecimal value)
    {
        DeliveredPrice = value.toString();
    }

//@Column(name = "TotalPrice")
	//public double TotalPrice;
	
	@Column(name = "DeliveredQty")
	public int DeliveredQty;

	@Column(name = "DeliveryDate")
	public long DeliveryDate;

	@Column(name = "TicketNo")
	public String ticketNo;
	
	@Column(name = "TicketProductDesc")
	public String ticketProductDesc; 
	
	@Column(name = "TicketStartTime")
	public String ticketStartTime;
	
	@Column(name = "TicketFinishTime")
	public String ticketFinishTime;
	
	@Column(name = "TicketStartTotaliser")
	public double ticketStartTotaliser;
	
	@Column(name = "TicketEndTotaliser")
	public double ticketEndTotaliser;
	
	@Column(name = "TicketGrossVolume")
	public double ticketGrossVolume;
	
	@Column(name = "TicketNetVolume")
	public double ticketNetVolume;
	
	@Column(name = "TicketTemperature")
	public double ticketTemperature;

	@Column(name = "TicketAt15Degrees")
	public boolean ticketAt15Degrees;
	
	//
	// Static methods.
	//
	
	public static void DeleteAll()
	{
		new Delete().from(dbTripOrderLine.class).execute();
	}

	// Find trip by ColossusID.
	public static dbTripOrderLine FindByColossusID(int ColossusID)
	{
		return new Select().from(dbTripOrderLine.class).where("ColossusID=?", ColossusID).executeSingle();
	}
	
	//
	// Non-static methods.
	//

	// Ordered.
	
	public BigDecimal getOrderedNettValue()
	{
		BigDecimal nettValue = new BigDecimal(OrderedQty).multiply(getOrderedPrice().divide(getRatio()));

		return Utils.roundNearest(nettValue, 2);
	}
	
	public BigDecimal getOrderedSurchargeValue()
	{
        if (SurchargePerUOM)
        {
            return new BigDecimal(OrderedQty).multiply(getSurcharge().divide(getRatio()));
        }
        else
        {
            return getSurcharge();
        }
	}

	public BigDecimal getOrderedVatPerc()
	{
		return OrderedQty <= VatPerc2Above ? getVatPerc1() : getVatPerc2();
	}
	
	// Delivered.
	
	public boolean getDeliveredQtyVariesFromOrdered()
	{
		if (DeliveredQty == 0)
        {
            return false;
        }

		return (Math.abs(OrderedQty - DeliveredQty) > 50);
	}
	
	// The delivered price includes any surcharge.
	// (Used by the ticket print out)
	public BigDecimal getPriceDelivered()
	{
		BigDecimal value = getDeliveredNettValue().add(getDeliveredSurchargeValue());

		return Utils.roundNearest(value.divide(new BigDecimal(DeliveredQty)), 4);
	}
	
	public BigDecimal getDeliveredNettValue()
	{
		BigDecimal value = new BigDecimal(DeliveredQty).multiply(getDeliveredPrice().divide(getRatio()));

		return Utils.roundNearest(value, 2);
	}

	public BigDecimal getDeliveredSurchargeValue()
	{
        BigDecimal value;

        if (SurchargePerUOM)
        {
            value = new BigDecimal(DeliveredQty).multiply(getSurcharge().divide(getRatio()));
        }
        else
        {
            value = getSurcharge();
        }

        return Utils.roundNearest(value, 2);
	}
	
	public BigDecimal getDeliveredVatPerc()
	{
        return DeliveredQty <= VatPerc2Above ? getVatPerc1() : getVatPerc2();
	}

	//
	// Modify orderline.
	//
	
	// OrderLine delivered.
	public void delivered(int qty)
	{
		// Record when delivery occurred 
		DeliveryDate = Utils.getCurrentTime();

		// and quantity delivered.
		DeliveredQty = qty;
		
		// if delivered qty is +/- 50 from ordered qty, 
		// then ask driver for a new price.
        setDeliveredPrice(getDeliveredQtyVariesFromOrdered() ? BigDecimal.ZERO : getOrderedPrice());

		// Mark line as delivered.
		Delivered = true;

		save();
	}
	
	// Used by driver when delivered qty is +/- 50 from ordered qty.
	public void setDeliveredPricePrice(BigDecimal newPrice)
	{
		setDeliveredPrice(newPrice);

		save();
	}
}