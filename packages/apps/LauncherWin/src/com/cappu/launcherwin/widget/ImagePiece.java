package com.cappu.launcherwin.widget;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.WindowManager;
import com.cappu.launcherwin.R;

public class ImagePiece {
	/**
	 * 切成块的图
	 */
	public Bitmap[][] cellBitmaps;
	/**
	 * 切成块的图附加半透效果图
	 */
	public Bitmap[][] cellBitmapsBg;
	public Bitmap hotseatBmp;
	public Bitmap statusBarBmp;
	public Bitmap indicatorBmp;
	public Bitmap widgetBmp;
	public Bitmap widgetBmpBg;
	public Bitmap workspaceBmp;
	private Context mContext;
	private int width;
	private int height;
	int cellx=3;
	int celly=3;
	public ImagePiece(Context context){
		mContext=context;
	}
	public void initPiece(int res){
		cellBitmaps=new Bitmap[cellx][celly];
		cellBitmapsBg=new Bitmap[cellx][celly];
		Bitmap mScreenBitmap=getScreenBmp(res);
		statusBarBmp=Bitmap.createBitmap(mScreenBitmap, 0, 0,  
				width, getStatusBarHeight()); 
		hotseatBmp=Bitmap.createBitmap(mScreenBitmap, 0, height-dip2px(mContext,100),  
				width, dip2px(mContext,100));
		indicatorBmp=Bitmap.createBitmap(mScreenBitmap, 0, height-dip2px(mContext,112),  
				width, dip2px(mContext,12));
		workspaceBmp=Bitmap.createBitmap(mScreenBitmap, 0, getStatusBarHeight(),  
				width,height-getStatusBarHeight()-dip2px(mContext,112));
		setPiece(workspaceBmp);
	}
	public void initBitmapPiece(Drawable drawable){
		cellBitmaps=new Bitmap[cellx][celly];
		cellBitmapsBg=new Bitmap[cellx][celly];
		Bitmap mScreenBitmap=getScreenBmp(drawable);
		statusBarBmp=Bitmap.createBitmap(mScreenBitmap, 0, 0,  
				width, getStatusBarHeight()); 
		hotseatBmp=Bitmap.createBitmap(mScreenBitmap, 0, height-dip2px(mContext,100),  
				width, dip2px(mContext,100));
		indicatorBmp=Bitmap.createBitmap(mScreenBitmap, 0, height-dip2px(mContext,112),  
				width, dip2px(mContext,12));
		workspaceBmp=Bitmap.createBitmap(mScreenBitmap, 0, getStatusBarHeight(),  
				width,height-getStatusBarHeight()-dip2px(mContext,112));
		setPiece(workspaceBmp);
	}
	private void setPiece(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int pieceWidth = width / 3;
		int pieceHeight = height / 3;
		widgetBmp=Bitmap.createBitmap(bitmap, 0, 0,
				pieceWidth*3, pieceHeight*2);
		widgetBmpBg=createIconBgBitmap(widgetBmp);
		for (int i = 0; i < cellx; i++) {
			for (int j = 0; j < celly; j++) {
				int xValue = i * pieceWidth;
				int yValue = j * pieceHeight;
				cellBitmaps[i][j] = Bitmap.createBitmap(bitmap, xValue, yValue,
						pieceWidth, pieceHeight);
				cellBitmapsBg[i][j]=createIconBgBitmap(cellBitmaps[i][j]);
				
			}
		}
	}
	/**
	 * 创建切成块的图附加半透效果图
	 * @param bitmap
	 * @return
	 */
	private Bitmap createIconBgBitmap(Bitmap bitmap) {
		Bitmap mBitmap=bitmap.copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas=new Canvas(mBitmap);
		Drawable drawable = mContext.getResources().getDrawable(R.drawable.cell_nine_grids_middle_pressed);
		drawable.setBounds(0, 0,bitmap.getWidth(),bitmap.getHeight());
		drawable.draw(canvas);
		return mBitmap;
}
	private Bitmap getScreenBmp(Drawable drawable) {
		WindowManager wm = (WindowManager) mContext.getSystemService(
				Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
		Log.v("jeff_tag", "width="+width+",height="+height);
		Bitmap mScreenBitmap = Bitmap.createBitmap(width,
				height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mScreenBitmap);
		drawable.setBounds(0, 0, width,height);
		drawable.draw(canvas);
		return mScreenBitmap;
	}
	private Bitmap getScreenBmp(int res) {
		WindowManager wm = (WindowManager) mContext.getSystemService(
				Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
		Log.v("jeff_tag", "width="+width+",height="+height);
		Bitmap mScreenBitmap = Bitmap.createBitmap(width,
				height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mScreenBitmap);
		Drawable bg = mContext.getResources().getDrawable(res);
		bg.setBounds(0, 0, width,height);
		bg.draw(canvas);
		return mScreenBitmap;
	}
	private int dip2px(Context context, double d) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (d * scale + 0.5f);
    }
	private int getStatusBarHeight() {
		int result = 0;
		int resourceId = mContext.getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			result = mContext.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
}