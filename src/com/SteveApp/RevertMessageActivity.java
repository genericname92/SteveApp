package com.SteveApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.SteveApp.R;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.os.Build;
import android.provider.ContactsContract;

public class RevertMessageActivity extends Activity
{
	private ProgressBar pb;
    private TextView tv;
    private int prg = 0;
    private ArrayList <contactDuo> contacts;
    private ContentResolver resolver;
	
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
	
	@SuppressWarnings("resource")
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_message);
        pb = (ProgressBar) findViewById(R.id.pbId);
        tv = (TextView) findViewById(R.id.tvId);
        
		// Show the Up button in the action bar.
		setupActionBar();
		
		String message = "";
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
					message = "Something bad happened.";
					e.printStackTrace();
					showDialog(message);
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
				message = "No need to undo changes. Contact list is pristine.";
				e.printStackTrace();
				showDialog(message);
				return;
			}
			catch (IOException e)
			{
				message = "Something bad happened.";
				e.printStackTrace();
				showDialog(message);
				return;
			}
		}

		// Create ArrayList of contactDuos to populate
		contacts = new ArrayList<contactDuo> ();
		
		// Now contactString is a string that contains the entire contact list 'encoded'
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
		new Thread(myThread).start();
	}
	
	private Runnable myThread = new Runnable()
	{ 
		@SuppressLint("InlinedApi")
		@Override
	    public void run() 
	    {
			// 'Repair' contact list
	    	for (int i = 0; i < contacts.size(); i++)
	    	{
	    		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
	    				 
	    		// DERPY UPDATE DONE HERE
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
	    			e.printStackTrace();
	    		}
	    		catch (OperationApplicationException e)
	    		{
	    			e.printStackTrace();
	    		}
	    	}

	        runOnUiThread(new Runnable()
	        { 
	        	public void run()
	        	{
	        		tv.setText("Contacts list repaired.");
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
	            tv.setText(perc + "/" + String.valueOf(contacts.size()) + " contacts reverted.");
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
		getMenuInflater().inflate(R.menu.revert_message, menu);
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
