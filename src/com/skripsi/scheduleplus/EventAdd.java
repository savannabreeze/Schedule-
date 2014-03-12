package com.skripsi.scheduleplus;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableDeleteCallback;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;



public class EventAdd extends Activity
{
	private MobileServiceClient mClient;
	private MobileServiceTable<Event> mEventTable;
	private MobileServiceTable<Invitation> mInvitationTable;
	private MobileServiceTable<InvitationExtended> mInviteExtendedTable;
	
	private Intent receivedIntent;
	private int formMode;
	String eventID;
	
	EditText eventTitleAddEditText;
	EditText eventDateAddEditText;
	EditText eventTimeAddEditText;
	EditText eventPlaceAddEditText;
	EditText eventDescriptionAddEditText;
	protected int mHour;
	protected int mDay;
	protected int mMonth;
	protected int mYear;
	protected int mMin;
	
	// List penampung invitation
	private List<Invitation> invitationList;
	private List<InvitationExtended> invitationExtendedList;
	
	InviteListAdapter inviteAdapter;
	
	private static final String DATE_FORMAT = "yyyyMMddHHmm"; //String format tanggal yang dipake buat konversi ke long. Format ini yang disimpen di database
	private static final String DATE_FORMAT_VIEW = "MMM d, yyyy"; //String format tanggal yang dipake buat ditampilin ke user
	private static final String TIME_FORMAT_VIEW = "HH : mm";
	
	//Objek yang dipake buat format tanggal
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
	private static final SimpleDateFormat dateFormatView = new SimpleDateFormat(DATE_FORMAT_VIEW, Locale.getDefault());
	private static final SimpleDateFormat timeFormatView = new SimpleDateFormat(TIME_FORMAT_VIEW, Locale.getDefault());
	
	//Objek Date yang menampung tanggal dan waktu yang dipilih
	private Date pickedDate;
	private Date pickedTime;
	private Date pickedDateAndTime;
	
	//Objek Calendar yang menampung Tanggal sistem dan jadwal
	private Calendar dateAndTimeCalendar;
	private Calendar systemCal;
	
	//Variabel penampung format tanggal untuk diinsert ke database
	private long saveDateLong;
	private String saveDateString;
	
	//	Thread untuk menyimpan event ke database
	private Thread eventFormThread;
	private Runnable eventFormRunnable;
	private Bundle eventFormBundle;
	private Message eventFormMessage;
	
	//	Thread untuk menyimpan invitation ke database
	private Thread invitationFormThread;
	private Runnable invitationFormRunnable;
	private Bundle invitationFormBundle;
	private Message invitationFormMessage;
	
	//	Objek untuk list invitation
	private ListView invitedListView;
	
	//	SharedPreferences untuk mengambil session
	private SharedPreferences mSharedPref;
	
	private boolean errorFlag = false;
	
	private int existingInvite = 0;
	
	private ProgressDialog progs;
	private ProgressDialog progs2;
	
