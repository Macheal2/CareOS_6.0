package com.cappu.huayan;

import java.util.List;

import com.cappu.healthmanage.R;

import dao.Sqldao;
import entity.ReportDetail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.MeasureSpec;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cappu.widget.TopBar;

public class HYDetailActivity extends Activity {

    private RelativeLayout mSearchLayout;
    private ListView list;
    private List<ReportDetail> detail;
    private TopBar mTopBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_huayan);
        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        String name = intent.getStringExtra("name");
        mSearchLayout = (RelativeLayout) findViewById(R.id.edit_layout);
        mSearchLayout.setVisibility(View.GONE);
        //mTitleBar = (TextView) findViewById(R.id.title_bar);
        mTopBar = (TopBar) findViewById(R.id.topbar);
        Sqldao sqldao = new Sqldao(this);
        detail = sqldao.selectreportdetail(id);
        mTopBar.setText(name);
        list = (ListView) findViewById(R.id.huayan_list);
        mAdapter = new Myadapter();
        setListViewHeightBasedOnChildren(mAdapter, list);
        list.setAdapter(mAdapter);

        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent intent = new Intent(HYDetailActivity.this, XiangQingActivity.class);
                intent.putExtra("lid", detail.get(arg2).getLrid());
                intent.putExtra("cid", detail.get(arg2).getCaid());
                startActivity(intent);

            }
        });
    }

    // ����������
    private class Myadapter extends BaseAdapter {
        private LayoutInflater inflater;

        public Myadapter() {
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return detail.size();
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
                convertView.findViewById(R.id.huayan_list_item_shoumingimage).setVisibility(View.GONE);
                tag.detailtext = (TextView) convertView.findViewById(R.id.huayan_list_item_shoumingtext);
                convertView.setTag(tag);
            } else {
                tag = (GameTag) convertView.getTag();
            }
            tag.detailtext.setText(detail.get(arg0).getName());
            return convertView;
        }

    }

    private class GameTag {
        TextView detailtext;
    }

    private ViewGroup.LayoutParams params;
    private Myadapter mAdapter;

    public void setListViewHeightBasedOnChildren(BaseAdapter mAdapter, ListView listView) {
        if (mAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View listItem = mAdapter.getView(i, null, listView);
            int desiredWidth = MeasureSpec.makeMeasureSpec(list.getWidth(), MeasureSpec.AT_MOST);
            listItem.measure(desiredWidth, 0);
            totalHeight += listItem.getMeasuredHeight();
            // Log.i("test","desiredWidth=="+desiredWidth);
            // Log.i("test","totalHeight=="+totalHeight);
        }

        params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (mAdapter.getCount()));

        listView.setLayoutParams(params);
    }

}
