package com.skripsi.scheduleplus;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.skripsi.scheduleplus.R;
import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.File;

public class FileListFormAdapter extends SimpleCursorAdapter
{
	private AlertDialog.Builder deleteConfirm;
	private Context theContext;
	private int theLayout;
	
	public FileListFormAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		theContext = context;
		theLayout = layout;
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

	    Cursor c = getCursor();

	    final LayoutInflater inflater = LayoutInflater.from(theContext);
	    View v = inflater.inflate(theLayout, parent, false);

	    int agendaIdCol = c.getColumnIndex("agendaID");
	    String agendaId = c.getString(agendaIdCol);
	    
	    int fileDirCol = c.getColumnIndex("fileLocation");
	    String fileDir = c.getString(fileDirCol);
	    
	    int fileNameCol = c.getColumnIndex("fileName");
	    String fileName = c.getString(fileNameCol);

	    TextView fileIdFormTextView = (TextView)v.findViewById(R.id.fileIdFormTextView);
		TextView fileNameFormTextView = (TextView)v.findViewById(R.id.fileNameFormTextView);
		TextView fileDirFormTextView = (TextView)v.findViewById(R.id.fileDirFormTextView);
		if (fileIdFormTextView != null) {
	    	fileIdFormTextView.setText(agendaId);
	    }
	    
	    if (fileNameFormTextView != null) {
	    	fileNameFormTextView.setText(fileName);
	    }
	    
	    if (fileDirFormTextView != null) {
	    	fileDirFormTextView.setText(fileDir);
	    }
	    return v;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{		
		final TextView fileIdFormTextView = (TextView)view.findViewById(R.id.fileIdFormTextView);
		final TextView fileNameFormTextView = (TextView)view.findViewById(R.id.fileNameFormTextView);
		final TextView fileDirFormTextView = (TextView)view.findViewById(R.id.fileDirFormTextView);
		
		final Button fileDeleteButton = (Button)view.findViewById(R.id.fileDeleteButton);
		
		fileIdFormTextView.setText(cursor.getString(0));
		fileNameFormTextView.setText(cursor.getString(2));
		fileDirFormTextView.setText(cursor.getString(3));
		
		fileDeleteButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) 
			{
				final long fileId = Long.parseLong(fileIdFormTextView.getText().toString());
				deleteConfirm = new AlertDialog.Builder(theContext);
				deleteConfirm.setTitle(R.string.Delete_Title_File);
				deleteConfirm.setMessage(R.string.Delete_Text_File);
				deleteConfirm.setPositiveButton(R.string.Yes, 
						new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which) {
						final ContentResolver resolver = theContext.getContentResolver();
					      new Thread(new Runnable() { //sintaks diwabah ini berjalan di thread lain		         
					         public void run() {
					            Uri delUri = ContentUris.withAppendedId(File.CONTENT_URI, fileId);
					            int resCount = resolver.delete(delUri, null, null);
					            if (resCount == 0) //jika resCount = 0, maka penghapusan gagal. sampaikan pesan kegagalan melalui eventBus
					            {
					            	Log.d("FileDelete","File deletion successful");
					            }
					            else
					            {
					            	Log.d("FileDelete","File deletion failed");
					            }
					         }
					      }).start();
						
					}
					
				});
				deleteConfirm.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						
					}
					
					
				});
				deleteConfirm.create().show();
				
			}
			
		});
	}
}
