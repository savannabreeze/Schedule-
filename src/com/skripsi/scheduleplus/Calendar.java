package com.skripsi.scheduleplus;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Calendar extends Fragment {
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) 
	{
	    View v = inflater.inflate(R.layout.fragment_calendar, container, false);
	    TextView tv = (TextView) v.findViewById(R.id.textView3);
	    tv.setText(this.getTag() + " Content");
	    return v;
	}
}
