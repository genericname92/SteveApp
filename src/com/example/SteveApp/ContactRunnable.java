package com.example.SteveApp;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

public class ContactRunnable implements Runnable
{
	private int m_start;
	private int m_end;
	private ContentResolver m_resolver;
	private ArrayList <contactDuo> m_contacts;
	private String m_universal;
	
	public ContactRunnable (int start, int end, ContentResolver resolver, ArrayList <contactDuo> contacts, String universal)
	{
		m_start = start;
		m_end = end;
		m_resolver = resolver;
		m_contacts = contacts;
		m_universal = universal;
	}

	@SuppressLint("InlinedApi")
	@Override
	public void run()
	{		
        // This loop changes all of the contacts
		for (int i = m_start; i < m_end; i++)
		{	 
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			// DERPY UPDATE DONE HERE
			ops.add(ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
				.withSelection(ContactsContract.RawContacts._ID + " LIKE ?", new String[] {m_contacts.get(i).id})
				.withValue(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, m_universal)
				.build());
						 
			try
			{
				m_resolver.applyBatch(ContactsContract.AUTHORITY, ops);
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
	}
}
