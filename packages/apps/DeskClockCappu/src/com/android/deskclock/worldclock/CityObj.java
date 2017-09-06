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

package com.android.deskclock.worldclock;

import java.util.ArrayList;

import com.android.deskclock.worldclock.HanziToPinyin.Token;

import android.content.SharedPreferences;

public class CityObj {

    private static final String CITY_NAME = "city_name_";
    private static final String CITY_TIME_ZONE = "city_tz_";
    private static final String CITY_ID = "city_id_";
    private static final String CITY_NAME_FULL_PY = "city_name_full_py_";
    private static final String CITY_NAME_FIRST_PY = "city_name_first_py_";

    public String mCityName;
    public String mTimeZone;
    public String mCityId;
    public String mCityNamePinYin;
    public String mCityNameFPinYin;
    public boolean isHeader;

    public CityObj(String name, String timezone, String id) {
        mCityName = name;
        mTimeZone = timezone;
        mCityId = id;
        mCityNamePinYin=getFullPinYin(name);
        mCityNameFPinYin=getFirstPinYin(name);
        
    }

    @Override
    public String toString() {
        return "CityObj{" +
                "name=" + mCityName +
                ", timezone=" + mTimeZone +
                ", id=" + mCityId +
                '}';
    }

    public CityObj(SharedPreferences prefs, int index) {
        mCityName = prefs.getString(CITY_NAME + index, null);
        mTimeZone = prefs.getString(CITY_TIME_ZONE + index, null);
        mCityId = prefs.getString(CITY_ID + index, null);
        mCityNamePinYin= prefs.getString(CITY_NAME_FULL_PY + index, null);
        mCityNameFPinYin= prefs.getString(CITY_NAME_FIRST_PY + index, null);
    }

    public void saveCityToSharedPrefs(SharedPreferences.Editor editor, int index) {
        editor.putString (CITY_NAME + index, mCityName);
        editor.putString (CITY_TIME_ZONE + index, mTimeZone);
        editor.putString (CITY_ID + index, mCityId);
        editor.putString (CITY_NAME_FULL_PY + index, mCityNamePinYin);
        editor.putString (CITY_NAME_FIRST_PY + index, mCityNameFPinYin);
    }
    
    public String getFullPinYin(String source){
    	ArrayList<Token> tokens = HanziToPinyin.getInstance().getTokens(source);
    	if (tokens == null || tokens.size() == 0) {
    		return source;
    	}
    	
    	StringBuffer result = new StringBuffer();
    	for (Token token : tokens) {
    		if (token.type == Token.PINYIN) {
    			result.append(token.target);
    		} else {
    			result.append(token.source);
    		}
    	}
    	return result.toString();
   }
    	
    public String getFirstPinYin(String source){
    	ArrayList<Token> tokens = HanziToPinyin.getInstance().getTokens(source);
    	if (tokens == null || tokens.size() == 0) {
    		return source;
    	}
    	
    	StringBuffer result = new StringBuffer();
    	for (Token token : tokens) {
    		if (token.type == Token.PINYIN) {
    			result.append(token.target.charAt(0));
    		} else {
    			result.append(token.source);
    		}
    	}
    	return result.toString();
    }
}
