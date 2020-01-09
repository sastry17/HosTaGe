package de.tudarmstadt.informatik.hostage.ui.fragment;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.tudarmstadt.informatik.hostage.R;
import de.tudarmstadt.informatik.hostage.sync.android.SyncUtils;

/**
 * Manages and creates the application preferences view
 *
 * @author Alexander Brakowski
 * @created 02.03.14 21:03
*/
public class PreferenceHostageFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
	/**
	 * Contains preferences for which to display a preview of the value in the summary
	 */
	private HashSet<String> mPrefValuePreviewSet;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		// these preferences are all text preferences
		String[] textPreferences = new String[]{
				"pref_external_location",
				"pref_upload_server",
                "pref_download_server",
				"pref_max_connections",
				"pref_timeout",
				"pref_sleeptime",
				"pref_location_time",
				"pref_location_retries",
				"pref_portscan_timeout"
		};

		mPrefValuePreviewSet = new HashSet<String>();
		mPrefValuePreviewSet.add("pref_external_location");
		mPrefValuePreviewSet.add("pref_upload_server");

		addPreferencesFromResource(R.xml.settings_preferences);

		for(String k: textPreferences){
			updatePreferenceSummary(k);
		}
	}
	/**
	 * Updates the summary text of the given preference
	 *
	 * @param key the preference key
	 */
	private void updatePreferenceSummary(String key){
		Preference p = findPreference(key);
		SharedPreferences sharedPreferences = this.getPreferenceManager().getSharedPreferences();

		if(p != null && p instanceof EditTextPreference) {
			if (mPrefValuePreviewSet.contains(key)) {
				p.setSummary(sharedPreferences.getString(key, ""));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

/*	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		super.onPreferenceTreeClick(preferenceScreen, preference);

		if (preference instanceof PreferenceScreen) {
			if(MainActivity.getInstance().mDisplayedFragment != null && MainActivity.getInstance().mDisplayedFragment instanceof UpNavigatible){
				((UpNavigatible) MainActivity.getInstance().mDisplayedFragment).setUpNavigatible(true);
				((UpNavigatible) MainActivity.getInstance().mDisplayedFragment).setUpFragment(SettingsFragment.class);
				MainActivity.getInstance().setDrawerIndicatorEnabled(false);
				return true;
			}
		}

		return false;
	}*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updatePreferenceSummary(key);
	}
}
