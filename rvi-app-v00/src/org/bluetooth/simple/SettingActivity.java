package org.bluetooth.simple;

import java.util.Locale;

import org.opencv.samples.tutorial3.R;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SettingActivity extends MyActivity {
	Spinner spLanguage;
	Button btnReturn;

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);

		spLanguage = (Spinner) findViewById(R.id.spLanguage);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.language, android.R.layout.simple_spinner_item);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spLanguage.setAdapter(adapter);
		Locale locale = Locale.getDefault();
		if (locale.equals(Locale.CHINA)) {
			spLanguage.setSelection(1);
		} else {
			spLanguage.setSelection(0);
		}
		spLanguage
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

					
					public void onItemSelected(AdapterView<?> parent, View v,
							int position, long id) {
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
						getBaseContext().getResources().updateConfiguration(
								config,
								getBaseContext().getResources()
										.getDisplayMetrics());
						SharedPreferences settings = getSharedPreferences(
								"locale", 0);
						settings.edit().putInt("Locale", position).commit();
					}

					
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

		btnReturn = (Button) findViewById(R.id.btnReturn);
		btnReturn.setOnClickListener(new OnClickListener() {

			
			public void onClick(View v) {
				finish();
			}
		});
	}
}