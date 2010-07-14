package edu.brandeis.jbs.rh;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.BasicHttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import edu.brandeis.jbs.rh.RoommateHelperHttpClient;

public class Whiteboards extends Activity {
	public static final String WHITEBOARDS_XML_URL = "https://roommate-helper.heroku.com/whiteboards.xml";

	private LinearLayout whiteboardsLayout;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.whiteboards);
		whiteboardsLayout = (LinearLayout) findViewById(R.id.whiteboards_layout);
	}
	
	public void onResume() {
		super.onResume();
		DownloadWhiteboardsTask dwb = new DownloadWhiteboardsTask(this);
		try {
			dwb.execute();
			Document dom = dwb.get();
			NodeList whiteboardNodes = dom.getElementsByTagName("whiteboard");
			int numWhiteboards = whiteboardNodes.getLength();
			
			/*
			 * For each whiteboard, insert a TextView containing the whiteboard's name
			 * into the layout inside whiteboards.xml
			 */
			
			LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT );
			for( int i = 0; i < numWhiteboards; i++ ){
				Node whiteboard = whiteboardNodes.item(i);
				NodeList whiteboardChildNodes = whiteboard.getChildNodes();
				String whiteboardName = "";
				// Loop through each tag inside <whiteboard> until we get to <name>
				for( int j = 0; j < whiteboardChildNodes.getLength(); j++){
					String nodeName = whiteboardChildNodes.item(j).getNodeName();
					if( nodeName.equals("name") ){
						whiteboardName = whiteboardChildNodes.item(j).getTextContent();
						break;
					}
				}
				// Create a TextView with the whiteboard name and add it to the layout inside whiteboards.xml
				TextView textView = new TextView(this);
				textView.setText(whiteboardName);
				textView.setLayoutParams(tlp);
				whiteboardsLayout.addView(textView);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private class DownloadWhiteboardsTask extends AsyncTask<Void, Void, Document> {
		private CookieStore cookieStore;
		public DownloadWhiteboardsTask(Context whiteboardsContext){
			RoommateHelperHttpClient rhClient = new RoommateHelperHttpClient(whiteboardsContext);
			cookieStore = rhClient.login();
		}
		
		protected Document doInBackground(Void... v) {
			AndroidHttpClient client = AndroidHttpClient.newInstance("RoommateHelperClient");
			BasicHttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
			
			// set up a request object for the passed in URL
			HttpGet req = new HttpGet(WHITEBOARDS_XML_URL);
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

}
