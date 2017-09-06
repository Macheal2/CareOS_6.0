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

package com.cappu.contacts;

import android.content.Context;
import android.content.ComponentName;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings;
import android.util.Log;

import com.android.contacts.R;

/**
 * Functions to easily prepare contextual help menu option items with an intent that opens up the
 * browser to a particular URL, while taking into account the preferred language and app version.
 */
public class I99Configure {
    private final static String TAG = I99Configure.class.getName();
    public final static boolean USED_I99 = true ;
    private static final int FONT_SMALL = 42 ;
    private static final int FONT_NORMAL = 1 ;
    private static final int FONT_LARGE = 2 ;

    public static final int FAMILY_ID = 501;
    public static final int RELATIVE_ID = 502;
    public static final int FRIEND_ID = 503;
    public static final int COMMUNITY_ID = 504;

    public static void updateFont(Context context){
        // TODO NOTHING
        /**
        Context _context = null;
        int size = -1 ;
        try {
            _context = context.createPackageContext("com.android.cappu", Context.CONTEXT_IGNORE_SECURITY);
            size = Settings.Secure.getInt(_context.getContentResolver(), "textSizeType");
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"size == " + size);
        switch(size){
            case FONT_SMALL:
                I99Font.TOPBAR = getResDimen(context,R.dimen.i99_text_topbar_size_small);
                I99Font.TITLE = getResDimen(context,R.dimen.i99_text_size_small);
                I99Font.SUMMERY = getResDimen(context,R.dimen.i99_text_summary_size_small);
            break;

            case FONT_NORMAL:
                I99Font.TOPBAR = getResDimen(context,R.dimen.i99_text_topbar_size_normal);
                I99Font.TITLE = getResDimen(context,R.dimen.i99_text_size_normal);
                I99Font.SUMMERY = getResDimen(context,R.dimen.i99_text_summary_size_normal);
            break;

            
            case FONT_LARGE:
                I99Font.TOPBAR = getResDimen(context,R.dimen.i99_text_topbar_size_large);
                I99Font.TITLE = getResDimen(context,R.dimen.i99_text_size_large);
                I99Font.SUMMERY = getResDimen(context,R.dimen.i99_text_summary_size_large);
                break;

            default:
                I99Font.TOPBAR = getResDimen(context,R.dimen.i99_text_topbar_size_default);
                I99Font.TITLE = getResDimen(context,R.dimen.i99_text_size_default);
                I99Font.SUMMERY = getResDimen(context,R.dimen.i99_text_summary_size_default);
            break;
        }
        I99Font.updateOtherFont();
         */
    }
    /**
    * 将px值转换为sp值，保证文字大小不变
    *
    * @param pxValue
    * @param fontScale （DisplayMetrics类中属性scaledDensity）
    * @return
    */
    public static float px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static float getResDimen(Context context , int resid){
        float size = context.getResources().getDimension(resid);
        return px2sp(context,size);
    }

    public static boolean getDialpadSpeechState(Context context){
		Context _context = null;
		int state = Settings.System.getInt(context.getContentResolver(), "dialpad_speech_status", -1);

        Log.i(TAG,"Speech , state == " + state);
        if(state == 0){
            return false;
        }else{
            return true;
        }
	}

}
