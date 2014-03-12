package com.skripsi.scheduleplus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ListView;
import com.skripsi.scheduleplus.CalendarAdapter;
import com.skripsi.scheduleplus.R;
import com.skripsi.scheduleplus.dataprovider.SchedulePlusContract.Agenda;



public class AgendaFragment extends Fragment implements LoaderCallbacks<Cursor> 
{	
	protected ListView theListView;
	protected TextView noAgendaTextView;
	private AgendaListCursorAdapter agendaAdapter;
	public GregorianCalendar month, itemmonth;// calendar instances.
	protected LinearLayout rLayout;
	public CalendarAdapter adapter;// adapter instance\
	public Handler handler;
	private Intent viewIntent;
	View selectedCalGrid;
	
	
	//interface yang memanggiil method di main.java. Dipakai untuk memanggil activity lain karena main.java sebagai base activity disini.
	public interface MasterCallback
	{
		void agendaForm(long agendaID);
		void agendaView(long agendaID);
	}
	//identifier loader agenda
	protected final static int AGENDALIST_LOADER = 100;
	
	private String selectedDate;
	private long longSelectedDate;
	
	private static final String DATE_FORMAT = "yyyyMMdd";	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
	private Date theDate;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedStateInstance)
	{
		return inflater.inflate(R.layout.fragment_agenda, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		/*
		 * inisialisasi objek dalam layout
		 */
		theListView = (ListView)getView().findViewById(R.id.agendaFragmentList);
		rLayout = (LinearLayout)getView().findViewById(R.id.text);
		month = (GregorianCalendar) GregorianCalendar.getInstance();
		itemmonth = (GregorianCalendar) month.clone();
		adapter = new CalendarAdapter(getActivity(), month);
		noAgendaTextView = (TextView)getView().findViewById(R.id.noAgendaTextView);
		
		/*
		 *Mengambil tanggal terkini 
		 */
		selectedDate = dateFormat.format(month.getTime());
		longSelectedDate = Long.parseLong(dateFormat.format(new Date()));
		
		/*
		 * inisialisasi gridView dan Adapternya
		 */
		GridView gridview = (GridView) getView().findViewById(R.id.gridview);
		gridview.setAdapter(adapter);
		
		/*
		 * Judul Kalender yang menampilkan bulan dan tahun
		 */
		TextView title = (TextView) getView().findViewById(R.id.title);
		title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));

		RelativeLayout previous = (RelativeLayout) getView().findViewById(R.id.prevDate);

		previous.setOnClickListener(new OnClickListener() 
		{

			@Override
			public void onClick(View v) 
			{
				setPreviousMonth();
				refreshCalendar();
			}
		});

		RelativeLayout next = (RelativeLayout) getView().findViewById(R.id.nextDate);
		next.setOnClickListener(new OnClickListener() 
		{

			@Override
			public void onClick(View v) 
			{
				setNextMonth();
				refreshCalendar();

			}
		});
		
		/*
		 *inisialisasi simplecursor adapter 
		 */
		agendaAdapter = new AgendaListCursorAdapter(getActivity(), (Cursor)null, 0);
		theListView.setAdapter(agendaAdapter);
		
		
		/*
		 * OnItemClickListener untuk setiap item dalam list agenda
		 */
		theListView.setOnItemClickListener(new OnItemClickListener()
				{
					/*
					 * Panggil Activity AgendaViewActivity. Dalam put extra terdapat ID agenda yang dipilih
					 */
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) 
					{
						viewIntent = new Intent(getActivity(), AgendaViewActivity.class);
						viewIntent.putExtra("INTENT_AGENDA_VIEW",id);
						startActivity(viewIntent);							
					}
							
				});
				
		gridview.setOnItemClickListener(new OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) 
			{
				// hapus view sebelumnya jika ada
				if (((LinearLayout) rLayout).getChildCount() > 0) 
				{
					((LinearLayout) rLayout).removeAllViews();
				}
				((CalendarAdapter) parent.getAdapter()).setSelected(v);
				String selectedGridDate = CalendarAdapter.dayString
						.get(position);
				//selectedDate = selectedGridDate;
				String[] separatedTime = selectedGridDate.split("-");
				String gridvalueString = separatedTime[2].replaceFirst("^0*",
						"");// taking last part of date. ie; 2 from 2012-12-02.
				int gridvalue = Integer.parseInt(gridvalueString);
				// navigate to next or previous month on clicking offdays.
				if ((gridvalue > 10) && (position < 8)) 
				{
					setPreviousMonth();
					refreshCalendar();
				} 
				else if ((gridvalue < 7) && (position > 28)) 
				{
					setNextMonth();
					refreshCalendar();
				}
				((CalendarAdapter) parent.getAdapter()).setSelected(v);
				
				try
				{
					theDate = dateFormat.parse(separatedTime[0]+separatedTime[1]+separatedTime[2]);
				}
				catch(ParseException e)
				{
					e.printStackTrace();
				}
				longSelectedDate = Long.parseLong(dateFormat.format(theDate));
				
				Log.d("hasilGridDate",selectedDate);
				
				//AgendaFragment.this.onStart();
				getLoaderManager().restartLoader(AGENDALIST_LOADER, null, AgendaFragment.this);
			}
			
		});
		
		
				
	}	
	
	@Override
	public void onStart()
	{
		super.onStart();
		if(getLoaderManager().getLoader(AGENDALIST_LOADER) != null)
		{
			getLoaderManager().restartLoader(AGENDALIST_LOADER, null, this);

		}
		else
		{
			getLoaderManager().initLoader(AGENDALIST_LOADER, null, this);
		}
		refreshCalendar();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.agenda_list_menu, menu);
	}

	//ini nanti buat action pas tiap item di menu diklik
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	   switch (item.getItemId()) {
	   case R.id.addAgenda: //jika menu add di klik, panggil method agendaForm untuk membuka activity AgendaFormFragment
	      ((MasterCallback)getActivity()).agendaForm(-1);
	      break;
	   }
	   return super.onOptionsItemSelected(item);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		String where = Agenda.TIME+" = "+longSelectedDate;		
		return new CursorLoader(getActivity(), Agenda.CONTENT_URI, Agenda.AGENDA_PROJECTION, where, null, Agenda.DEFAULT_SORT_ORDER);
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(data != null && data.moveToFirst())
		{
			Log.d("RefreshTanggal","Kalo kesini berarti swap jalan");
			agendaAdapter.swapCursor(data);
			noAgendaTextView.setVisibility(View.GONE);
			theListView.setVisibility(View.VISIBLE);		
		}
		else
		{
			
			Log.d("RefreshTanggal","Kalo kesini berarti null");
			agendaAdapter.swapCursor(null);
			noAgendaTextView.setVisibility(View.VISIBLE);
			theListView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d("ResetTanggal","reset dipanggil");
		agendaAdapter.swapCursor(null);
	}
	
	protected void setNextMonth() 
	{
		if (month.get(GregorianCalendar.MONTH) == month
				.getActualMaximum(GregorianCalendar.MONTH)) 
		{
			month.set((month.get(GregorianCalendar.YEAR) + 1),
					month.getActualMinimum(GregorianCalendar.MONTH), 1);
		} 
		else 
		{
			month.set(GregorianCalendar.MONTH,
					month.get(GregorianCalendar.MONTH) + 1);
		}

	}

	protected void setPreviousMonth() 
	{
		if (month.get(GregorianCalendar.MONTH) == month
				.getActualMinimum(GregorianCalendar.MONTH)) {
			month.set((month.get(GregorianCalendar.YEAR) - 1),
					month.getActualMaximum(GregorianCalendar.MONTH), 1);
		} else {
			month.set(GregorianCalendar.MONTH,
					month.get(GregorianCalendar.MONTH) - 1);
		}

	}

	protected void showToast(String string) {
		Toast.makeText(getActivity(), string, Toast.LENGTH_SHORT).show();

	}

	public void refreshCalendar() {
		TextView title = (TextView) getView().findViewById(R.id.title);

		adapter.refreshDays();
		adapter.notifyDataSetChanged();
		//handler.post(calendarUpdater); // generate some calendar items

		title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
	}

}
