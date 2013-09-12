// Helper functions class

package com.SteveApp;

import android.os.Environment;

public class Util
{
    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable()
    {
	String state = Environment.getExternalStorageState();
	if (Environment.MEDIA_MOUNTED.equals(state))
	{
	    return true;
	}
	return false;
    }
}
