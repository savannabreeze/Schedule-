package com.skripsi.scheduleplus.dataprovider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class SchedulePlusContract 
{
	//authority. Intinya nama kontraknya, mirip2 sama nama package
	public static final String AUTHORITY = "de.skripsi.scheduleplus";
	
	//Konten URI untuk authority top-level scheduleplus
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY);
	
	//Perintah untuk select berdasarkan ID
	public static final String SELECT_WITH_ID = BaseColumns._ID + " = ?";
	
	//Konstanta untuk tabel agenda di provider
	public static final class Agenda implements CommonColumns
	{
		//Uri ke tabel agenda
		public static final Uri CONTENT_URI = Uri.withAppendedPath(SchedulePlusContract.CONTENT_URI, "agenda");
		
		//mime type direktori agenda
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/agenda";
		
		//mime type satu item agenda
		public static final String CONTENT_AGENDA_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/agenda";
		
		//proyeksi semua kolom di tabel
		public static final String[] AGENDA_PROJECTION = {"_id as _id", TITLE, TIME, DESCRIPTION, HAVE_ATTACHMENT};
		
		//Urutan sorting
		public static final String DEFAULT_SORT_ORDER = TIME + " DESC";
	}
	
	//Konstanta untuk tabel file di provider
	//Konstanta untuk tabel agenda di provider
	public static final class File implements BaseColumns
	{
		//Uri ke tabel file
		public static final Uri CONTENT_URI = Uri.withAppendedPath(SchedulePlusContract.CONTENT_URI, "agendaFiles");
		
		//mime type direktori file
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/agendaFiles";
		
		//mime type satu file
		public static final String CONTENT_FILE_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/agendaFiles";
		
		//nama kolom agendaID
		public static final String AGENDAID = "agendaID";
		
		//nama kolom filename
		public static final String FILENAME = "fileName";
		
		//nama kolom filelocation
		public static final String FILELOCATION = "fileLocation";
		
		//proyeksi semua kolom di tabel
		public static final String[] FILE_PROJECTION = {"_id as _id", AGENDAID, FILENAME, FILELOCATION};
		
		//Urutan sorting
		public static final String DEFAULT_SORT_ORDER = AGENDAID + " ASC";
	}
	
	public static final class SMS implements BaseColumns
	{
		//Uri ke tabel SMS
		public static final Uri CONTENT_URI = Uri.withAppendedPath(SchedulePlusContract.CONTENT_URI, "SMS");
		
		//mime type direktori SMS
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/SMS";
		
		//mime type satu item SMS
		public static final String CONTENT_SMS_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/SMS";
		
		//nama kolom SMSID
		public static final String PHONE = "phone";
		
		//nama kolom SMS
		public static final String TIME = "time";
				
		//nama kolom SMS
		public static final String MESSAGE = "message";
			
				
		//proyeksi semua kolom di tabel
		public static final String[] SMS_PROJECTION = {"_id as _id", PHONE, TIME, MESSAGE};
				
		//Urutan sorting
		public static final String DEFAULT_SORT_ORDER = TIME + " ASC";
	}
	
	public static final class ReminderLocation implements BaseColumns
	{
		//Uri ke tabel 
		public static final Uri CONTENT_URI = Uri.withAppendedPath(SchedulePlusContract.CONTENT_URI, "ReminderLocation");
		
		//mime type direktori 
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/ReminderLocation";
		
		//mime type satu item 
		public static final String CONTENT_SMS_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/ReminderLocation";
		
		public static final String REMINDERLOCATION_TITLE = "title";
		public static final String REMINDERLOCATION_TIME = "time";
		public static final String REMINDERLOCATION_DESCRIPTION = "description";
		public static final String PLACE_NAME = "place_name";
		public static final String LONGITUDE = "longitude";
		public static final String LATITUDE = "latitude";
		public static final String STATUS = "status";
			
				
		//proyeksi semua kolom di tabel
		public static final String[] REMINDERLOCATION_PROJECTION = {"_id as _id", REMINDERLOCATION_TITLE, REMINDERLOCATION_TIME, REMINDERLOCATION_DESCRIPTION, PLACE_NAME, LONGITUDE, LATITUDE, STATUS};
		
		//seleksi berdasarkan status
		public static final String REMINDERLOCATION_ARGS = STATUS+" = 0";
		//Urutan sorting
		public static final String DEFAULT_SORT_ORDER = REMINDERLOCATION_TIME + " ASC";
	}
	
	public static final class AgendaEntity implements CommonColumns
	{
		//Uri ke tabel file
				public static final Uri CONTENT_URI = Uri.withAppendedPath(SchedulePlusContract.CONTENT_URI, "agenda_entity");
				
				//mime type direktori file
				public static final String CONTENT_ENTITY = ContentResolver.CURSOR_DIR_BASE_TYPE + "/agenda_entity";
				
				//mime type satu file
				public static final String CONTENT_ENTITY_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/agenda_entity";
				
				//nama kolom filename
				public static final String FILENAME = "fileName";
				
				//nama kolom filelocation
				public static final String FILELOCATION = "fileLocation";
				
				//proyeksi semua kolom di tabel
				public static final String[] AGENDA_ENTITY_PROJECTION = {DBSchema.DATABASE_TABLE_AGENDA+"."+_ID, TITLE, TIME, DESCRIPTION, FILENAME, FILELOCATION};
				
				//Urutan sorting
				public static final String DEFAULT_SORT_ORDER = TIME + " ASC";
	}
	
	
	
	public static interface CommonColumns extends BaseColumns
	{
		public static final String TITLE = "title";
		public static final String TIME = "time";
		public static final String DESCRIPTION = "description";
		public static final String HAVE_ATTACHMENT = "haveAttachment";
	}
}
