package com.swiftsoft.colossus.mobileoil.database.model;

import java.util.Date;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.swiftsoft.colossus.mobileoil.Utils;

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
	public double OrderedPrice;

	@Column(name = "Surcharge")
	public double Surcharge;
	
	// If true multiply Qty by Surcharge and divide by Ratio,
	// otherwise just add Surcharge to NettValue.
	@Column(name = "SurchargePerUOM")
	public boolean SurchargePerUOM;
	
	@Column(name = "Ratio")
	public double Ratio;
	
	@Column(name = "VatPerc1")
	public double VatPerc1;
	
	@Column(name = "VatPerc2")
	public double VatPerc2;
	
	@Column(name = "VatPerc2Above")
	public double VatPerc2Above;
	
	//
	// Modified by app.
	//
	
	@Column(name = "Delivered")
	public boolean Delivered;

	@Column(name = "DeliveredPrice")
	public double DeliveredPrice;
	
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
	
	public double getOrderedNettValue()
	{
		double nettValue = (this.OrderedQty * (this.OrderedPrice / this.Ratio));
		return Utils.RoundNearest(nettValue, 2);
	}
	
	public double getOrderedSurchargeValue()
	{
		if (this.SurchargePerUOM)
			return (this.OrderedQty * (this.Surcharge / this.Ratio));
		else
			return this.Surcharge;
	}

	public double getOrderedVatPerc()
	{
		if (this.OrderedQty <= this.VatPerc2Above)
			return this.VatPerc1;
		else
			return this.VatPerc2;
	}
	
	// Delivered.
	
	public boolean getDeliveredQtyVariesFromOrdered()
	{
		if (DeliveredQty == 0)
			return false;
		
		return (Math.abs(OrderedQty - DeliveredQty) > 50);
	}
	
	// The delivered price includes any surcharge.
	// (Used by the ticket print out)
	public double getDeliveredPrice()		
	{
		double value = this.getDeliveredNettValue() + this.getDeliveredSurchargeValue();
		return Utils.RoundNearest(value / this.DeliveredQty, 4);
	}
	
	public double getDeliveredNettValue()
	{
		double value = (this.DeliveredQty * (this.DeliveredPrice / this.Ratio));
		return Utils.RoundNearest(value, 2);
	}

	public double getDeliveredSurchargeValue()
	{
		if (this.SurchargePerUOM)
			return Utils.RoundNearest(this.DeliveredQty * (this.Surcharge / this.Ratio), 2);
		else
			return Utils.RoundNearest(this.Surcharge, 2);
	}
	
	public double getDeliveredVatPerc()
	{
		if (this.DeliveredQty <= this.VatPerc2Above)
			return this.VatPerc1;
		else
			return this.VatPerc2;
	}

	//
	// Modify orderline.
	//
	
	// OrderLine delivered.
	public void delivered(int qty)
	{
		// Record when delivery occurred 
		DeliveryDate = new Date().getTime();

		// and quantity delivered.
		DeliveredQty = qty;
		
		// if delivered qty is +/- 50 from ordered qty, 
		// then ask driver for a new price.
		if (getDeliveredQtyVariesFromOrdered())
			DeliveredPrice = 0;
		else
			DeliveredPrice = OrderedPrice;
		
		// Mark line as delivered.
		Delivered = true;
		save();
	}
	
	// Used by driver when delivered qty is +/- 50 from ordered qty.
	public void setDeliveredPricePrice(double newPrice)
	{
		DeliveredPrice = newPrice;
		save();
	}

}
