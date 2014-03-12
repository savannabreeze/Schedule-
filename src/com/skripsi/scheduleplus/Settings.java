package com.skripsi.scheduleplus;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Settings extends ListFragment {
	
	private SharedPreferences theSharedPreferences;
	private static boolean loginStatus = false;
	
	ListView settingsListView;
	ArrayAdapter<String> settingsAdapter;
	List<String> settingList = new ArrayList<String>();
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		theSharedPreferences = this.getActivity().getSharedPreferences("SCHEDULEPLUS_PREF", Context.MODE_PRIVATE);
		settingList.add("Account");
		settingList.add("Help");
		settingList.add("About");
	    settingsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, settingList);
	    this.setListAdapter(settingsAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) 
	{
	    View v = inflater.inflate(R.layout.fragment_settings, container, false);
	    loginStatus = theSharedPreferences.getBoolean("LOGGED_IN", false); 
	    return v;
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();
		Log.d("prefBool", String.valueOf(loginStatus));
		loginStatus = theSharedPreferences.getBoolean("LOGGED_IN", false); 
	}
	
	@Override
	public void onListItemClick(ListView listview, View view, int position, long id)
	{
		if(position == 0)
		{
			Intent AccountIntent;
			if(loginStatus == false)
			{
				AccountIntent = new Intent(getActivity(), LogIn.class);
				startActivity(AccountIntent);
			}
			else
			{
				AccountIntent = new Intent(getActivity(), UserAccount.class);
				startActivity(AccountIntent);
			}
		}
		else if(position == 1)
		{
			Intent HelpIntent = new Intent(getActivity(), Help.class);
			startActivity(HelpIntent);
		}
		else if(position == 2)
		{
			Intent AboutIntent = new Intent(getActivity(), About.class);
			startActivity(AboutIntent);
		}
	}
}
