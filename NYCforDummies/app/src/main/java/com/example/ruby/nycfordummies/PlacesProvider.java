package com.example.ruby.nycfordummies;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;

/**
 * Created by ruby on 8/7/17.
 */

//PlacesProvider a ContentProvider that stores given data into SQlite database
public class PlacesProvider extends ContentProvider {

	//unique namespace for my provider
	static final String PROVIDER_NAME = "com.example.ruby.nycfordummies.PlacesProvider";

	static final String URL = "content://" + PROVIDER_NAME + "/places";

	//we will use CONTENT_URL to refer to the URL
	static final Uri CONTENT_URL = Uri.parse(URL);

	//Following are the columns that will be present in the database
	static final String ID = "id";
	static final String PLACE_NUMBER = "placeNumber";
	static final String NAME = "name";
	static final String ADDRESS = "location";
	static final String INFO = "info";
	static final String LATITUDE = "latitude";
	static final String LONGITUDE = "longitude";
	static final String HOURS = "hours";
	static final String NEARBY_TRAINS = "nearbyTrains";
	static final String IMAGE_NAMES = "imageNames";

	static final int URI_CODE = 1;

	private static HashMap<String, String> PLACES_PROJECTION_MAP;

	static final UriMatcher URI_MATCHER;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(PROVIDER_NAME, "places", URI_CODE);
	}

	private SQLiteDatabase db;
	static final String DATABASE_NAME = "NYC";
	static final String PLACES_TABLE_NAME = "places";
	static final int DATABASE_VERSION = 1;
	static final String CREATE_DB_TABLE =
					" CREATE TABLE " + PLACES_TABLE_NAME +
					" (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
					" name TEXT NOT NULL, " +
					" placeNumber TEXT NOT NULL, " +
					" location TEXT NOT NULL, " +
					" info TEXT NOT NULL, " +
					" latitude TEXT NOT NULL, " +
					" longitude TEXT NOT NULL, " +
					" hours TEXT NOT NULL, " +
					" nearbyTrains TEXT NOT NULL, " +
					" imageNames TEXT NOT NULL);";



	@Override
	public boolean onCreate() {
		DatabaseHelper dbHelper = new DatabaseHelper(getContext());
		db = dbHelper.getWritableDatabase();

		return (db == null)? false:true;
	}

	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
		sqLiteQueryBuilder.setTables(PLACES_TABLE_NAME);

		switch (URI_MATCHER.match(uri)) {
			case URI_CODE:
				sqLiteQueryBuilder.setProjectionMap(PLACES_PROJECTION_MAP);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);

		}

		if (sortOrder == null || sortOrder == ""){ //Sort by Place Numbers
			/**
			 * By default sort on Place Numbers
			 */
			sortOrder = PLACE_NUMBER;
		}

		Cursor cursor = sqLiteQueryBuilder.query(db,projection,selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}


	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)){
			/**
			 * Get all place records
			 */
			case URI_CODE:
				return "vnd.android.cursor.dir/vnd.example.places";
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	//Method inserts values into database
	@Override
	public Uri insert(Uri uri, ContentValues contentValues) {
		/**
		 * Add a new place record
		 */
		long rowID = db.insert(	PLACES_TABLE_NAME, "", contentValues);
		/**
		 * If record is added successfully
		 */
		if (rowID > 0)
		{
			Uri _uri = ContentUris.withAppendedId(CONTENT_URL, rowID);
			getContext().getContentResolver().notifyChange(_uri, null);
			return _uri;
		}
		throw new SQLException("Failed to add a record into " + uri);
	}

	//Methods deletes rows from data base
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;

		switch (URI_MATCHER.match(uri)){
			case URI_CODE:
				count = db.delete(PLACES_TABLE_NAME, selection, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	//Method updates values in the rows of Database
	@Override
	public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
		int count = 0;

		switch (URI_MATCHER.match(uri)){
			case URI_CODE:
				count = db.update(PLACES_TABLE_NAME, contentValues,
						selection, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri );
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			//The data will be inserted into the database here because it should only be inserted once
			db.execSQL(CREATE_DB_TABLE);

			//The data for Place 1
			ContentValues contentValues1 = new ContentValues();
			contentValues1.put(PlacesProvider.PLACE_NUMBER, "1");
			contentValues1.put(PlacesProvider.NAME, "Statue of liberty");
			contentValues1.put(PlacesProvider.ADDRESS, " New York, NY 10004");
			contentValues1.put(PlacesProvider.INFO, "The Statue of Liberty was a joint effort between France and the United States, intended to commemorate the lasting friendship between the peoples of the two nations. The French sculptor Frederic-Auguste Bartholdi created the statue itself out of sheets of hammered copper, while Alexandre-Gustave Eiffel, the man behind the famed Eiffel Tower, designed the statue’s steel framework. The Statue of Liberty was then given to the United States and erected atop an American-designed pedestal on a small island in Upper New York Bay, now known as Liberty Island, and dedicated by President Grover Cleveland in 1886. Over the years, the statue stood tall as millions of immigrants arrived in America via nearby Ellis Island; in 1986, it underwent an extensive renovation in honor of the centennial of its dedication. Today, the Statue of Liberty remains an enduring symbol of freedom and democracy, as well as one of the world’s most recognizable landmarks.\n\n" +
													"Fun Fact: Total weight of the Statue of Liberty is 225 tons (or 450,000 pounds).");
			contentValues1.put(PlacesProvider.LATITUDE, "40.689277");
			contentValues1.put(PlacesProvider.LONGITUDE, "-74.044502");
			contentValues1.put(PlacesProvider.HOURS,"Monday\t\t\t\t8:30AM–4PM\n" +
													"Tuesday\t\t\t8:30AM–4PM\n" +
													"Wednesday\t8:30AM–4PM\n" +
													"Thursday\t\t\t8:30AM–4PM\n" +
													"Friday\t\t\t\t\t8:30AM–4PM\n" +
													"Saturday\t\t\t8:30AM–4PM\n" +
													"Sunday\t\t\t\t8:30AM–4PM");
			contentValues1.put(PlacesProvider.NEARBY_TRAINS, " No nearby trains; take a ferry instead");
			contentValues1.put(PlacesProvider.IMAGE_NAMES, "place11,place12,place13,place14,place15");
			db.insert("places",null, contentValues1);

			//The data for Place 2
			ContentValues contentValues2 = new ContentValues();
			contentValues2.put(PlacesProvider.PLACE_NUMBER, "2");
			contentValues2.put(PlacesProvider.NAME, "Central Park");
			contentValues2.put(PlacesProvider.ADDRESS, " Central Park streches from 110th to 59th St, and from 8th Ave to 5th Ave.");
			contentValues2.put(PlacesProvider.INFO, "Central Park is the most visited urban park in the United States, with approximately 40 million visitors per year from around the world. Designed by Frederick Law Olmsted and Calvert Vaux, it is a beautiful and welcome hiatus for New Yorkers and tourists alike. The park has several lakes and ponds, two ice-skating rinks, the Central Park Zoo, Belvedere Castle and the famous Strawberry Fields. There are also lots of grassy areas good for picnics, and there are also quite a few playgrounds for children. Many events and concerts take place every year in Central Park, and many people also like to celebrate their special events and weddings in the surrounding beauty.\n\n" +
													"Fun Fact:  Central Park was the first public landscaped park in all of the United States.");
			contentValues2.put(PlacesProvider.LATITUDE, "40.782848");
			contentValues2.put(PlacesProvider.LONGITUDE, "-73.965334");
			contentValues2.put(PlacesProvider.HOURS,"Monday\t\t\t\t6AM–1AM\n" +
													"Tuesday\t\t\t6AM–1AM\n" +
													"Wednesday\t6AM–1AM\n" +
													"Thursday\t\t\t6AM–1AM\n" +
													"Friday\t\t\t\t\t6AM–1AM\n" +
													"Saturday\t\t\t6AM–1AM\n" +
													"Sunday\t\t\t\t6AM–1AM\n");
			contentValues2.put(PlacesProvider.NEARBY_TRAINS, " N, R, Q : Located at 57th & 7th \n" +
															" 2, 3 Trains\n");
			contentValues2.put(PlacesProvider.IMAGE_NAMES, "place21,place22,place23,place24,place25");
			db.insert("places",null, contentValues2);

			//The data for Place 3
			ContentValues contentValues3 = new ContentValues();
			contentValues3.put(PlacesProvider.PLACE_NUMBER, "3");
			contentValues3.put(PlacesProvider.NAME, "The Metropolitan Museum of Art");
			contentValues3.put(PlacesProvider.ADDRESS, " 1000 5th Ave, New York, NY 10028");
			contentValues3.put(PlacesProvider.INFO, "The Metropolitan Museum of Art was founded on April 13, 1870, \"to be located in the City of New York, for the purpose of establishing and maintaining in said city a Museum and library of art, of encouraging and developing the study of the fine arts, and the application of arts to manufacture and practical life, of advancing the general knowledge of kindred subjects and of furnishing popular instruction.\" The permanent collection consists of art from classical antiquity and ancient Egypt, paintings and sculptures from nearly all the European masters, and collection of American and modern art. The Met maintains extensive holdings of African, Asian, Oceanian, Byzantine, Indian, and Islamic art. The museum is home to encyclopedic collections of musical instruments, costumes and accessories and antique weapons and armor from around the world. Several notable interiors, ranging from first-century Rome through modern American design, are installed in its galleries.\n\n" +
													"Fun Fact: It's home to the world's oldest surviving piano.");
			contentValues3.put(PlacesProvider.LATITUDE, "40.779421");
			contentValues3.put(PlacesProvider.LONGITUDE, "-73.963223");
			contentValues3.put(PlacesProvider.HOURS,"Monday\t\t\t\t10AM–5:30PM\n" +
													"Tuesday\t\t\t10AM–5:30PM\n" +
													"Wednesday\t10AM–5:30PM\n" +
													"Thursday\t\t\t10AM–5:30PM\n" +
													"Friday\t\t\t\t\t10AM–5:30PM\n" +
													"Saturday\t\t\t10AM–9PM\n" +
													"Sunday\t\t\t\t10AM–9PM\n");
			contentValues3.put(PlacesProvider.NEARBY_TRAINS," 6 : Located at 77th & 86th \n " +
			  												" 4, 5 : Located at 86th");
			contentValues3.put(PlacesProvider.IMAGE_NAMES, "place31,place32,place33,place34,place35");
			db.insert("places",null, contentValues3);

			//The data for Place 4
			ContentValues contentValues4 = new ContentValues();
			contentValues4.put(PlacesProvider.PLACE_NUMBER, "4");
			contentValues4.put(PlacesProvider.NAME, "New York Botanical Garden");
			contentValues4.put(PlacesProvider.ADDRESS, " 2900 Southern Blvd, Bronx, NY 10458");
			contentValues4.put(PlacesProvider.INFO, "The New York Botanical Garden is an iconic living museum, a major educational institution, and a renowned plant research and conservation organization. Founded in 1891, it is one of the greatest botanical gardens in the world and the largest in any city in the United States, distinguished by the beauty of its diverse landscape and extensive collections and gardens, as well as by the scope and excellence of its programs. he Garden is also a major educational institution. As of 2016, more than 1,000,000 people annually—among them Bronx families, school children, and teachers—learn about plant science, ecology, and healthful eating through NYBG's hands-on, curriculum-based programming. Nearly 90,000 of those visitors are children from underserved neighboring communities, while more than 3,000 are teachers from NYC's public school system participating in professional development programs that train them to teach science courses at all grade levels.\n\n" +
													"Fun Fact: Living collections contain more than one million plants. \n");
			contentValues4.put(PlacesProvider.LATITUDE, "40.863148");
			contentValues4.put(PlacesProvider.LONGITUDE, "-73.877008");
			contentValues4.put(PlacesProvider.HOURS, "Monday\t\t\t\tCLOSED\n" +
													"Tuesday \t\t\t10AM–6PM\n" +
													"Wednesday\t10AM–6PM\n" +
													"Thursday\t\t\t10AM–6PM\n" +
													"Friday\t\t\t\t\t10AM–6PM\n" +
													"Saturday\t\t\t10AM–6PM\n" +
													"Sunday\t\t\t\t10AM–6PM\n");
			contentValues4.put(PlacesProvider.NEARBY_TRAINS, " B, D, 4, 2 : Located at  Bedford Park Blvd");
			contentValues4.put(PlacesProvider.IMAGE_NAMES, "place41,place42,place43,place44,place45");
			db.insert("places",null, contentValues4);

			//The data for Place 5
			ContentValues contentValues5 = new ContentValues();
			contentValues5.put(PlacesProvider.PLACE_NUMBER, "5");
			contentValues5.put(PlacesProvider.NAME, "Empire State Building");
			contentValues5.put(PlacesProvider.ADDRESS, " 350 5th Ave, New York, NY 10118");
			contentValues5.put(PlacesProvider.INFO, "The Empire State Building is a 102-story skyscraper located on Fifth Avenue between West 33rd and 34th Streets in Midtown, Manhattan, New York City.  Its name is derived from the nickname for New York, the Empire State. It stood as the world's tallest building for nearly 40 years, from its completion in early 1931 until the topping out of the original World Trade Center's North Tower in late 1970. The Empire State Building is currently the fifth-tallest completed skyscraper in the United States and the 35th-tallest in the world. It is also the fifth-tallest freestanding structure in the Americas. When measured by pinnacle height, it is the fourth-tallest building in the United States.The Empire State Building is an American cultural icon. It is designed in the distinctive Art Deco style and has been named as one of the Seven Wonders of the Modern World by the American Society of Civil Engineers.\n\n" +
													"Fun Fact: The top of the Empire State Building is used for broadcasting the majority of commercial TV stations and FM radio stations in New York City. \n");
			contentValues5.put(PlacesProvider.LATITUDE, "40.748757");
			contentValues5.put(PlacesProvider.LONGITUDE, "-73.985806");
			contentValues5.put(PlacesProvider.HOURS, "Monday\t\t\t\t8AM–2AM\n" +
													"Tuesday\t\t\t8AM–2AM\n" +
													"Wednesday\t8AM–2AM\n" +
													"Thursday\t\t\t8AM–2AM\n" +
													"Friday\t\t\t\t\t8AM–2AM\n" +
													"Saturday\t\t\t8AM–2AM\n" +
													"Sunday\t\t\t\t8AM–2AM\n");
			contentValues5.put(PlacesProvider.NEARBY_TRAINS, " 1, 2, 3 : Located at Penn Station/34th \n" +
															" B, D, F, M, N, Q, R : at Herald Square \n");
			contentValues5.put(PlacesProvider.IMAGE_NAMES, "place51,place52,place53,place54,place55");
			db.insert("places",null, contentValues5);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int i, int i1) {
			db.execSQL("DROP TABLE IF EXISTS" + PLACES_TABLE_NAME);
			onCreate(db);

		}
	}
}
