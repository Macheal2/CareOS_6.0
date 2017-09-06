package android.os;

import com.cappu.download.PushReceiver;
import com.cappu.download.PushTypeSharedPreferences;
import com.cappu.download.database.PushSettings;
import com.cappu.download.utils.PushConstants;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public class RealSystemFacade implements SystemFacade {
    private final static String TAG = "PushReceiver";
    private Context mContext;
    private NotificationManager mNotificationManager;
    // 2 GB
    private static final long DOWNLOAD_MAX_BYTES_OVER_MOBILE = 2 * 1024 * 1024 * 1024L;
    // 1 GB
    private static final long DOWNLOAD_RECOMMENDED_MAX_BYTES_OVER_MOBILE = 1024 * 1024 * 1024;

    public RealSystemFacade(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public Integer getActiveNetworkType() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w(PushConstants.TAG, "couldn't get connectivity manager");
            return null;
        }

        NetworkInfo activeInfo = connectivity.getActiveNetworkInfo();
        if (activeInfo == null) {
            if (PushConstants.LOGVV) {
                Log.v(PushConstants.TAG, "network is not available");
            }
            return null;
        }
        return activeInfo.getType();
    }

    public boolean isNetworkRoaming() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w(PushConstants.TAG, "couldn't get connectivity manager");
            return false;
        }

        NetworkInfo info = connectivity.getActiveNetworkInfo();
        boolean isMobile = (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE);
        final TelephonyManager mgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        boolean isRoaming = isMobile && mgr.isNetworkRoaming();
        //if (Constants.LOGVV && isRoaming) {
            Log.v(PushConstants.TAG, "network is roaming");
        //}
        return isRoaming;
    }

    public Long getMaxBytesOverMobile() {
        return DOWNLOAD_MAX_BYTES_OVER_MOBILE;
    }

    @Override
    public Long getRecommendedMaxBytesOverMobile() {
        return DOWNLOAD_RECOMMENDED_MAX_BYTES_OVER_MOBILE;
    }

    @Override
    public void sendBroadcast(Intent intent) {
        mContext.sendBroadcast(intent);
    }

    @Override
    public boolean userOwnsPackage(int uid, String packageName) throws NameNotFoundException {
        return mContext.getPackageManager().getApplicationInfo(packageName, 0).uid == uid;
    }

    @Override
    public void postNotification(long id, Notification notification) {
        /**
         * TODO: The system notification manager takes ints, not longs, as IDs,
         * but the download manager uses IDs take straight from the database,
         * which are longs. This will have to be dealt with at some point.
         */
        mNotificationManager.notify((int) id, notification);
    }

    @Override
    public void cancelNotification(long id) {
        mNotificationManager.cancel((int) id);
    }

    @Override
    public void cancelAllNotifications() {
        mNotificationManager.cancelAll();
    }

    @Override
    public void startThread(Thread thread) {
        thread.start();
    }
    
    /**在接下来wakeup毫秒内开始执行pushTypeValues的服务
     * wakeUp 毫秒
     * pushTypeValues 类型
     * */
    public void scheduleAlarm(long wakeUp,Context context,int pushTypeValues) {
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) {
            Log.e(TAG, "couldn't get alarm manager");
            return;
        }
        Intent intent = new Intent(PushConstants.PUSH_ACTION);
        intent.putExtra(PushSettings.PushType.PUSH_TYPE, pushTypeValues);
        intent.setClassName(context.getPackageName(), PushReceiver.class.getName());
        //PendingIntent 的四个参数中最后一个参数 
        //int     FLAG_CANCEL_CURRENT     如果已经存在 PendingIntent, 则会取消目前的 Intent 后再产生新的 Intent.
        //int     FLAG_NO_CREATE  如果并不存在 PendingIntent, 则传回 null.
        //int     FLAG_ONE_SHOT   此 PendingIntent 只能使用一次.
        //int     FLAG_UPDATE_CURRENT     如果已存在 PendingIntent, 则更新 extra data.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        Log.e(TAG, wakeUp+"毫秒后执行  "+ PushTypeSharedPreferences.getTypeCHName(context,pushTypeValues)+"   推送");
        if(wakeUp > 90*60*1000){//如果超过一个半小时的就一个半小时后执行
            wakeUp = 60*1000;//方便测试才打开的
        }
        alarms.set(AlarmManager.RTC_WAKEUP, currentTimeMillis() + wakeUp, pendingIntent);
        
    }
    
    
    
}
