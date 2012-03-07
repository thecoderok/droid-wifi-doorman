package ua.org.bytes.android.wifibutler.listeners;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import ua.org.bytes.android.wifibutler.EpamWiFiDoormanActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class ConnectionChangeReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null) {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(context);
			String network = preferences.getString(
					EpamWiFiDoormanActivity.NETWORK,
					EpamWiFiDoormanActivity.NO_VALUE);
			if (!network.equalsIgnoreCase(EpamWiFiDoormanActivity.NO_VALUE)) {
				WifiManager wifiMgr = (WifiManager) context
						.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

				if (activeNetInfo.getTypeName().equalsIgnoreCase("WIFI")) {
					if (wifiInfo.getSSID().equalsIgnoreCase(network)) {
						//If check connection enabled in preferences
						//How to ceck connection - try to open some URL and then check result URL.
						//If URL is different - we were redirected
						boolean checkConnection = preferences.getBoolean(
								EpamWiFiDoormanActivity.CHECK_CONNECTION,
								false);
						if (checkConnection){
							// Check inet connection without redirects
							if (isConnected())
								return; // If already connected to internet - do
										// nothing
						}
						Log.i(EpamWiFiDoormanActivity.LOG_TAG,
								"Launching activity");
						Intent i = new Intent(context,
								EpamWiFiDoormanActivity.class);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						i.putExtra(EpamWiFiDoormanActivity.AUTOLOGIN, true);
						context.startActivity(i);
					}
				}
			}
		}
	}

	private boolean isConnected() {
		boolean result = false;
		Log.i(EpamWiFiDoormanActivity.LOG_TAG, "Started Inet connection check");
		try{
			URL url = new URL("http://www.android.com/");
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			try {
				InputStream in = new BufferedInputStream(
						urlConnection.getInputStream());
				if (!url.getHost().equals(urlConnection.getURL().getHost())) {
					//Using method described here: http://developer.android.com/reference/java/net/HttpURLConnection.html
					result = false;
				} else {
					result = true;
				}
			} finally {
				urlConnection.disconnect();
				Log.i(EpamWiFiDoormanActivity.LOG_TAG, "Ended Inet connection check, result = " + result);
			}
			return result;
		} catch(Exception ex){
			Log.e(EpamWiFiDoormanActivity.LOG_TAG, "Exception during inet conenction check: " + ex.getMessage());
			return false;
		}
	}
}