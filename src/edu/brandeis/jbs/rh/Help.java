package edu.brandeis.jbs.rh;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Help extends Activity {
	private WebView webview;
	// "file:///android_asset" is a special URI that loads files
	// from the "/assets" folder.
	private final String ASSET_URI = "file:///android_asset";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		webview = new WebView(this);
		// Set entire activity window as WebView
		setContentView(webview);
		webview.setWebViewClient(new RHWebViewClient());
		webview.loadUrl(ASSET_URI + "/index.html");
	}

	/**
	 * Override BACK button so that user can go to previous pages rather than
	 * exiting activity.
	 * 
	 * @param keyCode
	 * @param event
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			webview.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private class RHWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
}