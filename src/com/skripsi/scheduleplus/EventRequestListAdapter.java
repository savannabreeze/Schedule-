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

public class EventRequestListAdapter extends BaseExpandableListAdapter
{
	Context mContext;
	List<String> mHeader;
	HashMap<String, List<InvitationExtended>> mChild;
	
	private Runnable requestRun;
	private Thread requestThread;
	private Bundle requestBundle;
	private Message requestMessage;
	
	private MobileServiceClient mClient;
	private MobileServiceTable<Invitation> mTable;
	
	public EventRequestListAdapter(Context context, List<String> header, HashMap<String, List<InvitationExtended>> child)
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
		
		this.requestThread = new Thread(null, requestRun, "EventRequestThread");
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
		if(convertView == null)
		{
			LayoutInflater anInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = anInflater.inflate(R.layout.request_list_header, null);
		}
		
		TextView headerTitleTextView = (TextView) convertView.findViewById(R.id.requestListHeader);
		headerTitleTextView.setTypeface(null, Typeface.BOLD);
		headerTitleTextView.setTextColor(mContext.getResources().getColor(R.color.gray));
		headerTitleTextView.setText(mHeader.get(0).toString());
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final InvitationExtended childText = (InvitationExtended) getChild(groupPosition, childPosition);
		
		if(convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.request_list_item, null);
		}
		
		TextView reqListTextView = (TextView) convertView.findViewById(R.id.reqListTextView);
		Button acceptButton = (Button) convertView.findViewById(R.id.acceptButton);
		Button declineButton = (Button) convertView.findViewById(R.id.declineButton);
		
		reqListTextView.setText(childText.get_title());
		
		acceptButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) {
				
					requestBundle.putString("InviteID", childText.get_invitation_id());
					requestBundle.putString("ID", childText.get_event_id());
					requestBundle.putString("UID", childText.get_invited_id());
					requestBundle.putInt("Status", 2);
					requestBundle.putInt("ButtonFlag", 1);
					
					requestMessage.setData(requestBundle);
					
					requestThread.start();
				
				
			}
			
		});
		
		declineButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) {
				
				requestBundle.putString("ID", childText.get_event_id());
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
	private Handler friendRequestHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Bundle threadBundle = msg.getData();
			
			String invitationID = threadBundle.getString("InviteID");
			String ID = threadBundle.getString("ID");
			int ButtonFlag = threadBundle.getInt("ButtonFlag");
			Log.d("EventReqListAdapter", invitationID);
			Log.d("EventReqListAdapter", ID);
			try
			{
				mClient = new MobileServiceClient(
						"https://scheduleplustest.azure-mobile.net/",
						"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
						mContext);
			
				mTable = mClient.getTable(Invitation.class);
				Invitation invitationUpdate = new Invitation();
				
				if(ButtonFlag == 1)
				{
					int status = threadBundle.getInt("Status");
					String UID = threadBundle.getString("UID");
					
					invitationUpdate.set_invitation_id(invitationID);
					invitationUpdate.set_event_id(ID);
					invitationUpdate.set_invited_id(UID);
					invitationUpdate.set_status(status);
					mTable.update(invitationUpdate, new TableOperationCallback<Invitation>()
							{
	
								@Override
								public void onCompleted(Invitation arg0,
										Exception arg1, ServiceFilterResponse arg2) {
									if(arg1 == null)
									{
										createAndShowDialog("Event Request Accepted", "Invitation");
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
					mTable.delete(invitationUpdate, new TableDeleteCallback() {
						
						@Override
						public void onCompleted(Exception arg0, ServiceFilterResponse arg1) {
							if(arg0 != null)
							{
								createAndShowDialog(arg0, "Error");
								stopThread(requestThread);
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
