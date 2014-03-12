package com.skripsi.scheduleplus;

import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.widget.SimpleCursorAdapter;

import com.skripsi.scheduleplus.R;
import com.skripsi.scheduleplus.R.id;
import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.ReminderLocation;

public class ReminderFragment extends ListFragment implements LoaderCallbacks<Cursor> 
{	
	//identifier loader agenda
	private static int REMINDERLOCATIONLIST_LOADER = 300;
	
	protected Intent viewIntent;
	
	private SharedPreferences theSharedPref;
	private SharedPreferences.Editor theSharedPrefEditor;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		theSharedPref = getActivity().getSharedPreferences("SCHEDULEPLUS_SERVICE", Context.MODE_PRIVATE);
		//inisialisasi simplecursor adapter
		SimpleCursorAdapter agendaAdapter =
				new SimpleCursorAdapter(
						getActivity(),
						R.layout.list_layout,
						(Cursor)null,
						new String[]{ReminderLocation._ID, ReminderLocation.REMINDERLOCATION_TITLE, ReminderLocation.PLACE_NAME},
						new int[]{id.agendaID, id.agendaTitle, id.agendaDate},
						0);
		this.setListAdapter(agendaAdapter);
		getLoaderManager().initLoader(REMINDERLOCATIONLIST_LOADER, null, this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedStateInstance)
	{
		return inflater.inflate(R.layout.fragment_reminder, container, false);
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.reminder_add_menu, menu);
	}

	//ini nanti buat action pas tiap item di menu diklik
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	   switch (item.getItemId()) {
	   case R.id.addLocationReminder: //jika menu add di klik, panggil method Form untuk membuka activity LocationReminderFormFragment
		   Intent addReminderIntent = new Intent(getActivity(), ReminderFormFragment.class);
		   startActivity(addReminderIntent);
		   break;
	   }
	   return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onListItemClick(ListView listview, View view, int position, long id)
	{
		super.onListItemClick(listview, view, position, id);
		TextView idTextView = (TextView) view.findViewById(R.id.agendaID);
		viewIntent = new Intent(getActivity(), ReminderViewActivity.class);
		viewIntent.putExtra("INTENT_REMINDER_VIEW", Long.parseLong(idTextView.getText().toString()));
		Log.d("ReminderFragment", ""+id);
		Log.d("ReminderFragment", idTextView.getText().toString());
		startActivity(viewIntent);
	}
	
	
	

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		if(getActivity() != null)
		{
			return new CursorLoader(getActivity(), ReminderLocation.CONTENT_URI,ReminderLocation.REMINDERLOCATION_PROJECTION, null, null, ReminderLocation.DEFAULT_SORT_ORDER);
		}
		else
		{
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int counter = 0;
		Intent serviceIntent = new Intent(getActivity(), ReminderService.class);
		theSharedPrefEditor = theSharedPref.edit();
		if(getListAdapter() != null)
		{
			//	Periksa kolom status setiap baris data
			if(data.moveToFirst() && data.getCount() != 0)
			{
				Log.d("ReminderFragment", "Masuk If");
				//	Ulang selama data terakhir belum terlewati
				while(!data.isAfterLast())
				{
					Log.d("ReminderFragment", "Masuk Perulangan");
					Log.d("ReminderFragment", ""+data.getInt(data.getColumnIndex(ReminderLocation.STATUS)));
					//	Status belum "done"
					if(data.getInt(data.getColumnIndex(ReminderLocation.STATUS)) == 0)
					{
						//	Jalankan service, lalu keluar loop
						Log.d("ReminderFragment", ""+theSharedPref.getBoolean("SERVICE_STARTED", false));
						//if(theSharedPref.getBoolean("SERVICE_STARTED", false) == false)
						//{
							getActivity().startService(serviceIntent);
							Log.d("ReminderFragment", "Service Started");
						//}
						break;
					}
					counter++;
					data.moveToNext();
				}
			}
			//	Jika counter bernilai sama dengan jumlah data, maka semua reminder sudah "done"
			if(counter == data.getCount())
			{
				getActivity().stopService(serviceIntent);
				//theSharedPrefEditor.putBoolean("SERVICE_STARTED", false);
				//theSharedPrefEditor.apply();
				Log.d("ReminderFragment", "Service Stopped");
			}
			((SimpleCursorAdapter)this.getListAdapter()).swapCursor(data);
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		((SimpleCursorAdapter)this.getListAdapter()).swapCursor(null);
	}
	
	public void onEventMainThread(boolean restart)
	{
		if(restart == true)
		{
			getLoaderManager().restartLoader(REMINDERLOCATIONLIST_LOADER, null, null);
		}
	}
	//method buat call fragment add agenda
	/*private void callAddAgenda()
	{
		Fragment f = AgendaFormFragment.newInstance(-1);
		FragmentTransaction transact = getFragmentManager().beginTransaction();
	    transact.replace(R.id.scheduleplus_fragment_container, f);
	    transact.addToBackStack("scheduleplus");
	    transact.commit();
	}*/
}