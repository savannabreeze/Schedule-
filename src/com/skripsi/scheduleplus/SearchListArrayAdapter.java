package com.skripsi.scheduleplus;

import java.net.MalformedURLException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;

public class SearchListArrayAdapter extends ArrayAdapter<UsersExtended> 
{
	private Context mContext;
	private int mResource;
	private List<UsersExtended> mObjects;
	private TextView idTextView;
	private TextView nameTextView;
	private TextView contactStatusTextView;
	private Button addButton;
	protected Runnable addFriendRun;
	protected Thread addFriendThread;
	protected Bundle addFriendBundle;
	protected Message addFriendMessage;
	protected MobileServiceClient mClient;
	protected MobileServiceTable<FriendList> mTable;
	protected SharedPreferences mSharedPref;
	
	// Constructor
	public SearchListArrayAdapter(Context context, int resource, List<UsersExtended> objects) {
		super(context, resource, objects);
		this.mContext = context;
		this.mResource = resource;
		this.mObjects = objects;
		this.addFriendMessage = new Message();
		this.addFriendBundle = new Bundle();
		
		//	Inisialisasi Runnable Add Friend
		addFriendRun = new Runnable()
		{

			@Override
			public void run() {
				//	Start addFriendHandler dengan mengirim Message yang berisi data yang akan di insert.
				addFriendHandler.sendMessage(addFriendMessage);
				
			}
			
		};
		
		//	Inisialisasi Thread
		addFriendThread = new Thread(null, addFriendRun, "addFriendThread");
		
		mSharedPref = mContext.getSharedPreferences("SCHEDULEPLUS_PREF", Context.MODE_PRIVATE);
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View v = convertView;
		if(v == null)
		{
			LayoutInflater mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = mInflater.inflate(this.mResource, parent, false);
		}
		
		if(mObjects != null)
		{
			final UsersExtended usersItem = mObjects.get(position);
			idTextView = (TextView) v.findViewById(R.id.userIdTextView);
			nameTextView = (TextView) v.findViewById(R.id.userNameTextView);
			contactStatusTextView = (TextView) v.findViewById(R.id.contactStatusTextView);
			addButton = (Button) v.findViewById(R.id.addContactButton);
			
			String fullName = usersItem.get_first_name() + " " + usersItem.get_last_name();
			

				idTextView.setText(usersItem.get_user_id());
				nameTextView.setText(fullName);
				Log.d("SearchAdapter", "status: "+usersItem.get_status());
				
				/* if true: Friend Request Sudah Dikirim
				 * else if true: Sudah menjadi friend
				 * else true: Belum menjadi friend dan belum mengirim Friend Request
				 */
				if(usersItem.get_status() == 1)
				{
					addButton.setVisibility(View.GONE);
					contactStatusTextView.setVisibility(View.VISIBLE);
					contactStatusTextView.setText("Added");
				}
				else if (usersItem.get_status() == 2)
				{
					addButton.setVisibility(View.GONE);
					contactStatusTextView.setVisibility(View.GONE);
				}
				else
				{
					addButton.setVisibility(View.VISIBLE);
					addButton.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v) {
							addFriendBundle.putString("friendRequestSender", mSharedPref.getString("LOGGED_IN_ID", ""));
							addFriendBundle.putString("friendRequestReceiver", usersItem.get_user_id());
							addFriendBundle.putInt("friendRequestStatus", 1);
							
							addFriendMessage.setData(addFriendBundle);
							
							addFriendThread.start();
							
						}
						
					});
				}
		}
		return v;
		
	}
	
	@SuppressLint("HandlerLeak")
	protected Handler addFriendHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			try
			{
				mClient = new MobileServiceClient(
						"https://scheduleplustest.azure-mobile.net/",
						"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
						mContext);
				mTable = mClient.getTable(FriendList.class);
				Bundle receivedBundle = msg.getData();
				FriendList newFriend = new FriendList();
				newFriend.set_user_id(receivedBundle.getString("friendRequestSender"));
				newFriend.set_friend_id(receivedBundle.getString("friendRequestReceiver"));
				newFriend.set_acc_status(receivedBundle.getInt("friendRequestStatus"));
				mTable.insert(newFriend, new TableOperationCallback<FriendList>()
						{

							@Override
							public void onCompleted(FriendList arg0,
									Exception arg1, ServiceFilterResponse arg2) {
								if(arg1 == null)
								{
									createAndShowDialog("Friend Request Sent", "Add Friend");
									addButton.setVisibility(View.GONE);
									contactStatusTextView.setVisibility(View.VISIBLE);
									contactStatusTextView.setText("Added");
									stopThread(addFriendThread);
								}
								else
								{
									createAndShowDialog(arg1, "Error");
									stopThread(addFriendThread);
								}
								
							}
					
						});
			}
			catch(MalformedURLException e)
			{
				createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
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
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
