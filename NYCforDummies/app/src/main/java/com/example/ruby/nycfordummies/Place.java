package com.example.ruby.nycfordummies;

import java.util.ArrayList;

/**
 * Created by ruby on 8/8/17.
 */

//This custom class is contains the details necessary to set up Map Activity and Place Activity
public class Place {
	private int placeNumber;
	private String name;
	private String location;
	private String info;
	private double latitude;
	private double longitude;
	private String hours;
	private String nearbyTrains;
	private ArrayList<String> images;


	public Place(int placeNumber, String name, String location, String info, double latitude, double longitude, String hours, String nearbyTrains, ArrayList<String> images) {
		System.out.println("Place constructor called");
		this.placeNumber = placeNumber;
		this.name = name;
		this.location = location;
		this.info = info;
		this.latitude = latitude;
		this.longitude = longitude;
		this.hours = hours;
		this.nearbyTrains = nearbyTrains;
		this.images = images;
	}

	public int getPlaceNumber() {
		return placeNumber;
	}

	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

	public String getInfo() {
		return info;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;}

	public String getHours() {
		return hours;
	}

	public String getNearbyTrains() {
		return nearbyTrains;
	}

	public ArrayList<String> getImages() {
		return images;
	}
}
