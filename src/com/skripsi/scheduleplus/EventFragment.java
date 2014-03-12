package com.skripsi.scheduleplus;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import com.skripsi.scheduleplus.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;

public class EventFragment extends ListFragment 
{
	/* Mobile Service Azure Object
	 */
	private MobileServiceClient mClient;
	private MobileServiceTable<Event> mEventTable;
		
	private EventListAdapter mAdapter;
	
	SharedPreferences schedulePlusSP;
	boolean LoginStatus;
	
	private List<Event> loadedEvent;
	
	ProgressDialog progs;
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		schedulePlusSP = getActivity().getSharedPreferences("SCHEDULEPLUS_PREF",Context.MODE_PRIVATE);
		
		loadedEvent = new ArrayList<Event>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) 
	{
	    View v = inflater.inflate(R.layout.fragment_event, container, false);
	    return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		Log.d("EventFragment", "logged in: "+schedulePlusSP.getBoolean("LOGGED_IN", false));
		
			
		
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		LoginStatus = schedulePlusSP.getBoolean("LOGGED_IN", false);
		try
		{
			mClient = new MobileServiceClient(
					"https://scheduleplustest.azure-mobile.net/",
					"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
					getActivity());
			progs = ProgressDialog.show(getActivity(), "Loading", "Loading Data, Please Wait");
			
			mEventTable = mClient.getTable(Event.class);
			
			mEventTable.where().field("userId").eq(schedulePlusSP.getString("LOGGED_IN_ID", null))
			.execute(new TableQueryCallback<Event>() {
				
				@Override
				public void onCompleted(List<Event> arg0, int arg1, Exception arg2,
						ServiceFilterResponse arg3) {
					if(arg0 != null)
					{
						if(arg0.size() > 0)
						{
							loadedEvent.clear();
							for(int a = 0; a < arg0.size(); a++)
							{
								loadedEvent.add(arg0.get(a));
							}
							Log.d("EventFragment", "Punya Event");
							checkInvitedEvent(arg0.get(0).get_userId());
						}
					}
					else if(arg2 != null)
					{
						createAndShowDialog(arg2, "Error");
					}
				}
			});
			
		}
		catch(MalformedURLException e)
		{
			createAndShowDialog(e, "Error");
		}
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		loadedEvent.clear();
		setListAdapter(null);
	}
	
	/* Method untuk set/notify adapter
	 * 
	 */
	private void adapterSet()
	{
		mAdapter = new EventListAdapter(getActivity(), R.layout.list_layout, loadedEvent);
		setListAdapter(mAdapter);
	}
	
	/* Method untuk memeriksa event yang diterima
	 * 
	 */
	private void checkInvitedEvent(String id)
	{
		Log.d("EventFragment", "Masuk check invited event");
		mEventTable.parameter("userId", schedulePlusSP.getString("LOGGED_IN_ID", null))
		.parameter("invitedEvent", "true").execute(new TableQueryCallback<Event>() {
			
			@Override
			public void onCompleted(List<Event> arg0, int arg1, Exception arg2,
					ServiceFilterResponse arg3) {
				if(arg0 != null)
				{
					if(arg0.size() > 0)
					{
						Log.d("EventFragment", "Ada invited event");
						for(int y = 0; y < arg0.size(); y++)
						{
							loadedEvent.add(arg0.get(y));
						}
						if(progs != null)
						{
							progs.dismiss();
						}
						adapterSet();
					}
					else
					{
						if(progs != null)
						{
							progs.dismiss();
						}
						Log.d("EventFragment", "ga ada invited event");
						adapterSet();
					}
				}
				else if(arg2 != null)
				{
					if(progs != null)
					{
						progs.dismiss();
					}
					createAndShowDialog(arg2, "Error Loading Invited Event");
					adapterSet();
				}
				else
				{
					if(progs != null)
					{
						progs.dismiss();
					}
					createAndShowDialog("Unkown error occured while loading invited event", "Error");
				}
			}
		});
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.contact_list_menu, menu);
	}

	//ini nanti buat action pas tiap item di menu diklik
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	   switch (item.getItemId()) {
	   case R.id.addEvent:
		   if(LoginStatus == true)
		   {
			    Intent addEventIntent = new Intent(getActivity(), EventAdd.class);
		   		addEventIntent.putExtra("formMode", 1);
		   		startActivity(addEventIntent);
		   }
		   else
		   {
			   createAndShowDialog("You must be logged in to add an event", "Error");
		   }
		   break;
	   case R.id.contactList: //jika menu add di klik, panggil method agendaForm untuk membuka activity AgendaFormFragment
		   if(LoginStatus == true)
		   {
			   Intent viewIntent = new Intent(getActivity(), ContactList.class);
			   startActivity(viewIntent);	
		   }
		   else
		   {
			   createAndShowDialog("You must be logged in to view your contact", "Error");
		   }
	      break;
	   case R.id.requestList:
		  if(LoginStatus == true)
		  {
			   Intent reqIntent = new Intent(getActivity(), RequestActivity.class);
			   startActivity(reqIntent);
		  }
		  else
		  {
			  createAndShowDialog("You must be logged in to view requests", "Error");
		  }
		   break;
	   }
	   return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		TextView idTV = (TextView)v.findViewById(R.id.agendaID);
		Intent viewIntent = new Intent(getActivity(), EventViewActivity.class);
		viewIntent.putExtra("eventId", idTV.getText().toString());
		startActivity(viewIntent);
		
	}
	
	
	
	private void createAndShowDialog(Exception exception, String title) {
		Throwable ex = exception;
		if(exception.getCause() != null){
			ex = exception.getCause();
		}
		createAndShowDialog(ex.getMessage(), title);
	}
	
	private void createAndShowDialog(String message, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message);
		builder.setTitle(title);
		builder.create().show();
	}
}
