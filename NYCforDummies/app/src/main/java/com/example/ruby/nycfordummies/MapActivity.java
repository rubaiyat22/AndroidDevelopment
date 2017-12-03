package com.example.ruby.nycfordummies;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by ruby on 8/6/17.
 */

//MapActivy sets the primary screen of the app; In this screen, a user is able to click on different places present in the list
//and choose to visit a place about the respective place
public class MapActivity extends Activity implements OnMapReadyCallback {

	GoogleMap googleMap;
	private ListView placeList;  //ListView for the names of the NYC attractions
	private ArrayAdapter<String> arrayAdapter;
	public ArrayList <String> places; //ArrayList which will contain the names of the places
	private static  ArrayList <Place> placeObjects; //ArrayList which will contain Place Objext
	Marker currentMarker;
	private static double currentLatitude;
	private static double  currentLongitude;
	private BroadcastReceiver broadcastReceiver; //BroadcastReceiver object receives data from LocationService
	private static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 1;
	private static boolean isConnected; //boolean to check whether there is internet connection in the device
	int [] icons; //array which contains the icons of the markers


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);


		if (CheckForUserPermission()) {
			//if we do need permission; It is taken care of in onRequesetPermissioResult method
		} else { //if we do not need permission
			Intent newIntent = new Intent(getApplicationContext(),LocationService.class);
			startService(newIntent); //calls the service
		}

		TextView current = (TextView) findViewById(R.id.backButton); //button which places a marker and leads back to current location
		if (!isConnected()){  //if no internet connections
			isConnected = false;
			current.setVisibility(View.INVISIBLE);  //set the button invisible

		}else{
			current.setVisibility(View.VISIBLE);
			createMap(); //create the map only if there is internet connection
			isConnected = true;
		}

		placeObjects = new ArrayList<Place>();
		places = new ArrayList<String>();

		//setting the icons from res folder
		icons = new int[5];
		icons[0] = R.drawable.icon1;
		icons[1] = R.drawable.icon2;
		icons[2] = R.drawable.icon3;
		icons[3] = R.drawable.icon4;
		icons[4] = R.drawable.icon5;

		placeList = (ListView) findViewById(R.id.placeList);
		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, places);
		placeList.setAdapter(arrayAdapter);
		placeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> adapterView, View view, final int pos, long l) {
				if (!isConnected){ //if no internet connections, the list items place no marker on the map but instead open up the corresponding pages
					Intent intent = new Intent(adapterView.getContext(), PlaceActivity.class);
					intent.putExtra("POSITION", pos);
					startActivity(intent);
				} else{
					//setting the location of the place that was clicked in the ListView
					final LatLng placeLocation = new LatLng(placeObjects.get(pos).getLatitude(), placeObjects.get(pos).getLongitude());
					//creating a marker associated with the place
					Marker marker = googleMap.addMarker(new MarkerOptions()
							.position(placeLocation)
							.title(places.get(pos))
							.icon(BitmapDescriptorFactory.fromResource(icons[pos])));
					marker.showInfoWindow();
					//zooming into the marker previously created
					zoomToCurrentLocation(placeLocation);

					//handles click on the Info windows corresponding to each marker;
					//when InfoWindows are clicked, corresponding page to the place is opened
					googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
						@Override
						public void onInfoWindowClick(Marker marker) {
							//if the marker is the current location marker, no new page opens up
							if (marker.getTitle().equals("You are here!")) {
								//not a clickable marker
							} else { // A PlaceActivity is opened
								Intent intent = new Intent(adapterView.getContext(), PlaceActivity.class);
								intent.putExtra("POSITION", pos);
								startActivity(intent);
							}
						}
					});
				}

			}
		});

		//this method loads the data from the database and creates objects of Place with the data
		loadDataFromDatabse();

		//the names from Place Objects are stored into the places arraylist
		for (int i =0; i < placeObjects.size(); i++) {
			places.add(placeObjects.get(i).getName());
		}


		//this receives data from Location Serivice and stores them
		if (broadcastReceiver == null) {
			broadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					currentLatitude = (intent.getDoubleExtra("LATITUDE", 0.0));
					currentLongitude = (intent.getDoubleExtra("LONGITUDE", 0.0));
				}
			};
			registerReceiver(broadcastReceiver, new IntentFilter("LOCATION_UPDATE"));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
	}


	//This method asks user for permission to use location if the criteria is met and returs true
	//otherwise, returns false indicating that no permissions were necessary
	private boolean CheckForUserPermission() {
		if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[] { ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION },
					MY_PERMISSION_ACCESS_COURSE_LOCATION);
			return true;
		}
		return false;
	}

	//This method retreives data that was inserted into the SQLite database
	//Once the data is retreived, Place objects are created and initialzed with the respective data
	private void loadDataFromDatabse() {
		// Retrieve Place records
		String URL = "content://com.example.ruby.nycfordummies.PlacesProvider/places";
		Uri places = Uri.parse(URL);
		Cursor c = getContentResolver().query(places, null, null, null, "placeNumber");
		if (c.moveToFirst() && c !=null) {
			do{
				//ArrayList which will contain the names of the images that are used for each place
				ArrayList<String> images = new ArrayList<>();
				//Because the names are stored as a single string in the database, they need to split
				getImageNamesFromString(c.getString(c.getColumnIndex(PlacesProvider.IMAGE_NAMES)), images);
				//Creating and Initializing Place Objects
				Place newPlace = new Place(c.getInt(c.getColumnIndex(PlacesProvider.PLACE_NUMBER)),
						c.getString(c.getColumnIndex(PlacesProvider.NAME)),
						c.getString(c.getColumnIndex(PlacesProvider.ADDRESS)),
						c.getString(c.getColumnIndex(PlacesProvider.INFO)),
						c.getDouble(c.getColumnIndex(PlacesProvider.LATITUDE)),
						c.getDouble(c.getColumnIndex(PlacesProvider.LONGITUDE)),
						c.getString(c.getColumnIndex(PlacesProvider.HOURS)),
						c.getString(c.getColumnIndex(PlacesProvider.NEARBY_TRAINS)),images);

				placeObjects.add(newPlace); //Each Place Object is inserted into the Arraylist
			} while (c.moveToNext());
		}
	}

	//Function breaks up a string containing multiple images names into single image names and edits them in the array
	private void getImageNamesFromString(String string, ArrayList<String> image) {
		String [] s = string.split(",");
		for (String ss: s) {
			image.add(ss);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	//Method creates a new map on the fragment
	private void createMap() {
		MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment);
		mapFragment.getMapAsync(this);

	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.googleMap = googleMap;
		LatLng currentLocation;
		String title;
		if (currentLatitude == 0 && currentLongitude == 0) { //if no current location available, use NYC lat and long as default
			currentLocation = new LatLng(40.71448, -74.00598);
			title = "New York City";
			currentMarker = this.googleMap.addMarker(new MarkerOptions()
					.position(currentLocation)
					.title(title));
			this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation)); //zoom into the city
		} else { //use currect lat and long
			currentLocation = new LatLng(currentLatitude, currentLongitude);
			title = "You are here!";
			currentMarker = this.googleMap.addMarker(new MarkerOptions()
					.position(currentLocation)
					.title(title));
			zoomToCurrentLocation(currentLocation); //zoom into current location
		}
	}

	//Method zooms into a given location
	public void zoomToCurrentLocation(LatLng loc) {
		float zoomLevel = 15;
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomLevel)); //moves to loc with zoomlevel
		googleMap.animateCamera(CameraUpdateFactory.zoomIn()); //zooms in, animating the camera
		int duration = 2000;
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel), duration, null); //zoom out to zoomLevel with duration of 2 secs

	}

	//Button that places a marker and zooms to current location
	public void setBackToCurrentLocation (View view) {
		if (currentMarker != null) {
			currentMarker.remove();
		}
		LatLng currentLocation = new LatLng(currentLatitude, currentLongitude);
		currentMarker = this.googleMap.addMarker(new MarkerOptions()
				.position(currentLocation)
				.title("You are here!"));
		zoomToCurrentLocation(currentLocation);
		currentMarker.showInfoWindow();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == MY_PERMISSION_ACCESS_COURSE_LOCATION ){
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
				Intent newIntent = new Intent(getApplicationContext(),LocationService.class);
				startService(newIntent);
			} else { //Permission is asked again
				CheckForUserPermission();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:{
				this.finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	//Method checks whether or not there is internet connection in the device
	public boolean isConnected() {
		ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnectedOrConnecting()){
			return true;
		}
		return false;
	}

	//returns the currentLatitude for PlaceActivity to use
	public static double getCurrentLatitude() {
		return currentLatitude;
	}

	//returns the currentLongitude for PlaceActivity to use
	public static double getCurrentLongitude() {
		return currentLongitude;
	}

	//returns the placeObjects Arraylist for PlaceActivity to use
	public static ArrayList<Place> getPlaceObjects() {
		return placeObjects;
	}
}
