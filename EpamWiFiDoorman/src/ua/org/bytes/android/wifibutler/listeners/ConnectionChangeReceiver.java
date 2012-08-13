package ua.org.bytes.android.wifibutler.listeners;
//For connection status processing
import ua.org.bytes.android.wifibutler.EPAMWiFiDoormanActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
//import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.util.Log;
//For connection check
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;

public class ConnectionChangeReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
                //Check Action to be sure
                if (!intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) return;
                //Get network SSID from preferences and return if it is not specified
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String network = preferences.getString(EPAMWiFiDoormanActivity.NETWORK,EPAMWiFiDoormanActivity.NO_VALUE);
                if (network.equalsIgnoreCase(EPAMWiFiDoormanActivity.NO_VALUE)) return;
  		//Get active network connection info
//		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		NetworkInfo activeNetInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		//Check if active connection is available, connected and its type is WiFi
		if (activeNetInfo == null || !activeNetInfo.isAvailable() || !activeNetInfo.isConnected() || !activeNetInfo.getTypeName().equalsIgnoreCase("WIFI")) return;
                //Get current Wi-Fi connection info
		WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                //Check Wi-Fi network SSID, if connected
		if (wifiInfo == null || !wifiInfo.getSSID().equalsIgnoreCase(network)) return;
		//Check Inet connection if it is enabled in preferences
		boolean checkConnection = preferences.getBoolean(EPAMWiFiDoormanActivity.CHECK_CONNECTION,false);
                if (checkConnection && isConnected()) return;
                //Start WiFiDoorman
		Log.i(EPAMWiFiDoormanActivity.LOG_TAG,"Launching activity");
		Intent i = new Intent(context,EPAMWiFiDoormanActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.putExtra(EPAMWiFiDoormanActivity.AUTOLOGIN, true);
		context.startActivity(i);
	}
        private boolean isConnected() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://clients3.google.com/generate_204");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setInstanceFollowRedirects(false);
                    urlConnection.setConnectTimeout(10000);
                    urlConnection.setReadTimeout(10000);
                    urlConnection.setUseCaches(false);
                    urlConnection.getInputStream();
                    return urlConnection.getResponseCode() == 204;
                } catch (IOException e) {
                    Log.e(EPAMWiFiDoormanActivity.LOG_TAG,"Walled garden check - probably not a portal: exception " + e);
                    return false;
                } finally {
                    if (urlConnection != null) urlConnection.disconnect();
                }
        }
}
