package org.bluetooth.simple;

import java.util.Locale;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

public class MyActivity extends Activity {
	/* Get Default Adapter */
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		refresh();
		_bluetooth.enable();
	}

	public void refresh() {
		SharedPreferences settings = getSharedPreferences("locale", 0);
		int position = settings.getInt("Locale", 0);
		Locale locale2;
		Configuration config = new Configuration();
		if (position == 0) {
			locale2 = Locale.ENGLISH;
			Locale.setDefault(locale2);
			config.locale = locale2;
		} else if (position == 1) {
			locale2 = Locale.ENGLISH;
			Locale.setDefault(locale2);
			config.locale = locale2;
		}
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());
	}
}
