package com.cappu.launcherwin.tools;

import android.content.Context;

import com.cappu.launcherwin.LauncherApplication;
import com.cappu.launcherwin.R;

public class AppAliasesTools {
    
    public static String replaceAppAliases(Context context,String packageName,String className,String defName){
        String as = null;
        if(packageName == null || className == null){
            return defName;
        }
        
        if(packageName.equals("com.android51111155.folder6723") && className.equals("com.uucun.android.folder.main.UUFolderActivity")){
            as = context.getString(R.string.alis_download_center);
        }else if(packageName.equals("com.android.gallery3d") && className.equals("com.android.gallery3d.app.Gallery")){
            as = context.getString(R.string.alis_album);
        }else if(packageName.equals("cn.etouch.ecalendar.chenovo") && className.equals("cn.etouch.ecalendar.tools.almanac.AlmanacActivity")){
            as = context.getString(R.string.alis_almanac);
        }else if(packageName.equals("com.android.settings") && className.equals("com.android.settings.Settings")){
            as = context.getString(R.string.android_system);
        }else if(packageName.equals("com.baidu.searchbox") && className.equals("com.baidu.searchbox.MainActivity")){
            as = context.getString(R.string.gooduse_baidu_search);
        }else if (packageName.equals("com.android.dialer") && className.equals("com.android.dialer.calllog.CallLogActivity")){
	    as = context.getString(R.string.gooduse_dialor);//通话记录->电话
	}        
        if(as == null){
            as = defName;
        }
        return as;
    }

}
