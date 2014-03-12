package com.skripsi.scheduleplus.dataprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DBHelper  extends SQLiteOpenHelper 
{
	private static final String NAME = DBSchema.DATABASE_NAME;
	private static final int VERSION = 3;
	
	public DBHelper(Context context) 
	{
		super(context, NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		db.execSQL(DBSchema.CREATE_AGENDA);
		db.execSQL(DBSchema.CREATE_FILE);
		db.execSQL(DBSchema.CREATE_SMS);
		db.execSQL(DBSchema.CREATE_REMINDERLOCATION);

	}
	
	@Override
	public void onOpen(SQLiteDatabase db)
	{
		db.execSQL("PRAGMA foreign_keys = ON");
	}
	
	@Override
	public void onConfigure(SQLiteDatabase db)
	{
		db.execSQL("PRAGMA foreign_keys = ON");
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+DBSchema.DATABASE_TABLE_AGENDA);
		db.execSQL("DROP TABLE IF EXISTS "+DBSchema.DATABASE_TABLE_FILE);
		db.execSQL("DROP TABLE IF EXISTS "+DBSchema.DATABASE_TABLE_SMS);
		db.execSQL("DROP TABLE IF EXISTS "+DBSchema.DATABASE_TABLE_REMINDERLOCATION);
		this.onCreate(db);
		
	}
	
	
	
	
	
}
