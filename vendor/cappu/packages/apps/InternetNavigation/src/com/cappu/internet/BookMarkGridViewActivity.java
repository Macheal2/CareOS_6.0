package com.cappu.internet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
//import com.cappu.internet.widget.TopBar;
//import com.cappu.internet.widget.TopBar.onTopBarListener;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;
import com.cappu.internet.widget.I99ThemeDialogUI;
import android.content.DialogInterface;

import android.content.pm.PackageManager.NameNotFoundException;
import java.lang.reflect.InvocationTargetException;

public class BookMarkGridViewActivity extends Activity {

	private GridView gv;
	private List<Map<String, Object>> data;
	private SQLiteHelper mSqliteHelper;

	private PopupWindow mPopupWindowMenu;
	private LinearLayout mMenuView;

	private int mCurrDelPosition = -1;

	// 实例化监听
	private onTopBarListener mTopBarListener = new onTopBarListener(){
	    public void onLeftClick(View v){
	    	finish();
	    }
	    
	    public void onRightClick(View v){
	    }   
	    
	    public void onTitleClick(View v){
	    }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//market check
		/*if(!checkCareOS()){
			finish();
			return;
		}*/
		
		setContentView(R.layout.bookmark_grid);

		// 构造MyDataBaseAdapter对象
		mSqliteHelper = new SQLiteHelper(this);

		// 取得数据库对象
		mSqliteHelper.open();

		gv = (GridView) findViewById(R.id.bookmark_gv);

		UpdataAdapter();

		gv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Log.e("dengying", "onItemClick");
				
				String sUrl = String.valueOf(data.get(position).get("url"));

				Intent it = new Intent();
				it.setData(Uri.parse(sUrl));
				it.setAction(Intent.ACTION_VIEW);
				// it.addCategory("android.intent.category.BROWSABLE");
				// it.addCategory("android.intent.category.APP_BROWSER");

				// it.setClassName("com.android.browser",
				// "com.android.browser.BrowserActivity");
				
				if (position < (data.size() - 1)) {
					/*if (0 == position) {
						view.setBackgroundResource(R.drawable.web_fenghuang_h);
					} else if (1 == position) {
						view.setBackgroundResource(R.drawable.web_baidu_h);
					} else if (2 == position) {
						view.setBackgroundResource(R.drawable.web_jiankang_h);
					} else if (3 == position) {
						view.setBackgroundResource(R.drawable.web_wzdq_h);
					} else if (4 == position) {
						view.setBackgroundResource(R.drawable.web_jingdong_h);
					} else {
						view.setBackgroundResource(R.drawable.web_bookmark_h);
					}*/

					BookMarkGridViewActivity.this.startActivity(it);
				} else {
					Intent i = new Intent(BookMarkGridViewActivity.this, BookMarkCreateActivity.class);
					startActivity(i);
				}
			}
		});

		gv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				
				Log.e("dengying", "onItemLongClick");
				
				if(data.get(position).get("id")!= null){
					int bookmarkid = Integer.parseInt(String.valueOf(data.get(position).get("id")));
	
					mCurrDelPosition = position;
	
					Log.e("dengying", "position=" + position + " id=" + id + " bookmarkid=" + bookmarkid + " data.size()=" + data.size());
	
					if (bookmarkid > 31 && position < (data.size() - 1)) {
						showPopupWindow();
						return true;
					}
				}
				return false;
			}
		});

		TopBar mTopBar = (TopBar)findViewById(R.id.topbar);
		mTopBar.setText(R.string.desktop_title_browser);
		// 设置监听
		mTopBar.setOnTopBarListener(mTopBarListener);		
		
		/*ImageButton mBtnCancel = (ImageButton) findViewById(R.id.cancel);
		mBtnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});*/
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		UpdataAdapter();
	}

	@Override
	protected void onDestroy() {

		if(mSqliteHelper!=null){
			mSqliteHelper.close();
		}
		
		super.onDestroy();
	}

	public void UpdataAdapter() {
		data = getData();
		MyAdapter adapter = new MyAdapter(this);
		gv.setAdapter(adapter);
	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		HashMap<String, Object> map;

		int i = 1;

		Cursor cur = mSqliteHelper.fetchAllData();

		while (cur.moveToNext()) {
			map = new HashMap<String, Object>();

			map.put("id", cur.getString(cur.getColumnIndex("_id")));

			if (1 == i) {
				map.put("icon", R.drawable.web_kbshw);
			} else if (2 == i) {
				map.put("icon", R.drawable.web_ykt);
			} else if (3 == i) {
				map.put("icon", R.drawable.web_kbqn);
			} else if (4 == i) {
				map.put("icon", R.drawable.web_baidu);
			} else if (5 == i) {
				map.put("icon", R.drawable.web_fenghuang);
			} else if (6 == i) {
				map.put("icon", R.drawable.web_huanqiuwang);
			} else if (7 == i) {
				map.put("icon", R.drawable.web_tiexue);
			} else if (8 == i) {
				map.put("icon", R.drawable.web_tengxunshipin);
			} else if (9 == i) {
				map.put("icon", R.drawable.web_aiqiyi);
			} else if (10 == i) {
				map.put("icon", R.drawable.web_yangshiwang);
			} else if (11 == i) {
				map.put("icon", R.drawable.web_qqmusic);
			} else if (12 == i) {
				map.put("icon", R.drawable.web_wangyiyinyue);
			} else if (13 == i) {
				map.put("icon", R.drawable.web_haodaifu);
			} else if (14 == i) {
				map.put("icon", R.drawable.web_wenyisheng);
			} else if (15 == i) {
				map.put("icon", R.drawable.web_39jiankang);
			} else if (16 == i) {
				map.put("icon", R.drawable.web_weiyiguahao);
			} else if (17 == i) {
				map.put("icon", R.drawable.web_tonghuashun);
			} else if (18 == i) {
				map.put("icon", R.drawable.web_hexunwang);
			}else if (19 == i) {
				map.put("icon", R.drawable.web_caixinwang);
			} else if (20 == i) {
				map.put("icon", R.drawable.web_jingdong);
			} else if (21 == i) {
				map.put("icon", R.drawable.web_tianmao);
			}else if (22 == i) {
				map.put("icon", R.drawable.web_taobao);
			} else if (23 == i) {
				map.put("icon", R.drawable.web_sunning);
			} else if (24 == i) {
				map.put("icon", R.drawable.web_dazhongdianping);
			} else if (25 == i) {
				map.put("icon", R.drawable.web_tongcheng);
			} else if (26 == i) {
				map.put("icon", R.drawable.web_xiecheng);
			} else if (27 == i) {
				map.put("icon", R.drawable.web_tuniu);
			} else if (28 == i) {
				map.put("icon", R.drawable.web_jiemeng);
			} else if (29 == i) {
				map.put("icon", R.drawable.web_choubai);
			} else if (30 == i) {
				map.put("icon", R.drawable.web_pengfu);
			} else if (31 == i) {
				map.put("icon", R.drawable.web_wzdq);
			} else {
				map.put("icon", R.drawable.web_bookmark);
			}

			try {
				byte[] b = cur.getBlob(cur.getColumnIndex("name"));
				String name = new String(b, "UTF-8");
				map.put("name", name);
			} catch (Exception e) {
				// TODO: handle exception
			}

			map.put("url", cur.getString(cur.getColumnIndex("url")));
			list.add(map);
			i++;
		}

		cur.close();

		map = new HashMap<String, Object>();
		map.put("icon", R.drawable.web_tianjia);
		map.put("name", getString(R.string.bookmark_add));
		list.add(map);

		return list;
	}

	static class ViewHolder {
		public TextView bookmark;
	}

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater = null;

		private MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {

			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (convertView == null) {
				holder = new ViewHolder();

				convertView = mInflater.inflate(R.layout.bookmark_griditem, null);

				holder.bookmark = (TextView) convertView.findViewById(R.id.bookmark);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.bookmark.setBackgroundResource((Integer) data.get(position).get("icon"));

			if (position > 30 && position < (data.size() - 1)) {//dengying@20140809 //dengying@20140825
				holder.bookmark.setText((String) data.get(position).get("name"));
			}else{
				holder.bookmark.setText("");
			}

			return convertView;
		}
	}

	private void showPopupWindow() {
		mMenuView = (LinearLayout) View.inflate(this, R.layout.popmenu, null);

		if (mPopupWindowMenu == null) {
			mPopupWindowMenu = new PopupWindow(mMenuView, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			mPopupWindowMenu.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_default));
			mPopupWindowMenu.setFocusable(true);
			mPopupWindowMenu.setAnimationStyle(R.style.menushow);
			mPopupWindowMenu.update();
			mMenuView.setFocusableInTouchMode(true);
		}

		Button btnDelBookMark = (Button) mMenuView.findViewById(R.id.btnDelBookMark);

		btnDelBookMark.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mPopupWindowMenu.isShowing()) {
					mPopupWindowMenu.dismiss();
				}
				//showDelConfirm();
				i99ShowDelConfig();
			}
		});

		mPopupWindowMenu.showAtLocation(mMenuView, Gravity.BOTTOM, 0, 0);
	}

  private void i99ShowDelConfig(){
		  String name = String.valueOf(data.get(mCurrDelPosition).get("name"));
		  String message = (getResources().getString(R.string.bookmark_del_message) + name + "?");

		  final I99ThemeDialogUI dialog = new I99ThemeDialogUI(BookMarkGridViewActivity.this, I99ThemeDialogUI.DIALOG_STYLE_TWO_BUTTONS);
	      dialog.setTitle(R.string.bookmark_del);
		  dialog.setMessage(message);
	      dialog.setPositiveButton(R.string.i99_dialog_right, new android.content.DialogInterface.OnClickListener() {
	      @Override
			public void onClick(DialogInterface listener, int which) {
				mSqliteHelper.deleteData(Integer.parseInt(String.valueOf(data.get(mCurrDelPosition).get("id"))));
				mCurrDelPosition = -1;
				dialog.dismiss();
				UpdataAdapter();
			}
		});
		
	   dialog.show();
  }

	private void showDelConfirm() {

		final AlertDialog dlg = new AlertDialog.Builder(this).create();

		dlg.show();

		Window window = dlg.getWindow();
		window.setContentView(R.layout.dialog_confirm);

		TextView txtMessage = (TextView) window.findViewById(R.id.message);

		String name = String.valueOf(data.get(mCurrDelPosition).get("name"));

		txtMessage.setText(getResources().getString(R.string.bookmark_del_message) + name + "?");

		Button ok = (Button) window.findViewById(R.id.done);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				mSqliteHelper.deleteData(Integer.parseInt(String.valueOf(data.get(mCurrDelPosition).get("id"))));
				mCurrDelPosition = -1;
				dlg.cancel();
				UpdataAdapter();
			}
		});

		Button cancel = (Button) window.findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dlg.cancel();
			}
		});
	}
	
	public boolean checkCareOS() {
		boolean ret = false;

		try {
			ret = APKInstallTools.checkCareOS(this);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return ret;
	}
}