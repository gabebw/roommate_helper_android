package edu.brandeis.jbs.rh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.net.http.AndroidHttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;


public class NoteEditor extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noteeditor);
        
        EditText content = (EditText)findViewById(R.id.note_content);
        
        // this is just for testing purposes
        String url = "http://roommate-helper.heroku.com/households/822834891/whiteboard";
        String email = "singingwolfboy@gmail.com";
        String password = "secure";
        
        AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        
        HttpHost host = new HttpHost("http://roommate-helper.heroku.com", 80, "http");
        HttpGet req = new HttpGet(url);
        req.addHeader("Accept", "text/xml");
        
        try {
        	HttpResponse resp = client.execute(host, req);
        	HttpEntity entity = resp.getEntity();
        	if (entity != null) {
        		InputStream instream = entity.getContent();
        		String result= convertStreamToString(instream);
        		content.setText(result);
        	} else {
        		content.setText("<null>");
        	}
        } catch (IOException ex) {
        	content.setText("<could not connect>");
        	ex.printStackTrace();
        }
    }
    
    private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}