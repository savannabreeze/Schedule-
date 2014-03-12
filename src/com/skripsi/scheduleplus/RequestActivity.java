package com.skripsi.scheduleplus;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;


public class RequestActivity extends Activity
{
	ExpandableListView friendRequestExpandable;
	ExpandableListView eventRequestExpandable;
	
	private MobileServiceClient mClient;
	private MobileServiceTable<FriendListExtended> mFriendTable;
	private MobileServiceTable<InvitationExtended> mEventTable;
	
	private FriendRequestListAdapter friendRequestAdapter;
	private EventRequestListAdapter eventRequestAdapter;
	
	private List<String> friendHeader = new ArrayList<String>();
	private List<String> eventHeader = new ArrayList<String>();
	
	private HashMap<String, List<FriendListExtended>> friendRequestChild = new HashMap<String, List<FriendListExtended>>();
	private HashMap<String, List<InvitationExtended>> eventRequestChild = new HashMap<String, List<InvitationExtended>>();
	
	private SharedPreferences mSharedPref;
	
	private ProgressDialog progs;
	
	@Override 
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.requests_activity);
		
		mSharedPref = getSharedPreferences("SCHEDULEPLUS_PREF", MODE_PRIVATE);
		
		friendRequestExpandable = (ExpandableListView) findViewById(R.id.friendRequestsExpandableListView);
		eventRequestExpandable = (ExpandableListView) findViewById(R.id.eventRequestsExpandableListView);

		//Inisialisasi Azure Mobile Service
				try
				{
					mClient = new MobileServiceClient(
							"https://scheduleplustest.azure-mobile.net/",
							"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
							this);
					mFriendTable = mClient.getTable(FriendListExtended.class);
					mEventTable = mClient.getTable(InvitationExtended.class);
					if(progs == null)
					{
						progs = ProgressDialog.show(this, "Loading", "Loading Data, Please Wait");
					}
					mFriendTable.parameter("userId", mSharedPref.getString("LOGGED_IN_ID", null)).parameter("status", "1")
					.parameter("type", "2").execute(new TableQueryCallback<FriendListExtended>()
							{

								@Override
								public void onCompleted(
										List<FriendListExtended> arg0,
										int arg1, Exception arg2,
										ServiceFilterResponse arg3) {
									if(arg0 != null)
									{
										if(arg0.size() != 0)
										{
											friendHeader.add(new String("Friend Request"));
											friendRequestChild.put(friendHeader.get(0), arg0);
										
											friendRequestAdapter = new FriendRequestListAdapter(RequestActivity.this, friendHeader, friendRequestChild);
											friendRequestExpandable.setAdapter(friendRequestAdapter);
										}
										else
										{
											Toast.makeText(RequestActivity.this, "You Have No Friend Request", Toast.LENGTH_SHORT).show();
										}
									}
									else if(arg2 != null)
									{
										createAndShowDialog(arg2, "Error");
									}
									else
									{
										createAndShowDialog("Unknown Error", "Error");
									}
								}
						
							});
					mEventTable.parameter("invitedId", mSharedPref.getString("LOGGED_IN_ID", null)).parameter("eventId", "").parameter("queryType", "1").execute(new TableQueryCallback<InvitationExtended>() {
						
						@Override
						public void onCompleted(List<InvitationExtended> arg0, int arg1,
								Exception arg2, ServiceFilterResponse arg3) {
							if(arg0 != null)
							{
								if(arg0.size() != 0)
								{
									eventHeader.add(new String("Event Invitation"));
									eventRequestChild.put(eventHeader.get(0), arg0);
								
									eventRequestAdapter = new EventRequestListAdapter(RequestActivity.this, eventHeader, eventRequestChild);
									eventRequestExpandable.setAdapter(eventRequestAdapter);
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
									Toast.makeText(RequestActivity.this, "You Have No Event Request", Toast.LENGTH_SHORT).show();
								}
							}
							else if(arg2 != null)
							{
								createAndShowDialog(arg2, "Error");
							}
							else
							{
								createAndShowDialog("Unknown Error While Loading Event Invitation List", "Error");
							}
							
						}
					});
				}
				catch(MalformedURLException e)
				{
					createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
					this.finish();
				}
	}
	
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

}
