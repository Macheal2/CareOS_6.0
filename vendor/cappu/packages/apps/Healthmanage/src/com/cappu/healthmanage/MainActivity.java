package com.cappu.healthmanage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cappu.huayan.HuaYanActivity;
import com.cappu.huayan.XiangQingActivity;
import com.cappu.widget.TopBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Rect;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import android.widget.TextView;

public class MainActivity extends Activity {

    private ListView list;
    private List<Map<String, Object>> mylist = new ArrayList<Map<String, Object>>();
    private int statusheight;
    private TopBar mTopBar;
    private int layoutheight;
    
    private final int ACTION_INTENT_DRUG_STEWARD = 0;
    private final int ACTION_INTENT_FAMILY_MEDICINE = 2;
    private final int ACTION_INTENT_ASSAY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = (ListView) findViewById(R.id.listview);
        mTopBar = (TopBar) findViewById(R.id.topbar);
        layoutheight = mTopBar.getLayoutParams().height;
        
        adddate();
        getstatusheight();// 获取状态栏的高度
        Myadaper adapter = new Myadaper();
        list.setAdapter(adapter);
        setlistlistence();// 设置listview的监听
        readsqlite();
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, HuaYanActivity.class);
        startActivity(intent);
        this.finish();

    }

    // 把数据文件写到date文件夹下面去
    public void readsqlite() {
        AssetManager am = getAssets();
        try {
            InputStream is = am.open("health_report.db");
            File filedate = getDatabasePath("health_report.db");

            if (!filedate.exists()) {
                File fileparent = filedate.getParentFile();
                if (!fileparent.exists()) {
                    fileparent.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(fileparent + "/" + filedate.getName());
                Long a = System.currentTimeMillis();

                int len = 0;
                byte bb[] = new byte[2048];
                while ((len = is.read(bb)) != -1) {
                    fos.write(bb, 0, len);
                }
                is.close();
                fos.close();
                Long b = System.currentTimeMillis();
                Log.i("test", "bbb==" + (a - b));
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    //状态栏的高度
    private void getstatusheight() {
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            statusheight = getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("test", statusheight + "==statusHeight");
    }

    //listview的item监听
    private void setlistlistence() {
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent intent = new Intent();

                switch (arg2) {
                    case ACTION_INTENT_DRUG_STEWARD:
                        ComponentName Comp = new ComponentName("com.cappu.healthmanage", "com.cappu.drugsteward.DrugStewardActivity");
                        intent.setComponent(Comp);
                        intent.setAction("android.intent.action.MAIN");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                    case ACTION_INTENT_FAMILY_MEDICINE:
                        Intent tIntent = new Intent();
                        ComponentName tComp = new ComponentName("com.example.magcomm_6y17rfamilypharmacy", "com.example.magcomm_6y17rfamilypharmacy.OpenActivity");
                        intent.setComponent(tComp);
                        intent.setAction("android.intent.action.MAIN");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
    
                        //
                        // //获取launcher中的应用列表
                        // intent.setAction(Intent.ACTION_MAIN);
                        // intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        // List<ResolveInfo> lists =
                        // getPackageManager().queryIntentActivities(intent, 0);
                        // //获取launcher中应用列表的名称和图标
                        // ResolveInfo info = lists.get(0);
                        // info.loadIcon(getPackageManager());
                        // info.loadLabel(getPackageManager());
                        //
                        // //从当前应用跳转到另一个应用中
                        // Intent intenta =
                        // getPackageManager().getLaunchIntentForPackage(info.activityInfo.packageName);
                        // startActivity(intenta);
                        break;
                    case ACTION_INTENT_ASSAY:
                        intent.setClass(MainActivity.this, HuaYanActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    //添加数据
    public void adddate() {
        int[] images = { R.drawable.logo_pill_box, /*R.drawable.logo_drug,*/ R.drawable.logo_huayan };
        String[] titles = {getResources().getString(R.string.drug_steward),
                           /*getResources().getString(R.string.family_medicine),*/
                           getResources().getString(R.string.laboratory_analysis)};
        for (int i = 0; i < titles.length; i++) {
            Map<String, Object> maa = new HashMap<String, Object>();
            maa.put("images", images[i]);
            maa.put("titles", titles[i]);
            mylist.add(maa);
        }
    }

    //设置适配器
    private class Myadaper extends BaseAdapter {
        private LayoutInflater inflater;
        private int heighta;
        private int widtha;

        public Myadaper() {
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            DisplayMetrics display = getResources().getDisplayMetrics();
            heighta = display.heightPixels;
            widtha = display.widthPixels;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mylist.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public View getView(int arg0, View convertView, ViewGroup arg2) {
            GameTag tag = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item, null);
                tag = new GameTag();
                tag.flagimage = (ImageView) convertView.findViewById(R.id.list_item_flagimage);
                tag.flagtext = (TextView) convertView.findViewById(R.id.list_item_flagtext);
                convertView.setTag(tag);
            } else {
                tag = (GameTag) convertView.getTag();
            }

            tag.flagimage.setImageResource((Integer) mylist.get(arg0).get("images"));
            tag.flagtext.setText((String) mylist.get(arg0).get("titles"));
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            int item_h = (heighta - statusheight - layoutheight - 5) / mylist.size();
            params.height = item_h > 180 ? 180 : item_h;

            convertView.setLayoutParams(params);
            return convertView;
        }

    }

    class GameTag {
        ImageView flagimage;
        TextView flagtext;
    }

}
