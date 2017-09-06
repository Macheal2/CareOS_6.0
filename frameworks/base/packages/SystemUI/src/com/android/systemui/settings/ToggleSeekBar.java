/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.systemui.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;

public class ToggleSeekBar extends SeekBar {
    private String mAccessibilityLabel;
    private float oldpointX;//edit by hmq 20151025 Modify for 老人机下拉状态栏 精度条拖动有效，直接点击无效
	
    public ToggleSeekBar(Context context) {
        super(context);
    }

    public ToggleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs, 0);// edit by hmq 20151025 Modify for 老人机下拉状态栏 [使用自定义的attrs，如果不加0，是被SeekBar父类替换]
    }

    public ToggleSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            setEnabled(true);
        }
        /* begin: edit by hmq 20151025 Modify for 老人机下拉状态栏 */
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            oldpointX = event.getX();

            break;
        case MotionEvent.ACTION_UP:
            if (event.getX() == oldpointX){
                return false;
            }
            break;
        }
        /* end: edit by hmq 20151025 Modify for 老人机下拉状态栏 */
        return super.onTouchEvent(event);
    }

    public void setAccessibilityLabel(String label) {
        mAccessibilityLabel = label;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (mAccessibilityLabel != null) {
            info.setText(mAccessibilityLabel);
        }
    }
}
