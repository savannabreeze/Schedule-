package com.skripsi.scheduleplus;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;



public class ReminderService<MainActivity> extends Service implements
			GooglePlayServicesClient.ConnectionCallbacks,
			GooglePlayServicesClient.OnConnectionFailedListener
{
	private static final float MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 150; // Dalam satuan meter
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1800000; // Dalam satuan millisecond (30 menit)
	protected LocationClient locationClient;
	protected LocationRequest theLocRequest;
	protected PendingIntent locationPendingIntent;
	private SharedPreferences mSharedPref;
	private SharedPreferences.Editor mSharedPrefEditor;
		
	@Override
	public void onCreate()
	{
		super.onCreate();
		theLocRequest = LocationRequest.create();
		theLocRequest.setInterval(MINIMUM_TIME_BETWEEN_UPDATES);
		theLocRequest.setSmallestDisplacement(MINIMUM_DISTANCE_CHANGE_FOR_UPDATES);
		theLocRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		
		mSharedPref = getSharedPreferences("SCHEDULEPLUS_SERVICE", Context.MODE_PRIVATE);
		
		locationClient = new LocationClient(this,this,this);
		locationClient.connect();
		Log.d("ReminderService", "Reminder Service Created");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);
		Log.d("ReminderService", "Service started...");
		mSharedPrefEditor = mSharedPref.edit();
		mSharedPrefEditor.putBoolean("SERVICE_STARTED", true);
		mSharedPrefEditor.apply();
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy()
	{
		mSharedPrefEditor = mSharedPref.edit();
		mSharedPrefEditor.putBoolean("SERVICE_STARTED", false);
		mSharedPrefEditor.apply();
		locationClient.disconnect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.d("ReminderService", "onConnectionFailed");
		if(arg0.hasResolution())
		{
			createNotificationReminder("SchedulePlus Location Service", 
				"Google Play Services are unavailable. Click here to resolve this issue", arg0.getResolution(),0);
		}
		else
		{
			createNotificationReminder("SchedulePlus Location Service",
				"Google Play Services are unavailable. Error: "+arg0.getErrorCode(), null, 1);
		}
		stopSelf();
	}

	@Override
	public void onConnected(Bundle arg0) {
		Intent locUpdateIntent = new Intent(getApplicationContext(), ReminderReceiver.class);
		locationPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, locUpdateIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		locationClient.requestLocationUpdates(theLocRequest, locationPendingIntent);
		Log.d("ReminderService", "Connected");		
	}

	@Override
	public void onDisconnected() {
		locationClient.removeLocationUpdates(locationPendingIntent);
		Log.d("ReminderService", "Disconnected");		
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void createNotificationReminder(String title, String text, PendingIntent intent, int id)
	{
		long pattern[] = {500,500};
		Notification notif = new Notification.Builder(getApplicationContext())
					.setContentTitle(title)
					.setContentText(text)
					.setVibrate(pattern)
					.setContentIntent(intent)
					.build();
		
		NotificationManager notifManager  = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.notify(id, notif);
	}

}
