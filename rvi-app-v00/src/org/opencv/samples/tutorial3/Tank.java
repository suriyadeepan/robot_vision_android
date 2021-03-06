package org.opencv.samples.tutorial3;

public class Tank 
{
	
	static int hMin;
	static int hMax;
	static int sMin;
	static int sMax;
	static int vMin;
	static int vMax;
	
	static int posX;
	static int posY;
	
	public static String btData;
	
	public static boolean valsSet;
	public static boolean posSet;
	public static boolean btDataSet;
	public static boolean turboMode;
	
	public static void setMinVals(int hMin1,int sMin1,int vMin1)
	{
		hMin=hMin1;
		sMin=sMin1;
		vMin=vMin1;
				
	}
	
	public static void setMaxVals(int hMax1,int sMax1,int vMax1)
	{
		hMax=hMax1;
		sMax=sMax1;
		vMax=vMax1;
				
	}
	
	public static void setCoordinates(int x,int y)
	{
		posX=x;
		posY=y;
	}
	
	public static void setBtData(String btData1)
	{
		btData=btData1;
	}
	
	
	public static int getHueMin()
	{
		return hMin;
	}
	
	public static int getHueMax()
	{
		return hMax;
	}
	
	public static int getSatMin()
	{
		return sMin;
	}
	public static int getSatMax()
	{
		return sMax;
	}
	public static int getValMin()
	{
		return vMin;
	}
	public static int getValMax()
	{
		return vMax;
	}
	
	public static int getPosX()
	{
		return posX;
	}
	
	public static int getPosY()
	{
		return posY;
	}
	
	public static String getBtData()
	{
		return btData;
	}

}
