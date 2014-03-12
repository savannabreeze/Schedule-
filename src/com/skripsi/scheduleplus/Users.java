package com.skripsi.scheduleplus;

public class Users 
{
	//Nama kolom
	@com.google.gson.annotations.SerializedName("id")
	private String mId;
	
	@com.google.gson.annotations.SerializedName("email")
	private String mEmail;
		
	@com.google.gson.annotations.SerializedName("password")
	private String mPassword;
		
	@com.google.gson.annotations.SerializedName("first_name")
	private String mFirstName;
		
	@com.google.gson.annotations.SerializedName("last_name")
	private String mLastName;
	
	
	//Nama kolom
	
	//constructor
		public Users()
		{
			
		}
		
		//Inisialisasi ContactListItem
		public Users(String id, String email, String pass, String frst_name, String lst_name)
		{
			this.mId = id;
			this.mPassword = pass;
			this.mFirstName = frst_name;
			this.mLastName = lst_name;
			this.mEmail = email;
		}
		
		//getter friend user id
		public String get_user_id()
		{
			return this.mId;
		}
		
		//setter friend user id
		public void set_user_id(String id)
		{
			this.mId = id;
		}
		
		//getter first name
		public String get_first_name()
		{
			return this.mFirstName;
		}
				
		//setter first name
		public void set_first_name(String fName)
		{
			this.mFirstName = fName;
		}
		//getter Last Name
		
		public String get_last_name()
		{
			return this.mLastName;
		}
				
		//setter Last Name
		public void set_last_name(String lName)
		{
			this.mLastName = lName;
		}
		
		//getter friend request accept status
		public String get_email()
		{
			return this.mEmail;
		}
				
		//setter friend user id
		public void set_email(String email)
		{
			this.mEmail = email;
		}

		//getter friend request accept status
		public String get_password()
		{
			return this.mPassword;
		}
				
		//setter friend user id
		public void set_password(String password)
		{
			this.mPassword = password;
		}
		
		/*
		@com.google.gson.annotations.SerializedName("handle")
	    private String mHandle;


		public String getHandle() {
			return mHandle;
		}


		public final void setHandle(String handle) {
			mHandle = handle;
		}*/
}
