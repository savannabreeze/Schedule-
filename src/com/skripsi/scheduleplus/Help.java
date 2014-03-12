package com.skripsi.scheduleplus;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class Help extends Activity
{	TextView helpTextView;
	String text = "How to use the Agenda feature?\n"+
			"Click on the Agenda tab.\n"+
			"To add a new agenda, click add button.\n"+
			"To view existing agenda, simply click on the selected date.\n"+
			"To delete or edit existing agenda, click on the delete or edit button during viewing the agenda.\n\n"+
			"How to use the SMS Scheduler feature?\n"+
			"Click on the SMS Scheduler tab.\n"+
			"To add a scheduled SMS, click add button.\n"+
			"To view existing Scheduled SMS, simply click on the selected Scheduled SMS.\n"+
			"To delete or edit Scheduled SMS, click on the delete or edit button during viewing the Scheduled SMS.\n\n"+
			"How to use the Reminders feature?\n"+
			"Click on the Reminders tab.\n"+
			"To add a reminder, click add button.\n"+
			"To view existing reminder, simply click on the selected reminder.\n"+
			"To delete or edit reminder, click on the delete or edit button during viewing the reminder.\n\n"+

			"How to use the Event Invitation feature?\n"+
			"Click on the Event Invitation tab.\n"+
			"To add an event, click add button.\n"+
			"To view existing event, simply click on the selected event.\n"+
			"If you are the creator if event you can delete or edit the event, simply click on the delete or edit button during viewing the event.\n\n"+
			"Why I cannot access the event-invitation feature?\n"+
			"To use this feature, you need to log in first.\n\n"+
			"How to login?\n"+
			"Go to Setting -> Log in. Fill in your registered email and password in the respective column and click Log in.\n\n"+
			"How about if I don't have account?\n"+
			"You need to register first. Go to Setting -> Log in -> Sign up. Fill in all the required data and then click Sign up Button.\n\n"+
			"How about if I forgot my password?\n"+
			"Click the forgot password link to retrieve your password back. An email will be sent to your registered email.";
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_activity);
		
		helpTextView = (TextView) findViewById(R.id.helpTextView);
		helpTextView.setText(text);
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
