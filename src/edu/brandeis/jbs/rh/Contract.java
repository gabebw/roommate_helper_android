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

import android.app.ListActivity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class Contract extends ListActivity {
	public static final String CONTRACT_XML_URL = "https://roommate-helper.heroku.com/contract.xml";
	
	private String[] clauses;
	private CookieStore cookieStore;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cookieStore = new RoommateHelperHttpClient(this).login();	
	}

	public void onResume() {
		super.onResume();

		DownloadContractTask dct = new DownloadContractTask(CONTRACT_XML_URL);
		try {
			dct.execute();
			Document dom = dct.get();
			NodeList clauseNodes = dom.getElementsByTagName("clause");
			int numClauses = clauseNodes.getLength();
			clauses = new String[numClauses];
			
			for( int i = 0; i < numClauses; i++ ){
				Node clauseNode = clauseNodes.item(i);
				clauses[i] = clauseNode.getTextContent();
			}
			
			// http://developer.android.com/guide/tutorials/views/hello-listview.html
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, clauses));
			getListView().setTextFilterEnabled(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private class DownloadContractTask extends AsyncTask<Void, Void, Document> {
		private String url;

		public DownloadContractTask(String url) {
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
}