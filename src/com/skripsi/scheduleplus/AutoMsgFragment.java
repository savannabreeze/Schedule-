package com.skripsi.scheduleplus;



import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.skripsi.scheduleplus.R;
import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.SMS;

public class AutoMsgFragment extends ListFragment implements LoaderCallbacks<Cursor> 
{	
	
	//interface yang memanggiil method di main.java. Dipakai untuk memanggil activity lain karena main.java sebagai base activity disini.
	interface MasterCallback
	{
		void SMSForm(long SMSID);
		void SMSView(long SMSID);
	}
	//identifier loader sms
	protected static int SMSLIST_LOADER = 200;
	
	private Intent viewIntent;
	
	private String INTENT_SMS_FORM = "INTENT_SMS_FORM";
	
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
		//inisialisasi simplecursor adapter
		AutoMsgListCursorAdapter SMSAdapter = new AutoMsgListCursorAdapter(getActivity(), (Cursor)null, 0);
		this.setListAdapter(SMSAdapter);
		getLoaderManager().initLoader(SMSLIST_LOADER, null, this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedStateInstance)
	{
		return inflater.inflate(R.layout.fragment_automsg, container, false);
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.sms_add_menu, menu);
	}

	//ini nanti buat action pas tiap item di menu diklik
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	   switch (item.getItemId()) {
	   case R.id.addSMS: //jika menu add di klik, panggil method SMSForm untuk membuka activity AgendaFormFragment
		   Intent addIntent = new Intent(getActivity(), AutoMsgFormFragment.class);
			addIntent.putExtra(INTENT_SMS_FORM, -1);
			startActivity(addIntent);
	      break;
	   }
	   return super.onOptionsItemSelected(item);
	}
	
	
	
	@Override
	public void onListItemClick(ListView listview, View view, int position, long id)
	{
		super.onListItemClick(listview, view, position, id);
		//((MasterCallback)getActivity()).SMSView(id);
		viewIntent = new Intent(getActivity(), AutoMsgViewActivity.class);
		viewIntent.putExtra("INTENT_SMS_VIEW",id);
		startActivity(viewIntent);
	}
	
	
	

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		if(getActivity() != null)
		{
			return new CursorLoader(getActivity(), SMS.CONTENT_URI, SMS.SMS_PROJECTION, null, null, SMS.DEFAULT_SORT_ORDER);
		}
		else
		{
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(getListAdapter() != null)
		{
			((AutoMsgListCursorAdapter)this.getListAdapter()).swapCursor(data);
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		((AutoMsgListCursorAdapter)this.getListAdapter()).swapCursor(null);
	}
	
	public void onEventMainThread(boolean restart)
	{
		if(restart == true)
		{
			getLoaderManager().restartLoader(SMSLIST_LOADER, null, null);
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