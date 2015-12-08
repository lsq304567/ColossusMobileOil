package com.swiftsoft.colossus.mobileoil.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.swiftsoft.colossus.mobileoil.CrashReporter;

import java.util.ArrayList;
import java.util.List;

@Table(name = "VehicleStock")
public class dbVehicleStock extends Model
{
	@Column(name = "Vehicle")
	public dbVehicle Vehicle;

	@Column(name = "Compartment")
	public int Compartment;
	
	@Column(name = "Product")
	public dbProduct Product;

	@Column(name = "OpeningStock")
	public int OpeningStock;

	@Column(name = "CurrentStock")
	public int CurrentStock;
	
	// Static methods.
	public static void DeleteAll()
	{
		new Delete().from(dbVehicleStock.class).execute();
	}

	public static List<dbVehicleStock> GetAll(dbVehicle vehicle)
	{
		return new Select().from(dbVehicleStock.class).where("Vehicle=?", vehicle.getId()).execute();
	}
	
	// Returns VehicleStock row, for specified vehicle & compartment.
	public static dbVehicleStock FindByVehicleCompartment(dbVehicle vehicle, int compartment)
	{
		return new Select().from(dbVehicleStock.class).where("Vehicle=? and Compartment=?", vehicle.getId(), compartment).executeSingle();
	}
	
	// Returns a list of VehicleStock rows, which are not by compartment.
	public static List<dbVehicleStock> FindAllNonCompartmentStock(dbVehicle vehicle)
	{
		return new Select().from(dbVehicleStock.class).where("Vehicle=? and Compartment=-1", vehicle.getId()).execute();
	}

	// Returns a list of VehicleStock rows, per product.
	public static List<dbVehicleStock> GetStockByProduct(dbVehicle vehicle)
	{
		CrashReporter.leaveBreadcrumb("dbVehicleStock: GetStockByProduct");

		List<dbVehicleStock> stockList = new ArrayList<dbVehicleStock>();
		
		// Add non-compartment stock.
		for (dbVehicleStock vs : FindAllNonCompartmentStock(vehicle))
		{
			AddStockByProduct(stockList, vs);
		}

        CrashReporter.leaveBreadcrumb("dbVehicleStock: GetStockByProduct - Finding hosereel stock ...");
		
		// Add any hosereel stock.
		dbVehicleStock lineVS = FindByVehicleCompartment(vehicle, 0);

        if (lineVS != null)
        {
            AddStockByProduct(stockList, lineVS);
        }
				
		return stockList;
	}

	// Return a list of VehicleStock rows, per compartment (inc. hosereel).
	public static List<dbVehicleStock> GetStockByCompartment(dbVehicle vehicle)
	{
		List<dbVehicleStock> stockList = new ArrayList<dbVehicleStock>();
		
    	for (int compartment = 0; compartment < vehicle.getCompartmentCount(); compartment++)
    	{
			int no = vehicle.getCompartmentNo(compartment);
			
			stockList.add(FindByVehicleCompartment(vehicle, no));
    	}
				
		return stockList;
	}
	
	private static void AddStockByProduct(List<dbVehicleStock> stockList, dbVehicleStock stockItem)
	{
        CrashReporter.leaveBreadcrumb("dbVehicleStock: AddStockByProduct");

		for (dbVehicleStock vehicleStock : stockList)
		{
			if (vehicleStock.Product.getId() == stockItem.Product.getId())
			{
                CrashReporter.leaveBreadcrumb("dbVehicleStock: AddStockByProduct - Updating the opening stock ...");
				vehicleStock.OpeningStock += stockItem.OpeningStock;

                CrashReporter.leaveBreadcrumb("dbVehicleStock: AddStockByProduct - Updating the current stock ...");
				vehicleStock.CurrentStock += stockItem.CurrentStock;

				return;
			}
		}
		
		// Add stockItem to stockList.
        CrashReporter.leaveBreadcrumb("dbVehicleStock: AddStockByProduct - Addint to stock list ...");
		stockList.add(stockItem);
	}
	
	public static dbVehicleStock FindOrCreateByVehicleCompartment(dbVehicle vehicle, int compartment)
	{
		dbVehicleStock vs = new Select().from(dbVehicleStock.class).where("Vehicle=? and Compartment=?", vehicle.getId(), compartment).executeSingle();
		
		if (vs == null)
		{
			// Create new VehicleStock
			vs = new dbVehicleStock();
			vs.Vehicle = vehicle;
			vs.Compartment = compartment;
			vs.Product = null;
			vs.OpeningStock = 0;
			vs.CurrentStock = 0;
		}
		
		return vs;
	}
	
	public static dbVehicleStock FindOrCreateByVehicleProduct(dbVehicle vehicle, dbProduct product)
	{
		dbVehicleStock vs = new Select().from(dbVehicleStock.class).where("Vehicle=? and Product=? and Compartment=-1", vehicle.getId(), product.getId()).executeSingle();
		
		if (vs == null)
		{
			// Create new VehicleStock
			vs = new dbVehicleStock();
			vs.Vehicle = vehicle;
			vs.Compartment = -1;
			vs.Product = product;
			vs.OpeningStock = 0;
			vs.CurrentStock = 0;
		}
		
		return vs;
	}
	
	public static void RemoveZeroProducts(dbVehicle vehicle)
	{
		List<dbVehicleStock> stockList = GetAll(vehicle);
		
		for (dbVehicleStock vs : stockList)
		{
			if (vs.CurrentStock == 0)
				vs.delete();
		}
	}
}
