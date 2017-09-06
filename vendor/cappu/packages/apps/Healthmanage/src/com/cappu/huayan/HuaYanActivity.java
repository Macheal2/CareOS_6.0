package com.cappu.huayan;

import java.util.ArrayList;
import java.util.List;

import com.cappu.healthmanage.R;

import dao.Sqldao;
import entity.Content;
import entity.LaboratoryReport;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.MeasureSpec;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class HuaYanActivity extends Activity {

    private EditText huayan_search;
    private ListView list;
    private Myadapter adapter;
    private Sqldao sqldao;
    private List<LaboratoryReport> report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_huayan);
        huayan_search = (EditText) findViewById(R.id.huayan_search);
        list = (ListView) findViewById(R.id.huayan_list);
        sqldao = new Sqldao(this);
        report = sqldao.selectreport();
        adapter = new Myadapter();
        list.setAdapter(adapter);
        setListViewHeightBasedOnChildren(adapter, list);
        huayan_search.setFocusable(false);
        list.setFocusable(false);
        setlistlistence();//设置list的监听
        huayan_search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(HuaYanActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
    }
    
	@Override
	protected void onStop() {
		super.onStop();
		
		this.finish(); 
	}
    
    private void setlistlistence() {
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent intent = new Intent();
                intent.setClass(HuaYanActivity.this, HYDetailActivity.class);
                intent.putExtra("id", report.get(arg2).getId());
                intent.putExtra("name", report.get(arg2).getName());
                startActivity(intent);

            }
        });
    }

    //设置适配器
    private class Myadapter extends BaseAdapter {
        private LayoutInflater inflater;

        public Myadapter() {
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return report.size();
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
                convertView = inflater.inflate(R.layout.main_huayan_list_item, null);
                tag = new GameTag();
                tag.huashoutext = (TextView) convertView.findViewById(R.id.huayan_list_item_shoumingtext);
                convertView.setTag(tag);
            } else {
                tag = (GameTag) convertView.getTag();
            }
            tag.huashoutext.setText(report.get(arg0).getName());
            return convertView;
        }

    }

    private class GameTag {
        TextView huashoutext;
    }

    private ViewGroup.LayoutParams params;
    private TextView biaoti;

    public void setListViewHeightBasedOnChildren(BaseAdapter mAdapter, ListView listView) {
        Log.i("test", mAdapter + "===" + listView);
        if (mAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View listItem = mAdapter.getView(i, null, listView);
            int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
            Log.i("test", "listitem=" + listItem + ";width" + desiredWidth + ";i=" + i);
            listItem.measure(desiredWidth, 0);
            totalHeight += listItem.getMeasuredHeight();
            // Log.i("test","desiredWidth=="+desiredWidth);
            // Log.i("test","totalHeight=="+totalHeight);
        }

        /*
         * 但是我每次调用的时候都会产生在listItem.measure(0,0)报空指针异常。
         * 我debug 发现listItem 并不是为空啊，为啥会报错。在经过一番查找之后。
         * 我发现原来是自己item的布局用了RelativeLayout 把他换为LinearLayout 就好了。
         * 究其原因，原来是 Linearlayout重写了onmeasure方法，其他的布局文件没有重写onmeasure,
         * 所以在调用listItem.measure(0, 0); 会报空指针异常，如果想用这个东东，就必须用linearlayout布局喽。
         */
        params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (mAdapter.getCount()));

        listView.setLayoutParams(params);
    }

}
