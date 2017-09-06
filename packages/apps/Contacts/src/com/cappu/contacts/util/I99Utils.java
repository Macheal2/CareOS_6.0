/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cappu.contacts.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import android.text.TextUtils;
import com.android.contacts.R;
import android.provider.CallLog;
import android.util.DisplayMetrics;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import com.android.contacts.common.CallUtil;
import com.cappu.contacts.I99GroupActivity;
import com.android.contacts.common.ContactsUtils;
import com.android.contacts.common.model.Contact;
import android.provider.ContactsContract.Contacts;
import com.android.contacts.common.util.Constants;
import com.mediatek.contacts.simcontact.SlotUtils;
import android.provider.ContactsContract.PhoneLookup;
import com.mediatek.contacts.simcontact.SimCardUtils;
import android.provider.ContactsContract.Intents.Insert;
import com.android.contacts.activities.ContactEditorActivity;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import com.android.contacts.interactions.ContactDeletionInteraction;
import java.io.IOException;
import java.io.InputStream;
import android.content.res.AssetManager;
/**
 * Functions to easily prepare contextual help menu option items with an intent that opens up the
 * browser to a particular URL, while taking into account the preferred language and app version.
 */
public class I99Utils {
    private final static String TAG = I99Utils.class.getName();
    public final static boolean USED_I99 = true ;
    
    private static I99Utils mUtils;

    public final static int SIM1 = 0 ;
    public final static int SIM2 = 1 ;

    private I99Utils(Context context) {

    }

    public static I99Utils getInstance(Context context){
        if(mUtils == null){
            mUtils = new I99Utils(context);
        }
        return mUtils;
    }

