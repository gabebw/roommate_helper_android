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

import android.app.ListActivity;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import edu.brandeis.jbs.rh.RoommateHelperHttpClient;

public class Whiteboards extends ListActivity {
	public static final String WHITEBOARDS_XML_URL = "https://roommate-helper.heroku.com/whiteboards.xml";

	private ArrayAdapter<WhiteboardHolder> adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.whiteboards);

		ArrayList<WhiteboardHolder> whiteboards = new ArrayList<WhiteboardHolder>();
        adapter = new ArrayAdapter<WhiteboardHolder>(this, android.R.layout.simple_list_item_1, whiteboards);
		setListAdapter(adapter);
        
		// Turn on text filter so that when user begins typing, the list will be filtered
		getListView().setTextFilterEnabled(true);
	}
	
	public void onListItemClick(ListView l, View v, int position, long id){
		WhiteboardHolder whiteboard = adapter.getItem(position);
		Intent whiteboardIntent = new Intent(this, Whiteboard.class);
		//Store the whiteboard ID to pass it to the Whiteboard activity
		whiteboardIntent.putExtra("whiteboard_id", whiteboard.id);
		startActivity(whiteboardIntent);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// If we don't clear, every time we resume we get N more whiteboards, where N = how many whiteboards there really are
		adapter.clear();
        
		DownloadWhiteboardsTask dwb = new DownloadWhiteboardsTask();
		try {
			dwb.execute();
			Document dom = dwb.get();;
			NodeList whiteboardNodes = dom.getElementsByTagName("whiteboard");
			int numWhiteboards = whiteboardNodes.getLength();
			
			// Don't notify the View that we added whiteboards.
			// Once all whiteboards are loaded, then we manually call this.
			adapter.setNotifyOnChange(false);
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
				adapter.add(whiteboard);
			}
			// All whiteboards are loaded, notify the View.
			adapter.notifyDataSetChanged();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private class DownloadWhiteboardsTask extends AsyncTask<Void, Void, Document> {
		private CookieStore cookieStore;
		private RoommateHelperHttpClient rhClient;
		
		public DownloadWhiteboardsTask(){
			rhClient = new RoommateHelperHttpClient(Whiteboards.this);
		}
		
		protected Document doInBackground(Void... v) {
			cookieStore = rhClient.login();

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
