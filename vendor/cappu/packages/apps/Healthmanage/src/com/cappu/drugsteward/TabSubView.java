package com.cappu.drugsteward;

import java.util.Calendar;
import java.util.List;

import com.cappu.drugsteward.entity.Drug;
import com.cappu.drugsteward.sqlite.SqlOperation;
import com.cappu.drugsteward.util.DensityUtil;
import com.cappu.healthmanage.R;

import android.app.AlertDialog;
import android.app.CareDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewDebug.ExportedProperty;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class TabSubView extends LinearLayout implements OnClickListener {
    private TextView mTitleView;
    private ListView mListView;
    //private LinearLayout mTitleLine;
    private ImageButton mButtonDetail;
    private CareDialog mAlertDialog;
    private Context mContext;
    private SharedPreferences sp;
    private int mTag = -1;
    private List<Drug> mDrug;
    private ListAdapter mAdapter;
//    public TypedArray color_bg = getResources().obtainTypedArray(R.array.array_color_bg_drawables);
    public TypedArray color_point = getResources().obtainTypedArray(R.array.array_color_point_drawables);
    private String TAG = DensityUtil.Tag;
    
    public TabSubView(Context context) {
        this(context, null);
    }
    
    public TabSubView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 在构造函数中将Xml中定义的布局解析出来。
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.tab_sub_view, this, true);
        //mTitleLine = (LinearLayout) findViewById(R.id.title_line);
        mTitleView = (TextView) findViewById(R.id.title_view);
        mButtonDetail = (ImageButton) findViewById(R.id.but_Detail);
        mListView = (ListView) findViewById(R.id.list_view);
//        mTag = this.getTag();
//        
//        int tag = Integer.valueOf((String) mTag).intValue();
//
//        int colorIndex = tag % (color_bg.length());
//        Log.e(TAG,this.getClass() + ".getView-- tag="+tag+"; colorIndex=" + colorIndex);

//        if (colorIndex < 0 && colorIndex > (color_bg.length() - 1)) {
//            mTitleLine.setBackground(this.getResources().getDrawable(color_bg.getResourceId(0, 0)));
//        } else {
//            mTitleLine.setBackground(this.getResources().getDrawable(color_bg.getResourceId(tag - 1, 0)));
//        }
        mButtonDetail.setOnClickListener(this);
        mTitleView.setOnClickListener(this);
        //onResume();
    }

//    public void onResume() {
//        // TODO Auto-generated method stub
//        SqlOperation operation = new SqlOperation(mContext);
//        //int tag = Integer.valueOf((String) mTag).intValue();
//        if (mTag != -1) {
//            mDrug = operation.getdurg(mTag, "order by duetime");
//            Log.e("hmq","onResume mTag="+mTag);
//            mAdapter = new ListAdapter(mContext, mDrug, mTag);
//            mListView.setAdapter(mAdapter);
//            mAdapter.notifyDataSetChanged();
//            SharedPreferences sp = mContext.getSharedPreferences(DensityUtil.USERNAME_KEY, mContext.MODE_PRIVATE);
//            String titletext = sp.getString((String) this.getTag(), "");
//            if (titletext.isEmpty() || titletext.equals("")) {
//                titletext = getResources().getString(R.string.drug_box_title_null);
//            }
//            mTitleView.setText(titletext);
//        }
//
//    }

    void onTag(int tag){
        Log.e("hmq","onTag");
        mTag = tag;//Integer.valueOf((String) tag).intValue();
        SqlOperation operation = new SqlOperation(mContext);
        if(mAdapter == null){
            mDrug = operation.getdurg(mTag, "order by duetime");
            Log.e("hmq","onResume mTag="+mTag);
            mAdapter = new ListAdapter(mContext, mDrug, mTag);
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        } else if(mAdapter.mFlag != mTag){
            mDrug = operation.getdurg(mTag, "order by duetime");
            mAdapter.mFlag = mTag;
            mAdapter.mDrug = mDrug;
            mAdapter.notifyDataSetChanged();
        }
        
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Log.e("hmq","onClick v="+v.toString()+"; tag="+mTag);
        if (v == mTitleView) {
            final LinearLayout mTextEntryView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.input_edittext, null);
            EditText edit_text = (EditText) mTextEntryView.findViewById(R.id.username_edit);
            if (!isEmptyString((String) mTitleView.getText())) {
                edit_text.setText(mTitleView.getText());
            }
            mAlertDialog = new CareDialog.Builder(mContext).setTitle(R.string.input_title_name).setView(mTextEntryView)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            EditText username = (EditText) mTextEntryView.findViewById(R.id.username_edit);
                            String input_username = username.getText().toString();
                            sp = mContext.getSharedPreferences(DensityUtil.USERNAME_KEY, mContext.MODE_PRIVATE);
                            Editor editor = sp.edit();
                            editor.putString(String.valueOf(mTag), input_username);
                            editor.commit();
                            if (input_username.isEmpty() || input_username.equals("")) {
                                mTitleView.setText(getResources().getString(R.string.drug_box_title_null));
                            } else {
                                mTitleView.setText(input_username);
                            }
                        }
                    }).setNegativeButton(android.R.string.cancel, null).show();
        }else if (v== mButtonDetail){
            int tag = mTag;
            Intent intent = new Intent(mContext, YaoXiangActivity.class);
            intent.putExtra("usergroup", tag);
            mContext.startActivity(intent);
        }
    }

    public boolean isEmptyString(String s) {
        String string = getResources().getString(R.string.drug_box_title_null);
        if (string.equals(s))
            return true;
        return false;
    }

    public class ListAdapter extends BaseAdapter {

        private Context mContext;
        private List<Drug> mDrug;
        private LayoutInflater inflater;
        private SqlOperation operation;
        private int mFlag;

        public ListAdapter(Context context, List<Drug> d, int flag) {
            this.mContext = context;
            this.mDrug = d;
            this.mFlag = flag;
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Log.i("test", "==适配器==" + mDrug.size());
            operation = new SqlOperation(mContext);
        }

        @Override
        public int getCount() {
            if (mDrug == null) {
                return 0;
            }
            int count = mDrug.size() > 4 ? 4 : mDrug.size();
            return count;
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
        public View getView(final int position, View convertView, ViewGroup arg2) {
            smallConvert tag = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.activity_exist_member_item, null);
                tag = new smallConvert();
                tag.mName = (TextView) convertView.findViewById(R.id.member_name);
                tag.mPoint = (ImageView) convertView.findViewById(R.id.member_point);
                tag.mNumber = (TextView) convertView.findViewById(R.id.member_number);
                convertView.setTag(tag);
            }
            tag = (smallConvert) convertView.getTag();
            tag.mName.setText("" + mDrug.get(position).getDname());
            tag.mNumber.setText("" + mDrug.get(position).getNumber());
            
            if (mFlag < 0 && mFlag > color_point.length()) {
                tag.mPoint.setBackground(mContext.getResources().getDrawable(color_point.getResourceId(0, 0)));
            } else {
                tag.mPoint.setBackground(mContext.getResources().getDrawable(color_point.getResourceId(mFlag, 0)));
            }
            
            Calendar endCal = DensityUtil.StringToCalendar(mDrug.get(position).getDuetime());
            Calendar startCal = Calendar.getInstance();
            int indexDay = DensityUtil.getGapCount(startCal, endCal);
            if(indexDay <= 0){
                tag.mName.setTextColor(Color.RED);
                tag.mNumber.setTextColor(Color.RED);
            }
            return convertView;
        }

        private class smallConvert {
            TextView mName;
            TextView mNumber;
            ImageView mPoint;

        }
    }
}