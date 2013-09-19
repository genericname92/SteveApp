package com.SteveApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import com.SteveApp.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class DisplayMessageActivity extends Activity
{
	private ProgressBar pb;
    private TextView tv;
    private int prg = 0;
    private ArrayList <contactDuo> contacts;
    private ContentResolver resolver;
    private String universal;
	
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
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	// Grab typed in string from first screen
     	Intent intent = getIntent();
     	universal = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
     	String message = "";
     	if (universal.equals(""))
     	{
     		message = "Please input a value to change all of this phone's contacts to.";
     		showDialog(message);
     		return;
     	}
        
        setContentView(R.layout.activity_display_message);

        pb = (ProgressBar) findViewById(R.id.pbId);
        tv = (TextView) findViewById(R.id.tvId);
        
    	// Create folder in external storage for us to store things in
     	// Check if SD card is mounted
     	if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
     	{
     		File Dir = new File(android.os.Environment.getExternalStorageDirectory(), "SteveApp");
     		if (!Dir.exists()) // if directory is not here
     		{
     			Dir.mkdirs(); // make directory
     		}
     	}
     		
     	// Long delimited, formatted string that acts as a 'backup'
     	String contactInfo = "";
     		
		resolver = getContentResolver();

		// NOW WE BEGIN GRABBING CONTACTS
		// A projection is an array of strings that defines which fields we want
		// to get from our query
		// In our case, we just want contact ID and primary display name
		final String[] projection = { ContactsContract.RawContacts._ID,
				ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY };

		// Contact IDs are greater than 0 so this SQLite query grabs every
		// contact essentially
		// A cursor is a list of objects obtained from a query. Data is
		// separated into columns
		Cursor cc = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
				projection, ContactsContract.RawContacts._ID + " > 0", null,
				null);

		// CONTACTS HAVE NOW BEEN GRABBED. CC HOLDS ALL OF THEM

		// This is an ArrayList of contactDuos, a bean I created that holds 2
		// variables
		// The variables are ID and display name
		// After we read all the data we need into this data structure, we will
		// read the list's contents into a file
		contacts = new ArrayList<contactDuo>();

		int nameFieldColumnIndex1;
		int nameFieldColumnIndex2;
		while (cc.moveToNext()) {
			// Grab the columns of the 2 relevant fields we found. Grab data
			// from each column. Write into array list
			nameFieldColumnIndex1 = cc
					.getColumnIndex(ContactsContract.RawContacts._ID);
			nameFieldColumnIndex2 = cc
					.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
			if (!cc.getString(nameFieldColumnIndex2).equals("null")) {
				contacts.add(new contactDuo(
						cc.getString(nameFieldColumnIndex1), cc
								.getString(nameFieldColumnIndex2)));
			}
		}
		cc.close();

		// This for loop creates the formatted string that we write to the file
		// Format is id:displayname|id2:displayname2
		for (int i = 0; i < contacts.size(); i++) {
			contactInfo += contacts.get(i).id;
			contactInfo += ":";
			contactInfo += contacts.get(i).display_name;
			contactInfo += "|";
		}
		pb.setMax(contacts.size());

		// Write pristine contact info to file for future reversal
		// Try external first
		if (Util.isExternalStorageWritable()) {
			File file = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/SteveApp/", "backup_contacts.txt");
			if (!file.exists()) {
				try {
					FileWriter fWriter = new FileWriter(Environment
							.getExternalStorageDirectory().getPath()
							+ "/SteveApp/backup_contacts.txt");
					fWriter.write(contactInfo);
					fWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
					message = "Something bad happened.";
					showDialog(message);
					return;
				}
			}
		}
		// Now internal
		else {
			String ContactLists = "Contact_Lists";
			FileOutputStream fos;
			try {
				File file = getBaseContext().getFileStreamPath("Contact_Lists");
				if (!file.exists()) {
					fos = openFileOutput(ContactLists, Context.MODE_PRIVATE);
					fos.write(contactInfo.getBytes());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				message = "Something bad happened.";
				showDialog(message);
				return;
			} catch (IOException e) {
				e.printStackTrace();
				message = "Something bad happened.";
				showDialog(message);
				return;
			}
		}
        new Thread(myThread).start();
    }

    private Runnable myThread = new Runnable()
    { 
        @SuppressLint("InlinedApi")
		@Override
        public void run() 
        {
        	// This loop changes all of the contacts
    		for (int i = 0; i < contacts.size(); i++)
    		{	 
    			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
    			// DERPY UPDATE DONE HERE
    			ops.add(ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
    				.withSelection(ContactsContract.RawContacts._ID + " LIKE ?", new String[] {contacts.get(i).id})
    				.withValue(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, universal)
    				.build());
    						 
    			try
    			{
    				resolver.applyBatch(ContactsContract.AUTHORITY, ops);
    				hnd.sendMessage(hnd.obtainMessage());
    			}
    			catch (RemoteException e)
    			{
    				e.printStackTrace();
    			}
    			catch (OperationApplicationException e)
    			{
    				e.printStackTrace();
    			}
    		}

            runOnUiThread(new Runnable() { 
                public void run() {
                    tv.setText("All contacts changed.");
                }
            });          
        }
    
        Handler hnd = new Handler()
        {    
            @Override
            public void handleMessage(Message msg) 
            {
                prg++;
                pb.setProgress(prg);

                String perc = String.valueOf(prg).toString();
                tv.setText(perc + "/" + String.valueOf(contacts.size()) + " contacts changed.");
            }
        };
    };

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




