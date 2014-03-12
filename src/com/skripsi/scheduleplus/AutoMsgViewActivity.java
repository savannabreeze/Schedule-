package com.skripsi.scheduleplus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.SMS;



public class AutoMsgViewActivity extends Activity implements LoaderCallbacks<Cursor>
{
	//identitas loader
	private static final int SMS_VIEW_LOADER = 220;
		
	private static String INTENT_SMS_FORM = "INTENT_SMS_FORM";
	
	//deklrasi textview
	protected TextView SMSPhoneTextView;
	protected TextView SMSTimeTextView;
	protected TextView SMSMessageTextView;
	
	private Intent SMSViewIntent;
	private Intent SMSEditIntent;
	
	//private SimpleCursorAdapter fileListAdapter;
	
	private CursorLoader viewLoader;
	
	private static String INTENT_SMS_VIEW = "INTENT_SMS_VIEW";
	
	protected long SMSID;
	
	//deklarasi alert dialog
	AlertDialog.Builder errDialog;
	AlertDialog.Builder confirmDialog;
	
	private long temp_date;
	private long deleteTime;
	
	private static final String DATE_FORMAT = "yyyyMMddHHmm";
	private static final String DATE_FORMAT_VIEW = "MMMM dd, yyyy HH:mm";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
	private static final SimpleDateFormat dateFormatView = new SimpleDateFormat(DATE_FORMAT_VIEW, Locale.getDefault());
	private Date convertedDate;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_sms_view);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//Ambil ID SMS dari Intent
		SMSViewIntent = getIntent();
		SMSID = SMSViewIntent.getLongExtra(INTENT_SMS_VIEW, -1);	
		
		//Inisialisaasi TextView
		SMSPhoneTextView = (TextView)findViewById(R.id.SMSPhoneTextView);
		SMSTimeTextView = (TextView)findViewById(R.id.SMSTimeTextView);
		SMSMessageTextView = (TextView)findViewById(R.id.SMSMessageTextView);	
		
		//tanpilkan pesan error jika id yang diterima adalah -1
		if(SMSID == -1)
		{
			errDialog = new AlertDialog.Builder(this);
			errDialog.setMessage("Error opening SMS schedule");
			errDialog.setTitle("Open Error");
			errDialog.setPositiveButton("OK", 
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							AutoMsgViewActivity.this.finish();
							
						}
					});
			errDialog.create().show();
		}
		else
		{
			//inisialisasi LoaderManager jika id yang diterima selain -1.
			getLoaderManager().initLoader(SMS_VIEW_LOADER, null, this);
		}
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater mInflater = new MenuInflater(this);
		mInflater.inflate(R.menu.sms_view_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.SMSDelete: //jika option menu hapus agenda dipilih, tampilkan kotak dialog konfirmasi untuk menghapus agenda
			confirmDialog = new AlertDialog.Builder(this);
			confirmDialog.setTitle(R.string.SMS_delete_title);
			confirmDialog.setMessage(R.string.Delete_Text_SMS);
			confirmDialog.setPositiveButton(R.string.Yes, 
					new DialogInterface.OnClickListener()
			{

				@Override
				public void onClick(DialogInterface dialog, int which) {
					final ContentResolver resolver = getContentResolver();
				      new Thread(new Runnable() 
				      {	         
				         public void run() 
				         {
				            Uri delUri = ContentUris.withAppendedId(SMS.CONTENT_URI, SMSID);
				            int resCount = resolver.delete(delUri, null, null);
				            if(resCount == 0)
				            {
				            	runOnUiThread(new Runnable()
				            	{
				            		public void run()
				            		{
					            		createAndShowDialog("A problem occurred when deleting scheduled message", "Error");
				            		}
				            	});
				            }
				            else
				            {
				            	runOnUiThread(new Runnable()
				            	{
				            		public void run()
				            		{
				            			AlertDialog.Builder builder = new AlertDialog.Builder(AutoMsgViewActivity.this);
				            			builder.setMessage("Message deleted successfully");
				            			builder.setTitle("Success");
				            			builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				            				
				            				@Override
				            				public void onClick(DialogInterface dialog, int which) {
				            					SMSReceiver.cancelSchedule(getApplication(), SMSPhoneTextView.getText().toString(), deleteTime, SMSMessageTextView.getText().toString(), SMSID);
				            					AutoMsgViewActivity.this.finish();	
				            				}
				            			});
				            			builder.create().show();
						            }
				            	});
				            	
				            }
				         }
				      }).start();
					
				}
				
			});
			confirmDialog.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					
				}
				
				
			});
			confirmDialog.create().show();
			break;
		case R.id.SMSEdit:
			SMSEditIntent = new Intent(this, AutoMsgFormFragment.class);
			SMSEditIntent.putExtra(INTENT_SMS_FORM, SMSID);
			startActivity(SMSEditIntent);
			break;
		case android.R.id.home:
			this.finish();
			break;
		}
		return true;
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(id == 220)
		{
			Uri viewLoaderUri = ContentUris.withAppendedId(SMS.CONTENT_URI, SMSID);
			viewLoader =  new CursorLoader(
					this,
					viewLoaderUri,
					SMS.SMS_PROJECTION,
					null,
					null,
					null);
		}
		
		return viewLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(loader.getId() == 220)
		{
			if(data != null && data.moveToFirst())
			{
				temp_date = data.getLong(data.getColumnIndex(SMS.TIME));
				try {
					convertedDate = dateFormat.parse(String.valueOf(temp_date));
					deleteTime = convertedDate.getTime();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SMSTimeTextView.setText(dateFormatView.format(convertedDate));
				SMSPhoneTextView.setText(data.getString(data.getColumnIndex(SMS.PHONE)));
				SMSMessageTextView.setText(data.getString(data.getColumnIndex(SMS.MESSAGE)));			
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		
	}
	
	private void createAndShowDialog(String message, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		builder.create().show();
	}
}