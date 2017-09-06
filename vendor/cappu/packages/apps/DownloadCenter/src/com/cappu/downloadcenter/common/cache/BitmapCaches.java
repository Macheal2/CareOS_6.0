package com.cappu.downloadcenter.common.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import android.util.Log;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.joy.network.RecommendShortcutInfo;
import com.joy.network.impl.ProtocalFactory;
import com.joy.network.impl.Service;
import com.joy.network.util.AsyncTask;
import com.joy.network.util.AsyncTask.CallBack;
import com.joy.util.Logger;
import com.joy.util.Util;
import com.cappu.downloadcenter.common.Constants;
import com.cappu.downloadcenter.R;

public class BitmapCaches {

    private static BitmapCaches cache;

    private Map<String, MySoftRef> hashRefs;

    private ReferenceQueue<Bitmap> queue;

    private Service mService;
    
    private static String TAG =com.cappu.downloadcenter.common.utils.Util.TAG;
    private static boolean DEBUG = com.cappu.downloadcenter.common.utils.Util.DEBUG;
    private static boolean ERROR_DEBUG = com.cappu.downloadcenter.common.utils.Util.ERROR_DEBUG;

    private class MySoftRef extends SoftReference<Bitmap> {
        public String key;

        public MySoftRef(Bitmap bmp, ReferenceQueue<Bitmap> queue, String key) {
            super(bmp, queue);
            this.key = key;
        }
    }

