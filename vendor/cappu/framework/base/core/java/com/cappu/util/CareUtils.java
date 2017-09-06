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
 * @data: 2016年6月26日 下午4:59:16 
 * @author: y.haiyang@qq.com
 * @company: Cappu Co.,Ltd. 
 */

package com.cappu.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;

public class CareUtils {

    private static final String TAG = "CareUtils";
    public static final String EMPTY = "";

    public static int[] getScreenSize(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        int[] size = { dm.widthPixels, dm.heightPixels };
        return size;
    }

    public static String plusString(String... strs) {
        StringBuilder builder = new StringBuilder();
        for (String str : strs) {
            if (!TextUtils.isEmpty(str)) {
                builder.append(str);
            }
        }
        return builder.toString();
    }

    public static String getNowTime() {
        SimpleDateFormat sfd = new SimpleDateFormat("HH:mm");
        Date date = new Date(System.currentTimeMillis());
        return sfd.format(date);
    }

    public static String getTime(Context context, long time) {
        SimpleDateFormat sfd;
        if (DateFormat.is24HourFormat(context)) {
            sfd = new SimpleDateFormat("HH:mm");
        } else {
            sfd = new SimpleDateFormat("a hh:mm");
        }
        Date date = new Date(time);
        return sfd.format(date);
    }

    public static List deepCopy(List src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);
        ByteArrayInputStream byteIn = new ByteArrayInputStream(
                byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        List dest = (List) in.readObject();
        return dest;
    }

}
