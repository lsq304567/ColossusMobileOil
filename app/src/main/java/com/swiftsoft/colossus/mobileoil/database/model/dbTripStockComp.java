package com.swiftsoft.colossus.mobileoil.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;

//
// This table is currently NOT used.
//

@Table(name = "TripStockComp")
public class dbTripStockComp extends Model
{
	@Column(name = "TripStock")
	public dbTripStock TripStock;

	@Column(name = "Compartment")
	public int Compartment;
	
	@Column(name = "Product")
	public dbProduct Product;

	@Column(name = "Qty")
	public int Qty;
	
	// Static methods.
	
	public static void DeleteAll()
	{
		new Delete().from(dbTripStockComp.class).execute();
	}
}
