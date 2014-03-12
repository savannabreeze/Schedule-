package com.skripsi.scheduleplus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.content.CursorLoader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.ReminderLocation;

public class ReminderViewActivity extends Activity implements LoaderCallbacks<Cursor>
{
	TextView ReminderTitleView;
	TextView ReminderDescriptionView;
	TextView ReminderPlaceView;
	ToggleButton ReminderDoneToggleButton;
	
	private final static int REMINDER_VIEW_LOADER = 301;
	private long ReminderId;
	
	Intent RecievedIntent;
	CursorLoader reminderLoader = null;
	Uri uri;
	ContentResolver resolver;
	int updateCount = 0;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_reminder_view);
		
		RecievedIntent = getIntent();
		ReminderId = RecievedIntent.getLongExtra("INTENT_REMINDER_VIEW", -1);
		uri = ContentUris.withAppendedId(ReminderLocation.CONTENT_URI, ReminderId);
		
		ReminderTitleView = (TextView) findViewById(R.id.ReminderTitleView);
		ReminderDescriptionView = (TextView) findViewById(R.id.ReminderDescriptionView);
		ReminderPlaceView = (TextView) findViewById(R.id.ReminderPlaceView);
		ReminderDoneToggleButton = (ToggleButton) findViewById(R.id.reminderDoneToggleButton);
		
		resolver = getContentResolver();
		
		if(ReminderId != -1)
		{
			getLoaderManager().initLoader(REMINDER_VIEW_LOADER, null, this);
		}
		else
		{
			AlertDialog.Builder errorAlert = new AlertDialog.Builder(this);
			errorAlert.setTitle("Error");
			errorAlert.setMessage("Error loading reminder");
			errorAlert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener()
			{

				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
				
			});
			errorAlert.create().show();
		}
		
		ReminderDoneToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				
				ContentValues check = new ContentValues();
				if(isChecked)
				{
					check.put(ReminderLocation.STATUS, 1);
					updateCount = resolver.update(uri, check, null, null);
				}
				else
				{
					check.put(ReminderLocation.STATUS, 0);
					updateCount = resolver.update(uri, check, null, null);
				}
				
				if(updateCount == 0)
				{
					Log.d("ReminderViewActivity", "Error updating status");
				}
				else
				{
					Log.d("ReminderViewActivity", "Status update success");
				}
			}
			
		});
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.reminder_view_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.reminderDelete:
			AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
			confirmDialog.setTitle("Reminder Delete Confirmation");
			confirmDialog.setMessage("Are you sure you want to delete this reminder?");
			confirmDialog.setPositiveButton(R.string.Yes, 
					new DialogInterface.OnClickListener()
			{

				@Override
				public void onClick(DialogInterface dialog, int which) {

					AlertDialog.Builder builder = new AlertDialog.Builder(ReminderViewActivity.this);
        			builder.setMessage("Reminder deleted successfully");
        			builder.setTitle("Success");
        			builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
        				
        				@Override
        				public void onClick(DialogInterface dialog, int which) 
        				{
        					final ContentResolver resolver = getContentResolver();
      				      new Thread(new Runnable() { //sintaks diwabah ini berjalan di thread lain		         
      				         public void run() {
      				            Uri delUri = ContentUris.withAppendedId(ReminderLocation.CONTENT_URI, ReminderId);
      				            int resCount = resolver.delete(delUri, null, null);
      				            if (resCount == 0) //jika resCount = 0, maka penghapusan gagal. sampaikan pesan kegagalan melalui eventBus
      				            {
      				            	runOnUiThread(new Runnable()
      				            	{
      				            		public void run()
      				            		{
      				            			AlertDialog.Builder builder = new AlertDialog.Builder(ReminderViewActivity.this);
      				            			builder.setMessage("Reminder deletion failed");
      				            			builder.setTitle("Error");
      				            			builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
      				            				
      				            				@Override
      				            				public void onClick(DialogInterface dialog, int which) 
      				            				{
      				            				}
      				            			});
      				            			builder.create().show();
      				            		}
      				            	});
      				            		
      				            }
      				         }
      				      }).start();
      				      ReminderViewActivity.this.finish();
        				}
        			});
        			builder.create().show();
				}
				
			});
			confirmDialog.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					
				}
				
				
			});
			confirmDialog.create().show();
			break;
		case R.id.reminderEdit:
			Intent reminderEditIntent = new Intent(this, ReminderFormFragment.class);
			reminderEditIntent.putExtra("reminderID", ReminderId);
			startActivity(reminderEditIntent);
		case android.R.id.home:
			this.finish();
			break;
		}
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(id == REMINDER_VIEW_LOADER)
		{
			reminderLoader = new CursorLoader(this, uri,
					ReminderLocation.REMINDERLOCATION_PROJECTION, null, null, null);
		}
		
		return reminderLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(data != null)
		{
			if(data.moveToFirst() && data.getCount() > 0)
			{
				ReminderTitleView.setText(data.getString(data.getColumnIndex(ReminderLocation.REMINDERLOCATION_TITLE)));
				ReminderDescriptionView.setText(data.getString(data.getColumnIndex(ReminderLocation.REMINDERLOCATION_DESCRIPTION)));
				ReminderPlaceView.setText(data.getString(data.getColumnIndex(ReminderLocation.PLACE_NAME)));
				if(data.getInt(data.getColumnIndex(ReminderLocation.STATUS)) == 0)
				{
					ReminderDoneToggleButton.setChecked(false);
				}
				else
				{
					ReminderDoneToggleButton.setChecked(true);
				}
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		
	}
}
