
package com.cappu.launcherwin.tools;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.install.APKInstallTools;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;


public class AppComponentNameReplace {
    
    private Context mContext;
    public AppComponentNameReplace(Context context){
        this.mContext = context;
    }
    
    public ComponentName Replace(String packageName,String className){
        /*兼容 联系人 ComponentName 替换*/
        ComponentName cn = null;
        if (packageName.equals("com.android.contacts") && className.equals("com.android.contacts.activities.PeopleActivity")) {
            /* 版本选择 1 晨想老人机    2晨想老人机线上版本    3 派信老人机版本*/
            if(BasicKEY.LAUNCHER_VERSION == 2){
                cn = new ComponentName("com.cappu.launcherwin", "com.cappu.launcherwin.contacts.ContactListMultiChoiceActivity");
            }
            /*if (APKInstallTools.checkApkInstall(mContext, "com.android.contacts", "com.android.contacts.activities.PeopleActivity")) {
                cn = new ComponentName("com.android.contacts", "com.android.contacts.activities.PeopleActivity");
            }else{
                cn = new ComponentName("com.cappu.launcherwin", "com.cappu.launcherwin.contacts.ContactListMultiChoiceActivity");
            }*/
        }
        
        /*兼容 Camera ComponentName 替换*/
        if (packageName.equals("com.android.gallery3d") && className.equals("com.android.camera.CameraLauncher")) {
            if (APKInstallTools.checkApkInstall(mContext, "com.motorola.Camera", "com.motorola.Camera.Camera")) {
                cn = new ComponentName("com.motorola.Camera", "com.motorola.Camera.Camera");
            } else if (APKInstallTools.checkApkInstall(mContext, "com.android.camera", "com.android.camera.CameraEntry")) {
                cn = new ComponentName("com.android.camera", "com.android.camera.CameraEntry");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sec.android.app.camera", "com.sec.android.app.camera.Camera")) {
                cn = new ComponentName("com.sec.android.app.camera", "com.sec.android.app.camera.Camera");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.google.android.camera", "com.android.camera.Camera")) {
                cn = new ComponentName( "com.google.android.camera", "com.android.camera.Camera");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.camera", "com.android.camera.Camera")) {
                cn = new ComponentName("com.android.camera", "com.android.camera.Camera");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sonyericsson.android.camera", "com.sonyericsson.android.camera.CameraActivity")) {
                cn = new ComponentName( "com.sonyericsson.android.camera", "com.sonyericsson.android.camera.CameraActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.oppo.camera", "com.oppo.camera.Camera")) {
                cn = new ComponentName("com.oppo.camera", "com.oppo.camera.Camera");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.gallery3d", "com.android.hwcamera")) {
                cn = new ComponentName("com.android.gallery3d", "com.android.hwcamera");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.oppo.camera", "com.oppo.camera.activity.CameraActivity")) {
                cn = new ComponentName("com.oppo.camera", "com.oppo.camera.activity.CameraActivity");
            }
        }
        
        /*兼容 Music ComponentName 替换  ", "  */
        if (packageName.equals("com.cappu.strongmusic") && className.equals("com.cappu.activity.StrongMusicLoading")) {
            if (APKInstallTools.checkApkInstall(mContext, "com.android.music", "com.android.music.MusicBrowserActivity")) {
                cn = new ComponentName("com.android.music", "com.android.music.MusicBrowserActivity");
            } else if (APKInstallTools.checkApkInstall(mContext, "com.htc.music", "com.htc.music.HtcMusic")) {
                cn = new ComponentName( "com.htc.music", "com.htc.music.HtcMusic");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sec.android.app.music", "com.sec.android.app.music.list.activity.MpMainTabActivity")) {
                cn = new ComponentName( "com.sec.android.app.music", "com.sec.android.app.music.list.activity.MpMainTabActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.music", "com.android.music.MusicBrowserActivity")) {
                cn = new ComponentName("com.android.music", "com.android.music.MusicBrowserActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.music", "com.android.music.list.activity.MpMainTabActivity")) {
                cn = new ComponentName("com.android.music", "com.android.music.list.activity.MpMainTabActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.oppo.music", "com.oppo.music.MainListActivity")) {
                cn = new ComponentName("com.oppo.music", "com.oppo.music.MainListActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sec.android.app.music", "com.samsung.musicplus.MusicMainActivity")) {
                cn = new ComponentName("com.sec.android.app.music", "com.samsung.musicplus.MusicMainActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.mediacenter", "com.android.mediacenter.PageActivity")) {
                cn = new ComponentName("com.android.mediacenter", "com.android.mediacenter.PageActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.lewa.player", "com.lewa.player.ui.outer.MusicMainEntryActivity")) {
                cn = new ComponentName( "com.lewa.player", "com.lewa.player.ui.outer.MusicMainEntryActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.miui.player", "com.miui.player.ui.MusicBrowserActivity")) {
                cn = new ComponentName("com.miui.player", "com.miui.player.ui.MusicBrowserActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.bbkmusic", "com.android.bbkmusic.WidgetToTrackActivity")) {
                cn = new ComponentName("com.android.bbkmusic", "com.android.bbkmusic.WidgetToTrackActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.oppo.music", "com.oppo.music.MediaPlaybackActivity")) {
                cn = new ComponentName("com.oppo.music", "com.oppo.music.MediaPlaybackActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sec.android.app.music", "com.sec.android.app.music.MusicActionTabActivity")) {
                cn = new ComponentName("com.sec.android.app.music", "com.sec.android.app.music.MusicActionTabActivity");
            }
        }
        
        /*兼容 计算器 ComponentName 替换  ", "  */
        if (packageName.equals("com.android.calculator2") && className.equals("com.android.calculator2.Calculator")) {
            if (APKInstallTools.checkApkInstall(mContext, "com.sec.android.app.calculator", "com.sec.android.app.calculator.Calculator")) {
                cn = new ComponentName( "com.sec.android.app.calculator", "com.sec.android.app.calculator.Calculator");
            } else if (APKInstallTools.checkApkInstall(mContext, "com.sec.android.app.popupcalculator", "com.sec.android.app.popupcalculator.Calculator")) {
                cn = new ComponentName("com.sec.android.app.popupcalculator", "com.sec.android.app.popupcalculator.Calculator");
            } else if (APKInstallTools.checkApkInstall(mContext, "com.android.bbkcalculator", "com.android.bbkcalculator.Calculator")) {
                cn = new ComponentName("com.android.bbkcalculator", "com.android.bbkcalculator.Calculator");
            }
        }
        
        /*兼容 拨号 ComponentName 替换  ", "  */
        if (packageName.equals("com.android.contacts") && className.equals("com.cappu.contacts.I99CallLogs")) {
            if (APKInstallTools.checkApkInstall(mContext, "com.android.contacts", "com.android.contacts.DialtactsActivity")) {
                cn = new ComponentName("com.android.contacts", "com.android.contacts.DialtactsActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.htcdialer", "com.android.htcdialer.Dialer")) {
                cn = new ComponentName("com.android.htcdialer", "com.android.htcdialer.Dialer");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sec.android.app.dialertab", "com.sec.android.app.dialertab.DialerTabActivity")) {
                cn = new ComponentName("com.sec.android.app.dialertab", "com.sec.android.app.dialertab.DialerTabActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sonyericsson.android.socialphonebook", "com.sonyericsson.android.socialphonebook.DialerEntryActivity")) {
                cn = new ComponentName("com.sonyericsson.android.socialphonebook", "com.sonyericsson.android.socialphonebook.DialerEntryActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.contacts", "com.android.dialer.DialtactsActivity")) {
                cn = new ComponentName("com.android.contacts", "com.android.dialer.DialtactsActivity");
            }
        }
        
        
        /*兼容 短信MMS ComponentName 替换  ", "  */
        if (packageName.equals("com.android.mms") && className.equals("com.android.mms.ui.BootActivity")) {
            if (APKInstallTools.checkApkInstall(mContext, "com.android.mms", "com.android.mms.ui.ConversationList")) {
                cn = new ComponentName("com.android.mms", "com.android.mms.ui.ConversationList");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sonyericsson.conversations", "com.sonyericsson.conversations.ui.ConversationListActivity")) {
                cn = new ComponentName("com.sonyericsson.conversations", "com.sonyericsson.conversations.ui.ConversationListActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.mms", "com.android.mms.ui.ConversationComposer")) {
                cn = new ComponentName("com.android.mms", "com.android.mms.ui.ConversationComposer");
            }
        }
        
        /*兼容 日历 ComponentName 替换  ", "  */
        if (packageName.equals("cn.etouch.ecalendar.chenovo") && className.equals("cn.etouch.ecalendar.tools.almanac.AlmanacActivity")) {
            if(APKInstallTools.checkApkInstall(mContext, "cn.etouch.ecalendar.chenovo", "cn.etouch.ecalendar.tools.almanac.AlmanacActivity")){
                cn = new ComponentName("cn.etouch.ecalendar.chenovo", "cn.etouch.ecalendar.tools.almanac.AlmanacActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.calendar", "com.android.calendar.LaunchActivity")) {
                cn = new ComponentName("com.android.calendar", "com.android.calendar.LaunchActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.htc.calendar", "com.htc.calendar.LaunchActivity")) {
                cn = new ComponentName("com.htc.calendar", "com.htc.calendar.LaunchActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.google.android.calendar", "com.android.calendar.LaunchActivity")) {
                cn = new ComponentName("com.google.android.calendar", "com.android.calendar.LaunchActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.when.android.calendar365", "com.when.android.calendar365.CalendarMain")) {
                cn = new ComponentName( "com.when.android.calendar365", "com.when.android.calendar365.CalendarMain");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sec.android.app.popupcalculator", "com.sec.android.app.popupcalculator.Calculator")) {
                cn = new ComponentName("com.sec.android.app.popupcalculator", "com.sec.android.app.popupcalculator.Calculator");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.bbk.calendar", "com.bbk.calendar.MainActivity")) {
                cn = new ComponentName("com.bbk.calendar", "com.bbk.calendar.MainActivity");
            }
            
        }
        
        /*兼容 设置Setting ComponentName 替换  ", "  */
        if (packageName.equals("com.android.settings") && className.equals("com.android.settings.Settings")) {
            Log.i("HHJ", "兼容 设置Setting ComponentName 替换 :");
            if (APKInstallTools.checkApkInstall(mContext, "com.android.settings", "com.android.settings.framework.activity.HtcSettings")) {
                cn = new ComponentName("com.android.settings", "com.android.settings.framework.activity.HtcSettings");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.settings", "com.android.settings.GridSettings")) {
                cn = new ComponentName("com.android.settings", "com.android.settings.GridSettings");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.settings", "com.android.settings.HWSettings")) {
                cn = new ComponentName("com.android.settings", "com.android.settings.HWSettings");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.settings", "com.android.settings.Settings")) {
                cn = new ComponentName("com.android.settings", "com.android.settings.Settings");
            }
        }
        
        /*兼容 相册 ComponentName 替换  ", "  */
        if (packageName.equals("com.android.gallery3d") && className.equals("com.android.gallery3d.app.Gallery")) {
            if (APKInstallTools.checkApkInstall(mContext, "com.sec.android.gallery3d", "com.sec.android.gallery3d.app.GalleryActivity")) {
                cn = new ComponentName("com.sec.android.gallery3d", "com.sec.android.gallery3d.app.GalleryActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.oppo.gallery3d", "com.oppo.gallery3d.app.Gallery")) {
                cn = new ComponentName("com.oppo.gallery3d", "com.oppo.gallery3d.app.Gallery");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.miui.gallery", "com.miui.gallery.app.Gallery")) {
                cn = new ComponentName("com.miui.gallery", "com.miui.gallery.app.Gallery");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.htc.album", "com.htc.album.AlbumMain.ActivityMainDropList")) {
                cn = new ComponentName("com.htc.album", "com.htc.album.AlbumMain.ActivityMainDropList");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sonyericsson.album", "com.sonyericsson.album.MainActivity")) {
                cn = new ComponentName("com.sonyericsson.album", "com.sonyericsson.album.MainActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.gallery3d", "com.android.gallery3d.vivo.GalleryTabActivity")) {
                cn = new ComponentName("com.android.gallery3d", "com.android.gallery3d.vivo.GalleryTabActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sec.android.gallery3d", "com.sec.android.gallery3d.app.Gallery")) {
                cn = new ComponentName("com.sec.android.gallery3d", "com.sec.android.gallery3d.app.Gallery");
            }
        }
        
        /*兼容 视频 ComponentName 替换  ", "  */
        if (packageName.equals("com.cappu.media") && className.equals("com.cappu.media.VideoActivity")) {
            if (APKInstallTools.checkApkInstall(mContext, "com.samsung.everglades.video", "com.samsung.everglades.video.VideoMain")) {
                cn = new ComponentName("com.samsung.everglades.video", "com.samsung.everglades.video.VideoMain");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.oppo.video", "com.oppo.video.VideoListActivity")) {
                cn = new ComponentName("com.oppo.video", "com.oppo.video.VideoListActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.videoeditor", "com.android.videoeditor.ProjectsActivity")) {
                cn = new ComponentName("com.android.videoeditor", "com.android.videoeditor.ProjectsActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.htc.connectedMedia", "com.htc.connectedMedia.ConnectedMedia")) {
                cn = new ComponentName("com.htc.connectedMedia", "com.htc.connectedMedia.ConnectedMedia");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sonyericsson.video", "com.sonyericsson.video.browser.BrowserActivity")) {
                cn = new ComponentName("com.sonyericsson.video", "com.sonyericsson.video.browser.BrowserActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.BBKVivoVideo", "com.android.BBKVivoVideo.LocalVideoActivity")) {
                cn = new ComponentName("com.android.BBKVivoVideo", "com.android.BBKVivoVideo.LocalVideoActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.android.video", "com.android.video.VideoListActivity")) {
                cn = new ComponentName( "com.android.video", "com.android.video.VideoListActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sec.android.app.videoplayer", "com.sec.android.app.videoplayer.activity.MainTab")) {
                cn = new ComponentName("com.sec.android.app.videoplayer", "com.sec.android.app.videoplayer.activity.MainTab");
            }
        }
        
        /*兼容 收音机FM ComponentName 替换  ", "  */
        if (packageName.equals("com.mediatek.FMRadio") && className.equals("com.mediatek.FMRadio.FMRadioActivity")) {
            if (APKInstallTools.checkApkInstall(mContext, "com.huawei.android.FMRadio", "com.huawei.android.FMRadio.FMRadioMainActivity")) {
                cn = new ComponentName("com.huawei.android.FMRadio", "com.huawei.android.FMRadio.FMRadioMainActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.miui.fmradio", "com.miui.fmradio.FMRadioMain")) {
                cn = new ComponentName("com.miui.fmradio", "com.miui.fmradio.FMRadioMain");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.htc.fm", "com.htc.fm.FMRadio")) {
                cn = new ComponentName("com.htc.fm", "com.htc.fm.FMRadio");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.fmradio", "com.fmradio.FMRadioActivity")) {
                cn = new ComponentName("com.fmradio", "com.fmradio.FMRadioActivity");
            }else if (APKInstallTools.checkApkInstall(mContext, "com.sec.android.app.fm", "com.sec.android.app.fm.MainActivity")) {
                cn = new ComponentName("com.sec.android.app.fm", "com.sec.android.app.fm.MainActivity");
            }
        }
        
        /*兼容 时钟 ComponentName 替换  ", "  */
        if (packageName.equals("com.android.deskclock") && className.equals("com.android.deskclock.DeskClock")) {
            if (APKInstallTools.checkApkInstall(mContext, "", "")) {
                cn = new ComponentName("", "");
            }else if (APKInstallTools.checkApkInstall(mContext, "", "")) {
                cn = new ComponentName("", "");
            }
        }
        
        /*兼容  ComponentName 替换  ", "  */
        if (packageName.equals("") && className.equals("")) {
            if (APKInstallTools.checkApkInstall(mContext, "", "")) {
                cn = new ComponentName("", "");
            }else if (APKInstallTools.checkApkInstall(mContext, "", "")) {
                cn = new ComponentName("", "");
            }
        }
        
        
        return cn;
        
    }
}
