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

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;

public class RoommateHelperHttpClient extends Activity {
	private BasicHttpContext context;
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
	}
	
	/**
	 * Grabs user's email and password from SharedPreferences.
	 */
	public RoommateHelperHttpClient(){
		settings = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);

		// Get user email and password
		email = settings.getString("email", "");
		password = settings.getString("password", "");
	}

	/**
	 * Create new user session using provided email and password.
	 * @param email
	 * @param password
	 * @return BasicHttpContext The BasicHttpContext that stores the cookies returned by logging in.
	 */
	public BasicHttpContext login() {
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
			client.execute(login, context);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			client.close();
		}
		return context;
	}
	
	/**
	 * Call after calling login(email, password) to get the BasicHttpContext containing the logged-in user's cookies.
	 * @return BasicHttpContext context
	 */
	public BasicHttpContext getContext(){
		return context;
	}
}
