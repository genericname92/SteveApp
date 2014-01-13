// Helper functions class

package com.SteveApp;

import java.util.ArrayList;
import java.util.Random;

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
	
	// Return random string from given ArrayList of strings
	public static String randomString (ArrayList<String> possibles)
	{
		// FOR ROBUSTNESS, IONO
		if (possibles.size() == 0)
		{
			return "";
		}
		Random numGen = new Random();
		int randNum = numGen.nextInt(possibles.size());
		return possibles.get(randNum);
	}
}
