package com.google.android.glass.sample.level;

import android.util.Log;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.channels.NotYetConnectedException;

/**
 * Provides a WebSocketClient interface to a rosbridge
 * @author Sam
 *
 */
public class RosWebSocketClient extends WebSocketClient {
	public boolean safe = false; 

	public RosWebSocketClient(URI serverURI) {
		super(serverURI,  new Draft_17());
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		Log.i("ROS CLOSE", reason.toString());
	}

	@Override
	public void onError(Exception ex) {}

	@Override
	public void onMessage(String message) {
		Log.i("ROS MSG", "I got " + message.toString());
	}

	@Override
	public void onOpen(ServerHandshake arg0) {
		// Subscribe
		this.subscribe("/chatter", "std_msgs/String");
		
		// Pan/Tilt messages
		this.advertise("/pantilt/set_position", "rosserial_arduino/pan_tilt");
		
		// Ready to publish
		safe = true;
	}
	
	/**
	 * Advertise on ros topic
	 * @param topic string topic (ex. "std_msgs/String")
	 * @param msg JSONObject message (ex. JSONObject({data:"something"}) )
	 */
	public void advertise(String topic, String type) {
		JSONObject json = new JSONObject();
		try {
			json.put("op", "advertise");
			json.put("topic", topic);
			json.put("type", type);
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		Log.i("ROS ADVERTISE","ADV: "+json.toString());
		this.send(json.toString());
	}
	
	/**
	 * Publish msg on ros topic
	 * @param topic string topic (ex. "std_msgs/String")
	 * @param msg JSONObject message (ex. JSONObject({data:"something"}) )
	 */
	public void publish(String topic, JSONObject msg) {
		JSONObject json = new JSONObject();
		try {
			json.put("op", "publish");
			json.put("topic", topic);
			json.put("msg", msg);
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		Log.i("ROS PUB", "PUB: "+json.toString());
		try {
			this.send(json.toString());
		} catch (NotYetConnectedException ex) {
			Log.w("ROS NO PUB", "Not yet connected...");
		}
	}
	
	public void publishPanTilt(int pan, int tilt) {
		try {
			publish("/pantilt/set_position", new JSONObject().put("pan", pan).put("tilt", tilt) );
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("SAM", "Couldn't publish pantilt :(");
			e.printStackTrace();
		}
	}
	
	/**
	 * Subscribe to ros topic with given message type
	 * @param topic String (ex. "/chatter")
	 * @param type String (ex. "std_msgs/String")
	 */
	public void subscribe(String topic, String type) {
		JSONObject json = new JSONObject();
		try {
			json.put("op", "subscribe");
			json.put("topic", topic);
			json.put("type", type);
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		Log.i("ROS SUB", "SUB: "+json.toString());
		this.send(json.toString());
	}

	public boolean isSafe() {
		return safe;
	}
	
}