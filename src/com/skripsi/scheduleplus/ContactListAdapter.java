package com.skripsi.scheduleplus;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ContactListAdapter extends ArrayAdapter<FriendListExtended>
{
	Context theContext;
	int thelayoutResourceId;
	private List<FriendListExtended> mList;
	
	String fullname;
	
	public ContactListAdapter(Context context, int layoutResourceId, List<FriendListExtended> theList)
	{
		super(context,layoutResourceId, theList);
		
		theContext = context;
		thelayoutResourceId = layoutResourceId;
		this.mList = theList;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		//Layout yang digunakan untuk menyimpan data tiap baris list
		View row = convertView;
		
		FriendListExtended currItem = mList.get(position);
		if(row == null)
		{
			LayoutInflater mInflater = ((Activity)theContext).getLayoutInflater();
			row = mInflater.inflate(thelayoutResourceId, parent, false);
		}
		
		//row.setTag(currItem);
		fullname = currItem.get_first_name() + " " + currItem.get_last_name();
		TextView fullNameTextView = (TextView)row.findViewById(R.id.friendNameTextView);
		TextView friendIdTextView = (TextView)row.findViewById(R.id.friendIdTextView);
		fullNameTextView.setText(fullname);
		friendIdTextView.setText(currItem.get_friend_user_id());
		
		return row;
		
	}
	
}
