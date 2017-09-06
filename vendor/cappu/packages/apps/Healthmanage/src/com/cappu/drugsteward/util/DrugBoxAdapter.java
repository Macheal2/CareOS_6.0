package com.cappu.drugsteward.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.R.anim;
import android.R.integer;
/*import com.cappu.widget.CareDatePickerDialog;
import com.cappu.widget.CareDatePickerDialog.OnDateSetListener;
import com.cappu.widget.CareDatePicker;*/
import android.app.CareDatePickerDialog;
import android.app.CareDatePickerDialog.OnDateSetListener;
import android.app.CareDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
//import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CareDatePicker;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.cappu.drugsteward.AddDrugDetailActivity;
import com.cappu.drugsteward.entity.Drug;
import com.cappu.drugsteward.example.xing.activity.CaptureActivity;
import com.cappu.healthmanage.R;
import com.cappu.drugsteward.sqlite.SqlOperation;
import com.cappu.drugsteward.util.DurgCareDailogView.DurgCareDailogCallback;

public class DrugBoxAdapter extends BaseAdapter implements OnClickListener {

    private Context mContext;
    private List<Drug> mDrug;
    private LayoutInflater inflater;
    private SqlOperation mOperation;
    private int mUserGroup;
    private boolean mIsEditable;
    private CareDialog mCreatItem = null;
    private DurgCareDailogView mDurgCareDailog;
    public EditModeCallback mCallback;//回调函数是否可编辑状态
    public final static boolean UN_EDIT = false;
    public final static boolean IS_EDIT = true;
    
    public  HashMap<Integer, Boolean> mIsCheck;
    private List<Drug> mDeleteArray = new ArrayList<Drug>();
    
    public interface EditModeCallback {  
        public void editModeCallback(boolean status);
      }
    
    public void setCallback(EditModeCallback callback) { 
        this.mCallback = callback; 
      }
    
    public DrugBoxAdapter(Context context, List<Drug> d, int user_group) {
        this.mContext = context;
        this.mDrug = d;
//        mLastItem = this.mDrug.size();
        //this.mDrug.add(addNullItem());
        mUserGroup = user_group;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Log.e("test", "==适配器==" + mDrug.size());
        mOperation = new SqlOperation(mContext);
        mDeleteArray.clear();
        
        mIsCheck = new HashMap<Integer, Boolean>();
        for (int i = 0; i < mDrug.size(); i++) {
            mIsCheck.put(i, false);
        }
        // mColorPoints =
        // context.getResources().obtainTypedArray(R.array.array_color_point_drawables);
    }

