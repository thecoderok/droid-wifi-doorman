package ua.org.bytes.android.wifibutler;

import java.util.LinkedList;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;

import java.util.Queue;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class EPAMWiFiDoormanActivity extends Activity {
	public static final String CUSTOM_HOST = "customHost";
	public static final String LOGIN_HOST = "loginHost";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String USE_CUSTOM_HOST = "useCustomHost";
	public static final String NETWORK = "network";
	public static final String LOG_TAG = "wifiDoorman";
	SharedPreferences preferences;
	public static final String DEFAULT_CISCO_HOST = "https://wifi.epam.com/"; // by Maksymenko
	public static final String NO_VALUE = "N/A";
	public static final String AUTOLOGIN = "AUTOLOGIN";
	public static final String CHECK_CONNECTION = "isCheckConnection";
	private static Queue<String> wfQueue = new LinkedList<String>();
	WebView webView;
	private boolean autologin = false;
	
	private String p_userName = NO_VALUE;
	private String p_password = NO_VALUE;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.main);
		Bundle extraInfo = getIntent().getExtras();
		if (extraInfo != null) {
			autologin = extraInfo.getBoolean(AUTOLOGIN);
		} else {
			autologin = false;
		}

		Button button = (Button) findViewById(R.id.loginButton);
		button.setOnClickListener(new OnClickListenerImpl());

		button = (Button) findViewById(R.id.settings);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showPropertiesActivity();
			}
		});

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		webView = (WebView) findViewById(R.id.webView);

		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				EPAMWiFiDoormanActivity.this.setProgress(progress * 100);
			}
		});
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();
			}

			public void onPageFinished(WebView view, String url) {
				if (wfQueue.size() != 0) {
					String scr = wfQueue.poll();
					//Log.i(LOG_TAG, "Loading next URL");
					webView.loadUrl(scr);

				}
			}
		});

		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

		if (autologin){
			Button loginButton = (Button) findViewById(R.id.loginButton);
			loginButton.performClick();
		}else{
			webView.loadDataWithBaseURL("", epamLogo, "text/html", "UTF-8", "");
		}
	}

	private String getCiscoHost() {
		boolean isCustomHost = preferences.getBoolean(USE_CUSTOM_HOST, false);
		if (!isCustomHost)
			return preferences.getString(LOGIN_HOST, NO_VALUE);
		else
			return preferences.getString(CUSTOM_HOST, NO_VALUE);
	}

	private void showPropertiesActivity() {
		Intent i = new Intent(EPAMWiFiDoormanActivity.this,
				PreferencesActivity.class);
		startActivity(i);
	}

	class OnClickListenerImpl implements OnClickListener {

		public void onClick(View view) {
			String ciscoHost = getCiscoHost();
			String userName = preferences.getString(USERNAME, NO_VALUE);
			String password = preferences.getString(PASSWORD, NO_VALUE);
			Log.i(LOG_TAG, password);
			System.out.println(password);
			if (userName.equalsIgnoreCase(NO_VALUE)
					|| password.equalsIgnoreCase(NO_VALUE)
					|| ciscoHost.equalsIgnoreCase(NO_VALUE)) {
				Toast.makeText(
						EPAMWiFiDoormanActivity.this,
						"Please, specify your EPAM office, username and password in properties",
						Toast.LENGTH_LONG).show();
				showPropertiesActivity();
				return;
			}
			p_userName = userName;
			p_password = password; 
			wfQueue.clear();
			wfQueue.add(checkScript);
			wfQueue.add(loginScript.replace("${username}", userName).replace(
					"${password}", password));
			if (autologin){
				wfQueue.add(finishActivity);
			}

			Log.i(LOG_TAG, "Opening Cisco Net Access Host " + ciscoHost);
			webView.loadUrl(ciscoHost + "logout.html");
		}
	}

	private String checkScript = "javascript: if(document.forms[0].userStatus.value != 0){ window.HTMLOUT.loggedIn(); }"; //  by Maksymenko
	private String loginScript = "javascript:var els = document.getElementsByName('username'); "
			+ "var strPassword =window.HTMLOUT.getPassword();"
			+ "var strUsername =window.HTMLOUT.getUserName();"
			+ "els[0].value = strUsername; "
			+ " var els = document.getElementsByName('password'); "
			+ " els[0].value = strPassword; "
			+ " var els = document.getElementsByName('Submit'); " // by Maksymenko
			+ " els[0].click();";
	private String epamLogo = "<html><head> <table width=\"100%\" height=\"100%\" align=\"center\" valign=\"center\"> <tr><td>"
			+ "<center><img src='file:///android_asset/logo.gif' /></center>"
			+ "</td></tr></table></head><html>";
	private String finishActivity = "javascript: window.HTMLOUT.finishActivity();";

	class MyJavaScriptInterface {
		public void loggedIn() {
			EPAMWiFiDoormanActivity.wfQueue.clear();
			//Toast.makeText(EPAMWiFiDoormanActivity.this,
			//		"You already logged in", Toast.LENGTH_LONG).show();
			if (autologin){
				finishActivity();
			}
		}

		public void finishActivity(){
			finish();
		}
		
		public String getPassword(){
			return EPAMWiFiDoormanActivity.this.getPassword();
		}
		
		public String getUserName(){
			return EPAMWiFiDoormanActivity.this.getUserName();
		}
	}

	public String getUserName() {
		return p_userName;
	}

	public String getPassword() {
		return p_password;
	}
}
