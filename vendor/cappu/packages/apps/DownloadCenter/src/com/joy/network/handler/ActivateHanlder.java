package com.joy.network.handler;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivateHanlder {

	public boolean isActivate(JSONObject result) {
		int state = 0;
		try {
			state = result.optInt("state");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return state == 1;
	}
}
