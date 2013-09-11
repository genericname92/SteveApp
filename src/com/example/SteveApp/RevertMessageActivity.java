package com.example.SteveApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.example.SteveApp.R;

import android.os.Bundle;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.Build;
import android.provider.ContactsContract;

public class RevertMessageActivity extends Activity {

	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Boolean bail = false;
		
		String message = "";
		String contactString = "";
		
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
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			message = "No need to undo changes. Contact list is pristine.";
			bail = true;
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			message = "Something bad happened.";
			bail = true;
			e.printStackTrace();
		}
		
		if (!bail)
		{
			// Create ArrayList of contactDuos to populate
			ArrayList <contactDuo> contacts = new ArrayList<contactDuo> ();
			
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
			
			ContentResolver resolver = getContentResolver();
			
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
			     }
			     catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						message = "Something bad happened.";
			     }
			     catch (OperationApplicationException e)
			     {
						// TODO Auto-generated catch block
						e.printStackTrace();
						message = "Something bad happened.";
				 }
			}
		}

		if (!bail)
		{
			message = "You have successfully undone the contact list changes.";
		
			// Delete reversion file
			File dir = getFilesDir();
			File file = new File(dir, "Contact_Lists");
			file.delete();
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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
