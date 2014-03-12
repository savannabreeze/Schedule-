package com.skripsi.scheduleplus;

import com.google.android.gms.location.LocationClient;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.ReminderLocation;

public class ReminderReceiver extends BroadcastReceiver 
{
	Context mContext;
	private double latitude;
	private double longitude;
	private Location location;

	private Cursor retrievedData;
	
	int notifCount = 0;
	static int notifID = 100;
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		mContext = context;
		
		location = intent.getParcelableExtra(LocationClient.KEY_LOCATION_CHANGED);
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		
		checkReminder();
	}
	
	private void checkReminder()
	{
		double dbLatitude;
		double dbLongitude;
		double compareResult;
		Intent intent;
		PendingIntent pendingIntent;
		
		retrievedData = mContext.getContentResolver().query(ReminderLocation.CONTENT_URI, 
				ReminderLocation.REMINDERLOCATION_PROJECTION, 
				ReminderLocation.REMINDERLOCATION_ARGS, 
				null, 
				ReminderLocation.DEFAULT_SORT_ORDER);
		
		if(retrievedData != null)
		{
			if(retrievedData.getCount() > 0 && retrievedData.moveToFirst())
			{
				while(!retrievedData.isAfterLast())
				{
					dbLatitude = retrievedData.getDouble(retrievedData.getColumnIndex(ReminderLocation.LATITUDE));
					dbLongitude = retrievedData.getDouble(retrievedData.getColumnIndex(ReminderLocation.LONGITUDE));
					Location dbLoc = new Location("dbLoc");
					dbLoc.setLatitude(dbLatitude);
					dbLoc.setLongitude(dbLongitude);
					Log.d("ReminderReciever", "latitude user = "+latitude);
					Log.d("ReminderReciever", "longitude user = "+longitude);
					Log.d("ReminderReciever", "latitude di db = "+dbLatitude);
					Log.d("ReminderReciever", "longitude di db = "+dbLongitude);
					//float tempResult[] = new float[3];
					//Location.distanceBetween(latitude, longitude, dbLatitude, dbLongitude, tempResult);
					compareResult = location.distanceTo(dbLoc);
					Log.d("ReminderReciever", "hasil distanceBetween = "+compareResult);
					
					if(compareResult <= 100.0)
					{
						Log.d("ReminderReciever", "compare result < 100");
						intent = new Intent(mContext, ReminderViewActivity.class);
						intent.putExtra("INTENT_REMINDER_VIEW", 
								(long) retrievedData.getInt(retrievedData.getColumnIndex(ReminderLocation._ID)));
						pendingIntent = PendingIntent.getActivity(mContext, notifID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
						createNotificationReminder("Schedule+ Reminder", retrievedData.getString(retrievedData.getColumnIndex(ReminderLocation.REMINDERLOCATION_TITLE)),
								pendingIntent, notifID);
						notifID++;
						notifCount++;
					}
					else
					{
						Log.d("ReminderReciever", "compare result > 100");
					}
					retrievedData.moveToNext();
				}
				
				if(notifCount == 0)
				{
					Intent serviceIntent = new Intent(mContext, ReminderService.class);
					mContext.stopService(serviceIntent);
				}
			}
		}
		else
		{
			Log.d("ReminderService", "Somehow query returns null");
		}
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void createNotificationReminder(String title, String text, PendingIntent intent, int id)
	{
		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		long pattern[] = {500,500};
		Notification notif = new Notification.Builder(mContext)
					.setContentTitle(title)
					.setContentText(text)
					.setSmallIcon(R.drawable.schedplusicon)
					.setVibrate(pattern)
					.setSound(uri)
					.setContentIntent(intent)
					.build();
		
		NotificationManager notifManager  = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.notify(id, notif);
	}

	

}
