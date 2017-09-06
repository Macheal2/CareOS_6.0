package com.cappu.music.entiy_gai;

import android.provider.MediaStore;

public class Song {

	

	private long id;
	private String title;
	private String album;
	private int duration;
	
	private String artist;
	private String url;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Song(long id, String title, String album, int duration,
			String artist, String url) {
		super();
		this.id = id;
		this.title = title;
		this.album = album;
		this.duration = duration;
		this.artist = artist;
		this.url = url;
	}
	public Song() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
