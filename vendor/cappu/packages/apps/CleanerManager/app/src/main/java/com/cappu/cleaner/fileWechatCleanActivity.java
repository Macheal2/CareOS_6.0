package com.cappu.cleaner;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.cleaner.FileClean.SearchListener;
import com.cappu.cleaner.context.AppExpandableListAdapter;
import com.cappu.cleaner.context.fileCleanInfo;
import com.cappu.cleaner.context.fileTitleInfo;
import com.cappu.cleaner.widget.TopBar;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.cappu.cleaner.FileClean.RESULT_AD;
import static com.cappu.cleaner.R.id.expandableListView;

public class fileWechatCleanActivity extends Activity implements SearchListener {
    private ExpandableListView mExpandableListView;
    private AppExpandableListAdapter mDownloadExListAdapter;
    private FileClean mFileClen;
    private TextView mScrollText;
    private TextView mCleanTotalSize;
    private TextView mCleanTotalUnit;
    private TopBar mTopBar;

    private List<AppExpandableListAdapter.TreeNode> mNodes;
    private long mTotalSizeTypeB;

    private final static int ADFILES = 1;
    private final static int APKPACKAGE = 2;
    private final static int WECHAT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        initCleaner();
        initView();
        mDownloadExListAdapter.UpdateTreeNode(mNodes);
        mExpandableListView.setAdapter(mDownloadExListAdapter);

        for (int i = 0; i < mDownloadExListAdapter.getGroupCount(); i++) {
            mExpandableListView.expandGroup(i);
        }

