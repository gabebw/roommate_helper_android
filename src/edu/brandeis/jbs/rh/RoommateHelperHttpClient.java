package edu.brandeis.jbs.rh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

public class RoommateHelperHttpClient {
	private BasicHttpContext httpContext;
	private SharedPreferences settings;
	private String email;
	private String password;
	
	public static final String PREFS_FILE = "rh.settings";
	public static final String LOGIN_URL = "https://roommate-helper.heroku.com/user_sessions";

	/**
	 * Overload constructor so we can pass email and password if we want.
	 * @param email
	 * @param password
	 */
	public RoommateHelperHttpClient(String email, String password){
		this.email = email;
		this.password = password;
		httpContext = new BasicHttpContext();
	}
	
	/**
	 * Grabs user's email and password from SharedPreferences.
	 * Must provide a Context (can use this since Activity extends Context)
	 * so that we can use getSharedPreferences().
	 */
	public RoommateHelperHttpClient(Context context){
		settings = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

		// Get user email and password
		email = settings.getString("email", "");
		password = settings.getString("password", "");
		httpContext = new BasicHttpContext();
	}
	
	public BasicHttpContext login(){
		LoginTask loginTask = new LoginTask();//email, password);
		try {
			loginTask.execute();
			httpContext = loginTask.get();
			return httpContext;
		} catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}

	private class LoginTask extends AsyncTask<Void, Void, BasicHttpContext> {
		/**
		 * Create new user session using provided email and password.
		 * 
		 * @param email
		 * @param password
		 * @return BasicHttpContext The BasicHttpContext that stores the cookies
		 *         returned by logging in.
		 */
		public BasicHttpContext doInBackground(Void... v) {
			AndroidHttpClient client = AndroidHttpClient.newInstance("RoommateHelperClient");
			// log in (create a new user session)
			HttpPost login = new HttpPost(LOGIN_URL);

			// TERRIBLE COPY-PASTED CODE
			try {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("user_session[email]", email));
				nvps.add(new BasicNameValuePair("user_session[password]", password));
				login.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			} catch (Exception ex) {
				ex.printStackTrace();
			} // END OF COPY-PASTE

			try {
				client.execute(login, httpContext);
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				client.close();
			}
			return httpContext;
		}
	}
	
	/**
	 * Call after calling login(email, password) to get the BasicHttpContext containing the logged-in user's cookies.
	 * @return BasicHttpContext context
	 */
	public BasicHttpContext getContext(){
		return httpContext;
	}
}
