package com.star.patrick.wumbo.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.star.patrick.wumbo.DatabaseHandler;
import com.star.patrick.wumbo.R;
import com.star.patrick.wumbo.model.User;

/**
 * Created by jesse on 19/11/16.
 */

public class SettingsActivity extends PreferenceActivity{
    Intent returnIntent = new Intent();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key   ) {
            if (key.equals("pref_user_name")) {
                DatabaseHandler db = new DatabaseHandler(this.getActivity(), null);
                User me = db.getMe();
                String newName = sharedPreferences.getString("pref_user_name", "Anonymous");
                if (newName.equals("")) {
                    newName = getResources().getString(R.string.default_name);
                }
                db.updateUserDisplayName(me.getId(), newName);
                Log.d("SE464", "New Name is: "+ newName);
                Intent returnName = new Intent();
                returnName.putExtra("new_name", newName);
                this.getActivity().setResult(RESULT_OK, returnName);
                this.getActivity().finish();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }
}
