package org.bluetooth.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import org.opencv.samples.tutorial3.R;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MonitorActivity extends MyActivity 
{
	
	// commands to arduino for
	//  motor direction control
	static final String FORWARD = "$fwd~";
	static final String BACKWARD = "$bwd~";
	static final String LEFT = "$lft~";
	static final String RIGHT = "$rgt~";
	static final String STOP = "$stp~";
	
	
	private static final int REQUEST_DISCOVERY = 0x1;;
	private final String TAG = "ocv";
	private Handler _handler = new Handler();
	private final int maxlength = 2048;
	private BluetoothDevice device = null;
	private BluetoothSocket socket = null;
	
	public String direction="";

	
	private OutputStream outputStream;
	private InputStream inputStream;
	
	private Object obj1 = new Object();
	private Object obj2 = new Object();
	public static boolean canRead = true;

	public static StringBuffer hexString = new StringBuffer();
	
	Button fwdButton;
	Button leftButton;
	Button bwdButton;
	Button rightButton;
	public boolean isSynced = false;

	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.monitor);
		
		setupViews();
	
		BluetoothDevice finalDevice = this.getIntent().getParcelableExtra(
				BluetoothDevice.EXTRA_DEVICE);
		SocketApplication app = (SocketApplication) getApplicationContext();
		device = app.getDevice();
		Log.d(TAG, "test1");
		if (finalDevice == null) {
			if (device == null) {
				Log.d(TAG, "test2");
				Intent intent = new Intent(this, SearchDeviceActivity.class);
				startActivity(intent);
				finish();
				return;
			}
			Log.d(TAG, "test4");
		} else if (finalDevice != null) {
			Log.d(TAG, "test3");
			app.setDevice(finalDevice);
			device = app.getDevice();
		}
		new Thread() {
			public void run() {
				connect(device);
			};
		}.start();
		
		
		
		
	}

	
	
	private void setupViews() 
	{
		fwdButton=(Button)findViewById(R.id.btnFwd);
		bwdButton=(Button)findViewById(R.id.btn_back);
		leftButton=(Button)findViewById(R.id.btn_left);
		rightButton=(Button)findViewById(R.id.btn_right);
		
		//fwdButton.setOn
		
		fwdButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) 
			{
				direction=FORWARD;
				
				
				sendMethod();
				
				
			}
		});

		
bwdButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) 
			{
				direction=BACKWARD;
				sendMethod();
				
			}
		});
		
rightButton.setOnClickListener(new View.OnClickListener() {
	
	public void onClick(View arg0) 
	{
		direction=RIGHT;
		sendMethod();
		
	}
});

