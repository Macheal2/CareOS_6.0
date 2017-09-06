package com.joy.network.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.joy.network.VirtualShortcutInfo;
import com.joy.util.Logger;

public class VirtualShortcutListHandler {

	public List<VirtualShortcutInfo> geShortcutList(JSONObject json) {
		List<VirtualShortcutInfo> list = new ArrayList<VirtualShortcutInfo>();
		try {
			if (json == null || json.getInt("state") != 1) {
				return null;
			}
			JSONArray jsonarry = json.getJSONArray("item");

			int length = jsonarry.length();
			for (int i = 0; i < length; i++) {
				JSONObject item = jsonarry.getJSONObject(i);
				VirtualShortcutInfo info = new VirtualShortcutInfo();
				info.id = item.isNull("id") ? 0 : item.getInt("id");
				info.icon = item.isNull("icon") ? null : item.getString("icon");
				info.softType = item.isNull("type") ? 0 : item.getInt("type");
				info.className = item.isNull("packageName") ? null : item.getString("packageName");
				info.packageName = item.isNull("packageName") ? null : item.getString("packageName");
				info.fileName = item.isNull("name") ? null : item.getString("name");
				info.fileSize = item.isNull("size") ? 0 : item.getInt("size");
				info.url = item.isNull("url") ? null : item.getString("url");
				list.add(info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Logger.warn("VirtualShortcutListHandler", "JSONException e:" + e);
			return null;
		}
		return list;
	}
}