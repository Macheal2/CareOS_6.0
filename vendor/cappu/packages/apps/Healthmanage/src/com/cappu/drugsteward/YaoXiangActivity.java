package com.cappu.drugsteward;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.CareDatePickerDialog;
import android.app.CareDialog;
import android.app.ListActivity;
import android.app.CareDatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CareDatePicker;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.drugsteward.entity.Drug;
import com.cappu.healthmanage.R;
import com.cappu.drugsteward.sqlite.SqlOperation;
import com.cappu.drugsteward.util.DensityUtil;
import com.cappu.drugsteward.util.DrugBoxAdapter;
import com.cappu.drugsteward.util.DurgCareDailogView;
import com.cappu.widget.CareButton;
import com.cappu.widget.TopBar;

public class YaoXiangActivity extends ListActivity implements OnClickListener {
    private ListView mList;
    private SqlOperation mOperation;
    private DrugBoxAdapter mAdapter;
    private List<Drug> mDrug;
    private int mUserGroup;
    private TopBar mTopBar;
    private CheckBox mCheckbox;
    private CareButton mDrugAdd;
    private CareDialog mCareDialog;
    private DurgCareDailogView mDurgCareDailog;
    //private final View care_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_add);
        initview();
        mOperation = new SqlOperation(this);
        mDrug = mOperation.getdurg(mUserGroup);
        mAdapter = new DrugBoxAdapter(this, mDrug, mUserGroup);
        setListAdapter(mAdapter);
        
        mAdapter.setCallback(new DrugBoxAdapter.EditModeCallback() {
            public void editModeCallback(boolean status) {
                Drawable drawable;
                if (status == mAdapter.IS_EDIT) {
                    drawable = getBaseContext().getResources().getDrawable(R.drawable.delete_item_icon);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    mDrugAdd.setCompoundDrawables(drawable, null, null, null);
                    mDrugAdd.setText(R.string.delete);
                    mCheckbox.setVisibility(View.INVISIBLE);
                } else {
                    drawable = getBaseContext().getResources().getDrawable(R.drawable.add_item_icon);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    mDrugAdd.setCompoundDrawables(drawable, null, null, null);
                    mDrugAdd.setText(R.string.add);
                    mCheckbox.setVisibility(View.GONE);
                }
            }
        });
//        mList.setOnItemClickListener(new OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                Intent intent = new Intent();
//                intent.setClass(YaoXiangActivity.this, AddDrugDetailActivity.class);
//                Drug d = mDrug.get(arg2);
//                intent.putExtra("drug", d);
//                intent.putExtra("type", "read");
//                intent.putExtra("usergroup", mUserGroup);
//                startActivity(intent);
//
//            }
//        });
    }

    public void initview() {
        mList = getListView();
        Intent intent = getIntent();
        mUserGroup = intent.getIntExtra("usergroup", 0);
        SharedPreferences sp = getSharedPreferences(DensityUtil.USERNAME_KEY, this.MODE_PRIVATE);
        String user_name = sp.getString(String.valueOf(mUserGroup), "");
        
        mCheckbox = (CheckBox) findViewById(R.id.checkbox);
        mDrugAdd = (CareButton) findViewById(R.id.care_add_item);
        mDrugAdd.setOnClickListener(this);
        
        mDurgCareDailog = new DurgCareDailogView(this);
        mDurgCareDailog.setCallback(new DurgCareDailogView.DurgCareDailogCallback() {
            public void dataCallback(Drug info) {
                Drug drug = new Drug(mUserGroup, info.getDname(), info.getNumber(), info.getUnit(), info.getDuetime());
                mOperation.adddrug(drug);
//                mDrug.add(drug);
                mDrug = mOperation.getdurg(mUserGroup);
                mAdapter.setDrug(mDrug);
                mAdapter.notifyDataSetChanged();
                mList.setSelection(mList.getCount());//显示最后一行
            }
        });
        
        mTopBar = (TopBar) findViewById(R.id.topbar);
        if (user_name.isEmpty() || user_name.equals("") || user_name.equals(getResources().getString(R.string.drug_box_title_null))) {
            mTopBar.setText(getResources().getString(R.string.drug_box_title_null));
        } else {
            mTopBar.setText(user_name + getResources().getString(R.string.drug_box_title));
        }
        mTopBar.setOnTopBarListener(new TopBar.onTopBarListener(){
            @Override
            public void onLeftClick(View v){
            	YaoXiangActivity.this.finish();
            }
            @Override
            public void onRightClick(View v){
                mAdapter.setEditable(!mAdapter.getEditable());//topbar 右测删除按键
//                Intent intent = new Intent();
//                intent.setClass(YaoXiangActivity.this, AddDrugDetailActivity.class);
//                intent.putExtra("type", "add");
//                intent.putExtra("usergroup", mUserGroup);
//                startActivity(intent);
//                finish();
            }
            @Override
            public void onTitleClick(View v){
            }
        });
    }

    @Override
    public void onClick(View v) {
//        Intent intent = new Intent();
        switch (v.getId()) {
//        case R.id.drug_add_detail:
//            intent.setClass(this, AddDrugDetailActivity.class);
//            intent.putExtra("type", "add");
//            intent.putExtra("usergroup", mUserGroup);
//            startActivity(intent);
//            finish();
        // break;
        case R.id.care_add_item:
            CareButton button = (CareButton)v;
            if(button.getText().equals(getBaseContext().getResources().getString(R.string.delete))){
                Log.e("hmq","size="+mAdapter.getDeleteList().size());

                
//                new CareDialog.Builder(this)
//                .setTitle(R.string.confirm_export_title)
//                .setMessage(getString(R.string.confirm_export_message,
//                        getTargetFileForDisplay()))
//                .setPositiveButton(android.R.string.ok,
//                        new ExportConfirmationListener(mTargetFileName))
//                .setNegativeButton(android.R.string.cancel, this)
//                .setOnCancelListener(this)
//                .create();
//                
//                CareDialog.Builder(this)
//                .setTitle(contactsAreAvailable
//                        ? R.string.dialog_import_export
//                        : R.string.dialog_import)
//                .setSingleChoiceItems(adapter, -1, clickListener)
//                .create();
                String num = ""+mAdapter.getDeleteList().size();
                new CareDialog.Builder(this)
                        .setTitle(this.getResources().getString(R.string.delete))
                        .setMessage(getString(R.string.delete_message, num))
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i=0; i< mAdapter.getDeleteList().size(); i++){
                                    Log.e("hmq","id="+mAdapter.getDeleteList().get(i).getId());
                                    mOperation.deletedrug(mAdapter.getDeleteList().get(i));
                                }
                                mDrug = mOperation.getdurg(mUserGroup);
                                mAdapter.setDrug(mDrug);
                                mAdapter.setEditable(mAdapter.UN_EDIT);
                            }
                        }).create().show();
            }else{
                mDurgCareDailog.onCreat();
                mDurgCareDailog.onShow();
            }

//            mOperation.deletedrug(mDrug.get(position));
//            mDrug = mOperation.getdurg(mUserGroup);
//            notifyDataSetChanged();
            break;
        }

    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && mAdapter.getEditable()) {
            mAdapter.setEditable(!mAdapter.getEditable());
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
