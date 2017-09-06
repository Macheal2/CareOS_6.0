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

package sim.android.mtkcit.testitem;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import sim.android.mtkcit.R;

/**
 * A layout which handles the preview aspect ratio and the position of the
 * gripper.
 */
public class PreviewFrameLayout extends ViewGroup {
	private static final int MIN_HORIZONTAL_MARGIN = 10; // 10dp
	private final String TAG = "PreviewFrameLayout";

	/** A callback to be invoked when the preview frame's size changes. */
	public interface OnSizeChangedListener {
		public void onSizeChanged();
	}

	private double mAspectRatio = 4.0 / 3.0;
	// private double mAspectRatio = 3.0/4.0 ;
	private FrameLayout mFrame;
	private OnSizeChangedListener mSizeListener;
	private final DisplayMetrics mMetrics = new DisplayMetrics();

	public PreviewFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(mMetrics);
	}

	public void setOnSizeChangedListener(OnSizeChangedListener listener) {
		mSizeListener = listener;
	}

	@Override
	protected void onFinishInflate() {
		Log.v(TAG, "onFinishInflate");
		mFrame = (FrameLayout) findViewById(R.id.frame);
		if (mFrame == null) {
			throw new IllegalStateException(
					"must provide child with id as \"frame\"");
		}
	}

	public void setAspectRatio(double ratio) {
		if (ratio <= 0.0)
			throw new IllegalArgumentException();

		if (mAspectRatio != ratio) {
			mAspectRatio = ratio;
			requestLayout();
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// Try to layout the "frame" in the center of the area, and put
		// "gripper" just to the left of it. If there is no enough space for
		// the gripper, the "frame" will be moved a little right so that
		// they won't overlap with each other.
		Log.v(TAG, "onLayout");
			int frameWidth = getWidth();
			int frameHeight = getHeight();
			Log.v(TAG, "frameWidth="+frameWidth+"frameHeight="+frameHeight);   //320  382 

				frameWidth = frameWidth ^ frameHeight;
				frameHeight = frameWidth ^ frameHeight;
				frameWidth = frameWidth ^ frameHeight;
//			}
			Log.v(TAG, "frameWidth2="+frameWidth+"frameHeight2="+frameHeight);   //  382   320

			FrameLayout f = mFrame;
				int horizontalPadding = f.getPaddingLeft()
						+ f.getPaddingRight();
				int verticalPadding = f.getPaddingBottom() + f.getPaddingTop();
				Log.v(TAG, "verticalPadding="+verticalPadding+"horizontalPadding="+horizontalPadding);   //320  382 

				// Ignore the vertical paddings, so that we won't draw the frame
				// on the
				// top and bottom sides
				int previewHeight = frameHeight;
				int previewWidth = frameWidth - horizontalPadding;

				// resize frame and preview for aspect ratio
				if (previewWidth > previewHeight * mAspectRatio) {
					previewWidth = (int) (previewHeight * mAspectRatio + .5);
				} else {
					previewHeight = (int) (previewWidth / mAspectRatio + .5);
				}

				frameWidth = previewWidth + horizontalPadding;
				frameHeight = previewHeight + verticalPadding;

				int hSpace = ((r - l) - frameWidth) / 2;
				int vSpace = ((b - t) - frameHeight) / 2;
				mFrame.measure(MeasureSpec.makeMeasureSpec(frameWidth,
						MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
						frameHeight, MeasureSpec.EXACTLY));
				mFrame.layout(l + hSpace, t + vSpace, r - hSpace, b - vSpace);
//			if (mSizeListener != null) {
//				mSizeListener.onSizeChanged();
//			}
		} 


}