    public static String plusString(String one , String two){
        StringBuilder builder = new StringBuilder(one);
        builder.append(two);
        return builder.toString();
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
    
    public static int[] getScreenSize(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        int[] size = { dm.widthPixels, dm.heightPixels };
        return size;
    }

    /**
     * 获取状态栏的高度
     * @param activity
     * @return
     */
    public static int getStatusBarH(Activity activity){
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    public static void deleteContact(Activity activity, Contact data ){
        Uri contactUri = data.getLookupUri();
        if (data.getIndicate() < 0) {
            ContactDeletionInteraction.start(activity, contactUri, true);
        } 
    }
    public static void editContact(Context context ,Uri lookupUri){
        Intent intent = new Intent("com.android.contacts.action.FULL_EDIT", lookupUri);//dengying modify
        intent.putExtra(ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true);
        context.startActivity(intent);
    }
    public static void shareContact(Context context , Uri shareUri ,long contactId){
        if (shareUri == null) return ;
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(Contacts.CONTENT_VCARD_TYPE);
        intent.putExtra("contactId", String.valueOf(contactId));
        intent.setType(Contacts.CONTENT_VCARD_TYPE);
        intent.putExtra(Intent.EXTRA_STREAM, shareUri);
        final CharSequence chooseTitle = context.getText(R.string.share_via);
        final Intent chooseIntent = Intent.createChooser(intent, chooseTitle);
        try {
            context.startActivity(chooseIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, R.string.share_error, Toast.LENGTH_SHORT).show();
        }

    }

    public static void shareContact(Context context , Contact data){
        if (data == null) return ;

        final String lookupKey = data.getLookupKey();
        Uri shareUri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, lookupKey);
        final Intent intent = new Intent(Intent.ACTION_SEND);

        if (data.isUserProfile()) {
            shareUri = getPreAuthorizedUri(context ,shareUri);
            intent.setType(Contacts.CONTENT_VCARD_TYPE);
            intent.putExtra("userProfile", "true");
        } else {
            intent.setType(Contacts.CONTENT_VCARD_TYPE);
            intent.putExtra("contactId", String.valueOf(data.getContactId()));
        }
        intent.setType(Contacts.CONTENT_VCARD_TYPE);
        intent.putExtra(Intent.EXTRA_STREAM, shareUri);

        final CharSequence chooseTitle = context.getText(R.string.share_via);
        final Intent chooseIntent = Intent.createChooser(intent, chooseTitle);

        try {
            context.startActivity(chooseIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, R.string.share_error, Toast.LENGTH_SHORT).show();
        }

    }

    public static void shareContactBySms(Context context , String message){
        Uri uri = Uri.fromParts("sms", "", null);
        Intent intent = new Intent(Intent.ACTION_SENDTO,uri);
        intent.putExtra("sms_body", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    /**
    * Calls into the contacts provider to get a pre-authorized version of the given URI.
    */
    private static Uri getPreAuthorizedUri(Context context ,Uri uri) {
        Bundle uriBundle = new Bundle();
        uriBundle.putParcelable(ContactsContract.Authorization.KEY_URI_TO_AUTHORIZE, uri);
        Bundle authResponse = context.getContentResolver().call(
                ContactsContract.AUTHORITY_URI,
                ContactsContract.Authorization.AUTHORIZATION_METHOD,
                null,
                uriBundle);
        if (authResponse != null) {
            return (Uri) authResponse.getParcelable(
                    ContactsContract.Authorization.KEY_AUTHORIZED_URI);
        } else {
            return uri;
        }
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
     * add  "groupId" to contact
     */
    public static void addContact(Context context, int groupId){
        final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
        intent.putExtra(I99GroupActivity.KEY_GROUP_ID, groupId);
        context.startActivity(intent);
    }
    /**
     * send message for this number
     */
    public static void sendMessage(Context context, String number){
        Uri uri = Uri.fromParts("sms", number, null);
        final Intent intent = new Intent(Intent.ACTION_SENDTO,uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    public static void doCall(Context context, String number){
        Uri uri = Uri.fromParts("tel", number, null);
        final Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void doIPCall(Context context,String number){
        android.util.Log.e("wangcunxi","number:"+number);
        Intent intent = CallUtil.getCallIntent(CallUtil.getCallUri(number), null,Constants.DIAL_NUMBER_INTENT_IP);
        context.startActivity(intent);

    }    
    
    public static void openSms(Context context){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn = new ComponentName("com.android.mms", "com.android.mms.ui.ConversationList");
        intent.setComponent(cn);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    /**
     *@param number : the phone number
     */
    public static  Uri getLookupUri(Context context, String number){
        Uri lookupUri = null ;
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor c= context.getContentResolver().query(uri, null,null,null,null);

        if(c != null && c.moveToFirst()){
            long id = c.getLong(c.getColumnIndex(PhoneLookup._ID));
            String key = c.getString(c.getColumnIndex(PhoneLookup.LOOKUP_KEY));
            lookupUri = Contacts.getLookupUri(id, key);
        }
        c.close();
        return lookupUri ;
    }   

    
    /**
     * Determine whether contact has been saved 
     *  return 
     *      true : saved
     *      false: unsaved
     */
    public static boolean isSavedNumber(Context context , String number){
        boolean saved = false;
        ContentResolver resolver = context.getContentResolver();
        final Cursor cursor = resolver.query(ContactsContract.Data.CONTENT_URI, 
            null, "data1=? AND mimetype=?", new String[]{number,Phone.CONTENT_ITEM_TYPE},null);
        if(cursor.getCount() > 0){
            saved = true;
        }else{
            saved = false;
        }
        cursor.close();
        return saved;
    }
    
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
            final float roundPx = bitmap.getWidth()/7;
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            return output;
        } catch (Exception e) {
            return bitmap;
        }
    }

    public static boolean isCustomerService(Context context){
        return false;
    }
    //wangcunxi add begin Care_os(get Assets bitmap)
    public static Bitmap getImageFromAssetsFile(Context context, String fileName){
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try{
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return image;
    }
    //wangcunxi add end
}
