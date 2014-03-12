package com.skripsi.scheduleplus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.ReminderLocation;;

public class ReminderFormFragment extends Activity implements LoaderCallbacks<Cursor>
{
	EditText txtReminderTitle;
	EditText txtReminderDescription;
	EditText reminderLocationEditText;
	
	private double latitude;
	private double longitude;
	private int status = 0;
	
	private Intent receivedIntent;
	
	private long reminderID;
	
	ContentValues newValue;
	
	private static int REMINDER_ADD_LOADER = 200;
	private Uri addResult;
	private int updateResult;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_locationreminder_add);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		receivedIntent = getIntent();
		reminderID = receivedIntent.getLongExtra("reminderID", -1);
		
		txtReminderTitle = (EditText) findViewById(R.id.txtReminderTitle);
		txtReminderDescription = (EditText) findViewById(R.id.txtReminderDescription);
		reminderLocationEditText = (EditText) findViewById(R.id.reminderLocationEditText);
		
		if(reminderID != -1)
		{
			getLoaderManager().initLoader(REMINDER_ADD_LOADER, null, this);
		}
		
		reminderLocationEditText.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View v) {
				Intent getLocIntent = new Intent(ReminderFormFragment.this, LocationSearchActivity.class);
				startActivityForResult(getLocIntent, 1);
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.reminder_form_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.saveLocationReminder:
			if(txtReminderTitle.getText().toString().equals("") &&
			txtReminderDescription.getText().toString().equals("") &&
			reminderLocationEditText.getText().toString().equals(""))
			{
				createAndShowDialog("You must fill all of the field", "Error");
			}
			else if(txtReminderTitle.getText().toString().equals(""))
			{
				createAndShowDialog("You must enter a title", "Error");
			}
			else if(txtReminderDescription.getText().toString().equals(""))
			{
				createAndShowDialog("You must enter a description", "Error");
			}
			else if(reminderLocationEditText.getText().toString().equals(""))
			{
				createAndShowDialog("You must enter a location", "Error");
			}
			else
			{
				String title = txtReminderTitle.getText().toString();
				String desc =  txtReminderDescription.getText().toString();
				String loc = reminderLocationEditText.getText().toString();
				newValue = new ContentValues();
				newValue.put(ReminderLocation.REMINDERLOCATION_TITLE, title);
				newValue.put(ReminderLocation.REMINDERLOCATION_DESCRIPTION, desc);
				newValue.put(ReminderLocation.REMINDERLOCATION_TIME, 100);
				newValue.put(ReminderLocation.PLACE_NAME, loc);
				newValue.put(ReminderLocation.LATITUDE, latitude);
				newValue.put(ReminderLocation.LONGITUDE, longitude);
				newValue.put(ReminderLocation.STATUS, status);
				
				ContentResolver reminderResolver = getContentResolver();
				if(reminderID == -1)
				{
					addResult = reminderResolver.insert(ReminderLocation.CONTENT_URI, newValue);
					if(addResult != null)
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(ReminderFormFragment.this);
						builder.setMessage("Reminder saved successfully");
						builder.setTitle("Success");
						builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ReminderFormFragment.this.finish();	
							}
						});
						builder.create().show();
					}
					else
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(ReminderFormFragment.this);
						builder.setMessage("Fail to add reminder");
						builder.setTitle("Error");
						builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});
						builder.create().show();
					}
				}
				else
				{
					Uri updateUri = ContentUris.withAppendedId(ReminderLocation.CONTENT_URI, reminderID);
					updateResult = reminderResolver.update(updateUri, newValue, null, null);
					if(updateResult == 0)
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(ReminderFormFragment.this);
						builder.setMessage("Fail to save reminder");
						builder.setTitle("Error");
						builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});
						builder.create().show();
					}
					else
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(ReminderFormFragment.this);
						builder.setMessage("Reminder saved successfully");
						builder.setTitle("Success");
						builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ReminderFormFragment.this.finish();	
							}
						});
						builder.create().show();
					}
				}
			}
			break;
		case android.R.id.home:
			AlertDialog.Builder builder = new AlertDialog.Builder(ReminderFormFragment.this);
			builder.setMessage("You have unsaved changes. Exit anyway?");
			builder.setTitle("Warning");
			builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ReminderFormFragment.this.finish();	
				}
			});
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			});
			builder.create().show();
			break;
		}
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
	        AlertDialog.Builder builder = new AlertDialog.Builder(ReminderFormFragment.this);
			builder.setMessage("You have unsaved changes. Exit anyway?");
			builder.setTitle("Warning");
			builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ReminderFormFragment.this.finish();	
				}
			});
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			});
			builder.create().show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
    }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(id == REMINDER_ADD_LOADER)
		{
			Uri loaderUri = ContentUris.withAppendedId(ReminderLocation.CONTENT_URI, reminderID);
			return new CursorLoader(
					this,
					loaderUri,
					ReminderLocation.REMINDERLOCATION_PROJECTION,
					null,
					null,
					null);
		}
		else
		{
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(loader.getId() == REMINDER_ADD_LOADER)
		{
			if(data != null && data.moveToFirst() == true)
			{
				txtReminderTitle.setText(data.getString(data.getColumnIndex(ReminderLocation.REMINDERLOCATION_TITLE)));
				txtReminderDescription.setText(data.getString(data.getColumnIndex(ReminderLocation.REMINDERLOCATION_DESCRIPTION)));
				reminderLocationEditText.setText(data.getString(data.getColumnIndex(ReminderLocation.PLACE_NAME)));
				latitude = data.getDouble(data.getColumnIndex(ReminderLocation.LATITUDE));
				longitude = data.getDouble(data.getColumnIndex(ReminderLocation.LONGITUDE));
				status = data.getInt(data.getColumnIndex(ReminderLocation.STATUS));
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == 1)
		{
			if(resultCode == RESULT_OK)
			{
				latitude = data.getDoubleExtra("latitude", 0);
				longitude = data.getDoubleExtra("longitude", 0);
				reminderLocationEditText.setText(data.getStringExtra("name"));
				Log.d("ReminderForm", String.valueOf(latitude));
				Log.d("ReminderForm", String.valueOf(longitude));
				Log.d("ReminderForm", data.getStringExtra("name"));
			}
		}
	}
	
	private void createAndShowDialog(final String message, final String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		builder.create().show();
	}
}
