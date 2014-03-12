package com.skripsi.scheduleplus.dataprovider;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.skripsi.scheduleplus.BuildConfig;
import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.Agenda;
import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.File;
import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.AgendaEntity;
import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.SMS;
import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.ReminderLocation;


public class schedulePlusProvider extends ContentProvider
{
	//konstanta helper
	private static final int AGENDA_LIST = 1;
	private static final int AGENDA_ID = 2;
	private static final int FILE_LIST = 3;
	private static final int FILE_ID = 4;
	private static final int AGENDA_ENTITY_LIST = 5;
	private static final int AGENDA_ENTITY_ID = 6;
	private static final int SMS_LIST = 7;
	private static final int SMS_ID = 8;
	private static final int REMINDERLOCATION_LIST = 9;
	private static final int REMINDERLOCATION_ID = 10;
	private static final UriMatcher URI_MATCHER;
	
	private DBHelper mHelper = null;
	private final ThreadLocal<Boolean> mIsInBatchMode = new ThreadLocal<Boolean>();
	
	//URI Matcher
	static
	{
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(SchedulePlusContract.AUTHORITY, "agenda", AGENDA_LIST);
		URI_MATCHER.addURI(SchedulePlusContract.AUTHORITY, "agenda/#", AGENDA_ID);
		URI_MATCHER.addURI(SchedulePlusContract.AUTHORITY, "agendaFiles", FILE_LIST);
		URI_MATCHER.addURI(SchedulePlusContract.AUTHORITY, "agendaFiles/#", FILE_ID);
		URI_MATCHER.addURI(SchedulePlusContract.AUTHORITY, "agenda_entity", AGENDA_ENTITY_LIST);
		URI_MATCHER.addURI(SchedulePlusContract.AUTHORITY, "agenda_entity/#", AGENDA_ENTITY_ID);
		URI_MATCHER.addURI(SchedulePlusContract.AUTHORITY, "SMS", SMS_LIST);
		URI_MATCHER.addURI(SchedulePlusContract.AUTHORITY, "SMS/#", SMS_ID);
		URI_MATCHER.addURI(SchedulePlusContract.AUTHORITY, "ReminderLocation", REMINDERLOCATION_LIST);
		URI_MATCHER.addURI(SchedulePlusContract.AUTHORITY, "ReminderLocation/#", REMINDERLOCATION_ID);
	}

