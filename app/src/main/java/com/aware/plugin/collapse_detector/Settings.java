package com.aware.plugin.collapse_detector;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.aware.Aware;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String STATUS_PLUGIN_collapse_detector = "status_plugin_collapse_detector";

    public static final String ACCELEROMETER_DELAY_collapse_detector = "accelerometer_delays_collapse_detector";

    public static final String THRESHOLD_collapse_detector = "threshold_collapse_detector";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        syncSettings();
    }

    private void syncSettings() {
        //Make sure to load the latest values
        CheckBoxPreference status = (CheckBoxPreference) findPreference(STATUS_PLUGIN_collapse_detector);
        status.setChecked(Aware.getSetting(this, STATUS_PLUGIN_collapse_detector).equals("true"));
        ListPreference delays = (ListPreference) findPreference(ACCELEROMETER_DELAY_collapse_detector);

        //...
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference setting = (Preference) findPreference(key);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if( setting.getKey().equals(STATUS_PLUGIN_collapse_detector) ) {
            boolean is_active = sharedPreferences.getBoolean(key, false);
            Aware.setSetting(this, key, is_active);
            if( is_active ) {
                Aware.startPlugin(this, getPackageName());
            } else {
                Aware.stopPlugin(this, getPackageName());
            }
        }

        if(setting.getKey().equals(ACCELEROMETER_DELAY_collapse_detector)){


        }

        //Apply the new settings
        Intent apply = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(apply);
    }
}
