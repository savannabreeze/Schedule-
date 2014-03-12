package com.skripsi.scheduleplus;

import java.net.MalformedURLException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;



public class ForgotPasswordActivity extends Activity
{
	EditText forgotPasswordEditText;
	Button forgotPasswordButton;
	
	private MobileServiceClient mClient;
	private MobileServiceTable<Users> mTable;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);
		
		forgotPasswordEditText = (EditText)findViewById(R.id.forgotPassEditText);
		forgotPasswordButton = (Button)findViewById(R.id.forgotPassButton);
		
		forgotPasswordButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				try
				{
					mClient = new MobileServiceClient(
							"https://scheduleplustest.azure-mobile.net/",
							"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
							ForgotPasswordActivity.this);
					
					mTable = mClient.getTable(Users.class);
					
					mTable.where().field("email").eq(forgotPasswordEditText.getText().toString())
					.execute(new TableQueryCallback<Users>()
					{

						@Override
						public void onCompleted(List<Users> arg0, int arg1,
								Exception arg2, ServiceFilterResponse arg3) {
							if(arg0 != null)
							{
								if(arg0.size() == 1)
								{
									doRequestRecovery(arg0.get(0).get_email(), arg0.get(0).get_password());
								}
								else
								{
									createAndShowDialog("Wrong number of row returned", "Error");
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
					
				}
				
			}
		});
	}
	
	private void doRequestRecovery(String email, String password)
	{
		Log.d("ForgotPassword", "requestRecovery called");
		try
		{
			mClient = new MobileServiceClient(
					"https://scheduleplustest.azure-mobile.net/",
					"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
					ForgotPasswordActivity.this);
			
			mTable = mClient.getTable(Users.class);
			
			mTable.parameter("restoreEmail", email)
			.parameter("restoredPassword", password)
			.parameter("restorePass", "true").execute(new TableQueryCallback<Users>()
			{

				@Override
				public void onCompleted(
						List<Users> arg0, int arg1,
						Exception arg2,
						ServiceFilterResponse arg3) {
					if(arg3 != null && arg3.getStatus().getStatusCode() == 200)
					{
						AlertDialog.Builder forgotPassAlert = new AlertDialog.Builder(ForgotPasswordActivity.this);
						forgotPassAlert.setTitle("Success");
						forgotPassAlert.setMessage("Your password will be sent to your email");
						forgotPassAlert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ForgotPasswordActivity.this.finish();
							}
						});
						forgotPassAlert.create().show();
					}
					else if(arg3 != null && arg3.getStatus().getStatusCode() == 404)
					{
						AlertDialog.Builder forgotPassAlert = new AlertDialog.Builder(ForgotPasswordActivity.this);
						forgotPassAlert.setTitle("Not Found");
						forgotPassAlert.setMessage("The email you entered is not found in our database");
						forgotPassAlert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});
						forgotPassAlert.create().show();
					}
					else if(arg2 != null)
					{
						createAndShowDialog(arg2, "Error");
					}
					else
					{
						createAndShowDialog("Unknown error has occures", "Error");
					}
				}

				
				
				
			});
		}
		catch(MalformedURLException e)
		{
			
		}
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
		AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.create().show();
	}
}
