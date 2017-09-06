package com.cappu.launcherwin.kookview.assembly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cappu.launcherwin.widget.LauncherLog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.util.TypedValue;
import com.cappu.launcherwin.R;
public class ClassRoomManager {
	private String TAG="ClassRoomManager";
	private static ClassRoomManager mClassRoomManager;
	private Context mContext;
	private String classRoomAddress="http://yun.cappu.cn/ketang/v1/course";
	private static final String CLASS_ROOM_NAME = "CloudClassRoomData";
	private SharedPreferences mSharedPreferences;
	private String saveFileDir="/sdcard/CAPPU/ClassRoom";
	
	private ArrayList<ClassRoomInfo> mListMap = new ArrayList<ClassRoomInfo>();
	
	private ArrayList<String> downloadUris=new ArrayList<String>();
	
	private ClassRoomManager(Context context){
		mContext=context;
		mSharedPreferences=mContext.getSharedPreferences(CLASS_ROOM_NAME, 0);
	}
	public static void init(Context context) {
		mClassRoomManager = new ClassRoomManager(context);
    }
	public static ClassRoomManager getInstance(){
		if (mClassRoomManager == null) {
            throw new IllegalStateException("Uninitialized");
        }

        return mClassRoomManager;
	}
	public ArrayList<ClassRoomInfo> getListMap(){
		return mListMap;
	}
	private int imageWidth=677;
	private int imageHeight=414;
	private int mBorderRadius=15;
	public static final int mCarouselNum = 4;// 轮播张数
	private String[] classRoomIds = new String[] { "5942345bd695631a74377dd9",
			"59422e33d6956326142cd8ba", "59422e3bd6956326142cd8bb",
			"59422e3bd6956326142cd8bd" };
	private String[] classRoomPaths = new String[] { "http://yun.cappu.com",
			"http://yun.cappu.com/index.php/page/index/kbdjt",
			"http://yun.cappu.com/index.php/page/index/yinpin",
			"http://yun.cappu.com/index.php/page/index/kbdjt2" };
	private String[] classRoomTitles = new String[] { "卡布云课堂,爸妈的掌上大学",
			"学微信,教您玩转智能手机", "广场舞,红歌,老歌,戏曲,相声", "卡布大讲堂 名师汇聚" };
	private int[] classRoomImages = new int[] {
			R.drawable.bg_cloud_class_room_0, R.drawable.bg_cloud_class_room_1,
			R.drawable.bg_cloud_class_room_2 ,R.drawable.bg_cloud_class_room_3};
	private Bitmap createRoundConerImage(Bitmap source) {
		Bitmap target;
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		target = Bitmap.createBitmap(imageWidth, imageHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(target);
		RectF rect = new RectF(0, 0, imageWidth, imageHeight);
		canvas.drawRoundRect(rect, mBorderRadius, mBorderRadius, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(source, 0, 0, paint);
		return target;
	}
	public void updateListMap(){
		mListMap.clear();
		for(int i=0;i<mCarouselNum;i++){
			ClassRoomInfo info=new ClassRoomInfo();
			info.setId(getClassRoomString("class_room_id_"+i,classRoomIds[i]));
			info.setPath(getClassRoomString("class_room_path_"+i,classRoomPaths[i]));
			info.setTitle(getClassRoomString("class_room_title_"+i,classRoomTitles[i]));
			String fileName=getClassRoomString("class_room_image_"+i,null);
			Bitmap imageBmp=BitmapFactory.decodeFile(saveFileDir+"/"+fileName);
			if(imageBmp!=null){
				info.setImageBmp(createRoundConerImage(imageBmp));
				if(imageBmp!=null&&!imageBmp.isRecycled()){
					imageBmp.recycle();
					imageBmp=null;
				}
			}else{
				imageBmp=BitmapFactory.decodeResource(mContext.getResources(), classRoomImages[i]);
				info.setImageBmp(imageBmp);
			}
			mListMap.add(info);
		}
	}
	public String getClassRoomString(String keyName,String defValue){
		return mSharedPreferences.getString(keyName, defValue);
	}
	private String getImageFileName(String imageString){
		String[] all_start=imageString.split("\\?st=");
		String[] all_end=all_start[0].split("/");
		return all_end[all_end.length-1];
	}
	private void analysisJson(String data){
		JSONObject root;
		try {
			root = new JSONObject(data.toString());
			JSONArray arr = root.getJSONArray("data");
			downloadUris.clear();
			for (int i = 0; i < mCarouselNum; i++) {
				JSONObject shape = (JSONObject) arr.get(i);
				LauncherLog.v(TAG, shape.getString("id"));
				LauncherLog.v(TAG, shape.getString("image"));
				LauncherLog.v(TAG, shape.getString("path"));
				LauncherLog.v(TAG, shape.getString("title"));
				LauncherLog.v(TAG, shape.getString("weight"));
				String class_id=getClassRoomString("class_room_id_"+i,null);
				String class_image=getClassRoomString("class_room_image_"+i,null);
				String id=shape.getString("id");
				String image=getImageFileName(shape.getString("image"));
				File file=new File(saveFileDir+"/"+image);
				if (class_id != null && class_image != null
						&& class_id.equals(id) && class_image.equals(image) && file.exists()) {
					continue;
				}
				Editor e = mSharedPreferences.edit();
		        e.putString("class_room_id_"+i, id);
		        e.putString("class_room_path_"+i, shape.getString("path"));
		        e.putString("class_room_title_"+i, shape.getString("title"));
		        e.putString("class_room_image_"+i, image);
		        e.commit();
		        downloadUris.add(shape.getString("image"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//hejianfeng add start
	private String getDeviceAddUrl(long ts) {

		String SIGN = md5Encode(APP_SECRET + ts + SIGN_KEY);
		/**
		 * 添加认证 认证在头部
		 */
		String AUTHORITIES_HEAD = "?appkey=" + AppKey + "&sign=" + SIGN;

		String DEVICE_ADD = classRoomAddress+AUTHORITIES_HEAD;
		return DEVICE_ADD;
	}

	private static final String SIGN_KEY = "cappu-g@od";
	private static final String APP_SECRET = "c540ccb20c484e0aade76cb95a6eb759";
	private static final String AppKey = "d53f0f1b2b24443ca3e2474bd4f5d14c";

	private String md5Encode(String inStr) {
		MessageDigest md5 = null;
		new StringBuffer();

		try {
			md5 = MessageDigest.getInstance("MD5");
			byte[] e = inStr.getBytes("UTF-8");
			byte[] md5Bytes = md5.digest(e);
			String content = binToHex(md5Bytes);
			return content;
		} catch (Exception var6) {
			System.out.println(var6.toString());
			var6.printStackTrace();
			return "";
		}
	}

	private String binToHex(byte[] md) {
		StringBuffer sb = new StringBuffer("");
		int read = 0;
		for (int i = 0; i < md.length; i++) {
			read = md[i];
			if (read < 0)
				read += 256;
			if (read < 16)
				sb.append("0");
			sb.append(Integer.toHexString(read));
		}
		return sb.toString();
	}
	private long currentTime;
	//hejianfeng add end
	public void updateData(){
		currentTime=System.currentTimeMillis();
		LauncherLog.v(TAG, "updateData, jeff URL="+getDeviceAddUrl(currentTime));
		ClassRoomSaveTask mClassRoomSaveTask=new ClassRoomSaveTask(getDeviceAddUrl(currentTime));
		mClassRoomSaveTask.execute();
	}
	/**
	 * 通过URL获取图片
	 * 
	 * @return URL地址图片的输入流。
	 */
	private InputStream getInputStream(String mUrl, boolean head) {
		InputStream inputStream = null;
		HttpURLConnection httpURLConnection = null;

		try {
			// 根据URL地址实例化一个URL对象，用于创建HttpURLConnection对象。
			URL url = new URL(mUrl);

			if (url != null) {
				// openConnection获得当前URL的连接
				httpURLConnection = (HttpURLConnection) url.openConnection();
				// 设置3秒的响应超时
				httpURLConnection.setConnectTimeout(3000);
				if(head){
					httpURLConnection.addRequestProperty("ts", currentTime+"");
				}
				// 设置允许输入
				httpURLConnection.setDoInput(true);
				// 设置为GET方式请求数据
				httpURLConnection.setRequestMethod("GET");
				// 获取连接响应码，200为成功，如果为其他，均表示有问题
				int responseCode = httpURLConnection.getResponseCode();
				if (responseCode == 200) {
					// getInputStream获取服务端返回的数据流。
					inputStream = httpURLConnection.getInputStream();
				}
			}

		} catch (MalformedURLException e) {
			LauncherLog.v(TAG, "getInputStream doInBackground11 MalformedURLException ");
		} catch (IOException e) {
			LauncherLog.v(TAG, "getInputStream doInBackground11 IOException ");
		}
		return inputStream;
	}
	class ClassRoomSaveTask extends AsyncTask<String, Void, String> {

        String mUrl;
        public ClassRoomSaveTask(String url) {
            this.mUrl = url;
        }

        @Override
        protected String doInBackground(String... params) {
            LauncherLog.v(TAG, " doInBackground  ");
            try {
            	InputStream inputStream=getInputStream(mUrl,true);
            	if(inputStream==null){
            		return null;
            	}
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String str;
                StringBuffer builder = new StringBuffer();
                while ((str = br.readLine()) != null) {
                    builder.append(str);
                }
                br.close();
                if(builder.toString()!=null){
                	analysisJson(builder.toString());
                }
                return builder.toString();
            } catch (MalformedURLException e) {
            	LauncherLog.v(TAG, " ClassRoomSaveTask doInBackground MalformedURLException ");
            } catch (IOException e) {
                LauncherLog.v(TAG, " ClassRoomSaveTask doInBackground IOException ");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String re) {
            super.onPostExecute(re);
            LauncherLog.v(TAG, " onPostExecute jeff");
            if(re!=null){
            	saveIntentImage();
            }
        }
	}
	private void saveIntentImage(){
		DownloadImageTask mDownloadImageTask=new DownloadImageTask();
		mDownloadImageTask.execute();
	}
    
    private void inputstreamtofile(InputStream ins, File file) {
    	LauncherLog.v(TAG, " saveImage,jeff fileName="+file.getName());
		FileOutputStream os = null;
		try {
			File fileDir=new File(saveFileDir);  
			//文件夹不存在，则创建它  
	        if(!fileDir.exists()){  
	        	fileDir.mkdir();  
	        } 
			if(!file.exists()){
				file.createNewFile();
			}
			os = new FileOutputStream(file);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ins != null)
					ins.close();
			} catch (IOException e) {
			}

			try {
				if (os != null)
					os.close();
			} catch (IOException e) {
			}
		}
	}
	class DownloadImageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            LauncherLog.v(TAG, " DownloadImageTask,jeff doInBackground  ");
            for(String mUrl :downloadUris){
				InputStream inputStream = getInputStream(mUrl,false);
				if (inputStream == null) {
					continue;
				}
				String fileName=getImageFileName(mUrl);
	            File file=new File(saveFileDir+"/"+fileName);
	            if(file.exists()){
	            	continue;
	            }
	            inputstreamtofile(inputStream,file);
            }
			return null;
			
        }
        @Override
        protected void onPostExecute(String re) {
            super.onPostExecute(re);
            LauncherLog.v(TAG, "DownloadImageTask onPostExecute jeff");
            updateListMap();//hejianfeng add
        }
	}
}
