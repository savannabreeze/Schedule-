package com.skripsi.scheduleplus;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableDeleteCallback;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;

public class FriendRequestListAdapter extends BaseExpandableListAdapter
{
	private Context mContext;
	private List<String> mHeader;
	private HashMap<String, List<FriendListExtended>> mChild;
	
	protected Runnable requestRun;
	protected Thread requestThread;
	protected Bundle requestBundle;
	protected Message requestMessage;
	
	protected MobileServiceClient mClient;
	protected MobileServiceTable<FriendList> mTable;
	
	Button acceptButton;
	Button declineButton;
	
	public FriendRequestListAdapter(Context context, List<String> header, HashMap<String, List<FriendListExtended>> child)
	{
		this.mContext = context;
		this.mHeader = header;
		this.mChild = child;
		
		this.requestBundle = new Bundle();
		this.requestMessage = new Message();
		
		this.requestRun = new Runnable()
		{

			@Override
			public void run() {
				friendRequestHandler.sendMessage(requestMessage);
				
			}
			
		};
		
		this.requestThread = new Thread(null, requestRun, "requestThread");
	}

	@Override
	public int getGroupCount() {
		return this.mHeader.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this.mChild.get(this.mHeader.get(groupPosition))
                .size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mHeader.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		
		return this.mChild.get(this.mHeader.get(groupPosition)).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String headerTitle = (String) getGroup(groupPosition);
		Log.d("header", headerTitle);
		if(convertView == null)
		{
			LayoutInflater anInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = anInflater.inflate(R.layout.request_list_header, null);
		}
		
		TextView headerTitleTextView = (TextView) convertView.findViewById(R.id.requestListHeader);
		headerTitleTextView.setTypeface(null, Typeface.BOLD);
		headerTitleTextView.setTextColor(mContext.getResources().getColor(R.color.gray));
		headerTitleTextView.setText(headerTitle);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final FriendListExtended childText = (FriendListExtended) getChild(groupPosition, childPosition);
		final int pos = childPosition;
		if(convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.request_list_item, null);
		}
		
		TextView reqListTextView = (TextView) convertView.findViewById(R.id.reqListTextView);
		acceptButton = (Button) convertView.findViewById(R.id.acceptButton);
		declineButton = (Button) convertView.findViewById(R.id.declineButton);
		
		reqListTextView.setText(childText.get_first_name() + " " + childText.get_last_name());
		
		acceptButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) {
				
					requestBundle.putString("ID", childText.get_id());
					requestBundle.putString("UID", childText.get_user_id());
					requestBundle.putString("friendUID", childText.get_friend_user_id());
					requestBundle.putInt("Status", 2);
					requestBundle.putInt("TargetTable", 0);
					requestBundle.putInt("ButtonFlag", 1);
					requestBundle.putInt("ChildPos", pos);
					
					requestMessage.setData(requestBundle);
					
					requestThread.start();
				
				
			}
			
		});
		
		declineButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) {
				
				requestBundle.putString("ID", childText.get_id());
				requestBundle.putInt("TargetTable", 0);
				requestBundle.putInt("ButtonFlag", 2);
				
				requestMessage.setData(requestBundle);
				
				requestThread.start();
				
			}
			
		});
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public int getGroupType(int groupPosition)
	{
		/*	Friend Request = 0
		 * 	Event Request = 1
		 */
		if(groupPosition == 0)
		{
			return 0;
		}
		else
		{
			return 1;
		}
	}
	
	@SuppressLint("HandlerLeak")
	protected Handler friendRequestHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Bundle threadBundle = msg.getData();
			
			String ID = threadBundle.getString("ID");
			int ButtonFlag = threadBundle.getInt("ButtonFlag");
			int pos = threadBundle.getInt("ChildPos");
			
			try
			{
				mClient = new MobileServiceClient(
						"https://scheduleplustest.azure-mobile.net/",
						"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
						mContext);
			
				mTable = mClient.getTable(FriendList.class);
				FriendList newFriend = new FriendList();
				
				if(ButtonFlag == 1)
				{
					int status = threadBundle.getInt("Status");
					String UID = threadBundle.getString("UID");
					String friendUID = threadBundle.getString("friendUID");
					
					newFriend.set_id(ID);
					newFriend.set_user_id(UID);
					newFriend.set_friend_id(friendUID);
					newFriend.set_acc_status(status);
					mTable.update(newFriend, new TableOperationCallback<FriendList>()
							{
	
								@Override
								public void onCompleted(FriendList arg0,
										Exception arg1, ServiceFilterResponse arg2) {
									if(arg1 == null)
									{
										acceptButton.setVisibility(View.GONE);
										declineButton.setVisibility(View.GONE);
										createAndShowDialog("Friend Request Accepted", "Success");
										stopThread(requestThread);
									}
									else
									{
										createAndShowDialog(arg1, "Error");
										stopThread(requestThread);
									}
									
								}
						
							});
				}
				else if(ButtonFlag == 2)
				{
					mTable.delete(newFriend, new TableDeleteCallback() {
						
						@Override
						public void onCompleted(Exception arg0, ServiceFilterResponse arg1) {
							if(arg0 != null)
							{
								createAndShowDialog(arg0, "Error");
								stopThread(requestThread);
							}
							else
							{
								acceptButton.setVisibility(View.GONE);
								declineButton.setVisibility(View.GONE);
							}
							
						}
					});
				}
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
