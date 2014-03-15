/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.glass.sample.level;

import java.net.URI;
import java.net.URISyntaxException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * View used to draw the level line.
 */
public class LevelView extends View {
	/** lastStartTime */
	private static long lastTime = System.currentTimeMillis(); // Used to track delays

	private static final long YAW_ZERO_START_DELAY = 1000; // How long to wait at start before setting zero yaw
	private static final long ROS_PUBLISH_DELAY = 40; // How often to publish to rosbridge
	
	private static final String ADDRESS = "71.206.193.17:9090"; // IP and PORT of rosbridge server
	private RosWebSocketClient rosclient;

	private Paint mPaint = new Paint();
	private Paint mTextPaint = new Paint();
	private float mAngle = 0.f;
	private float mYaw = 0.f;
	private float mYawCorrected = 0.f; // offset yaw
	private float mStartYaw = 0.f;
	private boolean mHasStartYaw = false; // used to set start yaw the first
											// time it gets it

	private float mPitch = 0.f;
	private float mRoll = 0.f;

	private int mPan = 0, mTilt = 0, mPan_old = mPan, mTilt_old = mTilt;

	public LevelView(Context context) {
		this(context, null, 0);
	}

	public LevelView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LevelView(Context context, AttributeSet attrs, int style) {
		super(context, attrs, style);

		// Keep screen on
		setKeepScreenOn(true); // Doesn't work :(

		mPaint.setColor(Color.BLUE);
		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStrokeWidth(5);

		mTextPaint.setColor(Color.GREEN);
		mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextSize(30);

		// Set up rosclient
		try {
			Log.i("SAM", "Trying to connect to ros...!");
			rosclient = new RosWebSocketClient(new URI("ws://" + ADDRESS + "/"));

			rosclient.connect();
			Log.i("SAM", "Ros client connected!");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Set the angle of the level line.
	 * 
	 * @param angle
	 *            Angle of the level line.
	 */
	public void setAngle(float angle) {
		mAngle = angle;
		// Redraw the line.
		invalidate();
	}

	public float getAngle() {
		return mAngle;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = canvas.getWidth();
		int height = canvas.getHeight() / 2;

		// Compute the coordinates.
		float y = (float) Math.tan(mAngle) * width / 2;

		// Draw the level line.
		canvas.drawLine(0, y + height, width, -y + height, mPaint);

		// Draw yaw/pitch/roll
		canvas.drawText(String.format("Yaw: %+3.1f", mYaw), 5, 30, mTextPaint);
		canvas.drawText(String.format("Pitch: %+3.1f", mPitch), 5, 60,
				mTextPaint);
		canvas.drawText(String.format("Roll: %+3.1f", mRoll), 5, 90, mTextPaint);
		 canvas.drawText(String.format("YawCorrected: %+3.1f", mYawCorrected), 5, 120, mTextPaint);
		 canvas.drawText(String.format("YawOffset: %+3.1f", mStartYaw), 5, 150, mTextPaint);
		canvas.drawText(String.format("Pan/Tilt: %2d %2d", mPan, mTilt), 5, 180, mTextPaint);
	}

	public void setYawPitchRoll(float mYaw, float mPitch, float mRoll) {
		if (!mHasStartYaw
				&& (System.currentTimeMillis() - lastTime > YAW_ZERO_START_DELAY)) {
			mHasStartYaw = true;
			mStartYaw = mYaw;
		}
		this.mYaw = mYaw;
		if (mYaw - mStartYaw > 180) {
			this.mYawCorrected = 360 - (mYaw - mStartYaw);
		} else if (mYaw - mStartYaw < -180) {
			this.mYawCorrected = (mYaw - mStartYaw) + 360;
		} else {
			this.mYawCorrected = mYaw - mStartYaw;
		}
		this.mPitch = mPitch;
		this.mRoll = mRoll;

		// Yaw +- 50, Clamped
		mPan = (int) ((mYawCorrected + 50) * 100 / 100);
		mPan = mPan < 0 ? 0 : mPan > 100 ? 100 : mPan;

		// Pitch +- 40, Clamped
		mTilt = (int) (mPitch + 40) * 100 / 80;
		mTilt = mTilt < 0 ? 0 : mTilt > 100 ? 100 : mTilt;

		// If last time has been greater than pub delay
		if (System.currentTimeMillis() - lastTime > ROS_PUBLISH_DELAY
				&& (mPan_old != mPan || mTilt_old != mTilt)) {
			// Log.i("SAM_DEBUG",String.format("Pan/Tilt: %2d %2d", mPan,
			// mTilt));
			if (rosclient != null) {
				// Publish to ROS
				rosclient.publishPanTilt(mPan, mTilt);
			}
			mPan_old = mPan;
			mTilt_old = mTilt;
			lastTime = System.currentTimeMillis();
		}
	}
	
	/**
	 * Creates new ros socket (if old doesn't exist)
	 */
	public void startROSSocket() {
		if (rosclient != null) { return; }
		
		try {
			rosclient = new RosWebSocketClient(new URI("ws://"+ADDRESS+"/"));
			rosclient.connect();
			Log.i("SAM", "Ros client connected!");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes ros socket
	 */
	public void closeROSSocket() {
		rosclient.close();
		rosclient = null;
		Log.i("SAM_DEBUG", "Ros client closed");
	}

	public void rezeroYaw() {
		mStartYaw = mYaw;
	}

}
