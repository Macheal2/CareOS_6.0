package com.cappu.launcherwin.kookview;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.net.nntp.Threadable;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import com.cappu.launcherwin.BubbleView.OnChildViewClick;
import com.cappu.launcherwin.Launcher;
import com.cappu.launcherwin.Launcher.LauncherType;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.install.APKInstallTools;
import com.cappu.launcherwin.netinfo.BaseCard;
import com.cappu.launcherwin.netinfo.widget.BaseViewPager;
import com.cappu.launcherwin.netinfo.widget.NetDateDao;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.widget.RoundImageView;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.Color;

//将爱分享到图片在相册中轮播  added by wangyang 2016.07.11
public class AlbumsRelativeLayout extends RelativeLayout implements OnChildViewClick {
    private String TAG = "AlbumsRelativeLayout";
    private ViewPagerAdapter mViewPagerAdapter;
    private Context mContext;
    private int mPosition;
    private Bitmap bmp;
    private Bitmap mHanderBitmap;
    private List<String> mIShareDaoList = new ArrayList<String>();
    private List<String> mCheckIShare = new ArrayList<String>();
    private List<String> mLastIShare = new ArrayList<String>();//判断当前集合是否为空 modify by wangyang 2016.9.14 
    
    private long threadCurrent;
    private UpdateThread mUpdateThread = null;
    private FaceThread mFaceThread = null;
    private faceFileCache mFaceFileCache;
    private ExecutorService mExecutors;
    private static int POOL_SIZE = 1;
    private static String WANGFACE = "wangFace";

