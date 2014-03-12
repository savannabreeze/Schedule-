package com.skripsi.scheduleplus;

public class InvitationExtended 
{
	@com.google.gson.annotations.SerializedName("id")
	private String mId;
	
	@com.google.gson.annotations.SerializedName("eventId")
	private String mEventId;
		
	@com.google.gson.annotations.SerializedName("invitedId")
	private String mInvitedId;
	
	@com.google.gson.annotations.SerializedName("status")
	private int mStatus;
	
	@com.google.gson.annotations.SerializedName("title")
	private String mTitle;
	
	@com.google.gson.annotations.SerializedName("first_name")
	private String mFName;
	
	@com.google.gson.annotations.SerializedName("last_name")
	private String mLName;
	
	public InvitationExtended()
	{
		
	}
	
	public InvitationExtended(String id, String event, String user, int status, String title, String fName, String lName)
	{
		this.mId = id;
		this.mEventId = event;
		this.mInvitedId = user;
		this.mStatus = status;
		this.mTitle = title;
		this.mFName = fName;
		this.mLName = lName;
	}
	
	public void set_invitation_id(String id)
	{
		this.mId = id;
	}
	
	public String get_invitation_id()
	{
		return this.mId;
	}
	
	public void set_event_id(String event)
	{
		this.mEventId = event;
	}
	
	public String get_event_id()
	{
		return this.mEventId;
	}
	
	public void set_invited_id(String invited)
	{
		this.mInvitedId = invited;
	}
	
	public String get_invited_id()
	{
		return this.mInvitedId;
	}
	
	public void set_status(int status)
	{
		this.mStatus = status;
	}
	
	public int get_status()
	{
		return this.mStatus;
	}
	
	public void set_title(String title)
	{
		this.mTitle = title;
	}
	
	public String get_title()
	{
		return this.mTitle;
	}
	
	
	public void set_first_name(String fName)
	{
		this.mFName = fName;
	}
	
	public String get_first_name()
	{
		return this.mFName;
	}
	
	
	public void set_last_name(String lName)
	{
		this.mLName = lName;
	}
	
	public String get_last_name()
	{
		return this.mLName;
	}
	
}
