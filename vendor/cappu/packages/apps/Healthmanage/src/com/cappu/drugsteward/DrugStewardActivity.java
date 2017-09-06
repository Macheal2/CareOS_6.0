package com.cappu.drugsteward;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import com.cappu.drugsteward.example.xing.activity.CaptureActivity;
import com.cappu.drugsteward.util.DensityUtil;
import com.cappu.healthmanage.R;

public class DrugStewardActivity extends ListActivity implements OnClickListener, OnScrollListener{

    private EditText editcode;
    private String TAG = DensityUtil.Tag;
    private ListView mListView;
    private ListViewAdapter mListViewAdapter = new ListViewAdapter();
    private TypedArray color_bg;
    private TypedArray color_point;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draug_main);
        color_bg = getResources().obtainTypedArray(R.array.array_color_bg_drawables);
        color_point = getResources().obtainTypedArray(R.array.array_color_point_drawables);
        initview();

    }

    // 初始化控件
    public void initview() {
        Button twodimension = (Button) findViewById(R.id.twodimension);
        //RelativeLayout existyaoxiang = (RelativeLayout) findViewById(R.id.exist_yaoxiang);
        //RelativeLayout memberadd = (RelativeLayout) findViewById(R.id.member_add);
        editcode = (EditText) findViewById(R.id.edit_code);
        Button selectdrug = (Button) findViewById(R.id.select_drug);
        twodimension.setOnClickListener(this);
        //existyaoxiang.setOnClickListener(this);
        //memberadd.setOnClickListener(this);
        selectdrug.setOnClickListener(this);
        mListView = (ListView) getListView();
        
        mListView.setOnScrollListener(this);
    }
    
    class ListViewAdapter extends BaseAdapter {
        int count = 6;
        LayoutInflater inflater;

        public int getCount() {
            return count;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getListView(){
            return mListView;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TabView sub_view = null;
            Log.e(TAG,this.getClass() + ".getView-- position="+position + "; convertView=" + (convertView == null));
            if (convertView == null) {
                inflater = (LayoutInflater) DrugStewardActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.tab_view, null);
                sub_view = new TabView();
                sub_view.mTitleLayout = (LinearLayout) convertView.findViewById(R.id.title_line);
                sub_view.mTitleTextView = (TextView) convertView.findViewById(R.id.title_view);
                sub_view.mTitleButton = (ImageButton) convertView.findViewById(R.id.but_Detail);
                sub_view.mListView = (ListView) convertView.findViewById(R.id.list_view);
                sub_view.mTitleLayout.setTag(position);
                convertView.setTag(sub_view);
            } else {
                sub_view = (TabView) convertView.getTag();
                sub_view.mTitleLayout.setTag(position);
                
            }
            ((TabSubView)convertView).onTag(position);
            int colorIndex = position % (color_bg.length());
            Log.e(TAG,this.getClass() + ".getView-- colorIndex=" + colorIndex);
            if (colorIndex < 0 && colorIndex > (color_bg.length() - 1)) {
                sub_view.mTitleLayout.setBackground(DrugStewardActivity.this.getResources().getDrawable(color_bg.getResourceId(0, 0)));
            } else {
                sub_view.mTitleLayout.setBackground(DrugStewardActivity.this.getResources().getDrawable(color_bg.getResourceId(colorIndex, 0)));
            }
            SharedPreferences sp = getApplicationContext().getSharedPreferences(DensityUtil.USERNAME_KEY, getApplicationContext().MODE_PRIVATE);
            String titletext = sp.getString(String.valueOf(position), "");
            if (titletext.isEmpty() || titletext.equals("")) {
                titletext = getResources().getString(R.string.drug_box_title_null);
            }
            sub_view.mTitleTextView.setText(titletext);
            return convertView;
        }
    }
    
    private class TabView {
        LinearLayout mTitleLayout;
        TextView mTitleTextView;
        ImageButton mTitleButton;
        ListView mListView;
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
       // mListViewAdapter.notify();
        setListAdapter(mListViewAdapter); 
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();

        switch (v.getId()) {
//        case R.id.member_add:
//            intent.setClass(this, ExistMemberActivity.class);
//            startActivity(intent);
//            break;
//        case R.id.exist_yaoxiang:
//            intent.setClass(this, YaoXiangActivity.class);
//            startActivity(intent);
//            break;
        case R.id.twodimension:
            intent.setClass(this, CaptureActivity.class);
            startActivity(intent);
            break;
        case R.id.select_drug:
            String scanresult = editcode.getText().toString();
            try {
                int index = 0;
                for ( ;index < scanresult.length(); index++){
                    Integer.valueOf(scanresult.substring(index, index + 1)).intValue();
                }
                if(index != 20 && index != 13){
                    Toast.makeText(getApplicationContext(), R.string.query_Toast_number_error, Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.query_Toast_error, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
            intent.setClass(this, TwoScanActivity.class);
            intent.putExtra("scanresult", scanresult);
            startActivity(intent);
            finish();
            break;
        }

    }
    
    @Override
    public void onScrollStateChanged(AbsListView view, int mScrollState) {
        /**
         * 当ListView滑动到最后一条记录时这时，我们会看到已经被添加到ListView的"加载项"布局， 这时应该加载剩余数据。
         */
//        if (mLastItem == mListViewAdapter.count && mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
//            if (mListViewAdapter.count <= mCount) {
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mListViewAdapter.count += 10;
//                        mListViewAdapter.notifyDataSetChanged();
//                        mListView.setSelection(mLastItem);
//                    }
//                }, 1000);
//            }
//        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        mLastItem = firstVisibleItem + visibleItemCount - 1;
//        if (mListViewAdapter.count > mCount) {
//            mListView.removeFooterView(mLoadLayout);
//        }
    }
}
