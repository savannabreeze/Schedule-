package com.skripsi.scheduleplus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.SMS;

public class AutoMsgFormFragment extends Activity implements LoaderCallbacks<Cursor> {
	
	//Id loader untuk load data dari tabel agenda
	private static final int SMS_ADD_LOADER = 210;


	public static int NOTIF_ID = 0;
	
	
	private Intent formIntent;
	
	//deklarasi objek di layout xml
	EditText txtPhoneNo;
	EditText txtMessage;
	EditText smsDate;
	EditText smsTime;
	DatePicker dpResult;
	protected int mHour;
	protected int mDay;
	protected int mMonth;
	protected int mYear;
	protected int mMin;
	
	private static String INTENT_SMS_FORM = "INTENT_SMS_FORM";
	
	/*	ID SMS
	 * 	-1 --> Create AutoMsg
	 *  Selain -1 --> Edit AutoMsg
	 */
	private long SMSID = -1;
	
	//	Uri agenda yang baru di-insert
	private Uri result;
	
	//	Counter update
	private int updateCount = 0;
	
	private static final String DATE_FORMAT = "yyyyMMddHHmm"; //Format tanggal yang disimpan di database
	private static final String DATE_FORMAT_VIEW = "MMM d, yyyy"; //Format tanggal untuk ditampilkan ke user
	private static final String TIME_FORMAT_VIEW = "HH : mm"; //Format waktu untuk ditampilkan ke user
	
	//	SimpleDateFormat untuk formatting tanggal
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
	private static final SimpleDateFormat dateFormatView = new SimpleDateFormat(DATE_FORMAT_VIEW, Locale.getDefault());
	private static final SimpleDateFormat timeFormatView = new SimpleDateFormat(TIME_FORMAT_VIEW, Locale.getDefault());
	
	//	Date untuk menampung tanggal dan waktu yang dipilih
	private Date pickedDate;
	private Date pickedTime;
	private Date pickedDateAndTime;
	
	//	Calendar untuk menampung waktu dan tanggal sistem dan yang dipilih user
	private Calendar dateAndTimeCalendar;
	private Calendar systemCal;
	
	/*	Variabel long untuk menampung waktu dan tanggal yang telah di-konversi
	 * 
	 * 	setTime: Waktu dan tanggal pilihan user
	 * 	saveDateLong: Waktu dan tanggal dalam format penyimpanan database.
	 * 	saveDateString: Waktu dan tanggal dalam format penyimpanan database versi String
	 *  cancelTime: Waktu dan tanggal yang akan digunakan untuk membatalkan alarmManager dengan ID yang sesuai 
	 */
	private long setTime;
	private long saveDateLong; //sebagai id alarm yang akan di cancel
	private String saveDateString;
	private long cancelTime;
	
