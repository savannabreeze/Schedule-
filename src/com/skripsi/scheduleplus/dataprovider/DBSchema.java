package com.skripsi.scheduleplus.dataprovider;


interface DBSchema {
		//Nama Kolom Di Tabel Agenda
		public static final String KEY_AGENDAID = "_id";
		public static final String KEY_TITLE_AGENDA = "title";
		public static final String KEY_TIME_AGENDA = "time";
		public static final String KEY_DESCRIPTION_AGENDA = "description";
		
		//Nama Kolom Di Tabel File
		public static final String KEY_FILEID = "_id";
		public static final String KEY_FILENAME_FILE = "fileName";
		public static final String KEY_FILELOCATION_FILE = "fileLocation";
		
		//Nama Kolom Di Tabel SMS
		public static final String KEY_SMSID = "_id";
		public static final String KEY_PHONE_SMS = "phone";//harusnya join dari tabel contact
		public static final String KEY_TIME_SMS = "time";
		public static final String KEY_MESSAGE_SMS = "message";
		
		//Nama Kolom Di Tabel Reminder
		public static final String KEY_REMINDERLOCATIONID = "_id";
		public static final String KEY_TITLE_REMINDERLOCATION = "title";
		public static final String KEY_DESCRIPTION_REMINDERLOCATION = "description";
		public static final String KEY_TIME_REMINDERLOCATION = "time";
		public static final String KEY_PLACE_NAME = "place_name";
		public static final String KEY_LATITUDE = "latitude";
		public static final String KEY_LONGITUDE = "longitude";
		
		//Tag LogCat
		public static final String TAG = "DBHandler";
		
		//Nama Database
		public static final String DATABASE_NAME = "SchedulePlus.db";
		
		//Versi Database
		public static final int DATABASE_VERSION = 3;
		
		//Nama Tabel
		public static final String DATABASE_TABLE_AGENDA = "Agenda";
		public static final String DATABASE_TABLE_FILE = "File";
		public static final String DATABASE_TABLE_SMS = "SMS";
		public static final String DATABASE_TABLE_REMINDERLOCATION = "ReminderLocation";

		
		//Sintaks Create
		public static final String CREATE_AGENDA ="CREATE TABLE Agenda ( _id INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR(10)," +
				"time long, description VARCHAR(1000), haveAttachment INTEGER)";
		public static final String CREATE_FILE ="CREATE TABLE File ( _id INTEGER PRIMARY KEY AUTOINCREMENT, agendaID INTEGER," +
				"fileName VARCHAR(100), fileLocation VARCHAR(500), FOREIGN KEY(agendaID)REFERENCES agenda( _id) ON UPDATE CASCADE ON DELETE CASCADE)";
		public static final String CREATE_SMS ="CREATE TABLE SMS ( _id INTEGER PRIMARY KEY AUTOINCREMENT, phone VARCHAR(20)," +
				"time long, message VARCHAR(1000))";
		public static final String CREATE_REMINDERLOCATION ="CREATE TABLE ReminderLocation ( _id INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR(50)," +
				"description VARCHAR(1000), time long,  place_name VARCHAR(100), latitude REAL, longitude REAL, status INTEGER)";
		
		//Sintaks Drop
		public static final String DROP_FILE_TABLE = "DROP TABLE IF EXISTS file";
		public static final String DROP_AGENDA_TABLE = "DROP TABLE IF EXISTS agenda";
		public static final String DROP_SMS_TABLE = "DROP TABLE IF EXISTS SMS";
		public static final String DROP_REMINDERLOCATION_TABLE = "DROP TABLE IF EXISTS ReminderLocation";
		
		//Sintaks Where ID
		public static final String WHERE_ID = "_id = ?";
		
		//Sintaks sort order
		public static final String DEFAULT_SORT = "time ASC";
		
}
