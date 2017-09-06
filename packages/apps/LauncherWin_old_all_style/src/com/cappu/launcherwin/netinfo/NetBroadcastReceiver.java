
package com.cappu.launcherwin.netinfo;

import com.cappu.launcherwin.LauncherProvider;
import com.cappu.launcherwin.LauncherSettings;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NetBroadcastReceiver extends BroadcastReceiver {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    
    public static final int TYPE_NEWS = 1;

    public static final int TYPE_HEALTH = 2;

    public static final int TYPE_TXT = 3;

    private Handler sWorker = new Handler();
    
    public static final String AUTHORITY = "com.cappu.launcherwin";
    
    Calendar mDummyDate;

    @Override
    public void onReceive(Context context, Intent intent) {


        Calendar nowDate = Calendar.getInstance();
        mDummyDate = Calendar.getInstance();
        mDummyDate.setTimeZone(nowDate.getTimeZone());
        Date date = mDummyDate.getTime();
        
        String dateStr = dateFormat.format(date);
        
        Bundle bundle = intent.getExtras();
        String jsonStr = bundle.getString("json");
        Log.i("hehangjun", "json:" + jsonStr);
        try {
            JSONObject json = new JSONObject(jsonStr);
            int type = json.optInt("type");
            JSONObject itemJson = json.optJSONObject("item");
            
            String title = itemJson.optString("title");
            String introduce = itemJson.optString("introduce");
            String address = itemJson.optString("address");
            String icon = itemJson.optString("icon");
            String banner = itemJson.optString("banner");
            
            String selection = null;
            if(title == null || "".equals(title)){
                selection = " type ='"+type+"' and date='"+dateStr+"' and introduce='"+introduce+"' and address='"+address+"' and icon='"+icon+"' and banner='"+banner+"'";
            }else if(introduce == null || "".equals(introduce)){
                selection = " type ='"+type+"' and date='"+dateStr+"' and title='"+title+"' and address='"+address+"' and icon='"+icon+"' and banner='"+banner+"'";
            }else if(address == null || "".equals(address)){
                selection = " type ='"+type+"' and date='"+dateStr+"' and title='"+title+"' and introduce='"+introduce+"' and icon='"+icon+"' and banner='"+banner+"'";
            }else if(icon == null || "".equals(icon)){
                selection = " type ='"+type+"' and date='"+dateStr+"' and title='"+title+"' and introduce='"+introduce+"' and address='"+address+"' and banner='"+banner+"'";
            }else if(banner == null || "".equals(banner)){
                selection = " type ='"+type+"' and date='"+dateStr+"' and title='"+title+"' and introduce='"+introduce+"' and address='"+address+"' and icon='"+icon+"'";
            }else{
                selection = " type ='"+type+"' and date='"+dateStr+"' and title='"+title+"' and introduce='"+introduce+"' and address='"+address+"' and icon='"+icon+"' and banner='"+banner+"'";
            }
            
            
            if(title.substring(0, 2).equals("XW")){
                ContentValues values = new ContentValues();
                values.put("type", 1);
                values.put("date", dateStr);
                values.put("title", title);
                values.put("introduce", introduce);
                values.put("address", address);
                values.put("icon", icon);
                values.put("banner", banner);
                if(QueryDatabase(context, selection)){
                    InsertDatabase(context, values);
                }
            }else if(title.substring(0, 2).equals("JK")){
                ContentValues values = new ContentValues();
                values.put("type", 2);
                values.put("date", dateStr);
                values.put("title", title);
                values.put("introduce", introduce);
                values.put("address", address);
                values.put("icon", icon);
                values.put("banner", banner);
                if(QueryDatabase(context, selection)){
                    InsertDatabase(context, values);
                }
            }else{
                ContentValues values = new ContentValues();
                values.put("type", type);
                values.put("date", dateStr);
                values.put("title", title);
                values.put("introduce", introduce);
                values.put("address", address);
                values.put("icon", icon);
                values.put("banner", banner);
                if(QueryDatabase(context, selection)){
                    InsertDatabase(context, values);
                }
                
            }
            
/*            switch (type) {
                case TYPE_NEWS:
                    String title = itemJson.optString("title");
                    String introduce = itemJson.optString("introduce");
                    String address = itemJson.optString("address");
                    String icon = itemJson.optString("icon");
                    
                    ContentValues values = new ContentValues();
                    values.put("date", dateStr);
                    values.put("title", title);
                    values.put("introduce", introduce);
                    values.put("address", address);
                    values.put("icon", icon);
                    InsertDatabase(context, values);
                    break;
                case TYPE_HEALTH:

                    break;
                case TYPE_TXT:

                    break;
            }*/

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void InsertDatabase(Context context,final ContentValues values) {
        final Uri uri = Uri.parse("content://" +AUTHORITY + "/" + LauncherProvider.TABLE_NETINFO);
        final ContentResolver cr = context.getContentResolver();
        cr.insert(uri, values);
    }
    
    public boolean QueryDatabase(Context context,final String selection) {
        try {
            final Uri uri = Uri.parse("content://" +AUTHORITY + "/" + LauncherProvider.TABLE_NETINFO);
            final ContentResolver cr = context.getContentResolver();
            int count = -1;
            Cursor c = cr.query(uri, null,selection, null, null);
            if(c!=null){
                count = c.getCount();
                Log.i("hehangjun", "QueryDatabase count :"+count);
            }
            c.close();
            return count > 0?false:true;
        } catch (Exception e) {
            Log.i("hehangjun", "QueryDatabase Exception e:"+e.toString());
            return false;
        }
        
    }
    
    /*public void DeleteDatabase(Context context,final ContentValues values) {
        final Uri uri = Uri.parse("content://" +AUTHORITY + "/" + LauncherProvider.TABLE_NETINFO);
        final ContentResolver cr = context.getContentResolver();
        sWorker.post(new Runnable() {
            public void run() {
                Cursor c = cr.delete(url, "id", selectionArgs);
            }
        });
    }*/
}
