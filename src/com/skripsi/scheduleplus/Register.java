package com.skripsi.scheduleplus;

import java.net.MalformedURLException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;

public class Register extends Activity
{
	EditText registerUsernameEditText;
	EditText registerPasswordEditText;
	EditText registerEmailEditText;
	EditText registerFirstNameEditText;
	EditText registerLastNameEditText;
	EditText confirmPasswordEditText;
	
	Button registerButton;
	
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
	public void onCreate(Bundle savedInstanceState)
	{
		try 
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_register);
		
			// Create the Mobile Service Client instance, using the provided
			// Mobile Service URL and key
			mClient = new MobileServiceClient(
					"https://scheduleplustest.azure-mobile.net/",
					"icZObaEYpaeKMdjSJQlItPpkRmDjOu29",
					this);

			// Get the Mobile Service Table instance to use
			mUsersTable = mClient.getTable(Users.class);		
		
			registerPasswordEditText = (EditText)findViewById(R.id.registerPasswordEditText);
			registerEmailEditText = (EditText)findViewById(R.id.registerEmailEditText);
			registerFirstNameEditText = (EditText)findViewById(R.id.registerFirstNameEditText);
			registerLastNameEditText = (EditText)findViewById(R.id.registerLastNameEditText);
			confirmPasswordEditText = (EditText)findViewById(R.id.confirmPasswordEditText);
		
			registerButton = (Button)findViewById(R.id.registerButton);
		
		
			
		
			registerButton.setOnClickListener(new OnClickListener()
			{
	
				@Override
				public void onClick(View v) {
					if(registerPasswordEditText.getText().toString().equals("")|| registerEmailEditText.getText().toString().equals("") 
							|| registerFirstNameEditText.getText().toString().equals("")
							|| confirmPasswordEditText.getText().toString().equals("") 
							|| registerLastNameEditText.getText().toString().equals(""))
					{
						createAndShowDialog("All field must be filled", "Warning");
					}
					else if(!registerPasswordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString()) )
					{
						createAndShowDialog("Entered password do not match", "Warning");
					}
					else
					{
						if(progs == null)
						{
							progs = ProgressDialog.show(Register.this, "Register", "Registering.");
						}
						else
						{
							progs.show();
						}
						String newFirstName = registerFirstNameEditText.getText().toString();
						String newLastName = registerLastNameEditText.getText().toString();
						String newPassword = registerPasswordEditText.getText().toString();
						String newEmail = registerEmailEditText.getText().toString();
						Users newUsers = new Users(); 
						newUsers.set_password(newPassword);
						newUsers.set_first_name(newFirstName);
						newUsers.set_last_name(newLastName);
						newUsers.set_email(newEmail);
						mUsersTable.insert(newUsers, new TableOperationCallback<Users>()
						{
	
							@Override
							public void onCompleted(Users arg0, Exception arg1,
									ServiceFilterResponse arg2) {
								if(arg1 == null)
								{
									if(progs != null)
									{
										progs.dismiss();
									}
									AlertDialog.Builder forgotPassAlert = new AlertDialog.Builder(Register.this);
									forgotPassAlert.setTitle("Success");
									forgotPassAlert.setMessage("Registration successful");
									forgotPassAlert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											Register.this.finish();
										}
									});
									forgotPassAlert.create().show();
								}
								else
								{
									createAndShowDialog(arg1,"Registration Fail");
								}
								
								
							}
							
						});
					}
					
				}
				
			});
	}catch(MalformedURLException e)
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
	
