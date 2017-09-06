package com.cappu.launcherwin.kookview.assembly;

import android.graphics.Bitmap;

public class ClassRoomInfo {
	private String title;
	private String path;
	private String id;
	private Bitmap imageBmp;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Bitmap getImageBmp() {
		return imageBmp;
	}
	public void setImageBmp(Bitmap imageBmp) {
		this.imageBmp = imageBmp;
	}
}
