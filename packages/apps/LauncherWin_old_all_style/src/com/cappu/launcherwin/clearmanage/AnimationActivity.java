
package com.cappu.launcherwin.clearmanage;

import com.cappu.launcherwin.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class AnimationActivity extends Activity {
    private static final String TAG = "AnimationActivity";

    private static final int MESSAGE_ROTATE_FINISHED = 0;

    private static final int MESSAGE_UPDATE_WIDTH = 1;

    private static final int MESSAGE_FINISH = 2;

    private RelativeLayout mShortcut;

    private RelativeLayout mRelativeLayout;

    private Rect rect;

    private ImageView backImageView;

    private ImageView roateImageView;

    private TextView textView;
    
    private String mTips;

    private int mWidth;

    private static enum Direction {
        RIGHT, LEFT
    }

    private Direction direction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clear_manage);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        // 取得Lanucher传过来的所点击的快捷方式的矩形坐标。
        rect = intent.getSourceBounds();
        if (rect == null) {
            finish();

            return;
        }

        Log.d(TAG, rect.toShortString());

        mRelativeLayout = (RelativeLayout) findViewById(R.id.framelayout);
        mShortcut = (RelativeLayout) findViewById(R.id.shortcut);

        backImageView = (ImageView) findViewById(R.id.clean_back);
        roateImageView = (ImageView) findViewById(R.id.clean_rotate);
        // iconmageView = (ImageView) findViewById(R.id.clean_icon);
        textView = (TextView) findViewById(R.id.text);

        // DisplayMetrics dm = new DisplayMetrics();
        int width = getWindowManager().getDefaultDisplay().getWidth();
        int hight = getWindowManager().getDefaultDisplay().getHeight();

        Log.d(TAG, "width = " + width);
        Log.d(TAG, "hight = " + hight);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mShortcut.getLayoutParams();
        // layoutParams.topMargin = rect.top - (rect.bottom - rect.top) / 4;

        layoutParams.topMargin = rect.top;// + (rect.bottom - rect.top) / 4;

        //Log.i("HHJ", "layoutParams.topMargin:" + layoutParams.topMargin + "   rect.top:" + rect.top + "     rect.bottom:" + rect.bottom);

        // 判断快捷方式在屏幕的哪一边，如果在左边，伸缩动画就会向右，如果在右边，伸缩动画向左。
        if (rect.left < width / 2) {
            direction = Direction.RIGHT;
            layoutParams.leftMargin = rect.left;

        } else {
            direction = Direction.LEFT;
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.rightMargin = width - rect.right;
            Log.d(TAG, "rightMargin = " + (width - rect.right));
        }

        mRelativeLayout.updateViewLayout(mShortcut, layoutParams);
    }

    private Handler mHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MESSAGE_ROTATE_FINISHED:
                    mWidth = backImageView.getWidth();
                    Log.d(TAG, "mWidth = " + mWidth);
                    updateWidth();
                    roateImageView.clearAnimation();
                    roateImageView.setVisibility(View.INVISIBLE);
                    break;
                case MESSAGE_UPDATE_WIDTH:
                    updateWidth();
                    break;
                case MESSAGE_FINISH:
                    finish();
                    overridePendingTransition(0, 0);// 加这一句是为了不让界面消失的时候黑屏
                    break;

                default:
                    break;
            }

        };
    };

    private void updateWidth() {
        // 宽度没有达到原来宽度的2.5度，继续做动画
        if (backImageView.getWidth() <= 2.5f * mWidth) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) backImageView.getLayoutParams();
            // 每次增加20的宽度，可以自行设置，和用户体验有关系，可自行调整
            layoutParams.width = backImageView.getWidth() + 20;
            mShortcut.updateViewLayout(backImageView, layoutParams);
            // 继续发更新消息。也可发送delay消息，和用户体验有关系，可自行调整
            mHandler.sendEmptyMessage(MESSAGE_UPDATE_WIDTH);
        } else {
            textView.setText(mTips);
            textView.setVisibility(View.VISIBLE);

        }

    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 旋转动画开始
        roateImageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_anim));

        
        int count = ClearMemory();
        long AvailMemory = getAvailMemory(this);
        long TotalMemory = getTotalMemory(this);
        mTips = getString(R.string.totalmemory, TotalMemory)+"\n"+getString(R.string.availmemory, AvailMemory)+"\n"+getString(R.string.clearapp, count);
        // 假设垃圾清理了两秒钟，然后开如做伸缩动画。
        mHandler.sendEmptyMessageDelayed(MESSAGE_ROTATE_FINISHED, 2000);

        mHandler.sendEmptyMessageDelayed(MESSAGE_FINISH, 6000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRelativeLayout.setVisibility(View.GONE);
        finish();
    }

    private long getAvailMemory(Context context) {
        // 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        // mi.availMem; 当前系统的可用内存
        // return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
        return mi.availMem / (1024 * 1024);
    }

    private long getTotalMemory(Context context) {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i(str2, num + "\t");
            }
            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();

        } catch (IOException e) {
        }
        // return Formatter.formatFileSize(context, initial_memory);//
        // Byte转换为KB或者MB，内存大小规格化
        return initial_memory / (1024 * 1024);
    }

    public int ClearMemory() {
        ActivityManager activityManger = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
        int count = 0;
        //Log.i("HHJ", "ClearMemory:"+list.size());
        if (list != null)
            for (int i = 0; i < list.size(); i++) {
                ActivityManager.RunningAppProcessInfo apinfo = list.get(i);

                System.out.println("pid            " + apinfo.pid);
                System.out.println("processName              " + apinfo.processName);
                System.out.println("importance            " + apinfo.importance);
                String[] pkgList = apinfo.pkgList;

                //if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE) {
                    //apinfo.importance这个值越小说明apk的系统权限越高
                    // Process.killProcess(apinfo.pid);
                    count += pkgList.length;
                    for (int j = 0; j < pkgList.length; j++) {
                        // 2.2以上是过时的,请用killBackgroundProcesses代替
                        Log.i("HHJ", "apinfo.pid:"+apinfo.pid);
                        Log.i("HHJ", "apinfo.processName:"+apinfo.processName);
                        Log.i("HHJ", "apinfo.importance:"+apinfo.importance);
                        activityManger.restartPackage(pkgList[j]);
                        activityManger.killBackgroundProcesses(pkgList[j]);
                    }
                }
            }
        return count;
    }
}
