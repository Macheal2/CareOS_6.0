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

package com.cappu.cleaner.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cappu.cleaner.R;

public class MonitorBall extends RelativeLayout {
    private Context mContext;
    private WaveView mWaveBall;
    private ArcProgress mArcProgress;
    private TextView mMCText, mBigText;
    private Button mMCImg;
    private int mAP_width;

    private ImageView mAnimView, mFinish;
    private Animation mRotateAnim;
    private int mState = -1;

    public final static int NOMAL = 0;
    public final static int SCANNING = 1;
    public final static int FINISH = 2;

    public MonitorBall(Context context) {
        super(context);
        mContext = context;
    }

    public MonitorBall(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.monitor_ball, this, true);
        mWaveBall = (WaveView) this.findViewById(R.id.wave_ball);
        mArcProgress = (ArcProgress) this.findViewById(R.id.arc_store);
        mMCText = (TextView) this.findViewById(R.id.monitor_content_text);
        mMCImg = (Button) this.findViewById(R.id.monitor_content);
        mAnimView = (ImageView) this.findViewById(R.id.view_anim);
        mRotateAnim= AnimationUtils.loadAnimation(mContext, R.anim.rotate_anim);
        mFinish = (ImageView) this.findViewById(R.id.health_finish);
        mBigText = (TextView) this.findViewById(R.id.monitor_big_text);
        setState(NOMAL);
    }

    private void initAnimViewSize(){
        if (mAnimView.getWidth() != mAP_width){
            ViewGroup.LayoutParams params;
            params = mAnimView.getLayoutParams();
            params.width = mAP_width;
            params.height = mAP_width;
            mAnimView.setLayoutParams(params);
        }
    }

    public void setProgress(int progress){
        mArcProgress.setProgress(progress);

    }

    public int getProgress(){
        return mArcProgress.getProgress();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e("hmq","onSizeChanged");
        if(mArcProgress != null && (mArcProgress.getWidth() != 0 && mAP_width != mArcProgress.getWidth())){
            Log.e("hmq","onSizeChanged layout="+mArcProgress.getWidth()+" "+mArcProgress.getLayoutParams().width);
            mAP_width = mArcProgress.getWidth();
            //initAnimViewSize();
        }
    }

    public int getState(){
        return mState;
    }

    public void setState(int state){
        if (state == mState) return;
        mState = state;

        switch(state){
            case SCANNING:
                setAnimRotateRun(true);
                mFinish.setVisibility(View.INVISIBLE);
                mBigText.setVisibility(View.INVISIBLE);
                mMCText.setVisibility(View.VISIBLE);
                mMCImg.setVisibility(View.VISIBLE);
                mMCImg.setText(R.string.str_optimization_cancel);
                break;
            case FINISH:
                setAnimRotateRun(false);
                mFinish.setVisibility(View.VISIBLE);
                if (mArcProgress.getProgress() > 80){
                    mMCText.setVisibility(View.INVISIBLE);
                    mMCImg.setVisibility(View.INVISIBLE);
                    mBigText.setVisibility(View.VISIBLE);
                    mBigText.setText(getResources().getString(R.string.str_health_great));
                }else{
                    mMCText.setVisibility(View.INVISIBLE);
                    mMCImg.setVisibility(View.VISIBLE);
                    mMCImg.setText(R.string.str_optimization_scheme);
                    mBigText.setVisibility(View.INVISIBLE);
                }
                break;
            case NOMAL:
                setAnimRotateRun(false);
                mFinish.setVisibility(View.INVISIBLE);
                mMCText.setVisibility(View.INVISIBLE);
                mMCImg.setVisibility(View.INVISIBLE);
                mBigText.setVisibility(View.VISIBLE);
                mBigText.setText(getResources().getString(R.string.str_health_repeat));
                break;
        }

        mArcProgress.setState(mState);
        invalidate();
    }

    public void setAnimRotateRun(boolean run) {
        if (mAnimView == null) return;

        if (run) {
            mAnimView.setVisibility(View.VISIBLE);

            if (mAnimView.getAnimation() == null) {
                //initAnimViewSize();
                mFinish.setVisibility(View.INVISIBLE);
                mAnimView.setBackgroundResource(R.drawable.img_monitor_scanning);
                mAnimView.startAnimation(mRotateAnim);
            } else if (!mAnimView.getAnimation().hasStarted()) {
                //initAnimViewSize();
                mAnimView.startAnimation(mRotateAnim);
            }
        } else {
            mAnimView.setVisibility(View.INVISIBLE);
            if (mAnimView.getAnimation() != null) {
                mAnimView.clearAnimation();
            }
        }

    }

    public void setVelueText(String str){
        mMCText.setText(str);
       // invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub

        Log.e("hmq", "onTouchEvent");
        postInvalidate();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
        Log.e("hmq","measureChild="+child.toString());
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
