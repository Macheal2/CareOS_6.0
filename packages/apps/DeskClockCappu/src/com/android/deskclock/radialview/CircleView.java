/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.deskclock.radialview;

import com.android.deskclock.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

/**
 * Draws a simple white circle on which the numbers will be drawn.
 */
public class CircleView extends View {
    private static final String TAG = "CircleView";

    private final Paint mPaint = new Paint();
    private int mWhite;
    private int mBlack;
    private float mCircleRadiusMultiplier;
    private boolean mIsInitialized;

    private boolean mDrawValuesReady;
    private int mXCenter;
    private int mYCenter;
    private int mCircleRadius;

    public CircleView(Context context) {
        super(context);

        Resources res = context.getResources();
        mWhite = res.getColor(R.color.white);
        mBlack = res.getColor(R.color.numbers_text_color);
        mPaint.setAntiAlias(true);
        mIsInitialized = false;
    }

    public void initialize(Context context) {
        if (mIsInitialized) {
            Log.e(TAG, "CircleView may only be initialized once.");
            return;
        }

        Resources res = context.getResources(); 
        mCircleRadiusMultiplier = Float.parseFloat(res.getString(R.string.circle_radius_multiplier));
        mIsInitialized = true;
    }


    @Override
    public void onDraw(Canvas canvas) {
        int viewWidth = getWidth();
        if (viewWidth == 0 || !mIsInitialized) {
            return;
        }

        if (!mDrawValuesReady) {
            mXCenter = getWidth() / 2;
            mYCenter = getHeight() / 2;
            mCircleRadius = (int) (Math.min(mXCenter, mYCenter) * mCircleRadiusMultiplier);
            mDrawValuesReady = true;
        }

        // Draw the white circle.
        mPaint.setColor(mWhite);
        canvas.drawCircle(mXCenter, mYCenter, mCircleRadius, mPaint);

        // Draw a small black circle in the center.
//        mPaint.setColor(mBlack);
//        canvas.drawCircle(mXCenter, mYCenter, 2, mPaint);
    }
}