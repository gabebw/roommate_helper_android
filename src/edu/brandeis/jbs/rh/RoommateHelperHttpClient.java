package edu.brandeis.jbs.rh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class RoommateHelperHttpClient {
	private CookieStore cookieStore;
	private SharedPreferences settings;
	private String email;
	private String password;

	public static final String PREFS_FILE = "rh.settings";
	public static final String LOGIN_URL = "https://roommate-helper.heroku.com/user_sessions";

	/**
	 * Overload constructor so we can pass email and password if we want.
	 * 
	 * @param email
	 * @param password
	 */
	public RoommateHelperHttpClient(String email, String password) {
		this.email = email;
		this.password = password;
	}

	/**
	 * Grabs user's email and password from SharedPreferences. Must provide a
	 * Context (can use this since Activity extends Context) so that we can use
	 * getSharedPreferences().
	 */
	public RoommateHelperHttpClient(Context context) {
		settings = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

		// Get user email and password
		email = settings.getString("email", "");
		password = settings.getString("password", "");
	}

	public CookieStore login() {
		LoginTask loginTask = new LoginTask();// email, password);
		try {
			loginTask.execute();
			cookieStore = loginTask.get();
			return cookieStore;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private class LoginTask extends AsyncTask<Void, Void, CookieStore> {
		/**
		 * Create new user session using provided email and password.
		 * 
		 * @param email
		 * @param password
		 * @return List<Cookie> The list of Cookies returned by logging in.
		 */
		public CookieStore doInBackground(Void... v) {
			DefaultHttpClient httpclient = new DefaultHttpClient();

			// log in (create a new user session)
			HttpPost login = new HttpPost(LOGIN_URL);

			// TERRIBLE COPY-PASTED CODE
			try {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("user_session[email]", email));
				nvps.add(new BasicNameValuePair("user_session[password]",
						password));
				login.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			} catch (Exception ex) {
				ex.printStackTrace();
			} // END OF COPY-PASTE

			try {
				HttpResponse response = httpclient.execute(login);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					entity.consumeContent();
				}
				cookieStore = httpclient.getCookieStore();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				httpclient.getConnectionManager().shutdown();
			}
			return cookieStore;
		}
	}

	/**
	 * Call after calling login(email, password) to get the CookieStore
	 * containing the logged-in user's cookies.
	 * 
	 * @return CookieStore A CookieStore containing the logged-in user's cookies.
	 */
	public CookieStore getCookieStore() {
		return cookieStore;
	}
}
