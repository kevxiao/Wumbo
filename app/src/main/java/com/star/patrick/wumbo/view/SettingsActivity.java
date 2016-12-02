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

public class SettingsActivity extends PreferenceActivity{
    private static final String userKey = "pref_user_name";
    private static final String userIntentKey = "new_name";
    private static final String defaultName = "Anonymous";

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
            if (key.equals(userKey)) {
                String newName = sharedPreferences.getString(userKey, defaultName);
                //Set default name
                if (newName.equals("")) {
                    newName = defaultName;
                }
                //Update display name in db
                DatabaseHandler db = new DatabaseHandler(this.getActivity(), null);
                User me = db.getMe();
                db.updateUserDisplayName(me.getId(), newName);
                Log.d("SE464", "New Name is: "+ newName);
                //Return intent to main activity
                Intent returnName = new Intent();
                returnName.putExtra(userIntentKey, newName);
                this.getActivity().setResult(RESULT_OK, returnName);
                this.getActivity().finish();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            //Register Listener to sharedPreferences changes
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            //Unregister Listener to sharedPreferences changes
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }
}
