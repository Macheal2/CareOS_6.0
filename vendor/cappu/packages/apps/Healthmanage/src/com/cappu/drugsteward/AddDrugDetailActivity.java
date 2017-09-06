package com.cappu.drugsteward;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.drugsteward.entity.Drug;
import com.cappu.drugsteward.entity.Member;
import com.cappu.healthmanage.R;
import com.cappu.drugsteward.sqlite.SqlOperation;
import com.cappu.widget.TopBar;

public class AddDrugDetailActivity extends Activity implements OnClickListener {

    private DatePickerDialog date;
    Calendar ca = Calendar.getInstance();
    private TextView production;
    private TextView duetime;
    private EditText mDname;
    private String dname;
    private Spinner member;
    private TextView dateupdate;
    private String type;
    //private TextView mSaveDrug;
    private EditText drugnumber;
    private EditText drugremark;
    private EditText drugcompany;
    private TextView youxiaotime;
    String mname;
    private SqlOperation operation;
    List<String> mFamily = new ArrayList<String>();
    private Drug mDrug;
    private int mUserGroup;
    private TopBar mTopBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_detail);
        Intent intent = getIntent();
        mDrug = intent.getParcelableExtra("drug");
        dname = intent.getStringExtra("dname");
        type = intent.getStringExtra("type");
        mUserGroup = intent.getIntExtra("usergroup", 0);
        operation = new SqlOperation(this);
        List<Member> mMs = operation.getmember();
        for (int i = 0; i < mMs.size(); i++) {
            mFamily.add(mMs.get(i).getName());
        }
        initview();
//        if (type != null && type.equals("read")) {
//            mSaveDrug.setText("修改");
//        }

    }

    // 初始化控件
    public void initview() {

        member = (Spinner) findViewById(R.id.drug_sppinner);
        dateupdate = (TextView) findViewById(R.id.date_update);
        youxiaotime = (TextView) findViewById(R.id.youxiao_update);
        mDname = (EditText) findViewById(R.id.drug_dname);
        production = (TextView) findViewById(R.id.production_time);
        duetime = (TextView) findViewById(R.id.due_time);
        //mSaveDrug = (TextView) findViewById(R.id.drug_save);
        drugnumber = (EditText) findViewById(R.id.drug_number);
        drugremark = (EditText) findViewById(R.id.drug_remark);
        drugcompany = (EditText) findViewById(R.id.drug_company);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mFamily);
        member.setAdapter(adapter);
        dateupdate.setOnClickListener(this);
        youxiaotime.setOnClickListener(this);
        //mSaveDrug.setOnClickListener(this);

        if (type.equals("add")) {
            mDname.setClickable(true);
        } else if (type.equals("read")) {
            setvalues();
        } else {
            mDname.setText(dname);
            mDname.setClickable(false);
            mDname.setFocusable(false);
        }
        int y = ca.get(Calendar.YEAR);
        int m = ca.get(Calendar.MONTH);
        int d = ca.get(Calendar.DAY_OF_MONTH);
        production.setText(y + "-" + (m + 1) + "-" + d + "");

        duetime.setText(y + "-" + (m + 1) + "-" + d + "");
        // member.setSelection(position, animate)
        member.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> ada, View arg1, int arg2, long arg3) {
                mname = ada.getItemAtPosition(arg2).toString();
                // shopname=arg0.getItemAtPosition(arg2).toString();//传递门店
                Toast.makeText(AddDrugDetailActivity.this, "===你选择的是==" + mname, 0).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        
        mTopBar = (TopBar) findViewById(R.id.topbar);
        mTopBar.setOnTopBarListener(new TopBar.onTopBarListener(){
            @Override
            public void onLeftClick(View v){
                finish();
            }
    
            @Override
            public void onRightClick(View v){
            	Intent intent = new Intent();
            	if (type != null && type.equals("read")) {
                    Drug d = getsave();
                    if (d != null) {
                        d.setId(mDrug.getId());
                        operation.updatedurg(d);
                        intent.setClass(AddDrugDetailActivity.this, YaoXiangActivity.class);
                        intent.putExtra("usergroup", mUserGroup);
                        startActivity(intent);
                        finish();
                    }

                } else {
                    String dnameb = mDname.getText().toString();
                    if (!operation.selectsingle(dnameb)) {
                        Drug d = getsave();
                        if (d != null) {
                            operation.adddrug(d);
                            Log.i("test", "添加了没有");
                            intent.setClass(AddDrugDetailActivity.this, YaoXiangActivity.class);
                            intent.putExtra("usergroup", mUserGroup);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(AddDrugDetailActivity.this, "此药品已经存在药库", 0).show();
                    }
                }
            }

            @Override
            public void onTitleClick(View v){

            }    
        });
    }

    // 修改时间
    public void showdate(final String s) {

        date = new DatePickerDialog(this, new OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date.cancel();
                if (s.equals("p")) {
                    production.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                } else {
                    duetime.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                }

            }
        }, ca.get(Calendar.YEAR), ca.get(Calendar.MONTH), ca.get(Calendar.DAY_OF_MONTH));
        date.show();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
        case R.id.date_update:
            showdate("p");
            break;
        case R.id.youxiao_update:
            showdate("");
            break;
