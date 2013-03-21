package org.opencv.samples.tutorial3;

import org.bluetooth.simple.BtBufferServer;
import org.bluetooth.simple.BtBufferServer.LocalBinder;
import org.bluetooth.simple.SearchDeviceActivity;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class BaseActivity extends Activity
{

	private TextView tvCoordinates;
	private Button btnGetCoordinates;
	private Button btnTrainer;
	private TextView tvHsvStatus;
	public static final String tag = "ocv";
	
	
	// delay time
	private static final int DELAY_TIME = 80;
	
	/*
	// socket programming
	private String serverIpAddress = "10.0.0.3";

    private boolean connected = false;
    
    private boolean dataFlag = false;
	*/
	
    // Service
    private BtBufferServer mService;
	private boolean mBound;

	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) 
	{
        @Override
        public void onManagerConnected(int status) 
        {
        	switch (status) 
        	{
            
        		case LoaderCallbackInterface.SUCCESS:
                	{
                		Log.i(tag, "OpenCV loaded successfully");

                		// 	Load native library after(!) OpenCV initialization
                		System.loadLibrary("native_sample");

                    
                	}
                	break;
                	
                default:
                {
                    super.onManagerConnected(status);
                } 
                break;
                
            }
        	
        }
        
    };
	private int posX;
	private int posY;
	private Button btnStartBtService;
	
	private CheckBox cbTurboMode;
	private Button btnSearchDev;
	private Button btnSendData;
	

    public BaseActivity() 
    {
        Log.i(tag, "Instantiated new " + this.getClass());
    }


    
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.base_layout);
		
		setupViews();
		
		/*
		if (!connected) {
            
            if (!serverIpAddress.equals("")) {
            	
            	Log.i(tag,"starting cThread");
            	
                Thread cThread = new Thread(new ClientThread());
                cThread.start();
            }
        }*/
		
		//startActivity(new Intent(BaseActivity.this,Sample3Native.class));
	}

	private void setupViews() 
	{
		
		// TODO Auto-generated method stub
		tvCoordinates = (TextView)findViewById(R.id.tv_coordinates);
		
		btnGetCoordinates = (Button)findViewById(R.id.btn_get_coordinates);
		btnGetCoordinates.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				// TODO Auto-generated method stub
				new PauseAndStartActivity().execute((Void)null);
				
			}
		});
		
		
		btnTrainer = (Button)findViewById(R.id.btn_trainer);
		btnTrainer.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(BaseActivity.this,HsvTrainer.class));
				
				//startActivity(new Intent(this,HsvTrainer.class));
				
			}
		});
		
		
		tvHsvStatus = (TextView)findViewById(R.id.tv_hsv_status);
		
		// a button to start BT Base Activity
		btnStartBtService = (Button)findViewById(R.id.btn_start_bt_service2);
		
		btnStartBtService.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) 
			{
				
				Log.i(tag,"Button to start Bt service clicked");
				
				// TODO Auto-generated method stub
				startBtService();
				
				
			}
		});
		
		
		cbTurboMode=(CheckBox)findViewById(R.id.cb_turbo_mode);
		
		
		btnSearchDev = (Button)findViewById(R.id.btn_bt_search_dev);
		
		btnSearchDev.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(BaseActivity.this,SearchDeviceActivity.class));
				
			}
		});
		
		btnSendData = (Button)findViewById(R.id.btn_send_data);
		
		btnSendData.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				try {
					mService.issueCmd("Cooool!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.e(tag,"Error: "+e);
				}
				
			}
		});
		
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		/*
		if (!connected) {
            
            if (!serverIpAddress.equals("")) {
            	
            	Log.i(tag,"starting cThread");
            	
                Thread cThread = new Thread(new ClientThread());
                cThread.start();
            }
        }
		*/
	
		
		Log.i(tag , this.getLocalClassName()+": inside onResume()");
		
		// socket programming
		/*
		dataFlag=true;
		*/
		
		if(Tank.valsSet)
		{
			Log.i(tag , "pos set");
			
			Log.i(tag,"Hue: "+Tank.getHueMin()+" - "+Tank.getHueMax()
					+"\n"+"Sat: "+Tank.getSatMin()+" - "+Tank.getSatMax() 
					+"\n"+"Val: "+Tank.getValMin()+" - "+Tank.getValMax());
			
			tvHsvStatus.setText("Hue: "+Tank.getHueMin()+" - "+Tank.getHueMax()
					+"\n"+"Sat: "+Tank.getSatMin()+" - "+Tank.getSatMax() 
					+"\n"+"Val: "+Tank.getValMin()+" - "+Tank.getValMax());
			
			
		}
		
		if(Tank.posSet)
		{
			
			posX=Tank.getPosX();
			posY = Tank.getPosY();
			
			Log.i(tag,"("+Tank.getPosX()+","+Tank.getPosY()+")");
			tvCoordinates.setText("("+Tank.getPosX()+","+Tank.getPosY()+")");
			
			
			
			
			try {
				if(posX<400)
				{
					
					Log.i(tag,"right");
					
					
					for(int cmdCount=0;cmdCount<3;cmdCount++)
					{
						mService.issueCmd("$rgt~");
					
					}
					
				}
				
				else
					{
					Log.i(tag,"left");
					
					for(int cmdCount=0;cmdCount<3;cmdCount++)
					{
						mService.issueCmd("$lft~");
					}
					
					}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(tag, "Error: "+e);
			}
			
			
			Tank.posSet=false;
			
			
			
		}
		
		
	//	startActivity(new Intent(BaseActivity.this,Sample3Native.class));
		
		if(cbTurboMode.isChecked())
		new PauseAndStartActivity().execute((Void)null);
		
	}
	
	
	public void startBtService()
	{	
		Log.i(tag,"trying to start service");
		
		
		
		try {
			Log.e(tag,"Bind Service");
			
			// Bind to LocalService
	        Intent startBtService = new Intent(this, BtBufferServer.class);
	        bindService(startBtService , mConnection, Context.BIND_AUTO_CREATE);
			
	        
	        
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(tag,"Error: "+e);
		}
	
	}
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		if (mBound) {
            unbindService(mConnection);
            mBound = false;
		}
	}



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		try {
			stopService(new Intent(BaseActivity.this,BtBufferServer.class));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(tag, this.getLocalClassName()+" : Error: "+e);
		}
		System.exit(0);
		
		
	}


	private class PauseAndStartActivity extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... params) {
			
			try {
				Thread.sleep(DELAY_TIME);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.e(tag,"Error: "+e);
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			startActivity(new Intent(BaseActivity.this,Sample3Native.class));
		}
		
		
		
	}
	
/*	
	
    public class ClientThread implements Runnable {

        public void run() {
            try {
            	
            	
            	
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                Log.d(tag, "C: Connecting...");
                Socket socket = new Socket(serverAddr, 8080);
                
                connected = true;
                while (connected) {
                    try {
                        //Log.d(tag, "C: Sending command.");
                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                                    .getOutputStream())), true);

                        
                        
                        
                        
                        if(dataFlag)
                        	{
                        		Log.d(tag,"Data to Server: "+posX+","+posY);
                        		out.println(posX+","+posY);
                        	}
                        
                        dataFlag=false;
                        
                        
                        
                        
                        // where you issue the commands
                          //  out.println("cool");
                          //  Log.d(tag, "C: Sent.");
                    } catch (Exception e) {
                        Log.e(tag, "S: Error", e);
                    }
                }
                socket.close();
                Log.d(tag, "C: Closed.");
            } catch (Exception e) {
                Log.e(tag, "C: Error", e);
                connected = false;
            }
        }
    }*/
	
    
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        

		@Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
			
			Log.i(tag,"OnServiceConnected...");
			
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	
        	Log.i(tag,"OnServiceDisconnected...");
            mBound = false;
        }
    };
	

}
