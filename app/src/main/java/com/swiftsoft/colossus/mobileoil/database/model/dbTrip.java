package com.swiftsoft.colossus.mobileoil.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.swiftsoft.colossus.mobileoil.CrashReporter;

import java.util.List;

@Table(name = "Trip")
public class dbTrip extends Model
{
	@Column(name = "ColossusID")
	public int ColossusID;
	
	@Column(name = "No")
	public int No;

	@Column(name = "Date")
	public long Date;

	@Column(name = "Vehicle")
	public dbVehicle Vehicle;

	@Column(name = "Driver")
	public dbDriver Driver;

	@Column(name = "LoadingNotes")
	public String LoadingNotes;
	
	//
	// Modified by app
	//
	
	@Column(name = "Delivering")
	public boolean Delivering;
	
	@Column(name = "Delivered")
	public boolean Delivered;

    @Column(name = "OriginalHosereelProduct")
    public dbProduct OriginalHosereelProduct;

	//
	// Static methods.
	//
	
	public static void DeleteAll()
	{
		new Delete().from(dbTrip.class).execute();
	}

	// Find trip by ColossusID.
	public static dbTrip FindByColossusID(int ColossusID)
	{
		return new Select().from(dbTrip.class).where("ColossusID=?", ColossusID).executeSingle();
	}

	// Find undelivered trips for specified vehicle and driver.
	public static List<dbTrip> GetUndeliveredTrips(dbVehicle vehicle, dbDriver driver)
	{
		return new Select().from(dbTrip.class).where("Vehicle=? and Driver=? and Delivered=?", vehicle.getId(), driver.getId(), 0).orderBy("Date").execute();
	}
	
	//
	// Non-static methods.
	//
	
	
	// Find all orders.
	public List<dbTripOrder> GetOrders() 
	{
		return new Select().from(dbTripOrder.class).where("Trip=?", this.getId()).orderBy("DeliveryOrder").execute();
	}

	// Return list of undelivered orders for this trip.
	public List<dbTripOrder> GetUndelivered()
	{
		return new Select().from(dbTripOrder.class).where("Trip=? and DeliveryNo=0", this.getId()).orderBy("DeliveryOrder").execute();
	}

	// Return list of delivered orders for this trip.
	public List<dbTripOrder> GetDelivered()
	{
		return new Select().from(dbTripOrder.class).where("Trip=? and DeliveryNo>0", this.getId()).orderBy("DeliveryNo DESC").execute();
	}

	// Return list of stock transactions for this trip.
	public List<dbTripStock> GetStockTrans()
	{
		return new Select().from(dbTripStock.class).where("Trip=?", this.getId()).orderBy("Date").execute();
	}
	
	// Start delivering.
	public void start()
	{
		CrashReporter.leaveBreadcrumb("dbTrip: start");

		// Check if any orders already delivered on this trip.
		if (GetDelivered().size() > 0)
		{
			CrashReporter.leaveBreadcrumb("dbTrip: start - Some orders have been delivered");

			return;
		}
		
		// Check if any stock transactions for this trip.
		if (GetStockTrans().size() > 0)
		{
			CrashReporter.leaveBreadcrumb("dbTrip: start - There are existing Stock Transaction for this Trip");

			return;
		}
		
		// Copy CurrentStock to OpeningStock at the start of this trip.
        CrashReporter.leaveBreadcrumb("dbTrip: start - Saving Opening Stock values ...");
		for (dbVehicleStock vehicleStock : dbVehicleStock.GetAll(Vehicle))
		{
			vehicleStock.OpeningStock = vehicleStock.CurrentStock;
			vehicleStock.save();
		}

        // Save the original hosereel product
        OriginalHosereelProduct = Vehicle.getHosereelProduct();
		
		// Mark trip as Delivering.
		Delivering = true;

        CrashReporter.leaveBreadcrumb("dbTrip: start - Saving Trip ...");
        save();
	}
	
	// Stop delivering.
	public void stop()
	{
		Delivering = false;
		save();
	}
	
	// Trip delivered.
	public void delivered()
	{
		Delivering = false;
		Delivered = true;
		save();
	}
	
	// Trip reverse delivered - this method is for Engineering use only
	public void reverseDelivered()
	{
		Delivering = true;
		Delivered = false;
		save();
	}
}