    @Override
    public int getCount() {
        if (mDrug == null) {
            return 0;
        }
        return mDrug.size();
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
        DrugBoxTag tag = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_drug_box_item, null);
            tag = new DrugBoxTag();
            tag.mItemLinear = (LinearLayout) convertView.findViewById(R.id.item_linear);
            tag.mColorPoint = (ImageView) convertView.findViewById(R.id.color_point);
            tag.mName = (TextView) convertView.findViewById(R.id.drug_box_name);
            tag.mNumber = (TextView) convertView.findViewById(R.id.drug_box_number);
            tag.mUnit = (TextView) convertView.findViewById(R.id.drug_box_unit);
            tag.mDuetime = (TextView) convertView.findViewById(R.id.drug_box_duetime);
            tag.mDelete = (CheckBox) convertView.findViewById(R.id.del_check_box);
            tag.mDelete.setChecked(mIsCheck.get(position));
            convertView.setTag(tag);
        } else {
            tag = (DrugBoxTag) convertView.getTag();
            tag.mName.setText(mDrug.get(position).getDname());
            tag.mNumber.setText("" + (mDrug.get(position).getNumber() < 1 ? "" : mDrug.get(position).getNumber()));
            tag.mUnit.setText(mDrug.get(position).getUnit());
            tag.mDuetime.setText(mDrug.get(position).getDuetime());
            tag.mDelete.setVisibility(mIsEditable ? View.VISIBLE : View.GONE);
            tag.mDelete.setChecked(mIsCheck.get(position));
            
            tag.mItemLinear.setTag(position);
            tag.mDelete.setTag(position);

            Calendar endCal = DensityUtil.StringToCalendar(mDrug.get(position).getDuetime());
            Calendar startCal = Calendar.getInstance();
            int indexDay = DensityUtil.getGapCount(startCal, endCal);
            if (indexDay <= 0) {
//                tag.mColorPoint.setBackground(mContext.getResources().getDrawable(R.drawable.img_point_red));
                tag.mColorPoint.setBackgroundColor(Color.RED);
                tag.mName.setTextColor(Color.RED);
                tag.mNumber.setTextColor(Color.RED);
                tag.mUnit.setTextColor(Color.RED);
                tag.mDuetime.setTextColor(Color.RED);
            } else {
//                tag.mColorPoint.setBackground(mContext.getResources().getDrawable(R.drawable.img_point_grey));
                tag.mColorPoint.setBackgroundColor(Color.BLUE);
                tag.mName.setTextColor(Color.BLACK);
                tag.mNumber.setTextColor(Color.BLACK);
                tag.mUnit.setTextColor(Color.BLACK);
                tag.mDuetime.setTextColor(Color.BLACK);
            }
            return convertView;
        }

        tag.mName.setText(mDrug.get(position).getDname());
        tag.mNumber.setText("" + (mDrug.get(position).getNumber() < 1 ? "" : mDrug.get(position).getNumber()));
        tag.mUnit.setText(mDrug.get(position).getUnit());
        tag.mDuetime.setText(mDrug.get(position).getDuetime());

        tag.mDelete.setVisibility(mIsEditable ? View.VISIBLE : View.GONE);

        Calendar endCal = DensityUtil.StringToCalendar(mDrug.get(position).getDuetime());
        Calendar startCal = Calendar.getInstance();
        int indexDay = DensityUtil.getGapCount(startCal, endCal);
        if (indexDay <= 0) {
//            tag.mColorPoint.setBackground(mContext.getResources().getDrawable(R.drawable.img_point_red));
            tag.mColorPoint.setBackgroundColor(Color.RED);
            tag.mName.setTextColor(Color.RED);
            tag.mNumber.setTextColor(Color.RED);
            tag.mUnit.setTextColor(Color.RED);
            tag.mDuetime.setTextColor(Color.RED);
        } else {
//            tag.mColorPoint.setBackground(mContext.getResources().getDrawable(R.drawable.img_point_grey));
            tag.mColorPoint.setBackgroundColor(Color.BLUE);
            tag.mName.setTextColor(Color.BLACK);
            tag.mNumber.setTextColor(Color.BLACK);
            tag.mUnit.setTextColor(Color.BLACK);
            tag.mDuetime.setTextColor(Color.BLACK);
        }

        tag.mItemLinear.setTag(position);
        tag.mDelete.setTag(position);
        
        tag.mItemLinear.setOnClickListener(this);
        tag.mItemLinear.setOnLongClickListener(new Onlongclick());
        tag.mDelete.setOnClickListener(this);
        return convertView;
    }

    private class DrugBoxTag {
        LinearLayout mItemLinear;
        ImageView mColorPoint;
        TextView mName;
        TextView mNumber;
        TextView mUnit;
        TextView mDuetime;
        CheckBox mDelete;

    }

    public boolean getEditable() {
        return mIsEditable;
    }

    public void setEditable(boolean status) {
        mIsEditable = status;
        mCallback.editModeCallback(mIsEditable);
        notifyDataSetChanged();
    }

    public List<Drug> getDeleteList(){
        return mDeleteArray;
    }
    
    public void setDrug(List<Drug> drug){
        this.mDrug = drug;
        for (int i = 0; i < mDrug.size(); i++) {
            mIsCheck.put(i, false);
        }
    }
    
    @Override
    public void onClick(View v) {
        if (mIsEditable == UN_EDIT) return;
        int position = Integer.valueOf(String.valueOf(v.getTag())).intValue();
        
        switch (v.getId()) {
        case R.id.del_check_box:
            if (((CheckBox)v).isChecked()) {
                mDeleteArray.add(mDrug.get(position));
                mIsCheck.put(position, true);
            } else {
                mDeleteArray.remove(mDrug.get(position));
                mIsCheck.put(position, false);
            }
            
            if (mDeleteArray.size() == 0){
                mIsEditable = UN_EDIT;
                mCallback.editModeCallback(mIsEditable);
                notifyDataSetChanged();
            }
            break;
        case R.id.item_linear:
            mDurgCareDailog = new DurgCareDailogView(mContext, mDrug.get(position));
            mDurgCareDailog.onCreat();
            mDurgCareDailog.onShow();
            
            mDurgCareDailog.setCallback(new DurgCareDailogView.DurgCareDailogCallback() {
                public void dataCallback(Drug info) {
                    mOperation.updatedurg(info);
                    notifyDataSetChanged();
                }
            });
        default:
            break;

        }
    }
    
    class Onlongclick implements OnLongClickListener {

        public boolean onLongClick(View v) {
            // TODO Auto-generated method stub
            if (mIsEditable == IS_EDIT) return true;
            mIsEditable = IS_EDIT;
            mDeleteArray.clear();
            mCallback.editModeCallback(mIsEditable);
            for (int i = 0; i < mDrug.size(); i++) {
                mIsCheck.put(i, false);
            }
            notifyDataSetChanged();
            return true;
        }
    }
}
