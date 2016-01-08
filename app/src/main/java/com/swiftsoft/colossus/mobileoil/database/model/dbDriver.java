package com.swiftsoft.colossus.mobileoil.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "Driver")
public class dbDriver extends Model
{
	@Column(name = "ColossusID")
	public int ColossusID;
	
	@Column(name = "No")
	public int No;
	
	@Column(name = "PIN")
	public int PIN;
	
	@Column(name = "Name")
	public String Name;

	// Static methods

	public static void DeleteAll()
	{
		new Delete().from(dbDriver.class).execute();
	}
	
	public static List<dbDriver> GetAll()
	{
		return new Select().from(dbDriver.class).execute();
	}

	public static dbDriver FindByNo(int No)
	{
		return new Select().from(dbDriver.class).where("No=?", No).executeSingle();
	}
	
	public static dbDriver FindByColossusID(int ColossusID)
	{
		return new Select().from(dbDriver.class).where("ColossusID=?", ColossusID).executeSingle();
	}
}
