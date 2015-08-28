package com.swiftsoft.colossus.mobileoil.database.model;

import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

@Table(name = "Setting")
public class dbSetting extends Model
{
	@Column(name = "Key")
	public String Key;
	
	@Column(name = "IntValue")
	public int IntValue;

	@Column(name = "StringValue")
	public String StringValue;
	
	@Column(name = "BinaryValue")
	public byte[] BinaryValue;
	
	// Static methods.
	public static void DeleteAll()
	{
		new Delete().from(dbSetting.class).execute();
	}

	public static List<dbSetting> GetAll()
	{
		return new Select().from(dbSetting.class).execute();
	}

	public static List<dbSetting> GetAllBrandLogos()
	{
		return new Select().from(dbSetting.class).where("Key like ?", "Brand Logo:%").execute();
	}
	
	public static dbSetting FindByKey(String Key)
	{
		return new Select().from(dbSetting.class).where("Key=?", Key).executeSingle();
	}
	
	public static dbSetting FindByKeyOrCreate(String key)
	{
		dbSetting setting = FindByKey(key);
		if (setting == null)
		{
			setting = new dbSetting();
			setting.Key = key;
		}
		
		return setting;
	}
}
