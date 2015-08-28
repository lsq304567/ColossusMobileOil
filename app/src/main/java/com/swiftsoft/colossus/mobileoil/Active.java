package com.swiftsoft.colossus.mobileoil;

import android.app.Activity;

import com.swiftsoft.colossus.mobileoil.database.model.dbDriver;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.database.model.dbTrip;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrderLine;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicle;

public class Active
{
	public static volatile Activity activity;
	public static volatile dbVehicle vehicle;
	public static volatile dbDriver driver;
	public static volatile dbTrip trip;
	public static volatile dbTripOrder order;
	public static volatile dbTripOrderLine orderLine;
	public static volatile dbProduct lineChangeProduct;
}
