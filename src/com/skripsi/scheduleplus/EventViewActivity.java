package com.skripsi.scheduleplus;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableDeleteCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;

@SuppressLint("HandlerLeak")
public class EventViewActivity extends Activity 
{
	protected TextView eventTitleTextView;
	protected TextView eventDateTextView;
	protected TextView eventPlaceTextView;
	protected TextView eventDescriptionTextView;
	
	protected ListView eventViewListView; 
	
	protected MobileServiceClient mClient;
	protected MobileServiceTable<Event> mEventTable;
	protected MobileServiceTable<InvitationExtended> mInvitationTable;
	protected MobileServiceTable<Invitation> mInvitation;
	
	protected InviteListAdapter mAdapter;
	
	private SharedPreferences mSharedPref;
	
	protected String userId;
	
	protected String eventId;
	
	private Intent receivedIntent;
	
	private Event loadedEvent;
	
	private long convertedTime;
	private static final String DATE_FORMAT_VIEW = "MMMM dd, yyyy HH:mm";
	private static final String DATE_FORMAT_DB = "yyyyMMddHHmm";
	private static final SimpleDateFormat dateFormatView = new SimpleDateFormat(DATE_FORMAT_VIEW,Locale.getDefault());
	private static final SimpleDateFormat dateFormatDb = new SimpleDateFormat(DATE_FORMAT_DB,Locale.getDefault());
	private Date fetchedDate;
	
	private Thread eventLoadThread;
	private Runnable eventLoadRunnable;
	
	private Thread eventDeleteThread;
	private Runnable eventDeleteRunnable;

	private Thread invitationLoadThread;
	private Runnable invitationLoadRunnable;
	
	private boolean eventLoadFlag = false;
	private boolean inviteLoadFlag = false;
	
	private ProgressDialog progs;
	private ProgressDialog progs2;
	
