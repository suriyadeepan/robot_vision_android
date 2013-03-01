package org.opencv.samples.tutorial3;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
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
	
	public static String imgFileRoot = "pic";
	
	public int imgFileNum=0;
	
	
	public static String TAG="hsv-trainer";
	public ImageView jpgView; 
	public SeekBar sbMin,sbMax;
	public TextView hsvStatus;
	public Button switchHsv;
	char hsvSelection='H';
	
	 
	
	
	// the img matrix
	Mat m,tempMat,hsvTemp;
	
	// the bitmap
	Bitmap bmp;
	
	public int hueMin = 30;
	public int hueMax = 150;
	
	public int satMin = 30;
	public int satMax = 200;
	
	
	public int valMin = 60;
	public int valMax = 210;
	
	
	
	
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("native_sample");

                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

	private int imgFilesMax;
	
    public void onCreate(Bundle savedInstanceState) 
    {
    	
    	
    	
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hsv_trainer_layout);
        
        
        Log.i(TAG,"content View set");
        
        //****************************************************************
       
        
       setupViews();
        //*****************************************************************
        
        
       
       File sdcardRoot = Environment.getExternalStorageDirectory();
		File myDir = new File(sdcardRoot,"/cv-testing/");
		
		
		imgFilesMax = myDir.listFiles().length;
       
        

       	Log.i(TAG,"Image File: "+Environment.getExternalStorageDirectory().
        		getAbsolutePath()+"/cv-testing/"+getNextFile()+" is to be loaded");
       	
       	/*
        m = Highgui.imread(Environment.getExternalStorageDirectory().
        		getAbsolutePath()+"/"+imgFileRoot+imgFileNum+".jpg");
        		*/
       	
       	String tempFileName = getNextFile();
       	
       	m = Highgui.imread(Environment.getExternalStorageDirectory().
        		getAbsolutePath()+"/cv-testing/"+tempFileName);
        if (m != null)
        {
             Log.i(TAG,"Height: " + m.height() + " Width: " + m.width());
        }
        else
        {
             Log.i(TAG,"mat is null!");
        }
        
        
        //Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2RGB);
        
        bmp = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
        Log.i(TAG,"bitmap created from m");
        
        
        Utils.matToBitmap(m, bmp);
        Log.i(TAG,"mat converted to bitmap");
        
        jpgView.setImageBitmap(bmp);
        Log.i(TAG,"bmp set to ImageView");
        
    }


    
    /*
     * 
     * 	SETUP VIEWS
     * 
     * 
     */
    
    public void setupViews() 
	{
		jpgView= (ImageView)findViewById(R.id.iv_train);
        Log.i(TAG,"Image View is declared");
        
        
        // setup seekbars
        sbMin=(SeekBar)findViewById(R.id.sb_min);
        
        sbMin.setProgress((hueMin*100)/255);
        
        
        Log.i(TAG,"Seek bar1 set!");
        
       
        
        sbMax=(SeekBar)findViewById(R.id.sb_max);
        sbMax.setProgress((hueMax*100)/255);
        
        Log.i(TAG,"Seek bar1 set!");
        
        sbMin.setOnSeekBarChangeListener(this);
        sbMax.setOnSeekBarChangeListener(this);
     
        
        // setup switch button
        switchHsv=(Button)findViewById(R.id.bt_hsv_switch);
        hsvStatus=(TextView)findViewById(R.id.tv_hsv_status);
        
        
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
					hsvSelection='V';
					sbMin.setProgress((valMin*100)/255);
					sbMax.setProgress((valMax*100)/255);
					break;
					
				case 'V':
					hsvSelection='H';
					sbMin.setProgress((hueMin*100)/255);
					sbMax.setProgress((hueMax*100)/255);
					break;
					
				  default:
					  hsvSelection='H';
				}
				
				hsvStatus.setText(hsvSelection+"");
			
				
			}
		});
        
        
       
        
        
        
       	
	}
	
	
    private final Handler chgImgHandler = new Handler() {
		public void handleMessage(Message msg)
		{
	       	Log.i(TAG,"Image File: "+Environment.getExternalStorageDirectory().
	        		getAbsolutePath()+"/cv-testing/"+getNextFile()+" is to be loaded");
	       	
	       	
	        m = Highgui.imread(Environment.getExternalStorageDirectory().
	        		getAbsolutePath()+"/cv-testing/"+getNextFile());
	        if (m != null)
	        {
	             Log.i(TAG,"Height: " + m.height() + " Width: " + m.width());
	        }
	        else
	        {
	             Log.i(TAG,"mat is null!");
	        }
	        
	        
	        //Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);
	        Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2RGB);
	        
	        bmp = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
	        Log.i(TAG,"bitmap created from m");
	        
	        
	        Utils.matToBitmap(m, bmp);
	        Log.i(TAG,"mat converted to bitmap");
	        
	        jpgView.setImageBitmap(bmp);
	        Log.i(TAG,"bmp set to ImageView");

			
		}
		
    };
    
    
	private final Handler handler1 = new Handler() {
		public void handleMessage(Message msg)
		{

		
			 tempMat= new Mat(m.height(), m.width(), CvType.CV_8UC1);
			 hsvTemp = m.clone();
		    Imgproc.cvtColor(m, hsvTemp,Imgproc.COLOR_RGB2HSV);
		    	
			Core.inRange(hsvTemp,  new Scalar(hueMin,satMin,valMin), new Scalar(hueMax,satMax,valMax), tempMat);
		        
			
			Log.i(TAG, "hueMin: " +hueMin);
			Log.i(TAG, "hueMax: " +hueMax);
			Log.i(TAG, "satMin: " +satMin);
			Log.i(TAG, "satMax: " +satMax);
			Log.i(TAG, "valMin: " +valMin);
			Log.i(TAG, "valMax: " +valMax);
			
			
		        bmp = Bitmap.createBitmap(tempMat.cols(), tempMat.rows(), Bitmap.Config.ARGB_8888);
		        
		        Utils.matToBitmap(tempMat, bmp);
			
			
			jpgView.setImageBitmap(bmp);
			
			Log.i(TAG,"it actually does it!");
		}
		
	
		
		};
		
		
		public String getNextFile()
		{
			String fileName = null;
			
			File sdcardRoot = Environment.getExternalStorageDirectory();
			File myDir = new File(sdcardRoot,"/cv-testing/");
			
			
			int i=0;
			
			for(File f : myDir.listFiles())
			{
					
				if(f.isFile())
				{
					
						fileName = f.getName();
						
						if(i==imgFileNum)
						{
							
							break;
						}
						
						
						i++;
						
				}
				
			}
			
			return fileName;
		}
		


	@Override
	public void onProgressChanged(SeekBar bar, int prog, boolean arg2) 
	{
		
		
		
		//arg0.getId()
		Log.i(TAG,"inside onProgressChanged()");
		
		
		int progVal=(int)(prog*255)/100;
		
		switch(bar.getId())
		{
		
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
			Log.i(TAG,"Default case of seek bar! "+prog);
			
		}
		
		
		
	
			
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) 
	{
		
		
		Log.i(TAG,"inside onStartTrackingTouch()");
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar bar) 
	{
		
	
		Log.i(TAG,"inside onStopTrackingTouch()");
		
		
		
		new Thread(new Runnable() {
			   public void run() 
			   {
				   
				   Message msg = handler1.obtainMessage();

				   handler1.sendMessage(msg);
		
			   }
			  }).start();
		
        
		Log.i(TAG,"inside onProgressChanged() method - done with setting image");
	
	
		
	
		
	}
    
    

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    
    	
    	getMenuInflater().inflate(R.menu.hsv_trainer_layout, menu);
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	
    	switch(item.getItemId())
    	{
    	case R.id.m_export:
    		Log.i(TAG, "Export option selected...");
    		Tank.setMaxVals(hueMax, satMax, valMax);
    		Tank.setMinVals(hueMin, satMin, valMin);
    		Tank.valsSet=true;
    		finish();
    		return true;
    		
    		
    	case R.id.m_load_image:
    		Log.i(TAG,"load a new image...");
    		
    		if(imgFileNum<imgFilesMax)
    		{
    			imgFileNum++;
    		}
    		
    		
    		
    		else
    			imgFileNum=0;
    		
    		
    		new Thread(new Runnable() {
  			   public void run() 
  			   {
  				   
  				   Message msg = chgImgHandler.obtainMessage();

  				   chgImgHandler.sendMessage(msg);
  		
  			   }
  			  }).start();

    		
    		return true;
    		
    	
    	
    	default:
    		Log.i(TAG,"default");
    		return super.onOptionsItemSelected(item);
    		
    	}
    	
    
    	
    }
 
	
	
	


}

