package com.cappu.launcherwin.applicationList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	
    private static final String dy_Tag = "dengyingApp";
    
    public static final String DB_NAME = "applist.db";
    private static final int version = 2;
    private static final String TBL_NAME = "app";
                                                                               
    private static final String CREATE_TBL = " create table " + TBL_NAME + "(_" +
    		"id integer primary key autoincrement," +	//自增的ID
    		"app_pkg text," +							//包名
    		"app_activity text," +						//类名
    		"type integer," +							//类型 1:经典应用 2:极简应用 3:游戏 4:工具
    		"app_order integer" +						//顺序
    		") ";
    
    private SQLiteDatabase db;
    //private SQLiteDatabase mDefaultWritableDatabase = null;
    
    private final AppInfo[] DEFAULT_APPS = {
    		
    		    new AppInfo("",null,"com.mediatek.filemanager","com.mediatek.filemanager.FileManagerOperationActivity",1),
    	    	new AppInfo("",null,"cn.goapk.market","cn.goapk.market.GoApkLoginAndRegister",1),
    		    new AppInfo("", null, "net.micode.fileexplorer", "net.micode.fileexplorer.FileExplorerTabActivity", 1),
            	//new AppInfo("", null, "com.tencent.mobileqq", "com.tencent.mobileqq.activity.SplashActivity", 1),
	    	    new AppInfo("", null, "com.android.soundrecorder", "com.android.soundrecorder.SoundRecorder", 1),
            	new AppInfo("", null, "com.android.browser", "com.android.browser.BrowserActivity", 1),
            	new AppInfo("", null, "com.tencent.android.qqdownloader", "com.tencent.assistant.activity.SplashActivity", 1),
            	new AppInfo("", null, "com.mediatek.security", "com.mediatek.security.ui.AutoBootAppManageActivity", 1),
            	new AppInfo("", null, "cappu.com.runthridapplication", "cappu.com.runthridapplication.MainActivity", 1),
            	new AppInfo("", null, "com.android.remotecare.cappup", "com.android.remotecare.activity.ContactListActivity", 1),


    		new AppInfo("",null,"com.mediatek.filemanager","com.mediatek.filemanager.FileManagerOperationActivity",2),
    		new AppInfo("",null,"com.android.browser","com.android.browser.BrowserActivity",2),
    		new AppInfo("",null,"cn.goapk.market","cn.goapk.market.GoApkLoginAndRegister",2),
    		new AppInfo("",null,"com.cnvcs.junqi","com.cnvcs.App",2),
    		new AppInfo("",null,"com.cnvcs.xiangqi","com.cnvcs.App",2),
    		new AppInfo("",null,"game.winten.Gobang","game.winten.Gobang.WintenGobang",2),
    		new AppInfo("",null,"com.cappu.launcherwin","com.cappu.launcherwin.phoneutils.PhoneUtilActivity",2),    		
    		new AppInfo("",null,"com.android.calculator2","com.android.calculator2.Calculator",2),
    		new AppInfo("",null,"com.cappu.magnifiter","com.cappu.magnifiter.MainActivity",2),
    		new AppInfo("",null,"com.cappu.pedometer","com.cappu.pedometer.Pedometer",2),
    		new AppInfo("",null,"com.example.xing","com.example.xing.activity.CaptureActivity",2),     		
    		
    		new AppInfo("",null,"com.cnvcs.junqi","com.cnvcs.App",3), 
    		new AppInfo("",null,"com.cnvcs.xiangqi","com.cnvcs.App",3),
    		new AppInfo("",null,"com.cnvcs.gomoku","com.cnvcs.App",3),
		new AppInfo("",null,"com.qqgame.happymj","com.onevcat.uniwebview.AndroidPlugin",3),
                new AppInfo("",null,"com.cnvcs.ddz","com.cnvcs.App",3),
    		
    		new AppInfo("",null,"com.cappu.launcherwin","com.cappu.launcherwin.phoneutils.PhoneUtilActivity",4),
    		new AppInfo("",null,"com.android.calculator2","com.android.calculator2.Calculator",4),
    		new AppInfo("",null,"com.cappu.magnifiter","com.cappu.magnifiter.MainActivity",4),
    		new AppInfo("",null,"com.cappu.pedometer","com.cappu.pedometer.Pedometer",4), 
    		new AppInfo("",null,"com.example.xing","com.example.xing.activity.CaptureActivity",4), 
                new AppInfo("",null,"com.cappu.healthmanage","com.cappu.healthmanage.MainActivity",4),
    		
    		};    
    
    
    public DBHelper(Context c) {
        super(c, DB_NAME, null, version);
    }

    public DBHelper(Context c, String name) {
        super(c, DB_NAME, null, version);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        try {
        	Log.e(dy_Tag, "onCreate:create table");
        	
            db.execSQL(CREATE_TBL);
            
            DefaultApps(db);
            
        } catch (SQLException ex) {
            Log.e(dy_Tag, "create table failure:ex=" + ex.toString());
        }
    }
       
    private void DefaultApps(SQLiteDatabase db){           
       
        ContentValues cv = new ContentValues();
        for (AppInfo app : DEFAULT_APPS) {
            cv.put("app_pkg", app.getPackage_name());
            cv.put("app_activity", app.getActivity_name());
            cv.put("type", app.getType());
            db.insert(TBL_NAME, null, cv);
            cv.clear();              
        }
    } 
    
    public boolean insert(ContentValues values) {
        try {
            if (db == null) {
                db = getWritableDatabase();
            }
            db.insert(TBL_NAME, null, values);
            db.close();
            return true;
        } catch (SQLException ex) {
            Log.d(dy_Tag, "insert table failure");
            return false;
        }
    }

    public Cursor query() {
        if (db == null) {
            db = getWritableDatabase();
        }
        Cursor c = db.query(TBL_NAME, null, null, null, null, null, null);
        return c;
    }

    public Cursor queryByDesc() {
        if (db == null) {
            db = getWritableDatabase();
        }
        Cursor c = db.query(TBL_NAME, null, null, null, null, null, "_id desc");
        return c;
    }    
    
    public Cursor queryByType(int type) {
        if (db == null) {
            db = getWritableDatabase();
        }
        Cursor c = db.query(TBL_NAME, null, "type=?", new String[] { String.valueOf(type)}, null, null, null);
        return c;
    }     
    
    public Cursor queryByTypeDesc(int type) {
        if (db == null) {
            db = getWritableDatabase();
        }
        Cursor c = db.query(TBL_NAME, null, "type=?", new String[] { String.valueOf(type)}, null, null, "_id desc");
        return c;
    }
    
    public boolean del(int id) {
        try {
            if (db == null) {
                db = getWritableDatabase();
            }
            db.delete(TBL_NAME, "_id=?", new String[] { String.valueOf(id) });
            return true;
        } catch (SQLException ex) {
            Log.d(dy_Tag, "update table failure");
            return false;
        }
    }

    public boolean delpkg(String pkg) {
        try {
            if (db == null) {
                db = getWritableDatabase();
            }
            db.delete(TBL_NAME, "app_pkg=?", new String[] { pkg });
            return true;
        } catch (SQLException ex) {
            Log.d(dy_Tag, "update table failure");
            return false;
        }
    }
    
    public boolean delByActivityByType(String pkg,String activity,int type) {
        try {
            if (db == null) {
                db = getWritableDatabase();
            }
              
            if(activity!=null && !activity.equals("")){
                int ret = db.delete(TBL_NAME, "app_pkg=? and app_activity=? and type=?", new String[] {pkg,activity,String.valueOf(type)});
            
            	Log.d(dy_Tag, "delByActivityByType app_pkg="+pkg+",activity="+activity + ", ret="+ret);
            	
            	if(ret == 0){
            		db.delete(TBL_NAME, "app_pkg=? and type=?", new String[] {pkg,String.valueOf(type)});
            	}
            	
            }else{
                db.delete(TBL_NAME, "app_pkg=? and type=?", new String[] {pkg,String.valueOf(type)});
            }
            
            return true;
        } catch (SQLException ex) {
            Log.d(dy_Tag, "update table failure");
            return false;
        }
    }    

    public boolean delByActivity(String pkg,String activity) {
        try {
            if (db == null) {
                db = getWritableDatabase();
            }
            db.delete(TBL_NAME, "app_pkg=? and app_activity=?", new String[] {pkg,activity});
            return true;
        } catch (SQLException ex) {
            Log.d(dy_Tag, "update table failure");
            return false;
        }
    }     
    
    public boolean update(String pkg, ContentValues values) {
        try {
            if (db == null) {
                db = getWritableDatabase();
            }
            db.update(TBL_NAME, values, "app_pkg=?", new String[] { pkg });
            db.close();
            return true;
        } catch (SQLException ex) {
            Log.d(dy_Tag, "update table failure");
            return false;
        }
    }

    @Override
    public void close() {
        if (db != null)
            db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS diary");
        onCreate(db);
    }
    
    @Override
    public SQLiteDatabase getWritableDatabase() {
        if(db == null){
            db = super.getWritableDatabase();
        }
        return db;
    }
}
