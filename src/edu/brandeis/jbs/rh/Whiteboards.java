package edu.brandeis.jbs.rh;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import edu.brandeis.jbs.rh.RoommateHelperHttpClient;

public class Whiteboards extends ListActivity {
	public static final String WHITEBOARDS_XML_URL = "https://roommate-helper.heroku.com/whiteboards.xml";

	ArrayList<WhiteboardHolder> whiteboards;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		whiteboards = new ArrayList<WhiteboardHolder>();
	}
	
	public void onListItemClick(ListView l, View v, int position, long id){
		WhiteboardHolder whiteboard = whiteboards.get(position);
		Intent whiteboardIntent = new Intent(this, Whiteboard.class);
		//Store the whiteboard ID to pass it to the Whiteboard activity
		whiteboardIntent.putExtra("whiteboard_id", whiteboard.id);
		startActivity(whiteboardIntent);
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
			 * Get the name of each whiteboard and insert it into an ArrayList. Then pass that ArrayList
			 * to an ArrayAdapter to create our ListView.
			 */
			for( int i = 0; i < numWhiteboards; i++ ){
				Node whiteboardNode = whiteboardNodes.item(i);
				NodeList whiteboardChildNodes = whiteboardNode.getChildNodes();
				WhiteboardHolder whiteboard = new WhiteboardHolder();
				
				// Loop through each tag inside <whiteboard> until we get to <name>
				for( int j = 0; j < whiteboardChildNodes.getLength(); j++){
					Node whiteboardChildNode = whiteboardChildNodes.item(j);
					String nodeName = whiteboardChildNode.getNodeName();
					if( nodeName.equals("name") ){
						whiteboard.name = whiteboardChildNode.getTextContent();
					} else if( nodeName.equals("id") ){
						whiteboard.id = Integer.valueOf(whiteboardChildNode.getTextContent());
					}
				}	
				whiteboards.add(whiteboard);
			}
			// http://developer.android.com/guide/tutorials/views/hello-listview.html
			setListAdapter(new ArrayAdapter<WhiteboardHolder>(this, android.R.layout.simple_list_item_1, whiteboards));
			getListView().setTextFilterEnabled(true);
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
	
	/**
	 * Just a little class to store the whiteboard's name and ID.
	 * @author gabe
	 */
	private class WhiteboardHolder {
		public String name;
		public int id;
		
		// Provide a toString so that the ListAdapter displays the items correctly.
		public String toString(){
			return name;
		}
	}

}