leftButton.setOnClickListener(new View.OnClickListener() {
	
	public void onClick(View arg0) 
	{
		direction=LEFT;
		sendMethod();
		
	}
});
		
		
	}

	// start of sendMethod()...
	private void sendMethod() 
	{
		
		
		
		
		String tempHex = "";
		byte bytes[] = direction.getBytes();
	
		try {
			if (outputStream != null) {
				synchronized (obj2) {
					
					isSynced  = true;
					
					//outputStream.write(bytes);
					outputStream.write(bytes);
				}
			} else {
				
				
				Toast.makeText(getBaseContext(),
						getResources().getString(R.string.wait),
						Toast.LENGTH_SHORT).show();
			}
		} catch (IOException e) {
			Log.e(TAG, ">>", e);
			e.printStackTrace();
		}
		
		new Thread(){
			public void run(){
				String editText = direction;
				String tempHex = "";
				
				String hex = hexString.toString();
				if (hex == "") {
					hexString.append("-->");
				} else {
					if (hex.lastIndexOf("-->") < hex.lastIndexOf("<--")) {
						hexString.append("\n-->");
					}
				}
				hexString.append(tempHex);
				hex = hexString.toString();
				if (hex.length() > maxlength) {
					try {
						hex = hex.substring(hex.length() - maxlength, hex.length());
						hex = hex.substring(hex.indexOf(" "));
						hex = "-->" + hex;
						hexString = new StringBuffer();
						hexString.append(hex);
					} catch (Exception e) {
						e.printStackTrace();
						Log.e(TAG, "e", e);
					}
				}
				final String showStr;
				
					showStr=bufferStrToHex(hexString.toString(),false).trim();
				
				_handler.post(new Runnable(){
					
					public void run() {
	            
					}
				});
			}
		}.start();


		
	} // end of send method...



	/* after select, connect to device */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != REQUEST_DISCOVERY) {
			finish();
			return;
		}
		if (resultCode != RESULT_OK) {
			finish();
			return;
		}
		final BluetoothDevice device = data
				.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		new Thread() {
			public void run() {
				connect(device);
			};
		}.start();
	}

	protected void onDestroy() {
		super.onDestroy();
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			Log.e(TAG, ">>", e);
		}
	}

	protected void connect(BluetoothDevice device) {
		try {
			Log.d(TAG, "��������");
			// Create a Socket connection: need the server's UUID number of
			// registered
			Method m = device.getClass().getMethod("createRfcommSocket",
					new Class[] { int.class });
			socket = (BluetoothSocket) m.invoke(device, 1);
			socket.connect();
			Log.d(TAG, ">>Client connectted");
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			int read = -1;
			final byte[] bytes = new byte[2048];
			while (true) {
				synchronized (obj1) {
					read = inputStream.read(bytes);
					Log.d(TAG, "read:" + read);
					if (read > 0) {
						final int count = read;
						String str = SamplesUtils.byteToHex(bytes, count);
//						Log.d(TAG, "test1:" + str);
						String hex = hexString.toString();
						if (hex == "") {
							hexString.append("<--");
						} else {
							if (hex.lastIndexOf("<--") < hex.lastIndexOf("-->")) {
								hexString.append("\n<--");
							}
						}
						hexString.append(str);
						hex = hexString.toString();
//						Log.d(TAG, "test2:" + hex);
						if (hex.length() > maxlength) {
							try {
								hex = hex.substring(hex.length() - maxlength,
										hex.length());
								hex = hex.substring(hex.indexOf(" "));
								hex = "<--" + hex;
								hexString = new StringBuffer();
								hexString.append(hex);
							} catch (Exception e) {
								e.printStackTrace();
								Log.e(TAG, "e", e);
							}
						}
						_handler.post(new Runnable() {
							public void run() {
								/*
									sTextView
											.setText(bufferStrToHex(
													hexString.toString(), false)
													.trim());
								
								Log.d(TAG, "ScrollY: " + mScrollView.getScrollY());   
					            int off = sTextView.getMeasuredHeight() - mScrollView.getHeight();   
					            if (off > 0) {   
					                mScrollView.scrollTo(0, off);   
					            } */   
							}
						});
					}
				}
			}

		} catch (Exception e) {
			Log.e(TAG, ">>", e);
			Toast.makeText(getBaseContext(),
					getResources().getString(R.string.ioexception),
					Toast.LENGTH_SHORT).show();
			return;
		} finally {
			if (socket != null) {
				try {
					Log.d(TAG, ">>Client Socket Close");
					socket.close();
					socket = null;
					// this.finish();
					return;
				} catch (IOException e) {
					Log.e(TAG, ">>", e);
				}
			}
		}
	}

	public String bufferStrToHex(String buffer, boolean flag) {
		String all = buffer;
		StringBuffer sb = new StringBuffer();
		String[] ones = all.split("<--");
		for (int i = 0; i < ones.length; i++) {
			if (ones[i] != "") {
				String[] twos = ones[i].split("-->");
				for (int j = 0; j < twos.length; j++) {
					if (twos[j] != "") {
						if (flag) {
							sb.append(SamplesUtils.stringToHex(twos[j]));
						} else {
							sb.append(SamplesUtils.hexToString(twos[j]));
						}
						if (j != twos.length - 1) {
							if (sb.toString() != "") {
								sb.append("\n");
							}
							sb.append("-->");
						}
					}
				}
				if (i != ones.length - 1) {
					if (sb.toString() != "") {
						sb.append("\n");
					}
					sb.append("<--");
				}
			}
		}
		return sb.toString();
	}

}