    public AlbumsRelativeLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        Log.i(TAG, "AlbumsRelativeLayout jeff 111111111");
        init(context);
    }

    public AlbumsRelativeLayout(Context context) {
        super(context);
        Log.i(TAG, "AlbumsRelativeLayout jeff 2222222222");
        init(context);
    }

    public AlbumsRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "AlbumsRelativeLayout jeff 333333333");
        init(context);
    }
    @Override
    protected void onDetachedFromWindow() {
    	Log.v(TAG, "onDetachedFromWindow,jeff ");
    	if(ThemeManager.getInstance().getCurrentThemeType()!=ThemeManager.THEME_NINE_GRIDS){
    		handler.removeMessages(0);
    	}
		if (mIShareDaoList != null) {
			mIShareDaoList.clear();
		}
		if (mCheckIShare != null) {
			mCheckIShare.clear();
		}
		if (mIShareFaceList != null) {
			mIShareFaceList.clear();
		}
		if (mLastIShare != null) {
			mLastIShare.clear();
		}
		if (mUpdateThread != null) {
			LauncherLog.v(TAG, "onDestory jeff,mUpdateThread != null ");
			mUpdateThread.interrupt();
			mUpdateThread = null;
		}
		if (mFaceThread != null) {
			LauncherLog.v(TAG, "onDestory jeff,mFaceThread != null ");
			mFaceThread.interrupt();
			mFaceThread = null;
		}
    }
    private void init(Context context) {
        this.mContext = context;
        if(context instanceof Launcher){
            this.mLauncher = (Launcher) context;
        }
        mFaceFileCache = new faceFileCache();
        mExecutors = Executors.newFixedThreadPool(POOL_SIZE);
        
        //add by wangyang 2016.9.19 start
		if (ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE) {
			Typeface mTypeface = Typeface.createFromAsset(getContext()
					.getAssets(), "fonts/minilishu.ttf");
			RelativeLayout.LayoutParams layoutParamsBackground = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
			ImageView imageViewBackground = new ImageView(mContext);
			imageViewBackground
					.setBackgroundResource(R.drawable.theme_china_ishare_back);
			addView(imageViewBackground, layoutParamsBackground);

			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
			ImageView imageView = new ImageView(mContext);
			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try {
						if (APKInstallTools.checkApkInstall(mContext,
								"com.cappu.ishare","com.cappu.ishare.ui.activitys.SplashActivity")) {
							if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
								mLauncher.getSpeechTools().startSpeech(
										mContext.getString(R.string.cloud_album),
										mLauncher.getSpeechStatus());
							}else{
								mLauncher.getSpeechTools().startSpeech(
										mContext.getString(R.string.aifenxiang),
										mLauncher.getSpeechStatus());
							}
						}
					} catch (Exception e) {
						Log.i("zazaaaaaaaaaaaaa",
								"e             e = " + e.toString());
					}
					openApp("com.cappu.ishare","com.cappu.ishare.ui.activitys.SplashActivity");
				}
			});
			imageView
					.setBackgroundResource(R.drawable.chinses_style_ishare_back);
			addView(imageView, layoutParams);
			RelativeLayout.LayoutParams layoutParamsViewPager = new RelativeLayout.LayoutParams(
					567, 233);
			layoutParamsViewPager.addRule(RelativeLayout.CENTER_IN_PARENT);
			mBaseViewPager = new BaseViewPager(mContext);
			addView(mBaseViewPager, layoutParamsViewPager);
		} else {
			LayoutParams layoutParams = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			mBaseViewPager = new BaseViewPager(mContext);
			addView(mBaseViewPager, layoutParams);
		}
      //add by wangyang 2016.9.19 end
        
        if (APKInstallTools.checkApkInstall(mContext, "com.cappu.ishare","com.cappu.ishare.ui.activitys.SplashActivity")) {
            updateNetLook(1);
        } else{
            updateNetLook(0);
        }
        handler.sendEmptyMessage(0);
        Log.i(TAG, "aaxxxxxxxxxxxx         "+mIShareDaoList.size());
    }
    //检查本地文件中的图片是否存在
    public void checkData(){
        Uri uri = Uri.parse("content://" +"com.cappu.ishare.authority" + "/ShareBean"+"?authorities_pkg="+"com.android.magcomm");
        String[] projection = {
                "createtime",// 发布时间 0
                "summary",// 描述 1
                "uid",// 用户ID 2
                "duration",// 录音文件时长 3
                "phone",// 手机号 4
                "voice",// 录音文件 5
                "name",// 昵称 6
                "gid",// 主题ID 7
                "image",// 主题图片 8
                //"imagername",//主题图片文件名 9
                "imageuri",//主题图片文件链接 10
                "avatar",// 主题头像 11
                //"isFavorite",//不为0则表示已收藏 11
                "size",//音频文件大小 12
                "avatarname",//头像文件名 13
                "imagername",//头像文件名 15
                "refreshtime",//刷新时间,当服务器请求来一次数据时候就更新当前时间
                };
        Cursor cursor=null;
		try {
			cursor = mContext.getContentResolver().query(uri, projection, null,
					null, "_id desc limit 5");
			if (cursor == null || cursor.getCount() == 0) {
				mIShareDaoList.clear();
				setDate();
			} else {
				mCheckIShare.clear();
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
						.moveToNext()) {
					String stringUri = cursor.getString(9);
					if (stringUri != null) {
						String imageuri = stringUri.substring(7,
								stringUri.length());
						File file=new File(imageuri);
						if (file.exists()) {
							mCheckIShare.add(imageuri);
						} else {
							mCheckIShare.clear();
							break;
						}
					}
				}
				if (mIShareDaoList.containsAll(mCheckIShare)
						&& mIShareDaoList.size() == mCheckIShare.size()) {

				} else {
					Log.i(TAG,
							"         mIShareDaoList != mCheckIShare         ");
					mIShareDaoList.clear();
					if (mCheckIShare.size() == 0 || mCheckIShare == null) {
						mIShareDaoList.clear();
						setDate();
					} else {
						for (int a = 0; a < mCheckIShare.size(); a++) {
							mIShareDaoList.add(mCheckIShare.get(a));
						}
						setDate();
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG,"jeff Exception");
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
    }
    
	private  Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			LauncherLog.v(TAG, " handler jeff msg=" + msg);
			if (mLauncher.getLauncherType() == LauncherType.onResume) {
				if (mIShareDaoList != null && mIShareDaoList.size() > 0) {
					Log.i(TAG, "start mCurrent " + mCurrent + "imageURL= "
							+ mIShareDaoList.get(mCurrent));
					File file=new File(mIShareDaoList.get(mCurrent));
					if(!file.exists()){
						checkData();
					}
					int max = mIShareDaoList.size() - 1;
					if (mCurrent == max) {
						mBaseViewPager.setCurrentItem(mCurrent, false);
						mCurrent = 0;
					} else {
						mBaseViewPager.setCurrentItem(mCurrent, false);
						mCurrent++;
					}
					this.sendEmptyMessageDelayed(0, 5000);
				} else {
					// 当显示的是默认的图片时给爱分享发送一个广播 start
					Intent intent = new Intent();
					intent.setAction("com.android.magcomm.ishare.reqpicture");
					mContext.sendBroadcast(intent);
					checkData();// 每次循环检查是否图片集合更改了
					// 当显示的是默认的图片时给爱分享发送一个广播 end
					Log.i(TAG, "handler是默认的图片,发送广播给爱分享请求图片   ");
					mCurrent=0;
					mBaseViewPager.setCurrentItem(mCurrent, false);
					this.sendEmptyMessageDelayed(0, 5000);
				}

			} else {
				Log.i(TAG, "handleMessage else mIShareDaoList size="
						+ mIShareDaoList.size());
				if (mViewPagerAdapter == null) {
					mViewPagerAdapter = new ViewPagerAdapter(mIShareDaoList);
					setDate();
				}
				this.sendEmptyMessageDelayed(0, 5000);
			}
		}
	};
    

	public void setDate() {
		if (mFaceThread == null) {
			Log.i(WANGFACE, "faceThread == null    ");
			mFaceThread = new FaceThread();
		}
		mExecutors.execute(mFaceThread);
		try {
			if (mViewPagerAdapter == null) {
				mViewPagerAdapter = new ViewPagerAdapter(mIShareDaoList);
			}
			Log.i(TAG, "mViewPagerAdapter.mViewPagerList != list  list = "
					+ mIShareDaoList.size());
			if (mBaseViewPager.getAdapter() == null) {
				mBaseViewPager.setAdapter(mViewPagerAdapter);
				mBaseViewPager.setOffscreenPageLimit(3);
			}
			mViewPagerAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			Log.i(TAG, "Exception = " + e.toString());
		}
		Log.i(TAG, "updateNetLook updateNetLook");
	}

    private int mCurrent = 0;
    private BaseViewPager mBaseViewPager;
    private Launcher mLauncher;

    private class ViewPagerAdapter extends PagerAdapter {
        private List<String> mViewPagerList;

         public ViewPagerAdapter(List<String> list) {
         this.mViewPagerList = list;
         }
         
         public void setData(List<String> iShareList){
             mViewPagerList = iShareList;
         }

        @Override
        public int getCount() {
            if(mViewPagerList.size() == 0 || mViewPagerList == null){
                return 1;
            }else{
                return mViewPagerList.size();
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
        
        @SuppressLint("NewApi")
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            mPosition = position;
            ViewHolder viewHolder = createAlbums();
            LauncherLog.v(TAG, "instantiateItem,jeff mViewPagerList.size()="+mViewPagerList.size());
            if(mViewPagerList.size() == 0 || mViewPagerList == null){
                LauncherLog.v(TAG, "instantiateItem jeff ,mViewPagerList.size() == 0");
                viewHolder.mIShareImage.setImagePath("abc");
                container.addView(viewHolder.mRelativeLayout);
                return viewHolder.mRelativeLayout;
            } else{
            	LauncherLog.v(TAG, "instantiateItem jeff ,mViewPagerList.size()>0");
                String netDateDao = mViewPagerList.get(position);
                LauncherLog.v(TAG, "instantiateItem jeff ,netDateDao.imageuri "+netDateDao+" position="+position +"mCurrent="+mCurrent);
                viewHolder.mIShareImage.setImagePath(netDateDao);
                container.addView(viewHolder.mRelativeLayout);
                return viewHolder.mRelativeLayout;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        	LauncherLog.v(TAG, "destroyItem jeff,  position = " + position);
            container.removeView((View) object);
        }
        
        @Override
        public int getItemPosition(Object object)   {
              return POSITION_NONE;
        } 
    }
  @Override
  public void onClick(Context c) {
	  
  }
    private ViewHolder createAlbums() {
        RelativeLayout relativeLayout = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        IShareImage mImageView = new IShareImage(mContext);
        relativeLayout.addView(mImageView, params);
        mImageView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                try{
                    if (APKInstallTools.checkApkInstall(mContext, "com.cappu.ishare","com.cappu.ishare.ui.activitys.SplashActivity")) {
                    	if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
							mLauncher.getSpeechTools().startSpeech(
									mContext.getString(R.string.cloud_album),
									mLauncher.getSpeechStatus());
						}else{
							mLauncher.getSpeechTools().startSpeech(
									mContext.getString(R.string.aifenxiang),
									mLauncher.getSpeechStatus());
						}
                    }
                } catch(Exception e){
                    Log.i("zazaaaaaaaaaaaaa", "e = " + e.toString());
                }
                openApp("com.cappu.ishare","com.cappu.ishare.ui.activitys.SplashActivity");
            }
        });
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.mRelativeLayout = relativeLayout;
        viewHolder.mIShareImage = mImageView;
        return viewHolder;
    }

    private class ViewHolder {
        public RelativeLayout mRelativeLayout;
        public IShareImage mIShareImage;
    }
    
    private Handler mHandler = new Handler(){
        
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 1:
                setDate();
            }
        }
    };
    
    private class UpdateThread extends Thread{

        private int isInstallIShare;
        public UpdateThread(int isInstallIShare){
            this.isInstallIShare = isInstallIShare;
        }
        @Override
        public void run() {
            LauncherLog.v(TAG, "UpdateThread jeff,isInstallIShare = " + isInstallIShare);
            if(mIShareDaoList.size()>0 && mIShareDaoList != null){//判断当前集合是否为空 modify by wangyang
                mLastIShare.clear();
                for(int a = 0; a<mIShareDaoList.size(); a++){
                    mLastIShare.add(mIShareDaoList.get(a));
                }
            }
            mIShareDaoList.clear();
            mCurrent = 0;
            Uri uri = Uri.parse("content://" +"com.cappu.ishare.authority" + "/ShareBean"+"?authorities_pkg="+"com.cappu.launcherwin");
            String[] projection = {
                    "createtime",// 发布时间 0
                    "summary",// 描述 1
                    "uid",// 用户ID 2
                    "duration",// 录音文件时长 3
                    "phone",// 手机号 4
                    "voice",// 录音文件 5
                    "name",// 昵称 6
                    "gid",// 主题ID 7
                    "image",// 主题图片 8
                    //"imagername",//主题图片文件名 9
                    "imageuri",//主题图片文件链接 10
                    "avatar",// 主题头像 11
                    //"isFavorite",//不为0则表示已收藏 11
                    "size",//音频文件大小 12
                    "avatarname",//头像文件名 13
                    "imagername",//头像文件名 15
                    "refreshtime",//刷新时间,当服务器请求来一次数据时候就更新当前时间
                    };
            Cursor cursor = null;
			try {
				if (isInstallIShare == 1) {
					cursor = mContext.getContentResolver().query(uri,
							projection, null, null, null);// 由爱分享
				}
				if (cursor != null) {
					LauncherLog.v(TAG,"UpdateThread jeff,cursor = "+ cursor.getCount());
					for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
							.moveToNext()) {
						try {
							String stringUri = cursor.getString(9);
							LauncherLog.v(TAG, "UpdateThread jeff,stringUri = "+ stringUri);
							if (stringUri != null) {
								String imageuri = stringUri.substring(7,
										stringUri.length());
								LauncherLog.v(TAG, "UpdateThread jeff,imageuri = "+ imageuri);
								File decodeFile=new File(imageuri);
								LauncherLog.v(TAG, "UpdateThread jeff,decodeFile = "+ (decodeFile.exists()));
								if (!decodeFile.exists()) {
									break;
								}
								mIShareDaoList.add(imageuri);
							} else {
							}
						} catch (Exception e) {
							Log.i(TAG, "Exception " + e.toString());
						}
					}
				}
				if (mIShareDaoList.size() == 0 || mIShareDaoList == null) {// 判断当前集合是否为空
					for (int c = 0; c < mLastIShare.size(); c++) {
						mIShareDaoList.add(mLastIShare.get(c));
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "jeff Exception");
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
            Message message = new Message();
            message.what = 1;
            mHandler.sendMessage(message);
        }
    }
    
    public void updateNetLook(int isInstallIShare) {
        if((System.currentTimeMillis() - threadCurrent) > 200){//爱分享给我的通知过于集中，1秒钟能给我十几个通知，当两个通知之间大于200ms 才会处理 modify by wangyang 2016.10.31
            threadCurrent = System.currentTimeMillis();
            if(mUpdateThread != null){
            	mUpdateThread.interrupt();
    			mUpdateThread = null;
            }
            LauncherLog.v(TAG, "updateNetLook jeff,mUpdateThread == null ");
            mUpdateThread = new UpdateThread(isInstallIShare);
            mUpdateThread.start();
        }
        
    }
    private class IShareImage extends ImageView {

        private String mImagePath = null;
        
        private Matrix mMatrix;
        private Paint mBitmapPaint;
        private int mBorderRadius;
        private static final int BODER_RADIUS_DEFAULT = 5;
        
        private RectF mRoundRect;
        private BitmapShader mBitmapShader;

        public IShareImage(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
            mBorderRadius = a.getDimensionPixelSize(R.styleable.RoundImageView_borderRadius,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BODER_RADIUS_DEFAULT, getResources().getDisplayMetrics()));// 默认为10dp
            
            int viewType = a.getInt(R.styleable.RoundImageView_type, 0);
            a.recycle();
            init();
        }

        public IShareImage(Context context) {
            this(context,null);
            init();
        }

        private void init() {
            mMatrix = new Matrix();
            mBitmapPaint = new Paint();
            mBitmapPaint.setAntiAlias(true);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);
            //当没有路径时设置为默认的图片    
            if("abc".equals(mImagePath)){
                LauncherLog.v(TAG, "onDraw jeff,abc");
                if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE){
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ishare_default_china);
                } else{
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ishare_default);
                }
            } else{
            	LauncherLog.v(TAG, "onDraw jeff,图片地址  mImagePath="+mImagePath);
                bmp = mFaceFileCache.getBitmapCache(mImagePath);
                if(bmp == null){
                    bmp = BitmapFactory.decodeFile(mImagePath);
                    
                    if(bmp != null){
                        if(bmp.getHeight() > getHeight()){//当图片的高大于widget后做裁剪 modify by wangyang 2016.9.7
                            if(bmp.getHeight() > getHeight()*2){//当图片的高大于2倍widget后做裁剪
                                bmp = Bitmap.createBitmap(bmp, 0, bmp.getHeight()/3, bmp.getWidth(), bmp.getHeight()/3*2);
                                Log.i(TAG, "h = "+bmp.getHeight()/3*2 +"w = "+bmp.getWidth());
                            } else{
                                bmp = Bitmap.createBitmap(bmp, 0, bmp.getHeight()/5, bmp.getWidth(), bmp.getHeight()/5*4);
                                Log.i(TAG, "h = "+bmp.getHeight()/5*4 +"w = "+bmp.getWidth());
                            }
                        }
                    }
                }
                
                Log.i(WANGFACE, "bmp = "+(bmp == null));
            }
            if(bmp == null){
                if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE){
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ishare_default_china);
                } else{
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ishare_default);
                }
            }
            if(bmp!=null){
                // 将bmp作为着色器，就是在指定区域内绘制bmp
                   mBitmapShader = new BitmapShader(bmp, TileMode.CLAMP, TileMode.CLAMP);
                   float scale = 1.0f;
                   if (!(bmp.getWidth() == getWidth() && bmp.getHeight() == getHeight())) {
                       // 如果图片的宽或者高与view的宽高不匹配，计算出需要缩放的比例；缩放后的图片的宽高，一定要大于我们view的宽高；所以我们这里取大值；
                        scale = Math.max(getWidth() * 1.0f / bmp.getWidth(), getHeight() * 1.0f / bmp.getHeight());
                   }
                   mMatrix.setScale(scale, scale);
                   // 设置变换矩阵
                   mBitmapShader.setLocalMatrix(mMatrix);
                   // 设置shader
                   mBitmapPaint.setShader(mBitmapShader);
               }
            if(mRoundRect == null){
                mRoundRect = new RectF(0, 0, getWidth(), getHeight());
            }
            if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE){
                canvas.drawRoundRect(mRoundRect, 0, 0, mBitmapPaint);
            } else{
                canvas.drawRoundRect(mRoundRect, mBorderRadius, mBorderRadius, mBitmapPaint);
            }
        }
        
        private Bitmap createRoundConerImage(Bitmap source){
            Bitmap target;
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
                target = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(target);
            RectF rect = new RectF(0, 0, getWidth()+8, getHeight()+4);
            canvas.drawRoundRect(rect, 5, 5, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(source, 0, 0, paint);
            return target;
        }
        
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // TODO Auto-generated method stub
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        
        private void setUpShader() {
            // TODO Auto-generated method stub
            
            Log.i(TAG, "bmp      ="+(bmp == null) +bmp);
            if(bmp == null){
                bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ishare_default);
            }
            mBitmapShader = new BitmapShader(bmp, TileMode.CLAMP, TileMode.CLAMP);
            float scale = 1.0f;
            mMatrix.setScale(scale, scale);
            // 设置变换矩阵
            mBitmapShader.setLocalMatrix(mMatrix);
            // 设置shader
            mBitmapPaint.setShader(mBitmapShader);
        }

        public void setImagePath(String imagePath) {
            Log.i(TAG, "setImagePath          "+imagePath);
            if(!imagePath.equals(mImagePath)){
                this.mImagePath = imagePath;
                requestLayout();
            }
        }
    }
    
    /** 通过包名去启动一个应用*/
	private void openApp(String packageName, String className) {
		// TODO 把应用杀掉然后再启动，保证进入的是第一个页面
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ComponentName cn = new ComponentName(packageName, className);
		intent.setComponent(cn);
		mContext.getApplicationContext().startActivity(intent);
	}
    
  //人脸识别 add by wangyang 2016.10.21 start
    private int mImageFaceWidth, mImageFaceHeight;
    private int mMumberOfFace = 1;
    private FaceDetector.Face[] myFace;
    private FaceDetector myFaceDetect;
    private BitmapFactory.Options mBitmapFactoryOptionsbfo = null;
    private List<String> mIShareFaceList = new ArrayList<String>();//1.在FaceThread里遍历的时候mIShareDaoList的集合可能会改变，而引起脚标越界或得到的资源不是我们想要的, 2.优化内存 人脸识别比较耗时，这里尽量少识别人脸 modify by wangyang 2016.10.31
    
	private class FaceThread extends Thread {

		@Override
		public void run() {
			LauncherLog.v(TAG, "FaceThread,jeff mIShareFaceList="
					+ mIShareFaceList + ",mIShareDaoList=" + mIShareDaoList);
			try {
				if (mIShareFaceList.size() == mIShareDaoList.size()
						&& mIShareFaceList.containsAll(mIShareDaoList)) {
					// 两个集合相等
				} else {// 否则不相等
					if (mIShareFaceList != null) {
						mIShareFaceList.clear();
					}
					for (int face = 0; face < mIShareDaoList.size(); face++) {
						mIShareFaceList.add(mIShareDaoList.get(face));
					}

					if (mBitmapFactoryOptionsbfo == null) {
						mBitmapFactoryOptionsbfo = new BitmapFactory.Options();
						mBitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;
					}

					for (int a = 0; a < mIShareFaceList.size(); a++) {
						Bitmap mFaceBitmap = BitmapFactory.decodeFile(
								mIShareFaceList.get(a),
								mBitmapFactoryOptionsbfo);
						Log.i(WANGFACE, "run  run" + "       mFaceBitmap = "
								+ (mFaceBitmap == null));
						if (mFaceBitmap != null) {
							mImageFaceWidth = mFaceBitmap.getWidth();
							mImageFaceHeight = mFaceBitmap.getHeight();
							myFace = new FaceDetector.Face[mMumberOfFace];
							myFaceDetect = new FaceDetector(mImageFaceWidth,
									mImageFaceHeight, mMumberOfFace);
							int getNumberFace = myFaceDetect.findFaces(
									mFaceBitmap, myFace);
							if (getNumberFace != 0) {
								Face f = myFace[0];
								PointF midPoint = new PointF();
								if (f != null) {
									float dis = f.eyesDistance();
									f.getMidPoint(midPoint);
									int dd = (int) (dis);
									int eyeX = (int) midPoint.x;
									int eyeY = (int) midPoint.y;
									int width;
									int height;
									if (ThemeManager.getInstance()
											.getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE) {
										width = 567;
										height = 233;
									} else {
										width = 720;
										height = 360;
									}

									Log.i(WANGFACE, "       width = " + width
											+ "   height = " + height);
									int faceWidth = mFaceBitmap.getWidth();
									int faceHeight = mFaceBitmap.getHeight();

									// 图片尺寸的处理 wangyang facebegin
									if (faceWidth >= width
											&& faceHeight >= height) {

										if (eyeX <= width / 2
												&& eyeY <= height / 2) {// 1
											Log.i(WANGFACE, "   111111    ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, 0, 0, width,
													height);
										} else if (width / 2 < eyeX
												&& eyeX < (faceWidth - width / 2)
												&& eyeY < height / 2) {// 2
											Log.i(WANGFACE, "    222222222   ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, eyeX - width
															/ 2, 0, width,
													height);
										} else if (eyeX >= (faceWidth - width / 2)
												&& eyeY <= height / 2) {// 3
											Log.i(WANGFACE, "    333333   ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, faceWidth
															- width, 0, width,
													height);
										} else if (eyeX <= width / 2
												&& eyeY >= height / 2
												&& eyeY <= (faceHeight - height / 2)) {// 4
											Log.i(WANGFACE, "    44444444   ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, 0, eyeY
															- height / 2,
													width, height);
										} else if (eyeX >= width / 2
												&& eyeX <= (faceWidth - width / 2)
												&& eyeY >= height / 2
												&& eyeY <= (faceHeight - height / 2)) {// 5
											Log.i(WANGFACE, "      55555555 ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, eyeX - width
															/ 2, eyeY - height
															/ 2, width, height);
										} else if (eyeX >= (faceWidth - width / 2)
												&& eyeY >= height / 2
												&& eyeY <= (faceHeight - height / 2)) {// 6
											Log.i(WANGFACE, "    6666666666   ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, faceWidth
															- width, eyeY
															- height / 2,
													width, height);
										} else if (eyeX <= width / 2
												&& eyeY >= (faceHeight - height / 2)) {// 7
											Log.i(WANGFACE, "    77777777   ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, 0, faceHeight
															- height, width,
													height);
										} else if (eyeX >= width / 2
												&& eyeX < (faceWidth - width / 2)
												&& eyeY >= (faceHeight - height / 2)) {// 8
											Log.i(WANGFACE, "     888888  ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, eyeX - width
															/ 2, faceHeight
															- height, width,
													height);
										} else if (eyeX >= (faceWidth - width / 2)
												&& eyeY >= (faceHeight - height / 2)) {// 9
											Log.i(WANGFACE, "    9999   ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, faceWidth
															- width, faceHeight
															- height, width,
													height);
										} else {
											Log.i(WANGFACE,
													"    faceWidth >= width && faceHeight >= height   ");
										}

									} else if (faceWidth >= width
											&& faceHeight <= height) {

										if (eyeX <= width / 2) {// 1
											Log.i(WANGFACE, "   11    ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, 0, 0, width,
													faceHeight);
										} else if (eyeX >= width / 2
												&& eyeX <= (faceWidth - width / 2)) {// 2
											Log.i(WANGFACE, "   12    ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, eyeX - width
															/ 2, 0, width,
													faceHeight);
										} else if (eyeX >= (faceWidth - width / 2)) {// 3
											Log.i(WANGFACE, "    13   ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, eyeX - width,
													0, width, faceHeight);
										} else {
											Log.i(WANGFACE,
													"    faceWidth >= width && faceHeight <= height   ");
										}

									} else if (faceWidth <= width
											&& faceHeight >= height) {
										if (eyeY <= height / 2) {// 1
											Log.i(WANGFACE, "   14    ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, 0, 0,
													faceWidth, height);
										} else if (eyeY >= height / 2
												&& eyeY <= (faceHeight - height / 2)) {// 2
											Log.i(WANGFACE, "   15    ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, 0, eyeY
															- height / 2,
													faceWidth, height);
										} else if (eyeY >= (faceHeight - height / 2)) {// 3
											Log.i(WANGFACE, "    16   ");
											mFaceBitmap = Bitmap.createBitmap(
													mFaceBitmap, 0, faceHeight
															- height / 2,
													faceWidth, height);
										} else {
											Log.i(WANGFACE,
													"    faceWidth <= width && faceHeight >= height   ");
										}
									} else if (faceWidth <= width
											&& faceHeight <= height) {
										Log.i(WANGFACE,
												"    faceWidth <= width && faceHeight <= height   ");
									}
									// 图片尺寸的处理 wangyang faceend

									if (mFaceFileCache == null) {
										mFaceFileCache = new faceFileCache();
									}
									mFaceFileCache
											.setBitmapCache(
													mIShareFaceList.get(a),
													mFaceBitmap);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				LauncherLog.v(TAG, "FaceThread,jeff Exception");
			}
		}
	}
    
    //图片缓存的存取
    private class faceFileCache{
        
        private com.cappu.launcherwin.tools.DiskLruCache mDiskLruCache = null;
        private String mFildName;
        private Bitmap mBitmap;
        
        public faceFileCache(){
            init();
        }

        private void init() {
            // TODO Auto-generated method stub
            File cacheDir;
            try {
                    cacheDir = getDiskCacheDir(mContext, "difbitmap");
                    if (!cacheDir.exists()) {
                        cacheDir.mkdirs();
                    }
                Log.e(WANGFACE, " face init ");
                mDiskLruCache = com.cappu.launcherwin.tools.DiskLruCache.open(cacheDir, 1, 1, 5 * 1024 * 1024);//缓存5M的图片
            } catch (IOException e){
                Log.e(WANGFACE, "  init  face e = "+e.toString());
            }
        }
        
        private File getDiskCacheDir(Context context, String uniqueName) {
            String cachePath;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
                cachePath = context.getExternalCacheDir().getPath();
            } else {
                cachePath = context.getCacheDir().getPath();
            }
            return new File(cachePath + File.separator + uniqueName);
        }
        
        //存入图片
        private boolean setBitmapCache(String fileName, Bitmap bitmap){
            if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE){
                fileName = fileName + "2";
            } else{
                fileName = fileName + "0";
            }
            
            String key = hashKeyForDisk(fileName);
            Log.i(WANGFACE, "setkey = "+key);
            try {
                com.cappu.launcherwin.tools.DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                if (editor != null) {
                    OutputStream outputStream = editor.newOutputStream(0);
                    if (downloadUrlToStream(bitmap, outputStream)) {
                        Log.i(WANGFACE, "        存入图片成功   ");
                        editor.commit();
                    } else {
                        Log.i(WANGFACE, "        存放图片失败   ");
                        editor.abort();
                    }
                }
                mDiskLruCache.flush();
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
        
        private boolean downloadUrlToStream(Bitmap bitmap, OutputStream outputStream) {
          try {
              bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
              return true;
          } catch (final Exception e) {
              e.printStackTrace();
              return false;
          }
      }
        
        //取出图片
        private Bitmap getBitmapCache(String fildName){
            
            //主题区分 start
            if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE){
                fildName = fildName + "2";
            } else{
                fildName = fildName + "0";
            }
          //主题区分 end
            
            try {
                String key = hashKeyForDisk(fildName);
                Log.i(WANGFACE, "getkey = "+key);
                com.cappu.launcherwin.tools.DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
                if (snapShot != null) {
                    InputStream is = snapShot.getInputStream(0);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    Log.i(WANGFACE, "   取出图片 ");
                    return bitmap;
                } else{
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(WANGFACE, "   没有取出图片 ");
                return null;
            }
        }
        
        public String hashKeyForDisk(String key) {
            String cacheKey;
            try {
                final MessageDigest mDigest = MessageDigest.getInstance("MD5");
                mDigest.update(key.getBytes());
                cacheKey = bytesToHexString(mDigest.digest());
            } catch (NoSuchAlgorithmException e) {
                cacheKey = String.valueOf(key.hashCode());
            }
            return cacheKey;
        }
        
        private String bytesToHexString(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        }
    }
    //人脸识别 add by wangyang 2016.10.21 end
}
