package edu.brandeis.jbs.rh;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.net.http.AndroidHttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.impl.client.BasicCookieStore;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import edu.brandeis.jbs.rh.RoommateHelper;

public class Whiteboard extends Activity implements OnClickListener {
	private Button saveButton;
	private EditText editText;
	private BasicHttpContext context;
	private String email;
	private String password;
	private SharedPreferences settings;
	
	private String whiteboardUrl;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.whiteboard);
		
        settings = getSharedPreferences(RoommateHelper.PREFS_FILE, MODE_PRIVATE);
		
		// Get user email and password
		email = settings.getString("email", "");
		password = settings.getString("password", "");
		
		whiteboardUrl = "https://roommate-helper.heroku.com/whiteboards/1";

		// Set up HTTP client and cookie store
		context = new BasicHttpContext();
		context.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());

		editText = (EditText) findViewById(R.id.whiteboard_content);

		saveButton = (Button) findViewById(R.id.save_whiteboard_button);
		saveButton.setOnClickListener(this);
	}

	public void onResume() {
		super.onResume();

		DownloadNoteTask dnt = new DownloadNoteTask(whiteboardUrl, context, email, password);
		try {
			dnt.execute();
			Document dom = dnt.get();
			String text = dom.getElementsByTagName("text").item(0).getTextContent();
			editText.setText(text);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onClick(View v) {
		UpdateNoteTask unt = new UpdateNoteTask(whiteboardUrl, editText, context);
		try {
			unt.execute();
			String x = unt.get();
			System.out.println(x);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private class DownloadNoteTask extends AsyncTask<Void, Void, Document> {
		private String url;
		private BasicHttpContext context;
		private String email;
		private String password;

		public DownloadNoteTask(String url, BasicHttpContext context, String email, String password) {
			this.url = url;
			this.context = context;
			this.email = email;
			this.password = password;
		}

		protected Document doInBackground(Void... v) {
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");

			// log in (create a new user session)
			HttpPost login = new HttpPost(
					"https://roommate-helper.heroku.com/user_sessions");

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
			}

			// set up a request object for the passed in URL
			HttpGet req = new HttpGet(this.url + ".xml");
			req.addHeader("Accept", "text/xml");

			try {
				// actually make the request
				HttpResponse resp = client.execute(req, context);
				HttpEntity entity = resp.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					try {
						DocumentBuilder dombuilder = DocumentBuilderFactory
								.newInstance().newDocumentBuilder();
						Document dom = dombuilder.parse(instream);
						return dom;
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				client.close();
			}
			return null;
		}
	}

	private class UpdateNoteTask extends AsyncTask<Void, Void, String> {
		private String whiteboardUrl;
		private EditText editText;
		private BasicHttpContext context;

		public UpdateNoteTask(String whiteboardUrl, EditText editText, BasicHttpContext context) {
			this.whiteboardUrl = whiteboardUrl;
			this.editText = editText;
			this.context = context;
		}

		protected String doInBackground(Void... v) {
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			String newText = editText.getText().toString();

			HttpPut whiteboardPut = new HttpPut(whiteboardUrl);
			try {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("whiteboard[text]", newText));
				whiteboardPut.setEntity(new UrlEncodedFormEntity(nvps,
						HTTP.UTF_8));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		
			try {
				HttpResponse resp = client.execute(whiteboardPut, context);
				HttpEntity entity = resp.getEntity();
				return entity.toString();
			} catch (IOException ioe) {
				return "oh no!";
			} finally {
				client.close();
			}
		}
	}

}