	private String inviteeId;
	Invitation delInvite;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_view_activity);
		
		receivedIntent = getIntent();
		eventId = receivedIntent.getStringExtra("eventId");
		Log.d("viewEventId", eventId);
		
		mSharedPref = getSharedPreferences("SCHEDULEPLUS_PREF", MODE_PRIVATE);
		userId = mSharedPref.getString("LOGGED_IN_ID", null);
		Log.d("EventView", userId);
		eventTitleTextView = (TextView) findViewById(R.id.eventTitleTextView);
		eventDateTextView = (TextView) findViewById(R.id.eventDateTextView);
		eventPlaceTextView = (TextView) findViewById(R.id.eventPlaceTextView);
		eventDescriptionTextView = (TextView) findViewById(R.id.eventDescriptionTextView);
		
		eventViewListView = (ListView) findViewById(R.id.eventViewListView);
		
		eventLoadRunnable = new Runnable()
		{

			@Override
			public void run() {
				eventLoadHandler.sendEmptyMessage(0);
			}
			
		};
		eventLoadThread = new Thread(null, eventLoadRunnable, "EventLoadThread");
		
		eventDeleteRunnable = new Runnable()
		{

			@Override
			public void run() {
				eventDeleteHandler.sendEmptyMessage(0);
			}
			
		};
		eventDeleteThread = new Thread(null, eventDeleteRunnable, "EventDeleteThread");
		
		invitationLoadRunnable = new Runnable()
		{

			@Override
			public void run() {
				invitationLoadHandler.sendEmptyMessage(0);
			}
			
		};
		invitationLoadThread = new Thread(null, invitationLoadRunnable, "InvitationLoadThread");

		
		
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		progs = ProgressDialog.show(this, "Loading", "Loading Data, Please Wait");
		if(eventLoadFlag == true)
		{
			eventLoadThread.run();
		}
		else
		{
			eventLoadThread.start();
		}
		
		if(inviteLoadFlag == true)
		{
			invitationLoadThread.run();
		}
		else
		{
			invitationLoadThread.start();
		}
	}
	@Override
	protected void onStop()
	{
		super.onStop();
		eventLoadThread.interrupt();
		invitationLoadThread.interrupt();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater mInflater = new MenuInflater(this);
		mInflater.inflate(R.menu.agenda_view_menu, menu);
		return true;
	}
	
	private void doCancelAttendance()
	{
		mInvitation.delete(delInvite, new TableDeleteCallback() {
			
			@Override
			public void onCompleted(Exception arg0, ServiceFilterResponse arg1) {
				if(arg0 == null)
				{
					createAndShowDialog("You have canceled your attendance", "Succedd");
				}
				else
				{
					createAndShowDialog(arg0, "Error");
				}
			}
		});
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.agendaEdit:
			if(userId.equals(loadedEvent.get_userId()))
			{
				Intent editIntent = new Intent(this, EventAdd.class);
				editIntent.putExtra("eventId", eventId);
				editIntent.putExtra("formMode", 2);
				startActivity(editIntent);
			}
			else
			{
				createAndShowDialog("You are not authorized to edit this event", "Warning");
			}
			break;
		case R.id.agendaDelete:
			if(userId.equals(loadedEvent.get_userId()))
			{
				AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
				confirmDialog.setTitle("Event Delete Confirmation");
				confirmDialog.setMessage("Are you sure you want to delete this event?");
				confirmDialog.setPositiveButton(R.string.Yes, 
						new DialogInterface.OnClickListener()
				{
	
					@Override
					public void onClick(DialogInterface dialog, int which) {
						progs2 = ProgressDialog.show(EventViewActivity.this, "Deleting", "Deleting Event");
						eventDeleteThread.start();
	
						
					}
					
				});
				confirmDialog.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
	
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						
					}
					
					
				});
				confirmDialog.create().show();
			}
			else
			{
				//createAndShowDialog("You are not authorized to delete this event", "Warning");
				mInvitation = mClient.getTable(Invitation.class);
				delInvite = new Invitation("", inviteeId, userId, 2);
				mInvitation.where().field("eventId").eq(eventId).and().field("invitedId").eq(userId)
				.execute(new TableQueryCallback<Invitation>() {

					@Override
					public void onCompleted(List<Invitation> arg0, int arg1,
							Exception arg2, ServiceFilterResponse arg3) {
						if(arg0 != null)
						{
							if(arg0.size() > 0)
							{
								delInvite.set_invitation_id(arg0.get(0).get_invitation_id());
								doCancelAttendance();
							}
						}
						else if(arg2 != null)
						{
							
						}
						else
						{
							
						}
					}
				});
				
			}
			break;
		case android.R.id.home:
			this.finish();
			break;
		}
		return true;
	}
	
	//	Event Data Loader AsyncTask
	@SuppressLint("HandlerLeak")
	private Handler eventLoadHandler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
				try
				{
					mClient = new MobileServiceClient(
							"https://scheduleplustest.azure-mobile.net/",
							"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
							EventViewActivity.this);
					mEventTable = mClient.getTable(Event.class);
					mEventTable.where().field("id").eq(eventId).execute(new TableQueryCallback<Event>() 
					{
						
						@Override
						public void onCompleted(List<Event> arg0, int arg1, Exception arg2,
								ServiceFilterResponse arg3) 
						{
							if(arg0 != null)
							{
								if(arg0.size() == 1)
								{
									convertedTime = arg0.get(0).get_time().longValue();
									try {
										fetchedDate = dateFormatDb.parse(String.valueOf(convertedTime));
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									inviteeId = arg0.get(0).get_userId();
									eventTitleTextView.setText(arg0.get(0).get_title());
									eventDateTextView.setText(dateFormatView.format(fetchedDate));
									eventPlaceTextView.setText(arg0.get(0).get_place());
									eventDescriptionTextView.setText(arg0.get(0).get_desc());
									loadedEvent = arg0.get(0);
								}
								else
								{
									createAndShowDialog("Query Error", "Error");
								}
							}
							else if(arg2 != null)
							{
								createAndShowDialog(arg2, "Error");
							}
							
						}
					});
					stopThread(eventLoadThread);
					
				}
				catch(MalformedURLException e)
				{
					createAndShowDialog(e, "error");
					stopThread(eventLoadThread);
				}
				eventLoadFlag = true;
		}
		
		
		
	};
	
	private Handler invitationLoadHandler = new Handler()
	{

		public void handleMessage(Message msg) 
		{
				try
				{
					
					mClient = new MobileServiceClient(
							"https://scheduleplustest.azure-mobile.net/",
							"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
							EventViewActivity.this);
					mInvitationTable = mClient.getTable(InvitationExtended.class);
					mInvitationTable.parameter("invitedId", "").parameter("eventId", eventId).parameter("queryType", "2").execute(new TableQueryCallback<InvitationExtended>() {
						
						@Override
						public void onCompleted(List<InvitationExtended> arg0, int arg1, Exception arg2,
								ServiceFilterResponse arg3) {
							if(arg0 != null)
							{
									if(arg0.size() != 0)
									{
										if(progs != null)
										{
											progs.dismiss();
										}
										mAdapter = new InviteListAdapter(EventViewActivity.this, android.R.layout.simple_list_item_2, arg0);
										eventViewListView.setAdapter(mAdapter);
									}
									else
									{
										if(progs != null)
										{
											progs.dismiss();
										}
									}
							}
							else if(arg2 != null)
							{
								if(progs != null)
								{
									progs.dismiss();
								}
								createAndShowDialog(arg2, "Error");
							}
							else
							{
								if(progs != null)
								{
									progs.dismiss();
								}
							}
							
						}
					});
					stopThread(invitationLoadThread);
				}
				catch(MalformedURLException e)
				{
					createAndShowDialog(e, "Error");
					stopThread(invitationLoadThread);
				}
				inviteLoadFlag = true;
		}
		
		
	};
	
	private Handler eventDeleteHandler = new Handler()
	{
		private Event deletedEvent = new Event();
		public void handleMessage(Message msg)
		{
				try
				{
					mClient = new MobileServiceClient(
							"https://scheduleplustest.azure-mobile.net/",
							"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
							EventViewActivity.this);
					mEventTable = mClient.getTable(Event.class);
					deletedEvent.set_event_id(eventId);
					mEventTable.delete(deletedEvent, new TableDeleteCallback() {
						
						@Override
						public void onCompleted(Exception arg0, ServiceFilterResponse arg1) {
							if(arg0 == null)
							{
								if(progs2 != null)
								{
									progs2.dismiss();
								}
								EventViewActivity.this.runOnUiThread(new Runnable() 
								{
									
									@Override
									public void run() {
										AlertDialog.Builder builder = new AlertDialog.Builder(EventViewActivity.this);
				            			builder.setMessage("Event deleted successfully");
				            			builder.setTitle("Success");
				            			builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				            				
				            				@Override
				            				public void onClick(DialogInterface dialog, int which) {
				            					EventViewActivity.this.finish();				            				}
				            			});
				            			builder.create().show();
																					
									}
								});
							}
							else
							{
								if(progs2 != null)
								{
									progs2.dismiss();
								}
								Log.d("EventView", "masuk else");
								createAndShowDialog(arg0,"Error");
							}
							
						}
					});
					stopThread(eventDeleteThread);

				}
				catch(MalformedURLException e)
				{
					createAndShowDialog(e, "Delete Error");
				}
		}
		
	};
	
	private void createAndShowDialog(Exception exception, String title) {
		Throwable ex = exception;
		if(exception.getCause() != null){
			ex = exception.getCause();
		}
		createAndShowDialog(ex.getMessage(), title);
	}
	
	private void createAndShowDialog(String message, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		builder.create().show();
	}
	
	private synchronized void stopThread(Thread theThread)
	{
	    if (theThread != null)
	    {
	        theThread = null;
	    }
	}
}
