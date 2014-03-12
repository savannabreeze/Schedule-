package com.skripsi.scheduleplus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.webkit.MimeTypeMap;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.skripsi.scheduleplus.R;
import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.Agenda;
import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.File;
import com.skripsi.scheduleplus.FilePicker.*;

import de.greenrobot.event.EventBus;

public class AgendaFormFragment extends Activity implements LoaderCallbacks<Cursor> {
	
	//Id loader untuk load data dari tabel agenda
	private static final int AGENDA_ADD_LOADER = 110;
	
	private static final int AGENDA_ADD_FILE_LOADER = 140;
	
	private Intent formIntent;
	
	//deklarasi objek di layout xml
	EditText agendaDateAddEditText;
	
	EditText agendaTitleAddEditText;
	
	EditText agendaDescriptionAddEditText;
	
	ListView fileListView;
	//deklarasi objek di layour xml
	
	//Cursor adapter custom untuk File List
	private SimpleCursorAdapter fileListAdapter;
	
	private static String INTENT_AGENDA_FORM = "INTENT_AGENDA_FORM";
	
	long agendaID;
	
	//Uri agenda yang baru di-insert
	private Uri result;
	
	//Uri file yang baru di-insert
	private Uri fileResult;
	
	//hasil update
	private int updateResult = 0;
	
	private int REQUEST_CODE = 99;
	
	private long newAgendaId =  -1;
	
	//List penampung nama file dan direktori sebelum di-insert ke database
	private List<String> fileNameList = new ArrayList<String>();
	
	private List<String> fileDirList = new ArrayList<String>();
	//List penampung nama file dan direktori sebelum di-insert ke database
	
	//Adapter array untuk File List
	private ArrayAdapter<String> fileListArrayAdapter;
	
	private Intent browseFileIntent;
	
	private String TAG_BROWSER_FILE = "File_Browser";
	
	private AlertDialog.Builder fileDeleteDialog;
	
	private static final String DATE_FORMAT = "yyyyMMdd";
	private static final String DATE_FORMAT_VIEW = "MMMM dd, yyyy";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
	private static final SimpleDateFormat dateFormatView = new SimpleDateFormat(DATE_FORMAT_VIEW, Locale.getDefault());
	private Date convertedDate;
	
