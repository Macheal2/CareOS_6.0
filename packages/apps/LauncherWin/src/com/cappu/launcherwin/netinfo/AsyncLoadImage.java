
package com.cappu.launcherwin.netinfo;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;

/**
 * 线程池+缓存+Handler加载图片
 */
public class AsyncLoadImage {
    //缓存
    private Map<String,SoftReference<Bitmap>> imageCache = new HashMap<String,SoftReference<Bitmap>>();
    //Hanlder
    private Handler mHandler = new Handler();
    public interface ImageCallback {
        void imageLoad(Bitmap image,String imageUrl);
    }
    /**
     * 
     * @param imageUrl 图片的地址
     * @param imageCallback 回调接口
     * @return 返回内存中缓存的图像 第一次返回null
     */
    public Bitmap loadDrawable(final ImageView mIcon,final String imageUrl,final ImageCallback imageCallback){
        if (!TextUtils.isEmpty(imageUrl)) {
            new ImageDownloadTask(mIcon).execute(imageUrl);
        }
        return null ;
    }
    
    /* 读取图片 */
    public static Bitmap loadBitmap(String imgpath) {
        File f=new File(imgpath);
        if(!f.exists()){
            return null;
        }
        return BitmapFactory.decodeFile(imgpath);
    }
    
    /**
     * 从内存缓存中拿
     * 
     * @param url
     */
    public Bitmap getBitmapFromCache(String url) {
        SoftReference<Bitmap> softReference = imageCache.get(url);
        if (softReference != null &&  softReference.get() != null) {// 判断是否有Bitmap
            return softReference.get(); // 有则返回
        } else {
            return null;
        }
    }
    
    /**
     * 加入到内存缓存中
     * 
     * @param url
     * @param bitmap
     */
    public void putBitmapToCache(String url, Bitmap bitmap) {
        imageCache.put(url, new SoftReference<Bitmap>(bitmap));// 将加载的图片放入到内存中
    }
    
    class ImageDownloadTask extends AsyncTask<String, Integer, Bitmap> {
        private String imageUrl;
        private ImageView imageView;

        public ImageDownloadTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            imageUrl = params[0];

            Bitmap bitmap = getBitmapFromCache(imageUrl);
            if (bitmap != null) {
                return bitmap;
            }else{
             // 将图片加入到内存缓存中
                bitmap = loadBitmap(imageUrl);
                if(bitmap != null){
                    putBitmapToCache(imageUrl, bitmap);
                }
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null) {
                // 通过 tag 来防止图片错位
                if (imageView.getTag() != null && imageView.getTag().equals(imageUrl)) {
                    imageView.setImageBitmap(result);
                }
            }
        }
    }

}