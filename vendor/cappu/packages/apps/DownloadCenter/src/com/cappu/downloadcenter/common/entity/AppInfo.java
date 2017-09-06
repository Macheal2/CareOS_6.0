package com.cappu.downloadcenter.common.entity;

import android.graphics.drawable.Drawable;

public class AppInfo {
	private String appName;
	private String packageName;
	private Drawable icon;
	private boolean isFolderContains;

	public boolean isFolderContains() {
		return isFolderContains;
	}

	public void setFolderContains(boolean isFolderContains) {
		this.isFolderContains = isFolderContains;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	@Override
	public String toString() {
		return "appName: " + appName + " | packageName: " + packageName + " | isFolderContains: " + isFolderContains;
	}
}
