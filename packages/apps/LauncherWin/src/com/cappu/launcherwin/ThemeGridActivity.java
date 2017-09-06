package com.cappu.launcherwin;

import java.io.File;
import java.util.ArrayList;

import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.widget.LauncherLog;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
public class ThemeGridActivity extends BasicActivity {
	private String TAG="ThemeGridActivity";
	private final static int BTN_CAMERA=1001;
	private final static int ACTION_SET_WALLPAPER=1002;
	private static final String CURRENT_THEME_BG = "current_theme_bg";
	private GridView gridView;
	private int[] imageThemeId = new int[] {
			R.drawable.bg_launcher_nine_default, R.drawable.bg_launcher_nine_0,
			R.drawable.bg_launcher_nine_1, R.drawable.bg_launcher_nine_2,
			R.drawable.bg_launcher_nine_3, R.drawable.bg_launcher_nine_4,
			R.drawable.bg_launcher_nine_5, R.drawable.bg_launcher_nine_6,
			R.drawable.bg_launcher_nine_7, R.drawable.bg_launcher_nine_8,
			R.drawable.bg_launcher_nine_9 ,R.drawable.bg_launcher_nine_10,
			R.drawable.bg_launcher_nine_11,R.drawable.bg_launcher_nine_12 };
	private ArrayList<Bitmap> bitmaps=new ArrayList<Bitmap>();
	private int currentTheme;
	private Button btnCamera;
	private Button btnGallery;
	private GridViewAdapter gridViewAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_theme_grid);
		gridView = (GridView) findViewById(R.id.gridView);
		gridViewAdapter = new GridViewAdapter();
		gridView.setAdapter(gridViewAdapter);
		// 为GridView设定监听器
		gridView.setOnItemClickListener(new gridViewListener());
		
		btnCamera=(Button)findViewById(R.id.btn_camera);
		btnCamera.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
				startActivityForResult(intent, BTN_CAMERA);
			}
		});
		btnGallery=(Button)findViewById(R.id.btn_gallery);
		btnGallery.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startGallery();
			}
		});
		for (int i = 0; i < imageThemeId.length; i++) {
			bitmaps.add(BitmapFactory.decodeResource(getResources(),
					imageThemeId[i]));
		}
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		currentTheme=Settings.Global.getInt(
				getContentResolver(), CURRENT_THEME_BG,
				0);
		initBitmaps();
	}
	private void initBitmaps(){
		if (currentTheme == -1) {
			WallpaperManager wallpaperManager = WallpaperManager
					.getInstance(this);
			// 获取当前壁纸
			Drawable wallpaperDrawable = wallpaperManager.getDrawable();
			// 将Drawable,转成Bitmap
			Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();
			ThemeManager.getInstance().setCurWallpaperBmp(bm);
			if(bitmaps.size()!=imageThemeId.length){
				bitmaps.remove(bitmaps.size()-1);
			}
			bitmaps.add(bm);
		}
		gridViewAdapter.notifyDataSetChanged();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		LauncherLog.v(TAG, "onActivityResult,jeff requestCode="+requestCode+",resultCode="+resultCode);
		if (requestCode == BTN_CAMERA && resultCode == RESULT_OK) {
			File mPictureFile=new File("/sdcard/DCIM/Camera/"+Settings.Global.getString(getContentResolver(),"jeff_current_picture_name"));
				startCamera(mPictureFile);
		}
	}
	private void startCamera(File file){
		Intent intent=new Intent("android.service.wallpaper.CROP_AND_SET_WALLPAPER");  
		Uri mUri=Uri.fromFile(file);
		intent.setDataAndType(mUri, "image/*");
		startActivity(intent);
	}
	private void startGallery(){
		Intent intent=new Intent(Intent.ACTION_SET_WALLPAPER);     
		intent.setComponent(new ComponentName(
                    "com.android.gallery3d", "com.android.gallery3d.app.Wallpaper"));
		startActivityForResult(intent,ACTION_SET_WALLPAPER);
	}
	class gridViewListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			LauncherLog.v(TAG, "onItemClick,jeff arg2="+arg2);
			if(arg2==currentTheme ||(currentTheme==-1&&arg2==bitmaps.size()-1)){
				return ;
			}
			Intent intent=new Intent(ThemeGridActivity.this, ThemePreviewActivity.class);
			if(arg2<imageThemeId.length){
				intent.putExtra("setWallpaper", imageThemeId[arg2]);
				intent.putExtra("currentPosition", arg2);
			}else{
				intent.putExtra("setWallpaper", -1);
				intent.putExtra("currentPosition", -1);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private class GridViewAdapter extends BaseAdapter {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageview; // 声明ImageView的对象
			ImageView imgCurrentTheme;
			if (convertView == null) {
				LayoutInflater mInflater=LayoutInflater.from(ThemeGridActivity.this);
				convertView=mInflater.inflate(R.layout.theme_grid_item, null);
				imageview=(ImageView)convertView.findViewById(R.id.img_theme);
				convertView.setTag(imageview);
			} else {
				imageview = (ImageView) convertView.getTag();
			}
			imgCurrentTheme=(ImageView)convertView.findViewById(R.id.current_theme);
			if(position==currentTheme || (bitmaps.size()!=imageThemeId.length&&currentTheme==-1&& position==bitmaps.size()-1)){
				imgCurrentTheme.setVisibility(View.VISIBLE);
			}else{
				imgCurrentTheme.setVisibility(View.GONE);
			}
			imageview.setBackground(new BitmapDrawable(bitmaps.get(position))); // 为ImageView设置要显示的图片
			return convertView;
		}

		/*
		 * 功能：获得当前选项的ID
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}

		/*
		 * 功能：获得当前选项
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			return position;
		}

		/*
		 * 获得数量
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return bitmaps.size();
		}
	}
}
