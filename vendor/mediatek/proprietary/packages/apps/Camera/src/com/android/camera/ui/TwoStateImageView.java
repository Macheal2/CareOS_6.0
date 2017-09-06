/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

// dengjianzhang@20150728 add start
import android.content.res.TypedArray;
// dengjianzhang@20150728 add end

/**
 * A @{code ImageView} which change the opacity of the icon if disabled.
 */
public class TwoStateImageView extends ImageView {
    private static final float DISABLED_ALPHA = 0.4f;
    private boolean mFilterEnabled = true;
    
    // dengjianzhang@20150728 add start
    private final static int ATTRS[] = new int[] {android.R.attr.enabled};
    // dengjianzhang@20150728 add end
    public TwoStateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // dengjianzhang@20150728 add start
        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
        boolean enable = a.getBoolean(0, true);
        if(!enable){
            mFilterEnabled = true;
            setEnabled(false);
        }
        a.recycle();
        // dengjianzhang@20150728 add end
    }
    
    public TwoStateImageView(Context context) {
        this(context, null);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mFilterEnabled) {
            if (enabled) {
                setAlpha(1.0f);
            } else {
                setAlpha(DISABLED_ALPHA);
            }
        }
    }

    public void enableFilter(boolean enabled) {
        mFilterEnabled = enabled;
    }
}
