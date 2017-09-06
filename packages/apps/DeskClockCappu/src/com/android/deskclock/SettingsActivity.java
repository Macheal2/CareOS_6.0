/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.deskclock;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.deskclock.worldclock.Cities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import com.cappu.widget.TopBar;

/**
 * Settings for the Alarm Clock.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY_ALARM_SNOOZE =
            "snooze_duration";
    public static final String KEY_VOLUME_BEHAVIOR =
            "volume_button_setting";
    public static final String KEY_AUTO_SILENCE =
            "auto_silence";
    public static final String KEY_CLOCK_STYLE =
            "clock_style";
    public static final String KEY_HOME_TZ =
            "home_time_zone";
    public static final String KEY_AUTO_HOME_CLOCK =
            "automatic_home_clock";
    public static final String KEY_VOLUME_BUTTONS =
            "volume_button_setting";

    public static final String DEFAULT_VOLUME_BEHAVIOR = "0";
    public static final String VOLUME_BEHAVIOR_SNOOZE = "1";
    public static final String VOLUME_BEHAVIOR_DISMISS = "2";


    private long mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_ALARM);

        setContentView(R.layout.magcomm_setting);
        
        addPreferencesFromResource(R.xml.settings);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        	actionBar.hide();
        }
        
        PreferenceHead ph = new PreferenceHead(this);
        ph.setOnBackButtonClickListener(new OnClickListener() {
         
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ph.setOrder(0);
        getPreferenceScreen().addPreference(ph);
        // We don't want to reconstruct the timezone list every single time
        // onResume() is called so we do it once in onCreate
        ///M:just refresh the timezone, don't care about the location or others
//        ListPreference listPref;
//        listPref = (ListPreference) findPreference(KEY_HOME_TZ);
        mTime = System.currentTimeMillis();
        ///M: no need use as static
//        CharSequence[][] mTimezones = getAllTimezones();
//        listPref.setEntryValues(mTimezones[0]); //GMT
//        listPref.setEntries(mTimezones[1]); //Cities
//        listPref.setSummary(listPref.getEntry());
//        listPref.setOnPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //getWindow().getDecorView().setBackgroundColor(Utils.getCurrentHourColor());//dengjianzhang@20150521 start 
        refresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        MenuItem help = menu.findItem(R.id.menu_item_help);
        if (help != null) {
            Utils.prepareHelpMenuItem(this, help);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        if (KEY_AUTO_SILENCE.equals(pref.getKey())) {
            final ListPreference listPref = (ListPreference) pref;
            String delay = (String) newValue;
            updateAutoSnoozeSummary(listPref, delay);
//dengjianzhang@20150521 start 
//        } else if (KEY_CLOCK_STYLE.equals(pref.getKey())) {
//            final ListPreference listPref = (ListPreference) pref;
//            final int idx = listPref.findIndexOfValue((String) newValue);
//            listPref.setSummary(listPref.getEntries()[idx]);
//dengjianzhang@20150521 end
//        } else if (KEY_HOME_TZ.equals(pref.getKey())) {
//            final ListPreference listPref = (ListPreference) pref;
//            final int idx = listPref.findIndexOfValue((String) newValue);
//            listPref.setSummary(listPref.getEntries()[idx]);
//            notifyHomeTimeZoneChanged();
//        } else if (KEY_AUTO_HOME_CLOCK.equals(pref.getKey())) {
//            boolean state =((CheckBoxPreference) pref).isChecked();
//            Preference homeTimeZone = findPreference(KEY_HOME_TZ);
//            homeTimeZone.setEnabled(!state);
//            notifyHomeTimeZoneChanged();
        } else if (KEY_VOLUME_BUTTONS.equals(pref.getKey())) {
            final ListPreference listPref = (ListPreference) pref;
            final int idx = listPref.findIndexOfValue((String) newValue);
            listPref.setSummary(listPref.getEntries()[idx]);
        }
        return true;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        // Exported activity but no headers we support.
        return false;
    }

    private void updateAutoSnoozeSummary(ListPreference listPref,
            String delay) {
        int i = Integer.parseInt(delay);
        if (i == -1) {
            listPref.setSummary(R.string.auto_silence_never);
        } else {
            listPref.setSummary(getString(R.string.auto_silence_summary, i));
        }
    }

    private void notifyHomeTimeZoneChanged() {
        Intent i = new Intent(Cities.WORLDCLOCK_UPDATE_INTENT);
        sendBroadcast(i);
    }


    private void refresh() {
        ListPreference listPref = (ListPreference) findPreference(KEY_AUTO_SILENCE);
        listPref.setLayoutResource(R.layout.magcomm_preference);
        String delay = listPref.getValue();
        updateAutoSnoozeSummary(listPref, delay);
        listPref.setOnPreferenceChangeListener(this);
//dengjianzhang@20150521 start 
//        listPref = (ListPreference) findPreference(KEY_CLOCK_STYLE);
//        listPref.setSummary(listPref.getEntry());
//        listPref.setOnPreferenceChangeListener(this);
//dengjianzhang@20150521 end      
//        Preference pref = findPreference(KEY_AUTO_HOME_CLOCK);
//        boolean state = ((CheckBoxPreference) pref).isChecked();
//        pref.setOnPreferenceChangeListener(this);
//
//        listPref = (ListPreference)findPreference(KEY_HOME_TZ);
//        listPref.setEnabled(state);
//        listPref.setSummary(listPref.getEntry());

        listPref = (ListPreference) findPreference(KEY_VOLUME_BUTTONS);
        listPref.setLayoutResource(R.layout.magcomm_preference);
        listPref.setSummary(listPref.getEntry());
        listPref.setOnPreferenceChangeListener(this);

        SnoozeLengthDialog snoozePref = (SnoozeLengthDialog) findPreference(KEY_ALARM_SNOOZE);
        snoozePref.setSummary();
    }

    private class TimeZoneRow implements Comparable<TimeZoneRow> {
        private static final boolean SHOW_DAYLIGHT_SAVINGS_INDICATOR = false;

        public final String mId;
        public final String mDisplayName;
        public final int mOffset;

        public TimeZoneRow(String id, String name) {
            mId = id;
            TimeZone tz = TimeZone.getTimeZone(id);
            boolean useDaylightTime = tz.useDaylightTime();
            mOffset = tz.getOffset(mTime);
            mDisplayName = buildGmtDisplayName(id, name, useDaylightTime);
        }

        @Override
        public int compareTo(TimeZoneRow another) {
            return mOffset - another.mOffset;
        }

        public String buildGmtDisplayName(String id, String displayName, boolean useDaylightTime) {
            int p = Math.abs(mOffset);
            StringBuilder name = new StringBuilder("(GMT");
            name.append(mOffset < 0 ? '-' : '+');

            name.append(p / DateUtils.HOUR_IN_MILLIS);
            name.append(':');

            int min = p / 60000;
            min %= 60;

            if (min < 10) {
                name.append('0');
            }
            name.append(min);
            name.append(") ");
            name.append(displayName);
            if (useDaylightTime && SHOW_DAYLIGHT_SAVINGS_INDICATOR) {
                name.append(" \u2600"); // Sun symbol
            }
            return name.toString();
        }
    }


    /**
     * Returns an array of ids/time zones. This returns a double indexed array
     * of ids and time zones for Calendar. It is an inefficient method and
     * shouldn't be called often, but can be used for one time generation of
     * this list.
     *
     * @return double array of tz ids and tz names
     */
    public CharSequence[][] getAllTimezones() {
        Resources resources = this.getResources();
        String[] ids = resources.getStringArray(R.array.timezone_values);
        String[] labels = resources.getStringArray(R.array.timezone_labels);
        int minLength = ids.length;
        if (ids.length != labels.length) {
            minLength = Math.min(minLength, labels.length);
            LogUtils.e("Timezone ids and labels have different length!");
        }
        List<TimeZoneRow> timezones = new ArrayList<TimeZoneRow>();
        for (int i = 0; i < minLength; i++) {
            timezones.add(new TimeZoneRow(ids[i], labels[i]));
        }
        Collections.sort(timezones);

        CharSequence[][] timeZones = new CharSequence[2][timezones.size()];
        int i = 0;
        for (TimeZoneRow row : timezones) {
            timeZones[0][i] = row.mId;
            timeZones[1][i++] = row.mDisplayName;
        }
        return timeZones;
    }
    
    public class PreferenceHead extends Preference {
    	 
        private OnClickListener onBackButtonClickListener;
     
        public PreferenceHead(Context context) {
            super(context);
            setLayoutResource(R.layout.magcomm_topbar);
        }
     
        @Override
        protected void onBindView(View view) {
            super.onBindView(view);
            TopBar topBar = (TopBar) view.findViewById(R.id.topbar);
            topBar.setOnTopBarListener(
                new TopBar.onTopBarListener(){
                    @Override
                    public void onLeftClick(View v){
                        if (onBackButtonClickListener != null) {
                            onBackButtonClickListener.onClick(v);
                        }
                    }

                    @Override
                    public void onRightClick(View v){}

                    @Override
                    public void onTitleClick(View v){}
                }
            );
        }
     
        public void setOnBackButtonClickListener(OnClickListener onClickListener) {
            this.onBackButtonClickListener = onClickListener;
        }
    }
}
