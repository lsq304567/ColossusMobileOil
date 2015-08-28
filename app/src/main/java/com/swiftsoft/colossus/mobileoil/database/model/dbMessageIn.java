package com.swiftsoft.colossus.mobileoil.database.model;

import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

@Table(name = "MessageIn")
public class dbMessageIn extends Model
{
	@Column(name = "MessageID")
	public int MessageID;

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
		new Delete().from(dbMessageIn.class).execute();
	}
	
	public static List<dbMessageIn> GetAll()
	{
		return new Select().from(dbMessageIn.class).orderBy("DateTime").execute();
	}
}
