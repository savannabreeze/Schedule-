package com.skripsi.scheduleplus;

public class Invitation 
{
	@com.google.gson.annotations.SerializedName("id")
	private String mId;
	
	@com.google.gson.annotations.SerializedName("eventId")
	private String mEventId;
		
	@com.google.gson.annotations.SerializedName("invitedId")
	private String mInvitedId;
	
	@com.google.gson.annotations.SerializedName("status")
	private int mStatus;
	
	public Invitation()
	{
		
	}
	
	public Invitation(String id, String event, String user, int status)
	{
		this.mId = id;
		this.mEventId = event;
		this.mInvitedId = user;
		this.mStatus = status;
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
}
