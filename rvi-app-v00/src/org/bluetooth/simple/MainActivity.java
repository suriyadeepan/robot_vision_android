package org.bluetooth.simple;

import org.opencv.samples.tutorial3.R;
import org.opencv.samples.tutorial3.Tank;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;



public class MainActivity extends MyActivity 
{
	
	// a TAG for debuggin
	public static final String TAG = "ocv";

	
	
	
	/*
	 * UI components
	 * 
	 * 1. search button
	 * 2. a button to start data transfer
	 * 
	 */
	Button btnSearchDevice;
	Button btnMonitor;
	
	
	// get the default bluetooth adapter
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();




	private Button btnVoiceController;




	private Button btnStartBtService;

	// onCreate() method starts here... 
	public void onCreate(Bundle savedInstanceState) 
	{
		Log.i(TAG, "Entered onCreate method");
		
		
		super.onCreate(savedInstanceState);
		
		// set layout as ./res/layout/main.xml
		setContentView(R.layout.main);

		/* 
		 *  Setup Buttons
		 */
		// Initialize search button using reference
		btnSearchDevice = (Button) findViewById(R.id.btnSearchDevice);
		// Handle button event
		btnSearchDevice.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) 
			{
				/*
				 *   should start the search activity
				 *    ./src/pkg_name/SearchDeviceActivity.java
				 *   
				 */
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, SearchDeviceActivity.class);
				startActivity(intent);
			}
		});

		
		// Initialize monitor button
		btnMonitor = (Button) findViewById(R.id.btnMonitor);
		// event handling
		btnMonitor.setOnClickListener(new OnClickListener() 
		{

			 
			public void onClick(View v) 
			{
				// enable default bt adapter
				_bluetooth.enable();
				
				// Start monitor activity
				//  ./src/pkg_name/MonitorActivity.java
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, MonitorActivity.class);
				startActivity(intent);
			}
		});
		
		
		// a button 2 start voice controller activity
		btnVoiceController=(Button)findViewById(R.id.btn_voice);
		btnVoiceController.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v1) 
			{
				/*
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, VoiceController.class);
				startActivity(intent);
				*/
				
				Log.i(TAG, "shld call VoiceController activity here...");
				
			}
		});
		
		btnStartBtService = (Button)findViewById(R.id.btn_bt_service);
		
		btnStartBtService.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
			
				Tank.setBtData("$fwd~");
				Tank.btDataSet=true;
				
				// TODO Auto-generated method stub
				Intent startBtService = new Intent(MainActivity.this,BtBufferServer.class);
				
				
			
				startService(new Intent(MainActivity.this,BtBufferServer.class));
				
			}
		});
		
	}
/*
	// when back key is pressed, the user is alerted
	//  with a dialog - yes or no - to quit
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		
		
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			dialog();
			return false;
		}
		return false;
	}

	// the dialog is built and displayed in this method
	protected void dialog() {
		AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
		build.setTitle(R.string.message);
		build.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					 
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if (_bluetooth.isEnabled()) {
							_bluetooth.disable();
						}
						SocketApplication app = (SocketApplication) getApplicationContext();
						app.setDevice(null);
						MainActivity.this.finish();
					}
				});
		build.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					 
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		build.create().show();
	}*/

	// onDestroy() method...
	//  called wen app is closed...
	public void onDestroy() 
	{
		super.onDestroy();
		//System.exit(0);
	}
}