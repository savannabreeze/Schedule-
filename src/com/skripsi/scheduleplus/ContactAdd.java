package com.skripsi.scheduleplus;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;

public class ContactAdd extends ListActivity 
{
	SearchView contactSearchView;
	private SearchListArrayAdapter mAdapter;
	MobileServiceClient mClient;
	MobileServiceTable<Users> mTable;
	MobileServiceTable<FriendList> mTableFriendlist;
	private SharedPreferences mSharedPref;
	private String loggedInId;
	//private ArrayList<UsersExtended> usersItems;
	private Runnable azureFetcher;
	private Thread fetchThread;
	private Bundle queryData;
	private Message threadMessage;
	private List<UsersExtended> fetchResultUserExtended;
	private int x;
	private int y;
	private boolean fetcherThreadFlag = false;
	private ProgressDialog progs;
	private int iterationNumber = 0;
	private int completeNumber = 0;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.contact_add_layout);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//Mengambil id user yang sedang log in dari shared preferences
		mSharedPref = getSharedPreferences("SCHEDULEPLUS_PREF", MODE_PRIVATE);
		loggedInId = mSharedPref.getString("LOGGED_IN_ID", null);
		Log.d("ContactAdd", loggedInId);
		
		//Inisialisasi Azure Mobile Service
		try
		{
			mClient = new MobileServiceClient(
					"https://scheduleplustest.azure-mobile.net/",
					"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
					this);
			mTable = mClient.getTable(Users.class);
			mTableFriendlist = mClient.getTable(FriendList.class);
		}
		catch(MalformedURLException e)
		{
			createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
			this.finish();
		}
		
		/*	Inisialisasi SearchListArrayAdapter
		 * 	
		 * 	param
		 * 	@context
		 * 	@resource
		 * 	@ArrayList
		 */
		//mAdapter = new SearchListArrayAdapter(ContactAdd.this, R.layout.search_list_item, fetchResult);
		//setListAdapter(mAdapter);
		
		/*	Inisialisasi Runnable Thread
		 * 	
		 * 	Runnable ini nantinya akan dipanggil setiap kali user melakukan pencarian
		 */
		azureFetcher = new Runnable()
		{

			@Override
			public void run() {
				fetcher.sendMessage(threadMessage);
			}
			
		};
		
		/*	Inisialisasi Thread
		 * 
		 * 	Thread ini berisi perintah yang sudah didefinisikan 
		 */
		//fetchThread = new Thread(null, azureFetcher, "UserSearchFetcher"+searchCount);
		
		// 	Inisialisasi Bundle
		queryData = new Bundle();
		
		//	Inisialisasi Message
		
		fetchThread = new Thread(null, azureFetcher, "UserSearchFetcher");
		
		//	Inisialisasi Search Box
		contactSearchView = (SearchView) findViewById(R.id.contactSearchView);
		
		contactSearchView.setOnQueryTextListener(new OnQueryTextListener()
		{

			@Override
			public boolean onQueryTextSubmit(String query) {
				
				Log.d("SearchQuery", query);
				queryData.putString("UserQuery", query);
				threadMessage = new Message();
				threadMessage.setData(queryData);
				if(progs == null)
				{
					progs = ProgressDialog.show(ContactAdd.this, "Loading", "Loading Data, Please Wait");
				}
				else
				{
					progs.show();
				}
				if(!fetcherThreadFlag)
				{
					fetchThread.start();
				}
				else
				{
					fetchThread.run();
				}
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
			
		});
		
		fetchResultUserExtended = new ArrayList<UsersExtended>();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		if(progs != null)
		{
			progs.dismiss();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case android.R.id.home:
			this.finish();
			break;
		}
		return true;
	}
	
	@SuppressLint("HandlerLeak")
	private Handler fetcher = new Handler()
	{
		public void handleMessage(Message msg)
		{
			fetchResultUserExtended.clear();
			fetcherThreadFlag = true;
			String receivedMsg = msg.getData().getString("UserQuery");
			String first, second;
			if(receivedMsg.contains(" "))
			{
				first = receivedMsg.substring(0, receivedMsg.indexOf(" "));
				second = receivedMsg.substring(receivedMsg.indexOf(" ") + 1);
			}
			else
			{
				first = second = receivedMsg;
			}
			Log.d("ContactAdd", first);
			mTable.where().field("first_name").eq(first).or().field("last_name").eq(second).or().field("email")
			.eq(first).execute(new TableQueryCallback<Users>()
					{

						@Override
						public void onCompleted(List<Users> arg0,
								int arg1, Exception arg2,
								ServiceFilterResponse arg3) 
						{
							if(arg0 != null)
							{	
								if(arg0.size() > 0)
								{
									extraFetch(arg0);
								}
								else
								{
									if(progs != null)
									{
										progs.hide();
									}
									Log.d("ContactAdd", "first query not found");
									createAndShowDialog("User not found", "Not Found");
								}
							}
							else if(arg2 != null)
							{
								if(progs != null)
								{
									progs.hide();
								}
								createAndShowDialog(arg2 ,"Error");
							}
						}
				
					});
		}
	};
	
	private void setAdapter()
	{
		mAdapter = new SearchListArrayAdapter(ContactAdd.this, R.layout.search_list_item, fetchResultUserExtended);
		setListAdapter(mAdapter);
	}
	
	private void onCompleteResponse()
	{
		completeNumber++;
		Log.d("Loop", "IterationNumber: "+iterationNumber);
		Log.d("Loop", "CompleteNumber: "+completeNumber);
		if(iterationNumber == completeNumber)
		{
			if(progs != null)
			{
				progs.hide();
			}
			Log.d("extraFetchStats", "Adapter Set");
			setAdapter();
			
		}
	}
	
	private void extraFetch(List<Users> arg)
	{
		
		try
		{
			mClient = new MobileServiceClient(
					"https://scheduleplustest.azure-mobile.net/",
					"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
					this);
			mTableFriendlist = mClient.getTable(FriendList.class);
			y = 0;
			iterationNumber = arg.size();
			completeNumber = 0;
			for(x = 0; x < arg.size(); x++)
			{
				Log.d("ContactAdd", "Iterasi ke-"+x);
				UsersExtended temp = new UsersExtended(arg.get(x).get_user_id(), arg.get(x).get_email() 
						,arg.get(x).get_password(), arg.get(x).get_first_name(), arg.get(x).get_last_name(), -1);
				fetchResultUserExtended.add(temp);
				mTableFriendlist.where().field("user_id").eq(loggedInId).and().field("friend_user_id")
				.eq(fetchResultUserExtended.get(x).get_user_id()).execute(new TableQueryCallback<FriendList>()
						{

							@Override
							public void onCompleted(
									List<FriendList> arg0,
									int arg1,
									Exception arg2,
									ServiceFilterResponse arg3) 
							{
								if(arg0 != null)
								{
									Log.d("extraFetchStats", "Ukuran arg0 "+arg0.size());
									if(arg0.size() > 0)
									{
										
										fetchResultUserExtended.get(y).set_status(arg0.get(0).get_acc_status());
										Log.d("extraFetchStats", "status friendlist"+arg0.get(0).get_acc_status());
										y++;
										
									}
									else
									{
										//mAdapter = new SearchListArrayAdapter(ContactAdd.this, R.layout.search_list_item, fetchResultUserExtended);
										//setListAdapter(mAdapter);
									}
								}
								else if(arg2 != null)
								{
									createAndShowDialog(arg2, "Error");
								}
								else
								{
									createAndShowDialog("Error occurred", "Error");
								}
								onCompleteResponse();
							}
					
						});
			}
		}
		catch(MalformedURLException e)
		{
			
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
		builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which) {
				stopThread(fetchThread);
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
