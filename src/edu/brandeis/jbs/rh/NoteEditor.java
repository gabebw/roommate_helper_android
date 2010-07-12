package edu.brandeis.jbs.rh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.net.http.AndroidHttpClient;
import android.net.Uri;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.impl.client.DefaultHttpClient;


public class NoteEditor extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noteeditor);
    }
    
    public void onStart() {
    	super.onStart();
        EditText content = (EditText)findViewById(R.id.note_content);
        
        // this is just for testing purposes
        String url = "https://roommate-helper.heroku.com/households/822834891/whiteboard";
        
        DownloadNoteTask dnt = new DownloadNoteTask();
        try {
        	dnt.execute(url);
        	content.setText(dnt.get());
        } catch (Exception ex) {
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
    
    private class DownloadNoteTask extends AsyncTask<String, Integer, String> {    	
        protected String doInBackground(String... urls) {
        	// Set up http client and cookie store
        	BasicHttpContext context = new BasicHttpContext();
        	context.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());
        	AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        	
        	/*
        	DefaultHttpClient defclient = new DefaultHttpClient();
        	HttpGet get = new HttpGet("https://roommate-helper.heroku.com");
        	try {
        		defclient.execute(get);
        	} catch (Exception ex) {
        		ex.printStackTrace();
        	}
        	*/
        	 
        	// log in (create a new user session)
        	//HttpHost host = new HttpHost("https://roommate-helper.heroku.com");
        	HttpPost login = new HttpPost("https://roommate-helper.heroku.com/user_sessions");
        	BasicHttpParams params = new BasicHttpParams();
        	String email = "singingwolfboy@gmail.com";
            String password = "secure";
            /*
        	params.setParameter("user_session[email]", email);
        	params.setParameter("user_session[password]", password);
        	login.setParams(params);
        	*/
        	
            // TERRIBLE COPY-PASTED CODE
        	try {
        		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        		nvps.add(new BasicNameValuePair("user_session[email]", email));
        		nvps.add(new BasicNameValuePair("user_session[password]", password));
        		login.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        	} catch (Exception ex) {
        		ex.printStackTrace();
        	} // END OF COPY-PASTE
        	
        	
        	try {
        		client.execute(login, context);
        	} catch (IOException ex) {
        		ex.printStackTrace();
        		return "<could not log in>";
        	}
        	
        	/*
        	// as a test, get the home page
        	HttpGet homepage = new HttpGet("https://roommate-helper.heroku.com");
        	try {
        		HttpResponse resp = client.execute(homepage, context);
        		HttpEntity entity = resp.getEntity();
             	if (entity != null) {
             		InputStream instream = entity.getContent();
             		return convertStreamToString(instream);
             	}
        	} catch (IOException ex) {
        		ex.printStackTrace();
        	}
        	*/
        	
             
        	// set up a request object for the passed in URL
            HttpGet req = new HttpGet(urls[0]);
            req.addHeader("Accept", "text/xml");
             
            try {
            	// actually make the request
            	HttpResponse resp = client.execute(req, context);
            	HttpEntity entity = resp.getEntity();
             	if (entity != null) {
             		InputStream instream = entity.getContent();
             		return convertStreamToString(instream);
             	} else {
             		return "<null>";
             	}
            } catch (IOException ex) {
             	ex.printStackTrace();
             	return "<could not connect>";
            } finally {
            	client.close();
            }
        }
    }
}