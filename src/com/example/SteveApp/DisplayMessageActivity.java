// I love cookies

package com.example.SteveApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import com.example.qwer.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class DisplayMessageActivity extends Activity {

	private int NUM_THREADS = 36;
	
	private void showDialog(String message)
	{
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int which) {dialog.dismiss(); finish();}
                });
        AlertDialog alert = builder.create();
        alert.show();
	}
	
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Grab typed in string from first screen
		Intent intent = getIntent();
		String universal = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		
		// Message is what's displayed to the user after we steve the contacts
		String message = "";
		
		// Long delimited, formatted string that acts as a 'backup'
		String contactInfo = "";
		
		if (universal.equals(""))
		{
			message = "Please input a value to change all of this phone's contacts to.";
			showDialog(message);
			return;
		}
		else
		{	 
			ContentResolver resolver = getContentResolver();
			 
			// NOW WE BEGIN GRABBING CONTACTS
			// A projection is an array of strings that defines which fields we want to get from our query
			// In our case, we just want contact ID and primary display name
			final String[] projection = {ContactsContract.RawContacts._ID, 
					 ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY};
			 
			// Contact IDs are greater than 0 so this SQLite query grabs every contact essentially
			// A cursor is a list of objects obtained from a query. Data is separated into columns
			Cursor cc = resolver.query(ContactsContract.RawContacts.CONTENT_URI, projection,
					 ContactsContract.RawContacts._ID + " > 0", null, null);
			 
			// CONTACTS HAVE NOW BEEN GRABBED. CC HOLDS ALL OF THEM
			 
			// This is an ArrayList of contactDuos, a bean I created that holds 2 variables
			// The variables are ID and display name
			// After we read all the data we need into this data structure, we will read the list's contents into a file
			ArrayList <contactDuo> contacts = new ArrayList<contactDuo> ();
			 
			int nameFieldColumnIndex1;
			int nameFieldColumnIndex2;
			while (cc.moveToNext())
			{ 
				 // Grab the columns of the 2 relevant fields we found. Grab data from each column. Write into array list
				 nameFieldColumnIndex1 = cc.getColumnIndex(ContactsContract.RawContacts._ID);
				 nameFieldColumnIndex2 = cc.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
				 if (!cc.getString(nameFieldColumnIndex2).equals("null"))
				 {
					 contacts.add(new contactDuo(cc.getString(nameFieldColumnIndex1),
						 cc.getString(nameFieldColumnIndex2)));
				 }
		    }
			 
			// This for loop creates the formatted string that we write to the file
			// Format is id:displayname|id2:displayname2
			for (int i = 0; i < contacts.size(); i++)
			{
				 contactInfo += contacts.get(i).id;
				 contactInfo += ":";
				 contactInfo += contacts.get(i).display_name;
				 contactInfo += "|";
			}
			 
			// Write pristine contact info to file for future reversal
			String ContactLists = "Contact_Lists";
			FileOutputStream fos;
			try
			{
				File file = getBaseContext().getFileStreamPath("Contact_Lists");
				if(file.exists())
				{
					Log.d(Constants.LOG, "File already exists.");
				}
				else
				{
					fos = openFileOutput(ContactLists, Context.MODE_PRIVATE);
					fos.write(contactInfo.getBytes());
				}
			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				message = "Something bad happened.";
				showDialog(message);
				return;
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				message = "Something bad happened.";
				showDialog(message);
				return;
			}
			 
			cc.close();
			
			// Now we need to populate an ArrayList of Threads build with ContactRunnables
			// Initialize it here
			ArrayList <Thread> contactWorkers = new ArrayList <Thread> ();
			
			int workLoad = contacts.size()/NUM_THREADS;
			if (contacts.size() == 0)
			{
				message = "This person has no contacts, which is pretty sad.";
			}
			else if (workLoad < 1)
			{
				contactWorkers.add(new Thread(new ContactRunnable(0, contacts.size(), getContentResolver(), contacts, universal) ) );
			}
			else
			{
				// 8 threads split up X-(X%8) contact changing tasks
				for (int i = 0; i < NUM_THREADS; i++)
				{
					contactWorkers.add(new Thread(new ContactRunnable((i * workLoad), ((i + 1) * workLoad), getContentResolver(), contacts, universal) ) );
				}
				
				// 9th thread picks up the rest if need be
				if ((contacts.size()%NUM_THREADS) > 0)
				{
					contactWorkers.add(new Thread(new ContactRunnable(contacts.size() - ((contacts.size()) % NUM_THREADS), contacts.size(), getContentResolver(), contacts, universal) ) );
				}
			}
			
			// Loop that goes through contactWorkers and runs start() on all of them
			for (int i = 0; i < contactWorkers.size(); i++)
			{
				contactWorkers.get(i).start();
			}
			
			// We exit this loop once every thread has terminated
			int i = 0;
			while (i < contactWorkers.size())
			{
				if (contactWorkers.get(i).getState() == Thread.State.TERMINATED)
				{
					i++;
				}
			}
			
			message = "You have successfully changed all of the contacts on this phone to " + universal;
			showDialog(message);
			return;
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_message, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
