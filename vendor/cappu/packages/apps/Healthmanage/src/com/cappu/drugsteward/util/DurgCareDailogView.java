package com.cappu.drugsteward.util;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.cappu.drugsteward.entity.Drug;
import com.cappu.healthmanage.R;
import android.R.integer;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CareDatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.app.CareDialog;
/*import com.cappu.widget.CareDatePickerDialog;
import com.cappu.widget.CareDatePickerDialog.OnDateSetListener;
import com.cappu.widget.CareDatePicker;*/
import android.app.CareDatePickerDialog;
import android.app.CareDatePickerDialog.OnDateSetListener;


public class DurgCareDailogView implements OnClickListener {
    private Context mContext;
    private CareDialog mCareDialog;
    private Drug mDrug;
    private int mEditStyle;
    private ArrayAdapter<String> mAdapter;
    private final static int CREAT = 1;
    private final static int UPDATE = 2;
    
    public DurgCareDailogCallback mCallback;
    
    public interface DurgCareDailogCallback {
        public void dataCallback(Drug drug);
      }
    
    public void setCallback(DurgCareDailogCallback callback) { 
        this.mCallback = callback; 
      } 
    
    public DurgCareDailogView(Context context) {
        //super(context);
        // TODO Auto-generated constructor stub
        mContext = context;
        mDrug = new Drug();
        mEditStyle = CREAT;
    }

    public DurgCareDailogView(Context context, Drug drug) {
        mContext = context;
        this.mDrug = drug;
        mEditStyle = UPDATE;
    }
    
    public void onCreat(){
        String[] unitArray = mContext.getResources().getStringArray(R.array.unit_array);
        final View care_dialog = LayoutInflater.from(mContext).inflate(R.layout.care_dialog, null);
        
        Button date = (Button)care_dialog.findViewById(R.id.add_date_edit);
        date.setOnClickListener(this);
        date.setInputType(InputType.TYPE_NULL);
        
        mAdapter = new UnitArrayAdapter(mContext, unitArray);
        Spinner unit_view = (Spinner)care_dialog.findViewById(R.id.add_unit_spinner);
        unit_view.setAdapter(mAdapter);
        if(mEditStyle == UPDATE){
            EditText name_view = (EditText)care_dialog.findViewById(R.id.add_name_edit);
            EditText number_view = (EditText)care_dialog.findViewById(R.id.add_number_edit);

            name_view.setText(this.mDrug.getDname());
            number_view.setText(this.mDrug.getNumber()+"");
            int i = 0;
            for (; i < unitArray.length; i++){
                if(unitArray[i].equals(this.mDrug.getUnit()))
                    break;
            }
            unit_view.setSelection(i, true);
            date.setText(this.mDrug.getDuetime());
        }
        mCareDialog = new CareDialog.Builder(mContext).setTitle(mContext.getResources().getString(R.string.add_drug)).setView(care_dialog)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Settings.this.finish();
                        try {
                            Field field = mCareDialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // SpeechTools.setSpeaker(Settings.this, mSpeaker.speaker);
                        // Settings.this.finish();
                        EditText name = (EditText)care_dialog.findViewById(R.id.add_name_edit);
                        EditText number = (EditText)care_dialog.findViewById(R.id.add_number_edit);
                        Spinner unit = (Spinner)care_dialog.findViewById(R.id.add_unit_spinner);
                        Button date = (Button)care_dialog.findViewById(R.id.add_date_edit);
                        
                        if(hasAddItem(name.getText().toString(), number.getText().toString(), unit.getSelectedItem().toString(), date.getText().toString())){
                            //保存
                            String dname = name.getText().toString();
                            
                            String duetime = date.getText().toString();
                            String numberS = number.getText().toString();
                            String unitS = unit.getSelectedItem().toString();//unit.getText().toString();
                            
                            int numberInt = 0;
                            try {
                                numberInt = Integer.valueOf(numberS).intValue();
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                numberInt = 0;
                            }
                            mDrug.setDname(dname);
                            mDrug.setNumber(numberInt);
                            mDrug.setUnit(unitS);
                            mDrug.setDuetime(duetime);
                            mCallback.dataCallback(mDrug);
                            try {
                                Field dailogClose = mCareDialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                dailogClose.setAccessible(true);
                                dailogClose.set(dialog, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Field dailogHold = mCareDialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                dailogHold.setAccessible(true);
                                dailogHold.set(dialog, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).create();
        mCareDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
    }
    
    public void onShow(){
        this.mCareDialog.show();
    }
    
    /*
     * 自定义caredailog
     * 判断输入的内容是否都符合规定
     */
    private boolean hasAddItem(String name, String number, String unit, String date){
        Log.e("hmq","hasAddItem");
        if (name.isEmpty()){
            Toast.makeText(mContext, mContext.getResources().getString(R.string.drug_box_name_not_null) ,Toast.LENGTH_LONG).show();
            return false;
        }
        
        int num = 0;
        if(number.isEmpty()){
            Toast.makeText(mContext, mContext.getResources().getString(R.string.drug_box_number_not_null) ,Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            num = Integer.valueOf(number).intValue();
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(mContext, mContext.getResources().getString(R.string.drug_box_number_error) ,Toast.LENGTH_SHORT).show();
            return false;
        }
        if (num < 1 && num > 999){
            Toast.makeText(mContext, mContext.getResources().getString(R.string.drug_box_number_limit) ,Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (unit.isEmpty()){
            Toast.makeText(mContext, mContext.getResources().getString(R.string.drug_box_unit_not_null) ,Toast.LENGTH_SHORT).show();
            return false;
        }

        if (date.isEmpty()){
            Toast.makeText(mContext, mContext.getResources().getString(R.string.drug_box_date_not_null) ,Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    
    @Override
    public void onClick(final View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.add_date_edit:
                Log.e("hmq","add_date_edit");
    
                Calendar calendar = Calendar.getInstance();
                new CareDatePickerDialog(mContext, new OnDateSetListener() {
    
                    @Override
                    public void onDateSet(CareDatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        Calendar calendar_now = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String afterString = DensityUtil.CalendarToString(calendar);
    
                        int result = calendar.compareTo(calendar_now);
                        if (result > 0) {
                            ((Button)v).setText(afterString);
                        } else {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.drug_box_date_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
            default:
                break;
        }
    }

    class UnitArrayAdapter extends ArrayAdapter<String> {
        private Context mContext;
        private String[] mStringArray;

        public UnitArrayAdapter(Context context, String[] stringArray) {
            super(context, android.R.layout.simple_spinner_item, stringArray);
            mContext = context;
            mStringArray = stringArray;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            // 修改Spinner展开后的字体颜色
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }
            // 此处text1是Spinner默认的用来显示文字的TextView
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(mStringArray[position]);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 修改Spinner选择后结果的字体颜色
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            // 此处text1是Spinner默认的用来显示文字的TextView
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(mStringArray[position]);
            // tv.setTextSize(mContext.getResources().getDimension(R.dimen.care_text_size_large));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            tv.setBackgroundResource(R.drawable.search_edittext_bg);
            tv.setGravity(Gravity.RIGHT);
            return convertView;
        }
    }

}
