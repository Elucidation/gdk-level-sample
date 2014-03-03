package com.google.android.glass.sample.level;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

public class LevelSocketClient extends AsyncTask<String, Void, String>{
	// Socket server Address information
	private static final String HOST_NAME = "192.168.62.190";
	private static final int PORT = 50007;
	
	Socket mSocket;
	boolean socketStatus = false;
	PrintWriter out;
    BufferedReader in;
	
	@Override
	protected String doInBackground(String... params) {
		
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
        
		String msg = "No message received";
		if (socketStatus) {
        	Log.v("SAM_DEBUG", "Sending over socket...");
        	out.write(params[0]);
        	out.flush();
        	Log.v("SAM_DEBUG", "Message sent...");
			try {
				Log.v("SAM_DEBUG", "Trying to receive...");
				msg = in.readLine();
				Log.v("SAM_DEBUG", "Received message: "+ msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				Log.v("SAM_DEBUG", "Trying to close in...");
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			out.close();
			Log.v("SAM_DEBUG", "Cleaning up done...");
        }
		
		return msg;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		
		
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		
		Log.v("SAM_DEBUG", "FIN - Received message: "+ result);
		
		if (mSocket != null && mSocket.isConnected()) {
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

}