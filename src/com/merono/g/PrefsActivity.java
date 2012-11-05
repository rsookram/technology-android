package com.merono.g;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class PrefsActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		if (arg1.equals("board")) {
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(this);
			Log.d("Prefs", "" + pref.getString("board", "g"));
		}
	}
}
