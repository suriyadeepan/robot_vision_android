package org.opencv.samples.tutorial3;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class Sample3Native extends Activity implements CvCameraViewListener {
    private static final String TAG = "ocv-activity";

    private Mat                    mRgba;
    private Mat                    mGrayMat;
    private CameraBridgeViewBase   mOpenCvCameraView;

    private BaseLoaderCallback     mLoaderCallback = new BaseLoaderCallback(this) {
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
                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

	private Mat tempMat;

    public Sample3Native() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial3_surface_view);

        
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial4_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGrayMat = new Mat(height, width, CvType.CV_8UC1);
        tempMat= new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGrayMat.release();
    }

    public Mat onCameraFrame(Mat inputFrame) {
       
    	inputFrame.copyTo(mRgba);
    	/* inputFrame.copyTo(mRgba);
        Imgproc.cvtColor(mRgba, mGrayMat, Imgproc.COLOR_RGBA2GRAY);
        FindFeatures(mGrayMat.getNativeObjAddr(), mRgba.getNativeObjAddr());
        
    	*/
    	 
    	
    	
    	// convert RGB to HSV
    	//  create a temp container to store HSV img
    	Mat hsvTemp = mRgba.clone();
    	Imgproc.cvtColor(mRgba, hsvTemp,Imgproc.COLOR_RGB2HSV);
    	
    	// get hsv values from Tank
    	if(Tank.valsSet)
    		Core.inRange(hsvTemp,  new Scalar(Tank.hMin,Tank.sMin,Tank.vMin), new Scalar(Tank.hMax,Tank.sMax,Tank.vMax), tempMat);
    	
    	else
    		Core.inRange(hsvTemp,  new Scalar(35,14,107), new Scalar(83,185,255), tempMat);
    	
    	// Adaptive Thresholding
       	/*
    	Imgproc.adaptiveThreshold(tempMat,tempMat,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
    			Imgproc.THRESH_BINARY,35,25);
    	*/
    	
    	
    	// Smoothing
       	Imgproc.blur(tempMat, tempMat, new Size(3,3));
    	
    	
    	// Morphological op => open
    	// creating a kernel for it
    	Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_OPEN, new Size(3,3), new Point(1,1));
       	Imgproc.morphologyEx(tempMat, tempMat, Imgproc.MORPH_OPEN, kernel);
    	

       	// Smoothing
       	Imgproc.blur(tempMat, tempMat, new Size(3,3));
    	
       	// binary thresholding
       	Imgproc.threshold(tempMat, tempMat, 120, 255, Imgproc.THRESH_BINARY);
    	
       	/*
       	// Smoothing
       	Imgproc.blur(tempMat, tempMat, new Size(3,3));
       	
       	
       	// CANNY
       	Imgproc.Canny(tempMat, tempMat,8,8);
       	*/
       	Mat circleImage = new Mat(tempMat.rows(), tempMat.cols(), CvType.CV_8UC1);
       	
       	
       	Imgproc.HoughCircles(tempMat, circleImage, Imgproc.CV_HOUGH_GRADIENT,2 ,
       	         (double) tempMat.height() / 70);
       	
       	
       	Moments mm=Imgproc.moments(tempMat);
       	
       	double mm00=mm.get_m00();
       	double mm01=mm.get_m01();
       	double mm10=mm.get_m10();
       	
       	int posX=(int) (mm10/mm00);
       	int posY=(int) (mm01/mm00);
       	
       	//Log.i(TAG, "Area:"+mm00);
       	
       	// radius of circle changes dynamically with M00
       	//  relation: radius = 0.000027742 * M00
       	//int radius = (int) (0.000027742*mm00);
       	//  doesn't seem to work tho' , so,
       	//   just using a constant
       	Core.circle(mRgba, new Point(posX,posY),40 , new Scalar(255, 0, 0),2);
       	
       	
       	
       	
    	

        return mRgba;
    }
    
    

    //public native void FindFeatures(long matAddrGr, long matAddrRgba);
 
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    
    	
    	getMenuInflater().inflate(R.menu.tutorial3_surface_view, menu);
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	
    	switch(item.getItemId())
    	{
    	case R.id.m_trainer:
    		Log.i(TAG, "Trainer");
    		startActivity(new Intent(this,HsvTrainer.class));
    		Log.i(TAG, "Intent called");
    		return true;
    		
    	case R.id.m_exit:
    		finish();
    		return true;
    	
    	
    	default:
    		Log.i(TAG,"default");
    		return super.onOptionsItemSelected(item);
    		
    	}
    	
    
    	
    }
    
}
