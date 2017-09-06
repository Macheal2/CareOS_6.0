package com.cappu.music;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;

public class SyncImageLoader {

    private Object lock = new Object();

    private boolean mAllowLoad = true;

    private boolean firstLoad = true;

    private int mStartLoadLimit = 0;

    private int mStopLoadLimit = 0;

    final Handler handler = new Handler();

    private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();

    public interface OnImageLoadListener {
        public void onImageLoad(Integer position, Bitmap bitmap);

        public void onError(Integer position);
    }
    
    private Context mContext;
    public SyncImageLoader(Context context) {
        mContext = context;
    }

    public void setLoadLimit(int startLoadLimit, int stopLoadLimit) {
        if (startLoadLimit > stopLoadLimit) {
            return;
        }
        mStartLoadLimit = startLoadLimit;
        mStopLoadLimit = stopLoadLimit;
    }

    public void restore() {
        mAllowLoad = true;
        firstLoad = true;
    }

    public void lock() {
        mAllowLoad = false;
        firstLoad = false;
    }

    public void unlock() {
        mAllowLoad = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void beginloadImage(final Integer position,final OnImageLoadListener listener,final long song_id,final long album_id) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                if (!mAllowLoad) {
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }

                if (mAllowLoad && firstLoad) {
                    /*loadImage(mImageUrl, mt, mListener, author);*/
                    loadImage(position, listener, song_id, album_id);
                }

                if (mAllowLoad && position <= mStopLoadLimit && position >= mStartLoadLimit) {
                    //loadImage(mImageUrl, mt, mListener, author);
                    loadImage(position, listener, song_id, album_id);
                }
            }

        }).start();
    }

    private void loadImage(final Integer position, final OnImageLoadListener mListener,final long song_id,final long album_id) {
        String key = song_id+"-"+album_id;

        if (imageCache.containsKey(key)) {
            System.out.println("drawable");
            SoftReference<Bitmap> softReference = imageCache.get(key);
            final Bitmap bitmap = softReference.get();
            if (bitmap != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mAllowLoad) {
                            mListener.onImageLoad(position, bitmap);
                        }
                    }
                });
                return;
            }
        }
        try {
            final Bitmap d = loadImageFromUrl(song_id,album_id);
            if (d != null) {
                imageCache.put(key, new SoftReference<Bitmap>(d));
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (mAllowLoad) {
                        mListener.onImageLoad(position, d);
                    }
                }
            });
        } catch (Exception e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onError(position);
                }
            });
            e.printStackTrace();
        }
    }

    private Bitmap loadImageFromUrl(long song_id, long album_id) {
        return MusicUtils.getArtwork(mContext, song_id, album_id, false);
    }
}
