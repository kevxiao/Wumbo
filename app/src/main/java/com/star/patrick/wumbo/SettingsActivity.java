package com.star.patrick.wumbo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 * Created by jesse on 19/11/16.
 */

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        // listener implementation
                    }
                };

    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals("pref_user_name")) {
            DatabaseHandler db = new DatabaseHandler(getApplicationContext(), null, null, null);
        }
    }

}
