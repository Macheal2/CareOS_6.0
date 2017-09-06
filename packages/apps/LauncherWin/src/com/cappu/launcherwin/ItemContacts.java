/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 */

/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.cappu.launcherwin;

import java.io.IOException;
import java.io.InputStream;

import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.characterSequence.tools.SortModel;
import com.cappu.launcherwin.widget.LauncherLog;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

//START: added by Yar @20170824
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.content.ContentUris;
import android.provider.ContactsContract;
import java.util.HashMap;
import java.util.Map;
import android.os.Handler;
import android.database.ContentObserver;
//END: added by Yar @20170824

/**
 * Represents an item in the launcher.
 */
public class ItemContacts extends ItemInfo {
    
    public static final String TAG = "ItemContacts";
    
    Intent intent;
    /**这个是显示的名字  当没有联系人名字的时候默认就是这个,默认是添加*/
    CharSequence title;
    /**联系人号码*/
    String phoneNumber = null;
    /**联系人名字*/
    String contactName = null;
    /**联系人头像的uri地址*/
    Uri CPUri;
    /**头像名字 联系人特有的头像*/
    String cellDefImage = null;
    /**头像的图片*/
    Bitmap mHeadBitmap;
    /**别名 默认开始的时候是添加，因为没有联系人名字*/
    String mAliasTitle;
    /**这个是文字显示的背景图*/
    String aliasTitleBackground = null;
    /**这个是磁块的背景名字*/
    String background;
    /**背景的图片res*/
    int mBackgroundRes;
    /**是否是自定义头像*/
    private boolean isCustom = false;
    
    Context mContext;
    
    private Paint mPaint;
    
    //START: added by Yar @20170824
    Handler mHandler;
    int mRawId = -1;
    //END: added by Yar @20170824

    ItemContacts(Context context, Handler handler) {//modified by Yar @20170824
        this.mContext = context;
        //START: added by Yar @20170824
        this.mHandler = handler;
        //END: added by Yar @20170824
        itemType = LauncherSettings.Favorites.ITEM_TYPE_CONTACTS;
    }
    void init() {
		if (!TextUtils.isEmpty(background)) {
			mBackgroundRes = mContext.getResources().getIdentifier(background,
					"drawable", "com.cappu.launcherwin");
		}
		android.util.Log.i("Yar", " ItemContacts init(), mRawId = " + mRawId + ", phoneNumber = " + phoneNumber + ", contactName = " + contactName + ", CPUri = " + CPUri + ", mAliasTitle = " + mAliasTitle);
        mPaint=new Paint();
		mPaint.setAntiAlias(true); 
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CPUri;
        InputStream input = null;
        if(uri != null){
            input = openDisplayPhoto(uri);
            isCustom = true;
        }
        if (input == null) {
        	LauncherLog.v(TAG, "init,jeff intent="+intent);
        	LauncherLog.v(TAG, "init,jeff cellDefImage="+cellDefImage);
            mHeadBitmap = ThemeManager.getInstance().getItemContactsIcon(intent, cellDefImage);
            if(mHeadBitmap == null){
            	Drawable mHeadDrawable = mContext.getResources().getDrawable(R.drawable.i99_default_photo);
                BitmapDrawable bd = (BitmapDrawable) mHeadDrawable;
                mHeadBitmap = bd.getBitmap();
            }
            isCustom = false;
        }else{
            Bitmap photo = BitmapFactory.decodeStream(input);
            mHeadBitmap = photo;
            isCustom = true;
        }
        if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
        	mHeadBitmap=ThemeRes.getInstance().setPhotoFrame(mHeadBitmap.copy(Bitmap.Config.ARGB_8888, true));
        }
        if(TextUtils.isEmpty(contactName)){
            title = getAliasTitle(mAliasTitle);
        }else{
            title = contactName;
        }
        
