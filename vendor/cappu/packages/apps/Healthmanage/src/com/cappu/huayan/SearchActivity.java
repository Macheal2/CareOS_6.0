package com.cappu.huayan;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.cappu.healthmanage.R;

import dao.Sqldao;
import entity.ReportDetail;

public class SearchActivity extends Activity {

    private EditText searchedit;
    private TextView chaxunjiegu;
    private ListView list;
    private Button btn;
    private List<ReportDetail> details = new ArrayList<ReportDetail>();
    private Sqldao sqldao;
    private Myadapter adapter;
    private String type = "distory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.huayan_search);
        searchedit = (EditText) findViewById(R.id.huayan_editext_search);
        chaxunjiegu = (TextView) findViewById(R.id.search_text_chaxunjiegu);
        btn = (Button) findViewById(R.id.search_button);
        list = (ListView) findViewById(R.id.search_listview);
        adddata();
        adapter = new Myadapter();
        list.setAdapter(adapter);
        seteditstatus();// 设置edittext的状态
        setbuttonlistence();// 设置button的点击事件

        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                sqldao.addreadreport(details.get(arg2), System.currentTimeMillis());
                Intent intent = new Intent(SearchActivity.this, XiangQingActivity.class);
                intent.putExtra("cid", details.get(arg2).getCaid());
                if (type.equals("new")) {
                    intent.putExtra("lid", details.get(arg2).getLrid());
                } else if (type.equals("history")) {
                    intent.putExtra("lid", Integer.parseInt(details.get(arg2).getContent()));
                }

                startActivity(intent);

            }
        });

    }

    // 设置button的点击事件
    private void setbuttonlistence() {
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String sname = searchedit.getText().toString();

                details = sqldao.searchreportdetail(sname);
                type = "new";
                if (details != null) {
                    if (details.size() < 0) {
                        chaxunjiegu.setVisibility(View.VISIBLE);
                    } else {
                        chaxunjiegu.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(SearchActivity.this, "没有数据到数据", 0).show();
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    // 添加数据
    public void adddata() {
        sqldao = new Sqldao(this);
        details = sqldao.selectreadreport();
        type = "history";
        if (details.size() > 0) {
            chaxunjiegu.setVisibility(View.GONE);
        } else {
            chaxunjiegu.setVisibility(View.VISIBLE);
        }
    }

    // 设置edittext的状态
    private void seteditstatus() {
        searchedit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
                System.out.println("-1-onTextChanged-->" + searchedit.getText().toString() + "<--");
                Log.i("test", "==这个应该是值改变后11111==" + searchedit.getText().toString());

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
                Log.i("test", "==这个应该是值改变之前==" + searchedit.getText().toString());

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
                Log.i("test", "==这个应该是值改变之后==" + searchedit.getText().toString());
                if (searchedit.getText().toString().equals("")) {
                    details = sqldao.selectreadreport();
                    type = "history";
                    adapter.notifyDataSetChanged();
                }

            }
        });
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
            return details.size();
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
            tag.detailtext.setText(details.get(arg0).getName());
            return convertView;
        }

    }

    private class GameTag {
        TextView detailtext;
    }

}