        if (mFileClen == null) {
            mFileClen = FileClean.getInstance(this);
        }
        File path = initSDcard();
        if (path != null) {
            mFileClen.searchListFiles(path.getAbsolutePath(), FileClean.SEARCH_ALL);
            mFileClen.setOnSearchListener(this);
        }
    }

    private void initView() {
        mTopBar = (TopBar) findViewById(R.id.topbar);
        mScrollText = (TextView) findViewById(R.id.scroll_text);
        mCleanTotalSize = (TextView) findViewById(R.id.clean_value);
        mCleanTotalUnit = (TextView) findViewById(R.id.clean_unit);
        LinearLayout myEmpty = (LinearLayout) findViewById(R.id.list_empty);
        mExpandableListView = (ExpandableListView) this.findViewById(expandableListView);
        mExpandableListView.setGroupIndicator(null);// 设置默认的箭头
        mExpandableListView.setEmptyView(myEmpty);
        mDownloadExListAdapter = new AppExpandableListAdapter(this);

        mTopBar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mTopBar.getTitleContent().setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mTopBar.getLeftButton().setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mTopBar.getRightButton().setBackgroundColor(getResources().getColor(android.R.color.transparent));

        mCleanTotalSize.setText("0");
        mCleanTotalUnit.setText("B");
    }

    private void initCleaner() {
        mTotalSizeTypeB = 0;
        mNodes = new ArrayList<AppExpandableListAdapter.TreeNode>();

        AppExpandableListAdapter.TreeNode Adfile = new AppExpandableListAdapter.TreeNode();
        fileTitleInfo AdfileInfo = new fileTitleInfo();
        AdfileInfo.id = ADFILES;
        AdfileInfo.name = getResources().getString(R.string.str_clean_ad_folder);
        AdfileInfo.total_size = 0;
        Adfile.parent = AdfileInfo;
        mNodes.add(Adfile);
        AppExpandableListAdapter.TreeNode apkpackage = new AppExpandableListAdapter.TreeNode();
        fileTitleInfo apkpackageInfo = new fileTitleInfo();
        apkpackageInfo.id = APKPACKAGE;
        apkpackageInfo.name = getResources().getString(R.string.str_clean_package);
        apkpackageInfo.total_size = 0;
        apkpackage.parent = apkpackageInfo;
        mNodes.add(apkpackage);
        AppExpandableListAdapter.TreeNode wechat = new AppExpandableListAdapter.TreeNode();
        fileTitleInfo wechatInfo = new fileTitleInfo();
        wechatInfo.id = WECHAT;
        wechatInfo.name = getResources().getString(R.string.str_clean_wechat);
        wechatInfo.total_size = 0;
        wechat.parent = wechatInfo;
        mNodes.add(wechat);
    }

    private File initSDcard() {
        File path = null;
        //检测SD卡是否存在
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            path = Util.getExternalFile();
        } else {
            Toast.makeText(this, "没有SD卡", Toast.LENGTH_LONG).show();
        }
        return path;
    }

    @Override
    public void onSearching(String result) {
        if (mScrollText != null) {
            mScrollText.setText(result);
        }
    }

    @Override
    public void onCatchSearchCache(PackageStats result, float percent) {

    }

    /**
     * @param result 路径
     * @param type   类型
     * @param name   文件数组
     */
    @Override
    public void onCatchSearchFile(String result, int type, int name) {
        Log.e("hmq", "onCatchSearch = " + result+"; type="+type+"; name="+name);
//        switch (type) {
//            case FileClean.RESULT_APK:
//                if (mDownloadExListAdapter.getGroupById(APKPACKAGE) != null) {
//                    List<fileCleanInfo> list = mDownloadExListAdapter.getChildGroupByID(APKPACKAGE);
//                    list.add(getPackageInfo(result));
//                    mDownloadExListAdapter.updateChildGroup(APKPACKAGE, list);
//                }
//                break;
//            case RESULT_AD:
//                if (mDownloadExListAdapter.getGroupById(ADFILES) != null) {
//                    List<fileCleanInfo> list = mDownloadExListAdapter.getChildGroupByID(ADFILES);
//                    list.add(getFileInfo(type, name, result));
//                    mDownloadExListAdapter.updateChildGroup(ADFILES, list);
//                }
//                break;
//            case FileClean.RESULT_WECHAT:
//                if (mDownloadExListAdapter.getGroupById(WECHAT) != null) {
//                    List<fileCleanInfo> list = mDownloadExListAdapter.getChildGroupByID(WECHAT);
//                    list.add(getFileInfo(type, name, result));
//                    mDownloadExListAdapter.updateChildGroup(WECHAT, list);
//                }
//                break;
//            case FileClean.RESULT_CACHE:
//
//                break;
//        }


    }

    @Override
    public void onSearchState(int state) {
        switch (state) {
            case FileClean.NONE:
                break;
            case FileClean.WORKING_CACHE:
            case FileClean.WORKING_FILE:
                break;
            case FileClean.FINISH:

                if (mScrollText != null) {
                    mScrollText.setText(getResources().getString(R.string.str_finish));
                }
                break;
        }
    }

    /**
     * @param type    3.微信 2.无效文件
     * @param name    数组
     * @param apkPath 文件夹路径
     * @return 文件信息
     */
    private fileCleanInfo getFileInfo(int type, int name, String apkPath) {
        long fileSizeTypeB = 0;
        fileCleanInfo file_info = new fileCleanInfo();
        Drawable icon = null;

        file_info.setFileSavePath(apkPath);
        fileSizeTypeB = Util.getFilesSizeForLong(apkPath);
        file_info.setFilesize(fileSizeTypeB);//得到文件大小

        if (type == FileClean.RESULT_WECHAT) {
            file_info.setLabel(NameList.WechatNameList[name]);
            icon = Util.mWechatIcon;
        } else if (type == FileClean.RESULT_AD) {
            file_info.setLabel(NameList.ADNameList[name]);
            icon = getResources().getDrawable(R.drawable.ic_folder);
        }
        file_info.setIcon(icon);

        mTotalSizeTypeB += fileSizeTypeB;

        double fileSizeFormet;
        DecimalFormat df = new DecimalFormat("#.00");
        if (mTotalSizeTypeB < 1024) {
            fileSizeFormet = Util.FormatFileSize(mTotalSizeTypeB, Util.SIZETYPE_B);
            mCleanTotalUnit.setText("B");
        } else if (mTotalSizeTypeB < 1048576) {
            fileSizeFormet = Util.FormatFileSize(mTotalSizeTypeB, Util.SIZETYPE_KB);
            mCleanTotalUnit.setText("KB");
        } else if (mTotalSizeTypeB < 1073741824) {
            fileSizeFormet = Util.FormatFileSize(mTotalSizeTypeB, Util.SIZETYPE_MB);
            mCleanTotalUnit.setText("MB");
        } else {
            fileSizeFormet = Util.FormatFileSize(mTotalSizeTypeB, Util.SIZETYPE_GB);
            mCleanTotalUnit.setText("GB");
        }
        mCleanTotalSize.setText(String.valueOf(fileSizeFormet));

        return file_info;
    }

    private fileCleanInfo getPackageInfo(String apkPath) {
        long fileSizeTypeB = 0;
        fileCleanInfo file_info = new fileCleanInfo();
        PackageManager pm = getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);

        if (info != null) {
            file_info.setFileSavePath(apkPath);
            fileSizeTypeB = Util.getFilesSizeForLong(apkPath);
            file_info.setFilesize(fileSizeTypeB);//得到文件大小
            ApplicationInfo appInfo = info.applicationInfo;
            /* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            file_info.setLabel(appInfo.loadLabel(pm).toString());//(pm.getApplicationLabel(appInfo).toString());
            file_info.setPackageName(appInfo.packageName);//得到安装包名称
            String version = info.versionName;//String.valueOf(info.versionCode);       //得到版本信息
            file_info.setVersion(version);
            Drawable icon = pm.getApplicationIcon(appInfo);//得到图标信息
            file_info.setIcon(icon);

            mTotalSizeTypeB += fileSizeTypeB;

            double fileSizeFormet;
            DecimalFormat df = new DecimalFormat("#.00");
            if (mTotalSizeTypeB < 1024) {
                fileSizeFormet = Util.FormatFileSize(mTotalSizeTypeB, Util.SIZETYPE_B);
                mCleanTotalUnit.setText("B");
            } else if (mTotalSizeTypeB < 1048576) {
                fileSizeFormet = Util.FormatFileSize(mTotalSizeTypeB, Util.SIZETYPE_KB);
                mCleanTotalUnit.setText("KB");
            } else if (mTotalSizeTypeB < 1073741824) {
                fileSizeFormet = Util.FormatFileSize(mTotalSizeTypeB, Util.SIZETYPE_MB);
                mCleanTotalUnit.setText("MB");
            } else {
                fileSizeFormet = Util.FormatFileSize(mTotalSizeTypeB, Util.SIZETYPE_GB);
                mCleanTotalUnit.setText("GB");
            }
            mCleanTotalSize.setText(String.valueOf(fileSizeFormet));

            Log.e("hmq", "version=" + version + "; versioncode=" + info.versionCode + "   file_info:" + file_info.toString());
        }
        return file_info;
    }
}