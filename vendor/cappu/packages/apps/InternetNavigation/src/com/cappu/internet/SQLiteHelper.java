package com.cappu.internet;

import java.io.UnsupportedEncodingException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper {
	private static final String TAG = "SQLiteHelper";

	public static final String KEY_ID = "_id";

	public static final String KEY_NAME = "name";

	public static final String KEY_URL = "url";

	private static final String DB_NAME = "db_bookmark.db";

	private static final String DB_TABLE = "tb_bookmark";

	private static final int DB_VERSION = 1;

	private Context mContext = null;

	private static final String DB_CREATE = "CREATE TABLE " + DB_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_URL + " TEXT)";

	private SQLiteDatabase mSQLiteDatabase = null;

	private DatabaseHelper mDatabaseHelper = null;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DB_CREATE);

			/*
			 * String name_llq = "浏览器";
			 * 
			 * try { byte[] val_llq = new byte[name_llq.length()]; val_llq =
			 * name_llq.getBytes("GBK"); name_llq=new String(val_llq,"UTF-8"); }
			 * catch (UnsupportedEncodingException e) { // TODO Auto-generated
			 * catch block e.printStackTrace(); }
			 */

			
			// init
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('卡布生活网','http://www.careos.com.cn')");//dengying@20140809
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('云课堂','http://yun.cappu.com')");//dengying@20140825
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('卡布奇诺','http://www.cappu.com')");//dengying@20140825
			 
			
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('百度','http://m.baidu.com/?from=1000225j')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('凤凰网','http://i.ifeng.com')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('环球网','http://m.huanqiu.com/')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('铁血','http://m.tiexue.net/touch/')");
			

			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('腾讯视频','https://v.qq.com/index.html')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('爱奇艺','http://m.iqiyi.com/')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('央视网','http://m.cctv.com/index.shtml')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('qq音乐','https://m.y.qq.com/')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('网易音乐','http://music.163.com/m/')");
			
			
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('好大夫','http://m.haodf.com/')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('问医生','http://m.120ask.com/')");		
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('39健康网','http://m.39.net')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('微医挂号','http://3g.guahao.com')");
			
			
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('同花顺','http://m.10jqka.com.cn/')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('和讯网','http://m.hexun.com/')");		
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('财新网','http://m.caixin.com/m/')");
		
				
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('京东','https://union-click.jd.com/jdc?d=G5wTti&sid=SacVVgf2yVJW')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('天猫','https://www.tmall.com/?from=m')");		
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('淘宝','https://m.taobao.com/#index')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('苏宁','http://m.suning.com/')");				
				

			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('大众点评','https://m.dianping.com')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('同程','https://m.ly.com/')");		
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('携程','http://m.ctrip.com/html5/')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('途牛','http://m.tuniu.com/')");					 
				 	 
			
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('解梦','http://m.jjdzc.com/zhougong.html')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('糗百','https://www.qiushibaike.com/')");		
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('捧腹','https://m.pengfu.com/')");
			db.execSQL("INSERT INTO tb_bookmark (name,url) values ('网址大全','http://nav.careos.com.cn')");			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}

	public SQLiteHelper(Context context) {
		mContext = context;
	}

	public void open() throws SQLException {
		mDatabaseHelper = new DatabaseHelper(mContext);
		mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
	}

	public void close() {
		mDatabaseHelper.close();
	}

	public long insertData(String name, String url) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_URL, url);

		return mSQLiteDatabase.insert(DB_TABLE, KEY_ID, initialValues);
	}

	public boolean deleteData(long rowId) {
		return mSQLiteDatabase.delete(DB_TABLE, KEY_ID + "=" + rowId, null) > 0;
	}

	public Cursor fetchAllData() {
		return mSQLiteDatabase.query(DB_TABLE, new String[] { KEY_ID, KEY_NAME, KEY_URL }, null, null, null, null, null);
	}

	public Cursor fetchData(long rowId) throws SQLException {

		Cursor mCursor =

		mSQLiteDatabase.query(true, DB_TABLE, new String[] { KEY_ID, KEY_NAME, KEY_URL }, KEY_ID + "=" + rowId, null, null, null, null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	public boolean updateData(long rowId, String name, String url) {
		ContentValues args = new ContentValues();
		args.put(KEY_NAME, name);
		args.put(KEY_URL, url);

		return mSQLiteDatabase.update(DB_TABLE, args, KEY_ID + "=" + rowId, null) > 0;
	}

}
