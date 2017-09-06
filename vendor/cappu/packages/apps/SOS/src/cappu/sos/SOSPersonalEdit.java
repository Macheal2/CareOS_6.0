package cappu.sos;

import cappu.sos.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

//import cappu.sos.widget.TopBar;
//import cappu.sos.widget.TopBar.onTopBarListener;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;
import android.text.InputType;
import android.widget.RadioGroup.OnCheckedChangeListener; 

import android.widget.RadioButton;
import android.util.Log;

public class SOSPersonalEdit extends Activity {

	SharedPreferences sharedPreferences;
    //boolean isEdit = false;
    
	public RadioButton rbMale;
	public RadioButton rbFemale;	
	public RadioGroup rgSex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_info);
		
		initView();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}	
		
	private void initView() {

		//TextView txtTitle = (TextView) findViewById(R.id.title);
		//txtTitle.setText(R.string.edit_title);

		sharedPreferences = getSharedPreferences("sospref", Context.MODE_PRIVATE);

		String name = sharedPreferences.getString("name", "");
		String sex = sharedPreferences.getString("sex", "");
		String age = sharedPreferences.getString("age", "");
		String mobile = sharedPreferences.getString("mobile", "");
		String address = sharedPreferences.getString("address", "");
		String note = sharedPreferences.getString("note", "");

		final EditText txtName = (EditText) findViewById(R.id.name_editor);
		//final EditText txtSex = (EditText) findViewById(R.id.sex_editor);
		final EditText txtAge = (EditText) findViewById(R.id.age_editor);
		final EditText txtMobile = (EditText) findViewById(R.id.mobile_editor);
		final EditText txtAddress = (EditText) findViewById(R.id.address_editor);
		final EditText txtNote = (EditText) findViewById(R.id.note_editor);

		rbMale=(RadioButton)findViewById(R.id.male);  
	    rbFemale=(RadioButton)findViewById(R.id.female); 
		rgSex = (RadioGroup)findViewById(R.id.sex);

        txtMobile.setInputType(InputType.TYPE_CLASS_PHONE);
        txtAge.setInputType(InputType.TYPE_CLASS_PHONE);

		txtName.setText(name);
		//txtSex.setText(sex);
		txtAge.setText(age);
		txtMobile.setText(mobile);
		txtAddress.setText(address);
		txtNote.setText(note);

		if(sex.equals("1")){
			rbMale.setChecked(true);
			rbFemale.setChecked(false);
		}else{
			rbMale.setChecked(false);
			rbFemale.setChecked(true);		
		}

		rgSex.setOnCheckedChangeListener(new OnCheckedChangeListener(){  
			  
            public void onCheckedChanged(RadioGroup arg0, int checkedId) {  
                Editor editor = sharedPreferences.edit();  
                  
                if(checkedId==rbMale.getId()){  
                      editor.putString("sex", "1");
                }  
                  
                if(checkedId==rbFemale.getId()){  
                      editor.putString("sex", "2"); 
                }  
                
                editor.commit();
            }   
        }); 		
		
		TopBar mTopBar = (TopBar)findViewById(R.id.topbar);
		
		// 实例化监听
		onTopBarListener mTopBarListener = new onTopBarListener(){
		    public void onLeftClick(View v){
		    	finish();
		    }
		    
		    public void onRightClick(View v){
				/*if(!isEdit){
					//btnSave.setImageResource(R.drawable.icon_save);
					isEdit = true;
				}else{*/				
					Editor editor = sharedPreferences.edit();
					editor.putString("name", txtName.getText().toString());
					//editor.putString("sex", txtSex.getText().toString());
					editor.putString("age", txtAge.getText().toString());
					editor.putString("mobile", txtMobile.getText().toString());
					editor.putString("address", txtAddress.getText().toString());
					editor.putString("note", txtNote.getText().toString());

					editor.commit();
					finish();
				//}
		    }   
		    
		    public void onTitleClick(View v){
		    }
		};
		
		// 设置监听
		mTopBar.setOnTopBarListener(mTopBarListener);
		
		/*ImageButton btnCancel = (ImageButton) findViewById(R.id.cancel);

		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		final ImageButton btnSave = (ImageButton) findViewById(R.id.save);

		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				if(!isEdit){
					btnSave.setImageResource(R.drawable.icon_save);
					isEdit = true;
				}else{				
					Editor editor = sharedPreferences.edit();
					editor.putString("name", txtName.getText().toString());
					editor.putString("sex", txtSex.getText().toString());
					editor.putString("age", txtAge.getText().toString());
					editor.putString("mobile", txtMobile.getText().toString());
					editor.putString("address", txtAddress.getText().toString());
					editor.putString("note", txtNote.getText().toString());
	
					editor.commit();
					finish();
				//}
			}
		});*/

	}

}
