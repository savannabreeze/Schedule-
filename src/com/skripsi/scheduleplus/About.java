package com.skripsi.scheduleplus;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class About extends Activity 
{
	TextView aboutTextView;
	String text = "Schedule+ is a productivity tool that contains 4 main features which are: Agenda , SMS Scheduler, Location Based Reminder and Event Invitation.\n\n"+
	       "Version 1.0";
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity);
		
		aboutTextView = (TextView)findViewById(R.id.aboutTextView);
		aboutTextView.setText(text);
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
}
