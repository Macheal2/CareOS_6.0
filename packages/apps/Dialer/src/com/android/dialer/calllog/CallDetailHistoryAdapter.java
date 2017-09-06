/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.dialer.calllog;

import android.content.Context;
import android.provider.CallLog.Calls;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.contacts.common.CallUtil;
import com.android.dialer.PhoneCallDetails;
import com.android.dialer.R;
import com.android.dialer.util.DialerUtils;
import com.google.common.collect.Lists;
import com.mediatek.dialer.ext.ExtensionManager;
import com.mediatek.dialer.util.DialerFeatureOptions;

import java.util.ArrayList;

//yuan tong qin add start
import android.widget.ImageView;
import com.android.dialer.care_os.I99Font;
import com.android.dialer.care_os.CallLogDateFormatHelper;
import android.util.Log;
// yuan tong qin add end
/**
 * Adapter for a ListView containing history items from the details of a call.
 */
public class CallDetailHistoryAdapter extends BaseAdapter {
    /** The top element is a blank header, which is hidden under the rest of the UI. */
    private static final int VIEW_TYPE_HEADER = 0;
    /** Each history item shows the detail of a call. */
    private static final int VIEW_TYPE_HISTORY_ITEM = 1;

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final CallTypeHelper mCallTypeHelper;
    private final PhoneCallDetails[] mPhoneCallDetails;

    /**
     * List of items to be concatenated together for duration strings.
     */
    private ArrayList<CharSequence> mDurationItems = Lists.newArrayList();

    public CallDetailHistoryAdapter(Context context, LayoutInflater layoutInflater,
            CallTypeHelper callTypeHelper, PhoneCallDetails[] phoneCallDetails) {
        mContext = context;
        mLayoutInflater = layoutInflater;
        mCallTypeHelper = callTypeHelper;
        mPhoneCallDetails = phoneCallDetails;
    }

    @Override
    public boolean isEnabled(int position) {
        // None of history will be clickable.
        return false;
    }

    @Override
    public int getCount() {
        return mPhoneCallDetails.length + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position == 0) {
            return null;
        }
        return mPhoneCallDetails[position - 1];
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return -1;
        }
        return position - 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_HISTORY_ITEM;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            final View header = convertView == null
                    ? mLayoutInflater.inflate(R.layout.call_detail_history_header, parent, false)
                    : convertView;
            return header;
        }

        // Make sure we have a valid convertView to start with
        final View result  = convertView == null
                ? mLayoutInflater.inflate(R.layout.call_detail_history_item, parent, false)
                : convertView;

        PhoneCallDetails details = mPhoneCallDetails[position - 1];
        // yuan tong qin add 
//        CallTypeIconsView callTypeIconView =
//                (CallTypeIconsView) result.findViewById(R.id.call_type_icon);
		// yuan tong qin add end
        TextView callTypeTextView = (TextView) result.findViewById(R.id.call_type_text);
        TextView dateView = (TextView) result.findViewById(R.id.date);
        TextView durationView = (TextView) result.findViewById(R.id.duration);
// yuan tong qin add textsize
        callTypeTextView.setTextSize(I99Font.TITLE);
        dateView.setTextSize(I99Font.TITLE);
        durationView.setTextSize(I99Font.TITLE);
// yuan tong qin add end        
        
        int callType = details.callTypes[0];
        boolean isVideoCall = (details.features & Calls.FEATURES_VIDEO) == Calls.FEATURES_VIDEO
                && CallUtil.isVideoEnabled(mContext);

// yuan tong qin add start        
//        callTypeIconView.clear();
//        callTypeIconView.add(callType);
//        callTypeIconView.setShowVideo(isVideoCall);

        /// would be changed, such as video state changed.
 //       callTypeIconView.requestLayout();
     // yuan tong qin add end 
        /// M: [Ip-Prefix] Show Ip-Prefix for IP call @{
        /*
        callTypeTextView.setText(mCallTypeHelper.getCallTypeText(callType, isVideoCall));
        */
        if (DialerFeatureOptions.IP_PREFIX && !TextUtils.isEmpty(details.ipPrefix)
                && callType == Calls.OUTGOING_TYPE) {
            String mIPOutgoingName = mContext
                    .getString(R.string.type_ip_outgoing, details.ipPrefix);
            callTypeTextView.setText(mIPOutgoingName);
        } else {
            callTypeTextView.setText(mCallTypeHelper.getCallTypeText(callType, isVideoCall));
        }
        //yuan tong qin add start 
        String data = CallLogDateFormatHelper.getFormatedDateText(mContext, details.date);
        callTypeTextView.setText(data);
		// yuan tong qin add end 
        
        /// @}
        // Set the date.  yuan tong qin add start 
        //   CharSequence dateValue = DateUtils.formatDateRange(mContext, details.date, details.date,
        //        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
        //      DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR);
        // Log.i("test", "===设置时间=="+dateValue);
       
        
        CharSequence  dateValue = DateUtils.formatDateRange(mContext, details.date, details.date,
                DateUtils.FORMAT_SHOW_TIME |DateUtils.FORMAT_SHOW_WEEKDAY);
	        dateView.setText(dateValue);
	    // yuan tong qin add end    
        // Set the duration
        if (Calls.VOICEMAIL_TYPE == callType || CallTypeHelper.isMissedCallType(callType)) {
            durationView.setVisibility(View.GONE);
        } else {
            durationView.setVisibility(View.VISIBLE);
            //yuan tong qin add start
            String cc=formatDurationAndDataUsage(details.duration, details.dataUsage).toString();
            int d=cc.indexOf("、");
            if(d>0){
            	  String dd=cc.substring(0,d);
                  durationView.setText(dd);
            }else{
            	durationView.setText(cc);
            }
            //yuan tong qin add end
          
        }

        /// M: for Plug-in @{
        ExtensionManager.getInstance().getCallDetailExtension().setDurationViewVisibility(
                durationView);
        /// @}
               
        return result;
    }

    private CharSequence formatDuration(long elapsedSeconds) {
        long minutes = 0;
        long seconds = 0;

        if (elapsedSeconds >= 60) {
            minutes = elapsedSeconds / 60;
            elapsedSeconds -= minutes * 60;
            seconds = elapsedSeconds;
            return mContext.getString(R.string.callDetailsDurationFormat, minutes, seconds);
        } else {
            seconds = elapsedSeconds;
            return mContext.getString(R.string.callDetailsShortDurationFormat, seconds);
        }
    }

    /**
     * Formats a string containing the call duration and the data usage (if specified).
     *
     * @param elapsedSeconds Total elapsed seconds.
     * @param dataUsage Data usage in bytes, or null if not specified.
     * @return String containing call duration and data usage.
     */
    private CharSequence formatDurationAndDataUsage(long elapsedSeconds, Long dataUsage) {
        CharSequence duration = formatDuration(elapsedSeconds);

        if (dataUsage != null) {
            mDurationItems.clear();
            mDurationItems.add(duration);
            mDurationItems.add(Formatter.formatShortFileSize(mContext, dataUsage));

            return DialerUtils.join(mContext.getResources(), mDurationItems);
        } else {
            return duration;
        }
    }
}
