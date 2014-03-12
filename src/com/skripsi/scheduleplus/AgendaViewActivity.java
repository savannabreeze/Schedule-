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
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.skripsi.scheduleplus.R;
import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.Agenda;
import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.File;



public class AgendaViewActivity extends Activity implements LoaderCallbacks<Cursor>
{
	//identitas loader
	private static final int AGENDA_VIEW_LOADER = 120;
	private static final int AGENDA_FILE_LOADER = 130;
		
	private static String INTENT_AGENDA_FORM = "INTENT_AGENDA_FORM";
	
	//deklrasi textview
	protected TextView agendaTitleTextView;
	protected TextView agendaDateTextView;
	protected TextView agendaDescriptionTextView;
	
	protected ListView agendaViewListView;
	
	private Intent agendaViewIntent;
	private Intent agendaEditIntent;
	
	private SimpleCursorAdapter fileListAdapter;
	
	private CursorLoader viewLoader;
	
	private static String INTENT_AGENDA_VIEW = "INTENT_AGENDA_VIEW";
	
	protected long agendaID;
	
	private static final String DATE_FORMAT = "yyyyMMdd";
	private static final String DATE_FORMAT_VIEWING = "MMM d, yyyy";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.getDefault());
	private static final SimpleDateFormat dateFormatViewing = new SimpleDateFormat(DATE_FORMAT_VIEWING,Locale.getDefault());
	private long tempDateLong;
	private Date tempDate;
	
	//deklarasi alert dialog
	public AlertDialog.Builder errDialog;
	public AlertDialog.Builder confirmDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_agenda_view);
		
		//mengambil id yang dikirim melalui intent
		agendaViewIntent = getIntent();
		agendaID = agendaViewIntent.getLongExtra(INTENT_AGENDA_VIEW, -1);		
		//inisialisaasi textview
		agendaTitleTextView = (TextView)findViewById(R.id.agendaTitleTextView);
		agendaDateTextView = (TextView)findViewById(R.id.agendaDateTextView);
		agendaDescriptionTextView = (TextView)findViewById(R.id.agendaDescriptionTextView);
		agendaViewListView = (ListView)findViewById(R.id.agendaViewListView);
		
		fileListAdapter = new SimpleCursorAdapter(
				this,
				R.layout.file_list_layout,
				(Cursor)null,
				new String[] {File.FILENAME, File.FILELOCATION, File._ID},
				new int[] {R.id.fileNameTextView, R.id.fileDirTextView, R.id.fileIdTextView}
				,0);
		
		agendaViewListView.setAdapter(fileListAdapter);
		
		agendaViewListView.setOnItemClickListener(new OnItemClickListener()
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
		//tanpilkan pesan error jika id yang diterima adalah -1
		if(agendaID == -1)
		{
			errDialog = new AlertDialog.Builder(this);
			errDialog.setMessage("Error opening agenda");
			errDialog.setTitle("Agenda Error");
			errDialog.setPositiveButton("OK", 
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							AgendaViewActivity.this.finish();
							
						}
					});
			errDialog.create().show();
		}
		else
		{
			//inisialisasi LoaderManager jika id yang diterima selain -1.
			getLoaderManager().initLoader(AGENDA_VIEW_LOADER, null, this);
			getLoaderManager().initLoader(AGENDA_FILE_LOADER, null, this);
		}
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater mInflater = new MenuInflater(this);
		mInflater.inflate(R.menu.agenda_view_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.agendaDelete: //jika option menu hapus agenda dipilih, tampilkan kotak dialog konfirmasi untuk menghapus agenda
			confirmDialog = new AlertDialog.Builder(this);
			confirmDialog.setTitle(R.string.agenda_delete_title);
			confirmDialog.setMessage(R.string.Delete_Text_Agenda);
			confirmDialog.setPositiveButton(R.string.Yes, 
					new DialogInterface.OnClickListener()
			{

				@Override
				public void onClick(DialogInterface dialog, int which) {
					final ContentResolver resolver = getContentResolver();
				      new Thread(new Runnable() { //sintaks diwabah ini berjalan di thread lain		         
				         public void run() {
				            Uri delUri = ContentUris.withAppendedId(Agenda.CONTENT_URI, agendaID);
				            int resCount = resolver.delete(delUri, null, null);
				            if (resCount == 0) //jika resCount = 0, maka penghapusan gagal. sampaikan pesan kegagalan melalui eventBus
				            {
				            	runOnUiThread(new Runnable()
				            	{
				            		public void run()
				            		{
				            			createAndShowDialog("Agenda deletion failed", "Error");
				            		}
				            	});
				            		
				            }
				            else
				            {
				            	runOnUiThread(new Runnable()
				            	{
				            		public void run()
				            		{
				            			AlertDialog.Builder builder = new AlertDialog.Builder(AgendaViewActivity.this);
				            			builder.setMessage("Agenda deleted successfully");
				            			builder.setTitle("Success");
				            			builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				            				
				            				@Override
				            				public void onClick(DialogInterface dialog, int which) {
				            					AgendaViewActivity.this.finish();	
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
		case R.id.agendaEdit:
			agendaEditIntent = new Intent(this, AgendaFormFragment.class);
			agendaEditIntent.putExtra(INTENT_AGENDA_FORM, agendaID);
			startActivity(agendaEditIntent);
			break;
		case android.R.id.home:
			this.finish();
			break;
		}
		
		return true;
	}
	
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(id == 120)
		{
			Uri viewLoaderUri = ContentUris.withAppendedId(Agenda.CONTENT_URI, agendaID);
			viewLoader =  new CursorLoader(
					this,
					viewLoaderUri,
					Agenda.AGENDA_PROJECTION,
					null,
					null,
					null);
		}
		else
		{
			viewLoader =  new CursorLoader(
					this,
					File.CONTENT_URI,
					File.FILE_PROJECTION,
					"agendaID = "+agendaID,
					null,
					null);
		}
		return viewLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(loader.getId() == 120)
		{
			if(data != null && data.moveToFirst())
			{
				tempDateLong = data.getLong(data.getColumnIndex(Agenda.TIME));
				try {
					tempDate = dateFormat.parse(String.valueOf(tempDateLong));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				agendaDateTextView.setText(dateFormatViewing.format(tempDate));
				agendaTitleTextView.setText(data.getString(data.getColumnIndex(Agenda.TITLE)));
				agendaDescriptionTextView.setText(data.getString(data.getColumnIndex(Agenda.DESCRIPTION)));
				this.getLoaderManager().restartLoader(AGENDA_FILE_LOADER, null, this);
			}
		}
		else
		{
			if(data != null && data.moveToFirst())
			{
				fileListAdapter.swapCursor(data);
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
