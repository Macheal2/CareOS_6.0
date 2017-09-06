/**
 *  
 * Copyright (C) 2016 The Cappu Android Source Project
 * <p>
 * Licensed under the Cappu License, (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.cappu.cn
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @data: 2016年12月1日
 * @author: huangminqi@cappu.cn
 * @company: Cappu Co.,Ltd. 
 */
package com.cappu.pictorial.network;

import android.util.Base64;

import com.cappu.pictorial.Util;


public class ProtocalFactory {
    /** 申请画报的端口号 */
//    public static final int OP_WALLPAPER_LIST = 2001;
    public static String SIGN_KEY = "cappu-g@od";
    public static String HOST_PUSH = "http://theme.careos.com.cn/theme/validate/theme?";
    //"http://192.168.0.51:8088/theme2/validate/theme?";//"http://192.168.0.51:8082/theme/v1/theme.do?model=c630&version=0";
    private static final String LOG_MSG = "ProtocalFactory  ";

    public ProtocalFactory() {
    }

    public static String getServerURL(int version){
        StringBuffer basePath = new StringBuffer();
        StringBuffer url = new StringBuffer();
        basePath.append("model=");
        basePath.append(android.os.Build.MODEL);
        basePath.append("&version=");
        basePath.append(version);
        basePath.append(SIGN_KEY);

        url.append(HOST_PUSH);
        url.append("token=");
        url.append(Base64.encodeToString(basePath.toString().getBytes(), Base64.DEFAULT));
        Util.MyLog("i",LOG_MSG+"url="+url.toString().getBytes()+"; info="+basePath.toString());
        //e.g http://theme.careos.com.cn/theme/validate/theme?token=bW9kZWw9YzYzMCZ2ZXJzaW9uPTBjYXBwdS1nQG9
        return url.toString();
    }
}
