package com.skripsi.scheduleplus;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
 

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressLint("NewApi")
public class SMSReceiver extends BroadcastReceiver
{
	@Override
    public void onReceive(Context arg0, Intent intent) 
    {
    	  System.currentTimeMillis();

          Bundle bundle = intent.getExtras();
          String telno = bundle.getString("telno");
          String msg = bundle.getString("msg");
          int time = bundle.getInt("time");
         // Toast.makeText(arg0, msg, Toast.LENGTH_LONG).show();
          // final int NOTIF_ID = 1;
          NotificationManager notofManager = (NotificationManager) arg0
                  .getSystemService(Context.NOTIFICATION_SERVICE);

          Intent notificationIntent = new Intent(arg0, Main.class);
          PendingIntent.getActivity(arg0, 0,
                  notificationIntent, 0);
         
          Notification notification = new Notification.Builder(arg0).setContentTitle("Scheduleplus AutoSMS")
        		  .setContentText("Message to "+telno+" sent successfully")
        		  .build();

          /*
          Notification notification = new Notification(icon, msg, when);

          notification.setLatestEventInfo(arg0, telno, msg, contentIntent);
		*/
          notification.defaults |= Notification.DEFAULT_SOUND;
          notification.flags |= Notification.FLAG_AUTO_CANCEL;
          notification.icon |= R.drawable.schedplusicon;

          notofManager.notify(time, notification);
    	
    	// TODO Auto-generated method stub

        /*int myCount;
        String cnt=intent.getStringExtra("NOTIF_ID");
        if(cnt==null)
        Log.d("MYAPP","Data not received");
        Log.d("MYAPP", "NOTIF_ID:"+cnt);
        myCount=Integer.parseInt(cnt);*/

        Log.d("MYAPP","Broadcast receiver called...");
        Log.d("sendNo", telno);
        Log.d("sendMsg", msg);
        SmsManager sm = SmsManager.getDefault();
        sm.sendTextMessage(telno, null, msg, null, null);
        Log.d("MYAPP", "Message Sent");
        Toast.makeText(arg0, "Msg sent successfully", Toast.LENGTH_LONG).show();
        
        ContentValues values = new ContentValues(); 
        
        values.put("msg", msg); 
                  
        values.put("telno", telno); 
        
        if (arg0.getContentResolver().insert(Uri.parse("content://sms/conversations"), values) != null)
        {
        	Log.d("Automsg","sukses");
        }
        else
        {
        	Log.d("Automsg","gagal");
        }
       
        //sm.sendTextMessage(ZaxSmsScheduler.telno, null, ZaxSmsScheduler.msg, null, null);
        
      
      
    	//---get the SMS message passed in---
       /* Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        String str = "";            
        if (bundle != null)
        {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];            
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                str += "SMS from " + msgs[i].getOriginatingAddress();                     
                str += " :";
                str += msgs[i].getMessageBody().toString();
                str += "\n";        
            }
            //---display the new SMS message---
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        }        */                 
    }
    
	public static void setSchedule(Context context, String telno, long setTime, String msg, long smsid)
	{
		 Intent intent = new Intent(context, SMSReceiver.class);
         
         intent.putExtra("telno", telno);
         intent.putExtra("time", (int) setTime);
         intent.putExtra("msg", msg);
         
         PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) smsid, intent, PendingIntent.FLAG_UPDATE_CURRENT);
     
         AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE); 
                
         alarmManager.set(AlarmManager.RTC_WAKEUP,
        		 setTime, pendingIntent);
         
	}
	
	public static void updateSchedule(Context context, String telno, long setTime, long cancelTime, String msg, long smsid)
	{
		 Intent intentUpdate = new Intent(context, SMSReceiver.class);
		 Intent intentCancel = new Intent(context, SMSReceiver.class);
         
		 intentUpdate.putExtra("telno", telno);
		 intentUpdate.putExtra("time", (int) setTime);
		 intentUpdate.putExtra("msg", msg);
		 
		 intentCancel.putExtra("telno", telno);
		 intentCancel.putExtra("time", (int) cancelTime);
		 intentCancel.putExtra("msg", msg);
         
         PendingIntent pendingIntentUpdate = PendingIntent.getBroadcast(context, (int) smsid, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
         
         PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(context, (int) smsid, intentCancel, PendingIntent.FLAG_UPDATE_CURRENT);
     
         AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE); 
                 
         alarmManager.cancel(pendingIntentCancel);
         
         alarmManager.set(AlarmManager.RTC_WAKEUP,
        		setTime, pendingIntentUpdate);
         
	}
	
	public static void cancelSchedule(Context context, String telno, long setTime, String msg, long smsid)
	{
		 Intent intent = new Intent(context, SMSReceiver.class);
         
         intent.putExtra("telno", telno);
         intent.putExtra("time", (int) setTime);
         intent.putExtra("msg", msg);
         
         PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) smsid, intent, PendingIntent.FLAG_UPDATE_CURRENT);
     
         AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE); 
                 
         alarmManager.cancel(pendingIntent);
	}
}