package com.joy.network.impl;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.cappu.downloadcenter.common.entity.DownloadCenterTitle;
import com.joy.network.RecommendShortcutInfo;
import com.joy.network.VirtualShortcutInfo;
import com.joy.network.handler.ActivateHanlder;
import com.joy.network.handler.BitmapHandler;
import com.joy.network.handler.RecommendDataHandler;
import com.joy.network.handler.VirtualShortcutListHandler;
import com.joy.network.util.ClientHttp;
import com.joy.network.util.ClientHttp2;
import com.joy.network.util.ClientInterface;
import com.joy.network.util.Protocal;
import com.joy.util.Logger;

/**
 * ����ӿڵľ���ʵ��
 * 
 * @author wanghao
 */
public class Service {

    private static Service service;

    ClientInterface cs = null;

    ProtocalFactory pfactory;

    Map<String, Protocal> protocals = Collections.synchronizedMap(new HashMap<String, Protocal>());

    public static Context mContext;

    private Service(Context mContext) {
        this.mContext = mContext;
    };

    public static synchronized Service getInstance() {
        if (service == null) {
            Logger.info("Service", "service == null");
        }
        return service;
    }

    public static synchronized Service getInstance(Context context, boolean formal) {
        if (service == null) {
            service = new Service(context);
            service.cs = new ClientHttp();
            service.pfactory = new ProtocalFactory(formal);
        }
        return service;
    }

    public void shutdownNetwork() {
        cs.shutdownNetwork();
        cs = null;
        service = null;
    }

    public Bitmap getBitmapByUrl(String url) {
        Protocal protocal = pfactory.bitmapProtocal(url);
        InputStream in = cs.getInputStream(protocal);
        BitmapHandler bhandler = new BitmapHandler();
        Bitmap bp = bhandler.getBitmapByUrl(in, url);
        return bp;
    }

    public InputStream getDownLoadInputStream(String url) {

        Protocal protocal = pfactory.downloadApkProtocal(url);
        InputStream iStream = cs.getInputStream(protocal);
        protocals.put(url, protocal);
        return iStream;
    }

    public long getDownLoadInputStreamLength(String url) {
        Protocal protocal = pfactory.downloadApkProtocal(url);
        long totalsize = cs.getDownloadFileSize(protocal);

        return totalsize;
    }

    public InputStream getDownLoadInputStream(String url, int startPos, int endPos) {

        Protocal protocal = pfactory.downloadApkProtocal(url);
        protocal.setStartPos(startPos);
        protocal.setEndPos(endPos);
        InputStream iStream = cs.getInputStream(protocal);
        protocals.put(url, protocal);
        return iStream;
    }

    public long getupdateInputStreamLength(String url) {
        Protocal protocal = pfactory.updateProtocal(url);
        long totalsize = cs.getDownloadFileSize(protocal);

        return totalsize;
    }

    public InputStream getupdateInputStream(String url, int startPos, int endPos) {
        Protocal protocal = pfactory.updateProtocal(url);
        protocal.setStartPos(startPos);
        protocal.setEndPos(endPos);
        InputStream iStream = cs.getInputStream(protocal);
        protocals.put(url, protocal);
        return iStream;
    }

    public InputStream getPushDownLoadInputStream(String url, int startPos, int endPos) {

        Protocal protocal = pfactory.downloadPushApkProtocal(url);
        protocal.setStartPos(startPos);
        protocal.setEndPos(endPos);
        InputStream iStream = cs.getInputStream(protocal);
        protocals.put(url, protocal);
        return iStream;
    }

    public boolean getIsBreakPoint(String url) {
        Protocal protocal = protocals.get(url);
        boolean isBreakPoint = false;
        if (protocal != null) {
            isBreakPoint = protocal.getIsBreakPoint();
        }
        protocals.remove(url);
        return isBreakPoint;
    }

    // add by huangming for push.
    public InputStream getDownLoadPushApkInputStream(int id) {

        Protocal protocal = pfactory.downloadPushApkProtocal(id);
        InputStream iStream = cs.getInputStream(protocal);
        return iStream;
    }

    public InputStream getDownLoadPushApkInputStream(String url) {

        Protocal protocal = pfactory.downloadPushApkProtocal(url);
        InputStream iStream = cs.getInputStream(protocal);
        return iStream;
    }

    public Bitmap getDownLoadPushImage(int id) {

        Protocal protocal = pfactory.downloadPushImageProtocal(id);
        InputStream in = cs.getInputStream(protocal);
        BitmapHandler handler = new BitmapHandler();
        Bitmap image = handler.getBitmapByUrl(in);
        return image;
    }

