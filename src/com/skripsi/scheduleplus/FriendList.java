package com.skripsi.scheduleplus;

public class FriendList 
{
	@com.google.gson.annotations.SerializedName("id")
	private String mId;
	
	@com.google.gson.annotations.SerializedName("user_id")
	private String mUserId;
	
	@com.google.gson.annotations.SerializedName("friend_user_id")
	private String mFriendUserId;
	
	@com.google.gson.annotations.SerializedName("acc_status")
	private int mAccStatus;
	
	//@com.google.gson.annotations.SerializedName("handle")
    //private String mHandle;
	
	public FriendList()
	{
		
	}
	
	public FriendList(String id, String userId, String friendUserId, int accStatus)
	{
		this.mId = id;
		this.mUserId = userId;
		this.mFriendUserId = friendUserId;
		this.mAccStatus = accStatus;
	}
	
	public String get_id()
	{
		return this.mId;
	}
	
	public void set_id(String id)
	{
		this.mId = id;
	}
	
	public String get_user_id()
	{
		return this.mUserId;
	}
	
	public void set_user_id(String userId)
	{
		this.mUserId = userId;
	}
	
	public String get_friend_id()
	{
		return this.mFriendUserId;
	}
	
	public void set_friend_id(String friendId)
	{
		this.mFriendUserId = friendId;
	}
	
	public int get_acc_status()
	{
		return this.mAccStatus;
	}
	
	public void set_acc_status(int accStatus)
	{
		this.mAccStatus = accStatus;
	}
	/*
	public String getHandle() 
	{
		return mHandle;
	}
	
	public final void setHandle(String handle) 
	{
		mHandle = handle;
	}*/
}
