package com.skripsi.scheduleplus;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class LocationAdapter extends ArrayAdapter<HashMap<String, String>>{
	
	Context locationContext;
	int searchResult;
	private List<HashMap<String, String>> location;

	public LocationAdapter(Context context, int textViewResourceId, List<HashMap<String, String>> locationlist) {
		super(context, textViewResourceId, locationlist);
		
		locationContext = context;
		searchResult = textViewResourceId;
		location = locationlist;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		HashMap<String, String> locationhash = location.get(position);
		
		if(convertView == null)
		{
			LayoutInflater mInflater = ((Activity)locationContext).getLayoutInflater();
			convertView = mInflater.inflate(searchResult, parent, false );
		}
		
		// Getting address of the place
		String placeAddr = locationhash.get("formatted_address");
		// Getting latitude of the place
        double lat = Double.parseDouble(locationhash.get("lat"));
        
        // Getting longitude of the place
        double lng = Double.parseDouble(locationhash.get("lng"));
        
        // Getting name of the place
        String placeNm = locationhash.get("name");
		
		TextView address = (TextView)convertView.findViewById(R.id.address_location);
		TextView latitude = (TextView)convertView.findViewById(R.id.latitude_location);
		TextView longitude = (TextView)convertView.findViewById(R.id.longitude_location);
		TextView placeName = (TextView)convertView.findViewById(R.id.name_location);
		
		address.setText(placeAddr);
		latitude.setText(String.valueOf(lat));
		longitude.setText(String.valueOf(lng));
		placeName.setText(placeNm);
		
		return convertView;
	}

}