	ComponentName component;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.fragment_sms_add);
	      
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	      
	    /*	Mengambil SMSID dari intent yang diterima
	     * 	
	     * 	Create AutoMsg: -1
	     * 	Edit AutoMsg: Selain -1
	     */
	    formIntent = getIntent();
	    SMSID = formIntent.getLongExtra(INTENT_SMS_FORM, -1);
	    Log.d("AutoMsgForm", "Received SMSID: "+SMSID);
	      
	    //	inisialisasi objek yang ada di xml
	    smsDate = (EditText) findViewById(R.id.smsDate);
	    smsTime = (EditText) findViewById(R.id.smsTime);
	    txtPhoneNo=(EditText) findViewById(R.id.txtPhoneNo);
        txtMessage=(EditText) findViewById(R.id.txtMessage);
         
        //	Kalender sistem
        systemCal = dateAndTimeCalendar = Calendar.getInstance(Locale.getDefault());
	     
        if(SMSID != -1)
        {
        	getLoaderManager().initLoader(SMS_ADD_LOADER, null, this);
        }
	    smsDate.setOnClickListener(new OnClickListener() 
	    {

	            @Override
	            public void onClick(View arg0) {
	                DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {

	                    @Override
	                    public void onDateSet(DatePicker view, int year, int monthofyear, int dayofmonth) {
	                        // TODO Auto-generated method stub
	                        mMonth = monthofyear;
	                        mYear = year;
	                        mDay = dayofmonth;
	                        Toast.makeText(getBaseContext(), "Date Set is :"+mDay+"/"+(mMonth+1)+"/"+mYear, Toast.LENGTH_SHORT).show();
	                        //tempNewDate = String.valueOf(year) + String.valueOf(monthofyear) + String.valueOf(dayofmonth);	                        
	                        dateAndTimeCalendar.set(Calendar.YEAR, year);
	                        dateAndTimeCalendar.set(Calendar.MONTH, monthofyear);
	                        dateAndTimeCalendar.set(Calendar.DAY_OF_MONTH, dayofmonth);
	                        if(dateAndTimeCalendar.getTimeInMillis() < System.currentTimeMillis())
	                        {
	                        	createAndShowDialog("You cannot enter past date", "Error");
	                        }
	                        else
	                        {
	                        	pickedDate = dateAndTimeCalendar.getTime();
	                        	smsDate.setText(dateFormatView.format(pickedDate));
	                        }
	                    }
	                };
	                new DatePickerDialog(AutoMsgFormFragment.this,d,systemCal.get(Calendar.YEAR),systemCal.get(Calendar.MONTH),systemCal.get(Calendar.DAY_OF_MONTH)).show();
	            }
	        });
	        
	    smsTime.setOnClickListener(new OnClickListener() 
	    {

	            @Override
	            public void onClick(View arg0) {
	                // TODO Auto-generated method stub
	                TimePickerDialog.OnTimeSetListener t=new TimePickerDialog.OnTimeSetListener() {

	                    @Override
	                    public void onTimeSet(TimePicker view2, int hour, int min) {
	                        // TODO Auto-generated method stub
	                        mHour = hour;
	                        mMin = min;
							//tempNewTime = String.valueOf(hour) + String.valueOf(min);
	                        Log.d("MYAPP", "hh:"+mHour+"\nmm:"+mMin);
	                        Toast.makeText(getBaseContext(), "Time Set is:"+mHour+":"+mMin+":00", Toast.LENGTH_SHORT).show();
	                        dateAndTimeCalendar.set(Calendar.HOUR_OF_DAY, hour);
	                        dateAndTimeCalendar.set(Calendar.MINUTE, min);
	                        dateAndTimeCalendar.set(Calendar.MILLISECOND, 0);
	                        pickedTime = dateAndTimeCalendar.getTime();
	                        smsTime.setText(timeFormatView.format(pickedTime));
	                    }
	                };
	                new TimePickerDialog(AutoMsgFormFragment.this,t,systemCal.get(Calendar.HOUR_OF_DAY),systemCal.get(Calendar.MINUTE),true).show();

	            }
	        });
	        
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.sms_form_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.smsSave:
			long timeToTrigger;
        	
			//Format tanggal dalam milisecond yang akan digunakan oleh AlarmManager
            setTime = dateAndTimeCalendar.getTimeInMillis();
            Log.d("AutoMsgForm", "setTime = "+setTime);

            if(setTime > System.currentTimeMillis())
            {	if(txtPhoneNo.getText().toString().equals("") && txtMessage.getText().toString().equals("") 
            		&& smsDate.getText().toString().equals("")
            		&& smsTime.getText().toString().equals(""))
            	{
            		createAndShowDialog("You must fill all the field", "Error");
            	}
            	else if(txtPhoneNo.getText().toString().equals(""))
            	{
            		createAndShowDialog("You must enter a phone number", "Error");
            	}
            	else if(txtMessage.getText().toString().equals(""))
            	{
            		createAndShowDialog("You cannot send an empty message", "Error");
            	}
            	else if(smsDate.getText().toString().equals(""))
            	{
            		createAndShowDialog("You must enter a date", "Error");
            	}
            	else if(smsTime.getText().toString().equals(""))
            	{
            		createAndShowDialog("You must enter a time", "Error");
            	}
            	else
            	{
	            	Log.d("AutoMsgForm", "setTimeBener");
	            	if(SMSID == -1)
	            	{
	            		Log.d("AutoMsgForm", "Masuk if new sms");
		                timeToTrigger=setTime-System.currentTimeMillis();
		                String msg = txtMessage.getText().toString();
		                String telno = txtPhoneNo.getText().toString();
		                
		                //konversi format tanggal untuk disimpan di database
		                pickedDateAndTime = dateAndTimeCalendar.getTime();
		                saveDateString = dateFormat.format(pickedDateAndTime);
		                saveDateLong = Long.parseLong(saveDateString);
		                Log.d("AutoMsgSave", String.valueOf(saveDateLong));
		
		                ContentValues cv = new ContentValues();
		                cv.put("phone", telno);
		                cv.put("time", saveDateLong);
		                cv.put("message", msg);
		                
		                result = getContentResolver().insert(SMS.CONTENT_URI, cv);
		                
		                if(result != null)
		                {
		                	 String newID = result.getLastPathSegment();
		                	 Log.d("AutoMsgForm", newID);
		                	 SMSReceiver.setSchedule(getApplication(), telno, setTime, msg, Long.parseLong(newID));
		                     createAndShowDialog("Sms Scheduled after:"+(timeToTrigger/1000/60)+" min", "Success");
		                }
		                else
		                {
		                	createAndShowDialog("Fail to schedule sms", "Error");
		                }
	            	}
	            	else
	            	{
	            		Log.d("AutoMsgForm", "masuk if edit form");
	            		timeToTrigger=setTime-System.currentTimeMillis();
		                String msg = txtMessage.getText().toString();
		                String telno = txtPhoneNo.getText().toString();
		                
		                pickedDateAndTime = dateAndTimeCalendar.getTime();
		                saveDateString = dateFormat.format(pickedDateAndTime);
		                saveDateLong = Long.parseLong(saveDateString);
		                
		                ContentValues cv = new ContentValues();
		                cv.put("phone", telno);
		                cv.put("time", saveDateLong);
		                cv.put("message", msg);
		                
		                updateCount = getContentResolver().update(SMS.CONTENT_URI, cv, "_id = "+SMSID, null);
		                
		                if(updateCount != 0)
		                {
		                	SMSReceiver.updateSchedule(getApplication(), telno, setTime, cancelTime, msg, SMSID);
		                	createAndShowDialog("SMS schedule updated to be sent in: "+(timeToTrigger/1000/60)+" min", "Success");
		                }
		                else
		                {
		                	createAndShowDialog("Fail to schedule sms", "Error");
		                }
	            	}
	            }
	            
            }
            else
            {
            	createAndShowDialog("Invalid Date/Time Specified", "Error");
            }
            break;
		case android.R.id.home:
			AlertDialog.Builder builder = new AlertDialog.Builder(AutoMsgFormFragment.this);
			builder.setMessage("You have unsaved changes. Exit anyway?");
			builder.setTitle("Warning");
			builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					AutoMsgFormFragment.this.finish();	
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
	        AlertDialog.Builder builder = new AlertDialog.Builder(AutoMsgFormFragment.this);
			builder.setMessage("You have unsaved changes. Exit anyway?");
			builder.setTitle("Warning");
			builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					AutoMsgFormFragment.this.finish();	
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
		if(id == SMS_ADD_LOADER)
		{
			Uri loaderUri = ContentUris.withAppendedId(SMS.CONTENT_URI, SMSID);
			return new CursorLoader(
					this,
					loaderUri,
					SMS.SMS_PROJECTION,
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
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		if(arg1 != null && arg1.moveToFirst())
		{
			txtPhoneNo.setText(arg1.getString(arg1.getColumnIndex(SMS.PHONE)));
			txtMessage.setText(arg1.getString(arg1.getColumnIndex(SMS.MESSAGE)));
			saveDateLong  = arg1.getLong(arg1.getColumnIndex(SMS.TIME));
			//dateAndTimeCalendar.setTimeInMillis(saveDateLong);
			try {
				pickedDateAndTime = dateFormat.parse(String.valueOf(saveDateLong));
				dateAndTimeCalendar.setTime(pickedDateAndTime);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			smsTime.setText(timeFormatView.format(pickedDateAndTime));
			smsDate.setText(dateFormatView.format(pickedDateAndTime));
			
			//Log.d("AutoMsgForm", "dataLoaded");
			//cancelTime = pickedDateAndTime.getTime();
			//Log.d("AutoMsgForm", "cancelTime = "+cancelTime);
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private void createAndShowDialog(final String message, final String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(title.equalsIgnoreCase("Success"))
				{
					AutoMsgFormFragment.this.finish();
				}
				
			}
		});
		builder.create().show();
	}
}

	    
