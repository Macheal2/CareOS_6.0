package com.cappu.launcherwin.kookview.assembly;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.BubbleView.OnChildViewClick;
import com.cappu.launcherwin.kookview.AlbumsRelativeLayout;

import android.view.MotionEvent;

public class AssemblyRelativeLayout extends RelativeLayout implements OnChildViewClick{
	private ViewPager viewPager;
	private List<View> lists = new ArrayList<View>();
	private AssemblyAdapter mAssemblyAdapter;
	private RadioGroup radioGroup;
	private RadioButton radioButtonLeft;
	private RadioButton radioButtonMiddle;
	private RadioButton radioButtonRight;
	public AssemblyRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		LayoutInflater.from(context).inflate(R.layout.assembly_view, this);
		initView(context);
	}
	public List<View> getLists(){
		return lists;
	}
	private void initView(Context context){
		lists.add(LayoutInflater.from(context).inflate(R.layout.albums_appwidget, null));
		lists.add(LayoutInflater.from(context).inflate(R.layout.health_appwidget, null));
		lists.add(LayoutInflater.from(context).inflate(R.layout.class_room_appwidget, null));
		mAssemblyAdapter = new AssemblyAdapter(lists);

		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setAdapter(mAssemblyAdapter);
		viewPager.setOnTouchListener( new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}

		});
		viewPager.setCurrentItem(1); 
		radioGroup=(RadioGroup)findViewById(R.id.rg_radio);
		radioButtonLeft=(RadioButton)findViewById(R.id.rb_left);
		radioButtonMiddle=(RadioButton)findViewById(R.id.rb_middle);
		radioButtonRight=(RadioButton)findViewById(R.id.rb_right);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				radioButtonLeft.setTextColor(Color.rgb(44, 44, 44));
				radioButtonMiddle.setTextColor(Color.rgb(44, 44, 44));
				radioButtonRight.setTextColor(Color.rgb(44, 44, 44));
				 switch(checkedId){  
			        case R.id.rb_left:  
			        	radioButtonLeft.setTextColor(Color.WHITE);
			        	viewPager.setCurrentItem(0);  
			            break;  
			        case R.id.rb_middle:  
			        	radioButtonMiddle.setTextColor(Color.WHITE);
			        	viewPager.setCurrentItem(1);  
			            break;  
			        case R.id.rb_right: 
			        	radioButtonRight.setTextColor(Color.WHITE);
			        	viewPager.setCurrentItem(2); 
			            break; 
				 }
			}
		});
	}

	private class AssemblyAdapter extends PagerAdapter {

	    List<View> viewLists;
	    
	    public AssemblyAdapter(List<View> lists)
	    {
	        viewLists = lists;
	    }

	    @Override
	    public int getCount() {                                                           
	        // TODO Auto-generated method stub
	        return viewLists.size();
	    }

	    @Override
	    public boolean isViewFromObject(View arg0, Object arg1) {                         
	        // TODO Auto-generated method stub
	        return arg0 == arg1;
	    }
	    
	    @Override
	    public void destroyItem(View view, int position, Object object)                
	    {
	        ((ViewPager) view).removeView(viewLists.get(position));
	    }
	    
	    @Override
	    public Object instantiateItem(View view, int position)        
	    {
	        ((ViewPager) view).addView(viewLists.get(position), 0);
	        return viewLists.get(position);
	    }

	}

	@Override
	public void onClick(Context c) {
		// TODO Auto-generated method stub
		
	}
}
