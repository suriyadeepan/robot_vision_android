package org.bluetooth.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import org.opencv.samples.tutorial3.R;
import org.opencv.samples.tutorial3.Tank;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

public class BtBufferServer extends Service
{
	
	
	
private static final String tag = "ocv";

	
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
		
		//public String direction="";
		public String direction=LEFT;

		
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

	
	
	
	
	
	
	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();


	public boolean isConnected=false;
    
	 /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public BtBufferServer getService() {
            // Return this instance of LocalService so clients can call public methods
            return BtBufferServer.this;
        }
    }
	
	
	
	

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		
		Log.i(tag,this.getClass().toString()+"Inside onStartCommand()");
		

		
		BluetoothDevice finalDevice = intent.getParcelableExtra(
				BluetoothDevice.EXTRA_DEVICE);
		SocketApplication app = (SocketApplication) getApplicationContext();
		device = app.getDevice();
		Log.d(TAG, "test1");
		if (finalDevice == null) {
			if (device == null) {
				Log.d(TAG, "test2");
				Intent openSearchDev = new Intent(this, SearchDeviceActivity.class);
				startActivity(intent);
				this.stopSelf();
				
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

		
/*
		if(Tank.btDataSet)
			{
				direction=Tank.getBtData();
				Tank.btDataSet=false;
			
				sendMethod(direction);
				
				
				Log.i(tag,"Dir: "+direction);
			}
	*/	
		return super.onStartCommand(intent, flags, startId);
		
		
			
	}
	
	

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		Log.i(tag,this.getClass().toString()+"Inside onCreate()");
	
		

	}

	
	
	// start of sendMethod()...
		public void issueCmd(String dir) 
		{
			
			Log.i(tag,"Inside sendMethod: "+dir);
			
			direction = dir;
			
			//String tempHex = "";
			byte bytes[] = dir.getBytes();
		
			try {
				if (outputStream != null) {
					synchronized (obj2) {
						
						isSynced  = true;
						Log.i(tag,"isSynced: true");
						
						
						outputStream.write(bytes);
						
						Log.i(tag,"Op Stream: "+bytes);
						
						
					}
				} else {
							Log.i(tag,"Wait!!!");
					/*
					Toast.makeText(getBaseContext(),
							getResources().getString(R.string.wait),
							Toast.LENGTH_SHORT).show();
							
							*/
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


/*
		 //after select, connect to device 
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
*/

		
		

		public void onDestroy() {
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
				
				isConnected = true;
				
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
//							Log.d(TAG, "test1:" + str);
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
//							Log.d(TAG, "test2:" + hex);
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
						isConnected = false;
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


		public void closeSock() {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				Log.e(TAG, ">>", e);
			}
			
		}

	
	

}
