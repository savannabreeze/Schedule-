package com.skripsi.scheduleplus;

import java.net.MalformedURLException;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;

public class LogIn extends Activity {
	
	//deklarasi
	EditText emailLoginEditText;
	EditText passwordLoginEditText;
	
	Button loginButton;
	Button registerButton;
	
	TextView forgotPasswordTextView;
	
	SharedPreferences loginStatus;
	
	AlertDialog.Builder loginAlert;
	
	/**
	 * Mobile Service Client reference
	 */
	private MobileServiceClient mClient;

	/**
	 * Mobile Service Table used to access data
	 */
	private MobileServiceTable<Users> mUsersTable;
	
	public static final String SENDER_ID = "905841047833";
	
	private ProgressDialog progs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_log_in);
			
			// Create the Mobile Service Client instance, using the provided
			// Mobile Service URL and key
			mClient = new MobileServiceClient(
					"https://scheduleplustest.azure-mobile.net/",
					"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
					this);
			
			mUsersTable = mClient.getTable(Users.class);
			
			loginStatus = getSharedPreferences("SCHEDULEPLUS_PREF", MODE_PRIVATE);
			
			//inisialisasi setiap objek di XML
			emailLoginEditText = (EditText) findViewById(R.id.emailLoginEditText);
			passwordLoginEditText = (EditText) findViewById(R.id.passwordLoginEditText);
			loginButton = (Button) findViewById(R.id.loginButton);
			registerButton = (Button) findViewById(R.id.registerButton);
			forgotPasswordTextView = (TextView) findViewById(R.id.forgotPasswordTextView);
			
			
			registerButton.setOnClickListener(new OnClickListener()
			{
	
				@Override
				public void onClick(View v) {
					Intent regIntent = new Intent(LogIn.this, Register.class);
					startActivity(regIntent);
					
				}
				
			});
			
			loginButton.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v) {
					String inputEmail = emailLoginEditText.getText().toString();
					String inputPassword = passwordLoginEditText.getText().toString();
					if(progs == null)
					{
						progs = ProgressDialog.show(LogIn.this, "Log In", "Logging In");
					}
					else
					{
						progs.show();
					}
					mUsersTable.where().field("email").eq(inputEmail).and().field("password").eq(inputPassword).execute(new TableQueryCallback<Users>()
							{

								@Override
								public void onCompleted(List<Users> arg0,
										int arg1, Exception arg2,
										ServiceFilterResponse arg3) {
									Log.d("Query Count", String.valueOf(arg1));
									if(arg0 != null)
									{
										if(!arg0.isEmpty() && arg0.size() == 1)
										{
											Log.d("fetchedEmail", arg0.get(0).get_email());
											Log.d("fetchedPassword", arg0.get(0).get_password());
											Editor loginStatusEditor = loginStatus.edit();
											loginStatusEditor.putBoolean("LOGGED_IN", true);
											loginStatusEditor.putString("LOGGED_IN_ID", arg0.get(0).get_user_id());
											loginStatusEditor.putString("LOGGED_IN_USER", arg0.get(0).get_email());
											loginStatusEditor.putString("LOGGED_IN_PASS", arg0.get(0).get_password());
											loginStatusEditor.putString("LOGGED_IN_FIRST_NAME", arg0.get(0).get_first_name());
											loginStatusEditor.putString("LOGGED_IN_LAST_NAME", arg0.get(0).get_last_name());
											loginStatusEditor.commit();
											if(progs != null)
											{
												progs.hide();
											}
											AlertDialog.Builder forgotPassAlert = new AlertDialog.Builder(LogIn.this);
											forgotPassAlert.setTitle("Success");
											forgotPassAlert.setMessage("You are now logged in");
											forgotPassAlert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which) {
													LogIn.this.finish();
												}
											});
											forgotPassAlert.create().show();
										}
										else if(arg2 != null)
										{
											if(progs != null)
											{
												progs.hide();
											}
											createAndShowDialog(arg2, "Login Exception");
										}
										else if(arg0.size() == 0)
										{
											if(progs != null)
											{
												progs.hide();
											}
											createAndShowDialog("Incorrect Username/Password", "Credentials Error");
										}
										else
										{	if(progs != null)
											{
												progs.hide();
											}								
											createAndShowDialog(arg3.getStatus().toString(), "Error");
										}
									}
									else
									{
										if(progs != null)
										{
											progs.hide();
										}
										createAndShowDialog("Null List", "Null List Returned");
									}
								}
									
								
						
							});				
				}
				
			});
			
			forgotPasswordTextView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent forgotPassIntent = new Intent(LogIn.this, ForgotPasswordActivity.class);
					startActivity(forgotPassIntent);
				}
			});
		}
		catch(MalformedURLException e)
		{
			createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
		}
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log_in, menu);
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
	
	private void createAndShowDialog(Exception exception, String title) {
		Throwable ex = exception;
		if(exception.getCause() != null){
			ex = exception.getCause();
		}
		createAndShowDialog(ex.getMessage(), title);
	}
	
	private void createAndShowDialog(String message, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
			
		});
		builder.create().show();
	}
	
	

}

