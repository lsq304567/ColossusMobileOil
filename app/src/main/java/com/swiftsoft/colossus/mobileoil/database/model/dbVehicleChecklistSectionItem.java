package com.swiftsoft.colossus.mobileoil.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;

@Table(name = "VehicleCheckListSectionItem")
public class dbVehicleChecklistSectionItem extends Model
{
	@Column(name = "VehicleChecklistSection")
	public dbVehicleChecklistSection VehicleChecklistSection;
	
	@Column(name = "Title")
	public String Title;
	
	@Column(name = "Summary")
	public String Summary;
	
	// Static method.
	public static void DeleteAll()
	{
		new Delete().from(dbVehicleChecklistSectionItem.class).execute();
	}
}
