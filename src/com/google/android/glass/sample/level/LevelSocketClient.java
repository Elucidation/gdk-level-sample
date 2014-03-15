package com.google.android.glass.sample.level;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

public class LevelSocketClient extends AsyncTask<Void, Void, Void>{
	@Override
	protected void onCancelled(Void result) {
		// TODO Auto-generated method stub
		super.onCancelled(result);
		this.onCancelled();
	}

	// Socket server Address information
	private static final String HOST_NAME = "192.168.62.190";

	private boolean isCancelled = false;
	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		super.onCancelled();
		
		isCancelled = true;
		Log.v("SAM_DEBUG", "Cancel received, closing stuff...");
		if (in != null) {
			try {
				Log.v("SAM_DEBUG", "Trying to close in...");
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (out != null) {
			out.close();
		}
		
		if (mSocket != null && mSocket.isConnected()) {
			try { 
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static final int PORT = 50007;
	
	Socket mSocket;
	boolean socketStatus = false;
	PrintWriter out;
    BufferedReader in;
	
	@Override
	protected Void doInBackground(Void... params) {
		
		Log.v("SAM_DEBUG", "1 - Try socket.");
        try {
			mSocket = new Socket(HOST_NAME, PORT);
			socketStatus = true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("SAM_DEBUG", "1.a - Unknown.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("SAM_DEBUG", "1.b - IO."); 
		}
        Log.v("SAM_DEBUG", "2 - Socket? " + socketStatus);
        
        try {
        	if (socketStatus && mSocket != null) {
            	out = new PrintWriter(mSocket.getOutputStream(), true);
        	    in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            }
        } catch (IOException e) {
			e.printStackTrace();
			socketStatus = false;
		}
		
		Log.v("SAM_DEBUG", "3 - Trying with Socket? " + socketStatus);
		
		while (mSocket.isConnected() && ! isDone()) {};
		
		return null;
	}
	
	private boolean isDone() {
		// TODO Auto-generated method stub
		return isCancelled;
	}

	public boolean sendData(float mYaw, float mPitch, float mRoll) {
		if (socketStatus) {
			out.printf("%+3.1f %+3.1f %+3.1f\n", mYaw, mPitch, mRoll);
			return true;
		} else {
			return false;
		}
	}
	
	void sendMessage(String msg) {
		Log.v("SAM_DEBUG", "Trying to send message...");
		if (socketStatus) {
			out.println(msg);
			Log.v("SAM_DEBUG", "Sent message!");
		} else {
			Log.v("SAM_DEBUG", "socket closed, can't send message...");
		}
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		
		
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		Log.v("SAM_DEBUG", "FIN");
		
		try {
			Log.v("SAM_DEBUG", "Trying to close in...");
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.close();
		
		if (mSocket != null && mSocket.isConnected()) {
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.v("SAM_DEBUG", "Cleaning up done...");
	}
	

}