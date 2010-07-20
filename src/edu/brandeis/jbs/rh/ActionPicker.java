package edu.brandeis.jbs.rh;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ActionPicker extends Activity implements OnClickListener, RoommateHelperConstants {
	private SharedPreferences settings;
	private Button whiteboardsButton;
	private Button contractButton;
	private Button helpButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actionpicker2);
        
        settings = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        
        TextView user_email = (TextView)findViewById(R.id.user_email);
        user_email.setText(settings.getString("email", ""));
        
        whiteboardsButton = (Button) findViewById(R.id.actionpicker_whiteboards_button);
        whiteboardsButton.setOnClickListener(this);
        contractButton = (Button) findViewById(R.id.contract_button);
        contractButton.setOnClickListener(this);
        helpButton = (Button) findViewById(R.id.help_button);
        helpButton.setOnClickListener(this);
        
        Button logout_button = (Button)findViewById(R.id.logout_button);
        logout_button.setOnClickListener(this);
    }
    
    public void onClick(View view) {
    	Intent i;
    	switch(view.getId()) {
    	case R.id.actionpicker_whiteboards_button:
    		i = new Intent(ActionPicker.this, Whiteboards.class);
    		startActivity(i);
    		break;
    	case R.id.contract_button:
    		i = new Intent(ActionPicker.this, Contract.class);
    		startActivity(i);
    		break;
    	case R.id.help_button:
    		startActivity(new Intent(ActionPicker.this, Help.class));
    		break;
    	case R.id.logout_button:
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
