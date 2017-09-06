package com.cappu.drugsteward;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.cappu.drugsteward.entity.Member;
import com.cappu.healthmanage.R;
import com.cappu.drugsteward.sqlite.SqlOperation;
import com.cappu.drugsteward.util.MemberAdapter;
import com.cappu.widget.TopBar;

public class ExistMemberActivity extends Activity implements OnClickListener {

    private ListView mList;
    private MemberAdapter mAdapter;
    private List<Member> mFamily;
    private TopBar mTopBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_add);
        SqlOperation openration = new SqlOperation(this);
        mFamily = openration.getmember();
        Log.i("test", "===长度==" + mFamily.size());
        mAdapter = new MemberAdapter(this, mFamily);
        initview();
        mList.setAdapter(mAdapter);
        //TextView member_back = (TextView) findViewById(R.id.member_back);
        //member_back.setOnClickListener(this);

    }

    public void initview() {
        TextView adddetail = (TextView) findViewById(R.id.member_add_detail);
        adddetail.setOnClickListener(this);
        mList = (ListView) findViewById(R.id.exist_member);

        mList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent intent = new Intent(ExistMemberActivity.this, AddMemberActivity.class);
                intent.putExtra("source", "exist");
                intent.putExtra("name", mFamily.get(arg2).getName());
                intent.putExtra("sex", mFamily.get(arg2).getSex());
                intent.putExtra("age", mFamily.get(arg2).getAge());
                startActivity(intent);
                finish();

            }
        });
        
        mTopBar = (TopBar) findViewById(R.id.topbar);
        mTopBar.setOnTopBarListener(new TopBar.onTopBarListener(){
            @Override
            public void onLeftClick(View v){
            	ExistMemberActivity.this.finish();
            }
            @Override
            public void onRightClick(View v){
            }
            @Override
            public void onTitleClick(View v){
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.member_add_detail:
            Intent intent = new Intent(this, AddMemberActivity.class);
            startActivity(intent);

            break;
//        case R.id.member_back:
//            finish();
//            break;
        default:
            break;
        }

    }

}
