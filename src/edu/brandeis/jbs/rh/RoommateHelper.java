package edu.brandeis.jbs.rh;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
//import android.widget.Button;

public class RoommateHelper extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        View button = findViewById(R.id.login_form_submit);
        button.setOnClickListener(this);
    }
    
    public void onClick(View view) {
    	Intent i = new Intent(RoommateHelper.this, ActionPicker.class);
    	startActivity(i);
    }
}