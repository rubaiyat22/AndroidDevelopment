package com.example.ruby.nycfordummies;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;


//MainActivity acts as the welcome page for the app; It has a button, when clicked, leads to the main screen of the app
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		setText();
	}

	//Sets the text that will appear in the welcome page
	public void setText() {
		TextView description = (TextView) findViewById(R.id.description);
		description.setText("\"I love New York. You can pop out of the Underworld in Central Park, hail a taxi, head down Fifth Avenue with a giant hellhound loping behind you, and nobody even looks at you funny.\"\n" +
							"- Rick Riordan");

	}

	//This button opens the next page in the app
	public void setStartButton(View view) {
		//click of button opens up MapActivity
		startActivity(new Intent(this, MapActivity.class));
	}

}