	private static long selectedDateToBeInserted;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.fragment_agenda_add);
	      //getActionBar().setDisplayHomeAsUpEnabled(true);
	      //mengambil data extra. Kalo dari Fragment Agenda ngasih data extra "-1" berarti form dipake buat add.
	      //kalo selain "-1", form dipake buat edit agenda yang udah ada
	      formIntent = getIntent();
	      agendaID = formIntent.getLongExtra(INTENT_AGENDA_FORM, -1);
	      
	      //inisialisasi objek yang ada di xml
	      agendaDateAddEditText = (EditText)findViewById(R.id.agendaDateAddEditText); 	
	      agendaTitleAddEditText = (EditText)findViewById(R.id.agendaTitleAddEditText);	  	
		  agendaDescriptionAddEditText = (EditText)findViewById(R.id.agendaDescriptionAddEditText);
		  fileListView = (ListView)findViewById(R.id.fileListView);
		  
		  //setup adapter buat listview File List. Kalo new agenda, ArrayAdapter dipake karena data file yang di
		  //attach belum ada di database. Sumber datanya dari List penampung. List penampung diisi waktu milih file
		  //di file browser
		  if (agendaID != -1) 
		  {	  //edit agenda
			  getLoaderManager().initLoader(AGENDA_ADD_LOADER, null, this);
			  getLoaderManager().initLoader(AGENDA_ADD_FILE_LOADER, null, this);
			  fileListAdapter = new SimpleCursorAdapter(
					  this,
						R.layout.file_list_layout,
						(Cursor)null,
						new String[] {File._ID, File.FILENAME, File.FILELOCATION},
						new int[] {R.id.fileIdTextView,R.id.fileNameTextView, R.id.fileDirTextView}
						,0);
			  fileListView.setAdapter(fileListAdapter); //adapter yang dipake custom CursorAdapter
			  
			  //listener buat tiap item di File List.
			  fileListView.setOnItemClickListener(new OnItemClickListener()
			  {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					View theView = parent.getChildAt(position);
					TextView theText = (TextView)theView.findViewById(R.id.fileDirTextView);
					String Dir = theText.getText().toString();				
					String extension = MimeTypeMap.getFileExtensionFromUrl(Dir);				
					String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);			
					Uri filePath = Uri.parse(Dir);
					Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
					openFileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					openFileIntent.setDataAndType(filePath,type);
					Intent openInChooser = Intent.createChooser(openFileIntent, "Open With....");
					startActivity(openInChooser);
					
				}
				  
			  });
			  
			  fileListView.setOnItemLongClickListener(new OnItemLongClickListener()
			  {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view,
						int position, long id) 
				{
					View theView2 = parent.getChildAt(position);
					TextView theIdText = (TextView)theView2.findViewById(R.id.fileIdTextView);					
					String idString = theIdText.getText().toString();
					final long fileId = Long.parseLong(idString);
					fileDeleteDialog = new AlertDialog.Builder(AgendaFormFragment.this);
					fileDeleteDialog.setTitle("Confirm File Deletion");
					fileDeleteDialog.setMessage("Are you sure you want to delete this file?");
					fileDeleteDialog.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							final ContentResolver resolver = getContentResolver();
						      new Thread(new Runnable() { //sintaks diwabah ini berjalan di thread lain		         
						         public void run() {
						            Uri delUri = ContentUris.withAppendedId(File.CONTENT_URI, fileId);
						            int resCount = resolver.delete(delUri, null, null);
						            if (resCount == 0) //jika resCount = 0, maka penghapusan gagal. sampaikan pesan kegagalan melalui eventBus
						            {
						            	EventBus.getDefault().post("deleteFailure");
						            }
						            else
						            {
						            	EventBus.getDefault().post("deleteSuccessful");
						            	fileListAdapter.notifyDataSetChanged();
						            }
						         }
						      }).start();
						      getLoaderManager().restartLoader(AGENDA_ADD_FILE_LOADER, null, AgendaFormFragment.this);
						}
					});
					fileDeleteDialog.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
						
						
					});
					fileDeleteDialog.create().show();
					return false;
				}
				  
			  });
		  }
		  else
		  {   //add agenda
			  fileListArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,fileNameList);
			  fileListView.setAdapter(fileListArrayAdapter);//adapter yang dipake ArrayAdapter
			  
			  //Item click, menampilkan dialog box "Open With.."
			  fileListView.setOnItemClickListener(new OnItemClickListener()
			  {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String Dir = fileDirList.get(position).toString();				
					String extension = MimeTypeMap.getFileExtensionFromUrl(Dir);				
					String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);			
					Uri filePath = Uri.parse(Dir);
					Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
					openFileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					openFileIntent.setDataAndType(filePath,type);
					Intent openInChooser = Intent.createChooser(openFileIntent, "Open With....");
					startActivity(openInChooser);
					
				}
				  
			  });
			  
			  //Long item click, menampilkan dialog "delete file"
			  fileListView.setOnItemLongClickListener(new OnItemLongClickListener()
			  {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view,
						int position, long id) 
				{
					final int filePos = position;
					Log.d("nilaiFilePos","Nilai filePos adalah: "+filePos);
					fileDeleteDialog = new AlertDialog.Builder(AgendaFormFragment.this);
					fileDeleteDialog.setTitle("Confirm File Deletion");
					fileDeleteDialog.setMessage("Are you sure you want to delete this file?");
					fileDeleteDialog.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							fileDirList.remove(filePos);
							fileNameList.remove(filePos);
							fileListArrayAdapter.notifyDataSetChanged();
							createAndShowDialog("File deleted successfully", "Success");
						}
					});
					fileDeleteDialog.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
						
						
					});
					fileDeleteDialog.create().show();
					return false;
				}
				  
			  });
		  }
		  
		  //Listener yang nampilin DatePicker waktu field date di klik
		  agendaDateAddEditText.setOnClickListener(new OnClickListener()
		  {

			@Override
			public void onClick(View v) {
				DatePickerDialog.OnDateSetListener dp = new DatePickerDialog.OnDateSetListener() {
					
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear,
							int dayOfMonth) {
						Calendar selectedDate = Calendar.getInstance();
						selectedDate.set(Calendar.YEAR, year);
						selectedDate.set(Calendar.MONTH, monthOfYear);
						selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						if(selectedDate.getTimeInMillis() < System.currentTimeMillis())
						{
							createAndShowDialog("You cannot enter a past date","Error");
						}
						else
						{
							Date test = selectedDate.getTime();
							selectedDateToBeInserted = Long.parseLong(dateFormat.format(test));
							String theDate = dateFormatView.format(test);
						
							agendaDateAddEditText.setText(theDate.toString());
						}
					}
				};
				Calendar cal = Calendar.getInstance();
				new DatePickerDialog(AgendaFormFragment.this,dp,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show();
				
			}
			
		  });
		  
		 
		  
		  
		 
		  
		 getActionBar().setDisplayHomeAsUpEnabled(true);
	}//end of on create
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
	        AlertDialog.Builder builder = new AlertDialog.Builder(AgendaFormFragment.this);
			builder.setMessage("You have unsaved changes. Exit anyway?");
			builder.setTitle("Warning");
			builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					AgendaFormFragment.this.finish();	
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
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater mInflater = new MenuInflater(this);
		mInflater.inflate(R.menu.agenda_form_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.agendaSave:
		{
			if(agendaDateAddEditText.getText().toString().equals("") && agendaTitleAddEditText.getText().toString().equals("") )
			{
				createAndShowDialog("You must enter a title and date", "Error");
			}
			else if(agendaDateAddEditText.getText().toString().equals(""))
			{
				createAndShowDialog("You must enter a date", "Error");					
			}
			else if(agendaTitleAddEditText.getText().toString().equals(""))
			{
				createAndShowDialog("You must enter a title", "Error");
			}
			else
			{
				String textTitle = agendaTitleAddEditText.getText().toString();
				String textDesc = agendaDescriptionAddEditText.getText().toString();
				ContentValues agendaCV = new ContentValues();
				agendaCV.put(Agenda.TIME, selectedDateToBeInserted);
				agendaCV.put(Agenda.TITLE, textTitle);
				agendaCV.put(Agenda.DESCRIPTION, textDesc);
				Log.d("Form", String.valueOf(selectedDateToBeInserted));
				if(fileListView.getChildCount() != 0)
				{
					agendaCV.put(Agenda.HAVE_ATTACHMENT, 1);
				}
				else
				{
					agendaCV.put(Agenda.HAVE_ATTACHMENT, 0);
				}
				ContentResolver agendaResolver = getContentResolver();
				if(agendaID == -1)
				{
					result = agendaResolver.insert(Agenda.CONTENT_URI, agendaCV);
					Log.d("resultUri", result.toString());
					if(result == null)
					{
						createAndShowDialog("Fail to save the Agenda", "Error");
					}
					else
					{
						newAgendaId = Long.parseLong(result.getLastPathSegment());
						Log.d("NewAgendaId", String.valueOf(newAgendaId));
						if(newAgendaId != -1)
						{
							for(int x = 0; x < fileDirList.size(); x++)
							{
								ContentValues fileCV = new ContentValues();
								fileCV.put(File.AGENDAID,newAgendaId);
								fileCV.put(File.FILENAME, fileNameList.get(x));
								fileCV.put(File.FILELOCATION, fileDirList.get(x));
								fileResult = agendaResolver.insert(File.CONTENT_URI, fileCV);
								Log.d("FileInsertLog", fileResult.toString());
								if(fileResult != null)
								{
									fileCV.clear();
								}
								else
								{
									createAndShowDialog("Fail to save the Agenda", "Error");
									break;
								}
							}
						}
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setMessage("Agenda saved successfully");
						builder.setTitle("Success");
						builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								AgendaFormFragment.this.finish();
							}
						});
						builder.create().show();
					}
				}
				else
				{
					String where = "_id = " + agendaID;
					updateResult = agendaResolver.update(Agenda.CONTENT_URI, agendaCV, where, null);
					if(updateResult == 0)
					{
						createAndShowDialog("Fail to save the Agenda", "Error");
					}
					else
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setMessage("Agenda saved successfully");
						builder.setTitle("Success");
						builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								AgendaFormFragment.this.finish();
							}
						});
						builder.create().show();
					}
				}
			}
			break;
			
		}
		case R.id.agendaAddFile:
			browseFileIntent = new Intent(getBaseContext(),FileDialog.class);
			browseFileIntent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getPath().toString());
			browseFileIntent.putExtra(FileDialog.CAN_SELECT_DIR, true);
			startActivityForResult(browseFileIntent, REQUEST_CODE);
			break;
		case android.R.id.home:
			AlertDialog.Builder builder = new AlertDialog.Builder(AgendaFormFragment.this);
			builder.setMessage("You have unsaved changes. Exit anyway?");
			builder.setTitle("Warning");
			builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					AgendaFormFragment.this.finish();	
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
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle arg1) {
		if(loaderID == 110)
		{
			Uri loaderUri = ContentUris.withAppendedId(Agenda.CONTENT_URI, agendaID);
			return new CursorLoader(
					this,
					loaderUri,
					Agenda.AGENDA_PROJECTION,
					null,
					null,
					null);
		}
		else if(loaderID == 140)
		{
			return new CursorLoader(
					this,
					File.CONTENT_URI,
					File.FILE_PROJECTION,
					"agendaID = "+agendaID,
					null,
					null);
		}
		else
		{
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if(loader.getId() == 110)
		{
			if(cursor != null && cursor.moveToFirst())
			{
				selectedDateToBeInserted = cursor.getLong(cursor.getColumnIndex(Agenda.TIME));
				try {
					convertedDate = dateFormat.parse(String.valueOf(selectedDateToBeInserted));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				agendaDateAddEditText.setText(dateFormatView.format(convertedDate));
				agendaTitleAddEditText.setText(cursor.getString(cursor.getColumnIndex(Agenda.TITLE)));
				agendaDescriptionAddEditText.setText(cursor.getString(3));
				this.getLoaderManager().restartLoader(AGENDA_ADD_FILE_LOADER, null, this);
			}
		}
		else 
		{
			if(cursor != null && cursor.moveToFirst())
			{
				fileListAdapter.swapCursor(cursor);
			}
		}
		
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		if(agendaID != -1)
		{
			fileListAdapter.swapCursor(null);
		}
	}
	
	public synchronized void onActivityResult(final int requestCode,
            int resultCode, final Intent data) 
	{

            if (resultCode == Activity.RESULT_OK) {
            		/*
                    if (requestCode == REQUEST_SAVE) {
                            System.out.println("Saving...");
                    } else if (requestCode == REQUEST_LOAD) {
                            System.out.println("Loading...");
                    }*/
                    
                    String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
                    String fileName = data.getStringExtra(FileDialog.RESULT_FILENAME);
                    
                    Log.d("FileDir",filePath);
                    Log.d("FileName",fileName);
                    
                    if(agendaID == -1)
                    {
                    	fileDirList.add(filePath);
                    	fileNameList.add(fileName);
                    	fileListArrayAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                    	ContentValues fileCV = new ContentValues();
                    	fileCV.put(File.AGENDAID, agendaID);
                    	fileCV.put(File.FILELOCATION, filePath);
                    	fileCV.put(File.FILENAME,fileName);
                    	ContentResolver fileResolver = getContentResolver();
                    	Uri fileResult = fileResolver.insert(File.CONTENT_URI, fileCV);
                    	if(fileResult == null)
                    	{
                    		Toast.makeText(this,"Fail to add agenda", Toast.LENGTH_LONG).show();
                    	}
                    }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.d(TAG_BROWSER_FILE, "File selection cancelled");
            }

    }
	
	//saat sebuah pesan masuk dalam eventbus, method ini dijalankan.
		public void onEvent(String deleteRes)
		{
			if(deleteRes == "deleteFailure")
			{
				Toast.makeText(this, "A problem occured while deleting this agenda", Toast.LENGTH_LONG).show();
			}
			else
			{
				EventBus.getDefault().post(true);
				this.finish();
			}
		}
		
		public static Calendar getCalendarFromFormattedLong(long l)
		{
			   try {
			                 Calendar c = Calendar.getInstance();
			                 c.setTime(dateFormat.parse(String.valueOf(l)));
			                 return c;
			                  
			          } catch (ParseException e) {
			                 return null;
			          }

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
