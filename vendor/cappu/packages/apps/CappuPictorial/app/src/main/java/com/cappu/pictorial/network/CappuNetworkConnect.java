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

import android.content.Context;

import com.cappu.pictorial.Util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static com.cappu.pictorial.Util.MyLog;


/**
 * Created by hmq on 16-12-1.
 */

public class CappuNetworkConnect {
    private static final String LOG_MSG = "CappuNetworkConnect  ";
    private static CappuNetworkConnect service;


    private CappuNetworkConnect(Context mContext) {
    }

    // HttpGet方式请求
    public static String requestByHttpGet(String postUrl) {
        String result = null;
        HttpResponse httpResponse = null;

        try {
            HttpGet httpget = new HttpGet(postUrl);// 新建HttpGet对象
            httpResponse = new DefaultHttpClient().execute(httpget);// 获取HttpResponse实例

            if (httpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
                // 第三步，使用getEntity方法活得返回结果
                result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                Util.MyLog("i", LOG_MSG + " ----HttpGet方式请求成功，返回数据如下：" + result);
            } else {
                Util.MyLog("i", LOG_MSG + " ----HttpGet方式请求失败 code="+httpResponse.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
            MyLog("e", LOG_MSG + "ERROR --requestByHttpGet IOException msg:" + e.toString());
        }
        return result;
    }

    // HttpGet方式请求
    public static String requestByRedirect302Http(String postUrl) {
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(postUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(2000);
            urlConnection.setConnectTimeout(2000);
            urlConnection.setUseCaches(false);
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.connect();
            int code = urlConnection.getResponseCode();

            if(code / 100 == 3){
                String location= urlConnection.getHeaderField("Location");
                MyLog("i", LOG_MSG + "下载文件需要重定向 code="+code+"; result="+location);
                return requestByRedirect302Http(location);
            }

            String result = urlConnection.getURL().toString();
            MyLog("i", LOG_MSG + "下载文件 code="+code+"; result="+result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            MyLog("e", LOG_MSG + "ERROR --requestByRedirect302Http 302跳转 IOException msg:" + e.getMessage());
        } finally {
            urlConnection.disconnect();
        }

        return null;
    }

    public static byte[] requestByDownloadHttp(String postUrl) {
        HttpURLConnection urlConnection = null;
        byte[] data = null;

        try {
            URL url = new URL(postUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5 * 1000);
            urlConnection.setRequestMethod("GET");
            InputStream inStream = urlConnection.getInputStream();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                MyLog("i", LOG_MSG + "获得图片流，开始保存到本地");
                data = readStream(inStream);
            }
            return data;
        } catch (ProtocolException e) {
            e.printStackTrace();
            MyLog("e", LOG_MSG + "ERROR --requestByDownloadHttp ProtocolException msg:"+e.getMessage());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            MyLog("e", LOG_MSG + "ERROR --requestByDownloadHttp MalformedURLException msg:"+e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            MyLog("e", LOG_MSG + "ERROR --requestByDownloadHttp IOException msg:"+e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            MyLog("e", LOG_MSG + "ERROR --requestByDownloadHttp Exception msg:"+e.getMessage());
        } finally {
            android.util.Log.i("liukun", "urlConnection = " + urlConnection);
            if(urlConnection != null){
                urlConnection.disconnect();
            }
        }
        return null;
    }

    public static byte[] readStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1){
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }
}
