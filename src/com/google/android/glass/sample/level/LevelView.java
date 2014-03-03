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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * View used to draw the level line.
 */
public class LevelView extends View {

    private Paint mPaint = new Paint();
    private Paint mTextPaint = new Paint();
    private float mAngle = 0.f;
	private float mYaw = 0.f;
	private float mPitch = 0.f;
	private float mRoll = 0.f;

    public LevelView(Context context) {
        this(context, null, 0);
    }

    public LevelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LevelView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        
        // Keep screen on
        setKeepScreenOn(true);

        mPaint.setColor(Color.BLUE);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(5);
        
        mTextPaint.setColor(Color.GREEN);
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(30);
    }

    /**
     * Set the angle of the level line.
     *
     * @param angle Angle of the level line.
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
        canvas.drawText("Yaw: " + mYaw, 5, 30,  mTextPaint);
        canvas.drawText("Pitch: " + mPitch, 5, 60,  mTextPaint);
        canvas.drawText("Roll: " + mRoll, 5, 90,  mTextPaint);
    }

	public void setYawPitchRoll(float mYaw, float mPitch, float mRoll) {
		this.mYaw = mYaw;
		this.mPitch = mPitch;
		this.mRoll = mRoll;
	}

}
