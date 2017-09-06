package com.cappu.huayan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cappu.healthmanage.R;

import dao.Sqldao;
import entity.Content;
import entity.ReportDetail;

public class XiangQingActivity extends Activity {

    private WebView webview;
    private TextView detail_content;
    private TextView detail_biaoti;
    private int lid;
    private int cid;
    private ReportDetail detail;
    List<Map<String, Boolean>> mylist = new ArrayList<Map<String, Boolean>>();
    private ListView list;
    private Myadapter mAdapter;
    private List<Content> mycont = new ArrayList<Content>();
    private int sum = 0;
    String type = "new";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_webview);
        detail_biaoti = (TextView) findViewById(R.id.report_detail_biaoti);
        detail_content = (TextView) findViewById(R.id.report_detail_content);
        Intent intent = getIntent();
        lid = intent.getIntExtra("lid", 0);
        cid = intent.getIntExtra("cid", 0);
        type = intent.getStringExtra("type");
        adddate();// 添加数据
        setdufault();
        list = (ListView) findViewById(R.id.xiangqing_listview);
        mAdapter = new Myadapter();

        setListViewHeightBasedOnChildren(mAdapter, list, 0);
        list.setAdapter(mAdapter);
        list.setFocusable(false);
        setlistence();// 设置listview的监听
    }

    private void setlistence() {
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
                final TextView xiangqing_item_text = (TextView) arg1.findViewById(R.id.xiangqing_item_text);
                final ImageView image = (ImageView) arg1.findViewById(R.id.huayan_list_item_shoumingimage);

                ViewTreeObserver vto2 = xiangqing_item_text.getViewTreeObserver();
                vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        xiangqing_item_text.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                        Log.i("test", xiangqing_item_text.getHeight() + "==高度==");
                        if (mylist.get(arg2).get(mycont.get(arg2).getTitle())) {
                            sum = sum + xiangqing_item_text.getHeight();
                            setListViewHeightBasedOnChildren(mAdapter, list, sum);
                        } else {
                            sum = sum - xiangqing_item_text.getHeight();
                            setListViewHeightBasedOnChildren(mAdapter, list, sum);
                        }

                    }
                });

                if (mylist.get(arg2).get(mycont.get(arg2).getTitle())) {
                    image.setImageResource(R.drawable.arrow_bottom_transparent);
                    xiangqing_item_text.setVisibility(View.GONE);
                    mylist.get(arg2).put(mycont.get(arg2).getTitle(), false);
                } else {
                    image.setImageResource(R.drawable.arrow_up);
                    xiangqing_item_text.setVisibility(View.VISIBLE);
                    mylist.get(arg2).put(mycont.get(arg2).getTitle(), true);
                    Log.i("test", "这里已经执行了返回结果是true了");
                }

            }
        });
    }

    // 添加数据
    private void adddate() {
        Sqldao dao = new Sqldao(this);
        detail = dao.selectalonedetail(lid, cid);

        try {
            JSONObject obj = new JSONObject(detail.getContent());
            JSONArray array = obj.getJSONArray("data");
            for (int i = 1; i < array.length(); i++) {
                JSONObject obja = array.getJSONObject(i);
                String title = obja.getString("title");
                String content = obja.getString("content");
                Content cont = new Content(title, content);
                detail.getMycontent().add(cont);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (detail != null) {
            detail_biaoti.setText(detail.getName());
            detail_content.setText(convert(detail.getDesc()));
        }
        String sre = "";
        Html.fromHtml("");//
        mycont = detail.getMycontent();
    }

    public String convert(String str) {
        while (str.indexOf(">") >= 0) {
            int a = str.indexOf("<");
            int b = str.indexOf(">");
            str = str.substring(0, a) + str.substring(b + 1);
        }
        return str;
    }

    // 为list<map>设置默认值
    public void setdufault() {
        for (int i = 0; i < mycont.size(); i++) {
            Map<String, Boolean> maa = new HashMap<String, Boolean>();
            Content cont = mycont.get(i);
            maa.put(cont.getTitle(), false);
            mylist.add(maa);
        }
    }

    // 设置适配器
    private class Myadapter extends BaseAdapter {
        private LayoutInflater inflater;

        public Myadapter() {
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mycont.size();
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
                convertView = inflater.inflate(R.layout.main_xiangqing, null);
                tag = new GameTag();
                tag.xiangqingtext = (TextView) convertView.findViewById(R.id.huayan_list_item_shoumingtext);
                tag.xiangqingimage = (ImageView) convertView.findViewById(R.id.huayan_list_item_shoumingimage);
                tag.xiangqingdetail = (TextView) convertView.findViewById(R.id.xiangqing_item_text);
                convertView.setTag(tag);
            } else {
                tag = (GameTag) convertView.getTag();
            }
            String sa = "";
            // if(convert(mycont.get(arg0).getTitle()).indexOf(detail.getName())>=0)
            // {
            // sa=convert(mycont.get(arg0).getTitle()).substring(convert(mycont.get(arg0).getTitle()).indexOf(detail.getName())+detail.getName().length());
            // }
            // else
            // {
            // sa=convert(mycont.get(arg0).getTitle());
            // }
            if (Html.fromHtml(mycont.get(arg0).getTitle()).toString().indexOf(detail.getName()) >= 0) {
                sa = Html.fromHtml(mycont.get(arg0).getTitle()).toString()
                        .substring(Html.fromHtml(mycont.get(arg0).getTitle()).toString().indexOf(detail.getName()) + detail.getName().length());
            } else {
                sa = Html.fromHtml(mycont.get(arg0).getTitle()).toString();
            }
            tag.xiangqingtext.setText(sa.trim());
            // tag.xiangqingimage.setImageResource(R.drawable.arrow_bottom_transparent);
            // tag.xiangqingdetail.setText(convert(mycont.get(arg0).getContent()));
            tag.xiangqingdetail.setText(Html.fromHtml(mycont.get(arg0).getContent()).toString().trim());
            return convertView;
        }

    }

    private class GameTag {
        TextView xiangqingtext;
        ImageView xiangqingimage;
        TextView xiangqingdetail;
    }

    private ViewGroup.LayoutParams params;

    public void setListViewHeightBasedOnChildren(BaseAdapter mAdapter, ListView listView, int sum) {
        if (mAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View listItem = mAdapter.getView(i, null, listView);
            int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
            listItem.measure(desiredWidth, 0);
            totalHeight += listItem.getMeasuredHeight();
            // Log.i("test","desiredWidth=="+desiredWidth);
            // Log.i("test","totalHeight=="+totalHeight);
        }

        params = listView.getLayoutParams();
        if (sum > 0) {
            params.height = totalHeight + (listView.getDividerHeight() * (mAdapter.getCount())) + sum;
        } else {
            sum = 0;
            params.height = totalHeight + (listView.getDividerHeight() * (mAdapter.getCount()));
        }
        listView.setLayoutParams(params);
    }

}
