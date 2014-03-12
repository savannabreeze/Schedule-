package com.skripsi.scheduleplus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UserAccount extends Activity 
{
	TextView signedInEmailTextView;
	EditText displayNameEditText;
	Button logOutButton;
	AlertDialog.Builder logOutAlert;
	
	private SharedPreferences mSharedPref;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_account_activity);
		
		mSharedPref = getSharedPreferences("SCHEDULEPLUS_PREF", MODE_PRIVATE);
		
		signedInEmailTextView = (TextView)findViewById(R.id.signedInEmailTextView);
		displayNameEditText = (EditText)findViewById(R.id.displayNameEditText);
		logOutButton = (Button)findViewById(R.id.logOutButton);
		
		signedInEmailTextView.setText(mSharedPref.getString("LOGGED_IN_USER", null));
		displayNameEditText.setText(mSharedPref.getString("LOGGED_IN_FIRST_NAME", null));
		logOutButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				logOutAlert = new AlertDialog.Builder(UserAccount.this);
				logOutAlert.setTitle("Confirm Log Out");
				logOutAlert.setMessage("Are you sure you want to Log Out?");
				logOutAlert.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Editor sharedPreferencesEditor = mSharedPref.edit();
						sharedPreferencesEditor.clear();
						sharedPreferencesEditor.commit();
						createAndShowDialog("You have been logged out", "Log Out");
					}
				});
				logOutAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				});
				logOutAlert.create().show();
				
			}
		});
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
	
	private void createAndShowDialog(String message, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				UserAccount.this.finish();				
			}
		});
		builder.create().show();
	}
}