	@Override
	public boolean onCreate() {
		mHelper = new DBHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase dbs = mHelper.getReadableDatabase();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		boolean useAuthorityUri = false; //ini buat apa masih belum ngerti
		Log.v("ProviderUri",uri.toString());
		switch(URI_MATCHER.match(uri))
		{
		case AGENDA_LIST:
			queryBuilder.setTables(DBSchema.DATABASE_TABLE_AGENDA); //set builder buat melakukan query di tabel agenda
			if(TextUtils.isEmpty(sortOrder))
			{
				sortOrder = DBSchema.DEFAULT_SORT;
			}
			break;
		case AGENDA_ID:
			queryBuilder.setTables(DBSchema.DATABASE_TABLE_AGENDA);
			//disini dimasukin kondisi 'where' sesuai id
			queryBuilder.appendWhere(Agenda._ID + " = " + uri.getLastPathSegment());
			break;
		case FILE_LIST:
			queryBuilder.setTables(DBSchema.DATABASE_TABLE_FILE);
			break;
		case FILE_ID:
			queryBuilder.setTables(DBSchema.DATABASE_TABLE_FILE);
			//disini dimasukin kondisi 'where' sesuai id
			queryBuilder.appendWhere(File._ID + " = " + uri.getLastPathSegment());
			break;
		case SMS_LIST:
			queryBuilder.setTables(DBSchema.DATABASE_TABLE_SMS); //set builder buat melakukan query di tabel agenda
			if(TextUtils.isEmpty(sortOrder))
			{
				sortOrder = DBSchema.DEFAULT_SORT;
			}
			break;
		case SMS_ID:
			queryBuilder.setTables(DBSchema.DATABASE_TABLE_SMS);
			//disini dimasukin kondisi 'where' sesuai id
			queryBuilder.appendWhere(SMS._ID + " = " + uri.getLastPathSegment());
			break;	
		case REMINDERLOCATION_LIST:
			
			queryBuilder.setTables(DBSchema.DATABASE_TABLE_REMINDERLOCATION); //set builder buat melakukan query di tabel agenda
			
			if(TextUtils.isEmpty(sortOrder))
			{
				sortOrder = DBSchema.DEFAULT_SORT;
			}
			break;
		case REMINDERLOCATION_ID:
			queryBuilder.setTables(DBSchema.DATABASE_TABLE_REMINDERLOCATION);
			//disini dimasukin kondisi 'where' sesuai id
			queryBuilder.appendWhere(SMS._ID + " = " + uri.getLastPathSegment());
			break;	
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			   logQuery(queryBuilder,  projection, selection, sortOrder);
			}
			else {
			   logQueryDeprecated(queryBuilder, projection, selection, sortOrder);
			}
		Cursor cursor = queryBuilder.query(dbs, projection, selection, selectionArgs, null, null, sortOrder);
		
		if(useAuthorityUri)
		{
			cursor.setNotificationUri(getContext().getContentResolver(), SchedulePlusContract.CONTENT_URI);
		}
		else
		{
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
		}
		
		return cursor;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void logQuery(SQLiteQueryBuilder builder, String[] projection, String selection, String sortOrder) 
	{
		if (BuildConfig.DEBUG) 
		{
		   Log.v("cpsample", "query: " + builder.buildQuery(projection, selection, null, null, sortOrder, null));
		}
	}

	@SuppressWarnings("deprecation")
	private void logQueryDeprecated(SQLiteQueryBuilder builder, String[] projection, String selection, String sortOrder) 
	{
	   if (BuildConfig.DEBUG) 
	   {
	      Log.v("cpsample", "query: " + builder.buildQuery(projection, selection, null, null, null, sortOrder, null));
	   }
	}
	@Override
	public String getType(Uri uri) {
		switch(URI_MATCHER.match(uri))
		{
		case AGENDA_LIST:
			return Agenda.CONTENT_TYPE;
		case AGENDA_ID:
			return Agenda.CONTENT_AGENDA_TYPE;
		case FILE_LIST:
			return File.CONTENT_TYPE;
		case FILE_ID:
			return File.CONTENT_FILE_TYPE;
		case SMS_LIST:
			return SMS.CONTENT_TYPE;
		case SMS_ID:
			return SMS.CONTENT_SMS_TYPE;
		case REMINDERLOCATION_LIST:
			return ReminderLocation.CONTENT_TYPE;
		case REMINDERLOCATION_ID:
			return ReminderLocation.CONTENT_SMS_TYPE;
		case AGENDA_ENTITY_LIST:
			return AgendaEntity.CONTENT_ENTITY;
		case AGENDA_ENTITY_ID:
			return AgendaEntity.CONTENT_ENTITY_TYPE;
			
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		//ini cek kalo uri-nya salah
		if(URI_MATCHER.match(uri) != AGENDA_LIST && URI_MATCHER.match(uri) != FILE_LIST && URI_MATCHER.match(uri) != SMS_LIST && URI_MATCHER.match(uri) != REMINDERLOCATION_LIST)
		{
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		SQLiteDatabase dbs = mHelper.getWritableDatabase();
		if(URI_MATCHER.match(uri) == AGENDA_LIST)
		{
			//disini insert ke database
			long newID = dbs.insert(DBSchema.DATABASE_TABLE_AGENDA, null, values);
			return getUriForId(newID, uri);
		}
		else if(URI_MATCHER.match(uri) == SMS_LIST)
		{
			//disini insert ke database
			long newID = dbs.insert(DBSchema.DATABASE_TABLE_SMS, null, values);
			return getUriForId(newID, uri);
		}
		else if(URI_MATCHER.match(uri) == REMINDERLOCATION_LIST)
		{
			//disini insert ke database
			long newID = dbs.insert(DBSchema.DATABASE_TABLE_REMINDERLOCATION, null, values);
			return getUriForId(newID, uri);
		}
		else
		{
			long newID = dbs.insertWithOnConflict(DBSchema.DATABASE_TABLE_FILE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			return getUriForId(newID,uri);
		}
		
	}
	private Uri getUriForId(long newID, Uri uri) {
	      if (newID > 0) {
	         Uri itemUri = ContentUris.withAppendedId(uri, newID);
	         if (!isInBatchMode()) {
	            // notify all listeners of changes and return itemUri:
	            getContext().
	                  getContentResolver().
	                        notifyChange(itemUri, null);
	         }
	         return itemUri;
	      }
	      throw new SQLException("Problem while inserting into uri: " + uri);
		}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase dbs = mHelper.getWritableDatabase();
		int delCount = 0;
		switch(URI_MATCHER.match(uri))
		{
		case AGENDA_LIST:
			delCount = dbs.delete(DBSchema.DATABASE_TABLE_AGENDA, selection, selectionArgs);
			break;
		case AGENDA_ID:
			String theID = uri.getLastPathSegment();
			String where = Agenda._ID + " = " + theID;
			delCount = dbs.delete(DBSchema.DATABASE_TABLE_AGENDA, where, selectionArgs);
			break;
		case FILE_LIST:
			delCount = dbs.delete(DBSchema.DATABASE_TABLE_FILE, selection, selectionArgs);
			break;
		case FILE_ID:
			String theFileID = uri.getLastPathSegment();
			String whereFile = File._ID + " = " + theFileID;
			delCount = dbs.delete(DBSchema.DATABASE_TABLE_FILE, whereFile, selectionArgs);
			break;
		case SMS_LIST:
			delCount = dbs.delete(DBSchema.DATABASE_TABLE_SMS, selection, selectionArgs);
			break;
		case SMS_ID:
			String theSMSID = uri.getLastPathSegment();
			String whereSMS = SMS._ID + " = " + theSMSID;
			delCount = dbs.delete(DBSchema.DATABASE_TABLE_SMS, whereSMS, selectionArgs);
			break;
		case REMINDERLOCATION_LIST:
			delCount = dbs.delete(DBSchema.DATABASE_TABLE_REMINDERLOCATION, selection, selectionArgs);
			break;
		case REMINDERLOCATION_ID:
			String theReminderLocationID = uri.getLastPathSegment();
			String whereReminderLocation = ReminderLocation._ID + " = " + theReminderLocationID;
			delCount = dbs.delete(DBSchema.DATABASE_TABLE_REMINDERLOCATION, whereReminderLocation, selectionArgs);
			break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null, false);
		return delCount;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase dbs = mHelper.getWritableDatabase();
		int updateCount = 0;
		switch(URI_MATCHER.match(uri))
		{
		case AGENDA_LIST:
			updateCount = dbs.update(DBSchema.DATABASE_TABLE_AGENDA, values, selection, selectionArgs);
			break;
		case AGENDA_ID:
			String theID = uri.getLastPathSegment();
			String where = Agenda._ID + " = " + theID;
			if(!TextUtils.isEmpty(selection))
			{
				where += " AND " + selection;
			}
			updateCount = dbs.update(DBSchema.DATABASE_TABLE_AGENDA, values, where, selectionArgs);
			break;
		case SMS_LIST:
			updateCount = dbs.update(DBSchema.DATABASE_TABLE_SMS, values, selection, selectionArgs);
			break;
		case SMS_ID:
			String theSMSID = uri.getLastPathSegment();
			String whereSMS = SMS._ID + " = " + theSMSID;
			if(!TextUtils.isEmpty(selection))
			{
				whereSMS += " AND " + selection;
			}
			updateCount = dbs.update(DBSchema.DATABASE_TABLE_SMS, values, whereSMS, selectionArgs);
			break;
		case REMINDERLOCATION_LIST:
			updateCount = dbs.update(DBSchema.DATABASE_TABLE_REMINDERLOCATION, values, selection, selectionArgs);
			break;
		case REMINDERLOCATION_ID:
			String theReminderLocationID = uri.getLastPathSegment();
			String whereReminderLocation = ReminderLocation._ID + " = " + theReminderLocationID;
			if(!TextUtils.isEmpty(selection))
			{
				whereReminderLocation += " AND " + selection;
			}
			updateCount = dbs.update(DBSchema.DATABASE_TABLE_REMINDERLOCATION, values, whereReminderLocation, selectionArgs);
			break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		if (updateCount > 0 && !isInBatchMode()) 
		{
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return updateCount;
	}
	
	private boolean isInBatchMode() 
	{
	      return mIsInBatchMode.get() != null && mIsInBatchMode.get();
	}

	
}
