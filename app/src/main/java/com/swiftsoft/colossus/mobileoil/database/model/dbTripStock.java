package com.swiftsoft.colossus.mobileoil.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;

//
// This table records all stock transactions which occur on the trip.
//
// e.g. Loading stock, Deliveries, Returning stock, etc.
//

@Table(name = "TripStock")
public class dbTripStock extends Model
{
	@Column(name = "Trip")
	public dbTrip Trip;

	// If type is 'Order'
	@Column(name = "TripOrder")
	public dbTripOrder TripOrder;

	@Column(name = "Type")
	public String Type;
	
	@Column(name = "Date")
	public long Date;
	
	@Column(name = "InvoiceNo")
	public String InvoiceNo;
	
	@Column(name = "CustomerCode")
	public String CustomerCode;
	
	@Column(name = "TicketNo")
	public long TicketNo;
	
	@Column(name = "Description")
	public String Description;
	
	@Column(name = "Notes")
	public String Notes;
	
	// Static methods
	public static void DeleteAll()
	{
		new Delete().from(dbTripStock.class).execute();
	}
}