    public Bitmap getDownLoadPushImage(String url) {

        Protocal protocal = pfactory.downloadPushImageProtocal(url);
        InputStream in = cs.getInputStream(protocal);
        BitmapHandler handler = new BitmapHandler();
        Bitmap image = handler.getBitmapByUrl(in);
        return image;
    }

    public boolean activateLauncher() {
        Protocal protocal = pfactory.activateProtocal();
        JSONObject result = null;
        try {
            result = cs.request(protocal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ActivateHanlder activate = new ActivateHanlder();
        boolean isActivate = activate.isActivate(result);

        return isActivate;
    }

    public JSONObject checkUpdate() {
        Protocal protocal = pfactory.updateProtocal();
        JSONObject result = null;
        try {
            result = cs.request(protocal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<VirtualShortcutInfo> getShortcutListInFolder(int folderType) {
        Protocal protocal = pfactory.getAppInFolderProtocal(folderType);
        JSONObject result = null;
        try {
            result = cs.request(protocal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        VirtualShortcutListHandler handler = new VirtualShortcutListHandler();

        return handler.geShortcutList(result);
    }

    public List<RecommendShortcutInfo> getRecommendList(int type, int index, int num) {
        Log.e("dengyingMark", "Service.java getRecommendList");

        Protocal protocal = pfactory.getRecommendListProtocal(type, index, num);
        JSONObject result = null;
        try {
            result = cs.request(protocal);

        } catch (Exception e) {
            Log.e("dengyingMark", "getRecommendList Exception=" + e.toString());

            e.printStackTrace();
        }

        RecommendDataHandler handler = new RecommendDataHandler();

        return handler.getAppList(result);
    }

    /**
     * 获得分类APP列表
     * 
     * @param type
     * @return 返回数据列表
     * @author hmq
     */
    public List<RecommendShortcutInfo> getRecommendChild(int type) {
        Protocal protocal = pfactory.getRecommendChildProtocal(type);
        JSONObject result = null;
        try {
            result = cs.request(protocal);

        } catch (Exception e) {
            Log.e("hhmq", "getRecommendChild Exception=" + e.toString());

            e.printStackTrace();
        }
        RecommendDataHandler handler = new RecommendDataHandler();
        return handler.getAppList(result);
    }

    /**
     * 获得标题列表
     * 
     * @param type
     * @return 返回数据列表
     * @author hmq
     */
    public List<DownloadCenterTitle> getRecommendTitles() {

        Protocal protocal = pfactory.getRecommendTitleProtocal();
        JSONObject result = null;

        try {
            result = cs.request(protocal);
        } catch (Exception e) {
            Log.e("hhmq", "getRecommendTitles Exception=" + e.toString());

            e.printStackTrace();
        }
        RecommendDataHandler handler = new RecommendDataHandler();
        Log.e("hhmq", "getRecommendTitles result=" + result);
        return handler.getAppTitle(result);
    }

    public JSONObject getPushSettings() {
        Protocal protocal = pfactory.pushSettingsProtocal();
        JSONObject json = null;
        try {
            json = cs.request(protocal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONObject getPushList() {
        Protocal protocal = pfactory.pushListProtocal();
        JSONObject json = null;
        try {
            json = cs.request(protocal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONObject getPushDetail(int id) {
        Protocal protocal = pfactory.pushDetailProtocal(id);
        JSONObject json = null;
        try {
            json = cs.request(protocal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONObject getWallPaperListJson(int category, int previousPage) {
        Protocal protocal = pfactory.wallpaperListProtocal(category, previousPage);
        JSONObject result = null;
        try {
            result = cs.request(protocal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public JSONObject getWallpaperCategoryJson() {
        Protocal protocal = pfactory.wallpaperCategoryProtocal();
        JSONObject result = null;
        try {
            result = cs.request(protocal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Bitmap getWallpaperBitmap(String data, int width, int height) {
        Protocal protocal = pfactory.wallpaperBitmapProtocal(data, width, height);
        InputStream in = cs.getInputStream(protocal);
        BitmapHandler handler = new BitmapHandler();
        Bitmap image = handler.getBitmapByUrl(in);
        return image;
    }

    public Bitmap getWallpaperBitmap(String data, int width, int height, int referenceWidth) {
        Protocal protocal = pfactory.wallpaperBitmapProtocal(data, width, height);
        InputStream in = cs.getInputStream(protocal);
        BitmapHandler handler = new BitmapHandler();
        Bitmap image = handler.getBitmapByUrl(in, referenceWidth);
        return image;
    }

    public InputStream getWallpaperInputStream(String data, int width, int height) {
        Protocal protocal = pfactory.wallpaperBitmapProtocal(data, width, height);
        InputStream is = cs.getInputStream(protocal);
        return is;
    }

}
