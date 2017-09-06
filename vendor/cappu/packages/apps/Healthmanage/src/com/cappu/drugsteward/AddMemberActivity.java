package com.cappu.drugsteward;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.drugsteward.entity.Member;
import com.cappu.healthmanage.R;
import com.cappu.drugsteward.sqlite.SqlOperation;
import com.cappu.widget.TopBar;

public class AddMemberActivity extends Activity implements OnClickListener{
    
    private EditText mname;
    private EditText msex;
    private EditText mage;
    private Button commit;
    private SqlOperation operation;
    private TopBar mTopBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_detail);
       initview();
       Intent intent=getIntent();
       String source=intent.getStringExtra("source");
       if(source!=null&&!source.equals("")){
           String name=intent.getStringExtra("name");
           String sex=intent.getStringExtra("sex");
           int age=intent.getIntExtra("age",0);
           mname.setText(name+"");
           msex.setText(sex+"");
           mage.setText(age+"");
       }
       operation = new SqlOperation(this);
       
    }
    
    //初始化控件
    public void initview(){
        mname = (EditText) findViewById(R.id.member_name);
        msex = (EditText) findViewById(R.id.member_sex);
        mage = (EditText) findViewById(R.id.member_age);
        commit = (Button) findViewById(R.id.member_commit);
        //TextView back=(TextView) findViewById(R.id.back);
        //back.setOnClickListener(this);
        commit.setOnClickListener(this);
        mTopBar = (TopBar) findViewById(R.id.topbar);
        mTopBar.setOnTopBarListener(new TopBar.onTopBarListener(){
            @Override
            public void onLeftClick(View v){
            	AddMemberActivity.this.finish();
            }
            @Override
            public void onRightClick(View v){
            }
            @Override
            public void onTitleClick(View v){
            }
        });
    }
    
    //添加成员
    public void addmember(){
        String name=mname.getText().toString();
        String sex=msex.getText().toString();
        int age=Integer.parseInt(mage.getText().toString());
        if(name==null|| name.equals("")){
            Toast.makeText(this,"姓名不能为空",0).show();
        }else{
            Member m=new Member(name, sex, age);
            operation.addmember(m);
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent=new Intent();
        switch (view.getId()) {
//        case R.id.back:
//            finish();
//            break;
        case R.id.member_commit:
            addmember();
            intent.setClass(this, ExistMemberActivity.class);
            startActivity(intent);
            finish();
            break;
          }
      
        
    }
    
    

}
