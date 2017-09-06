package com.joy.network.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.util.Log;

import com.cappu.downloadcenter.common.entity.DownloadCenterTitle;
import com.joy.network.RecommendShortcutInfo;
import com.joy.network.VirtualShortcutInfo;
import com.joy.util.Constants;
import com.joy.util.Logger;
import com.joy.util.Util;

public class RecommendDataHandler {

    // public static int index = 1;
    // public static int num = 1;

    public List<RecommendShortcutInfo> getAppList(JSONObject json) {

        List<RecommendShortcutInfo> arrayList = new ArrayList<RecommendShortcutInfo>();
        try {
            if (json == null || json.getInt("state") != 1) {
                return null;
            }
//            JSONObject pageJsonObject = json.getJSONObject("page");
//            int index = pageJsonObject.getInt("pi");
//            int num = pageJsonObject.getInt("pn");

            JSONArray jsonarry = json.getJSONArray("item");

            int length = jsonarry.length();

            for (int i = 0; i < length; i++) {
                JSONObject item = jsonarry.getJSONObject(i);
                RecommendShortcutInfo info = new RecommendShortcutInfo();
                info.id = item.isNull("id") ? 0 : item.getInt("id");
                info.icon = item.isNull("icon") ? null : item.getString("icon");
                info.url = item.isNull("url") ? null : item.getString("url");
                info.size = item.isNull("size") ? 0 : item.getInt("size");
                info.packageName = item.isNull("packagename") ? null : item.getString("packagename");
                info.name = item.isNull("name") ? null : item.getString("name");
//                info.index = index;
//                info.num = num;
                Log.e("hhmq", "getAppList " + item.toString());
                arrayList.add(info);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.warn(this, "JSONException e:" + e);
            Log.e("dengyingMark", "getAppList" + " JSONException e:" + e);
            return null;
        }
        return arrayList;
    }

    public List<DownloadCenterTitle> getAppTitle(JSONObject json) {

        List<DownloadCenterTitle> arrayList = new ArrayList<DownloadCenterTitle>();
        try {

            if (json == null || json.getInt("state") != 1) {
                return null;
            }
//            JSONObject pageJsonObject = json.getJSONObject("page");//共几页
//            int index = pageJsonObject.getInt("pi");//第几页
//            int num = pageJsonObject.getInt("pn");//当前页申请多少应用

            JSONArray jsonarry = json.getJSONArray("item");

            int length = jsonarry.length();

            for (int i = 0; i < length; i++) {
                JSONObject item = jsonarry.getJSONObject(i);
                DownloadCenterTitle info = new DownloadCenterTitle();
                info.id = item.isNull("id") ? 0 : item.getInt("id");//id
                info.name = item.isNull("name") ? null : item.getString("name");//名称

                Log.e("hhmq", "getAppTitle " + item.toString());

                arrayList.add(info);
            }
        } catch (JSONException e) {
            e.printStackTrace();
//            Logger.warn(this, "JSONException e:" + e);
            Log.e("hhmq", "getAppList" + " JSONException e:" + e);
            return null;
        }
        return arrayList;
    }
    
    // public static String getAppLisLocalInfo(int type) {
    //
    // String string = Util.readString(Constants.DOWNLOAD_JSON_DIR + "/" + type
    // + "-" + Constants.FILENAME_APP_LIST);
    // return string;
    // }
    //
    // private static void saveAppLisLocalInfo(int type, String string) {
    // if (string != null && !string.equals("")) {
    // Util.saveString(Constants.DOWNLOAD_JSON_DIR + "/" + type + "-" +
    // Constants.FILENAME_APP_LIST, string);
    // }
    // }
}