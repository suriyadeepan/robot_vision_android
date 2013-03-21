package org.opencv.samples.tutorial3;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;



public class HsvTrainer extends Activity implements OnSeekBarChangeListener
{	
	// a variable to indicate current 
	//  file index under folder /sdcard/cv-testing/
	public int imgFileNum=0;
	
	// total num of files under /sdcard/cv-testing/
	private int imgFilesMax;
	
	// TAG for debugging with logcat
	public static String TAG="hsv-trainer";
		
	// Views in the layout
	public ImageView jpgView; 
	public SeekBar sbMin,sbMax;
	public TextView hsvStatus;
	public Button switchHsv;
	
	// char to indicate whether H/S/V is selected 
	char hsvSelection='H';
	
	// the img matrix
	Mat m,tempMat;
	
	// the bitmap object
	Bitmap bmp;
	
	// default HSV ranges
	public int hueMin = 30;
	public int hueMax = 150;
	
	public int satMin = 30;
	public int satMax = 200;
	
	public int valMin = 60;
	public int valMax = 210;
	
	/*
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) 
	{
        @Override
        public void onManagerConnected(int status) 
        {
        	switch (status) 
        	{
            
        		case LoaderCallbackInterface.SUCCESS:
                	{
                		Log.i(TAG, "OpenCV loaded successfully");

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
        
    };*/

    /*
     * -----------ON CREATE-----------	
     * onCreate() method
     */
	
    public void onCreate(Bundle savedInstanceState) 
    {
    	
        Log.i(TAG, "inside onCreate");
        
        super.onCreate(savedInstanceState);
        // set layout as hsv_trainer_layout.xml
        setContentView(R.layout.hsv_trainer_layout);
        
        Log.i(TAG,"layout set");
        
        // a method that instantiates all the views in
        //  the layout and set listeners
        setupViews();
        
        // get sdcard dir 
        File sdcardRoot = Environment.getExternalStorageDirectory();
        // 	select our dir
        File myDir = new File(sdcardRoot,"/DCIM/Camera/");
		
        
        // get number of files @ /sdcard/cv-testing
		imgFilesMax = myDir.listFiles().length;
       
        // set image to image view
		new AsyncImageChanger().execute((Void)null);
		
		        
    }// end of onCreate() method

    
    
    
    
