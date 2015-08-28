package com.swiftsoft.colossus.mobileoil.database.model;

import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

@Table(name = "MessageOut")
public class dbMessageOut extends Model
{
	@Column(name = "DeviceNo")
	public int DeviceNo;

	@Column(name = "DateTime")
	public long DateTime;
	
	@Column(name = "Type")
	public String Type;
	
	@Column(name = "Content")
	public String Content;
	
	@Column(name = "Guid")
	public String Guid;
	
	// Static methods.
	public static void DeleteAll()
	{
		new Delete().from(dbMessageOut.class).execute();
	}

	public static List<dbMessageOut> GetAll()
	{
		return new Select().from(dbMessageOut.class).orderBy("DateTime").execute();
	}
}
