/** 
 * Copyright (C) 2015 The yuhaiyang Android Source Project
 *
 * Licensed under the yuhaiyang License, (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://coding.net/yuhaiyang/License.git
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @data: 2015年6月26日 下午4:59:16 
 * @author: y.haiyang@qq.com
 */

package com.cappu.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class RecycleListView extends ListView {
    public boolean mRecycleOnMeasure = true;

    public RecycleListView(Context context) {
        super(context);
    }

    public RecycleListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecycleListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected boolean recycleOnMeasure() {
        return mRecycleOnMeasure;
    }
}
