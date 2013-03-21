package org.bluetooth.simple;


import android.app.Application;
import android.bluetooth.BluetoothDevice;

public class SocketApplication extends Application {

	private BluetoothDevice device = null;

	public BluetoothDevice getDevice() {
		return device;
	}

	public void setDevice(BluetoothDevice device) {
		this.device = device;
	}
	
}
