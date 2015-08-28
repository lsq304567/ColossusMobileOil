package com.swiftsoft.colossus.mobileoil.database.model;

import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;

@Table(name = "VehicleCheckListSection")
public class dbVehicleChecklistSection extends Model
{
	@Column(name = "VehicleChecklist")
	public dbVehicleChecklist VehicleChecklist;
	
	@Column(name = "Title")
	public String Title;
	
	// Find all items for this checklist section.
	public List<dbVehicleChecklistSectionItem> GetVehicleChecklistSectionItems() 
	{
		return getMany(dbVehicleChecklistSectionItem.class, "VehicleChecklistSection");
	}
	
	// Static methods
	public static void DeleteAll()
	{
		new Delete().from(dbVehicleChecklistSection.class).execute();
	}
}
