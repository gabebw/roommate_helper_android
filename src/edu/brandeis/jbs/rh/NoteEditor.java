package edu.brandeis.jbs.rh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
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

public class NoteEditor extends Activity implements OnClickListener {
	private Button saveButton;
	private EditText editText;
	private BasicHttpContext context;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.noteeditor);

		// Set up HTTP client and cookie store
		context = new BasicHttpContext();
		context.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());

		editText = (EditText) findViewById(R.id.note_content);

		saveButton = (Button) findViewById(R.id.save_whiteboard_button);
		saveButton.setOnClickListener(this);
	}

	public void onStart() {
		super.onStart();

		// this is just for testing purposes
		String url = "https://roommate-helper.heroku.com/households/822834891/whiteboard";

		DownloadNoteTask dnt = new DownloadNoteTask(url, context);
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
		UpdateNoteTask unt = new UpdateNoteTask(editText, context);
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

		public DownloadNoteTask(String url, BasicHttpContext context) {
			this.url = url;
			this.context = context;
		}

		protected Document doInBackground(Void... v) {
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");

			// log in (create a new user session)
			HttpPost login = new HttpPost(
					"https://roommate-helper.heroku.com/user_sessions");
			String email = "singingwolfboy@gmail.com";
			String password = "secure";

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
			HttpGet req = new HttpGet(this.url);
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
		private EditText editText;
		private BasicHttpContext context;

		public UpdateNoteTask(EditText editText, BasicHttpContext context) {
			this.editText = editText;
			this.context = context;
		}

		protected String doInBackground(Void... v) {
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");

			String newText = editText.getText().toString();

			HttpPut whiteboardPut = new HttpPut(
					"https://roommate-helper.heroku.com/households/822834891/whiteboard");
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