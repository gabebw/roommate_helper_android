package edu.brandeis.jbs.rh;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RoommateHelper extends Activity implements OnClickListener, RoommateHelperConstants {
	private Button loginButton;
	private EditText emailText;
	private EditText passwordText;
	private SharedPreferences settings;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		settings = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
		// Start ActionPicker activity right away if email and password exist.
		if (settings.getString("email", "").length() > 0
				&& settings.getString("password", "").length() > 0) {
			startActionPicker();
		}
		emailText = (EditText) findViewById(R.id.login_form_email);
		passwordText = (EditText) findViewById(R.id.login_form_password);

		loginButton = (Button) findViewById(R.id.login_form_submit);
		loginButton.setOnClickListener(this);
	}

	public void onClick(View view) {
		// Save email and password to shared preferences
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("email", emailText.getText().toString());
		editor.putString("password", passwordText.getText().toString());
		editor.commit();
		
		// Clear edit text fields so email and password aren't visible after logout
		emailText.setText("");
		passwordText.setText("");
		startActionPicker();
	}

	public void startActionPicker() {
		Intent i = new Intent(RoommateHelper.this, ActionPicker.class);
		startActivity(i);
	}
}