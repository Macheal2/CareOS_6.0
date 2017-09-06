/*
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

package com.magcomm.keyguard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R;

public class MonitorBall extends RelativeLayout{
    private Context mContext;
    private WaveView mWaveBall;
    private TextView mBatteryLevel, mChargingText;
    private int mViewWidth, mViewHeight;
    
    public MonitorBall(Context context) {
        super(context);
        mContext = context;
    }
    
    public MonitorBall(Context context,AttributeSet attrs){
        super(context, attrs);
        mContext = context;
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.monitor_ball, this, true);
        mWaveBall = (WaveView) this.findViewById(R.id.wave_ball);
        mBatteryLevel = (TextView) this.findViewById(R.id.battery_level);
        mChargingText = (TextView) this.findViewById(R.id.charging_battery_text);
        Typeface tf = getClockTypeface(MagcommLockscreen.CAPPU_REGULAR_FONT_STYLE);
        if (mBatteryLevel != null && tf != null) {
            mBatteryLevel.setTypeface(tf);
        }
        tf = getClockTypeface(MagcommLockscreen.CAPPU_MED_FONT_STYLE);
        if (mChargingText != null && tf != null) {
            mChargingText.setTypeface(tf);
        }
        
        KeyguardUpdateMonitor.getInstance(context).registerCallback(mUpdateMonitor);
        //setWillNotDraw(false);
    }
    
    private Typeface getClockTypeface(String file) {
        return Typeface.createFromFile(file);
    }
    
    @Override
    public void invalidate() {
        // TODO Auto-generated method stub
        super.invalidate();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        
        Log.e("hmq","onTouchEvent");
        postInvalidate();
        return super.onTouchEvent(event);
    }

    KeyguardUpdateMonitorCallback mUpdateMonitor = new KeyguardUpdateMonitorCallback() {
        @Override
        public void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus status) {
            
            boolean isChargingOrFull = status.status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status.status == BatteryManager.BATTERY_STATUS_FULL;
        
            if (!(status.isPluggedIn() && isChargingOrFull)){
                setVisibility(View.INVISIBLE);
                return;
            }
            
            setVisibility(View.VISIBLE);
            if (status.isBatteryLow()){
                mWaveBall.setShowWarningColor(true);
            }else{
                mWaveBall.setShowWarningColor(false);
            }
            
            double level = status.level > 100 ? 100 : status.level < 5 ? 5 : status.level;
            mWaveBall.setPercent(level / 100);
            if (status.isCharged()) {
                mBatteryLevel.setText(mContext.getResources().getString(R.string.keyguard_charged));
                mChargingText.setVisibility(View.GONE);
            } else {
                String level_str = status.level + "%";
                mBatteryLevel.setText(level_str);
                if (mChargingText.getVisibility() == View.GONE){
                    mChargingText.setVisibility(View.VISIBLE);
                }
            }
        }
    };
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = mWaveBall.getHeight();
        setMeasuredDimension(height, height);//重新定义view大小
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.dispatchDraw(canvas);
    }
    
    @Override
    public void draw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.draw(canvas);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
    }
}