//        case R.id.drug_save:
//            if (type != null && type.equals("read")) {
//                Drug d = getsave();
//                if (d != null) {
//                    d.setId(mDrug.getId());
//                    operation.updatedurg(d);
//                    intent.setClass(AddDrugDetailActivity.this, YaoXiangActivity.class);
//                    intent.putExtra("usergroup", mUserGroup);
//                    startActivity(intent);
//                    finish();
//                }
//
//            } else {
//                String dnameb = mDname.getText().toString();
//                if (!operation.selectsingle(dnameb)) {
//                    Drug d = getsave();
//                    if (d != null) {
//                        operation.adddrug(d);
//                        Log.i("test", "添加了没有");
//                        intent.setClass(AddDrugDetailActivity.this, YaoXiangActivity.class);
//                        intent.putExtra("usergroup", mUserGroup);
//                        startActivity(intent);
//                        finish();
//                    }
//                } else {
//                    Toast.makeText(AddDrugDetailActivity.this, "此药品已经存在药库", 0).show();
//                }
//            }
//            break;
        }

    }

    // 得到要保存的值
    public Drug getsave() {

        String dnamea = mDname.getText().toString();
        String ptime = production.getText().toString();
        String dtime = duetime.getText().toString();
        int number = Integer.parseInt(drugnumber.getText().toString());
        String dcompany = drugcompany.getText().toString();
        String remark = drugremark.getText().toString();
        String o = (String) member.getSelectedItem();
        Log.i("test", ptime + "==producetime==member==" + number + "==" + o);
        if (dnamea == null || dnamea.equals("")) {
            Toast.makeText(AddDrugDetailActivity.this, "药品名称不能为空", 0).show();
            return null;
        } else if (number == 0) {
            Toast.makeText(AddDrugDetailActivity.this, "药品数量不能为0", 0).show();
            return null;
        } else {
//            Drug d = new Drug(dnamea, ptime, dtime, number, o, dcompany, remark, mUserGroup);
            String unit = "";
            Drug d = new Drug(mUserGroup, dnamea, number, unit, dtime);
            
            return d;
        }

    }

    public void setvalues() {
        mDname.setText(mDrug.getDname());
        //production.setText(mDrug.getProducttime());
        duetime.setText(mDrug.getDuetime());
        drugnumber.setText(mDrug.getNumber() + "");
//        if (mDrug.getCompany() != null) {
//            drugcompany.setText(mDrug.getCompany());
//        }
//        if (mDrug.getRemark() != null) {
//            drugremark.setText(mDrug.getRemark());
//        }
//        for (int i = 0; i < mFamily.size(); i++) {
//            if (mFamily.get(i).equals(mDrug.getMember())) {
//                member.setSelection(i, true);
//            }
//        }
    }

}
