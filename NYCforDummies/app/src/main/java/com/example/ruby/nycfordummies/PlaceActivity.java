package com.example.ruby.nycfordummies;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ruby on 8/7/17.
 */

//PlaceActivity sets up the screen which displays all the details about a Tourist Attraction to NYC
public class PlaceActivity extends Activity {
	private ArrayList<Place> placeArrayList; //ArrayList which will store all the object of class Place which are passed by MapActivity
	private ArrayList<String> imageNames; //ArrayList will contain the references to images for each Place
	int [] imagesIDs; // array will contain the ids to each image from imageNames
	ViewFlipper viewFlipper; //for slidesow
	PopupWindow popupWindow; //for popupwidnow
	ImageView slideShowImage; //image which will put into viewFlipper
	private int position; //Position of the item clicked in the ListView from MapActivity
	private LayoutInflater layoutInflater;
	private RelativeLayout relativeLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place_activity);

		relativeLayout = (RelativeLayout) findViewById(R.id.placeRelative);

		position = getIntent().getIntExtra("POSITION", 0); //getting the position of the place clicked listView
		viewFlipper = (ViewFlipper) findViewById(R.id.slideShow); //initialzing view flipper
		placeArrayList = MapActivity.getPlaceObjects(); //storing placeArrayList with the arrayList from MapActivity
		imageNames = placeArrayList.get(getIntent().getIntExtra("POSITION", 0)).getImages(); //imageNames will contain the array which has the names of the images for each place

		imagesIDs = new int[imageNames.size()]; //initialzing size of ImageIDs
		//store imageIds
		for (int i = 0; i <imageNames.size(); i++){
			int id = this.getResources().getIdentifier(imageNames.get(i),"drawable",getPackageName());
			imagesIDs[i] = id;
		}

		//For each Image id set it in the view flipper
		for (int j=0; j <imagesIDs.length; j++) {
			setSlideShowImage(imagesIDs[j]); //calls methods which sets the image in view flipper
		}
		viewFlipper.setAutoStart(true);  //continuous looping of images in the view flipper

		setViews();
		customizeTitleBar();
	}

	//This customizes the title bar with the name of the place
	private void customizeTitleBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setIcon(android.R.color.transparent);
		actionBar.setTitle(placeArrayList.get(position).getName());
	}

	//This sets default views of each place
	private void setViews() {
		TextView t1 = (TextView) findViewById(R.id.showAddress);
		t1.setText(placeArrayList.get(position).getLocation());

		TextView t2 = (TextView) findViewById(R.id.showHours);
		t2.setText(placeArrayList.get(position).getHours());

		TextView t3 = (TextView) findViewById(R.id.showTrains);
		System.out.println(placeArrayList.get(position).getNearbyTrains());
		t3.setText(placeArrayList.get(position).getNearbyTrains());
	}

	//This sets the imageViews in the viewflipper
	private void setSlideShowImage(int imagesID) {
		slideShowImage = new ImageView(this);
		slideShowImage.setBackgroundResource(imagesID);
		slideShowImage.invalidate();
		viewFlipper.addView(slideShowImage);
	}

	//This handles the event where user decides to open direction in native Map Application
	public void onCickToOpenMapApp(View view) {
		double currentLat = MapActivity.getCurrentLatitude(); //get the current latitude from MapActivity which is always updating
		double currentLong = MapActivity.getCurrentLongitude();////get the current latitude from MapActivity which is always updating
		double destinationLat = placeArrayList.get(position).getLatitude(); //get  latitude of place
		double destinationLong =  placeArrayList.get(position).getLongitude();//get longitude of place
		//If the given current Latitude and Longitude are Zeroes, use the current location generated in the map app
		if (currentLat == 0 && currentLong == 0) {
			String uri = "http://maps.google.com/maps?daddr=" + destinationLat + "," + destinationLong + " (" + placeArrayList.get(position).getName() + ")";
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			intent.setPackage("com.google.android.apps.maps");
			startActivity(intent);
		} else {//use the given current Latitude and Longitude to launch Map
			System.out.println("cur lat " + currentLat + " cur long" + currentLong);
			String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)", currentLat, currentLong, "Current location", destinationLat, destinationLong, placeArrayList.get(position).getName());
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			intent.setPackage("com.google.android.apps.maps");
			startActivity(intent);
		}

	}

	//This handles the event where the user clicks the About Button
	//When clicked, it opens up a popup window which displays a textview containing details about the place
	public void onClickPopupWindow(View view) {
		layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

		ViewGroup viewGroup = (ViewGroup)layoutInflater.inflate(R.layout.pop_up_layout, null);
		TextView textView = (TextView) viewGroup.findViewById(R.id.popUp);

		String s = placeArrayList.get(position).getInfo(); //getting the details of the place
		textView.setText(s);

		popupWindow = new PopupWindow(viewGroup, 600, 900, true); //initialzing and setting the size of popup window

		popupWindow.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0); //putting the window in the denter
		//popupWindow disappears when clicked outside it
		viewGroup.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				popupWindow.dismiss();
				return false;
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
