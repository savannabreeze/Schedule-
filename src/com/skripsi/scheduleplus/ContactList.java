package com.skripsi.scheduleplus;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableDeleteCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;

public class ContactList extends Activity
{
	//referensi ke MobileServiceClient
	private MobileServiceClient theMobileClient;
	
	//tabel contact list
	private MobileServiceTable<FriendListExtended> mContactListTable;
	private MobileServiceTable<FriendList> mFriendList;
	
	private ListView contactsListView;
	
	ContactListAdapter mContactListAdapter;
	
	ProgressDialog theProgressBar;
	ProgressDialog progs2;
	
	SharedPreferences schedulePlusSP;
		
	TextView listTitleTextView;
	
	boolean forResult = false;
	
	private List<FriendList> theFriendList;
	
	public static final String SENDER_ID = "905841047833";
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_list_layout);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//	Mengambil Intent Extra
		Intent receivedIntent = getIntent();
		forResult = receivedIntent.getBooleanExtra("forResult", false);
		
		theFriendList = new ArrayList<FriendList>();
		
		contactsListView = (ListView) findViewById(R.id.contactListView);
		
		listTitleTextView = (TextView) findViewById(R.id.listTitleTextView);
		listTitleTextView.setText("Contact List");
		/* 
		 * Load session user dari shared preferences
		 */
		schedulePlusSP = getSharedPreferences("SCHEDULEPLUS_PREF",MODE_PRIVATE);
		boolean LoginStatus = schedulePlusSP.getBoolean("LOGGED_IN", false);
		/*
		 * Menampilkan warning jika page contact list dibuka saat user belum log in
		 */
		if(LoginStatus == false)
		{
			this.finish();
		}
		/*
		 * Mengambil userID dan username user yang sudah login
		 */
		else
		{
			try {
				// Create the Mobile Service Client instance, using the provided
				// Mobile Service URL and key
				theMobileClient = new MobileServiceClient(
						"https://scheduleplustest.azure-mobile.net/",
						"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
						this);

				// Get the Mobile Service Table instance to use
				mContactListTable = theMobileClient.getTable(FriendListExtended.class);
				theProgressBar = ProgressDialog.show(this, "Loading Data, Please Wait", "Loading");
				mContactListTable.parameter("userId", schedulePlusSP.getString("LOGGED_IN_ID", null)).parameter("status", "2")
						.parameter("type", "1").execute(new TableQueryCallback<FriendListExtended>() {

							@Override
							public void onCompleted(List<FriendListExtended> arg0, int arg1,
									Exception arg2, ServiceFilterResponse arg3) {
								if (arg2 == null) 
								{
									mContactListAdapter = new ContactListAdapter(ContactList.this, R.layout.contact_list_item, arg0);
									contactsListView.setAdapter(mContactListAdapter);
									
									for(int x = 0; x < arg0.size(); x++)
									{
										FriendList toBeAdded = new FriendList(arg0.get(x).get_id(), arg0.get(x).get_user_id(), 
												arg0.get(x).get_friend_user_id(), arg0.get(x).get_friend_acc());
										theFriendList.add(toBeAdded);
									}
									
									contactsListView.setOnItemClickListener(new OnItemClickListener()
									{

										@Override
										public void onItemClick(AdapterView<?> parent, View view,
												int position, long id) {
											if(forResult == true)
											{
												View itemView = parent.getChildAt(position);
												TextView idTextView = (TextView)itemView.findViewById(R.id.friendIdTextView);
												TextView nameTextView = (TextView)itemView.findViewById(R.id.friendNameTextView);
												String contactId = idTextView.getText().toString();
												String contactName = nameTextView.getText().toString();
												if(contactId != null)
												{
													Intent returnIntent = new Intent();
													returnIntent.putExtra("selectedId", contactId);
													returnIntent.putExtra("selectedName", contactName);
													setResult(RESULT_OK, returnIntent);
													finish();
												}
											}
											
										}
										
									});
									
									contactsListView.setOnItemLongClickListener(new OnItemLongClickListener() {

										@Override
										public boolean onItemLongClick(
												AdapterView<?> parent,
												View view, final int position, long id) 
										{
											if(forResult == false)
											{
												AlertDialog.Builder contactDeleteAlert = new AlertDialog.Builder(ContactList.this);
												contactDeleteAlert.setTitle("Delete Contact");
												contactDeleteAlert.setMessage("Are you sure you want to remove him/her from your contact list?");
												contactDeleteAlert.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener()
												{
	
													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														try {
															// Create the Mobile Service Client instance, using the provided
															// Mobile Service URL and key
															theMobileClient = new MobileServiceClient(
																	"https://scheduleplustest.azure-mobile.net/",
																	"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
																	ContactList.this);
															mFriendList = theMobileClient.getTable(FriendList.class);
															if(progs2 == null)
															{
																progs2 = ProgressDialog.show(ContactList.this, "Deleting, Please Wait", "Delete");
															}
															else
															{
																progs2.show();
															}
															mFriendList.delete(theFriendList.get(position), new TableDeleteCallback() {
															
																@Override
																public void onCompleted(Exception arg0, ServiceFilterResponse arg1) {
																	if(arg0 != null)
																	{
																		if(progs2 != null)
																		{
																			progs2.hide();
																		}
																		createAndShowDialog(arg0, "Error");
																	}
																	else
																	{
																		if(progs2 != null)
																		{
																			progs2.hide();
																		}
																		createAndShowDialog("Contact deleted successfully", "Success");
																		mContactListAdapter.notifyDataSetChanged();
																	}
																}
															});
															
														}
														catch(MalformedURLException e)
														{
															
														}
														
													}
													
												});
												contactDeleteAlert.setNegativeButton("No", new OnClickListener() {
													
													@Override
													public void onClick(DialogInterface dialog, int which) {
														
													}
												});
												contactDeleteAlert.create().show();
											}
											return false;
										}
									});
									if(theProgressBar != null)
									{
										theProgressBar.dismiss();
									}
								} 
								else 
								{
									if(theProgressBar != null)
									{
										theProgressBar.dismiss();
									}
									createAndShowDialog(arg2, "Error");
								}
								
							}
						});
				
				
			
				// Load the items from the Mobile Service
				//refreshItemsFromTable();

			} 
			catch (MalformedURLException e) 
			{
				createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
			}
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		if(forResult == false)
		{
			MenuInflater mInflater = new MenuInflater(this);
			mInflater.inflate(R.menu.contact_search_menu, menu);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.contactSearch:
			Intent contactAddIntent = new Intent(this, ContactAdd.class);
			startActivity(contactAddIntent);
			break;
		case android.R.id.home:
			this.finish();
			break;
		}
		return true;
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
		builder.create().show();
	}
}
