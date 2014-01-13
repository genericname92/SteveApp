package com.SteveApp;

import java.util.ArrayList;
import java.util.Arrays;

public class Presets
{
	public static String[] precons = {"Avengers", "Justice League",
							   		  "Three Stooges", "Harry Potter Trio",
							   		  "7 Dwarfs", "Lord of the Rings Fellowship",
							   		  "South Park Quad", "One Piece Crew",
							   		  "Scooby Gang", "3 Musketeers"};
	
	public static String[] TheAvengers = {"Iron Man", "Captain America",
								   		  "The Hulk", "Thor", "Black Widow", "Hawkeye"};

	public static String[] TheJusticeLeague = {"Superman", "Batman", "Wonder Woman",
			   								   "Flash", "Green Lantern", "Aquaman"};
	
	public static String[] TheThreeStooges = {"Moe", "Larry", "Curly"};
	
	public static String[] HarryPotterTrio = {"Harry", "Ron", "Hermione"};
	
	public static String[] TheSevenDwarfs = {"Doc", "Dopey", "Bashful", "Grumpy",
									  		 "Grumpy", "Sneezy", "Sleepy", "Happy"};
	
	public static String[] LotRFellowship = {"Frodo", "Sam", "Merry", "Pippin", "Legolas",
									  		 "Boromir", "Gandalf", "Gimli", "Aragorn"};
	
	public static String[] SouthParkQuad = {"Stan", "Kyle", "Kenny", "Cartman"};
	
	public static String[] OnePieceCrew = {"Luffy", "Zoro", "Nami", "Usopp", "Sanji", "Robin",
										   "Franky", "Brooke"};
	
	public static String[] ScoobyGang = {"Scooby", "Shaggy", "Velma", "Fred", "Daphne"};
	
	public static String[] ThreeMusketeers = {"Athos", "Porthos", "Aramis"};
	
	public static ArrayList<String> getList(String chosenList)
	{
		if (chosenList.equals("Avengers"))
		{
			return new ArrayList<String>(Arrays.asList(TheAvengers));
		}
		if (chosenList.equals("Justice League"))
		{
			return new ArrayList<String>(Arrays.asList(TheJusticeLeague));
		}
		if (chosenList.equals("Three Stooges"))
		{
			return new ArrayList<String>(Arrays.asList(TheThreeStooges));
		}
		if (chosenList.equals("Harry Potter Trio"))
		{
			return new ArrayList<String>(Arrays.asList(HarryPotterTrio));
		}
		if (chosenList.equals("7 Dwarfs"))
		{
			return new ArrayList<String>(Arrays.asList(TheSevenDwarfs));
		}
		if (chosenList.equals("Lord of the Rings Fellowship"))
		{
			return new ArrayList<String>(Arrays.asList(LotRFellowship));
		}
		if (chosenList.equals("South Park Quad"))
		{
			return new ArrayList<String>(Arrays.asList(SouthParkQuad));
		}
		if (chosenList.equals("One Piece Crew"))
		{
			return new ArrayList<String>(Arrays.asList(OnePieceCrew));
		}
		if (chosenList.equals("Scooby Gang"))
		{
			return new ArrayList<String>(Arrays.asList(ScoobyGang));
		}
		if (chosenList.equals("3 Musketeers"))
		{
			return new ArrayList<String>(Arrays.asList(ThreeMusketeers));
		}
		return new ArrayList<String>();
	}
}
