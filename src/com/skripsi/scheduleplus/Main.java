package com.skripsi.scheduleplus;

import com.skripsi.scheduleplus.AgendaFragment.MasterCallback;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

public class Main extends FragmentActivity implements MasterCallback
{	
	private FragmentTabHost mainTabHost;
	
	protected String INTENT_AGENDA_FORM = "INTENT_AGENDA_FORM";
	
	protected String INTENT_AGENDA_VIEW = "INTENT_AGENDA_VIEW";
		
	Resources res;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		//Inisialisasi container tab
		mainTabHost = (FragmentTabHost) findViewById(R.id.tabHost1);
		//Inisialisasi fragment manager
		mainTabHost.setup(this, getSupportFragmentManager(), R.id.tabFrameLayout);
		mainTabHost.getTabWidget().setDividerDrawable(null);
		
		res = getResources();
		
		//Tab 
		mainTabHost.addTab(mainTabHost.newTabSpec("agenda").setIndicator("", res.getDrawable(R.drawable.agenda)), AgendaFragment.class, null);
		mainTabHost.addTab(mainTabHost.newTabSpec("automsg").setIndicator("", res.getDrawable(R.drawable.smsscheduler)), AutoMsgFragment.class, null);
		mainTabHost.addTab(mainTabHost.newTabSpec("calendar").setIndicator("", res.getDrawable(R.drawable.reminders)), ReminderFragment.class, null);
		mainTabHost.addTab(mainTabHost.newTabSpec("event").setIndicator("", res.getDrawable(R.drawable.event)), EventFragment.class, null);
		mainTabHost.addTab(mainTabHost.newTabSpec("settings").setIndicator("", res.getDrawable(R.drawable.setting)), Settings.class, null);
		
		
	}
	
	public void agendaForm(long agendaID)
	{
		Intent addIntent = new Intent(this, AgendaFormFragment.class);
		addIntent.putExtra(INTENT_AGENDA_FORM, agendaID);
		startActivity(addIntent);
	}
	
	public void agendaView(long agendaID)
	{
		Intent viewIntent = new Intent(this, AgendaViewActivity.class);
		viewIntent.putExtra(INTENT_AGENDA_VIEW, agendaID);
		startActivity(viewIntent);
	}
	
	public void contactList()
	{
		Intent viewIntent = new Intent(this, ContactList.class);
		startActivity(viewIntent);
	}
	
}
