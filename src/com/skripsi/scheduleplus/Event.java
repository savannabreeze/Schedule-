package com.skripsi.scheduleplus;

import java.math.BigInteger;

public class Event 
{
	//Nama kolom
		@com.google.gson.annotations.SerializedName("id")
		private String mId;
		
		@com.google.gson.annotations.SerializedName("title")
		private String mTitle;
			
		@com.google.gson.annotations.SerializedName("time")
		private BigInteger mTime;
			
		@com.google.gson.annotations.SerializedName("place")
		private String mPlace;
			
		@com.google.gson.annotations.SerializedName("desc")
		private String mDescription;
		
		@com.google.gson.annotations.SerializedName("userId")
		private String mUID;
		
		public Event()
		{
			
		}
		
		public Event(String id, String title, BigInteger time, String place, String description, String UID)
		{
			this.mId = id;
			this.mTitle = title;
			this.mTime = time;
			this.mPlace = place;
			this.mDescription = description;
			this.mUID = UID;
		}
		
		public String get_event_id()
		{
			return this.mId;
		}
		
		public void set_event_id(String id)
		{
			this.mId = id;
		}
		
		public String get_title()
		{
			return this.mTitle;
		}

		public void set_title(String title)
		{
			this.mTitle = title;
		}

		public BigInteger get_time()
		{
			return this.mTime;
		}

		public void set_time(BigInteger time)
		{
			this.mTime = time;
		}
		
		public String get_place()
		{
			return this.mPlace;
		}

		public void set_place(String place)
		{
			this.mPlace = place;
		}
		
		public String get_desc()
		{
			return this.mDescription;
		}

		public void set_desc(String desc)
		{
			this.mDescription = desc;
		}
		
		public String get_userId()
		{
			return this.mUID;
		}

		public void set_userId(String UID)
		{
			this.mUID = UID;
		}
}
