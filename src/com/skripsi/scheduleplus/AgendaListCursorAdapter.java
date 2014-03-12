package com.skripsi.scheduleplus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.Agenda;

public class AgendaListCursorAdapter extends CursorAdapter {
	
	private LayoutInflater theInflater;
	
	private static final String DATE_FORMAT = "yyyyMMdd";
	private static final String DATE_FORMAT_VIEWING = "MMM d, yyyy";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.getDefault());
	private static final SimpleDateFormat dateFormatViewing = new SimpleDateFormat(DATE_FORMAT_VIEWING,Locale.getDefault());
	private Date theDate;

	public AgendaListCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		theInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		TextView toID = (TextView) arg0.findViewById(R.id.agendaID);
		TextView toDate = (TextView) arg0.findViewById(R.id.agendaDate);
		TextView toTitle = (TextView) arg0.findViewById(R.id.agendaTitle);
		
		ImageView haveAttachment = (ImageView) arg0.findViewById(R.id.agendaAttachImageView);
		
		long temp_date;
		
		if(arg2 != null)
		{
				toID.setText(String.valueOf(arg2.getInt(arg2.getColumnIndex(Agenda._ID))));
				toTitle.setText(arg2.getString(arg2.getColumnIndex(Agenda.TITLE)));
				Log.d("LoadedTitle", arg2.getString(arg2.getColumnIndex(Agenda.TITLE)));
				
				temp_date = arg2.getLong(arg2.getColumnIndex(Agenda.TIME));
				if(temp_date != 0)
				{
					try {
						theDate = dateFormat.parse(String.valueOf(temp_date));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					toDate.setText(dateFormatViewing.format(theDate));
					
				}
				else
				{
					Toast.makeText(arg1, "Error retrieving date", Toast.LENGTH_LONG).show();
					Log.e("RetrieveDate", "Something Wrong when parsing date from database");
				}
				
				if(arg2.getInt(arg2.getColumnIndex(Agenda.HAVE_ATTACHMENT)) == 1)
				{
					haveAttachment.setVisibility(android.view.View.VISIBLE);
				}
				else
				{
					haveAttachment.setVisibility(android.view.View.INVISIBLE);
				}
		}
		
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		
		return theInflater.inflate(R.layout.list_layout, arg2, false);
	}
}
