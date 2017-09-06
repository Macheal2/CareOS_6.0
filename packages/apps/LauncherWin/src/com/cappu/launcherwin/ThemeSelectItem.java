package com.cappu.launcherwin;

import android.content.Context;
import android.graphics.Bitmap;

public class ThemeSelectItem {
	private Bitmap mThemeBmp1;
	private String mThemeTitle;
	private int mCurrentTheme;
	private int ownTheme;
	public ThemeSelectItem(Context context,int currentTheme){
		mCurrentTheme=currentTheme;
	}
	public Bitmap getmThemeBmp1() {
		return mThemeBmp1;
	}
	public void setmThemeBmp1(Bitmap mThemeBmp1) {
		this.mThemeBmp1 = mThemeBmp1;
	}
	public String getmThemeTitle() {
		return mThemeTitle;
	}
	public void setmThemeTitle(String mThemeTitle) {
		this.mThemeTitle = mThemeTitle;
	}
	public boolean isUseTheme() {
		return mCurrentTheme==ownTheme;
	}
	public int getOwnTheme() {
		return ownTheme;
	}
	public void setOwnTheme(int ownTheme) {
		this.ownTheme = ownTheme;
	}
}
