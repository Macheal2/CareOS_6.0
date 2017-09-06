/** 
 * Copyright (C) 2014 The Cappu Android Source Project
 *
 * Licensed under the Cappu License, (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.cappu.cn
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @data: 2015年6月29日 下午4:59:16 
 * @author: y.haiyang@qq.com
 * @company: Shang Hai Cappu Co.,Ltd. 
 */

package com.cappu.util;

import android.content.Context;

/**
 * @hide
 */
public class CareConfigure {

    public static final int PROGRESS_DIALOG_COUNT = 30;

    public static int getTopBarHeight(Context context){
        
        return context.getResources().getDimensionPixelOffset(com.cappu.internal.R.dimen.topbar_height);
    }

}
