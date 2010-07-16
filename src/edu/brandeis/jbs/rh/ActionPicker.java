package edu.brandeis.jbs.rh;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ActionPicker extends Activity implements OnClickListener, RoommateHelperConstants {
	private SharedPreferences settings;
	private Button whiteboardsButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actionpicker);
        
        whiteboardsButton = (Button) findViewById(R.id.actionpicker_whiteboards_button);
        whiteboardsButton.setOnClickListener(this);
        
        View logout_button = (Button)findViewById(R.id.logout_button);
        logout_button.setOnClickListener(this);
    }
    
    public void onClick(View view) {
    	switch(view.getId()) {
    	case R.id.actionpicker_whiteboards_button:
    		Intent i = new Intent(ActionPicker.this, Whiteboards.class);
    		startActivity(i);
    		break;
    	case R.id.logout_button:
    		settings = getSharedPreferences(RoommateHelperHttpClient.PREFS_FILE, MODE_PRIVATE);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.remove("email");
    		editor.remove("password");
    		editor.commit();
    		/* If we're logging out, we want to go back to the login screen.
    		   But we shouldn't start a new activity -- that will go on top
    		   of the stack! Instead, we'll finish this activity and hope/assume
    		   that the previous activity on the stack (just below this one)
    		   is the login screen.
    		 */
    		this.finish();
    		break;
    	}
    }
}
