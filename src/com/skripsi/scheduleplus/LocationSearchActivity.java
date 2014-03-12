package com.skripsi.scheduleplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
 
import android.os.AsyncTask;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.util.Log;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LocationSearchActivity extends ListActivity
{
	Button mBtnFind;
	//GoogleMap mMap;
	EditText etPlace;
	LocationAdapter locationAdapter;
	// searchView
	SearchView locationSearchView;
	
	String nearbyURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
	String location;
	String radius = "radius=1000";
	String sensor = "sensor=true";
	String key = "key=AIzaSyCWmRBzwY3gPbav6cuIZxaWJTEReZ83Xnk";
	double latitude;
	double longitude;
	GPSTracker tracker;
	ProgressDialog progs;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.location_search_activity);
       
       getActionBar().setDisplayHomeAsUpEnabled(true);
       
       tracker = new GPSTracker(this);
        
       locationSearchView = (SearchView) findViewById(R.id.locationSearchView);
 
       locationSearchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit (String query) {
            	// Getting the place entered 
				String location = query;
 
                if(location==null || location.equals("")){
                    Toast.makeText(getBaseContext(), "No Place is entered", Toast.LENGTH_SHORT).show();
                    return false;
                }
 
                String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
 
                try {
                    // encoding special characters like space in the user input place
                    location = URLEncoder.encode(location, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
 
                String address = "query=" + location;
                 
                String sensor = "sensor=false";
                 
                // url , from where the geocoding data is fetched
                url = url + address + "&" + sensor + "&" + key;
                //url = url + address + "&" + sensor + "&" + key;
 
                // Instantiating DownloadTask to get places from Google Geocoding service
                // in a non-ui thread
                DownloadTask downloadTask = new DownloadTask();
 
                // Start downloading the geocoding places
                downloadTask.execute(url);
                
                return true;
            }

			@Override
			public boolean onQueryTextChange(String newText) {
				// TODO Auto-generated method stub
				return false;
			}
            
        });
       
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	getNearby();
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    }
    
    @Override
    protected void onListItemClick(ListView lv, View v, int pos, long id)
    {
    	TextView nameTV = (TextView)v.findViewById(R.id.name_location);
		TextView latitudeTV = (TextView)v.findViewById(R.id.latitude_location);
		TextView longitudeTV = (TextView)v.findViewById(R.id.longitude_location);
		Intent returnIntent = new Intent();
		returnIntent.putExtra("name", nameTV.getText().toString());
		returnIntent.putExtra("latitude", Double.parseDouble(latitudeTV.getText().toString()));
		returnIntent.putExtra("longitude", Double.parseDouble(longitudeTV.getText().toString()));
		setResult(RESULT_OK, returnIntent);
		tracker.stopUsingGPS();
		finish();
    }
 
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
 
            // Connecting to url
            urlConnection.connect();
 
            // Reading data from url
            iStream = urlConnection.getInputStream();
 
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
 
            StringBuffer sb = new StringBuffer();
 
            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }
 
            data = sb.toString();
            br.close();
            Log.d("br disconnected", "br connection close");		
        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
            Log.d("disconnected", "connection close");
        }
 
        return data;
    }
    /** A class, to download Places from Geocoding webservice */
    private class DownloadTask extends AsyncTask<String, Integer, String>{
 
        String data = null;
 
        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
 
        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){
 
            // Instantiating ParserTask which parses the json data from Geocoding webservice
            // in a non-ui thread
            ParserTask parserTask = new ParserTask();
 
            // Start parsing the places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }
    }
 
    /** A class to parse the Geocoding Places in non-ui thread */
    class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{
 
        JSONObject jObject;
 
        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String,String>> doInBackground(String... jsonData) {
 
            List<HashMap<String, String>> places = null;
            JSONParser parser = new JSONParser();
 
            try{
                jObject = new JSONObject(jsonData[0]);
 
                /** Getting the parsed data as a an ArrayList */
                places = parser.parse(jObject);
                Log.d("parse result", String.valueOf(places.size()));
 
            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }
        
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list)
        {
        	if(progs != null)
        	{
        		progs.hide();
        	}
        	if(list != null)
        	{
        		locationAdapter = new LocationAdapter(LocationSearchActivity.this, R.layout.location_list_layout, list);
        		setListAdapter(locationAdapter);
        	}
        	else
        	{
        		createAndShowDialog("Unable to retrieve data", "Error");
        	}
        }
 
    }
    
    //	Method untuk mencari place di lokasi sekitar pengguna
    private void getNearby()
    {
    	if(progs == null)
    	{
    		progs = ProgressDialog.show(this, "Loading", "Loading Data, Please Wait");
    	}
    	else
    	{
    		progs.show();
    	}
    	//	Mengambil latitude dan longitude pengguna
    	latitude = tracker.getLatitude();
    	longitude = tracker.getLongitude();
    	
    	location = "location="+latitude+","+longitude;
    	nearbyURL = nearbyURL + location + "&" + radius + "&" + sensor + "&" + key;
    	
    	DownloadTask download = new DownloadTask();
    	download.execute(nearbyURL);    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case android.R.id.home:
    		this.finish();
    		break;
    	}
    	return true;
    }
    
    @Override
    public void onStop()
    {
    	super.onStop();
    	tracker.stopUsingGPS();
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	tracker.stopUsingGPS();
    }
    
    @Override
	public void onDestroy()
	{
		super.onDestroy();
		if(progs != null)
		{
			progs.dismiss();
	
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

