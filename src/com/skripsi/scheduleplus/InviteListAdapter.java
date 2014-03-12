package com.skripsi.scheduleplus;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class InviteListAdapter extends ArrayAdapter<InvitationExtended>
{
	private Context mContext;
	private int mResource;
	private List<InvitationExtended> mObjects;
	
	public InviteListAdapter(Context context, int resource,
			List<InvitationExtended> objects) {
		super(context, resource, objects);
		this.mContext = context;
		this.mResource = resource;
		this.mObjects = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if(convertView == null)
		{
			LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(mResource, parent, false);
		}
		
		if(mObjects != null)
		{
			if(mObjects.size() != 0)
			{
				String fName = mObjects.get(position).get_first_name();
				String lName = mObjects.get(position).get_last_name();
				String fullName = fName+" "+lName;
				String id = mObjects.get(position).get_invitation_id();
				
				TextView theTextView = (TextView) convertView.findViewById(android.R.id.text1);
				theTextView.setTextSize(14);
				theTextView.setText(fullName);
				
				if(id != null)
				{
					TextView theId = (TextView) convertView.findViewById(android.R.id.text2);
					theId.setText(id);
					theId.setVisibility(View.GONE);
				}
				
			}
		}
		return convertView;
	}

}
