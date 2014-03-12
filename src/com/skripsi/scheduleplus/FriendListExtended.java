package com.skripsi.scheduleplus;

public class FriendListExtended {
	
	//Nama kolom
	@com.google.gson.annotations.SerializedName("id")
	private String mId;
	
	@com.google.gson.annotations.SerializedName("user_id")
	private String mUserId;
	
	@com.google.gson.annotations.SerializedName("friend_user_id")
	private String mFriendUserId;
	
	@com.google.gson.annotations.SerializedName("first_name")
	private String mFirstName;
	
	@com.google.gson.annotations.SerializedName("last_name")
	private String mLastName;
	
	@com.google.gson.annotations.SerializedName("email")
	private String mEmail;
	
	@com.google.gson.annotations.SerializedName("acc_status")
	private int mAccStatus;
	//Nama kolom
	
	//constructor
	public FriendListExtended()
	{
		
	}
	
	//Inisialisasi ContactListItem
	public FriendListExtended(String id, String user_id, String f_user_id, int acc_stat, String frst_name, String lst_name, String email)
	{
		this.mId = id;
		this.mUserId = user_id;
		this.mFriendUserId = f_user_id;
		this.mFirstName = frst_name;
		this.mLastName = lst_name;
		this.mEmail = email;
		this.mAccStatus = acc_stat;
	}
	
	public String get_id()
	{
		return this.mId;
	}
	
	public void set_id(String id)
	{
		this.mId = id;
	}
	
	//getter user id
	public String get_user_id()
	{
		return this.mUserId;
	}
	
	//setter user_id
	public void set_user_id(String uid)
	{
		this.mUserId = uid;
	}
	
	//getter friend user id
	public String get_friend_user_id()
	{
		return this.mFriendUserId;
	}
	
	//setter friend user id
	public void set_friend_user_id(String id)
	{
		this.mFriendUserId = id;
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
	
	//getter email
	public String get_email()
	{
		return this.mEmail;
	}
			
	//setter email
	public void set_email(String email)
	{
		this.mEmail = email;
	}
	
	//getter friend acceptance status
	public int get_friend_acc()
	{
		return this.mAccStatus;
	}
	
	//setter friend acceptance status
	public void set_friend_acc(int acc_stats)
	{
		this.mAccStatus = acc_stats;
	}
	//
	
	@com.google.gson.annotations.SerializedName("handle")
    private String mHandle;


	public String getHandle() {
		return mHandle;
	}


	public final void setHandle(String handle) {
		mHandle = handle;
	}
}
