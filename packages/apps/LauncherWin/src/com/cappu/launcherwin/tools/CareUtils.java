package com.cappu.launcherwin.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicKEY;
//import com.cappu.launcherwin.mms.ui.CareMmsNewEditActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.SyncStateContract.Constants;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebView.FindListener;
import android.widget.Toast;

public class CareUtils {

    private static final String TAG = "CareUtils";
    public static final String EMPTY = "";
    
    public static int[] getScreenSize(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        int[] size = { dm.widthPixels, dm.heightPixels };
        return size;
    }

    public static String plusString(String ...strs){
        StringBuilder builder = new StringBuilder();
        for (String str:strs){
            if(!TextUtils.isEmpty(str)){
                builder.append(str);
            }
        }
        return builder.toString();
    }


    public static void doCall(Context context, String number){
        if(TextUtils.isEmpty(number)){
            Log.i(TAG,"number ==null just return");
            Toast.makeText(context, R.string.number_null, Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.fromParts("tel", number, null);
        final Intent intent = new Intent(Intent.ACTION_CALL, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public static void doIpCall(Context context, String number){
    	if(TextUtils.isEmpty(number)){
            Log.i(TAG,"number ==null just return");
            Toast.makeText(context, R.string.number_null, Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.fromParts("tel", number, null);
        final Intent intent = new Intent(Intent.ACTION_CALL, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        context.startActivity(intent);
    }
    
    /**
     * send message for this number
     */
    public static void sendMessage(Context context, String number){
		if (TextUtils.isEmpty(number)) {
			Log.i(TAG, "number ==null just return");
			Toast.makeText(context, R.string.number_null, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		Uri uri = Uri.fromParts("sms", number, null);
		final Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
        
    }

    /**
     *@param number : the phone number
     */
    public static  Uri getLookupUri(Context context, String number){
    	Cursor cursor = null ;
    	Uri lookupUri = null ;
    	try{
	    	if(TextUtils.isEmpty(number)){
	            Log.i(TAG,"number ==null just return");
	            return null;
	        }
	    	
	        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	        cursor = context.getContentResolver().query(uri, null,null,null,null);
	
	        if(cursor != null && cursor.moveToFirst()){
	            long id = cursor.getLong(cursor.getColumnIndex(PhoneLookup._ID));
	            String key = cursor.getString(cursor.getColumnIndex(PhoneLookup.LOOKUP_KEY));
	            lookupUri = Contacts.getLookupUri(id, key);
	        }
    	}finally{
    		cursor.close();
    		cursor = null;
    	}
        return lookupUri ;
    }

    public static String getDisplayNameByNumber(Context context, String number){
    	Cursor cursor = null ;
    	String name = EMPTY;
    	try{
    		cursor = context.getContentResolver().query(Phone.CONTENT_URI, null, Phone.NUMBER + "=?", new String[]{number}, null);
    		cursor.moveToFirst();
    		name = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
    		
    	}catch(Exception e){
    		return EMPTY;
    	}finally{
    		cursor.close();
    		cursor = null;
    	}
    	return name;
    	
    }
    
    public static String getNowTime(){
        SimpleDateFormat sfd = new SimpleDateFormat("HH:mm");
        Date date = new Date(System.currentTimeMillis());
        return sfd.format(date);
    }
    public static String getTime(Context context, long time){
        SimpleDateFormat sfd;
        if(DateFormat.is24HourFormat(context)){
        	sfd = new SimpleDateFormat("HH:mm");
        }else{
        	sfd = new SimpleDateFormat("a hh:mm");
        }
        Date date = new Date(time);
        return sfd.format(date);
    }
    
    
    /**
     * add  contact
     */
    public static void addContact(Context context){
        final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
        context.startActivity(intent);
    }
    
    /**
     * add  "number" to contact
     */
    public static void addContact(Context context, String number){
        final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
        intent.putExtra(Insert.PHONE, number);
        context.startActivity(intent);
    }
    /**
     * add  contact
     */
    public static void viewContact(Context context, Uri uri){
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    
}
