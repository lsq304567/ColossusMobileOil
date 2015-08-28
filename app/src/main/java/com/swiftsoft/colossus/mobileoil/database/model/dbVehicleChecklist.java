package com.swiftsoft.colossus.mobileoil.database.model;

import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;

@Table(name = "VehicleCheckList")
public class dbVehicleChecklist extends Model
{
	@Column(name = "Vehicle")
	public dbVehicle Vehicle;

	// Version numbers are used to prevent 
	// unnecessary downloading of data from the server.
	@Column(name = "Version")
	public int Version;
	
	// Find all sections for this checklist.
	public List<dbVehicleChecklistSection> GetVehicleChecklistSections() 
	{
		return getMany(dbVehicleChecklistSection.class, "VehicleChecklist");
	}
	
	// Static methods
	public static void DeleteAll()
	{
		new Delete().from(dbVehicleChecklist.class).execute();
	}
}
