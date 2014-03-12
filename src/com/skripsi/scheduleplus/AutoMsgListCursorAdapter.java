package com.skripsi.scheduleplus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.SMS;

public class AutoMsgListCursorAdapter extends CursorAdapter {
	
	protected LayoutInflater theInflater;
	
	private long temp_date_long;
	private Date temp_date;
	
	//Date Formatting 
	private static final String DATE_FORMAT_VIEW = "MMMM dd, yyyy HH:mm";
	private static final String DATE_FORMAT_DB = "yyyyMMddHHmm";
	private static final SimpleDateFormat dateFormatView = new SimpleDateFormat(DATE_FORMAT_VIEW,Locale.getDefault());
	private static final SimpleDateFormat dateFormatDb = new SimpleDateFormat(DATE_FORMAT_DB,Locale.getDefault());
	
	public AutoMsgListCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		theInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView toID = (TextView) view.findViewById(R.id.agendaID);
		TextView toDate = (TextView) view.findViewById(R.id.agendaDate);
		TextView toTitle = (TextView) view.findViewById(R.id.agendaTitle);
		
		
		if(cursor != null)
		{
			toID.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(SMS._ID))));
			toTitle.setText(cursor.getString(cursor.getColumnIndex(SMS.PHONE)));			
			temp_date_long = cursor.getLong(cursor.getColumnIndex(SMS.TIME));
			try {
				temp_date = dateFormatDb.parse(String.valueOf(temp_date_long));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			toDate.setText(dateFormatView.format(temp_date));
		}
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		
		return theInflater.inflate(R.layout.list_layout, parent, false);
	}

}
