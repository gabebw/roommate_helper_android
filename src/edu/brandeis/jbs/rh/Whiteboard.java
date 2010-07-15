package edu.brandeis.jbs.rh;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.http.AndroidHttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import edu.brandeis.jbs.rh.RoommateHelperHttpClient;

public class Whiteboard extends Activity implements OnClickListener {
	private Button saveButton;
	private EditText editText;
	private TextView lastUpdatedText;
	
	private CookieStore cookieStore;
	
	private String whiteboardUrl;
	
	private RoommateHelperHttpClient rhClient;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * The Whiteboards (plural) activity places the whiteboard id inside
		 * a Bundle inside the Intent extras. So, we grab it.
		 */
		int whiteboardId = this.getIntent().getIntExtra("whiteboard_id", 0);
		
		setContentView(R.layout.whiteboard);
		
		rhClient = new RoommateHelperHttpClient(this);
		
		cookieStore = rhClient.login();
		
		whiteboardUrl = "https://roommate-helper.heroku.com/whiteboards/" + whiteboardId + ".xml";

		editText = (EditText) findViewById(R.id.whiteboard_content);
		lastUpdatedText = (TextView) findViewById(R.id.whiteboard_last_updated);

		saveButton = (Button) findViewById(R.id.save_whiteboard_button);
		saveButton.setOnClickListener(this);
	}

	public void onResume() {
		super.onResume();

		DownloadNoteTask dnt = new DownloadNoteTask(whiteboardUrl);
		try {
			dnt.execute();
			Document dom = dnt.get();
			String text = dom.getElementsByTagName("text").item(0).getTextContent();
			String lastUpdatedAtInWords = dom.getElementsByTagName("last_updated_time_in_words").item(0).getTextContent();
			lastUpdatedText.setText("last updated " + lastUpdatedAtInWords + " ago");
			editText.setText(text);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onClick(View v) {
		UpdateNoteTask unt = new UpdateNoteTask(whiteboardUrl, editText, cookieStore);
		try {
			unt.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private class DownloadNoteTask extends AsyncTask<Void, Void, Document> {
		private String url;

		public DownloadNoteTask(String url) {
			this.url = url;
		}

		protected Document doInBackground(Void... v) {
			AndroidHttpClient client = AndroidHttpClient.newInstance("RoommateHelperClient");
			BasicHttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
			
			// set up a request object for the passed in URL
			HttpGet req = new HttpGet(this.url);
			req.addHeader("Accept", "text/xml");

			try {
				// actually make the request
				HttpResponse resp = client.execute(req, context);
				HttpEntity entity = resp.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					try {
						DocumentBuilder dombuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
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

	private class UpdateNoteTask extends AsyncTask<Void, Void, Integer> {
		private String whiteboardUrl;
		private EditText editText;
		private CookieStore cookieStore;

		public UpdateNoteTask(String whiteboardUrl, EditText editText, CookieStore cookieStore) {
			this.whiteboardUrl = whiteboardUrl;
			this.editText = editText;
			this.cookieStore = cookieStore;
		}

		
		/**
		 * Returns the Integer status code result of the request (e.g. 422, or 200).
		 * If an exception occurred, returns 1.
		 */
		protected Integer doInBackground(Void... v) {
			AndroidHttpClient client = AndroidHttpClient.newInstance("RoommateHelperClient");
			BasicHttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

			String newText = editText.getText().toString();

			HttpPut whiteboardPut = new HttpPut(whiteboardUrl);
			try {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("whiteboard[text]", newText));
				whiteboardPut.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			try {
				HttpResponse resp = client.execute(whiteboardPut, context);
				client.close();

				int statusCode = resp.getStatusLine().getStatusCode();
				return statusCode;
			} catch (IOException ioe) {
				client.close();
				return 1;
			} finally {
				client.close();
			}
		}
		
		protected void onPostExecute(Integer statusCode){
			Toast toast;

			if( statusCode == 200 ){
				toast = Toast.makeText(Whiteboard.this, "Successfully updated whiteboard.", Toast.LENGTH_SHORT);
				// A bit of a cheat, but that's OK.
				lastUpdatedText.setText("last updated less than a minute ago");
			} else {
				// Probably either 422 (Unprocessable Entity) or 1 (general error)
				toast = Toast.makeText(Whiteboard.this, "Oops! Something went wrong. Please try again.", Toast.LENGTH_LONG);
			}
			toast.show();
		}
	}

}