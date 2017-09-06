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

import android.text.TextUtils;

import com.cappu.pictorial.PictorialInfo;
import com.cappu.pictorial.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ParseJsonUtils {
    public static final String RT = "rt";
    public static final String VERSION = "version";
    public static final String ITEM = "pictures";
    public static final String COUNT = "count";
    private static final String LOG_MSG = "ParseJsonUtils  ";
    /**
     * 获取消息队列
     *
     * @param values
     * @param table
     * @return
     */
    public static List<PictorialInfo> ParseToArray(String values, String table, int version) {
        if (values == null) {
            return null;
        }
        List<PictorialInfo> list = new ArrayList<PictorialInfo>();

        try {
            JSONObject jsonObject = new JSONObject(values);
            // 返回json的数组
            JSONArray jsonArray = jsonObject.getJSONArray(table);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);

                String id = jsonObject2.getString("id");
                String md5 = jsonObject2.getString("md5");
                String resolution = jsonObject2.getString("resolution");
                String suffixes = jsonObject2.getString("suffixes");
                String summary = jsonObject2.getString("summary");
                long time = jsonObject2.getLong("time");
                String url = jsonObject2.getString("url");
                String fileSavePath = null;

                PictorialInfo item = new PictorialInfo(id, md5, resolution, suffixes, summary, time, url, fileSavePath);
                item.setIndex(i);
                item.setState(PictorialInfo.ISREADY);
                item.setVersion(version);
                list.add(item);
                Util.MyLog("i", LOG_MSG + "握手获得第"+ i +"记录 ::"+item.toString());
            }
            return list;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            Util.MyLog("e", LOG_MSG + "ERROR --ParseToArray parse Exception msg:"+e.getMessage());
        }
        return null;
    }

    public static boolean ParseToItemBoolean(String json,String key) {
        boolean rt = false;
        if (json == null) {
            return rt;
        }

        try {
            JSONObject jsonObject = new JSONObject(json);
            rt = jsonObject.getBoolean(key);
            return rt;
        } catch (Exception e) {
            // TODO: handle exception
            Util.MyLog("e", LOG_MSG + "ERROR --ParseToItemBoolean Exception msg:"+e.getMessage());
            return rt;
        }
    }

    public static String ParseToItemString(String json,String key) {
        String rt = null;
        if (TextUtils.isEmpty(json)) {
            return rt;
        }

        if(!json.contains(key)){
            return rt;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            rt = jsonObject.getString(key);
            return rt;
        }catch (JSONException e){
            Util.MyLog("e", LOG_MSG + "ERROR --ParseToItemString JSONException msg:"+e.getMessage());
            return rt;
        }
    }

    public static long ParseToItemLong(String json,String key) {
        long rt = -1;
        if (json == null) {
            return rt;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            rt = jsonObject.getLong(key);
            return rt;
        }catch (JSONException e){
            Util.MyLog("e", LOG_MSG + "ERROR --ParseToItemLong JSONException msg:"+e.getMessage());
            return rt;
        }
    }

    public static int ParseToItemInt(String json,String key) {
        int rt = -1;
        if (json == null) {
            return rt;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            rt = jsonObject.getInt(key);
            return rt;
        }catch (JSONException e){
            Util.MyLog("e", LOG_MSG + "ERROR --ParseToItemInt JSONException msg:"+e.getMessage());
            return rt;
        }
    }
}
