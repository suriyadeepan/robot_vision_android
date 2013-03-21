package org.opencv.samples.tutorial3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bluetooth.simple.BtBufferServer;
import org.bluetooth.simple.BtBufferServer.LocalBinder;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class Sample3Native extends Activity implements CvCameraViewListener {
    
	private static final String CMD_FORWARD = "fwd";
	private static final String CMD_RIGHT = "rgt";
	private static final String CMD_LEFT = "lft";


	private static final int FRAME_PROC_RATE = 8;
	private static final int NEUTRAL_ZONE_MAX = 450;
	private static final int NEUTRAL_ZONE_MIN = 350;



	/*
	 *  Debugging
	 */
	private static final String TAG = "ocv";



    /*
     * 	CV Parameters
     * 
     */
    private Mat mRgba;
    private Mat tempMat;
    // extra GREY matrix for use 
    //private Mat shadowGrayMat;

    
	// HSV - range values
	// hue - sat - val
	int hueMin,hueMax;
	int satMin,satMax;
	int valMin,valMax;
    
	// Frame Count - "duh"
	private int frameCount = 0;
	
	// Max and Min radius
	//  [Radius obtained from max Area contour]
	private static final int MIN_OBJ_RADIUS = 30;
	private static final int MAX_OBJ_RADIUS = 180;
	
	
	
	private int ERROR_P;
	private double GAIN_P= 0.03;
	private double FWD_GAIN_P = 2.5;

	
	
	
	/*
	 * Bluetooth
	 * 
	 */

	// get the default bluetooth adapter
	public BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	// bluetooth device to store BtBee  
	public BluetoothDevice _bee ;
	
	
	/*
	 *  Bluetooth Service - BtBufferServer.java 
	 */
    private BtBufferServer mService;
	private boolean mBound;

	
	/*
	 * 	Misc
	 */
	private CameraBridgeViewBase mOpenCvCameraView;
	
	
	// Callback function that connects Opencv Manager and this app
	//  provides lib functions
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("native_sample");

                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.enableFpsMeter();
                    
                    Log.i(TAG,"Opencv camerview enabled");
                    
                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    public Sample3Native() 
    {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    
    
    /*
     * 	---------------------onCreate() method---------------------
     * 
     */
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    
    	Log.i(TAG, "called onCreate");
    	
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        
        // set layout
        setContentView(R.layout.tutorial3_surface_view);
        Log.i(TAG,"Layout set");
        
        // init frameCount
        frameCount = 0;
        
        // set trained HSV values for HSV thresholding
        setHsvValues(124,181,61,216,112,255);
        
        // setup native camview
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial4_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        Log.i(TAG,"camera view setup complete");
        
        

      
        // *****************Connect to Device********************************
      
        new Thread() {
			public void run() {
				
		        // Enable Bluetooth
		        Log.i(TAG,"Enabling bt");
		        if(!_bluetooth.isEnabled())
		        	_bluetooth.enable();
		        
				while(!_bluetooth.isEnabled())
				{
					//Log.i(TAG,"enabling bt");
				}
				
				Log.i(TAG, "Getting bonded devs");
				Set<BluetoothDevice> pDevs = _bluetooth.getBondedDevices();
				Log.i(TAG, "Iterating thro' bonded devices");
				Log.i(TAG, "*********************************");
				for (BluetoothDevice device : pDevs) {
					Log.i(TAG, device.getName());

					if (device.getName().equals("linvor")) {
						_bee = device;
						break;
					}

				}
				Log.i(TAG, "*********************************");
				if (_bee != null) {
					Log.i(TAG, "trying to start Bt Service");

					// Start Bt Service
					startBtService();

				}

				else
					Log.i(TAG, "null boss!");
			}
		}.start();
		
		
		// ****************************************************************
     	
		/*
		 * ---------- Start WIFI Thread
		 */
		/*
		if (!wifiConnected){ 
           Thread cThread = new Thread(new ClientThread());
           cThread.start();
		}*/
   	
        
    }

    @Override
    public void onPause()
    {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
    }

  
    
	public void onDestroy() {
        
		super.onDestroy();
		
        mService.closeSock();
        
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        
        
        System.exit(0);
        
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        tempMat= new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        tempMat.release();
        
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    
    	
    	getMenuInflater().inflate(R.menu.tutorial3_surface_view, menu);
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	
    	switch(item.getItemId())
    	{/*
    		case R.id.item_hue:
    			HSV_SELECT=0;
    			return true;
    			

    		case R.id.item_sat:
    			HSV_SELECT=1;
    			return true;
    			

    		case R.id.item_val:
    			HSV_SELECT=2;
    			return true;
    		*/	
    		case R.id.m_exit:
    			
    			mService.closeSock();
    	        
    	        if (mOpenCvCameraView != null)
    	            mOpenCvCameraView.disableView();
    	        
    	        System.exit(0);
    	        
    			return true;
    			


    		default:
    			
    			return true;
    	
    	
    	}
    	
    	
    }
    
    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		mService.closeSock();
		
		if (mBound) {
            unbindService(mConnection);
            mBound = false;
		}
	}

    /*
     * 
     * 	-------------------- onResume() method --------------------
     */
    
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        
        // re-init Frame Count
        frameCount  = 0;
        
        Log.i(TAG, "inside onResume()");
        
        // set trained HSV values for HSV thresholding
        // setHsvValues(130,183,48,255,81,255);
        setHsvValues(124,181,61,216,112,255);
    }

    
    /*
     * 
     * 		---------------------- onCameraFrame() ---------------------------
     * 
     * 
     */
    
    
    // this method acts on each frame captured by cam
    //  and returns the processed image
    public Mat onCameraFrame(final Mat inputFrame) 
    {	
    	Mat hsvTemp = null;
    	
    	// Radius of obj obtained from max-contour area
    	int objRadius = 0;

    	// center of moment - from cv moments 
    	Point centerOfMoment = null;
    	
    	// increment frame count
    	frameCount++;
    	
    	
    	inputFrame.copyTo(mRgba);
    
    	
			// convert RGB to HSV
			//  create a temp container to store HSV img
    		
			hsvTemp = mRgba.clone();
			Imgproc.cvtColor(mRgba, hsvTemp, Imgproc.COLOR_RGB2HSV);
			
    	
    	
			//	------- Histogram Equalization -------
			//hsvTemp = equalizeVal(hsvTemp);
			//Imgproc.cvtColor(hsvTemp, hsvTemp,Imgproc.COLOR_HSV2RGB);
	
			
			/*
			 *  ----------------------- HSV Thresholding ----------------------- 
			 */
			
			
			 
			 // get hsv values from Tank class (storage)
			Core.inRange(hsvTemp, new Scalar(hueMin, satMin, valMin), new Scalar(
										hueMax, satMax, valMax), tempMat);
			
						
			// --------------- Morphing --------------- 
			// Morphological op => open
			morphImage(new Size(3,3), new Size(3,3));
			
	
			// ---------- Binary Thresholding ---------
			binaryThresh(new Size(5,5),80,255);
			
			// ---------------- Smooth ----------------
			Imgproc.blur(tempMat, tempMat, new Size(3,3));
			
			
			
			// ------------- Canny Op -----------------
			//applyCanny(new Size(5,5),10,10);
						
			// ----------------- HOUGH CIRCLES ---------------------
			//drawHoughCircles(houghCircles1());
			//drawHoughCircles(houghCircles2());
					     
			// -------------------- CV Moments ---------------------
			centerOfMoment = getCenterOfMoment();
					     
			
			// -------------------- Contours -----------------------
			// get a copy of tempMat
			Mat img4Contour = tempMat.clone();
				double maxArea = getMaxContourArea(img4Contour);
			// release memory
				img4Contour.release();

			// get radius from maxArea
			objRadius=(int) ( Math.sqrt(maxArea*7/22)*0.9);
			
			Log.i(TAG,"obj radius: "+objRadius);

			
			
			if(mService.isConnected)
			issueCmd(centerOfMoment.x,objRadius);
			
	
		
       	// draw a circle of fixed radius[5] with
       	// (posX,posY) as center in red color (255,0,0)
		// Core.circle(mRgba, centerOfMoment,5 , new Scalar(255, 0, 0),-1);
    	 
		// finding radius from maximum contour-area
		 Core.circle(mRgba,centerOfMoment,objRadius , new Scalar(0, 0, 255),3);
		 
		// return Mat over which circle is drawn
		return mRgba;
    }



	/**
	 * @param hsvTemp 
	 * @param hsvTemp
	 */
	private Mat equalizeVal(Mat hsvTemp) {
		Mat channel0 = new Mat(hsvTemp.height(), hsvTemp.width(),CvType.CV_8UC1);;
		Mat channel1 = new Mat(hsvTemp.height(), hsvTemp.width(),CvType.CV_8UC1);;
		Mat channel2 = new Mat(hsvTemp.height(), hsvTemp.width(),CvType.CV_8UC1);;
		
		
			List<Mat> mv = new ArrayList<Mat>();
			Core.split(hsvTemp,mv);
			
			channel0 = mv.get(0);
			channel1 = mv.get(1);
			channel2 = mv.get(2);
			
			// Log.i(TAG,"**"+frameCount);
			
				//Imgproc.equalizeHist(channel0, channel0);
				//Imgproc.equalizeHist(channel1, channel1);
				Imgproc.equalizeHist(channel2, channel2);
				
				mv.set(0, channel0);
				mv.set(1, channel1);
				mv.set(2, channel2);
				
				Core.merge(mv,hsvTemp);
				
			
			channel0.release();
			channel1.release();
			channel2.release();
				
			
			
			
			return hsvTemp;
			
	}

    
 
    /*
     * 
     * 	--------------- IMAGE PROCESSING METHODS --------------- 
     * 
     */

	private double getMaxContourArea(Mat img) {
		// TODO Auto-generated method stub
    	ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(img, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		double maxArea = -1;
		int maxAreaIdx = -1;
		
		for (int idx = 0; idx < contours.size(); idx++) {
		    Mat contour = contours.get(idx);
		    double contourarea = Imgproc.contourArea(contour);
		    if (contourarea > maxArea) {
		        maxArea = contourarea;
		        maxAreaIdx = idx;
		    }
		}
		
		
		return maxArea;
		
	}



	private Point getCenterOfMoment() {
		// TODO Auto-generated method stub
    	// obtain image moments from tempMat
		Moments mm = Imgproc.moments(tempMat);
		// get individual moment values
		double mm00 = mm.get_m00();
		double mm01 = mm.get_m01();
		double mm10 = mm.get_m10();
		
		return new Point((int) (mm10 / mm00),(int) (mm01 / mm00));
	}



	private void drawHoughCircles(Mat circles) {
		// TODO Auto-generated method stub
    	if (circles.cols() > 0)
	     {
	    	 
	    	 Log.i(TAG,"circle found!");
	    	 
	    	    for (int x = 0; x < circles.cols(); x++) 
	    	        {
	    	    		double vCircle[] = circles.get(0,x);

	    	    		if (vCircle == null)
	    	            {
	    	    			Log.i(TAG,"vCircle => null");
	    	    			break;
	    	            }

	    	    		Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
	    	    		int radius = (int)Math.round(vCircle[2]);
	    	    		
	    	    		Log.i(TAG,"Point: "+pt.toString());
	    	    		Log.i(TAG,"Radius: "+radius);

	    	        // draw the found circle
	    	    		Core.circle(mRgba, pt, radius, new Scalar(0,255,0),-1);
	    	    		Core.circle(mRgba, pt, 5, new Scalar(0,0,255), -1);
	    	        }
	     
	     }

		
	}



	private Mat houghCircles2() {
		// TODO Auto-generated method stub
		Mat circles = new Mat();
		
		try {
			Imgproc.HoughCircles(tempMat, circles, Imgproc.CV_HOUGH_GRADIENT, 1d, (double)tempMat.height()/7);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG,"Error @ HoughCircle: "+e);
		}
		
		Log.i(TAG,"hough cirlces done!");
		
		return circles;
		
	}



	private Mat houghCircles1() {
		// TODO Auto-generated method stub
    	Mat circles = new Mat();
    	
    	int iCannyUpperThreshold = 100;
		int iMinRadius = 10;
		int iMaxRadius = 600;
		int iAccumulator = 300;

		Imgproc.HoughCircles(tempMat, circles, Imgproc.CV_HOUGH_GRADIENT, 
		         2.0, tempMat.rows() / 8, iCannyUpperThreshold, iAccumulator, 
		         iMinRadius, iMaxRadius);
	
		return circles;
	}



	private void applyCanny(Size smoothKernelSize,int threshMin,int threshMax) 
    {
		// TODO Auto-generated method stub
		// Smoothing
		Imgproc.blur(tempMat, tempMat, smoothKernelSize);
		
		Imgproc.Canny(tempMat, tempMat, threshMin,threshMax);

		
	}



	private void binaryThresh(Size smoothKernelSize,int threshMin,int threshMax) {
		// TODO Auto-generated method stub
    	// Smoothing
		Imgproc.blur(tempMat, tempMat, smoothKernelSize);
		
		
		
		// binary thresholding
		Imgproc.threshold(tempMat, tempMat, threshMin,threshMax,
				Imgproc.THRESH_BINARY);
		
	}



	private void morphImage(Size smoothKernelSize,Size morphKernelSize) 
    {
		// TODO Auto-generated method stub
		// Smoothing
		Imgproc.blur(tempMat, tempMat, smoothKernelSize);

					
					
		// Morphological op => open
		// creating a kernel for it
		Mat kernel = Imgproc.getStructuringElement(
							Imgproc.MORPH_OPEN, morphKernelSize, new Point(
									1, 1));
		Imgproc.morphologyEx(tempMat, tempMat,
							Imgproc.MORPH_OPEN, kernel);
		
		kernel.release();
		
		
					
		
	}


 
	/*
     * ------------------------UTILS------------------------
     * 
     */
    
    
    private void setHsvValues(int hueMin,int hueMax,int satMin,int satMax,int valMin,int valMax)
    {
		// TODO Auto-generated method stub
    	this.hueMin = hueMin;
    	this.hueMax= hueMax;
    	
    	this.satMin = satMin;
    	this.satMax = satMax;
    	
    	this.valMin = valMin;
    	this.valMax = valMax;
		
	}

    
	private void logHsvValues() {
		Log.i(TAG,"Hue: "+hueMin+"-"+hueMax);
		Log.i(TAG,"sat: "+satMin+"-"+satMax);
		Log.i(TAG,"val: "+valMin+"-"+valMax);
	}

	private String formCmd(String dir,int delay)
	{	
		if(delay>99 && delay<1000)
		return "$"+dir+"#"+Integer.toString(delay)+"~";
		
		else if(delay<100 && delay>9)
			return "$"+dir+"#"+"0"+Integer.toString(delay)+"~";
		
		else
			return "$"+dir+"#"+"00"+Integer.toString(delay)+"~";
	}
	
    public void startBtService()
	{	
		Log.i(TAG,"trying to start service");
		
		try {
			Log.e(TAG,"Bind Service");
			
			// Bind to LocalService
			Intent bindToBtBuffer = new Intent();
			bindToBtBuffer.putExtra("android.bluetooth.device.extra.DEVICE",_bee);
			bindToBtBuffer.setClass(Sample3Native.this, BtBufferServer.class);
	        
			// start Service
	        startService(bindToBtBuffer);
	        
	        // bind service to activity
	        bindService(bindToBtBuffer , mConnection, Context.BIND_AUTO_CREATE);
		   
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG,"Error @ Binding Service: "+e);
		}
	
	}
    

    
    private void issueCmd(double x, int objRadius) {
		
    
    	
    	if(objRadius < MAX_OBJ_RADIUS && objRadius > MIN_OBJ_RADIUS)
		{
    		
    		
    		
			if(x < NEUTRAL_ZONE_MIN )
			{
				ERROR_P = (int)Math.abs(x-NEUTRAL_ZONE_MIN);
				mService.issueCmd(formCmd(CMD_LEFT,(int)(ERROR_P*GAIN_P)));
				Log.i(TAG,"x < ...ZONE_MIN and error =" + ERROR_P + " final val ="+ (int)ERROR_P*GAIN_P);
				
			}
			
		
			else if(x > NEUTRAL_ZONE_MAX)
			{
				ERROR_P = (int)Math.abs(x-NEUTRAL_ZONE_MIN);
				mService.issueCmd(formCmd(CMD_RIGHT,(int)(ERROR_P*GAIN_P)));
				Log.i(TAG,"x < ...ZONE_MAX and error =" + ERROR_P + " final val ="+ (int)ERROR_P*GAIN_P);

			}
				
			
			else
			{
				ERROR_P = 180-objRadius;
				mService.issueCmd(formCmd(CMD_FORWARD,(int)(ERROR_P*FWD_GAIN_P ) ));
			}
		
		}
    	
	}
    
    
    

    /*
     *  ---------- Service Callback Methods ---------- 
     * 
     */


	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        

		@Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
			
			Log.i(TAG,"OnServiceConnected...");
			
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	
        	Log.i(TAG,"OnServiceDisconnected...");
            mBound = false;
        }
    };
	public boolean wifiConnected;
	
    /*
     * ------------------ WIFI CLIENT THREAD ------------------ 
     * 
     * 
     */
    
    public class ClientThread implements Runnable {

        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName("10.0.0.4");
                Log.d(TAG, "Wifi: Connecting...");
                Socket socket = new Socket(serverAddr, 8080);
                
                wifiConnected = true;
                while (wifiConnected) {
                    try {

                    	/*
                    	 *  Write to Server 
                    	 */
                    	/*
                    			PrintWriter out = new PrintWriter(new BufferedWriter(
                        				new OutputStreamWriter(socket
                        						.getOutputStream())), true);

                        		out.println("Data To Server "+frameCount);
                        
                        		Log.i(TAG, "Data to Server: "+frameCount);
                        	*/
                        	
                        	
                        	BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String dataFromServer = null;
                            while ((dataFromServer = in.readLine()) != null) 
                            {
                            	Log.i(TAG, "Data from Server: "+dataFromServer);
                            	//Log.i(TAG,"inside inner while(....)");
                            }
                            
                            //Log.i(TAG,"inside while(wifiConnected)");
                            
                            
                            
                            
                        //}
                    	
                        
                    	/*
                    	 *  Read from Server
                    	 * 
                    	 */
                    	/*
                        
                        */
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Wifi: Error", e);
                    }
                }
                socket.close();
                Log.d(TAG, "Wifi: Closed.");
            } catch (Exception e) {
                Log.e(TAG, "Wifi: Error", e);
                wifiConnected = false;
            }
        }
    }

    
}
