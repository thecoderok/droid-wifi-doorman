package ua.org.bytes.android.wifibutler;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity {
	SharedPreferences preferences;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    
	    preferences = PreferenceManager.getDefaultSharedPreferences(this);
	    boolean isEnabled = preferences.getBoolean(EPAMWiFiDoormanActivity.USE_CUSTOM_HOST, false);
	    getPreferenceScreen().findPreference(EPAMWiFiDoormanActivity.LOGIN_HOST).setEnabled(!isEnabled);
	    getPreferenceScreen().findPreference(EPAMWiFiDoormanActivity.CUSTOM_HOST).setEnabled(isEnabled);
	    getPreferenceScreen().findPreference(EPAMWiFiDoormanActivity.USE_CUSTOM_HOST).setOnPreferenceChangeListener(pref_click);
	}
	private OnPreferenceChangeListener pref_click = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean isEnabled = preferences.getBoolean(EPAMWiFiDoormanActivity.USE_CUSTOM_HOST, true);
		    getPreferenceScreen().findPreference(EPAMWiFiDoormanActivity.LOGIN_HOST).setEnabled(isEnabled);
		    getPreferenceScreen().findPreference(EPAMWiFiDoormanActivity.CUSTOM_HOST).setEnabled(!isEnabled);
		    return true;
		}
	};

}
