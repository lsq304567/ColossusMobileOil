package com.swiftsoft.colossus.mobileoil.database.model;

import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

@Table(name = "Product")
public class dbProduct extends Model
{
	@Column(name = "ColossusID")
	public int ColossusID;
	
	@Column(name = "Code")
	public String Code;
	
	@Column(name = "Desc")
	public String Desc;
	
	@Column(name = "Colour")
	public int Colour;

	@Column(name = "MobileOil")
	public int MobileOil;
	
	// Static methods.
	public static void DeleteAll()
	{
		new Delete().from(dbProduct.class).execute();
	}

	public static List<dbProduct> GetAllMetered()
	{
		return new Select().from(dbProduct.class).where("MobileOil=1").orderBy("MobileOil,Desc").execute();
	}
	
	public static List<dbProduct> GetAllMeteredAndNonMetered()
	{
		return new Select().from(dbProduct.class).where("MobileOil=1 or MobileOil=2").orderBy("MobileOil,Desc").execute();
	}
	
	public static dbProduct FindByCode(String Code)
	{
		return new Select().from(dbProduct.class).where("Code=?", Code).executeSingle();
	}
	
	public static dbProduct FindByColossusID(int ColossusID)
	{
		return new Select().from(dbProduct.class).where("ColossusID=?", ColossusID).executeSingle();
	}
}
