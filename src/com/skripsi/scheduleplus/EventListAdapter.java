package com.skripsi.scheduleplus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.math.BigInteger;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EventListAdapter extends ArrayAdapter<Event>
{
	private Context theContext;
	private int thelayoutResourceId;
	private List<Event> mList;
	
	String title;
	BigInteger time;
	Date fetchedDate;
	
	private long convertedTime;
	private static final String DATE_FORMAT_VIEW = "MMMM dd, yyyy HH:mm";
	private static final String DATE_FORMAT_DB = "yyyyMMddHHmm";
	private static final SimpleDateFormat dateFormatView = new SimpleDateFormat(DATE_FORMAT_VIEW,Locale.getDefault());
	private static final SimpleDateFormat dateFormatDb = new SimpleDateFormat(DATE_FORMAT_DB,Locale.getDefault());
	
	public EventListAdapter(Context context, int layoutResourceId, List<Event> theList)
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
		if(row == null)
		{
			LayoutInflater mInflater = ((Activity)theContext).getLayoutInflater();
			row = mInflater.inflate(thelayoutResourceId, parent, false);
		}
		
		if(mList != null)
		{
			Event currItem = mList.get(position);
			title = currItem.get_title();
			time = currItem.get_time();
			
			//	Time Format Conversion
			convertedTime = time.longValue();
			try {
				fetchedDate = dateFormatDb.parse(String.valueOf(convertedTime));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			TextView titleTextView = (TextView)row.findViewById(R.id.agendaTitle);
			TextView timeTextView = (TextView)row.findViewById(R.id.agendaDate);
			TextView idTextView = (TextView)row.findViewById(R.id.agendaID);
			
			idTextView.setText(currItem.get_event_id());
			titleTextView.setText(title);
			timeTextView.setText(dateFormatView.format(fetchedDate));
		}
		return row;
		
	}
}