        textSize = Settings.Global.getInt(mContext.getContentResolver(), "textSize", mContext.getResources().getDimensionPixelSize(R.dimen.xl_text_size));
    }
    public String getAliasTitle(String aliasTitle) {
        int thumbRes = mContext.getResources().getIdentifier(aliasTitle, "string", "com.cappu.launcherwin");
        String aliastitle = mContext.getResources().getString(thumbRes);
        return aliastitle;
    }
    
    public InputStream openDisplayPhoto(Uri uri) {
        
        try {
            AssetFileDescriptor fd =mContext.getContentResolver().openAssetFileDescriptor(uri, "r");
            return fd.createInputStream();
        } catch (IOException e) {
            Log.i(TAG, "  openDisplayPhoto:"+e.toString());
            return null;
        }
    }
    
    //START: added by Yar @20170824
    private ContactsObserver mObserver;
	
	class ContactsObserver extends ContentObserver {
		public ContactsObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			// 当联系人表发生变化时进行相应的操作
			Log.i("Yar", " onChange() mRawId = " + mRawId);
			
		}
	}
    //END: added by Yar @20170824
    
    /**网络版用到的*/
    private Bitmap getContactHeadPhoto(ContentResolver cr ,String mNameStr, String mNumberStr){
        Bitmap bitmap = null;
        Cursor cursor = cr.query(LauncherSettings.Favorites.CONTACTS_URI,null, " name = '"+mNameStr+"' and number = '"+mNumberStr+"'",null,null);
        Log.i(TAG, "getContactHeadPhoto cursor:"+cursor.getCount());
        try {
            
            final int idIndex = cursor.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int nameIndex = cursor.getColumnIndexOrThrow("name");
            final int numberIndex = cursor.getColumnIndexOrThrow("number");
            final int groupIndex = cursor.getColumnIndexOrThrow("groups");
            final int headIndex = cursor.getColumnIndexOrThrow("head");
            
            final int headPhotoIndex = cursor.getColumnIndexOrThrow("headPhoto");
            
            String CurDate = null;
            
            while (cursor.moveToNext()) {
                int id = (int) cursor.getLong(idIndex);
                String name = cursor.getString(nameIndex);
                String number = cursor.getString(numberIndex);
                long group = cursor.getLong(groupIndex);
                String head = cursor.getString(headIndex);
                
                byte[] in = cursor.getBlob(headPhotoIndex);
                if(in != null){
                    bitmap = BitmapFactory.decodeByteArray(in, 0, in.length);
                }

            }
        }catch(Exception e){
            Log.i(TAG, "getContactHeadPhoto Exception e  "+e);
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return bitmap;
    }
    
    /**网络版用到的*/
    private String getContactHeadStr(ContentResolver cr ,String mNameStr, String mNumberStr){
        String head = null;
        Cursor cursor = cr.query(LauncherSettings.Favorites.CONTACTS_URI,null, " name = '"+mNameStr+"' and number = '"+mNumberStr+"'",null,null);
        try {
            
            final int idIndex = cursor.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int nameIndex = cursor.getColumnIndexOrThrow("name");
            final int numberIndex = cursor.getColumnIndexOrThrow("number");
            final int groupIndex = cursor.getColumnIndexOrThrow("groups");
            final int headIndex = cursor.getColumnIndexOrThrow("head");
            
            final int headPhotoIndex = cursor.getColumnIndexOrThrow("headPhoto");
            
            String CurDate = null;
            
            while (cursor.moveToNext()) {
                head = cursor.getString(headIndex);
            }
        }catch(Exception e){
            Log.i(TAG, "getContactHeadPhoto Exception e  "+e);
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return head;
    }
    
    /**获取联系人号码*/
    public String getPhoneNumber(){
        return phoneNumber;
    }
    
    /**获取联系人名字*/
    public String getContactName(){
        return contactName;
    }
    
    /**联系人获取显示的头像*/
    public Bitmap getHeadBitmap(){
        return mHeadBitmap;
    }
    
    /**获取磁块背景*/
    public int getBackgroundRes(){
        return mBackgroundRes;
    }
    /**获取磁块背景*/
    public String getBackgroundName(){
        return background;
    }
    /**是否是自定义头像*/
    public boolean getCustomHeader(){
        return isCustom;
    }
    
    public String getTitle(){
        return (String) title;
    }
    
    public int getTextSize(){
        return textSize;
    }
    
    public int getTextBackgroundRes() {
         return R.drawable.shape_circle_blck_transparent;
    }
    
    //START: added by Yar @20170824
    public String getDisplayName(ContentResolver cr ,int contact_id){
        Cursor nameCur = cr.query(Contacts.CONTENT_URI,
                new String[]{Contacts.DISPLAY_NAME},
                Contacts._ID  + "= ?",
                new String []{contact_id + ""},
                null);
        boolean hasData = nameCur.moveToFirst();
        if (!hasData) {
        	android.util.Log.i("Yar", " getDisplayName() no data");
        	return "";
        }
        android.util.Log.i("Yar", " getDisplayName() has data");
        String name = nameCur.getString(0);
        nameCur.close();
        return name;
    }
    
    public Map<String, String> getQuickInfo(ContentResolver cr ,int raw_contact_id){
        Map<String, String> info = new HashMap<String, String>();

        Cursor dataCursor = cr.query(Data.CONTENT_URI,null,"raw_contact_id=?", new String[] { raw_contact_id + "" }, null);
        
        //dengyingContact@20160628 begin
        String data1data2_2= "";
        String data1data2_7= "";
        String data1data2_other= "";
        //dengyingContact@20160628 end 
        /*boolean hasData = dataCursor.moveToFirst();
        if (!hasData) {
        	android.util.Log.i("Yar", " getQuickInfo() no data");
        	return null;
        }
        android.util.Log.i("Yar", " getQuickInfo() has data");*/
        
        while (dataCursor.moveToNext()) {
            String data1 = dataCursor.getString(dataCursor.getColumnIndex(Data.DATA1));
            String data2 = dataCursor.getString(dataCursor.getColumnIndex(Data.DATA2));//data2=2 第一个号码 data2=7 第二个号码 //dengying@20160603
            String mime = dataCursor.getString(dataCursor.getColumnIndex(Data.MIMETYPE));
            if (Phone.CONTENT_ITEM_TYPE.equals(mime)) {
                 Log.i("Yar", "number == " + data1 +",data2=="+data2);
                
					if ("2".equals(data2)) {//dengying@20160603  //modified by Yar @20170828
						//info.put(Phone.CONTENT_ITEM_TYPE, data1);
						data1data2_2 = data1;
					}else if ("7".equals(data2)) { //modified by Yar @20170828
						data1data2_7 = data1;
					}else{                     
						data1data2_other = data1;
					}
					
                 info.put(Phone.CONTENT_ITEM_TYPE, data1);
            }else if(Photo.CONTENT_ITEM_TYPE.equals(mime)){
                final int colum = dataCursor.getColumnIndex("photo_key");
                if(colum > 0){
                    String key = dataCursor.getString(colum);
                    info.put(Photo.CONTENT_ITEM_TYPE, key);
                }
            }
        }
        
		//dengyingContact@20160628 begin
		if(!"".equals(data1data2_2)){
			info.put(Phone.CONTENT_ITEM_TYPE, data1data2_2);
		}else if(!"".equals(data1data2_7)){
			info.put(Phone.CONTENT_ITEM_TYPE, data1data2_7);
		}else{
			info.put(Phone.CONTENT_ITEM_TYPE, data1data2_other);
		}
		//dengyingContact@20160628 end
		
        dataCursor.close();
        return info ;
    }
    
    public Uri parsePhotoUri(ContentResolver cr ,int contact_id){
        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contact_id);
        return Uri.withAppendedPath(photoUri, Contacts.Photo.DISPLAY_PHOTO);
    }
    //END: added by Yar @20170824

}
