package com.cappu.internet;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookMarkListViewActivity extends Activity {

	private ListView lv;
	private List<Map<String, Object>> data;
	private SQLiteHelper mSqliteHelper;

	private PopupWindow mPopupWindowMenu;
	private LinearLayout mMenuView;

	private int mCurrDelPosition = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bookmark_listview);

		// 构造MyDataBaseAdapter对象
		mSqliteHelper = new SQLiteHelper(this);

		// 取得数据库对象
		mSqliteHelper.open();

		lv = (ListView) findViewById(R.id.bookmark_lv);

		View footerView = getLayoutInflater().inflate(R.layout.bookmark_listitem, lv, false);
		ImageView bookmark_icon = (ImageView) footerView.findViewById(R.id.bookmark_icon);
		TextView bookmark_name = (TextView) footerView.findViewById(R.id.bookmark_name);
		bookmark_icon.setBackgroundResource(R.drawable.butt_fastopen_add);
		bookmark_name.setText(R.string.bookmark_add);
		RelativeLayout bookmark_add = (RelativeLayout) footerView.findViewById(R.id.bookmark_list);
		bookmark_add.setOnClickListener(footerViewListener);

		lv.addFooterView(footerView);

		UpdataAdapter();

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				String sUrl = String.valueOf(data.get(position).get("url"));

				Intent it = new Intent();
				it.setData(Uri.parse(sUrl));
				it.setAction(Intent.ACTION_VIEW);
				// it.addCategory("android.intent.category.BROWSABLE");
				// it.addCategory("android.intent.category.APP_BROWSER");

				// it.setClassName("com.android.browser",
				// "com.android.browser.BrowserActivity");

				BookMarkListViewActivity.this.startActivity(it);
			}
		});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				int bookmarkid = Integer.parseInt(String.valueOf(data.get(position).get("id")));

				mCurrDelPosition = position;

				if (bookmarkid > 5) {
					showPopupWindow();
				}
				return false;
			}
		});

		ImageButton mBtnCancel = (ImageButton) findViewById(R.id.cancel);
		mBtnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		UpdataAdapter();
	}

	@Override
	protected void onDestroy() {

		mSqliteHelper.close();

		super.onDestroy();
	}

	public void UpdataAdapter() {
		data = getData();
		MyAdapter adapter = new MyAdapter(this);
		lv.setAdapter(adapter);
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
				map.put("icon", R.drawable.webmark_browser);
			} else if (2 == i) {
				map.put("icon", R.drawable.webmark_jingdong);
			} else if (3 == i) {
				map.put("icon", R.drawable.webmark_yisou);
			} else if (4 == i) {
				map.put("icon", R.drawable.webmark_wzdq);
			} else if (5 == i) {
				map.put("icon", R.drawable.webmark_fenghuang);
			} else {
				map.put("icon", R.drawable.webmark_bookmark);
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

		return list;
	}

	static class ViewHolder {
		public ImageView icon;
		public TextView name;
		public TextView info;
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

				convertView = mInflater.inflate(R.layout.bookmark_listitem, null);
				holder.icon = (ImageView) convertView.findViewById(R.id.bookmark_icon);
				holder.name = (TextView) convertView.findViewById(R.id.bookmark_name);
				// holder.info = (TextView)convertView.findViewById(R.id.info);

				/*
				 * LayoutParams para = holder.icon.getLayoutParams();
				 * holder.icon.setLayoutParams(para); para.height = 300;
				 * para.width = 300; holder.icon.setLayoutParams(para);
				 * holder.icon.setPadding(8, 8, 8, 8);
				 */

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.icon.setBackgroundResource((Integer) data.get(position).get("icon"));
			holder.name.setText((String) data.get(position).get("name"));
			// holder.info.setText((String)data.get(position).get("info"));

			return convertView;
		}
	}

	private OnClickListener footerViewListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {

			Intent i = new Intent(BookMarkListViewActivity.this, BookMarkCreateActivity.class);
			startActivity(i);
		}

	};

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
				showDelConfirm();
			}
		});

		mPopupWindowMenu.showAtLocation(mMenuView, Gravity.BOTTOM, 0, 0);
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
}