package com.cappu.calendar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

public class FirstActivity extends Activity {

    public static SQLiteDatabase mSQLiteDatabase;//数据库
    public static final String filePath = "data/data/com.cappu.calendar/databases/calendar.db"; // 数据库存储路径
    public static final String pathStr = "data/data/com.cappu.calendar/databases";// 数据库存放的文件夹 data/data/com.main.jh 下面
    /*节气保存数组*/
    public static String mSolarterm_array[]; 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(mSQLiteDatabase == null){
            //mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(getDatabasePath("calendar.db"), null);
            mSQLiteDatabase = openDatabase(this);
        }
        
        if(!isSolarTermArrayExists()){
            String[] columns = {"sDate","SolarTerm"};
            Cursor cursor = mSQLiteDatabase.query("HL_s", columns, " SolarTerm <> \'\'", null, null, null, null);
            try {
                final int dateIndex = cursor.getColumnIndexOrThrow("sDate");
                final int jqIndex = cursor.getColumnIndexOrThrow("SolarTerm");
                mSolarterm_array = new String[cursor.getCount()];
                while (cursor.moveToNext()) {
                    String date = cursor.getString(dateIndex);//阳历日期
                    date = date.substring(0, date.indexOf(" "));
                    String jieqi = cursor.getString(jqIndex);//节气
                    mSolarterm_array[cursor.getPosition()] = date+" "+jieqi;
                }
            } catch (Exception e) {
                Log.e("hmq", "Exception:" + e.toString());
            }finally{
                cursor.close();
            }
        }
        
        SharedPreferences sp = getSharedPreferences(BasicActivity.CALENDAR_MENU_SP, getBaseContext().MODE_WORLD_WRITEABLE);
        int sp_intent = sp.getInt(BasicActivity.CALENDAR_MENU_SP_ACT, -1);

        if (sp_intent == 1) {
            startActivity(new Intent(this, DetailsCalendarActivity.class));
        } else {
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
        }
        finish();
    }
    
    /*
     * 打开数据库
     */
    public SQLiteDatabase openDatabase(Context context) {
        File jhPath = new File(filePath);
        // 查看数据库文件是否存在
        if (jhPath.exists()) {
            // 存在则直接返回打开的数据库
            return SQLiteDatabase.openOrCreateDatabase(jhPath, null);
        } else {
            // 不存在先创建文件夹
            File path = new File(pathStr);
            if (path.mkdir()) {
                Log.e("HHJ","创建成功");
            } else {
                Log.e("HHJ","创建失败");
            }
            try {
                // 得到资源
                AssetManager am = context.getAssets();
                // 得到数据库的输入流
                InputStream is = am.open("calendardata.db");
                // 用输出流写到SDcard上面
                FileOutputStream fos = new FileOutputStream(jhPath);
                // 创建byte数组 用于1KB写一次
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                // 最后关闭就可以了
                fos.flush();
                fos.close();
                is.close();
            } catch (IOException e) {
                Log.e("HHJ", "错了"+e.toString());
            }
            // 如果没有这个数据库 我们已经把他写到SD卡上了，然后在执行一次这个方法 就可以返回数据库了
            return SQLiteDatabase.openOrCreateDatabase(jhPath, null);
        }
    }
    
    /**
     * edit by hmq
     * this init Chinese calendar solar term
     * To determine whether there is a array
     */
    private boolean isSolarTermArrayExists(){
        if (mSolarterm_array == null)
            return false;
        if (mSolarterm_array.length < 2)
            return false;
        return true;
    }
}
