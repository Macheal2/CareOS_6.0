/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 */

package com.magcomm.soundrecorder.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.android.soundrecorder.R;
import com.android.soundrecorder.Recorder;
import com.android.soundrecorder.SoundRecorderService;

public class VUMeter extends View {

	public static final int POWER_ON = 1 ;
	public static final int POWER_OFF = 2 ;
    private static final float DROPOFF_STEP = 0.18f;
    private static final long ANIMATION_INTERVAL = 70;
    private static final float MIN_ANGLE = (float) Math.PI / 8;
    private static final float MAX_ANGLE = (float) Math.PI * 7 / 8;
    private static final float BASE_NUMBER = 32768;
	public float mCurrentAngle;
	
	private int mViewHight , mViewWidth;
	public int mCurrentSingle;
	public int mStatus = POWER_OFF;
	
	private final Context mContext;
	private Recorder mRecorder;
	
	private static final int [] SINGLE =
		{ R.drawable.window_power_on_0,R.drawable.window_power_on_1,
		  R.drawable.window_power_on_2,R.drawable.window_power_on_3,
		  R.drawable.window_power_on_4,R.drawable.window_power_on_5,
		  R.drawable.window_power_on_6,R.drawable.window_power_on_7,
		  R.drawable.window_power_on_8,R.drawable.window_power_on_9,
		  R.drawable.window_power_on_10,R.drawable.window_power_on_11,
		  R.drawable.window_power_on_12,R.drawable.window_power_on_13,
		  R.drawable.window_power_on_14,R.drawable.window_power_on_15,
		  R.drawable.window_power_on_16
		};

    /**
     * constructor of VUMeter
     *
     * @param context
     *            the context that show VUMeter
     */
    public VUMeter(Context context) {
        super(context);
        mContext = context;
        init();
    }

    /**
     * constructor of VUMeter
     *
     * @param context
     *            the context that show VUMeter
     * @param attrs
     *            AttributeSet
     */
    public VUMeter(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    /**
     * set Recorder that bind with this VUMeter
     *
     * @param recorder
     *            Recorder to be set
     */
    public void setRecorder(Recorder recorder) {
        mRecorder = recorder;
        invalidate();
    }
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
		setMeasuredDimension(mViewWidth,mViewHight);
	}
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float minAngle = MIN_ANGLE;
        final float maxAngle = MAX_ANGLE;

        float angle = minAngle;
        if (null != mRecorder) {
            angle += (float) (maxAngle - minAngle) * mRecorder.getMaxAmplitude() / BASE_NUMBER;
        }

        if (angle > mCurrentAngle) {
            mCurrentAngle = angle;
        } else {
            mCurrentAngle = Math.max(angle, mCurrentAngle - DROPOFF_STEP);
        }

        mCurrentAngle = Math.min(maxAngle, mCurrentAngle);
		mCurrentSingle = computeSingle(mCurrentAngle);

		Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), SINGLE[mCurrentSingle]);
		canvas.drawBitmap(bmp, 0, 0, null);
        if ((null != mRecorder)
                && (SoundRecorderService.STATE_RECORDING == mRecorder.getCurrentState())) {
            postInvalidateDelayed(ANIMATION_INTERVAL);
        }
    }

	private int computeSingle(float angle){
		float dis = (MAX_ANGLE - MIN_ANGLE)/17 ;
        for (int i=0 ; i< 17 ; i++){
			if(angle < MIN_ANGLE + (i+1)*dis){
				return i;
			}
		}
		return 0;
	}

	public void setStatus( int status ){
		if( status == POWER_ON) {
			setBackgroundResource(R.drawable.window_power_on);
		}else if ( status == POWER_OFF ){
			setBackgroundResource(R.drawable.window_power_off);
		}	
	}
	
    /**
     * initialize variable of VUMter
     */
    private void init() {
		Bitmap bgBit = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.window_power_off);
		mViewHight = bgBit.getHeight();
		mViewWidth = bgBit.getWidth();
        setBackgroundResource(R.drawable.window_power_off);
        mRecorder = null;
        mCurrentAngle = 0;
    }
}