	/*
     *----------SETUP VIEWS----------
     *	setupViews() method
     */
    
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		Log.i(TAG,"inside onDestroy()");
		m.release();
		tempMat.release();
		
		
	}





	public void setupViews() 
	{
    	// setup ImageView
    	jpgView= (ImageView)findViewById(R.id.iv_train);
        Log.i(TAG,"Image View is setup");
        
        // setup seekbars
        sbMin=(SeekBar)findViewById(R.id.sb_min);
        sbMax=(SeekBar)findViewById(R.id.sb_max);
        
        // set default progress of seekbars
        //  mapping (0-255) to (0-100)
        sbMin.setProgress((hueMin*100)/255);
        sbMax.setProgress((hueMax*100)/255);
        
        // setup listeners to seekbars
        sbMin.setOnSeekBarChangeListener(this);
        sbMax.setOnSeekBarChangeListener(this);
        
        Log.i(TAG,"Seek bars set!");
        
        // a TextView to display H/S/V selected
        hsvStatus=(TextView)findViewById(R.id.tv_hsv_status);
        
        // setup switch button
        //  a button to switch b/w H/S/V
        switchHsv=(Button)findViewById(R.id.bt_hsv_switch);
        
        // add listener to switchHsv button
        switchHsv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) 
			{
				
				switch(hsvSelection)
				{
				
				case 'H':
					// going to Sat
					hsvSelection='S';
					sbMin.setProgress((satMin*100)/255);
					sbMax.setProgress((satMax*100)/255);
					break;
					
				case 'S':
					// going to Val
					hsvSelection='V';
					sbMin.setProgress((valMin*100)/255);
					sbMax.setProgress((valMax*100)/255);
					break;
					
				case 'V':
					// going to Val					
					hsvSelection='H';
					sbMin.setProgress((hueMin*100)/255);
					sbMax.setProgress((hueMax*100)/255);
					break;
					
				  default:
					  hsvSelection='H';
				}
				
				// set TextView hsvStatus to display
				//  H/S/V selected to the user
				hsvStatus.setText(hsvSelection+"");
					
			}
		});
        
           	
	}// end of method setupViews()...
	
	

    // seekbar listener method
    //  gets called when progress is changed
	@Override
	public void onProgressChanged(SeekBar bar, int prog, boolean arg2) 
	{
		Log.i(TAG,"inside onProgressChanged()");
		
		// map 0-100 to 0-255
		int progVal=(int)(prog*255)/100;
		
		switch(bar.getId())
		{
		// Max Val seekbar
		case R.id.sb_max:
			
			switch(hsvSelection)
			{
			
			case 'H':
				hueMax = progVal;
				break;
				
			case 'S':
				satMax= progVal;
				break;
				
			case 'V':
				valMax=progVal;
				break;
			
			default:
				Log.i(TAG,"sbmax default case");
			}
			
			break;
			
			
		// Min Val seekbar			
		case R.id.sb_min:
			switch(hsvSelection)
			{
			
			case 'H':
				hueMin = progVal;
				break;
				
			case 'S':
				satMin= progVal;
				break;
				
			case 'V':
				valMin=progVal;
				break;
			
			default:
				Log.i(TAG,"sbmin default case");
				
			}
			
			break;
				
		default:
			Log.i(TAG,"Default case of seek bar! ");
			
		}
				
	}// end of onProgressChanged() method

	
	@Override
	public void onStartTrackingTouch(SeekBar arg0) 
	{
		
		
		Log.i(TAG,"inside onStartTrackingTouch()");
		
	}

	
	// this method is called when change(touch) in
	//  progress of seekbar is stoped 
	//   calls the handler threshImage 
	@Override
	public void onStopTrackingTouch(SeekBar bar) 
	{
		Log.i(TAG,"inside onStopTrackingTouch()");
		/*
		new Thread(new Runnable() {
			   public void run() 
			   {
				   Message msg = threshImage.obtainMessage();
				   threshImage.sendMessage(msg);
			   }}).start();
		*/
		
		
		Tank.setMaxVals(hueMax, satMax, valMax);
		Tank.setMinVals(hueMin, satMin, valMin);
		
		Tank.valsSet=true;
		
		
		
		new AsyncThreshImage().execute((Void)null);
		
		Log.i(TAG,"inside onProgressChanged() method - done with setting image");
	}
    

	// Inflate menu using /res/menu/hsv_trainer_layout.xml 
	@Override
    public boolean onCreateOptionsMenu(Menu menu) 
	{
    	getMenuInflater().inflate(R.menu.hsv_trainer_layout, menu);
       	return true;
    }
    
	// this method is called when a menu option 
	//  is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	
    	switch(item.getItemId())
    	{
    	// export option - exports HSV values to
    	//   Tank class which is obtained by the other
    	//    activity
    	case R.id.m_export:
    		Log.i(TAG, "Export option selected...");
    		new ExportHsv().execute((Void)null);
    		return true;
    		
    	// load the next image onto the 
    	//  ImageView
    	case R.id.m_load_image:
    		Log.i(TAG,"load a new image...");
    		
    		if(imgFileNum<imgFilesMax)
    		{
    			imgFileNum++;
    		}
    		else
    			imgFileNum=0;
    		
    		
    		// replacing the handler with AsyncTask
    		new AsyncImageChanger().execute((Void)null);
    		
    		return true;
    	
    	default:
    		Log.i(TAG,"default");
    		return super.onOptionsItemSelected(item);
    	}
    		
    }// end of onOptionsItemSelected() method()
    
    
    /*
     * --------------GENERAL UTILS---------------
     *  
     */
    
    // this method gets the next file @ the
    //  folder /sdcard/cv-testing/ and
    //   returns the filename
	public String getNextFile()
	{
		String fileName = null;
			
		// get folder location
		File sdcardRoot = Environment.getExternalStorageDirectory();
		File myDir = new File(sdcardRoot,"/DCIM/Camera/");
		
		int i=0;
		
		// iterate thro' the folder
		for(File f : myDir.listFiles())
		{					
			if(f.isFile())
			{
				fileName = f.getName();
				if(i==imgFileNum)
				break;

				i++;
			}
				
		}
				
		return fileName;
	
	}
		

	/*
	 * --------------ASYNCTASK---------------
	 *
	 * trying to switch to AsyncTask completely from 
	 *  Handlers and Threads
	 */
	private class AsyncThreshImage extends AsyncTask<Void,Void,Void>
	{

		@Override
		protected Void doInBackground(Void... arg0) {
			
			Log.i(TAG, "inside doInBackground of AsyncTask...");
			
			
			// a temporary matrix to store thresholded image
			tempMat= new Mat(m.height(), m.width(), CvType.CV_8UC1);
					
			
			
			// get a copy of matrix 'm'
			Mat hsvTemp = m.clone();
						
			// convert RGB to HSV
			Imgproc.cvtColor(m, hsvTemp,Imgproc.COLOR_RGB2HSV);
					    	
			// perform thresholding with particular ranges of HSV
			Core.inRange(hsvTemp,  new Scalar(hueMin,satMin,valMin),
								new Scalar(hueMax,satMax,valMax), tempMat);
					        
			// print HSV ranges
			Log.i(TAG, "H: [" +hueMin+" - "+hueMax+"]");
			Log.i(TAG, "S: [" +satMin+" - "+satMax+"]");
			Log.i(TAG, "V: [" +valMin+" - "+valMax+"]");
			
			// create bimap image with dimensions taken
			//  from tempMat matrix 
			bmp = Bitmap.createBitmap(tempMat.cols(), tempMat.rows(), 
					Bitmap.Config.ARGB_8888);
					        
			// map bitmap
			Utils.matToBitmap(tempMat, bmp);
			
			Log.i(TAG, "@ the end of doInBackground of AsyncTask...");
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			Log.i(TAG, "inside onPostExecute() of AsyncTask...");
			
	        // set ImageView as the bitmap image
	        jpgView.setImageBitmap(bmp);
	        Log.i(TAG,"inside onPostExecute() bmp set to ImageView");
			
		}
		
	}
	
	
	
	private class AsyncImageChanger extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			
			

	    	try {
				// print file location 
				Log.i(TAG,"Image File: "+Environment.getExternalStorageDirectory().
				        		getAbsolutePath()+"/DCIM/Camera/"+getNextFile()+" is to be loaded");
				       	
				m = Highgui.imread(Environment.getExternalStorageDirectory().
				        		getAbsolutePath()+"/DCIM/Camera/"+getNextFile());
				       	
				Imgproc.resize(m, m, new Size(640,480));   	
				       	
				 // check if the image matrix is null or not
				 if (m != null)
				        {
				             Log.i(TAG,"Height: " + m.height() + " Width: " + m.width());
				        }
				        else
				        {
				             Log.i(TAG,"mat is null!");
				        }
				        
				        
				        // convert from BGR to RGB before bitmap conversion
				        Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2RGB);
				        
				        // create a bitmap object of dimensions from image matrix
				        bmp = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
				        Log.i(TAG,"bitmap created from m");
				        
				        // mapping bitmap 
				        Utils.matToBitmap(m, bmp);
				        Log.i(TAG,"mat converted to bitmap");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Error: "+e);
			}
			
			return null;
		}
		
		

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			Log.i(TAG, "Trying to set jpgView");
			
			 try {
				// set ImageView as the bitmap image
				jpgView.setImageBitmap(bmp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Error @ setting jpgView: "+e);
			}
	        
	        
	        
	        Log.i(TAG,"bmp set to ImageView");
		}
		
		
		
	}
	
	
	private class ExportHsv extends AsyncTask<Void, Void, Void> 
	{

		@Override
		protected Void doInBackground(Void... arg0) {
			Tank.setMaxVals(hueMax, satMax, valMax);
			Tank.setMinVals(hueMin, satMin, valMin);
			
			Tank.valsSet=true;
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			finish();
		}
		
		
		
		
	}
	
	
    
    /*
     * --------------HANDLERS----------------
     * 
     */
    
	/*
    // a handler to switch b/w images 
    private final Handler chgImgHandler = new Handler() {
		public void handleMessage(Message msg)
		{
		
			// the setImage() method automatically  
			//  updates the ImageView with the next
			//   image
			setImage();
			
	    }
		
    };// end of chgImgHandler
    
    
    
    // this handler performs HSV thresholding over images 
    //  and updates the ImageView
	private final Handler threshImage = new Handler() {
		public void handleMessage(Message msg)
		{
			// this method gets a copy of Image Matrix 'm'
			//  and thresholds it with current HSV values
			//   updates ImageView
			setThreshImage();
			
		}
	};*/
	
	
	
	
	/*
	 * ----------- IMAGE PROC - METHODS -------------
	 * 
	 */
	// this method updates the ImageView with
	//  current matrix 'm'
	
	/*
    private void setImage() 
    {
    	
    	// print file location 
    	Log.i(TAG,"Image File: "+Environment.getExternalStorageDirectory().
    	        		getAbsolutePath()+"/cv-testing/"+getNextFile()+" is to be loaded");
    	       	
    	m = Highgui.imread(Environment.getExternalStorageDirectory().
    	        		getAbsolutePath()+"/cv-testing/"+getNextFile());
    	       	
    	       	
    	       	
    	 // check if the image matrix is null or not
    	 if (m != null)
    	        {
    	             Log.i(TAG,"Height: " + m.height() + " Width: " + m.width());
    	        }
    	        else
    	        {
    	             Log.i(TAG,"mat is null!");
    	        }
    	        
    	        
    	        // convert from BGR to RGB before bitmap conversion
    	        Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2RGB);
    	        
    	        // create a bitmap object of dimensions from image matrix
    	        bmp = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
    	        Log.i(TAG,"bitmap created from m");
    	        
    	        // mapping bitmap 
    	        Utils.matToBitmap(m, bmp);
    	        Log.i(TAG,"mat converted to bitmap");
    	        
    	        // set ImageView as the bitmap image
    	        jpgView.setImageBitmap(bmp);
    	        Log.i(TAG,"bmp set to ImageView");
		
	}

 
	// this method gets a copy of matrix 'm'
    //  HSV thresholds it based on current HSV values
    //   and updates ImageView
	private void setThreshImage() 
	{
		// a temporary matrix to store thresholded image
		tempMat= new Mat(m.height(), m.width(), CvType.CV_8UC1);
					
		// get a copy of matrix 'm'
		Mat hsvTemp = m.clone();
					
		// convert RGB to HSV
		Imgproc.cvtColor(m, hsvTemp,Imgproc.COLOR_RGB2HSV);
				    	
		// perform thresholding with particular ranges of HSV
		Core.inRange(hsvTemp,  new Scalar(hueMin,satMin,valMin),
							new Scalar(hueMax,satMax,valMax), tempMat);
				        
		// print HSV ranges
		Log.i(TAG, "H: [" +hueMin+" - "+hueMax+"]");
		Log.i(TAG, "S: [" +satMin+" - "+satMax+"]");
		Log.i(TAG, "V: [" +valMin+" - "+valMax+"]");
		
		// create bimap image with dimensions taken
		//  from tempMat matrix 
		bmp = Bitmap.createBitmap(tempMat.cols(), tempMat.rows(), 
				Bitmap.Config.ARGB_8888);
				        
		// map bitmap
		Utils.matToBitmap(tempMat, bmp);
					
		// update ImageView with bmp
		jpgView.setImageBitmap(bmp);
					
		Log.i(TAG,"END OF setThreshImage() method");			
		
	}*/
	
	
	
	


}