	private boolean eventHandlerFlag = false;
	private boolean invitationHandlerFlag = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_add_activity);
		receivedIntent = getIntent();
		formMode = receivedIntent.getIntExtra("formMode", 1);
		eventID = receivedIntent.getStringExtra("eventId");

		systemCal = Calendar.getInstance(Locale.getDefault());
		dateAndTimeCalendar = Calendar.getInstance(Locale.getDefault());
		
		mSharedPref = getSharedPreferences("SCHEDULEPLUS_PREF", MODE_PRIVATE);
		
		invitationList = new ArrayList<Invitation>();
		invitationExtendedList = new ArrayList<InvitationExtended>();
		
		eventTitleAddEditText = (EditText) findViewById(R.id.eventTitleAddEditText);
		eventDateAddEditText= (EditText) findViewById(R.id.eventDateAddEditText);
		eventTimeAddEditText= (EditText) findViewById(R.id.eventTimeAddEditText);
		eventPlaceAddEditText= (EditText) findViewById(R.id.eventPlaceAddEditText);
		eventDescriptionAddEditText= (EditText) findViewById(R.id.eventDescriptionAddEditText);
		invitedListView = (ListView) findViewById(R.id.invitedListView);
		
		inviteAdapter = new InviteListAdapter(this, android.R.layout.simple_list_item_2, invitationExtendedList);
		invitedListView.setAdapter(inviteAdapter);
		
		try
		{
			mClient = new MobileServiceClient(
					"https://scheduleplustest.azure-mobile.net/",
					"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
					this);
			if(formMode != 1)
			{
					progs = ProgressDialog.show(this, "Loading", "Loading Data, Please Wait");
					Log.d("EventForm", "Masuk load data");
					mEventTable = mClient.getTable(Event.class);
					mEventTable.where().field("id").eq(eventID).execute(new TableQueryCallback<Event>() {
						
						@Override
						public void onCompleted(List<Event> arg0, int arg1, Exception arg2,
								ServiceFilterResponse arg3) {
							if(arg0!= null)
							{
								if(arg0.size() != 0)
								{
									Log.d("EventForm", "fetched data > 0");
									BigInteger tempDateTime = arg0.get(0).get_time();
									long longDateTime = tempDateTime.longValue();
									try {
										pickedDateAndTime = dateFormat.parse(String.valueOf(longDateTime));
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									eventTitleAddEditText.setText(arg0.get(0).get_title());
									eventDateAddEditText.setText(dateFormatView.format(pickedDateAndTime));
									eventTimeAddEditText.setText(timeFormatView.format(pickedDateAndTime));
									eventPlaceAddEditText.setText(arg0.get(0).get_place());
									eventDescriptionAddEditText.setText(arg0.get(0).get_desc());
									
									inviteLoad();
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
							
							
						}
					});
					
					
			}
		}
		catch(MalformedURLException e)
		{
			createAndShowDialog(e, "Error");
		}
		
		
		invitedListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final int pos = position;
				AlertDialog.Builder builder = new AlertDialog.Builder(EventAdd.this);
				builder.setMessage("Are you sure you want to remove the contact from your invite list?");
				builder.setTitle("Warning");
				builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d("EventForm", "position: "+pos);
						Log.d("EventForm", "existingInvite:"+existingInvite);
						if(pos < existingInvite)
						{
							Invitation delInvitation = new Invitation(invitationExtendedList.get(pos).get_invitation_id(),
									invitationExtendedList.get(pos).get_event_id(),
									invitationExtendedList.get(pos).get_invited_id(), 
									invitationExtendedList.get(pos).get_status());
							mInvitationTable = mClient.getTable(Invitation.class);
							mInvitationTable.delete(delInvitation, new TableDeleteCallback() {
								
								@Override
								public void onCompleted(Exception arg0, ServiceFilterResponse arg1) {
									if(arg0 == null)
									{
										invitationExtendedList.remove(pos);
										inviteAdapter.notifyDataSetChanged();
										createAndShowDialog("User has been removed from invitation list", "Success");
									}
									else if(arg0 != null)
									{
										createAndShowDialog(arg0, "Error");
									}
								}
							});
							existingInvite--;
						}
						else
						{
							invitationExtendedList.remove(pos);
							for(int y = 0; y < invitationList.size(); y++)
							{
								if(invitationList.get(y).get_invited_id().equals(invitationExtendedList.get(pos).get_invited_id()))
								{
									invitationList.remove(y);
									break;
								}
							}
							
						}
					}
				});
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				builder.create().show();
				return true;
			}
		});
		
		eventFormRunnable = new Runnable()
		{

			@Override
			public void run() {
				eventFormHandler.sendMessage(eventFormMessage);
				
			}
			
		};
		eventFormThread = new Thread(null, eventFormRunnable,"eventFormThread");
		
		invitationFormRunnable = new Runnable()
		{

			@Override
			public void run() {
				invitationFormHandler.sendMessage(invitationFormMessage);
			}
			
		};
		invitationFormThread = new Thread(null, invitationFormRunnable, "invitationFormThread");
		
		
		
		
		
		eventDateAddEditText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {

	                    @Override
	                    public void onDateSet(DatePicker view, int year, int monthofyear, int dayofmonth) {
	                        // TODO Auto-generated method stub
	                        mMonth = monthofyear;
	                        mYear = year;
	                        mDay = dayofmonth;
	                        dateAndTimeCalendar.set(Calendar.YEAR, year);
	                        dateAndTimeCalendar.set(Calendar.MONTH, monthofyear);
	                        dateAndTimeCalendar.set(Calendar.DAY_OF_MONTH, dayofmonth);
	                        if(dateAndTimeCalendar.getTimeInMillis() < System.currentTimeMillis())
	                        {
	                        	createAndShowDialog("You cannot enter a past date", "Error");
	                        }
	                        else
	                        {
	                        	pickedDate = dateAndTimeCalendar.getTime();
	                        	eventDateAddEditText.setText(dateFormatView.format(pickedDate));
	                        }
	                    }
	                };
	                new DatePickerDialog(EventAdd.this,d,systemCal.get(Calendar.YEAR),systemCal.get(Calendar.MONTH),systemCal.get(Calendar.DAY_OF_MONTH)).show();
			}
		});
		
		eventTimeAddEditText.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						TimePickerDialog.OnTimeSetListener t=new TimePickerDialog.OnTimeSetListener() {

		                    @Override
		                    public void onTimeSet(TimePicker view2, int hour, int min) {
		                        mHour = hour;
		                        mMin = min;
		                        dateAndTimeCalendar.set(Calendar.HOUR_OF_DAY, hour);
		                        dateAndTimeCalendar.set(Calendar.MINUTE, min);
		                        dateAndTimeCalendar.set(Calendar.MILLISECOND, 0);
		                        pickedTime = dateAndTimeCalendar.getTime();
		                        eventTimeAddEditText.setText(timeFormatView.format(pickedTime));
		                    }
		                };
		                new TimePickerDialog(EventAdd.this,t,systemCal.get(Calendar.HOUR_OF_DAY),systemCal.get(Calendar.MINUTE),true).show();
					}
				});
		
		eventFormBundle = new Bundle();
		eventFormMessage = new Message();
		invitationFormBundle = new Bundle();
		invitationFormMessage = new Message();
		
		
	}
	
	private void inviteLoad()
	{
		mInviteExtendedTable = mClient.getTable(InvitationExtended.class);
		mInviteExtendedTable.parameter("queryType", "3").parameter("eventId", eventID)
		.execute(new TableQueryCallback<InvitationExtended>() {

			@Override
			public void onCompleted(List<InvitationExtended> arg0,
					int arg1, Exception arg2,
					ServiceFilterResponse arg3) {
				if(arg0 != null)
				{
					if(arg0.size() > 0)
					{
						for(int z = 0; z < arg0.size(); z++)
						{
							invitationExtendedList.add(arg0.get(z));
						}
						existingInvite = arg0.size();
						inviteAdapter.notifyDataSetChanged();
						if(progs != null)
						{
							progs.dismiss();
						}
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
					createAndShowDialog(arg2, "Invite List Error");
				}
				else
				{
					if(progs != null)
					{
						progs.dismiss();
					}
					createAndShowDialog("Unknown Error Occured While Loading Invitation List", "Error");
				}
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater mInflater = new MenuInflater(this);
		mInflater.inflate(R.menu.event_form_menu, menu);
		return true;
		
	}
	
	private void backAlertDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(EventAdd.this);
		builder.setMessage("You have unsaved changes. Exit anyway?");
		builder.setTitle("Warning");
		builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EventAdd.this.finish();	
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		builder.create().show();
	}
	
	@SuppressLint("HandlerLeak")
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.event_save:
			if(eventTitleAddEditText.getText().toString().equals("") && eventDateAddEditText.getText().toString().equals("") 
					&& eventTimeAddEditText.getText().toString().equals("") && eventPlaceAddEditText.getText().toString().equals("")
					&& eventDescriptionAddEditText.getText().toString().equals(""))
			{
				createAndShowDialog("You must fill all field", "Error");
			}
			else if(eventTitleAddEditText.getText().toString().equals(""))
			{
				createAndShowDialog("You must enter a title", "Error");
			}
			else if(eventDateAddEditText.getText().toString().equals(""))
			{
				createAndShowDialog("You must enter a date", "Error");
			}
			else if(eventTimeAddEditText.getText().toString().equals(""))
			{
				createAndShowDialog("You must enter a time", "Error");
			}
			else if(eventPlaceAddEditText.getText().toString().equals(""))
			{
				createAndShowDialog("You must enter a location", "Error");
			}
			else if(eventDescriptionAddEditText.getText().toString().equals(""))
			{
				createAndShowDialog("You must enter a description", "Error");
			}
			else
			{
				pickedDateAndTime = dateAndTimeCalendar.getTime();
				saveDateString = dateFormat.format(pickedDateAndTime);
				saveDateLong = Long.parseLong(saveDateString);
				eventFormBundle.putInt("formMode", formMode);
				eventFormBundle.putString("tableTarget", "Event");
				eventFormBundle.putString("eventTitle", eventTitleAddEditText.getText().toString());
				eventFormBundle.putLong("eventDateTime", saveDateLong);
				eventFormBundle.putString("eventPlace", eventPlaceAddEditText.getText().toString());
				eventFormBundle.putString("eventDesc", eventDescriptionAddEditText.getText().toString());
				eventFormBundle.putString("eventUser", mSharedPref.getString("LOGGED_IN_ID", null));
				eventFormMessage.setData(eventFormBundle);
				if(!eventHandlerFlag)
				{
					progs2 = ProgressDialog.show(this, "Saving", "Saving Event");
					eventFormThread.start();
				}
				else
				{
					progs2 = ProgressDialog.show(this, "Saving", "Saving Event");
					eventFormThread.run();
				}
			}
			break;
		case R.id.event_invite:
			Intent pickFriendIntent = new Intent(this, ContactList.class);
			pickFriendIntent.putExtra("forResult", true);
			startActivityForResult(pickFriendIntent, 1);
			break;
		case android.R.id.home:
			backAlertDialog();
			break;
		}
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
	        backAlertDialog();
			return true;
		}
		return super.onKeyDown(keyCode, event);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == 1)
		{
			if(resultCode == RESULT_OK)
			{
				boolean exist = false;
				String resultId = data.getStringExtra("selectedId");
				String resultName = data.getStringExtra("selectedName");
				
				for(int x =0; x < invitationExtendedList.size(); x++)
				{
					if(resultId.equals(invitationExtendedList.get(x).get_invited_id()))
					{
						exist = true;
						break;
					}
				}
				if(!exist)
				{
					Invitation newInvitation = new Invitation();
					newInvitation.set_invited_id(resultId);
					newInvitation.set_status(1);
					invitationList.add(newInvitation);
					
					InvitationExtended newInvitationExtended = new InvitationExtended();
					newInvitationExtended.set_invited_id(resultId);
					newInvitationExtended.set_first_name(resultName.substring(0, resultName.indexOf(" ")));
					newInvitationExtended.set_last_name(resultName.substring(resultName.indexOf(" ") + 1));
					invitationExtendedList.add(newInvitationExtended);
					invitedListView.setAdapter(inviteAdapter);
					inviteAdapter.notifyDataSetChanged();
				}
				else
				{
					createAndShowDialog("User Already Invited", "Warning");
				}
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	private Handler eventFormHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			eventHandlerFlag = true;
			mEventTable = mClient.getTable(Event.class);
			Bundle receivedBundle = msg.getData();
			int formMode = receivedBundle.getInt("formMode");
			BigInteger dateTime = BigInteger.valueOf(receivedBundle.getLong("eventDateTime"));
			Event newEvent = new Event(null, receivedBundle.getString("eventTitle"), dateTime, receivedBundle.getString("eventPlace")
					, receivedBundle.getString("eventDesc"), receivedBundle.getString("eventUser"));
			if(formMode == 1)
			{
				Log.d("EventForm", "Masuk formMode 1 Di Thread");
				try
				{
					mClient = new MobileServiceClient(
							"https://scheduleplustest.azure-mobile.net/",
							"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
							EventAdd.this);
					
					mEventTable.insert(newEvent, new TableOperationCallback<Event>() {
						
						@Override
						public void onCompleted(Event arg0, Exception arg1,
								ServiceFilterResponse arg2) 
						{
							Log.d("EventAdd", "insertCompleted");
							if(arg1 == null)
							{
								eventID = arg0.get_event_id();
								invitationFormBundle.putInt("operation", 1);
								invitationFormBundle.putString("eventId", eventID);
								invitationFormMessage.setData(invitationFormBundle);
								invitationFormMessage.obj = (Object) invitationList;
								if(!invitationHandlerFlag)
								{
									invitationFormThread.start();
								}
								else
								{
									invitationFormThread.run();
								}
								stopThread(eventFormThread);
							}
							else
							{
								if(progs2 != null)
								{
									progs2.dismiss();
								}
								createAndShowDialog(arg1, "Insert Error");
								stopThread(eventFormThread);
							}
						}
					});
				}
				catch(MalformedURLException e)
				{
					createAndShowDialog(e, "Error");
				}
				
				
			}
			else if(formMode == 2)
			{
				Log.d("EventForm", "Masuk formMode 2 Di Thread");
				mEventTable.update(newEvent, new TableOperationCallback<Event>() {
					
					@Override
					public void onCompleted(Event arg0, Exception arg1,
							ServiceFilterResponse arg2) {
						if(arg1 == null)
						{
							if(progs != null)
							{
								progs.dismiss();
							}
							stopThread(eventFormThread);
						}
						else
						{
							if(progs != null)
							{
								progs.dismiss();
							}
							createAndShowDialog(arg1, "Insert Error");
							stopThread(eventFormThread);
						}
					}
				});
			}
		}
	};
	
	
	@SuppressLint("HandlerLeak")
	private Handler invitationFormHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			invitationHandlerFlag = true;
			@SuppressWarnings("unchecked")
			List<Invitation> receivedList = (List<Invitation>) msg.obj;
			Invitation newInvitation;
			mInvitationTable = mClient.getTable(Invitation.class);
			//	operation = 1 --> Saving Event
			
				for(int x = 0; x < receivedList.size(); x++)
				{
					newInvitation = receivedList.get(x);
					newInvitation.set_event_id(eventID);
					mInvitationTable.insert(newInvitation, new TableOperationCallback<Invitation>() {

						@Override
						public void onCompleted(Invitation arg0,
								Exception arg1, ServiceFilterResponse arg2) {
							if(arg1 != null)
							{
								createAndShowDialog(arg1,"Error");
								errorFlag = true;
							}
							
						}
						
						
					});
					if(errorFlag == true)
					{
						break;
					}
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(EventAdd.this);
				builder.setMessage("Event saved successfully");
				builder.setTitle("Success");
				builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						EventAdd.this.finish();
						
					}
				});
				builder.create().show();
				stopThread(invitationFormThread);
			
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
