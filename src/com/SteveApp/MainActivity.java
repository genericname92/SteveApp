package com.SteveApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.SteveApp.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity
{
	final Context context = this;
	public final static String EXTRA_MESSAGE = "com.SteveApp.MESSAGE";
	private int prg = 0;
	private ArrayList <contactDuo> contacts;
	private ContentResolver resolver;
	private TextView tv;
	private ProgressBar pb;
	private String universal;
	
	public void showDialog(String message, Context context)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		 
		// set dialog message
		alertDialogBuilder
			.setMessage(message)
			.setCancelable(false)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, close
					// current activity
					dialog.cancel();
				}
			  });

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}
	
	@SuppressLint("InlinedApi")
	public void onChangeClick(final View v)
	{
		EditText editText = (EditText) findViewById(R.id.edit_message);
		universal = editText.getText().toString();
		if (universal.equals(""))
		{
			showDialog("Please enter something to change all of this phone's contacts into.",
						context);
		}
		else
		{
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
			// A projection is an array of strings that defines which fields we
			// want
			// to get from our query
			// In our case, we just want contact ID and primary display name
			final String[] projection = { ContactsContract.RawContacts._ID,
					ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY };

			// Contact IDs are greater than 0 so this SQLite query grabs every
			// contact essentially
			// A cursor is a list of objects obtained from a query. Data is
			// separated into columns
			Cursor cc = resolver.query(ContactsContract.RawContacts.CONTENT_URI, projection,
									   ContactsContract.RawContacts._ID + " > 0", null, null);

			// CONTACTS HAVE NOW BEEN GRABBED. CC HOLDS ALL OF THEM

			// This is an ArrayList of contactDuos, a bean I created that holds 2 variables
			// The variables are ID and display name
			// After we read all the data we need into this data structure, we will
			// read the list's contents into a file
			contacts = new ArrayList<contactDuo>();

			int nameFieldColumnIndex1;
			int nameFieldColumnIndex2;
			while (cc.moveToNext())
			{
				// Grab the columns of the 2 relevant fields we found. Grab data
				// from each column. Write into array list
				nameFieldColumnIndex1 = cc
						.getColumnIndex(ContactsContract.RawContacts._ID);
				nameFieldColumnIndex2 = cc
						.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
				if (!cc.getString(nameFieldColumnIndex2).equals("null"))
				{
					contacts.add(new contactDuo(cc.getString(nameFieldColumnIndex1),
												cc.getString(nameFieldColumnIndex2)));
				}
			}
			cc.close();

			// This for loop creates the formatted string that we write to the
			// file
			// Format is id:displayname|id2:displayname2
			for (int i = 0; i < contacts.size(); i++)
			{
				contactInfo += contacts.get(i).id;
				contactInfo += ":";
				contactInfo += contacts.get(i).display_name;
				contactInfo += "|";
			}
			pb.setMax(contacts.size());

			// Write pristine contact info to file for future reversal
			// Try external first
			if (Util.isExternalStorageWritable())
			{
				File file = new File(Environment.getExternalStorageDirectory()
						.getPath() + "/SteveApp/", "backup_contacts.txt");
				if (!file.exists())
				{
					try
					{
						FileWriter fWriter = new FileWriter(Environment.getExternalStorageDirectory().getPath()
															+ "/SteveApp/backup_contacts.txt");
						fWriter.write(contactInfo);
						fWriter.close();
					}
					catch (Exception e)
					{
						showDialog("Creating a backup file in external memory failed.",
									context);
						setContentView(R.layout.activity_main);
						return;
					}
				}
			}
			// Now internal
			else
			{
				String ContactLists = "Contact_Lists";
				FileOutputStream fos;
				try
				{
					File file = getBaseContext().getFileStreamPath("Contact_Lists");
					if (!file.exists())
					{
						fos = openFileOutput(ContactLists, Context.MODE_PRIVATE);
						fos.write(contactInfo.getBytes());
					}
				}
				catch (FileNotFoundException e)
				{
					showDialog("Backup file creation failed.", context);
					setContentView(R.layout.activity_main);
					return;
				}
				catch (IOException e)
				{
					showDialog("Reading from backup file failed.", context);
					setContentView(R.layout.activity_main);
					return;
				}
			}

			Runnable myThread = new Runnable()
			{
				@SuppressLint("InlinedApi")
				@Override
				public void run()
				{
					// Reset progress from older runs
					if (prg != 0)
					{
						prg = 0;
					}
					// This loop changes all of the contacts
					for (int i = 0; i < contacts.size(); i++)
					{
						ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
						// CONTACT UPDATE DONE HERE
						ops.add(ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
								.withSelection(ContactsContract.RawContacts._ID+ " LIKE ?", new String[] { contacts.get(i).id })
								.withValue(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, universal).build());

						try
						{
							resolver.applyBatch(ContactsContract.AUTHORITY, ops);
							hnd.sendMessage(hnd.obtainMessage());
						}
						catch (RemoteException e)
						{
							showDialog("An error occured during contacts changing.",
										context);
							setContentView(R.layout.activity_main);
							return;
						}
						catch (OperationApplicationException e)
						{
							showDialog("An error occured during contacts changing.",
										context);
							setContentView(R.layout.activity_main);
							return;
						}
					}

					runOnUiThread(new Runnable()
					{
						public void run() {
							tv.setText("All contacts successfully changed.");
							showDialog("All contacts successfully changed.",
										context);
							setContentView(R.layout.activity_main);
						};
					});
				}

				@SuppressLint("HandlerLeak")
				Handler hnd = new Handler()
				{
					@Override
					public void handleMessage(Message msg)
					{
						prg++;
						pb.setProgress(prg);

						String perc = String.valueOf(prg).toString();
						tv.setText(perc + "/" + String.valueOf(contacts.size())
									+ " contacts changed.");
					}
				};
			};
			new Thread(myThread).start();
		}
	}
	
	@SuppressWarnings("resource")
	public void onUndoClick(View arg0)
	{
		setContentView(R.layout.activity_display_message);
        pb = (ProgressBar) findViewById(R.id.pbId);
        tv = (TextView) findViewById(R.id.tvId);
        
		String contactString = "";
		Boolean done = false;
		
		// Try to read from external storage first
		if (Util.isExternalStorageWritable())
		{
			File extFile = new File(Environment.getExternalStorageDirectory().getPath() + "/SteveApp/", "backup_contacts.txt" );
			// If file exists in external storage, grab its contents
			if (extFile.exists())
			{
				BufferedReader br = null;
				try
				{
					String sCurrentLine;
					br = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().getPath() + "/SteveApp/backup_contacts.txt"));
					while((sCurrentLine = br.readLine()) != null)
					{
						contactString += sCurrentLine;
					}
					// Set a boolean so we don't look around in internal storage
					done = true;
					
					// Delete the file
					extFile.delete();
				}
				catch (IOException e)
				{
                	showDialog("Error reading from external storage.", context);
                	setContentView(R.layout.activity_main);
                	return;
				}
			}
		}
		// Now try to read from internal storage
		if (!done)
		{
			try
			{
				FileInputStream test = openFileInput ("Contact_Lists");
				byte [] bytes = new byte [1];
				while (test.read(bytes, 0, 1) != -1)
				{
					contactString += (char) bytes[0];
					bytes = new byte [1];
				}
				test.close();
				
				// Delete reversion file
				File dir = getFilesDir();
				File file = new File(dir, "Contact_Lists");
				file.delete();
			}
			catch (FileNotFoundException e)
			{
            	showDialog("No need to undo changes. Contacts list is pristine.", context);
            	setContentView(R.layout.activity_main);
            	return;
			}
			catch (IOException e)
			{
				showDialog("Reading from internal storage failed.", context);
            	setContentView(R.layout.activity_main);
            	return;
			}
		}

		// Create ArrayList of contactDuos to populate
		contacts = new ArrayList<contactDuo> ();
		
		// Now contactString is a string that contains the entire contact list 'encoded'
		// Parse its contents into contacts
		for (int i = 0; i < contactString.length(); i++)
		{
			String id = "";
			while (contactString.charAt(i) != ':')
			{
				id += contactString.charAt(i);
				i++;
			}
			i++;
					
			String name = "";
			while (contactString.charAt(i) != '|')
			{
				name += contactString.charAt(i);
				i++;
			}
			contacts.add(new contactDuo(id, name));
		}
		pb.setMax(contacts.size());
				
		resolver = getContentResolver();
			
		Runnable myThread = new Runnable()
	    { 
		    @SuppressLint("InlinedApi")
		    @Override
		    public void run() 
		    {
		    	if (prg != 0)
		        {
		       		prg = 0;
		        }
		        	
		        // 'Repair' contact list
			   	for (int i = 0; i < contacts.size(); i++)
			    {
			   		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			    				 
			    	// CONTACT UPDATE DONE HERE
			    	ops.add(ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
			   				.withSelection(ContactsContract.RawContacts._ID + " LIKE ?", new String[] {contacts.get(i).id})
			   			    .withValue(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, contacts.get(i).display_name)
			    			.build());
			    				 
			    	try
			    	{
			    		resolver.applyBatch(ContactsContract.AUTHORITY, ops);
			    		hnd.sendMessage(hnd.obtainMessage());
			    	}
			    	catch (RemoteException e)
			    	{
			    		tv.setText("Reverting contacts failed.");
		                showDialog("Reverting contacts failed.", context);
		               	setContentView(R.layout.activity_main);
		               	return;
			    	}
			    	catch (OperationApplicationException e)
			    	{
			    		tv.setText("Reverting contacts failed.");
		               	showDialog("Reverting contacts failed.", context);
		                setContentView(R.layout.activity_main);
		               	return;
			    	}
			    }

			    runOnUiThread(new Runnable()
			    { 
		        	public void run()
		        	{
			        	tv.setText("Contacts list repaired.");
			        	showDialog("Contacts list repaired.", context);
		                setContentView(R.layout.activity_main);
			        }
			    });       
		    }
		    
		    @SuppressLint("HandlerLeak")
			Handler hnd = new Handler()
		    {    
		        @Override
		        public void handleMessage(Message msg) 
		        {
		            prg++;
		            pb.setProgress(prg);

		            String perc = String.valueOf(prg).toString();
	                tv.setText(perc + "/" + String.valueOf(contacts.size()) + " contacts repaired.");
	            }
		    };
		};
		new Thread(myThread).start();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
