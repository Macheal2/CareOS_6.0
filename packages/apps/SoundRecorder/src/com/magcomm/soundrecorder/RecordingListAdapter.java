package com.magcomm.soundrecorder;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.soundrecorder.R;

public class RecordingListAdapter extends BaseAdapter{

	private Context mContext;
	private ArrayList<HashMap<String, Object>> mArrayData;
    private static final String PATH = "path";
    private static final String DURATION = "duration";
    private static final String FILE_NAME = "filename";
    private static final String CREAT_DATE = "creatdate";
    private static final String FORMAT_DURATION = "formatduration";
    private static final String RECORD_ID = "recordid";	
	public RecordingListAdapter (Context context , ArrayList<HashMap<String, Object>> data){
		mContext = context ;
		mArrayData = data ;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mArrayData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mArrayData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RecordingViewTAG TAG ;
		if( convertView == null ){
			LayoutInflater inflater = LayoutInflater.from(mContext);
			TAG = new RecordingViewTAG();
			convertView = inflater.inflate(R.layout.mag_recording_list_adapter, null);
			TAG.icon = (ImageView) convertView.findViewById(R.id.record_file_icon);
			TAG.play = (ImageView) convertView.findViewById(R.id.record_file_play);
			TAG.name = (TextView) convertView.findViewById(R.id.record_file_name);
			TAG.title = (TextView) convertView.findViewById(R.id.record_file_title);
			TAG.duration = (TextView)convertView.findViewById(R.id.record_file_duration);
			convertView.setTag(TAG);
		}else{
			TAG = (RecordingViewTAG) convertView.getTag();
		}
		HashMap<String, Object> map = mArrayData.get(position);
		TAG.name.setText(map.get(FILE_NAME).toString());
		TAG.title.setText(map.get(CREAT_DATE).toString());
		TAG.duration.setText(map.get(FORMAT_DURATION).toString());
		
		return convertView;
	}

	public class RecordingViewTAG{
		public ImageView icon;
		public ImageView play;
		public TextView name;
		public TextView title;
		public TextView duration;
	}
}
