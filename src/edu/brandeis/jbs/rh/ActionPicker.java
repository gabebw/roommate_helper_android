package edu.brandeis.jbs.rh;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ActionPicker extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actionpicker);
        
        View button = (Button)findViewById(R.id.notes_button);
        button.setOnClickListener(this);
    }
    
    public void onClick(View view) {
    	switch(view.getId()) {
    	case R.id.notes_button:
    		Intent i = new Intent(ActionPicker.this, NoteEditor.class);
    		startActivity(i);
    	}
    }
}