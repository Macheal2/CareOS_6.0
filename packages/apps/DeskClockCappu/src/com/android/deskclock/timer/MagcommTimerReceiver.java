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

package com.android.deskclock.timer;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.deskclock.DeskClock;
import com.android.deskclock.R;
import com.android.deskclock.TimerRingService;
import com.android.deskclock.Utils;
import com.android.deskclock.alarms.AlarmService;
import com.android.deskclock.timer.Timers;

import java.util.ArrayList;
import java.util.Iterator;

public class MagcommTimerReceiver extends BroadcastReceiver {
	
    private static final int NOTIFICATION_ID = R.drawable.stat_notify_timer;
    private static final int NOTIFICATION_UP_ID = 2;
    @Override
    public void onReceive(final Context context, final Intent intent) {
    	
        String actionType = intent.getAction();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int mPageIndex=prefs.getInt(Timers.TIMER_FRAGMENT_PAGE,Timers.PAGE_SETUP_TIMER);
        
        if (Timers.NOTIF_TIMES_SHOW.equals(actionType)) {
            showTimesUpNotification(context);
            return;
        } else if (Timers.NOTIF_TIMES_CANCEL.equals(actionType)) {
        	cancelTimesUpNotification(context);
            return;
        }else if (Timers.NOTIF_TIMES_STOP.equals(actionType)) {  	
        	cancelTimesUpNotification(context);
        	MagcommTimerAlertActivity.stopRingtone();
            MagcommTimerAlertActivity.instance.finish();
            return;
        }
        
        if(mPageIndex==Timers.PAGE_SETUP_TIMER){
        	cancelNotification(context);
        	return;
        }
        
        if (Timers.NOTIF_SHOW.equals(actionType)) {
            showNotification(context);
            return;
        } else if (Timers.NOTIF_CANCEL.equals(actionType)) {
            cancelNotification(context);
            return;
        } 
        
    }
    
    private void showNotification(final Context context) {
    	
        Long mBroadcastTime = null;
        String title;
        long now = Utils.getTimeNow();
        
    	Intent activityIntent = new Intent(context, DeskClock.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.putExtra(DeskClock.SELECT_TAB_INTENT_EXTRA, DeskClock.TIMER_TAB_INDEX);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
   
        long lastMarkedTime = prefs.getLong(Timers.TIMER_MARK, 0);
        long completionTime = prefs.getLong(Timers.TIMER_REMAINDER_MILLISECS, 0);
        long time = completionTime - (now-lastMarkedTime);
        boolean mState=prefs.getBoolean(Timers.TIMER_RUN_STATE, false);
        if (time > Timers.MINUTE_IN_MILLIS) {
        	mBroadcastTime = getBroadcastTime(now, time);
        }
       
        if(mState){
        	title=context.getString(R.string.timer_notification_label);
        }else{
        	title=context.getString(R.string.timer_stopped);
        }
        
        Notification.Builder builder = new Notification.Builder(context)
        .setAutoCancel(false)
        .setContentTitle(title)
        .setContentText(buildTimeRemaining(context,time))
        .setDeleteIntent(pendingIntent)
        .setOngoing(true)
        .setPriority(Notification.PRIORITY_HIGH)
        .setShowWhen(false)
		.setVisibility(Notification.VISIBILITY_PUBLIC)
        .setLocalOnly(true)
        .setSmallIcon(R.drawable.stat_notify_timer);
     

        Notification notification = builder.build();
        notification.contentIntent = pendingIntent;
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
        
        if ((mBroadcastTime != null)&&(mState)) {            
	        Intent mBroadcast = new Intent();
	        mBroadcast.setAction(Timers.NOTIF_SHOW);
	        PendingIntent pendingBroadcast =
	                PendingIntent.getBroadcast(context, 0, mBroadcast, 0);
	        AlarmManager alarmManager =(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	        alarmManager.set(AlarmManager.ELAPSED_REALTIME, mBroadcastTime, pendingBroadcast);
        }
    }
    

    private long getBroadcastTime(long now, long timeUntilBroadcast) {
        long seconds = timeUntilBroadcast / 1000;
        seconds = seconds - ( (seconds / 60) * 60 );
        return now + (seconds * 1000);
    }
    
    private String buildTimeRemaining(Context context, long time) {
        if (time < 0) {         
            return null;
        }

        long hundreds, seconds, minutes, hours;
        seconds = time / 1000;
        minutes = seconds / 60;
        seconds = seconds - minutes * 60;
        hours = minutes / 60;
        minutes = minutes - hours * 60;
        if (hours > 99) {
            hours = 0;
        }

        String hourSeq = (hours == 0) ? "" :
            ( (hours == 1) ? context.getString(R.string.hour) :
                context.getString(R.string.hours, Long.toString(hours)) );
        String minSeq = (minutes == 0) ? "" :
            ( (minutes == 1) ? context.getString(R.string.minute) :
                context.getString(R.string.minutes, Long.toString(minutes)) );

        boolean dispHour = hours > 0;
        boolean dispMinute = minutes > 0;
        int index = (dispHour ? 1 : 0) | (dispMinute ? 2 : 0);
        String[] formats = context.getResources().getStringArray(R.array.timer_notifications);
        return String.format(formats[index], hourSeq, minSeq);
    }

  
    private void cancelNotification(final Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
    

    private void showTimesUpNotification(final Context context) {
        // Content Intent. When clicked will show the timer full screen
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MagcommTimerAlertActivity.class).putExtra(
                        Timers.TIMER_INTENT_EXTRA, 0),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Add stop/done action button
        PendingIntent stopIntent = PendingIntent.getBroadcast(context, 0,
                new Intent(Timers.NOTIF_TIMES_STOP)
                        .putExtra(Timers.TIMER_INTENT_EXTRA, 0),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification creation
        Notification notification = new Notification.Builder(context)
                .setContentIntent(contentIntent)
                .addAction(
                         R.drawable.ic_notify_stop,
                        context.getResources().getString(R.string.ok),
                        stopIntent)
                .setContentTitle(context.getString(R.string.timer_notification_label))
                .setContentText(context.getResources().getString(R.string.timer_end_title))
                .setSmallIcon(R.drawable.stat_notify_timer)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setWhen(0)
                .setCategory(Notification.CATEGORY_ALARM)
				.setVisibility(Notification.VISIBILITY_PUBLIC)
                .setLocalOnly(true)
                .build();

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(
        		NOTIFICATION_UP_ID, notification);
       
    }


    private void cancelTimesUpNotification(final Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_UP_ID);
    }
}