    private BitmapCaches() {
        hashRefs = new Hashtable<String, MySoftRef>();
        queue = new ReferenceQueue<Bitmap>();
        try {
            mService = Service.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BitmapCaches getInstance() {
        if (cache == null) {
            cache = new BitmapCaches();
        }
        return cache;
    }

    private void addCacheBitmap(String key, Bitmap bmp) {
        cleanCache();
        MySoftRef ref = new MySoftRef(bmp, queue, key);
        hashRefs.put(key, ref);
    }
    
    @SuppressWarnings("deprecation")
    private void setBitmap(View view, Bitmap bm) {
        if (view == null || bm == null) {
        } else if (view instanceof ImageView) {
            ImageView iv = (ImageView) view;
            iv.setImageBitmap(bm);
        } else if (view instanceof LinearLayout) {
            LinearLayout ll = (LinearLayout) view;
            ll.setBackgroundDrawable(new BitmapDrawable(bm));
        } else {
            Logger.warn(this, "setBitmap--!imageview|linearlayout");
        }
    }
    
    public final static String suffix = ".png";
    
    public Bitmap haveImg (final Context context, RecommendShortcutInfo recommendInfo){
        final String url = recommendInfo.icon;
        final String key = getFileNameByUrl(url, "?");
        String imageName = key + suffix;
        Bitmap bm = null;
//        Paint paint = new Paint();
//        Bitmap forebitmap = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.mark)).getBitmap();
        
//        paint.setAntiAlias(true);// 抗锯齿
//        paint.setDither(true);// 防抖动
//        paint.setFilterBitmap(true);// 滤波
        if (hashRefs.containsKey(key)) {//已经保存在MAP中
            MySoftRef ref = hashRefs.get(key);
            bm = ref.get();
            if (bm != null) {
                return bm;
            }
        }
        
        try {
            //bm = BitmapFactory.decodeStream(new FileInputStream(Constants.DOWNLOAD_IMAGE_DIR + "/" + imageName));
            bm = BitmapFactory.decodeFile(Constants.DOWNLOAD_IMAGE_DIR + "/" + imageName);
        } catch (OutOfMemoryError e) {
            if(ERROR_DEBUG)Log.e(TAG, "ERROR haveImg --OutOfMemoryError:" + Constants.DOWNLOAD_IMAGE_DIR + "/" + imageName);
        } catch (Exception e) {
        }
        if (bm != null) {
            return bm;
        }
        return null;
    }
    
    public void setSaveAppIcon(Context context, RecommendShortcutInfo recommendInfo,ImageDownLoadCallback imageDownLoadCallback){
        this.setSaveAppIcon(context, null, recommendInfo, imageDownLoadCallback);
    }
    
    public void setSaveAppIcon(Context context, ImageView imgview, RecommendShortcutInfo recommendInfo, ImageDownLoadCallback imageDownLoadCallback){
        String url = recommendInfo.icon;
        String key = getFileNameByUrl(url, "?");
        
        new MyTask(imgview, key, context, imageDownLoadCallback).execute(url);
    }
    
    class MyTask extends android.os.AsyncTask<String, Integer, Void> {
        public String key;
        public ImageView imgview;
        public Context context;
        public ImageDownLoadCallback imageDownLoadCallback;
        public Bitmap bitmap=null;
        
        public MyTask(ImageView imgview, String key, Context context, ImageDownLoadCallback imageDownLoadCallback){
            this.imgview = imgview;
            this.context = context;
            this.key = key;
            this.imageDownLoadCallback = imageDownLoadCallback;
        }
        
        @Override
        protected void onPreExecute() {
            if (imageDownLoadCallback != null) {
                imageDownLoadCallback.imageDownLoaded(1, null);
            }
        }
        
        @Override
        protected Void doInBackground(String... params) {
            bitmap = mService.getBitmapByUrl(params[0]);
            if (imageDownLoadCallback != null) {
                imageDownLoadCallback.imageDownLoaded(2, null);
            }
            return null;

        }
        
        // 在doInBackground方法执行结束后再运行，并且运行在UI线程当中
        // 主要用于将异步操作任务执行的结果展示给用户
        @Override
        protected void onPostExecute(Void a) {
            if (bitmap != null) {
                Bitmap bp = fermodeImage(context, bitmap);
                addCacheBitmap(key, bp);
                saveBitmapToSD(key, bp, suffix);
                if (imgview != null){
                    imgview.setImageBitmap(bp);
                }
                if (imageDownLoadCallback != null) {
                    imageDownLoadCallback.imageDownLoaded(3, bp);
                }
            }
        }
    }
    
//    public Bitmap getBitmap(Context context, RecommendShortcutInfo recommendInfo) {
//        BitmapCaches.getInstance().getBitmap(context, recommendInfo.icon, null);
//        return null;
//    }

    public Bitmap getBitmap(Context context, final String url, final ImageDownLoadCallback imageDownLoadCallback) {
        final String key = getFileNameByUrl(url, "?");
        final Context context_final = context;
        
        Bitmap bm = null;
        if (hashRefs.containsKey(key)) {//已经保存在MAP中
            MySoftRef ref = hashRefs.get(key);
            bm = ref.get();
            if (bm != null) {
                Logger.info(this, "getBitmap,cache");
                if (imageDownLoadCallback != null) {
                    imageDownLoadCallback.imageDownLoaded(3,bm);
                }
                return bm;
            }
        }

        Integer resId = null;
        try {
            resId = Integer.parseInt(key);
        } catch (Exception e) {
        }
        if (resId != null) {//含有资源文件
            bm = Util.getBitmapById(resId);
            if (bm != null) {
                Logger.info(this, "getBitmap,from drawable" + key);
                addCacheBitmap(key, bm);
                if (imageDownLoadCallback != null) {
                    imageDownLoadCallback.imageDownLoaded(3,bm);
                }
                return bm;
            }
        }
        String imageName = key + suffix;
        try {
            bm = BitmapFactory.decodeStream(new FileInputStream(Constants.DOWNLOAD_IMAGE_DIR + "/" + imageName));
        } catch (OutOfMemoryError e) {
            if(ERROR_DEBUG)Log.e(TAG, "Error getBitmap--OutOfMemoryError:" + Constants.DOWNLOAD_IMAGE_DIR + "/" + imageName);
        } catch (Exception e) {
        }
        if (bm != null) {//已经下载过文件，保存在本地
            Logger.info(this, "getBitmap,sd" + key);
            addCacheBitmap(key, bm);
            if (imageDownLoadCallback != null) {
                imageDownLoadCallback.imageDownLoaded(3,bm);
            }
            return bm;
        }
        
        return null;
    }
    
    public void getBitmap(Context context, final String key, final View view, final ImageDownLoadCallback imageDownLoadCallback) {
        getBitmap(context, key, view, imageDownLoadCallback, ".png");
    }
    
    public void getBitmap(Context context, final String url, final View view, final ImageDownLoadCallback imageDownLoadCallback, final String suffix) {
        final String key = getFileNameByUrl(url, "?");
        final Context context_final = context;
        
        Bitmap bm = null;
        if (hashRefs.containsKey(key)) {
            MySoftRef ref = hashRefs.get(key);
            bm = ref.get();
            if (bm != null) {
                Logger.info(this, "getBitmap,cache");
                setBitmap(view, bm);
                if (imageDownLoadCallback != null) {
                    imageDownLoadCallback.imageDownLoaded(3,bm);
                }
                return;
            }
        }

        Integer resId = null;
        try {
            resId = Integer.parseInt(key);
        } catch (Exception e) {
        }
        if (resId != null) {
            bm = Util.getBitmapById(resId);
            if (bm != null) {
                Logger.info(this, "getBitmap,from drawable" + key);
                addCacheBitmap(key, bm);
                setBitmap(view, bm);
                if (imageDownLoadCallback != null) {
                    imageDownLoadCallback.imageDownLoaded(3,bm);
                }
                return;
            }
        }

        String imageName = key + suffix;
        try {
            bm = BitmapFactory.decodeStream(new FileInputStream(Constants.DOWNLOAD_IMAGE_DIR + "/" + imageName));
        } catch (OutOfMemoryError e) {
            Logger.warn(this, "getBitmap--OutOfMemoryError:" + Constants.DOWNLOAD_IMAGE_DIR + "/" + imageName);
        } catch (Exception e) {
        }
        if (bm != null) {
            Logger.info(this, "getBitmap,sd" + key);
            addCacheBitmap(key, bm);
            setBitmap(view, bm);
            if (imageDownLoadCallback != null) {
                imageDownLoadCallback.imageDownLoaded(3,bm);
            }
            return;
        }

        AsyncTask.getInstance().run(new CallBack() {
            Bitmap bitmap = null;

            @Override
            public void onPreExecute() {

            }

            @Override
            public void onPostExecute() {
                setBitmap(view, bitmap);
                if (imageDownLoadCallback != null) {
                    imageDownLoadCallback.imageDownLoaded(3,bitmap);
                }
            }

            @Override
            public void doInBackground() {
                bitmap = mService.getBitmapByUrl(url);
                bitmap = fermodeImage(context_final, bitmap);
                if (bitmap != null) {
                    Logger.info(this, "getBitmap,network:" + url);
                    addCacheBitmap(key, bitmap);
                    
//                    saveBitmapToSD(key, bitmap, suffix);
                } else {
                }
            }
        });
    }

    private Bitmap fermodeImage(Context context, Bitmap bitmap){
//        Drawable bg = context.getResources().getDrawable(R.drawable.mark);
//        if (bitmap == null){
//            return null;
//        }
        Bitmap forebitmap = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.mark)).getBitmap();
        Bitmap backbitmap = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.mark_stroke)).getBitmap();
        
        int height = forebitmap.getHeight();
        int width = forebitmap.getWidth();
        
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN)); 
        
        Bitmap iconbitmap = Util.zoomBitmap(bitmap, width, height);
        Canvas canvas = new Canvas(iconbitmap);
        canvas.drawBitmap(forebitmap, 0f, 0f, paint);
        paint.setXfermode(null);
        
        Bitmap returnbitmap = Bitmap.createBitmap(backbitmap.getWidth(), backbitmap.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(returnbitmap); 
        canvas.drawBitmap(backbitmap, 0, 0, null);
        float offsetX = (backbitmap.getWidth() - width) / 2;
        float offsetY = (backbitmap.getHeight() - height) / 2;
        canvas.drawBitmap(iconbitmap, offsetX, offsetY, null);
        
        return returnbitmap;
    }
    
    private void saveBitmapToSD(String key, Bitmap bm, String suffix) {
        if (Util.hasSdcard() && bm != null) {
            try {
                String fileName = key + suffix;
                File file = new File(Constants.DOWNLOAD_IMAGE_DIR + "/" + fileName);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                if (fileName.toUpperCase().endsWith(".PNG")) {
                    bm.compress(CompressFormat.PNG, 90, fos);
                } else {
                    bm.compress(CompressFormat.JPEG, 90, fos);
                }
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Logger.info(this, "FileNotFoundException :" + e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void cleanCache() {
        MySoftRef ref = null;
        while ((ref = (MySoftRef) queue.poll()) != null) {
            hashRefs.remove(ref.key);
        }
    }

    private String getFileNameByUrl(String url, String split) {
        if (url == null || "".equals(url.trim())) {
            return null;
        }
        return url.substring(url.lastIndexOf(split) + 1);
    }

    public void clearCache() {
        cleanCache();
        hashRefs.clear();
        hashRefs = null;
        System.runFinalization();
    }
}
