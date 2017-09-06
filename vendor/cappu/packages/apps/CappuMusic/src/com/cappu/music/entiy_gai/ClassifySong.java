package com.cappu.music.entiy_gai;

public class ClassifySong {
	
	private int id;
	private int songid;
	private int songInventoryId;
	private String title;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSongid() {
		return songid;
	}
	public void setSongid(int songid) {
		this.songid = songid;
	}
	public int getSongInventoryId() {
		return songInventoryId;
	}
	public void setSongInventoryId(int songInventoryId) {
		this.songInventoryId = songInventoryId;
	}
	public ClassifySong(int id, int songid, int songInventoryId,String title) {
		super();
		this.id = id;
		this.songid = songid;
		this.songInventoryId = songInventoryId;
		this.title=title;
	}
	public ClassifySong() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	

}
